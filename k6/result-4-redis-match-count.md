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

## after — Redis Sorted Set

> 측정 일시: 2026-06-30 17:10 (Uptime 22.7분, JIT 워밍업 완료)
> 스크립트: `k6/step4-redis-match-count.js` (vus=10, 40s) → 결과 저장: `k6/result-4-after.txt`

| 지표 | before (DB) | after (Redis) | 개선율 |
|---|---|---|---|
| avg | 252ms | **100ms** | **-60%** |
| p(90) | 320ms | **125ms** | **-61%** |
| p(95) | 376ms | **137ms** | **-64%** |
| max | 717ms | 188ms | -74% |
| RPS | 10.01/s | **12.50/s** | **+25%** |
| 총 요청 수 | 405개 | 507개 | - |
| 에러율 | 0% | 0% | - |

> **Load Average 상승 (0.963 → 2.09)**: Redis 연결 + 냉장고 변경 시 refresh() 계산 비용.  
> 그러나 응답시간은 60% 단축 — DB 쿼리 제거 효과가 시스템 부하 증가를 상회.

### 왜 빨라졌나

```
before: 매 요청마다 10,000개 레시피 × 50,000건 JOIN + GROUP BY + filesort
after:  Redis ZREVRANGE O(log N) — 사전 계산된 순위를 메모리에서 조회

DB 쿼리 수: 매 요청 풀스캔 → 5쿼리 (Redis hit + recipe 조회 2개 + ingredient 조회 2개)
```

---

## Grafana 상세 (after)

### Basic Statistics (a3-1)
| 지표 | 수치 |
|---|---|
| Uptime | 22.7분 |
| CPU Mean/Max | 0.0373 / 0.536 |
| Load Average Mean/Max | 0.276 / 2.09 |
| Heap Used | 14.1% |
| Non-Heap Used | 14.5% |

### JVM GC (a3-2)
| 지표 | 수치 |
|---|---|
| GC Count Max | 0.2/s |
| GC Stop the World Mean/Max | 328μs / 4.53ms |

> before(8.07ms) 대비 GC pause 44% 감소 — 메모리 할당 감소 효과

### HikariCP (a3-3)
| 지표 | 수치 |
|---|---|
| Active Connection Max | **1** (before: 3) |
| Idle | 9~10 |
| Connection Usage Time | ~80ms max |
| Connection Acquire Time | ~600μs |

### HTTP Statistics (a3-4)
| 엔드포인트 | Mean | Max | Min |
|---|---|---|---|
| GET /recipes | **101ms** | 162ms | 73.9ms |

---

## 4단계 누적 비교

> k6 avg/p(95)/RPS — 동일 스크립트(vus=10, 40s), 워밍업 완료 기준 수치

| 지표 | Step 0 원본 | Step 1 like_count | Step 2 N+1 제거 | Step 3 Redis |
|---|---|---|---|---|
| avg | 664ms | 333ms | 101ms | **100ms** |
| p(95) | 943ms | 585ms | 161ms | **137ms** |
| RPS | 6.57/s | 9.06/s | 12.46/s | **12.50/s** |
| Active Connection Max | - | 2 | 2 | **1** |
| Grafana HTTP Mean | - | - | - | 101ms |

> Step 1~2는 `GET /recipes?sort=LIKE_COUNT` 기준.  
> Step 3은 `sort=MATCH_COUNT` 기준 (다른 정렬이지만 더 복잡한 쿼리였고, Redis 후 동급 성능).

---

## 스크린샷 목록

### Before (step4-before-match-count/)
| 파일 | 내용 |
|---|---|
| `b2-1.jpg` | Basic Statistics (CPU, Load Average, Uptime) |
| `b2-2.jpg` | JVM GC (GC Count, Stop the World) |
| `b2-3.jpg` | HikariCP (Active Connection, Usage Time) |
| `b2-4.jpg` | HTTP Statistics (응답시간, RPS) |

### After (step4-after-match-count/)
| 파일 | 내용 |
|---|---|
| `a3-1.jpg` | Basic Statistics (CPU 0.536 max, Load Average 2.09 max) |
| `a3-2.jpg` | JVM GC (GC Stop the World 4.53ms max) |
| `a3-3.jpg` | HikariCP (Active Connection max 1) |
| `a3-4.jpg` | HTTP Statistics (GET /recipes Mean 101ms, Max 162ms) |
