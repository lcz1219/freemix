<template>
  <van-config-provider :theme="currentTheme">
    <div class="app-container" :class="{ 'dark-mode': true }">

      <!-- 顶部导航栏 (毛玻璃效果) -->
      <van-nav-bar fixed placeholder class="glass-nav" :border="false" z-index="100" :safe-area-inset-top="true">
        <template #left>
          <div class="nav-brand">FreeMix</div>
        </template>
        <template #right>
          <div class="nav-actions">
            <!-- <div class="icon-btn" @click="toggleTheme">
              <van-icon :name="isDark ? 'sun-o' : 'moon-o'" size="20" />
            </div> -->
            <div class="icon-btn" @click="goToSettings">
              <van-icon name="setting-o" size="20" />
            </div>
          </div>
        </template>
      </van-nav-bar>

      <div class="content-wrapper">
        <!-- Hero 区域 -->
        <section class="hero-section">
          <div class="hero-card">
            <div class="hero-content">
              <div class="hero-text">
                <h1>早安，进击者</h1>
                <p>让每一个目标都清晰可见</p>
              </div>
              <div class="hero-decoration">
                <van-icon name="fire" />
              </div>
            </div>
            <div class="hero-actions">
              <van-button class="action-btn primary" icon="plus" block @click="goToAddGoal">
                新建目标
              </van-button>
              <van-button class="action-btn secondary" icon="chart-trending-o" block @click="goToStatistics">
                数据统计
              </van-button>
            </div>
          </div>
        </section>

        <!-- 快捷入口 -->
        <section class="quick-grid-section">
          <div class="grid-card">
            <div class="grid-item" @click="goToAddGoal">
              <div class="grid-icon-box blue"><van-icon name="flag-o" /></div>
              <span>目标</span>
            </div>
            <div class="grid-item" @click="goToStatistics">
              <div class="grid-icon-box green"><van-icon name="bar-chart-o" /></div>
              <span>统计</span>
            </div>
            <div class="grid-item" @click="goToMessageCenter">
              <div class="grid-icon-box orange"><van-icon name="chat-o" /></div>
              <span>消息</span>
            </div>
            <div class="grid-item" @click="goToAIAssistant">
              <div class="grid-icon-box teal"><van-icon name="smile-o" /></div>
              <span>AI助手</span>
            </div>
            <div class="grid-item" @click="goToGuide">
              <div class="grid-icon-box purple"><van-icon name="bulb-o" /></div>
              <span>指南</span>
            </div>
          </div>
        </section>

        <!-- 统计概览 -->
        <section class="stats-overview">
          <div class="section-header">概览</div>
          <div class="stats-row">
            <div class="stat-widget processing">
              <div class="widget-head">
                <van-icon name="play-circle-o" />
                <span>进行中</span>
              </div>
              <div class="widget-num">{{ goalIngCount }}</div>
              <van-progress :percentage="progressOngoing" :show-pivot="false" color="#4f8ef7"
                track-color="rgba(79,142,247,0.15)" stroke-width="4" />
            </div>
            <div class="stat-widget finished">
              <div class="widget-head">
                <van-icon name="checked" />
                <span>已完成</span>
              </div>
              <div class="widget-num">{{ goalFinishCount }}</div>
              <van-progress :percentage="progressFinished" :show-pivot="false" color="#00c9a7"
                track-color="rgba(0,201,167,0.15)" stroke-width="4" />
            </div>
            <div class="stat-widget expired">
              <div class="widget-head">
                <van-icon name="warning-o" />
                <span>已过期</span>
              </div>
              <div class="widget-num">{{ goalExpireCount }}</div>
              <van-progress :percentage="progressExpired" :show-pivot="false" color="#ff6b6b"
                track-color="rgba(255,107,107,0.15)" stroke-width="4" />
            </div>
          </div>
        </section>

        <!-- 目标列表 -->
        <section class="goals-container">
          <van-pull-refresh v-model="isRefreshing" @refresh="refreshGoals">
            <van-tabs v-model:active="activeTab" sticky offset-top="46" background="transparent" line-width="20px"
              line-height="3px" color="#00c9a7" title-active-color="#00c9a7"
              :title-inactive-color="isDark ? '#888' : '#666'" class="custom-tabs">
              <van-tab title="全部">
                <div v-if="isLoading" class="skeleton-list">
                  <van-skeleton title avatar :row="2" class="custom-skeleton" v-for="i in 3" :key="i" />
                </div>
                <div v-else>
                  <van-list v-model:loading="listLoading" :finished="listFinished" finished-text="没有更多了"
                    @load="loadMore">
                    <div class="goal-list-wrap">
                      <van-swipe-cell v-for="goal in goals" :key="goal.id" class="goal-card-swipe">
                        <div class="goal-card" @click="showGoalDetail(goal)">
                          <div class="card-status-line" :class="goal.status"></div>
                          <div class="card-main">
                            <div class="card-header">
                              <h3 class="card-title">{{ goal.title }}</h3>
                              <van-tag :type="getGoalStatusType(goal.status)" round class="status-pill">{{
                                getGoalStatusText(goal.status) }}</van-tag>
                            </div>
                            <div class="card-meta">
                              <span class="meta-item"><van-icon name="clock-o" /> {{ goal.deadlineString }}</span>
                              <span class="meta-item"><van-icon name="manager-o" /> {{ goal.owner }}</span>
                            </div>
                            <div class="card-progress">
                              <div class="progress-info">
                                <span>进度</span>
                                <span>{{ goalProgress(goal) }}%</span>
                              </div>
                              <van-progress :percentage="goalProgress(goal)" :show-pivot="false" stroke-width="6"
                                track-color="var(--bg-secondary)" />
                            </div>
                          </div>
                          <van-icon name="arrow" class="card-arrow" />
                        </div>
                        <template #right>
                          <div class="swipe-action-btn" @click="markGoalFinished(goal)">
                            <van-icon name="success" size="20" />
                          </div>
                        </template>
                      </van-swipe-cell>
                    </div>
                  </van-list>
                </div>
              </van-tab>

              <van-tab title="进行中">
                <!-- 复用逻辑，仅过滤显示 -->
                <div class="goal-list-wrap pt-2">
                  <van-empty v-if="goals.filter(g => g.status === 'in-progress').length === 0" description="暂无进行中的目标"
                    image="search" />
                  <div v-else class="goal-card-simple" v-for="goal in goals.filter(g => g.status === 'in-progress')"
                    :key="goal.id" @click="showGoalDetail(goal)">
                    <div class="simple-info">
                      <div class="title">{{ goal.title }}</div>
                      <div class="date">{{ formatDate(goal.deadline) }} 截止</div>
                    </div>
                    <van-icon name="arrow" color="#ccc" />
                  </div>
                </div>
              </van-tab>

              <van-tab title="已完成">
                <div class="goal-list-wrap pt-2">
                  <van-empty v-if="goals.filter(g => g.status === 'completed').length === 0" description="暂无已完成的目标"
                    image="search" />
                  <div v-else class="goal-card-simple finished"
                    v-for="goal in goals.filter(g => g.status === 'completed')" :key="goal.id"
                    @click="showGoalDetail(goal)">
                    <div class="simple-info">
                      <div class="title">{{ goal.title }}</div>
                      <div class="date">任务已完成</div>
                    </div>
                    <van-icon name="checked" color="#00c9a7" />
                  </div>
                </div>
              </van-tab>
            </van-tabs>
          </van-pull-refresh>
        </section>
      </div>

      <!-- 底部导航 -->
      <!-- <van-tabbar v-model="activeTabbar" route fixed border placeholder class="glass-tabbar" active-color="#00c9a7" :inactive-color="isDark ? '#666' : '#999'">
        <van-tabbar-item replace to="/home" icon="home-o">首页</van-tabbar-item>
        <van-tabbar-item replace to="/goals" icon="apps-o">目标</van-tabbar-item>
        <van-tabbar-item replace to="/profile" icon="user-o">我的</van-tabbar-item>
      </van-tabbar> -->

      <!-- 详情弹窗 (iOS Sheet 风格) -->
      <van-popup v-model:show="showDetailModal" position="bottom" round closeable class="detail-popup"
        :style="{ height: '85%' }">
        <div class="popup-wrapper">
          <div class="popup-header">
            <div class="popup-tag">
              <van-tag :type="getGoalStatusType(selectedGoal?.status)" size="medium" round>{{
                getGoalStatusText(selectedGoal?.status) }}</van-tag>
            </div>
            <h2 class="popup-title">{{ selectedGoal?.title }}</h2>
            <div class="popup-meta">
              <span><van-icon name="manager" /> {{ selectedGoal?.owner }}</span>
              <span><van-icon name="underway" /> {{ selectedGoal?.deadlineString }}</span>
            </div>
          </div>

          <div class="popup-body">
            <van-tabs v-model:active="detailTab" animated swipeable shrink background="transparent" color="#00c9a7">
              <van-tab title="任务详情">
                <div class="detail-content-box">
                  <div class="section-label">描述</div>
                  <div class="desc-text">{{ selectedGoal?.description || '暂无详细描述...' }}</div>

                  <div class="section-label mt-4">当前进度</div>
                  <div class="progress-circle-wrap">
                    <van-circle v-model:current-rate="constProgress" :rate="goalProgress(selectedGoal)"
                      :color="isDark ? '#00c9a7' : '#00c9a7'" :layer-color="isDark ? '#333' : '#f2f3f5'"
                      :text="goalProgress(selectedGoal) + '%'" size="120px" />
                  </div>
                </div>
              </van-tab>
              <van-tab title="子目标清单">
                <div class="subgoals-list">
                  <van-empty v-if="!(selectedGoal?.childGoals && selectedGoal.childGoals.length > 0)"
                    description="没有子任务" />
                  <van-checkbox-group v-else v-model="checkedSubGoals">
                    <van-cell-group inset>
                    <van-cell v-for="(subGoal, index) in (selectedGoal?.childGoals || [])" :key="subGoal._id"
                      class="subgoal-item">
                      <van-checkbox v-model="subGoal.finish" :name="subGoal._id" shape="round" checked-color="#00c9a7"
                        @click="()=>handleSubGoalChange(subGoal, index)" :disabled="isExp">
                        <span :class="{ 'text-crossed': checkedSubGoals.includes(subGoal._id) }">{{ subGoal.message
                          }}</span>
                      </van-checkbox>
                    </van-cell>
                    </van-cell-group>
                  </van-checkbox-group>
                </div>
              </van-tab>
            </van-tabs>
          </div>

          <div class="popup-footer">
            <van-button type="primary" block round color="linear-gradient(to right, #00c9a7, #00e0b0)" size="large"
              class="shadow-btn" @click="markGoalFinished(selectedGoal)" :disabled="isExp">
              标记为完成
            </van-button>
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
import { postM, getMPaths, isSuccess,baseURL } from '@/utils/request'
import { useUser } from '@/hooks'

