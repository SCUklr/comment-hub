package com.ssp.comment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentEntity {

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
    private Integer isDelete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
