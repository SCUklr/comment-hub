package com.ssp.comment.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class NotificationPO implements Serializable {

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
    private Date createTime;
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
