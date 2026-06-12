# ssp-comment-center API 测试指南

> 服务地址：`http://localhost:8080`  
> 认证方式：Header 携带 `X-User-Id` 模拟登录用户  
> 请求格式：JSON (`Content-Type: application/json`)  
> 响应格式：统一包装 `{"code": 200, "message": "success", "data": ...}`

---

## 一、curl 快速测试命令

以下命令可直接在终端复制粘贴执行（**注意替换返回的 ID**）。

### 1. 创建一级评论

```bash
curl -s -X POST http://localhost:8080/api/comment/create \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "type": 1,
    "commentObjectId": 10001,
    "commentType": 1,
    "content": "这是一条测试评论"
  }'
```

**字段说明：**
- `type`: `1` = 创建一级评论，`2` = 创建回复
- `commentObjectId`: 被评论的对象 ID（如帖子 ID、视频 ID）
- `commentType`: 业务类型（如 `1`=帖子、`2`=视频，用于 ShardingSphere 分库）
- `content`: 评论内容（必填）

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "type": 1,
    "id": 720209260377591808,
    "commentObjectId": 10001,
    "commentType": 1,
    "content": "这是一条测试评论",
    "images": "[]",
    "createTime": "2026-06-11T17:34:28.516004",
    "updateTime": "2026-06-11T17:34:28.516007"
  }
}
```

> **记下返回的 `id`**，后续回复、点赞、删除都需要用到。

---

### 2. 回复评论

```bash
curl -s -X POST http://localhost:8080/api/comment/create \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 2" \
  -d '{
    "type": 2,
    "commentId": 720209260377591808,
    "content": "这是一条回复"
  }'
```

**字段说明：**
- `type`: `2` = 回复
- `commentId`: 上一级评论的 ID（上一步返回的 `id`）
- `parentId`: 楼中楼回复时填上级回复 ID，一级回复可不填或填 `0`
- `beRepliedUserId`: 被回复人的用户 ID（可选）

---

### 3. 查询评论列表

```bash
curl -s "http://localhost:8080/api/comment/list?commentObjectId=10001&commentType=1&page=1&pageSize=10&topReplyLimit=3"
```

**字段说明：**
- `commentObjectId` + `commentType`: 定位评论对象（必填）
- `page` / `pageSize`: 分页，默认 `1` / `20`
- `topReplyLimit`: 每条评论下展示几条热回复，默认 `3`

---

### 4. 点赞评论

```bash
curl -s -X POST http://localhost:8080/api/comment/like \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 3" \
  -d '{
    "targetId": 720209260377591808,
    "targetType": 1
  }'
```

**字段说明：**
- `targetId`: 评论/回复 ID
- `targetType`: `1` = 评论，`2` = 回复

---

### 5. 取消点赞

```bash
curl -s -X POST http://localhost:8080/api/comment/unlike \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 3" \
  -d '{
    "targetId": 720209260377591808,
    "targetType": 1
  }'
```

---

### 6. 删除评论

```bash
curl -s -X POST http://localhost:8080/api/comment/delete \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "type": 1,
    "id": 720209260377591808
  }'
```

**字段说明：**
- `type`: `1` = 删除评论，`2` = 删除回复
- `id`: 要删除的评论/回复 ID

---

### 7. 编辑评论

```bash
curl -s -X POST http://localhost:8080/api/comment/edit \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "type": 1,
    "id": 720209260377591808,
    "content": "编辑后的评论内容"
  }'
```

---

### 8. 置顶/取消置顶评论

```bash
curl -s -X POST http://localhost:8080/api/comment/pin \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "commentId": 720209260377591808,
    "isPin": true
  }'
```

---

### 9. 查询热评列表

```bash
curl -s "http://localhost:8080/api/comment/hot?commentObjectId=10001&commentType=1&page=1&pageSize=10"
```

---

### 10. 查询回复列表（楼中楼）

```bash
curl -s "http://localhost:8080/api/reply/list?commentId=720209260377591808&parentId=0&page=1&pageSize=10"
```

**字段说明：**
- `commentId`: 所属评论 ID（必填）
- `parentId`: `0` = 查一级回复，其他值 = 查楼中楼回复

---

### 11. 查询"我的评论"

```bash
curl -s "http://localhost:8080/api/comment/my/list?commentType=1&interactionType=1&page=1&pageSize=10" \
  -H "X-User-Id: 1"
