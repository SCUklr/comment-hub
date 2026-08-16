package com.ssp.comment.controller;

import com.ssp.comment.CommentBizService;
import com.ssp.comment.bo.*;
import com.ssp.comment.common.LoginRequired;
import com.ssp.comment.entity.CommentAuditEntity;
import com.ssp.comment.common.Result;
import com.ssp.comment.common.UserContext;
import com.ssp.comment.convertor.CommentVOConvertor;
import com.ssp.comment.vo.req.*;
import com.ssp.comment.vo.resp.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentBizService commentBizService;

    @PostMapping("/create")
    @LoginRequired
    public Result<CommentCreateRespVO> create(@RequestBody @Valid CommentCreateReqVO req) {
        CommentCreateBO bo = commentBizService.create(
                req.getType(),
                req.getCommentObjectId(),
                req.getCommentType(),
                req.getCommentId(),
                req.getParentId(),
                req.getBeRepliedUserId(),
                req.getContent(),
                req.getImages(),
                UserContext.getCurrentUserId());
        return Result.success(CommentVOConvertor.toVO(bo));
    }

    @PostMapping("/delete")
    @LoginRequired
    public Result<Void> delete(@RequestBody @Valid CommentDeleteReqVO req) {
        commentBizService.delete(req.getType(), req.getId(), UserContext.getCurrentUserId());
        return Result.success();
    }

    @PostMapping("/edit")
    @LoginRequired
    public Result<CommentEditRespVO> edit(@RequestBody @Valid CommentEditReqVO req) {
        CommentEditBO bo = commentBizService.edit(req.getType(), req.getId(), req.getContent(), req.getImages(),
                UserContext.getCurrentUserId());
        return Result.success(CommentVOConvertor.toVO(bo));
    }

    @PostMapping("/pin")
    @LoginRequired
    public Result<Void> pin(@RequestBody @Valid CommentPinReqVO req) {
        commentBizService.pin(req.getCommentId(), Boolean.TRUE.equals(req.getIsPin()));
        return Result.success();
    }

    @GetMapping("/list")
    public Result<CommentListRespVO> list(@Valid CommentListReqVO req) {
        CommentListBO bo = commentBizService.listComments(
                req.getCommentObjectId(),
                req.getCommentType(),
                req.getPage(),
                req.getPageSize(),
                req.getTopReplyLimit(),
                UserContext.getCurrentUserId());
        return Result.success(CommentVOConvertor.toVO(bo));
    }

    @PostMapping("/like")
    @LoginRequired
    public Result<CommentLikeRespVO> like(@RequestBody @Valid CommentLikeReqVO req) {
        CommentLikeBO bo = commentBizService.like(req.getTargetId(), req.getTargetType(), UserContext.getCurrentUserId());
        return Result.success(CommentVOConvertor.toVO(bo));
    }

    @PostMapping("/unlike")
    @LoginRequired
    public Result<CommentLikeRespVO> unlike(@RequestBody @Valid CommentLikeReqVO req) {
        CommentLikeBO bo = commentBizService.unlike(req.getTargetId(), req.getTargetType(), UserContext.getCurrentUserId());
        return Result.success(CommentVOConvertor.toVO(bo));
    }

    @PostMapping("/audit/callback")
    public Result<Void> auditCallback(@RequestBody @Valid CommentAuditCallbackReqVO req) {
        commentBizService.auditCallback(req.getTargetId(), req.getTargetType(), req.getAuditStatus(), req.getAuditReason(), req.getAuditOperator());
        return Result.success();
    }

    @GetMapping("/audit/list")
    public Result<CommentListRespVO> auditList(@Valid CommentListReqVO req) {
        CommentListBO bo = commentBizService.listCommentsForAudit(
                req.getCommentObjectId(),
                req.getCommentType(),
                req.getPage(),
                req.getPageSize(),
                req.getTopReplyLimit());
        return Result.success(CommentVOConvertor.toVO(bo));
    }

    @GetMapping("/audit/history")
    public Result<CommentAuditHistoryRespVO> auditHistory(@Valid CommentAuditHistoryReqVO req) {
        List<CommentAuditEntity> list = commentBizService.listAuditHistory(req.getTargetId(), req.getTargetType());
        return Result.success(CommentVOConvertor.toVO(req.getTargetId(), req.getTargetType(), list));
    }

    @GetMapping("/hot")
    public Result<CommentListRespVO> hot(@Valid CommentHotListReqVO req) {
        CommentListBO bo = commentBizService.listHotComments(
                req.getCommentObjectId(),
                req.getCommentType(),
                req.getPage(),
                req.getPageSize(),
                UserContext.getCurrentUserId());
        return Result.success(CommentVOConvertor.toVO(bo));
    }

    @GetMapping("/my/list")
    @LoginRequired
    public Result<MyCommentListRespVO> myList(@Valid MyCommentListReqVO req) {
        MyCommentListBO bo = commentBizService.listMyComments(
                UserContext.getCurrentUserId(),
                req.getCommentType(),
                req.getInteractionType(),
                req.getPage(),
                req.getPageSize());
        return Result.success(CommentVOConvertor.toVO(bo));
    }
}
