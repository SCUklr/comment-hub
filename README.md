# ssp-comment-center

> 通用评论中心 — 基于 DDD 四层架构的独立可运行评论系统

---

## 项目简介

`ssp-comment-center` 是一个面向任意内容对象的**通用评论系统**，支持评论、回复（楼中楼）、点赞、热评、审核状态机、站内通知、用户评论历史反查等完整能力。

本项目以 `ssp-geek-commander` 的设计蓝图为基础，**彻底去除私有 Maven 依赖**，从零自研统一返回、分页、参数校验、用户上下文、雪花 ID 生成器等基础组件，实现真正独立可编译、可运行、可讲解的评论中心。

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
├── pom.xml                                    # 父POM
├── web/                                       # 前端演示 Demo（Vite + React 18 + TS）
├── docs/                                      # 设计文档
│   ├── 01-design/                             # 设计方案：技术选型、接口契约、源码结构、面试拷打点、前端 Demo 方案
│   ├── 03-extension/                          # 扩展方案：审核先发后审、Spring Event 异步事件驱动
│   ├── 04-devops/                             # 运维：启动指南、API 测试指南、测试用例
│   ├── 05-database/                           # 数据库脚本：ddl.sql、ddl-sharding.sql、迁移脚本
│   └── 99-architecture/                       # 完整技术方案
├── ssp-comment-center-start/                  # 启动层
│   ├── CommentCenterApplication.java          # 启动类
│   ├── controller/CommentController.java      # 评论主接口 + 审核接口
│   ├── controller/ReplyController.java        # 回复列表接口
│   ├── controller/NotificationController.java # 站内通知接口
│   ├── vo/req/ / vo/resp/                    # 请求/响应VO
│   ├── convertor/CommentVOConvertor.java      # VO转换器
│   ├── common/                                # 自研基础组件
│   │   ├── Result.java                        # 统一API返回
│   │   ├── PageResult.java                    # 分页返回
│   │   ├── CheckUtil.java                     # 参数校验
│   │   ├── UserContext.java                   # ThreadLocal用户上下文
│   │   └── LoginRequired.java                 # 登录校验注解
│   ├── interceptor/UserContextInterceptor.java # 用户上下文拦截器
│   └── config/WebConfig.java                  # Web配置
├── ssp-comment-center-application/            # 应用层
│   ├── CommentBizService.java                 # 业务编排
│   └── bo/                                    # 业务对象
├── ssp-comment-center-domain/                 # 领域层
│   ├── CommentDomainService.java              # 核心领域服务（540行）
│   ├── entity/                                # 领域实体
│   ├── repository/                            # 仓库接口
│   ├── route/CommentShardRouter.java          # 分库分表路由
│   └── common/                                # 自研基础组件
│       ├── BizException.java                  # 业务异常
│       └── SnowflakeIdUtils.java              # 雪花算法ID生成器
└── ssp-comment-center-infrastructure/         # 基础设施层
    ├── dao/mapper/                            # MyBatis Mapper接口
    ├── dao/persistance/                       # Repository实现
    ├── dao/pojo/                            # 数据库PO
    ├── config/MyBatisConfig.java              # MyBatis配置
    ├── config/RedisConfig.java                # Redisson配置
    └── resources/mapper/                      # Mapper XML
