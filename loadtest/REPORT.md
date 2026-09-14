# ssp-comment-center 高并发压测报告

> 目的：验证「高并发评论服务中台」的真实承载能力，产出可写进简历、经得起面试追问的量化指标。
> 手段：k6 阶梯/分档压测，**真实跑出来的数据**，非估算。

---

## 0. 压测环境与搭建

| 项 | 说明 |
|---|---|
| 后端 | `ssp-comment-center-start-1.0.0-SNAPSHOT.jar`，端口 8080，已启动并通过 `/actuator/health` |
| 数据库 | docker 起的 `mysql:8.0`，映射端口 **33306**（避开本机原生 mysqld 的 3306） |
| 库 | `ssp_comment_0` / `ssp_comment_1`，**2库×4表=40 张分片表 + 审核单表**，已导入 `ddl-sharding.sql` |
| 缓存 | 本机 Redis 6379（无密码） |
| 数据 | 预置热对象 `comment_object_id=10001` 评论 **10000 条**（落单分片 `ssp_comment_1.component_comment_1`）；写路径测试再分散写入跨分片评论 |
| 压测工具 | k6（原生给出 P50/P95/P99、错误率） |
| **压测有效性关键** | 用运行时参数**关闭了 SQL-show/MyBatis 调试日志**（默认配置每条 SQL 打日志会严重拉低吞吐、污染数据），并把 DS 指向 33306 |

> ⚠️ 运行方式上后端用命令行覆盖连接串：`--spring.shardingsphere.datasource.ds_0.jdbc-url=.../ssp_comment_0?...` 等，**未改动源码配置**，可逆。

---

## 1. 测试接口与基线结果（20 并发，30s）

| 接口 | QPS/TPS | avg | p(50) | p(95) | p(99) | max | 错误率 |
|---|---|---|---|---|---|---|---|
| `GET /api/comment/list`（评论分页） | **358 QPS** | 55.6ms | 52.5ms | 90ms | 135ms | 253ms | 0% |
| `GET /api/comment/hot`（热评，Redis zset） | **444 QPS** | 44.9ms | 42.5ms | 71ms | 96ms | 246ms | 0% |
| `POST /api/comment/create`（创建评论，写） | **≈1498 TPS** | 13.3ms | 12ms | 24ms | 41ms | 415ms | 0% |

**读链路**（list）是典型的 **DB 密集**：主查询每次都回源 MySQL（无列表级缓存），只有点赞数走 Redis RMap。所以 list 的 ~358 QPS 是真实 DB 承载，不是缓存兜底刷出来的。

**写链路**（create）响应极快（avg 13ms），因为**重活被 SpringEvent 异步剥离**——缓存刷新、热评分数、通知都在独立线程池消费，主链路只同步写 DB。这是很好的架构点。

---

## 2. 阶梯/分档：找到饱和拐点（读链路 list）

| 并发 VUs | QPS | p(50) | p(95) | p(99) | max | 错误 |
|---|---|---|---|---|---|---|
| 20 | 358 | 52ms | 90ms | 135ms | 253ms | 0% |
| **50** | **429** | 115ms | 176ms | **218ms** | 307ms | 0% |
| 100 | 372 | 274ms | 404ms | 513ms | 714ms | 0% |
| 200 | 380 | 532ms | 753ms | 917ms | 1.32s | 0% |

**结论（读链路）：**
- 吞吐在 **50 并发时到顶 ≈430 QPS**（P99=218ms）——最优工作点。
- 之后加并发**吞吐不再增长**（~370-380 QPS），但延迟急剧恶化：P99 从 218ms → **917ms**（200 并发），max 1.32s。
- **全程 0 错误**——但这是有条件的，见 §4 洞察。

这是非常标准、可一图说清的**饱和曲线**：加并发不加吞吐、只加延迟 → 系统容量接近 MySQL/连接池上限。

---

## 3. 资源观测

| 指标 | 表现 |
|---|---|
| MySQL `Threads_connected` | 空闲 ~42 → 高压 **峰值 101**，且**压测结束仍保持 101**（Threads_running 回落到 2） |
| MySQL `Threads_running` | 峰值 **47** |
| Redis `keyspace_hits / misses` | 压测期间 ~100 万+ hits / **1 miss**，缓存命中极佳，Redis 非瓶颈 |
| HTTP 错误 | 全程 0（业务 code=200 全部通过） |

---

## 4. 关键洞察 / 真实发现（面试加分）

