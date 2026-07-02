/**
 * Step 6 — DB 이중화 전 고부하 스트레스 테스트 (단일 DB 한계 증명용)
 *
 * [목적]
 *   기존 baseline(step0~step5)은 10 VUs 기준이라 "단일 DB로 감당 불가"를
 *   증명하기엔 부하가 너무 낮다. Read Replica를 붙이고 라우팅을 배포하면
 *   "단일 DB가 고부하에서 무너지는 모습"은 다시 재현하기 번거로워지므로,
 *   배포 직전(Master 한 대뿐인 지금) 딱 한 번 잡아둬야 하는 수치다.
 *
 *   PPT Slide 17 "트래픽 2배 → DB 한 대로 감당 불가" 주장의 근거 자료.
 *   실행 중 Grafana에서 아래를 함께 캡처:
 *     - HikariCP Active Connections (풀 한도에 붙는지)
 *     - RDS CPU/커넥션 (CloudWatch 또는 Grafana RDS 대시보드)
 *
 * [시나리오]
 *   읽기 트래픽 비중(~90%)을 반영해 3개 조회 엔드포인트를 랜덤 혼합 호출
 *   GET /recipes?sort=LATEST   (레시피 목록 — 가장 빈번)
 *   GET /me/feed               (피드)
 *   GET /me/ingredients        (냉장고 재료)
 *
 * [부하 강도]
 *   기존 baseline(10 VUs) 대비 훨씬 높은 100 VUs까지 램프업.
 *   목적은 "통과"가 아니라 "어디서 무너지는지" 관찰이므로 threshold는 참고용.
 *
 * [토큰 발급]
 *   https://test.naeng-gu.kr/api/swagger-ui/index.html
 *   → 카카오 로그인 → 응답의 accessToken 복사
 *
 * [실행]
 *   k6 run -e TOKEN=eyJhbGci... k6/step6-stress-before.js 2>&1 | tee k6/result-6-stress-before.txt
 */

import http from 'k6/http';
import { sleep, check } from 'k6';

const BASE_URL = 'https://test.naeng-gu.kr/api';
const TOKEN = __ENV.TOKEN || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0NzQyOTU2ODk0IiwiZXhwIjoxNzgyOTYxMDM1fQ.nfRyJ54DFSdTiZEK3wafQU5YQynxnQmvqPS9ezzM-9exRLIg0i517-MsU1qPg08Otx5yN9mR2M_ENDYOdzqLjw';

export const options = {
  stages: [
    { duration: '30s', target: 50 },   // 0명 → 50명
    { duration: '30s', target: 100 },  // 50명 → 100명
    { duration: '40s', target: 100 },  // 100명 유지 (포화 관찰 구간)
    { duration: '20s', target: 0 },    // 100명 → 0명
  ],
  thresholds: {
    http_req_failed:   ['rate<0.05'],   // 고부하라 baseline보다 느슨하게
    http_req_duration: ['p(95)<8000'],
  },
};

const ENDPOINTS = [
  `${BASE_URL}/recipes?sort=LATEST`,
  `${BASE_URL}/me/feed`,
  `${BASE_URL}/me/ingredients`,
];

export default function () {
  const url = ENDPOINTS[Math.floor(Math.random() * ENDPOINTS.length)];
  const res = http.get(url, {
    headers: { Authorization: `Bearer ${TOKEN}` },
  });

  check(res, {
    '상태 코드 200': (r) => r.status === 200,
  });

  sleep(0.1);
}