const isDark = ref(false)
const currentTheme = ref('dark')

// 保持原有的 themeVars 逻辑，但颜色稍作调整以适应高级感
// const themeVars = computed(() => ({
//   primaryColor: '#00c9a7',
//   backgroundColor: isDark.value ? '#121212' : '#f7f8fa', // 背景不再是纯黑/纯白
//   textColor: isDark.value ? '#e2e2ea' : '#2c3e50',
//   navBarTextColor: isDark.value ? '#e2e2ea' : '#2c3e50',
//   navBarBackgroundColor: 'transparent', // 导航栏透明交由CSS控制
//   tabbarActiveColor: '#00c9a7',
//   tabbarInactiveColor: isDark.value ? '#666' : '#999',
// }))

const router = useRouter()
const { userInfo } = useUser();

// State
const activeTab = ref(0)
const activeTabbar = ref(0)
const goals = ref<any[]>([])
const showDetailModal = ref(false)
const selectedGoal = ref<any | null>(null)
const checkedSubGoals = ref<any[]>([])
const detailTab = ref(0)
const isExp = computed(() => selectedGoal?.value?.status === 'expired')
const goalFinishCount = ref(0)
const goalExpireCount = ref(0)
const goalIngCount = ref(0)

const isLoading = ref(true)
const isRefreshing = ref(false)
const listLoading = ref(false)
const listFinished = ref(true)

