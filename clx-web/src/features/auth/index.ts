// 认证模块导出

// 类型
export type {
  LoginRequest,
  LoginResponse,
  UserInfo,
  TokenInfo,
  RegisterRequest,
  RegisterResponse,
  CaptchaResponse,
  EmailCodeRequest,
  SmsCodeRequest,
  PasswordResetRequest,
  PasswordResetConfirmRequest,
} from './types/auth.types';

// API
export { authApi } from './api/authApi';

// Store
export { useAuthStore } from './store/authStore';

// Hooks
export { useLogin, useLogout, useAuth, useRegister } from './hooks/useAuth';

// Components
export { LoginForm } from './components/LoginForm';
export { RegisterForm } from './components/RegisterForm';
export { ForgotPasswordForm } from './components/ForgotPasswordForm';

// Pages
export { LoginPage } from './pages/LoginPage';
export { RegisterPage } from './pages/RegisterPage';
export { ForgotPasswordPage } from './pages/ForgotPasswordPage';
export { OAuthCallbackPage } from './pages/OAuthCallbackPage';
