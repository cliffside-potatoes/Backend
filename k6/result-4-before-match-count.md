# Step 3 Before — match_count DB 풀스캔 베이스라인

> 측정 일시: 2026-06-30
> 서버 Uptime: 7.4시간 (JIT 워밍업 충분)
> 대상 API: `GET /recipes?sort=MATCH_COUNT`
> 실험 환경: 더미 레시피 10,000개 + recipe_ingredient 50,000건 + 테스트 냉장고 재료 20개

---

## k6 결과 (2회차 기준)

| 지표 | 1회차 (콜드) | **2회차 (워밍업 — 공식 수치)** |
|---|---|---|
| avg | 326ms | **252ms** |
| p(90) | 478ms | **320ms** |
| p(95) | 611ms | **376ms** |
| max | 1,410ms | 717ms |
| RPS | 9.19/s | **10.01/s** |
| 에러율 | 0% | 0% |

---

## Grafana 지표 (2회차 기준)

### Basic Statistics (b2-1)
| 지표 | 수치 |
|---|---|
| Uptime | 7.4시간 |
| CPU Max | 0.728 |
| Load Average Max | **0.963** |

### JVM GC (b2-2)
| 지표 | 수치 |
|---|---|
| GC Count Max | 0.267/s |
| GC Stop the World Max | 8.07ms |

### HikariCP (b2-3)
| 지표 | 수치 |
|---|---|
| Active Connection Max | **3** |
| Connection Usage Time | ~100ms |
| Connection Acquire Time | ~700μs |

> Step 2 (N+1 제거, LIKE_COUNT 기준) Active Max가 2였는데
> MATCH_COUNT는 GROUP BY + 집계 시간이 길어 커넥션 점유 시간이 늘어남

### HTTP Statistics (b2-4)
| 엔드포인트 | Mean | Max |
|---|---|---|
| GET /recipes | **215ms** | 397ms |

---

## 왜 느린가

```sql
SELECT r.* FROM recipe                        -- 10,000개 전체 스캔
LEFT JOIN recipe_ingredient ri
  ON ri.ingredient_id IN (내 냉장고 재료 20개) -- 50,000행 JOIN
GROUP BY r.id                                  -- 매 요청마다 임시 테이블
ORDER BY COUNT(ri.id) DESC                     -- 매 요청마다 filesort
LIMIT 21
```

- 사용자마다 `fridgeIngredientIds`가 달라 결과 공유 불가
- like_count처럼 비정규화(컬럼 저장) 불가 — 사용자마다 값이 다름
- 레시피가 늘수록 O(N) 비례해서 느려짐

---

## 다음 단계 (After 측정을 위한 체크리스트)

- [ ] 서버 docker-compose.yml에 Redis 컨테이너 추가
  ```yaml
  redis:
    image: redis:7-alpine
    container_name: naenggu_redis
    restart: always
    mem_limit: 128m
    command: redis-server --maxmemory 100mb --maxmemory-policy allkeys-lru
    ports:
      - "6379:6379"
    networks:
      - naenggu_net
    volumes:
      - redis_data:/data
  ```
- [ ] backend-test environment에 `REDIS_HOST: redis`, `REDIS_PORT: 6379` 추가
- [ ] `sudo docker compose up -d redis` 실행
- [ ] 새 코드 배포 (Redis Sorted Set 브랜치)
- [ ] Swagger에서 냉장고 재료 1개 추가/수정 → `refresh()` 호출 → Redis 키 생성 확인
  ```bash
  docker exec -it naenggu_redis redis-cli KEYS match:ranking:*
  ```
- [ ] after k6 측정 (서버 Uptime 20분 이상 확인 후)
  ```bash
  k6 run -e TOKEN=<토큰> k6/step4-redis-match-count.js 2>&1 | tee k6/result-4-after.txt
  ```
- [ ] Grafana 스크린샷 저장 (a2-1 ~ a2-4 형태)
- [ ] `result-4-redis-match-count.md` after 수치 채우기

---

## 스크린샷 목록

| 파일 | 내용 |
|---|---|
| `k6/screenshots/b2-1.jpg` | Basic Statistics (CPU, Load Average, Uptime) |
| `k6/screenshots/b2-2.jpg` | JVM GC (GC Count, Stop the World) |
| `k6/screenshots/b2-3.jpg` | HikariCP (Active Connection, Usage Time) |
| `k6/screenshots/b2-4.jpg` | HTTP Statistics (응답시간, RPS) |
