<template>
  <van-config-provider :theme="currentTheme">
    <div class="app-container" :class="{ 'dark-mode': true }">

      <!-- 顶部导航栏 (毛玻璃效果) -->
      <van-nav-bar fixed placeholder class="glass-nav" :border="false" z-index="100" :safe-area-inset-top="true">
        <template #left>
          <div class="nav-brand">
            <img src="/icons/icon.png" alt="" class="nav-brand-icon" />
            FreeMix
          </div>
        </template>
        <template #right>
          <div class="nav-actions">
            <!-- 通知铃铛 - 带未读徽标 -->
            <div class="icon-btn bell-btn" @click="goToNotifications">
              <van-icon name="bell" size="20" />
              <span class="bell-badge" v-if="notificationUnread > 0">{{ notificationUnread > 99 ? '99+' : notificationUnread }}</span>
            </div>
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
            <div class="hero-bg-layer-2"></div>
            <div class="hero-bg-shape"></div>
            <div class="hero-bg-shape-2"></div>
            <div class="hero-bg-grid"></div>
            <div class="hero-content">
              <div class="hero-text">
                <p class="hero-date">{{ currentDate }}</p>
                <h1 class="hero-title">{{currentTimeDesc}}，进击者</h1>
                <p class="hero-sub">让每一个目标都清晰可见</p>
              </div>
              <div class="hero-decoration">
                <van-icon name="fire-o" />
              </div>
            </div>
            <!-- 底部操作按钮 -->
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
          <div class="capsule-item" @click="generateAIMorning">
            <van-icon name="notes-o" size="16" /><span>AI晨报</span>
          </div>
        </section>

        <!-- 🔧 调试面板（点击测试通知 & 快捷操作） -->
        <section class="debug-panel">
          <!-- <div class="debug-toggle" @click="showDebug = !showDebug">
            <van-icon name="setting-o" size="16" /><span>原生调试 (显示/隐藏)</span>
          </div> -->
          <div v-if="showDebug" class="debug-actions">
            <div class="debug-row">
              <van-button size="small" round type="primary" @click="debugTestNotify">测试通知 (5s)</van-button>
              <van-button size="small" round @click="debugGetShortcut">检查快捷操作</van-button>
              <van-button size="small" round @click="debugGetPending">待发通知数</van-button>
            </div>
            <div class="debug-log" v-if="debugLog.length">
              <div v-for="(log, i) in debugLog" :key="i" class="debug-line">{{ log }}</div>
            </div>
          </div>
        </section>

        <!-- 统计概览 -->
        <section class="stats-overview">
          <div class="stats-row">
            <div class="stat-widget processing">
              <div class="widget-head">
                <van-icon name="play-circle-o" />
                <span>进行中</span>
              </div>
              <div class="widget-num">{{ animIngCount }}</div>
              <van-progress :percentage="progressOngoing" :show-pivot="false" color="#4f8ef7"
                track-color="rgba(79,142,247,0.15)" stroke-width="4" />
            </div>
            <div class="stat-widget finished">
              <div class="widget-head">
                <van-icon name="checked" />
                <span>已完成</span>
              </div>
              <div class="widget-num">{{ animFinishCount }}</div>
              <van-progress :percentage="progressFinished" :show-pivot="false" color="#00c9a7"
                track-color="rgba(0,201,167,0.15)" stroke-width="4" />
            </div>
            <div class="stat-widget expired">
              <div class="widget-head">
                <van-icon name="warning-o" />
                <span>已过期</span>
              </div>
              <div class="widget-num">{{ animExpireCount }}</div>
              <van-progress :percentage="progressExpired" :show-pivot="false" color="#ff6b6b"
                track-color="rgba(255,107,107,0.15)" stroke-width="4" />
            </div>
          </div>
        </section>

        <!-- 目标列表 -->
        <section class="goals-container">
          <van-pull-refresh v-model="isRefreshing" @refresh="refreshGoals">
            <van-tabs v-model:active="activeTab"  offset-top="46" background="transparent" line-width="20px"
              line-height="3px" color="#00c9a7" title-active-color="#00c9a7"
              :title-inactive-color="isDark ? '#888' : '#666'" class="custom-tabs"
              @click-tab="onTabClick">
              <template #nav-right>
                <div class="tab-search-trigger" @click.stop="showSearchBar = !showSearchBar">
                  <van-icon :name="showSearchBar ? 'cross' : 'search'" size="18" color="var(--text-secondary)" />
                </div>
              </template>
              <van-tab title="全部">
                <!-- 内嵌搜索栏 -->
                <transition name="fade-slide-up">
                  <van-search v-show="showSearchBar" v-model="searchQuery" placeholder="搜索目标..." shape="round"
                    background="transparent" class="inline-search" />
                </transition>
                <div v-if="isLoading" class="skeleton-list upgraded">
                  <div class="glass-skeleton" v-for="i in 3" :key="i">
                    <div class="sk-line w-70"></div>
                    <div class="sk-line w-40"></div>
                    <div class="sk-line w-90"></div>
                  </div>
                </div>
                <div v-else>
                  <van-list v-model:loading="listLoading" :finished="listFinished" finished-text="没有更多了"
                    @load="loadMore">
                    <!-- 目标卡片（左滑显示操作按钮） -->
                    <div class="goal-list-wrap">
                      <van-swipe-cell v-for="goal in searchFilteredGoals" :key="goal.id" :right-width="160">
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
                          <van-button square class="swipe-btn error"  text="删除" @click="deleteGoal(goal)" />
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
                  <div v-if="searchFilteredGoals.filter(g => g.status === 'in-progress').length === 0" class="empty-glass-card">
                    <div class="empty-icon-animated">
                      <van-icon name="todo-list-o" size="40" />
                    </div>
                    <p class="empty-title">暂无进行中的目标</p>
                    <p class="empty-desc">点击上方「新建目标」开始吧</p>
                  </div>
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
                  <div v-if="searchFilteredGoals.filter(g => g.status === 'completed' || g.status === 'finished').length === 0" class="empty-glass-card">
                    <div class="empty-icon-animated">
                      <van-icon name="checked" size="40" />
                    </div>
                    <p class="empty-title">暂无已完成的目标</p>
                    <p class="empty-desc">完成的目标会在这里展示</p>
                  </div>
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
                  <div v-if="searchFilteredGoals.filter(g => g.status === 'expired').length === 0" class="empty-glass-card">
                    <div class="empty-icon-animated">
                      <van-icon name="smile-o" size="40" />
                    </div>
                    <p class="empty-title">暂无已过期的目标</p>
                    <p class="empty-desc">继续保持！</p>
                  </div>
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
            <!-- 顶部发光装饰线 -->
            <div class="popup-glow-bar"></div>
            <div class="popup-status-icon" :class="selectedGoal?.status">
              <van-icon :name="getStatusIcon(selectedGoal?.status)" size="22" color="#fff" />
            </div>
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
                  <div v-if="!(selectedGoal?.childGoals && selectedGoal.childGoals.length > 0)" class="empty-glass-card small">
                    <van-icon name="list" size="32" color="rgba(255,255,255,0.2)" />
                    <p class="empty-title">没有子任务</p>
                  </div>
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

      <!-- AI晨报弹窗 (深度定制样式) -->
      <van-dialog 
        v-model:show="showAIMorningDialog" 
        class="custom-glass-dialog"
        :show-confirm-button="false"
      >
        <div class="glass-dialog-inner">
          <div class="glass-dialog-header">
            <div class="header-icon"><van-icon name="fire" color="#00c9a7" /></div>
            <div class="header-title">今日晨报</div>
          </div>
          <div class="ai-morning-content" v-html="aiCleanCOntentWatch"></div>
          <div class="glass-dialog-footer">
            <van-button class="glass-confirm-btn" round block @click="showAIMorningDialog = false">
              好的，去完成目标
            </van-button>
          </div>
        </div>
      </van-dialog>

    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref, computed,watch, onMounted, nextTick, onUnmounted } from 'vue'
