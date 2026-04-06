<template>
  <div class="dark-chat-app">
    <!-- 1. 首页：联系人列表 -->
    <div class="main-view">
      <!-- 顶部吸顶区域 -->
      <div class="sticky-header">
        <van-nav-bar :border="false" class="custom-nav-bar" :safe-area-inset-top="true">
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
          <van-search v-model="searchUser" placeholder="搜索联系人" shape="round" background="transparent"
            class="custom-search" />
        </div>
      </div>

      <!-- 用户列表区域 -->
      <div class="list-container">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh" success-text="已更新" class="custom-pull-refresh">
          <van-list v-model:loading="loading" :finished="finished" finished-text="" @load="onLoad">
            <transition-group name="fade-slide">
              <div v-for="user in filteredUsers" :key="user.username" class="user-item" @click="openChat(user)">
                <!-- 头像 -->
                <div class="avatar-box">
                  <van-badge :content="getBadgeCount(user.username)" max="99" :show-zero="false" class="msg-badge">
                    <van-image round width="52px" height="52px" :src="showAvatar(user.avatarUrl)" fit="cover"
                      class="avatar-img" />
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

        <van-empty v-if="filteredUsers.length === 0 && !loading" image="search" description="暂无联系人"
          class="dark-empty" />
      </div>
    </div>

    <!-- 2. 聊天详情页 (全屏覆盖) -->
    <van-popup v-model:show="showChatWindow" position="right" :style="{ width: '100%', height: '100%' }" duration="0.3"
      class="chat-window-popup">
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

            <div v-for="(msg, index) in messages" :key="msg.id"
              :class="['msg-row', msg.fromUser === currentUser.username ? 'msg-sent' : 'msg-received']">
              <!-- 时间分割线 (每隔一段时间显示一次) -->
              <div v-if="shouldShowTime(msg, index)" class="time-divider">
                <span>{{ formatChatTime(msg.createdAt) }}</span>
              </div>

              <div class="msg-wrapper">
                <!-- 接收方头像 -->
                <van-image v-if="msg.fromUser !== currentUser.username" round width="32px" height="32px"
                  :src="showAvatar(selectedUser.avatarUrl)" class="chat-avatar" />

                <!-- 消息气泡 -->
                <div class="bubble">
                  <!-- 文本消息 -->
                  <div v-if="msg.type === 'text' || !msg.type" class="bubble-text">{{ msg.content }}</div>

                  <!-- 图片消息 -->
                  <div v-else-if="msg.type === 'image'" class="bubble-image">
                    <van-image :src="msg.content" fit="cover" class="msg-image" @click="previewImage(msg.content)" />
                  </div>

                  <!-- 语音消息 -->
                  <!-- <div v-else-if="msg.type === 'voice'" class="bubble-voice">
                    <div class="voice-player" @click="playVoice(msg)">
                      <van-icon name="volume-o" class="voice-icon" />
                      <span class="voice-duration">{{ getVoiceDuration(msg.content) }}"</span>
                    </div>
                  </div> -->
                  <!-- 语音消息 -->
                  <div v-else-if="msg.type === 'voice'" class="bubble-voice">
                    <div class="voice-player" @click="playVoice(msg)">
                      <!-- 根据 playingVoiceId 判断是否加上 is-playing 动画类 -->
                      <van-icon name="volume-o" class="voice-icon"
                        :class="{ 'is-playing': playingVoiceId === msg.id }" />
                      <span class="voice-duration">{{ getVoiceDuration(msg.content) }}"</span>
                    </div>
                  </div>

                  <div class="bubble-meta">
                    <span class="meta-time">{{ formatTimeOnly(msg.createdAt) }}</span>
                    <van-icon v-if="msg.fromUser === currentUser.username && msg.isRead" name="success"
                      class="read-icon" />
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
          <!-- 输入区域 -->
          <div class="input-container">
            <van-field v-if="!isRecording" v-model="newMessage" rows="1" autosize type="textarea" placeholder="说点什么..."
              class="custom-field" :border="false" @focus="scrollToBottom(true)" />

            <!-- 录音状态显示 -->
            <div v-if="isRecording" class="recording-display">
              <div class="recording-waves">
                <div class="wave"></div>
                <div class="wave"></div>
                <div class="wave"></div>
                <div class="wave"></div>
                <div class="wave"></div>
              </div>
              <span class="recording-time">{{ getRecordingTime() }}</span>
            </div>

            <!-- 发送按钮 -->
            <button v-if="newMessage.trim() && !isRecording" @click="handleSend" class="send-btn"
              :disabled="sendingMessage">
              <van-icon v-if="sendingMessage" name="loading" class="loading-icon" />
              <span v-else>发送</span>
            </button>
          </div>

          <!-- 功能工具栏 -->
          <div class="function-toolbar">
            <button class="tool-btn emoji-btn" @click="showEmojiPicker = !showEmojiPicker"
              :class="{ active: showEmojiPicker }">
              <van-icon name="smile-o" />
            </button>
            <button class="tool-btn" @click="showImageActionSheet">
              <van-icon name="photograph" />
            </button>

            <!-- 录音按钮 -->
            <button v-if="!isRecording" class="tool-btn voice-btn" @touchstart="startVoiceRecording"
              @touchend="stopVoiceRecording" @touchcancel="cancelVoiceRecording">
              <van-icon name="volume-o" />
            </button>

            <!-- 录音中按钮 -->
            <button v-if="isRecording" class="tool-btn recording-btn" @touchend="stopVoiceRecording"
              @touchcancel="cancelVoiceRecording">
              <van-icon name="plus" />
            </button>
          </div>

          <!-- 表情选择器 -->
          <div class="emoji-picker-container" v-show="showEmojiPicker">
            <div class="emoji-picker-header">
              <span>表情</span>
              <button @click="showEmojiPicker = false" class="close-btn">
                <van-icon name="cross" />
              </button>
            </div>
            <EmojiPicker :native="true" @select="onEmojiSelect" :group-names="optionsName" :display-recent="true"
              :hide-search="true" :disable-skin-tones="true" theme="auto" />
          </div>
        </div>
      </div>
    </van-popup>
    <van-action-sheet v-model:show="showActionSheetMenu" :actions="imageActions" cancel-text="取消" close-on-click-action
      @select="onImageActionSelect" title="选择图片" description="请选择图片来源" />
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted, nextTick, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { getM, postM, isSuccess, baseURL } from '@/utils/request.js'
import { sendMessageWeb } from '@/utils/websocket.js'
import { showToast, showImagePreview, showDialog } from 'vant'
import EmojiPicker from 'vue3-emoji-picker'
import 'vue3-emoji-picker/css'

