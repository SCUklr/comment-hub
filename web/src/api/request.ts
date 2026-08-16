import type { ApiResult } from '../types';

const BASE_URL = '/api';

// 当前演示用户（全局可变，切换用户时更新）
let currentUserId = 10001;

export function setCurrentUserId(id: number) {
  currentUserId = id;
}

export function getCurrentUserId() {
  return currentUserId;
}

export class ApiError extends Error {
  code: number;
  constructor(code: number, message: string) {
    super(message);
    this.code = code;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };
  // 所有请求默认携带 X-User-Id 模拟登录用户
  headers['X-User-Id'] = String(currentUserId);

  const resp = await fetch(BASE_URL + path, { ...options, headers });

  let body: ApiResult<T> | null = null;
  try {
    body = (await resp.json()) as ApiResult<T>;
  } catch {
    throw new ApiError(resp.status, `HTTP ${resp.status} ${resp.statusText}`);
  }

  if (!resp.ok || body.code !== 200) {
    // 兼容两种错误体：Result 包装（code/message）与 Spring 默认错误 JSON（error/message/status）
    const errBody = body as unknown as { code?: number; message?: string; error?: string; status?: number; path?: string };
    const code = errBody.code ?? errBody.status ?? resp.status;
    const message =
      errBody.message ||
      (errBody.error ? `${errBody.error} (${code})` : '') ||
      `请求失败 (${code})`;
    throw new ApiError(code, message);
  }
  return body.data;
}

export const get = <T>(path: string, params?: Record<string, string | number | undefined>) => {
  const qs = params
    ? '?' +
      Object.entries(params)
        .filter(([, v]) => v !== undefined && v !== null && v !== '')
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
        .join('&')
    : '';
  return request<T>(path + qs, { method: 'GET' });
};

export const post = <T>(path: string, body?: unknown) =>
  request<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) });
