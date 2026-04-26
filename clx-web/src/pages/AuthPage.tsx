import { FormEvent, useState, useEffect } from 'react';
import { Globe2, Layers, LockKeyhole, Mail, ShieldCheck, UserRound } from 'lucide-react';
import { authApi, saveToken } from '../api';
import type { CaptchaResult, LoginVO } from '../api/types';

interface AuthPageProps {
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
  setMessage: (msg: { text: string; error?: boolean }) => void;
  onLoginSuccess: () => void;
}

function captchaSrc(captcha?: CaptchaResult) {
  return captcha?.image || '';
}

// 登录注册页
export function AuthPage({ run, setMessage, onLoginSuccess }: AuthPageProps) {
  const [captcha, setCaptcha] = useState<CaptchaResult>();
  const [authMode, setAuthMode] = useState<'login' | 'register' | 'phone'>('login');
  const [login, setLogin] = useState({ username: '', password: '', captchaCode: '', rememberMe: true });
  const [register, setRegister] = useState({ username: '', password: '', confirmPassword: '', nickname: '', email: '', emailCode: '' });
  const [phone, setPhone] = useState({ phone: '', smsCode: '', captchaCode: '' });

  useEffect(() => {
    refreshCaptcha();
  }, []);

  async function refreshCaptcha() {
    const data = await run(() => authApi.captcha(), '图形验证码已刷新。');
    if (data) setCaptcha(data);
  }

  async function handleLogin(event: FormEvent) {
    event.preventDefault();
    const data = await run(() => authApi.login({ ...login, captchaId: captcha?.id || '' }), '登录成功。');
    if (data) {
      saveToken(data);
      onLoginSuccess();
    }
  }

  async function handlePhoneLogin(event: FormEvent) {
    event.preventDefault();
    const data = await run(() => authApi.phoneLogin({ phone: phone.phone, smsCode: phone.smsCode }), '手机号登录成功。');
    if (data) {
      saveToken(data as LoginVO);
      onLoginSuccess();
    }
  }

  async function handleRegister(event: FormEvent) {
    event.preventDefault();
    const data = await run(() => authApi.register(register), '注册成功，已保存登录态。');
    if (data) {
      saveToken(data);
      onLoginSuccess();
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-brand">
        <div className="auth-logo"><Layers size={38} /></div>
        <strong>CLXHXH</strong>
      </div>
      <div className="auth-card">
        {authMode === 'login' ? (
          <>
            <form className="form-grid" onSubmit={handleLogin}>
              <label className="auth-field-label">用户名</label>
              <div className="auth-input">
                <UserRound size={20} />
                <input className="field" placeholder="请输入用户名" value={login.username} onChange={(e) => setLogin({ ...login, username: e.target.value })} />
              </div>
              <label className="auth-field-label">密码</label>
              <div className="auth-input">
                <LockKeyhole size={20} />
                <input className="field" placeholder="请输入密码" type="password" value={login.password} onChange={(e) => setLogin({ ...login, password: e.target.value })} />
              </div>
              <label className="auth-field-label">图形验证码</label>
              <div className="captcha-row">
                <div className="auth-input captcha-input">
                  <ShieldCheck size={20} />
                  <input className="field" placeholder="请输入验证码" value={login.captchaCode} onChange={(e) => setLogin({ ...login, captchaCode: e.target.value })} />
                </div>
                {captchaSrc(captcha) && <img className="captcha-img" alt="点击刷新验证码" src={captchaSrc(captcha)} onClick={refreshCaptcha} title="点击刷新" />}
              </div>
              <div className="auth-options">
                <label><input type="checkbox" checked={login.rememberMe} onChange={(e) => setLogin({ ...login, rememberMe: e.target.checked })} /> 记住我</label>
                <span>关闭浏览器后失效</span>
              </div>
              <button className="soft-btn primary">登录</button>
            </form>
            <div className="auth-divider"><span>其他登录方式</span></div>
            <div className="social-login">
              <button type="button" title="GitHub 登录" onClick={() => run(() => authApi.oauthAuthorize('github'), '正在跳转 GitHub 授权...').then((url) => url && (window.location.href = String(url)))}>G</button>
              <button type="button" title="手机号登录" onClick={() => setAuthMode('phone')}><Globe2 size={20} /></button>
            </div>
            <p className="auth-switch">没有账号？<button type="button" onClick={() => setAuthMode('register')}>注册</button></p>
          </>
        ) : authMode === 'phone' ? (
          <>
            <form className="form-grid" onSubmit={handlePhoneLogin}>
              <label className="auth-field-label">手机号</label>
              <div className="auth-input">
                <UserRound size={20} />
                <input className="field" placeholder="请输入手机号" value={phone.phone} onChange={(e) => setPhone({ ...phone, phone: e.target.value })} />
              </div>
              <label className="auth-field-label">短信验证码</label>
              <div className="captcha-row">
                <div className="auth-input captcha-input">
                  <ShieldCheck size={20} />
                  <input className="field" placeholder="请输入短信验证码" value={phone.smsCode} onChange={(e) => setPhone({ ...phone, smsCode: e.target.value })} />
                </div>
                <button type="button" className="soft-btn" onClick={() => run(() => authApi.phoneSmsCode({ phone: phone.phone, captchaId: captcha?.id || '', captchaCode: phone.captchaCode }), '短信验证码已发送。')} disabled={!phone.phone}>发送验证码</button>
              </div>
              <label className="auth-field-label">图形验证码（发送短信前验证）</label>
              <div className="captcha-row">
                <div className="auth-input captcha-input">
                  <ShieldCheck size={20} />
                  <input className="field" placeholder="请输入图形验证码" value={phone.captchaCode} onChange={(e) => setPhone({ ...phone, captchaCode: e.target.value })} />
                </div>
                {captchaSrc(captcha) && <img className="captcha-img" alt="点击刷新验证码" src={captchaSrc(captcha)} onClick={refreshCaptcha} title="点击刷新" />}
              </div>
              <button className="soft-btn primary">手机号登录</button>
            </form>
            <p className="auth-switch">使用用户名登录？<button type="button" onClick={() => setAuthMode('login')}>登录</button></p>
          </>
        ) : (
          <>
            <form className="form-grid" onSubmit={handleRegister}>
              <label className="auth-field-label">用户名</label>
              <div className="auth-input">
                <UserRound size={20} />
                <input className="field" placeholder="请输入用户名" value={register.username} onChange={(e) => setRegister({ ...register, username: e.target.value })} />
              </div>
              <label className="auth-field-label">昵称</label>
              <input className="field" placeholder="请输入昵称" value={register.nickname} onChange={(e) => setRegister({ ...register, nickname: e.target.value })} />
              <label className="auth-field-label">邮箱</label>
              <div className="auth-input">
                <Mail size={20} />
                <input className="field" placeholder="请输入邮箱" value={register.email} onChange={(e) => setRegister({ ...register, email: e.target.value })} />
              </div>
              <button type="button" className="soft-btn" onClick={() => run(() => authApi.sendEmailCode(register.email), '邮箱验证码已发送。')}>发送邮箱验证码</button>
              <input className="field" placeholder="请输入邮箱验证码" value={register.emailCode} onChange={(e) => setRegister({ ...register, emailCode: e.target.value })} />
              <div className="auth-input">
                <LockKeyhole size={20} />
                <input className="field" placeholder="请输入密码" type="password" value={register.password} onChange={(e) => setRegister({ ...register, password: e.target.value })} />
              </div>
              <div className="auth-input">
                <LockKeyhole size={20} />
                <input className="field" placeholder="请确认密码" type="password" value={register.confirmPassword} onChange={(e) => setRegister({ ...register, confirmPassword: e.target.value })} />
              </div>
              <button className="soft-btn primary">注册</button>
            </form>
            <p className="auth-switch">已有账号？<button type="button" onClick={() => setAuthMode('login')}>登录</button></p>
          </>
        )}
      </div>
    </section>
  );
}