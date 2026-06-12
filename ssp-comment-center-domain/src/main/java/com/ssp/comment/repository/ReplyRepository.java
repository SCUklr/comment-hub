package com.ssp.comment.repository;

import com.ssp.comment.entity.ReplyEntity;

import java.util.List;

public interface ReplyRepository {

    void save(ReplyEntity replyEntity);

    ReplyEntity queryById(Long id);

    long countByCommentAndParent(Long commentId, Long parentReplyId, Integer currentUserId);

    List<ReplyEntity> queryPageByCommentAndParent(Long commentId, Long parentReplyId, int offset, int pageSize, Integer currentUserId);

    List<ReplyEntity> queryByCommentIdsAndParent(List<Long> commentIds, Long parentReplyId, Integer currentUserId);

    void updateDeleteMark(Long id, Integer operatorId);

    void updateContent(Long id, String content, String images);

    void updateLikeCount(Long id, int delta);

    void updateAuditStatus(Long id, Integer auditStatus);
}
