package com.ssp.comment.repository;

import com.ssp.comment.entity.CommentLikeEntity;

public interface CommentLikeRepository {

    boolean exists(Integer userId, Long targetId, Integer targetType);

    void save(CommentLikeEntity commentLikeEntity);

    boolean delete(Integer userId, Long targetId, Integer targetType);
}
