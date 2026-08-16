// 与后端 vo/req、vo/resp 对齐的 TypeScript 类型
// 注意：所有 ID 字段为 string —— 后端雪花 ID（如 744087244154953728）超出
// JS Number 安全整数范围，已配置 Jackson 将 boxed Long 序列化为字符串，前端按 string 处理。

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
  id: string;
  commentObjectId: string;
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
  id: string;
  commentId: string;
  parentId: string;
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
  commentObjectId: string;
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
  id: string;
  auditStatus: number;
  auditContent: string;
  auditReason: string;
  auditOperator: string;
  auditTime: string;
}

export interface AuditHistoryResp {
  targetId: string;
  targetType: number;
  list: AuditHistoryItem[];
}

// ---------- 通知 ----------

export interface NotificationItem {
  id: string;
  type: number; // 1回复 2点赞
  subjectId: string;
  subjectType: number; // 1评论 2回复
  actorId: number;
  commentObjectId: string;
  commentType: number;
  content: string;
  isRead: number; // 0未读 1已读
  createTime: string;
}
