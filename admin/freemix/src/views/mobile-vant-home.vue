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
            <div class="icon-btn" @click="openQrScanner">
              <van-icon name="scan" size="20" />
            </div>
            <div class="icon-btn" @click="goToSettings">
              <van-icon name="setting-o" size="20" />
            </div>
          </div>
        </template>
      </van-nav-bar>

      <div class="content-wrapper">
        <!-- Hero 区域（沉浸式大卡片） -->
        <section class="hero-section">
          <div class="hero-card">
            <!-- 动态渐变装饰层 -->
            <div class="hero-bg-layer"></div>
            <div class="hero-bg-shape"></div>
            <div class="hero-bg-shape-2"></div>
            <div class="hero-content">
              <div class="hero-text">
                <p class="hero-date">{{ currentDate }}</p>
                <h1 class="hero-title">早安，进击者</h1>
                <p class="hero-sub">让每一个目标都清晰可见</p>
              </div>
              <div class="hero-decoration">
                <van-icon name="fire-o" />
              </div>
            </div>
            <!-- 半透明毛玻璃按钮 -->
            <div class="hero-actions">
              <van-button class="action-btn primary" icon="plus" @click="goToAddGoal">新建目标</van-button>
              <van-button class="action-btn secondary" icon="chart-trending-o" @click="goToStatistics">数据统计</van-button>
            </div>
          </div>
        </section>

        <!-- 快捷入口（胶囊风格，浮动在统计区上方） -->
        <section class="quick-capsules">
          <div class="capsule-item" @click="goToMessageCenter">
            <van-icon name="chat-o" size="16" /><span>消息</span>
          </div>
          <div class="capsule-item" @click="goToAIAssistant">
            <van-icon name="smile-o" size="16" /><span>AI助手</span>
          </div>
          <div class="capsule-item" @click="goToGuide">
            <van-icon name="bulb-o" size="16" /><span>指南</span>
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
          <van-search v-model="searchQuery" placeholder="搜索目标..." shape="round" background="transparent" />
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
                    <!-- 目标卡片（左滑显示操作按钮） -->
                    <div class="goal-list-wrap">
                      <van-swipe-cell v-for="goal in searchFilteredGoals" :key="goal.id" :right-width="120">
                        <div class="goal-card-glass" @click="showGoalDetail(goal)">
                          <!-- 左侧状态色点 -->
                          <div class="card-dot" :class="goal.status"></div>
                          <div class="card-main">
                            <div class="card-header">
                              <h3 class="card-title">{{ goal.title }}</h3>
                              <van-tag :type="getGoalStatusType(goal.status)" round size="small" class="status-pill">
                                {{ getGoalStatusText(goal.status) }}</van-tag>
                            </div>
                            <div class="card-meta">
                              <span class="meta-item"><van-icon name="clock-o" /> {{ goal.deadlineString }}</span>
                              <span class="meta-item"><van-icon name="manager-o" /> {{ goal.owner }}</span>
                            </div>
                            <!-- 标签展示 -->
                            <div class="card-tags" v-if="goal.tags && goal.tags.length">
                              <van-tag v-for="tag in goal.tags" :key="tag" plain type="primary" size="mini" class="mr-1">{{ tag }}</van-tag>
                            </div>
                            <div class="card-progress">
                              <div class="progress-info">
                                <span>进度</span>
                                <span>{{ goalProgress(goal) }}%</span>
                              </div>
                              <van-progress :percentage="goalProgress(goal)" :show-pivot="false" stroke-width="2"
                                track-color="var(--bg-glass)" />
                            </div>
                          </div>
                        </div>
                        <template #right>
                          <van-button square class="swipe-btn edit"  text="编辑" @click="openEditGoal(goal)" />
                          <van-button square class="swipe-btn finish"  text="完成" @click="markGoalFinished(goal)" />
                          <!-- <div class="swipe-btn edit" @click="openEditGoal(goal)">
                            <van-icon name="edit" size="18" />
                            <span>编辑</span>
                          </div>
                          <div class="swipe-btn finish" @click="markGoalFinished(goal)">
                            <van-icon name="success" size="18" />
                            <span>完成</span>
                          </div> -->
                        </template>
                      </van-swipe-cell>
                    </div>
                  </van-list>
                </div>
              </van-tab>

              <van-tab title="进行中">
                <!-- 沉浸式玻璃卡片列表 -->
                <div class="goal-list-wrap pt-2">
                  <van-empty v-if="searchFilteredGoals.filter(g => g.status === 'in-progress').length === 0" description="暂无进行中的目标"
                    image="search" />
                  <div v-else class="goal-card-glass"
                    v-for="goal in searchFilteredGoals.filter(g => g.status === 'in-progress')" :key="goal.id"
                    @click="showGoalDetail(goal)">
                    <div class="card-dot in-progress"></div>
                    <div class="simple-info">
                      <div class="title">{{ goal.title }}</div>
                      <div class="date">{{ formatDate(goal.deadline) }} 截止</div>
                      <div class="tags" v-if="goal.tags && goal.tags.length">
                        <van-tag v-for="tag in goal.tags" :key="tag" plain type="primary" size="mini" class="mr-1">{{ tag }}</van-tag>
                      </div>
                    </div>
                    <van-icon name="arrow" color="var(--text-secondary)" size="16" />
                  </div>
                </div>
              </van-tab>

              <van-tab title="已完成">
                <div class="goal-list-wrap pt-2">
                  <van-empty v-if="searchFilteredGoals.filter(g => g.status === 'completed' || g.status === 'finished').length === 0" description="暂无已完成的目标"
                    image="search" />
                  <div v-else class="goal-card-glass finished"
                    v-for="goal in searchFilteredGoals.filter(g => g.status === 'completed' || g.status === 'finished')" :key="goal.id"
                    @click="showGoalDetail(goal)">
                    <div class="card-dot completed"></div>
                    <div class="simple-info">
                      <div class="title">{{ goal.title }}</div>
                      <div class="date">任务已完成</div>
                    </div>
                    <van-icon name="checked" color="#00c9a7" size="16" />
                  </div>
                </div>
              </van-tab>

              <van-tab title="已过期">
                <div class="goal-list-wrap pt-2">
                  <van-empty v-if="searchFilteredGoals.filter(g => g.status === 'expired').length === 0" description="暂无已过期的目标"
                    image="search" />
                  <div v-else class="goal-card-glass expired"
                    v-for="goal in searchFilteredGoals.filter(g => g.status === 'expired')" :key="goal.id"
                    @click="showGoalDetail(goal)">
                    <div class="card-dot expired-dot"></div>
                    <div class="simple-info">
                      <div class="title">{{ goal.title }}</div>
                      <div class="date">{{ formatDate(goal.deadline) }} 截止</div>
                    </div>
                    <van-icon name="warning-o" color="#ff6b6b" size="16" />
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

      <!-- 详情弹窗（毛玻璃半屏面板） -->
      <van-popup v-model:show="showDetailModal" position="bottom" round closeable
        class="detail-popup-glass" :style="{ height: '75%' }" overlay-class="glass-overlay">
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
                      <van-checkbox :name="subGoal._id" shape="round" checked-color="#00c9a7"
                        @click="handleSubGoalChange(subGoal, index)" :disabled="isExp">
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

          
        </div>
      </van-popup>


    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast } from 'vant'