// Lifecycle
onMounted(async () => {
  await fetchGoals()
})

// Methods
const fetchGoals = async () => {
  try {
    isLoading.value = true
    // 注意：这里保留了你原本的代码逻辑
    const response = await getMPaths("getGoals", userInfo.value.username, "正在获取目标数据...");
    goals.value = response.data.data || []

    // 重新计算统计数据
    goalFinishCount.value = goals.value.filter(g => g.status === 'completed' || g.status === 'finished').length
    goalExpireCount.value = goals.value.filter(g => g.status === 'expired').length
    goalIngCount.value = goals.value.filter(g => g.status === 'in-progress' || g.status === 'in-progress').length

    listFinished.value = true
  } catch (error) {
    console.error(error)
    showToast('获取目标列表失败')
  } finally {
    isLoading.value = false
    isRefreshing.value = false
    listLoading.value = false
  }
}

const refreshGoals = async () => {
  isRefreshing.value = true
  await fetchGoals()
}

const loadMore = () => {
  listLoading.value = false
  listFinished.value = true
}

const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}

// Navigation
const goToSettings = () => router.push('/settings')
const goToAddGoal = () => router.push('/add-goal')
const goToStatistics = () => router.push('/statistics')
const goToMessageCenter = () => router.push('/messages')
const goToGuide = () => router.push('/user-guide')
const goToAIAssistant = () => router.push('/AIAssistantWindow')

