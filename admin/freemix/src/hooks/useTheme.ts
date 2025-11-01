import { ref, inject, watch } from 'vue';

export function useTheme() {
  // 注入主题变量
  const isDark = inject('isDark', ref(false));
  const toggleTheme = inject('toggleTheme', () => {});
  
  // 主题设置
  const themeSettings = ref({
    mode: 'auto' as 'light' | 'dark' | 'auto',
    animationsEnabled: true
  });
  
  // 保存主题设置
  const saveTheme = (isDarkRef: Ref<boolean>, toggleThemeFn: () => void) => {
    // 应用主题设置
    if (themeSettings.value.mode === 'dark') {
      if (!isDarkRef.value) {
        toggleThemeFn();
      }
    } else if (themeSettings.value.mode === 'light') {
      if (isDarkRef.value) {
        toggleThemeFn();
      }
    }
  };
  
  return {
    isDark,
    toggleTheme,
    themeSettings,
    saveTheme
  };
}