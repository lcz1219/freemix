<template>
  <div class="achievements-page" :class="{ dark: isDark }">
    <!-- 装饰背景 -->
    <div class="bg-blur-gradient"></div>

    <div class="container-lg">
      
      <!-- 头部：Dashboard 风格 -->
      <div class="header-section">
        <div class="header-titles">
          <h1 class="main-title">成就中心</h1>
          <p class="sub-title">探索你的里程碑，见证每一次突破</p>
        </div>
        
        <!-- 数据胶囊 -->
        <div class="stats-capsule">
          <div class="stat-item">
            <span class="label">已获得</span>
            <span class="value text-success">{{ unlockedCount }}</span>
          </div>
          <div class="divider"></div>
          <div class="stat-item">
            <span class="label">总成就</span>
            <span class="value">{{ totalCount }}</span>
          </div>
          <div class="divider"></div>
          <div class="stat-item progress-item">
            <span class="label">完成率 {{ completionPercentage }}%</span>
            <n-progress 
              type="line" 
              :percentage="completionPercentage" 
              :show-indicator="false"
              :height="4"
              color="#0969da"
              style="width: 80px"
            />
          </div>
        </div>
      </div>

      <!-- 加载骨架 -->
      <div v-if="achievementsLoading" class="grid-container">
        <div v-for="n in 6" :key="n" class="skeleton-wrapper">
          <n-skeleton height="100%" width="100%" :sharp="false" />
        </div>
      </div>

      <!-- 真实数据 -->
      <div v-else class="grid-container">
        <TransitionGroup name="staggered-fade">
          <AchievementCard 
            v-for="(achievement, index) in achievements" 
            :key="achievement.id"
            v-bind="achievement"
            :style="{ '--delay': `${index * 0.05}s` }"
          />
        </TransitionGroup>
      </div>

       <!-- Empty State -->
       <div v-if="!achievementsLoading && totalCount === 0" class="empty-box">
         <n-empty description="暂无成就，快去探索吧！" size="large" />
       </div>

    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted, computed } from 'vue';
import { NProgress, NSkeleton, NEmpty } from 'naive-ui';
import { useAchievements } from '@/hooks/useAchievements';
import AchievementCard from '@/components/AchievementCard.vue';

const isDark = inject('isDark', ref(false));
const { achievements, loading: achievementsLoading, getMyAchievements } = useAchievements();

const totalCount = computed(() => achievements.value.length);
const unlockedCount = computed(() => achievements.value.filter(a => a.unlocked).length);
const completionPercentage = computed(() => {
  if (totalCount.value === 0) return 0;
  return Math.round((unlockedCount.value / totalCount.value) * 100);
});

onMounted(() => {
  getMyAchievements();
});
</script>

<style scoped>
.achievements-page {
  /* 定义企业级配色变量 */
  --bg-body: #fdfdfd;
  --text-main: #111827;
  --text-sub: #6b7280;
  --accent: #0969da;
  
  min-height: 100vh;
  background-color: var(--bg-body);
  color: var(--text-main);
  padding: 48px 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.achievements-page.dark {
  --bg-body: #0d1117;
  --text-main: #f0f6fc;
  --text-sub: #8b949e;
  --accent: #58a6ff;
}

/* 背景光晕装饰 */
.bg-blur-gradient {
  position: fixed;
  top: -100px;
  right: -100px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(9, 105, 218, 0.08) 0%, transparent 70%);
  filter: blur(60px);
  z-index: 0;
  pointer-events: none;
}

.container-lg {
  max-width: 1000px; /* 稍微收窄一点，让卡片不要太宽 */
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  z-index: 1;
}

/* Header 样式 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 48px;
  flex-wrap: wrap;
  gap: 24px;
}

.main-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.sub-title {
  color: var(--text-sub);
  margin: 0;
  font-size: 15px;
}

/* 统计胶囊 */
.stats-capsule {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(0,0,0,0.08);
  padding: 12px 20px;
  border-radius: 99px;
  gap: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.achievements-page.dark .stats-capsule {
  background: rgba(22, 27, 34, 0.8);
  border-color: #30363d;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-item.progress-item {
  align-items: flex-end;
}

.label {
  font-size: 12px;
  text-transform: uppercase;
  color: var(--text-sub);
  font-weight: 600;
  letter-spacing: 0.5px;
}

.value {
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
}

.text-success { color: #1a7f37; }
.achievements-page.dark .text-success { color: #3fb950; }

.divider {
  width: 1px;
  height: 24px;
  background-color: rgba(0,0,0,0.1);
}
.achievements-page.dark .divider { background-color: rgba(255,255,255,0.1); }


/* Grid 布局 */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); /* 更宽的卡片 */
  gap: 20px;
}

.skeleton-wrapper {
  height: 140px;
  border-radius: 12px;
  overflow: hidden;
}

.empty-box {
  padding: 60px 0;
}

/* 序列动画 */
.staggered-fade-enter-active,
.staggered-fade-leave-active {
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  transition-delay: var(--delay); /* 使用 CSS 变量实现交错动画 */
}

.staggered-fade-enter-from,
.staggered-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>