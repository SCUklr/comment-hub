package com.ssp.comment.event;

/**
 * 评论/回复删除事件
 *
 * <p>在评论或回复被逻辑删除并提交事务后发布，用于驱动缓存清理、热度剔除、用户索引标记删除等异步扩展链路。</p>
 */
public record CommentDeletedEvent(
    Integer targetType,
    Long targetId,
    Long commentObjectId,
    Integer commentType,
    Integer operatorUserId
) {}
