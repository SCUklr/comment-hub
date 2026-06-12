package com.ssp.comment.listener;

import com.ssp.comment.event.CommentCreatedEvent;
import com.ssp.comment.event.ReplyCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户评论索引异步更新监听器
 *
 * <p>监听评论/回复创建事件，异步维护用户维度的评论索引（如"我评论过哪些"）。</p>
 *
 * <p>当前项目中用户索引已在同步链路写入，本监听器可作为：<br>
 * 1. 高并发场景下的削峰补充；<br>
 * 2. 后续索引重建/补偿的入口。</p>
 */
@Component
@Slf4j
public class UserIndexUpdateListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentCreated(CommentCreatedEvent event) {
        log.info("[UserIndexUpdate] commentCreated event: commentId={}, userId={}",
                event.commentId(), event.commentUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onReplyCreated(ReplyCreatedEvent event) {
        log.info("[UserIndexUpdate] replyCreated event: replyId={}, userId={}",
                event.replyId(), event.replyUserId());
    }
}
