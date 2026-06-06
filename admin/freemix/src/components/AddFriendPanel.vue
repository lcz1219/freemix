<template>
  <div class="add-friend-panel">
    <!-- 面板头部 -->
    <div class="panel-header">
      <span class="panel-title">添加好友</span>
      <n-button text @click="$emit('close')" class="close-btn">
        <n-icon size="20"><CloseCircleOutline /></n-icon>
      </n-button>
    </div>

    <!-- 标签切换 -->
    <div class="panel-tabs">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'search' }"
        @click="activeTab = 'search'"
      >
        搜索用户
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'requests' }"
        @click="activeTab = 'requests'"
      >
        好友申请
        <span v-if="pendingCount > 0" class="tab-badge">{{ pendingCount > 99 ? '99+' : pendingCount }}</span>
      </div>
    </div>

    <!-- 搜索用户 Tab -->
    <div v-if="activeTab === 'search'" class="panel-body">
      <n-input
        v-model:value="searchKeyword"
        placeholder="输入用户名搜索..."
        clearable
        @keydown.enter="handleSearch"
      >
        <template #prefix>
          <n-icon><Search /></n-icon>
        </template>
        <template #suffix>
          <n-button text type="primary" size="tiny" @click="handleSearch" :loading="searchLoading">
            搜索
          </n-button>
        </template>
      </n-input>

      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" class="results-list">
        <div
          v-for="user in searchResults"
          :key="user.username"
          class="result-item"
        >
          <div class="result-user">
            <n-avatar
              round
              :size="36"
              :src="showAvatar(user.avatarUrl)"
            />
            <div class="result-info">
              <span class="result-name">{{ user.chinesename || user.username }}</span>
              <span class="result-username">@{{ user.username }}</span>
            </div>
          </div>
          <n-button
            v-if="!user.requestStatus"
            size="small"
            type="primary"
            @click="sendFriendRequest(user)"
            :loading="user._sending"
          >
            添加
          </n-button>
          <n-tag v-else-if="user.requestStatus === 'sent'" type="info" size="small">
            已申请
          </n-tag>
          <n-tag v-else-if="user.requestStatus === 'received'" type="warning" size="small">
            待确认
          </n-tag>
        </div>
      </div>

      <n-empty
        v-if="searched && searchResults.length === 0"
        description="未找到用户"
        size="small"
        class="search-empty"
      />
    </div>

    <!-- 好友申请 Tab -->
    <div v-if="activeTab === 'requests'" class="panel-body">
      <div v-if="pendingRequests.length === 0" class="empty-requests">
        <n-empty description="暂无待处理的申请" size="small" />
      </div>

      <div
        v-for="req in pendingRequests"
        :key="req.requestId"
        class="request-item"
      >
        <div class="request-user">
          <n-avatar
            round
            :size="40"
            :src="showAvatar(req.avatarUrl)"
          />
          <div class="request-info">
            <span class="request-name">{{ req.chinesename || req.username }}</span>
            <span class="request-username">@{{ req.username }}</span>
            <span class="request-time">{{ formatTime(req.requestTime) }}</span>
          </div>
        </div>
        <div class="request-actions">
          <n-button
            size="small"
            type="success"
            @click="acceptRequest(req)"
            :loading="req._loading"
          >
            同意
          </n-button>
          <n-button
            size="small"
            type="error"
            text
            @click="rejectRequest(req)"
            :disabled="req._loading"
          >
            拒绝
          </n-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import {
  NButton, NInput, NIcon, NAvatar, NTag, NEmpty
} from 'naive-ui'
import {CloseCircleOutline, Search} from '@vicons/ionicons5'
import { getM, postM, isSuccess, baseURL } from '@/utils/request'

const emit = defineEmits(['close', 'friend-added'])

const message = useMessage()

// 状态
const activeTab = ref('search')
const searchKeyword = ref('')
const searchResults = ref([])
const searched = ref(false)
const searchLoading = ref(false)
const pendingRequests = ref([])
const pendingCount = ref(0)

// 头像显示
const showAvatar = (url) => {
  if (!url) return 'https://api.dicebear.com/7.x/miniavs/svg?seed=default'
  return url.startsWith('http') ? url : `${baseURL()}${url}`
}

