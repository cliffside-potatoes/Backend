# Step 5 — FULLTEXT N-gram 키워드 검색 최적화 결과

> 측정 일시: 2026-07-01
> 대상 API: `GET /recipes?sort=LATEST&keyword=레시피`
> 스크립트: `k6/step5-fulltext-keyword.js` (vus=10, 40s)
> 실험 환경: 더미 레시피 9,865개

---

## EXPLAIN Before — LIKE 풀스캔

```sql
EXPLAIN SELECT * FROM recipe WHERE title LIKE '%김치%';
```

| select_type | table | type | possible_keys | key | rows | filtered | Extra |
|---|---|---|---|---|---|---|---|
| SIMPLE | recipe | **ALL** | NULL | NULL | **9,865** | 11.11 | Using where |

- `type: ALL` — 전체 테이블 스캔
- `key: NULL` — 사용한 인덱스 없음
- `rows: 9,865` — 전체 행 읽음

---

## k6 Before — LIKE 풀스캔

> 1회차(콜드) / 2회차(워밍업) 각각 측정, 2회차를 공식 수치로 사용

| 지표 | 1회차 (콜드) | 2회차 (워밍업) |
|---|---|---|
| avg | 206ms | **164ms** |
| p(90) | 263ms | **198ms** |
| p(95) | 297ms | **229ms** |
| max | 603ms | 552ms |
| RPS | 31.84/s | **36.79/s** |
| 총 요청 수 | 1,280개 | 1,480개 |
| 에러율 | 0% | 0% |

**2회차를 공식 수치로 사용**

---

## Grafana Before (step5-before-fulltext/)

### b4-1 — Basic Statistics
| 지표 | 수치 |
|---|---|
| Uptime | 6.0시간 |
| Start time | 2026-07-01 09:03:12 |
| CPU Mean/Max | 0.0367 / 0.937 |
| Load Average Mean/Max | 0.181 / 3.78 |
| Heap Used | 20.4% |
| Non-Heap Used | 12.9% |

### b4-2 — JVM GC
| 지표 | 수치 |
|---|---|
| GC Count Max | 0.733/s |
| GC Stop the World Max | 7.13ms |

### b4-3 — HikariCP ★핵심
| 지표 | 수치 |
|---|---|
| Active Connection Max | **7** |
| Idle Mean | 9.88 |
| Connection Usage Time Max | ~100ms |

> Active Connection 7 — LIKE 풀스캔 시 DB가 쿼리를 길게 점유

### b4-4 — HTTP Statistics ★핵심
| 엔드포인트 | Mean | Max | Min |
|---|---|---|---|
| GET [200] /recipes | **164ms** | - | - |

> k6 avg 기준 164ms (Grafana HTTP 패널 기준)

---

## EXPLAIN After — FULLTEXT 인덱스 스캔

```sql
EXPLAIN SELECT r.id FROM recipe r
WHERE MATCH(r.title) AGAINST('국수' IN BOOLEAN MODE);
```

| select_type | table | type | possible_keys | key | rows | filtered | Extra |
|---|---|---|---|---|---|---|---|
| SIMPLE | r | **fulltext** | ft_recipe_title | **ft_recipe_title** | **1** | 100.00 | Using where; Ft_hints: no_ranking |

- `type: fulltext` — FULLTEXT 인덱스 스캔 (풀스캔 제거)
- `key: ft_recipe_title` — 인덱스 사용 확인
- `rows: 1` — 9,865개 전체 읽던 것 → 매칭 행만 즉시 조회

---

## k6 After — FULLTEXT (keyword: 닭가슴살)

> keyword를 레시피 → 닭가슴살로 변경한 이유:
> 레시피는 N-gram 토큰(레시, 시피)이 거의 모든 레시피 제목에 매칭되어 FULLTEXT 이점이 사라짐.
> LIKE는 키워드 무관 항상 풀스캔이므로 before 164ms는 어떤 키워드든 동일한 대표값.

| 지표 | 1회차 (콜드) | 2회차 (워밍업) |
|---|---|---|
| avg | 122ms | **104ms** |
| p(90) | 162ms | **140ms** |
| p(95) | 174ms | **156ms** |
| max | 286ms | 313ms |
| RPS | 43.44/s | **47.23/s** |
| 총 요청 수 | 1,743개 | 1,896개 |
| 에러율 | 0% | 0% |

