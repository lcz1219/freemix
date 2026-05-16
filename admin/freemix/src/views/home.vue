<template>
  <div :class="isDark ? 'home-container' : 'home-container-light'" class="main-content-wrapper">
    <!-- 全局快捷搜索框 -->
    <div class="global-search-container">
      <n-input-group>
        <n-input
          round
          placeholder="直接问 AI 帮你寻找目标或制定计划... (Alt + Enter)"
          class="cmd-k-input"
          v-model:value="aiSearchInput"
          @keyup.alt.enter="handleAiSearch"
        >
        </n-input>
      </n-input-group>
    </div>

    <div class="main-content">
        <!-- 头部区域：欢迎语与操作 -->
        <header class="bento-header">
          <div class="header-info">
            <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">你好, {{ userInfo?.username || '目标追踪者' }}</h1>
            <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">掌控你的目标，衡量你的成功</p>
          </div>
          <div class="header-actions">
            <n-button secondary type="primary" round strong @click="addGoal">
              <template #icon>
                <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6V13z"/></svg></n-icon>
              </template>
              添加新目标
            </n-button>
            <n-button ghost strong round @click="store.commit('setAiDrawer', true)">
              <template #icon>
                <n-icon size="24"><AIAssistantIcon /></n-icon>
              </template>
              AI助手
            </n-button>
          </div>
        </header>

        <StatsOverview :total-goals="totalGoals" :completed-goals="completedGoals" :in-progress-goals="inProgressGoals"
          :expired-goals="expiredGoals" />
<!-- <n-grid-item span="12"> -->
            <div class="quick-actions-grid">
              <n-card 
                :class="isDark ? 'feature-card' : 'feature-card-light'" 
                class="bento-card action-card clickable-card"
                @click="showRecentGoalsModal = true"
              >
                <div class="action-card-content">
                  <div class="action-icon recent-icon">
                    <n-icon size="24"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M13 3a9 9 0 0 0-9 9H1l3.89 3.89l.07.14L9 12H6a7 7 0 1 1 7 7a7.07 7.07 0 0 1-6-3.18l-1.42 1.42A8.9 8.9 0 0 0 13 21a9 9 0 0 0 0-18zm-1 5v5l4.25 2.52l.77-1.28l-3.52-2.09V8z"/></svg></n-icon>
                  </div>
                  <div class="action-info">
                    <div class="action-title">近期目标</div>
                    <div class="action-desc">查看最近需要完成的任务进度</div>
                  </div>
                  <n-icon size="20" class="arrow-icon"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M8.59 16.59L13.17 12L8.59 7.41L10 6l6 6l-6 6l-1.41-1.41z"/></svg></n-icon>
                </div>
              </n-card>
            </div>
          <!-- </n-grid-item> -->
        <!-- Bento Grid 布局区域 -->
        <n-grid :cols="12" :x-gap="20" :y-gap="20" item-responsive responsive="screen" style="margin-top: 1.5rem;">
          <!-- 左侧：AI 智能洞察 (占 4/12) -->
          <n-grid-item span="12 m:4">
            <AIGeneratedInsights />
          </n-grid-item>

          <!-- 右侧：完成趋势 (占 8/12) -->
          <n-grid-item span="12 m:8">
            <n-card :class="isDark ? 'feature-card' : 'feature-card-light'" class="bento-card">
              <div class="card-header">
                <div ref="trendChartIcon" class="echart-icon"></div>
                <h2 class="card-title">完成趋势</h2>
              </div>
              <div class="chart-container" style="height: 21.875rem;">
                <canvas ref="trendChart"></canvas>
              </div>
            </n-card>
          </n-grid-item>

          <!-- 下方：快捷操作入口 (占 12/12) -->
          
        </n-grid>

        <!-- 近期目标弹窗 -->
        <n-modal v-model:show="showRecentGoalsModal" preset="card" style="width: 800px; max-width: 95vw; border-radius: 20px;" class="recent-goals-modal">
          <template #header>
            <div class="modal-header-custom">
              <n-icon size="24" color="#00c9a7"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M13 3a9 9 0 0 0-9 9H1l3.89 3.89l.07.14L9 12H6a7 7 0 1 1 7 7a7.07 7.07 0 0 1-6-3.18l-1.42 1.42A8.9 8.9 0 0 0 13 21a9 9 0 0 0 0-18zm-1 5v5l4.25 2.52l.77-1.28l-3.52-2.09V8z"/></svg></n-icon>
              <span>近期目标回顾</span>
            </div>
          </template>
          <RecentGoals :goals="goals" :formatDate="formatDate" :checktype="checktype" />
        </n-modal>

        <GoalDetail v-model:show="showDetailModal" :goal="selectedGoal" @save="saveGoal" @updateGoal="getGoals" />
      </div>
    </div>
</template>

<script setup>