```

**字段说明：**
- `commentType`: 业务类型过滤（可选）
- `interactionType`: `1` = 评论，`2` = 回复（可选）

---

### 12. 审核回调（模拟审核系统）

```bash
curl -s -X POST http://localhost:8080/api/comment/audit/callback \
  -H "Content-Type: application/json" \
  -d '{
    "targetId": 720209260377591808,
    "targetType": 1,
    "auditStatus": 1,
    "auditReason": "审核通过",
    "auditOperator": 999
  }'
```

**字段说明：**
- `auditStatus`: `0` = 待审核，`1` = 通过，`2` = 拒绝
- 该接口**不需要** `X-User-Id`，供内部审核系统调用

---

## 二、Postman / Apifox 测试步骤

### 基础配置

1. **新建 Collection**，命名为 `ssp-comment-center`
2. **设置 Base URL**：`http://localhost:8080`
3. **设置公共 Header**（在 Collection 级别配置，所有请求自动继承）：
   - `Content-Type: application/json`
   - `X-User-Id: 1`

> Apifox 操作：进入 Collection → 点击「修改文档」→「Header 参数」中添加以上两项。

---

### 接口 1：创建一级评论

| 配置项 | 值 |
|--------|-----|
| 方法 | `POST` |
| URL | `/api/comment/create` |
| Body 类型 | `json` |
| Body 内容 | 见下方 |

```json
{
  "type": 1,
  "commentObjectId": 10001,
  "commentType": 1,
  "content": "Postman 测试评论"
}
```

**断言建议：**
- 响应状态码 = 200
- `json.code` = 200
- `json.data.type` = 1
- `json.data.id` 存在且不为 null

> **将 `json.data.id` 提取为环境变量 `{{commentId}}`**，供后续接口使用。

---

### 接口 2：回复评论

| 配置项 | 值 |
|--------|-----|
| 方法 | `POST` |
| URL | `/api/comment/create` |
| Header | `X-User-Id: 2`（换用户模拟回复） |
| Body | 见下方 |

```json
{
  "type": 2,
  "commentId": {{commentId}},
  "content": "Postman 测试回复"
}
```

**断言建议：**
- `json.code` = 200
- `json.data.type` = 2
- `json.data.commentId` = `{{commentId}}`

> **提取 `json.data.id` 为环境变量 `{{replyId}}`**。

---

### 接口 3：查询评论列表

| 配置项 | 值 |
|--------|-----|
| 方法 | `GET` |
| URL | `/api/comment/list` |
| Query 参数 | 见下方 |

| 参数名 | 值 |
|--------|-----|
| `commentObjectId` | `10001` |
| `commentType` | `1` |
| `page` | `1` |
| `pageSize` | `10` |
| `topReplyLimit` | `3` |

**断言建议：**
- `json.code` = 200
- `json.data.total` >= 1
- `json.data.list[0].id` = `{{commentId}}`
- `json.data.list[0].replyCount` >= 1

---

### 接口 4：点赞评论

| 配置项 | 值 |
|--------|-----|
| 方法 | `POST` |
| URL | `/api/comment/like` |
| Header | `X-User-Id: 3`（换用户点赞） |
| Body | 见下方 |

```json
{
  "targetId": {{commentId}},
  "targetType": 1
}
```

**断言建议：**
- `json.code` = 200
- `json.data.liked` = true

---

### 接口 5：取消点赞

| 配置项 | 值 |
|--------|-----|
| 方法 | `POST` |
| URL | `/api/comment/unlike` |
| Header | `X-User-Id: 3` |
| Body | 同点赞，仅 URL 不同 |

**断言建议：**
- `json.data.liked` = false

---

### 接口 6：删除评论

| 配置项 | 值 |
|--------|-----|
| 方法 | `POST` |
| URL | `/api/comment/delete` |
| Header | `X-User-Id: 1`（评论创建者删除） |
| Body | 见下方 |

```json
{
  "type": 1,
  "id": {{commentId}}
}
```

**断言建议：**
- `json.code` = 200
- `json.data` = null

---

### 接口 7：查询热评

| 配置项 | 值 |
|--------|-----|
| 方法 | `GET` |
| URL | `/api/comment/hot` |
| Query 参数 | `commentObjectId=10001&commentType=1&page=1&pageSize=10` |

**断言建议：**
- `json.code` = 200
- 点赞数高的评论排在前面

---

### 接口 8：查询回复列表

| 配置项 | 值 |
|--------|-----|
| 方法 | `GET` |
| URL | `/api/reply/list` |
| Query 参数 | 见下方 |

| 参数名 | 值 |
|--------|-----|
| `commentId` | `{{commentId}}` |
| `parentId` | `0` |
| `page` | `1` |
| `pageSize` | `10` |

