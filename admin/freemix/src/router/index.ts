import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Login from '@/views/login.vue'
import Register from '@/views/register.vue'
import HomePage from '@/views/home.vue'
import AddGoal from '@/views/add-goal.vue'
import GoalManagement from '@/views/goal-management.vue'
import Statistics from '@/views/statistics.vue'
import Settings from '@/views/settings.vue'
import MobileLogin from '@/views/mobile-login.vue'
import MobileRegister from '@/views/mobile-register.vue'
import MobileHome from '@/views/mobile-home.vue'
import MobileAddGoal from '@/views/mobile-add-goal.vue'
import MobileSettings from '@/views/mobile-settings.vue'
import MobileStatistics from '@/views/mobile-statistics.vue'

import { isMobile } from '@/utils/device.js'

// 根据设备类型选择组件
const getComponent = (desktopComponent: any, mobileComponent: any) => {
  return isMobile() ? mobileComponent : desktopComponent
}

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home.vue'),
  },
  {
    path: '/goal-structure',
    name: 'GoalStructure',
    component: () => import('@/views/goal-structure.vue'),
  },
  { 
    path: '/login', 
    component: getComponent(Login, MobileLogin),
    name: 'Login'
  },
  { 
    path: '/register', 
    component: getComponent(Register, MobileRegister),
    name: 'Register'
  },
  { path: '/', redirect: '/login' },
  { 
    path: '/home', 
    name: 'Home', 
    component: getComponent(HomePage, MobileHome) 
  },
  { 
    path: '/add-goal', 
    name: 'AddGoal', 
    component: getComponent(AddGoal, MobileAddGoal) 
  },
  { 
    path: '/goal-management', 
    name: 'GoalManagement', 
    component: getComponent(GoalManagement, GoalManagement) 
  },
  { 
    path: '/statistics', 
    name: 'Statistics', 
    component: getComponent(Statistics, MobileStatistics) 
  },
  { 
    path: '/settings', 
    name: 'Settings', 
    component: getComponent(Settings, MobileSettings) 
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫（示例：登录状态验证）
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token')
  
  // 允许访问登录和注册页面，无论是否已认证
  if (to.path === '/login' || to.path === '/register') {
    // 如果已经登录，重定向到主页
    if (isAuthenticated) {
      next('/home')
    } else {
      next()
    }
    return
  }
  
  // 对于其他页面，检查认证状态
  if (!isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router