<template>
  <!-- 顶栏容器 — 参照 fast-soy-admin GlobalHeader 的设计 -->
  <!-- 暗色模式使用深色容器，亮色模式使用浅色容器 -->
  <div class="global-header" :class="{ 'header-dark': isDark, 'header-light': !isDark }">
    <!-- 移动端：显示汉堡菜单按钮 -->
    <div v-if="isMobileDevice" class="header-left-mobile">
      <n-button text @click="toggleMobileMenu">
        <n-icon size="22">
          <MenuOutline />
        </n-icon>
      </n-button>
    </div>

    <!-- Logo 区域 — 仅在侧边栏隐藏时显示 -->
    <!-- <div v-if="showLogo" class="header-logo">
      <img src="/icons/icon.png" alt="Freemix" class="header-logo-img" />
      <span class="header-logo-text">目标追踪者</span>
    </div> -->

    <!-- 面包屑导航 — 显示当前位置 -->
    <div class="header-breadcrumb">
      <n-breadcrumb>
        <n-breadcrumb-item @click="router.push('/home')">
          <n-icon><HomeOutline /></n-icon>
          首页
        </n-breadcrumb-item>
        <n-breadcrumb-item v-if="pageTitle !== '首页'">
          {{ pageTitle }}
        </n-breadcrumb-item>
      </n-breadcrumb>
    </div>

    <!-- 右侧操作区 -->
    <div class="header-actions">
      <!-- 全局搜索按钮 — 按 Ctrl+K 或点击唤起 -->
      <n-tooltip>
        <template #trigger>
          <n-button text class="header-action-btn" @click="showSearchModal = true">
            <n-icon size="20"><SearchOutline /></n-icon>
          </n-button>
        </template>
        <span>搜索 (Alt+K)</span>
      </n-tooltip>

      <!-- 全屏切换 — 仅桌面端 -->
      <n-tooltip v-if="!isMobileDevice">
        <template #trigger>
          <n-button text class="header-action-btn" @click="toggleFullscreen">
            <n-icon size="20">
              <ExpandOutline v-if="!isFullscreen" />
              <ContractOutline v-if="isFullscreen" />
            </n-icon>
          </n-button>
        </template>
        <span>{{ isFullscreen ? '退出全屏' : '全屏' }}</span>
      </n-tooltip>

      <!-- AI 助手快捷入口 -->
      <n-tooltip>
        <template #trigger>
          <n-button text class="header-action-btn ai-btn" @click="store.commit('setAiDrawer', true)">
            <n-icon size="20"><SparklesOutline /></n-icon>
          </n-button>
        </template>
        <span>Freemix AI (Alt+K)</span>
      </n-tooltip>

      <!-- 主题切换 — 三态切换 light/dark/auto -->
      <n-tooltip>
        <template #trigger>
          <n-button text class="header-action-btn" @click="toggleTheme">
            <n-icon size="20">
              <SunnyOutline v-if="isDark" />
              <MoonOutline v-if="!isDark" />
            </n-icon>
          </n-button>
        </template>
        <span>{{ isDark ? '切换亮色' : '切换暗色' }}</span>
      </n-tooltip>

      <!-- 用户头像下拉菜单 — pill 风格胶囊按钮 -->
      <n-dropdown
        :inverted="isDark"
        animated
        :options="userMenuOptions"
        placement="bottom-end"
        trigger="click"
        @select="handleUserMenuSelect"
        :loading="logoutLoading"
      >
        <div class="header-user-btn">
          <n-avatar :size="30" :src="avatarUrl" round :fallback-src="defaultAvatar" />
          <span class="header-username" v-if="!isMobileDevice">{{ currentUser?.username || '用户' }}</span>
          <n-icon size="14" class="user-arrow" v-if="!isMobileDevice"><ChevronDown /></n-icon>
        </div>
      </n-dropdown>
    </div>
  </div>

  <!-- 全局搜索模态框 -->
  <n-modal v-model:show="showSearchModal" preset="card" closable style="width: 600px; max-width: 90vw; border-radius: 12px;" title="全局搜索">
    <div class="search-modal-body">
      <n-input
        ref="searchInputRef"
        size="large"
        placeholder="搜索页面、目标或功能..."
        v-model:value="searchQuery"
        @input="handleSearch"
        @keydown.esc="showSearchModal = false"
        round
      >
        <template #prefix>
          <n-icon :component="SearchOutline" />
        </template>
      </n-input>
      <div class="search-results" v-if="searchQuery && searchResults.length > 0">
        <div
          v-for="item in searchResults"
          :key="item.path"
          class="search-result-item"
          @click="navigateTo(item.path); showSearchModal = false"
        >
          <n-icon><component :is="item.icon" /></n-icon>
          <span>{{ item.title }}</span>
        </div>
      </div>
      <div class="search-empty" v-if="searchQuery && searchResults.length === 0">
        未找到匹配结果
      </div>
    </div>
  </n-modal>
