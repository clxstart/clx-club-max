<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>CLX 后台管理系统</h2>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const errorMsg = ref('')
const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  errorMsg.value = ''

  try {
    const res = await authApi.login(form.value.username, form.value.password)
    if (res.data.code === 200) {
      const token = res.data.data.token
      userStore.setToken(token)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      errorMsg.value = res.data.msg || '登录失败'
    }
  } catch (e: any) {
    errorMsg.value = e.response?.data?.msg || '网络错误'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
}

.login-card {
  width: 400px;
  padding: 20px;

  h2 {
    text-align: center;
    margin-bottom: 20px;
  }

  .el-button {
    width: 100%;
  }
}

.error {
  color: #f56c6c;
  text-align: center;
  margin-top: 10px;
}
</style>