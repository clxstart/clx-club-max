/**
 * 图形验证码响应
 */
export interface CaptchaResponse {
  captchaId: string;
  captchaImage: string;
}

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string;
  password: string;
  captchaId: string;
  captchaCode: string;
  rememberMe?: boolean;
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string;
  tokenName: string;
  tokenTimeout?: number;
  activeTimeout?: number;
  rememberMe?: boolean;
}

/**
 * 注册请求
 */
export interface RegisterRequest {
  username: string;
  password: string;
  confirmPassword: string;
  nickname?: string;
  email: string;
  emailCode: string;
}

/**
 * 注册响应
 */
export interface RegisterResponse {
  userId: number;
  username: string;
  token: string;
  tokenName: string;
  tokenTimeout: number;
  activeTimeout: number;
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

/**
 * 邮箱验证码请求
 */
export interface EmailCodeRequest {
  email: string;
}

/**
 * 手机验证码请求
 */
export interface SmsCodeRequest {
  phone: string;
  captchaId: string;
  captchaCode: string;
}

/**
 * 密码重置请求
 */
export interface PasswordResetRequest {
  email: string;
  captchaId: string;
  captchaCode: string;
}

/**
 * 密码重置确认请求
 */
export interface PasswordResetConfirmRequest {
  email: string;
  resetCode: string;
  newPassword: string;
  confirmPassword: string;
}
