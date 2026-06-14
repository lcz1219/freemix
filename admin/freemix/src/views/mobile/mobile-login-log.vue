<template>
  <van-config-provider theme="dark">
    <div class="login-log-container dark-mode">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        fixed
        placeholder
        class="glass-nav"
        :border="false"
        z-index="100"
        :safe-area-inset-top="true"
      >
        <template #left>
          <div class="nav-back" @click="goBack">
            <van-icon name="arrow-left" />
            <span>返回</span>
          </div>
        </template>
        <template #title>
          <span class="nav-title">登录日志</span>
        </template>
      </van-nav-bar>

      <div class="content-wrapper">
        <!-- 筛选条件卡片 -->
        <div class="glass-card filter-card">
          <div class="filter-header" @click="showFilter = !showFilter">
            <div class="filter-header-left">
              <van-icon name="filter-o" color="#00c9a7" />
              <span>筛选条件</span>
            </div>
            <div class="filter-header-right">
              <span class="filter-tag" v-if="activeFilterCount > 0">{{ activeFilterCount }} 个筛选中</span>
              <van-icon :name="showFilter ? 'arrow-up' : 'arrow-down'" color="rgba(255,255,255,0.5)" />
            </div>
          </div>

          <div class="filter-body" v-show="showFilter">
            <!-- 时间范围 -->
            <div class="filter-row">
              <div class="filter-label">时间范围</div>
              <div class="filter-date-row">
                <van-field
                  v-model="displayStartDate"
                  placeholder="开始日期"
                  readonly
                  is-link
                  input-align="center"
                  class="date-field"
                  @click="showStartPicker = true"
                />
                <span class="date-separator">至</span>
                <van-field
                  v-model="displayEndDate"
                  placeholder="结束日期"
                  readonly
                  is-link
                  input-align="center"
                  class="date-field"
                  @click="showEndPicker = true"
                />
              </div>
            </div>

            <!-- 状态筛选 -->
            <div class="filter-row">
              <div class="filter-label">登录状态</div>
              <div class="status-options">
                <van-tag
                  v-for="opt in statusOptions"
                  :key="opt.value"
                  :class="['status-tag', { active: filter.status === opt.value }]"
                  round
                  @click="filter.status = opt.value"
                >{{ opt.label }}</van-tag>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="filter-actions">
              <van-button size="small" round plain class="reset-btn" @click="resetFilter">
                <van-icon name="replay" /> 重置
              </van-button>
              <van-button size="small" round class="search-btn" @click="searchLoginLogs">
                <van-icon name="search" /> 搜索
              </van-button>
            </div>
          </div>
        </div>

        <!-- 统计概览 -->
        <div class="stats-bar">
          <div class="stat-chip">
            <van-icon name="records-o" />
            <span>共 {{ total }} 条记录</span>
          </div>
          <div class="stat-chip success">
            <van-icon name="success" />
            <span>{{ successCount }} 次成功</span>
          </div>
          <div class="stat-chip fail">
            <van-icon name="fail" />
            <span>{{ failCount }} 次失败</span>
          </div>
        </div>

        <!-- 日志列表 -->
        <div class="log-list" v-if="loginLogs.length > 0">
          <div
            v-for="(log, index) in loginLogs"
            :key="log._id || index"
            class="glass-card log-item"
            @click="showLogDetail(log)"
          >
            <div class="log-row top-row">
              <div class="log-status-dot" :class="log.loginSuccess ? 'success' : 'fail'"></div>
              <div class="log-time">{{ formatDateTime(log.loginTime) }}</div>
              <van-tag
                :type="log.loginSuccess ? 'success' : 'danger'"
                size="small"
                round
                class="log-status-tag"
              >{{ log.loginSuccess ? '成功' : '失败' }}</van-tag>
            </div>
            <div class="log-row bottom-row">
              <div class="log-meta">
                <span class="meta-label">IP</span>
                <span class="meta-value mono">{{ log.ipAddress }}</span>
              </div>
              <van-icon name="arrow" color="rgba(255,255,255,0.3)" size="14" />
            </div>
            <div class="log-row device-row">
              <span class="meta-label">设备</span>
              <span class="meta-value">{{ log.browser || '-' }} · {{ log.os || '-' }}</span>
            </div>
            <div class="log-row error-row" v-if="!log.loginSuccess && log.errorMessage">
              <van-icon name="info-o" color="#ee0a24" size="14" />
              <span class="error-msg">{{ log.errorMessage }}</span>
            </div>
          </div>

          <!-- 加载更多 -->
          <div class="load-more" v-if="hasMore">
            <van-loading v-if="loading" size="20" color="#00c9a7" />
            <span v-else @click="loadMore" class="load-more-text">加载更多</span>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else-if="!loading">
          <van-icon name="records-o" size="48" color="rgba(255,255,255,0.2)" />
          <p>暂无登录记录</p>
          <van-button round plain size="small" @click="resetFilter" class="reset-empty-btn">
            重置筛选条件
          </van-button>
        </div>

        <!-- 加载中骨架 -->
        <div class="loading-state" v-if="loading && loginLogs.length === 0">
          <div class="skeleton-item" v-for="i in 5" :key="i">
            <div class="skeleton-line w-60"></div>
            <div class="skeleton-line w-80"></div>
            <div class="skeleton-line w-40"></div>
          </div>
        </div>
      </div>

      <!-- 日志详情弹窗 -->
      <van-action-sheet v-model:show="showDetail" :round="true" class="detail-sheet">
        <template #default>
          <div class="detail-content">
            <div class="detail-header">
              <div class="detail-status-icon" :class="detailData?.loginSuccess ? 'success' : 'fail'">
                <van-icon :name="detailData?.loginSuccess ? 'checked' : 'cross'" size="24" color="#fff" />
              </div>
              <div class="detail-title">
                {{ detailData?.loginSuccess ? '登录成功' : '登录失败' }}
              </div>
              <div class="detail-time">{{ formatDateTime(detailData?.loginTime) }}</div>
            </div>

            <div class="detail-body">
              <div class="detail-row">
                <span class="row-label">IP 地址</span>
                <span class="row-value mono">{{ detailData?.ipAddress || '-' }}</span>
              </div>
              <div class="detail-row">
                <span class="row-label">浏览器</span>
                <span class="row-value">{{ detailData?.browser || '-' }}</span>
              </div>
              <div class="detail-row">
                <span class="row-label">操作系统</span>
                <span class="row-value">{{ detailData?.os || '-' }}</span>
              </div>
              <div class="detail-row" v-if="detailData?.errorMessage">
                <span class="row-label">失败原因</span>
                <span class="row-value error-text">{{ detailData.errorMessage }}</span>
              </div>
            </div>
          </div>
        </template>
      </van-action-sheet>

      <!-- 日期选择器 -->
      <van-popup v-model:show="showStartPicker" position="bottom" round>
        <van-date-picker
          v-model="selectedStartDate"
          title="选择开始日期"
          :min-date="minDate"
          :max-date="maxDate"
          @confirm="onStartDateConfirm"
          @cancel="showStartPicker = false"
        />
      </van-popup>

      <van-popup v-model:show="showEndPicker" position="bottom" round>
        <van-date-picker
          v-model="selectedEndDate"
          title="选择结束日期"
          :min-date="minDate"
          :max-date="maxDate"
          @confirm="onEndDateConfirm"
          @cancel="showEndPicker = false"
        />
      </van-popup>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { getM } from '@/utils/request.js'
