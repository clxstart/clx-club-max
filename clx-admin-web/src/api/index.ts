import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const TOKEN_KEY = 'admin_token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')

      // 401 未授权，跳转登录
      if (res.code === 401) {
        clearToken()
        useAuthStore().logout()
        window.location.href = '/login'
      }

      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

// 通用请求方法
export function request<T>(config: AxiosRequestConfig): Promise<T> {
  return service(config) as Promise<T>
}

export const get = <T>(url: string, params?: Record<string, unknown>): Promise<T> =>
  request<T>({ method: 'GET', url, params })

export const post = <T>(url: string, data?: unknown): Promise<T> =>
  request<T>({ method: 'POST', url, data })

export const put = <T>(url: string, data?: unknown): Promise<T> =>
  request<T>({ method: 'PUT', url, data })

export const del = <T>(url: string): Promise<T> =>
  request<T>({ method: 'DELETE', url })

export default service
