package com.ssp.comment.bo;

import lombok.Data;

@Data
public class CommentLikeBO {

    private Long targetId;
    private Integer targetType;
    private Boolean liked;
}
