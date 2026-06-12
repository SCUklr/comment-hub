package com.ssp.comment.dao.persistance;

import com.ssp.comment.dao.mapper.UserCommentIndexMapper;
import com.ssp.comment.dao.pojo.UserCommentIndexPO;
import com.ssp.comment.entity.UserCommentIndexEntity;
import com.ssp.comment.repository.UserCommentIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserCommentIndexRepositoryImpl implements UserCommentIndexRepository {

    @Autowired
    private UserCommentIndexMapper userCommentIndexMapper;

    @Override
    public void saveOrUpdate(UserCommentIndexEntity indexEntity) {
        UserCommentIndexPO po = new UserCommentIndexPO();
        po.setId(indexEntity.getId());
        po.setUserId(indexEntity.getUserId());
        po.setCommentObjectId(indexEntity.getCommentObjectId());
        po.setCommentType(indexEntity.getCommentType());
        po.setInteractionType(indexEntity.getInteractionType());
        po.setTargetCommentId(indexEntity.getTargetCommentId());
        po.setTargetReplyId(indexEntity.getTargetReplyId());
        po.setLatestContent(indexEntity.getLatestContent());
        po.setLatestTime(java.sql.Timestamp.valueOf(indexEntity.getLatestTime()));
        po.setInteractionCount(indexEntity.getInteractionCount());
        po.setIsDelete(indexEntity.getIsDelete());
        po.setCreateTime(java.sql.Timestamp.valueOf(indexEntity.getCreateTime()));
        po.setUpdateTime(java.sql.Timestamp.valueOf(indexEntity.getUpdateTime()));
        userCommentIndexMapper.upsertByUserAndObject(po);
    }

    @Override
    public int markDeleted(Integer userId, Long commentObjectId, Integer commentType, Integer interactionType) {
        return userCommentIndexMapper.markDeleted(userId, commentObjectId, commentType, interactionType);
    }

    @Override
    public long countByUser(Integer userId, Integer commentType, Integer interactionType) {
        return userCommentIndexMapper.countByUser(userId, commentType, interactionType);
    }

    @Override
    public List<UserCommentIndexEntity> queryPageByUser(Integer userId, Integer commentType, Integer interactionType, int offset, int pageSize) {
        List<UserCommentIndexPO> pos = userCommentIndexMapper.selectPageByUser(userId, commentType, interactionType, offset, pageSize);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    private UserCommentIndexEntity toEntity(UserCommentIndexPO po) {
        if (po == null) {
            return null;
        }
        UserCommentIndexEntity entity = new UserCommentIndexEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setCommentObjectId(po.getCommentObjectId());
        entity.setCommentType(po.getCommentType());
        entity.setInteractionType(po.getInteractionType());
        entity.setTargetCommentId(po.getTargetCommentId());
        entity.setTargetReplyId(po.getTargetReplyId());
        entity.setLatestContent(po.getLatestContent());
        entity.setInteractionCount(po.getInteractionCount());
        entity.setIsDelete(po.getIsDelete());
        if (po.getLatestTime() != null) {
            entity.setLatestTime(new java.sql.Timestamp(po.getLatestTime().getTime()).toLocalDateTime());
        }
        if (po.getCreateTime() != null) {
            entity.setCreateTime(new java.sql.Timestamp(po.getCreateTime().getTime()).toLocalDateTime());
        }
        if (po.getUpdateTime() != null) {
            entity.setUpdateTime(new java.sql.Timestamp(po.getUpdateTime().getTime()).toLocalDateTime());
        }
        return entity;
    }
}