</template>

<script setup>
import { ref, computed, inject, h, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { isMobile } from '@/utils/device.js'
import { isDesktop } from '@/utils/device.js'
import {
  NButton, NIcon, NAvatar, NDropdown, NTooltip, NBreadcrumb, NBreadcrumbItem,
  NModal, NInput,
  useMessage
} from 'naive-ui'
import {
  SunnyOutline, MoonOutline, SearchOutline, HomeOutline,
  ExpandOutline, ContractOutline, SparklesOutline,
  Settings, LogInOutline, IdCardSharp, Person, GitCompareOutline,
  HelpCircleOutline, MenuOutline, Desktop, Analytics, ClipboardSharp,
  Podium, TrophyOutline, CalendarOutline, RepeatOutline, ChatboxEllipses,
  ChevronDown
} from '@vicons/ionicons5'
import { useUser } from '@/hooks/useUser'
import { useAvatar } from '@/hooks/useAvatar'

const router = useRouter()
const route = useRoute()
const store = useStore()
const message = useMessage()
const isMobileDevice = isMobile()

// 全屏功能 — 使用原生 Fullscreen API（项目未安装 @vueuse/core）
const isFullscreen = ref(false)
const toggleFullscreen = async () => {
  if (!document.fullscreenElement) {
    await document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    await document.exitFullscreen()
    isFullscreen.value = false
  }
}

// 监听全屏变化事件（按 ESC 退出全屏时同步状态）
const onFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}
onMounted(() => document.addEventListener('fullscreenchange', onFullscreenChange))
onUnmounted(() => document.removeEventListener('fullscreenchange', onFullscreenChange))

// 主题状态 — 从 provide 获取
const isDark = inject('isDark', ref(true))
const appToggleTheme = inject('toggleTheme', () => {})
// 封装 toggleTheme：App.vue 的 toggleTheme(value) 需要传入布尔值，这里自动取反
const toggleTheme = () => {
  appToggleTheme(!isDark.value)
}

// 用户信息
const { avatarUrl, logout: userLogout, renderCustomHeader } = useUser()
const currentUser = computed(() => store.state.user)

// 加载头像
onMounted(() => {
  const saveAvatarUrl = store.state.user?.avatarUrl
  if (saveAvatarUrl) {
    const { userAvatar } = useAvatar(saveAvatarUrl)
    avatarUrl.value = userAvatar.value
  }
})

