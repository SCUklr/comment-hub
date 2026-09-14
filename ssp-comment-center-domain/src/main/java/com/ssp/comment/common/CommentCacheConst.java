package com.ssp.comment.common;

import java.time.Duration;

/**
 * 评论中心 Redis 缓存常量。
 *
 * <p>Key 模板与 TTL 统一在这里定义，避免 domain 与 application 两个模块各写一份字符串——
 * 之前 {@code comment:like:obj:%s} 这类模板在 3 个文件里各写了一遍，改一处容易漏两处。</p>
 */
public final class CommentCacheConst {

    /** 评论点赞数：Hash，field = commentId，value = likeCount */
    public static final String COMMENT_LIKE_KEY = "comment:like:obj:%s";

    /** 回复点赞数：Hash，field = replyId，value = likeCount */
    public static final String REPLY_LIKE_KEY = "reply:like:obj:%s";

    /** 用户点赞状态：String，key 存在即表示该用户已点赞 */
    public static final String USER_LIKE_KEY = "user:like:%s:target:%s:%s";

    /** 热评榜：ZSet，member = commentId，score = 热度分（见 {@link HotScoreUtils}） */
    public static final String HOT_COMMENT_KEY = "comment:hot:obj:%s:type:%s";

    /**
     * 点赞数缓存 TTL。
     *
     * <p>缓存只是加速层，过期后回源 DB 重建。加了 TTL 就有兜底：即使某次写入出错（例如热评榜里
     * 残留了已删除的评论 ID），最多错 {@code LIKE_CACHE_TTL} 就自动恢复，而不是永久错下去。</p>
     */
    public static final Duration LIKE_CACHE_TTL = Duration.ofHours(1);

    /** 热评榜 TTL（设计值 1 小时，见 docs/02-interview/设计与稳定性追问.md §3.2） */
    public static final Duration HOT_CACHE_TTL = Duration.ofHours(1);

    private CommentCacheConst() {
    }
}