import { postM, getMPaths, isSuccess, baseURL } from '@/utils/request'
import { useUser } from '@/hooks'
import { Capacitor } from '@capacitor/core';
import { BarcodeScanner, BarcodeFormat, LensFacing } from '@capacitor-mlkit/barcode-scanning';

const router = useRouter()
const { userInfo } = useUser();

// State
const isDark = ref(false)
const currentTheme = ref('dark')
const isRefreshing = ref(false)
const isLoading = ref(true)
const listLoading = ref(false)
const listFinished = ref(true)
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
const constProgress = ref(0)
const searchQuery = ref('')

// 当前日期（用于 Hero 区域显示周几）
const currentDate = computed(() => {
  const now = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
})

const searchFilteredGoals = computed(() => {
  if (!searchQuery.value) return goals.value
  const query = searchQuery.value.toLowerCase()
  return goals.value.filter(g => 
    (g.title && g.title.toLowerCase().includes(query)) || 
    (g.description && g.description.toLowerCase().includes(query))
  )
})

// Methods
const openQrScanner = () => {
  router.push('/mobile/scan');
};

const fetchGoals = async () => {
  try {
    isLoading.value = true
    const response = await getMPaths("getGoals", userInfo.value.username, "正在获取目标数据...");
    goals.value = response.data.data || []
    
    // Format dates
    goals.value.forEach(g => {
      g.deadlineString = formatDate(g.deadline)
    })

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

// Edit/Add Logic
const goToAddGoal = () => {
  router.push('/add-goal')
}

const openEditGoal = (goal: any) => {
  showDetailModal.value = false
  // 传递 goalData 到 state 中，避免 URL 参数过长
  router.push({
    path: '/add-goal',
    query: { id: goal._id || goal.id },
    state: { goalData: JSON.stringify(goal) }
  })
}

// Navigation
const goToSettings = () => router.push('/settings')
// const goToAddGoal = () => router.push('/add-goal') // Replaced above
const goToStatistics = () => router.push('/statistics')
const goToMessageCenter = () => router.push('/messages')
const goToGuide = () => router.push('/user-guide')
const goToAIAssistant = () => router.push('/AIAssistantWindow')

// Logic
const showGoalDetail = (goal: any) => {
  selectedGoal.value = goal
  const subs = goal?.childGoals || []
  if(goal.childGoals){

    checkedSubGoals.value = subs.filter((sub: any) => sub.finish).map((sub: any) => sub._id)
  }else{
    checkedSubGoals.value = [];
  }
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

const handleSubGoalChange = async (subGoal, index) => {
  if (isExp.value) return;

  // 立即通过 nextTick 确保 checkedSubGoals 已经由 van-checkbox-group 更新
  await nextTick();
  // 过滤掉传入的 null 或 undefined 值
  checkedSubGoals.value = checkedSubGoals.value.filter(n => n !== null && n !== undefined);

  try {
    const data = {
      goalId: selectedGoal.value._id,
      childGoalIds: checkedSubGoals.value
    };
    const res = await postM('finishGoal', data);
    if (isSuccess(res)) {
      // 更新本地数据，避免全量刷新导致的状态闪烁或错位
      if (selectedGoal.value && selectedGoal.value.childGoals) {
        selectedGoal.value.childGoals.forEach(sg => {
          sg.finish = checkedSubGoals.value.includes(sg._id);
        });
      }
      // 同步更新列表中的数据
      const goalIndex = goals.value.findIndex(g => (g._id || g.id) === selectedGoal.value._id);
      if (goalIndex > -1) {
        goals.value[goalIndex].childGoals = JSON.parse(JSON.stringify(selectedGoal.value.childGoals));
      }
      await fetchGoals()
      
      showToast('进度更新成功');
    } else {
      showToast('更新失败');
    }
  } catch (error) {
    console.error('更新子目标失败:', error);
    showToast('更新失败');
  }
};

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const getGoalStatusType = (status: string) => {
  switch (status) {
    case 'in-progress': return 'primary'
    case 'finished':
    case 'completed': return 'success'
    case 'expired': return 'danger'
    default: return 'default'
  }
}

const getGoalStatusText = (status: string) => {
  switch (status) {
    case 'in-progress': return '进行中'
    case 'finished':
    case 'completed': return '已完成'
    case 'expired': return '已过期'
    default: return '未知状态'
  }
}

const goalProgress = (goal: any) => {
  if (!goal) return 0
  return goal.progress;
}

// Lifecycle
onMounted(async () => {
  await fetchGoals()
})
</script>

<style scoped lang="scss">
/* ============================================
   沉浸式毛玻璃主题 - 全局变量
   ============================================ */
.app-container {
  --bg-primary: #f0f2f5;
  --bg-secondary: rgba(255, 255, 255, 0.72);
  --bg-glass: rgba(255, 255, 255, 0.55);
  --text-primary: #1a1a2e;
  --text-secondary: #8e8e9a;
  --card-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  --brand-color: #00c9a7;
  --glass-bg: rgba(255, 255, 255, 0.78);
  --border-line: rgba(0, 0, 0, 0.06);

  min-height: 100vh;
  /* 底层渐变背景（滚动时产生景深感） */
  background: linear-gradient(160deg, #f0fdf9 0%, #f0f2f5 30%, #f8f4ff 70%, #f0fdf9 100%);
  color: var(--text-primary);
  transition: background 0.5s ease, color 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
}

/* --- 暗黑模式覆盖 --- */
.app-container.dark-mode {
  --bg-primary: #0a0a0f;
  --bg-secondary: rgba(30, 30, 35, 0.7);
  --bg-glass: rgba(30, 30, 35, 0.5);
  --text-primary: #e8e8f0;
  --text-secondary: #888;
  --card-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
  --glass-bg: rgba(30, 30, 35, 0.75);
  --border-line: rgba(255, 255, 255, 0.06);

  background: linear-gradient(160deg, #0a0a0f 0%, #111118 30%, #0e0e1a 70%, #0a0a0f 100%);
}

/* ============================================
   内容区
   ============================================ */
.content-wrapper {
  padding: 16px;
  padding-bottom: 40px;
}

/* ============================================
   顶部导航栏（毛玻璃吸附）
   ============================================ */
.glass-nav {
  background: var(--glass-bg);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);

  ::v-deep(.van-nav-bar__content) {
    height: 50px;
  }
}

.nav-brand {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--brand-color), #00a0e9);
  -webkit-background-clip: text;
  color: transparent;
  letter-spacing: -0.5px;
}

.nav-actions {
  display: flex;
  gap: 12px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(125, 125, 125, 0.08);
  color: var(--text-primary);
  transition: transform 0.2s, background 0.2s;

  &:active {
    transform: scale(0.9);
    background: rgba(0, 201, 167, 0.12);
  }
}

/* ============================================
   Hero 区域（沉浸式大卡片，占屏 1/3）
   ============================================ */
.hero-section {
  margin-top: 8px;
  margin-bottom: 24px;
}

.hero-card {
  position: relative;
  padding: 28px 24px 24px;
  border-radius: 28px;
  /* 动态渐变背景 */
  background: linear-gradient(145deg, #00c9a7 0%, #009a8c 40%, #00b8a0 70%, #00d4b0 100%);
  background-size: 200% 200%;
  animation: heroGradient 8s ease-in-out infinite;
  color: white;
  box-shadow:
    0 8px 32px rgba(0, 201, 167, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

/* 动态渐变动画 */
@keyframes heroGradient {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

/* 装饰层 - 光晕 */
.hero-bg-layer {
  position: absolute;
  top: -40%;
  right: -20%;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  pointer-events: none;
}

/* 装饰形状1 - 大圆 */
.hero-bg-shape {
  position: absolute;
  bottom: -30px;
  left: -30px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  pointer-events: none;
}

/* 装饰形状2 - 小圆 */
.hero-bg-shape-2 {
  position: absolute;
  top: 20px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.hero-text {
  flex: 1;
}

.hero-date {
  margin: 0 0 8px;
  font-size: 13px;
  opacity: 0.75;
  font-weight: 400;
  letter-spacing: 0.3px;
}

.hero-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.hero-sub {
  margin: 0;
  font-size: 15px;
  opacity: 0.85;
  font-weight: 400;
}

.hero-decoration {
  font-size: 52px;
  opacity: 0.15;
  transform: rotate(15deg);
  margin-top: -4px;
}

/* 半透明毛玻璃按钮 */
.hero-actions {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 12px;
}

.action-btn {
  border-radius: 16px;
  height: 46px;
  font-weight: 600;
  font-size: 15px;
  border: none;
  flex: 1;
}

.action-btn.primary {
  background: rgba(255, 255, 255, 0.95);
  color: #00c9a7;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.18);
  color: white;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.25);
}

/* ============================================
   快捷入口（胶囊按钮行）
   ============================================ */
.quick-capsules {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 20px;
}

.capsule-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border-radius: 24px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  background: var(--bg-glass);
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  border: 1px solid var(--border-line);
  box-shadow: var(--card-shadow);
  transition: all 0.25s ease;
  cursor: pointer;

  &:active {
    transform: scale(0.95);
    background: rgba(0, 201, 167, 0.12);
    border-color: var(--brand-color);
  }
}

/* ============================================
   统计概览（玻璃卡片）
   ============================================ */
.stats-overview {
  margin-bottom: 24px;
}

.section-header {
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 14px;
  padding-left: 4px;
  color: var(--text-primary);
  letter-spacing: -0.2px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat-widget {
  background: var(--bg-glass);
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  border: 1px solid var(--border-line);
  padding: 14px 12px;
  border-radius: 20px;
  box-shadow: var(--card-shadow);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 96px;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.97);
  }
}

.widget-head {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.widget-num {
  font-size: 28px;
  font-weight: 300;
  margin: 4px 0;
  letter-spacing: -1px;
  font-variant-numeric: tabular-nums;
}

.processing .widget-num { color: #4f8ef7; }
.finished .widget-num { color: #00c9a7; }
.expired .widget-num { color: #ff6b6b; }

/* ============================================
   目标列表（无边框 + 分割线）
   ============================================ */
.goals-container {
  min-height: 300px;

  /* 搜索框圆角调整 */
  ::v-deep(.van-search__content) {
    background: var(--bg-glass);
    backdrop-filter: blur(10px);
    border-radius: 20px;
  }
}

.goal-list-wrap {
  padding-top: 8px;
  padding-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.pt-2 {
  padding-top: 16px;
}

/* --- 左滑单元格分割线 --- */
.goal-list-wrap ::v-deep(.van-swipe-cell) {
  border-bottom: 0.5px solid var(--border-line);

  &:last-child {
    border-bottom: none;
  }
}

/* --- 沉浸式玻璃样式卡片 --- */
.goal-card-glass {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  background: transparent;
  transition: background 0.2s;
  cursor: pointer;

  &:active {
    background: rgba(0, 201, 167, 0.04);
  }
}

/* 完成 / 过期态的淡化处理 */
.goal-card-glass.finished {
  opacity: 0.65;
  .title {
    text-decoration: line-through;
  }
}

.goal-card-glass.expired {
  opacity: 0.7;
  .title {
    color: #ff6b6b;
  }
}

/* 左侧 3px 状态色点 */
.card-dot {
  width: 3px;
  min-height: 36px;
  border-radius: 2px;
  align-self: stretch;
  flex-shrink: 0;
  background: #ddd;

  &.in-progress { background: #4f8ef7; }
  &.completed,
  &.finished { background: #00c9a7; }
  &.expired,
  &.expired-dot { background: #ff6b6b; }
}

.card-main {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 70%;
  color: var(--text-primary);
}

.card-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 3px;
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

.card-tags {
  margin-bottom: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.mr-1 {
  margin-right: 4px;
}

/* --- 左滑操作按钮 --- */
.swipe-btn {
  height: 100%;
  // width: 100%;
  // display: flex;
  // flex-direction: column;
  // align-items: center;
  // justify-content: center;
  // gap: 4px;
  font-size: 11px;
  color: white;
  cursor: pointer;

  &.edit {
    background: #4f8ef7;
  }

  &.finish {
    background: #00c9a7;
  }
}

/* --- 简易信息样式（其他 tab 复用） --- */
.simple-info {
  flex: 1;
  .title {
    font-weight: 600;
    font-size: 15px;
    margin-bottom: 3px;
    color: var(--text-primary);
  }

  .date {
    font-size: 12px;
    color: var(--text-secondary);
  }

  .tags {
    margin-top: 4px;
  }
}

/* ============================================
   详情弹窗（毛玻璃半屏面板）
   ============================================ */

/* 弹窗背后遮罩毛玻璃 */
:deep(.glass-overlay) {
  background: rgba(0, 0, 0, 0.3) !important;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.detail-popup-glass {
  background: var(--glass-bg);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.popup-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.popup-header {
  padding: 24px 24px 16px;
  text-align: center;
  border-bottom: 0.5px solid var(--border-line);
}

.popup-title {
  margin: 12px 0;
  font-size: 22px;
  font-weight: 700;
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
  font-size: 12px;
  font-weight: 700;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.desc-text {
  font-size: 15px;
  line-height: 1.65;
  color: var(--text-primary);
  background: rgba(125, 125, 125, 0.04);
  backdrop-filter: blur(4px);
  padding: 16px;
  border-radius: 14px;
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
  display: flex;
  flex-direction: column;
  padding: 16px 24px 32px;
  background: transparent;
}

.shadow-btn {
  box-shadow: 0 4px 16px rgba(0, 201, 167, 0.3);
}

/* ============================================
   Skeleton & 工具类
   ============================================ */
.skeleton-list {
  padding: 20px 0;
}

.custom-skeleton {
  margin-bottom: 20px;
  background: var(--bg-glass);
  backdrop-filter: blur(4px);
  padding: 16px;
  border-radius: 16px;
}

.mt-4 {
  margin-top: 16px;
}

/* ============================================
   底部 Tabbar（保留备用）
   ============================================ */
.glass-tabbar {
  background: var(--glass-bg);
  backdrop-filter: blur(14px);
  border-top: none;
  box-shadow: 0 -2px 16px rgba(0, 0, 0, 0.03);
}
</style>


