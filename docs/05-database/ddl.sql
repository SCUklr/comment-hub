-- 评论中心数据库DDL
-- 数据库: ssp_comment
-- 字符集: utf8mb4

CREATE DATABASE IF NOT EXISTS ssp_comment
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ssp_comment;

-- 1. 一级评论表
CREATE TABLE IF NOT EXISTS component_comment (
    id              BIGINT PRIMARY KEY COMMENT '评论ID',
    comment_object_id BIGINT NOT NULL COMMENT '评论对象ID（如题解ID、面经ID）',
    comment_type    INT NOT NULL COMMENT '评论对象类型',
    content         TEXT NOT NULL COMMENT '评论内容',
    images          VARCHAR(2000) DEFAULT '[]' COMMENT '图片URL列表JSON',
    comment_user_id INT NOT NULL COMMENT '评论用户ID',
    sort            INT DEFAULT 0 COMMENT '置顶排序（越大越靠前）',
    reply_count     INT DEFAULT 0 COMMENT '回复数',
    like_count      INT DEFAULT 0 COMMENT '点赞数',
    audit_status    INT DEFAULT 0 COMMENT '审核状态: 0未审核 1通过 2拒绝',
    is_delete       TINYINT DEFAULT 0 COMMENT '是否删除: 0正常 1删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_object (comment_object_id, comment_type, is_delete),
    INDEX idx_user (comment_user_id, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='一级评论表';

-- 2. 回复表
CREATE TABLE IF NOT EXISTS component_comment_reply (
    id              BIGINT PRIMARY KEY COMMENT '回复ID',
    comment_id      BIGINT NOT NULL COMMENT '所属一级评论ID',
    parent_reply_id BIGINT DEFAULT 0 COMMENT '父回复ID（0表示直接回复评论）',
    reply_type      INT DEFAULT 1 COMMENT '回复类型: 1一级回复 2楼中楼回复',
    content         TEXT NOT NULL COMMENT '回复内容',
    images          VARCHAR(2000) DEFAULT '[]' COMMENT '图片URL列表JSON',
    reply_user_id   INT NOT NULL COMMENT '回复用户ID',
    be_replied_user_id INT DEFAULT -1 COMMENT '被回复用户ID',
    like_count      INT DEFAULT 0 COMMENT '点赞数',
    audit_status    INT DEFAULT 0 COMMENT '审核状态',
    is_delete       TINYINT DEFAULT 0 COMMENT '是否删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_comment (comment_id, parent_reply_id, is_delete),
    INDEX idx_user (reply_user_id, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回复表';

-- 3. 点赞记录表
CREATE TABLE IF NOT EXISTS component_comment_like (
    id              BIGINT PRIMARY KEY COMMENT '记录ID',
    user_id         INT NOT NULL COMMENT '用户ID',
    target_id       BIGINT NOT NULL COMMENT '目标ID（评论ID或回复ID）',
    target_type     INT NOT NULL COMMENT '目标类型: 1评论 2回复',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_user_target (user_id, target_id, target_type),
    INDEX idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- 4. 审核记录表
CREATE TABLE IF NOT EXISTS component_comment_audit (
    id              BIGINT PRIMARY KEY COMMENT '记录ID',
    target_id       BIGINT NOT NULL COMMENT '目标ID',
    target_type     INT NOT NULL COMMENT '目标类型: 1评论 2回复',
    audit_content   VARCHAR(500) COMMENT '审核内容快照',
    audit_status    INT NOT NULL COMMENT '审核状态',
    audit_reason    VARCHAR(500) COMMENT '审核原因',
    audit_operator  BIGINT DEFAULT 0 COMMENT '审核操作人ID',
    audit_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    INDEX idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- 5. 用户评论索引表（支撑"我评论过哪些"反查）
CREATE TABLE IF NOT EXISTS component_user_comment_index (
    id                  BIGINT PRIMARY KEY COMMENT '记录ID',
    user_id             INT NOT NULL COMMENT '用户ID',
    comment_object_id   BIGINT NOT NULL COMMENT '评论对象ID',
    comment_type        INT NOT NULL COMMENT '评论对象类型',
    interaction_type    INT NOT NULL COMMENT '互动类型: 1评论 2回复',
    target_comment_id   BIGINT COMMENT '目标评论ID',
    target_reply_id     BIGINT COMMENT '目标回复ID',
    latest_content      VARCHAR(500) COMMENT '最新内容快照',
    latest_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最新互动时间',
    interaction_count   INT DEFAULT 1 COMMENT '互动次数',
    is_delete           TINYINT DEFAULT 0 COMMENT '是否删除',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_user_object (user_id, comment_object_id, comment_type, interaction_type),
    INDEX idx_user_time (user_id, latest_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户评论索引表';

-- 6. 站内通知表
CREATE TABLE IF NOT EXISTS component_notification (
    id                  BIGINT PRIMARY KEY COMMENT '通知ID',
    user_id             INT NOT NULL COMMENT '接收人ID',
    type                INT NOT NULL COMMENT '通知类型: 1回复通知 2点赞通知',
    subject_id          BIGINT NOT NULL COMMENT '触发对象ID（评论ID/回复ID）',
    subject_type        INT NOT NULL COMMENT '触发对象类型: 1评论 2回复',
    actor_id            INT NOT NULL COMMENT '触发者用户ID',
    comment_object_id   BIGINT NOT NULL COMMENT '评论对象ID',
    comment_type        INT NOT NULL COMMENT '业务类型',
    content             VARCHAR(500) COMMENT '内容摘要',
    is_read             TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    is_delete           TINYINT DEFAULT 0 COMMENT '是否删除: 0正常 1删除',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知表';
