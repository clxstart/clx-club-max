import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';

type StatusType = 'success' | 'error' | 'loading' | null;

// 静态配置 - 与 LoginPage 保持一致
const FLOATING_ORBS = [
  { size: 300, color: '#39ff14', top: '10%', left: '10%', delay: '0s' },
  { size: 250, color: '#a020f0', top: '60%', right: '5%', delay: '-5s' },
  { size: 200, color: '#ff006e', bottom: '10%', left: '30%', delay: '-10s' },
];

const GLOW_CIRCLES = [
  { size: 180, type: 'green', top: '5%', left: '5%', delay: '0s' },
  { size: 220, type: 'purple', top: '15%', right: '10%', delay: '-3s' },
  { size: 150, type: 'pink', bottom: '20%', left: '8%', delay: '-6s' },
];

const ROTATING_SQUARES = [
  { size: 16, color: '#39ff14', top: '12%', left: '8%', delay: '0s' },
  { size: 12, color: '#a020f0', top: '70%', right: '12%', delay: '-2s' },
  { size: 14, color: '#ff006e', bottom: '25%', left: '15%', delay: '-4s' },
];

const PULSE_DOTS = [
  { color: '#39ff14', top: '20%', left: '35%', delay: '0s' },
  { color: '#ff006e', bottom: '30%', right: '20%', delay: '-1s' },
  { color: '#a020f0', top: '65%', left: '10%', delay: '-0.5s' },
];

const SPARKLES = 12;

/**
 * 手机号登录页面 - 与 LoginPage 保持一致的视觉风格
 */
