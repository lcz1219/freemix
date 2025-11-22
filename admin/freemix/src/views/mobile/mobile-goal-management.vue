<template>
  <van-config-provider :theme="currentTheme">
    <div class="mobile-goal-management">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="目标管理"
        left-text="返回"
        left-arrow
        @click-left="goBack"
      >
        <template #right>
          <van-icon name="plus" size="18" @click="addNewGoal" />
        </template>
      </van-nav-bar>

      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section">
        <van-search
          v-model="searchQuery"
          placeholder="搜索目标..."
          @search="handleSearch"
          @clear="handleClear"
        />
        
        <van-dropdown-menu>
          <van-dropdown-item 
            v-model="statusFilter" 
            :options="statusOptions" 
            title="状态筛选"
            @change="handleFilterChange"
          />
        </van-dropdown-menu>
      </div>

      <!-- 目标列表 -->
      <div class="goals-list">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
          >
            <van-cell
              v-for="goal in filteredGoals"
              :key="goal.id"
              class="goal-item"
              @click="showGoalDetail(goal)"
              is-link
            >
              <template #title>
                <div class="goal-header">
                  <span class="goal-title">{{ goal.title }}</span>
                  <van-tag
                    :type="getGoalStatusType(goal.status)"
                    plain
                    size="small"
                  >
                    {{ getGoalStatusText(goal.status) }}
                  </van-tag>
                </div>
              </template>
              
              <template #label>
                <div class="goal-meta">
                  <div class="goal-deadline">
                    <van-icon name="calendar-o" size="14" />
                    <span>{{ formatDate(goal.deadline) }}</span>
                  </div>
                  <div class="goal-owner">
                    <van-icon name="contact" size="14" />
                    <span>{{ goal.owner }}</span>
                  </div>
                </div>
                
                <!-- 进度条 -->
                <div class="progress-section">
                  <van-progress
                    :percentage="goal.progress"
                    stroke-width="6"
                    :show-pivot="false"
                    color="linear-gradient(90deg, #81c683, #4b0082)"
                  />
                  <span class="progress-text">{{ goal.progress }}%</span>
                </div>
                
                <!-- 标签 -->
                <div class="goal-tags" v-if="goal.tags && goal.tags.length">
                  <van-tag
                    v-for="tag in goal.tags"
                    :key="tag"
                    :type="getTagType(tag)"
                    plain
                    size="small"
                    class="tag-item"
                  >
                    {{ tag }}
                  </van-tag>
                </div>
              </template>
            </van-cell>
          </van-list>
        </van-pull-refresh>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredGoals.length === 0 && !loading" class="empty-state">
        <van-empty description="暂无目标">
          <van-button type="primary" round @click="addNewGoal">
            添加第一个目标
          </van-button>
        </van-empty>
      </div>

      <!-- 悬浮操作按钮 -->
      <van-floating-bubble
        axis="xy"
        icon="plus"
        @click="addNewGoal"
      />
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
const currentTheme = computed(() => isDark.value ? 'dark' : 'light')

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

const getTagType = (tag) => {
  const tagMap = {
    '学习': 'success',
    '工作': 'primary',
    '生活': 'warning',
    '运动': 'danger'
  }
  return tagMap[tag] || 'default'
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
.mobile-goal-management {
  min-height: 100vh;
  background-color: var(--van-background-color);
}

.search-filter-section {
  padding: 8px 0;
  background-color: var(--van-background-color-light);
  position: sticky;
  top: 46px;
  z-index: 1;
}

.goals-list {
  padding: 0 12px;
}

.goal-item {
  margin-bottom: 8px;
  border-radius: 8px;
  background-color: var(--van-background-color-light);
}

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.goal-title {
  font-weight: 600;
  font-size: 16px;
  color: var(--van-text-color);
}

.goal-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--van-text-color-2);
}

.goal-deadline,
.goal-owner {
  display: flex;
  align-items: center;
  gap: 4px;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.progress-text {
  font-size: 12px;
  color: var(--van-text-color-2);
  min-width: 36px;
}

.goal-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.tag-item {
  margin: 0;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

/* 深色主题适配 */
:deep(.van-theme-dark) {
  --van-background-color: #1a1a1a;
  --van-background-color-light: #2a2a2a;
  --van-text-color: #f5f5f5;
  --van-text-color-2: #a0a0a0;
}
</style>