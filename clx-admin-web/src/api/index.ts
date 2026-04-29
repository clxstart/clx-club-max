import axios from 'axios'
import type {
  ApiResponse,
  UserQuery,
  UserPageVO,
  PageResult,
  RoleVO,
  UserUpdateDTO,
  UserInfoVO
} from './types'

const http = axios.create({
  baseURL: '',
  timeout: 10000
})

// 请求拦截器：添加 Token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('clx_admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：处理 401
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('clx_admin_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 用户 API
export const userApi = {
  /** 分页查询用户列表 */
  getPage(data: UserQuery) {
    return http.post<ApiResponse<PageResult<UserPageVO>>>('/admin/user/page', data)
  },

  /** 获取用户详情 */
  getById(userId: number) {
    return http.get<ApiResponse<UserPageVO>>(`/admin/user/${userId}`)
  },

  /** 封禁用户 */
  ban(userId: number) {
    return http.put<ApiResponse<void>>(`/admin/user/${userId}/ban`)
  },

  /** 解封用户 */
  unban(userId: number) {
    return http.put<ApiResponse<void>>(`/admin/user/${userId}/unban`)
  },

  /** 更新用户资料 */
  update(userId: number, data: UserUpdateDTO) {
    return http.put<ApiResponse<void>>(`/admin/user/${userId}`, data)
  },

  /** 获取用户角色 */
  getUserRoles(userId: number) {
    return http.get<ApiResponse<number[]>>(`/admin/user/${userId}/roles`)
  },

  /** 更新用户角色 */
  updateUserRoles(userId: number, roleIds: number[]) {
    return http.put<ApiResponse<void>>(`/admin/user/${userId}/roles`, roleIds)
  }
}

// 角色 API
export const roleApi = {
  /** 获取角色列表 */
  getList() {
    return http.get<ApiResponse<RoleVO[]>>('/admin/role/list')
  }
}

// 当前用户 API
export const authApi = {
  /** 登录 */
  login(username: string, password: string) {
    return axios.post<ApiResponse<{ token: string; tokenName: string }>>(
      'http://localhost:9100/auth/login',
      { username, password }
    )
  },

  /** 获取当前用户信息 */
  me() {
    return http.get<ApiResponse<UserInfoVO>>('/admin/user/me')
  }
}