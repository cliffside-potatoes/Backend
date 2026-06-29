# Step 0 — 베이스라인 측정 결과

> 측정 일시: 2026-06-28  
> 조건: N+1 미수정, 캐싱 없음, Redis 없음  
> 대상 API: `GET /recipes?category=KOREAN&sort=LIKE_COUNT`  
> 스크립트: `k6/step0-baseline.js` (vus=10, 40s)

---

## k6 결과

| 지표 | 수치 | 설명 |
|---|---|---|
| avg 응답시간 | **97ms** | 507개 요청의 평균. 빠른 요청이 많을수록 낮아져서 단독으로 보기엔 부족함 |
| p(90) 응답시간 | 143ms | 빠른 순으로 90% 지점의 값. "507개 중 90%는 143ms 안에 응답" |
| p(95) 응답시간 | **182ms** | 빠른 순으로 95% 지점의 값. 성능 기준으로 가장 많이 쓰는 지표 |
| max 응답시간 | 402ms | 가장 느렸던 요청 1개. 일시적 요인(GC, 네트워크)으로 튀는 값이라 단독으로는 의미 약함 |
| 총 요청 수 | 507개 | 40초 동안 보낸 총 HTTP 요청 수 |
| 초당 처리량 (RPS) | 12.6/s | 초당 처리한 요청 수. vus=10인데 12.6인 이유는 sleep(0.5)로 한 명이 초당 ~1.3번만 요청하기 때문 |
| 에러율 | 0% | 507개 요청 중 실패 없음 |

> **avg < p(90) < p(95) 는 정상이다.**
> 507개를 빠른 순으로 정렬하면 p(90)은 90% 지점, p(95)는 95% 지점이므로
> 당연히 avg보다 높다. 빠른 요청들이 평균을 낮게 끌어당기기 때문이다.
>
> k6 응답시간은 로컬 PC → 서버 왕복 네트워크 시간을 포함한다.
> 서버 내부 처리 시간만 보려면 Grafana HTTP Statistics를 참고한다.

---

## Grafana 결과

### HTTP Statistics

| API | Mean | Max | Min |
|---|---|---|---|
| `GET /recipes` | **212ms** | **605ms** | 66ms |
| `GET /profiles` | 46ms | 46ms | 46ms |
| `GET /me/ingredients` | 121ms | 121ms | 121ms |
| `POST /oauth/token` | 15ms | 17ms | 14ms |

### HikariCP (DB 커넥션 풀)

| 지표 | Mean | Max | Min |
|---|---|---|---|
| Active (사용 중) | 0.03 | **3** | 0 |
| Idle (대기 중) | 9.97 | 10 | **7** |
| Pending (대기 요청) | — | 0 | — |

> 커넥션 풀(10개) 포화는 발생하지 않았다.
> vus=10이지만 sleep(0.5)로 인해 실제 동시 DB 작업은 최대 3개 수준이었다.
> N+1 영향은 커넥션 포화보다 **응답시간과 CPU**에서 더 명확하게 나타났다.

### CPU / 시스템

| 지표 | Mean | Max | 설명 |
|---|---|---|---|
| System CPU Usage | 0.033 | **0.559** | 서버 전체(OS 레벨) CPU 사용률. 0~1 사이 값 (1.0 = 100%) |
| Process CPU Usage | 0.016 | **0.458** | System CPU 중 Spring Boot 프로세스만 사용한 CPU |
| Load Average [1m] | 0.274 | **2.59** | 1분간 CPU 대기 중인 작업 평균 개수. CPU 코어 수와 비교해서 해석 |
| CPU Core Size | 2 | — | 서버 CPU 코어 수. Load Average 해석의 기준값 |

**Load Average 해석 기준 (코어 2개 서버)**

```
Load Average 1.0  → 1코어가 100% 가동 중 (여유 있음)
Load Average 2.0  → 2코어가 100% 가동 중 (이 서버의 한계)
Load Average 2.59 → 한계를 넘어 작업이 대기 중인 상태
```

> k6 실행 전 0.04 → 피크 2.59 로 급등.
> N+1로 인한 쿼리 폭발이 CPU를 포화시킨 직접적인 증거다.
> System(55.9%)에서 Process(45.8%)를 빼면 나머지는 OS, MySQL 등 다른 프로세스 몫이다.

### JVM GC

| 지표 | Mean | Max | 설명 |
|---|---|---|---|
| GC Count | 0.003 | **0.067** | 단위 시간당 GC 발생 횟수 |
| GC Stop the World | 103µs | **3.20ms** | GC 실행 중 애플리케이션이 완전히 멈춘 시간 |

**GC가 뭔가**

JVM은 더 이상 사용하지 않는 객체를 주기적으로 메모리에서 제거한다. 이 작업이 Garbage Collection(GC)이다.
GC가 실행되는 동안에는 애플리케이션이 완전히 멈춘다 — 이를 **Stop the World**라고 한다.

```
N+1 없을 때: 레시피 20개 처리 → 객체 생성 적음 → GC 부담 낮음
N+1 있을 때: 레시피 20개 × 5회 쿼리 결과 객체 → 100개 분량의 객체 생성/폐기 반복
             → GC 발생 빈도 증가 → Stop the World 중 응답 지연 발생
```

**이번 결과 해석**

- GC Count Max 0.067 → k6 실행 중 GC가 평소보다 집중적으로 발생
- Stop the World Max 3.20ms → GC 중 최대 3.2ms 동안 모든 요청이 멈춤
- max 응답시간 402ms 중 일부는 이 GC pause 때문일 수 있음

---

## 현재 상태 요약

```
쿼리 수 (레시피 20개 기준): 약 101개
서버 응답시간 (Grafana Mean): 212ms
서버 응답시간 (Grafana Max):  605ms
k6 avg:  97ms
k6 p95: 182ms
CPU Max: 0.559 / Load Average Max: 2.59
```

---

## 다음 단계

```
Step 3: N+1 제거 (배치 쿼리) → 쿼리 수 101개 → 5개
         k6/result-2-n1-fixed.md 에 결과 기록

Step 4: 캐싱 적용 (Redis)    → 쿼리 수 5개 → 0개 (히트 시)
         k6/result-3-cached.md 에 결과 기록
```
