package com.ssp.comment.vo.req;

import lombok.Data;

@Data
public class NotificationListReqVO {

    private Integer page = 1;

    private Integer pageSize = 20;
}
