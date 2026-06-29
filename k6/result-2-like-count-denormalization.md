# Step 1 — like_count 비정규화 적용 결과

> 측정 일시: 2026-06-29
> 조건: like_count 비정규화 적용 (feat-#56), N+1 미수정, 캐싱 없음
> 대상 API: `GET /recipes?sort=LIKE_COUNT`
> 스크립트: `k6/step0-baseline.js` (vus=10, 40s)

---

## k6 결과 (워밍업 후 2회차 기준)

| 지표 | 수치 |
|---|---|
| avg 응답시간 | **333ms** |
| p(90) 응답시간 | 474ms |
| p(95) 응답시간 | **585ms** |
| max 응답시간 | 774ms |
| 총 요청 수 | 367개 |
| 초당 처리량 (RPS) | 9.06/s |
| 에러율 | 0% |

> 1회차(콜드 스타트 6.5분)는 JVM JIT 워밍업 미완료로 이상치 — 2회차를 유효 측정값으로 사용

---

## Before / After 전체 비교

### k6

| 지표 | Before | After 1회 (콜드) | After 2회 (워밍업) | 변화 |
|---|---|---|---|---|
| avg | 664ms | 522ms | **333ms** | **▼ 50% 개선** |
| p(90) | 839ms | 613ms | **474ms** | **▼ 43% 개선** |
| p(95) | 943ms | 890ms | **585ms** | **▼ 38% 개선** |
| 처리량 | 6.57/s | 7.45/s | **9.06/s** | **▲ 38% 증가** |

### Grafana HTTP Statistics — GET /recipes

| 지표 | Before | After 1회 (콜드) | After 2회 (워밍업) | 변화 |
|---|---|---|---|---|
| Mean | 360ms | 397ms | **292ms** | **▼ 19% 개선** |
| Max | 780ms | 601ms | **361ms** | **▼ 54% 개선** |

### Grafana CPU / Load Average

| 지표 | Before | After 1회 | After 2회 | 변화 |
|---|---|---|---|---|
| System CPU Max | 0.605 | 0.820 | **0.497** | ▼ 개선 |
| Load Average Max | **2.27** | 1.50 | **1.17** | **▼ 48% 개선** |

> Load Average 2.27 → 1.17: 코어 2개 서버 기준 포화 상태(>2.0) 완전 해소

### Grafana HikariCP Active Max

| Before | After 1회 | After 2회 |
|---|---|---|
| **10 (커넥션 풀 포화)** | 4 | 6 |

### Grafana JVM GC Stop the World Max

| Before | After 1회 | After 2회 |
|---|---|---|
| 4.53ms | 5.27ms | **4ms** |

---

## 측정 참고 사항

- After 1회: 서버 Uptime 6.5분 상태에서 측정 → JIT 미워밍업으로 일부 지표 이상치 발생
- After 2회: 1회 종료 직후 동일 서버(Uptime 19.5분)에서 재측정 → JIT 워밍업 완료 상태
- **2회차를 공식 After 수치로 사용**

---

## 적용된 변경 사항

- `Recipe.java`: `like_count` 컬럼 + `(like_count DESC, id DESC)` 인덱스 추가
- `RecipeRepository.java`: `GROUP BY + COUNT(pfr)` → `ORDER BY r.likeCount` 로 전환 (쿼리 6개)
- `RecipeFavoriteService.java`: 좋아요/취소 시 `like_count` 실시간 동기화
- `RecipeQueryService.java`: 커서 페이징 likeCount를 `countByRecipe()` 대신 `getLikeCount()`로 통일
- DB 마이그레이션: `migrate-like-count.sql` 실행 (기존 데이터 백필 완료)

---

## EXPLAIN 변화 (실측)

**변경 전** — `GROUP BY + COUNT(pfr) ORDER BY like_count DESC`
```
+----+-------------+-------+-------+-------------------------------------+-----------------------------+---------+--------------+------+----------+---------------------------------+
| id | select_type | table | type  | possible_keys                       | key                         | key_len | ref          | rows | filtered | Extra                           |
+----+-------------+-------+-------+-------------------------------------+-----------------------------+---------+--------------+------+----------+---------------------------------+
|  1 | SIMPLE      | r     | index | PRIMARY,UK26qul6xdajf5eabsovqawx18j | PRIMARY                     | 8       | NULL         | 9906 |   100.00 | Using temporary; Using filesort |
|  1 | SIMPLE      | pfr   | ref   | FKfphisd49a4hrmjxispvoy8le9         | FKfphisd49a4hrmjxispvoy8le9 | 8       | naenggu.r.id |    3 |   100.00 | Using index                     |
+----+-------------+-------+-------+-------------------------------------+-----------------------------+---------+--------------+------+----------+---------------------------------+
```

**변경 후** — `ORDER BY r.like_count DESC, r.id DESC`
```
+----+-------------+-------+------------+-------+---------------+-----------------------+---------+------+------+----------+-------+
| id | select_type | table | partitions | type  | possible_keys | key                   | key_len | ref  | rows | filtered | Extra |
+----+-------------+-------+------------+-------+---------------+-----------------------+---------+------+------+----------+-------+
|  1 | SIMPLE      | r     | NULL       | index | NULL          | idx_recipe_like_count | 12      | NULL |   21 |   100.00 | NULL  |
+----+-------------+-------+------------+-------+---------------+-----------------------+---------+------+------+----------+-------+
```

| 항목 | 변경 전 | 변경 후 |
|---|---|---|
| 참조 테이블 수 | 2개 (recipe + profile_favorite_recipe) | **1개 (recipe)** |
| key | PRIMARY | **idx_recipe_like_count** |
| rows | **9,906** | **21** |
| Extra | **Using temporary; Using filesort** | **(없음)** |

---

## 다음 단계

```
Step 2: N+1 배치 쿼리 적용 (계획 43)
        toItemResponse 루프 내 쿼리 4개 → IN절 배치 쿼리
        k6/result-3-n1-batch-query.md 에 결과 기록
```
