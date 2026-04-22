import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';

type StatusType = 'success' | 'error' | 'loading' | null;
type Step = 'send' | 'reset';

const validateEmail = (value: string): boolean => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);

export const ForgotPasswordForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [resetCode, setResetCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [captchaCode, setCaptchaCode] = useState('');
  const [captchaId, setCaptchaId] = useState('');
  const [captchaImage, setCaptchaImage] = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState<Step>('send');
  const [status, setStatus] = useState<{ type: StatusType; message: string }>({
    type: null,
    message: '',
  });
  const [shake, setShake] = useState(false);

  const navigate = useNavigate();
  const shakeTimerRef = useRef<number | null>(null);

  const clearTimers = useCallback(() => {
    if (shakeTimerRef.current !== null) {
      window.clearTimeout(shakeTimerRef.current);
      shakeTimerRef.current = null;
    }
  }, []);

  useEffect(() => clearTimers, [clearTimers]);

  // 获取图形验证码
  const fetchCaptcha = useCallback(async () => {
    try {
      const response = await authApi.getCaptcha();
      if (response.code === 200 && response.data) {
        setCaptchaId(response.data.captchaId);
        setCaptchaImage(response.data.captchaImage);
        setCaptchaCode('');
      }
    } catch {
      setStatus({ type: 'error', message: '获取验证码失败，请刷新页面重试。' });
    }
  }, []);

  useEffect(() => {
    fetchCaptcha();
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
    if (status.type) {
      setStatus({ type: null, message: '' });
    }
  }, [status.type]);

  // 发送重置邮件
  const handleSendResetCode = useCallback(async () => {
    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
      setStatus({ type: 'error', message: '请输入邮箱地址。' });
      triggerShake();
      return;
    }

    if (!validateEmail(trimmedEmail)) {
      setStatus({ type: 'error', message: '邮箱格式不正确。' });
      triggerShake();
      return;
    }

    if (!captchaId || !captchaCode.trim()) {
      setStatus({ type: 'error', message: '请输入图形验证码。' });
      triggerShake();
      return;
    }

    setLoading(true);
    setStatus({ type: 'loading', message: '' });

    try {
      const response = await authApi.sendPasswordReset({
        email: trimmedEmail,
        captchaId,
        captchaCode: captchaCode.trim(),
      });

      if (response.code === 200) {
        setStatus({ type: 'success', message: '重置验证码已发送到邮箱，请查收。' });
        setStep('reset');
      } else {
        setStatus({ type: 'error', message: response.msg || '发送失败，请重试。' });
        triggerShake();
        fetchCaptcha();
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : '网络异常，请稍后重试。';
      setStatus({ type: 'error', message });
      triggerShake();
      fetchCaptcha();
    } finally {
      setLoading(false);
    }
  }, [email, captchaId, captchaCode, fetchCaptcha, triggerShake]);

  // 重置密码
  const handleResetPassword = useCallback(async () => {
    const trimmedEmail = email.trim();
    const trimmedResetCode = resetCode.trim();

    if (!trimmedResetCode) {
      setStatus({ type: 'error', message: '请输入重置验证码。' });
      triggerShake();
      return;
    }

    if (!newPassword) {
      setStatus({ type: 'error', message: '请输入新密码。' });
      triggerShake();
      return;
    }

    if (newPassword.length < 8 || newPassword.length > 128) {
      setStatus({ type: 'error', message: '密码长度需在 8 到 128 个字符之间。' });
      triggerShake();
      return;
    }

    if (!confirmPassword) {
      setStatus({ type: 'error', message: '请再次输入密码。' });
      triggerShake();
      return;
    }

    if (newPassword !== confirmPassword) {
      setStatus({ type: 'error', message: '两次输入的密码不一致。' });
      triggerShake();
      return;
    }

    setLoading(true);
    setStatus({ type: 'loading', message: '' });

    try {
      const response = await authApi.confirmPasswordReset({
        email: trimmedEmail,
        resetCode: trimmedResetCode,
        newPassword,
        confirmPassword,
      });

      if (response.code === 200) {
        setStatus({ type: 'success', message: '密码重置成功，正在跳转到登录页...' });
        setTimeout(() => {
          navigate('/login');
        }, 1500);
      } else {
        setStatus({ type: 'error', message: response.msg || '重置失败，请重试。' });
        triggerShake();
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : '网络异常，请稍后重试。';
      setStatus({ type: 'error', message });
      triggerShake();
    } finally {
      setLoading(false);
    }
  }, [email, resetCode, newPassword, confirmPassword, navigate, triggerShake]);

  return (
    <div
      className={`glass-card acid-scanlines rounded-[28px] border border-[#39ff14]/28 p-6 md:p-8 ${shake ? 'shake' : ''}`}
      translate="no"
    >
      <div className="mb-6 flex items-start justify-between gap-4">
        <div>
          <p className="mb-2 text-xs uppercase tracking-[0.35em] text-[#39ff14]/55">Password Reset</p>
          <h2 className="text-3xl font-black uppercase tracking-[0.14em] text-[#39ff14]">找回密码</h2>
          <p className="mt-3 text-sm leading-6 text-[#d7ffd1]/70">
            {step === 'send' ? '输入注册邮箱，我们将发送重置验证码。' : '输入收到的验证码和新密码。'}
          </p>
        </div>
        <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-[#39ff14]/30 bg-[#39ff14]/10 text-[#39ff14] shadow-[0_0_24px_rgba(57,255,20,0.15)]">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
        </div>
      </div>

      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (step === 'send') {
            handleSendResetCode();
          } else {
            handleResetPassword();
          }
        }}
        className="space-y-4"
      >
        {/* 邮箱 */}
        <Field
          label="邮箱"
          hint="注册时使用的邮箱"
          icon={(
            <>
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
              <polyline points="22,6 12,13 2,6" />
            </>
          )}
        >
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            onFocus={clearStatus}
            className="w-full acid-input rounded-xl px-4 py-3 pl-12 text-sm text-[#39ff14] placeholder:text-[#39ff14]/28"
            placeholder="your@email.com"
            autoComplete="email"
            disabled={step === 'reset'}
          />
        </Field>

        {step === 'send' && (
          <>
            {/* 图形验证码 */}
            <Field
              label="图形验证码"
              hint="请输入下方显示的验证码"
              icon={(
                <>
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                </>
              )}
            >
              <div className="flex gap-2">
                <input
                  type="text"
                  value={captchaCode}
                  onChange={(event) => setCaptchaCode(event.target.value.toUpperCase())}
                  onFocus={clearStatus}
                  className="acid-input w-full rounded-xl px-4 py-3 pl-12 text-sm text-[#39ff14] placeholder:text-[#39ff14]/28"
                  placeholder="4位验证码"
                  maxLength={4}
                />
                <button
                  type="button"
                  onClick={fetchCaptcha}
                  className="flex min-w-[120px] items-center justify-center rounded-xl border border-[#39ff14]/30 bg-[#39ff14]/10 px-2 transition hover:bg-[#39ff14]/20"
                >
                  {captchaImage ? (
                    <img
                      src={captchaImage}
                      alt="验证码"
                      className="h-9 rounded"
                    />
                  ) : (
                    <span className="text-sm font-mono text-[#39ff14]">获取验证码</span>
                  )}
                </button>
              </div>
            </Field>

            <button
              type="submit"
              disabled={loading}
              className="acid-button w-full rounded-xl py-3 text-sm font-bold uppercase tracking-[0.28em] text-[#0a0a0a] disabled:cursor-not-allowed disabled:opacity-50"
            >
              <span className="flex items-center justify-center gap-2">
                {loading ? (
                  <>
                    <span className="loader" />
                    发送中
                  </>
                ) : (
                  '发送重置验证码'
                )}
              </span>
            </button>
          </>
        )}

        {step === 'reset' && (
          <>
            {/* 重置验证码 */}
            <Field
              label="重置验证码"
              hint="邮箱收到的6位验证码"
              icon={(
                <>
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </>
              )}
            >
              <input
                type="text"
                value={resetCode}
                onChange={(event) => setResetCode(event.target.value.toUpperCase())}
                onFocus={clearStatus}
                className="w-full acid-input rounded-xl px-4 py-3 pl-12 text-sm text-[#39ff14] placeholder:text-[#39ff14]/28"
                placeholder="6位验证码"
                maxLength={6}
              />
            </Field>

            {/* 新密码 */}
            <div className="grid gap-4 md:grid-cols-2">
              <Field
                label="新密码"
                hint="8-128 位"
                icon={(
                  <>
                    <rect x="3" y="11" width="18" height="10" rx="2" />
                    <path d="M7 11V8a5 5 0 0 1 10 0v3" />
                  </>
                )}
              >
                <input
                  type="password"
                  value={newPassword}
                  onChange={(event) => setNewPassword(event.target.value)}
                  onFocus={clearStatus}
                  className="w-full acid-input rounded-xl px-4 py-3 pl-12 text-sm text-[#39ff14] placeholder:text-[#39ff14]/28"
                  placeholder="输入新密码"
                  autoComplete="new-password"
                />
              </Field>

              <Field
                label="确认密码"
                hint="再次输入相同密码"
                icon={(
                  <>
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z" />
                    <path d="m9.5 12 1.8 1.8 3.7-3.7" />
                  </>
                )}
              >
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  onFocus={clearStatus}
                  className="w-full acid-input rounded-xl px-4 py-3 pl-12 text-sm text-[#39ff14] placeholder:text-[#39ff14]/28"
                  placeholder="再次输入密码"
                  autoComplete="new-password"
                />
              </Field>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="acid-button w-full rounded-xl py-3 text-sm font-bold uppercase tracking-[0.28em] text-[#0a0a0a] disabled:cursor-not-allowed disabled:opacity-50"
            >
              <span className="flex items-center justify-center gap-2">
                {loading ? (
                  <>
                    <span className="loader" />
                    重置中
                  </>
                ) : (
                  '重置密码'
                )}
              </span>
            </button>

            <button
              type="button"
              onClick={() => {
                setStep('send');
                setResetCode('');
                setNewPassword('');
                setConfirmPassword('');
                fetchCaptcha();
              }}
              className="w-full rounded-xl border border-[#39ff14]/30 py-3 text-sm text-[#39ff14] transition hover:bg-[#39ff14]/10"
            >
              重新发送验证码
            </button>
          </>
        )}

        {status.type && status.type !== 'loading' && (
          <div
            className={`rounded-xl px-4 py-3 text-center text-sm ${
              status.type === 'error' ? 'status-error' : 'status-success'
            }`}
          >
            {status.message}
          </div>
        )}

        <div className="flex items-center justify-between gap-3 border-t border-[#39ff14]/12 pt-4 text-sm">
          <span className="text-[#39ff14]/55">想起密码了？</span>
          <Link to="/login" className="text-[#39ff14] transition hover:text-[#e6ff00] hover:underline">
            返回登录
          </Link>
        </div>
      </form>
    </div>
  );
};

type FieldProps = {
  label: string;
  hint: string;
  icon: React.ReactNode;
  children: React.ReactNode;
};

const Field: React.FC<FieldProps> = ({ label, hint, icon, children }) => (
  <label className="block">
    <div className="mb-2 flex items-center justify-between gap-3">
      <span className="acid-label">{label}</span>
      <span className="text-[11px] text-[#39ff14]/42">{hint}</span>
    </div>
    <div className="relative">
      {children}
      <span className="icon-container pointer-events-none absolute left-4 top-1/2 -translate-y-1/2">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          {icon}
        </svg>
      </span>
    </div>
  </label>
);

export default ForgotPasswordForm;
