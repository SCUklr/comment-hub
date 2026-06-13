# ssp-comment-center-web

> ssp-comment-center 前端演示工程，基于 React 18 + TypeScript + Ant Design 5.x + Vite 构建。

---

## 快速开始

```bash
cd web
npm install
npm run dev
```

前端服务默认启动在 `http://localhost:3000`，通过 Vite Proxy 自动代理 `/api` 请求到后端 `http://localhost:8080`。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| React | 18.x | 函数组件 + Hooks |
| TypeScript | 5.x | 严格模式 |
| Vite | 5.x | 构建工具，支持 dev server proxy |
| Ant Design | 5.x | 企业级 UI 组件库 |
| Axios | 1.x | HTTP 客户端，统一拦截器处理 |

## 目录结构

```
web/
├── src/
│   ├── api/              # 接口封装层
│   ├── components/       # 业务组件
│   ├── pages/            # 页面
│   ├── hooks/            # 自定义 Hooks
│   ├── types/            # TypeScript 类型定义（与后端接口契约对齐）
│   ├── utils/            # 工具与常量
│   ├── App.tsx           # 根组件（UserContext + ConfigProvider）
│   └── main.tsx          # 入口
├── .env.development      # 开发环境变量
├── vite.config.ts        # Vite 配置（含 proxy）
└── package.json
```

## 核心功能

- **评论列表**：分页展示，支持最新/最早/热评排序切换
- **发布评论**：顶部输入框，支持直接发表评论
- **楼中楼回复**：点击评论下方「回复」展开回复树，支持回复一级评论和回复具体回复
- **点赞**：乐观更新，点击即时反馈，失败自动回滚
- **用户切换**：顶部选择当前用户 ID（模拟登录），自动注入 `X-User-Id` Header
- **热评 Tab**：独立展示热门评论列表

## 构建部署

```bash
npm run build
```

构建产物输出到 `dist/` 目录，可通过 Nginx 或任意静态服务器托管。

## 后端联调

确保后端 `ssp-comment-center-start` 已启动在 `8080` 端口，且 `CorsConfig` 允许前端域名。开发阶段通过 Vite proxy 已自动解决跨域。
