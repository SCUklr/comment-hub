package com.ssp.comment.listener;

import com.ssp.comment.common.SnowflakeIdUtils;
import com.ssp.comment.entity.UserCommentIndexEntity;
import com.ssp.comment.event.CommentCreatedEvent;
import com.ssp.comment.event.CommentDeletedEvent;
import com.ssp.comment.event.ReplyCreatedEvent;
import com.ssp.comment.repository.UserCommentIndexRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * 用户评论索引异步更新监听器
 *
 * <p>监听评论/回复创建与删除事件，异步维护用户维度的评论索引表
 * {@code component_user_comment_index}，支撑 "我评论过哪些" 等反查场景。</p>
 *
 * <p>采用 {@link TransactionalEventListener#phase()} = {@link TransactionPhase#AFTER_COMMIT}
 * 确保事务提交后再更新索引，避免事务回滚导致索引脏写。</p>
 */
@Component
@Slf4j
public class UserIndexUpdateListener {

    private static final int INTERACTION_TYPE_COMMENT = 1;
    private static final int INTERACTION_TYPE_REPLY = 2;
    private static final int NORMAL_MARK = 0;

    @Autowired
    private UserCommentIndexRepository userCommentIndexRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentCreated(CommentCreatedEvent event) {
        try {
            UserCommentIndexEntity entity = buildIndexEntity(
                    event.commentUserId(),
                    event.commentObjectId(),
                    event.commentType(),
                    INTERACTION_TYPE_COMMENT,
                    event.commentId(),
                    null,
                    event.content(),
                    event.createTime()
            );
            userCommentIndexRepository.saveOrUpdate(entity);
            log.info("[UserIndexUpdate] commentCreated index saved. commentId={}, userId={}, objectId={}",
                    event.commentId(), event.commentUserId(), event.commentObjectId());
        } catch (Exception e) {
            log.error("[UserIndexUpdate] failed to save comment index. commentId={}, userId={}",
                    event.commentId(), event.commentUserId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onReplyCreated(ReplyCreatedEvent event) {
        try {
            UserCommentIndexEntity entity = buildIndexEntity(
                    event.replyUserId(),
                    event.commentObjectId(),
                    event.commentType(),
                    INTERACTION_TYPE_REPLY,
                    event.commentId(),
                    event.replyId(),
                    event.content(),
                    event.createTime()
            );
            userCommentIndexRepository.saveOrUpdate(entity);
            log.info("[UserIndexUpdate] replyCreated index saved. replyId={}, userId={}, commentId={}",
                    event.replyId(), event.replyUserId(), event.commentId());
        } catch (Exception e) {
            log.error("[UserIndexUpdate] failed to save reply index. replyId={}, userId={}",
                    event.replyId(), event.replyUserId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentDeleted(CommentDeletedEvent event) {
        try {
            if (event.commentObjectId() == null || event.commentType() == null || event.targetAuthorId() == null) {
                log.warn("[UserIndexUpdate] skip delete mark due to missing info. targetType={}, targetId={}",
                        event.targetType(), event.targetId());
                return;
            }
            Integer interactionType = targetTypeToInteractionType(event.targetType());
            if (interactionType == null) {
                log.warn("[UserIndexUpdate] skip delete mark due to unknown targetType. targetType={}, targetId={}",
                        event.targetType(), event.targetId());
                return;
            }
            int rows = userCommentIndexRepository.markDeleted(
                    event.targetAuthorId(),
                    event.commentObjectId(),
                    event.commentType(),
                    interactionType
            );
            log.info("[UserIndexUpdate] commentDeleted index marked. targetType={}, targetId={}, userId={}, rows={}",
                    event.targetType(), event.targetId(), event.targetAuthorId(), rows);
        } catch (Exception e) {
            log.error("[UserIndexUpdate] failed to mark deleted index. targetType={}, targetId={}",
                    event.targetType(), event.targetId(), e);
        }
    }

    private Integer targetTypeToInteractionType(Integer targetType) {
        if (Integer.valueOf(1).equals(targetType)) {
            return INTERACTION_TYPE_COMMENT;
        }
        if (Integer.valueOf(2).equals(targetType)) {
            return INTERACTION_TYPE_REPLY;
        }
        return null;
    }

    private UserCommentIndexEntity buildIndexEntity(Integer userId,
                                                    Long commentObjectId,
                                                    Integer commentType,
                                                    Integer interactionType,
                                                    Long targetCommentId,
                                                    Long targetReplyId,
                                                    String latestContent,
                                                    LocalDateTime latestTime) {
        UserCommentIndexEntity entity = new UserCommentIndexEntity();
        LocalDateTime now = LocalDateTime.now();
        entity.setId(SnowflakeIdUtils.nextId());
        entity.setUserId(userId);
        entity.setCommentObjectId(commentObjectId);
        entity.setCommentType(commentType);
        entity.setInteractionType(interactionType);
        entity.setTargetCommentId(targetCommentId);
        entity.setTargetReplyId(targetReplyId);
        entity.setLatestContent(latestContent);
        entity.setLatestTime(latestTime != null ? latestTime : now);
        entity.setInteractionCount(1);
        entity.setIsDelete(NORMAL_MARK);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        return entity;
    }
}
