package com.ssp.comment;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssp.comment.bo.CommentCreateBO;
import com.ssp.comment.bo.CommentEditBO;
import com.ssp.comment.bo.CommentLikeBO;
import com.ssp.comment.bo.CommentListBO;
import com.ssp.comment.bo.MyCommentListBO;
import com.ssp.comment.bo.ReplyListBO;
import com.ssp.comment.entity.CommentAuditEntity;
import com.ssp.comment.entity.CommentEntity;
import com.ssp.comment.entity.ReplyEntity;
import com.ssp.comment.entity.UserCommentIndexEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommentBizService {

    private final CommentDomainService commentDomainService;

    public CommentCreateBO create(Integer type,
                                  Long commentObjectId,
                                  Integer commentType,
                                  Long commentId,
                                  Long parentId,
                                  Integer beRepliedUserId,
                                  String content,
                                  String images,
                                  Integer currentUserId) {
        if (type != null && type == 1) {
            CommentEntity entity = commentDomainService.createComment(commentObjectId, commentType, content, images, currentUserId);
            CommentCreateBO bo = new CommentCreateBO();
            bo.setType(1);
            bo.setId(entity.getId());
            bo.setCommentObjectId(entity.getCommentObjectId());
            bo.setCommentType(entity.getCommentType());
            bo.setContent(entity.getContent());
            bo.setImages(entity.getImages());
            bo.setCreateTime(entity.getCreateTime());
            bo.setUpdateTime(entity.getUpdateTime());
            return bo;
        }

        ReplyEntity entity = commentDomainService.createReply(commentId, parentId, beRepliedUserId, content, images, currentUserId);
        CommentCreateBO bo = new CommentCreateBO();
        bo.setType(2);
        bo.setId(entity.getId());
        bo.setCommentId(entity.getCommentId());
        bo.setParentId(entity.getParentReplyId());
        bo.setReplyType(entity.getReplyType());
        bo.setReplyUserId(entity.getReplyUserId());
        bo.setBeRepliedUserId(entity.getBeRepliedUserId());
        bo.setContent(entity.getContent());
        bo.setImages(entity.getImages());
        bo.setCreateTime(entity.getCreateTime());
        bo.setUpdateTime(entity.getUpdateTime());
        return bo;
    }

    public void delete(Integer type, Long id, Integer currentUserId) {
        commentDomainService.deleteTarget(type, id, currentUserId);
    }

    public CommentEditBO edit(Integer type, Long id, String content, String images, Integer currentUserId) {
        CommentEditBO bo = new CommentEditBO();
        if (type != null && type == 1) {
            CommentEntity entity = commentDomainService.editComment(id, content, images, currentUserId);
            bo.setType(1);
            bo.setId(entity.getId());
            bo.setContent(entity.getContent());
            bo.setImages(entity.getImages());
            bo.setUpdateTime(entity.getUpdateTime());
            return bo;
        }

        ReplyEntity entity = commentDomainService.editReply(id, content, images, currentUserId);
        bo.setType(2);
        bo.setId(entity.getId());
        bo.setContent(entity.getContent());
        bo.setImages(entity.getImages());
        bo.setUpdateTime(entity.getUpdateTime());
        return bo;
    }

    public void pin(Long commentId, boolean pin) {
        commentDomainService.pinComment(commentId, pin);
    }

    public CommentListBO listComments(Long commentObjectId,
                                      Integer commentType,
                                      Integer page,
                                      Integer pageSize,
                                      Integer topReplyLimit,
                                      Integer currentUserId) {
        CommentDomainService.PageResult<CommentEntity> result = commentDomainService.queryCommentPage(
                commentObjectId, commentType, page, pageSize, topReplyLimit, currentUserId);
        CommentListBO bo = new CommentListBO();
        bo.setTotal(result.total());
        bo.setPage(result.page());
        bo.setPageSize(result.pageSize());
        bo.setList(result.list().stream().map(comment -> {
            CommentListBO.CommentItemBO item = new CommentListBO.CommentItemBO();
            item.setId(comment.getId());
            item.setCommentObjectId(comment.getCommentObjectId());
            item.setCommentType(comment.getCommentType());
            item.setContent(comment.getContent());
            item.setImages(comment.getImages());
            item.setUserId(comment.getCommentUserId());
            item.setSort(comment.getSort());
            item.setReplyCount(comment.getReplyCount());
            item.setLikeCount(comment.getLikeCount());
            item.setAuditStatus(comment.getAuditStatus());
            item.setLiked(commentDomainService.isCommentLiked(comment.getId()));
            item.setCreateTime(comment.getCreateTime());
            item.setUpdateTime(comment.getUpdateTime());
            item.setTopReplies(commentDomainService.topRepliesOf(comment.getId()).stream().map(this::toCommentReplyItemBO).collect(Collectors.toList()));
            return item;
        }).collect(Collectors.toList()));
        return bo;
    }

    public ReplyListBO listReplies(Long commentId,
                                   Long parentId,
                                   Integer page,
                                   Integer pageSize,
                                   Integer currentUserId) {
        CommentDomainService.PageResult<ReplyEntity> result = commentDomainService.queryReplyPage(
                commentId, parentId, page, pageSize, currentUserId);
        ReplyListBO bo = new ReplyListBO();
        bo.setTotal(result.total());
        bo.setPage(result.page());
        bo.setPageSize(result.pageSize());
        bo.setList(result.list().stream().map(this::toReplyItemBO).collect(Collectors.toList()));
        return bo;
    }

    public CommentLikeBO like(Long targetId, Integer targetType, Integer currentUserId) {
        CommentDomainService.LikeResult result = commentDomainService.like(currentUserId, targetId, targetType);
        CommentLikeBO bo = new CommentLikeBO();
        bo.setTargetId(result.targetId());
        bo.setTargetType(result.targetType());
        bo.setLiked(result.liked());
        return bo;
    }

    public CommentLikeBO unlike(Long targetId, Integer targetType, Integer currentUserId) {
        CommentDomainService.LikeResult result = commentDomainService.unlike(currentUserId, targetId, targetType);
        CommentLikeBO bo = new CommentLikeBO();
        bo.setTargetId(result.targetId());
        bo.setTargetType(result.targetType());
        bo.setLiked(result.liked());
        return bo;
    }

    public void auditCallback(Long targetId,
                              Integer targetType,
                              Integer auditStatus,
                              String auditReason,
                              Long auditOperator) {
        commentDomainService.handleAuditCallback(targetId, targetType, auditStatus, auditReason, auditOperator);
    }

    public List<CommentAuditEntity> listAuditHistory(Long targetId, Integer targetType) {
        return commentDomainService.queryAuditHistory(targetId, targetType);
    }

    public CommentListBO listCommentsForAudit(Long commentObjectId,
                                              Integer commentType,
                                              Integer page,
                                              Integer pageSize,
                                              Integer topReplyLimit) {
        CommentDomainService.PageResult<CommentEntity> result = commentDomainService.queryCommentPageForAudit(
                commentObjectId, commentType, page, pageSize, topReplyLimit);
        CommentListBO bo = new CommentListBO();
        bo.setTotal(result.total());
        bo.setPage(result.page());
        bo.setPageSize(result.pageSize());
        bo.setList(result.list().stream().map(comment -> {
            CommentListBO.CommentItemBO item = new CommentListBO.CommentItemBO();
            item.setId(comment.getId());
            item.setCommentObjectId(comment.getCommentObjectId());
            item.setCommentType(comment.getCommentType());
            item.setContent(comment.getContent());
            item.setImages(comment.getImages());
            item.setUserId(comment.getCommentUserId());
            item.setSort(comment.getSort());
            item.setReplyCount(comment.getReplyCount());
            item.setLikeCount(comment.getLikeCount());
            item.setAuditStatus(comment.getAuditStatus());
            item.setLiked(commentDomainService.isCommentLiked(comment.getId()));
            item.setCreateTime(comment.getCreateTime());
            item.setUpdateTime(comment.getUpdateTime());
            item.setTopReplies(commentDomainService.topRepliesOf(comment.getId()).stream().map(this::toCommentReplyItemBO).collect(Collectors.toList()));
            return item;
        }).collect(Collectors.toList()));
        return bo;
    }

    public CommentListBO listHotComments(Long commentObjectId,
                                         Integer commentType,
                                         Integer page,
                                         Integer pageSize,
                                         Integer currentUserId) {
        CommentDomainService.PageResult<CommentEntity> result = commentDomainService.queryHotComments(
                commentObjectId, commentType, page, pageSize, currentUserId);
        CommentListBO bo = new CommentListBO();
        bo.setTotal(result.total());
        bo.setPage(result.page());
        bo.setPageSize(result.pageSize());
        bo.setList(result.list().stream().map(comment -> {
            CommentListBO.CommentItemBO item = new CommentListBO.CommentItemBO();
            item.setId(comment.getId());
            item.setCommentObjectId(comment.getCommentObjectId());
            item.setCommentType(comment.getCommentType());
            item.setContent(comment.getContent());
            item.setImages(comment.getImages());
            item.setUserId(comment.getCommentUserId());
            item.setSort(comment.getSort());
            item.setReplyCount(comment.getReplyCount());
            item.setLikeCount(comment.getLikeCount());
            item.setAuditStatus(comment.getAuditStatus());
            item.setLiked(commentDomainService.isCommentLiked(comment.getId()));
            item.setCreateTime(comment.getCreateTime());
            item.setUpdateTime(comment.getUpdateTime());
            return item;
        }).collect(Collectors.toList()));
        return bo;
    }

    public MyCommentListBO listMyComments(Integer currentUserId,
                                          Integer commentType,
                                          Integer interactionType,
                                          Integer page,
                                          Integer pageSize) {
        CommentDomainService.PageResult<UserCommentIndexEntity> result = commentDomainService.queryMyComments(
                currentUserId, commentType, interactionType, page, pageSize);
        MyCommentListBO bo = new MyCommentListBO();
        bo.setTotal(result.total());
        bo.setPage(result.page());
        bo.setPageSize(result.pageSize());
        bo.setList(result.list().stream().map(this::toMyCommentItem).collect(Collectors.toList()));
        return bo;
    }

    private ReplyListBO.ReplyItemBO toReplyItemBO(ReplyEntity replyEntity) {
        ReplyListBO.ReplyItemBO item = new ReplyListBO.ReplyItemBO();
        item.setId(replyEntity.getId());
        item.setCommentId(replyEntity.getCommentId());
        item.setParentId(replyEntity.getParentReplyId());
        item.setReplyType(replyEntity.getReplyType());
        item.setContent(replyEntity.getContent());
        item.setImages(replyEntity.getImages());
        item.setReplyUserId(replyEntity.getReplyUserId());
        item.setBeRepliedUserId(replyEntity.getBeRepliedUserId());
        item.setLikeCount(replyEntity.getLikeCount());
        item.setAuditStatus(replyEntity.getAuditStatus());
        item.setLiked(commentDomainService.isReplyLiked(replyEntity.getId()));
        item.setCreateTime(replyEntity.getCreateTime());
        item.setUpdateTime(replyEntity.getUpdateTime());
        return item;
    }

    private CommentListBO.ReplyItemBO toCommentReplyItemBO(ReplyEntity replyEntity) {
        CommentListBO.ReplyItemBO item = new CommentListBO.ReplyItemBO();
        item.setId(replyEntity.getId());
        item.setCommentId(replyEntity.getCommentId());
        item.setParentId(replyEntity.getParentReplyId());
        item.setReplyType(replyEntity.getReplyType());
        item.setContent(replyEntity.getContent());
        item.setImages(replyEntity.getImages());
        item.setReplyUserId(replyEntity.getReplyUserId());
        item.setBeRepliedUserId(replyEntity.getBeRepliedUserId());
        item.setLikeCount(replyEntity.getLikeCount());
        item.setAuditStatus(replyEntity.getAuditStatus());
        item.setLiked(commentDomainService.isReplyLiked(replyEntity.getId()));
        item.setCreateTime(replyEntity.getCreateTime());
        item.setUpdateTime(replyEntity.getUpdateTime());
        return item;
    }

    private MyCommentListBO.MyCommentItemBO toMyCommentItem(UserCommentIndexEntity indexEntity) {
        MyCommentListBO.MyCommentItemBO item = new MyCommentListBO.MyCommentItemBO();
        item.setCommentObjectId(indexEntity.getCommentObjectId());
        item.setCommentType(indexEntity.getCommentType());
        item.setInteractionType(indexEntity.getInteractionType());
        item.setLatestContent(indexEntity.getLatestContent());
        item.setLatestTime(indexEntity.getLatestTime());
        item.setInteractionCount(indexEntity.getInteractionCount());
        return item;
    }
}
