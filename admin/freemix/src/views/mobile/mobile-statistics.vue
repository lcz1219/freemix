<template>
  <van-config-provider :theme="currentTheme">
    <div class="mobile-statistics">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="统计数据"
        left-text="返回"
        left-arrow
        @click-left="goBack"
      />

      <!-- 统计概览卡片 -->
      <div class="stats-overview">
        <van-grid :column-num="2" :border="false" :gutter="12">
          <van-grid-item>
            <van-card class="stat-card total">
              <template #title>
                <div class="stat-title">总目标数</div>
              </template>
              <template #desc>
                <div class="stat-value">{{ totalGoals }}</div>
              </template>
            </van-card>
          </van-grid-item>
          
          <van-grid-item>
            <van-card class="stat-card completed">
              <template #title>
                <div class="stat-title">已完成</div>
              </template>
              <template #desc>
                <div class="stat-value">{{ completedGoals }}</div>
              </template>
            </van-card>
          </van-grid-item>
          
          <van-grid-item>
            <van-card class="stat-card in-progress">
              <template #title>
                <div class="stat-title">进行中</div>
              </template>
              <template #desc>
                <div class="stat-value">{{ inProgressGoals }}</div>
              </template>
            </van-card>
          </van-grid-item>
          
          <van-grid-item>
            <van-card class="stat-card expired">
              <template #title>
                <div class="stat-title">已过期</div>
              </template>
              <template #desc>
                <div class="stat-value">{{ expiredGoals }}</div>
              </template>
            </van-card>
          </van-grid-item>
        </van-grid>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section">
        <!-- 目标状态分布 -->
        <van-card class="chart-card">
          <template #title>
            <div class="chart-title">
              <van-icon name="pie-chart-o" />
              <span>目标状态分布</span>
            </div>
          </template>
          <template #default>
            <div ref="statusChart" class="chart-container"></div>
          </template>
        </van-card>

        <!-- 月度完成趋势 -->
        <van-card class="chart-card">
          <template #title>
            <div class="chart-title">
              <van-icon name="chart-trending-o" />
              <span>月度完成趋势</span>
            </div>
          </template>
          <template #default>
            <div ref="trendChart" class="chart-container"></div>
          </template>
        </van-card>

        <!-- 目标类型分布 -->
        <van-card class="chart-card">
          <template #title>
            <div class="chart-title">
              <van-icon name="bar-chart-o" />
              <span>目标类型分布</span>
            </div>
          </template>
          <template #default>
            <div ref="typeChart" class="chart-container"></div>
          </template>
        </van-card>
      </div>

      <!-- 详细数据列表 -->
      <div class="details-section">
        <van-card>
          <template #title>
            <div class="section-title">
              <van-icon name="list-switch" />
              <span>详细数据</span>
            </div>
          </template>
          
          <van-collapse v-model="activeNames">
            <van-collapse-item title="最近完成的目标" name="recent">
              <van-list>
                <van-cell
                  v-for="goal in recentCompletedGoals"
                  :key="goal.id"
                  :title="goal.title"
                  :value="formatDate(goal.completedAt)"
                  :label="goal.owner"
                >
                  <template #right-icon>
                    <van-tag type="success" size="small">已完成</van-tag>
                  </template>
                </van-cell>
              </van-list>
            </van-collapse-item>
            
            <van-collapse-item title="即将到期的目标" name="upcoming">
              <van-list>
                <van-cell
                  v-for="goal in upcomingGoals"
                  :key="goal.id"
                  :title="goal.title"
                  :value="formatDate(goal.deadline)"
                  :label="goal.owner"
                >
                  <template #right-icon>
                    <van-tag type="warning" size="small">进行中</van-tag>
                  </template>
                </van-cell>
              </van-list>
            </van-collapse-item>
            
            <van-collapse-item title="已过期的目标" name="expired">
              <van-list>
                <van-cell
                  v-for="goal in expiredGoalsList"
                  :key="goal.id"
                  :title="goal.title"
                  :value="formatDate(goal.deadline)"
                  :label="goal.owner"
                >
                  <template #right-icon>
                    <van-tag type="danger" size="small">已过期</van-tag>
                  </template>
                </van-cell>
              </van-list>
            </van-collapse-item>
          </van-collapse>
        </van-card>
      </div>

      <!-- 底部统计信息 -->
      <div class="bottom-stats">
        <van-notice-bar
          left-icon="info-o"
          :scrollable="false"
          background="var(--van-primary-color)"
          color="white"
        >
          完成率: {{ completionRate }}% | 平均完成时间: {{ avgCompletionTime }}天
        </van-notice-bar>
      </div>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted, nextTick } from 'vue'
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
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册 ECharts 组件
echarts.use([
  PieChart,
  LineChart,
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
])

