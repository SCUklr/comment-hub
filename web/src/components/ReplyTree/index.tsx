import React, { useState, useCallback } from 'react';
import { List, Typography, Space, Input, Button, message } from 'antd';
import { ReplyItemVO } from '../../types/comment';
import { createComment, getReplyList } from '../../api/comment';
import { useUser } from '../../hooks/useUser';
import LikeButton from '../LikeButton';

const { Text } = Typography;

interface ReplyTreeProps {
  replies: ReplyItemVO[];
  commentId: number;
}

const ReplyTree: React.FC<ReplyTreeProps> = ({ replies: initialReplies, commentId }) => {
  const [replies, setReplies] = useState<ReplyItemVO[]>(initialReplies);
  const [loadingMore, setLoadingMore] = useState(false);
  const [replyTo, setReplyTo] = useState<ReplyItemVO | null>(null);
  const [replyContent, setReplyContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { userId } = useUser();

  const loadMore = useCallback(async () => {
    setLoadingMore(true);
    try {
      const res = await getReplyList({
        commentId,
        parentId: 0,
        page: 1,
        pageSize: 20,
      });
      setReplies(res.list);
    } finally {
      setLoadingMore(false);
    }
  }, [commentId]);

  const handleReply = (item: ReplyItemVO) => {
    if (!userId) {
      message.warning('请先设置用户 ID');
      return;
    }
    setReplyTo(item);
    setReplyContent(`@${item.replyUserId} `);
  };

  const handleSubmit = async () => {
    if (!replyContent.trim()) return;
    setSubmitting(true);
    try {
      await createComment({
        type: 2,
        commentId,
        parentId: replyTo?.id || 0,
        beRepliedUserId: replyTo?.replyUserId,
        content: replyContent.trim(),
      });
      message.success('回复成功');
      setReplyTo(null);
      setReplyContent('');
      loadMore();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="comment-nested">
      <List
        dataSource={replies}
        renderItem={(item) => (
          <List.Item key={item.id} style={{ padding: '8px 0' }}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Space>
                <Text type="secondary">用户 {item.replyUserId}</Text>
                {item.beRepliedUserId > 0 && (
                  <Text type="secondary">
                    回复 用户 {item.beRepliedUserId}
                  </Text>
                )}
              </Space>
              <Text>{item.content}</Text>
              <Space>
                <LikeButton
                  targetId={item.id}
                  targetType={2}
                  liked={item.liked}
                  count={item.likeCount}
                />
                <Text type="secondary">{item.createTime}</Text>
                <Button
                  type="link"
                  size="small"
                  onClick={() => handleReply(item)}
                >
                  回复
                </Button>
              </Space>
            </Space>
          </List.Item>
        )}
      />
      {replies.length >= 3 && (
        <Button
          type="link"
          loading={loadingMore}
          onClick={loadMore}
          block
        >
          加载更多回复
        </Button>
      )}
      {replyTo && (
        <div style={{ marginTop: 12, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
          <Text type="secondary">回复: {replyTo.content.slice(0, 30)}...</Text>
          <Input.TextArea
            rows={2}
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            style={{ marginTop: 8 }}
          />
          <Space style={{ marginTop: 8 }}>
            <Button type="primary" size="small" loading={submitting} onClick={handleSubmit}>
              提交
            </Button>
            <Button size="small" onClick={() => setReplyTo(null)}>
              取消
            </Button>
          </Space>
        </div>
      )}
    </div>
  );
};

export default ReplyTree;
