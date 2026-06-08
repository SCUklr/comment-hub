package com.ssp.comment.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ReplyPO implements Serializable {

    private Long id;
    private Long commentId;
    private Long parentReplyId;
    private Integer replyType;
    private String content;
    private String images;
    private Integer replyUserId;
    private Integer beRepliedUserId;
    private Integer likeCount;
    private Integer auditStatus;
    private Date createTime;
    private Date updateTime;
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