import { updateGoalsData } from '@/hooks/useNativeBridge'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast,closeToast,showConfirmDialog } from 'vant'
import { postM, getMPaths, isSuccess, baseURL } from '@/utils/request'
import { useUser } from '@/hooks'
import { Capacitor } from '@capacitor/core';
import { BarcodeScanner, BarcodeFormat, LensFacing } from '@capacitor-mlkit/barcode-scanning';
import { callCozeAPI } from '@/utils/aiService.js'

const router = useRouter()
const currentTimeDesc = computed(() => {
  const hour = new Date().getHours()
  if (hour < 11) return '早安'       // 05:00 - 10:59  (可自行调整起始)
  else if (hour < 13) return '中午好' // 11:00 - 12:59
  else if (hour < 17) return '下午好' // 13:00 - 16:59
  else if (hour < 19) return '傍晚好' // 17:00 - 18:59
  else return '晚上好'                // 19:00 以后
})

// ===== 调试面板 =====
const showDebug = ref(false)
const debugLog = ref<string[]>([])

function addDebugLog(msg: string) {
  debugLog.value.unshift(`[${new Date().toLocaleTimeString()}] ${msg}`)
  if (debugLog.value.length > 50) debugLog.value.pop()
}
const deleteGoal = (row) => {
  showConfirmDialog({
  title: '确认删除吗？',
  message:
    '确认删除后，该目标将无法恢复。',
    confirmButtonColor: '#00c9a7',
})
  .then(() => {
    postM('deleteGoal', { row }).then((res) => {
      if (isSuccess(res)) {
        showToast('删除成功');
        refreshGoals();
      }
    })
    // on confirm
  })
  .catch(() => {
    // on cancel
  });

}
function debugTestNotify() {
  // 通知由 AppDelegate 在进后台时自动调度，这里只演示流程
  addDebugLog('📱 已暴露 ' + (goals.value?.length || 0) + ' 条目标到 window.freemixGoalsData')
  addDebugLog('📱 退到后台时 AppDelegate 会自动读取并调度通知')
  updateGoalsData(goals.value)
  showToast('已刷新目标数据，请退到后台测试通知')
}

