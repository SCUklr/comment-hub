package com.ssp.comment.bo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentEditBO {

    private Integer type;
    private Long id;
    private String content;
    private String images;
    private LocalDateTime updateTime;
}
