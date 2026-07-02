# Step 6 — 읽기+쓰기 혼합 스트레스 테스트 결과 (읽기 90% / 쓰기 10%)

> 측정 일시: 2026-07-02
> 목적: `result-6-stress-before.md`(읽기 전용)는 "커넥션 풀이 찼다"까지만 보여줬기 때문에,
> 실제 서비스 비율(읽기 90% / 쓰기 10%)로 섞어서 쓰기 요청도 함께 지연/실패하는지 확인
> 스크립트: `k6/step6-stress-mixed.js` (동일 부하 프로필: 0→50→100 VU, 총 2분)
> 대상: 읽기 3종(`/recipes`, `/me/feed`, `/me/ingredients`) + 쓰기(`POST/DELETE /recipes/{id}/favorites`)

---

## k6 결과 (2차 실행 기준)

| 지표 | 수치 |
|---|---|
| 총 요청 수 | 9,354건 |
| 읽기 체크 | 100% 통과 |
| 쓰기 체크(`200/201`) | 93.9% 통과 (820 / 873) |
| **쓰기 실패** | **53건 (6.07%)** — 전부 500 |
| http_req_failed(전체) | 0.56% |
| avg | 749ms |
| p(95) | 1.68s |

읽기는 이번에도 100% 정상. 실패는 전부 **쓰기 �361 요청 중 일부**에서만 발생했고, 상태 코드는 전부 500.

---

## 원인 조사 — Grafana Loki

`{container="naenggu_backend_test"} |= "com.potatoes.Naengu"` 쿼리로 스택트레이스를 추적해서 **두 가지 서로 다른 버그**를 확인했다.

### 버그 1 — `DATABASE_INCONSISTENCY` (createFavorite, 동시 INSERT)

```java
// RecipeFavoriteService.createFavorite()
boolean exists = profileFavoriteRecipeRepository.existsByProfileAndRecipe(profile, recipe);
if (exists) return;          // ← 동시에 여러 요청이 여기를 통과
...
profileFavoriteRecipeRepository.save(favorite);   // ← 두 번째 INSERT가 유니크 제약 위반
```

- 유니크 제약: `uk_profile_favorite_recipe_profile_id_recipe_id (profile_id, recipe_id)`
- `GlobalExceptionHandler.CONSTRAINT_ERROR_CODES`에 이 제약 이름이 등록돼 있지 않아 `DataIntegrityViolationException`이 일반 `DATABASE_INCONSISTENCY`(500)로 처리됨

### 버그 2 — `INTERNAL_ERROR` (deleteFavorite, 동시 DELETE) ★ 로그로 확정

```
org.springframework.orm.ObjectOptimisticLockingFailureException:
Unexpected row count (expected row count 1 but was 0)
[delete from profile_favorite_recipe where id=?]
for entity [com.potatoes.Naengu.recipe.domain.model.ProfileFavoriteRecipe with id '30114']

Caused by: org.hibernate.StaleObjectStateException: ...

at RecipeFavoriteService$$SpringCGLIB$$0.deleteFavorite(<generated>)
at RecipeFavoriteController.delete(RecipeFavoriteController.java:97)
```

- `deleteFavorite()`도 find-then-act 구조: `findByProfileAndRecipe(...).ifPresent(fav -> delete(fav))`
- 두 요청이 동시에 같은 row를 찾아온 뒤, 먼저 커밋된 쪽이 row를 지움 → 나중 트랜잭션이 flush할 때 영향받은 row가 0개 → Hibernate가 `StaleObjectStateException` 발생 → `GlobalExceptionHandler`의 일반 핸들러(`handleUnexpected`)로 떨어져 500

---

## 결론

두 버그 모두 **같은 근본 원인**: `createFavorite`/`deleteFavorite`가 "확인 후 실행(check-then-act)"을 원자적으로 처리하지 않아서 생기는 경쟁 상태(race condition). 100 VU가 같은 토큰(같은 유저)으로 같은 레시피를 동시에 찜/해제하면서 재현됨.

**DB 이중화(Read Replica)와는 무관한 별개의 버그**다 — 커넥션 풀이나 Master/Slave 라우팅으로 해결되는 문제가 아니라, 애플리케이션 코드의 동시성 처리 미흡이 원인. `result-6-stress-before.md`(읽기 전용, 순수 커넥션 풀 포화 증거)의 결론과는 분리해서 다룬다.

**다음 조치**: 구조화 로깅(JSON + traceId) 도입 후 → 이 동시성 버그 수정 → DB 이중화 재개. 자세한 순서는 `.claude/plans/53-post-replica-next-steps.md` 참고.

---

## 디버깅 과정에서 얻은 교훈

- Promtail에 멀티라인(multiline) stage가 없어서, `log.error(msg, exception)`으로 찍힌 스택트레이스가 Loki에서 한 줄씩 별개 로그로 흩어짐 → `INTERNAL_ERROR` 문자열로 필터링해도 정작 원인 줄은 안 걸림
- 요청 단위로 로그를 묶어주는 traceId(MDC)가 없어서, 시간대만 보고 눈으로 스택트레이스를 찾아야 했음
- 최종적으로 `|= "com.potatoes.Naengu"` (우리 코드 스택 프레임만 필터링)로 우회해서 찾음 — 근본 해결책은 아니고, 구조화 로깅 도입이 필요함
