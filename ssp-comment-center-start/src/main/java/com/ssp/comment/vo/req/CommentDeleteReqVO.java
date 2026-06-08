package com.ssp.comment.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDeleteReqVO {

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotNull(message = "ID不能为空")
    private Long id;
}
