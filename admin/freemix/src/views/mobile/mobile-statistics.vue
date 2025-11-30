<template>
  <div class="mobile-statistics-container" :data-theme="currentTheme">
    <!-- 顶部导航栏 (毛玻璃效果) -->
    <div class="apple-nav-bar">
      <div class="nav-left" @click="goBack">
        <van-icon name="chevron-left" size="24" color="#007AFF" />
        <van-icon name="arrow-left" /> 
      </div>
      <div class="nav-title">数据概览</div>
      <div class="nav-right"></div>
    </div>

    <div class="content-scroll-wrapper">
      <!-- 大标题区域 -->
      <div class="page-header">
        <h1>统计</h1>
        <p class="subtitle">{{ formatDate(new Date()) }}</p>
      </div>

      <!-- 统计概览 (Bento 风格) -->
      <div class="stats-grid">
        <div class="stat-card primary">
          <div class="stat-icon-bg"><van-icon name="flag-o" /></div>
          <div class="stat-info">
            <div class="stat-label">总目标</div>
            <div class="stat-value">{{ totalGoals }}</div>
          </div>
        </div>
        <div class="stat-card success">
          <div class="stat-icon-bg"><van-icon name="passed" /></div>
          <div class="stat-info">
            <div class="stat-label">已完成</div>
            <div class="stat-value">{{ completedGoals }}</div>
          </div>
        </div>
        <div class="stat-card warning">
          <div class="stat-icon-bg"><van-icon name="clock-o" /></div>
          <div class="stat-info">
            <div class="stat-label">进行中</div>
            <div class="stat-value">{{ inProgressGoals }}</div>
          </div>
        </div>
        <div class="stat-card danger">
          <div class="stat-icon-bg"><van-icon name="warning-o" /></div>
          <div class="stat-info">
            <div class="stat-label">已过期</div>
            <div class="stat-value">{{ expiredGoals }}</div>
          </div>
        </div>
      </div>

      <!-- 底部关键指标 -->
      <div class="apple-group-card summary-card">
        <div class="summary-item">
          <div class="ring-container">
             <!-- 简单的 CSS 圆环模拟完成率 -->
            <svg viewBox="0 0 36 36" class="circular-chart">
              <path class="circle-bg" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="circle" :stroke-dasharray="`${completionRate}, 100`" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
            </svg>
            <span class="ring-text">{{ completionRate }}<small>%</small></span>
          </div>
          <span class="summary-label">完成率</span>
        </div>
        <div class="divider-vertical"></div>
        <div class="summary-item">
          <div class="summary-big-num">{{ avgCompletionTime }}<small>天</small></div>
          <span class="summary-label">平均耗时</span>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="chart-section-title">分析图表</div>
      
      <div class="apple-group-card">
        <div class="chart-header">
          <span class="chart-title">状态分布</span>
        </div>
        <div ref="statusChart" class="chart-box"></div>
      </div>

      <div class="apple-group-card">
        <div class="chart-header">
          <span class="chart-title">趋势分析</span>
        </div>
        <div ref="trendChart" class="chart-box"></div>
      </div>

      <div class="apple-group-card">
        <div class="chart-header">
          <span class="chart-title">类型偏好</span>
        </div>
        <div ref="typeChart" class="chart-box"></div>
      </div>

      <!-- 详细列表 -->
      <div class="chart-section-title">详细记录</div>
      <div class="apple-group-card list-card">
        <van-collapse v-model="activeNames" :border="false" accordion>
          
          <van-collapse-item name="recent" :border="false">
            <template #title>
              <div class="collapse-header">
                <div class="icon-box green"><van-icon name="checked" /></div>
                <span>最近完成</span>
              </div>
            </template>
            <div class="list-content">
              <div v-if="recentCompletedGoals.length > 0">
                <div v-for="goal in recentCompletedGoals" :key="goal.id" class="list-row">
                  <div class="row-main">
                    <div class="row-title">{{ goal.title }}</div>
                    <div class="row-sub">{{ formatDate(goal.completedAt) }}</div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-text">暂无记录</div>
            </div>
          </van-collapse-item>

          <van-collapse-item name="upcoming" :border="false">
            <template #title>
               <div class="collapse-header">
                <div class="icon-box orange"><van-icon name="underway-o" /></div>
                <span>即将到期</span>
              </div>
            </template>
            <div class="list-content">
              <div v-if="upcomingGoals.length > 0">
                <div v-for="goal in upcomingGoals" :key="goal.id" class="list-row">
                  <div class="row-main">
                    <div class="row-title">{{ goal.title }}</div>
                    <div class="row-sub">截止: {{ formatDate(goal.deadline) }}</div>
                  </div>
                   <van-tag color="#FF9500" plain round>急</van-tag>
                </div>
              </div>
              <div v-else class="empty-text">无即将到期目标</div>
            </div>
          </van-collapse-item>

          <van-collapse-item name="expired" :border="false">
            <template #title>
               <div class="collapse-header">
                <div class="icon-box red"><van-icon name="clear" /></div>
                <span>已过期</span>
              </div>
            </template>
            <div class="list-content">
              <div v-if="expiredGoalsList.length > 0">
                <div v-for="goal in expiredGoalsList" :key="goal.id" class="list-row">
                  <div class="row-main">
                    <div class="row-title">{{ goal.title }}</div>
                    <div class="row-sub">截止: {{ formatDate(goal.deadline) }}</div>
                  </div>
                  <van-tag color="#FF3B30" plain round>过期</van-tag>
                </div>
              </div>
              <div v-else class="empty-text">无过期目标</div>
            </div>
          </van-collapse-item>

        </van-collapse>
      </div>
      
      <!-- 底部留白 -->
      <div class="bottom-spacer"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/hooks/useUser'
