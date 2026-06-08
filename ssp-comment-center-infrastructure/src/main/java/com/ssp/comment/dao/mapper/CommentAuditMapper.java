package com.ssp.comment.dao.mapper;

import com.ssp.comment.dao.pojo.CommentAuditPO;

public interface CommentAuditMapper {

    int insertSelective(CommentAuditPO record);
}
