package com.ssp.comment.dao.mapper;

import com.ssp.comment.dao.pojo.ReplyPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReplyMapper {

    int insertSelective(ReplyPO record);

    ReplyPO selectOneById(@Param("id") Long id);

    long countByCommentAndParent(@Param("commentId") Long commentId,
                                 @Param("parentReplyId") Long parentReplyId,
                                 @Param("currentUserId") Integer currentUserId);

    List<ReplyPO> selectPageByCommentAndParent(@Param("commentId") Long commentId,
                                                @Param("parentReplyId") Long parentReplyId,
                                                @Param("offset") int offset,
                                                @Param("pageSize") int pageSize,
                                                @Param("currentUserId") Integer currentUserId);

    List<ReplyPO> selectByCommentIdsAndParent(@Param("commentIds") List<Long> commentIds,
                                               @Param("parentReplyId") Long parentReplyId,
                                               @Param("currentUserId") Integer currentUserId);

    int updateDeleteMark(@Param("id") Long id, @Param("isDelete") Integer isDelete);

    int updateContent(@Param("id") Long id, @Param("content") String content, @Param("images") String images);

    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") Integer auditStatus);
}
