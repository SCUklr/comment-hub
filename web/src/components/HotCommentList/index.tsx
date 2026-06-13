import React, { useState, useEffect, useCallback } from 'react';
import { List, Spin, Empty } from 'antd';
import { getHotCommentList } from '../../api/comment';
import { CommentItemVO, CommentHotListReqVO } from '../../types/comment';
import { DEFAULT_PAGE_SIZE } from '../../utils/constants';
import CommentItem from '../CommentItem';

interface HotCommentListProps {
  commentObjectId: number;
  commentType: number;
}

const HotCommentList: React.FC<HotCommentListProps> = ({
  commentObjectId,
  commentType,
}) => {
  const [comments, setComments] = useState<CommentItemVO[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchComments = useCallback(async () => {
    setLoading(true);
    try {
      const params: CommentHotListReqVO = {
        commentObjectId,
        commentType,
        page: 1,
        pageSize: DEFAULT_PAGE_SIZE,
      };
      const res = await getHotCommentList(params);
      setComments(res.list);
    } finally {
      setLoading(false);
    }
  }, [commentObjectId, commentType]);

  useEffect(() => {
    fetchComments();
  }, [fetchComments]);

  if (loading) {
    return <Spin style={{ display: 'block', margin: '40px auto' }} />;
  }

  if (comments.length === 0) {
    return <Empty description="暂无热门评论" />;
  }

  return (
    <List
      dataSource={comments}
      renderItem={(item) => (
        <List.Item key={item.id}>
          <CommentItem comment={item} />
        </List.Item>
      )}
    />
  );
};

export default HotCommentList;
