import React from 'react';
import { Outlet } from 'react-router-dom';

/**
 * 认证布局（登录/注册页面）
 * 无侧边栏和顶部导航
 */
export const AuthLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-[#0a0a0a]">
      <Outlet />
    </div>
  );
};

export default AuthLayout;