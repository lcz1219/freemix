<template>
  <n-config-provider 
    :theme="isDark ? darkTheme : null" 
    :theme-overrides="themeOverrides"
    :locale="locale"
    :date-locale="dateLocale"
    class="config-provider"
  >
  <van-config-provider :theme="isDark?'dark':'light'">
    <!-- 自定义拖动区域（仅桌面端）- 仅窗口顶部边缘 -->
    <div v-if="!isMobileDevice" class="drag-region">
      <!-- 仅用于窗口拖动的透明区域 -->
    </div>

    <!-- 全局样式 -->
    <n-global-style />
    <n-dialog-provider>
      <n-loading-bar-provider>
    
    <!-- 消息提供器 -->
    <n-message-provider>
      <!-- 路由视图 - 应用主题类 -->
      <router-view :class="themeClass" />
      
      <!-- 移动端浮动导航组件 -->
      <MobileFloatingNav v-if="isMobileDevice" />
    </n-message-provider>
    </n-loading-bar-provider>
    </n-dialog-provider>
    </van-config-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { isMobile } from '@/utils/device.js'
import { ref, computed, onMounted, watch, type CSSProperties, provide } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  NConfigProvider, 
  darkTheme, 
  NSwitch,
  NMessageProvider,
  type GlobalThemeOverrides,
  NGlobalStyle,
  NDialogProvider,
  NLoadingBarProvider,
  NTooltip,
  NIcon,
  zhCN,
  dateZhCN
} from 'naive-ui';
import upload from '@/components/upload.vue';
import { SunnyOutline, MoonOutline } from '@vicons/ionicons5';
import MobileFloatingNav from '@/components/MobileFloatingNav.vue';

// 主题状态管理
const isDark = ref(false);
const isMobileDevice = isMobile(); // 调用函数获取是否为移动端

// 路由信息
const route = useRoute();
const router = useRouter();

const locale = computed(() => {
  return zhCN;
});
const dateLocale = computed(() => {
  return dateZhCN;
});

// 计算主题类
const themeClass = computed(() => {
  return isDark.value ? 'dark-theme' : 'light-theme';
});

// 导航到指定路径
const navigateTo = (path: string) => {
  showNavigation.value = false;
  router.push(path);
};

// 切换主题并保存状态
const toggleTheme = (value: boolean) => {
  isDark.value = value;
  localStorage.setItem('theme-dark', JSON.stringify(value));
  updateBodyTheme();
};

// 更新body主题类
const updateBodyTheme = () => {
  if (isDark.value) {
    document.body.classList.add('dark-theme');
    document.body.classList.remove('light-theme');
  } else {
    document.body.classList.add('light-theme');
    document.body.classList.remove('dark-theme');
  }
};

// 初始化时读取保存的主题状态
onMounted(() => {
  const savedTheme = localStorage.getItem('theme-dark');
  if (savedTheme) {
    isDark.value = JSON.parse(savedTheme);
  }
  updateBodyTheme();
});

// 主题样式覆盖配置
const themeOverrides = computed<GlobalThemeOverrides>(() => {
  const commonOverrides = {
    // 统一字体
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
    // 统一圆角
    borderRadius: '8px'
  };
  
  if (isDark.value) {
    return {
      common: {
        ...commonOverrides,
        bodyColor: '#121212',
        cardColor: '#1e1e1e',
        modalColor: '#252525',
        popoverColor: '#252525',
        tableColor: '#1e1e1e',
        textColorBase: '#e0e0e0',
        borderColor: '#333333',
        hoverColor: '#2a2a2a',
        primaryColor: '#8a2be2',
        primaryColorHover: '#9d45e5',
        primaryColorPressed: '#7a1fc9',
        primaryColorSuppl: '#7a1fc9'
      },
      Switch: {
        railColorActive: '#8a2be2'
      }
    };
  } else {
    return {
      common: {
        ...commonOverrides,
        bodyColor: '#f8f9fa',
        cardColor: '#ffffff',
        modalColor: '#ffffff',
        popoverColor: '#ffffff',
        tableColor: '#ffffff',
        textColorBase: '#333333',
        borderColor: '#e0e0e0',
        hoverColor: '#f5f5f5',
        primaryColor: '#8a2be2',
        primaryColorHover: '#9d45e5',
        primaryColorPressed: '#7a1fc9',
        primaryColorSuppl: '#7a1fc9'
      },
      Switch: {
        railColorActive: '#8a2be2'
      }
    };
  }
});
provide('isDark', isDark);
provide('toggleTheme', toggleTheme);
// 开关轨道样式
const railStyle = ({
  focused,
  checked
}: {
  focused: boolean;
  checked: boolean;
}) => {
  const style: CSSProperties = {};
  if (checked) {
    style.background = '#8a2be2';
    if (focused) style.boxShadow = '0 0 0 2px rgba(138, 43, 226, 0.3)';
  } else {
    style.background = '#e0e0e0';
    if (focused) style.boxShadow = '0 0 0 2px rgba(224, 224, 224, 0.3)';
  }
  return style;
};

// 图标组件
const SunIcon = SunnyOutline;
const MoonIcon = MoonOutline;
</script>

<style>
/* 全局基础样式 */
body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  margin: 0;
  padding: 0;
  transition: background-color 0.3s, color 0.3s;
}

/* 明亮主题样式 */
.light-theme {
  --bg-color: #f8f9fa;
  --text-color: #333333;
  --card-bg: #ffffff;
  --border-color: #e0e0e0;
  
  background-color: var(--bg-color);
  color: var(--text-color);
}

/* 暗黑主题样式 */
.dark-theme {
  --bg-color: #121212;
  --text-color: #e0e0e0;
  --card-bg: #1e1e1e;
  --border-color: #333333;
  
  background-color: var(--bg-color);
  color: var(--text-color);
}

/* 开关位置样式 */
.theme-switch {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  background: var(--card-bg);
  border-radius: 50%;
  padding: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
}


/* 确保所有Naive UI组件使用主题变量 */
.n-card {
  background-color: var(--card-bg) !important;
  color: var(--text-color) !important;
  border-color: var(--border-color) !important;
}

.n-button {
  color: var(--text-color) !important;
}

.n-input {
  background-color: var(--card-bg) !important;
  color: var(--text-color) !important;
  border-color: var(--border-color) !important;
}

.n-tabs-tab {
  color: var(--text-color) !important;
}

/* 添加平滑过渡 */
.n-config-provider, 
.n-config-provider * {
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background-color: rgba(164, 160, 170, 0.26);
  border-radius: 3px;
  transition: background-color 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background-color: rgba(137, 43, 226, 0);
}

::-webkit-scrollbar-track {
  background-color: transparent;
  border-radius: 3px;
}

::-webkit-scrollbar-corner {
  background-color: transparent;
}

/* 亮色模式下的全局滚动条样式 */
.light-theme ::-webkit-scrollbar-thumb {
  background-color: rgba(138, 43, 226, 0.3);
}

.light-theme ::-webkit-scrollbar-thumb:hover {
  background-color: rgba(138, 43, 226, 0.5);
}

.light-theme ::-webkit-scrollbar-track {
  background-color: transparent;
}

body {
  overflow-y: auto;
}
</style>