// Logic
const showGoalDetail = (goal: any) => {
  selectedGoal.value = goal
  const subs = goal?.childGoals || []
  checkedSubGoals.value = subs.filter((sub: any) => sub.finish).map((sub: any) => sub._id)
  console.log("checkedSubGoals.value", checkedSubGoals.value)
  showDetailModal.value = true
}

const markGoalFinished = async (goal: any) => {
  if (!goal) return
  try {
    let data = {
      goalId: goal._id,
      type: 'success'
    }
    const res = await postM('finishGoal', data)
    if (isSuccess(res)) {
      await fetchGoals()
      showToast({ type: 'success', message: '太棒了！目标达成' })
      showDetailModal.value = false
    } else {
      showToast('操作失败')
    }
  } catch (error) {
    showToast('操作失败')
  }
}

const toggleSubGoal = async (sGoal: any,index: number) => {
  console.log(11232323);

  const subGoal = selectedGoal.value?.childGoals[index]
  if (subGoal) {
    subGoal.finish = !subGoal.finish
    if (subGoal.finish) {
      checkedSubGoals.value.push(subGoal._id)
    } else {
      const index = checkedSubGoals.value.indexOf(subGoal._id)
      if (index !== -1) {
        checkedSubGoals.value.splice(index, 1)
      }
    }
  }
  await finishChildGoal(checkedSubGoals.value)
}
// 修改方法
const handleSubGoalChange = async (subGoal, index) => {
 if(isExp.value) return
  
  // 调用 API
  try {
    const data = {
      goalId: selectedGoal.value._id,
      childGoalIds: checkedSubGoals.value
    }
    const res = await postM('finishGoal', data)  // 注意接口名称
    if (isSuccess(res)) {
      await fetchGoals()
      showToast('进度更新成功')
    } else {
      showToast('更新失败')
    }
  } catch (error) {
    console.error('更新子目标失败:', error)
    showToast('更新失败')
  }
}

// 可以删除原来的 toggleSubGoal 方法

