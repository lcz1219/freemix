import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login.vue'
import Register from '@/views/register.vue'
import HomePage from '@/views/home.vue'
import AddGoal from '@/views/add-goal.vue'

const routes = [
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/', redirect: '/login' },
  { path: '/home', name: 'Home', component: HomePage },
  { path: '/add-goal', name: 'AddGoal', component: AddGoal }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（示例：登录状态验证）
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token')
  if (to.path !== '/login'&&to.path!='/register' && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router