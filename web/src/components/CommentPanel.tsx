import { useCallback, useEffect, useState } from 'react';
import {
  createComment,
  deleteComment,
  editComment,
  like,
  listComments,
  listHotComments,
  listReplies,
  pinComment,
  unlike,
  TARGET_TYPE_COMMENT,
  TARGET_TYPE_REPLY,
  TYPE_COMMENT,
  TYPE_REPLY,
} from '../api/comment';
import type { CommentItem, ReplyItem } from '../types';
import { AUDIT_STATUS_MAP, DEFAULT_OBJECT_ID, DEFAULT_OBJECT_TYPE, formatTime } from '../utils/constants';
import { toast } from '../utils/toast';

// 评论与回复共用的最小操作目标
type Target = CommentItem | ReplyItem;
const isComment = (t: Target): t is CommentItem => 'userId' in t;

function avatarClass(userId: number): string {
  const cls = `avatar-${userId}`;
  return ['avatar-1', 'avatar-2', 'avatar-3', 'avatar-4'].includes(cls) ? cls : 'avatar-default';
}

function AuditBadge({ status }: { status: number }) {
  const m = AUDIT_STATUS_MAP[status] ?? { label: `未知(${status})`, color: '#999' };
  return (
    <span className="badge" style={{ background: `${m.color}1a`, color: m.color }}>
      {m.label}
    </span>
  );
}

function LikeBtn({ liked, count, onToggle }: { liked: boolean; count: number; onToggle: () => void }) {
  return (
    <button className={`btn sm ${liked ? 'primary' : ''}`} onClick={onToggle}>
      {liked ? '👍 已赞' : '👍 点赞'} {count > 0 ? count : ''}
    </button>
  );
}

interface RowProps {
  reply: ReplyItem;
  currentUserId: number;
  onToggleLike: (r: ReplyItem) => void;
  onDelete: (r: ReplyItem) => void;
  onEdit: (r: ReplyItem, content: string) => void;
  onReply: (r: ReplyItem, content: string) => void;
}

function ReplyItemRow({ reply, currentUserId, onToggleLike, onDelete, onEdit, onReply }: RowProps) {
  const [editing, setEditing] = useState(false);
  const [editContent, setEditContent] = useState(reply.content);
  const [replying, setReplying] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const isMine = reply.replyUserId === currentUserId;

  return (
    <div className="reply-item">
      <div className="comment-head" style={{ marginBottom: 2 }}>
        <span className={`comment-avatar ${avatarClass(reply.replyUserId)}`} style={{ width: 22, height: 22, fontSize: 11 }}>
          {reply.replyUserId}
        </span>
        <span className="comment-user">用户{reply.replyUserId}</span>
        {reply.beRepliedUserId > 0 ? <span className="comment-meta">回复 用户{reply.beRepliedUserId}</span> : null}
        <AuditBadge status={reply.auditStatus} />
      </div>

      {editing ? (
        <div className="reply-form">
          <textarea className="textarea" value={editContent} onChange={(e) => setEditContent(e.target.value)} />
          <div className="reply-form-row">
            <button className="btn primary sm" onClick={() => { onEdit(reply, editContent); setEditing(false); }}>保存</button>
            <button className="btn sm" onClick={() => { setEditing(false); setEditContent(reply.content); }}>取消</button>
          </div>
        </div>
      ) : (
        <div className="reply-content">{reply.content}</div>
      )}

      <div className="reply-actions">
        <LikeBtn liked={!!reply.liked} count={reply.likeCount} onToggle={() => onToggleLike(reply)} />
        <button className="btn sm" onClick={() => { setReplying(!replying); setReplyContent(''); }}>回复</button>
        {isMine && (
          <>
            <button className="btn sm" onClick={() => setEditing(!editing)}>编辑</button>
            <button className="btn sm danger" onClick={() => onDelete(reply)}>删除</button>
          </>
        )}
      </div>

      {replying && (
        <div className="reply-form">
          <textarea
            className="textarea"
            placeholder={`回复 用户${reply.replyUserId}（楼中楼）...`}
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
          />
          <div className="reply-form-row">
            <button
              className="btn primary sm"
              disabled={!replyContent.trim()}
              onClick={() => { onReply(reply, replyContent.trim()); setReplying(false); setReplyContent(''); }}
            >
              发表回复
            </button>
            <button className="btn sm" onClick={() => setReplying(false)}>取消</button>
          </div>
        </div>
      )}
    </div>
  );
}

