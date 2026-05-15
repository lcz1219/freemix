<template>
  <n-card :class="isDark ? 'feature-card' : 'feature-card-light'" class="bento-card ai-insights-card">
    <div class="card-header-ai">
      <div class="header-left">
        <div class="ai-icon-wrapper">
          <n-icon size="22" color="#a855f7">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2L4.5 20.29L5.21 21L12 18L18.79 21L19.5 20.29L12 2Z"/>
            </svg>
          </n-icon>
        </div>
        <h2 class="card-title-ai">AI 智能洞察</h2>
      </div>
      <n-button quaternary circle size="small" @click="refreshInsights" class="refresh-btn">
        <template #icon>
          <n-icon :class="{ 'spinning': loading }">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M23 4v6h-6"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
            </svg>
          </n-icon>
        </template>
      </n-button>
    </div>

    <div class="insights-body">
      <div v-if="loading" class="skeleton-wrapper">
        <n-skeleton text :repeat="2" style="margin-bottom: 8px;" />
        <n-skeleton text style="width: 40%;" />
      </div>
      <div v-else class="insight-content-wrapper">
        <p :class="isDark ? 'insight-text' : 'insight-text-light'">
          {{ currentInsight }}
        </p>
        <div class="insight-footer">
          <n-button text type="primary" size="small" @click="showMoreInsights" class="more-btn">
            <template #icon>
              <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M16.17 11l-5.38-5.38L12.21 4.21L19 11l-6.79 6.79l-1.41-1.41L16.17 13H4v-2h12.17z"/></svg></n-icon>
            </template>
            查看更多建议
          </n-button>
        </div>
      </div>
    </div>
  </n-card>
</template>

<script setup>
import { ref, onMounted, computed, inject } from 'vue';
import { NCard, NIcon, NButton, NSkeleton } from 'naive-ui';

const isDark = inject('isDark', ref(true));
const loading = ref(true);
const insights = ref([
  "您有 3 个学习目标即将过期，建议优先处理，以避免影响整体进度。",
  "根据您的历史数据，本月在运动方面的投入不足，可以考虑调整计划，增加活动量。",
  "您的工作目标完成度普遍较高，可以考虑挑战更高难度或设定更具挑战性的新目标。",
  "近期您在生活类目标上表现突出，保持良好习惯，继续享受生活。",
  "发现您在团队协作方面有很大潜力，尝试在下一个团队目标中承担更多领导角色。",
  "您的目标分布较为均衡，但可以尝试在每个季度设定一个突破性目标，激发更大潜能。"
]);
const currentInsightIndex = ref(0);
const currentInsight = computed(() => insights.value[currentInsightIndex.value]);

const refreshInsights = () => {
  loading.value = true;
  setTimeout(() => {
    currentInsightIndex.value = (currentInsightIndex.value + 1) % insights.value.length;
    loading.value = false;
  }, 800);
};

const showMoreInsights = () => {
  console.log("查看更多 AI 建议");
};

onMounted(() => {
  setTimeout(() => {
    loading.value = false;
  }, 1000);
});
</script>

<style scoped>
.ai-insights-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: rgba(30, 30, 42, 0.4) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 20px !important;
}

.card-header-ai {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-icon-wrapper {
  width: 36px;
  height: 36px;
  background: rgba(168, 85, 247, 0.15);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-title-ai {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(to right, #fff, #d3c1ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.feature-card-light .card-title-ai {
  background: none;
  -webkit-text-fill-color: initial;
  color: #1a1a1a;
}

.refresh-btn {
  color: rgba(255, 255, 255, 0.4);
}

.refresh-btn:hover {
  color: #a855f7;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.insights-body {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.insight-content-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.insight-text {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  font-weight: 400;
}

.insight-text-light {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(0, 0, 0, 0.75);
  margin: 0;
}

.insight-footer {
  margin-top: 20px;
}

.more-btn {
  font-weight: 600;
  letter-spacing: 0.5px;
  opacity: 0.8;
  transition: all 0.3s ease;
}

.more-btn:hover {
  opacity: 1;
  transform: translateX(4px);
}

.skeleton-wrapper {
  padding-top: 4px;
}
</style>
