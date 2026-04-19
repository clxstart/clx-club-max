/**
 * 后端统一响应格式
 */
export interface ApiResponse<T = unknown> {
  code: number;
  msg: string;
  data: T;
  timestamp?: number;
}

/**
 * 分页响应格式
 */
export interface PageResponse<T = unknown> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

/**
 * 成功响应判断
 */
export function isSuccess<T>(response: ApiResponse<T>): boolean {
  return response.code === 200;
}

/**
 * 获取响应数据
 */
export function getData<T>(response: ApiResponse<T>): T {
  return response.data;
}

/**
 * 获取响应消息
 */
export function getMessage<T>(response: ApiResponse<T>): string {
  return response.msg;
}