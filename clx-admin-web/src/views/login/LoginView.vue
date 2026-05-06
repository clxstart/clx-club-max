<template>
  <div class="auth-page">
    <div class="auth-brand">
      <div class="auth-logo">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="28" height="28">
          <circle cx="12" cy="12" r="10"/>
          <path d="m4.93 4.93 4.24 4.24"/>
          <path d="m14.83 9.17 4.24-4.24"/>
          <path d="m14.83 14.83 4.24 4.24"/>
          <path d="m9.17 14.83-4.24 4.24"/>
          <circle cx="12" cy="12" r="4"/>
        </svg>
      </div>
      <strong>CLX</strong>
    </div>
    <div class="auth-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        @submit.prevent="handleLogin"
      >
        <div class="auth-input">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          <el-form-item prop="username" style="margin-bottom: 0; flex: 1;">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              :border="false"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
        </div>

        <div class="auth-input">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect width="18" height="11" x="3" y="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
          <el-form-item prop="password" style="margin-bottom: 0; flex: 1;">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              show-password
              :border="false"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
        </div>

        <el-button
          type="primary"
          :loading="loading"
          class="auth-btn"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(form.username, form.password)

    // 检查是否有 admin 角色
    if (!authStore.isAdmin) {
      ElMessage.error('您没有管理员权限')
      authStore.logout()
      return
    }

    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err) {
    ElMessage.error('登录失败，请检查用户名密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 复用前台登录页样式 */
.auth-page {
  position: relative;
  min-height: 100vh;
  display: grid;
  align-content: center;
  justify-items: center;
  padding: 24px 20px;
  color: var(--clx-text);
  background: var(--clx-bg);
}

.auth-page::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background:
    radial-gradient(circle at 15% 25%, rgba(109, 93, 252, 0.15) 0%, transparent 30%),
    radial-gradient(circle at 85% 75%, rgba(78, 205, 196, 0.12) 0%, transparent 30%),
    radial-gradient(circle at 45% 85%, rgba(255, 107, 107, 0.10) 0%, transparent 25%),
    radial-gradient(circle at 70% 20%, rgba(255, 230, 109, 0.08) 0%, transparent 20%),
    radial-gradient(circle at 25% 60%, rgba(109, 93, 252, 0.10) 0%, transparent 25%);
  animation: bgDrift 25s ease-in-out infinite;
  pointer-events: none;
}

@keyframes bgDrift {
  0%, 100% { transform: translate(0, 0) rotate(0deg); opacity: 1; }
  25% { transform: translate(-3%, 3%) rotate(2deg); opacity: 0.8; }
  50% { transform: translate(3%, -2%) rotate(-2deg); opacity: 1; }
  75% { transform: translate(-2%, -3%) rotate(1deg); opacity: 0.9; }
}

.auth-page::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle 120px at 8% 12%, rgba(109, 93, 252, 0.18), transparent),
    radial-gradient(circle 100px at 92% 18%, rgba(78, 205, 196, 0.15), transparent),
    radial-gradient(circle 80px at 12% 78%, rgba(255, 107, 107, 0.12), transparent),
    radial-gradient(circle 70px at 88% 85%, rgba(255, 230, 109, 0.10), transparent);
  animation: shapesFloat 12s ease-in-out infinite;
}

@keyframes shapesFloat {
  0%, 100% { opacity: 0.5; transform: scale(1) rotate(0deg); }
  33% { opacity: 0.8; transform: scale(1.1) rotate(5deg); }
  66% { opacity: 0.6; transform: scale(0.95) rotate(-3deg); }
}

.auth-brand {
  display: grid;
  justify-items: center;
  gap: 10px;
  margin-bottom: 20px;
  animation: brandFadeIn 0.6s ease-out;
  position: relative;
  z-index: 1;
}

@keyframes brandFadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.auth-logo {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: var(--clx-accent);
  background: var(--clx-surface);
  box-shadow: 6px 6px 12px #b8bcc2, -6px -6px 12px #ffffff;
  animation: logoPulse 3s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% { box-shadow: 6px 6px 12px #b8bcc2, -6px -6px 12px #ffffff; transform: scale(1); }
  50% { box-shadow: 4px 4px 8px #b8bcc2, -4px -4px 8px #ffffff, 0 0 30px rgba(109, 93, 252, 0.3); transform: scale(1.05); }
}

.auth-brand strong {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: .06em;
  color: var(--clx-text);
}

.auth-card {
  width: min(380px, calc(100vw - 32px));
  border-radius: 20px;
  background: var(--clx-surface);
  box-shadow: 8px 8px 16px #b8bcc2, -8px -8px 16px #ffffff;
  padding: 24px;
  animation: cardSlideIn 0.5s ease-out 0.2s both;
  position: relative;
  z-index: 1;
}

@keyframes cardSlideIn {
  from { opacity: 0; transform: translateY(30px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.auth-input {
  display: grid;
  grid-template-columns: 28px 1fr;
  align-items: center;
  min-height: 42px;
  padding: 0 14px;
  border-radius: 14px;
  color: var(--clx-text);
  background: var(--clx-surface);
  box-shadow: var(--clx-inset);
  margin-bottom: 14px;
}

.auth-input svg {
  color: var(--clx-muted);
  width: 18px;
  height: 18px;
}

.auth-input :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none !important;
  border: none;
  padding: 0 0 0 8px;
}

.auth-input :deep(.el-input__inner) {
  font-size: 15px;
  color: var(--clx-text);
}

.auth-input :deep(.el-input__inner::placeholder) {
  color: var(--clx-muted);
}

.auth-btn {
  width: 100%;
  min-height: 46px;
  border: 0;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  color: var(--clx-text);
  background: var(--clx-surface);
  box-shadow: 5px 5px 10px #b8bcc2, -5px -5px 10px #ffffff;
  cursor: pointer;
  transition: all .2s ease;
  margin-top: 10px;
}

.auth-btn:hover {
  color: var(--clx-accent);
}

.auth-btn:active {
  box-shadow: var(--clx-inset);
}
</style>
