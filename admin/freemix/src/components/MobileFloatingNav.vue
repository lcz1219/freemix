<template>
  <div v-if="showMobileNav" class="mobile-fab-container">
    <van-floating-bubble 
      :class="isDark ? 'fab-dark' : 'fab-light'"
      axis="lock"
      magnetic="x"
      icon="more-outline"
      @click="showNavigation = !showNavigation"
    >
      <n-icon size="24">
        <svg v-if="!showNavigation" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
          <path d="M3,15h18v-2H3V15z M3,19h18v-2H3V19z M3,11h18V9H3V11z M3,5v2h18V5H3z"/>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
          <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41z"/>
        </svg>
      </n-icon>
    </van-floating-bubble>
    
    <!-- 弹出式导航菜单 -->
    <transition name="slide-up">
      <div 
        v-if="showNavigation" 
        class="navigation-menu"
        :class="isDark ? 'menu-dark' : 'menu-light'"
      >
        <div class="menu-content">
          <n-space vertical>
            <n-button 
              quaternary 
              block 
              size="large"
              :type="activeTab === 'home' ? 'primary' : 'default'"
              @click="navigateTo('/home')"
              class="menu-button"
            >
              <div class="menu-item">
                <n-icon size="20">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                    <path d="M10,20v-6h4v6h5v-8h3L12,3L2,12h3v8H10z M14,15c0.6,0,1-0.4,1-1s-0.4-1-1-1s-1,0.4-1,1S13.4,15,14,15z"/>
                  </svg>
                </n-icon>
                <span>首页</span>
              </div>
            </n-button>
            
            <n-button 
              quaternary 
              block 
              size="large"
              :type="activeTab === 'add' ? 'primary' : 'default'"
              @click="navigateTo('/add-goal')"
              class="menu-button"
            >
              <div class="menu-item">
                <n-icon size="20">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                    <path d="M20,12H4v-1c0-0.6,0.4-1,1-1h14c0.6,0,1,0.4,1,1V12z"></path>
                    <path d="M4,12h16v1c0,0.6-0.4,1-1,1H5c-0.6,0-1-0.4-1-1V12z"></path>
                    <path d="M20,10H4c-0.6,0-1,0.4-1,1v2c0,0.6,0.4,1,1,1h16c0.6,0,1-0.4,1-1v-2C21,10.4,20.6,10,20,10z M20,12H4v-1h16V12z"></path>
                  </svg>
                </n-icon>
                <span>添加</span>
              </div>
            </n-button>
            
            <n-button 
              quaternary 
              block 
              size="large"
              :type="activeTab === 'statistics' ? 'primary' : 'default'"
              @click="navigateTo('/statistics')"
              class="menu-button"
            >
              <div class="menu-item">
                <n-icon size="20">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                    <path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M9,17H7v-5h2V17z M9,9H7V7h2V9z M13,17h-2v-3h2V17z M13,11h-2V7h2V11z M17,17h-2v-5h2V17z M17,9h-2V7h2V9z"/>
                  </svg>
                </n-icon>
                <span>统计</span>
              </div>
            </n-button>
            
            <n-button 
              quaternary 
              block 
              size="large"
              :type="activeTab === 'settings' ? 'primary' : 'default'"
              @click="navigateTo('/settings')"
              class="menu-button"
            >
              <div class="menu-item">
                <n-icon size="20">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                    <path d="M19.1,12.9l-3.3-3.3c0.1-0.4,0.1-0.8,0.1-1.2c0-1.1-0.4-2.1-1.1-2.8l2.5-2.5c0.4-0.4,0.4-1,0-1.4l-1.4-1.4c-0.4-0.4-1-0.4-1.4,0 l-2.5,2.5c-0.8-0.7-1.8-1.1-2.8-1.1c-0.4,0-0.8,0-1.2,0.1L7.1,4.9c-0.4,0.4-1,0.4-1.4,0L4.3,6.3c-0.4,0.4-0.4,1,0,1.4l3.3,3.3 C7.5,11.4,7.5,11.8,7.5,12.2c0,1.1,0.4,2.1,1.1,2.8l-2.5,2.5c-0.4,0.4-0.4,1,0,1.4l1.4,1.4c0.4,0.4,1,0.4,1.4,0l2.5-2.5 c0.8,0.7,1.8,1.1,2.8,1.1c0.4,0,0.8,0,1.2-0.1l3.3,3.3c0.4,0.4,1,0.4,1.4,0l1.4-1.4c0.4-0.4,0.4-1,0-1.4L19.1,12.9z M12.2,16.9 c-1.3,0-2.4-0.9-2.7-2.2H7.2c0.3,1.3,1.4,2.2,2.7,2.2s2.4-0.9,2.7-2.2h2.3C14.6,16,13.5,16.9,12.2,16.9z"/>
                  </svg>
                </n-icon>
                <span>设置</span>
              </div>
            </n-button>
          </n-space>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, inject, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  NFloatButton,
  NIcon,
  NSpace,
  NButton
} from 'naive-ui';

// 注入主题变量
const isDark = inject('isDark', ref(false));

// 路由相关信息
const route = useRoute();
const router = useRouter();
const showNavigation = ref(false);

// 计算当前激活的标签
const activeTab = computed(() => {
  const path = route.path;
  if (path.includes('/home')) return 'home';
  if (path.includes('/add-goal')) return 'add';
  if (path.includes('/statistics')) return 'statistics';
  if (path.includes('/settings')) return 'settings';
  return 'home';
});

// 计算是否显示移动端导航
const showMobileNav = computed(() => {
  // 在登录和注册页面不显示底部导航栏
  return !['/login', '/register'].includes(route.path);
});

// 导航到指定路径
const navigateTo = (path) => {
  showNavigation.value = false;
  router.push(path);
};
</script>

<style scoped>
/* 移动端浮动操作按钮容器 */
.mobile-fab-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

/* 浮动按钮样式 */
.fab-dark {
  background: rgba(138, 43, 226, 0.9) !important;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.fab-light {
  background: rgba(138, 43, 226, 0.9) !important;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 导航菜单样式 */
.navigation-menu {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 200px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  transform: translateY(0);
  opacity: 1;
}

.menu-dark {
  background-color: rgba(30, 30, 40, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.menu-light {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.menu-content {
  padding: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-button {
  padding: 12px !important;
}

/* 滑动动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(20px);
  opacity: 0;
}
</style>