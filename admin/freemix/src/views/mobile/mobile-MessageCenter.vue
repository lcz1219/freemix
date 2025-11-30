<template>
  <div class="dark-chat-app">
    <!-- 1. 首页：联系人列表 -->
    <div class="main-view">
      <!-- 顶部吸顶区域 -->
      <div class="sticky-header">
        <van-nav-bar
          :border="false"
          class="custom-nav-bar"
          :safe-area-inset-top="true"
        >
          <template #left>
            <span class="page-title">消息</span>
          </template>
          <template #right>
            <div class="nav-icon-btn" @click="turnHome">
              <van-icon name="wap-home-o" size="24" />
            </div>
          </template>
        </van-nav-bar>

        <div class="search-wrapper">
          <van-search 
            v-model="searchUser" 
            placeholder="搜索联系人" 
            shape="round"
            background="transparent"
            class="custom-search"
          />
        </div>
      </div>

      <!-- 用户列表区域 -->
      <div class="list-container">
        <van-pull-refresh 
          v-model="refreshing" 
          @refresh="onRefresh" 
          success-text="已更新"
          class="custom-pull-refresh"
        >
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text=""
            @load="onLoad"
          >
            <transition-group name="fade-slide">
              <div
                v-for="user in filteredUsers"
                :key="user.username"
                class="user-item"
                @click="openChat(user)"
              >
                <!-- 头像 -->
                <div class="avatar-box">
                  <van-badge :content="getBadgeCount(user.username)" max="99" :show-zero="false" class="msg-badge">
                    <van-image 
                      round 
                      width="52px" 
                      height="52px" 
                      :src="showAvatar(user.avatarUrl)" 
                      fit="cover"
                      class="avatar-img"
                    />
                  </van-badge>
                  <div v-if="isUserOnline(user.username)" class="online-dot"></div>
                </div>

                <!-- 内容 -->
                <div class="content-box">
                  <div class="row-top">
                    <span class="username">{{ user.chinesename || user.username }}</span>
                    <span class="time">{{ formatListTime(user.lastMsgTime) }}</span>
                  </div>
                  <div class="row-bottom">
                    <p class="preview-msg">{{ user.lastMsgContent || '点击开始聊天' }}</p>
                  </div>
                </div>
              </div>
            </transition-group>
          </van-list>
        </van-pull-refresh>

        <van-empty 
          v-if="filteredUsers.length === 0 && !loading" 
          image="search" 
          description="暂无联系人" 
          class="dark-empty"
        />
      </div>
    </div>

    <!-- 2. 聊天详情页 (全屏覆盖) -->
    <van-popup
      v-model:show="showChatWindow"
      position="right"
      :style="{ width: '100%', height: '100%' }"
      duration="0.3"
      class="chat-window-popup"
    >
      <div class="chat-layout" v-if="selectedUser">
        <!-- 聊天顶部 Header -->
        <div class="chat-header glass-effect">
          <div class="header-left" @click="closeChat">
            <van-icon name="arrow-left" size="24" color="#0A84FF" />
          </div>
          <div class="header-center">
            <span class="chat-title">{{ selectedUser.chinesename || selectedUser.username }}</span>
            <span class="chat-subtitle">
              <span v-if="isUserOnline(selectedUser.username)" class="status-online">● 在线</span>
              <span v-else class="status-offline">离线</span>
            </span>
          </div>
          <div class="header-right">
            <van-icon name="ellipsis" size="24" color="#0A84FF" />
          </div>
        </div>

        <!-- 聊天消息区域 -->
        <div class="chat-viewport" @click="hideKeyboard">
          <div class="scroll-content">
            <div class="spacer-top"></div>
            
            <div v-if="messages.length === 0" class="chat-start-tip">
              <span class="tip-text">打个招呼吧 👋</span>
            </div>

            <div 
              v-for="(msg, index) in messages" 
              :key="msg.id" 
              :class="['msg-row', msg.fromUser === currentUser.username ? 'msg-sent' : 'msg-received']"
            >
              <!-- 时间分割线 (每隔一段时间显示一次) -->
              <div v-if="shouldShowTime(msg, index)" class="time-divider">
                <span>{{ formatChatTime(msg.createdAt) }}</span>
              </div>

              <div class="msg-wrapper">
                <!-- 接收方头像 -->
                <van-image 
                  v-if="msg.fromUser !== currentUser.username"
                  round 
                  width="32px" 
                  height="32px" 
                  :src="showAvatar(selectedUser.avatarUrl)" 
                  class="chat-avatar"
                />

                <!-- 消息气泡 -->
                <div class="bubble">
                  <div class="bubble-text">{{ msg.content }}</div>
                  <div class="bubble-meta">
                    <span class="meta-time">{{ formatTimeOnly(msg.createdAt) }}</span>
                    <van-icon v-if="msg.fromUser === currentUser.username && msg.isRead" name="success" class="read-icon" />
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 底部锚点，用于自动滚动 -->
            <div ref="bottomAnchor" class="bottom-anchor"></div>
          </div>
        </div>

        <!-- 底部输入区域 -->
        <div class="chat-input-bar glass-effect">
          <div class="input-container">
            <van-field
              v-model="newMessage"
              rows="1"
              autosize
              type="textarea"
              placeholder="发消息..."
              class="custom-field"
              :border="false"
              @focus="scrollToBottom(true)"
            />
            <button 
              class="send-btn" 
              :class="{ 'active': newMessage.trim() }"
              @click="handleSend"
              :disabled="sendingMessage || !newMessage.trim()"
            >
              <van-loading v-if="sendingMessage" type="spinner" size="20px" color="#fff" />
              <van-icon v-else name="arrow-up" size="20" />
            </button>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted, nextTick, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { getM, postM, isSuccess, baseURL } from '@/utils/request.js'