---

### 接口 9：查询"我的评论"

| 配置项 | 值 |
|--------|-----|
| 方法 | `GET` |
| URL | `/api/comment/my/list` |
| Header | `X-User-Id: 1` |
| Query 参数 | `commentType=1&interactionType=1&page=1&pageSize=10` |

---

## 三、验证 Spring Event 异步监听器

执行点赞后，查看应用日志（`/tmp/springboot.log`），应出现类似以下内容：

```
[comment-event-1] [HotScoreUpdate] commentId=..., newLikeCount=1, hotScore=...
[comment-event-4] [CacheEvict] updated likeCount cache for targetId=..., delta=1, newCount=1
```

说明：
- 点赞主链路仅写数据库
- 热评分数更新、缓存刷新由异步监听器在独立线程池中处理
- 线程名前缀为 `comment-event-`

---

## 四、完整测试流程（建议顺序）

```
1. 创建评论（用户 1）→ 记录 commentId，此时 audit_status=0
2. 审核回调（auditStatus=1）→ 评论对所有人可见
3. 回复评论（用户 2）→ 记录 replyId，此时 reply 也进入待审核
4. 审核回调回复（auditStatus=1）
5. 查询评论列表（无登录）→ 验证 replyCount=1
6. 点赞评论（用户 3）→ 验证 liked=true，用户 1 收到点赞通知
7. 查询评论列表 → 验证 likeCount=1
8. 取消点赞（用户 3）→ 验证 liked=false
9. 查询热评列表 → 验证排序
10. 查询回复列表 → 验证回复数据
11. 查询"我的评论"（用户 1）→ 验证互动记录
12. 查询"我的通知"（用户 1）→ 验证收到点赞/回复通知
13. 编辑评论（用户 1）→ 验证内容变更
14. 置顶评论（用户 1）→ 验证 sort 变化
15. 删除评论（用户 1）→ 验证列表为空
```

> 注意：未调用审核回调前，非作者用户调用 `/api/comment/list` 不会看到新创建的评论/回复。

---

## 五、常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| `500` + `Column 'id' cannot be null` | `SnowflakeId` 未设置 | 已修复，确保使用最新代码 |
| `500` + 空指针 | `createTime` / `updateTime` 未设置 | 已修复 |
| `401` / 登录相关错误 | 缺少 `X-User-Id` Header | 在请求头中添加 `X-User-Id: 1` |
| `Can't connect to local MySQL` | MySQL 容器未启动 | `docker start mysql-comment` |
| `Connection refused` (Redis) | Redis 未启动 | `redis-server --daemonize yes --port 6379` |
| 端口 8080 被占用 | 已有进程占用 | `lsof -i :8080` 找到 PID 后 `kill -9 <PID>` |

---

## 六、站内通知测试

### 6.1 查询我的通知列表

```bash
curl -s "http://localhost:8080/api/notification/list?page=1&pageSize=10" \
  -H "X-User-Id: 1"
```

### 6.2 标记单条通知已读

```bash
curl -s -X POST http://localhost:8080/api/notification/read \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "id": 720209260377591900
  }'
```

### 6.3 标记全部通知已读

```bash
curl -s -X POST http://localhost:8080/api/notification/read/all \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1"
```

---

## 七、审核状态机测试

### 7.1 创建评论后自动进入待审核

创建评论后，`component_comment_audit` 表会自动生成一条 `audit_status=0` 的记录。

```bash
curl -s -X POST http://localhost:8080/api/comment/create \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "type": 1,
    "commentObjectId": 10001,
    "commentType": 1,
    "content": "待审核测试评论"
  }'
```

### 7.2 审核回调

```bash
curl -s -X POST http://localhost:8080/api/comment/audit/callback \
  -H "Content-Type: application/json" \
  -d '{
    "targetId": 720209260377591808,
    "targetType": 1,
    "auditStatus": 1,
    "auditReason": "审核通过",
    "auditOperator": 999
  }'
```

### 7.3 查询审核历史

```bash
curl -s "http://localhost:8080/api/comment/audit/history?targetId=720209260377591808&targetType=1"
```

### 7.4 审核状态过滤说明

- 评论/回复创建后默认 `audit_status=0`（待审核），**非作者用户无法在未审核通过前看到**。
- 调用审核回调将 `auditStatus` 设为 `1` 后，未登录用户才能在列表中查看。
- 作者（`comment_user_id` / `reply_user_id` 与当前登录用户一致）始终可以看到自己的待审核/被拒绝内容。


