-- ============================================================
-- 评论中心分库分表环境 DDL
-- ============================================================
-- 分片策略：
--   1. 评论表 component_comment：comment_type 分库(2库)，comment_user_id 分表(4表)
--   2. 回复表 component_comment_reply：comment_id 分库分表(2库×4表)
--   3. 点赞表 component_comment_like：user_id 分库分表(2库×4表)
--   4. 用户索引表 component_user_comment_index：user_id 分库分表(2库×4表)
--   5. 审核表 component_comment_audit：不分片，单表存储于 ds_0
--   6. 站内通知表 component_notification：user_id 分库分表(2库×4表)
-- ============================================================

CREATE DATABASE IF NOT EXISTS ssp_comment_0
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ssp_comment_1
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- ds_0: ssp_comment_0
-- ============================================================
USE ssp_comment_0;

-- ---------- 评论表分片 ----------
CREATE TABLE IF NOT EXISTS component_comment_0 (
    id              BIGINT PRIMARY KEY COMMENT '评论ID',
    comment_object_id BIGINT NOT NULL COMMENT '评论对象ID',
    comment_type    INT NOT NULL COMMENT '评论对象类型（分库键）',
    content         TEXT NOT NULL COMMENT '评论内容',
    images          VARCHAR(2000) DEFAULT '[]' COMMENT '图片URL列表JSON',
    comment_user_id INT NOT NULL COMMENT '评论用户ID（分表键）',
    sort            INT DEFAULT 0 COMMENT '置顶排序',
    reply_count     INT DEFAULT 0 COMMENT '回复数',
    like_count      INT DEFAULT 0 COMMENT '点赞数',
    audit_status    INT DEFAULT 0 COMMENT '审核状态',
    is_delete       TINYINT DEFAULT 0 COMMENT '是否删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_object (comment_object_id, comment_type, is_delete),
    INDEX idx_user (comment_user_id, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS component_comment_1 LIKE component_comment_0;
CREATE TABLE IF NOT EXISTS component_comment_2 LIKE component_comment_0;
CREATE TABLE IF NOT EXISTS component_comment_3 LIKE component_comment_0;

-- ---------- 回复表分片 ----------
CREATE TABLE IF NOT EXISTS component_comment_reply_0 (
    id              BIGINT PRIMARY KEY COMMENT '回复ID',
    comment_id      BIGINT NOT NULL COMMENT '所属一级评论ID（分库分表键）',
    parent_reply_id BIGINT DEFAULT 0 COMMENT '父回复ID',
    reply_type      INT DEFAULT 1 COMMENT '回复类型',
    content         TEXT NOT NULL COMMENT '回复内容',
    images          VARCHAR(2000) DEFAULT '[]' COMMENT '图片URL列表JSON',
    reply_user_id   INT NOT NULL COMMENT '回复用户ID',
    be_replied_user_id INT DEFAULT -1 COMMENT '被回复用户ID',
    like_count      INT DEFAULT 0 COMMENT '点赞数',
    audit_status    INT DEFAULT 0 COMMENT '审核状态',
    is_delete       TINYINT DEFAULT 0 COMMENT '是否删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comment (comment_id, parent_reply_id, is_delete),
    INDEX idx_user (reply_user_id, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS component_comment_reply_1 LIKE component_comment_reply_0;
CREATE TABLE IF NOT EXISTS component_comment_reply_2 LIKE component_comment_reply_0;
CREATE TABLE IF NOT EXISTS component_comment_reply_3 LIKE component_comment_reply_0;

-- ---------- 点赞表分片 ----------
CREATE TABLE IF NOT EXISTS component_comment_like_0 (
    id              BIGINT PRIMARY KEY COMMENT '记录ID',
    user_id         INT NOT NULL COMMENT '用户ID（分库分表键）',
    target_id       BIGINT NOT NULL COMMENT '目标ID',
    target_type     INT NOT NULL COMMENT '目标类型',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_user_target (user_id, target_id, target_type),
    INDEX idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS component_comment_like_1 LIKE component_comment_like_0;
CREATE TABLE IF NOT EXISTS component_comment_like_2 LIKE component_comment_like_0;
CREATE TABLE IF NOT EXISTS component_comment_like_3 LIKE component_comment_like_0;

-- ---------- 用户评论索引表分片 ----------
CREATE TABLE IF NOT EXISTS component_user_comment_index_0 (
    id                  BIGINT PRIMARY KEY COMMENT '记录ID',
    user_id             INT NOT NULL COMMENT '用户ID（分库分表键）',
    comment_object_id   BIGINT NOT NULL COMMENT '评论对象ID',
    comment_type        INT NOT NULL COMMENT '评论对象类型',
    interaction_type    INT NOT NULL COMMENT '互动类型',
    target_comment_id   BIGINT COMMENT '目标评论ID',
    target_reply_id     BIGINT COMMENT '目标回复ID',
    latest_content      VARCHAR(500) COMMENT '最新内容快照',
    latest_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    interaction_count   INT DEFAULT 1 COMMENT '互动次数',
    is_delete           TINYINT DEFAULT 0 COMMENT '是否删除',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_user_object (user_id, comment_object_id, comment_type, interaction_type),
    INDEX idx_user_time (user_id, latest_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS component_user_comment_index_1 LIKE component_user_comment_index_0;
CREATE TABLE IF NOT EXISTS component_user_comment_index_2 LIKE component_user_comment_index_0;
CREATE TABLE IF NOT EXISTS component_user_comment_index_3 LIKE component_user_comment_index_0;

-- ---------- 站内通知表分片 ----------
CREATE TABLE IF NOT EXISTS component_notification_0 (
    id                  BIGINT PRIMARY KEY COMMENT '通知ID',
    user_id             INT NOT NULL COMMENT '接收人ID（分库分表键）',
    type                INT NOT NULL COMMENT '通知类型: 1回复通知 2点赞通知',
    subject_id          BIGINT NOT NULL COMMENT '触发对象ID（评论ID/回复ID）',
    subject_type        INT NOT NULL COMMENT '触发对象类型: 1评论 2回复',
    actor_id            INT NOT NULL COMMENT '触发者用户ID',
    comment_object_id   BIGINT NOT NULL COMMENT '评论对象ID',
    comment_type        INT NOT NULL COMMENT '业务类型',
    content             VARCHAR(500) COMMENT '内容摘要',
    is_read             TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    is_delete           TINYINT DEFAULT 0 COMMENT '是否删除: 0正常 1删除',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS component_notification_1 LIKE component_notification_0;
CREATE TABLE IF NOT EXISTS component_notification_2 LIKE component_notification_0;
CREATE TABLE IF NOT EXISTS component_notification_3 LIKE component_notification_0;

-- ---------- 审核表（不分片，单表） ----------
CREATE TABLE IF NOT EXISTS component_comment_audit (
    id              BIGINT PRIMARY KEY COMMENT '记录ID',
    target_id       BIGINT NOT NULL COMMENT '目标ID',
    target_type     INT NOT NULL COMMENT '目标类型',
    audit_content   VARCHAR(500) COMMENT '审核内容快照',
    audit_status    INT NOT NULL COMMENT '审核状态',
    audit_reason    VARCHAR(500) COMMENT '审核原因',
    audit_operator  BIGINT DEFAULT 0 COMMENT '审核操作人ID',
    audit_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- ds_1: ssp_comment_1
-- ============================================================
USE ssp_comment_1;

CREATE TABLE IF NOT EXISTS component_comment_0 LIKE ssp_comment_0.component_comment_0;
CREATE TABLE IF NOT EXISTS component_comment_1 LIKE ssp_comment_0.component_comment_0;
CREATE TABLE IF NOT EXISTS component_comment_2 LIKE ssp_comment_0.component_comment_0;
CREATE TABLE IF NOT EXISTS component_comment_3 LIKE ssp_comment_0.component_comment_0;

CREATE TABLE IF NOT EXISTS component_comment_reply_0 LIKE ssp_comment_0.component_comment_reply_0;
CREATE TABLE IF NOT EXISTS component_comment_reply_1 LIKE ssp_comment_0.component_comment_reply_0;
CREATE TABLE IF NOT EXISTS component_comment_reply_2 LIKE ssp_comment_0.component_comment_reply_0;
CREATE TABLE IF NOT EXISTS component_comment_reply_3 LIKE ssp_comment_0.component_comment_reply_0;

CREATE TABLE IF NOT EXISTS component_comment_like_0 LIKE ssp_comment_0.component_comment_like_0;
CREATE TABLE IF NOT EXISTS component_comment_like_1 LIKE ssp_comment_0.component_comment_like_0;
CREATE TABLE IF NOT EXISTS component_comment_like_2 LIKE ssp_comment_0.component_comment_like_0;
CREATE TABLE IF NOT EXISTS component_comment_like_3 LIKE ssp_comment_0.component_comment_like_0;

CREATE TABLE IF NOT EXISTS component_user_comment_index_0 LIKE ssp_comment_0.component_user_comment_index_0;
CREATE TABLE IF NOT EXISTS component_user_comment_index_1 LIKE ssp_comment_0.component_user_comment_index_0;
CREATE TABLE IF NOT EXISTS component_user_comment_index_2 LIKE ssp_comment_0.component_user_comment_index_0;
CREATE TABLE IF NOT EXISTS component_user_comment_index_3 LIKE ssp_comment_0.component_user_comment_index_0;

CREATE TABLE IF NOT EXISTS component_notification_0 LIKE ssp_comment_0.component_notification_0;
CREATE TABLE IF NOT EXISTS component_notification_1 LIKE ssp_comment_0.component_notification_0;
CREATE TABLE IF NOT EXISTS component_notification_2 LIKE ssp_comment_0.component_notification_0;
CREATE TABLE IF NOT EXISTS component_notification_3 LIKE ssp_comment_0.component_notification_0;