import { showToast } from 'vant'

const router = useRouter()
const store = useStore()
const currentUser = computed(() => store.state.user)

// 导航
const goBack = () => router.back()

// 数据
const loginLogs = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasMore = ref(true)

// 筛选状态（默认展开）
const showFilter = ref(true)
const filter = ref({
  startDate: null as Date | null,
  endDate: null as Date | null,
  status: 'all'
})

const statusOptions = [
  { label: '全部', value: 'all' },
  { label: '成功', value: 'success' },
  { label: '失败', value: 'failed' }
]

// 日期选择器
const showStartPicker = ref(false)
const showEndPicker = ref(false)
const selectedStartDate = ref<string[]>([])
const selectedEndDate = ref<string[]>([])
const minDate = new Date(2020, 0, 1)
const maxDate = new Date()

const displayStartDate = computed(() => {
  return filter.value.startDate
    ? `${filter.value.startDate.getFullYear()}-${String(filter.value.startDate.getMonth() + 1).padStart(2, '0')}-${String(filter.value.startDate.getDate()).padStart(2, '0')}`
    : ''
})
const displayEndDate = computed(() => {
  return filter.value.endDate
    ? `${filter.value.endDate.getFullYear()}-${String(filter.value.endDate.getMonth() + 1).padStart(2, '0')}-${String(filter.value.endDate.getDate()).padStart(2, '0')}`
    : ''
})

const activeFilterCount = computed(() => {
  let count = 0
  if (filter.value.startDate) count++
  if (filter.value.endDate) count++
  if (filter.value.status !== 'all') count++
  return count
})

const successCount = computed(() => loginLogs.value.filter(l => l.loginSuccess).length)
const failCount = computed(() => loginLogs.value.filter(l => !l.loginSuccess).length)

