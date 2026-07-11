/**
 * Step 3 — Redis Sorted Set match_count 전/후 측정
 *
 * [경로]
 *   GET /recipes?sort=MATCH_COUNT
 *   → 내 냉장고 재료와 가장 많이 겹치는 레시피 순서 조회
 *   → before: DB GROUP BY + COUNT 전체 스캔
 *   → after : Redis Sorted Set ZREVRANGE (O(log N))
 *
 * [토큰 발급]
 *   https://test.naeng-gu.kr/api/swagger-ui/index.html
 *   → 카카오 로그인 → 응답의 accessToken 복사
 *   (냉장고에 재료가 있어야 after 시 Redis 경로가 동작함)
 *
 * [실행]
 *   before:
 *     k6 run -e TOKEN=eyJhbGci... k6/step4-redis-match-count.js 2>&1 | tee k6/result-4-before.txt
 *   after:
 *     k6 run -e TOKEN=eyJhbGci... k6/step4-redis-match-count.js 2>&1 | tee k6/result-4-after.txt
 */

import http from 'k6/http';
import { sleep, check } from 'k6';

const BASE_URL = 'https://test.naeng-gu.kr/api';
const TOKEN = __ENV.TOKEN || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0NzQyOTU2ODk0IiwiZXhwIjoxNzgyODA3NDM3fQ.XasxC9hO-Bn9h2jSBmwdP2jwbqEAdlaBhFBI1gGjLYfmKfxUcKZ4vXqvIQTiBf3rX7mSnnac3gepWgeuug8y9w';

export const options = {
  stages: [
    { duration: '10s', target: 10 },
    { duration: '20s', target: 10 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed:   ['rate<0.01'],
    http_req_duration: ['p(95)<5000'],
  },
};

export default function () {
  const res = http.get(
    `${BASE_URL}/recipes?sort=MATCH_COUNT`,
    { headers: { Authorization: `Bearer ${TOKEN}` } }
  );

  check(res, {
    '상태 코드 200': (r) => r.status === 200,
    '응답 body 존재': (r) => r.body && r.body.length > 0,
  });

  sleep(0.5);
}
