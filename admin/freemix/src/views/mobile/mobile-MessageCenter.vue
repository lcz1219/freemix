<template>
  <div class="mobile-message-center-apple">
    <van-nav-bar
      title="消息中心"
      left-arrow
      @click-left="goBack"
      class="apple-nav-bar"
    >
      <template #right>
        <van-icon name="ellipsis" size="22" @click="showMoreActions = true" />
      </template>
    </van-nav-bar>

    <div class="apple-tabs-container">
      <van-tabs v-model:active="activeTab" shrink animated>
        <van-tab>
          <template #title>
            <van-badge :content="unreadCount > 0 ? unreadCount : ''" class="apple-badge">全部</van-badge>
          </template>
        </van-tab>
        <van-tab>
          <template #title>
            <van-badge :content="systemUnreadCount > 0 ? systemUnreadCount : ''" class="apple-badge">系统通知</van-badge>
          </template>
        </van-tab>
        <van-tab>
          <template #title>
            <van-badge :content="goalUnreadCount > 0 ? goalUnreadCount : ''" class="apple-badge">目标提醒</van-badge>
          </template>
        </van-tab>
        <van-tab>
          <template #title>
            <van-badge :content="collabUnreadCount > 0 ? collabUnreadCount : ''" class="apple-badge">协作消息</van-badge>
          </template>
        </van-tab>
      </van-tabs>
    </div>

    <div class="apple-list-container">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="apple-pull-refresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <van-swipe-cell
            v-for="message in filteredMessages"
            :key="message.id"
            class="apple-swipe-cell"
          >
            <div :class="['apple-message-card', { 'unread': !message.read }]" @click="openMessage(message)">
              <div class="card-header">
                <div class="card-title-row">
                  <van-icon :name="getMessageTypeIcon(message.type)" size="18" class="card-icon" />
                  <h4 class="card-title">{{ message.title }}</h4>
                </div>
                <span class="card-time">{{ formatTime(message.createdAt) }}</span>
              </div>
              <p class="card-content">{{ message.content }}</p>
              <div v-if="!message.read" class="unread-dot"></div>
            </div>
            <template #left>
              <van-button square type="primary" text="已读" class="swipe-btn-read" @click="markAsRead(message)" />
            </template>
            <template #right>
              <van-button square type="danger" text="删除" class="swipe-btn-delete" @click="deleteMessage(message)" />
            </template>
          </van-swipe-cell>
        </van-list>
      </van-pull-refresh>
    </div>

    <van-empty v-if="filteredMessages.length === 0 && !loading" description="暂无消息" class="apple-empty-state" />

    <van-action-sheet
      v-model:show="showMoreActions"
      :actions="moreActions"
      cancel-text="取消"
      class="apple-action-sheet"
      @select="onActionSelect"
    />

    <van-popup
      v-model:show="showMessageDetail"
      position="bottom"
      round
      closeable
      :style="{ height: '85%' }"
      class="apple-message-popup"
    >
      <div v-if="selectedMessage" class="apple-popup-content">
        <div class="popup-header">
          <van-icon :name="getMessageTypeIcon(selectedMessage.type)" size="24" class="popup-icon" />
          <h3 class="popup-title">{{ selectedMessage.title }}</h3>
          <span class="popup-time">{{ formatTime(selectedMessage.createdAt) }}</span>
        </div>
        <div class="popup-body">
          <p class="popup-text">{{ selectedMessage.content }}</p>
        </div>
        <div v-if="selectedMessage.actionUrl" class="popup-footer">
          <van-button
            type="primary"
            block
            round
            class="apple-main-btn"
            @click="handleMessageAction(selectedMessage)"
          >
            查看详情
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/hooks/useUser'
import { useSettings } from '@/hooks/useSettings'
import { getM, postM, isSuccess } from '@/utils/request.js'
import { 
  showToast, 
  showLoadingToast, 
  closeToast, 
  showSuccessToast,
  showConfirmDialog 
} from 'vant'


// 路由和状态管理
const router = useRouter()
const { user } = useUser()
const { isDark } = useSettings()

// 主题配置
const currentTheme = computed(() => isDark.value ? 'dark' : 'light')

// 响应式数据
const messages = ref([])
const activeTab = ref(0)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = ref(20)
const showMoreActions = ref(false)
const showMessageDetail = ref(false)
const showClearConfirm = ref(false)
const selectedMessage = ref(null)

