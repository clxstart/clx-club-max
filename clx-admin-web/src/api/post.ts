// 帖子管理 API

export interface PostAdminVO {
  id: number
  title: string
  content: string
  authorId: number
  authorName: string
  categoryId?: number
  categoryName?: string
  status: number // 0=正常, 1=隐藏, 2=删除
  viewCount: number
  likeCount: number
  commentCount: number
  createdAt: string
}

export interface PostPageRequest {
  title?: string
  status?: number
  authorId?: number
  pageNo: number
  pageSize: number
}

export interface PostPageResponse {
  total: number
  list: PostAdminVO[]
}

import { get, post, put, del } from './index'

export const postAdminApi = {
  /** 分页查询帖子 */
  page: (params: PostPageRequest) => post<PostPageResponse>('/admin/post/page', params),

  /** 获取帖子详情 */
  get: (postId: number) => get<PostAdminVO>(`/admin/post/${postId}`),

  /** 更新帖子状态 */
  updateStatus: (postId: number, status: number) =>
    put<void>(`/admin/post/${postId}/status`, { status }),

  /** 删除帖子 */
  remove: (postId: number) => del<void>(`/admin/post/${postId}`)
}
