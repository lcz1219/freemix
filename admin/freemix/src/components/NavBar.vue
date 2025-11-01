<template>
  <div class="sidebar-container" :class="{ collapsed: isCollapsed }">
    <div class="logo-section">
      <div class="logo">
        <div class="logo-icon">
          <img src="/icons/icon.png" alt="Logo" class="custom-logo" />
        </div>
        <span class="logo-text" v-if="!isCollapsed">目标追踪者</span>
      </div>
      <n-button
        class="collapse-btn"
        text
        @click="toggleCollapse"
        :style="{ marginLeft: isCollapsed ? '0' : 'auto' }"
      >
        <n-icon size="18">
          <ChevronBack v-if="!isCollapsed" />
          <ChevronForward v-if="isCollapsed" />
        </n-icon>
      </n-button>
    </div>

    <nav class="nav-menu">
      <n-button text type="primary" class="nav-link" :class="{ active: activeTab === 'dashboard' }"
        @click="goTo('/home')">
        <NIcon class="icon">
          <Desktop />
        </NIcon>
        <span class="nav-text" v-if="!isCollapsed">仪表盘</span>
      </n-button>
      <n-button text type="primary" class="nav-link" :class="{ active: activeTab === 'goalmanagement' }"
        @click="goTo('/goal-management')">
        <NIcon class="icon">
          <ClipboardSharp />
        </NIcon>
        <span class="nav-text" v-if="!isCollapsed">{{ '目标管理' }}</span>
      </n-button>
      <n-button text type="primary" class="nav-link" :class="{ active: activeTab === 'statistics' }"
        @click="goTo('/statistics')">
        <NIcon class="icon">
          <Analytics />
        </NIcon>
        <span class="nav-text" v-if="!isCollapsed">统计数据</span>
      </n-button>
      <n-button text type="primary" class="nav-link" :class="{ active: activeTab === 'goalstructure' }"
        @click="goTo('/goal-structure')">
        <NIcon class="icon">
          <Podium />
        </NIcon>
        <span class="nav-text" v-if="!isCollapsed">目标结构</span>
      </n-button>
    </nav>

    <div class="sidebar-footer">
      <n-button class="nav-link footer-button" :class="{ active: activeTab === 'messages' }"
        @click="goTo('/messages')">
        <NIcon class="icon">
          <ChatboxEllipses />
        </NIcon>
        <span class="nav-text" v-if="!isCollapsed">消息</span>
      </n-button>
      <n-dropdown :inverted="isDark" animated  inverted :options="options" placement="right-start" trigger="hover" @select="handleSelect" :loading="logoutLoading">
        <n-button class="nav-link footer-button">
          <n-icon>
            <PersonCircle />
          </n-icon>
          <span class="nav-text" v-if="!isCollapsed">用户</span>
        </n-button>
      </n-dropdown>

    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted, render, h, computed,watch } from 'vue';
import { NLayoutHeader, NIcon, NButton, NAvatar, useDialog, NSwitch, NTooltip, NPopselect, NText, NDropdown, useMessage, NInput, useLoadingBar } from 'naive-ui';
import { useRouter, useRoute } from 'vue-router';
import {
  SunnyOutline,
  IdCardSharp,
  Person,
  MoonOutline,
  Settings,
  LogInOutline,
  LogIn,
  GitCompareOutline,
  PersonCircle,
  Podium,
  Analytics,
  ClipboardSharp,
  Desktop,
  ChatboxEllipses,
  ChevronBack,
  ChevronForward
} from '@vicons/ionicons5';
import { isMobile } from '@/utils/device.js';
import { useStore } from 'vuex';
import upload from './upload.vue';
import TabsView from '@/components/TabsView.vue';
import { getToken } from '@/utils/tokenUtils.js'; // 导入token工具函数
import { removeToken } from '@/utils/tokenUtils.js'; // 导入token工具函数
import { useUser } from '@/hooks/useUser';
import { useNavigation } from '@/hooks/useNavigation';

const emit = defineEmits(['update:collapsed'])

const dialog = useDialog();
const loadingBar = useLoadingBar()
const router = useRouter();
const route = useRoute();
const { loading, loadingFinish: loadingfinish } = useNavigation();
onMounted(() => {
  console.log("初始路由:", route.path)
  loading()
  setTimeout(() => {
    loadingfinish()
  }, 1000)
})
const props = defineProps({
  activeTab: {
    type: String,
    default: 'dashboard'
  }
});
const message = useMessage();
const selectValue = ref('');

// 使用useUser hook
const { avatarUrl, fashionTitle, uploadAvatar, renderCustomHeader, editFashionTitle, logout: userLogout } = useUser();

// 添加折叠状态
const isCollapsed = ref(false);

// 切换折叠状态函数
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
  emit('update:collapsed', isCollapsed.value);
};

