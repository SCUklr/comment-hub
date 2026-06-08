package com.ssp.comment.dao.persistance;

import com.ssp.comment.dao.mapper.CommentAuditMapper;
import com.ssp.comment.dao.pojo.CommentAuditPO;
import com.ssp.comment.entity.CommentAuditEntity;
import com.ssp.comment.repository.CommentAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
}
