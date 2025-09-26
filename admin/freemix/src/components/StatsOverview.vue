<template>
  <section class="stats-overview">
    <n-grid :cols="4" :x-gap="16" :y-gap="16" responsive="screen">
      <n-grid-item>
        <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
          <div class="stat-card-content">
            <div class="stat-icon" style="background-color: rgba(138, 43, 226, 0.2);">
              <n-icon size="24" color="#8a2be2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                  <path d="M12,2C6.5,2,2,6.5,2,12s4.5,10,10,10s10-4.5,10-10S17.5,2,12,2z M12,20c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8 S16.4,20,12,20z"/>
                  <path d="M13,12l4-4c0.3-0.3,0.3-0.7,0-1s-0.7-0.3-1,0L12,11L8,7c-0.3-0.3-0.7-0.3-1,0s-0.3,0.7,0,1l4,4 c0.1,0.1,0.3,0.2,0.5,0.2S12.9,12.1,13,12z"/>
                </svg>
              </n-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalGoals }}</div>
              <div class="stat-label">总目标数</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
      
      <n-grid-item>
        <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
          <div class="stat-card-content">
            <div class="stat-icon" style="background-color: rgba(0, 201, 167, 0.2);">
              <n-icon size="24" color="#00c9a7">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                  <path d="M12,2C6.5,2,2,6.5,2,12s4.5,10,10,10s10-4.5,10-10S17.5,2,12,2z M12,20c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8 S16.4,20,12,20z"/>
                  <path d="M10.5,15.5l-2-2c-0.3-0.3-0.3-0.7,0-1s0.7-0.3,1,0l1,1l3.5-3.5c0.3-0.3,0.7-0.3,1,0s0.3,0.7,0,1L11.5,15.5 C11.2,15.8,10.8,15.8,10.5,15.5z"/>
                </svg>
              </n-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" style="color: #00c9a7;">{{ completedGoals }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
      
      <n-grid-item>
        <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
          <div class="stat-card-content">
            <div class="stat-icon" style="background-color: rgba(138, 43, 226, 0.2);">
              <n-icon size="24" color="#8a2be2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                  <path d="M12,2C6.5,2,2,6.5,2,12s4.5,10,10,10s10-4.5,10-10S17.5,2,12,2z M12,20c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8 S16.4,20,12,20z"/>
                  <path d="M12,8c-0.6,0-1,0.4-1,1v4c0,0.6,0.4,1,1,1s1-0.4,1-1V9C13,8.4,12.6,8,12,8z"/>
                  <circle cx="12" cy="16" r="1"/>
                </svg>
              </n-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" style="color: #8a2be2;">{{ inProgressGoals }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
      
      <n-grid-item>
        <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
          <div class="stat-card-content">
            <div class="stat-icon" style="background-color: rgba(255, 107, 107, 0.2);">
              <n-icon size="24" color="#ff6b6b">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                  <path d="M12,2C6.5,2,2,6.5,2,12s4.5,10,10,10s10-4.5,10-10S17.5,2,12,2z M12,20c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8 S16.4,20,12,20z"/>
                  <path d="M15.5,9.5l-1-1L12,11.1L9.5,8.5l-1,1L11.1,12l-2.6,2.5l1,1L12,12.9l2.5,2.6l1-1L12.9,12L15.5,9.5z"/>
                </svg>
              </n-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" style="color: #ff6b6b;">{{ expiredGoals }}</div>
              <div class="stat-label">已过期</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>
  </section>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import { NCard, NIcon, NGrid, NGridItem } from 'naive-ui';

// 定义组件的props
const props = defineProps({
  totalGoals: {
    type: Number,
    default: 0
  },
  completedGoals: {
    type: Number,
    default: 0
  },
  inProgressGoals: {
    type: Number,
    default: 0
  },
  expiredGoals: {
    type: Number,
    default: 0
  }
});

// 注入主题变量
const isDark = inject('isDark', false);
</script>

<style scoped>
.stats-overview {
  margin-bottom: 20px;
}

.stat-card, .stat-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  height: 100%;
}

.stat-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-card-light {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.stat-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}
</style>