import { ref, onMounted, inject, computed, watch, h } from 'vue';
import common from '@/views/common.vue';
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NButton,
  NIcon,
  NSwitch,
  NCard,
  NTimeline,
  NTimelineItem,
  NTag,
  NProgress,
  NAvatar,
  NModal,
  NSteps,
  NStep,
  useMessage,
  NMenu,
  NImage,
  NAlert,
  NTabs,
  NDataTable,
  NTabPane,
  NGrid,
  NGridItem,
  NSpace,
  NCollapse,
  NCollapseItem,
  NTable,
  NPagination,
  NDrawer,
  NDrawerContent,
  NInput,
  NInputGroup
} from 'naive-ui';
import Chart from 'chart.js/auto';
import * as echarts from 'echarts/core';
import { PieChart, LineChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent
} from 'echarts/components';
import StatsOverview from '@/components/StatsOverview.vue';
import RecentGoals from '@/components/RecentGoals.vue';
import AIGeneratedInsights from '@/components/AIGeneratedInsights.vue';
import { CanvasRenderer } from 'echarts/renderers';
import { ArrowRedoSharp } from '@vicons/ionicons5';
import AIAssistantIcon from '@/components/icons/AIAssistantIcon.vue';
import { useRouter } from 'vue-router'
import { getMPaths, isSuccess } from '@/utils/request'
import { useStore } from 'vuex'
import GoalDetail from '@/components/GoalDetail.vue';
import { useUser } from '@/hooks/useUser';
import { useSettings } from '@/hooks/useSettings';

const isDark = inject('isDark', ref(true))
const store = useStore();
const router = useRouter();
const { userInfo } = useUser();
const { themeSettings } = useSettings();

const trendChart = ref(null);
const trendChartIcon = ref(null);
const aiSearchInput = ref('');
const goals = ref([]);
const showDetailModal = ref(false);
const showRecentGoalsModal = ref(false);
const selectedGoal = ref({});
const getGoalsMoth = ref([]);

const pagination = { pageSize: 5 };

const columns = [
  { title: '目标名称', key: 'title', sorter: 'default' },
  { title: '负责人', key: 'owner', sorter: 'default' },
  {
    title: '截止日期',
    key: 'deadlineString',
    sorter: 'default',
    render(row) { return h('div', {}, formatDate(row.deadline)); }
  },
  {
    title: '进度',
    key: 'progress',
    sorter: 'default',
    render(row) {
      return h(NProgress, {
        type: 'line',
        percentage: row.progress,
        indicatorPlacement: 'inside',
        processing: row.status === 'in-progress',
        color: row.status === 'completed' ? '#00c9a7' : row.status === 'expired' ? '#ff6b6b' : '#00c9a7'
      });
    }
  },
  {
    title: '状态',
    key: 'status',
    sorter: 'default',
    render(row) {
      const statusMap = {
        'in-progress': { label: '进行中', type: 'info' },
        'completed': { label: '已完成', type: 'success' },
        'expired': { label: '已过期', type: 'error' }
      };
      const statusInfo = statusMap[row.status] || { label: '未知', type: 'default' };
      return h(NTag, { type: statusInfo.type, size: 'small' }, { default: () => statusInfo.label });
    }
  }
];

const formatDate = (dateStr) => {
  if (!dateStr) return '未设置';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '日期无效';
  return `${date.getFullYear()}年${(date.getMonth() + 1).toString().padStart(2, '0')}月${date.getDate().toString().padStart(2, '0')}日`;
};

const getGoals = async () => {
  const res = await getMPaths("getGoals", userInfo.value.username, "正在获取目标数据...");
  if (isSuccess(res)) {
    goals.value = res.data.data;
    goals.value?.forEach(goal => { goal.deadlineString = formatDate(goal.deadline); });
    getGoalsMoth.value = filterMoth();
  }
};

const saveGoal = (updatedGoal) => {
  const index = goals.value.findIndex(g => g.id === updatedGoal.id);
  if (index !== -1) goals.value[index] = { ...updatedGoal };
};

const addGoal = () => router.push('/add-goal');

const totalGoals = computed(() => goals.value.length);
const completedGoals = computed(() => goals.value.filter(g => g.status === 'completed').length);
const inProgressGoals = computed(() => goals.value.filter(g => g.status === 'in-progress').length);
const expiredGoals = computed(() => goals.value.filter(g => g.status === 'expired').length);

const checktype = (val) => {
  const tag = val.tags?.[0];
  if (tag === '学习') return 'success';
  if (tag === '工作') return 'info';
  if (tag === '生活') return 'warning';
  if (tag === '运动') return 'error';
  return 'default';
};

const goalFinishCount = computed(() => goals.value.filter(g => g.progress === 100).length);
const goalExpireCount = computed(() => goals.value.filter(g => Date.now() - Date.parse(g.deadline) > 0).length);
const goalIngCount = computed(() => goals.value.filter(g => g.progress !== 100 && Date.now() - Date.parse(g.deadline) < 0).length);

echarts.use([PieChart, LineChart, GridComponent, TooltipComponent, LegendComponent, GraphicComponent, CanvasRenderer]);

