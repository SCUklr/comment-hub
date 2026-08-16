import { useState } from 'react';
import { auditCallback, auditHistory, listAuditComments } from '../api/comment';
import type { AuditHistoryItem, CommentItem } from '../types';
import { AUDIT_STATUS_MAP, DEFAULT_OBJECT_ID, DEFAULT_OBJECT_TYPE, formatTime } from '../utils/constants';
import { toast } from '../utils/toast';

export default function AuditPanel() {
  // 审核回调表单
  const [targetId, setTargetId] = useState('');
  const [targetType, setTargetType] = useState(1);
  const [auditStatus, setAuditStatus] = useState(1);
  const [auditReason, setAuditReason] = useState('');

  // 待审核列表
  const [auditList, setAuditList] = useState<CommentItem[]>([]);
  const [auditListLoading, setAuditListLoading] = useState(false);

  // 审核历史
  const [history, setHistory] = useState<AuditHistoryItem[]>([]);
  const [historyLoaded, setHistoryLoaded] = useState(false);

  const loadAuditList = async () => {
    setAuditListLoading(true);
    try {
      const resp = await listAuditComments({ commentObjectId: DEFAULT_OBJECT_ID, commentType: DEFAULT_OBJECT_TYPE, page: 1, pageSize: 20 });
      setAuditList(resp.list);
    } catch (e) {
      toast((e as Error).message, 'error');
    } finally {
      setAuditListLoading(false);
    }
  };

  const loadHistory = async (id: number, type: number) => {
    try {
      const resp = await auditHistory({ targetId: id, targetType: type });
      setHistory(resp.list);
      setHistoryLoaded(true);
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  const submitCallback = async () => {
    if (!targetId) {
      toast('请输入被审核对象 ID', 'error');
      return;
    }
    try {
      await auditCallback({
        targetId: Number(targetId),
        targetType,
        auditStatus,
        auditReason: auditReason || undefined,
        auditOperator: 10004,
      });
      toast(`审核回调成功：${AUDIT_STATUS_MAP[auditStatus].label}`);
      loadAuditList();
      loadHistory(Number(targetId), targetType);
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  return (
    <div className="panel">
      <div className="panel-title">内容审核（运营视角 · 审核状态机 0待审 → 1通过 / 2拒绝）</div>
      <div className="hint">
        演示先发后审模式：内容默认发布后异步审核。在评论区发布一条新评论后，回到本页点击"刷新待审列表"，
        再选择该评论/回复 ID 模拟审核回调：通过（1）后内容对所有人可见，拒绝（2）后内容被隐藏。
      </div>

      <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 8 }}>① 模拟审核回调</div>
      <div className="audit-row">
        <input className="input" style={{ width: 160 }} placeholder="目标ID（评论/回复ID）" value={targetId} onChange={(e) => setTargetId(e.target.value)} />
        <select className="input" style={{ width: 110 }} value={targetType} onChange={(e) => setTargetType(Number(e.target.value))}>
          <option value={1}>评论</option>
          <option value={2}>回复</option>
        </select>
        <select className="input" style={{ width: 110 }} value={auditStatus} onChange={(e) => setAuditStatus(Number(e.target.value))}>
          <option value={1}>通过</option>
          <option value={2}>拒绝</option>
        </select>
        <input className="input" style={{ width: 140 }} placeholder="审核原因（可选）" value={auditReason} onChange={(e) => setAuditReason(e.target.value)} />
        <button className="btn primary" onClick={submitCallback}>提交回调</button>
      </div>

      <div style={{ fontWeight: 600, fontSize: 13, margin: '14px 0 8px' }}>② 待审核列表</div>
      <div className="audit-row">
        <button className="btn sm" onClick={loadAuditList}>刷新待审列表</button>
        <span className="count">对象 {DEFAULT_OBJECT_ID} · 类型 {DEFAULT_OBJECT_TYPE}（auditStatus ≠ 1 的内容）</span>
      </div>
      {auditListLoading ? (
        <div className="empty" style={{ padding: 16 }}>加载中...</div>
      ) : auditList.length === 0 ? (
        <div className="empty" style={{ padding: 16 }}>没有待审核内容（全部已通过审核）</div>
      ) : (
        auditList.map((c) => (
          <div className="reply-item" key={c.id}>
            <div className="comment-head" style={{ marginBottom: 2 }}>
              <span className="comment-user">#{c.id}</span>
              <span className="comment-meta">{c.userId ? `评论 by 用户${c.userId}` : '回复'}</span>
              <AuditBadge status={c.auditStatus} />
            </div>
            <div className="reply-content">{c.content}</div>
            <div className="reply-actions">
              <button className="btn sm" onClick={() => { setTargetId(String(c.id)); setTargetType(1); }}>
                填入回调
              </button>
              <button className="btn sm" onClick={() => loadHistory(c.id, 1)}>查看审核历史</button>
            </div>
          </div>
        ))
      )}

      <div className="audit-history">
        <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 8 }}>
          ③ 审核历史 {historyLoaded ? `（目标 #${targetId || '-'} · ${targetType === 1 ? '评论' : '回复'}）` : ''}
        </div>
        {history.length === 0 ? (
          <div className="empty" style={{ padding: 12 }}>暂无审核历史，提交回调后自动加载</div>
        ) : (
          history.map((h) => (
            <div className="history-item" key={h.id}>
              <AuditBadge status={h.auditStatus} />
              <span style={{ flex: 1 }}>{h.auditContent || h.auditReason || '-'}</span>
              <span className="comment-meta">
                {h.auditReason ? `原因: ${h.auditReason} · ` : ''}
                {formatTime(h.auditTime)}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

function AuditBadge({ status }: { status: number }) {
  const m = AUDIT_STATUS_MAP[status] ?? { label: `未知(${status})`, color: '#999' };
  return (
    <span className="badge" style={{ background: `${m.color}1a`, color: m.color }}>
      {m.label}
    </span>
  );
}
