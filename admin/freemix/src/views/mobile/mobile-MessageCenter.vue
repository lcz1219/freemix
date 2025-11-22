<template>
  <!-- <van-config-provider :theme="currentTheme"> -->
    <div class="mobile-message-center">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="消息中心"
        left-text="返回"
        left-arrow
        @click-left="goBack"
      >
        <template #right>
          <van-icon 
            name="more-o" 
            size="18" 
            @click="showMoreActions = true" 
          />
        </template>
      </van-nav-bar>

      <!-- 消息类型标签 -->
      <div class="message-tabs">
        <van-tabs v-model:active="activeTab" sticky @change="handleTabChange">
          <van-tab title="全部">
            <template #title>
              <van-badge :content="unreadCount > 0 ? unreadCount : ''">
                全部
              </van-badge>
            </template>
          </van-tab>
          <van-tab title="系统通知">
            <template #title>
              <van-badge :content="systemUnreadCount > 0 ? systemUnreadCount : ''">
                系统通知
              </van-badge>
            </template>
          </van-tab>
          <van-tab title="目标提醒">
            <template #title>
              <van-badge :content="goalUnreadCount > 0 ? goalUnreadCount : ''">
                目标提醒
              </van-badge>
            </template>
          </van-tab>
          <van-tab title="协作消息">
            <template #title>
              <van-badge :content="collabUnreadCount > 0 ? collabUnreadCount : ''">
                协作消息
              </van-badge>
            </template>
          </van-tab>
        </van-tabs>
      </div>

      <!-- 消息列表 -->
      <div class="message-list">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多消息了"
            @load="onLoad"
          >
            <van-swipe-cell
              v-for="message in filteredMessages"
              :key="message.id"
              class="message-item"
            >
              <van-cell
                :class="['message-cell', { 'unread': !message.read }]"
                @click="openMessage(message)"
                is-link
              >
                <template #title>
                  <div class="message-header">
                    <div class="message-title">{{ message.title }}</div>
                    <van-tag
                      :type="getMessageTypeColor(message.type)"
                      size="small"
                      plain
                    >
                      {{ getMessageTypeText(message.type) }}
                    </van-tag>
                  </div>
                </template>
                
                <template #label>
                  <div class="message-content">{{ message.content }}</div>
                  <div class="message-meta">
                    <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                    <span v-if="message.sender" class="message-sender">
                      来自: {{ message.sender }}
                    </span>
                  </div>
                </template>
                
                <template #right-icon>
                  <div class="message-status">
                    <van-icon
                      v-if="!message.read"
                      name="circle"
                      color="#1989fa"
                      size="8"
                    />
                  </div>
                </template>
              </van-cell>
              
              <!-- 左滑操作 -->
              <template #left>
                <van-button
                  square
                  type="primary"
                  text="标记已读"
                  @click="markAsRead(message)"
                />
              </template>
              
              <template #right>
                <van-button
                  square
                  type="danger"
                  text="删除"
                  @click="deleteMessage(message)"
                />
              </template>
            </van-swipe-cell>
          </van-list>
        </van-pull-refresh>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredMessages.length === 0 && !loading" class="empty-state">
        <van-empty description="暂无消息">
          <van-button type="primary" round @click="goToHome">
            返回首页
          </van-button>
        </van-empty>
      </div>

      <!-- 底部操作栏 -->
      <div v-if="filteredMessages.length > 0" class="bottom-actions">
        <van-button
          type="primary"
          size="small"
          @click="markAllAsRead"
          :disabled="unreadCount === 0"
        >
          全部已读
        </van-button>
        <van-button
          type="danger"
          size="small"
          @click="showClearConfirm = true"
        >
          清空消息
        </van-button>
      </div>

      <!-- 更多操作弹出菜单 -->
      <van-action-sheet
        v-model:show="showMoreActions"
        :actions="moreActions"
        @select="onActionSelect"
        cancel-text="取消"
      />

      <!-- 消息详情弹窗 -->
      <van-popup
        v-model:show="showMessageDetail"
        position="bottom"
        :style="{ height: '70%' }"
        round
      >
        <div v-if="selectedMessage" class="message-detail">
          <div class="detail-header">
            <h3>{{ selectedMessage.title }}</h3>
            <van-button
              type="primary"
              size="small"
              @click="closeMessageDetail"
            >
              关闭
            </van-button>
          </div>
          
          <div class="detail-content">
            <div class="detail-meta">
              <van-tag :type="getMessageTypeColor(selectedMessage.type)">
                {{ getMessageTypeText(selectedMessage.type) }}
              </van-tag>
              <span class="detail-time">{{ formatTime(selectedMessage.createdAt) }}</span>
              <span v-if="selectedMessage.sender" class="detail-sender">
                发送人: {{ selectedMessage.sender }}
              </span>
            </div>
            
            <div class="detail-text">
              {{ selectedMessage.content }}
            </div>
            
            <div v-if="selectedMessage.actionUrl" class="detail-actions">
              <van-button
                type="primary"
                block
                @click="handleMessageAction(selectedMessage)"
              >
                查看详情
              </van-button>
            </div>
          </div>
        </div>
      </van-popup>

      <!-- 清空确认弹窗 -->
      <van-dialog
        v-model:show="showClearConfirm"
        title="确认清空"
        show-cancel-button
        @confirm="clearAllMessages"
      >
        <div style="padding: 20px; text-align: center;">
          确定要清空所有消息吗？此操作不可恢复。
        </div>
      </van-dialog>
    </div>
  <!-- </van-config-provider> -->
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
.mobile-message-center {
  min-height: 100vh;
  background-color: var(--van-background-color);
}

.message-tabs {
  position: sticky;
  top: 46px;
  z-index: 1;
}

.message-list {
  padding: 0 12px;
}

.message-item {
  margin-bottom: 8px;
  border-radius: 8px;
  overflow: hidden;
}

.message-cell {
  background-color: var(--van-background-color-light);
  border-radius: 8px;
}

.message-cell.unread {
  background-color: var(--van-primary-color-light);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.message-title {
  font-weight: 600;
  font-size: 16px;
  color: var(--van-text-color);
  flex: 1;
  margin-right: 8px;
}

.message-content {
  color: var(--van-text-color-2);
  font-size: 14px;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--van-text-color-3);
}

.message-status {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background-color: var(--van-background-color);
  border-top: 1px solid var(--van-border-color);
  display: flex;
  gap: 12px;
  z-index: 100;
}

.message-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--van-border-color);
}

.detail-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--van-text-color);
}

.detail-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 12px;
  color: var(--van-text-color-2);
}

.detail-text {
  line-height: 1.6;
  color: var(--van-text-color);
  margin-bottom: 20px;
}

.detail-actions {
  margin-top: auto;
}

/* 深色主题适配 */
:deep(.van-theme-dark) {
  --van-background-color: #1a1a1a;
  --van-background-color-light: #2a2a2a;
  --van-text-color: #f5f5f5;
  --van-text-color-2: #a0a0a0;
  --van-text-color-3: #808080;
  --van-primary-color-light: rgba(25, 137, 250, 0.1);
}
</style>