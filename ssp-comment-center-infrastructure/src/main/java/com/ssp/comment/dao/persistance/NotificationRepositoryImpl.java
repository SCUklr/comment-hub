package com.ssp.comment.dao.persistance;

import com.ssp.comment.dao.mapper.NotificationMapper;
import com.ssp.comment.dao.pojo.NotificationPO;
import com.ssp.comment.entity.NotificationEntity;
import com.ssp.comment.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void save(NotificationEntity entity) {
        NotificationPO po = new NotificationPO();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setType(entity.getType());
        po.setSubjectId(entity.getSubjectId());
        po.setSubjectType(entity.getSubjectType());
        po.setActorId(entity.getActorId());
        po.setCommentObjectId(entity.getCommentObjectId());
        po.setCommentType(entity.getCommentType());
        po.setContent(entity.getContent());
        po.setIsRead(entity.getIsRead());
        po.setIsDelete(entity.getIsDelete());
        po.setCreateTime(java.sql.Timestamp.valueOf(entity.getCreateTime()));
        po.setUpdateTime(java.sql.Timestamp.valueOf(entity.getUpdateTime()));
        notificationMapper.insertSelective(po);
    }

    @Override
    public long countUnread(Integer userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public long countByUser(Integer userId) {
        return notificationMapper.countByUser(userId);
    }

    @Override
    public List<NotificationEntity> queryPage(Integer userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<NotificationPO> pos = notificationMapper.selectPageByUser(userId, offset, pageSize);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public boolean markRead(Long id, Integer userId) {
        return notificationMapper.markRead(id, userId) > 0;
    }

    @Override
    public boolean markAllRead(Integer userId) {
        return notificationMapper.markAllRead(userId) > 0;
    }

    private NotificationEntity toEntity(NotificationPO po) {
        if (po == null) {
            return null;
        }
        NotificationEntity entity = new NotificationEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setType(po.getType());
        entity.setSubjectId(po.getSubjectId());
        entity.setSubjectType(po.getSubjectType());
        entity.setActorId(po.getActorId());
        entity.setCommentObjectId(po.getCommentObjectId());
        entity.setCommentType(po.getCommentType());
        entity.setContent(po.getContent());
        entity.setIsRead(po.getIsRead());
        entity.setIsDelete(po.getIsDelete());
        if (po.getCreateTime() != null) {
            entity.setCreateTime(new java.sql.Timestamp(po.getCreateTime().getTime()).toLocalDateTime());
        }
        if (po.getUpdateTime() != null) {
            entity.setUpdateTime(new java.sql.Timestamp(po.getUpdateTime().getTime()).toLocalDateTime());
        }
        return entity;
    }
}
