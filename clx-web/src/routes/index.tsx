import { createBrowserRouter } from 'react-router-dom';
import { AuthLayout, MainLayout } from '@/layouts';
import { AuthGuard } from './guards';
import { LoginPage, RegisterPage, ForgotPasswordPage } from '@/features/auth';

// 路由配置
export const router = createBrowserRouter([
  {
    path: '/login',
    element: <AuthLayout />,
    children: [
      { index: true, element: <LoginPage /> },
    ],
  },
  {
    path: '/register',
    element: <AuthLayout />,
    children: [
      { index: true, element: <RegisterPage /> },
    ],
  },
  {
    path: '/forgot-password',
    element: <AuthLayout />,
    children: [
      { index: true, element: <ForgotPasswordPage /> },
    ],
  },
  {
    path: '/',
    element: <AuthGuard />,
    children: [
      {
        element: <MainLayout />,
        children: [
          { index: true, element: <div className="text-center text-xl">首页（待开发）</div> },
          { path: 'posts', element: <div className="text-center text-xl">帖子列表（待开发）</div> },
          { path: 'messages', element: <div className="text-center text-xl">私信（待开发）</div> },
          { path: 'notifications', element: <div className="text-center text-xl">通知（待开发）</div> },
        ],
      },
    ],
  },
]);

export default router;