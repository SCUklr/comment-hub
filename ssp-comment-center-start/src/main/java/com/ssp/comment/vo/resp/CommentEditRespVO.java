package com.ssp.comment.vo.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentEditRespVO {

    private Integer type;
    private Long id;
    private String content;
    private String images;
    private LocalDateTime updateTime;
}