const finishChildGoal = async (childIds: any[]) => {
  try {
    childIds = childIds.map(e => e._id)
    let data = {
      goalId: selectedGoal.value._id,
      childGoalIds: childIds
    }
    const res = await postM('finishGoal', data)
    if (isSuccess(res)) {
      await fetchGoals()
      showDetailModal.value = false
    } else {
      showToast('更新失败')
    }
    // showToast('进度更新')
  } catch (error) {
    showToast('更新失败')
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const getGoalStatusType = (status: string) => {
  switch (status) {
    case 'in-progress':
    case 'in-progress': return 'primary'
    case 'finished':
    case 'completed': return 'success'
    case 'expired': return 'danger'
    default: return 'default'
  }
}

const getGoalStatusText = (status: string) => {
  switch (status) {
    case 'in-progress':
    case 'in-progress': return '进行中'
    case 'finished':
    case 'completed': return '已完成'
    case 'expired': return '已过期'
    default: return '未知状态'
  }
}
const constProgress = ref(0)



const goalProgress = (goal: any) => {
  if (!goal) return 0
  return goal.progress;
}
</script>

<style scoped lang="scss">
/* --- 基础变量定义 --- */
.app-container {
  --bg-primary: #f7f8fa;
  --bg-secondary: #ffffff;
  --text-primary: #2c3e50;
  --text-secondary: #8590a6;
  --card-shadow: 0 8px 20px rgba(100, 100, 100, 0.06);
  --brand-color: #00c9a7;
  --glass-bg: rgba(255, 255, 255, 0.85);
  --border-line: #f0f2f5;

  min-height: 100vh;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
}

/* --- 暗黑模式覆盖 --- */
.app-container.dark-mode {
  --bg-primary: #121212;
  --bg-secondary: #1e1e1e;
  --text-primary: #e2e2ea;
  --text-secondary: #888;
  --card-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
  --glass-bg: rgba(30, 30, 30, 0.85);
  --border-line: #2c2c2c;
}

.content-wrapper {
  padding: 16px;
  padding-bottom: 80px;
  /* 留出 TabBar 空间 */
}

/* --- 顶部导航 --- */
.glass-nav {
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);

  ::v-deep(.van-nav-bar__content) {
    height: 50px;
  }
}

.nav-brand {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(90deg, var(--brand-color), #00a0e9);
  -webkit-background-clip: text;
  color: transparent;
  letter-spacing: -0.5px;
}

.nav-actions {
  display: flex;
  gap: 16px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(125, 125, 125, 0.1);
  color: var(--text-primary);
  transition: transform 0.2s;

  &:active {
    transform: scale(0.9);
  }
}

/* --- Hero 区域 --- */
.hero-section {
  margin-top: 10px;
  margin-bottom: 20px;
}

.hero-card {
  position: relative;
  padding: 24px;
  border-radius: 24px;
  background: linear-gradient(135deg, #00c9a7 0%, #00b686 100%);
  color: white;
  box-shadow: 0 10px 30px rgba(0, 201, 167, 0.3);
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.hero-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
}

.hero-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.hero-decoration {
  font-size: 40px;
  opacity: 0.2;
  transform: rotate(15deg);
}

.hero-actions {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 12px;
}

.action-btn {
  border-radius: 14px;
  height: 44px;
  font-weight: 600;
  border: none;
}

.action-btn.primary {
  background: white;
  color: #00c9a7;
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(4px);
}

/* --- 快捷入口 --- */
.quick-grid-section {
  margin-bottom: 24px;
}

.grid-card {
  display: flex;
  justify-content: space-between;
  background: var(--bg-secondary);
  padding: 16px 20px;
  border-radius: 20px;
  box-shadow: var(--card-shadow);
}

.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-primary);
  font-weight: 500;
}

.grid-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.92);
  }
}

.grid-icon-box.blue {
  background: rgba(79, 142, 247, 0.1);
  color: #4f8ef7;
}

.grid-icon-box.green {
  background: rgba(0, 201, 167, 0.1);
  color: #00c9a7;
}

