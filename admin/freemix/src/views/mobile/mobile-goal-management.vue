<template>
  <van-config-provider :theme="currentTheme" class="apple-style-provider">
    <div class="mobile-goal-management">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="目标管理"
        left-arrow
        @click-left="goBack"
        class="apple-nav-bar"
      >
        <template #left>
          <van-icon name="arrow-left" size="18" color="var(--van-text-color)" />
        </template>
        <template #right>
          <van-button
            round
            size="small"
            class="apple-gradient-button"
            @click="addNewGoal"
          >
            <van-icon name="plus" style="margin-right: 4px;" />
            新目标
          </van-button>
        </template>
      </van-nav-bar>

      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section">
        <van-search
          v-model="searchQuery"
          placeholder="搜索目标标题或描述..."
          @search="handleSearch"
          @clear="handleClear"
          shape="round"
          background="transparent"
        />
        <van-dropdown-menu class="apple-dropdown-menu">
          <van-dropdown-item
            v-model="statusFilter"
            :options="statusOptions"
            @change="handleFilterChange"
          />
        </van-dropdown-menu>
      </div>

      <!-- 目标列表 -->
      <div class="goals-list-container">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="apple-pull-refresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="已经到底了 ✨"
            @load="onLoad"
            :immediate-check="false"
          >
            <div v-for="goal in filteredGoals" :key="goal.id" class="apple-card-wrapper">
              <div class="apple-card goal-card" @click="showGoalDetail(goal)">
                <div class="card-header">
                  <h4 class="goal-title">{{ goal.title }}</h4>
                  <van-tag :type="getGoalStatusType(goal.status)" round>
                    {{ getGoalStatusText(goal.status) }}
                  </van-tag>
                </div>
                <div class="card-body">
                  <div class="goal-meta">
                    <span><van-icon name="calendar-o" /> {{ formatDate(goal.deadline) }}</span>
                    <span><van-icon name="user-o" /> {{ goal.owner }}</span>
                  </div>
                  <div class="progress-section">
                    <van-progress
                      :percentage="goal.progress"
                      stroke-width="8"
                      :show-pivot="false"
                      color="var(--apple-gradient-color)"
                      track-color="var(--van-background-color)"
                    />
                    <span class="progress-text">{{ goal.progress }}%</span>
                  </div>
                  <div class="goal-tags" v-if="goal.tags && goal.tags.length">
                    <van-tag
                      v-for="tag in goal.tags"
                      :key="tag"
                      plain
                      round
                      :color="getTagColor(tag)"
                      class="tag-item"
                    >
                      {{ tag }}
                    </van-tag>
                  </div>
                </div>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && filteredGoals.length === 0" class="empty-state">
        <van-empty description="还没有任何目标哦">
          <van-button
            round
            class="apple-gradient-button"
            @click="addNewGoal"
          >
            <van-icon name="plus" style="margin-right: 4px;" />
            创建第一个目标
          </van-button>
        </van-empty>
      </div>

    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/hooks/useUser'
import { useGoals } from '@/hooks/useGoals'
import { useSettings } from '@/hooks/useSettings'
import { showToast, showLoadingToast, closeToast } from 'vant'

// 路由和状态管理
const router = useRouter()
const { user } = useUser()
const { isDark } = useSettings()
const goalsHook = useGoals()

// 主题配置

// 响应式数据
const goals = ref([])
const searchQuery = ref('')
const statusFilter = ref('')
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 筛选选项
const statusOptions = [
  { text: '全部', value: '' },
  { text: '进行中', value: 'in-progress' },
  { text: '已完成', value: 'completed' },
  { text: '已过期', value: 'expired' }
]

