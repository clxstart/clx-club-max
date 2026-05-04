import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/components/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/user/UserListView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'user/:id',
        name: 'UserEdit',
        component: () => import('@/views/user/UserEditView.vue'),
        meta: { title: '用户编辑' }
      },
      {
        path: 'post',
        name: 'PostList',
        component: () => import('@/views/post/PostListView.vue'),
        meta: { title: '帖子管理' }
      },
      {
        path: 'comment',
        name: 'CommentList',
        component: () => import('@/views/comment/CommentListView.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'role',
        name: 'RoleList',
        component: () => import('@/views/role/RoleListView.vue'),
        meta: { title: '角色权限' }
      },
      {
        path: 'log',
        name: 'LogList',
        component: () => import('@/views/log/LogListView.vue'),
        meta: { title: '操作日志' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - CLX 后台` : 'CLX 后台'

  // 检查登录状态
  if (to.path !== '/login' && !authStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
