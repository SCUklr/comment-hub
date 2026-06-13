# 最简前端 Demo 实现方案

> 本文档用于保留“如何用最小成本做一个前端 Demo，把 ssp-comment-center 当前所有接口能力都展示出来”的思路。只给出方案与关键对接点，不生成源码。

---

## 一、目标与边界

### 1.1 目标

用最少代码、最少依赖跑起一个可交互页面，能完整演示评论中台对外暴露的所有接口：

- 评论：创建 / 删除 / 编辑 / 置顶 / 列表 / 热评 / 我的评论
- 回复：创建（复用评论创建接口 type=2）/ 删除 / 列表
- 点赞：点赞 / 取消点赞
- 通知：通知列表 / 单条已读 / 全部已读
- 审核：审核回调 / 审核列表 / 审核历史（运营/演示视角）

### 1.2 非目标

- 不做真实登录、JWT、权限管理
- 不做复杂 UI 设计，只保证功能可触达
- 不做单元测试、E2E、CI/CD
- 不耦合后端源码，前端单独目录 `web/` 或 `frontend-demo/`

---

## 二、技术选型（最简单路线）

| 层级 | 推荐 | 理由 |
|------|------|------|
| 构建工具 | Vite 5 + React 18 | 创建命令一行搞定，热更新快，社区最主流 |
| 语言 | TypeScript | 与后端接口字段对齐，减少联调错误 |
| UI | 原生 HTML + CSS（或 Tailwind CSS） | 避免引入 Ant Design / Element 等重型库，保持 Demo 最小化；若希望好看一点可用 Tailwind |
| 请求 | Fetch API 封装 | 无需 axios，现代浏览器内置足够 |
| 路由 | 不需要 | 单页多 Tab 即可展示全部能力 |
| 状态 | useState / useReducer | 规模小，不需要 Redux / Zustand |

> 备选：如果对 React 不熟，可直接用单个 HTML 文件 + Vue 3 CDN / 原生 JS，但接口契约文档仍按本文档对接。

---

## 三、目录结构建议

```
web/                          # 前端 Demo 目录
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
└── src/
    ├── main.tsx              # 应用入口
    ├── App.tsx               # 页面骨架 + Tab 切换
    ├── api/
    │   ├── request.ts        # 统一封装 fetch：baseURL、header、错误处理
    │   ├── comment.ts        # 评论/回复/点赞相关接口
    │   └── notification.ts   # 通知相关接口
    ├── components/
    │   ├── UserSwitcher.tsx  # 切换当前登录用户（改 X-User-Id）
    │   ├── CommentForm.tsx   # 发布/编辑评论表单
    │   ├── CommentList.tsx   # 评论列表 + 热评 Tab
    │   ├── CommentItem.tsx   # 单条评论 + 操作区
    │   ├── ReplyTree.tsx     # 回复列表 + 楼中楼展开
    │   ├── LikeButton.tsx    # 点赞/取消点赞
    │   ├── MyComments.tsx    # 我的评论列表
    │   ├── NotificationBox.tsx # 通知列表 + 已读
    │   └── AuditPanel.tsx    # 审核回调 / 审核列表 / 审核历史
    ├── types/
    │   └── index.ts          # 前后端字段对应的 TypeScript 类型
    └── utils/
        └── constants.ts      # 默认 commentObjectId、commentType 等
```

---

## 四、后端启动与前置条件

1. 启动 MySQL（含两个库 `ssp_comment_0`、`ssp_comment_1` 及对应分表）。
2. 启动 Redis（默认 6379）。
3. 启动后端：
   ```bash
   ./start-backend.sh
   # 或
   cd ssp-comment-center-start && mvn spring-boot:run
   ```
4. 确认后端监听 `http://localhost:8080`。

---

## 五、接口对接要点

### 5.1 统一请求封装

- **BaseURL**：`http://localhost:8080`
- **Header**：每个请求默认携带 `X-User-Id: <当前用户ID>`
- **响应结构**：`{ code, message, data }`，以 `code !== 200` 判定失败
- **登录拦截**：后端对 `@LoginRequired` 接口，未携带 `X-User-Id` 会返回 401

> 当前后端未配置 CORS。Demo 有两种跨域方案：
> 1. **推荐**：Vite 配置 `server.proxy` 把 `/api` 代理到 `http://localhost:8080`，前端请求写相对路径 `/api/xxx`。
> 2. 后端增加一个 `CorsConfig` 全局允许 `localhost` 来源（需要改后端源码，Demo 阶段不建议）。

### 5.2 页面功能区与接口映射

