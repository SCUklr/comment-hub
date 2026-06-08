package com.ssp.comment.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentPO implements Serializable {

    private Long id;
    private Long commentObjectId;
    private Integer commentType;
    private String content;
    private String images;
    private Integer commentUserId;
    private Integer sort;
    private Integer replyCount;
    private Integer likeCount;
    private Integer auditStatus;
    private Date createTime;
    private Date updateTime;
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
