// 评论管理 API

export interface CommentAdminVO {
  id: number
  content: string
  authorId: number
  authorName: string
  postId: number
  postTitle: string
  parentId?: number
  status: number
  likeCount: number
  createdAt: string
}

export interface CommentPageRequest {
  content?: string
  status?: number
  postId?: number
  pageNo: number
  pageSize: number
}

export interface CommentPageResponse {
  total: number
  list: CommentAdminVO[]
}

import { post, del } from './index'

export const commentAdminApi = {
  /** 分页查询评论 */
  page: (params: CommentPageRequest) => post<CommentPageResponse>('/admin/comment/page', params),

  /** 删除评论 */
  remove: (commentId: number) => del<void>(`/admin/comment/${commentId}`)
}
