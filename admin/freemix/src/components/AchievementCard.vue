<template>
  <div class="achievement-card" :class="{ 'unlocked': unlocked, 'dark': isDark }">
    <div class="achievement-icon">
      <n-icon :size="32" :component="flatIcon" v-if="flatIcon" />
      <span v-else>{{ icon }}</span>
    </div>
    <div class="achievement-info">
      <div class="achievement-header">
        <h3 class="achievement-title">{{ title }}</h3>
        <span v-if="unlocked" class="unlock-date">{{ formatDate(unlockedAt) }}</span>
      </div>
      <p class="achievement-desc">{{ description }}</p>
      <div class="progress-container" v-if="!unlocked && type === 'CUMULATIVE'">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: `${progressPercent}%` }"></div>
        </div>
        <span class="progress-text">{{ currentProgress }} / {{ conditionValue }}</span>
      </div>
    </div>
    <div class="achievement-status" v-if="unlocked">
      <n-icon size="20" color="#1a7f37"><CheckmarkCircle /></n-icon>
    </div>
    <div class="achievement-status" v-else>
      <n-icon size="20" color="#8c959f"><LockClosed /></n-icon>
    </div>
  </div>
</template>

<script setup>
import { computed, inject, ref } from 'vue';
import { NIcon } from 'naive-ui';
import { 
  CheckmarkCircle, 
  LockClosed,
  LeafOutline,
  TrophyOutline,
  LogoInstagram,
  MedalOutline,
  PeopleOutline,
  MoonOutline,
  SunnyOutline,
  FlameOutline,
  SparklesOutline,
  RibbonOutline,
  FootstepsOutline
} from '@vicons/ionicons5';

const isDark = inject('isDark', ref(false));

const props = defineProps({
  id: String,
  title: String,
  description: String,
  icon: String,
  unlocked: Boolean,
  unlockedAt: String,
  currentProgress: Number,
  conditionValue: Number,
  type: String
});

const iconMapping = {
  'first_step': FootstepsOutline,
  'first_win': TrophyOutline,
  'goal_hunter': LogoInstagram,
  'goal_master': MedalOutline,
  'social_butterfly': PeopleOutline,
  'night_owl': MoonOutline,
  'weekend_warrior': SunnyOutline,
  'consistency': FlameOutline,
  'perfectionist': SparklesOutline
};

const flatIcon = computed(() => {
  return iconMapping[props.id] || null;
});

const progressPercent = computed(() => {
  if (props.conditionValue === 0) return 0;
  return Math.min(100, (props.currentProgress / props.conditionValue) * 100);
});

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN');
};
</script>

<style scoped>
.achievement-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  background: #f6f8fa;
  gap: 16px;
  transition: all 0.3s ease;
  opacity: 0.7;
}

.achievement-card.unlocked {
  background: #ffffff;
  border-color: #1a7f37;
  opacity: 1;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.achievement-icon {
  font-size: 2.5rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border-radius: 50%;
  border: 1px solid #d0d7de;
  color: #656d76;
}

.unlocked .achievement-icon {
  border-color: #1a7f37;
  background: #dafbe1;
  color: #1a7f37;
}

.achievement-info {
  flex: 1;
}

.achievement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.achievement-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #1f2328;
}

.achievement-desc {
  margin: 0;
  font-size: 0.875rem;
  color: #656d76;
}

.unlock-date {
  font-size: 0.75rem;
  color: #1a7f37;
  font-weight: 500;
}

.progress-container {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #d0d7de;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #1a7f37;
  border-radius: 3px;
}

.progress-text {
  font-size: 0.75rem;
  color: #656d76;
}

/* Dark mode support */
.achievement-card.dark {
  background: #161b22;
  border-color: #30363d;
}

.achievement-card.dark.unlocked {
  background: #0d1117;
  border-color: #3fb950;
}

.achievement-card.dark .achievement-icon {
  background: #0d1117;
  border-color: #30363d;
}

.achievement-card.dark.unlocked .achievement-icon {
  background: #050905;
  border-color: #3fb950;
}

.achievement-card.dark .achievement-title {
  color: #e6edf3;
}

.achievement-card.dark .achievement-desc {
  color: #7d8590;
}

.achievement-card.dark .progress-bar {
  background: #30363d;
}

.achievement-card.dark .progress-fill {
  background: #3fb950;
}
</style>
