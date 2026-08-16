# ssp-comment-center

> 通用评论中心 — 基于 DDD 四层架构的独立可运行评论系统

`ssp-comment-center` 是一个面向任意内容对象的**通用评论系统**，支持评论、回复（楼中楼）、点赞、热评、审核状态机、站内通知、用户评论历史反查等完整能力。共 **15 个 HTTP 接口**、**6 张核心表**、**2库×4表分库分表**。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 3.0.7 |
| JDK | Java 21 |
| 数据库 | MySQL 8.0 + ShardingSphere-JDBC 5.4.1（分库分表） |
| ORM | MyBatis 3.5 + MyBatis-Spring-Boot-Starter |
| 缓存 | Redis + Redisson |
| 工具 | Lombok、MapStruct、Guava、PageHelper |
| 架构 | DDD 四层（start / application / domain / infrastructure） |

---

## 项目结构

```
ssp-comment-center/
├── web/                              # 前端演示 Demo（Vite + React 18 + TS）
├── docs/                             # 设计文档
│   ├── 01-design/                    # 设计方案、接口契约、面试复习速查
│   ├── 03-extension/                 # 扩展方案
│   ├── 04-devops/                    # 启动指南、API 测试
│   ├── 05-database/                  # 数据库脚本
│   └── 99-architecture/              # 完整技术方案
├── ssp-comment-center-start/         # 启动层（Controller、VO、自研基础组件）
├── ssp-comment-center-application/   # 应用层（业务编排）
├── ssp-comment-center-domain/        # 领域层（核心服务、实体、路由、雪花ID）
└── ssp-comment-center-infrastructure/# 基础设施层（MyBatis、Redis、Repository实现）
```

---

## 快速开始

> 更详细的踩坑与排障记录见 [`docs/04-devops/如何启动项目.md`](docs/04-devops/如何启动项目.md)。

### 环境要求

JDK 21+、Maven 3.9+、Node.js 18+、Docker、Redis 7.x

### 1. 启动 MySQL（Docker）

```bash
# 首次：创建容器 + 导入 DDL + 改认证插件
docker run -d --name mysql-comment -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8.0
docker exec -i mysql-comment mysql -u root -proot < docs/05-database/ddl-sharding.sql
docker exec mysql-comment mysql -u root -proot -e \
  "ALTER USER 'root'@'%' IDENTIFIED WITH caching_sha2_password BY 'root'; FLUSH PRIVILEGES;"

# 日常
docker start mysql-comment   # 启动
docker stop mysql-comment    # 停止
```

### 2. 启动 Redis

```bash
redis-server --daemonize yes --port 6379
redis-cli ping    # → PONG
```

### 3. 启动后端（8080）

```bash
cd ~/ssp-comment-center
mvn clean package -Dmaven.test.skip=true
java -jar ssp-comment-center-start/target/ssp-comment-center-start-1.0.0-SNAPSHOT.jar
```

验证：`curl http://localhost:8080/actuator/health` → `{"status":"UP"}`

### 4. 启动前端（5173）

```bash
cd ~/ssp-comment-center/web
npm install
npm run dev
```

浏览器打开 **http://localhost:5173**（Vite 已将 `/api` 代理到 `http://localhost:8080`）。

**注意**：先起后端（8080），再打开前端页面。

### 验证

```bash
curl http://localhost:8080/actuator/health
curl "http://localhost:8080/api/comment/list?commentObjectId=10001&commentType=1&page=1&pageSize=3"
```

### 停止

```bash
kill $(lsof -ti :8080)    # 后端
pkill -f "vite"           # 前端
redis-cli shutdown        # Redis（可选）
docker stop mysql-comment # MySQL（可选，数据保留）
```

### 常见问题

| 现象 | 解决 |
|------|------|
| `Access denied for user 'root'@'localhost'` | 3306 被宿主机其他 MySQL 占用，先停掉再重启容器 |
| `mvn package` 测试编译报错 | 加 `-Dmaven.test.skip=true`（start 模块既有测试引用已重构类） |
| `npm install` cache 权限错误 | `npm install --cache ./.npm-cache` |
| `Public Key Retrieval is not allowed` | 源码已配置 `allowPublicKeyRetrieval=true`，改后需重新打包 |

---

## 前端演示 Demo

提供 Vite + React 18 + TypeScript 单页 Demo，完整覆盖 15 个接口，适合理解与介绍项目。浏览器打开 http://localhost:5173 即可体验。

前端对接契约见 [`docs/01-design/接口契约.md`](docs/01-design/接口契约.md)。

---

## 文档索引

| 文档 | 用途 |
|------|------|
| [`docs/01-design/面试复习速查.md`](docs/01-design/面试复习速查.md) | **面试核心内容**：架构设计、数据库、缓存、分库分表、接口、踩坑记录 |
| [`docs/01-design/评论平台技术设计方案-精简版.md`](docs/01-design/评论平台技术设计方案-精简版.md) | 快速理解核心设计，适合喂给 LLM / 面试前复习 |
| [`docs/99-architecture/评论平台系统设计技术方案.md`](docs/99-architecture/评论平台系统设计技术方案.md) | 完整技术方案，含需求分析、接口设计、数据库设计、缓存设计、分库分表设计 |
| [`docs/01-design/评论模块源码结构说明.md`](docs/01-design/评论模块源码结构说明.md) | 源码结构、接口分布、关键流程伪代码、面试讲解顺序 |
| [`docs/01-design/评论系统关键问题拷打点.md`](docs/01-design/评论系统关键问题拷打点.md) | 面试高频追问及回答：分片键选择、热点数据、表结构设计、高并发更新 |
| [`docs/01-design/接口契约.md`](docs/01-design/接口契约.md) | 全部 REST API 的请求/响应契约，前后端对接唯一依据 |
| [`docs/04-devops/如何启动项目.md`](docs/04-devops/如何启动项目.md) | 详细启动步骤与环境踩坑记录 |
| [`docs/04-devops/API测试指南.md`](docs/04-devops/API测试指南.md) | curl 快速测试命令 |
| [`docs/05-database/ddl-sharding.sql`](docs/05-database/ddl-sharding.sql) | 分库分表环境建表脚本（2库×4表，5张分片表共40张物理表 + 审核单表，合计41张） |


---

## License

MIT
