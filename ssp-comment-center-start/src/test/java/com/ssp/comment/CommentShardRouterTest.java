package com.ssp.comment;

import com.ssp.comment.entity.ShardRoute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 评论表分片路由单元测试
 *
 * <p>验证评论表按 comment_object_id 分库分表后，同一对象下的评论路由到同一物理分片。</p>
 */
class CommentShardRouterTest {

    @Test
    void routeComment_shouldUseCommentObjectIdAsShardKey() {
        Long objectId = 1001L;
        ShardRoute route = CommentShardRouter.routeComment(objectId);

        int expectedDb = Math.abs(Long.hashCode(objectId)) % 2;
        int expectedTable = Math.abs(Long.hashCode(objectId)) % 4;

        assertEquals(expectedDb, route.getDbIndex());
        assertEquals(expectedTable, route.getTableIndex());
        assertEquals("ds_" + expectedDb + ".component_comment_" + expectedTable, route.getPhysicalTable());
    }

    @Test
    void routeComment_sameObjectId_shouldRouteToSameShard() {
        Long objectId = 2024L;
        ShardRoute first = CommentShardRouter.routeComment(objectId);
        ShardRoute second = CommentShardRouter.routeComment(objectId);

        assertEquals(first.getDbIndex(), second.getDbIndex());
        assertEquals(first.getTableIndex(), second.getTableIndex());
        assertEquals(first.getPhysicalTable(), second.getPhysicalTable());
    }

    @Test
    void routeComment_differentObjectIds_shouldCoverAllShards() {
        boolean[] dbHit = new boolean[2];
        boolean[] tableHit = new boolean[4];

        for (long objectId = 1; objectId <= 100; objectId++) {
            ShardRoute route = CommentShardRouter.routeComment(objectId);
            dbHit[route.getDbIndex()] = true;
            tableHit[route.getTableIndex()] = true;
        }

        assertTrue(dbHit[0], "db_0 应该被命中");
        assertTrue(dbHit[1], "db_1 应该被命中");
        assertTrue(tableHit[0], "table_0 应该被命中");
        assertTrue(tableHit[1], "table_1 应该被命中");
        assertTrue(tableHit[2], "table_2 应该被命中");
        assertTrue(tableHit[3], "table_3 应该被命中");
    }

    @Test
    void routeReply_shouldUseCommentIdAsShardKey() {
        Long commentId = 10001L;
        ShardRoute route = CommentShardRouter.routeReply(commentId);

        int expectedDb = Math.abs(Long.hashCode(commentId)) % 2;
        int expectedTable = Math.abs(Long.hashCode(commentId)) % 4;

        assertEquals(expectedDb, route.getDbIndex());
        assertEquals(expectedTable, route.getTableIndex());
        assertEquals("ds_" + expectedDb + ".component_comment_reply_" + expectedTable, route.getPhysicalTable());
    }
}