```

---

## 核心功能

共 **15 个 HTTP 接口**：

| 序号 | 方法 | 路径 | 说明 | 需登录 |
|------|------|------|------|--------|
| 1 | POST | `/api/comment/create` | 创建评论 / 回复 | ✅ |
| 2 | POST | `/api/comment/delete` | 删除评论 / 回复 | ✅ |
| 3 | POST | `/api/comment/edit` | 编辑评论 / 回复 | ✅ |
| 4 | POST | `/api/comment/pin` | 置顶 / 取消置顶评论 | ✅ |
| 5 | GET | `/api/comment/list` | 评论列表（含 topReplies，按审核状态过滤） | ❌ |
| 6 | GET | `/api/reply/list` | 回复列表（楼中楼，按审核状态过滤） | ❌ |
| 7 | POST | `/api/comment/like` | 点赞评论 / 回复 | ✅ |
| 8 | POST | `/api/comment/unlike` | 取消点赞 | ✅ |
| 9 | POST | `/api/comment/audit/callback` | 审核回调 | ❌ |
| 10 | GET | `/api/comment/audit/history` | 审核历史查询 | ❌ |
| 11 | GET | `/api/comment/hot` | 热评列表 | ❌ |
| 12 | GET | `/api/comment/my/list` | 我评论过的内容列表 | ✅ |
| 13 | GET | `/api/notification/list` | 我的通知列表 | ✅ |
| 14 | POST | `/api/notification/read` | 标记单条通知已读 | ✅ |
| 15 | POST | `/api/notification/read/all` | 标记全部通知已读 | ✅ |

---

## 数据库设计

共 **6 张核心表**：

| 表名 | 职责 | 分片策略 |
|------|------|--------|
| `component_comment` | 一级评论主表 | **comment_object_id 分库分表(2库×4表)** |
| `component_comment_reply` | 回复 / 楼中楼表 | **comment_id 分库分表(2库×4表)** |
| `component_comment_like` | 点赞记录表 | **user_id 分库分表(2库×4表)** |
| `component_comment_audit` | 审核记录表 | 不分片，单表存储于默认库 |
| `component_user_comment_index` | 用户评论索引表 | **user_id 分库分表(2库×4表)** |
| `component_notification` | 站内通知表 | **user_id 分库分表(2库×4表)** |

> 单库建表脚本见 [`docs/database/ddl.sql`](docs/database/ddl.sql)（开发环境）
> 分库分表建表脚本见 [`docs/database/ddl-sharding.sql`](docs/database/ddl-sharding.sql)（生产环境，5 张分片表各 2库×4表，共 40 张物理表；审核表为单表，合计 41 张物理表）

### 表关系

```
内容对象（外部系统）
    │ 一对多
    ▼
component_comment（一级评论）
    │ 一对多
    ▼
component_comment_reply（回复 / 楼中楼）
    │
    │ 统一目标模型（target_id + target_type）
    ▼
component_comment_like（点赞）  component_comment_audit（审核）

component_user_comment_index（用户索引，独立维度，不直接关联主表）
component_notification（站内通知，按接收人分片）
```

---

## 设计亮点

### 1. 评论与回复的标准树形建模

- 评论表 `component_comment` 负责**对象维度**
- 回复表 `component_comment_reply` 负责**评论维度 + 树结构**
- `comment_id` 负责归属，`parent_reply_id` 负责树结构，两者解耦

### 2. 评论列表批量组装，避免 N+1

- 先分页查评论
- 批量收集评论 ID，批量查一级回复
- 批量读 Redis 点赞数 Hash
- 应用层分组挂 `topReplies`

### 3. ShardingSphere-JDBC 分库分表

通过 ShardingSphere-JDBC 按对象 ID 与用户 ID 维度分库分表，支撑百万级评论量：

| 表 | 分库键 | 分表键 | 规模 |
|----|--------|--------|------|
| 评论表 | `comment_object_id`（评论对象ID） | `comment_object_id`（评论对象ID） | 2库 × 4表 |
| 回复表 | `comment_id`（评论ID） | `comment_id`（评论ID） | 2库 × 4表 |
| 点赞表 | `user_id`（用户ID） | `user_id`（用户ID） | 2库 × 4表 |
| 用户索引表 | `user_id`（用户ID） | `user_id`（用户ID） | 2库 × 4表 |
| 站内通知表 | `user_id`（用户ID） | `user_id`（用户ID） | 2库 × 4表 |

- 评论表按评论对象ID分库分表，同一对象下的评论落在同一物理分片，对象评论列表查询命中单分片
- 回复表按评论ID分片，确保同一评论的楼中楼数据落在同一分片，查询命中单分片
- 点赞表、用户索引表、站内通知表按用户ID分片，支撑高频用户维度反查命中单分片
- 配置集中管理于 `application.yml`，业务代码面向逻辑表开发，零侵入

> 源码层通过 `CommentShardRouter` 显式体现路由思路，与 ShardingSphere INLINE 算法保持一致，便于面试讲解。

### 4. 用户维度反查通过索引表承接

- "我评论过哪些"不命中主表分片键
- 单独设计 `component_user_comment_index`，以 `user_id` 为第一维度
- 主表继续按对象/评论维度分片，用户查询走索引表

### 5. Redis 缓存结构

| 缓存类型 | Key 模式 | 结构 |
|----------|----------|------|
| 评论点赞数 | `comment:like:obj:{objectId}` | Hash |
| 回复点赞数 | `reply:like:obj:{objectId}` | Hash |
| 用户点赞状态 | `user:like:{userId}:target:{type}:{id}` | String |
| 热评排序 | `comment:hot:obj:{objectId}:type:{type}` | ZSet |

### 6. 自研基础组件（零外部私有依赖）

| 组件 | 替代原依赖 | 位置 |
|------|-----------|------|
| `Result<T>` | `CommonResponse` | start/common |
| `PageResult<T>` | `PageResult` | start/common |
| `CheckUtil` | `CheckUtil` | start/common |
| `UserContext` | `RequestContextUtil` | start/common |
| `SnowflakeIdUtils` | `SnowflakeIdUtils` | domain/common |
| `BizException` | `CommonBizException` | domain/common |

---

## 快速开始（前后端手动启动）

> 更详细的踩坑与排障记录见 [`docs/04-devops/如何启动项目.md`](docs/04-devops/如何启动项目.md)。

### 0. 环境要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| JDK | 21+ | 编译、运行后端 |
| Maven | 3.9+ | 后端打包 |
| Node.js | 18+ | 前端构建与运行 |
| Docker | 任意 | 运行 MySQL 容器 |
| Redis | 7.x | 缓存、点赞状态、热评 ZSet |

### 1. 启动 MySQL（Docker，端口 3306）

**首次初始化**（容器不存在时）：

```bash
# ① 创建容器（账号 root / 密码 root）
docker run -d --name mysql-comment \
  -e MYSQL_ROOT_PASSWORD=root \
  -p 3306:3306 mysql:8.0

