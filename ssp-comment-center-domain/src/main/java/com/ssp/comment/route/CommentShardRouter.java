package com.ssp.comment.route;

import com.ssp.comment.entity.ShardRoute;

/**
 * 评论中心分片路由工具类
 *
 * <p>本类与 ShardingSphere-JDBC 配置中的 INLINE 分片算法保持一致，
 * 用于在源码层显式体现分片设计思路，同时作为日志输出和面试讲解的辅助工具。</p>
 *
 * <p>实际物理路由由 ShardingSphere-JDBC 中间件接管，业务代码面向逻辑表开发。</p>
 *
 * <h3>分片策略总览</h3>
 * <ul>
 *   <li><b>评论表 component_comment</b>：按评论对象ID（comment_object_id）分库分表
 *       <br>规模：2库 × 4表。同一对象下的评论落在同一分片，确保对象评论列表查询命中单分片。</li>
 *   <li><b>回复表 component_comment_reply</b>：按评论ID（comment_id）分库分表
 *       <br>规模：2库 × 4表。同一评论下的所有回复落在同一分片，确保楼中楼查询命中单分片。</li>
 *   <li><b>点赞表 component_comment_like</b>：按用户ID（user_id）分库分表
 *       <br>规模：2库 × 4表。支撑高频用户点赞反查。</li>
 *   <li><b>用户索引表 component_user_comment_index</b>：按用户ID（user_id）分库分表
 *       <br>规模：2库 × 4表。支撑"我评论过哪些"反查命中单分片。</li>
 * </ul>
 */
public final class CommentShardRouter {

    private static final int DB_COUNT = 2;
    private static final int TABLE_COUNT = 4;

    private CommentShardRouter() {
    }

    /**
     * 评论表路由：按评论对象ID分库分表
     *
     * <p>同一对象下的评论落在同一物理分片，对象详情页评论列表查询可命中单分片，
     * 避免跨分片聚合。</p>
     *
     * @param commentObjectId 评论对象ID（被评论的内容对象ID，如题解ID、帖子ID）
     * @return 路由结果
     */
    public static ShardRoute routeComment(Long commentObjectId) {
        int hash = Math.abs(Long.hashCode(commentObjectId));
        int dbIndex = hash % DB_COUNT;
        int tableIndex = hash % TABLE_COUNT;
        return new ShardRoute(dbIndex, tableIndex,
                "ds_" + dbIndex + ".component_comment_" + tableIndex);
    }

    /**
     * 回复表路由：按评论ID分库分表
     *
     * <p>回复跟随所属评论的分片，确保同一评论下的所有回复落在同一分片，
     * 楼中楼查询可命中单分片，避免跨分片聚合。</p>
     *
     * @param commentId 所属一级评论ID
     * @return 路由结果
     */
    public static ShardRoute routeReply(Long commentId) {
        int hash = Math.abs(Long.hashCode(commentId));
        int dbIndex = hash % DB_COUNT;
        int tableIndex = hash % TABLE_COUNT;
        return new ShardRoute(dbIndex, tableIndex,
                "ds_" + dbIndex + ".component_comment_reply_" + tableIndex);
    }

    /**
     * 点赞表路由：按用户ID分库分表
     *
     * @param userId 用户ID
     * @return 路由结果
     */
    public static ShardRoute routeLike(Integer userId) {
        int dbIndex = Math.abs(userId) % DB_COUNT;
        int tableIndex = Math.abs(userId) % TABLE_COUNT;
        return new ShardRoute(dbIndex, tableIndex,
                "ds_" + dbIndex + ".component_comment_like_" + tableIndex);
    }

    /**
     * 用户评论索引表路由：按用户ID分库分表
     *
     * @param userId 用户ID
     * @return 路由结果
     */
    public static ShardRoute routeUserIndex(Integer userId) {
        int dbIndex = Math.abs(userId) % DB_COUNT;
        int tableIndex = Math.abs(userId) % TABLE_COUNT;
        return new ShardRoute(dbIndex, tableIndex,
                "ds_" + dbIndex + ".component_user_comment_index_" + tableIndex);
    }
}
