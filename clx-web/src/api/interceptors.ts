import client from './client';
import type { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { getErrorMessage, isAuthError } from '@/constants/errorCodes';

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

    // 业务错误：根据错误码生成友好提示
    const friendlyMessage = getErrorMessage(data.code, data.msg);
    const error = new Error(friendlyMessage);
    (error as any).code = data.code;
    (error as any).originalMessage = data.msg; // 保留原始消息用于调试
    return Promise.reject(error);
  },
  (error: AxiosError) => {
    // HTTP 错误处理
    const status = error.response?.status;
    const responseData = error.response?.data as any;

    // 尝试从响应中获取业务错误码
    const businessCode = responseData?.code || status;

    // 生成友好提示
    let friendlyMessage: string;

    if (status === 401) {
      // Token 过期/无效，清除所有存储并跳转登录
      clearAuthStorage();
      window.location.href = '/login';
      friendlyMessage = '登录已过期，请重新登录';
    } else if (status === 403) {
      // 权限不足 - 区分 CORS 错误和业务权限错误
      if (responseData?.msg === 'Invalid CORS request' || !responseData?.msg) {
        // CORS 错误：通常是跨域配置问题
        friendlyMessage = '跨域请求被拒绝，请检查访问地址是否正确（建议使用 localhost 而非 127.0.0.1）';
      } else {
        // 业务权限错误
        friendlyMessage = getErrorMessage(403, responseData?.msg);
      }
    } else if (status === 404) {
      friendlyMessage = getErrorMessage(404);
    } else if (status === 500) {
      friendlyMessage = getErrorMessage(500);
    } else if (status === 503) {
      friendlyMessage = getErrorMessage(503);
    } else if (businessCode && businessCode !== status) {
      // 有业务错误码，使用映射
      friendlyMessage = getErrorMessage(businessCode, responseData?.msg);
    } else {
      // 确保有一个有效的状态码
      const effectiveStatus = status ?? 0;
      friendlyMessage = getErrorMessage(effectiveStatus, '网络异常，请稍后重试');
    }

    // 创建增强的错误对象
    const enhancedError = new Error(friendlyMessage);
    (enhancedError as any).code = businessCode;
    (enhancedError as any).status = status;
    (enhancedError as any).originalMessage = responseData?.msg;

    // 如果是认证错误，清除存储
    if (isAuthError(businessCode)) {
      clearAuthStorage();
    }

    return Promise.reject(enhancedError);
  }
);

/**
 * 清除所有认证相关存储
 */
function clearAuthStorage() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem('token');
  localStorage.removeItem('rememberMe');
  localStorage.removeItem('auth-storage');
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem('token');
}

export default client;