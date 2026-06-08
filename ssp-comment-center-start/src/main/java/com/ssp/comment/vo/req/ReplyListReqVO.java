package com.ssp.comment.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReplyListReqVO {

    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    private Long parentId = 0L;

    private Integer page = 1;

    private Integer pageSize = 20;
}
