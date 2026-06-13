package com.ssp.comment.event;

import java.time.LocalDateTime;

/**
 * 审核状态变更事件
 *
 * <p>在审核回调处理完成并提交事务后发布，用于驱动审核状态变更后的异步扩展链路<br>
 * （如热评缓存清理、用户评论索引标记删除等）。</p>
 */
public record CommentAuditChangedEvent(
    Long targetId,
    Integer targetType,
    Integer auditStatus,
    LocalDateTime auditTime,
    Long commentObjectId,
    Integer commentType,
    Integer authorId
) {
}
