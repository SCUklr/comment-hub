import { post } from './request';
import { CommentLikeReqVO, CommentLikeRespVO } from '../types/comment';

export function like(data: CommentLikeReqVO) {
  return post<CommentLikeRespVO>('/api/comment/like', data);
}

export function unlike(data: CommentLikeReqVO) {
  return post<CommentLikeRespVO>('/api/comment/unlike', data);
}
