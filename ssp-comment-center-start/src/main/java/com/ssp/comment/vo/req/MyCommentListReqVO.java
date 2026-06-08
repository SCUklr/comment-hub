package com.ssp.comment.vo.req;

import lombok.Data;

@Data
public class MyCommentListReqVO {

    private Integer commentType;

    private Integer interactionType;

    private Integer page = 1;

    private Integer pageSize = 20;
}
