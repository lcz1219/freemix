import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Login from '@/views/login.vue'
import Register from '@/views/register.vue'
import HomePage from '@/views/home.vue'
import AddGoal from '@/views/add-goal.vue'
import GoalManagement from '@/views/goal-management.vue'
import Statistics from '@/views/statistics.vue'
import Settings from '@/views/settings.vue'
import LoginLog from '@/views/login-log.vue'
import MobileLogin from '@/views/mobile/mobile-login.vue'
import MobileRegister from '@/views/mobile/mobile-register.vue'
import MobileExample from '@/views/mobile-example.vue'
import MobileVantHome from '@/views/mobile-vant-home.vue'
import GitHubOAuthCallback from '@/views/github-oauth-callback.vue'
import ShareView from '@/views/ShareView.vue';
import AIGenHistory from '@/components/AIGenHistory.vue';

// 移动端页面
import MobileGoalManagement from '@/views/mobile/mobile-goal-management.vue'
import MobileStatistics from '@/views/mobile/mobile-statistics.vue'
import MobileAddGoal from '@/views/mobile/mobile-add-goal.vue'
import MobileSettings from '@/views/mobile/mobile-settings.vue'
import MobileMessageCenter from '@/views/mobile/mobile-MessageCenter.vue'
import MobileUserGuide from '@/views/mobile/mobile-user-guide.vue'
import { isDesktop } from '@/utils/device.js'
// import { getLocalStorageDesktopToken, getToken } from '@/utils/desktopToken.js';
import { getToken } from '@/utils/tokenUtils.js'; // 导入token工具函数
import { isMobile } from '@/utils/device.js'
import { useStore } from 'vuex'


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
    path: '/recycle',
    name: 'recycle',
    component: () => import('@/views/recycle.vue'),
  },
  {
    path: '/AIAssistantWindow',
    name: 'AIAssistantWindow',
    component: () => import('@/components/AIAssistantWindow.vue'),
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
    component: getComponent(HomePage, MobileVantHome) 
  },
  { 
    path: '/add-goal', 
    name: 'AddGoal', 
    component: getComponent(AddGoal, MobileAddGoal) 
  },
  { 
    path: '/goal-management', 
    name: 'GoalManagement', 
    component: getComponent(GoalManagement, MobileGoalManagement) 
  },
  { 
    path: '/messages', 
    name: 'Messages', 
    component: getComponent(() => import('@/views/MessageCenter.vue'), MobileMessageCenter)
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
  },
  { 
    path: '/login-log', 
    name: 'LoginLog', 
    component: getComponent(LoginLog, LoginLog) 
  },
  {
    path: '/oauth/callback',
    name: 'GitHubOAuthCallback',
    component: GitHubOAuthCallback
  },
  {
    path: '/share/:token',
    name: 'ShareView',
    component: ShareView,
    props: true,
    meta: { requiresAuth: false } // 分享页面不需要登录
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue')
  },
  {
    path: '/ai-gen-history',
    name: 'AIGenHistory',
    component: AIGenHistory,
    meta: { requiresAuth: true }
  },
  {
    path: '/user-guide',
    name: 'UserGuide',
    component: getComponent(() => import('@/views/user-guide.vue'), MobileUserGuide),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})
const store = useStore()

// 路由守卫（示例：登录状态验证）
router.beforeEach(async (to, from, next) => {
  // 使用tokenUtils工具函数获取token（推荐）
  const token = await getToken();
  const isAuthenticated = !!token;
  
  if(to.path==='/oauth/callback'){
    next()
    return
  }
  
  // 允许访问分享页面，无论是否已认证
  if (to.path.startsWith('/share/')) {
    next()
    return
  }
  
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