// 详情弹窗
const showDetail = ref(false)
const detailData = ref<any>(null)
const showLogDetail = (log: any) => {
  detailData.value = log
  showDetail.value = true
}

// 日期回调
const onStartDateConfirm = ({ selectedValues }: any) => {
  const [year, month, day] = selectedValues
  filter.value.startDate = new Date(year, month - 1, day)
  showStartPicker.value = false
}
const onEndDateConfirm = ({ selectedValues }: any) => {
  const [year, month, day] = selectedValues
  filter.value.endDate = new Date(year, month - 1, day)
  showEndPicker.value = false
}

// 格式化
const formatDateTime = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  }).format(date)
}

// 数据获取
const fetchLoginLogs = async (append = false) => {
  if (!currentUser.value?.id) return

  loading.value = true
  try {
    const params: any = {
      userId: currentUser.value.id,
      page: currentPage.value,
      size: pageSize.value
    }

    let response
    if (filter.value.startDate && filter.value.endDate) {
      response = await getM('api/login-log/date-range', {
        ...params,
        startDate: filter.value.startDate.toISOString().split('T')[0],
        endDate: filter.value.endDate.toISOString().split('T')[0]
      })
    } else {
      response = await getM('api/login-log/user', params)
    }

    if (response.data?.code === 200) {
      let logs = response.data.data || []
      if (filter.value.status !== 'all') {
        logs = logs.filter((log: any) =>
          filter.value.status === 'success' ? log.loginSuccess : !log.loginSuccess
        )
      }
      if (append) {
        loginLogs.value = [...loginLogs.value, ...logs]
      } else {
        loginLogs.value = logs
      }
      total.value = logs.length
      hasMore.value = logs.length >= pageSize.value
    } else {
      if (!append) loginLogs.value = []
      total.value = 0
      hasMore.value = false
    }
  } catch (error) {
    console.error('获取登录日志失败:', error)
    if (!append) loginLogs.value = []
  } finally {
    loading.value = false
  }
}

const searchLoginLogs = () => {
  currentPage.value = 1
  fetchLoginLogs(false)
}

const loadMore = () => {
  currentPage.value++
  fetchLoginLogs(true)
}

const resetFilter = () => {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  filter.value = { startDate, endDate, status: 'all' }
  currentPage.value = 1
  fetchLoginLogs(false)
}

onMounted(() => {
  resetFilter()
})
</script>

<style scoped lang="scss">
$theme-green: #00c9a7;
$bg-primary: #0a0a0f;
$glass-bg: rgba(30, 30, 35, 0.75);
$text-primary: #e8e8f0;
$text-secondary: rgba(255, 255, 255, 0.5);
$border-line: rgba(255, 255, 255, 0.06);

.login-log-container {
  min-height: 100vh;
  background: linear-gradient(160deg, #0a0a0f 0%, #111118 30%, #0e0e1a 70%, #0a0a0f 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
}

/* ========== 导航栏 ========== */
.glass-nav {
  background: $glass-bg;
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);

  .nav-back {
    display: flex;
    align-items: center;
    gap: 4px;
    color: $theme-green;
    font-size: 14px;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 600;
    color: $text-primary;
  }
}

.content-wrapper {
  padding: 16px;
  padding-bottom: 40px;
}

/* ========== 筛选卡片 ========== */
.glass-card {
  background: $glass-bg;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid $border-line;
  border-radius: 16px;
}

