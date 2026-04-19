import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useLogin } from '../hooks/useAuth';

type StatusType = 'success' | 'error' | 'loading' | null;

export const LoginForm: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<{ type: StatusType; message: string }>({
    type: null,
    message: '',
  });
  const [shake, setShake] = useState(false);

  const navigate = useNavigate();
  const { login } = useLogin();
  const shakeTimerRef = useRef<number | null>(null);
  const redirectTimerRef = useRef<number | null>(null);

  const clearTimers = useCallback(() => {
    if (shakeTimerRef.current !== null) {
      window.clearTimeout(shakeTimerRef.current);
      shakeTimerRef.current = null;
    }
    if (redirectTimerRef.current !== null) {
      window.clearTimeout(redirectTimerRef.current);
      redirectTimerRef.current = null;
    }
  }, []);

  useEffect(() => clearTimers, [clearTimers]);

  const triggerShake = useCallback(() => {
    if (shakeTimerRef.current !== null) {
      window.clearTimeout(shakeTimerRef.current);
    }

    setShake(true);
    shakeTimerRef.current = window.setTimeout(() => {
      setShake(false);
      shakeTimerRef.current = null;
    }, 500);
  }, []);

  const clearStatus = useCallback(() => {
    setStatus({ type: null, message: '' });
  }, []);

  const handleSubmit = useCallback(async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!username.trim() || !password.trim()) {
      setStatus({ type: 'error', message: '请输入用户名和密码' });
      triggerShake();
      return;
    }

    setLoading(true);
    setStatus({ type: 'loading', message: '' });

    try {
      const response = await login({ username: username.trim(), password }, rememberMe);

      if (response.code === 200) {
        setStatus({
          type: 'success',
          message: response.data?.rememberMe ? '登录成功，已开启记住我' : '登录成功，仅当前会话有效',
        });
        redirectTimerRef.current = window.setTimeout(() => {
          navigate('/');
        }, 300);
        return;
      }

      setStatus({ type: 'error', message: response.msg || '登录失败' });
      triggerShake();
    } catch (error) {
      const message = error instanceof Error ? error.message : '网络错误';
      setStatus({ type: 'error', message });
      triggerShake();
    } finally {
      setLoading(false);
    }
  }, [login, navigate, password, rememberMe, triggerShake, username]);

  return (
    <div className={`glass-card rounded-xl p-6 md:p-8 card-hover fade-in ${shake ? 'shake' : ''}`} translate="no">
      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="acid-label block mb-2">用户名</label>
          <div className="relative group">
            <input
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              onFocus={clearStatus}
              className="w-full acid-input rounded-lg pl-12 pr-4 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
              placeholder="请输入用户名"
              autoComplete="username"
            />
            <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </span>
            {/* 悬浮提示 */}
            <div className="absolute left-0 -top-8 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none">
              <div className="bg-[#39ff14]/10 border border-[#39ff14]/30 rounded px-2 py-1 text-xs text-[#39ff14]/70 whitespace-nowrap">
                输入您的账号用户名
              </div>
            </div>
          </div>
        </div>

        <div>
          <label className="acid-label block mb-2">密码</label>
          <div className="relative group">
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              onFocus={clearStatus}
              className="w-full acid-input rounded-lg pl-12 pr-4 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
              placeholder="请输入密码"
              autoComplete="current-password"
            />
            <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </span>
            {/* 悬浮提示 */}
            <div className="absolute left-0 -top-8 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none">
              <div className="bg-[#39ff14]/10 border border-[#39ff14]/30 rounded px-2 py-1 text-xs text-[#39ff14]/70 whitespace-nowrap">
                输入您的登录密码
              </div>
            </div>
          </div>
        </div>

        <div className="flex items-center justify-between text-sm">
          <label className="flex items-center gap-2 cursor-pointer group relative">
            <input
              type="checkbox"
              checked={rememberMe}
              onChange={(event) => setRememberMe(event.target.checked)}
              className="w-4 h-4 rounded border-[#39ff14]/30 bg-transparent accent-[#39ff14]"
            />
            <span className="text-[#39ff14]/50">记住我</span>
            {/* 悬浮提示 - 在记住我旁边 */}
            <div className="absolute left-0 -top-8 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none">
              <div className="bg-[#39ff14]/10 border border-[#39ff14]/30 rounded px-2 py-1 text-xs text-[#39ff14]/70 whitespace-nowrap">
                {rememberMe ? '30天内免登录' : '勾选后Token将持久存储'}
              </div>
            </div>
          </label>
          <span className="text-[#39ff14]/40">{rememberMe ? '30 天免登录' : '关闭浏览器后失效'}</span>
        </div>

        <div className="relative group">
          <button
            type="submit"
            disabled={loading}
            className="w-full acid-button rounded-lg py-3 text-[#0a0a0a] font-bold uppercase tracking-widest disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span className="flex items-center justify-center gap-2">
              {loading ? (
                <>
                  <div className="loader" />
                  登录中...
                </>
              ) : (
                '登录'
              )}
            </span>
          </button>
          {/* 悬浮提示 - 登录按钮 */}
          <div className="absolute left-1/2 -translate-x-1/2 -top-10 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none">
            <div className="bg-[#39ff14]/10 border border-[#39ff14]/30 rounded px-3 py-1 text-xs text-[#39ff14]/70 whitespace-nowrap">
              点击登录进入系统
            </div>
          </div>
        </div>

        <div className="divider-gradient" />

        <div className="text-center">
          <p className="text-xs text-[#39ff14]/40 mb-3">其他登录方式</p>
          <div className="flex justify-center gap-4">
            <div className="relative group">
              <button
                type="button"
                className="w-10 h-10 rounded-lg border border-[#39ff14]/20 flex items-center justify-center text-[#39ff14]/50 hover:border-[#39ff14] hover:text-[#39ff14] transition-all"
                aria-label="网页登录"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.65c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" />
                </svg>
              </button>
              {/* 悬浮提示 */}
              <div className="absolute left-1/2 -translate-x-1/2 -top-9 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none">
                <div className="bg-[#39ff14]/10 border border-[#39ff14]/30 rounded px-2 py-1 text-xs text-[#39ff14]/70 whitespace-nowrap">
                  网页登录
                </div>
              </div>
            </div>
            <div className="relative group">
              <button
                type="button"
                className="w-10 h-10 rounded-lg border border-[#39ff14]/20 flex items-center justify-center text-[#39ff14]/50 hover:border-[#39ff14] hover:text-[#39ff14] transition-all"
                aria-label="代码仓登录"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12c0 4.42 2.87 8.17 6.84 9.49.5.09.68-.22.68-.48l-.01-1.7c-2.78.6-3.37-1.34-3.37-1.34-.45-1.15-1.11-1.46-1.11-1.46-.91-.62.07-.61.07-.61 1 .07 1.53 1.03 1.53 1.03.89 1.52 2.34 1.08 2.91.83.09-.65.35-1.09.63-1.34-2.22-.25-4.55-1.11-4.55-4.94 0-1.09.39-1.98 1.03-2.68-.1-.25-.45-1.27.1-2.64 0 0 .84-.27 2.75 1.02.8-.22 1.65-.33 2.5-.33.85 0 1.7.11 2.5.33 1.91-1.29 2.75-1.02 2.75-1.02.55 1.37.2 2.39.1 2.64.64.7 1.03 1.59 1.03 2.68 0 3.84-2.34 4.68-4.57 4.93.36.31.68.92.68 1.85l-.01 2.75c0 .26.18.58.69.48C19.14 20.16 22 16.42 22 12c0-5.52-4.48-10-10-10z" />
                </svg>
              </button>
              {/* 悬浮提示 */}
              <div className="absolute left-1/2 -translate-x-1/2 -top-9 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none">
                <div className="bg-[#39ff14]/10 border border-[#39ff14]/30 rounded px-2 py-1 text-xs text-[#39ff14]/70 whitespace-nowrap">
                  GitHub 登录
                </div>
              </div>
            </div>
            <div className="relative group">
              <button
                type="button"
                className="w-10 h-10 rounded-lg border border-[#39ff14]/20 flex items-center justify-center text-[#39ff14]/50 hover:border-[#39ff14] hover:text-[#39ff14] transition-all"
                aria-label="谷歌登录"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M20.283 10.356h-8.327v3.451h4.792c-.446 2.193-2.313 3.451-4.792 3.451-3.084 0-5.583-2.498-5.583-5.583s2.499-5.583 5.583-5.583c1.343 0 2.551.488 3.506 1.288l2.67-2.67C16.401 3.088 14.316 2 11.956 2c-4.914 0-8.896 3.981-8.896 8.896s3.982 8.896 8.896 8.896c4.692 0 8.916-3.5 8.916-8.896 0-.528-.06-1.04-.17-1.54z" />
                </svg>
              </button>
              {/* 悬浮提示 */}
              <div className="absolute left-1/2 -translate-x-1/2 -top-9 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none">
                <div className="bg-[#39ff14]/10 border border-[#39ff14]/30 rounded px-2 py-1 text-xs text-[#39ff14]/70 whitespace-nowrap">
                  Google 登录
                </div>
              </div>
            </div>
          </div>
        </div>
      </form>

      {status.type && status.type !== 'loading' && (
        <div className={`mt-4 p-3 rounded-lg text-sm text-center ${status.type === 'error' ? 'status-error' : 'status-success'}`}>
          {status.message}
        </div>
      )}
    </div>
  );
};

export default LoginForm;
