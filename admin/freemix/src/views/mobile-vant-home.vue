<template>
  <van-config-provider :theme="currentTheme">
    <div class="mobile-home">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="FreeMix"
        right-text="设置"
        @click-right="goToSettings"
      >
        <template #left>
          <van-icon 
            :name="isDark ? 'sun-o' : 'moon-o'" 
            size="18" 
            @click="toggleTheme" 
          />
        </template>
      </van-nav-bar>
      
      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <h1>欢迎使用 FreeMix</h1>
        <p>您的个人目标管理系统</p>
        <van-button 
          type="primary" 
          round 
          @click="goToAddGoal"
          class="add-goal-btn"
        >
          添加新目标
        </van-button>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stats-section">
        <van-grid :column-num="3" :border="false">
          <van-grid-item>
            <div class="stat-card">
              <van-icon name="underway-o" size="24" color="#00c9a7" />
              <div class="stat-info">
                <div class="stat-number">{{ goalIngCount }}</div>
                <div class="stat-label">进行中</div>
              </div>
            </div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-card">
              <van-icon name="checked" size="24" color="#00c9a7" />
              <div class="stat-info">
                <div class="stat-number">{{ goalFinishCount }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-card">
              <van-icon name="fail" size="24" color="#00c9a7" />
              <div class="stat-info">
                <div class="stat-number">{{ goalExpireCount }}</div>
                <div class="stat-label">已过期</div>
              </div>
            </div>
          </van-grid-item>
        </van-grid>
      </div>
      
      <!-- 目标列表 -->
      <div class="goals-section">
        <van-tabs v-model:active="activeTab" sticky>
          <van-tab title="全部">
            <div v-if="goals.length === 0" class="empty-state">
              <van-empty description="暂无目标">
                <van-button 
                  type="primary" 
                  round 
                  @click="goToAddGoal"
                >
                  添加第一个目标
                </van-button>
              </van-empty>
            </div>
            <div v-else>
              <van-list>
                <van-cell 
                  v-for="goal in goals" 
                  :key="goal.id"
                  @click="showGoalDetail(goal)"
                  is-link
                >
                  <template #title>
                    <div class="goal-title">{{ goal.title }}</div>
                  </template>
                  <template #label>
                    <div class="goal-meta">
                      <span>{{ formatDate(goal.deadline) }} 截止</span>
                      <van-tag 
                        :type="getGoalStatusType(goal.status)" 
                        plain
                        class="status-tag"
                      >
                        {{ getGoalStatusText(goal.status) }}
                      </van-tag>
                    </div>
                  </template>
                  <template #right-icon>
                    <van-icon name="arrow" />
                  </template>
                </van-cell>
              </van-list>
            </div>
          </van-tab>
          <van-tab title="进行中">
            <div>进行中的目标列表</div>
          </van-tab>
          <van-tab title="已完成">
            <div>已完成的目标列表</div>
          </van-tab>
        </van-tabs>
      </div>
      
      <!-- 底部导航栏 -->
      <van-tabbar v-model="activeTabbar" route>
        <van-tabbar-item replace to="/mobile/home" icon="home-o">首页</van-tabbar-item>
        <van-tabbar-item replace to="/mobile/goals" icon="apps-o">目标</van-tabbar-item>
        <van-tabbar-item replace to="/mobile/profile" icon="user-o">我的</van-tabbar-item>
      </van-tabbar>
      
      <!-- 目标详情弹窗 -->
      <van-popup 
        v-model:show="showDetailModal" 
        position="bottom" 
        :style="{ height: '80%' }"
        round
      >
        <div class="goal-detail">
          <van-nav-bar
            title="目标详情"
            left-text="返回"
            @click-left="showDetailModal = false"
          />
          <div class="detail-content">
            <h2>{{ selectedGoal?.title }}</h2>
            <p>{{ selectedGoal?.description }}</p>
            <van-divider />
            <div class="sub-goals">
              <h3>子目标</h3>
              <van-checkbox-group v-model="checkedSubGoals">
                <van-cell-group>
                  <van-cell 
                    v-for="subGoal in selectedGoal?.subGoals" 
                    :key="subGoal.id"
                    clickable
                    @click="toggleSubGoal(subGoal.id)"
                  >
                    <template #title>
                      <van-checkbox 
                        :name="subGoal.id" 
                        @click.stop
                        checked-color="#00c9a7"
                      >
                        {{ subGoal.title }}
                      </van-checkbox>
                    </template>
                  </van-cell>
                </van-cell-group>
              </van-checkbox-group>
            </div>
          </div>
        </div>
      </van-popup>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { postM, getMPaths } from '@/utils/request'

// 主题相关
const isDark = ref(false)
const currentTheme = computed(() => isDark.value ? 'dark' : 'light')

// 路由
const router = useRouter()

// 数据状态
const activeTab = ref(0)
const activeTabbar = ref(0)
const goals = ref([])
const showDetailModal = ref(false)
const selectedGoal = ref(null)
const checkedSubGoals = ref([])

// 统计数据
const goalFinishCount = ref(0)
const goalExpireCount = ref(0)
const goalIngCount = ref(0)

// 初始化
onMounted(async () => {
  await fetchGoals()
})

// 获取目标列表
const fetchGoals = async () => {
  try {
    const response = await postM('/api/goal/list', {})
    goals.value = response.data
    // 计算统计数据
    goalFinishCount.value = goals.value.filter(g => g.status === 'finished').length
    goalExpireCount.value = goals.value.filter(g => g.status === 'expired').length
    goalIngCount.value = goals.value.filter(g => g.status === 'ongoing').length
  } catch (error) {
    console.error('获取目标列表失败:', error)
    showToast('获取目标列表失败')
  }
}

// 切换主题
const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.setAttribute(
    'data-theme', 
    isDark.value ? 'dark' : 'light'
  )
}

