package com.ssp.comment.dao.persistance;

import com.ssp.comment.dao.mapper.ReplyMapper;
import com.ssp.comment.dao.pojo.ReplyPO;
import com.ssp.comment.entity.ReplyEntity;
import com.ssp.comment.repository.ReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ReplyRepositoryImpl implements ReplyRepository {

    @Autowired
    private ReplyMapper replyMapper;

    @Override
    public void save(ReplyEntity replyEntity) {
        ReplyPO po = new ReplyPO();
        po.setId(replyEntity.getId());
        po.setCommentId(replyEntity.getCommentId());
        po.setParentReplyId(replyEntity.getParentReplyId());
        po.setReplyType(replyEntity.getReplyType());
        po.setContent(replyEntity.getContent());
        po.setImages(replyEntity.getImages());
        po.setReplyUserId(replyEntity.getReplyUserId());
        po.setBeRepliedUserId(replyEntity.getBeRepliedUserId());
        po.setLikeCount(replyEntity.getLikeCount());
        po.setAuditStatus(replyEntity.getAuditStatus());
        po.setIsDelete(replyEntity.getIsDelete());
        po.setCreateTime(java.sql.Timestamp.valueOf(replyEntity.getCreateTime()));
        po.setUpdateTime(java.sql.Timestamp.valueOf(replyEntity.getUpdateTime()));
        replyMapper.insertSelective(po);
    }

    @Override
    public ReplyEntity queryById(Long id) {
        ReplyPO po = replyMapper.selectOneById(id);
        return toEntity(po);
    }

    @Override
    public long countByCommentAndParent(Long commentId, Long parentReplyId, Integer currentUserId) {
        return replyMapper.countByCommentAndParent(commentId, parentReplyId, currentUserId);
    }

    @Override
    public List<ReplyEntity> queryPageByCommentAndParent(Long commentId, Long parentReplyId, int offset, int pageSize, Integer currentUserId) {
        List<ReplyPO> pos = replyMapper.selectPageByCommentAndParent(commentId, parentReplyId, offset, pageSize, currentUserId);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<ReplyEntity> queryByCommentIdsAndParent(List<Long> commentIds, Long parentReplyId, Integer currentUserId) {
        List<ReplyPO> pos = replyMapper.selectByCommentIdsAndParent(commentIds, parentReplyId, currentUserId);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public void updateDeleteMark(Long id, Integer operatorId) {
        replyMapper.updateDeleteMark(id, 1);
    }

    @Override
    public void updateContent(Long id, String content, String images) {
        replyMapper.updateContent(id, content, images);
    }

    @Override
    public void updateLikeCount(Long id, int delta) {
        replyMapper.updateLikeCount(id, delta);
    }

    @Override
    public void updateAuditStatus(Long id, Integer auditStatus) {
        replyMapper.updateAuditStatus(id, auditStatus);
    }

    private ReplyEntity toEntity(ReplyPO po) {
        if (po == null) {
            return null;
        }
        ReplyEntity entity = new ReplyEntity();
        entity.setId(po.getId());
        entity.setCommentId(po.getCommentId());
        entity.setParentReplyId(po.getParentReplyId());
        entity.setReplyType(po.getReplyType());
        entity.setContent(po.getContent());
        entity.setImages(po.getImages());
        entity.setReplyUserId(po.getReplyUserId());
        entity.setBeRepliedUserId(po.getBeRepliedUserId());
        entity.setLikeCount(po.getLikeCount());
        entity.setAuditStatus(po.getAuditStatus());
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
