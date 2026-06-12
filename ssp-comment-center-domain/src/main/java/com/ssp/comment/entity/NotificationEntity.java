package com.ssp.comment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationEntity {

    private Long id;
    private Integer userId;
    private Integer type;
    private Long subjectId;
    private Integer subjectType;
    private Integer actorId;
    private Long commentObjectId;
    private Integer commentType;
    private String content;
    private Integer isRead;
    private Integer isDelete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
