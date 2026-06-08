package com.ssp.comment.dao.mapper;

import com.ssp.comment.dao.pojo.CommentLikePO;
import org.apache.ibatis.annotations.Param;

public interface CommentLikeMapper {

    int insertSelective(CommentLikePO record);

    int countByUserAndTarget(@Param("userId") Integer userId,
                             @Param("targetId") Long targetId,
                             @Param("targetType") Integer targetType);

    int deleteByUserAndTarget(@Param("userId") Integer userId,
                                @Param("targetId") Long targetId,
                                @Param("targetType") Integer targetType);
}
