package com.ssp.comment.dao.mapper;

import com.ssp.comment.dao.pojo.UserCommentIndexPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserCommentIndexMapper {

    int insertSelective(UserCommentIndexPO record);

    int upsertByUserAndObject(UserCommentIndexPO record);

    long countByUser(@Param("userId") Integer userId,
                      @Param("commentType") Integer commentType,
                      @Param("interactionType") Integer interactionType);

    List<UserCommentIndexPO> selectPageByUser(@Param("userId") Integer userId,
                                               @Param("commentType") Integer commentType,
                                               @Param("interactionType") Integer interactionType,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    int markDeleted(@Param("userId") Integer userId,
                    @Param("commentObjectId") Long commentObjectId,
                    @Param("commentType") Integer commentType,
                    @Param("interactionType") Integer interactionType);
}