| 页面区域 | 功能 | 对应接口 | 关键字段 |
|----------|------|----------|----------|
| 顶部栏 | 用户切换 | 无，仅改 Header 中的 X-User-Id | — |
| 评论区 | 发表评论 | `POST /api/comment/create` type=1 | commentObjectId / commentType / content |
| 评论区 | 评论列表 | `GET /api/comment/list` | commentObjectId / commentType / page / pageSize / topReplyLimit |
| 评论区 | 热评列表 | `GET /api/comment/hot` | 同列表 |
| 单条评论 | 编辑 | `POST /api/comment/edit` type=1 | id / content |
| 单条评论 | 删除 | `POST /api/comment/delete` type=1 | id |
| 单条评论 | 置顶/取消置顶 | `POST /api/comment/pin` | commentId / isPin |
| 单条评论 | 点赞/取消点赞 | `POST /api/comment/like` 或 `/unlike` | targetId / targetType=1 |
| 评论下方 | 展开回复 | `GET /api/reply/list` parentId=0 | commentId / page / pageSize |
| 评论下方 | 发表一级回复 | `POST /api/comment/create` type=2 | commentId / parentId=0 / content |
| 回复下方 | 展开楼中楼 | `GET /api/reply/list` parentId=回复ID | commentId / parentId / page / pageSize |
| 回复下方 | 回复某人 | `POST /api/comment/create` type=2 | commentId / parentId / beRepliedUserId / content |
| 回复项 | 编辑回复 | `POST /api/comment/edit` type=2 | id / content |
| 回复项 | 删除回复 | `POST /api/comment/delete` type=2 | id |
| 回复项 | 点赞/取消点赞 | `POST /api/comment/like` 或 `/unlike` | targetId / targetType=2 |
| 我的评论 Tab | 我的评论/回复 | `GET /api/comment/my/list` | commentType / interactionType / page / pageSize |
| 通知 Tab | 通知列表 | `GET /api/notification/list` | page / pageSize |
| 通知 Tab | 单条已读 | `POST /api/notification/read` | id |
| 通知 Tab | 全部已读 | `POST /api/notification/read/all` | 无 |
| 审核 Tab | 审核回调 | `POST /api/comment/audit/callback` | targetId / targetType / auditStatus / auditReason / auditOperator |
| 审核 Tab | 待审核列表 | `GET /api/comment/audit/list` | commentObjectId / commentType / page / pageSize |
| 审核 Tab | 审核历史 | `GET /api/comment/audit/history` | targetId / targetType |

### 5.3 关键字段说明

- `type`：在创建/编辑/删除接口中区分“评论”和“回复”
  - `1` = 评论
  - `2` = 回复
- `targetType`：在点赞/取消点赞/审核中区分对象
  - `1` = 评论
  - `2` = 回复
- `replyType`：回复数据结构中的类型，一般由后端返回，前端展示无需强依赖
- `liked`：布尔值，标识当前登录用户是否已点赞
- `images`：JSON 字符串，Demo 阶段可固定传 `"[]"`

---

## 六、页面交互设计（极简版）

### 6.1 顶部用户切换

- 一个 `<select>` 或输入框，选择/输入用户 ID（如 10001、10002、10003）。
- 切换后所有请求携带新的 `X-User-Id`。
- 切换后刷新当前 Tab 数据，方便演示“我评论/我点赞/我收到通知”。

### 6.2 评论区 Tab

- 输入框 + “发布评论”按钮。
- 列表分两个子 Tab：
  - **最新**：调用 `/api/comment/list`
  - **热评**：调用 `/api/comment/hot`
- 每条评论展示：用户 ID、内容、点赞数、回复数、时间。
- 操作按钮：点赞/取消、回复、编辑（仅自己）、删除（仅自己）、置顶/取消置顶（建议管理员，Demo 可全放开）。
- 点击“回复”展开该评论下的一级回复列表（`/api/reply/list?parentId=0`），并出现回复输入框。
- 一级回复可再展开楼中楼（`/api/reply/list?parentId=xxx`）。

### 6.3 我的评论 Tab

- 调用 `/api/comment/my/list`。
- 可用 `interactionType` 过滤：全部 / 仅评论 / 仅回复。
- 点击某条可跳转回评论区对应对象。

### 6.4 通知 Tab

- 列表展示未读 / 已读通知。
- 单条“标记已读”和“全部已读”按钮。
- 切换用户后通知列表会变化。

### 6.5 审核 Tab（运营视角）

- 输入被审核对象 ID 和类型，点击“审核通过/拒绝”调用 `/api/comment/audit/callback`。
- 查看审核列表 `/api/comment/audit/list`。
- 查看某对象的审核历史 `/api/comment/audit/history`。

---

## 七、跨域与代理配置

Vite 推荐配置：

```ts
// vite.config.ts
export default {
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
};
```

前端请求统一写 `/api/xxx`，开发时由 Vite 代理转发，浏览器不再触发 CORS 预检。

---

## 八、启动命令

```bash
cd web
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`，确认后端已启动即可开始演示。

---

## 九、演示数据建议

为了 demo 效果，建议提前准备：

- `commentObjectId = 10001`，`commentType = 1` 作为默认演示对象。
- 三个测试用户：10001（作者）、10002（评论者）、10003（点赞/回复者）。
- 预置几条评论和回复，确保热评、通知、我的评论 Tab 都有数据。

---

## 十、限制与后续可扩展点

| 当前 Demo 限制 | 后续扩展方向 |
|----------------|--------------|
| 用 Header 模拟登录，无真实鉴权 | 接入 JWT / OAuth / SSO |
| 无图片上传，images 固定传 `[]` | 接入 OSS 上传组件 |
| 单对象单页面展示 | 增加对象选择器，演示多业务类型 |
| 置顶权限未严格校验 | 引入角色权限（作者/管理员） |
| 审核回调由前端触发 | 实际应由内容审核服务回调 |
| 无分页加载更多动画 | 可用 Intersection Observer 做无限滚动 |

---

## 十一、总结

最小可行前端 Demo 的核心是：**Vite + React + Fetch + 一个单页多 Tab 布局**。通过 `X-User-Id` 模拟登录，通过 Vite 代理解决跨域，把《接口契约.md》中列出的 16+ 个接口按功能区组织到 4～5 个 Tab 中，即可完整展示评论中台的能力。
