package com.ssp.comment.controller;

import com.ssp.comment.common.LoginRequired;
import com.ssp.comment.common.PageResult;
import com.ssp.comment.common.Result;
import com.ssp.comment.common.UserContext;
import com.ssp.comment.entity.NotificationEntity;
import com.ssp.comment.repository.NotificationRepository;
import com.ssp.comment.vo.req.NotificationListReqVO;
import com.ssp.comment.vo.req.NotificationReadReqVO;
import com.ssp.comment.vo.resp.NotificationListRespVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/list")
    @LoginRequired
    public Result<PageResult<NotificationListRespVO>> list(@Valid NotificationListReqVO req) {
        Integer userId = UserContext.getCurrentUserId();
        long total = notificationRepository.countByUser(userId);
        List<NotificationEntity> entities = notificationRepository.queryPage(userId, req.getPage(), req.getPageSize());
        List<NotificationListRespVO> list = entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(PageResult.of(total, req.getPage(), req.getPageSize(), list));
    }

    @PostMapping("/read")
    @LoginRequired
    public Result<Boolean> read(@RequestBody @Valid NotificationReadReqVO req) {
        boolean success = notificationRepository.markRead(req.getId(), UserContext.getCurrentUserId());
        return Result.success(success);
    }

    @PostMapping("/read/all")
    @LoginRequired
    public Result<Boolean> readAll() {
        boolean success = notificationRepository.markAllRead(UserContext.getCurrentUserId());
        return Result.success(success);
    }

    private NotificationListRespVO toVO(NotificationEntity entity) {
        NotificationListRespVO vo = new NotificationListRespVO();
        vo.setId(entity.getId());
        vo.setType(entity.getType());
        vo.setSubjectId(entity.getSubjectId());
        vo.setSubjectType(entity.getSubjectType());
        vo.setActorId(entity.getActorId());
        vo.setCommentObjectId(entity.getCommentObjectId());
        vo.setCommentType(entity.getCommentType());
        vo.setContent(entity.getContent());
        vo.setIsRead(entity.getIsRead());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