const IconLogout = () => {
  return h(NButton, { onClick: userLogout, style: 'width:100%' }, { default: () => [h(NIcon, null, { default: () => h(LogInOutline) }), '退出登录'] })
}
const IconSetting = () => {
  return h(NButton, { onClick: () => { loading(); router.push("/settings");loadingfinish() }, style: 'width:100%' }, { default: () => [h(NIcon, null, { default: () => h(Settings) }), '个人设置'] })
}
const IconRecycle = () => {
  return h(NButton,
   { onClick: () => { loading(); router.push("/recycle");loadingfinish() }, style: 'width:100%' },
    { default: () => [h(NIcon, null, { default: () => h(GitCompareOutline) }), '回收站'] })
}
const IconProfile = () => {
  return h(NButton,
   { onClick: () => { loading(); router.push("/profile"); loadingfinish() }, style: 'width:100%' },
    { default: () => [h(NIcon, null, { default: () => h(Person) }), '个人主页'] })
}
const IconLoginLog = () => {
  return h(NButton,
   { onClick: () => { loading(); router.push("/login-log");loadingfinish() }, style: 'width:100%' },
    { default: () => [h(NIcon, null, { default: () => h(IdCardSharp) }), '登录日志'] })
}
const options = [
  {
    key: 'header',
    type: 'render',
    render: renderCustomHeader
  },
  {
    key: 'header-divider',
    type: 'divider'
  },
  {
    type: 'render',
    key: 'recycle',
    props: {
      type: 'error'
    },
    render: IconProfile
  },
  {
    type: 'render',
    key: 'logout',
    props: {
      type: 'error'
    },
    render: IconLogout
  },
  {
    type: 'render',
    key: ' setting',
    props: {
      type: 'error'
    },
    render: IconSetting
  },
  {
    type: 'render',
    key: 'recycle',
    props: {
      type: 'error'
    },
    render: IconRecycle
  },
  {
    type: 'render',
    key: 'recycle',
    props: {
      type: 'error'
    },
    render: IconLoginLog
  },
  

];

const store = useStore();
const logoutLoading = ref(false);
const handleSelect = (key) => {
  console.log(key);

  if (key === 'logout') {
    userLogout();
  }
}
// 检查是否为移动设备
const isMobileDevice = ref(isMobile());

// 注入主题变量
const isDark = inject('isDark', ref(false));
const toggleTheme = inject('toggleTheme', () => { 
  emit('toggleTheme');
});

// 图标组件
const SunIcon = SunnyOutline;
const MoonIcon = MoonOutline;

// 路由


// 开关轨道样式
const railStyle = ({ focused, checked }) => {
  const style = {};
  if (checked) {
    style.background = '#81c683';
    if (focused) style.boxShadow = '0 0 0 2px #d0305040';
  } else {
    style.background = '#81c683';
    if (focused) style.boxShadow = '0 0 0 2px #2080f040';
  }
  return style;
};

const goTo = async (path) => {
  // 检查用户是否已登录（使用新的工具函数）
  const token = await getToken();
  if (!token && path !== '/login' && path !== '/register') {
    router.push('/login');
    return;
  }

  router.push(path);
};
</script>

<style scoped>
.sidebar-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 0;
  background-color: var(--card-bg);
  transition: all 0.3s ease;
  width: 220px;
  min-width: 220px;
  max-width: 220px;
}

.sidebar-container.collapsed {
  width: 64px;
  min-width: 64px;
  max-width: 64px;
}

.logo-section {
  padding: 0 20px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.sidebar-container.collapsed .logo-section {
  padding: 0 0px 10px;
  justify-content: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  transition: all 0.3s ease;
  overflow: hidden;
}

.sidebar-container.collapsed .logo {
  justify-content: center;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #81c683, #4b0082);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.custom-logo {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.logo-text {
  background: linear-gradient(to right, #81c683, #4b0082);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.sidebar-container.collapsed .logo-text {
  opacity: 0;
  width: 0;
  height: 0;
}

.collapse-btn {
  transition: all 0.3s ease;
  margin-left: auto;
}

.sidebar-container.collapsed .collapse-btn {
  margin-left: 0;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 10px;
  /* padding: 0 10px; */
  flex: 1;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 500;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 0 10px;
  transition: all 0.3s ease;
  width: calc(100% - 20px);
  justify-content: flex-start;
  overflow: hidden;
}

.sidebar-container.collapsed .nav-link {
  justify-content: center;
  margin: 0 5px;
  width: calc(100% - 10px);
  padding: 12px 8px;
}

.nav-link:hover {
  background-color: rgba(129, 198, 131, 0.1);
}

.nav-link.active {
  background-color: rgba(129, 198, 131, 0.2);
  color: #81c683;
}

.nav-link.active::after {
  display: none;
}

.nav-text {
  white-space: nowrap;
  transition: all 0.3s ease;
}

.sidebar-container.collapsed .nav-text {
  opacity: 0;
  width: 0;
  height: 0;
  overflow: hidden;
}

.sidebar-footer {
  padding: 20px 10px 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: 20px;
  transition: all 0.3s ease;
}

.sidebar-container.collapsed .sidebar-footer {
  padding: 10px 5px;
}

.footer-button {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 500;
  padding: 6px 8px;
  border-radius: 8px;
  margin: 0 10px 10px;
  transition: all 0.3s ease;
  width: calc(100% - 20px);
  justify-content: flex-start;
  overflow: hidden;
}

.sidebar-container.collapsed .footer-button {
  justify-content: center;
  margin: 0 5px 5px;
  width: calc(100% - 10px);
  padding: 12px 8px;
}

.theme-switch {
  /* display: flex;
  justify-content: center;
  padding: 15px 0;
  transition: all 0.3s ease; */
}

.sidebar-container.collapsed .theme-switch {
  opacity: 0;
  height: 0;
  padding: 0;
  overflow: hidden;
}

.icon {
  margin: 0;
  min-width: 24px;
  text-align: center;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar-container {
    padding: 15px 0;
    width: 200px;
    min-width: 200px;
    max-width: 200px;
  }

  .sidebar-container.collapsed {
    width: 56px;
    min-width: 56px;
    max-width: 56px;
  }

  .logo-section {
    padding: 0 15px 15px;
  }

  .sidebar-container.collapsed .logo-section {
    padding: 0 8px 8px;
  }

  .logo {
    font-size: 18px;
  }

  .nav-link, .footer-button {
    padding: 10px 12px;
    font-size: 14px;
  }

  .sidebar-container.collapsed .nav-link,
  .sidebar-container.collapsed .footer-button {
    padding: 10px 6px;
  }

  .nav-text {
    font-size: 14px;
  }
}
</style>