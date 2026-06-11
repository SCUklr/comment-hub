package com.ssp.comment.event;

import java.time.LocalDateTime;

/**
 * 评论创建事件
 *
 * <p>在评论成功写入数据库并提交事务后发布，用于驱动热度初始化、缓存维护、用户索引更新等异步扩展链路。</p>
 */
public record CommentCreatedEvent(
    Long commentId,
    Long commentObjectId,
    Integer commentType,
    Integer commentUserId,
    String content,
    LocalDateTime createTime
) {}