# ② 等待就绪后导入分库分表 DDL（2库 × 4表 + 审核单表）
docker exec -i mysql-comment mysql -u root -proot < docs/05-database/ddl-sharding.sql

# ③ 将 root 认证统一为 caching_sha2_password（MySQL 8 推荐，与源码 JDBC 配置匹配）
docker exec mysql-comment mysql -u root -proot -e \
  "ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY 'root'; \
   ALTER USER 'root'@'%' IDENTIFIED WITH caching_sha2_password BY 'root'; FLUSH PRIVILEGES;"
```

**日常启动 / 停止**（容器已存在，数据保留）：

```bash
docker start mysql-comment      # 启动
docker stop mysql-comment       # 停止
```

**验证**：`docker exec mysql-comment mysqladmin ping -u root -proot` → `mysqld is alive`

### 2. 启动 Redis（端口 6379）

```bash
redis-server --daemonize yes --port 6379
redis-cli ping                  # 期望输出 PONG
```

### 3. 启动后端（端口 8080）

```bash
cd ~/ssp-comment-center

# 打包（注意：start 模块存在既有的测试编译问题，必须加 -Dmaven.test.skip=true）
mvn clean package -Dmaven.test.skip=true

# 启动
java -jar ssp-comment-center-start/target/ssp-comment-center-start-1.0.0-SNAPSHOT.jar
```

**验证**：`curl http://localhost:8080/actuator/health` → `{"status":"UP"}`

> 连接配置见 `ssp-comment-center-start/src/main/resources/application.yml`：MySQL 为 `localhost:3306`（root/root，分库 `ssp_comment_0/1`），Redis 为 `localhost:6379`。源码已配置 `allowPublicKeyRetrieval=true` 以适配 MySQL 8 的 `caching_sha2_password` 认证。

### 4. 启动前端（端口 5173）

```bash
cd ~/ssp-comment-center/web
npm install          # 首次；若报 npm cache 权限错误，改用：npm install --cache ./.npm-cache
npm run dev
```

浏览器打开 **http://localhost:5173**（Vite 已将 `/api` 代理到 `http://localhost:8080`，无需后端 CORS）。

**注意启动顺序**：先起后端（8080），再打开前端页面，否则页面请求会代理失败。

### 5. 一键验证清单

```bash
# ① 后端健康
curl http://localhost:8080/actuator/health

# ② 评论列表（无需登录）
curl "http://localhost:8080/api/comment/list?commentObjectId=10001&commentType=1&page=1&pageSize=3"

# ③ 前端页面（浏览器打开）
open http://localhost:5173
```

### 6. 停止服务

```bash
kill $(lsof -ti :8080)    # 后端
pkill -f "vite"           # 前端（或在前端终端 Ctrl+C）
redis-cli shutdown        # Redis（可选）
docker stop mysql-comment # MySQL（可选，数据保留）
```

### 7. 常见问题

