import { request } from '@/api';
import { API_ENDPOINTS } from '@/api/endpoints';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  UserInfo,
  CaptchaResponse,
  EmailCodeRequest,
  SmsCodeRequest,
  PasswordResetRequest,
  PasswordResetConfirmRequest,
} from '../types/auth.types';
import type { ApiResponse } from '@/api/response';

/**
 * 认证 API
 */
export const authApi = {
  /**
   * 获取图形验证码
   */
  getCaptcha: () => {
    return request.get<ApiResponse<CaptchaResponse>>(API_ENDPOINTS.AUTH.CAPTCHA);
  },

  /**
   * 登录
   */
  login: (data: LoginRequest) => {
    return request.post<ApiResponse<LoginResponse>>(API_ENDPOINTS.AUTH.LOGIN, data);
  },

  /**
   * 注册
   */
  register: (data: RegisterRequest) => {
    return request.post<ApiResponse<RegisterResponse>>(API_ENDPOINTS.AUTH.REGISTER, data);
  },

  /**
   * 登出
   */
  logout: () => {
    return request.post<ApiResponse<void>>(API_ENDPOINTS.AUTH.LOGOUT);
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser: () => {
    return request.get<ApiResponse<UserInfo>>(API_ENDPOINTS.AUTH.ME);
  },

  /**
   * 刷新 Token
   */
  refreshToken: (refreshToken: string) => {
    return request.post<ApiResponse<LoginResponse>>(API_ENDPOINTS.AUTH.REFRESH, {
      refreshToken,
    });
  },

  /**
   * 发送邮箱验证码
   */
  sendEmailCode: (data: EmailCodeRequest) => {
    return request.post<ApiResponse<void>>(API_ENDPOINTS.AUTH.EMAIL_CODE_SEND, data);
  },

  /**
   * 发送手机验证码
   */
  sendSmsCode: (data: SmsCodeRequest) => {
    return request.post<ApiResponse<string>>(API_ENDPOINTS.AUTH.SMS_CODE_SEND, data);
  },

  /**
   * 发送密码重置邮件
   */
  sendPasswordReset: (data: PasswordResetRequest) => {
    return request.post<ApiResponse<void>>(API_ENDPOINTS.AUTH.PASSWORD_RESET_SEND, data);
  },

  /**
   * 确认密码重置
   */
  confirmPasswordReset: (data: PasswordResetConfirmRequest) => {
    return request.post<ApiResponse<void>>(API_ENDPOINTS.AUTH.PASSWORD_RESET_CONFIRM, data);
  },
};

export default authApi;