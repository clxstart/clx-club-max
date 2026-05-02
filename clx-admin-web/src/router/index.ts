import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/login/LoginPage.vue')
    },
    {
      path: '/',
      component: () => import('../layouts/AdminLayout.vue'),
      redirect: '/user',
      children: [
        {
          path: 'user',
          name: 'UserList',
          component: () => import('../views/user/UserListPage.vue')
        },
        {
          path: 'analytics',
          name: 'Analytics',
          component: () => import('../views/analytics/AnalyticsPage.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('clx_admin_token')

  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router