// 跳转到设置页面
const goToSettings = () => {
  router.push('/mobile/settings')
}

// 跳转到添加目标页面
const goToAddGoal = () => {
  router.push('/mobile/add-goal')
}

// 显示目标详情
const showGoalDetail = (goal) => {
  selectedGoal.value = goal
  checkedSubGoals.value = goal.subGoals
    .filter(sub => sub.completed)
    .map(sub => sub.id)
  showDetailModal.value = true
}

// 切换子目标状态
const toggleSubGoal = (id) => {
  const index = checkedSubGoals.value.indexOf(id)
  if (index !== -1) {
    checkedSubGoals.value.splice(index, 1)
  } else {
    checkedSubGoals.value.push(id)
  }
  // 这里应该调用API更新子目标状态
  finishChildGoal(id)
}

// 完成子目标
const finishChildGoal = async (childId) => {
  try {
    await getMPaths(`/api/goal/finish/${childId}`)
    showToast('子目标状态更新成功')
  } catch (error) {
    console.error('更新子目标状态失败:', error)
    showToast('更新子目标状态失败')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getMonth() + 1}-${date.getDate()}`
}

// 获取目标状态类型
const getGoalStatusType = (status) => {
  switch (status) {
    case 'ongoing': return 'primary'
    case 'finished': return 'success'
    case 'expired': return 'danger'
    default: return 'default'
  }
}

// 获取目标状态文本
const getGoalStatusText = (status) => {
  switch (status) {
    case 'ongoing': return '进行中'
    case 'finished': return '已完成'
    case 'expired': return '已过期'
    default: return '未知'
  }
}
</script>

<style scoped>
.mobile-home {
  min-height: 100vh;
  background-color: var(--van-background-color);
  padding-bottom: 50px;
}

.welcome-section {
  padding: 20px;
  text-align: center;
  background: linear-gradient(135deg, var(--van-background-color-light), var(--van-background-color-lighter));
}

.welcome-section h1 {
  color: var(--van-text-color);
  margin-bottom: 8px;
}

.welcome-section p {
  color: var(--van-text-color-2);
  margin-bottom: 20px;
}

.add-goal-btn {
  background-color: #00c9a7 !important;
  border-color: #00c9a7 !important;
}

.stats-section {
  padding: 15px;
  background-color: var(--van-background-color-light);
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}

.stat-info {
  text-align: center;
  margin-top: 8px;
}

.stat-number {
  font-size: 20px;
  font-weight: bold;
  color: var(--van-text-color);
}

.stat-label {
  font-size: 12px;
  color: var(--van-text-color-3);
}

.goals-section {
  padding: 0 10px;
}

.goal-title {
  font-weight: 500;
  color: var(--van-text-color);
}

.goal-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.goal-meta span {
  font-size: 12px;
  color: var(--van-text-color-3);
}

.status-tag {
  font-size: 10px;
}

.detail-content {
  padding: 15px;
}

.detail-content h2 {
  color: var(--van-text-color);
  margin-bottom: 10px;
}

.detail-content p {
  color: var(--van-text-color-2);
  line-height: 1.6;
  margin-bottom: 20px;
}

.sub-goals h3 {
  color: var(--van-text-color);
  margin-bottom: 15px;
}

.empty-state {
  padding: 50px 20px;
  text-align: center;
}

/* 深色主题适配 */
.dark .mobile-home {
  background-color: #121212;
}

.dark .welcome-section {
  background: linear-gradient(135deg, #1e1e1e, #2d2d2d);
}

/* 浅色主题适配 */
.light .mobile-home {
  background-color: #f5f5f5;
}

.light .welcome-section {
  background: linear-gradient(135deg, #ffffff, #f0f0f0);
}
</style>