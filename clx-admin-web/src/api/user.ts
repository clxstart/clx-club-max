// 用户管理 API

export interface UserAdminVO {
  id: number
  username: string
  nickname?: string
  email?: string
  phone?: string
  status: number // 0=正常, 1=封禁
  roleIds?: number[]
  roleNames?: string[]
  createdAt: string
  lastLoginAt?: string
}

export interface UserPageRequest {
  username?: string
  status?: number
  pageNo: number
  pageSize: number
}

export interface UserPageResponse {
  total: number
  list: UserAdminVO[]
}

export interface UserDetailVO extends UserAdminVO {
  avatar?: string
  signature?: string
  gender?: string
  followCount: number
  fansCount: number
}

import { get, post, put } from './index'

export const userAdminApi = {
  /** 分页查询用户 */
  page: (params: UserPageRequest) => post<UserPageResponse>('/admin/user/page', params),

  /** 获取用户详情 */
  get: (userId: number) => get<UserDetailVO>(`/admin/user/${userId}`),

  /** 封禁用户 */
  ban: (userId: number) => put<void>(`/admin/user/${userId}/ban`),

  /** 解封用户 */
  unban: (userId: number) => put<void>(`/admin/user/${userId}/unban`),

  /** 更新用户信息 */
  update: (userId: number, data: { nickname?: string; status?: number }) =>
    put<void>(`/admin/user/${userId}`, data),

  /** 获取当前用户信息 */
  me: () => get<UserDetailVO>('/admin/user/me'),

  /** 更新用户角色 */
  updateRoles: (userId: number, roleIds: number[]) =>
    put<void>(`/admin/user/${userId}/roles`, roleIds)
}
