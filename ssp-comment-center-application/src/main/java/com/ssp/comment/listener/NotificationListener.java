package com.ssp.comment.listener;

import com.ssp.comment.event.ReplyCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 通知发送监听器（预留扩展）
 *
 * <p>监听回复创建事件，异步发送 "有人回复了你" 等站内通知。<br>
 * 当前为预留实现，仅打印日志，后续可接入消息推送服务。</p>
 */
@Component
@Slf4j
public class NotificationListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onReplyCreated(ReplyCreatedEvent event) {
        log.info("[Notification] replyCreated event: replyId={}, commentId={}, replyUserId={}",
                event.replyId(), event.commentId(), event.replyUserId());
    }
}
