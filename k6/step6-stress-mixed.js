/**
 * Step 6 — DB 이중화 전 고부하 스트레스 테스트 (읽기+쓰기 혼합, 90:10)
 *
 * [목적]
 *   step6-stress-before.js는 읽기만 섞어서 "커넥션 풀이 찼다"까지는 보여줬지만,
 *   "읽기가 몰리면 쓰기까지 밀린다"는 핵심 주장은 직접 증명하지 못했다.
 *   실제 서비스 트래픽 비율(읽기 90% / 쓰기 10%)을 그대로 재현해서,
 *   같은 커넥션 풀을 공유하는 지금 구조에서 쓰기 요청도 함께 지연되는지 확인한다.
 *
 * [시나리오]
 *   90% 읽기 — GET /recipes?sort=LATEST, GET /me/feed, GET /me/ingredients (랜덤)
 *   10% 쓰기 — 레시피 찜 토글 (POST/DELETE /recipes/{RECIPE_ID}/favorites, VU별로 번갈아 호출)
 *
 * [알려진 한계]
 *   기존 스크립트들과 동일하게 모든 VU가 같은 TOKEN(=같은 유저)을 공유한다.
 *   RecipeFavoriteService.createFavorite/deleteFavorite는 멱등하게 구현돼 있어
 *   (이미 찜했으면 조용히 리턴, 없으면 조용히 무시) 중복 호출 자체는 에러를 던지지 않는다.
 *   즉 200/201이 아닌 응답이 나온다면 그건 "동시 찜 충돌"이 아니라 실제 장애(커넥션 타임아웃 등)일
 *   가능성이 높다 — 실패 시 상태 코드/응답 본문을 콘솔에 남겨서 원인을 바로 확인한다.
 *
 * [사전 준비]
 *   RECIPE_ID를 실제 존재하는 레시피 id로 지정해야 한다.
 *   GET /recipes?sort=LATEST 응답에서 아무 id나 하나 골라서 사용.
 *
 * [토큰 발급]
 *   https://test.naeng-gu.kr/api/swagger-ui/index.html
 *   → 카카오 로그인 → 응답의 accessToken 복사
 *
 * [실행]
 *   k6 run -e TOKEN=eyJhbGci... -e RECIPE_ID=123 k6/step6-stress-mixed.js \
 *     2>&1 | tee k6/result-6-stress-mixed.txt
 */

import http from 'k6/http';
import { sleep, check, group } from 'k6';
import { Rate } from 'k6/metrics';

const BASE_URL = 'https://test.naeng-gu.kr/api';
const TOKEN = __ENV.TOKEN || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0NzQyOTU2ODk0IiwiZXhwIjoxNzgyOTYzMzUxfQ.WClKWOYL6kOgzJnbyR6qjcqQJwyBO54DmfnhkAbqnCZCC-SFEVCwh0p8qx1UcA36JNobuaXoxBqGlr7acNz3TA';
const RECIPE_ID = __ENV.RECIPE_ID || '1'; // ⚠️ 실행 전 실제 존재하는 recipeId로 교체

export const options = {
  stages: [
    { duration: '30s', target: 50 },   // 0명 → 50명
    { duration: '30s', target: 100 },  // 50명 → 100명
    { duration: '40s', target: 100 },  // 100명 유지 (포화 관찰 구간)
    { duration: '20s', target: 0 },    // 100명 → 0명
  ],
  thresholds: {
    http_req_failed:   ['rate<0.05'],
    http_req_duration: ['p(95)<8000'],
  },
};

const READ_ENDPOINTS = [
  `${BASE_URL}/recipes?sort=LATEST`,
  `${BASE_URL}/me/feed`,
  `${BASE_URL}/me/ingredients`,
];

// 쓰기 요청 중 200/201/409(동시성 충돌) 외의 진짜 에러 비율만 별도 추적
const writeUnexpectedErrorRate = new Rate('write_unexpected_error');

let liked = false; // VU별 로컬 상태 — POST/DELETE 토글용 (k6는 VU마다 독립된 모듈 상태를 가짐)

export default function () {
  const headers = { Authorization: `Bearer ${TOKEN}` };

  if (Math.random() < 0.9) {
    // 읽기 90%
    group('read', () => {
      const url = READ_ENDPOINTS[Math.floor(Math.random() * READ_ENDPOINTS.length)];
      const res = http.get(url, { headers });
      check(res, { '읽기 응답 200': (r) => r.status === 200 });
    });
  } else {
    // 쓰기 10% — 레시피 찜 토글
    group('write', () => {
      const url = `${BASE_URL}/recipes/${RECIPE_ID}/favorites`;
      const res = liked
        ? http.del(url, null, { headers })
        : http.post(url, null, { headers });

      const ok = check(res, {
        '쓰기 응답 정상(200/201)': (r) => r.status === 200 || r.status === 201,
      });
      writeUnexpectedErrorRate.add(!ok);

      if (ok) {
        liked = !liked;
      } else {
        console.log(`[write 실패] status=${res.status} body=${res.body ? res.body.slice(0, 300) : '(empty)'}`);
      }
    });
  }

  sleep(0.1);
}
