package com.ssp.comment.vo.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentCreateRespVO {

    private Integer type;
    private Long id;
    private Long commentObjectId;
    private Integer commentType;
    private Long commentId;
    private Long parentId;
    private Integer replyType;
    private Integer replyUserId;
    private Integer beRepliedUserId;
    private String content;
    private String images;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
