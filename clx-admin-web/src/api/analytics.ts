import http from './index'
import type { ApiResponse } from './types'

/** 日报表响应 */
export interface DailyReport {
  date: string
  dau: number
  wau: number
  mau: number
  newUsers: number
  newPosts: number
  newComments: number
  retention1d: number
  retention7d: number
  retention30d: number
}

/** 热门帖子 */
export interface HotPost {
  postId: number
  title: string
  authorName: string
  viewCount: number
  likeCount: number
  commentCount: number
}

/** 趋势数据 */
export interface TrendData {
  dates: string[]
  values: number[]
}

/** 分析报表 API */
export const analyticsApi = {
  /** 获取日报表 */
  getDaily(date: string) {
    return http.get<ApiResponse<DailyReport>>('/analytics/report/daily', {
      params: { date }
    })
  },

  /** 获取热门帖子 */
  getHotPosts(date: string, type: string = 'view', limit: number = 10) {
    return http.get<ApiResponse<HotPost[]>>('/analytics/report/hot-posts', {
      params: { date, type, limit }
    })
  },

  /** 获取趋势数据 */
  getTrend(startDate: string, endDate: string, metric: string = 'dau') {
    return http.get<ApiResponse<TrendData>>('/analytics/report/trend', {
      params: { startDate, endDate, metric }
    })
  }
}
