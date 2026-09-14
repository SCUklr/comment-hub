// k6 基线压测：评论列表读链路（/api/comment/list）
// 单一并发档，用于建立参考基线。输出 JSON 报告。
// 运行: k6 run -o json=/tmp/ssp-list-baseline.json list-baseline.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 20,          // 基线并发
  duration: '30s',  // 稳定时长
  summaryTrendStats: ['avg', 'min', 'med', 'p(50)', 'p(95)', 'p(99)', 'max'],
};

const BASE = 'http://localhost:8080';
// 命中单分片(comment_object_id=10001 -> ds_1 / component_comment_1)，page=1 取主分页
const URL = `${BASE}/api/comment/list?commentObjectId=10001&commentType=1&page=1&pageSize=10`;

export default function () {
  const res = http.get(URL);
  check(res, {
    'http_status_200': (r) => r.status === 200,
    'business_code_200': (r) => JSON.parse(r.body).code === 200,
  });
}
