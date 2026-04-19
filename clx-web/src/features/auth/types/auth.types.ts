// 认证相关类型定义

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string;
  tokenName: string;
}

/**
 * 用户信息
 */
export interface UserInfo {
  userId: number;
  username: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  phone?: string;
  roles?: string[];
  permissions?: string[];
}

/**
 * Token 信息
 */
export interface TokenInfo {
  tokenName: string;
  tokenValue: string;
  tokenTimeout: number;
}