package com.ssp.comment.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationReadReqVO {

    @NotNull(message = "通知ID不能为空")
    private Long id;
}
