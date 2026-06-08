package com.ssp.comment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyEntity {

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
    private Integer isDelete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
