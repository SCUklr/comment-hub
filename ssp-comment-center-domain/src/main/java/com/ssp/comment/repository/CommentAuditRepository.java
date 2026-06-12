package com.ssp.comment.repository;

import com.ssp.comment.entity.CommentAuditEntity;

import java.util.List;

public interface CommentAuditRepository {

    void save(CommentAuditEntity commentAuditEntity);

    List<CommentAuditEntity> queryByTarget(Long targetId, Integer targetType);
}
