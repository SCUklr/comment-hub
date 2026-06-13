package com.ssp.comment.dao.persistance;

import com.ssp.comment.dao.mapper.CommentMapper;
import com.ssp.comment.dao.pojo.CommentPO;
import com.ssp.comment.entity.CommentEntity;
import com.ssp.comment.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public void save(CommentEntity commentEntity) {
        CommentPO po = new CommentPO();
        po.setId(commentEntity.getId());
        po.setCommentObjectId(commentEntity.getCommentObjectId());
        po.setCommentType(commentEntity.getCommentType());
        po.setContent(commentEntity.getContent());
        po.setImages(commentEntity.getImages());
        po.setCommentUserId(commentEntity.getCommentUserId());
        po.setSort(commentEntity.getSort());
        po.setReplyCount(commentEntity.getReplyCount());
        po.setLikeCount(commentEntity.getLikeCount());
        po.setAuditStatus(commentEntity.getAuditStatus());
        po.setIsDelete(commentEntity.getIsDelete());
        po.setCreateTime(java.sql.Timestamp.valueOf(commentEntity.getCreateTime()));
        po.setUpdateTime(java.sql.Timestamp.valueOf(commentEntity.getUpdateTime()));
        commentMapper.insertSelective(po);
    }

    @Override
    public CommentEntity queryById(Long id) {
        CommentPO po = commentMapper.selectOneById(id);
        return toEntity(po);
    }

    @Override
    public CommentEntity queryById(Long id, Long commentObjectId) {
        CommentPO po = commentMapper.selectOneByIdWithObject(id, commentObjectId);
        return toEntity(po);
    }

    @Override
    public List<CommentEntity> queryByIds(List<Long> ids) {
        List<CommentPO> pos = commentMapper.selectByIds(ids);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> queryByIds(List<Long> ids, Long commentObjectId) {
        List<CommentPO> pos = commentMapper.selectByIdsWithObject(ids, commentObjectId);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public long countByObject(Long commentObjectId, Integer commentType, Integer currentUserId) {
        return commentMapper.countByObject(commentObjectId, commentType, currentUserId);
    }

    @Override
    public List<CommentEntity> queryPageByObject(Long commentObjectId, Integer commentType, int offset, int pageSize, Integer currentUserId) {
        List<CommentPO> pos = commentMapper.selectPageByObject(commentObjectId, commentType, offset, pageSize, currentUserId);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> queryHotPageByObject(Long commentObjectId, Integer commentType, int offset, int pageSize, Integer currentUserId) {
        List<CommentPO> pos = commentMapper.selectHotPageByObject(commentObjectId, commentType, offset, pageSize, currentUserId);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public long countForAudit(Long commentObjectId, Integer commentType) {
        return commentMapper.countForAudit(commentObjectId, commentType);
    }

    @Override
    public List<CommentEntity> queryPageForAudit(Long commentObjectId, Integer commentType, int offset, int pageSize) {
        List<CommentPO> pos = commentMapper.selectPageForAudit(commentObjectId, commentType, offset, pageSize);
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public void updateDeleteMark(Long id, Long commentObjectId, Integer operatorId) {
        commentMapper.updateDeleteMark(id, commentObjectId, 1);
    }

    @Override
    public void updateContent(Long id, Long commentObjectId, String content, String images) {
        commentMapper.updateContent(id, commentObjectId, content, images);
    }

    @Override
    public void updatePin(Long id, Long commentObjectId, boolean pin) {
        commentMapper.updatePin(id, commentObjectId, pin ? 999 : 0);
    }

    @Override
    public void updateReplyCount(Long id, Long commentObjectId, int delta) {
        commentMapper.updateReplyCount(id, commentObjectId, delta);
    }

    @Override
    public void updateLikeCount(Long id, Long commentObjectId, int delta) {
        commentMapper.updateLikeCount(id, commentObjectId, delta);
    }

    @Override
    public void updateAuditStatus(Long id, Long commentObjectId, Integer auditStatus) {
        commentMapper.updateAuditStatus(id, commentObjectId, auditStatus);
    }

    private CommentEntity toEntity(CommentPO po) {
        if (po == null) {
            return null;
        }
        CommentEntity entity = new CommentEntity();
        entity.setId(po.getId());
        entity.setCommentObjectId(po.getCommentObjectId());
        entity.setCommentType(po.getCommentType());
        entity.setContent(po.getContent());
        entity.setImages(po.getImages());
        entity.setCommentUserId(po.getCommentUserId());
        entity.setSort(po.getSort());
        entity.setReplyCount(po.getReplyCount());
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