.filter-card {
  margin-bottom: 12px;
  overflow: hidden;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  cursor: pointer;

  .filter-header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    color: $text-primary;
    font-size: 15px;
    font-weight: 600;
  }

  .filter-header-right {
    display: flex;
    align-items: center;
    gap: 8px;

    .filter-tag {
      font-size: 12px;
      color: $theme-green;
      background: rgba($theme-green, 0.1);
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
}

.filter-body {
  padding: 0 16px 16px;
}

.filter-row {
  margin-bottom: 14px;

  .filter-label {
    font-size: 13px;
    color: $text-secondary;
    margin-bottom: 8px;
  }
}

.filter-date-row {
  display: flex;
  align-items: center;
  gap: 8px;

  .date-field {
    flex: 1;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 10px;
    padding: 0 12px;
    height: 40px;
    border: 1px solid $border-line;

    :deep(.van-field__control) {
      color: $text-primary !important;
      font-size: 13px;
      text-align: center;
    }

    :deep(.van-field__right-icon) {
      .van-icon {
        color: rgba(255, 255, 255, 0.3);
      }
    }
  }

  .date-separator {
    color: $text-secondary;
    font-size: 13px;
    flex-shrink: 0;
  }
}

.status-options {
  display: flex;
  gap: 8px;

  .status-tag {
    padding: 6px 16px;
    background: rgba(255, 255, 255, 0.05);
    color: $text-secondary;
    font-size: 13px;
    border: 1px solid transparent;
    cursor: pointer;

    &.active {
      background: rgba($theme-green, 0.15);
      color: $theme-green;
      border-color: rgba($theme-green, 0.3);
    }
  }
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;

  .reset-btn {
    flex: 1;
    color: $text-secondary !important;
    border-color: rgba(255, 255, 255, 0.15) !important;
    font-size: 13px;
    height: 36px;
  }

  .search-btn {
    flex: 1;
    background: $theme-green !important;
    border: none !important;
    color: #000 !important;
    font-size: 13px;
    font-weight: 600;
    height: 36px;
  }
}

/* ========== 统计栏 ========== */
.stats-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;

  .stat-chip {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    padding: 8px 6px;
    background: $glass-bg;
    backdrop-filter: blur(20px);
    border-radius: 12px;
    border: 1px solid $border-line;
    font-size: 12px;
    color: $text-secondary;

    &.success {
      color: $theme-green;
    }

    &.fail {
      color: #ee0a24;
    }
  }
}

/* ========== 日志列表 ========== */
.log-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.log-item {
  padding: 14px 16px;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.7;
  }
}

.log-row {
  display: flex;
  align-items: center;
  gap: 8px;

  &.top-row {
    margin-bottom: 8px;
  }

  &.bottom-row {
    justify-content: space-between;
    margin-bottom: 6px;
  }

  &.device-row {
    margin-bottom: 4px;
  }

  &.error-row {
    margin-top: 6px;
    padding: 6px 10px;
    background: rgba(238, 10, 36, 0.08);
    border-radius: 8px;
    gap: 6px;

    .error-msg {
      font-size: 12px;
      color: #ee0a24;
      flex: 1;
    }
  }
}

.log-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;

  &.success { background: $theme-green; }
  &.fail { background: #ee0a24; }
}

.log-time {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
}

.log-status-tag {
  font-size: 11px;
}

.log-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-label {
  font-size: 12px;
  color: $text-secondary;
  flex-shrink: 0;
}

.meta-value {
  font-size: 13px;
  color: $text-primary;

  &.mono {
    font-family: 'SF Mono', 'Menlo', monospace;
  }
}

/* ========== 加载更多 ========== */
.load-more {
  text-align: center;
  padding: 16px 0;

  .load-more-text {
    font-size: 13px;
    color: $text-secondary;
    cursor: pointer;
  }
}

/* ========== 空状态 ========== */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: $text-secondary;

  p {
    margin: 12px 0 16px;
    font-size: 14px;
  }

  .reset-empty-btn {
    color: $theme-green !important;
    border-color: rgba($theme-green, 0.3) !important;
    font-size: 13px;
  }
}

/* ========== 加载骨架 ========== */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-item {
  background: $glass-bg;
  border-radius: 16px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-line {
  height: 12px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  animation: shimmer 1.5s infinite;

  &.w-60 { width: 60%; }
  &.w-80 { width: 80%; }
  &.w-40 { width: 40%; }
}

@keyframes shimmer {
  0% { opacity: 0.3; }
  50% { opacity: 0.6; }
  100% { opacity: 0.3; }
}

/* ========== 详情弹窗 ========== */
.detail-sheet {
  :deep(.van-action-sheet__content) {
    background: rgba(20, 20, 25, 0.98);
    backdrop-filter: blur(20px);
    border-radius: 20px 20px 0 0;
  }
}

.detail-content {
  padding: 24px 20px 40px;
}

.detail-header {
  text-align: center;
  margin-bottom: 24px;

  .detail-status-icon {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px;

    &.success { background: $theme-green; }
    &.fail { background: #ee0a24; }
  }

  .detail-title {
    font-size: 18px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 4px;
  }

  .detail-time {
    font-size: 13px;
    color: $text-secondary;
  }
}

.detail-body {
  .detail-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 0;
    border-bottom: 0.5px solid $border-line;

    &:last-child {
      border-bottom: none;
    }

    .row-label {
      font-size: 14px;
      color: $text-secondary;
    }

    .row-value {
      font-size: 14px;
      color: $text-primary;
      text-align: right;
      max-width: 55%;
      word-break: break-all;

      &.mono {
        font-family: 'SF Mono', 'Menlo', monospace;
        font-size: 13px;
      }

      &.error-text {
        color: #ee0a24;
      }
    }
  }
}
</style>
