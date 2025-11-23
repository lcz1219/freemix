<template>
  <div class="mobile-statistics-container">
    <div class="apple-header">
      <van-icon name="arrow-left" class="back-icon" @click="goBack" />
      <h1 class="apple-title">统计数据</h1>
    </div>

    <div class="content-wrapper">
      <!-- 统计概览 -->
      <div class="apple-card stats-overview-card">
        <van-grid :column-num="2" :border="false" :gutter="12">
          <van-grid-item>
            <div class="stat-item total">
              <div class="stat-value">{{ totalGoals }}</div>
              <div class="stat-label">总目标数</div>
            </div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-item completed">
              <div class="stat-value">{{ completedGoals }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-item in-progress">
              <div class="stat-value">{{ inProgressGoals }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-item expired">
              <div class="stat-value">{{ expiredGoals }}</div>
              <div class="stat-label">已过期</div>
            </div>
          </van-grid-item>
        </van-grid>
      </div>

      <!-- 图表区域 -->
      <div class="apple-card chart-card">
        <div class="card-header">
          <van-icon name="pie-chart-o" />
          <span>目标状态分布</span>
        </div>
        <div ref="statusChart" class="chart-container"></div>
      </div>

      <div class="apple-card chart-card">
        <div class="card-header">
          <van-icon name="chart-trending-o" />
          <span>月度完成趋势</span>
        </div>
        <div ref="trendChart" class="chart-container"></div>
      </div>

      <div class="apple-card chart-card">
        <div class="card-header">
          <van-icon name="bar-chart-o" />
          <span>目标类型分布</span>
        </div>
        <div ref="typeChart" class="chart-container"></div>
      </div>

      <!-- 详细数据 -->
      <div class="apple-card details-card">
        <div class="card-header">
          <van-icon name="list-switch" />
          <span>详细数据</span>
        </div>
        <van-collapse v-model="activeNames" :border="false">
          <van-collapse-item title="最近完成的目标" name="recent">
            <div v-if="recentCompletedGoals.length > 0">
              <div v-for="goal in recentCompletedGoals" :key="goal.id" class="detail-item">
                <div class="item-info">
                  <div class="item-title">{{ goal.title }}</div>
                  <div class="item-owner">{{ goal.owner }}</div>
                </div>
                <div class="item-extra">
                  <div class="item-date">{{ formatDate(goal.completedAt) }}</div>
                  <van-tag type="success" plain>已完成</van-tag>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">暂无数据</div>
          </van-collapse-item>
          <van-collapse-item title="即将到期的目标" name="upcoming">
            <div v-if="upcomingGoals.length > 0">
              <div v-for="goal in upcomingGoals" :key="goal.id" class="detail-item">
                <div class="item-info">
                  <div class="item-title">{{ goal.title }}</div>
                  <div class="item-owner">{{ goal.owner }}</div>
                </div>
                <div class="item-extra">
                  <div class="item-date">{{ formatDate(goal.deadline) }}</div>
                  <van-tag type="warning" plain>进行中</van-tag>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">暂无数据</div>
          </van-collapse-item>
          <van-collapse-item title="已过期的目标" name="expired">
            <div v-if="expiredGoalsList.length > 0">
              <div v-for="goal in expiredGoalsList" :key="goal.id" class="detail-item">
                <div class="item-info">
                  <div class="item-title">{{ goal.title }}</div>
                  <div class="item-owner">{{ goal.owner }}</div>
                </div>
                <div class="item-extra">
                  <div class="item-date">{{ formatDate(goal.deadline) }}</div>
                  <van-tag type="danger" plain>已过期</van-tag>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">暂无数据</div>
          </van-collapse-item>
        </van-collapse>
      </div>

      <!-- 底部统计 -->
      <div class="apple-card bottom-stats-card">
        <div class="bottom-stat-item">
          <div class="bottom-stat-value">{{ completionRate }}%</div>
          <div class="bottom-stat-label">完成率</div>
        </div>
        <div class="bottom-stat-item">
          <div class="bottom-stat-value">{{ avgCompletionTime }}天</div>
          <div class="bottom-stat-label">平均完成时间</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/hooks/useUser'
import { useSettings } from '@/hooks/useSettings'
import { useGoals } from '@/hooks/useGoals'
import { showToast, showLoadingToast, closeToast } from 'vant'
import * as echarts from 'echarts/core'
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
const { user } = useUser()
const { isDark } = useSettings()

const currentTheme = computed(() => (isDark.value ? 'dark' : 'light'))

const goals = ref([])
const statistics = ref({})
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

const getChartTheme = () => {
  const isDarkValue = isDark.value
  return {
    backgroundColor: 'transparent',
    textStyle: {
      color: isDarkValue ? '#E0E0E0' : '#333'
    },
    legend: {
      textStyle: {
        color: isDarkValue ? '#B0B0B0' : '#666'
      }
    },
    xAxis: {
      axisLine: {
        lineStyle: {
          color: isDarkValue ? '#555' : '#ccc'
        }
      },
      axisLabel: {
        color: isDarkValue ? '#B0B0B0' : '#666'
      }
    },
    yAxis: {
      axisLine: {
        lineStyle: {
          color: isDarkValue ? '#555' : '#ccc'
        }
      },
      splitLine: {
        lineStyle: {
          color: isDarkValue ? '#333' : '#eee'
        }
      },
      axisLabel: {
        color: isDarkValue ? '#B0B0B0' : '#666'
      }
    }
  }
}

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
  const option = {
    ...getChartTheme(),
    tooltip: { trigger: 'item' },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: getChartTheme().legend.textStyle
    },
    series: [{
      name: '目标状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: isDark.value ? '#2c2c2e' : '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: {
          show: true,
          fontSize: '20',
          fontWeight: 'bold'
        }
      },
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
  const months = []
  const data = []
  const now = new Date()
  for (let i = 5; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${date.getMonth() + 1}月`)
    data.push(Math.floor(Math.random() * 20) + 5)
  }
  const option = {
    ...getChartTheme(),
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value' },
    series: [{
      name: '完成数量',
      type: 'line',
      data: data,
      smooth: true,
      itemStyle: { color: '#007AFF' },
      lineStyle: { width: 3 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
          offset: 0,
          color: 'rgba(0, 122, 255, 0.3)'
        }, {
          offset: 1,
          color: 'rgba(0, 122, 255, 0)'
        }])
      }
    }]
  }
  trendChartInstance.setOption(option)
}

const initTypeChart = () => {
  if (!typeChart.value) return
  typeChartInstance = echarts.init(typeChart.value)
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
    ...getChartTheme(),
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: types.length > 0 ? types : ['无标签'] },
    yAxis: { type: 'value' },
    series: [{
      name: '目标数量',
      type: 'bar',
      data: counts.length > 0 ? counts : [goals.value.length],
      itemStyle: {
        color: '#34C759',
        borderRadius: [5, 5, 0, 0]
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
    showLoadingToast({ message: '加载中...', forbidClick: true, duration: 0 })
    const goalsHook = useGoals()
    const [goalsResponse, statsResponse] = await Promise.all([
      goalsHook.getGoals(),
      goalsHook.getStatistics()
    ])
    goals.value = goalsResponse.data
    statistics.value = statsResponse.data
    initCharts()
  } catch (error) {
    showToast('加载失败，请重试')
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
  if (statusChartInstance) {
    statusChartInstance.dispose()
    statusChartInstance = null
  }
  if (trendChartInstance) {
    trendChartInstance.dispose()
    trendChartInstance = null
  }
  if (typeChartInstance) {
    typeChartInstance.dispose()
    typeChartInstance = null
  }
}
</script>

<style scoped>
:root {
  --apple-background-color: #f0f0f5;
  --apple-card-background: #ffffff;
  --apple-text-color: #000000;
  --apple-secondary-text-color: #8a8a8e;
  --apple-border-color: #e5e5ea;
  --apple-system-blue: #007aff;
}

[data-theme="dark"] {
  --apple-background-color: #000000;
  --apple-card-background: #1c1c1e;
  --apple-text-color: #ffffff;
  --apple-secondary-text-color: #8d8d93;
  --apple-border-color: #38383a;
}

.mobile-statistics-container {
  background-color: var(--apple-background-color);
  min-height: 100vh;
  color: var(--apple-text-color);
  padding-top: 50px;
}

.apple-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  background-color: var(--apple-card-background);
  border-bottom: 1px solid var(--apple-border-color);
  z-index: 10;
}

.back-icon {
  position: absolute;
  left: 16px;
  font-size: 20px;
  color: var(--apple-system-blue);
}

.apple-title {
  font-size: 17px;
  font-weight: 600;
}

.content-wrapper {
  padding: 16px;
}

.apple-card {
  background-color: var(--apple-card-background);
  border-radius: 12px;
  margin-bottom: 16px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: background-color 0.3s, box-shadow 0.3s;
}

[data-theme="dark"] .apple-card {
  box-shadow: none;
}

.stats-overview-card .van-grid {
  width: 100%;
}

.stats-overview-card .van-grid-item {
  background: none;
}

.stat-item {
  text-align: center;
  padding: 12px 0;
  border-radius: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--apple-secondary-text-color);
  margin-top: 4px;
}

.stat-item.total .stat-value { color: var(--apple-system-blue); }
.stat-item.completed .stat-value { color: #34C759; }
.stat-item.in-progress .stat-value { color: #FF9500; }
.stat-item.expired .stat-value { color: #FF3B30; }

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 16px;
}

.chart-container {
  height: 250px;
  width: 100%;
}

.details-card .van-collapse {
  background: transparent;
}

.details-card .van-collapse-item {
  background: transparent;
}

.details-card :deep(.van-collapse-item__content) {
  padding: 0;
  background: transparent;
}

.details-card :deep(.van-cell) {
  background: transparent;
  color: var(--apple-text-color);
  padding-left: 0;
  padding-right: 0;
}

.details-card :deep(.van-cell::after) {
  border-bottom-color: var(--apple-border-color);
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--apple-border-color);
}

.detail-item:last-child {
  border-bottom: none;
}

.item-info {
  flex-grow: 1;
}

.item-title {
  font-weight: 500;
  font-size: 15px;
}

.item-owner {
  font-size: 13px;
  color: var(--apple-secondary-text-color);
  margin-top: 2px;
}

.item-extra {
  text-align: right;
}

.item-date {
  font-size: 13px;
  color: var(--apple-secondary-text-color);
  margin-bottom: 4px;
}

.empty-state {
  padding: 20px;
  text-align: center;
  color: var(--apple-secondary-text-color);
}

.bottom-stats-card {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.bottom-stat-item {
  padding: 8px;
}

.bottom-stat-value {
  font-size: 22px;
  font-weight: 600;
  color: var(--apple-system-blue);
}

.bottom-stat-label {
  font-size: 13px;
  color: var(--apple-secondary-text-color);
  margin-top: 4px;
}
</style>
