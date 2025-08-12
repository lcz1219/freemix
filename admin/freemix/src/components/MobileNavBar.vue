<template>
  <n-layout-footer bordered class="mobile-footer" :class="isDark ? 'dark' : 'light'">
    <n-space justify="space-around" class="footer-content">
      <n-button 
        quaternary 
        :type="activeTab === 'home' ? 'primary' : 'default'"
        @click="() => { $router.push('/home') }"
      >
        <div class="footer-button">
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
        :type="activeTab === 'add' ? 'primary' : 'default'"
        @click="() => { $router.push('/add-goal') }"
      >
        <div class="footer-button">
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
        :type="activeTab === 'statistics' ? 'primary' : 'default'"
        @click="() => { $router.push('/statistics') }"
      >
        <div class="footer-button">
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
        :type="activeTab === 'settings' ? 'primary' : 'default'"
        @click="() => { $router.push('/settings') }"
      >
        <div class="footer-button">
          <n-icon size="20">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M19.1,12.9l-3.3-3.3c0.1-0.4,0.1-0.8,0.1-1.2c0-1.1-0.4-2.1-1.1-2.8l2.5-2.5c0.4-0.4,0.4-1,0-1.4l-1.4-1.4c-0.4-0.4-1-0.4-1.4,0 l-2.5,2.5c-0.8-0.7-1.8-1.1-2.8-1.1c-0.4,0-0.8,0-1.2,0.1L7.1,4.9c-0.4,0.4-1,0.4-1.4,0L4.3,6.3c-0.4,0.4-0.4,1,0,1.4l3.3,3.3 C7.5,11.4,7.5,11.8,7.5,12.2c0,1.1,0.4,2.1,1.1,2.8l-2.5,2.5c-0.4,0.4-0.4,1,0,1.4l1.4,1.4c0.4,0.4,1,0.4,1.4,0l2.5-2.5 c0.8,0.7,1.8,1.1,2.8,1.1c0.4,0,0.8,0,1.2-0.1l3.3,3.3c0.4,0.4,1,0.4,1.4,0l1.4-1.4c0.4-0.4,0.4-1,0-1.4L19.1,12.9z M12.2,16.9 c-1.3,0-2.4-0.9-2.7-2.2H7.2c0.3,1.3,1.4,2.2,2.7,2.2s2.4-0.9,2.7-2.2h2.3C14.6,16,13.5,16.9,12.2,16.9z"/>
            </svg>
          </n-icon>
          <span>设置</span>
        </div>
      </n-button>
    </n-space>
  </n-layout-footer>
</template>

<script setup>
import { ref, inject } from 'vue';
import { 
  NLayoutFooter,
  NButton,
  NIcon,
  NSpace
} from 'naive-ui';

const props = defineProps({
  activeTab: {
    type: String,
    default: 'home'
  }
});

const isDark = inject('isDark', ref(false));
</script>

<style scoped>
.mobile-footer {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  padding: 8px 0;
  position: fixed;
  bottom: 0;
  width: 100%;
  z-index: 100;
}

.mobile-footer.light {
  background-color: rgba(255, 255, 255, 0.8);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.footer-content {
  width: 100%;
}

.footer-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 12px;
}

.footer-button span {
  margin-top: 4px;
}
</style>
