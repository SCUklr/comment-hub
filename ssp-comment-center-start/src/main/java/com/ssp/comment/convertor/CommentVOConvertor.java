package com.ssp.comment.convertor;

import com.ssp.comment.bo.*;
import com.ssp.comment.entity.CommentAuditEntity;
import com.ssp.comment.vo.resp.*;

import java.util.List;
import java.util.stream.Collectors;

public class CommentVOConvertor {

    public static CommentCreateRespVO toVO(CommentCreateBO bo) {
        if (bo == null) return null;
        CommentCreateRespVO vo = new CommentCreateRespVO();
        vo.setType(bo.getType());
        vo.setId(bo.getId());
        vo.setCommentObjectId(bo.getCommentObjectId());
        vo.setCommentType(bo.getCommentType());
        vo.setCommentId(bo.getCommentId());
        vo.setParentId(bo.getParentId());
        vo.setReplyType(bo.getReplyType());
        vo.setReplyUserId(bo.getReplyUserId());
        vo.setBeRepliedUserId(bo.getBeRepliedUserId());
        vo.setContent(bo.getContent());
        vo.setImages(bo.getImages());
        vo.setCreateTime(bo.getCreateTime());
        vo.setUpdateTime(bo.getUpdateTime());
        return vo;
    }

    public static CommentEditRespVO toVO(CommentEditBO bo) {
        if (bo == null) return null;
        CommentEditRespVO vo = new CommentEditRespVO();
        vo.setType(bo.getType());
        vo.setId(bo.getId());
        vo.setContent(bo.getContent());
        vo.setImages(bo.getImages());
        vo.setUpdateTime(bo.getUpdateTime());
        return vo;
    }

    public static CommentListRespVO toVO(CommentListBO bo) {
        if (bo == null) return null;
        CommentListRespVO vo = new CommentListRespVO();
        vo.setTotal(bo.getTotal());
        vo.setPage(bo.getPage());
        vo.setPageSize(bo.getPageSize());
        vo.setList(bo.getList().stream().map(CommentVOConvertor::toCommentItemVO).collect(Collectors.toList()));
        return vo;
    }

    public static ReplyListRespVO toVO(ReplyListBO bo) {
        if (bo == null) return null;
        ReplyListRespVO vo = new ReplyListRespVO();
        vo.setTotal(bo.getTotal());
        vo.setPage(bo.getPage());
        vo.setPageSize(bo.getPageSize());
        vo.setList(bo.getList().stream().map(CommentVOConvertor::toReplyItemVO).collect(Collectors.toList()));
        return vo;
    }

    public static CommentLikeRespVO toVO(CommentLikeBO bo) {
        if (bo == null) return null;
        CommentLikeRespVO vo = new CommentLikeRespVO();
        vo.setTargetId(bo.getTargetId());
        vo.setTargetType(bo.getTargetType());
        vo.setLiked(bo.getLiked());
        return vo;
    }

    public static CommentAuditHistoryRespVO toVO(Long targetId, Integer targetType, List<CommentAuditEntity> list) {
        CommentAuditHistoryRespVO vo = new CommentAuditHistoryRespVO();
        vo.setTargetId(targetId);
        vo.setTargetType(targetType);
        if (list == null) {
            return vo;
        }
        vo.setList(list.stream().map(item -> {
            CommentAuditHistoryRespVO.AuditItemVO auditItem = new CommentAuditHistoryRespVO.AuditItemVO();
            auditItem.setId(item.getId());
            auditItem.setAuditStatus(item.getAuditStatus());
            auditItem.setAuditContent(item.getAuditContent());
            auditItem.setAuditReason(item.getAuditReason());
            auditItem.setAuditOperator(item.getAuditOperator());
            auditItem.setAuditTime(item.getAuditTime());
            return auditItem;
        }).collect(Collectors.toList()));
        return vo;
    }