// 更多操作选项
const moreActions = [
  { name: '全部标记已读', value: 'markAllRead' },
  { name: '清空已读消息', value: 'clearRead' },
  { name: '消息设置', value: 'settings' }
]

// 计算属性
const filteredMessages = computed(() => {
  let result = messages.value
  
  switch (activeTab.value) {
    case 1: // 系统通知
      result = result.filter(m => m.type === 'system')
      break
    case 2: // 目标提醒
      result = result.filter(m => m.type === 'goal')
      break
    case 3: // 协作消息
      result = result.filter(m => m.type === 'collaboration')
      break
    default: // 全部
      break
  }
  
  return result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const unreadCount = computed(() => {
  return messages.value.filter(m => !m.read).length
})

const systemUnreadCount = computed(() => {
  return messages.value.filter(m => m.type === 'system' && !m.read).length
})

const goalUnreadCount = computed(() => {
  return messages.value.filter(m => m.type === 'goal' && !m.read).length
})

const collabUnreadCount = computed(() => {
  return messages.value.filter(m => m.type === 'collaboration' && !m.read).length
})

// 方法
const goBack = () => {
  router.back()
}

const goToHome = () => {
  router.push('/home')
}

const handleTabChange = () => {
  page.value = 1
  finished.value = false
  loadMessages(true)
}

const formatTime = (timeString) => {
  if (!timeString) return ''
  
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date
  
  // 小于1小时
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000))
    return `${minutes}分钟前`
  }
  
  // 小于24小时
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    return `${hours}小时前`
  }
  
  // 小于7天
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000))
    return `${days}天前`
  }
  
  // 超过7天显示具体日期
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

const getMessageTypeColor = (type) => {
  const typeMap = {
    'system': 'primary',
    'goal': 'success',
    'collaboration': 'warning',
    'reminder': 'danger'
  }
  return typeMap[type] || 'default'
}

const getMessageTypeText = (type) => {
  const typeMap = {
    'system': '系统通知',
    'goal': '目标提醒',
    'collaboration': '协作消息',
    'reminder': '提醒'
  }
  return typeMap[type] || '未知'
}

const getMessageTypeIcon = (type) => {
  const iconMap = {
    'system': 'setting-o',
    'goal': 'bullhorn-o',
    'collaboration': 'friends-o',
    'reminder': 'clock-o'
  }
  return iconMap[type] || 'chat-o'
}

// 数据加载方法
const loadMessages = async (isRefresh = false) => {
  try {
    if (isRefresh) {
      page.value = 1
      finished.value = false
    }

    // 模拟获取消息的API调用
    const response = await getM('message/get', {
      page: page.value,
      pageSize: pageSize.value,
      type: activeTab.value > 0 ? ['system', 'goal', 'collaboration'][activeTab.value - 1] : undefined
    })

    if (isSuccess(response)) {
      const data = response.data.data || []
      
      if (isRefresh) {
        messages.value = data
      } else {
        messages.value.push(...data)
      }

      // 判断是否加载完成
      if (data.length < pageSize.value) {
        finished.value = true
      } else {
        page.value++
      }
    } else {
      throw new Error(response.data.msg || '获取消息失败')
    }
  } catch (error) {
    showToast('加载失败，请重试')
    console.error('加载消息失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}



// 事件处理
const onLoad = () => {
  loadMessages()
}

const onRefresh = () => {
  refreshing.value = true
  loadMessages(true)
}

const openMessage = async (message) => {
  selectedMessage.value = message
  showMessageDetail.value = true
  
  // 标记为已读
  if (!message.read) {
    await markAsRead(message)
  }
}

const closeMessageDetail = () => {
  showMessageDetail.value = false
  selectedMessage.value = null
}

const markAsRead = async (message) => {
  try {
    await postM(`message/read/${message.id}`)
    message.read = true
    showToast('已标记为已读')
  } catch (error) {
    showToast('操作失败，请重试')
    console.error('标记已读失败:', error)
  }
}

const markAllAsRead = async () => {
  try {
    await postM('message/markAllAsRead')
    messages.value.forEach(m => m.read = true)
    showSuccessToast('全部消息已标记为已读')
  } catch (error) {
    showToast('操作失败，请重试')
    console.error('标记全部已读失败:', error)
  }
}

const deleteMessage = async (message) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这条消息吗？',
    })
    
    await postM(`message/delete/${message.id}`)
    const index = messages.value.findIndex(m => m.id === message.id)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
    
    showToast('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      showToast('删除失败，请重试')
      console.error('删除消息失败:', error)
    }
  }
}

