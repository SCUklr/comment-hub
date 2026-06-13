package com.ssp.comment.dao.mapper;

import com.ssp.comment.dao.pojo.CommentPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper {

    int insertSelective(CommentPO record);

    CommentPO selectOneById(@Param("id") Long id);

    List<CommentPO> selectByIds(@Param("idList") List<Long> idList);

    long countByObject(@Param("commentObjectId") Long commentObjectId,
                       @Param("commentType") Integer commentType,
                       @Param("currentUserId") Integer currentUserId);

    List<CommentPO> selectPageByObject(@Param("commentObjectId") Long commentObjectId,
                                      @Param("commentType") Integer commentType,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize,
                                      @Param("currentUserId") Integer currentUserId);

    List<CommentPO> selectHotPageByObject(@Param("commentObjectId") Long commentObjectId,
                                          @Param("commentType") Integer commentType,
                                          @Param("offset") int offset,
                                          @Param("pageSize") int pageSize,
                                          @Param("currentUserId") Integer currentUserId);

    long countForAudit(@Param("commentObjectId") Long commentObjectId,
                       @Param("commentType") Integer commentType);

    List<CommentPO> selectPageForAudit(@Param("commentObjectId") Long commentObjectId,
                                       @Param("commentType") Integer commentType,
                                       @Param("offset") int offset,
                                       @Param("pageSize") int pageSize);

    int updateDeleteMark(@Param("id") Long id, @Param("isDelete") Integer isDelete);

    int updateContent(@Param("id") Long id, @Param("content") String content, @Param("images") String images);

    int updatePin(@Param("id") Long id, @Param("sort") Integer sort);

    int updateReplyCount(@Param("id") Long id, @Param("delta") int delta);

    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") Integer auditStatus);
}
