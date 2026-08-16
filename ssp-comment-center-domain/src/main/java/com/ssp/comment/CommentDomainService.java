package com.ssp.comment;

import com.ssp.comment.common.BizException;
import com.ssp.comment.common.SnowflakeIdUtils;
import com.ssp.comment.entity.*;
import com.ssp.comment.event.CommentAuditChangedEvent;
import com.ssp.comment.event.CommentCreatedEvent;
import com.ssp.comment.event.CommentDeletedEvent;
import com.ssp.comment.event.CommentLikedEvent;
import com.ssp.comment.event.ReplyCreatedEvent;
import com.ssp.comment.repository.*;
import com.ssp.comment.route.CommentShardRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommentDomainService {

    private static final int DELETE_MARK = 1;
    private static final int NORMAL_MARK = 0;
    private static final int PIN_SORT = 999;
    private static final int COMMENT_TARGET_TYPE = 1;
    private static final int REPLY_TARGET_TYPE = 2;
    private static final String COMMENT_LIKE_KEY = "comment:like:obj:%s";
    private static final String REPLY_LIKE_KEY = "reply:like:obj:%s";
    private static final String USER_LIKE_KEY = "user:like:%s:target:%s:%s";
    private static final String HOT_COMMENT_KEY = "comment:hot:obj:%s:type:%s";

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentAuditRepository commentAuditRepository;
    private final UserCommentIndexRepository userCommentIndexRepository;
    private final RedissonClient redissonClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public CommentEntity createComment(Long commentObjectId,
                                       Integer commentType,
                                       String content,
                                       String images,
                                       Integer userId) {
        validateCommentCreate(commentObjectId, commentType, content, userId);
        ShardRoute route = CommentShardRouter.routeComment(commentObjectId);
        log.info("[CommentDomainService.createComment] shard route -> db={}, table={}, physical={}",
                route.getDbIndex(), route.getTableIndex(), route.getPhysicalTable());

        CommentEntity entity = new CommentEntity();
        entity.setId(SnowflakeIdUtils.nextId());
        entity.setCommentObjectId(commentObjectId);
        entity.setCommentType(commentType);
        entity.setContent(content);
        entity.setImages(defaultImages(images));
        entity.setCommentUserId(userId);
        entity.setSort(0);
        entity.setReplyCount(0);
        entity.setLikeCount(0);
        entity.setAuditStatus(1);
        entity.setIsDelete(NORMAL_MARK);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        commentRepository.save(entity);
        saveDefaultPassedAuditRecord(entity.getId(), COMMENT_TARGET_TYPE, content);

        eventPublisher.publishEvent(new CommentCreatedEvent(
            entity.getId(), commentObjectId, commentType, userId, content, entity.getCreateTime()
        ));
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public ReplyEntity createReply(Long commentId,
                                   Long parentId,
                                   Integer beRepliedUserId,
                                   String content,
                                   String images,
                                   Integer userId) {
        validateReplyCreate(commentId, content, userId);
        CommentEntity parentComment = commentRepository.queryById(commentId);
        if (parentComment == null || Objects.equals(parentComment.getIsDelete(), DELETE_MARK)) {
            throw new BizException("评论不存在");
        }
        ShardRoute route = CommentShardRouter.routeReply(commentId);
        log.info("[CommentDomainService.createReply] route reply shard -> {}", route.getPhysicalTable());

        ReplyEntity entity = new ReplyEntity();
        entity.setId(SnowflakeIdUtils.nextId());
        entity.setCommentId(commentId);
        entity.setParentReplyId(parentId == null ? 0L : parentId);
        entity.setReplyType(parentId == null || parentId == 0L ? 1 : 2);
        entity.setContent(content);
        entity.setImages(defaultImages(images));
        entity.setReplyUserId(userId);
        entity.setBeRepliedUserId(beRepliedUserId == null ? -1 : beRepliedUserId);
        entity.setLikeCount(0);
        entity.setAuditStatus(1);
        entity.setIsDelete(NORMAL_MARK);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        replyRepository.save(entity);
        commentRepository.updateReplyCount(commentId, parentComment.getCommentObjectId(), 1);
        saveDefaultPassedAuditRecord(entity.getId(), REPLY_TARGET_TYPE, content);

        eventPublisher.publishEvent(new ReplyCreatedEvent(
            entity.getId(), commentId, parentId, entity.getReplyType(), userId, content, entity.getCreateTime(),
            parentComment.getCommentObjectId(), parentComment.getCommentType(),
            entity.getBeRepliedUserId(), parentComment.getCommentUserId()
        ));
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTarget(Integer type, Long id, Integer userId) {
        if (type == null || id == null) {
            throw new BizException("删除参数不能为空");
        }
        if (type == COMMENT_TARGET_TYPE) {
            CommentEntity comment = commentRepository.queryById(id);
            if (comment == null || Objects.equals(comment.getIsDelete(), DELETE_MARK)) {
                throw new BizException("评论不存在");
            }
            checkOwner(comment.getCommentUserId(), userId, "评论");
            commentRepository.updateDeleteMark(id, comment.getCommentObjectId(), userId);
            eventPublisher.publishEvent(new CommentDeletedEvent(
                type, id, comment.getCommentObjectId(), comment.getCommentType(), userId, comment.getCommentUserId()
            ));
            return;
        }

        ReplyEntity reply = replyRepository.queryById(id);
        if (reply == null || Objects.equals(reply.getIsDelete(), DELETE_MARK)) {
            throw new BizException("回复不存在");
        }
        checkOwner(reply.getReplyUserId(), userId, "回复");
        replyRepository.updateDeleteMark(id, userId);
        CommentEntity comment = commentRepository.queryById(reply.getCommentId());
        if (comment != null) {
            commentRepository.updateReplyCount(reply.getCommentId(), comment.getCommentObjectId(), -1);
        }
        eventPublisher.publishEvent(new CommentDeletedEvent(
            type, id,
            comment != null ? comment.getCommentObjectId() : null,
            comment != null ? comment.getCommentType() : null,
            userId,
            reply.getReplyUserId()
        ));
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentEntity editComment(Long id, String content, String images, Integer userId) {
        CommentEntity comment = commentRepository.queryById(id);
        if (comment == null || Objects.equals(comment.getIsDelete(), DELETE_MARK)) {
            throw new BizException("评论不存在");
        }
        checkOwner(comment.getCommentUserId(), userId, "评论");
        commentRepository.updateContent(id, comment.getCommentObjectId(), content, defaultImages(images));
        comment.setContent(content);
        comment.setImages(defaultImages(images));
        comment.setUpdateTime(LocalDateTime.now());
        return comment;
    }

    @Transactional(rollbackFor = Exception.class)
    public ReplyEntity editReply(Long id, String content, String images, Integer userId) {
        ReplyEntity reply = replyRepository.queryById(id);
        if (reply == null || Objects.equals(reply.getIsDelete(), DELETE_MARK)) {
            throw new BizException("回复不存在");
        }
        checkOwner(reply.getReplyUserId(), userId, "回复");
        replyRepository.updateContent(id, content, defaultImages(images));
        reply.setContent(content);
        reply.setImages(defaultImages(images));
        reply.setUpdateTime(LocalDateTime.now());
        return reply;
    }

    @Transactional(rollbackFor = Exception.class)
    public void pinComment(Long commentId, boolean pin) {
        CommentEntity comment = commentRepository.queryById(commentId);
        if (comment == null || Objects.equals(comment.getIsDelete(), DELETE_MARK)) {
            throw new BizException("评论不存在");
        }
        commentRepository.updatePin(commentId, comment.getCommentObjectId(), pin);
    }

    public PageResult<CommentEntity> queryCommentPage(Long commentObjectId,
                                                      Integer commentType,
                                                      Integer page,
                                                      Integer pageSize,
                                                      Integer topReplyLimit,
                                                      Integer currentUserId) {
        validateCommentQuery(commentObjectId, commentType, page, pageSize);
        commentLikedCarrier.clear();
        replyLikedCarrier.clear();
        commentLikeReplyCarrier.clear();
        // 查询场景：评论表按 comment_object_id 分库分表。
        // 此处 SQL 条件含 comment_object_id，可精准命中单一物理分片。
        log.info("[CommentDomainService.queryCommentPage] comment_objectId={}, single-shard routing expected", commentObjectId);

        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;
        long total = commentRepository.countByObject(commentObjectId, commentType, currentUserId);
        List<CommentEntity> comments = commentRepository.queryPageByObject(commentObjectId, commentType, offset, safePageSize, currentUserId);
        if (CollectionUtils.isEmpty(comments)) {
            return new PageResult<>(total, safePage, safePageSize, comments);
        }

        batchFillCommentLikeCount(commentObjectId, comments, currentUserId);

        List<Long> commentIds = comments.stream().map(CommentEntity::getId).collect(Collectors.toList());
        List<ReplyEntity> replies = replyRepository.queryByCommentIdsAndParent(commentIds, 0L, currentUserId);
        batchFillReplyLikeCount(commentObjectId, replies, currentUserId);
        Map<Long, List<ReplyEntity>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(ReplyEntity::getCommentId));
        int limit = topReplyLimit == null || topReplyLimit <= 0 ? 3 : topReplyLimit;
        for (CommentEntity comment : comments) {
            List<ReplyEntity> topReplies = replyMap.getOrDefault(comment.getId(), new ArrayList<>()).stream()
                    .sorted(Comparator.comparing(ReplyEntity::getCreateTime))
                    .limit(limit)
                    .collect(Collectors.toList());
            comment.setUpdateTime(comment.getUpdateTime());
            commentLikeReplyCarrier.put(comment.getId(), topReplies);
        }
        return new PageResult<>(total, safePage, safePageSize, comments);
    }

    public PageResult<ReplyEntity> queryReplyPage(Long commentId,
                                                  Long parentReplyId,
                                                  Integer page,
                                                  Integer pageSize,
                                                  Integer currentUserId) {
        if (commentId == null) {
            throw new BizException("评论ID不能为空");
        }
        ShardRoute route = CommentShardRouter.routeReply(commentId);
        log.info("[CommentDomainService.queryReplyPage] route reply shard -> {}", route.getPhysicalTable());

        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;
        long total = replyRepository.countByCommentAndParent(commentId, parentReplyId == null ? 0L : parentReplyId, currentUserId);
        List<ReplyEntity> replies = replyRepository.queryPageByCommentAndParent(commentId, parentReplyId == null ? 0L : parentReplyId, offset, safePageSize, currentUserId);
        CommentEntity comment = commentRepository.queryById(commentId);
        if (comment != null) {
            replyLikedCarrier.clear();
            batchFillReplyLikeCount(comment.getCommentObjectId(), replies, currentUserId);
        }
        return new PageResult<>(total, safePage, safePageSize, replies);
    }

    @Transactional(rollbackFor = Exception.class)
    public LikeResult like(Integer userId, Long targetId, Integer targetType) {
        if (commentLikeRepository.exists(userId, targetId, targetType)) {
            throw new BizException("请勿重复点赞");
        }
        commentLikeRepository.save(buildLikeEntity(userId, targetId, targetType));

        Integer newLikeCount = null;
        Long commentObjectId = null;
        Integer commentType = null;
        Integer targetAuthorId = null;
        if (Objects.equals(targetType, COMMENT_TARGET_TYPE)) {
            CommentEntity comment = commentRepository.queryById(targetId);
            if (comment == null) {
                throw new BizException("评论不存在");
            }
            commentRepository.updateLikeCount(targetId, comment.getCommentObjectId(), 1);
            markUserLiked(userId, targetId, targetType, true);
            newLikeCount = comment.getLikeCount() + 1;
            commentObjectId = comment.getCommentObjectId();
            commentType = comment.getCommentType();
            targetAuthorId = comment.getCommentUserId();
        } else {
            ReplyEntity reply = replyRepository.queryById(targetId);
            if (reply == null) {
                throw new BizException("回复不存在");
            }
            replyRepository.updateLikeCount(targetId, 1);
            CommentEntity comment = commentRepository.queryById(reply.getCommentId());
            if (comment != null) {
                commentObjectId = comment.getCommentObjectId();
                commentType = comment.getCommentType();
            }
            markUserLiked(userId, targetId, targetType, true);
            newLikeCount = reply.getLikeCount() + 1;
            targetAuthorId = reply.getReplyUserId();
        }
        eventPublisher.publishEvent(new CommentLikedEvent(
            targetId, targetType, userId, commentObjectId, commentType, newLikeCount, true, targetAuthorId
        ));
        return new LikeResult(targetId, targetType, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public LikeResult unlike(Integer userId, Long targetId, Integer targetType) {
        boolean deleted = commentLikeRepository.delete(userId, targetId, targetType);
        if (!deleted) {
            throw new BizException("当前未点赞，无需取消");
        }

        Integer newLikeCount = null;
        Long commentObjectId = null;
        Integer commentType = null;
        Integer targetAuthorId = null;
        if (Objects.equals(targetType, COMMENT_TARGET_TYPE)) {
            CommentEntity comment = commentRepository.queryById(targetId);
            if (comment == null) {
                throw new BizException("评论不存在");
            }
            commentRepository.updateLikeCount(targetId, comment.getCommentObjectId(), -1);
            markUserLiked(userId, targetId, targetType, false);
            newLikeCount = Math.max(comment.getLikeCount() - 1, 0);
            commentObjectId = comment.getCommentObjectId();
            commentType = comment.getCommentType();
            targetAuthorId = comment.getCommentUserId();
        } else {
            ReplyEntity reply = replyRepository.queryById(targetId);
            if (reply == null) {
                throw new BizException("回复不存在");
            }
            replyRepository.updateLikeCount(targetId, -1);
            CommentEntity comment = commentRepository.queryById(reply.getCommentId());
            if (comment != null) {
                commentObjectId = comment.getCommentObjectId();
                commentType = comment.getCommentType();
            }
            markUserLiked(userId, targetId, targetType, false);
            newLikeCount = Math.max(reply.getLikeCount() - 1, 0);
            targetAuthorId = reply.getReplyUserId();
        }
        eventPublisher.publishEvent(new CommentLikedEvent(
            targetId, targetType, userId, commentObjectId, commentType, newLikeCount, false, targetAuthorId
        ));
        return new LikeResult(targetId, targetType, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleAuditCallback(Long targetId,
                                    Integer targetType,
                                    Integer auditStatus,
                                    String auditReason,
                                    Long auditOperator) {
        CommentEntity comment = null;
        ReplyEntity reply = null;
        Integer previousAuditStatus = null;
        Long commentObjectId = null;
        Integer commentType = null;
        Integer authorId = null;

        if (Objects.equals(targetType, COMMENT_TARGET_TYPE)) {
            comment = commentRepository.queryById(targetId);
            if (comment != null) {
                previousAuditStatus = comment.getAuditStatus();
                commentObjectId = comment.getCommentObjectId();
                commentType = comment.getCommentType();
                authorId = comment.getCommentUserId();
            }
        } else {
            reply = replyRepository.queryById(targetId);
            if (reply != null) {
                previousAuditStatus = reply.getAuditStatus();
                authorId = reply.getReplyUserId();
                CommentEntity parentComment = commentRepository.queryById(reply.getCommentId());
                if (parentComment != null) {
                    commentObjectId = parentComment.getCommentObjectId();
                    commentType = parentComment.getCommentType();
                }
            }
        }

        String contentSnapshot = comment != null ? comment.getContent() : (reply != null ? reply.getContent() : null);

        CommentAuditEntity auditEntity = new CommentAuditEntity();
        auditEntity.setId(SnowflakeIdUtils.nextId());
        auditEntity.setTargetId(targetId);
        auditEntity.setTargetType(targetType);
        auditEntity.setAuditContent(StringUtils.defaultString(contentSnapshot));
        auditEntity.setAuditStatus(auditStatus);
        auditEntity.setAuditReason(StringUtils.defaultString(auditReason));
        auditEntity.setAuditOperator(auditOperator == null ? 0L : auditOperator);
        auditEntity.setAuditTime(LocalDateTime.now());
        commentAuditRepository.save(auditEntity);

        if (Objects.equals(targetType, COMMENT_TARGET_TYPE)) {
            if (comment != null) {
                commentRepository.updateAuditStatus(targetId, comment.getCommentObjectId(), auditStatus);
            }
        } else {
            replyRepository.updateAuditStatus(targetId, auditStatus);
        }

        // 先发后审：回复从「通过」变为「拒绝」时，回退父评论的回复数
        if (Objects.equals(targetType, REPLY_TARGET_TYPE)
                && Objects.equals(auditStatus, 2)
                && Objects.equals(previousAuditStatus, 1)
                && reply != null) {
            CommentEntity parentComment = commentRepository.queryById(reply.getCommentId());
            if (parentComment != null) {
                commentRepository.updateReplyCount(reply.getCommentId(), parentComment.getCommentObjectId(), -1);
            }
        }

        eventPublisher.publishEvent(new CommentAuditChangedEvent(
                targetId, targetType, auditStatus, LocalDateTime.now(),
                commentObjectId, commentType, authorId
        ));
    }

    public PageResult<CommentEntity> queryCommentPageForAudit(Long commentObjectId,
                                                              Integer commentType,
                                                              Integer page,
                                                              Integer pageSize,
                                                              Integer topReplyLimit) {
        validateCommentQuery(commentObjectId, commentType, page, pageSize);
        commentLikedCarrier.clear();
        replyLikedCarrier.clear();
        commentLikeReplyCarrier.clear();

        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;
        long total = commentRepository.countForAudit(commentObjectId, commentType);
        List<CommentEntity> comments = commentRepository.queryPageForAudit(commentObjectId, commentType, offset, safePageSize);
        if (CollectionUtils.isEmpty(comments)) {
            return new PageResult<>(total, safePage, safePageSize, comments);
        }

        batchFillCommentLikeCount(commentObjectId, comments, null);

        List<Long> commentIds = comments.stream().map(CommentEntity::getId).collect(Collectors.toList());
        List<ReplyEntity> replies = replyRepository.queryByCommentIdsForAudit(commentIds);
        batchFillReplyLikeCount(commentObjectId, replies, null);
        Map<Long, List<ReplyEntity>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(ReplyEntity::getCommentId));
        int limit = topReplyLimit == null || topReplyLimit <= 0 ? 3 : topReplyLimit;
        for (CommentEntity comment : comments) {
            List<ReplyEntity> topReplies = replyMap.getOrDefault(comment.getId(), new ArrayList<>()).stream()
                    .sorted(Comparator.comparing(ReplyEntity::getCreateTime))
                    .limit(limit)
                    .collect(Collectors.toList());
            comment.setUpdateTime(comment.getUpdateTime());
            commentLikeReplyCarrier.put(comment.getId(), topReplies);
        }
        return new PageResult<>(total, safePage, safePageSize, comments);
    }

    public PageResult<CommentEntity> queryHotComments(Long commentObjectId,
                                                      Integer commentType,
                                                      Integer page,
                                                      Integer pageSize,
                                                      Integer currentUserId) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;
        String hotKey = String.format(HOT_COMMENT_KEY, commentObjectId, commentType);
        RScoredSortedSet<Long> hotSet = redissonClient.getScoredSortedSet(hotKey);

        List<Long> hotIds = new ArrayList<>(hotSet.valueRangeReversed(offset, offset + safePageSize - 1));
        List<CommentEntity> comments;
        if (CollectionUtils.isNotEmpty(hotIds)) {
            comments = commentRepository.queryByIds(hotIds, commentObjectId);
            Map<Long, CommentEntity> commentMap = comments.stream().collect(Collectors.toMap(CommentEntity::getId, item -> item, (a, b) -> a));
            comments = hotIds.stream().map(commentMap::get).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            comments = commentRepository.queryHotPageByObject(commentObjectId, commentType, offset, safePageSize, currentUserId);
            for (CommentEntity comment : comments) {
                updateHotCommentScore(commentObjectId, commentType, comment.getId(), comment.getLikeCount());
            }
        }
        batchFillCommentLikeCount(commentObjectId, comments, currentUserId);
        long total = commentRepository.countByObject(commentObjectId, commentType, currentUserId);
        return new PageResult<>(total, safePage, safePageSize, comments);
    }

    public List<CommentAuditEntity> queryAuditHistory(Long targetId, Integer targetType) {
        if (targetId == null || targetType == null) {
            throw new BizException("目标ID和目标类型不能为空");
        }
        return commentAuditRepository.queryByTarget(targetId, targetType);
    }

    public PageResult<UserCommentIndexEntity> queryMyComments(Integer userId,
                                                              Integer commentType,
                                                              Integer interactionType,
                                                              Integer page,
                                                              Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;
        long total = userCommentIndexRepository.countByUser(userId, commentType, interactionType);
        List<UserCommentIndexEntity> indexes = userCommentIndexRepository.queryPageByUser(userId, commentType, interactionType, offset, safePageSize);
        return new PageResult<>(total, safePage, safePageSize, indexes);
    }

    private void validateCommentCreate(Long commentObjectId, Integer commentType, String content, Integer userId) {
        if (commentObjectId == null || commentType == null || userId == null || userId < 0 || StringUtils.isBlank(content)) {
            throw new BizException("评论参数非法");
        }
    }

    /**
     * 校验当前操作者是否为内容所有者（编辑 / 删除仅限本人）
     */
    private void checkOwner(Integer ownerId, Integer userId, String targetName) {
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new BizException("无权操作他人" + targetName);
        }
    }

    private void validateReplyCreate(Long commentId, String content, Integer userId) {
        if (commentId == null || userId == null || userId < 0 || StringUtils.isBlank(content)) {
            throw new BizException("回复参数非法");
        }
    }

    private void validateCommentQuery(Long commentObjectId, Integer commentType, Integer page, Integer pageSize) {
        if (commentObjectId == null || commentType == null) {
            throw new BizException("评论列表参数非法");
        }
        if (page != null && page <= 0) {
            throw new BizException("页码非法");
        }
        if (pageSize != null && pageSize <= 0) {
            throw new BizException("分页大小非法");
        }
    }

    private int safePage(Integer page) {
        return page == null || page <= 0 ? 1 : page;
    }

    private int safePageSize(Integer pageSize) {
        return pageSize == null || pageSize <= 0 ? 20 : pageSize;
    }

    private String defaultImages(String images) {
        return StringUtils.isBlank(images) ? "[]" : images;
    }

    private void batchFillCommentLikeCount(Long commentObjectId, List<CommentEntity> comments, Integer currentUserId) {
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }
        RMap<Long, Integer> likeMap = redissonClient.getMap(String.format(COMMENT_LIKE_KEY, commentObjectId));
        for (CommentEntity comment : comments) {
            Integer likeCount = likeMap.get(comment.getId());
            if (likeCount != null) {
                comment.setLikeCount(likeCount);
            }
            commentLikedCarrier.put(comment.getId(), currentUserId == null ? Boolean.FALSE : isUserLiked(currentUserId, comment.getId(), COMMENT_TARGET_TYPE));
        }
    }

    private void batchFillReplyLikeCount(Long commentObjectId, List<ReplyEntity> replies, Integer currentUserId) {
        if (CollectionUtils.isEmpty(replies)) {
            return;
        }
        RMap<Long, Integer> likeMap = redissonClient.getMap(String.format(REPLY_LIKE_KEY, commentObjectId));
        for (ReplyEntity reply : replies) {
            Integer likeCount = likeMap.get(reply.getId());
            if (likeCount != null) {
                reply.setLikeCount(likeCount);
            }
            replyLikedCarrier.put(reply.getId(), currentUserId == null ? Boolean.FALSE : isUserLiked(currentUserId, reply.getId(), REPLY_TARGET_TYPE));
        }
    }

    private void incrementCommentLikeCache(Long commentObjectId, Long commentId, int delta) {
        RMap<Long, Integer> map = redissonClient.getMap(String.format(COMMENT_LIKE_KEY, commentObjectId));
        Integer current = map.get(commentId);
        map.put(commentId, Math.max((current == null ? 0 : current) + delta, 0));
    }

    private void incrementReplyLikeCache(Long commentObjectId, Long replyId, int delta) {
        RMap<Long, Integer> map = redissonClient.getMap(String.format(REPLY_LIKE_KEY, commentObjectId));
        Integer current = map.get(replyId);
        map.put(replyId, Math.max((current == null ? 0 : current) + delta, 0));
    }

    private void markUserLiked(Integer userId, Long targetId, Integer targetType, boolean liked) {
        String key = String.format(USER_LIKE_KEY, userId, targetType, targetId);
        RBucket<String> bucket = redissonClient.getBucket(key);
        if (liked) {
            bucket.set("1", Duration.ofDays(7));
        } else {
            bucket.delete();
        }
    }

    private boolean isUserLiked(Integer userId, Long targetId, Integer targetType) {
        return redissonClient.getBucket(String.format(USER_LIKE_KEY, userId, targetType, targetId)).isExists();
    }

    private void updateHotCommentScore(Long commentObjectId, Integer commentType, Long commentId, Integer likeCount) {
        long hotScore = (long) Math.max(likeCount, 0) * 100 + System.currentTimeMillis() / 1000;
        RScoredSortedSet<Long> scoredSortedSet = redissonClient.getScoredSortedSet(String.format(HOT_COMMENT_KEY, commentObjectId, commentType));
        scoredSortedSet.remove(commentId);
        scoredSortedSet.add(hotScore, commentId);
    }

    private void saveDefaultPassedAuditRecord(Long targetId, Integer targetType, String content) {
        CommentAuditEntity auditEntity = new CommentAuditEntity();
        auditEntity.setId(SnowflakeIdUtils.nextId());
        auditEntity.setTargetId(targetId);
        auditEntity.setTargetType(targetType);
        auditEntity.setAuditContent(StringUtils.defaultString(content));
        auditEntity.setAuditStatus(1);
        auditEntity.setAuditReason("默认通过");
        auditEntity.setAuditOperator(0L);
        auditEntity.setAuditTime(LocalDateTime.now());
        commentAuditRepository.save(auditEntity);
    }

    public Boolean isCommentLiked(Long commentId) {
        return commentLikedCarrier.getOrDefault(commentId, Boolean.FALSE);
    }

    public Boolean isReplyLiked(Long replyId) {
        return replyLikedCarrier.getOrDefault(replyId, Boolean.FALSE);
    }

    public List<ReplyEntity> topRepliesOf(Long commentId) {
        return commentLikeReplyCarrier.getOrDefault(commentId, List.of());
    }

    private final Map<Long, Boolean> commentLikedCarrier = new HashMap<>();
    private final Map<Long, Boolean> replyLikedCarrier = new HashMap<>();
    private final Map<Long, List<ReplyEntity>> commentLikeReplyCarrier = new HashMap<>();

    public record PageResult<T>(long total, int page, int pageSize, List<T> list) {
    }

    public record LikeResult(Long targetId, Integer targetType, Boolean liked) {
    }

    private CommentLikeEntity buildLikeEntity(Integer userId, Long targetId, Integer targetType) {
        CommentLikeEntity entity = new CommentLikeEntity();
        entity.setId(SnowflakeIdUtils.nextId());
        entity.setUserId(userId);
        entity.setTargetId(targetId);
        entity.setTargetType(targetType);
        entity.setCreateTime(LocalDateTime.now());
        return entity;
    }
}
