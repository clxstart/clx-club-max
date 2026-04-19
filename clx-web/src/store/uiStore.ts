import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface UIState {
  // 主题
  theme: 'dark' | 'light';
  // 侧边栏折叠
  sidebarCollapsed: boolean;
  // 加载状态
  loading: boolean;

  // 操作
  setTheme: (theme: 'dark' | 'light') => void;
  toggleSidebar: () => void;
  setLoading: (loading: boolean) => void;
}

/**
 * UI 状态管理（主题、侧边栏、加载）
 */
export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      theme: 'dark',
      sidebarCollapsed: false,
      loading: false,

      setTheme: (theme) => set({ theme }),
      toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
      setLoading: (loading) => set({ loading }),
    }),
    {
      name: 'ui-storage',
    }
  )
);

export default useUIStore;