import Recorder from 'recorder-core'
import 'recorder-core/src/engine/mp3'
import 'recorder-core/src/engine/mp3-engine'

// --- 基础配置 ---
const router = useRouter()
const store = useStore()
const currentUser = computed(() => store.state.user || { username: 'guest' })

// 表情选择器配置
const optionsName = {
  'Smileys & Emotion': '笑脸与表情',
  'People & Body': '人物与身体',
  'Animals & Nature': '动物与自然',
  'Food & Drink': '食物与饮料',
  'Activities': '活动',
  'Travel & Places': '旅行与地点',
  'Objects': '物品',
  'Symbols': '符号',
  'Flags': '旗帜'
}

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

// 表情选择器状态
const showEmojiPicker = ref(false)

// 语音录制状态
const isRecording = ref(false)
const recordingStartTime = ref(0)
const touchStartY = ref(0)
const isSwipingUp = ref(false)
const recordingTimer = ref(null)
const rec = ref(null) // 存放 recorder-core 实例
// 👇 新增这行，记录正在播放的语音消息 ID
const playingVoiceId = ref(null)

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
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 时间格式化 - 聊天分割线用
const formatChatTime = (ts) => {
  const date = new Date(ts)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 时间格式化 - 气泡内用
const formatTimeOnly = (ts) => {
  const date = new Date(ts)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
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
  } catch (e) { }
}

