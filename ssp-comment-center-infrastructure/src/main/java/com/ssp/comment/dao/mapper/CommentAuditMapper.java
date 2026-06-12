package com.ssp.comment.dao.mapper;

import com.ssp.comment.dao.pojo.CommentAuditPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentAuditMapper {

    int insertSelective(CommentAuditPO record);

    List<CommentAuditPO> selectByTarget(@Param("targetId") Long targetId,
                                        @Param("targetType") Integer targetType);
}