const clearAllMessages = async () => {
  try {
    await postM('message/clearAll')
    messages.value = []
    showSuccessToast('所有消息已清空')
  } catch (error) {
    showToast('清空失败，请重试')
    console.error('清空消息失败:', error)
  }
}

const onActionSelect = (action) => {
  showMoreActions.value = false
  
  switch (action.value) {
    case 'markAllRead':
      markAllAsRead()
      break
    case 'clearRead':
      clearReadMessages()
      break
    case 'settings':
      router.push('/notification-settings')
      break
  }
}

const clearReadMessages = async () => {
  try {
    await showConfirmDialog({
      title: '确认清空',
      message: '确定要清空所有已读消息吗？',
    })
    
    const readMessages = messages.value.filter(m => m.read)
    for (const message of readMessages) {
      await postM(`message/delete/${message.id}`)
    }
    
    messages.value = messages.value.filter(m => !m.read)
    showSuccessToast('已读消息已清空')
  } catch (error) {
    if (error !== 'cancel') {
      showToast('清空失败，请重试')
      console.error('清空已读消息失败:', error)
    }
  }
}

const handleMessageAction = (message) => {
  if (message.actionUrl) {
    router.push(message.actionUrl)
    closeMessageDetail()
  }
}

// 生命周期
onMounted(() => {
  loadMessages(true)
})
</script>

<style scoped>
.mobile-message-center-apple {
  background-color: #f7f8fa;
  min-height: 100vh;
}

.apple-nav-bar {
  background: rgba(255,255,255,0.8);
  backdrop-filter: blur(10px);
}

.apple-tabs-container {
  padding: 0 16px;
  background: rgba(255,255,255,0.8);
  backdrop-filter: blur(10px);
}

.apple-badge {
  font-weight: 600;
}

.apple-list-container {
  padding: 16px;
}

.apple-swipe-cell {
  margin-bottom: 16px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}

.apple-message-card {
  position: relative;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  transition: transform 0.2s ease;
}

.apple-message-card.unread {
  background: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-icon {
  color: #007aff;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1c1c1e;
}

.card-time {
  font-size: 12px;
  color: #8e8e93;
}

.card-content {
  font-size: 14px;
  color: #3c3c43;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.unread-dot {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 8px;
  height: 8px;
  background: #007aff;
  border-radius: 50%;
}

.swipe-btn-read, .swipe-btn-delete {
  height: 100%;
}

.apple-empty-state {
  padding-top: 80px;
}

.apple-action-sheet {
  --van-action-sheet-border-radius: 24px;
}

.apple-message-popup {
  background: rgba(242,242,247,0.95);
  backdrop-filter: blur(12px);
}

.apple-popup-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.popup-header {
  text-align: center;
  margin-bottom: 20px;
}

.popup-icon {
  color: #007aff;
  margin-bottom: 8px;
}

.popup-title {
  font-size: 20px;
  font-weight: 700;
  color: #1c1c1e;
}

.popup-time {
  font-size: 13px;
  color: #8e8e93;
}

.popup-body {
  flex: 1;
  overflow-y: auto;
}

.popup-text {
  font-size: 16px;
  line-height: 1.6;
  color: #3c3c43;
}

.popup-footer {
  margin-top: 20px;
}

.apple-main-btn {
  background: linear-gradient(90deg, #007aff 0%, #0052d9 100%);
  border: none;
}

/* Dark Mode */
.van-theme-dark .mobile-message-center-apple {
  background-color: #000;
}

.van-theme-dark .apple-nav-bar, .van-theme-dark .apple-tabs-container {
  background: rgba(28,28,30,0.85);
}

.van-theme-dark .apple-message-card {
  background: #1c1c1e;
  box-shadow: 0 4px 24px rgba(0,0,0,0.2);
}

.van-theme-dark .card-title, .van-theme-dark .popup-title {
  color: #fff;
}

.van-theme-dark .card-content, .van-theme-dark .popup-text {
  color: #e5e5ea;
}

.van-theme-dark .card-time, .van-theme-dark .popup-time {
  color: #8d8d93;
}

.van-theme-dark .apple-message-popup {
  background: rgba(28,28,30,0.95);
}
</style>