const fetchUnreadCount = async () => {
  try {
    const res = await getM('messages/unreadCount')
    if (isSuccess(res)) badge.value = res.data.data
  } catch (e) { }
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

// 获取录音时长
const getRecordingTime = () => {
  if (!isRecording.value || !recordingStartTime.value) return '0:00'
  const elapsed = Date.now() - recordingStartTime.value
  const seconds = Math.floor(elapsed / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 表情选择处理
const onEmojiSelect = (emoji) => {
  console.log('选择的表情:', emoji)
  if (emoji) {
    // vue3-emoji-picker 返回的是表情对象，需要获取实际的表情字符
    let emojiChar = ''

    if (typeof emoji === 'string') {
      emojiChar = emoji
    } else if (emoji && emoji.i) {
      // 如果是对象，获取 i 属性（表情字符）
      emojiChar = emoji.i
    } else if (emoji && typeof emoji === 'object') {
      // 尝试其他可能的属性
      emojiChar = emoji.emoji || emoji.char || emoji.native || Object.values(emoji)[0]
    }

    if (emojiChar) {
      newMessage.value += emojiChar
      // showEmojiPicker.value = false
      console.log('插入的表情:', emojiChar)
    } else {
      console.warn('无法获取表情字符:', emoji)
      showToast('表情格式错误')
    }
  } else {
    console.warn('表情数据为空:', emoji)
  }
}
// 唤起选择菜单（拍照 or 相册）
// --- 图片选择菜单相关变量 ---
const showActionSheetMenu = ref(false)
const imageActions = ref([
  { name: '拍照', value: 'CAMERA' },
  { name: '从相册选择', value: 'PHOTOS' }
])

// 唤起选择菜单
const showImageActionSheet = () => {
  showActionSheetMenu.value = true
}

// 用户点击了菜单项
const onImageActionSelect = (action) => {
  selectImage(action.value)
}

// --- 改造后的图片选择处理 ---
const selectImage = async (sourceType) => {
  try {
    const { Camera } = await import('@capacitor/camera')

    // 检查相机权限
    const permission = await Camera.checkPermissions()
    if (permission.camera !== 'granted') {
      const result = await Camera.requestPermissions()
      if (result.camera !== 'granted') {
        showToast('需要相机权限才能选择/拍摄图片')
        return
      }
    }

    // 调用 Capacitor 相机
    const image = await Camera.getPhoto({
      quality: 80,
      allowEditing: false, // ⚠️关闭编辑，防止部分安卓机返回空数据
      resultType: 'DataUrl', // 直接获取 base64
      source: sourceType // 'CAMERA' 或 'PHOTOS'
    })

    // 检查并构建图片数据
    let finalImageData = null

    if (image.dataUrl) {
      finalImageData = image.dataUrl
    } else if (image.base64String) {
      // 某些情况下 Capacitor 只返回 base64String，需要手动拼装 dataUrl
      const format = image.format || 'jpeg'
      finalImageData = `data:image/${format};base64,${image.base64String}`
    } else if (image.webPath) {
      finalImageData = image.webPath
    }

    if (!finalImageData) {
      showDialog({
        title: '图片获取失败',
        message: '无法获取有效的图片数据，请重试',
        theme: 'round-button',
      })
      return
    }

    // 发送图片消息
    if (finalImageData.startsWith('data:')) {
      sendImageData(finalImageData)
    } else {
      sendImageMessage(finalImageData)
    }

  } catch (error) {
    console.error('相机/相册调用失败:', error)
    // 捕获异常也用 Dialog 显示
    showDialog({
      title: '操作异常',
      message: error.message || '调用相机或相册时发生错误',
      theme: 'round-button',
    })
  }
}

// 按住说话 - 开始录音
// 按住说话 - 开始录音
const startVoiceRecording = (event) => {
  if (event) event.preventDefault()

  if (isRecording.value) return;

  try {
    // 记录触摸起始位置，用于判断上滑取消
    touchStartY.value = event.touches ? event.touches[0].clientY : 0
    isSwipingUp.value = false

    // 1. 初始化录音实例，强制输出 MP3 格式 (跨端兼容性最好)
    rec.value = Recorder({
      type: "mp3",
      sampleRate: 16000,
      bitRate: 16,
      onProcess: function (buffers, powerLevel, bufferDuration, bufferSampleRate, newSpk) {
        // 这里可以用来做音量波形动画，如果需要的话可以读取 powerLevel (0-100)
      }
    });

    // 2. 请求麦克风权限并打开录音
    rec.value.open(function () {
      // 成功打开麦克风，开始录制
      rec.value.start();

      isRecording.value = true;
      recordingStartTime.value = Date.now();
      showToast('正在录音...');

      // 设置最大录音时长 60 秒
      recordingTimer.value = setTimeout(() => {
        if (isRecording.value) {
          stopVoiceRecording(null, true) // 强制停止并保存
          showToast('录音时长已达上限')
        }
      }, 60000);

    }, function (msg, isUserNotAllow) {
      // 权限被拒绝或设备不支持
      console.error((isUserNotAllow ? "用户拒绝了权限：" : "无法录音：") + msg);
      showToast('无法录音，请检查麦克风权限');
    });

  } catch (error) {
    console.error('录音初始化失败:', error)
    showToast('录音初始化失败')
  }
}

// 按住说话 - 停止录音
// 按住说话 - 停止录音
const stopVoiceRecording = (event, isForceStop = false) => {
  if (event) event.preventDefault()

  if (!isRecording.value || !rec.value) return;

  // 清除超时定时器
  if (recordingTimer.value) {
    clearTimeout(recordingTimer.value)
    recordingTimer.value = null
  }

  // 检查是否上滑取消 (仅在手指操作时检查)
  if (!isForceStop && event && event.changedTouches) {
    const currentY = event.changedTouches[0].clientY
    const deltaY = touchStartY.value - currentY
    if (deltaY > 50) {
      isSwipingUp.value = true
    }
  }

  // 核心：调用 recorder-core 的 stop 拿到数据
  rec.value.stop(async function (blob, duration) {
    // 释放麦克风资源
    rec.value.close();
    rec.value = null;
    isRecording.value = false;

    if (isSwipingUp.value) {
      showToast('已取消录音')
      return;
    }

    if (duration < 1000) { // 少于1秒
      showToast('录音时间太短')
      return;
    }

    // 拿到 mp3 Blob 数据，发送语音
    await sendVoiceMessage(blob)

  }, function (msg) {
    console.error("录音停止出错:" + msg);
    if (rec.value) rec.value.close();
    rec.value = null;
    isRecording.value = false;
    showToast('录音失败');
  });
}

// 按住说话 - 取消录音
// 按住说话 - 取消录音
const cancelVoiceRecording = (event) => {
  if (event) event.preventDefault()

  if (!isRecording.value || !rec.value) return;

  // 强制标记为取消
  isSwipingUp.value = true;
  // 调用停止逻辑，里面会处理取消操作
  stopVoiceRecording(event);
}

// 发送语音消息
const sendVoiceMessage = async (audioBlob) => {
  if (!selectedUser.value) return

  sendingMessage.value = true
  try {
    // 将音频转换为 base64
    const reader = new FileReader()
    reader.onload = async (e) => {
      const base64Audio = e.target?.result as string

      const messageData = {
        toUser: selectedUser.value.username,
        content: base64Audio,
        type: 'voice',
        timestamp: Date.now()
      }

      const res = await postM('messages/send', messageData)
      sendMessageWeb(JSON.stringify(messageData))

      if (isSuccess(res)) {
        messages.value.push(res.data.data)
        newMessage.value = ''
        scrollToBottom()
      }
      await fetchMessages()
      showToast('语音发送成功')
    }
    reader.readAsDataURL(audioBlob)
  } catch (error) {
    console.error('发送语音失败:', error)
    showToast('发送失败')
  } finally {
    sendingMessage.value = false
  }
}

// 发送图片消息
const sendImageMessage = async (imageDataUrl) => {
  if (!selectedUser.value) return

  sendingMessage.value = true
  try {
    // 如果是 webPath，需要转换为 base64
    let finalImageData = imageDataUrl
    if (imageDataUrl.startsWith('file://')) {
      // 在移动端，需要将文件路径转换为 base64
      const response = await fetch(imageDataUrl)
      const blob = await response.blob()
      const reader = new FileReader()
      reader.onload = (e) => {
        finalImageData = e.target?.result as string
        // 继续发送
        sendImageData(finalImageData)
      }
      reader.readAsDataURL(blob)
      return
    }

    sendImageData(finalImageData)
  } catch (error) {
    console.error('发送图片失败:', error)
    showToast('发送失败')
  } finally {
    sendingMessage.value = false
  }
}

// 发送图片数据的辅助函数
const sendImageData = async (imageData) => {
  try {
    const messageData = {
      toUser: selectedUser.value.username,
      content: imageData,
      type: 'image',
      timestamp: Date.now()
    }
    console.log("sendImageMessage", messageData)

    const res = await postM('messages/send', messageData)
    sendMessageWeb(JSON.stringify(messageData))

    if (isSuccess(res)) {
      messages.value.push(res.data.data)
      newMessage.value = ''
      scrollToBottom()
    }
    await fetchMessages()
    showToast('图片发送成功')
  } catch (error) {
    console.error('发送图片失败:', error)
    showToast('发送失败')
  }
}

// 播放语音消息
// 播放语音消息
const playVoice = async (msg) => {
  try {
    console.log('准备播放语音:', msg.id)

    let audioSrc = msg.content

    // 【关键修复 1】：在处理任何异步操作之前，先实例化 Audio 对象。
    // 在 iOS 中，Audio 对象的创建和初步的 play() 尝试最好在用户点击事件的同一个执行栈中。
    const audio = new Audio()

    // 强制设置音频上下文，尝试绕过 iOS 的物理静音键限制（部分浏览器有效）
    // 注意：如果是套壳 App（如 Capacitor/Cordova），这招不一定绝对管用，最根本的还是提醒用户关掉静音键。
    if (window.AudioContext || window.webkitAudioContext) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      const ctx = new AudioContext();
      // 唤醒 audio context
      if (ctx.state === 'suspended') {
        ctx.resume();
      }
    }

    // 如果是 base64 数据，需要转换为 blob URL
    if (msg.content && msg.content.startsWith('data:audio/')) {
      try {
        // 【关键修复 2】：Base64 转 Blob 推荐使用 atob 方式，比 fetch 更稳定，兼容性更好
        const base64Data = msg.content.split(',')[1]
        const contentType = msg.content.split(',')[0].split(':')[1].split(';')[0]

        const byteCharacters = atob(base64Data)
        const byteArrays = []

        for (let offset = 0; offset < byteCharacters.length; offset += 512) {
          const slice = byteCharacters.slice(offset, offset + 512)
          const byteNumbers = new Array(slice.length)
          for (let i = 0; i < slice.length; i++) {
            byteNumbers[i] = slice.charCodeAt(i)
          }
          const byteArray = new Uint8Array(byteNumbers)
          byteArrays.push(byteArray)
        }

        const audioBlob = new Blob(byteArrays, { type: contentType || 'audio/wav' })
        audioSrc = URL.createObjectURL(audioBlob)
      } catch (blobError) {
        console.error('转换音频 base64 失败:', blobError)
        showToast('音频解析失败', blobError)
        return
      }
    }

    // 设置音频源
    audio.src = audioSrc
    audio.preload = 'auto'
    audio.playsInline = true

    // 事件监听
    audio.onplay = () => {
      showToast('正在播放语音')
      console.log('音频开始播放')
      playingVoiceId.value = msg.id;
    }

    audio.onended = () => {
      console.log('音频播放结束')
      playingVoiceId.value = null;
      if (audioSrc.startsWith('blob:')) {
        URL.revokeObjectURL(audioSrc) // 及时释放内存
      }
    }

    audio.onerror = (e) => {
      console.error('音频播放错误:', e)
      playingVoiceId.value = null;
      showToast(`播放失败，格式不支持, ${JSON.stringify(e)}`)
    }

    // 【关键修复 3】：处理播放 Promise，捕获 iOS 的拦截异常
    const playPromise = audio.play()

    if (playPromise !== undefined) {
      playPromise.then(() => {
        // 播放成功
      }).catch(error => {
        console.error('iOS/浏览器拦截了播放:', error)
        showToast(`iOS/浏览器拦截了播放: ${JSON.stringify(error)}`)
        // 在这里可以判断，如果是被拦截，通常是因为没有用户交互，或者静音键问题
        if (error.name === 'NotAllowedError') {
          showToast('请检查手机是否开启了静音模式')
        } else {
          showToast('无法播放此音频')
        }
      })
    }

  } catch (error) {
    console.error('播放语音逻辑崩溃:', error)
    showToast('播放出错', error)
  }
}

// 预览图片
const previewImage = (imageUrl) => {
  try {
    console.log('预览图片:', imageUrl)

    // 使用 Vant 的图片预览功能
    showImagePreview({
      images: [imageUrl],
      startPosition: 0,
      closeable: true,
      showIndicators: true
    })
  } catch (error) {
    console.error('预览图片失败:', error)
    // 备用方案：在新窗口打开
    window.open(imageUrl, '_blank')
  }
}

// 获取语音时长（估算）
const getVoiceDuration = (base64Audio) => {
  // 简单估算：根据 base64 字符串长度估算时长
  // 实际项目中应该从音频文件中读取真实时长
  const estimatedLength = Math.floor(base64Audio.length / 10000)
  return `${estimatedLength}"`
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
  --bg-color: #000000;
  /* 纯黑背景 (OLED 省电且高级) */
  --card-bg: #1C1C1E;
  /* 列表/卡片背景 (深灰) */
  --text-primary: #FFFFFF;
  /* 主文字 */
  --text-secondary: #8E8E93;
  /* 次要文字 */
  --accent-color: #0A84FF;
  /* iOS 蓝色高亮 */
  --bubble-sent: linear-gradient(135deg, #0A84FF, #5E5CE6);
  /* 发送气泡 */
  --bubble-received: #2C2C2E;
  /* 接收气泡 */
  --divider: #38383A;
  /* 分割线 */
  --input-bg: #2C2C2E;
  /* 输入框背景 */
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

/* 语音播放时的动画效果 */
.voice-icon.is-playing {
  animation: voicePlaying 1s linear infinite;
  color: #30D158;
  /* 播放时变成绿色，更明显 */
}

@keyframes voicePlaying {
  0% {
    opacity: 0.3;
  }

  50% {
    opacity: 1;
  }

  100% {
    opacity: 0.3;
  }
}

.sticky-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(28, 28, 30, 0.85);
  /* 毛玻璃 */
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
/* ActionSheet 主题适配 */
:deep(.van-action-sheet) {
  background-color: #1c1c1e !important;
  color: #ffffff !important;
}

:deep(.van-action-sheet__item) {
  background-color: #1c1c1e !important;
  color: #ffffff !important;
}

:deep(.van-action-sheet__cancel) {
  background-color: #2c2c2e !important;
  color: #ff3b30 !important;
  border-top: 8px solid #000000;
}

:deep(.van-action-sheet__header) {
  color: #ffffff !important;
  border-bottom: 0.5px solid rgba(255, 255, 255, 0.1);
}

:deep(.van-action-sheet__description) {
  color: rgba(255, 255, 255, 0.6) !important;
}

/* Dialog 主题适配 */
:deep(.van-dialog) {
  background-color: #1c1c1e !important;
  color: #ffffff !important;
}

:deep(.van-dialog__header) {
  color: #ffffff !important;
}

:deep(.van-dialog__message) {
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.van-dialog__footer) {
  border-top: 0.5px solid rgba(255, 255, 255, 0.1);
}

:deep(.van-dialog__confirm) {
  background-color: #1c1c1e !important;
  color: #0a84ff !important;
}

:deep(.van-dialog__cancel) {
  background-color: #1c1c1e !important;
  color: #ff3b30 !important;
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
  height: calc(60px + env(safe-area-inset-top));
  /* 适配刘海屏高度 */
  display: flex;
  align-items: center;
  padding: env(safe-area-inset-top) 12px 0;
  /* 顶部增加安全距离 */
  box-sizing: border-box;
  /* 确保高度包含 padding */
  z-index: 100;
  border-bottom: 0.5px solid var(--divider);
}

.header-left,
.header-right {
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

  .status-online {
    color: #30D158;
  }

  .status-offline {
    color: var(--text-secondary);
  }
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
  height: calc(70px + env(safe-area-inset-top));
  /* 留出 header 空间 (含安全区域) */
}

.chat-start-tip {
  text-align: center;
  margin-top: 40px;
  color: var(--text-secondary);
  font-size: 14px;
}

.bottom-anchor {
  height: 120px;
  /* 留出 footer 空间 */
  padding-bottom: env(safe-area-inset-bottom);
  /* 适配 iPhone 底部小黑条 */
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
    background: rgba(255, 255, 255, 0.1);
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
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
    border-bottom-right-radius: 4px;
    /* 小尾巴效果 */
  }
}

/* 接收方样式 */
.msg-received {
  align-items: flex-start;

  .bubble {
    background: var(--bubble-received);
    color: var(--text-primary);
    border-bottom-left-radius: 4px;
    /* 小尾巴效果 */
  }
}

/* 底部输入框 */
.chat-input-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: rgba(20, 25, 30, 0.95);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 12px 16px 20px;
  z-index: 100;
}

/* 输入容器 */
.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 12px;
}

/* 输入框样式 */
.custom-field {
  flex: 1;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  padding: 10px 16px;
  font-size: 16px;
  color: var(--text-primary);
  transition: all 0.3s ease;

  &:focus-within {
    background: rgba(255, 255, 255, 0.12);
    border-color: rgba(10, 132, 255, 0.5);
    box-shadow: 0 0 0 2px rgba(10, 132, 255, 0.2);
  }

  :deep(.van-field__control) {
    color: var(--text-primary);
    font-size: 16px;
    line-height: 1.4;
    min-height: 20px;

    &::placeholder {
      color: var(--text-secondary);
      font-size: 15px;
    }
  }
}

/* 发送按钮 */
.send-btn {
  min-width: 60px;
  height: 40px;
  background: linear-gradient(135deg, #0A84FF, #0056CC);
  border: none;
  border-radius: 20px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    transform: scale(0.95);
    background: linear-gradient(135deg, #0056CC, #003D8F);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .loading-icon {
    animation: spin 1s linear infinite;
  }
}

/* 功能工具栏 */
.function-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

/* 工具按钮 */
.tool-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    background: rgba(255, 255, 255, 0.15);
    transform: scale(0.9);
  }

  &.active {
    background: rgba(10, 132, 255, 0.2);
    border-color: rgba(10, 132, 255, 0.4);
    color: #0A84FF;
  }

  &.emoji-btn {
    &:active {
      background: rgba(255, 215, 0, 0.2);
      border-color: rgba(255, 215, 0, 0.3);
      color: #FFD700;
    }

    &.active {
      background: rgba(255, 215, 0, 0.2);
      border-color: rgba(255, 215, 0, 0.4);
      color: #FFD700;
    }
  }
}

