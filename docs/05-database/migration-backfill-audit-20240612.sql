-- ============================================================
-- 数据修复脚本：为子任务 1 审核状态机上线做旧数据兼容
-- 适用场景：已有评论 / 回复数据在上线前未经过审核状态机，audit_status 默认为 0，
--          上线后会导致非作者用户看不到历史内容。
-- 修复逻辑：
--   1. 将现有 component_comment / component_comment_reply 中 audit_status=0 的记录默认置为 1（已通过）；
--   2. 在 component_comment_audit 中补一条 "历史数据兼容，默认通过" 的审核记录，
--      保证 /api/comment/audit/history 接口对旧数据也能返回历史。
-- 执行方式：
--   docker exec -i mysql-comment mysql -u root -proot < docs/05-database/migration-backfill-audit-20240612.sql
-- 注意事项：
--   - 本脚本为一次性脚本，执行前请务必备份；
--   - 使用 UUID_SHORT() 生成 BIGINT 主键，确保与现有 Snowflake ID 不冲突；
--   - 若数据量巨大，建议按分片分批执行，避免长事务锁表。
-- ============================================================

USE ssp_comment_0;

-- ---------- component_comment_0~3 ----------
INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 1, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_0 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_0 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 1, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_1 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_1 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 1, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_2 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_2 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 1, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_3 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_3 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

-- ---------- component_comment_reply_0~3 ----------
INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 2, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_reply_0 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_reply_0 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 2, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_reply_1 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_reply_1 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 2, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_reply_2 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_reply_2 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 2, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_reply_3 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_reply_3 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

USE ssp_comment_1;

-- ---------- component_comment_0~3 ----------
INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 1, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_0 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_0 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 1, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_1 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_1 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 1, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_2 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_2 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 1, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_3 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_3 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

-- ---------- component_comment_reply_0~3 ----------
INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 2, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_reply_0 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_reply_0 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 2, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_reply_1 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_reply_1 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 2, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_reply_2 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_reply_2 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;

INSERT INTO component_comment_audit (id, target_id, target_type, audit_content, audit_status, audit_reason, audit_operator, audit_time)
SELECT UUID_SHORT(), id, 2, LEFT(content, 500), 1, '历史数据兼容，默认通过', 0, NOW()
FROM component_comment_reply_3 WHERE audit_status = 0 AND is_delete = 0;
UPDATE component_comment_reply_3 SET audit_status = 1 WHERE audit_status = 0 AND is_delete = 0;