import { useSettings } from '@/hooks/useSettings'
import { showToast, showLoadingToast, closeToast } from 'vant'
import * as echarts from 'echarts/core'
import { postM, getMPaths } from '@/utils/request'
import { PieChart, LineChart, BarChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  PieChart,
  LineChart,
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  CanvasRenderer
])

const router = useRouter()
const { user, userInfo } = useUser()
const { isDark } = useSettings()

// 保持原有逻辑

const goals = ref([])
const loading = ref(false)
const activeNames = ref(['recent'])

const statusChart = ref(null)
const trendChart = ref(null)
const typeChart = ref(null)

let statusChartInstance = null
let trendChartInstance = null
let typeChartInstance = null

const totalGoals = computed(() => goals.value.length)
const completedGoals = computed(() => goals.value.filter(g => g.status === 'completed').length)
const inProgressGoals = computed(() => goals.value.filter(g => g.status === 'in-progress').length)
const expiredGoals = computed(() => goals.value.filter(g => g.status === 'expired').length)

const completionRate = computed(() => {
  if (totalGoals.value === 0) return 0
  return Math.round((completedGoals.value / totalGoals.value) * 100)
})

const avgCompletionTime = computed(() => {
  const completedGoalsWithTime = goals.value.filter(g => 
    g.status === 'completed' && g.completedAt && g.createdAt
  )
  if (completedGoalsWithTime.length === 0) return 0
  const totalDays = completedGoalsWithTime.reduce((sum, goal) => {
    const created = new Date(goal.createdAt)
    const completed = new Date(goal.completedAt)
    return sum + Math.ceil((completed - created) / (1000 * 60 * 60 * 24))
  }, 0)
  return Math.round(totalDays / completedGoalsWithTime.length)
})

const recentCompletedGoals = computed(() => {
  return goals.value
    .filter(g => g.status === 'completed')
    .sort((a, b) => new Date(b.completedAt) - new Date(a.completedAt))
    .slice(0, 5)
})

const upcomingGoals = computed(() => {
  const now = new Date()
  const sevenDaysLater = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
  return goals.value
    .filter(g => 
      g.status === 'in-progress' && 
      new Date(g.deadline) <= sevenDaysLater
    )
    .sort((a, b) => new Date(a.deadline) - new Date(b.deadline))
    .slice(0, 5)
})

