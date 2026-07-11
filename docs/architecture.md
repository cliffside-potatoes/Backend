# 냉구 서비스 아키텍처

## 1. CI/CD 파이프라인

### Spring Boot (Backend)
1. 코드 push → GitHub
2. GitHub Actions 트리거
3. Docker 이미지 빌드
4. Docker Hub push (`dbpark/naeng-gu-backend`)

### React (Frontend)
1. 코드 push → GitHub
2. GitHub Actions 트리거
3. Docker 이미지 빌드 (Nginx 내장)
4. Docker Hub push (`dbpark/naeng-gu-frontend`)

> `main` 브랜치 push 시 자동 실행 — EC2에 SSH 접속 후 `docker compose pull && docker compose up`

---

## 2. 인프라 구성 (AWS VPC 내부)

### AWS EC2
- 모든 컨테이너가 단일 EC2 위에서 실행
- Docker Compose로 컨테이너 관리
- `naenggu_net` 브리지 네트워크로 컨테이너 간 통신

### Nginx (호스트 직접 설치)
- HTTP(80) 접근 시 HTTPS(443)로 301 리다이렉트
- 도메인별 트래픽 분기

| 도메인 | 목적 |
|---|---|
| `naeng-gu.kr` | 운영(prod) 환경 |
| `test.naeng-gu.kr` | 테스트 환경 |
| `storage.naeng-gu.kr` | S3 프록시 |

- `/` 요청 → 프론트엔드 컨테이너
- `/api/` 요청 → 백엔드 컨테이너 (prefix 제거 후 전달)

### Let's Encrypt (Certbot)
- Nginx에 SSL 인증서 적용
- ACME 프로토콜로 Let's Encrypt 서버에서 발급 및 자동 갱신

---

## 3. 운영 환경 (`naeng-gu.kr`)

| 컨테이너 | 이미지 | 포트 |
|---|---|---|
| frontend | `naeng-gu-frontend:latest` | 3000 |
| backend | `naeng-gu-backend:latest` | 8080 |

- 프론트 컨테이너 내부에 Nginx + React 빌드 포함 (정적 파일 서빙)

---

## 4. 테스트 환경 (`test.naeng-gu.kr`)

| 컨테이너 | 이미지 | 포트 |
|---|---|---|
| frontend-test | `naeng-gu-frontend:test` | 3001 |
| backend-test | `naeng-gu-backend:test` | 8081 |

비용 절감을 위해 운영 시간 자동화:
- **EventBridge Cron → Lambda 트리거**
  - 09:00 KST: EC2 + RDS 시작
  - 18:00 KST: EC2 + RDS 중지
- test 환경에만 적용

---

## 5. 외부 AWS 서비스

### Amazon RDS (MySQL)
- prod/test 백엔드 공용 사용
- VPC 내부에서만 접근 가능
- 환경변수로 접속 정보 주입 (`SPRING_DATASOURCE_URL` 등)

### Amazon S3
- 이미지/파일 저장소
- 백엔드에서 직접 업로드 (AWS SDK, IAM 키 사용)
- `storage.naeng-gu.kr` 도메인으로 외부 접근

### Amazon Route 53
- 도메인 DNS 관리
- `naeng-gu.kr`, `test.naeng-gu.kr`, `storage.naeng-gu.kr` → EC2 IP 매핑

---

## 6. 전체 요청 흐름

```
client
  → Route 53 (DNS 조회)
  → Nginx (80 → 443 리다이렉트)
  → 도메인별 분기
      ├── naeng-gu.kr
      │     ├── /      → frontend(:3000)
      │     └── /api/  → backend(:8080) → RDS
      ├── test.naeng-gu.kr
      │     ├── /      → frontend-test(:3001)
      │     └── /api/  → backend-test(:8081) → RDS
      └── storage.naeng-gu.kr → S3
```
