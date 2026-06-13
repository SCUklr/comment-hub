import React, { useState } from 'react';
import { Button } from 'antd';
import { LikeOutlined, LikeFilled } from '@ant-design/icons';
import { like, unlike } from '../../api/like';

interface LikeButtonProps {
  targetId: number;
  targetType: number;
  liked: boolean;
  count: number;
  onChange?: () => void;
}

const LikeButton: React.FC<LikeButtonProps> = ({
  targetId,
  targetType,
  liked,
  count,
  onChange,
}) => {
  const [loading, setLoading] = useState(false);
  const [localLiked, setLocalLiked] = useState(liked);
  const [localCount, setLocalCount] = useState(count);

  const handleClick = async () => {
    if (loading) return;
    setLoading(true);
    try {
      if (localLiked) {
        await unlike({ targetId, targetType });
        setLocalLiked(false);
        setLocalCount((c) => c - 1);
      } else {
        await like({ targetId, targetType });
        setLocalLiked(true);
        setLocalCount((c) => c + 1);
      }
      onChange?.();
    } finally {
      setLoading(false);
    }
  };

  return (
    <Button
      type="text"
      size="small"
      loading={loading}
      icon={localLiked ? <LikeFilled className="like-animated" style={{ color: '#1890ff' }} /> : <LikeOutlined />}
      onClick={handleClick}
    >
      {localCount}
    </Button>
  );
};

export default LikeButton;
