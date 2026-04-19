// API 模块入口

export { default as client } from './client';
export { default as request } from './request';
export { API_ENDPOINTS } from './endpoints';
export type { ApiResponse, PageResponse } from './response';
export { isSuccess, getData, getMessage } from './response';

// 导入拦截器（自动注册）
import './interceptors';