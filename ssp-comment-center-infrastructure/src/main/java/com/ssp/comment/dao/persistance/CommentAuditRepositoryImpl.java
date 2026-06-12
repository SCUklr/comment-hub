package com.ssp.comment.dao.persistance;

import com.ssp.comment.dao.mapper.CommentAuditMapper;
import com.ssp.comment.dao.pojo.CommentAuditPO;
import com.ssp.comment.entity.CommentAuditEntity;
import com.ssp.comment.repository.CommentAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CommentAuditRepositoryImpl implements CommentAuditRepository {

    @Autowired
    private CommentAuditMapper commentAuditMapper;

    @Override
    public void save(CommentAuditEntity commentAuditEntity) {
        CommentAuditPO po = new CommentAuditPO();
        po.setId(commentAuditEntity.getId());
        po.setTargetId(commentAuditEntity.getTargetId());
        po.setTargetType(commentAuditEntity.getTargetType());
        po.setAuditContent(commentAuditEntity.getAuditContent());
        po.setAuditStatus(commentAuditEntity.getAuditStatus());
        po.setAuditReason(commentAuditEntity.getAuditReason());
        po.setAuditOperator(commentAuditEntity.getAuditOperator());
        po.setAuditTime(java.sql.Timestamp.valueOf(commentAuditEntity.getAuditTime()));
        commentAuditMapper.insertSelective(po);
    }

    @Override
    public List<CommentAuditEntity> queryByTarget(Long targetId, Integer targetType) {
        List<CommentAuditPO> pos = commentAuditMapper.selectByTarget(targetId, targetType);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    private CommentAuditEntity toEntity(CommentAuditPO po) {
        CommentAuditEntity entity = new CommentAuditEntity();
        entity.setId(po.getId());
        entity.setTargetId(po.getTargetId());
        entity.setTargetType(po.getTargetType());
        entity.setAuditContent(po.getAuditContent());
        entity.setAuditStatus(po.getAuditStatus());
        entity.setAuditReason(po.getAuditReason());
        entity.setAuditOperator(po.getAuditOperator());
        if (po.getAuditTime() != null) {
            entity.setAuditTime(new java.sql.Timestamp(po.getAuditTime().getTime()).toLocalDateTime());
        }
        return entity;
    }
}
