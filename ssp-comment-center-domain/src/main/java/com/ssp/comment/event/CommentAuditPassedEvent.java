package com.ssp.comment.event;

import java.time.LocalDateTime;

/**
 * 审核状态变更事件
 *
 * <p>在审核回调处理完成并提交事务后发布，用于驱动审核通过后缓存预热、通知发送等异步扩展链路。</p>
 */
public record CommentAuditPassedEvent(
    Long targetId,
    Integer targetType,
    Integer auditStatus,
    LocalDateTime auditTime
) {}
