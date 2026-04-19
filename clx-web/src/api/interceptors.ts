import client from './client';
import type { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios';

// Token 键名
const TOKEN_KEY = 'Authorization';

// 请求拦截器 - 自动添加 Token
client.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 同时读取 localStorage 和 sessionStorage
    const token = localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
    if (token) {
      // 直接使用存储的值（已经是 Bearer xxx 格式）
      config.headers[TOKEN_KEY] = token;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理错误
client.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response;

    // 后端统一响应格式：{ code, msg, data }
    if (data.code === 200) {
      return data;
    }

    // 业务错误
    const error = new Error(data.msg || '请求失败');
    (error as any).code = data.code;
    return Promise.reject(error);
  },
  (error: AxiosError) => {
    // HTTP 错误处理
    const status = error.response?.status;

    if (status === 401) {
      // Token 过期/无效，清除所有存储并跳转登录
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem('token');
      localStorage.removeItem('rememberMe');
      localStorage.removeItem('auth-storage');
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem('token');
      window.location.href = '/login';
      return Promise.reject(new Error('登录已过期，请重新登录'));
    }

    if (status === 403) {
      return Promise.reject(new Error('没有权限访问'));
    }

    if (status === 404) {
      return Promise.reject(new Error('资源不存在'));
    }

    if (status === 500) {
      return Promise.reject(new Error('服务器错误'));
    }

    return Promise.reject(error);
  }
);

export default client;