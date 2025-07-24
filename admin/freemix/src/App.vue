<template>
  <!-- 开关位置调整到右上角 -->
   <n-config-provider 
    :theme="isDark ? darkTheme : null" 
    :theme-overrides="themeOverrides"
    style="width: 100%;height: 100%;"
  >
  <div class="theme-switch">
    <n-switch 
      v-model:value="isDark" 
      :rail-style="railStyle"
      @update:value="toggleTheme"
    >
      <template #checked></template>
      <template #unchecked></template>
    </n-switch>
  </div>

  <!-- 主题配置器 -->
  
  <n-global-style />
    <n-message-provider>
      <router-view :class="{'dark-theme': isDark, 'light-theme': !isDark}" />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { ref, computed, watch,onMounted, type CSSProperties } from 'vue';
import { 
  NConfigProvider, 
  darkTheme, 
  NSwitch,
  NMessageProvider,
  type GlobalThemeOverrides,
  NGlobalStyle

} from 'naive-ui';

// 主题状态管理
const isDark = ref(false);

// 切换主题并保存状态
const toggleTheme = (value: boolean) => {
  isDark.value = value;
  localStorage.setItem('theme-dark', JSON.stringify(value));
};

// 初始化时读取保存的主题状态
onMounted(() => {
  const savedTheme = localStorage.getItem('theme-dark');
  if (savedTheme) {
    isDark.value = JSON.parse(savedTheme);
  }
});

// 主题样式覆盖配置
const themeOverrides = computed<GlobalThemeOverrides>(() => ({
  common: {
    bodyColor: isDark.value ? '#000000' : '#ffffff',
    // textColorBase: isDark.value ? '#ffffff' : '#000000',
    // primaryColor: isDark.value ? '#ffffff' : '#000000',
    // primaryColorHover: isDark.value ? '#ffffff' : '#000000'
  },
  Switch: {
    // ✅ 动态按钮颜色
    buttonColor: isDark.value ? '#ffffff' : '#000000',
    // ✅ 激活状态按钮颜色
    buttonColorActive: isDark.value ? '#ffffff' : '#000000',
    buttonBoxShadow:isDark.value ? '#000000' : '#ffffff'
    // ✅ 轨道激活颜色（蓝色系）
    // railColorActive: isDark.value ? '#ffffff' : '#000000'
  }
}));

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
    style.background = '#000000';
     style.boxShadow = '0 0 0 2px #ffffff';
  } else {
    style.background = '#ffffff';
     style.boxShadow = '0 0 0 2px #000000';
  }
  return style;
};
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
  background-color: #ffffff;
  color: #000000;
}

/* 暗黑主题样式 */
.dark-theme {
  background-color: #000000;
  color: #ffffff;
}

/* 开关位置样式 */
.theme-switch {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
}
</style>