/* 录音显示样式 */
.recording-display {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(231, 76, 60, 0.1);
  border: 1px solid rgba(231, 76, 60, 0.3);
  border-radius: 20px;
  padding: 10px 16px;
  gap: 12px;
}

.recording-waves {
  display: flex;
  gap: 3px;
  align-items: center;
}

.wave {
  width: 3px;
  height: 20px;
  background: #e74c3c;
  border-radius: 2px;
  animation: waveAnimation 1s ease-in-out infinite;

  &:nth-child(1) {
    animation-delay: 0s;
  }

  &:nth-child(2) {
    animation-delay: 0.1s;
  }

  &:nth-child(3) {
    animation-delay: 0.2s;
  }

  &:nth-child(4) {
    animation-delay: 0.3s;
  }

  &:nth-child(5) {
    animation-delay: 0.4s;
  }
}

.recording-time {
  color: #e74c3c;
  font-size: 14px;
  font-weight: 500;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
}

@keyframes waveAnimation {

  0%,
  100% {
    height: 20px;
    opacity: 0.3;
  }

  50% {
    height: 30px;
    opacity: 1;
  }
}

/* 语音按钮样式 */
.voice-btn {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.2), rgba(255, 142, 83, 0.2));
  border-color: rgba(255, 107, 107, 0.3);
  color: #FF6B6B;

  &:active {
    background: linear-gradient(135deg, rgba(231, 76, 60, 0.3), rgba(192, 57, 43, 0.3));
    border-color: rgba(231, 76, 60, 0.5);
    transform: scale(0.9);
  }
}

