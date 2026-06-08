# ssp-comment-center

> 通用评论中心 — 基于 DDD 四层架构的独立可运行评论系统

---

## 项目简介

`ssp-comment-center` 是一个面向任意内容对象的**通用评论系统**，支持评论、回复（楼中楼）、点赞、热评、审核回调、用户评论历史反查等完整能力。

本项目以 `ssp-geek-commander` 的设计蓝图为基础，**彻底去除私有 Maven 依赖**，从零自研统一返回、分页、参数校验、用户上下文、雪花 ID 生成器等基础组件，实现真正独立可编译、可运行、可讲解的评论中心。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 3.0.7 |
| JDK | Java 17 |
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
├── docs/                                      # 设计文档
│   ├── ddl.sql                                # 单库建表脚本（开发环境）
│   ├── ddl-sharding.sql                       # 分库分表建表脚本（生产环境，2库×4表）
│   ├── 评论平台技术设计方案-精简版.md
│   ├── 评论平台系统设计技术方案.md
│   ├── 评论模块源码结构说明.md
│   └── 评论系统关键问题拷打点.md
├── ssp-comment-center-start/                  # 启动层
│   ├── CommentCenterApplication.java          # 启动类
│   ├── controller/CommentController.java      # 评论主接口
│   ├── controller/ReplyController.java        # 回复列表接口
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

共 **11 个 HTTP 接口**：

| 序号 | 方法 | 路径 | 说明 | 需登录 |
|------|------|------|------|--------|
| 1 | POST | `/api/comment/create` | 创建评论 / 回复 | ✅ |
| 2 | POST | `/api/comment/delete` | 删除评论 / 回复 | ✅ |
| 3 | POST | `/api/comment/edit` | 编辑评论 / 回复 | ✅ |
| 4 | POST | `/api/comment/pin` | 置顶 / 取消置顶评论 | ✅ |
| 5 | GET | `/api/comment/list` | 评论列表（含 topReplies） | ❌ |
| 6 | GET | `/api/reply/list` | 回复列表（楼中楼） | ❌ |
| 7 | POST | `/api/comment/like` | 点赞评论 / 回复 | ✅ |
| 8 | POST | `/api/comment/unlike` | 取消点赞 | ✅ |
| 9 | POST | `/api/comment/audit/callback` | 审核回调 | ❌ |
| 10 | GET | `/api/comment/hot` | 热评列表 | ❌ |
| 11 | GET | `/api/comment/my/list` | 我评论过的内容列表 | ✅ |

---

## 数据库设计

共 **5 张核心表**：

| 表名 | 职责 | 分片策略 |
|------|------|--------|
| `component_comment` | 一级评论主表 | **comment_type 分库(2库) + comment_user_id 分表(4表)** |
| `component_comment_reply` | 回复 / 楼中楼表 | **comment_id 分库分表(2库×4表)** |
| `component_comment_like` | 点赞记录表 | **user_id 分库分表(2库×4表)** |
| `component_comment_audit` | 审核记录表 | 不分片，单表存储于默认库 |
| `component_user_comment_index` | 用户评论索引表 | **user_id 分库分表(2库×4表)** |

> 单库建表脚本见 [`docs/ddl.sql`](docs/ddl.sql)（开发环境）
> 分库分表建表脚本见 [`docs/ddl-sharding.sql`](docs/ddl-sharding.sql)（生产环境，2库×4表共32张物理表）

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

通过 ShardingSphere-JDBC 按业务类型与用户 ID 维度分库分表，支撑百万级评论量：

| 表 | 分库键 | 分表键 | 规模 |
|----|--------|--------|------|
| 评论表 | `comment_type`（业务类型） | `comment_user_id`（用户ID） | 2库 × 4表 |
| 回复表 | `comment_id`（评论ID） | `comment_id`（评论ID） | 2库 × 4表 |
| 点赞表 | `user_id`（用户ID） | `user_id`（用户ID） | 2库 × 4表 |
| 用户索引表 | `user_id`（用户ID） | `user_id`（用户ID） | 2库 × 4表 |

- 评论表按业务类型分库，实现不同业务线数据隔离
- 回复表按评论ID分片，确保同一评论的楼中楼数据落在同一分片，查询命中单分片
- 点赞表和用户索引表按用户ID分片，支撑高频用户维度反查命中单分片
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

## 快速开始

### 1. 数据库（二选一）

**方案 A：单库模式（开发环境）**
```bash
mysql -u root -p < docs/ddl.sql
```

**方案 B：分库分表模式（生产环境）**
```bash
# 创建 2 个数据库，共 32 张物理分片表
mysql -u root -p < docs/ddl-sharding.sql
```

> 分片策略：2库 × 4表，评论表按业务类型分库、用户ID分表；回复表按评论ID分库分表。

### 2. Redis

确保本地 Redis 运行，或修改 `application.yml` 中的连接配置。

### 3. 编译运行

```bash
cd ssp-comment-center
mvn clean package
java -jar ssp-comment-center-start/target/ssp-comment-center-start-1.0.0-SNAPSHOT.jar
```

### 4. 接口测试

```bash
# 评论列表（无需登录）
curl "http://localhost:8080/api/comment/list?commentObjectId=1&commentType=1"

# 创建评论（需登录，通过 X-User-Id Header 传入用户ID）
curl -X POST "http://localhost:8080/api/comment/create" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 10001" \
  -d '{"type":1,"commentObjectId":1,"commentType":1,"content":"测试评论"}'
```

---

## 文档索引

| 文档 | 用途 |
|------|------|
| [`docs/评论平台技术设计方案-精简版.md`](docs/评论平台技术设计方案-精简版.md) | 快速理解核心设计，适合喂给 LLM / 面试前复习 |
| [`docs/评论平台系统设计技术方案.md`](docs/评论平台系统设计技术方案.md) | 完整技术方案，含需求分析、接口设计、数据库设计、缓存设计、分库分表设计 |
| [`docs/评论模块源码结构说明.md`](docs/评论模块源码结构说明.md) | 源码结构、接口分布、关键流程伪代码、面试讲解顺序 |
| [`docs/评论系统关键问题拷打点.md`](docs/评论系统关键问题拷打点.md) | 面试高频追问及回答：分片键选择、热点数据、表结构设计、高并发更新 |
| [`docs/ddl-sharding.sql`](docs/ddl-sharding.sql) | 分库分表环境建表脚本（2库×4表，共32张物理表） |

---

## 与原项目的关系

- **设计蓝图来源**：`ssp-geek-commander`（评论中心设计项目）
- **区别**：`ssp-comment-center` 彻底去除 `com.ssp.common` 等私有依赖，自研全部基础组件，实现真正独立可运行
- **保留内容**：11个接口、DDD四层架构、5张核心表、分库分表路由逻辑、Redis缓存结构、统一目标模型

---

## License

MIT
