import { useCallback, useEffect, useState } from 'react';
import AuditPanel from './components/AuditPanel';
import CommentPanel from './components/CommentPanel';
import MyComments from './components/MyComments';
import NotificationBox from './components/NotificationBox';
import UserSwitcher from './components/UserSwitcher';
import { getCurrentUserId, setCurrentUserId } from './api/request';
import { registerToast } from './utils/toast';
import type { ToastType } from './utils/toast';

type TabKey = 'comment' | 'my' | 'notification' | 'audit';

interface ToastItem {
  id: number;
  msg: string;
  type: ToastType;
}

export default function App() {
  const [userId, setUserId] = useState(getCurrentUserId());
  const [tab, setTab] = useState<TabKey>('comment');
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  useEffect(() => {
    registerToast((msg, type = 'success') => {
      const id = Date.now() + Math.random();
      setToasts((t) => [...t, { id, msg, type }]);
      setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 2600);
    });
  }, []);

  const switchUser = useCallback((id: number) => {
    setCurrentUserId(id);
    setUserId(id);
  }, []);

  const tabs: { key: TabKey; label: string }[] = [
    { key: 'comment', label: '💬 评论区' },
    { key: 'my', label: '📝 我的评论' },
    { key: 'notification', label: '🔔 通知' },
    { key: 'audit', label: '🛡 审核' },
  ];

  return (
    <div className="app">
      <div className="topbar">
        <h1>
          ssp-comment-center <span className="sub">通用评论中心 · 前端演示 Demo</span>
        </h1>
        <UserSwitcher userId={userId} onChange={switchUser} />
      </div>

      <div className="tabs">
        {tabs.map((t) => (
          <button key={t.key} className={`tab ${tab === t.key ? 'active' : ''}`} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'comment' && <CommentPanel currentUserId={userId} />}
      {tab === 'my' && <MyComments currentUserId={userId} />}
      {tab === 'notification' && <NotificationBox currentUserId={userId} />}
      {tab === 'audit' && <AuditPanel />}

      <div className="toast-container">
        {toasts.map((t) => (
          <div key={t.id} className={`toast ${t.type}`}>
            {t.msg}
          </div>
        ))}
      </div>
    </div>
  );
}
