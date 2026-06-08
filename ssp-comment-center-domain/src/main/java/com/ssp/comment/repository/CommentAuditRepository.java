package com.ssp.comment.repository;

import com.ssp.comment.entity.CommentAuditEntity;

public interface CommentAuditRepository {

    void save(CommentAuditEntity commentAuditEntity);
}
