package com.ssp.comment.vo.resp;

import lombok.Data;

@Data
public class CommentLikeRespVO {

    private Long targetId;
    private Integer targetType;
    private Boolean liked;
}
