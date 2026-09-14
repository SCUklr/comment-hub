// k6 写路径压测：创建评论（/api/comment/create）
// 说明：
//   - 分片键是 comment_object_id，这里在 10001..11200 间随机，使写入均匀分布在 2库×4表
//   - 每条评论用雪花ID，写 DB + 异步事件
//   - X-User-Id 模拟登录用户
// 运行: k6 run -o json=/tmp/ssp-create-write.json create-write.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 20,
  duration: '30s',
  summaryTrendStats: ['avg', 'min', 'med', 'p(50)', 'p(95)', 'p(99)', 'max'],
};

const BASE = 'http://localhost:8080';
const URL = `${BASE}/api/comment/create`;

export default function () {
  const objId = 10001 + (Math.floor(Math.random() * 1200));  // 10001..11200
  const userId = 1 + Math.floor(Math.random() * 500);        // 1..500
  const body = JSON.stringify({
    type: 1,
    commentObjectId: objId,
    commentType: 1,
    content: `k6-write-${__VU}-${Date.now()}`,
  });
  const res = http.post(URL, body, {
    headers: { 'Content-Type': 'application/json', 'X-User-Id': String(userId) },
  });
  check(res, {
    'http_status_200': (r) => r.status === 200,
    'business_code_200': (r) => JSON.parse(r.body).code === 200,
  });
}
