// 数据统计 API

export interface StatsOverview {
  dau: number
  mau: number
  newUsers: number
  newPosts: number
  newComments: number
}

export interface StatsTrend {
  dates: string[]
  dau: number[]
  posts: number[]
  comments: number[]
}

import { get } from './index'

export const statsAdminApi = {
  /** 获取概览数据 */
  overview: () => get<StatsOverview>('/admin/stats/overview'),

  /** 获取趋势数据 */
  trend: (days = 7) => get<StatsTrend>('/admin/stats/trend', { days })
}
