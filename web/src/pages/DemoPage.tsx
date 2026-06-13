import React, { useState } from 'react';
import { Card, Tabs, Typography } from 'antd';
import CommentList from '../components/CommentList';
import HotCommentList from '../components/HotCommentList';
import UserSwitcher from '../components/UserSwitcher';
import { DEFAULT_OBJECT_ID, DEFAULT_COMMENT_TYPE } from '../utils/constants';

const { Title } = Typography;

const DemoPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('comments');

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
      <Title level={3} style={{ textAlign: 'center' }}>
        评论中心演示
      </Title>
      <UserSwitcher />
      <Card style={{ marginTop: 16 }}>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'comments',
              label: '全部评论',
              children: (
                <CommentList
                  commentObjectId={DEFAULT_OBJECT_ID}
                  commentType={DEFAULT_COMMENT_TYPE}
                />
              ),
            },
            {
              key: 'hot',
              label: '热门评论',
              children: (
                <HotCommentList
                  commentObjectId={DEFAULT_OBJECT_ID}
                  commentType={DEFAULT_COMMENT_TYPE}
                />
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
};

export default DemoPage;
