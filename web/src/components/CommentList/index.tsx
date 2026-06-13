import React, { useState, useEffect, useCallback } from 'react';
import { List, Pagination, Spin, Empty, Radio, Input, Button, message } from 'antd';
import { getCommentList, getHotCommentList, createComment } from '../../api/comment';
import { CommentItemVO, CommentListReqVO } from '../../types/comment';
import { DEFAULT_PAGE_SIZE } from '../../utils/constants';
import CommentItem from '../CommentItem';

interface CommentListProps {
  commentObjectId: number;
  commentType: number;
  topReplyLimit?: number;
}

const CommentList: React.FC<CommentListProps> = ({
  commentObjectId,
  commentType,
  topReplyLimit = 3,
}) => {
  const [comments, setComments] = useState<CommentItemVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize] = useState(DEFAULT_PAGE_SIZE);
  const [sort, setSort] = useState<'timeDesc' | 'timeAsc' | 'hot'>('timeDesc');
  const [submitting, setSubmitting] = useState(false);
  const [newComment, setNewComment] = useState('');

  const fetchComments = useCallback(async () => {
    setLoading(true);
    try {
      const params: CommentListReqVO = {
        commentObjectId,
        commentType,
        page,
        pageSize,
        topReplyLimit,
      };
      let res;
      if (sort === 'hot') {
        res = await getHotCommentList({ commentObjectId, commentType, page, pageSize });
      } else {
        res = await getCommentList(params);
      }
      setComments(res.list);
      setTotal(res.total);
    } finally {
      setLoading(false);
    }
  }, [commentObjectId, commentType, page, pageSize, topReplyLimit, sort]);

  useEffect(() => {
    fetchComments();
  }, [fetchComments]);

  const handleSortChange = (e: any) => {
    setSort(e.target.value);
    setPage(1);
  };

  const handleSubmit = async () => {
    if (!newComment.trim()) return;
    setSubmitting(true);
    try {
      await createComment({
        type: 1,
        commentObjectId,
        commentType,
        content: newComment.trim(),
      });
      message.success('评论发布成功');
      setNewComment('');
      setPage(1);
      fetchComments();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Input.TextArea
          rows={3}
          placeholder="发表评论..."
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          maxLength={500}
          showCount
        />
        <Button
          type="primary"
          onClick={handleSubmit}
          loading={submitting}
          disabled={!newComment.trim()}
          style={{ marginTop: 8, float: 'right' }}
        >
          发布评论
        </Button>
        <div style={{ clear: 'both' }} />
      </div>
      <div style={{ marginBottom: 16 }}>
        <Radio.Group value={sort} onChange={handleSortChange}>
          <Radio.Button value="timeDesc">最新</Radio.Button>
          <Radio.Button value="timeAsc">最早</Radio.Button>
          <Radio.Button value="hot">热评</Radio.Button>
        </Radio.Group>
      </div>
      {loading && comments.length === 0 ? (
        <Spin style={{ display: 'block', margin: '40px auto' }} />
      ) : comments.length === 0 ? (
        <Empty description="暂无评论" />
      ) : (
        <>
          <List
            dataSource={comments}
            renderItem={(item) => (
              <List.Item key={item.id}>
                <CommentItem comment={item} onRefresh={fetchComments} />
              </List.Item>
            )}
          />
          <Pagination
            current={page}
            pageSize={pageSize}
            total={total}
            onChange={setPage}
            style={{ marginTop: 16, textAlign: 'right' }}
          />
        </>
      )}
    </div>
  );
};

export default CommentList;
