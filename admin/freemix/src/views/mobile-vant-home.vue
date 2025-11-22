<template>
  <van-config-provider :theme="currentTheme" :theme-vars="themeVars">
    <div class="mobile-home">
      <van-nav-bar title="FreeMix" right-text="设置" @click-right="goToSettings">
        <template #left>
          <van-icon :name="isDark ? 'sun-o' : 'moon-o'" size="18" @click="toggleTheme" />
        </template>
      </van-nav-bar>

      <section class="hero">
        <div class="hero-text">
          <h1>欢迎使用 FreeMix</h1>
          <p>你的目标管理与协作空间</p>
        </div>
        <div class="hero-actions">
          <van-button type="primary" round class="primary-btn" @click="goToAddGoal">添加新目标</van-button>
          <van-button plain round class="secondary-btn" @click="goToStatistics">查看统计</van-button>
        </div>
      </section>

      <section class="quick-actions">
        <van-grid :column-num="4" :border="false">
          <van-grid-item icon="apps-o" text="目标" @click="goToAddGoal" />
          <van-grid-item icon="chart-trending-o" text="统计" @click="goToStatistics" />
          <van-grid-item icon="chat-o" text="消息" @click="goToMessageCenter" />
          <van-grid-item icon="question-o" text="指南" @click="goToGuide" />
        </van-grid>
      </section>

      <section class="stats-section">
        <div class="stat-card">
          <div class="stat-icon">
            <van-icon name="underway-o" size="24" color="#00c9a7" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ goalIngCount }}</div>
            <div class="stat-label">进行中</div>
            <van-progress :percentage="progressOngoing" stroke-width="6" />
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <van-icon name="checked" size="24" color="#00c9a7" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ goalFinishCount }}</div>
            <div class="stat-label">已完成</div>
            <van-progress :percentage="progressFinished" stroke-width="6" />
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <van-icon name="fail" size="24" color="#00c9a7" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ goalExpireCount }}</div>
            <div class="stat-label">已过期</div>
            <van-progress :percentage="progressExpired" stroke-width="6" />
          </div>
        </div>
      </section>

      <van-pull-refresh v-model="isRefreshing" @refresh="refreshGoals">
        <section class="goals-section">
          <van-tabs v-model:active="activeTab" sticky>
            <van-tab title="全部">
              <div v-if="isLoading" class="skeleton-wrap">
                <van-skeleton row="3" animated />
              </div>
              <div v-else>
                <van-list v-model:loading="listLoading" :finished="listFinished" finished-text="没有更多了" @load="loadMore">
                  <van-swipe-cell v-for="goal in goals" :key="goal.id">
                    <van-cell is-link @click="showGoalDetail(goal)">
                      <template #title>
                        <div class="goal-title">{{ goal.title }}</div>
                      </template>
                      <template #label>
                        <div class="goal-meta">
                          <span>{{ formatDate(goal.deadline) }} 截止</span>
                          <van-tag :type="getGoalStatusType(goal.status)" plain class="status-tag">{{ getGoalStatusText(goal.status) }}</van-tag>
                        </div>
                        <div class="goal-progress">
                          <van-progress :percentage="goalProgress(goal)" stroke-width="6" />
                        </div>
                      </template>
                      <template #right-icon>
                        <van-icon name="arrow" />
                      </template>
                    </van-cell>
                    <template #right>
                      <van-button square type="primary" class="swipe-btn" @click="markGoalFinished(goal)">完成</van-button>
                    </template>
                  </van-swipe-cell>
                </van-list>
              </div>
            </van-tab>
            <van-tab title="进行中">
              <div v-if="isLoading" class="skeleton-wrap"><van-skeleton row="2" animated /></div>
              <div v-else>
                <van-empty v-if="goals.filter(g => g.status === 'ongoing').length === 0" description="暂无进行中的目标" />
                <div v-else>
                  <van-cell v-for="goal in goals.filter(g => g.status === 'ongoing')" :key="goal.id" is-link @click="showGoalDetail(goal)">
                    <template #title><div class="goal-title">{{ goal.title }}</div></template>
                    <template #label><div class="goal-meta"><span>{{ formatDate(goal.deadline) }} 截止</span></div></template>
                  </van-cell>
                </div>
              </div>
            </van-tab>
            <van-tab title="已完成">
              <div v-if="isLoading" class="skeleton-wrap"><van-skeleton row="2" animated /></div>
              <div v-else>
                <van-empty v-if="goals.filter(g => g.status === 'finished').length === 0" description="暂无已完成的目标" />
                <div v-else>
                  <van-cell v-for="goal in goals.filter(g => g.status === 'finished')" :key="goal.id" is-link @click="showGoalDetail(goal)">
                    <template #title><div class="goal-title">{{ goal.title }}</div></template>
                    <template #label><div class="goal-meta"><span>{{ formatDate(goal.deadline) }} 截止</span></div></template>
                  </van-cell>
                </div>
              </div>
            </van-tab>
          </van-tabs>
        </section>
      </van-pull-refresh>

      <van-tabbar v-model="activeTabbar" route active-color="#00c9a7" inactive-color="#9aa0a6">
        <van-tabbar-item replace to="/home" icon="home-o">首页</van-tabbar-item>
        <van-tabbar-item replace to="/goals" icon="apps-o">目标</van-tabbar-item>
        <van-tabbar-item replace to="/profile" icon="user-o">我的</van-tabbar-item>
      </van-tabbar>

      <van-popup v-model:show="showDetailModal" position="bottom" :style="{ height: '80%' }" round>
        <div class="goal-detail">
          <van-nav-bar title="目标详情" left-text="返回" @click-left="showDetailModal = false" />
          <div class="detail-content">
            <h2>{{ selectedGoal?.title }}</h2>
            <p>{{ selectedGoal?.description }}</p>
            <van-divider />
            <div class="sub-goals">
              <h3>子目标</h3>
              <van-checkbox-group v-model="checkedSubGoals">
                <van-cell-group>
                  <van-cell v-for="subGoal in (selectedGoal?.subGoals || [])" :key="subGoal.id" clickable @click="toggleSubGoal(subGoal.id)">
                    <template #title>
                      <van-checkbox :name="subGoal.id" @click.stop checked-color="#00c9a7">{{ subGoal.title }}</van-checkbox>
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
import { useUser } from '@/hooks'