// 当前页面标题 — 根据路由路径计算，与 TabsView 的 routeTitleMap 保持一致
const pageTitle = computed(() => {
  const path = route.path
  if (path === '/home' || path === '/') return '指挥中心'
  if (path.includes('/goal-management')) return '目标舰队'
  if (path.includes('/statistics')) return '数据星云'
  if (path.includes('/goal-structure')) return '目标星系'
  if (path.includes('/goal-library')) return '星际航道'
  if (path.includes('/achievements')) return '荣誉星章'
  if (path.includes('/calendar')) return '星历规划'
  if (path.includes('/recurring-goals')) return '定期巡航'
  if (path.includes('/recycle')) return '黑洞回收'
  if (path.includes('/log-management')) return '航线档案'
  if (path.includes('/messages')) return '信号收发站'
  if (path.includes('/settings')) return '星舰配置'
  if (path.includes('/profile')) return '宇航员档案'
  if (path.includes('/login-log')) return '星际航行日志'
  if (path.includes('/user-guide')) return '新手启航指南'
  if (path.includes('/add-goal')) return '发射新目标'
  if (path.includes('/share')) return '分享视图'
  return ''
})

// 登出加载状态
const logoutLoading = ref(false)

// 退出登录
const handleLogout = () => {
  logoutLoading.value = true
  userLogout().finally(() => {
    logoutLoading.value = false
  })
}

// 用户下拉菜单选项 — 紧凑现代风格，图标与标签对齐
const userMenuOptions = computed(() => {
  const isAdminUser = store.state.user?.email === '1033519224@qq.com'
  // 通用 label+icon 渲染函数
  const makeItem = (key, label, icon) => ({
    key,
    label: () => h('div', { style: 'display:flex;align-items:center;gap:10px;' }, [
      h(NIcon, { size: 18, style: 'opacity:0.7;' }, { default: () => h(icon) }),
      h('span', { style: 'font-size:14px;' }, label),
    ]),
  })

  return [
    makeItem('profile', '个人主页', Person),
    makeItem('settings', '星舰配置', Settings),
    makeItem('login-log', '登录日志', IdCardSharp),
    ...(isAdminUser ? [makeItem('log-management', '航线档案', ClipboardSharp)] : []),
    { type: 'divider', key: 'd1' },
    makeItem('recycle', '黑洞回收', GitCompareOutline),
    makeItem('user-guide', '使用指引', HelpCircleOutline),
    { type: 'divider', key: 'd2' },
    {
      key: 'logout',
      label: () => h('div', { style: 'display:flex;align-items:center;gap:10px;color:#f5222d;' }, [
        h(NIcon, { size: 18 }, { default: () => h(LogInOutline) }),
        h('span', { style: 'font-size:14px;' }, '退出登录'),
      ]),
    },
  ]
})

// 处理用户菜单点击
const handleUserMenuSelect = (key) => {
  const pathMap= {
    'profile': '/profile',
    'settings': '/settings',
    'login-log': '/login-log',
    'recycle': '/recycle',
    'user-guide': '/user-guide',
    'log-management': '/log-management',
  }

  if (key === 'logout') {
    handleLogout()
  } else if (pathMap[key]) {
    router.push(pathMap[key])
  }
}

// ============== 全局搜索 ==============
const showSearchModal = ref(false)
const searchQuery = ref('')
const searchInputRef = ref(null)

// 可搜索的路由列表 — 与 NavBar 按钮名称保持一致
const searchableRoutes = [
  { title: '指挥中心', path: '/home', icon: Desktop },
  { title: '目标舰队', path: '/goal-management', icon: ClipboardSharp },
  { title: '数据星云', path: '/statistics', icon: Analytics },
  { title: '目标星系', path: '/goal-structure', icon: Podium },
  { title: '荣誉星章', path: '/achievements', icon: TrophyOutline },
  { title: '星历规划', path: '/calendar', icon: CalendarOutline },
  { title: '定期巡航', path: '/recurring-goals', icon: RepeatOutline },
  { title: '黑洞回收', path: '/recycle', icon: GitCompareOutline },
  { title: '信号收发站', path: '/messages', icon: ChatboxEllipses },
  { title: '星舰配置', path: '/settings', icon: Settings },
  { title: '新手启航指南', path: '/user-guide', icon: HelpCircleOutline },
]

const searchResults = computed(() => {
  if (!searchQuery.value) return []
  const query = searchQuery.value.toLowerCase()
  return searchableRoutes.filter(item =>
    item.title.toLowerCase().includes(query)
  )
})