.grid-icon-box.orange {
  background: rgba(255, 159, 67, 0.1);
  color: #ff9f43;
}

.grid-icon-box.purple {
  background: rgba(165, 94, 234, 0.1);
  color: #a55eea;
}

.grid-icon-box.teal {
  background: rgba(0, 201, 167, 0.1);
  color: #00c9a7;
}

/* --- 统计概览 --- */
.stats-overview {
  margin-bottom: 24px;
}

.section-header {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
  padding-left: 4px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat-widget {
  background: var(--bg-secondary);
  padding: 12px;
  border-radius: 16px;
  box-shadow: var(--card-shadow);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 90px;
}

.widget-head {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--text-secondary);
}

.widget-num {
  font-size: 22px;
  font-weight: 700;
  margin: 8px 0;
}

.processing .widget-num {
  color: #4f8ef7;
}

.finished .widget-num {
  color: #00c9a7;
}

.expired .widget-num {
  color: #ff6b6b;
}

/* --- 目标列表 --- */
.goals-container {
  min-height: 300px;
}

.goal-list-wrap {
  padding-top: 10px;
  padding-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pt-2 {
  padding-top: 20px;
}

/* 卡片样式 */
.goal-card-swipe {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--card-shadow);
}

.goal-card {
  position: relative;
  background: var(--bg-secondary);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-status-line {
  width: 4px;
  height: 40px;
  border-radius: 2px;
  background: #eee;

  &.in-progress,
  &.in-progress {
    background: #4f8ef7;
  }

  &.finished,
  &.completed {
    background: #00c9a7;
  }

  &.expired {
    background: #ff6b6b;
  }
}

.card-main {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 70%;
}

.card-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-progress {
  background: transparent;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.card-arrow {
  color: #ccc;
  font-size: 16px;
}

.swipe-action-btn {
  height: 100%;
  width: 60px;
  background: #00c9a7;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

/* 简易卡片样式 */
.goal-card-simple {
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: 16px;
  box-shadow: var(--card-shadow);
  display: flex;
  justify-content: space-between;
  align-items: center;

  &.finished {
    opacity: 0.7;

    .title {
      text-decoration: line-through;
    }
  }
}

.simple-info {
  .title {
    font-weight: 600;
    font-size: 15px;
    margin-bottom: 4px;
  }

  .date {
    font-size: 12px;
    color: var(--text-secondary);
  }
}

/* --- 底部 Tabbar --- */
.glass-tabbar {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-top: none;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.03);
}

/* --- 详情弹窗 --- */
.detail-popup {
  background: var(--bg-secondary);
}

.popup-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.popup-header {
  padding: 24px;
  text-align: center;
  border-bottom: 1px solid var(--border-line);
}

.popup-title {
  margin: 12px 0;
  font-size: 20px;
  color: var(--text-primary);
}

.popup-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  color: var(--text-secondary);
  font-size: 13px;
}

.popup-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 0 20px 0;
}

.detail-content-box {
  padding: 20px 24px;
}

.section-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.desc-text {
  font-size: 15px;
  line-height: 1.6;
  color: var(--text-primary);
  background: rgba(125, 125, 125, 0.05);
  padding: 16px;
  border-radius: 12px;
}

.progress-circle-wrap {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.subgoals-list {
  padding: 10px;
}

.subgoal-item {
  padding: 16px;
  background: rgba(125, 125, 125, 0.03);
  margin-bottom: 10px;
  border-radius: 12px;
}

.text-crossed {
  text-decoration: line-through;
  color: var(--text-secondary);
}

.popup-footer {
  padding: 16px 24px 32px;
  /* iOS safe area */
  background: var(--bg-secondary);
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
}

.shadow-btn {
  box-shadow: 0 4px 12px rgba(0, 201, 167, 0.4);
}

/* Skeleton & Loading */
.skeleton-list {
  padding: 20px 0;
}

.custom-skeleton {
  margin-bottom: 20px;
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: 16px;
}

.mt-4 {
  margin-top: 16px;
}
</style>