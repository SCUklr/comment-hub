package com.ssp.comment.repository;

import com.ssp.comment.entity.NotificationEntity;

import java.util.List;

public interface NotificationRepository {

    void save(NotificationEntity entity);

    long countUnread(Integer userId);

    long countByUser(Integer userId);

    List<NotificationEntity> queryPage(Integer userId, Integer page, Integer pageSize);

    boolean markRead(Long id, Integer userId);

    boolean markAllRead(Integer userId);
}
