# Step 6 — DB 이중화 전 고부하 스트레스 테스트 결과

> 측정 일시: 2026-07-02
> 목적: Read Replica 라우팅 코드 배포 **전**, 단일 DB(Master만 존재)가 고부하에서 어디까지 버티는지 기록
> 대상 API: `GET /recipes?sort=LATEST`, `GET /me/feed`, `GET /me/ingredients` (랜덤 혼합)
> 스크립트: `k6/step6-stress-before.js` (stages: 0→50(30s)→100(30s)→100 유지(40s)→0(20s), 총 2분)
> 실험 환경: AWS RDS `naenggu-db`(Master) 단독 운영 — `slave-naenggu-db`는 생성만 됐고 애플리케이션은 아직 라우팅하지 않음(100% Master로만 트래픽 감)

---

## 배경 — 왜 지금 측정했나

- 기존 baseline(step0~step5)은 전부 10 VUs 기준이라 "단일 DB로 트래픽 감당 불가"를 증명하기엔 부하가 낮음
- Replica 라우팅을 배포하면 이후엔 읽기 트래픽이 Slave로 분산되기 때문에, "Master 한 대로 모든 트래픽을 받는" 상태의 고부하 수치는 지금이 아니면 다시 재현하기 번거로움
- Read Replica는 이미 AWS 콘솔에서 생성 완료(`slave-naenggu-db`, 사용 가능 상태)했지만, 이번 측정 시점엔 애플리케이션 코드(Step 2~6: DataSource 설정/라우팅)를 아직 배포하지 않아 실질적으로 순수 단일 DB 상태와 동일함 — Replica 존재 자체가 Master의 binlog 전송 부담을 아주 조금 늘릴 순 있으나 무시할 수준

---

## k6 결과 (100 VUs 고부하)

| 지표 | 수치 |
|---|---|
| 총 요청 수 | 6,264건 (0 interrupted) |
| 성공률(checks) | 100.00% (6264/6264) |
| 에러율(http_req_failed) | **0.00%** |
| RPS | 51.96/s |
| avg | **1.16s** |
| median | 928.08ms |
| min | 45.73ms |
| p(90) | 2.24s |
| p(95) | **2.91s** |
| max | 7.14s |
| max VUs | 100 |

**해석**: 에러 없이 전부 200을 응답했지만, 응답 시간이 baseline(10 VUs, 수백 ms대) 대비 크게 늘어남 — "죽지는 않지만 확실히 버거워한다"는 상태.

---

## Grafana — 애플리케이션/DB 지표 (`step6-stress-before/`)

### b5-1 — Basic Statistics / CPU / Load Average
| 지표 | 수치 |
|---|---|
| Uptime | 2.0시간 (Start time 2026-07-02 09:03:30) |
| Heap Used | 26.3% |
| Non-Heap Used | 13.1% |
| Process Open Files Max | ~110 (테스트 구간에서 급증) |
| System CPU Usage Mean/Max | 0.0749 / **0.991** |
| Load Average Mean/Max | 0.651 / **7.93** (CPU 코어 2개 기준 → 코어 수의 약 4배) |

### b5-2 — JVM GC
| 지표 | 수치 |
|---|---|
| GC Count Max | 0.733/s |
| GC Stop the World Duration Max | 8.67ms |

> 참고 지표. 병목의 주원인은 아님.

### b5-3 — HikariCP ★핵심 증거
| 지표 | 수치 |
|---|---|
| Pool Size | 10 |
| Active Connections Max | **10** (풀 최대치에 도달) |
| Idle Mean | 9.44 |
| **Pending(대기) Max** | **78** |
| Connection Creation Time | ~32~34ms |
| Connection Usage Time | ~60~90ms |
| **Connection Acquire Time Max** | **~400ms** |

> **가장 결정적인 지표.** DB 자체가 느려서가 아니라, 커넥션 풀(10개)이 100 VU를 감당 못 해 최대 78개 요청이 커넥션을 못 받고 대기했다. 응답 시간 증가의 직접 원인.

### b5-4 — HTTP Statistics ★핵심 증거
| 엔드포인트 | Mean | Max | Min |
|---|---|---|---|
| GET /recipes | **861ms** | 1.39s | 205ms |
| GET /me/ingredients | 731ms | 1.29s | 117ms |
| GET /me/feed | 712ms | 1.19s | 114ms |
| GET /profiles | 301ms | 301ms | 301ms |
| POST /oauth/token | 93.9ms | 178ms | 9.59ms |

> 서버 내부 관측(Grafana) 응답시간이 k6(클라이언트 관측, avg 1.16s / p95 2.91s)와 같은 시점·같은 방향으로 튐 → 두 소스가 서로 검증됨.