/* 录音中按钮样式 */
.recording-btn {
  background: linear-gradient(135deg, rgba(231, 76, 60, 0.3), rgba(192, 57, 43, 0.3));
  border-color: rgba(231, 76, 60, 0.5);
  color: #e74c3c;
  animation: recordingPulse 1.5s ease-in-out infinite;

  &:active {
    transform: scale(0.8);
    background: linear-gradient(135deg, rgba(192, 57, 43, 0.4), rgba(169, 50, 38, 0.4));
  }
}

@keyframes recordingPulse {

  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(231, 76, 60, 0.7);
  }

  50% {
    box-shadow: 0 0 0 10px rgba(231, 76, 60, 0);
  }
}

/* 表情选择器容器 */
.emoji-picker-container {
  background: rgba(20, 20, 30, 0.98);
  border-radius: 16px 16px 0 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  max-height: 40vh;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

.emoji-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);

  span {
    color: var(--text-primary);
    font-size: 14px;
    font-weight: 500;
  }

  .close-btn {
    background: none;
    border: none;
    color: var(--text-secondary);
    font-size: 16px;
    cursor: pointer;
    padding: 4px;
    border-radius: 4px;
    transition: all 0.2s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
      color: var(--text-primary);
    }
  }
}

/* 动画效果 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.bubble-image {
  .msg-image {
    max-width: 200px;
    max-height: 200px;
    border-radius: 8px;
    cursor: pointer;
  }
}

@keyframes voicePulse {

  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }

  50% {
    opacity: 0.6;
    transform: scale(1.1);
  }
}

/* 表情选择器弹窗 */
.emoji-picker-popup {
  background: var(--card-bg);

  :deep(.van-popup__content) {
    background: var(--card-bg);
  }
}