import { sendMessageWeb } from '@/utils/websocket.js'
import { showToast } from 'vant'

// --- 基础配置 ---
const router = useRouter()
const store = useStore()
const currentUser = computed(() => store.state.user || { username: 'guest' })

// --- 状态数据 ---
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const searchUser = ref('')
const allUsers = ref([])
const userStatus = ref({})
const badge = ref({})

// 聊天相关
const showChatWindow = ref(false)
const selectedUser = ref(null)
const messages = ref([])
const newMessage = ref('')
const sendingMessage = ref(false)
const bottomAnchor = ref(null) // 滚动锚点
let statusInterval = null

// --- 计算属性 ---
const filteredUsers = computed(() => {
  if (!searchUser.value) return allUsers.value
  const search = searchUser.value.trim().toLowerCase()
  return allUsers.value.filter(user => 
    (user.username && user.username.toLowerCase().includes(search)) || 
    (user.chinesename && user.chinesename.toLowerCase().includes(search))
  )
})

// --- 辅助函数 ---
const getBadgeCount = (username) => badge.value[username] || 0
const isUserOnline = (username) => userStatus.value[username] || false

const showAvatar = (url) => {
  if (!url) return 'https://bpic.588ku.com/element_origin_min_pic/19/04/10/e87e154ddafd724a915a119fb21c38b9.jpg'
  return url.startsWith('http') ? url : `${baseURL()}${url}`
}

const turnHome = () => router.push('/home')

