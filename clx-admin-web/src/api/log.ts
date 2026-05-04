// 操作日志 API

export interface OperLogVO {
  id: number
  operator: string
  operatorId: number
  action: string
  target: string
  detail?: string
  ip: string
  createdAt: string
}

export interface LogPageRequest {
  operator?: string
  action?: string
  startTime?: string
  endTime?: string
  pageNo: number
  pageSize: number
}

export interface LogPageResponse {
  total: number
  list: OperLogVO[]
}

import { post } from './index'

export const logAdminApi = {
  /** 分页查询日志 */
  page: (params: LogPageRequest) => post<LogPageResponse>('/admin/log/page', params)
}
