package com.ssp.comment;

import com.ssp.comment.CommentDomainService.PageResult;
import com.ssp.comment.dao.mapper.CommentMapper;
import com.ssp.comment.entity.CommentEntity;
import com.ssp.comment.entity.ReplyEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 评论表按 comment_object_id 分片集成测试
 *
 * <p>验证对象维度列表查询命中单一物理分片，且评论 CRUD/点赞/置顶等功能正常。</p>
 */
@SpringBootTest(classes = CommentCenterApplication.class)
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
@Transactional
class CommentShardingRoutingIT {

    @Autowired
    private CommentDomainService commentDomainService;

    @Autowired
    private CommentMapper commentMapper;

    private static final Long OBJECT_ID = 999_999_001L;
    private static final Integer OBJECT_TYPE = 1;
    private static final Integer USER_ID = 888_001;

    @BeforeEach
    void setUp() {
        cleanTestData();
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        // 通过对象维度删除评论（测试数据隔离）
        try {
            PageResult<CommentEntity> page = commentDomainService.queryCommentPage(
                    OBJECT_ID, OBJECT_TYPE, 1, 100, 0, USER_ID);
            for (CommentEntity comment : page.list()) {
                commentDomainService.deleteTarget(1, comment.getId(), USER_ID);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    void createComment_shouldRouteToSingleShard_andListQueryShouldHitSingleShard(CapturedOutput output) {
        CommentEntity comment = commentDomainService.createComment(
                OBJECT_ID, OBJECT_TYPE, "测试评论内容", "[]", USER_ID);

        assertNotNull(comment.getId());
        assertEquals(OBJECT_ID, comment.getCommentObjectId());

        // 清空日志，重新发起列表查询，验证只路由到一张物理表
        output.clear();
        PageResult<CommentEntity> page = commentDomainService.queryCommentPage(
                OBJECT_ID, OBJECT_TYPE, 1, 20, 0, USER_ID);

        assertEquals(1L, page.total());
        assertEquals("测试评论内容", page.list().get(0).getContent());

        String log = output.getOut();
        long routeCount = countPhysicalTableOccurrences(log, "component_comment_");
        assertEquals(1, routeCount,
                "评论列表查询应只路由到 1 张评论物理表，实际路由到 " + routeCount + " 张。日志：\n" + log);
    }

    @Test
    void editComment_shouldUpdateSuccessfullyWithShardKey() {
        CommentEntity comment = commentDomainService.createComment(
                OBJECT_ID, OBJECT_TYPE, "原始内容", "[]", USER_ID);

        CommentEntity edited = commentDomainService.editComment(
                comment.getId(), "编辑后内容", "[]");

        assertEquals("编辑后内容", edited.getContent());

        PageResult<CommentEntity> page = commentDomainService.queryCommentPage(
                OBJECT_ID, OBJECT_TYPE, 1, 20, 0, USER_ID);
        assertEquals("编辑后内容", page.list().get(0).getContent());
    }

    @Test
    void pinComment_shouldMoveCommentToTop() {
        CommentEntity first = commentDomainService.createComment(
                OBJECT_ID, OBJECT_TYPE, "第一条", "[]", USER_ID);
        CommentEntity second = commentDomainService.createComment(
                OBJECT_ID, OBJECT_TYPE, "第二条", "[]", USER_ID);

        commentDomainService.pinComment(second.getId(), true);

        PageResult<CommentEntity> page = commentDomainService.queryCommentPage(
                OBJECT_ID, OBJECT_TYPE, 1, 20, 0, USER_ID);
        assertEquals(second.getId(), page.list().get(0).getId());
    }

    @Test
    void likeComment_shouldIncreaseLikeCount() {
        CommentEntity comment = commentDomainService.createComment(
                OBJECT_ID, OBJECT_TYPE, "待点赞", "[]", USER_ID);

        commentDomainService.like(USER_ID + 1, comment.getId(), 1);

        PageResult<CommentEntity> page = commentDomainService.queryCommentPage(
                OBJECT_ID, OBJECT_TYPE, 1, 20, 0, USER_ID + 1);
        assertEquals(1, page.list().get(0).getLikeCount());
    }

    @Test
    void createReply_shouldRouteToSingleShard_andUpdateReplyCount() {
        CommentEntity comment = commentDomainService.createComment(
                OBJECT_ID, OBJECT_TYPE, "父评论", "[]", USER_ID);

        ReplyEntity reply = commentDomainService.createReply(
                comment.getId(), 0L, -1, "回复内容", "[]", USER_ID + 1);

        assertNotNull(reply.getId());
        assertEquals(comment.getId(), reply.getCommentId());

        PageResult<CommentEntity> page = commentDomainService.queryCommentPage(
                OBJECT_ID, OBJECT_TYPE, 1, 20, 3, USER_ID);
        assertEquals(1, page.list().get(0).getReplyCount());
    }

    /**
     * 统计 ShardingSphere 路由日志中实际命中的评论物理表数量
     */
    private long countPhysicalTableOccurrences(String log, String tablePrefix) {
        return java.util.Arrays.stream(log.split("\n"))
                .filter(line -> line.contains("Actual SQL") || line.contains("Logic SQL"))
                .flatMap(line -> java.util.Arrays.stream(line.split("\\s+")))
                .filter(token -> token.startsWith(tablePrefix) && token.length() > tablePrefix.length())
                .map(token -> token.replaceAll("[^a-z0-9_]", ""))
                .filter(token -> token.matches(tablePrefix + "[0-9]+"))
                .distinct()
                .count();
    }
}