function debugGetShortcut() {
  addDebugLog('📱 快捷操作由 AppDelegate 在冷启动时写入 UserDefaults')
  addDebugLog('📱 WebView 就绪后 dispatchEvent("quick-action") 到前端')
  showToast('快捷操作流程：原生 → UserDefaults → JS事件 → router.push')
}

function debugGetPending() {
  addDebugLog('📱 通知由 AppDelegate 在进后台时通过 UNCalendarNotificationTrigger 调度')
  addDebugLog('📱 每日概览通知：每天 9:00 触发')
  addDebugLog('📱 目标到期提醒：截止日当天 9:00 触发')
  showToast('通知在进入后台时自动调度，每天 9:00 触发')
}
// ===== 调试面板结束 =====
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
const showSearchBar = ref(false)
const goals = ref<any[]>([])
const showDetailModal = ref(false)
const selectedGoal = ref<any | null>(null)
const checkedSubGoals = ref<any[]>([])
const detailTab = ref(0)
const isExp = computed(() => selectedGoal?.value?.status === 'expired')
const goalFinishCount = ref(0)
const goalExpireCount = ref(0)
const goalIngCount = ref(0)
// 动画展示数字（用于统计卡片的滚动效果）
const animIngCount = ref(0)
const animFinishCount = ref(0)
const animExpireCount = ref(0)
const constProgress = ref(0)

// 数字动画：从 0 滚动到目标值
const animateCounter = (target: number, cb: (v: number) => void) => {
  const duration = 600
  const start = performance.now()
  const frame = (now: number) => {
    const elapsed = now - start
    const progress = Math.min(elapsed / duration, 1)
    // ease-out cubic
    const eased = 1 - Math.pow(1 - progress, 3)
    cb(Math.round(eased * target))
    if (progress < 1) requestAnimationFrame(frame)
  }
  requestAnimationFrame(frame)
}

watch([goalIngCount, goalFinishCount, goalExpireCount], ([ing, fin, exp]) => {
  animateCounter(ing, v => animIngCount.value = v)
  animateCounter(fin, v => animFinishCount.value = v)
  animateCounter(exp, v => animExpireCount.value = v)
})
const searchQuery = ref('')

// 通知未读数
const notificationUnread = ref(0)
const fetchNotificationUnread = async () => {
  try {
    const res = await postM('getUnreadNotificationCount')
    if (isSuccess(res)) {
      notificationUnread.value = res.data.data || 0
    }
  } catch (e) {
    // 静默处理
  }
}

