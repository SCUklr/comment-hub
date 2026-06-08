package com.ssp.comment.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentHotListReqVO {

    @NotNull(message = "对象ID不能为空")
    private Long commentObjectId;

    @NotNull(message = "对象类型不能为空")
    private Integer commentType;

    private Integer page = 1;

    private Integer pageSize = 20;
}
