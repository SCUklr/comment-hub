package com.ssp.comment.repository;

import com.ssp.comment.entity.CommentEntity;

import java.util.List;

public interface CommentRepository {

    void save(CommentEntity commentEntity);

    CommentEntity queryById(Long id);

    CommentEntity queryById(Long id, Long commentObjectId);

    List<CommentEntity> queryByIds(List<Long> ids);

    List<CommentEntity> queryByIds(List<Long> ids, Long commentObjectId);

    long countByObject(Long commentObjectId, Integer commentType, Integer currentUserId);

    List<CommentEntity> queryPageByObject(Long commentObjectId, Integer commentType, int offset, int pageSize, Integer currentUserId);

    List<CommentEntity> queryHotPageByObject(Long commentObjectId, Integer commentType, int offset, int pageSize, Integer currentUserId);

    long countForAudit(Long commentObjectId, Integer commentType);

    List<CommentEntity> queryPageForAudit(Long commentObjectId, Integer commentType, int offset, int pageSize);

    void updateDeleteMark(Long id, Long commentObjectId, Integer operatorId);

    void updateContent(Long id, Long commentObjectId, String content, String images);

    void updatePin(Long id, Long commentObjectId, boolean pin);

    void updateReplyCount(Long id, Long commentObjectId, int delta);

    void updateLikeCount(Long id, Long commentObjectId, int delta);

    void updateAuditStatus(Long id, Long commentObjectId, Integer auditStatus);
}
