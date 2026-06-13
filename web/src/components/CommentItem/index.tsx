import React, { useState } from 'react';
import { Avatar, Typography, Space, Divider, Input, Button } from 'antd';
import { CommentItemVO } from '../../types/comment';
import { createComment } from '../../api/comment';
import { useUser } from '../../hooks/useUser';
import LikeButton from '../LikeButton';
import ReplyTree from '../ReplyTree';

const { Text, Paragraph } = Typography;

interface CommentItemProps {
  comment: CommentItemVO;
  onRefresh?: () => void;
}

const CommentItem: React.FC<CommentItemProps> = ({ comment, onRefresh }) => {
  const [showReplies, setShowReplies] = useState(false);
  const [showReplyInput, setShowReplyInput] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { userId } = useUser();

  const handleReply = async () => {
    if (!replyContent.trim()) return;
    setSubmitting(true);
    try {
      await createComment({
        type: 2,
        commentId: comment.id,
        parentId: 0,
        content: replyContent.trim(),
      });
      setReplyContent('');
      setShowReplyInput(false);
      setShowReplies(true);
      onRefresh?.();
    } finally {
      setSubmitting(false);
    }
  };

  const avatarColor = (id: number) => {
    const colors = ['#f56a00', '#7265e6', '#ffbf00', '#00a2ae', '#87d068'];
    return colors[id % colors.length];
  };

  return (
    <div className="comment-item" style={{ width: '100%' }}>
      <Space align="start">
        <Avatar style={{ backgroundColor: avatarColor(comment.userId) }}>
          {comment.userId}
        </Avatar>
        <div style={{ flex: 1 }}>
          <Text type="secondary">用户 {comment.userId}</Text>
          <Paragraph style={{ margin: '8px 0' }}>{comment.content}</Paragraph>
          <Space split={<Divider type="vertical" />}>
            <LikeButton
              targetId={comment.id}
              targetType={1}
              liked={comment.liked}
              count={comment.likeCount}
              onChange={onRefresh}
            />
            {comment.replyCount > 0 && (
              <Text
                type="secondary"
                style={{ cursor: 'pointer' }}
                onClick={() => setShowReplies(!showReplies)}
              >
                {comment.replyCount} 条回复
              </Text>
            )}
            <Text
              type="secondary"
              style={{ cursor: 'pointer' }}
              onClick={() => {
                if (!userId) {
                  return;
                }
                setShowReplyInput(!showReplyInput);
              }}
            >
              回复
            </Text>
            <Text type="secondary">{comment.createTime}</Text>
          </Space>
          {showReplyInput && (
            <div style={{ marginTop: 12 }}>
              <Input.TextArea
                rows={2}
                placeholder="写下你的回复..."
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
              />
              <Space style={{ marginTop: 8 }}>
                <Button
                  type="primary"
                  size="small"
                  loading={submitting}
                  disabled={!replyContent.trim()}
                  onClick={handleReply}
                >
                  提交
                </Button>
                <Button size="small" onClick={() => setShowReplyInput(false)}>
                  取消
                </Button>
              </Space>
            </div>
          )}
          {showReplies && comment.topReplies && comment.topReplies.length > 0 && (
            <div style={{ marginTop: 12 }}>
              <ReplyTree replies={comment.topReplies} commentId={comment.id} />
            </div>
          )}
        </div>
      </Space>
    </div>
  );
};

export default CommentItem;
