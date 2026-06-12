package com.ssp.comment.dao.mapper;

import com.ssp.comment.dao.pojo.NotificationPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NotificationMapper {

    int insertSelective(NotificationPO record);

    long countUnread(@Param("userId") Integer userId);

    long countByUser(@Param("userId") Integer userId);

    List<NotificationPO> selectPageByUser(@Param("userId") Integer userId,
                                          @Param("offset") int offset,
                                          @Param("pageSize") int pageSize);

    int markRead(@Param("id") Long id, @Param("userId") Integer userId);

    int markAllRead(@Param("userId") Integer userId);
}
