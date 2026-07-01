import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '40s',
};

const BASE_URL = 'https://test.naeng-gu.kr/api';
const TOKEN = __ENV.TOKEN || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0NzQyOTU2ODk0IiwiZXhwIjoxNzgyODg5MTYwfQ.319chioPadYZBN8YUVFmrXMZOiU0xMq2TIxCr6S6EzRTL5HLbJDoTZ9iTF5oSfvAxtL9ebcEbxW1rFXNSWZDCg';

export default function () {
  const res = http.get(`${BASE_URL}/recipes?sort=LATEST&keyword=%EB%A0%88%EC%8B%9C%ED%94%BC`, {
    headers: { Authorization: `Bearer ${TOKEN}` },
  });
  if (res.status !== 200) {
    console.log(`status: ${res.status}, body: ${res.body}`);
  }
  sleep(0.1);
}
