import { createRouter, createWebHistory } from 'vue-router'
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
import LogManagement from '@/views/log-management.vue'
import ShareView from '@/views/ShareView.vue';
import AIGenHistory from '@/components/AIGenHistory.vue';

// 移动端页面
import MobileGoalManagement from '@/views/mobile/mobile-goal-management.vue'
import MobileStatistics from '@/views/mobile/mobile-statistics.vue'
import MobileAddGoal from '@/views/mobile/mobile-add-goal.vue'
import MobileSettings from '@/views/mobile/mobile-settings.vue'
import MobileMessageCenter from '@/views/mobile/mobile-MessageCenter.vue'
import MobileUserGuide from '@/views/mobile/mobile-user-guide.vue'
import MobileAIAssistant from '@/views/mobile/mobile-ai-assistant.vue'
import MobileProfile from '@/views/mobile/mobile-profile.vue'
import MobileScanConfirm from '@/views/mobile/mobile-scan-confirm.vue'
import MobileScan from '@/views/mobile/mobile-scan.vue'
import MobileLoginLog from '@/views/mobile/mobile-login-log.vue'
import MobileNotifications from '@/views/mobile/mobile-notifications.vue'
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
    component: getComponent(() => import('@/components/AIAssistantWindow.vue'), MobileAIAssistant),
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
    path: '/goal-library', 
    name: 'GoalLibrary', 
    component: getComponent(() => import('@/views/GoalLibrary.vue'), () => import('@/views/GoalLibrary.vue')) 
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
    path: '/calendar',
    name: 'Calendar',
    component: () => import('@/views/calendar.vue')
  },
  { 
    path: '/recurring-goals', 
    name: 'RecurringGoals', 
    component: () => import('@/views/RecurringGoals.vue') 
  },
  { 
    path: '/settings', 
    name: 'Settings', 
    component: getComponent(Settings, MobileSettings) 
  },
  { 
    path: '/login-log', 
    name: 'LoginLog', 
    component: getComponent(LoginLog, MobileLoginLog) 
  },
  { 
    path: '/log-management', 
    name: 'LogManagement', 
    component: LogManagement 
  },
  {
    path: '/oauth/callback',
    name: 'GitHubOAuthCallback',
    component: GitHubOAuthCallback
  },
  {
    path: '/share/goal/:token',
    name: 'ShareGoalView',
    component: () => import('@/views/ShareGoalView.vue'),
    props: true,
    meta: { requiresAuth: false }
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
    component: getComponent(() => import('@/views/Profile.vue'), MobileProfile)
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
  },
  {
    path: '/change-password',
    name: 'ChangePassword',
    component: () => import('@/views/mobile/mobile-change-password.vue')
  },
  {
    path: '/mobile/qr-confirm',
    name: 'MobileScanConfirm',
    component: MobileScanConfirm,
    meta: { requiresAuth: true }
  },
  {
    path: '/mobile/scan',
    name: 'MobileScan',
    component: MobileScan,
    meta: { requiresAuth: true }
  },
  {
    path: '/mobile/notifications',
    name: 'MobileNotifications',
    component: MobileNotifications,
    meta: { requiresAuth: true }
  },
  {
    path: '/achievements',
    name: 'Achievements',
    component: () => import('@/views/Achievements.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/updates',
    name: 'UpdateLogManager',
    component: () => import('@/views/UpdateLogManager.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/location-map',
    name: 'LocationMap',
    component: () => import('@/views/location-map.vue'),
    meta: { requiresAuth: true }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})
const store = useStore()

// 路由守卫（示例：登录状态验证）
router.beforeEach(async (to, from, next) => {
  // 【兼容处理】后端以 hash 模式重定向的第三方登录回调（如 /#/oauth/callback?token=xxx）
  // 当 URL 中包含 #/oauth/callback 时，将其解析为 History 模式的路径和参数并跳转
  const hash = window.location.hash;
  if (hash.startsWith('#/oauth/callback')) {
    // 去掉 # 前缀，得到 "/oauth/callback?token=xxx&..."
    const hashPath = hash.substring(1);
    const [path, queryString] = hashPath.split('?');
    // 清除地址栏中的 hash，替换为 History 模式的 URL
    window.history.replaceState(null, '', path + (queryString ? '?' + queryString : ''));
    // 解析查询参数
    const query: Record<string, string> = {};
    if (queryString) {
      queryString.split('&').forEach(pair => {
        const [key, value] = pair.split('=');
        if (key) query[key] = decodeURIComponent(value || '');
      });
    }
    // 用 replace 导航到目标路由，替换当前历史记录
    next({ path, query, replace: true });
    return;
  }

  // 使用tokenUtils工具函数获取token（推荐）
  const token = await getToken();
  const isAuthenticated = !!token;

  // 处理桌面端 Electron file:// 协议初始加载路径不匹配的问题
  // 例如 file:///path/to/dist/index.html 不会匹配任何路由
  if (to.matched.length === 0) {
    if (isAuthenticated) {
      next('/home')
    } else {
      next('/login')
    }
    return
  }
  
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