// 计算属性
const filteredGoals = computed(() => {
  let result = goals.value

  // 搜索筛选
  if (searchQuery.value) {
    result = result.filter(goal => 
      goal.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      goal.description?.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(goal => goal.status === statusFilter.value)
  }

  return result
})

// 方法
const goBack = () => {
  router.back()
}

const addNewGoal = () => {
  router.push('/add-goal')
}

const showGoalDetail = (goal) => {
  // 跳转到目标详情页面或显示详情弹窗
  router.push(`/goal-detail/${goal.id}`)
}

const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const getGoalStatusType = (status) => {
  const statusMap = {
    'in-progress': 'primary',
    'completed': 'success',
    'expired': 'danger'
  }
  return statusMap[status] || 'default'
}

const getGoalStatusText = (status) => {
  const statusMap = {
    'in-progress': '进行中',
    'completed': '已完成',
    'expired': '已过期'
  }
  return statusMap[status] || '未知'
}

const getTagColor = (tag) => {
  const tagColorMap = {
    '学习': '#4CAF50',
    '工作': '#2196F3',
    '生活': '#FF9800',
    '运动': '#f44336',
  }
  return tagColorMap[tag] || '#757575'
}

// 数据加载方法
const loadGoals = async (isRefresh = false) => {
  try {
    if (isRefresh) {
      page.value = 1
      finished.value = false
    }

    const response = await useGoals.getGoals({
      page: page.value,
      pageSize: pageSize.value,
      search: searchQuery.value,
      status: statusFilter.value
    })

    if (isRefresh) {
      goals.value = response.data
    } else {
      goals.value.push(...response.data)
    }

    // 判断是否加载完成
    if (response.data.length < pageSize.value) {
      finished.value = true
    } else {
      page.value++
    }
  } catch (error) {
    showToast('加载失败，请重试')
    console.error('加载目标失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 事件处理
const onLoad = () => {
  loadGoals()
}

const onRefresh = () => {
  refreshing.value = true
  loadGoals(true)
}

const handleSearch = (value) => {
  loadGoals(true)
}

const handleClear = () => {
  searchQuery.value = ''
  loadGoals(true)
}

const handleFilterChange = () => {
  loadGoals(true)
}

// 生命周期
onMounted(() => {
  loadGoals(true)
})
</script>

<style scoped>
/* 全局和根元素 */
.apple-style-provider {
  --van-background-color: #f7f8fa;
  --van-background-color-light: #ffffff;
  --van-text-color: #333;
  --van-text-color-2: #666;
  --van-border-color: #ebedf0;
  --apple-gradient-color: linear-gradient(135deg, #00c9a7, #00a98f);
}

.van-theme-dark .apple-style-provider {
  --van-background-color: #1c1c1e;
  --van-background-color-light: #2c2c2e;
  --van-text-color: #f5f5f7;
  --van-text-color-2: #a9a9b0;
  --van-border-color: #3a3a3c;
}

.mobile-goal-management {
  min-height: 100vh;
  background-color: var(--van-background-color);
  color: var(--van-text-color);
}

/* 导航栏 */
.apple-nav-bar {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--van-border-color);
  position: sticky;
  top: 0;
  z-index: 10;
}

.van-theme-dark .apple-nav-bar {
  background-color: rgba(28, 28, 30, 0.7);
}

:deep(.van-nav-bar__title) {
  font-weight: 600;
  color: var(--van-text-color);
}

/* 渐变按钮 */
.apple-gradient-button {
  border: none;
  color: white;
  font-weight: 500;
  background: var(--apple-gradient-color);
  transition: all 0.3s ease;
}

.apple-gradient-button:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 搜索和筛选 */
.search-filter-section {
  padding: 8px 16px;
  background-color: var(--van-background-color-light);
  border-bottom: 1px solid var(--van-border-color);
  position: sticky;
  top: 46px; /* 与 van-nav-bar 高度一致 */
  z-index: 9;
  display: flex;
  gap: 8px;
  align-items: center;
}

:deep(.van-search) {
  flex-grow: 1;
  padding: 0;
}

.apple-dropdown-menu {
  flex-shrink: 0;
  width: 100px;
  height: 34px;
  border-radius: 17px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

:deep(.van-dropdown-menu__bar) {
  height: 100%;
  background-color: var(--van-background-color);
}

/* 列表和卡片 */
.goals-list-container {
  padding: 16px;
}

.apple-card-wrapper {
  margin-bottom: 16px;
}

.apple-card {
  background-color: var(--van-background-color-light);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 16px;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.apple-card:active {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.van-theme-dark .apple-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.goal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--van-text-color);
  margin: 0;
}

.card-body .goal-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--van-text-color-2);
  margin-bottom: 12px;
}

.goal-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.progress-text {
  font-size: 12px;
  font-weight: 500;
  color: var(--van-text-color-2);
}

.goal-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-item {
  padding: 2px 8px;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.apple-pull-refresh {
  min-height: calc(100vh - 200px); /* 估算值，确保可滚动 */
}
</style>