// 当前日期（用于 Hero 区域显示周几）
const currentDate = computed(() => {
  const now = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
})
watch(searchQuery, ()=>computedStatusCount())

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
   computedStatusCount()

   // 更新原生端目标数据，用于进后台时调度通知
   updateGoalsData(goals.value)

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
const computedStatusCount=()=>{
   goalFinishCount.value = searchFilteredGoals.value.filter(g => g.status === 'completed' || g.status === 'finished').length
    goalExpireCount.value = searchFilteredGoals.value.filter(g => g.status === 'expired').length
    goalIngCount.value = searchFilteredGoals.value.filter(g => g.status === 'in-progress' || g.status === 'in-progress').length
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
const goToNotifications = () => router.push('/mobile/notifications')
// const goToAddGoal = () => router.push('/add-goal') // Replaced above
const goToStatistics = () => router.push('/statistics')
const goToMessageCenter = () => router.push('/messages')
const goToGuide = () => router.push('/user-guide')
const goToAIAssistant = () => router.push('/AIAssistantWindow')

// Logic
const onTabClick = () => {
  // 切换 Tab 时关闭搜索栏
  showSearchBar.value = false
}
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

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'in-progress': return 'fire-o'
    case 'completed':
    case 'finished': return 'checked'
    case 'expired': return 'warning-o'
    default: return 'more-o'
  }
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

// ==========================================
// AI晨报逻辑
// ==========================================
const showAIMorningDialog = ref(false)
const aiMorningContent = ref('')
const aiCleanCOntentWatch=computed(()=>
aiMorningContent.value.replace('[推荐问题', '<br>'))


