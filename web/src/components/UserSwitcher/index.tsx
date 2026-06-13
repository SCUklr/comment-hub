import React from 'react';
import { Select, Card } from 'antd';
import { useUser } from '../../hooks/useUser';

const UserSwitcher: React.FC = () => {
  const { userId, setUserId } = useUser();

  return (
    <Card size="small" title="切换用户（模拟登录）">
      <Select
        value={userId}
        onChange={setUserId}
        style={{ width: 200 }}
        options={[
          { value: 1, label: '用户 1' },
          { value: 2, label: '用户 2' },
          { value: 3, label: '用户 3' },
          { value: 4, label: '用户 4' },
          { value: 5, label: '用户 5' },
        ]}
      />
    </Card>
  );
};

export default UserSwitcher;
