package com.ssp.comment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentAuditEntity {

    private Long id;
    private Long targetId;
    private Integer targetType;
    private String auditContent;
    private Integer auditStatus;
    private String auditReason;
    private Long auditOperator;
    private LocalDateTime auditTime;
}
