import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { UserInfo } from '../types/auth.types';

interface AuthState {
  // 状态
  token: string | null;
  tokenName: string;
  user: UserInfo | null;
  isLoggedIn: boolean;
  rememberMe: boolean;

  // 操作
  setToken: (token: string, tokenName: string, rememberMe?: boolean) => void;
  setUser: (user: UserInfo) => void;
  logout: () => void;
}

// 获取存储的 Token（优先 localStorage，再 sessionStorage）
const getStoredToken = (): string | null => {
  return localStorage.getItem('token') || sessionStorage.getItem('token');
};

// 获取 Authorization Token（用于请求拦截器）
export const getAuthorizationToken = (): string | null => {
  return localStorage.getItem('Authorization') || sessionStorage.getItem('Authorization');
};

// 获取是否记住我
const getStoredRememberMe = (): boolean => {
  return localStorage.getItem('rememberMe') === 'true';
};

/**
 * 认证状态管理
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      // 初始状态
      token: getStoredToken(),
      tokenName: 'Authorization',
      user: null,
      isLoggedIn: !!getStoredToken(),
      rememberMe: getStoredRememberMe(),

      // 设置 Token
      setToken: (token, tokenName, rememberMe = false) => {
        const storage = rememberMe ? localStorage : sessionStorage;

        // 清除另一种存储方式中的旧数据
        const otherStorage = rememberMe ? sessionStorage : localStorage;
        otherStorage.removeItem('token');
        otherStorage.removeItem('Authorization');

        // 存储到选定的位置
        storage.setItem('token', token);
        storage.setItem('Authorization', `Bearer ${token}`);

        // 记住我标记只在 localStorage
        if (rememberMe) {
          localStorage.setItem('rememberMe', 'true');
        } else {
          localStorage.removeItem('rememberMe');
        }

        set({ token, tokenName, isLoggedIn: true, rememberMe });
      },

      // 设置用户信息
      setUser: (user) => {
        set({ user });
      },

      // 登出
      logout: () => {
        // 清除所有存储（两种存储方式都清除）
        localStorage.removeItem('token');
        localStorage.removeItem('Authorization');
        localStorage.removeItem('rememberMe');
        localStorage.removeItem('auth-storage');
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('Authorization');

        set({ token: null, user: null, isLoggedIn: false, rememberMe: false });
      },
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        // 只有 rememberMe 为 true 时才持久化到 localStorage
        ...(state.rememberMe && {
          token: state.token,
          tokenName: state.tokenName,
          isLoggedIn: state.isLoggedIn,
          rememberMe: state.rememberMe,
        }),
      }),
    }
  )
);

export default useAuthStore;