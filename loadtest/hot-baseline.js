// k6 基线压测：热评读链路（/api/comment/hot）
// 运行: k6 run -o json=/tmp/ssp-hot-baseline.json hot-baseline.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 20,
  duration: '30s',
  summaryTrendStats: ['avg', 'min', 'med', 'p(50)', 'p(95)', 'p(99)', 'max'],
};

const BASE = 'http://localhost:8080';
const URL = `${BASE}/api/comment/hot?commentObjectId=10001&commentType=1&page=1&pageSize=10`;

export default function () {
  const res = http.get(URL);
  check(res, {
    'http_status_200': (r) => r.status === 200,
    'business_code_200': (r) => JSON.parse(r.body).code === 200,
  });
}
