package com.ssp.comment.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentAuditPO implements Serializable {

    private Long id;
    private Long targetId;
    private Integer targetType;
    private String auditContent;
    private Integer auditStatus;
    private String auditReason;
    private Long auditOperator;
    private Date auditTime;

    private static final long serialVersionUID = 1L;
}
