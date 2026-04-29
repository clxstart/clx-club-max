import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfoVO } from '../api/types'
import { authApi } from '../api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('clx_admin_token') || '')
  const userInfo = ref<UserInfoVO | null>(null)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('clx_admin_token', newToken)
  }

  function clearToken() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('clx_admin_token')
  }

  async function fetchUserInfo() {
    const res = await authApi.me()
    if (res.data.code === 200) {
      userInfo.value = res.data.data
    }
  }

  function isAdmin() {
    return userInfo.value?.roles?.includes('admin') ?? false
  }

  return { token, userInfo, setToken, clearToken, fetchUserInfo, isAdmin }
})