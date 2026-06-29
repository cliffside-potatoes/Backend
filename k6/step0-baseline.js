/**
 * Step 0 — 베이스라인 측정 (N+1 수정, 캐싱 적용 전)
 *
 * [경로 선택]
 *
 * A. 토큰 있음 (추천) → toItemResponse() 경로
 *    레시피 1개당 5개 쿼리 → 20개 기준 101개 쿼리
 *    포폴 어필: "101개 → 0개" 로 더 드라마틱
 *
 * B. 토큰 없음         → toItemResponseAnonymous() 경로
 *    레시피 1개당 3개 쿼리 → 20개 기준 61개 쿼리
 *    빠르게 측정하고 싶을 때 사용
 *
 * [토큰 발급 방법]
 *   https://test.naeng-gu.kr/api/swagger-ui/index.html
 *   → 카카오 로그인 → 응답의 accessToken 복사
 *
 * [실행 방법]
 *   토큰 있음: k6 run -e TOKEN=eyJhbGci... k6/step0-baseline.js
 *   토큰 없음: k6 run k6/step0-baseline.js
 *
 * [결과 저장]
 *   k6 run -e TOKEN=eyJhbGci... k6/step0-baseline.js 2>&1 | tee k6/result-1-baseline.txt
 */

import http from 'k6/http';
import { sleep, check } from 'k6';

const BASE_URL = 'https://test.naeng-gu.kr/api';
const TOKEN = __ENV.TOKEN || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0NzQyOTU2ODk0IiwiZXhwIjoxNzgyNjYyMjc1fQ.1rN3iSTb4Y47tVX9XO6GcbDtc1ZI72EjhkdQ4JIZNl7zoLCbgLBwwS7VIKwLhpp0nFN8JcArxsRYazhMl_GQcg';

export const options = {
  stages: [
    { duration: '10s', target: 10 }, // 0명 → 10명
    { duration: '20s', target: 10 }, // 10명 유지
    { duration: '10s', target: 0 },  // 10명 → 0명
  ],
  thresholds: {
    http_req_failed:   ['rate<0.01'],    // 에러율 1% 미만
    http_req_duration: ['p(95)<5000'],   // 베이스라인이므로 느슨하게
  },
};

export default function () {
  let res;

  // 카테고리 필터 없이 전체 레시피 스캔 → 더미 10,000개 포함
  // findTopByLikeCount: recipe 전체 LEFT JOIN profile_favorite_recipe GROUP BY ORDER BY COUNT
  if (TOKEN) {
    res = http.get(
      `${BASE_URL}/recipes?sort=LIKE_COUNT`,
      { headers: { Authorization: `Bearer ${TOKEN}` } }
    );
  } else {
    res = http.get(`${BASE_URL}/recipes?sort=LIKE_COUNT`);
  }

  check(res, {
    '상태 코드 200': (r) => r.status === 200,
    '응답 body 존재': (r) => r.body && r.body.length > 0,
  });

  sleep(0.5);
}
