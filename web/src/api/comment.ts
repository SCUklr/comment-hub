import { get, post } from './request';
import {
  CommentCreateReqVO,
  CommentCreateRespVO,
  CommentDeleteReqVO,
  CommentEditReqVO,
  CommentEditRespVO,
  CommentPinReqVO,
  CommentListReqVO,
  CommentListRespVO,
  CommentHotListReqVO,
  MyCommentListReqVO,
  MyCommentListRespVO,
  ReplyListReqVO,
  ReplyListRespVO,
} from '../types/comment';

export function createComment(data: CommentCreateReqVO) {
  return post<CommentCreateRespVO>('/api/comment/create', data);
}

export function deleteComment(data: CommentDeleteReqVO) {
  return post<void>('/api/comment/delete', data);
}

export function editComment(data: CommentEditReqVO) {
  return post<CommentEditRespVO>('/api/comment/edit', data);
}

export function pinComment(data: CommentPinReqVO) {
  return post<void>('/api/comment/pin', data);
}

export function getCommentList(params: CommentListReqVO) {
  return get<CommentListRespVO>('/api/comment/list', { params });
}

export function getHotCommentList(params: CommentHotListReqVO) {
  return get<CommentListRespVO>('/api/comment/hot', { params });
}

export function getMyCommentList(params: MyCommentListReqVO) {
  return get<MyCommentListRespVO>('/api/comment/my/list', { params });
}

export function getReplyList(params: ReplyListReqVO) {
  return get<ReplyListRespVO>('/api/reply/list', { params });
}
