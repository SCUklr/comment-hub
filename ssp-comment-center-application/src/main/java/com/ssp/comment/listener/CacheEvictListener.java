package com.ssp.comment.listener;

import com.ssp.comment.common.CommentCacheConst;
import com.ssp.comment.event.CommentDeletedEvent;
import com.ssp.comment.event.CommentLikedEvent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 * 缓存清理/刷新监听器
 *
 * <p>监听删除事件和点赞事件，异步清理或刷新 Redis 缓存，避免脏数据。</p>
 *
 * <p><b>本次改造两个要点</b>：</p>
 * <ol>
 *   <li>点赞数改用 {@code RMap.addAndGet} <b>原子自增</b>。原来是"读出来 + 1 再写回去"，
 *       两个人同时点赞会互相覆盖，导致计数少 1（典型并发丢更新）。</li>
 *   <li>写入后 <b>续期 TTL</b>，并给整个方法加 try/catch：Redis 抖动只记日志，
 *       不能把异步线程打断，也不能让脏数据永久留在缓存里。</li>
 * </ol>
 */
@Component
@Slf4j
public class CacheEvictListener {

    @Autowired
    private RedissonClient redissonClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentDeleted(CommentDeletedEvent event) {
        if (event.commentObjectId() == null) {
            return;
        }
        try {
            log.info("[CacheEvict] clear like caches for objectId={}, type={}",
                    event.commentObjectId(), event.commentType());
            redissonClient.getMap(String.format(CommentCacheConst.COMMENT_LIKE_KEY, event.commentObjectId())).delete();
            redissonClient.getMap(String.format(CommentCacheConst.REPLY_LIKE_KEY, event.commentObjectId())).delete();
        } catch (Exception e) {
            log.warn("[CacheEvict] 删除点赞缓存失败（TTL 兜底，稍后自动过期）。objectId={}",
                    event.commentObjectId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("commentEventExecutor")
    public void onCommentLiked(CommentLikedEvent event) {
        if (event.commentObjectId() == null) {
            return;
        }
        String key = Objects.equals(event.targetType(), 1)
                ? String.format(CommentCacheConst.COMMENT_LIKE_KEY, event.commentObjectId())
                : String.format(CommentCacheConst.REPLY_LIKE_KEY, event.commentObjectId());
        int delta = event.isLike() ? 1 : -1;
        try {
            RMap<Long, Integer> map = redissonClient.getMap(key);
            // 原子自增：并发点赞不会丢更新
            Integer newCount = map.addAndGet(event.targetId(), delta);
            if (newCount != null && newCount < 0) {
                // 兜底：计数不应为负（正常路径不会走到这里）
                map.put(event.targetId(), 0);
                newCount = 0;
            }
            map.expire(CommentCacheConst.LIKE_CACHE_TTL);
            log.info("[CacheEvict] updated likeCount cache for targetId={}, delta={}, newCount={}",
                    event.targetId(), delta, newCount);
        } catch (Exception e) {
            log.warn("[CacheEvict] 刷新点赞数缓存失败（TTL 兜底）。key={}, targetId={}",
                    key, event.targetId(), e);
        }
    }
}
