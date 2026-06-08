package com.ssp.comment.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentLikeReqVO {

    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    @NotNull(message = "目标类型不能为空")
    private Integer targetType;
}
