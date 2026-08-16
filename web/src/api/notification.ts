import { get, post } from './request';
import type { NotificationItem, PageData } from '../types';

export const listNotifications = (params: { page?: number; pageSize?: number }) =>
  get<PageData<NotificationItem>>('/notification/list', params);

export const markRead = (body: { id: number }) => post<boolean>('/notification/read', body);

export const markAllRead = () => post<boolean>('/notification/read/all', {});