### b5-5 — AWS RDS CloudWatch (`naenggu-db`)
> ⚠️ 최초 캡처 시 그래프 x축이 UTC로 표시돼 "새벽 2시"로 오인 → AWS 콘솔 Time zone을 Local(KST)로 변경 후 재캡처하여 아래 수치로 확정

| 지표 | 수치 |
|---|---|
| CPUUtilization | 테스트 구간(10:55~11:00) 동안 뚜렷한 스파이크 없음 — 4~5% 유지, 종료 시점에 6%로 소폭 상승 |
| DatabaseConnections | 21.07 근처로 거의 변화 없음 |

> **DB 서버 자체는 여유 있었다.** CloudWatch 표준 모니터링이 5분 단위 집계라 2분짜리 버스트가 뭉개진 영향도 있지만, 더 근본적인 이유는 HikariCP 풀이 10개로 제한돼 있어 VU를 100까지 올려도 **DB에 실제로 도달하는 동시 쿼리는 항상 10개를 넘지 못했기** 때문. 즉 이번 테스트에서 실제로 포화된 자원은 DB 컴퓨팅이 아니라 **애플리케이션 커넥션 풀**이었다.

---

## 결론 — Read Replica가 실제로 개선할 지점

| 병목 후보 | 실제로 포화됐는가 | 근거 |
|---|---|---|
| DB CPU / 컴퓨팅 자원 | ❌ 아니오 | RDS CloudWatch CPU 4~6%대, 스파이크 없음 |
| 애플리케이션 CPU / Load Average | ✅ 예 (부수적) | Load Average Max 7.93 (코어 2개의 약 4배) |
| **HikariCP 커넥션 풀** | ✅ **예 (핵심)** | Active Max 10(풀 한도), **Pending Max 78**, Acquire Time Max 400ms |

→ Read Replica 도입의 실질적 효과는 "DB 서버 부하를 줄인다"가 아니라, **Master 풀(10) + Slave 풀(10)로 동시에 열 수 있는 총 커넥션 수 자체가 2배로 늘어나 Pending 대기열이 줄어드는 것**이다. Slide 17 "쿼리 최적화 이후에도 남은 병목 — 단일 DB" 근거 자료로 이 결론을 사용.

---

## 스크린샷 목록 (`k6/screenshots/step6-stress-before/`)

| 파일 | 내용 |
|---|---|
| `b5-1.jpg` | Basic Statistics / CPU / Load Average (Load Avg Max **7.93**) |
| `b5-2.jpg` | JVM GC (참고용, GC Stop the World Max 8.67ms) |
| `b5-3.jpg` | HikariCP ★PPT 핵심 — Active Max 10, **Pending Max 78**, Acquire Time Max ~400ms |
| `b5-4.jpg` | HTTP Statistics ★PPT 핵심 — GET /recipes Mean **861ms** |
| `b5-5.jpg` | RDS CloudWatch (Local 타임존 보정 후) — CPU/Connections 무변동 → DB는 여유, 병목은 앱 풀 |

---

## 원본 k6 출력

```
running (2m00.6s), 000/100 VUs, 6264 complete and 0 interrupted iterations
default ✓ [ 100% ] 000/100 VUs  2m0s

     ✓ 상태 코드 200

     checks.........................: 100.00% ✓ 6264      ✗ 0
     data_received..................: 22 MB   184 kB/s
     data_sent......................: 594 kB  4.9 kB/s
     http_req_blocked...............: avg=10.99ms  min=0s       med=0s       max=8.24s   p(90)=0s      p(95)=0s
     http_req_connecting............: avg=4.45ms   min=0s       med=0s       max=4.55s   p(90)=0s      p(95)=0s
   ✓ http_req_duration..............: avg=1.16s    min=45.73ms  med=928.08ms max=7.14s   p(90)=2.24s   p(95)=2.91s
       { expected_response:true }...: avg=1.16s    min=45.73ms  med=928.08ms max=7.14s   p(90)=2.24s   p(95)=2.91s
   ✓ http_req_failed................: 0.00%   ✓ 0         ✗ 6264
     http_req_receiving..............: avg=6.86ms   min=0s       med=506.6µs  max=4.15s   p(90)=5.2ms   p(95)=15.89ms
     http_req_sending.................: avg=163.49µs min=0s       med=0s       max=38.52ms p(90)=557.3µs p(95)=722.88µs
     http_req_tls_handshaking.........: avg=6.53ms   min=0s       med=0s       max=6.36s   p(90)=0s      p(95)=0s
     http_req_waiting.................: avg=1.15s    min=45.22ms  med=924.02ms max=7.14s   p(90)=2.23s   p(95)=2.89s
     http_reqs........................: 6264    51.959539/s
     iteration_duration...............: avg=1.28s    min=148.53ms med=1.03s    max=9.37s   p(90)=2.36s   p(95)=3.06s
     iterations........................: 6264    51.959539/s
     vus...............................: 1       min=1       max=100
     vus_max...........................: 100     min=100     max=100
```