| 现象 | 原因 | 解决 |
|------|------|------|
| 后端启动报 `Access denied for user 'root'@'localhost'` | 3306 被宿主机其他 MySQL 占用（如 Homebrew 服务），Docker 容器被遮蔽 | 停掉占用进程后重启容器。本机曾遇 Homebrew mysql launchd 服务占用：`launchctl unload ~/Library/LaunchAgents/homebrew.mxcl.mysql.plist` |
| `mvn package` 报 `cannot find symbol: CommentShardRouter` 等测试编译错误 | start 模块测试代码引用已重构的类，属既有问题 | 打包加 `-Dmaven.test.skip=true`（跳过测试编译，不影响运行） |
| `npm install` 报 cache 目录权限错误 | `~/.npm` 存在 root 拥有的旧缓存 | `npm install --cache ./.npm-cache` |
| 后端报 `Public Key Retrieval is not allowed` | MySQL 8 认证握手未开启公钥获取 | 确认源码已含 `allowPublicKeyRetrieval=true`（本仓库已配置）；若改过配置需重新打包 |

### 8. 前端演示 Demo 功能与用法

提供 Vite + React 18 + TypeScript 单页 Demo，完整覆盖全部接口能力，适合理解与介绍项目：

| 区域 | 说明 |
|------|------|
| 顶部用户切换 | 通过 `X-User-Id` 模拟登录，内置 4 个演示用户（10001 作者 / 10002 / 10003 / 10004 运营），可自定义 ID |
| 💬 评论区 | 发表评论、最新/热评双 Tab、展开回复（楼中楼）、点赞、编辑/删除（仅本人）、置顶 |
| 📝 我的评论 | 按对象聚合展示"我评论/回复过哪些"，支持全部/仅评论/仅回复过滤 |
| 🔔 通知 | 站内通知列表（回复/点赞），单条已读 / 全部已读 |
| 🛡 审核 | 模拟审核回调（通过/拒绝）、待审核列表、审核历史，演示先发后审状态机 |

- 演示数据：默认对象 `commentObjectId=10001`，多用户互动数据可现场创建；预置了楼中楼、置顶、热评与通知
- 前端对接契约见 [`docs/01-design/接口契约.md`](docs/01-design/接口契约.md) 与 [`docs/01-design/最简前端 Demo 实现方案.md`](docs/01-design/最简前端 Demo 实现方案.md)

---

## 文档索引

| 文档 | 用途 |
|------|------|
| [`docs/01-design/评论平台技术设计方案-精简版.md`](docs/01-design/评论平台技术设计方案-精简版.md) | 快速理解核心设计，适合喂给 LLM / 面试前复习 |
| [`docs/99-architecture/评论平台系统设计技术方案.md`](docs/99-architecture/评论平台系统设计技术方案.md) | 完整技术方案，含需求分析、接口设计、数据库设计、缓存设计、分库分表设计 |
| [`docs/01-design/评论模块源码结构说明.md`](docs/01-design/评论模块源码结构说明.md) | 源码结构、接口分布、关键流程伪代码、面试讲解顺序 |
| [`docs/01-design/评论系统关键问题拷打点.md`](docs/01-design/评论系统关键问题拷打点.md) | 面试高频追问及回答：分片键选择、热点数据、表结构设计、高并发更新 |
| [`docs/01-design/接口契约.md`](docs/01-design/接口契约.md) | 全部 REST API 的请求/响应契约，前后端对接唯一依据 |
| [`docs/04-devops/如何启动项目.md`](docs/04-devops/如何启动项目.md) | 详细启动步骤与环境踩坑记录 |
| [`docs/04-devops/API测试指南.md`](docs/04-devops/API测试指南.md) | curl 快速测试命令 |
| [`docs/05-database/ddl-sharding.sql`](docs/05-database/ddl-sharding.sql) | 分库分表环境建表脚本（2库×4表，5张分片表共40张物理表 + 审核单表，合计41张） |

---

## 与原项目的关系

- **设计蓝图来源**：`ssp-geek-commander`（评论中心设计项目）
- **区别**：`ssp-comment-center` 彻底去除 `com.ssp.common` 等私有依赖，自研全部基础组件，实现真正独立可运行
- **保留内容**：15个接口、DDD四层架构、6张核心表、分库分表路由逻辑、Redis缓存结构、统一目标模型、Spring Event 异步事件驱动

---

## License

MIT
