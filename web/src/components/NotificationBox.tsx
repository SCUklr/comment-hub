import { useCallback, useEffect, useState } from 'react';
import { listNotifications, markAllRead, markRead } from '../api/notification';
import type { NotificationItem } from '../types';
import { formatTime, NOTIFICATION_TYPE_MAP } from '../utils/constants';
import { toast } from '../utils/toast';

export default function NotificationBox({ currentUserId }: { currentUserId: number }) {
  const [list, setList] = useState<NotificationItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const pageSize = 10;

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      const resp = await listNotifications({ page: p, pageSize });
      setList(resp.list);
      setTotal(resp.total);
      setPage(p);
    } catch (e) {
      toast((e as Error).message, 'error');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(1);
  }, [load, currentUserId]);

  const unread = list.filter((n) => n.isRead === 0).length;

  const handleRead = async (n: NotificationItem) => {
    try {
      await markRead({ id: n.id });
      toast('已标记已读');
      load(page);
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  const handleReadAll = async () => {
    try {
      await markAllRead();
      toast('全部已读');
      load(1);
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  return (
    <div className="panel">
      <div className="panel-title">
        <span>我的通知（用户 {currentUserId}）{unread > 0 && <span className="badge" style={{ background: '#fff1f0', color: '#cf1322' }}>{unread} 未读</span>}</span>
        <button className="btn sm" onClick={handleReadAll}>全部已读</button>
      </div>

      {loading ? (
        <div className="empty">加载中...</div>
      ) : list.length === 0 ? (
        <div className="empty">暂无通知（让其他用户回复/点赞你试试）</div>
      ) : (
        list.map((n) => (
          <div className={`notify-item ${n.isRead === 0 ? 'unread' : ''}`} key={n.id}>
            <span className="comment-avatar avatar-3" style={{ width: 26, height: 26, fontSize: 12 }}>
              {NOTIFICATION_TYPE_MAP[n.type] ?? n.type}
            </span>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                <span style={{ fontSize: 13 }}>
                  用户 <b>{n.actorId}</b> {NOTIFICATION_TYPE_MAP[n.type] ?? '操作'}了你的
                  {n.subjectType === 1 ? '评论' : '回复'}
                  <span className="comment-meta">（对象 {n.commentObjectId}）</span>
                </span>
                <span className="comment-meta">{formatTime(n.createTime)}</span>
              </div>
              <div className="comment-content" style={{ fontSize: 13 }}>{n.content}</div>
              <div className="reply-actions">
                {n.isRead === 0 ? (
                  <button className="btn sm" onClick={() => handleRead(n)}>标记已读</button>
                ) : (
                  <span className="count">已读</span>
                )}
              </div>
            </div>
          </div>
        ))
      )}

      {totalPages > 1 && (
        <div className="pager">
          <button className="btn sm" disabled={page <= 1} onClick={() => load(page - 1)}>上一页</button>
          <span>{page} / {totalPages}</span>
          <button className="btn sm" disabled={page >= totalPages} onClick={() => load(page + 1)}>下一页</button>
        </div>
      )}
    </div>
  );
}
