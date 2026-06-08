package com.ssp.comment.controller;

import com.ssp.comment.CommentBizService;
import com.ssp.comment.bo.ReplyListBO;
import com.ssp.comment.common.Result;
import com.ssp.comment.common.UserContext;
import com.ssp.comment.convertor.CommentVOConvertor;
import com.ssp.comment.vo.req.ReplyListReqVO;
import com.ssp.comment.vo.resp.ReplyListRespVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reply")
public class ReplyController {

    @Autowired
    private CommentBizService commentBizService;

    @GetMapping("/list")
    public Result<ReplyListRespVO> list(@Valid ReplyListReqVO req) {
        ReplyListBO bo = commentBizService.listReplies(
                req.getCommentId(),
                req.getParentId(),
                req.getPage(),
                req.getPageSize(),
                UserContext.getCurrentUserId());
        return Result.success(CommentVOConvertor.toVO(bo));
    }
}