// 路由和状态管理
const router = useRouter()
const { user } = useUser()
const { isDark } = useSettings()

// 主题配置

// 响应式数据
const goals = ref([])
const statistics = ref({})
const loading = ref(false)
const activeNames = ref(['recent'])

// 图表引用
const statusChart = ref(null)
const trendChart = ref(null)
const typeChart = ref(null)

// 图表实例
let statusChartInstance = null
let trendChartInstance = null
let typeChartInstance = null

// 计算属性
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

// 方法
const goBack = () => {
  router.back()
}

const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    initStatusChart()
    initTrendChart()
    initTypeChart()
  })
}

// 初始化状态分布饼图
const initStatusChart = () => {
  if (!statusChart.value) return
  
  statusChartInstance = echarts.init(statusChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      top: 'center'
    },
    series: [
      {
        name: '目标状态',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: completedGoals.value, name: '已完成', itemStyle: { color: '#00c9a7' } },
          { value: inProgressGoals.value, name: '进行中', itemStyle: { color: '#81c683' } },
          { value: expiredGoals.value, name: '已过期', itemStyle: { color: '#ff6b6b' } }
        ]
      }
    ]
  }
  
  statusChartInstance.setOption(option)
}

// 初始化月度趋势折线图
const initTrendChart = () => {
  if (!trendChart.value) return
  
  trendChartInstance = echarts.init(trendChart.value)
  
  // 生成近6个月的模拟数据
  const months = []
  const data = []
  const now = new Date()
  
  for (let i = 5; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${date.getMonth() + 1}月`)
    data.push(Math.floor(Math.random() * 10) + 2)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: months
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '完成数量',
        type: 'line',
        data: data,
        smooth: true,
        itemStyle: {
          color: '#4682b4'
        }
      }
    ]
  }
  
  trendChartInstance.setOption(option)
}

// 初始化类型分布柱状图
const initTypeChart = () => {
  if (!typeChart.value) return
  
  typeChartInstance = echarts.init(typeChart.value)
  
  // 统计各类型目标数量
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
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: types.length > 0 ? types : ['无标签']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '目标数量',
        type: 'bar',
        data: counts.length > 0 ? counts : [goals.value.length],
        itemStyle: {
          color: '#00c9a7'
        }
      }
    ]
  }
  
  typeChartInstance.setOption(option)
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    showLoadingToast('加载中...')
    
    // 修复: useGoals 是一个 hook，应该这样调用
    const goalsHook = useGoals()
    
    const [goalsResponse, statsResponse] = await Promise.all([
      goalsHook.getGoals(),
      goalsHook.getStatistics()
    ])
    
    goals.value = goalsResponse.data
    statistics.value = statsResponse.data
    
    // 初始化图表
    initCharts()
    
  } catch (error) {
    showToast('加载失败，请重试')
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
    closeToast()
  }
}

// 生命周期
onMounted(() => {
  loadData()
})

// 清理图表实例
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

// 组件卸载时清理图表
import { onUnmounted } from 'vue'
onUnmounted(() => {
  cleanupCharts()
})
</script>

<style scoped>
.mobile-statistics {
  min-height: 100vh;
  background-color: var(--van-background-color);
  padding-bottom: 20px;
}

.stats-overview {
  padding: 12px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
}

.stat-card.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.completed {
  background: linear-gradient(135deg, #00c9a7 0%, #00b894 100%);
}

.stat-card.in-progress {
  background: linear-gradient(135deg, #81c683 0%, #4b0082 100%);
}

.stat-card.expired {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
}

.stat-title {
  color: white;
  font-size: 14px;
  font-weight: 500;
}

.stat-value {
  color: white;
  font-size: 28px;
  font-weight: bold;
  margin-top: 8px;
}

.charts-section {
  padding: 0 12px;
  margin-bottom: 16px;
}

.chart-card {
  margin-bottom: 12px;
  border-radius: 8px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.details-section {
  padding: 0 12px;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.bottom-stats {
  padding: 0 12px;
}

/* 深色主题适配 */
:deep(.van-theme-dark) {
  --van-background-color: #1a1a1a;
  --van-background-color-light: #2a2a2a;
  --van-text-color: #f5f5f5;
  --van-text-color-2: #a0a0a0;
}

:deep(.van-card) {
  background-color: var(--van-background-color-light);
}

:deep(.van-collapse-item__content) {
  background-color: var(--van-background-color-light);
}
</style>