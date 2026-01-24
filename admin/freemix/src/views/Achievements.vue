<template>
  <div class="achievements-container" :class="{ dark: isDark }">
    <div class="container-lg">
      <div class="achievements-header">
        <h2 class="section-title">我的成就</h2>
        <div class="achievement-stats">
          <span class="stat-item">已解锁: {{ achievements.filter(a => a.unlocked).length }} / {{ achievements.length }}</span>
        </div>
      </div>
      
      <div v-if="achievementsLoading" class="loading-state">
        <n-spin size="large" />
      </div>
      
      <div v-else class="achievements-grid">
        <AchievementCard 
          v-for="achievement in achievements" 
          :key="achievement.id"
          v-bind="achievement"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue';
import { NSpin } from 'naive-ui';
import { useAchievements } from '@/hooks/useAchievements';
import AchievementCard from '@/components/AchievementCard.vue';

// 注入主题变量
const isDark = inject('isDark', ref(false));
const { achievements, loading: achievementsLoading, getMyAchievements } = useAchievements();

onMounted(() => {
  getMyAchievements();
});
</script>

<style scoped>
.achievements-container {
  min-height: 100vh;
  background-color: #ffffff;
  color: #1f2328;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
  padding: 40px 0;
}

.achievements-container.dark {
  background-color: #0d1117;
  color: #e6edf3;
}

.container-lg {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.achievements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  color: #1f2328;
}

.achievements-container.dark .section-title {
  color: #e6edf3;
}

.stat-item {
  font-size: 0.875rem;
  color: #656d76;
}

.achievements-container.dark .stat-item {
  color: #7d8590;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding: 40px;
}
</style>
