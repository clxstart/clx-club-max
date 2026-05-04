// 角色权限 API

export interface RoleVO {
  id: number
  name: string
  code: string
  description?: string
  permissionIds?: number[]
}

export interface PermissionVO {
  id: number
  name: string
  code: string
  type: number // 1=菜单, 2=按钮
  parentId?: number
  path?: string
}

import { get, put } from './index'

export const roleAdminApi = {
  /** 获取角色列表 */
  list: () => get<RoleVO[]>('/admin/role/list'),

  /** 获取权限列表 */
  permissions: () => get<PermissionVO[]>('/admin/permission/list'),

  /** 更新角色权限 */
  updatePermissions: (roleId: number, permissionIds: number[]) =>
    put<void>(`/admin/role/${roleId}/permissions`, permissionIds)
}
