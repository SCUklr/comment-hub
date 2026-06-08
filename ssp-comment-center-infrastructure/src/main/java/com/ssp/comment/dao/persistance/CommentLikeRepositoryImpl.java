package com.ssp.comment.dao.persistance;

import com.ssp.comment.dao.mapper.CommentLikeMapper;
import com.ssp.comment.dao.pojo.CommentLikePO;
import com.ssp.comment.entity.CommentLikeEntity;
import com.ssp.comment.repository.CommentLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CommentLikeRepositoryImpl implements CommentLikeRepository {

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    public boolean exists(Integer userId, Long targetId, Integer targetType) {
        return commentLikeMapper.countByUserAndTarget(userId, targetId, targetType) > 0;
    }

    @Override
    public void save(CommentLikeEntity commentLikeEntity) {
        CommentLikePO po = new CommentLikePO();
        po.setId(commentLikeEntity.getId());
        po.setUserId(commentLikeEntity.getUserId());
        po.setTargetId(commentLikeEntity.getTargetId());
        po.setTargetType(commentLikeEntity.getTargetType());
        po.setCreateTime(java.sql.Timestamp.valueOf(commentLikeEntity.getCreateTime()));
        commentLikeMapper.insertSelective(po);
    }

    @Override
    public boolean delete(Integer userId, Long targetId, Integer targetType) {
        return commentLikeMapper.deleteByUserAndTarget(userId, targetId, targetType) > 0;
    }
}
