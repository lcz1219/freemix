<template>
  <n-layout-header class="header" bordered>
    <div class="logo">
      <div class="logo-icon">
        <n-icon size="24" color="white">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
            <path d="M18.73,7.05l0,0c-0.25-0.63-0.86-1.04-1.55-1.04c-0.05,0-0.11,0-0.16,0.01c-0.25-0.64-0.87-1.07-1.61-1.07 c-0.1,0-0.19,0.01-0.29,0.03c-0.28-0.64-0.93-1.06-1.7-1.06c-0.85,0-1.58,0.53-1.87,1.3c-0.26-0.05-0.52-0.07-0.78-0.07 c-2.34,0-4.15,2.01-3.9,4.33c-1.56,0.51-2.72,1.95-2.72,3.66c0,1.14,0.51,2.16,1.32,2.83C4.13,15.38,4,15.68,4,16 c0,1.66,1.34,3,3,3h10c2.76,0,5-2.24,5-5c0-2.64-2.05-4.78-4.66-4.96C18.82,9.3,19.11,8.12,18.73,7.05z"/>
          </svg>
        </n-icon>
      </div>
      <span class="logo-text">目标追踪者</span>
    </div>
    
    <nav class="nav">
      <n-button 
        text 
        type="primary" 
        class="nav-link" 
        :class="{ active: activeTab === 'dashboard' }"
        @click="goTo('/home')"
      >
        仪表盘
      </n-button>
      <n-button 
        text 
        type="primary" 
        class="nav-link" 
        :class="{ active: activeTab === 'goalmanagement' }"
        @click="goTo('/goal-management')"
      >
        {{  '目标管理' }}
      </n-button>
      <n-button 
        text 
        type="primary" 
        class="nav-link" 
        :class="{ active: activeTab === 'statistics' }"
        @click="goTo('/statistics')"
      >
        统计数据
      </n-button>
      <n-button 
        text 
        type="primary" 
        class="nav-link" 
        :class="{ active: activeTab === 'settings' }"
        @click="goTo('/settings')"
      >
        设置
      </n-button>
    </nav>
    
    <div class="header-actions">
      <!-- 主题切换按钮 -->
      <div class="theme-switch" v-if="!isMobileDevice">
        <n-tooltip placement="bottom">
          <template #trigger>
            <n-switch 
              v-model:value="isDark" 
              :rail-style="railStyle"
              @update:value="toggleTheme"
              size="small"
            >
              <template #icon>
                <n-icon v-if="isDark" :component="MoonIcon" :size="14" />
                <n-icon v-else :component="SunIcon" :size="14" />
              </template>
            </n-switch>
          </template>
          <span>{{ isDark ? '深色模式' : '浅色模式' }}</span>
        </n-tooltip>
      </div>
      
      <n-avatar round size="medium" src="https://api.dicebear.com/7.x/miniavs/svg?seed=3"></n-avatar>
    </div>
  </n-layout-header>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue';
import { NLayoutHeader, NIcon, NButton, NAvatar, NSwitch, NTooltip } from 'naive-ui';
import { useRouter } from 'vue-router';
import { SunnyOutline, MoonOutline } from '@vicons/ionicons5';
import { isMobile } from '@/utils/device.js';

const props = defineProps({
  activeTab: {
    type: String,
    default: 'dashboard'
  }
});

// 检查是否为移动设备
const isMobileDevice = ref(isMobile());

// 注入主题变量
const isDark = inject('isDark', ref(false));
const toggleTheme = inject('toggleTheme', () => {});

// 图标组件
const SunIcon = SunnyOutline;
const MoonIcon = MoonOutline;

// 路由
const router = useRouter();

// 开关轨道样式
const railStyle = ({ focused, checked }) => {
  const style = {};
  if (checked) {
    style.background = '#8a2be2';
    if (focused) style.boxShadow = '0 0 0 2px #d0305040';
  } else {
    style.background = '#2080f0';
    if (focused) style.boxShadow = '0 0 0 2px #2080f040';
  }
  return style;
};

const goTo = (path) => {
  // 检查用户是否已登录
  const token = localStorage.getItem('token');
  if (!token && path !== '/login' && path !== '/register') {
    router.push('/login');
    return;
  }
  
  router.push(path);
};
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #8a2be2, #4b0082);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  background: linear-gradient(to right, #8a2be2, #4b0082);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  font-size: 16px;
  font-weight: 500;
  position: relative;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #8a2be2, #4b0082);
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}
</style>