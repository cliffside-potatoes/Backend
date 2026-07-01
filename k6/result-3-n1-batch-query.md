# Step 2 — N+1 배치 쿼리 적용 결과

> 측정 일시: 2026-06-29
> 조건: like_count 비정규화(Step 1) + N+1 배치 쿼리 적용
> 대상 API: `GET /recipes?sort=LIKE_COUNT`
> 스크립트: `k6/step0-baseline.js` (vus=10, 40s)

---

## k6 결과 (워밍업 후 2회차 기준)

| 지표 | 수치 |
|---|---|
| avg 응답시간 | **101ms** |
| p(90) 응답시간 | 136ms |
| p(95) 응답시간 | **161ms** |
| max 응답시간 | 360ms |
| 총 요청 수 | 503개 |
| 초당 처리량 (RPS) | 12.46/s |
| 에러율 | 0% |

---

## 3단계 누적 비교

### k6

| 지표 | Step 0 원본 | Step 1 like_count | Step 2 N+1 제거 | 누적 변화 |
|---|---|---|---|---|
| avg | 664ms | 333ms | **101ms** | **▼ 85%** |
| p(90) | 839ms | 474ms | **136ms** | **▼ 84%** |
| p(95) | 943ms | 585ms | **161ms** | **▼ 83%** |
| max | - | 774ms | **360ms** | - |
| RPS | 6.57/s | 9.06/s | **12.46/s** | **▲ 90%** |

### Grafana HTTP Statistics — GET /recipes

| 지표 | Step 0 원본 | Step 1 like_count | Step 2 N+1 제거 | 누적 변화 |
|---|---|---|---|---|
| Mean | 360ms | 292ms | **112ms** | **▼ 69%** |
| Max | 780ms | 361ms | **186ms** | **▼ 76%** |

### Grafana HikariCP Active Max

| Step 0 원본 | Step 1 like_count | Step 2 N+1 제거 |
|---|---|---|
| **10 (풀 포화)** | 6 | **2** |

> HikariCP Active Max 10 → 2: 쿼리 수 81개 → 5개로 감소로 커넥션 점유 시간 급감

### Grafana Load Average Max

| Step 0 원본 | Step 1 like_count | Step 2 N+1 제거 |
|---|---|---|
| 2.27 | 1.17 | **2.21** |

> Step 2에서 Load Average가 소폭 상승한 이유:
> RPS가 9.06 → 12.46으로 37% 증가해 서버가 더 많은 요청을 처리했기 때문.
> 개별 응답시간은 크게 줄었으나 총 처리량 증가로 CPU 부하는 유사.

---

## 측정 참고 사항

- 서버 Uptime 22.7분 상태에서 측정 (JIT 워밍업 완료)
- 1회차(콜드): avg 179ms, p95 415ms, RPS 11.11/s
- 2회차(워밍업): avg 101ms, p95 161ms, RPS 12.46/s
- **2회차를 공식 수치로 사용**

---

## 적용된 변경 사항

- `RecipeIngredientRepository`: `countByRecipeIds`, `countMatchedByRecipeIds` 추가
- `RecipeReviewRepository`: `countByRecipeIds` 추가
- `ProfileFavoriteRecipeRepository`: `findLikedRecipeIds` 추가
- `RecipeQueryService`: 헬퍼 메서드 4개 추가 + `toItemResponse` 시그니처 변경 + 호출부 7곳 일괄 수정

---

## 쿼리 수 변화

| | 쿼리 수 (레시피 20개 기준) |
|---|---|
| Step 0 원본 | 1 + 20×4 = **81개** |
| Step 1 like_count | 1 + 20×4 = **81개** (정렬 쿼리만 개선) |
| Step 2 N+1 제거 | 1 + 4 = **5개** |