interface ItemProps {
  comment: CommentItem;
  currentUserId: number;
  onToggleLike: (t: Target) => void;
  onDelete: (t: Target) => void;
  onEdit: (t: Target, content: string) => void;
  onPin: (c: CommentItem, isPin: boolean) => void;
  onReply: (c: CommentItem, commentId: string, parentId: string, beRepliedUserId: number | null, content: string) => void;
  expanded: boolean;
  fullReplies: ReplyItem[] | null;
  onToggleExpand: (c: CommentItem) => void;
  replyLoading: boolean;
}

function CommentItemView({
  comment,
  currentUserId,
  onToggleLike,
  onDelete,
  onEdit,
  onPin,
  onReply,
  expanded,
  fullReplies,
  onToggleExpand,
  replyLoading,
}: ItemProps) {
  const [editing, setEditing] = useState(false);
  const [editContent, setEditContent] = useState(comment.content);
  const [replying, setReplying] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const isMine = comment.userId === currentUserId;
  const showReplies = expanded && fullReplies;

  return (
    <div className="comment-item">
      <div className="comment-head">
        <span className={`comment-avatar ${avatarClass(comment.userId)}`}>{comment.userId}</span>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <span className="comment-user">用户{comment.userId}</span>
            {comment.sort > 0 && (
              <span className="badge" style={{ background: '#fff7e6', color: '#fa8c16' }}>📌 置顶</span>
            )}
            <AuditBadge status={comment.auditStatus} />
          </div>
          <div className="comment-meta">{formatTime(comment.createTime)}</div>
        </div>
      </div>

      {editing ? (
        <div className="reply-form">
          <textarea className="textarea" value={editContent} onChange={(e) => setEditContent(e.target.value)} />
          <div className="reply-form-row">
            <button className="btn primary sm" onClick={() => { onEdit(comment, editContent); setEditing(false); }}>保存</button>
            <button className="btn sm" onClick={() => { setEditing(false); setEditContent(comment.content); }}>取消</button>
          </div>
        </div>
      ) : (
        <div className="comment-content">{comment.content}</div>
      )}

      <div className="comment-actions">
        <LikeBtn liked={!!comment.liked} count={comment.likeCount} onToggle={() => onToggleLike(comment)} />
        <button className="btn sm" onClick={() => { setReplying(!replying); setReplyContent(''); }}>回复</button>
        <button className="btn sm" onClick={() => onToggleExpand(comment)}>
          {expanded ? '收起回复' : `展开回复 (${comment.replyCount})`}
        </button>
        {isMine && (
          <>
            <button className="btn sm" onClick={() => setEditing(!editing)}>编辑</button>
            <button className="btn sm danger" onClick={() => onDelete(comment)}>删除</button>
          </>
        )}
        <button className="btn sm" onClick={() => onPin(comment, comment.sort <= 0)}>
          {comment.sort > 0 ? '取消置顶' : '置顶'}
        </button>
      </div>

      {replying && (
        <div className="reply-form">
          <textarea
            className="textarea"
            placeholder="回复楼主..."
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
          />
          <div className="reply-form-row">
            <button
              className="btn primary sm"
              disabled={!replyContent.trim()}
              onClick={() => { onReply(comment, comment.id, '0', null, replyContent.trim()); setReplying(false); setReplyContent(''); }}
            >
              发表回复
            </button>
            <button className="btn sm" onClick={() => setReplying(false)}>取消</button>
          </div>
        </div>
      )}

      {showReplies && (
        <div className="reply-area">
          {replyLoading ? (
            <div className="empty" style={{ padding: 12 }}>加载中...</div>
          ) : fullReplies.length === 0 ? (
            <div className="empty" style={{ padding: 12 }}>暂无回复</div>
          ) : (
            fullReplies.map((r) => (
              <ReplyItemRow
                key={r.id}
                reply={r}
                currentUserId={currentUserId}
                onToggleLike={(r) => onToggleLike(r)}
                onDelete={(r) => onDelete(r)}
                onEdit={(r, content) => onEdit(r, content)}
                onReply={(r, content) => onReply(comment, r.commentId, r.id, r.replyUserId, content)}
              />
            ))
          )}
          {expanded && !replyLoading && fullReplies.length < comment.replyCount && (
            <div className="count" style={{ padding: '6px 0' }}>
              共 {comment.replyCount} 条回复，已展示 {fullReplies.length} 条
            </div>
          )}
        </div>
      )}
    </div>
  );
}

