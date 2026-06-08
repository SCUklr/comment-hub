package com.ssp.comment.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserCommentIndexPO implements Serializable {

    private Long id;
    private Integer userId;
    private Long commentObjectId;
    private Integer commentType;
    private Integer interactionType;
    private Long targetCommentId;
    private Long targetReplyId;
    private String latestContent;
    private Date latestTime;
    private Integer interactionCount;
    private Integer isDelete;
    private Date createTime;
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