1. **读主链路是 DB 密集**：`/api/comment/list` 无列表级缓存，每请求都回源 MySQL（单分片路由）；缓存只覆盖点赞数 RMap 与热评 zset。所以这个系统的容量瓶颈是 **MySQL 与连接池**，而非 Redis——这点要能讲清。

2. **热评缓存与库不一致（发现一个 BUG）**：Redis 里 `comment:hot:obj:10001:type:1` zset 若残留**旧 ID**，`queryByIds` 会因 `where id in (...) and comment_object_id=10001` 匹配不到而返回空列表，导致 hot 接口「有数据却返回空」。清掉 zset 让浏览器回源 DB 后即恢复正常。→ 需要缓存一致性/失效策略（如 TTL、事件驱动重建、启动时预热）。

3. **连接数跑到 101，超过配置预期（40=20×2）**：配置 `maximum-pool-size:20`（ds_0/ds_1 各一个），但压测后 `Threads_connected` 恒为 101。这有两种可能：(a) ShardingSphere YAML 里 Hikari 参数没按 `hikari:` 嵌套绑定生效，走了默认；(b) 存在额外连接来源。**这其实解释了为何 0 错误**——连接没被真正限流，请求在 HTTP 线程里排队而非被连接池拒绝。若把池严格限到 40，高并发下很可能出现「Connection is not available」超时。**值得核查 pool 配置是否生效**。

4. **写路径扛快速是因为异步剥离**：create 主链路只同步写 DB + 发事件，缓存/热度/通知异步消费。所以 TPS 高 ≠ 系统轻松——真正的负载在异步监听器，需关注其线程池队列是否堆积（可加告警/背压）。

---

## 5. 简历可用的指标措辞（**真实测得**）

> **压测**：k6 阶梯/分档压测评论读链路，支撑峰值 **~430 QPS**（50 并发，P99 218ms）；加至 200 并发吞吐趋稳 ~380 QPS、P99 ~917ms 但**全程 0 错误**；热评读 **~440 QPS / P99 96ms**；写链路（创建评论）**~1500 TPS / P99 41ms**。
> **架构验证**：读主链路命中单分片路由（`comment_object_id` 分库分表），点赞数走 Redis RMap、热评走 Redis zset；写链路重活在 SpringEvent 异步线程池剥离，主链路仅同步写库。
> **问题定位**：压测暴露热评缓存与库数据不一致（旧 ID 残留导致 hot 空列表）、以及 Hikari 连接数未按配置收敛（40 → 101），为容量与一致性治理提供依据。

> 说明：写路径 1500 TPS 是**分散对象 ID**（写入均匀分布在 2库×4表）的聚合写吞吐；若压单个热对象（全部写同一分片），数值会因分片写竞争明显下降，属正常预期，面试可主动说明。

---

## 6. 复现方法

```bash
# 1. 起 MySQL 容器（33306）+ 导库 + root/root
docker run -d --name mysql-comment -e MYSQL_ROOT_PASSWORD=root -p 33306:3306 mysql:8.0
docker exec -i mysql-comment mysql -uroot -proot < docs/05-database/ddl-sharding.sql
docker exec mysql-comment mysql -uroot -proot -e "ALTER USER 'root'@'%' IDENTIFIED WITH caching_sha2_password BY 'root'; FLUSH PRIVILEGES;"

# 2. 起后端（关掉 SQL 日志 + 连 33306）——见下方命令

# 3. 造数（可选，read 热对象 10001 1万条评论）
#    见 README/本报告 §0；或直接用 loadtest/create-write.js 产数据

# 4. 压测
cd loadtest
k6 run list-baseline.js     # 20 并发基线
k6 run list-ramp.js         # 阶梯 1->200
VUS=50  k6 run generic-vu.js  # 分档 50/100/200
k6 run hot-baseline.js
k6 run create-write.js
```

后端启动（示例）：
```bash
java -jar ssp-comment-center-start/target/ssp-comment-center-start-1.0.0-SNAPSHOT.jar \
  --spring.shardingsphere.datasource.ds_0.jdbc-url="jdbc:mysql://localhost:33306/ssp_comment_0?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true" \
  --spring.shardingsphere.datasource.ds_1.jdbc-url="jdbc:mysql://localhost:33306/ssp_comment_1?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true" \
  --spring.shardingsphere.props.sql-show=false \
  --mybatis.configuration.log-impl=org.apache.ibatis.logging.nologging.NoLoggingImpl \
  --logging.level.com.ssp.comment.dao.mapper=info --logging.level.org.apache.shardingsphere=info
```
