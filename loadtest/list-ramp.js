// k6 阶梯加压：评论列表读链路，找到饱和拐点
// 并发 1 -> 25 -> 50 -> 100 -> 150 -> 200，每档 30s。
// 运行: k6 run -o json=/tmp/ssp-list-ramp.json list-ramp.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  stages: [
    { target: 1,   duration: '10s' },  // 预热点/暖机
    { target: 25,  duration: '30s' },
    { target: 50,  duration: '30s' },
    { target: 100, duration: '30s' },
    { target: 150, duration: '30s' },
    { target: 200, duration: '30s' },
  ],
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
