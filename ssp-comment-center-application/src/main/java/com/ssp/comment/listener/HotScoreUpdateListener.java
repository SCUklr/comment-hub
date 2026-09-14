package com.ssp.comment.listener;

import com.ssp.comment.common.CommentCacheConst;
import com.ssp.comment.common.HotScoreUtils;
import com.ssp.comment.event.CommentAuditChangedEvent;
import com.ssp.comment.event.CommentLikedEvent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 * 热评分数更新监听器
 *
 * <p>监听点赞/取消点赞事件，异步更新 Redis ZSet 中的热评排序分数；审核拒绝时把评论移出热评榜。</p>
 *
 * <p><b>分数公式已修正</b>（见 {@link HotScoreUtils}）：原公式 {@code likeCount * 100 + 当前秒级时间戳}
 * 里时间戳量级 1.7×10⁹，点赞项要 1700 万赞才能抵消 1 秒差，实际退化成"最新优先"。
 * 现在改成 {@code likeCount * 1000000 + 创建时间(小时)}：赞数主导排序，时间只在赞数相同时决定先后，
 * 且不再因为"当前时间"不断增大而漂移。</p>
 *
 * <p>每次写入都续期 {@link CommentCacheConst#HOT_CACHE_TTL}，保证脏数据不会永久留在榜上。</p>
 */
@Component
@Slf4j
public class HotScoreUpdateListener {

    @Autowired
    private RedissonClient redissonClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentLiked(CommentLikedEvent event) {
        if (!Objects.equals(event.targetType(), 1)) {
            return; // 热评榜只收一级评论，回复不参与
        }
        if (event.commentObjectId() == null || event.commentType() == null) {
            return;
        }
        String key = String.format(CommentCacheConst.HOT_COMMENT_KEY, event.commentObjectId(), event.commentType());
        try {
            long hotScore = HotScoreUtils.score(event.newLikeCount(), event.hotBase());
            RScoredSortedSet<Long> scoredSortedSet = redissonClient.getScoredSortedSet(key);
            scoredSortedSet.remove(event.targetId());
            scoredSortedSet.add(hotScore, event.targetId());
            scoredSortedSet.expire(CommentCacheConst.HOT_CACHE_TTL);
            log.info("[HotScoreUpdate] commentId={}, newLikeCount={}, hotScore={}",
                    event.targetId(), event.newLikeCount(), hotScore);
        } catch (Exception e) {
            log.warn("[HotScoreUpdate] 更新热评分数失败（TTL 兜底）。key={}, commentId={}",
                    key, event.targetId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentAuditChanged(CommentAuditChangedEvent event) {
        if (!Objects.equals(event.targetType(), 1)) {
            return;
        }
        if (!Objects.equals(event.auditStatus(), 2)) {
            return;
        }
        if (event.commentObjectId() == null || event.commentType() == null) {
            return;
        }
        String key = String.format(CommentCacheConst.HOT_COMMENT_KEY, event.commentObjectId(), event.commentType());
        try {
            RScoredSortedSet<Long> scoredSortedSet = redissonClient.getScoredSortedSet(key);
            scoredSortedSet.remove(event.targetId());
            log.info("[HotScoreUpdate] commentId={} removed from hot set due to audit rejection. key={}",
                    event.targetId(), key);
        } catch (Exception e) {
            log.warn("[HotScoreUpdate] 审核拒绝后移除热评失败（TTL 兜底）。key={}, commentId={}",
                    key, event.targetId(), e);
        }
    }
}
