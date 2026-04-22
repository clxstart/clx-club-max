import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';

/**
 * OAuth 登录回调页面
 *
 * 处理流程：
 * 1. 从 URL 获取 token 和 rememberMe 参数（后端重定向传来的）
 * 2. 存储 Token
 * 3. 获取用户信息
 * 4. 跳转首页
 */
export const OAuthCallbackPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const { setToken, setUser } = useAuthStore();

  useEffect(() => {
    const handleCallback = async () => {
      // 从 URL 获取参数
      const token = searchParams.get('token');
      const rememberMeParam = searchParams.get('rememberMe');
      const errorParam = searchParams.get('error');

      // 如果有错误参数，显示错误
      if (errorParam) {
        setError(errorParam);
        return;
      }

      if (!token) {
        setError('登录失败，未获取到 Token');
        return;
      }

      try {
        // 存储 Token
        const rememberMe = rememberMeParam === 'true';
        setToken(token, 'Authorization', rememberMe);

        // 获取用户信息
        const userResponse = await authApi.getCurrentUser();
        if (userResponse.code === 200 && userResponse.data) {
          setUser(userResponse.data);
        }

        // 跳转首页
        navigate('/');
      } catch (err) {
        const message = err instanceof Error ? err.message : '获取用户信息失败';
        setError(message);
      }
    };

    handleCallback();
  }, [searchParams, navigate, setToken, setUser]);

  if (error) {
    return (
      <div className="glass-card rounded-xl p-8 text-center">
        <div className="text-red-400 mb-4">{error}</div>
        <button
          type="button"
          onClick={() => navigate('/login')}
          className="acid-button px-6 py-2 rounded-lg text-[#0a0a0a] font-bold"
        >
          返回登录
        </button>
      </div>
    );
  }

  return (
    <div className="glass-card rounded-xl p-8 text-center">
      <div className="loader mx-auto mb-4" />
      <div className="text-[#39ff14]">正在登录中...</div>
    </div>
  );
};

export default OAuthCallbackPage;