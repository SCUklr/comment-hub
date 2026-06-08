package com.ssp.comment.vo.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentCreateReqVO {

    @NotNull(message = "类型不能为空")
    private Integer type;

    private Long commentObjectId;

    private Integer commentType;

    private Long commentId;

    private Long parentId;

    private Integer beRepliedUserId;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String images;
}
