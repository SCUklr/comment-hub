package com.ssp.comment.vo.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationListRespVO {

    private Long id;
    private Integer type;
    private Long subjectId;
    private Integer subjectType;
    private Integer actorId;
    private Long commentObjectId;
    private Integer commentType;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
}