const isDark = ref(false)
const currentTheme = computed(() => (isDark.value ? 'dark' : 'light'))
const themeVars = computed(() => ({
  primaryColor: '#00c9a7',
  backgroundColor: isDark.value ? '#0f0f13' : '#f5f5f5',
  textColor: isDark.value ? '#e2e2ea' : '#252525',
  navBarTextColor: isDark.value ? '#e2e2ea' : '#252525',
  navBarBackgroundColor: isDark.value ? '#0f0f13' : '#ffffff',
  tabbarActiveColor: '#00c9a7',
  tabbarInactiveColor: isDark.value ? '#6b7280' : '#9aa0a6',
  cellBackgroundColor: isDark.value ? '#14141a' : '#ffffff'
}))

const router = useRouter()

const activeTab = ref(0)
const activeTabbar = ref(0)
const goals = ref<any[]>([])
const showDetailModal = ref(false)
const selectedGoal = ref<any | null>(null)
const checkedSubGoals = ref<any[]>([])

const goalFinishCount = ref(0)
const goalExpireCount = ref(0)
const goalIngCount = ref(0)

const isLoading = ref(true)
const isRefreshing = ref(false)
const listLoading = ref(false)
const listFinished = ref(true)
const { userInfo } = useUser();
onMounted(async () => {
  await fetchGoals()
})

