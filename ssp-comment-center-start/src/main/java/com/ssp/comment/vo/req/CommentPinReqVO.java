package com.ssp.comment.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentPinReqVO {

    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    private Boolean isPin;
}
