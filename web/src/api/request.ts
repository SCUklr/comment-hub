import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { message } from 'antd';
import { API_BASE_URL } from '../utils/constants';

export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

const request: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
request.interceptors.request.use(
  (config) => {
    const userId = localStorage.getItem('comment_user_id');
    if (userId) {
      config.headers['X-User-Id'] = userId;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
request.interceptors.response.use(
  (response: AxiosResponse<Result<any>>) => {
    const { data } = response;
    if (data.code !== 0 && data.code !== 200) {
      message.error(data.message || '请求失败');
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return response;
  },
  (error) => {
    message.error(error?.response?.data?.message || error.message || '网络错误');
    return Promise.reject(error);
  }
);

export async function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await request.get<Result<T>>(url, config);
  return response.data.data;
}

export async function post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const response = await request.post<Result<T>>(url, data, config);
  return response.data.data;
}

export async function put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const response = await request.put<Result<T>>(url, data, config);
  return response.data.data;
}

export async function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await request.delete<Result<T>>(url, config);
  return response.data.data;
}

export default request;
