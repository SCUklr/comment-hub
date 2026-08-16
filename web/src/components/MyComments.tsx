import { useCallback, useEffect, useState } from 'react';
import { listMyComments } from '../api/comment';
import type { MyCommentItem } from '../types';
import { formatTime } from '../utils/constants';
import { toast } from '../utils/toast';

export default function MyComments({ currentUserId }: { currentUserId: number }) {
  const [list, setList] = useState<MyCommentItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [interactionType, setInteractionType] = useState<number | undefined>(undefined);
  const [loading, setLoading] = useState(false);

  const pageSize = 10;

  const load = useCallback(
    async (p: number, it: number | undefined) => {
      setLoading(true);
      try {
        const resp = await listMyComments({ interactionType: it, page: p, pageSize });
        setList(resp.list);
        setTotal(resp.total);
        setPage(p);
      } catch (e) {
        toast((e as Error).message, 'error');
      } finally {
        setLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    load(1, interactionType);
  }, [load, interactionType, currentUserId]);

  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  return (
    <div className="panel">
      <div className="panel-title">
        <span>我评论过的内容（用户 {currentUserId}）</span>
        <span className="count">共 {total} 条</span>
      </div>

      <div className="audit-row" style={{ marginBottom: 10 }}>
        <span style={{ fontSize: 13 }}>过滤：</span>
        {[
          { label: '全部', value: undefined },
          { label: '仅评论', value: 1 },
          { label: '仅回复', value: 2 },
        ].map((o) => (
          <button
            key={o.label}
            className={`btn sm ${interactionType === o.value ? 'primary' : ''}`}
            onClick={() => setInteractionType(o.value)}
          >
            {o.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="empty">加载中...</div>
      ) : list.length === 0 ? (
        <div className="empty">当前用户还没有评论/回复记录</div>
      ) : (
        list.map((item) => (
          <div className="my-item" key={`${item.commentObjectId}-${item.commentType}-${item.interactionType}`}>
            <span className={`comment-avatar ${item.interactionType === 1 ? 'avatar-2' : 'avatar-3'}`} style={{ width: 26, height: 26, fontSize: 12 }}>
              {item.interactionType === 1 ? '评' : '复'}
            </span>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                <span style={{ fontWeight: 600, fontSize: 13 }}>
                  对象 {item.commentObjectId} · 类型 {item.commentType}
                </span>
                <span className="comment-meta">
                  {item.interactionType === 1 ? '评论' : '回复'} {item.interactionCount} 次 · {formatTime(item.latestTime)}
                </span>
              </div>
              <div className="comment-content" style={{ fontSize: 13 }}>{item.latestContent}</div>
            </div>
          </div>
        ))
      )}

      {totalPages > 1 && (
        <div className="pager">
          <button className="btn sm" disabled={page <= 1} onClick={() => load(page - 1, interactionType)}>上一页</button>
          <span>{page} / {totalPages}</span>
          <button className="btn sm" disabled={page >= totalPages} onClick={() => load(page + 1, interactionType)}>下一页</button>
        </div>
      )}
    </div>
  );
}
