package com.ssp.comment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentLikeEntity {

    private Long id;
    private Integer userId;
    private Long targetId;
    private Integer targetType;
    private LocalDateTime createTime;
}
