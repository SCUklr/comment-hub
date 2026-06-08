package com.ssp.comment.repository;

import com.ssp.comment.entity.CommentEntity;

import java.util.List;

public interface CommentRepository {

    void save(CommentEntity commentEntity);

    CommentEntity queryById(Long id);

    List<CommentEntity> queryByIds(List<Long> ids);

    long countByObject(Long commentObjectId, Integer commentType);

    List<CommentEntity> queryPageByObject(Long commentObjectId, Integer commentType, int offset, int pageSize);

    List<CommentEntity> queryHotPageByObject(Long commentObjectId, Integer commentType, int offset, int pageSize);

    void updateDeleteMark(Long id, Integer operatorId);

    void updateContent(Long id, String content, String images);

    void updatePin(Long id, boolean pin);

    void updateReplyCount(Long id, int delta);

    void updateLikeCount(Long id, int delta);

    void updateAuditStatus(Long id, Integer auditStatus);
}
