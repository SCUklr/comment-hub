package com.ssp.comment.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentAuditCallbackReqVO {

    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    private String auditReason;

    private Long auditOperator;
}
