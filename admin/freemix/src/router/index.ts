import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login.vue'
import Register from '@/views/register.vue'

const routes = [
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（示例：登录状态验证）
router.beforeEach((to) => {
  const isAuthenticated = !!localStorage.getItem('token')
  if (to.path !== '/login' && !isAuthenticated) {
    return '/login'
  }
})

export default router