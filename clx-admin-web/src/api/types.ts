// API 类型定义

export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

export interface UserQuery {
  page: number
  size: number
  username?: string
  userId?: number
  status?: string
}

export interface UserPageVO {
  userId: number
  username: string
  nickname: string
  email: string
  phone: string
  status: string
  roles: string[]
  createTime: string
  lastLoginTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface RoleVO {
  roleId: number
  roleName: string
  roleCode: string
  description: string
}

export interface UserUpdateDTO {
  nickname?: string
  email?: string
  phone?: string
  signature?: string
  gender?: string
  birthday?: string
}

export interface UserInfoVO {
  userId: number
  username: string
  nickname: string
  roles: string[]
}