// ---------------- 主面板 ----------------

export default function CommentPanel({ currentUserId }: { currentUserId: number }) {
  const [subTab, setSubTab] = useState<'latest' | 'hot'>('latest');
  const [comments, setComments] = useState<CommentItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [newContent, setNewContent] = useState('');

  // 每个评论展开后的完整回复列表
  const [expandedMap, setExpandedMap] = useState<Record<string, boolean>>({});
  const [repliesMap, setRepliesMap] = useState<Record<string, ReplyItem[]>>({});
  const [replyLoadingMap, setReplyLoadingMap] = useState<Record<string, boolean>>({});

  const pageSize = 10;

  const load = useCallback(
    async (tab: 'latest' | 'hot', p: number) => {
      setLoading(true);
      try {
        const fn = tab === 'hot' ? listHotComments : listComments;
        const resp = await fn({ commentObjectId: DEFAULT_OBJECT_ID, commentType: DEFAULT_OBJECT_TYPE, page: p, pageSize });
        setComments(resp.list);
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
    load(subTab, 1);
  }, [subTab, load]);

  useEffect(() => {
    // 切换用户后刷新（点赞状态、我的内容随用户变化）
    load(subTab, page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUserId]);

  const toggleExpand = async (c: CommentItem) => {
    const next = !expandedMap[c.id];
    setExpandedMap((m) => ({ ...m, [c.id]: next }));
    if (next && !repliesMap[c.id]) {
      setReplyLoadingMap((m) => ({ ...m, [c.id]: true }));
      try {
        const resp = await listReplies({ commentId: c.id, parentId: '0', page: 1, pageSize: 50 });
        setRepliesMap((m) => ({ ...m, [c.id]: resp.list }));
      } catch (e) {
        toast((e as Error).message, 'error');
      } finally {
        setReplyLoadingMap((m) => ({ ...m, [c.id]: false }));
      }
    }
  };

  const refresh = () => load(subTab, page);

  const handleCreate = async () => {
    if (!newContent.trim()) return;
    try {
      await createComment({
        type: TYPE_COMMENT,
        commentObjectId: DEFAULT_OBJECT_ID,
        commentType: DEFAULT_OBJECT_TYPE,
        content: newContent.trim(),
        images: '[]',
      });
      toast('评论发表成功');
      setNewContent('');
      refresh();
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  const handleToggleLike = async (t: Target) => {
    try {
      const targetType = isComment(t) ? TARGET_TYPE_COMMENT : TARGET_TYPE_REPLY;
      const fn = t.liked ? unlike : like;
      await fn({ targetId: t.id, targetType });
      if (!isComment(t)) {
        // 回复点赞后刷新所属评论的回复列表
        const resp = await listReplies({ commentId: t.commentId, parentId: '0', page: 1, pageSize: 50 });
        setRepliesMap((m) => ({ ...m, [t.commentId]: resp.list }));
      }
      refresh();
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  const handleDelete = async (t: Target) => {
    if (!window.confirm(`确认删除这条${isComment(t) ? '评论' : '回复'}？`)) return;
    try {
      await deleteComment({ type: isComment(t) ? TYPE_COMMENT : TYPE_REPLY, id: t.id });
      toast('删除成功');
      refresh();
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  const handleEdit = async (t: Target, content: string) => {
    if (!content.trim()) return;
    try {
      await editComment({ type: isComment(t) ? TYPE_COMMENT : TYPE_REPLY, id: t.id, content: content.trim(), images: '[]' });
      toast('编辑成功');
      refresh();
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  const handlePin = async (c: CommentItem, isPin: boolean) => {
    try {
      await pinComment({ commentId: c.id, isPin });
      toast(isPin ? '已置顶' : '已取消置顶');
      refresh();
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  const handleReply = async (
    c: CommentItem,
    commentId: string,
    parentId: string,
    beRepliedUserId: number | null,
    content: string
  ) => {
    if (!content.trim()) return;
    try {
      await createComment({
        type: TYPE_REPLY,
        commentId,
        parentId,
        beRepliedUserId,
        content: content.trim(),
        images: '[]',
      });
      toast('回复成功');
      refresh();
      // 刷新该评论的回复列表
      const resp = await listReplies({ commentId: c.id, parentId: '0', page: 1, pageSize: 50 });
      setRepliesMap((m) => ({ ...m, [c.id]: resp.list }));
      setExpandedMap((m) => ({ ...m, [c.id]: true }));
    } catch (e) {
      toast((e as Error).message, 'error');
    }
  };

  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  return (
    <div className="panel">
      <div className="panel-title">
        <span>评论列表（对象 {DEFAULT_OBJECT_ID} · 类型 {DEFAULT_OBJECT_TYPE}）</span>
        <span className="count">共 {total} 条</span>
      </div>

      <div className="reply-form" style={{ marginBottom: 14 }}>
        <textarea
          className="textarea"
          placeholder="说点什么吧...（演示：切换顶部用户后，以该用户身份发表）"
          value={newContent}
          onChange={(e) => setNewContent(e.target.value)}
        />
        <div className="reply-form-row">
          <button className="btn primary" disabled={!newContent.trim()} onClick={handleCreate}>发表评论</button>
          <button className="btn" onClick={refresh}>刷新</button>
        </div>
      </div>

      <div className="tabs" style={{ marginBottom: 10 }}>
        <button className={`tab ${subTab === 'latest' ? 'active' : ''}`} onClick={() => setSubTab('latest')}>最新评论</button>
        <button className={`tab ${subTab === 'hot' ? 'active' : ''}`} onClick={() => setSubTab('hot')}>🔥 热评</button>
      </div>

      {loading ? (
        <div className="empty">加载中...</div>
      ) : comments.length === 0 ? (
        <div className="empty">还没有评论，来发第一条吧～</div>
      ) : (
        <div className="comment-list">
          {comments.map((c) => (
            <CommentItemView
              key={c.id}
              comment={c}
              currentUserId={currentUserId}
              onToggleLike={handleToggleLike}
              onDelete={handleDelete}
              onEdit={handleEdit}
              onPin={handlePin}
              onReply={handleReply}
              expanded={!!expandedMap[c.id]}
              fullReplies={repliesMap[c.id] ?? null}
              onToggleExpand={toggleExpand}
              replyLoading={!!replyLoadingMap[c.id]}
            />
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pager">
          <button className="btn sm" disabled={page <= 1} onClick={() => load(subTab, page - 1)}>上一页</button>
          <span>{page} / {totalPages}</span>
          <button className="btn sm" disabled={page >= totalPages} onClick={() => load(subTab, page + 1)}>下一页</button>
        </div>
      )}
    </div>
  );
}