const generateAIMorning = async () => {
  try {
    // 1. 检查后端是否已生成过今天的晨报
    const checkRes = await postM('getAiMorning')
    if (isSuccess(checkRes) && checkRes.data.data) {
      // 已经生成过，直接展示
      aiMorningContent.value = checkRes.data.data.content.replace(/\n/g, '<br>')
      showAIMorningDialog.value = true
      return
    }

    // 2. 如果没生成，检查是否有目标数据
    if (!goals.value || goals.value.length === 0) {
      showToast('当前没有目标数据，无需生成晨报')
      return
    }

    showLoadingToast({
      message: 'AI 正在生成今日晨报...',
      forbidClick: true,
      duration: 0
    })

    // 构建当日 todo 汇总
    const todayStr = new Date().toLocaleDateString('zh-CN', { 
      year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' 
    })
    const goalsSummary = goals.value.map((g: any, i: number) =>
      `${i + 1}. ${g.title}（状态：${g.status}，截止：${g.deadline ? new Date(g.deadline).toLocaleDateString() : '未设置'}）`
    ).join('\n')

    // AI 提示词
    const prompt = `今天是${todayStr}。以下是我的所有目标列表：
${goalsSummary}

请你作为我的目标管理助手，为我生成一份**今日晨报**。要求：
1. 以“早安！今日目标晨报”作为开头，并显示今天的日期。
2. 用简短的中文列出我**今天最需要注意**的目标（即将到期、高优先级、或需要推进的）。
3. 每条用序号列出，并给出**一条具体可执行的行动建议**（例如：“复习第3章 – 建议上午集中1小时”）。
4. 如果今天没有特别紧急的目标，请给我一句鼓励的话，并建议一个常规复盘任务。
5. 总字数不超过500字，保持积极、清晰的语气。
6. 不要输出分析过程，直接输出晨报内容。`

    let aiResult = ''
    await callCozeAPI(prompt, (update: any) => {
      if (update.messageType === 'answer' && update.content) {
        aiResult = update.content
      }
    })

    if (!aiResult) {
      closeToast()
      showToast('AI 生成失败，请重试')
      return
    }

    // 清理 markdown
    const cleanResult = aiResult
      .replace(/\*\*/g, '')
      .replace(/#{1,6}\s/g, '')
      .trim()

    // 3. 保存到后端
    await postM('saveAiMorning', { content: cleanResult })
    
    closeToast()
    
    // 4. 展示弹窗
    aiMorningContent.value = cleanResult.replace(/\n/g, '<br>')
    showAIMorningDialog.value = true

  } catch (e) {
    console.error('生成晨报失败:', e)
    closeToast()
    showToast('生成晨报失败')
  }
}

// Lifecycle
onMounted(async () => {
  await fetchGoals()
  // 获取通知未读数
  await fetchNotificationUnread()
  // 暴露目标数据到 window，原生端进后台时读取
  updateGoalsData(goals.value)
})

// App 回到前台时刷新通知未读数
const onVisibilityChange = () => {
  if (document.visibilityState === 'visible') {
    fetchNotificationUnread()
  }
}
document.addEventListener('visibilitychange', onVisibilityChange)

// 组件卸载时移除监听
onUnmounted(() => {
  document.removeEventListener('visibilitychange', onVisibilityChange)
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
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(0, 201, 167, 0.25) 30%,
      rgba(0, 201, 167, 0.25) 70%,
      transparent 100%
    );
    pointer-events: none;
  }

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
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-brand-icon {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  flex-shrink: 0;
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
  position: relative;

  &:active {
    transform: scale(0.9);
    background: rgba(0, 201, 167, 0.12);
  }
}

/* 铃铛未读徽标 */
.bell-badge {
  position: absolute;
  top: 0;
  right: -2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #ff4757;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  line-height: 16px;
  text-align: center;
  pointer-events: none;
  box-shadow: 0 2px 6px rgba(255, 71, 87, 0.4);
}

/* ============================================
   Hero 区域（沉浸式大卡片，占屏 1/3）
   ============================================ */
.hero-section {
  margin-top: 8px;
  margin-bottom: 11px;
  padding: 0 4px;
}

.hero-card {
  position: relative;
  padding: 32px 28px 28px;
  border-radius: 28px;
  /* 动态渐变背景：品牌绿到深邃绿 */
  background: linear-gradient(145deg, #00c9a7 0%, #00a38a 25%, #008b78 55%, #00c9a7 100%);
  background-size: 300% 300%;
  animation: heroGradient 6s ease-in-out infinite;
  color: white;
  box-shadow:
    0 12px 48px rgba(0, 201, 167, 0.35),
    0 0 0 1px rgba(255, 255, 255, 0.15) inset,
    0 1px 0 rgba(255, 255, 255, 0.25) inset;
  overflow: hidden;
  min-height: 220px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* 动态渐变动画（范围加大） */
@keyframes heroGradient {
  0%   { background-position: 0% 50%; }
  25%  { background-position: 50% 0%; }
  50%  { background-position: 100% 50%; }
  75%  { background-position: 50% 100%; }
  100% { background-position: 0% 50%; }
}

/* 装饰层1 - 右上角大光晕 */
.hero-bg-layer {
  position: absolute;
  top: -30%;
  right: -15%;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.18) 0%, transparent 70%);
  pointer-events: none;
}

/* 装饰层2 - 左下角暖色光晕 */
.hero-bg-layer-2 {
  position: absolute;
  bottom: -25%;
  left: -12%;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,200,100,0.12) 0%, transparent 70%);
  pointer-events: none;
}

/* 装饰形状1 - 大圆 */
.hero-bg-shape {
  position: absolute;
  bottom: -25px;
  left: -20px;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  pointer-events: none;
}

/* 装饰形状2 - 中圆 */
.hero-bg-shape-2 {
  position: absolute;
  top: 18px;
  right: 25px;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  pointer-events: none;
}

/* 装饰网格（细微点阵纹） */
.hero-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle, rgba(255,255,255,0.06) 1px, transparent 1px);
  background-size: 24px 24px;
  pointer-events: none;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
}