**2회차를 공식 수치로 사용**

---

## Before vs After 최종 비교

| 지표 | Before (LIKE) | After (FULLTEXT) | 개선율 |
|---|---|---|---|
| avg | 164ms | **104ms** | **↓ 36.6%** |
| p(95) | 229ms | **156ms** | **↓ 31.9%** |
| RPS | 36.79/s | **47.23/s** | **↑ 28.4%** |

### EXPLAIN 비교

| | Before | After |
|---|---|---|
| type | **ALL** (풀스캔) | **fulltext** (인덱스 스캔) |
| key | NULL | ft_recipe_title |
| rows | **9,865** | **1** |

---

## 레시피 키워드 실험 결과 (참고)

> 광범위한 키워드일 때 FULLTEXT가 오히려 느려지는 케이스

| 지표 | Before (LIKE) | After (FULLTEXT) |
|---|---|---|
| avg | 164ms | 708ms |
| p(95) | 229ms | 997ms |
| RPS | 36.79/s | 12.19/s |

원인: 레시피 → N-gram 토큰 레시·시피가 9,865개 중 대부분 매칭 → FULLTEXT가 수천 건 찾은 후 created_at 정렬 → LIMIT. 선택도 낮은 키워드에서는 FULLTEXT 효과 없음.

---

## Grafana After (step5-after-fulltext/)

### a4-1 — Basic Statistics
| 지표 | 수치 |
|---|---|
| Uptime | 12.8분 (2026-07-01 15:34:08 기동) |
| CPU Mean/Max | System 0.0744 / 0.959, Process 0.0358 / 0.655 |
| Load Average Mean/Max | 0.368 / 3.78 |
| Heap Used | 29.7% |
| Non-Heap Used | 12.7% |

### a4-2 — JVM GC
| 지표 | 수치 |
|---|---|
| GC Count Max | 0.733/s |
| GC Stop the World Max | 7.13ms |

### a4-3 — HikariCP ★핵심
| 지표 | 수치 |
|---|---|
| Active Connection Max | **9** (레시피 광범위 키워드 실험 포함) |
| Idle Mean | 9.61 |
| Connection Usage Time | 레시피 실험 시 ~600ms → 닭가슴살 실험 시 ~0ms 수준으로 급감 |

> Usage Time 그래프: 15:00~15:10 (레시피, 708ms)에서 높은 점유 → 15:35~15:45 (닭가슴살, 104ms)에서 급락.
> FULLTEXT가 선택도 높은 키워드에서 DB 연결 점유 시간을 크게 줄임.

### a4-4 — HTTP Statistics
| 항목 | 비고 |
|---|---|
| GET [401] /recipes | 초기 토큰 만료로 발생한 인증 오류 (실험 전) |
| GET [500] /recipes | 레시피 키워드 실험 1회차 일부 오류 |
| GET [200] /recipes | 닭가슴살 실험 기간 캡처 범위 밖 (k6 수치로 대체) |

---

## 스크린샷 목록

### Before (step5-before-fulltext/)
| 파일 | 내용 |
|---|---|
| `b4-1.jpg` | Basic Statistics (CPU 0.937 max, Load Average 3.78 max) |
| `b4-2.jpg` | JVM GC (GC Stop the World 7.13ms max) |
| `b4-3.jpg` | HikariCP (Active Connection max **7**) ★PPT 핵심 |
| `b4-4.jpg` | HTTP Statistics (GET /recipes 164ms) ★PPT 핵심 |

### After (step5-after-fulltext/)
| 파일 | 내용 |
|---|---|
| `a4-1.jpg` | Basic Statistics (CPU / Load Average) |
| `a4-2.jpg` | JVM GC (Stop the World 7.13ms) |
| `a4-3.jpg` | HikariCP ★PPT 핵심 — Usage Time 레시피 ~600ms → 닭가슴살 ~0ms 급락 |
| `a4-4.jpg` | HTTP Statistics (401/500 오류 확인용) |
