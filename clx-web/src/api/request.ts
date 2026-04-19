import client from './client';
import type { AxiosRequestConfig } from 'axios';

// 通用请求方法封装

export const request = {
  /**
   * GET 请求
   */
  get: <T>(url: string, config?: AxiosRequestConfig) => {
    return client.get<T, T>(url, config);
  },

  /**
   * POST 请求
   */
  post: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) => {
    return client.post<T, T>(url, data, config);
  },

  /**
   * PUT 请求
   */
  put: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) => {
    return client.put<T, T>(url, data, config);
  },

  /**
   * DELETE 请求
   */
  delete: <T>(url: string, config?: AxiosRequestConfig) => {
    return client.delete<T, T>(url, config);
  },

  /**
   * PATCH 请求
   */
  patch: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) => {
    return client.patch<T, T>(url, data, config);
  },
};

export default request;