const handleAiSearch = () => {
  if (!aiSearchInput.value.trim()) return;
  store.commit('setAiDrawer', true);
  store.commit('setAiInputContent', aiSearchInput.value);
  aiSearchInput.value = '';
};

const mothList = ref(new Array(12).fill(0));
const filterMoth = () => {
  const comp = new Array(12).fill(0);
  const total = new Array(12).fill(0);
  const currentYear = new Date().getFullYear();

  goals.value.forEach(g => {
    if (!g.deadline) return;
    const date = new Date(g.deadline);
    if (date.getFullYear() !== currentYear) return;
    const m = date.getMonth();
    if (g.status === 'completed') comp[m]++;
    total[m]++;
  });

  return comp.map((c, i) => total[i] === 0 ? null : (c / total[i] * 100));
};

onMounted(async () => {
  await getGoals();
  
  const trendChartIconInstance = echarts.init(trendChartIcon.value);
  trendChartIconInstance.setOption({
    xAxis: { type: 'category', boundaryGap: false, data: ['M', 'T', 'W', 'T', 'F', 'S', 'S'], axisLine: { show: false }, axisTick: { show: false }, axisLabel: { show: false } },
    yAxis: { type: 'value', splitLine: { show: false }, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { show: false } },
    series: [{
      data: [820, 932, 901, 934, 1290, 1330, 1320],
      type: 'line',
      smooth: true,
      lineStyle: { color: '#00c9a7', width: 2 },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0, 201, 167, 0.3)' }, { offset: 1, color: 'rgba(0, 201, 167, 0)' }]) },
      symbol: 'none'
    }],
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    tooltip: { show: false }
  });

  new Chart(trendChart.value, {
    type: 'line',
    data: {
      labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      datasets: [{
        label: '目标完成率',
        data: getGoalsMoth.value,
        borderColor: '#00c9a7',
        backgroundColor: 'rgba(0, 201, 167, 0.1)',
        tension: 0.4,
        fill: true,
        spanGaps: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        x: { grid: { display: false }, ticks: { color: 'grey' } },
        y: { min: 0, max: 100, grid: { color: isDark.value ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)' }, ticks: { color: 'grey', callback: (v) => v + '%' } }
      }
    }
  });
});
</script>

<style scoped>
.home-container {
  background-color: #0f0f13;
  color: #ffffff;
  min-height: 100%;
}

.home-container-light {
  background-color: #f5f5f7;
  color: #000000;
  min-height: 100%;
}

.main-content-wrapper {
  height: 100%;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: clamp(1rem, 2.5vw, 2.5rem);
}

.global-search-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 1rem clamp(1rem, 2vw, 1.5rem);
  position: sticky;
  top: 0.75rem;
  z-index: 100;
  transition: all 0.3s ease;
}

.home-container-light .global-search-container {
  background: linear-gradient(to bottom, #f5f5f7 0%, rgba(245, 245, 247, 0) 100%);
}

.cmd-k-input {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.home-container-light .cmd-k-input {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(0, 0, 0, 0.1) !important;
}

/* 快捷操作卡片样式 */
.quick-actions-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.25rem;
}

.clickable-card {
  cursor: pointer;
}

.action-card-content {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem;
}

.action-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recent-icon {
  background: rgba(0, 201, 167, 0.15);
  color: #00c9a7;
}

.action-info {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 2px;
}

.action-desc {
  font-size: 13px;
  opacity: 0.6;
}

.arrow-icon {
  opacity: 0.3;
  transition: transform 0.3s ease;
}

.action-card:hover .arrow-icon {
  transform: translateX(4px);
  opacity: 0.8;
}

.modal-header-custom {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 18px;
}

.recent-goals-modal {
  background: rgba(20, 20, 25, 0.95) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.bento-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 2rem;
  padding: 0 0.625rem;
}

.hero-title {
  font-size: clamp(1.5rem, 4vw, 2rem);
  font-weight: 800;
  background: linear-gradient(to right, #fff, #00c9a7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.hero-title-light {
  font-size: clamp(1.5rem, 4vw, 2rem);
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 0.5rem;
}

.hero-subtitle {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.6);
}

.hero-subtitle-light {
  font-size: 1rem;
  color: rgba(0, 0, 0, 0.6);
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.bento-card {
  border-radius: 1.25rem !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  /* background: rgba(30, 30, 42, 0.4) !important; */
  backdrop-filter: blur(20px);
  box-shadow: 0 0.5rem 2rem rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.home-container-light .bento-card {
  background: rgba(255, 255, 255, 0.7) !important;
  border: 1px solid rgba(0, 0, 0, 0.05) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.bento-card:hover {
  transform: translateY(-4px);
  border-color: rgba(0, 201, 167, 0.3) !important;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
}

.card-headerstatic {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
}

.echart-icon {
  width: 1.75rem;
  height: 1.75rem;
}

.chart-container {
  width: 100%;
}

@media (max-width: 900px) {
  .bento-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
}
</style>
