import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useLogin } from '../hooks/useAuth';

type StatusType = 'success' | 'error' | 'loading' | null;

/**
 * GitHub 登录按钮点击处理
 * 获取授权 URL 并跳转到 GitHub 授权页面
 */
const handleGithubLogin = async () => {
  try {
    const response = await authApi.getGithubAuthorizeUrl();
    if (response.code === 200 && response.data) {
      // 跳转到 GitHub 授权页面
      window.location.href = response.data;
    }
  } catch (error) {
    console.error('获取 GitHub 授权 URL 失败:', error);
  }
};

export const LoginForm: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [captchaCode, setCaptchaCode] = useState('');
  const [captchaId, setCaptchaId] = useState('');
  const [captchaImage, setCaptchaImage] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<{ type: StatusType; message: string }>({
    type: null,
    message: '',
  });
  const [shake, setShake] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

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

  const fetchCaptcha = useCallback(async (silent = false) => {
    try {
      const response = await authApi.getCaptcha();
      if (response.code === 200 && response.data) {
        setCaptchaId(response.data.captchaId);
        setCaptchaImage(response.data.captchaImage);
        setCaptchaCode('');
      } else if (!silent) {
        setStatus({ type: 'error', message: response.msg || '获取图形验证码失败，请刷新重试' });
      }
    } catch {
      if (!silent) {
        setStatus({ type: 'error', message: '获取图形验证码失败，请刷新重试' });
      }
    }
  }, []);

  useEffect(() => {
    void fetchCaptcha();
  }, [fetchCaptcha]);

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

    const trimmedUsername = username.trim();
    const trimmedCaptchaCode = captchaCode.trim();

    if (!trimmedUsername || !password.trim()) {
      setStatus({ type: 'error', message: '请输入用户名和密码' });
      triggerShake();
      return;
    }

    if (!captchaId || !trimmedCaptchaCode) {
      setStatus({ type: 'error', message: '请输入图形验证码' });
      triggerShake();
      return;
    }

    setLoading(true);
    setStatus({ type: 'loading', message: '' });

    try {
      const response = await login(
        {
          username: trimmedUsername,
          password,
          captchaId,
          captchaCode: trimmedCaptchaCode,
        },
        rememberMe,
      );

      if (response.code === 200) {
        setStatus({
          type: 'success',
          message: response.data?.rememberMe ? '登录成功，已开启记住我' : '登录成功',
        });
        redirectTimerRef.current = window.setTimeout(() => {
          navigate('/');
        }, 300);
        return;
      }

      setStatus({ type: 'error', message: response.msg || '登录失败' });
      triggerShake();
      await fetchCaptcha(true);
    } catch (error) {
      const message = error instanceof Error ? error.message : '网络错误';
      setStatus({ type: 'error', message });
      triggerShake();
      await fetchCaptcha(true);
    } finally {
      setLoading(false);
    }
  }, [captchaCode, captchaId, fetchCaptcha, login, navigate, password, rememberMe, triggerShake, username]);

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
          </div>
        </div>

        <div>
          <label className="acid-label block mb-2">密码</label>
          <div className="relative group">
            <input
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              onFocus={clearStatus}
              className="w-full acid-input rounded-lg pl-12 pr-12 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
              placeholder="请输入密码"
              autoComplete="current-password"
            />
            <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </span>
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-4 top-1/2 -translate-y-1/2 icon-container hover:text-[#39ff14] transition-colors"
            >
              {showPassword ? (
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                  <line x1="1" y1="1" x2="23" y2="23" />
                </svg>
              ) : (
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              )}
            </button>
          </div>
        </div>

        <div>
          <label className="acid-label block mb-2">图形验证码</label>
          <div className="flex gap-3">
            <div className="relative group flex-1">
              <input
                type="text"
                value={captchaCode}
                onChange={(event) => setCaptchaCode(event.target.value.toUpperCase())}
                onFocus={clearStatus}
                className="w-full acid-input rounded-lg pl-12 pr-4 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30 uppercase"
                placeholder="输入验证码"
                maxLength={4}
              />
              <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                </svg>
              </span>
            </div>
            <button
              type="button"
              onClick={() => void fetchCaptcha()}
              className="h-[50px] min-w-[110px] rounded-lg border border-[#39ff14]/20 bg-[#39ff14]/5 flex items-center justify-center overflow-hidden hover:border-[#39ff14]/40 hover:bg-[#39ff14]/10 transition-all"
            >
              {captchaImage ? (
                <img src={captchaImage} alt="验证码" className="h-9 rounded" />
              ) : (
                <span className="text-xs text-[#39ff14]/50">点击获取</span>
              )}
            </button>
          </div>
        </div>

        <div className="flex items-center justify-between text-sm">
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={rememberMe}
              onChange={(event) => setRememberMe(event.target.checked)}
              className="w-4 h-4 rounded border-[#39ff14]/30 bg-transparent accent-[#39ff14]"
            />
            <span className="text-[#39ff14]/50">记住我</span>
          </label>
          <Link to="/forgot-password" className="text-[#39ff14]/50 hover:text-[#39ff14] transition">
            忘记密码？
          </Link>
        </div>

        <div>
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
        </div>

        <div className="pt-1">
          <div className="auth-section-divider">其他登录方式</div>
          <div className="auth-social-list">
            <Link to="/phone-login" className="auth-social-button" aria-label="手机号登录">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="5" y="2" width="14" height="20" rx="2" ry="2" />
                <line x1="12" y1="18" x2="12.01" y2="18" />
              </svg>
            </Link>
            <button
              type="button"
              className="auth-social-button"
              aria-label="GitHub 登录"
              onClick={handleGithubLogin}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12c0 4.42 2.87 8.17 6.84 9.49.5.09.68-.22.68-.48l-.01-1.7c-2.78.6-3.37-1.34-3.37-1.34-.45-1.15-1.11-1.46-1.11-1.46-.91-.62.07-.61.07-.61 1 .07 1.53 1.03 1.53 1.03.89 1.52 2.34 1.08 2.91.83.09-.65.35-1.09.63-1.34-2.22-.25-4.55-1.11-4.55-4.94 0-1.09.39-1.98 1.03-2.68-.1-.25-.45-1.27.1-2.64 0 0 .84-.27 2.75 1.02.8-.22 1.65-.33 2.5-.33.85 0 1.7.11 2.5.33 1.91-1.29 2.75-1.02 2.75-1.02.55 1.37.2 2.39.1 2.64.64.7 1.03 1.59 1.03 2.68 0 3.84-2.34 4.68-4.57 4.93.36.31.68.92.68 1.85l-.01 2.75c0 .26.18.58.69.48C19.14 20.16 22 16.42 22 12c0-5.52-4.48-10-10-10z" />
              </svg>
            </button>
            <button type="button" className="auth-social-button" aria-label="Google 登录">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M20.283 10.356h-8.327v3.451h4.792c-.446 2.193-2.313 3.451-4.792 3.451-3.084 0-5.583-2.498-5.583-5.583s2.499-5.583 5.583-5.583c1.343 0 2.551.488 3.506 1.288l2.67-2.67C16.401 3.088 14.316 2 11.956 2c-4.914 0-8.896 3.981-8.896 8.896s3.982 8.896 8.896 8.896c4.692 0 8.916-3.5 8.916-8.896 0-.528-.06-1.04-.17-1.54z" />
              </svg>
            </button>
          </div>
        </div>
      </form>

      <div className="auth-footer text-sm">
        <span className="text-[#39ff14]/55">还没有账号？</span>
        <Link to="/register" className="text-[#39ff14] transition hover:text-[#e6ff00] hover:underline">
          去注册
        </Link>
      </div>

      {status.type && status.type !== 'loading' && (
        <div className={`mt-4 p-3 rounded-lg text-sm text-center ${status.type === 'error' ? 'status-error' : 'status-success'}`}>
          {status.message}
        </div>
      )}
    </div>
  );
};

export default LoginForm;
