// 与后端 vo/req、vo/resp 对齐的 TypeScript 类型

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageData<T> {
  total: number;
  page: number;
  pageSize: number;
  list: T[];
}

// ---------- 评论 ----------

export interface CommentItem {
  id: number;
  commentObjectId: number;
  commentType: number;
  content: string;
  images: string;
  userId: number;
  sort: number;
  replyCount: number;
  likeCount: number;
  auditStatus: number; // 0未审核 1通过 2拒绝
  liked: boolean;
  createTime: string;
  updateTime: string;
  topReplies: ReplyItem[];
}

export interface ReplyItem {
  id: number;
  commentId: number;
  parentId: number;
  replyType: number;
  content: string;
  images: string;
  replyUserId: number;
  beRepliedUserId: number;
  likeCount: number;
  auditStatus: number;
  liked: boolean;
  createTime: string;
  updateTime: string;
}

export interface CommentListResp {
  total: number;
  page: number;
  pageSize: number;
  list: CommentItem[];
}

export interface ReplyListResp {
  total: number;
  page: number;
  pageSize: number;
  list: ReplyItem[];
}

export interface MyCommentItem {
  commentObjectId: number;
  commentType: number;
  interactionType: number; // 1评论 2回复
  latestContent: string;
  latestTime: string;
  interactionCount: number;
}

export interface MyCommentListResp {
  total: number;
  page: number;
  pageSize: number;
  list: MyCommentItem[];
}

export interface AuditHistoryItem {
  id: number;
  auditStatus: number;
  auditContent: string;
  auditReason: string;
  auditOperator: number;
  auditTime: string;
}

export interface AuditHistoryResp {
  targetId: number;
  targetType: number;
  list: AuditHistoryItem[];
}

// ---------- 通知 ----------

export interface NotificationItem {
  id: number;
  type: number; // 1回复 2点赞
  subjectId: number;
  subjectType: number; // 1评论 2回复
  actorId: number;
  commentObjectId: number;
  commentType: number;
  content: string;
  isRead: number; // 0未读 1已读
  createTime: string;
}
