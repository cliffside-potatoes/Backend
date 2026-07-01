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

## k6 After

> 코드 변경 + ALTER TABLE 후 측정 예정

| 지표 | Before (LIKE) | After (FULLTEXT) | 개선율 |
|---|---|---|---|
| avg | 164ms | **?ms** | **?%** |
| p(95) | 229ms | **?ms** | **?%** |
| RPS | 36.79/s | **?/s** | **?%** |
| Active Connection Max | 7 | **?** | **?** |

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
| `a4-1.jpg` | Basic Statistics |
| `a4-2.jpg` | JVM GC |
| `a4-3.jpg` | HikariCP (Active Connection after) ★PPT 핵심 |
| `a4-4.jpg` | HTTP Statistics (GET /recipes after) ★PPT 핵심 |