const expiredGoalsList = computed(() => {
  return goals.value
    .filter(g => g.status === 'expired')
    .sort((a, b) => new Date(b.deadline) - new Date(a.deadline))
    .slice(0, 5)
})

const goBack = () => {
  router.back()
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`
}

// 定义更高级的图表主题配置


const initCharts = () => {
  nextTick(() => {
    initStatusChart()
    initTrendChart()
    initTypeChart()
  })
}

const initStatusChart = () => {
  if (!statusChart.value) return
  statusChartInstance = echarts.init(statusChart.value)
  const theme = getChartTheme()
  const option = {
    ...theme,
    tooltip: { trigger: 'item', backgroundColor:  'rgba(50,50,50,0.9)' , borderWidth: 0, textStyle: { color:  '#fff' } },
    series: [{
      name: '目标状态',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      itemStyle: {
        borderRadius: 8,
        borderColor: '#1C1C1E' ,
        borderWidth: 3
      },
      label: { show: false },
      data: [
        { value: completedGoals.value, name: '已完成', itemStyle: { color: '#34C759' } },
        { value: inProgressGoals.value, name: '进行中', itemStyle: { color: '#007AFF' } },
        { value: expiredGoals.value, name: '已过期', itemStyle: { color: '#FF3B30' } }
      ]
    }]
  }
  statusChartInstance.setOption(option)
}

const initTrendChart = () => {
  if (!trendChart.value) return
  trendChartInstance = echarts.init(trendChart.value)
  const theme = getChartTheme()
  const months = []
  const data = []
  const now = new Date()
  for (let i = 5; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${date.getMonth() + 1}月`)
    data.push(Math.floor(Math.random() * 15) + 2) // 模拟数据保持不变
  }
  const option = {
    ...theme,
    tooltip: { trigger: 'axis' },
    xAxis: { ...theme.xAxis, data: months },
    yAxis: { ...theme.yAxis },
    series: [{
      name: '完成数量',
      type: 'line',
      data: data,
      smooth: true,
      showSymbol: false,
      itemStyle: { color: '#007AFF' },
      lineStyle: { width: 3, cap: 'round' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
          offset: 0,
          color: 'rgba(0, 122, 255, 0.2)'
        }, {
          offset: 1,
          color: 'rgba(0, 122, 255, 0)'
        }])
      }
    }]
  }
  trendChartInstance.setOption(option)
}
const getChartTheme = () => {
  const isDarkValue = true
  const textColor = isDarkValue ? '#98989D' : '#8E8E93'
  const axisColor = isDarkValue ? '#38383A' : '#E5E5EA'
  
  return {
    backgroundColor: 'transparent',
    textStyle: { fontFamily: '-apple-system, BlinkMacSystemFont, sans-serif' },
    grid: { top: 30, right: 10, bottom: 20, left: 10, containLabel: true },
    legend: {
      bottom: 0,
      itemWidth: 10,
      itemHeight: 10,
      icon: 'circle',
      textStyle: { color: textColor, fontSize: 12 }
    },
    xAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: textColor, fontSize: 11, interval: 0 },
      splitLine: { show: false }
    },
    yAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false }, 
      splitLine: { 
        show: true, 
        lineStyle: { color: axisColor, type: 'dashed', width: 0.5 } 
      }
    }
  }
}

const initTypeChart = () => {
  if (!typeChart.value) return
  typeChartInstance = echarts.init(typeChart.value)
  const theme = getChartTheme()
  const typeCount = {}
  goals.value.forEach(goal => {
    if (goal.tags && goal.tags.length > 0) {
      goal.tags.forEach(tag => {
        typeCount[tag] = (typeCount[tag] || 0) + 1
      })
    }
  })
  const types = Object.keys(typeCount)
  const counts = Object.values(typeCount)
  
  const option = {
    ...theme,
    tooltip: { trigger: 'axis' },
    grid: { ...theme.grid, bottom: 20 },
    xAxis: { ...theme.xAxis, data: types.length > 0 ? types : ['无'], axisLabel: { ...theme.xAxis.axisLabel, interval: 0 } },
    yAxis: { ...theme.yAxis },
    series: [{
      name: '目标数量',
      type: 'bar',
      barWidth: 16,
      data: counts.length > 0 ? counts : [goals.value.length],
      itemStyle: {
        color: '#5856D6',
        borderRadius: [4, 4, 4, 4]
      }
    }]
  }
  typeChartInstance.setOption(option)
}

