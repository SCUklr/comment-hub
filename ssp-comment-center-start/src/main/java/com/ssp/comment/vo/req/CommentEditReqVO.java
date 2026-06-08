package com.ssp.comment.vo.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentEditReqVO {

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String images;
}