const fetchGoals = async () => {
  try {
    isLoading.value = true
    const response = await await getMPaths("getGoals", userInfo.value.username, "正在获取目标数据...");
    goals.value = response.data || []
    goalFinishCount.value = goals.value.filter(g => g.status === 'finished').length
    goalExpireCount.value = goals.value.filter(g => g.status === 'expired').length
    goalIngCount.value = goals.value.filter(g => g.status === 'ongoing').length
    listFinished.value = true
  } catch (error) {
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

const goToSettings = () => router.push('/settings')
const goToAddGoal = () => router.push('/add-goal')
const goToStatistics = () => router.push('/statistics')
const goToMessageCenter = () => router.push('/messages')
const goToGuide = () => router.push('/user-guide')

const showGoalDetail = (goal: any) => {
  selectedGoal.value = goal
  const subs = goal?.subGoals || []
  checkedSubGoals.value = subs.filter((sub: any) => sub.completed).map((sub: any) => sub.id)
  showDetailModal.value = true
}

const markGoalFinished = async (goal: any) => {
  try {
    await getMPaths('api/goal/finish', goal.id)
    await fetchGoals()
    showToast('目标已标记完成')
  } catch (error) {
    showToast('标记失败')
  }
}

const toggleSubGoal = async (id: any) => {
  const index = checkedSubGoals.value.indexOf(id)
  if (index !== -1) {
    checkedSubGoals.value.splice(index, 1)
  } else {
    checkedSubGoals.value.push(id)
  }
  await finishChildGoal(id)
}

const finishChildGoal = async (childId: any) => {
  try {
    await getMPaths('api/goal/finish', childId)
    showToast('子目标状态更新成功')
  } catch (error) {
    showToast('更新子目标状态失败')
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return `${date.getMonth() + 1}-${date.getDate()}`
}

const getGoalStatusType = (status: string) => {
  switch (status) {
    case 'ongoing': return 'primary'
    case 'finished': return 'success'
    case 'expired': return 'danger'
    default: return 'default'
  }
}

const getGoalStatusText = (status: string) => {
  switch (status) {
    case 'ongoing': return '进行中'
    case 'finished': return '已完成'
    case 'expired': return '已过期'
    default: return '未知'
  }
}

const goalProgress = (goal: any) => {
  const subs = goal?.subGoals || []
  if (!subs.length) return goal.status === 'finished' ? 100 : 0
  const done = subs.filter((s: any) => s.completed).length
  return Math.round((done / subs.length) * 100)
}

const total = computed(() => goals.value.length || 1)
const progressFinished = computed(() => Math.round((goalFinishCount.value / total.value) * 100))
const progressExpired = computed(() => Math.round((goalExpireCount.value / total.value) * 100))
const progressOngoing = computed(() => Math.round((goalIngCount.value / total.value) * 100))
</script>

<style scoped>
.mobile-home {
  min-height: 100vh;
  background-color: black;
  padding-bottom: 60px;
}

.dark .mobile-home {
  background-color: #0f0f13;
}
.hero{padding:24px 16px;background:linear-gradient(135deg,#0f0f13,#162023);border-radius:12px;margin:12px;color:#e2e2ea}
.hero-text h1{font-size:22px;margin-bottom:6px;color:#e2e2ea}
.hero-text p{color:#9aa0a6;margin-bottom:16px}
.hero-actions{display:flex;gap:12px}
.primary-btn{background-color:#00c9a7;border-color:#00c9a7}
.secondary-btn{color:#00c9a7;border-color:#00c9a7}
.quick-actions{padding:8px 12px}
.stats-section{display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;padding:12px}
.stat-card{display:flex;gap:12px;align-items:center;background:var(--van-background-color-light);border:1px solid var(--van-border-color);border-radius:12px;padding:12px}
.stat-number{font-size:18px;font-weight:700;color:var(--van-text-color)}
.stat-label{font-size:12px;color:var(--van-text-color-3)}
.goal-title{font-weight:600;color:var(--van-text-color)}
.goal-meta{display:flex;justify-content:space-between;align-items:center;margin-top:4px}
.goal-meta span{font-size:12px;color:var(--van-text-color-3)}
.status-tag{font-size:10px}
.goal-progress{margin-top:8px}
.skeleton-wrap{padding:16px}
.detail-content{padding:15px}
.detail-content h2{color:var(--van-text-color);margin-bottom:8px}
.detail-content p{color:var(--van-text-color-2);line-height:1.6;margin-bottom:16px}
.sub-goals h3{color:var(--van-text-color);margin-bottom:12px}
.dark .hero{background:linear-gradient(135deg,#0f0f13,#1b1f23)}
.light .hero {
  background: linear-gradient(135deg, #f5f7fa, #e0e5ec);
}
.swipe-btn{height:100%}
</style>