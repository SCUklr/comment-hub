package com.ssp.comment.event;

import java.time.LocalDateTime;

/**
 * 回复创建事件
 *
 * <p>在回复成功写入数据库并提交事务后发布，用于驱动评论回复数更新、通知发送、热度重算等异步扩展链路。</p>
 */
public record ReplyCreatedEvent(
    Long replyId,
    Long commentId,
    Long parentReplyId,
    Integer replyType,
    Integer replyUserId,
    String content,
    LocalDateTime createTime
) {}