.hero-content {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.hero-text {
  flex: 1;
}

.hero-date {
  margin: 0 0 10px;
  font-size: 13px;
  opacity: 0.8;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.hero-title {
  margin: 0 0 10px;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.5px;
  line-height: 1.15;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.hero-sub {
  margin: 0;
  font-size: 15px;
  opacity: 0.9;
  font-weight: 500;
  letter-spacing: 0.2px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

.hero-decoration {
  font-size: 64px;
  opacity: 0.12;
  transform: rotate(15deg);
  margin-top: -8px;
  filter: drop-shadow(0 0 20px rgba(255, 255, 255, 0.3));
}

/* 底部操作按钮 */
.hero-actions {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 12px;
}

.action-btn {
  border-radius: 16px;
  height: 48px;
  font-weight: 700;
  font-size: 15px;
  border: none;
  flex: 1;
}

.action-btn.primary {
  background: rgba(255, 255, 255, 0.95);
  color: #00a38a;
  backdrop-filter: blur(8px);
  box-shadow:
    0 4px 14px rgba(0, 0, 0, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.3) inset;
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(8px);
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
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
  padding: 9px 12px;
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

/* ---- 调试面板 ---- */
.debug-panel {
  margin-bottom: 12px;
}
.debug-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  color: #888;
  cursor: pointer;
  background: var(--bg-glass);
  border: 1px dashed var(--border-line);
}
.debug-actions {
  margin-top: 10px;
  padding: 14px;
  border-radius: 16px;
  background: var(--bg-glass);
  border: 1px solid var(--border-line);
}
.debug-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.debug-log {
  margin-top: 10px;
  max-height: 200px;
  overflow-y: auto;
  background: rgba(0,0,0,0.05);
  border-radius: 10px;
  padding: 10px;
}
.debug-line {
  font-size: 11px;
  font-family: 'SF Mono', 'Menlo', monospace;
  color: var(--text-secondary);
  padding: 2px 0;
  border-bottom: 0.5px solid var(--border-line);
  &:last-child { border-bottom: none; }
}

/* ============================================
   Tab 搜索触发按钮
   ============================================ */
.tab-search-trigger {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-right: 4px;
  transition: background 0.2s;

  &:active {
    background: rgba(0, 201, 167, 0.1);
  }
}

/* ============================================
   内嵌搜索栏（Tab 内）
   ============================================ */
.inline-search {
  padding: 8px 0;
  ::v-deep(.van-search__content) {
    background: var(--bg-glass);
    backdrop-filter: blur(10px);
    border-radius: 20px;
  }
}

/* ============================================
   加强版骨架屏（毛玻璃 + 呼吸光晕）
   ============================================ */
.skeleton-list.upgraded {
  padding: 20px 0;
}

.glass-skeleton {
  background: var(--bg-glass);
  backdrop-filter: blur(4px);
  padding: 20px 16px;
  border-radius: 16px;
  margin-bottom: 16px;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.04) 50%,
      transparent 100%
    );
    animation: skeletonShimmer 1.5s ease-in-out infinite;
  }

  &:last-child { margin-bottom: 0; }
}

.sk-line {
  height: 12px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  margin-bottom: 12px;

  &.w-70 { width: 70%; }
  &.w-40 { width: 40%; }
  &.w-90 { width: 90%; }

  &:last-child { margin-bottom: 0; }
}

@keyframes skeletonShimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* ============================================
   目标卡片入场动画（Stagger）
   ============================================ */
.goal-card-glass {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  background: transparent;
  transition: background 0.2s;
  cursor: pointer;
  animation: cardFadeIn 0.45s ease both;

  &:active {
    background: rgba(0, 201, 167, 0.04);
  }
}

.goal-list-wrap .van-swipe-cell:nth-child(1) .goal-card-glass { animation-delay: 0s; }
.goal-list-wrap .van-swipe-cell:nth-child(2) .goal-card-glass { animation-delay: 0.04s; }
.goal-list-wrap .van-swipe-cell:nth-child(3) .goal-card-glass { animation-delay: 0.08s; }
.goal-list-wrap .van-swipe-cell:nth-child(4) .goal-card-glass { animation-delay: 0.12s; }
.goal-list-wrap .van-swipe-cell:nth-child(5) .goal-card-glass { animation-delay: 0.16s; }
.goal-list-wrap .van-swipe-cell:nth-child(6) .goal-card-glass { animation-delay: 0.20s; }
.goal-list-wrap .van-swipe-cell:nth-child(7) .goal-card-glass { animation-delay: 0.24s; }
.goal-list-wrap .van-swipe-cell:nth-child(8) .goal-card-glass { animation-delay: 0.28s; }
.goal-list-wrap .van-swipe-cell:nth-child(9) .goal-card-glass { animation-delay: 0.32s; }
.goal-list-wrap .van-swipe-cell:nth-child(10) .goal-card-glass { animation-delay: 0.36s; }

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ============================================
   毛玻璃空状态卡片
   ============================================ */
.empty-glass-card {
  text-align: center;
  padding: 40px 24px;
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  border: 1px solid var(--border-line);
  border-radius: 20px;
  margin-top: 16px;

  &.small {
    padding: 24px;
    margin-top: 0;

    .empty-title {
      font-size: 14px;
    }
  }
}

.empty-icon-animated {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 201, 167, 0.08);
  color: rgba(255, 255, 255, 0.25);
  animation: emptyIconFloat 2.5s ease-in-out infinite;
}