// 时间格式化
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 搜索用户
const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return

  searchLoading.value = true
  searched.value = true
  try {
    const res = await getM('friend/search', { keyword })
    if (isSuccess(res)) {
      searchResults.value = res.data.data || []
    }
  } catch (e) {
    console.error('搜索用户失败', e)
  } finally {
    searchLoading.value = false
  }
}

// 发送好友申请
const sendFriendRequest = async (user) => {
  user._sending = true
  try {
    const res = await postM('friend/request', { toUser: user.username })
    if (isSuccess(res)) {
      message.success('好友申请已发送')
      user.requestStatus = 'sent'
    }
  } catch (e) {
    console.error('发送申请失败', e)
  } finally {
    user._sending = false
  }
}

// 加载待处理申请
const loadPendingRequests = async () => {
  try {
    const res = await getM('friend/pending')
    if (isSuccess(res)) {
      pendingRequests.value = (res.data.data || []).map(
        r => ({ ...r, _loading: false })
      )
    }
  } catch (e) {
    console.error('加载申请列表失败', e)
  }
}

// 加载待处理数量
const loadPendingCount = async () => {
  try {
    const res = await getM('friend/pending/count')
    if (isSuccess(res)) {
      pendingCount.value = res.data.data || 0
    }
  } catch (e) {
    console.error('加载申请数量失败', e)
  }
}

// 通过好友申请
const acceptRequest = async (req) => {
  req._loading = true
  try {
    const res = await postM('friend/accept', { requestId: req.requestId })
    if (isSuccess(res)) {
      message.success('已通过好友申请')
      pendingRequests.value = pendingRequests.value.filter(
        r => r.requestId !== req.requestId
      )
      await loadPendingCount()
      emit('friend-added')
    }
  } catch (e) {
    console.error('通过申请失败', e)
  } finally {
    req._loading = false
  }
}

// 拒绝好友申请
const rejectRequest = async (req) => {
  req._loading = true
  try {
    const res = await postM('friend/reject', { requestId: req.requestId })
    if (isSuccess(res)) {
      message.info('已拒绝好友申请')
      pendingRequests.value = pendingRequests.value.filter(
        r => r.requestId !== req.requestId
      )
      await loadPendingCount()
    }
  } catch (e) {
    console.error('拒绝申请失败', e)
  } finally {
    req._loading = false
  }
}

// 暴露初始化方法给父组件调用
const init = async () => {
  await Promise.all([loadPendingRequests(), loadPendingCount()])
}

defineExpose({ init })
</script>

<style scoped>
.add-friend-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
}

/* 头部 */
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  color: var(--text-color-2);
}

/* 标签切换 */
.panel-tabs {
  display: flex;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 13px;
  color: var(--text-color-2);
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
}

.tab-item.active {
  color: #00c9a7;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 28px;
  height: 2px;
  background: #00c9a7;
  border-radius: 1px;
}

.tab-badge {
  background: #f5222d;
  color: #fff;
  font-size: 10px;
  border-radius: 10px;
  padding: 1px 5px;
  margin-left: 4px;
  vertical-align: top;
}

/* 面板内容 */
.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

/* 搜索结果 */
.results-list {
  margin-top: 12px;
}

.result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  border-radius: 8px;
  transition: background 0.15s;
}

.result-item:hover {
  background: var(--hover-color);
}

.result-user {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.result-info {
  display: flex;
  flex-direction: column;
}

.result-name {
  font-size: 14px;
  font-weight: 500;
}

.result-username {
  font-size: 12px;
  color: var(--text-color-3);
}

.search-empty {
  margin-top: 40px;
}

/* 申请列表 */
.empty-requests {
  margin-top: 60px;
}

.request-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 10px;
  background: var(--hover-color);
  margin-bottom: 8px;
}

.request-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.request-info {
  display: flex;
  flex-direction: column;
}

.request-name {
  font-size: 14px;
  font-weight: 500;
}

.request-username {
  font-size: 12px;
  color: var(--text-color-3);
}

.request-time {
  font-size: 11px;
  color: var(--text-color-3);
  margin-top: 2px;
}

.request-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
