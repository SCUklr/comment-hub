package com.ssp.comment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCommentIndexEntity {

    private Long id;
    private Integer userId;
    private Long commentObjectId;
    private Integer commentType;
    private Integer interactionType;
    private Long targetCommentId;
    private Long targetReplyId;
    private String latestContent;
    private LocalDateTime latestTime;
    private Integer interactionCount;
    private Integer isDelete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
