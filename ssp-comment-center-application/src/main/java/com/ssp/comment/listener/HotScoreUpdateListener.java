package com.ssp.comment.listener;

import com.ssp.comment.event.CommentLikedEvent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.api.RScoredSortedSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 热评分数更新监听器
 *
 * <p>监听点赞/取消点赞事件，异步更新 Redis ZSet 中的热评排序分数。</p>
 */
@Component
@Slf4j
public class HotScoreUpdateListener {

    private static final String HOT_COMMENT_KEY = "comment:hot:obj:%s:type:%s";

    @Autowired
    private RedissonClient redissonClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentLiked(CommentLikedEvent event) {
        if (!Objects.equals(event.targetType(), 1)) {
            return;
        }
        if (event.commentObjectId() == null || event.commentType() == null) {
            return;
        }
        String key = String.format(HOT_COMMENT_KEY, event.commentObjectId(), event.commentType());
        long hotScore = (long) Math.max(event.newLikeCount() == null ? 0 : event.newLikeCount(), 0) * 100
                + System.currentTimeMillis() / 1000;
        RScoredSortedSet<Long> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        scoredSortedSet.remove(event.targetId());
        scoredSortedSet.add(hotScore, event.targetId());
        log.info("[HotScoreUpdate] commentId={}, newLikeCount={}, hotScore={}",
                event.targetId(), event.newLikeCount(), hotScore);
    }
}
