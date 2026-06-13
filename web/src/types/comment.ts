export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  total: number;
  page: number;
  pageSize: number;
  list: T[];
}

export interface CommentCreateReqVO {
  type: number;
  commentObjectId?: number;
  commentType?: number;
  commentId?: number;
  parentId?: number;
  beRepliedUserId?: number;
  content: string;
  images?: string;
}

export interface CommentCreateRespVO {
  type: number;
  id: number;
  commentObjectId: number;
  commentType: number;
  commentId?: number;
  parentId?: number;
  replyType?: number;
  replyUserId: number;
  beRepliedUserId?: number;
  content: string;
  images: string;
  createTime: string;
  updateTime: string;
}

export interface CommentDeleteReqVO {
  type: number;
  id: number;
}

export interface CommentEditReqVO {
  type: number;
  id: number;
  content: string;
  images?: string;
}

export interface CommentEditRespVO {
  type: number;
  id: number;
  content: string;
  images: string;
  updateTime: string;
}

export interface CommentPinReqVO {
  commentId: number;
  isPin?: boolean;
}

export interface CommentListReqVO {
  commentObjectId: number;
  commentType: number;
  page?: number;
  pageSize?: number;
  topReplyLimit?: number;
}

export interface CommentItemVO {
  id: number;
  commentObjectId: number;
  commentType: number;
  content: string;
  images: string;
  userId: number;
  sort: number;
  replyCount: number;
  likeCount: number;
  liked: boolean;
  createTime: string;
  updateTime: string;
  topReplies?: ReplyItemVO[];
}

export interface ReplyItemVO {
  id: number;
  commentId: number;
  parentId: number;
  replyType: number;
  content: string;
  images: string;
  replyUserId: number;
  beRepliedUserId: number;
  likeCount: number;
  liked: boolean;
  createTime: string;
  updateTime: string;
}

export interface CommentListRespVO {
  total: number;
  page: number;
  pageSize: number;
  list: CommentItemVO[];
}

export interface CommentHotListReqVO {
  commentObjectId: number;
  commentType: number;
  page?: number;
  pageSize?: number;
}

export interface MyCommentListReqVO {
  commentType?: number;
  interactionType?: number;
  page?: number;
  pageSize?: number;
}

export interface MyCommentItemVO {
  commentObjectId: number;
  commentType: number;
  interactionType: number;
  latestContent: string;
  latestTime: string;
  interactionCount: number;
}

export interface MyCommentListRespVO {
  total: number;
  page: number;
  pageSize: number;
  list: MyCommentItemVO[];
}

export interface CommentLikeReqVO {
  targetId: number;
  targetType: number;
}

export interface CommentLikeRespVO {
  targetId: number;
  targetType: number;
  liked: boolean;
}

export interface ReplyListReqVO {
  commentId: number;
  parentId?: number;
  page?: number;
  pageSize?: number;
}

export interface ReplyListRespVO {
  total: number;
  page: number;
  pageSize: number;
  list: ReplyItemVO[];
}

export interface NotificationItemVO {
  id: number;
  type: number;
  subjectId: number;
  subjectType: number;
  actorId: number;
  commentObjectId: number;
  commentType: number;
  content: string;
  isRead: number;
  createTime: string;
}

export interface NotificationListReqVO {
  page?: number;
  pageSize?: number;
}

export interface NotificationReadReqVO {
  id: number;
}

export interface CommentAuditCallbackReqVO {
  targetId: number;
  targetType: number;
  auditStatus: number;
  auditReason?: string;
  auditOperator?: number;
}

export interface CommentAuditHistoryReqVO {
  targetId: number;
  targetType: number;
}

export interface AuditHistoryItemVO {
  id: number;
  auditStatus: number;
  auditContent: string;
  auditReason: string;
  auditOperator: number;
  auditTime: string;
}

export interface CommentAuditHistoryRespVO {
  targetId: number;
  targetType: number;
  list: AuditHistoryItemVO[];
}
