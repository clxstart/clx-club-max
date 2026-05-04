import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { post, setToken, clearToken, getToken } from '@/api'
import { userAdminApi, type UserDetailVO } from '@/api/user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserDetailVO | null>(null)
  const token = ref<string>(getToken())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => {
    // 检查是否有 admin 角色
    return user.value?.roleNames?.includes('admin') || false
  })

  async function login(username: string, password: string) {
    const res = await post<{ tokenValue: string; tokenName: string }>('/auth/login', {
      username,
      password
    })
    const tokenValue = res.tokenValue
    if (tokenValue) {
      token.value = tokenValue
      setToken(tokenValue)
      await fetchUser()
    }
    return res
  }

  async function fetchUser() {
    try {
      user.value = await userAdminApi.me()
    } catch {
      user.value = null
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    clearToken()
  }

  // 初始化时获取用户信息
  if (token.value) {
    fetchUser()
  }

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    login,
    fetchUser,
    logout
  }
})