package com.ssp.comment.repository;

import com.ssp.comment.entity.UserCommentIndexEntity;

import java.util.List;

public interface UserCommentIndexRepository {

    void saveOrUpdate(UserCommentIndexEntity indexEntity);

    long countByUser(Integer userId, Integer commentType, Integer interactionType);

    List<UserCommentIndexEntity> queryPageByUser(Integer userId, Integer commentType, Integer interactionType, int offset, int pageSize);
}