export const PhoneLoginPage: React.FC = () => {
  const [phone, setPhone] = useState('');
  const [smsCode, setSmsCode] = useState('');
  const [captchaCode, setCaptchaCode] = useState('');
  const [captchaId, setCaptchaId] = useState('');
  const [captchaImage, setCaptchaImage] = useState('');
  const [countdown, setCountdown] = useState(0);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<{ type: StatusType; message: string }>({
    type: null,
    message: '',
  });
  const [shake, setShake] = useState(false);

  const navigate = useNavigate();
  const { setToken, setUser } = useAuthStore();
  const countdownRef = useRef<number | null>(null);

  // 生成闪烁点
  const sparkles = useMemo(() =>
    Array.from({ length: SPARKLES }, (_, i) => ({
      id: i,
      left: `${Math.random() * 100}%`,
      top: `${Math.random() * 100}%`,
      delay: `${Math.random() * 3}s`,
      color: ['green', 'purple', 'pink', 'cyan'][i % 4] as 'green' | 'purple' | 'pink' | 'cyan',
    })),
  []);

  // 获取图形验证码
  const fetchCaptcha = useCallback(async (silent = false) => {
    try {
      const response = await authApi.getCaptcha();
      if (response.code === 200 && response.data) {
        setCaptchaId(response.data.captchaId);
        setCaptchaImage(response.data.captchaImage);
        setCaptchaCode('');
      }
    } catch {
      if (!silent) {
        setStatus({ type: 'error', message: '获取图形验证码失败' });
      }
    }
  }, []);

  useEffect(() => {
    void fetchCaptcha();
  }, [fetchCaptcha]);

  // 倒计时
  useEffect(() => {
    if (countdown <= 0) {
      if (countdownRef.current) {
        window.clearInterval(countdownRef.current);
        countdownRef.current = null;
      }
      return;
    }
    countdownRef.current = window.setInterval(() => {
      setCountdown((prev) => prev - 1);
    }, 1000);
    return () => {
      if (countdownRef.current) {
        window.clearInterval(countdownRef.current);
      }
    };
  }, [countdown]);

  // 发送短信验证码
  const handleSendSmsCode = useCallback(async () => {
    if (!phone.trim()) {
      setStatus({ type: 'error', message: '请输入手机号' });
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(phone.trim())) {
      setStatus({ type: 'error', message: '手机号格式不正确' });
      return;
    }
    if (!captchaId || !captchaCode.trim()) {
      setStatus({ type: 'error', message: '请输入图形验证码' });
      return;
    }

    try {
      const response = await authApi.sendSmsCode({
        phone: phone.trim(),
        captchaId,
        captchaCode: captchaCode.trim(),
      });

      if (response.code === 200) {
        setStatus({ type: 'success', message: '验证码已发送' });
        setCountdown(60);
      } else {
        setStatus({ type: 'error', message: response.msg || '发送失败' });
        await fetchCaptcha(true);
      }
    } catch {
      setStatus({ type: 'error', message: '发送失败，请重试' });
      await fetchCaptcha(true);
    }
  }, [phone, captchaId, captchaCode, fetchCaptcha]);

  // 登录
  const handleSubmit = useCallback(async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const trimmedPhone = phone.trim();
    const trimmedCode = smsCode.trim();

    if (!trimmedPhone || !/^1[3-9]\d{9}$/.test(trimmedPhone)) {
      setStatus({ type: 'error', message: '请输入正确的手机号' });
      setShake(true);
      setTimeout(() => setShake(false), 500);
      return;
    }
    if (!trimmedCode) {
      setStatus({ type: 'error', message: '请输入验证码' });
      setShake(true);
      setTimeout(() => setShake(false), 500);
      return;
    }

    setLoading(true);
    setStatus({ type: 'loading', message: '' });

    try {
      const response = await authApi.phoneLogin({
        phone: trimmedPhone,
        smsCode: trimmedCode,
      });

      if (response.code === 200 && response.data) {
        setToken(response.data.token, response.data.tokenName, false);

        try {
          const userResponse = await authApi.getCurrentUser();
          if (userResponse.code === 200 && userResponse.data) {
            setUser(userResponse.data);
          }
        } catch {
          // 获取用户信息失败不影响登录
        }

        setStatus({ type: 'success', message: '登录成功' });
        setTimeout(() => navigate('/'), 300);
      } else {
        setStatus({ type: 'error', message: response.msg || '登录失败' });
        setShake(true);
        setTimeout(() => setShake(false), 500);
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : '网络错误';
      setStatus({ type: 'error', message });
      setShake(true);
      setTimeout(() => setShake(false), 500);
    } finally {
      setLoading(false);
    }
  }, [phone, smsCode, setToken, setUser, navigate]);

  const clearStatus = useCallback(() => {
    setStatus({ type: null, message: '' });
  }, []);

  return (
    <div className="min-h-screen bg-[#0a0a0a] relative overflow-hidden flex items-center justify-center">

      {/* 扫描线效果 */}
      <div className="scan-line bg-gradient-to-r from-transparent via-[#39ff14]/20 to-transparent" />

      {/* 浮动光球背景 */}
      {FLOATING_ORBS.map((orb, i) => (
        <div
          key={`orb-${i}`}
          className="floating-orb"
          style={{
            width: orb.size,
            height: orb.size,
            background: orb.color,
            top: orb.top,
            left: orb.left,
            right: orb.right,
            bottom: orb.bottom,
            animationDelay: orb.delay,
          }}
        />
      ))}

      {/* 发光圆圈装饰 */}
      {GLOW_CIRCLES.map((circle, i) => (
        <div
          key={`circle-${i}`}
          className={`glow-circle glow-circle-${circle.type}`}
          style={{
            width: circle.size,
            height: circle.size,
            top: circle.top,
            left: circle.left,
            right: circle.right,
            bottom: circle.bottom,
            animationDelay: circle.delay,
          }}
        />
      ))}

      {/* 旋转方块 */}
      {ROTATING_SQUARES.map((sq, i) => (
        <div
          key={`sq-${i}`}
          className="rotating-square"
          style={{
            width: sq.size,
            height: sq.size,
            borderColor: sq.color,
            top: sq.top,
            left: sq.left,
            right: sq.right,
            animationDelay: sq.delay,
          }}
        />
      ))}

      {/* 脉冲圆点 */}
      {PULSE_DOTS.map((dot, i) => (
        <div
          key={`pd-${i}`}
          className="pulse-dot"
          style={{
            background: dot.color,
            top: dot.top,
            left: dot.left,
            right: dot.right,
            animationDelay: dot.delay,
          }}
        />
      ))}

      {/* 闪烁点 */}
      {sparkles.map((s) => (
        <div
          key={s.id}
          className={`sparkle sparkle-${s.color}`}
          style={{ left: s.left, top: s.top, animationDelay: s.delay }}
        />
      ))}

      {/* 主内容 */}
      <div className="relative z-10 w-full max-w-md px-4">

        {/* Logo 区 */}
        <div className="text-center mb-8 fade-in">
          <div className="mb-4">
            <div className="w-16 h-16 mx-auto rounded-xl bg-gradient-to-br from-[#39ff14] to-[#20c9e0] flex items-center justify-center logo-glow">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#0a0a0a" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="12 2 2 7 12 12 22 7 12 2" />
                <polyline points="2 17 12 22 22 17" />
                <polyline points="2 12 12 17 22 12" />
              </svg>
            </div>
          </div>

          <h1 className="text-3xl md:text-4xl font-black uppercase tracking-widest text-[#39ff14] neon-text mb-2">
            CLXHXH
          </h1>

          <div className="divider-gradient mt-6" />
        </div>

        {/* 手机号登录表单 */}
        <div className={`glass-card rounded-xl p-6 md:p-8 card-hover fade-in ${shake ? 'shake' : ''}`} translate="no">
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* 手机号 */}
            <div>
              <label className="acid-label block mb-2">手机号</label>
              <div className="relative group">
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  onFocus={clearStatus}
                  className="w-full acid-input rounded-lg pl-12 pr-4 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
                  placeholder="请输入手机号"
                  maxLength={11}
                  autoComplete="tel"
                />
                <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="5" y="2" width="14" height="20" rx="2" ry="2" />
                    <line x1="12" y1="18" x2="12.01" y2="18" />
                  </svg>
                </span>
              </div>
            </div>

            {/* 图形验证码 */}
            <div>
              <label className="acid-label block mb-2">图形验证码</label>
              <div className="flex gap-3">
                <div className="relative group flex-1">
                  <input
                    type="text"
                    value={captchaCode}
                    onChange={(e) => setCaptchaCode(e.target.value.toUpperCase())}
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

            {/* 发送短信验证码按钮 */}
            <button
              type="button"
              onClick={() => void handleSendSmsCode()}
              disabled={countdown > 0}
              className="w-full rounded-lg border border-[#39ff14]/30 bg-[#39ff14]/5 py-2.5 text-sm text-[#39ff14] hover:border-[#39ff14]/50 hover:bg-[#39ff14]/10 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {countdown > 0 ? `${countdown}s 后重发` : '获取短信验证码'}
            </button>

            {/* 短信验证码 */}
            <div>
              <label className="acid-label block mb-2">短信验证码</label>
              <div className="relative group">
                <input
                  type="text"
                  value={smsCode}
                  onChange={(e) => setSmsCode(e.target.value)}
                  onFocus={clearStatus}
                  className="w-full acid-input rounded-lg pl-12 pr-4 py-3 text-[#39ff14] font-mono placeholder-[#39ff14]/30"
                  placeholder="请输入短信验证码"
                  maxLength={6}
                  autoComplete="one-time-code"
                />
                <span className="absolute left-4 top-1/2 -translate-y-1/2 icon-container">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                  </svg>
                </span>
              </div>
            </div>

            {/* 登录按钮 */}
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

            {/* 其他登录方式 */}
            <div className="pt-1">
              <div className="auth-section-divider">其他登录方式</div>
              <div className="auth-social-list">
                <Link to="/login" className="auth-social-button" aria-label="用户名登录">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                </Link>
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

      </div>

      {/* 底部装饰 */}
      <div className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-[#39ff14]/20 to-transparent" />
    </div>
  );
};

export default PhoneLoginPage;
