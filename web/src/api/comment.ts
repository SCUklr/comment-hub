import { get, post } from './request';
import type {
  AuditHistoryResp,
  CommentListResp,
  MyCommentListResp,
  ReplyListResp,
} from '../types';

// 常量：1=评论/评论对象，2=回复/回复对象
export const TYPE_COMMENT = 1;
export const TYPE_REPLY = 2;
export const TARGET_TYPE_COMMENT = 1;
export const TARGET_TYPE_REPLY = 2;

// 注意：雪花 ID 为字符串（超出 JS 安全整数范围），所有 id/commentId/parentId/targetId 均按 string 传递

// ---------- 评论 ----------

export const createComment = (body: {
  type: number;
  commentObjectId?: number | null;
  commentType?: number | null;
  commentId?: string | null;
  parentId?: string | null;
  beRepliedUserId?: number | null;
  content: string;
  images?: string;
}) => post('/comment/create', body);

export const deleteComment = (body: { type: number; id: string }) =>
  post('/comment/delete', body);

export const editComment = (body: { type: number; id: string; content: string; images?: string }) =>
  post('/comment/edit', body);

export const pinComment = (body: { commentId: string; isPin: boolean }) =>
  post('/comment/pin', body);

export const like = (body: { targetId: string; targetType: number }) =>
  post<{ targetId: string; targetType: number; liked: boolean }>('/comment/like', body);

export const unlike = (body: { targetId: string; targetType: number }) =>
  post<{ targetId: string; targetType: number; liked: boolean }>('/comment/unlike', body);

export const listComments = (params: {
  commentObjectId: number;
  commentType: number;
  page?: number;
  pageSize?: number;
  topReplyLimit?: number;
}) => get<CommentListResp>('/comment/list', params);

export const listHotComments = (params: {
  commentObjectId: number;
  commentType: number;
  page?: number;
  pageSize?: number;
}) => get<CommentListResp>('/comment/hot', params);

export const listMyComments = (params: {
  commentType?: number;
  interactionType?: number;
  page?: number;
  pageSize?: number;
}) => get<MyCommentListResp>('/comment/my/list', params);

// ---------- 审核 ----------

export const auditCallback = (body: {
  targetId: string;
  targetType: number;
  auditStatus: number;
  auditReason?: string;
  auditOperator?: number;
}) => post('/comment/audit/callback', body);

export const listAuditComments = (params: {
  commentObjectId: number;
  commentType: number;
  page?: number;
  pageSize?: number;
}) => get<CommentListResp>('/comment/audit/list', params);

export const auditHistory = (params: { targetId: string; targetType: number }) =>
  get<AuditHistoryResp>('/comment/audit/history', params);

// ---------- 回复 ----------

export const listReplies = (params: {
  commentId: string;
  parentId?: string;
  page?: number;
  pageSize?: number;
}) => get<ReplyListResp>('/reply/list', params);
