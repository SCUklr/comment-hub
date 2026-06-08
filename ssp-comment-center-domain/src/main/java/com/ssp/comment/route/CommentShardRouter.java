package com.ssp.comment.route;

import com.ssp.comment.entity.ShardRoute;

public final class CommentShardRouter {

    private static final int COMMENT_DB_COUNT = 2;
    private static final int COMMENT_TABLE_COUNT = 4;
    private static final int REPLY_DB_COUNT = 2;
    private static final int REPLY_TABLE_COUNT = 4;

    private CommentShardRouter() {
    }

    public static ShardRoute routeComment(Long commentObjectId) {
        int hash = Math.abs(Long.hashCode(commentObjectId));
        int dbIndex = hash % COMMENT_DB_COUNT;
        int tableIndex = hash % COMMENT_TABLE_COUNT;
        return new ShardRoute(dbIndex, tableIndex, "ds_comment_" + dbIndex + ".component_comment_" + tableIndex);
    }

    public static ShardRoute routeReply(Long commentId) {
        int hash = Math.abs(Long.hashCode(commentId));
        int dbIndex = hash % REPLY_DB_COUNT;
        int tableIndex = hash % REPLY_TABLE_COUNT;
        return new ShardRoute(dbIndex, tableIndex, "ds_reply_" + dbIndex + ".component_comment_reply_" + tableIndex);
    }
}