@keyframes emptyIconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
}

.empty-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

/* ============================================
   Tab 内容过渡动画
   ============================================ */
.custom-tabs {
  ::v-deep(.van-tab__pane-wrapper) {
    transition: opacity 0.2s ease;
  }
}

/* 搜索栏弹出收起动画 */
.fade-slide-up-enter-active,
.fade-slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.fade-slide-up-enter-from,
.fade-slide-up-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
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

  /* 搜索框圆角调整 + 与 Tab 间距 */
  ::v-deep(.van-search__content) {
    background: var(--bg-glass);
    backdrop-filter: blur(10px);
    border-radius: 20px;
  }

  /* 下拉刷新图标颜色统一为品牌色 */
  ::v-deep(.van-pull-refresh__head) {
    color: #00c9a7;
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
  &.error {
    background: #c91b00;
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
  position: relative;
  overflow: hidden;
}

/* 弹窗顶部发光装饰线 */
.popup-glow-bar {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00c9a7, transparent);
  border-radius: 0 0 3px 3px;
  box-shadow: 0 0 12px rgba(0, 201, 167, 0.4);
}

/* 弹窗状态图标 */
.popup-status-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;

  &.in-progress {
    background: rgba(79, 142, 247, 0.2);
  }
  &.completed,
  &.finished {
    background: rgba(0, 201, 167, 0.2);
  }
  &.expired {
    background: rgba(255, 107, 107, 0.2);
  }
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
  padding: 16px 24px 32px;
  background: transparent;
}

.popup-actions-row {
  display: flex;
  gap: 12px;
  width: 100%;

  .edit-btn {
    flex: 1;
    background: #4f8ef7;
    color: white;
    border: none;
    border-radius: 16px;
    height: 46px;
    font-weight: 600;
    font-size: 15px;
    box-shadow: 0 4px 16px rgba(79, 142, 247, 0.3);
  }

  .finish-btn {
    flex: 1;
    background: linear-gradient(135deg, #00c9a7, #00b686);
    color: white;
    border: none;
    border-radius: 16px;
    height: 46px;
    font-weight: 600;
    font-size: 15px;
    box-shadow: 0 4px 16px rgba(0, 201, 167, 0.3);
  }
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

.ai-morning-content {
  padding: 0 20px 20px;
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.85);
  text-align: left;
  max-height: 50vh;
  overflow-y: auto;
  
  /* 优化内容中的列表样式 */
  :deep(br) {
    display: block;
    margin-bottom: 8px;
    content: "";
  }
}

/* ==========================================
 * 自定义毛玻璃弹窗 (AI 晨报)
 * ========================================== */
:deep(.custom-glass-dialog) {
  background: transparent !important;
  width: 320px;
  border-radius: 24px;
  overflow: hidden;

  .van-dialog__header {
    display: none; /* 隐藏原生头部 */
  }
  
  .van-dialog__content {
    background: transparent;
  }
}

.glass-dialog-inner {
  background: rgba(25, 25, 30, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  display: flex;
  flex-direction: column;
}

.glass-dialog-header {
  padding: 24px 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;

  .header-icon {
    width: 48px;
    height: 48px;
    background: rgba(0, 201, 167, 0.15);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    box-shadow: 0 0 20px rgba(0, 201, 167, 0.2);
  }

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
    letter-spacing: 0.5px;
  }
}

.glass-dialog-footer {
  padding: 10px 20px 24px;

  .glass-confirm-btn {
    background: linear-gradient(135deg, #00c9a7 0%, #00a88b 100%);
    border: none;
    color: #fff;
    font-weight: 600;
    font-size: 16px;
    height: 44px;
    box-shadow: 0 4px 15px rgba(0, 201, 167, 0.3);
    
    &:active {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }
}
</style>


