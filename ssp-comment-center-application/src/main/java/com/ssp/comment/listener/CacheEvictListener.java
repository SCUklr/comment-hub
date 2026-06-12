package com.ssp.comment.listener;

import com.ssp.comment.event.CommentDeletedEvent;
import com.ssp.comment.event.CommentLikedEvent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 缓存清理/刷新监听器
 *
 * <p>监听删除事件和点赞事件，异步清理或刷新 Redis 缓存，避免脏数据。</p>
 */
@Component
@Slf4j
public class CacheEvictListener {

    private static final String COMMENT_LIKE_KEY = "comment:like:obj:%s";
    private static final String REPLY_LIKE_KEY = "reply:like:obj:%s";

    @Autowired
    private RedissonClient redissonClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentDeleted(CommentDeletedEvent event) {
        if (event.commentObjectId() == null) {
            return;
        }
        log.info("[CacheEvict] clear like caches for objectId={}, type={}",
                event.commentObjectId(), event.commentType());
        redissonClient.getMap(String.format(COMMENT_LIKE_KEY, event.commentObjectId())).delete();
        redissonClient.getMap(String.format(REPLY_LIKE_KEY, event.commentObjectId())).delete();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentLiked(CommentLikedEvent event) {
        if (event.commentObjectId() == null) {
            return;
        }
        String key;
        if (Objects.equals(event.targetType(), 1)) {
            key = String.format(COMMENT_LIKE_KEY, event.commentObjectId());
        } else {
            key = String.format(REPLY_LIKE_KEY, event.commentObjectId());
        }
        RMap<Long, Integer> map = redissonClient.getMap(key);
        Integer current = map.get(event.targetId());
        int delta = event.isLike() ? 1 : -1;
        map.put(event.targetId(), Math.max((current == null ? 0 : current) + delta, 0));
        log.info("[CacheEvict] updated likeCount cache for targetId={}, delta={}, newCount={}",
                event.targetId(), delta, map.get(event.targetId()));
    }
}
