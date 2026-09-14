package com.ssp.comment.common;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 热度分计算：{@code score = likeCount * LIKE_WEIGHT + 创建时间(小时)}。
 *
 * <p><b>为什么这么改</b>：原公式是 {@code likeCount * 100 + 当前秒级时间戳}，而秒级时间戳量级为
 * 1.7×10⁹，点赞项要 <b>1700 万赞</b>才能抵消 1 秒的时间差 —— 实际效果退化成"最新优先"，
 * 一条 1000 赞的优质老评论会被十几分钟后发的 0 赞新评论顶下去。</p>
 *
 * <p>现在改用评论自身的"创建时间"并给一个量级可控的小权重：1 个赞 = {@link #LIKE_WEIGHT} 小时
 * （约 114 年）的时间差。于是：</p>
 * <ul>
 *   <li><b>赞数主导排序</b>——赞多的排前面；</li>
 *   <li><b>时间只做同分时的先后</b>——赞数相同时，新评论靠前；</li>
 *   <li>不再使用"当前时间"，所以分数<b>不会随时间自己漂移</b>（原公式里老评论会越来越吃亏）。</li>
 * </ul>
 */
public final class HotScoreUtils {

    /** 1 个赞折算的时间权重（小时）。取 100 万小时 ≈ 114 年，保证赞数几乎总是主导排序。 */
    public static final long LIKE_WEIGHT = 1_000_000L;

    private HotScoreUtils() {
    }

    /**
     * 计算热度分。
     *
     * @param likeCount      点赞数，null 或负数按 0 处理
     * @param createTimeHour 评论创建时间折算的小时数，见 {@link #toEpochHour(LocalDateTime)}
     */
    public static long score(Integer likeCount, long createTimeHour) {
        long likes = Math.max(likeCount == null ? 0 : likeCount, 0);
        return likes * LIKE_WEIGHT + createTimeHour;
    }

    /**
     * 把创建时间折算为"自纪元起的小时数"。
     *
     * <p>用评论自身的时间而不是当前时间，是为了让同一条评论的热度分在多次点赞之间保持可比。</p>
     *
     * @return 创建时间对应的小时数；{@code null} 返回 0（防御性处理，正常 DB 数据不应为 null）
     */
    public static long toEpochHour(LocalDateTime createTime) {
        if (createTime == null) {
            return 0L;
        }
        return createTime.atZone(ZoneId.systemDefault()).toEpochSecond() / 3600;
    }
}
