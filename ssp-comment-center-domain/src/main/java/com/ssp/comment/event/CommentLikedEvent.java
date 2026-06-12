package com.ssp.comment.event;

/**
 * 点赞/取消点赞事件
 *
 * <p>在点赞记录成功写入/删除数据库并提交事务后发布，用于驱动 Redis 点赞数缓存刷新、热评 ZSet 更新、通知发送等异步扩展链路。</p>
 */
public record CommentLikedEvent(
    Long targetId,
    Integer targetType,
    Integer userId,
    Long commentObjectId,
    Integer commentType,
    Integer newLikeCount,
    boolean isLike,
    Integer targetAuthorId
) {}
