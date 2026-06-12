package com.ssp.comment.listener;

import com.ssp.comment.common.SnowflakeIdUtils;
import com.ssp.comment.entity.NotificationEntity;
import com.ssp.comment.event.CommentLikedEvent;
import com.ssp.comment.event.ReplyCreatedEvent;
import com.ssp.comment.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 站内通知监听器
 *
 * <p>监听回复创建、点赞事件，异步写入站内通知表。</p>
 */
@Component
@Slf4j
public class NotificationListener {

    private static final int TYPE_REPLY = 1;
    private static final int TYPE_LIKE = 2;

    private static final int SUBJECT_TYPE_COMMENT = 1;
    private static final int SUBJECT_TYPE_REPLY = 2;

    private static final int MAX_CONTENT_LENGTH = 200;

    @Autowired
    private NotificationRepository notificationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onReplyCreated(ReplyCreatedEvent event) {
        log.info("[Notification] replyCreated event: replyId={}, commentId={}, replyUserId={}",
                event.replyId(), event.commentId(), event.replyUserId());

        Set<Integer> notifiedUsers = new HashSet<>();
        LocalDateTime now = event.createTime() != null ? event.createTime() : LocalDateTime.now();
        String content = truncate(event.content());

        // 通知评论作者：有人回复了你的评论
        Integer commentAuthorId = event.commentAuthorId();
        if (commentAuthorId != null && !commentAuthorId.equals(event.replyUserId())) {
            saveNotification(commentAuthorId, TYPE_REPLY, event.replyId(), SUBJECT_TYPE_REPLY,
                    event.replyUserId(), event.commentObjectId(), event.commentType(), content, now);
            notifiedUsers.add(commentAuthorId);
        }

        // 通知被回复人：有人回复了你
        Integer beRepliedUserId = event.beRepliedUserId();
        if (beRepliedUserId != null && beRepliedUserId > 0
                && !beRepliedUserId.equals(event.replyUserId())
                && !notifiedUsers.contains(beRepliedUserId)) {
            saveNotification(beRepliedUserId, TYPE_REPLY, event.replyId(), SUBJECT_TYPE_REPLY,
                    event.replyUserId(), event.commentObjectId(), event.commentType(), content, now);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentLiked(CommentLikedEvent event) {
        log.info("[Notification] commentLiked event: targetId={}, targetType={}, userId={}",
                event.targetId(), event.targetType(), event.userId());

        if (!event.isLike()) {
            return;
        }

        Integer targetAuthorId = event.targetAuthorId();
        if (targetAuthorId != null && !targetAuthorId.equals(event.userId())) {
            LocalDateTime now = LocalDateTime.now();
            saveNotification(targetAuthorId, TYPE_LIKE, event.targetId(), event.targetType(),
                    event.userId(), event.commentObjectId(), event.commentType(), null, now);
        }
    }

    private void saveNotification(Integer userId, int type, Long subjectId, int subjectType,
                                  Integer actorId, Long commentObjectId, Integer commentType,
                                  String content, LocalDateTime createTime) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(SnowflakeIdUtils.nextId());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setSubjectId(subjectId);
        entity.setSubjectType(subjectType);
        entity.setActorId(actorId);
        entity.setCommentObjectId(commentObjectId);
        entity.setCommentType(commentType);
        entity.setContent(content);
        entity.setIsRead(0);
        entity.setIsDelete(0);
        entity.setCreateTime(createTime);
        entity.setUpdateTime(createTime);
        try {
            notificationRepository.save(entity);
        } catch (Exception e) {
            log.error("[Notification] save notification failed, userId={}, type={}", userId, type, e);
        }
    }

    private String truncate(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_CONTENT_LENGTH);
    }
}
