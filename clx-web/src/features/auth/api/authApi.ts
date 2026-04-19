import { request } from '@/api';
import { API_ENDPOINTS } from '@/api/endpoints';
import type { LoginRequest, LoginResponse, UserInfo } from '../types/auth.types';
import type { ApiResponse } from '@/api/response';

/**
 * 认证 API
 */
export const authApi = {
  /**
   * 登录
   */
  login: (data: LoginRequest) => {
    return request.post<ApiResponse<LoginResponse>>(API_ENDPOINTS.AUTH.LOGIN, data);
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
};

export default authApi;