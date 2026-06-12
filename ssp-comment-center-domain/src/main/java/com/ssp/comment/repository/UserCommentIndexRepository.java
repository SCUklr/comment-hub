package com.ssp.comment.repository;

import com.ssp.comment.entity.UserCommentIndexEntity;

import java.util.List;

public interface UserCommentIndexRepository {

    void saveOrUpdate(UserCommentIndexEntity indexEntity);

    long countByUser(Integer userId, Integer commentType, Integer interactionType);

    List<UserCommentIndexEntity> queryPageByUser(Integer userId, Integer commentType, Integer interactionType, int offset, int pageSize);

    /**
     * 将指定用户在指定评论对象上的互动索引标记为已删除。
     *
     * @param userId          用户ID
     * @param commentObjectId 评论对象ID
     * @param commentType     评论对象类型
     * @param interactionType 互动类型：1评论 2回复
     * @return 实际更新的记录数
     */
    int markDeleted(Integer userId, Long commentObjectId, Integer commentType, Integer interactionType);
}
