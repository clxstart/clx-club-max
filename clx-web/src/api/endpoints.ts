// API 端点定义

export const API_ENDPOINTS = {
  // ========== 认证相关 ==========
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    ME: '/auth/me',
    REFRESH: '/auth/refresh',
  },

  // ========== 用户相关 ==========
  USER: {
    LIST: '/user/list',
    DETAIL: (id: number | string) => `/user/${id}`,
    UPDATE: '/user',
    DELETE: (id: number | string) => `/user/${id}`,
    PROFILE: '/user/profile',
    SETTINGS: '/user/settings',
  },

  // ========== 帖子相关 ==========
  POST: {
    LIST: '/post/list',
    DETAIL: (id: number | string) => `/post/${id}`,
    CREATE: '/post',
    UPDATE: '/post',
    DELETE: (id: number | string) => `/post/${id}`,
    LIKE: (id: number | string) => `/post/${id}/like`,
  },

  // ========== 评论相关 ==========
  COMMENT: {
    LIST: (postId: number | string) => `/post/${postId}/comments`,
    CREATE: (postId: number | string) => `/post/${postId}/comment`,
    DELETE: (id: number | string) => `/comment/${id}`,
  },

  // ========== 消息相关 ==========
  MESSAGE: {
    LIST: '/message/list',
    SEND: '/message',
    READ: (id: number | string) => `/message/${id}/read`,
  },

  // ========== 通知相关 ==========
  NOTIFICATION: {
    LIST: '/notification/list',
    READ: (id: number | string) => `/notification/${id}/read`,
    READ_ALL: '/notification/read-all',
  },

  // ========== 搜索相关 ==========
  SEARCH: {
    POSTS: '/search/posts',
    USERS: '/search/users',
    COMMENTS: '/search/comments',
  },

  // ========== 管理后台 ==========
  ADMIN: {
    DASHBOARD: '/admin/dashboard',
    USERS: '/admin/users',
    POSTS: '/admin/posts',
    STATS: '/admin/stats',
  },
} as const;