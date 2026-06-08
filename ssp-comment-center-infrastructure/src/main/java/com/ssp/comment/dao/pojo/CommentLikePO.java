package com.ssp.comment.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentLikePO implements Serializable {

    private Long id;
    private Integer userId;
    private Long targetId;
    private Integer targetType;
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
