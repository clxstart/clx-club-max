import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useRegister } from '../hooks/useAuth';

type StatusType = 'success' | 'error' | 'loading' | null;

const validateUsername = (value: string): boolean => /^[a-zA-Z0-9_]+$/.test(value);
const validateEmail = (value: string): boolean => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);

export const RegisterForm: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [email, setEmail] = useState('');
  const [emailCode, setEmailCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [status, setStatus] = useState<{ type: StatusType; message: string }>({
    type: null,
    message: '',
  });
  const [shake, setShake] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const { register } = useRegister();
  const shakeTimerRef = useRef<number | null>(null);
  const countdownRef = useRef<number | null>(null);

  const clearTimers = useCallback(() => {
    if (shakeTimerRef.current !== null) {
      window.clearTimeout(shakeTimerRef.current);
      shakeTimerRef.current = null;
    }
    if (countdownRef.current !== null) {
      window.clearInterval(countdownRef.current);
      countdownRef.current = null;
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
    if (status.type) {
      setStatus({ type: null, message: '' });
    }
  }, [status.type]);

  const startCountdown = useCallback(() => {
    if (countdownRef.current !== null) {
      window.clearInterval(countdownRef.current);
      countdownRef.current = null;
    }

    setCountdown(60);
    countdownRef.current = window.setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          if (countdownRef.current !== null) {
            window.clearInterval(countdownRef.current);
            countdownRef.current = null;
          }
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  }, []);

  const handleSendEmailCode = useCallback(async () => {
    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
      setStatus({ type: 'error', message: '请先输入邮箱地址' });
      triggerShake();
      return;
    }

    if (!validateEmail(trimmedEmail)) {
      setStatus({ type: 'error', message: '邮箱格式不正确' });
      triggerShake();
      return;
    }

    setSendingCode(true);
    setStatus({ type: 'loading', message: '' });

    try {
      const response = await authApi.sendEmailCode({ email: trimmedEmail });

      if (response.code === 200) {
        setStatus({ type: 'success', message: '验证码已发送，请查收邮箱' });
        startCountdown();
        return;
      }

      setStatus({ type: 'error', message: response.msg || '发送失败，请重试' });
      triggerShake();
    } catch (error) {
      const message = error instanceof Error ? error.message : '网络异常，请稍后重试';
      setStatus({ type: 'error', message });
      triggerShake();
    } finally {
      setSendingCode(false);
    }
  }, [email, startCountdown, triggerShake]);

  const handleSubmit = useCallback(async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const trimmedUsername = username.trim();
    const trimmedNickname = nickname.trim();
    const trimmedEmail = email.trim();
    const trimmedEmailCode = emailCode.trim();

    if (!trimmedUsername) {
      setStatus({ type: 'error', message: '请输入用户名' });
      triggerShake();
      return;
    }

    if (trimmedUsername.length < 3 || trimmedUsername.length > 50) {
      setStatus({ type: 'error', message: '用户名长度需在 3 到 50 个字符之间' });
      triggerShake();
      return;
    }

    if (!validateUsername(trimmedUsername)) {
      setStatus({ type: 'error', message: '用户名只能包含字母、数字和下划线' });
      triggerShake();
      return;
    }

    if (!trimmedEmail) {
      setStatus({ type: 'error', message: '请输入邮箱地址' });
      triggerShake();
      return;
    }

    if (!validateEmail(trimmedEmail)) {
      setStatus({ type: 'error', message: '邮箱格式不正确' });
      triggerShake();
      return;
    }

    if (!trimmedEmailCode) {
      setStatus({ type: 'error', message: '请输入邮箱验证码' });
      triggerShake();
      return;
    }

    if (!password) {
      setStatus({ type: 'error', message: '请输入密码' });
      triggerShake();
      return;
    }

    if (password.length < 8 || password.length > 128) {
      setStatus({ type: 'error', message: '密码长度需在 8 到 128 个字符之间' });
      triggerShake();
      return;
    }

    if (!confirmPassword) {
      setStatus({ type: 'error', message: '请再次输入密码' });
      triggerShake();
      return;
    }

    if (password !== confirmPassword) {
      setStatus({ type: 'error', message: '两次输入的密码不一致' });
      triggerShake();
      return;
    }

    setLoading(true);
    setStatus({ type: 'loading', message: '' });

    try {
      const response = await register({
        username: trimmedUsername,
        password,
        confirmPassword,
        nickname: trimmedNickname || undefined,
        email: trimmedEmail,
        emailCode: trimmedEmailCode,
      });

      if (response.code === 200) {
        setStatus({ type: 'success', message: '注册成功，正在进入系统...' });
        return;
      }

      setStatus({ type: 'error', message: response.msg || '注册失败，请稍后重试' });
      triggerShake();
    } catch (error) {
      const message = error instanceof Error ? error.message : '网络异常，请稍后重试';
      setStatus({ type: 'error', message });
      triggerShake();
    } finally {
      setLoading(false);
    }
  }, [confirmPassword, email, emailCode, nickname, password, register, triggerShake, username]);

  return (
    <div
      className={`glass-card rounded-xl p-6 md:p-8 card-hover fade-in ${shake ? 'shake' : ''}`}
      translate="no"
    >
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
              placeholder="3-50 位，字母数字下划线"
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
          <label className="acid-label block mb-2">昵称 <span className="text-[#39ff14]/30">(可选)</span></label>
          <div className="relative group">
            <input
              type="text"
              value={nickname}
              onChange={(event) => setNickname(event.target.value)}
              onFocus={clearStatus}
              className="w-full acid-input rounded-lg pl-12 pr-4 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
              placeholder="你的展示名称"
              autoComplete="nickname"
            />
            <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="3" />
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9c.26.61.78 1 1.51 1H21a2 2 0 0 1 0 4h-.09c-.73 0-1.31.39-1.51 1Z" />
              </svg>
            </span>
          </div>
        </div>

        <div>
          <label className="acid-label block mb-2">邮箱</label>
          <div className="relative group">
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              onFocus={clearStatus}
              className="w-full acid-input rounded-lg pl-12 pr-4 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
              placeholder="your@email.com"
              autoComplete="email"
            />
            <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                <polyline points="22,6 12,13 2,6" />
              </svg>
            </span>
          </div>
        </div>

        <div>
          <label className="acid-label block mb-2">邮箱验证码</label>
          <div className="flex gap-3">
            <div className="relative group flex-1">
              <input
                type="text"
                value={emailCode}
                onChange={(event) => setEmailCode(event.target.value)}
                onFocus={clearStatus}
                className="w-full acid-input rounded-lg pl-12 pr-4 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
                placeholder="6位验证码"
                maxLength={6}
              />
              <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </span>
            </div>
            <button
              type="button"
              onClick={handleSendEmailCode}
              disabled={sendingCode || countdown > 0}
              className="acid-button rounded-lg px-4 py-3 text-[#0a0a0a] font-bold text-sm whitespace-nowrap disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {countdown > 0 ? `${countdown}s` : sendingCode ? '发送中...' : '发送验证码'}
            </button>
          </div>
        </div>

        <div className="divider-gradient" />

        <div>
          <label className="acid-label block mb-2">密码</label>
          <div className="relative group">
            <input
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              onFocus={clearStatus}
              className="w-full acid-input rounded-lg pl-12 pr-12 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
              placeholder="8-128 位，建议包含大小写和符号"
              autoComplete="new-password"
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
          <label className="acid-label block mb-2">确认密码</label>
          <div className="relative group">
            <input
              type={showConfirmPassword ? 'text' : 'password'}
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              onFocus={clearStatus}
              className="w-full acid-input rounded-lg pl-12 pr-12 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
              placeholder="再次输入密码"
              autoComplete="new-password"
            />
            <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                <path d="m9.5 12 1.8 1.8 3.7-3.7" />
              </svg>
            </span>
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute right-4 top-1/2 -translate-y-1/2 icon-container hover:text-[#39ff14] transition-colors"
            >
              {showConfirmPassword ? (
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
          <button
            type="submit"
            disabled={loading}
            className="w-full acid-button rounded-lg py-3 text-[#0a0a0a] font-bold uppercase tracking-widest disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span className="flex items-center justify-center gap-2">
              {loading ? (
                <>
                  <div className="loader" />
                  注册中...
                </>
              ) : (
                '立即注册'
              )}
            </span>
          </button>
        </div>

        {status.type && status.type !== 'loading' && (
          <div className={`p-3 rounded-lg text-sm text-center ${status.type === 'error' ? 'status-error' : 'status-success'}`}>
            {status.message}
          </div>
        )}
      </form>

      <div className="auth-footer text-sm">
        <span className="text-[#39ff14]/55">已有账号？</span>
        <Link to="/login" className="text-[#39ff14] transition hover:text-[#e6ff00] hover:underline">
          去登录
        </Link>
      </div>
    </div>
  );
};

export default RegisterForm;
