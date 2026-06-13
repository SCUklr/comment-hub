import React, { useState } from 'react';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { UserContext } from './hooks/useUser';
import DemoPage from './pages/DemoPage';

const App: React.FC = () => {
  const [userId, setUserId] = useState<number>(() => {
    const stored = localStorage.getItem('comment_user_id');
    return stored ? parseInt(stored, 10) || 1 : 1;
  });

  const handleSetUserId = (id: number) => {
    localStorage.setItem('comment_user_id', id.toString());
    setUserId(id);
  };

  return (
    <ConfigProvider locale={zhCN}>
      <UserContext.Provider value={{ userId, setUserId: handleSetUserId }}>
        <DemoPage />
      </UserContext.Provider>
    </ConfigProvider>
  );
};

export default App;
