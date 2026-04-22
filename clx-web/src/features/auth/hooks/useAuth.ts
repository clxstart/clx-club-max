import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';
import type { LoginRequest, RegisterRequest } from '../types/auth.types';

/**
 * 登录 Hook
 */
export function useLogin() {
  const { setToken, setUser } = useAuthStore();

  const login = useCallback(async (data: LoginRequest, rememberMe = false) => {
    // 1. 登录请求
    const response = await authApi.login({ ...data, rememberMe });

    // 2. 登录成功后存储 Token
    if (response.code === 200 && response.data) {
      setToken(response.data.token, response.data.tokenName, rememberMe);

      // 3. 获取用户信息（失败不影响登录结果）
      try {
        const userResponse = await authApi.getCurrentUser();
        if (userResponse.code === 200 && userResponse.data) {
          setUser(userResponse.data);
        }
      } catch {
        // 获取用户信息失败不影响登录
        console.warn('获取用户信息失败，但登录成功');
      }
    }

    return response;
  }, [setToken, setUser]);

  return { login };
}

/**
 * 注册 Hook
 */
export function useRegister() {
  const { setToken, setUser } = useAuthStore();
  const navigate = useNavigate();

  const register = useCallback(async (data: RegisterRequest) => {
    // 1. 注册请求
    const response = await authApi.register(data);

    // 2. 注册成功后自动登录
    if (response.code === 200 && response.data) {
      setToken(response.data.token, response.data.tokenName, false);

      // 3. 获取用户信息
      try {
        const userResponse = await authApi.getCurrentUser();
        if (userResponse.code === 200 && userResponse.data) {
          setUser(userResponse.data);
        }
      } catch {
        console.warn('获取用户信息失败，但注册成功');
      }

      // 4. 跳转首页
      navigate('/');
    }

    return response;
  }, [setToken, setUser, navigate]);

  return { register };
}

/**
 * 登出 Hook
 */
export function useLogout() {
  const navigate = useNavigate();
  const { logout: clearAuth } = useAuthStore();

  const logout = useCallback(async () => {
    await authApi.logout();
    clearAuth();
    navigate('/login');
  }, [clearAuth, navigate]);

  return { logout };
}

/**
 * 认证状态 Hook
 */
export function useAuth() {
  const { token, user, isLoggedIn } = useAuthStore();

  return {
    token,
    user,
    isLoggedIn,
  };
}

export default { useLogin, useRegister, useLogout, useAuth };