const resizeCharts = () => {
  if (statusChartInstance) statusChartInstance.resize()
  if (trendChartInstance) trendChartInstance.resize()
  if (typeChartInstance) typeChartInstance.resize()
}

const loadData = async () => {
  try {
    loading.value = true
    showLoadingToast({ message: '同步数据...', forbidClick: true, duration: 0 })
    const response = await getMPaths("getGoals", userInfo.value.username, "正在获取目标数据...");
    goals.value = response.data.data || []
    initCharts()
  } catch (error) {
    showToast('加载失败')
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
    closeToast()
  }
}

watch(isDark, () => {
  cleanupCharts()
  initCharts()
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  cleanupCharts()
  window.removeEventListener('resize', resizeCharts)
})

const cleanupCharts = () => {
  if (statusChartInstance) { statusChartInstance.dispose(); statusChartInstance = null }
  if (trendChartInstance) { trendChartInstance.dispose(); trendChartInstance = null }
  if (typeChartInstance) { typeChartInstance.dispose(); typeChartInstance = null }
}
</script>

<style scoped lang="scss">
/* --- Apple Design System Variables --- */
:root {
  --ios-bg: #F2F2F7;
  --ios-card: #FFFFFF;
  --ios-text-primary: #000000;
  --ios-text-secondary: #8E8E93;
  --ios-text-tertiary: #C7C7CC;
  --ios-separator: #E5E5EA;
  --ios-blue: #007AFF;
  --ios-green: #34C759;
  --ios-red: #FF3B30;
  --ios-orange: #FF9500;
  --ios-purple: #5856D6;
}

[data-theme="dark"] {
  --ios-bg: #000000;
  --ios-card: #1C1C1E;
  --ios-text-primary: #FFFFFF;
  --ios-text-secondary: #98989D;
  --ios-text-tertiary: #48484A;
  --ios-separator: #38383A;
}

/* --- Container & Layout --- */
.mobile-statistics-container {
  background-color: var(--ios-bg);
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  color: var(--ios-text-primary);
  -webkit-font-smoothing: antialiased;
}

.content-scroll-wrapper {
  padding: 64px 16px 40px 16px; /* Space for fixed header */
}

/* --- Navigation Bar (Translucent) --- */
.apple-nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  padding-top: env(safe-area-inset-top); /* Adapt to iPhone Notch */
  height: calc(44px + env(safe-area-inset-top));
  display: flex;
  align-items: flex-end; /* Align items to bottom */
  justify-content: space-between;
  padding-bottom: 10px;
  padding-left: 16px;
  padding-right: 16px;
  background: rgba(var(--ios-card), 0.8); /* Fallback */
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  background-color: transparent; /* Let blur do the work */
  z-index: 100;
  /* border-bottom: 0.5px solid var(--ios-separator); Optional */
}

[data-theme="light"] .apple-nav-bar { background: rgba(var(--ios-card), 0.8); }
[data-theme="dark"] .apple-nav-bar { background: rgba(28,28,30,0.75); }

.nav-left {
  display: flex;
  align-items: center;
  color: var(--ios-blue);
  cursor: pointer;
  z-index: 2;
  .nav-back-text { font-size: 17px; margin-left: -4px; }
}

.nav-title {
  position: absolute;
  left: 0; right: 0; bottom: 10px;
  text-align: center;
  font-weight: 600;
  font-size: 17px;
  pointer-events: none;
}