    public static MyCommentListRespVO toVO(MyCommentListBO bo) {
        if (bo == null) return null;
        MyCommentListRespVO vo = new MyCommentListRespVO();
        vo.setTotal(bo.getTotal());
        vo.setPage(bo.getPage());
        vo.setPageSize(bo.getPageSize());
        vo.setList(bo.getList().stream().map(CommentVOConvertor::toMyCommentItemVO).collect(Collectors.toList()));
        return vo;
    }

    private static CommentListRespVO.CommentItemVO toCommentItemVO(CommentListBO.CommentItemBO bo) {
        if (bo == null) return null;
        CommentListRespVO.CommentItemVO vo = new CommentListRespVO.CommentItemVO();
        vo.setId(bo.getId());
        vo.setCommentObjectId(bo.getCommentObjectId());
        vo.setCommentType(bo.getCommentType());
        vo.setContent(bo.getContent());
        vo.setImages(bo.getImages());
        vo.setUserId(bo.getUserId());
        vo.setSort(bo.getSort());
        vo.setReplyCount(bo.getReplyCount());
        vo.setLikeCount(bo.getLikeCount());
        vo.setAuditStatus(bo.getAuditStatus());
        vo.setLiked(bo.getLiked());
        vo.setCreateTime(bo.getCreateTime());
        vo.setUpdateTime(bo.getUpdateTime());
        if (bo.getTopReplies() != null) {
            vo.setTopReplies(bo.getTopReplies().stream().map(CommentVOConvertor::toReplyItemVO).collect(Collectors.toList()));
        }
        return vo;
    }

    private static ReplyListRespVO.ReplyItemVO toReplyItemVO(ReplyListBO.ReplyItemBO bo) {
        if (bo == null) return null;
        ReplyListRespVO.ReplyItemVO vo = new ReplyListRespVO.ReplyItemVO();
        vo.setId(bo.getId());
        vo.setCommentId(bo.getCommentId());
        vo.setParentId(bo.getParentId());
        vo.setReplyType(bo.getReplyType());
        vo.setContent(bo.getContent());
        vo.setImages(bo.getImages());
        vo.setReplyUserId(bo.getReplyUserId());
        vo.setBeRepliedUserId(bo.getBeRepliedUserId());
        vo.setLikeCount(bo.getLikeCount());
        vo.setAuditStatus(bo.getAuditStatus());
        vo.setLiked(bo.getLiked());
        vo.setCreateTime(bo.getCreateTime());
        vo.setUpdateTime(bo.getUpdateTime());
        return vo;
    }

    private static CommentListRespVO.ReplyItemVO toReplyItemVO(CommentListBO.ReplyItemBO bo) {
        if (bo == null) return null;
        CommentListRespVO.ReplyItemVO vo = new CommentListRespVO.ReplyItemVO();
        vo.setId(bo.getId());
        vo.setCommentId(bo.getCommentId());
        vo.setParentId(bo.getParentId());
        vo.setReplyType(bo.getReplyType());
        vo.setContent(bo.getContent());
        vo.setImages(bo.getImages());
        vo.setReplyUserId(bo.getReplyUserId());
        vo.setBeRepliedUserId(bo.getBeRepliedUserId());
        vo.setLikeCount(bo.getLikeCount());
        vo.setAuditStatus(bo.getAuditStatus());
        vo.setLiked(bo.getLiked());
        vo.setCreateTime(bo.getCreateTime());
        vo.setUpdateTime(bo.getUpdateTime());
        return vo;
    }

    private static MyCommentListRespVO.MyCommentItemVO toMyCommentItemVO(MyCommentListBO.MyCommentItemBO bo) {
        if (bo == null) return null;
        MyCommentListRespVO.MyCommentItemVO vo = new MyCommentListRespVO.MyCommentItemVO();
        vo.setCommentObjectId(bo.getCommentObjectId());
        vo.setCommentType(bo.getCommentType());
        vo.setInteractionType(bo.getInteractionType());
        vo.setLatestContent(bo.getLatestContent());
        vo.setLatestTime(bo.getLatestTime());
        vo.setInteractionCount(bo.getInteractionCount());
        return vo;
    }
}
