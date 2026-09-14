// k6 参数化：按环境变量 VUS 跑固定并发档
// 用法: VUS=100 k6 run -o json=/tmp/ssp-vu-100.json generic-vu.js
import http from 'k6/http';
import { check } from 'k6';

const VUS = Number(__ENV.VUS || 20);

export const options = {
  vus: VUS,
  duration: '25s',
  summaryTrendStats: ['avg', 'min', 'med', 'p(50)', 'p(95)', 'p(99)', 'max'],
};

const BASE = 'http://localhost:8080';
const URL = `${BASE}/api/comment/list?commentObjectId=10001&commentType=1&page=1&pageSize=10`;

export default function () {
  const res = http.get(URL);
  check(res, {
    'http_status_200': (r) => r.status === 200,
    'business_code_200': (r) => JSON.parse(r.body).code === 200,
  });
}