/* --- Page Header --- */
.page-header {
  margin-top: 20px;
  margin-bottom: 24px;
  h1 { font-size: 34px; font-weight: 700; margin: 0; letter-spacing: 0.3px; }
  .subtitle { font-size: 13px; color: var(--ios-text-secondary); margin-top: 4px; text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; }
}

/* --- Bento Grid Stats --- */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--ios-card);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  
  .stat-icon-bg {
    width: 32px; height: 32px; border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    font-size: 18px;
    margin-bottom: 8px;
  }
  
  .stat-label { font-size: 13px; color: var(--ios-text-secondary); font-weight: 500; }
  .stat-value { font-size: 24px; font-weight: 700; line-height: 1.1; }
  
  &.primary { .stat-icon-bg { background: rgba(0,122,255,0.1); color: var(--ios-blue); } .stat-value { color: var(--ios-blue); } }
  &.success { .stat-icon-bg { background: rgba(52,199,89,0.1); color: var(--ios-green); } .stat-value { color: var(--ios-green); } }
  &.warning { .stat-icon-bg { background: rgba(255,149,0,0.1); color: var(--ios-orange); } .stat-value { color: var(--ios-orange); } }
  &.danger  { .stat-icon-bg { background: rgba(255,59,48,0.1);  color: var(--ios-red); }  .stat-value { color: var(--ios-red); } }
}

/* --- Group Cards (Inset Grouped Style) --- */
.apple-group-card {
  background: var(--ios-card);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  /* box-shadow: 0 4px 12px rgba(0,0,0,0.04); */
}

.chart-section-title {
  margin: 24px 0 10px 4px;
  font-size: 20px;
  font-weight: 700;
  color: var(--ios-text-primary);
}

.chart-header {
  margin-bottom: 12px;
  .chart-title { font-size: 15px; font-weight: 600; }
}

.chart-box { height: 220px; width: 100%; }

/* --- Summary Footer Widget --- */
.summary-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 24px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.divider-vertical {
  width: 1px; height: 40px; background: var(--ios-separator);
}

.summary-label {
  margin-top: 8px; font-size: 12px; color: var(--ios-text-secondary); font-weight: 500;
}

.summary-big-num {
  font-size: 28px; font-weight: 700;
  small { font-size: 14px; font-weight: 500; color: var(--ios-text-secondary); margin-left: 2px; }
}

/* CSS Ring Chart */
.ring-container {
  position: relative; width: 48px; height: 48px;
}
.circular-chart { display: block; margin: 0 auto; max-width: 100%; max-height: 100%; transform: rotate(-90deg); }
.circle-bg { fill: none; stroke: var(--ios-separator); stroke-width: 3.5; }
.circle { fill: none; stroke: var(--ios-blue); stroke-width: 3.5; stroke-linecap: round; transition: stroke-dasharray 0.6s ease; }
.ring-text {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  font-size: 14px; font-weight: 700; color: var(--ios-text-primary);
  small { font-size: 9px; }
}

/* --- List Styles --- */
.list-card {
  padding: 0; /* Let collapse items handle padding */
  overflow: hidden;
}

.collapse-header {
  display: flex; align-items: center;
  span { font-size: 16px; font-weight: 500; margin-left: 10px; color: var(--ios-text-primary); }
}

.icon-box {
  width: 28px; height: 28px; border-radius: 6px; display: flex; align-items: center; justify-content: center; color: white;
  &.green { background: var(--ios-green); }
  &.orange { background: var(--ios-orange); }
  &.red { background: var(--ios-red); }
}

.list-content {
  padding: 0 16px 16px 54px; /* Indent to align with text */
}

.list-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0;
  border-bottom: 0.5px solid var(--ios-separator);
  &:last-child { border-bottom: none; }
}

.row-main {
  .row-title { font-size: 15px; font-weight: 500; margin-bottom: 2px; }
  .row-sub { font-size: 12px; color: var(--ios-text-secondary); }
}

.empty-text {
  font-size: 13px; color: var(--ios-text-tertiary); font-style: italic; padding: 10px 0;
}

.bottom-spacer { height: 40px; }
</style>