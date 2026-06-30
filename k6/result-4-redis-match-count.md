# Step 3 — Redis Sorted Set match_count 최적화 결과

> 측정 일시: 2026-06-30
> 조건: like_count 비정규화(Step 1) + N+1 배치 쿼리(Step 2) 적용 상태
> 대상 API: `GET /recipes?sort=MATCH_COUNT`
> 스크립트: `k6/step4-redis-match-count.js` (vus=10, 40s)
> 실험 환경: 더미 레시피 10,000개 + recipe_ingredient 50,000건 + 테스트 냉장고 재료 20개

---

## before — DB 풀스캔 (Redis 적용 전)

| 지표 | 1회차 (콜드) | 2회차 (워밍업) |
|---|---|---|
| avg 응답시간 | 326ms | **252ms** |
| p(90) 응답시간 | 478ms | **320ms** |
| p(95) 응답시간 | 611ms | **376ms** |
| max 응답시간 | 1,410ms | 717ms |
| 총 요청 수 | 371개 | 405개 |
| RPS | 9.19/s | **10.01/s** |
| 에러율 | 0% | 0% |

**2회차를 공식 수치로 사용**

### 왜 느린가

```sql
SELECT r.* FROM recipe                        -- 10,000개 전체 스캔
LEFT JOIN recipe_ingredient ri                 -- 50,000행 JOIN
  ON ri.ingredient_id IN (내 냉장고 재료 20개)
GROUP BY r.id                                  -- 매 요청마다 임시 테이블
ORDER BY COUNT(ri.id) DESC                     -- 매 요청마다 filesort
LIMIT 21
```

- 레시피 수에 비례하는 O(N) 작업이 매 요청마다 반복됨
- 사용자마다 `fridgeIngredientIds`가 달라 결과 캐싱 불가

---

## after — Redis Sorted Set (배포 후 작성 예정)

| 지표 | before (DB) | after (Redis) | 개선율 |
|---|---|---|---|
| avg | 252ms | - | - |
| p(95) | 376ms | - | - |
| RPS | 10.01/s | - | - |

---

## 4단계 누적 비교 (after 측정 후 작성)

| 지표 | Step 0 원본 | Step 1 like_count | Step 2 N+1 제거 | Step 3 Redis |
|---|---|---|---|---|
| avg | 664ms | 333ms | 101ms | - |
| p(95) | 943ms | 585ms | 161ms | - |
| RPS | 6.57/s | 9.06/s | 12.46/s | - |