const handleSearch = () => { /* 搜索由 computed 自动处理 */ }

const navigateTo = (path) => {
  router.push(path)
}

// 键盘快捷键：Alt+K 唤起搜索 / AI 助手
const handleKeydown = (e) => {
  if (e.altKey && e.key === 'k') {
    e.preventDefault()
    showSearchModal.value = true
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

// 移动端菜单切换
const emit = defineEmits(['toggleMobileMenu'])
const toggleMobileMenu = () => {
  emit('toggleMobileMenu')
}
</script>

<style scoped>
/* 顶栏主容器 — sticky 吸附顶部，随内容滚动 */
.global-header {
  display: flex;
  align-items: center;
  height: var(--fm-header-height);
  padding: 0 16px;
  background-color: rgb(var(--fm-inverted-bg-rgb));
  /* border-bottom: 1px solid rgba(var(--fm-base-text-rgb), 0.06); */
  /* box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06); */
  z-index: 100;
  position: sticky;
  top: 0;
  transition: background-color 0.3s, border-color 0.3s;
  -webkit-app-region: drag; /* Electron 窗口拖动区域 */
}

/* 面包屑区域 — 占满剩余空间 */
.header-breadcrumb {
  flex: 1;
  margin-left: 8px;
  -webkit-app-region: no-drag; /* 面包屑区域不可拖动 */
}

/* 右侧操作区 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  -webkit-app-region: no-drag; /* 按钮区域不可拖动 */
}

/* 操作按钮基础样式 */
.header-action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.header-action-btn:hover {
  background-color: rgba(var(--fm-primary-rgb), 0.08);
}

/* AI 按钮特殊样式 */
.ai-btn:hover {
  background-color: rgba(var(--fm-primary-rgb), 0.12);
}

/* 用户按钮 — pill 胶囊风格，带下拉箭头 */
.header-user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px 10px 2px 3px;
  border-radius: 20px;
  cursor: pointer;
  transition: background-color 0.2s, box-shadow 0.2s;
  border: 1px solid rgba(var(--fm-primary-rgb), 0.15);
}

.header-user-btn:hover {
  background-color: rgba(var(--fm-primary-rgb), 0.1);
  border-color: rgba(var(--fm-primary-rgb), 0.3);
  box-shadow: 0 0 8px rgba(var(--fm-primary-rgb), 0.1);
}

.header-username {
  font-size: 13px;
  font-weight: 500;
  color: rgb(var(--fm-base-text-rgb));
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-arrow {
  color: rgba(var(--fm-base-text-rgb), 0.4);
  transition: transform 0.2s, color 0.2s;
}

.header-user-btn:hover .user-arrow {
  color: rgb(var(--fm-primary-rgb));
}

/* 搜索模态框 */
.search-modal-body {
  padding: 8px 0;
}

.search-results {
  margin-top: 12px;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.15s;
  color: rgb(var(--fm-base-text-rgb));
}

.search-result-item:hover {
  background-color: rgba(var(--fm-primary-rgb), 0.08);
}

.search-empty {
  text-align: center;
  padding: 24px;
  color: rgba(var(--fm-base-text-rgb), 0.4);
  font-size: 14px;
}

/* 用户下拉面板 — 背景跟随主题色：dark=#121212，light=#fff */
:deep(.n-dropdown-menu) {
  background-color: rgb(var(--fm-inverted-bg-rgb)) !important;
  border: 1px solid rgba(var(--fm-base-text-rgb), 0.08) !important;
  border-radius: 10px !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
  padding: 6px 0 !important;
}

:deep(.n-dropdown-option) {
  color: rgb(var(--fm-base-text-rgb)) !important;
}

:deep(.n-dropdown-option:hover) {
  background-color: rgba(var(--fm-primary-rgb), 0.08) !important;
}

/* 移动端适配 */
.header-left-mobile {
  display: flex;
  align-items: center;
  margin-right: 8px;
  -webkit-app-region: no-drag;
}
</style>