.emoji-picker-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
}

.emoji-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--divider);

  span {
    color: var(--text-primary);
    font-size: 16px;
    font-weight: 500;
  }

  .van-button {
    color: var(--text-secondary);
    font-size: 20px;
  }
}

.emoji-picker {
  flex: 1;
  overflow-y: auto;

  :deep(.emoji-picker) {
    background: var(--card-bg);

    .emoji-picker__search {
      background: var(--input-bg);
      border: 1px solid var(--divider);
      color: var(--text-primary);
    }

    .emoji-picker__emoji {
      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }
    }

    .emoji-picker__category-name {
      color: var(--text-secondary);
    }
  }
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
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
<style>
/* 全局 Dialog 主题适配 (因为 showDialog 挂载在 body 上，scoped 无法生效) */
.van-dialog {
  background-color: #1c1c1e !important;
}

.van-dialog__header {
  color: #ffffff !important;
}

.van-dialog__message {
  color: rgba(255, 255, 255, 0.8) !important;
  /* 针对报错信息，左对齐并允许长单词换行，方便阅读 */
  text-align: left !important;
  word-break: break-all !important;
  max-height: 60vh;
  overflow-y: auto;
}

.van-dialog__footer {
  border-top: 0.5px solid rgba(255, 255, 255, 0.1) !important;
  background-color: #1c1c1e !important;
}

.van-dialog__confirm,
.van-dialog__cancel {
  background-color: #1c1c1e !important;
}

.van-dialog__confirm {
  color: #0a84ff !important;
}

.van-dialog__cancel {
  color: #ff3b30 !important;
}
</style>