// 时间格式化 - 列表用
const formatListTime = (str) => {
  if (!str) return ''
  const date = new Date(str)
  const now = new Date()
  if (date.toDateString() === now.toDateString()) {
    return `${date.getHours().toString().padStart(2,'0')}:${date.getMinutes().toString().padStart(2,'0')}`
  }
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 时间格式化 - 聊天分割线用
const formatChatTime = (ts) => {
  const date = new Date(ts)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2,'0')}`
}

// 时间格式化 - 气泡内用
const formatTimeOnly = (ts) => {
  const date = new Date(ts)
  return `${date.getHours().toString().padStart(2,'0')}:${date.getMinutes().toString().padStart(2,'0')}`
}

// 判断是否显示时间分割线 (超过5分钟显示一次)
const shouldShowTime = (msg, index) => {
  if (index === 0) return true
  const prevMsg = messages.value[index - 1]
  return (msg.createdAt - prevMsg.createdAt) > 5 * 60 * 1000
}

// --- 核心业务逻辑 ---

const fetchAllUsers = async () => {
  try {
    const res = await getM('getOwerList')
    if (isSuccess(res)) {
      const data = res.data.data.map(user => ({
        ...user,
        username: user.value,
        chinesename: user.text,
        avatarUrl: user.avatarUrl
      }))
      allUsers.value = data.filter(u => u.username !== currentUser.value.username)
      finished.value = true
    }
  } catch (e) { console.error(e) }
  loading.value = false
}

const fetchUserStatus = async () => {
  try {
    const res = await getM('user-status/all')
    if (res && res.data) userStatus.value = res.data
  } catch (e) {}
}

const fetchUnreadCount = async () => {
  try {
    const res = await getM('messages/unreadCount')
    if (isSuccess(res)) badge.value = res.data.data
  } catch (e) {}
}

const openChat = async (user) => {
  selectedUser.value = user
  showChatWindow.value = true
  messages.value = []
  await fetchMessages()
  scrollToBottom(true) // 立即滚动
}

const closeChat = () => {
  showChatWindow.value = false
  setTimeout(() => { selectedUser.value = null }, 300) // 等待动画结束
  fetchUnreadCount()
}

const fetchMessages = async () => {
  if (!selectedUser.value) return
  try {
    const [sentRes, receivedRes] = await Promise.all([
      getM('messages/sent'),
      getM('messages/received')
    ])
    
    if (isSuccess(sentRes) && isSuccess(receivedRes)) {
      const allMsgs = [...(sentRes.data.data || []), ...(receivedRes.data.data || [])]
      const chatMsgs = allMsgs.filter(
        msg => 
          (msg.fromUser === currentUser.value.username && msg.toUser === selectedUser.value.username) ||
          (msg.fromUser === selectedUser.value.username && msg.toUser === currentUser.value.username)
      )
      messages.value = chatMsgs.sort((a, b) => a.createdAt - b.createdAt)
      await markMessagesAsRead()
    }
  } catch (e) { showToast('加载失败') }
}

const markMessagesAsRead = async () => {
  const unreadIds = messages.value
    .filter(msg => msg.toUser === currentUser.value.username && !msg.isRead)
    .map(msg => msg.id)
  
  if (unreadIds.length > 0) {
    await postM('messages/markAsReadBatch', { messageIds: unreadIds })
    messages.value.forEach(msg => {
      if (unreadIds.includes(msg.id)) msg.isRead = true
    })
    if (badge.value[selectedUser.value.username]) badge.value[selectedUser.value.username] = 0
  }
}

const handleSend = async () => {
  if (!newMessage.value.trim() || !selectedUser.value) return
  
  sendingMessage.value = true
  const content = newMessage.value.trim()
  
  try {
    const messageData = {
      toUser: selectedUser.value.username,
      content: content,
      type: 'text',
      fromUser: currentUser.value.username
    }
    
    const res = await postM('messages/send', messageData)
    sendMessageWeb(JSON.stringify(messageData))
    
    if (isSuccess(res)) {
      messages.value.push(res.data.data)
      newMessage.value = ''
      scrollToBottom()
    }
  } catch (e) {
    showToast('发送失败')
  } finally {
    sendingMessage.value = false
  }
}

// 滚动到底部
const scrollToBottom = (instant = false) => {
  nextTick(() => {
    if (bottomAnchor.value) {
      bottomAnchor.value.scrollIntoView({
        behavior: instant ? 'auto' : 'smooth',
        block: 'end'
      })
    }
  })
}

// 点击背景收起键盘
const hideKeyboard = () => {
  if (document.activeElement instanceof HTMLElement) {
    document.activeElement.blur()
  }
}

// --- 生命周期 ---
const onLoad = async () => {
  if (allUsers.value.length === 0) await fetchAllUsers()
}

const onRefresh = async () => {
  await Promise.all([fetchAllUsers(), fetchUserStatus(), fetchUnreadCount()])
  refreshing.value = false
}

// 简单的轮询，保持数据新鲜
let poller = null
onMounted(async () => {
  await fetchAllUsers()
  await fetchUnreadCount()
  await fetchUserStatus()
  
  statusInterval = setInterval(fetchUserStatus, 8000)
  poller = setInterval(() => {
    if (showChatWindow.value) fetchMessages()
    else fetchUnreadCount()
  }, 3000)
})

onUnmounted(() => {
  clearInterval(statusInterval)
  clearInterval(poller)
})

watch(() => messages.value.length, () => {
  if (showChatWindow.value) scrollToBottom()
})
</script>

<style scoped lang="scss">
/* --- 1. 全局主题变量 (暗黑核心) --- */
:root {
  --bg-color: #000000;         /* 纯黑背景 (OLED 省电且高级) */
  --card-bg: #1C1C1E;          /* 列表/卡片背景 (深灰) */
  --text-primary: #FFFFFF;     /* 主文字 */
  --text-secondary: #8E8E93;   /* 次要文字 */
  --accent-color: #0A84FF;     /* iOS 蓝色高亮 */
  --bubble-sent: linear-gradient(135deg, #0A84FF, #5E5CE6); /* 发送气泡 */
  --bubble-received: #2C2C2E;  /* 接收气泡 */
  --divider: #38383A;          /* 分割线 */
  --input-bg: #2C2C2E;         /* 输入框背景 */
}

.dark-chat-app {
  background-color: var(--bg-color);
  min-height: 100vh;
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", sans-serif;
}

/* --- 2. 首页样式 --- */
.main-view {
  padding-bottom: env(safe-area-inset-bottom);
}

.sticky-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(28, 28, 30, 0.85); /* 毛玻璃 */
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 0.5px solid var(--divider);
}

.custom-nav-bar {
  background: transparent;
  .page-title {
    font-size: 28px;
    font-weight: 800;
    color: var(--text-primary);
    margin-left: 4px;
  }
  :deep(.van-icon) {
    color: var(--accent-color);
  }
}

.search-wrapper {
  padding: 0 16px 10px;
}

.custom-search {
  :deep(.van-search__content) {
    background-color: var(--card-bg);
  }
  :deep(.van-field__control) {
    color: var(--text-primary);
  }
}

.list-container {
  padding: 0 16px;
}

/* 用户列表项 */
.user-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 0.5px solid var(--divider);
  transition: opacity 0.2s;

  &:active {
    opacity: 0.6;
  }
}

.avatar-box {
  position: relative;
  margin-right: 16px;
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #30D158;
  border: 2px solid var(--bg-color);
  border-radius: 50%;
}

.content-box {
  flex: 1;
  min-width: 0;
}

.row-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.username {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}

.time {
  font-size: 13px;
  color: var(--text-secondary);
}

.row-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-msg {
  margin: 0;
  font-size: 15px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 90%;
}

/* --- 3. 聊天窗口样式 --- */
.chat-window-popup {
  background-color: var(--bg-color);
}

.chat-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.glass-effect {
  background: rgba(28, 28, 30, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

/* Header */
.chat-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: calc(60px + env(safe-area-inset-top)); /* 适配刘海屏高度 */
  display: flex;
  align-items: center;
  padding: env(safe-area-inset-top) 12px 0; /* 顶部增加安全距离 */
  box-sizing: border-box; /* 确保高度包含 padding */
  z-index: 100;
  border-bottom: 0.5px solid var(--divider);
}

.header-left, .header-right {
  width: 40px;
  display: flex;
  justify-content: center;
  cursor: pointer;
}

.header-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
}

.chat-subtitle {
  font-size: 11px;
  margin-top: 2px;
  .status-online { color: #30D158; }
  .status-offline { color: var(--text-secondary); }
}

/* 消息视口 */
.chat-viewport {
  flex: 1;
  overflow-y: auto;
  background-color: var(--bg-color);
  -webkit-overflow-scrolling: touch;
  position: relative;
  /* 关键：确保内容不被 header 和 footer 遮挡 */
  display: flex;
  flex-direction: column;
}

.scroll-content {
  padding: 0 16px;
  flex: 1;
}

.spacer-top {
  height: calc(70px + env(safe-area-inset-top)); /* 留出 header 空间 (含安全区域) */
}

.chat-start-tip {
  text-align: center;
  margin-top: 40px;
  color: var(--text-secondary);
  font-size: 14px;
}

.bottom-anchor {
  height: 70px; /* 留出 footer 空间 */
}

/* 消息行 */
.msg-row {
  display: flex;
  margin-bottom: 20px;
  flex-direction: column;
}

.time-divider {
  text-align: center;
  margin: 16px 0;
  span {
    background: rgba(255,255,255,0.1);
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 11px;
    color: var(--text-secondary);
  }
}

.msg-wrapper {
  display: flex;
  align-items: flex-end;
  max-width: 100%;
}

.chat-avatar {
  flex-shrink: 0;
  margin-right: 8px;
  margin-bottom: 4px;
}

/* 气泡样式核心 */
.bubble {
  position: relative;
  border-radius: 18px;
  font-size: 16px;
  line-height: 1.5;
  word-wrap: break-word;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.bubble-text {
  margin-bottom: 4px;
}

.bubble-meta {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 4px;
  opacity: 0.7;
}

.meta-time {
  font-size: 10px;
}

.read-icon {
  font-size: 12px;
}

/* 发送方样式 */
.msg-sent {
  align-items: flex-end;
  .msg-wrapper {
    flex-direction: row-reverse;
  }
  .bubble {
    background: var(--bubble-sent);
    color: #fff;
    border-bottom-right-radius: 4px; /* 小尾巴效果 */
  }
}

/* 接收方样式 */
.msg-received {
  align-items: flex-start;
  .bubble {
    background: var(--bubble-received);
    color: var(--text-primary);
    border-bottom-left-radius: 4px; /* 小尾巴效果 */
  }
}

/* 底部输入框 */
.chat-input-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 10px 16px;
  /* 适配 iPhone 底部横条 */
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  border-top: 0.5px solid var(--divider);
  z-index: 100;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background: var(--input-bg);
  padding: 6px;
  border-radius: 24px;
}

.custom-field {
  background: transparent;
  padding: 6px 12px;
  max-height: 100px;
  overflow-y: auto;
  :deep(.van-field__control) {
    color: #fff;
    font-size: 16px;
  }
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #3A3A3C;
  color: #8E8E93;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  flex-shrink: 0;

  &.active {
    background: var(--accent-color);
    color: #fff;
    transform: scale(1.05);
  }
}

/* 动画 */
.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from, .fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>