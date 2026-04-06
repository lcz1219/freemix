<template>
  <!-- 顶部导航栏 -->
  <n-layout style="height: 100%;">
    <n-layout-header >
      <!-- <div style="padding: 16px; display: flex; justify-content: flex-start; align-items: center;"> -->
        <!-- <n-button text @click="turnHome()">
          <n-icon style="margin-right: 10px"><ArrowBack /></n-icon>

        </n-button> -->
        <!-- <h2 style="margin: 0;">消息中心</h2> -->
        
      <!-- </div> -->
      <!-- <n-button type="primary" @click="show()">
         
          发送消息
        </n-button> -->
    </n-layout-header>
    
    <n-layout has-sider style="height: calc(100vh - 64px);; ">
      <!-- 侧边栏 - 用户列表 -->
      <n-layout-sider 
        bordered 
        width="240" 
        style="height: 100%;"
        :collapsed-width="0" 
        collapse-mode="width"
        :collapsed="collapsed"
        show-trigger
        @collapse="collapsed = true"
        @expand="collapsed = false"
      >
        <div style="padding: 16px; height: calc(100% - 32px); display: flex; flex-direction: column;">
          <n-input 
            v-model:value="searchUser" 
            placeholder="搜索用户" 
            clearable
            style="margin-bottom: 16px;"
          >
            <template #prefix>
              <n-icon><Search /></n-icon>
            </template>
          </n-input>
          
          <n-list style="flex: 1; overflow-y: auto;">
            <n-list-item 
              v-for="user in filteredUsers" 
              :key="user.username"
              @click="selectUser(user)"
              :class="{ 'active-user': selectedUser && selectedUser.username === user.username }"
              style="cursor: pointer;"
            >
              <n-thing>
                <template #avatar>
                <div style="position: relative; display: inline-block;">
                  <n-badge :value="computedBadge(user.username)" :max="15">
                    <n-avatar
                        v-if="user.avatarUrl"
                      :src="showAvatar(user.avatarUrl)" 
                      round 
                      size="small"
                        fallback-src="https://bpic.588ku.com/element_origin_min_pic/19/04/10/e87e154ddafd724a915a119fb21c38b9.jpg"
                    />
                    <n-icon :size="24"
                        v-else
                    ><Accessibility/></n-icon>
                  </n-badge>
                  <!-- 在线状态指示点 -->
                  <div 
                    :class="['status-indicator', isUserOnline(user.username) ? 'online' : 'offline']"
                  ></div>
                </div>
              </template>
                <template #header>
                  {{ user.chinesename || user.username }}
                </template>
                <template #description>
                  <n-text depth="3">
                    {{ user.username }}
                  </n-text>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </div>
      </n-layout-sider>
      
      <!-- 主内容区域 - 消息对话 -->
      <n-layout>
        <n-layout-content 
          ref="messageContainer" 
          style="padding: 16px; height: 80%; overflow-y: scroll; overflow-x: hidden; min-height: 100px;"
          v-if="selectedUser"
        >
          <div v-if="messages.length === 0" style="text-align: center; padding: 40px 0;">
            <n-empty description="暂无消息">
              <template #icon>
                <n-icon>
                  <Chatbox />
                </n-icon>
              </template>
            </n-empty>
          </div>
          
          <div 
            v-for="message in messages" 
            :key="message.id"
            :class="['message-item', message.fromUser === currentUser.username ? 'sent' : 'received']"
          >
            <div class="message-content">
              <!-- 文本消息 -->
              <div v-if="message.type === 'text' || !message.type" class="message-text">{{ message.content }}</div>
              
              <!-- 图片消息 -->
              <div v-else-if="message.type === 'image'" class="message-image">
                <img 
                  :src="message.content" 
                  alt="图片"
                  class="msg-image"
                  @click="previewImage(message.content)"
                />
              </div>
              
              <!-- 语音消息 -->
              <div v-else-if="message.type === 'voice'" class="message-voice">
                <div class="voice-player" @click="playVoice(message)">
                  <n-icon :component="MicrophoneIcon" class="voice-icon" />
                  <span class="voice-duration">{{ getVoiceDuration(message.content) }}"</span>
                </div>
              </div>
              
              <div class="message-time">
                {{ formatTime(message.createdAt) }}
              </div>
            </div>
          </div>
        </n-layout-content>
        
        <n-layout-footer 
          bordered 
          v-if="selectedUser"
          style="padding: 16px; height: 20%; min-height: 100px;"
        >
          <div class="input-area">
            <!-- 主输入区 -->
             <!-- 功能按钮区 -->
            <div class="function-buttons">
              <!-- <n-button circle @click="showEmojiPicker = !showEmojiPicker" title="表情">
                <template #icon>
                  <n-icon><SmileIcon /></n-icon>
                </template>
              </n-button> -->
              <n-button circle @click="selectImage" title="图片">
                <template #icon>
                  <n-icon><ImageIcon /></n-icon>
                </template>
              </n-button>
              <!-- <n-button circle @click="toggleVoiceRecording" :type="isRecording ? 'error' : 'default'" title="语音">
                <template #icon>
                  <n-icon><MicrophoneIcon /></n-icon>
                </template>
              </n-button> -->
              <!-- <n-button type="primary" @click="sendMessage" :disabled="!newMessage.trim()">
                发送
              </n-button> -->
            </div>
            <div class="input-container">
              <n-input 
                v-model:value="newMessage" 
                type="textarea" 
                placeholder="输入消息..." 
                :autosize="{ minRows: 2, maxRows: 4 }"
                @keydown.enter="handleSendMessage"
                style="flex: 1;"
              />
            </div>
            
            
            
            <!-- 表情选择器 -->
            <div class="emoji-picker-container" v-show="showEmojiPicker">
              <div class="emoji-picker-header">
                <span>表情</span>
                <button @click="showEmojiPicker = false" class="close-btn">
                  ×
                </button>
              </div>
              <EmojiPicker 
                :native="true" 
                @select="onEmojiSelect" 
                :group-names="optionsName" 
                :display-recent="true"
                :hide-search="true" 
                :disable-skin-tones="true" 
                theme="auto" 
              />
            </div>
            
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
          </div>
        </n-layout-footer>
        
        <n-layout-content v-else style="display: flex; justify-content: center; align-items: center; height: 100%;">
          <n-empty description="请选择一个用户开始聊天">
            <template #icon>
              <n-icon>
                <Chatbox />
              </n-icon>
            </template>
          </n-empty>
        </n-layout-content>
      </n-layout>
    </n-layout>
    
    <!-- 发送消息模态框 -->
    <n-modal v-model:show="showSendMessageModal" preset="dialog" title="发送消息">
      <n-form :model="messageForm" :rules="messageRules" ref="messageFormRef">
        <n-form-item label="接收者" path="toUser">
          <n-select 
            v-model:value="messageForm.toUser" 
            :options="userOptions" 
            placeholder="选择接收者"
            filterable
          />
        </n-form-item>
        <n-form-item label="消息内容" path="content">
          <n-input 
            v-model:value="messageForm.content" 
            type="textarea" 
            placeholder="输入消息内容"
            :autosize="{ minRows: 3, maxRows: 6 }"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="showSendMessageModal = false">取消</n-button>
        <n-button type="primary" @click="sendDirectMessage" :loading="sendingMessage">发送</n-button>
      </template>
    </n-modal>
  </n-layout>
</template>

<script lang="ts">
// 全局状态：用于在组件重建时保持滚动位置状态
let globalScrollPosition = 9999999;
</script>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useMessage } from 'naive-ui'
import NavBar from '@/components/NavBar.vue';
import { getM, postM, isSuccess,baseURL } from '@/utils/request'
import { 
  Send, 
  Search, 
  Chatbox,
  ArrowBack,
  Accessibility
  
} from '@vicons/ionicons5'
import { sendMessageWeb } from '@/utils/websocket.js'
import {
  NLayout,
  NLayoutHeader,
  NLayoutSider,
  NLayoutContent,
  NLayoutFooter,
  NButton,
  NInput,
  NList,
  NListItem,
  NThing,
  NAvatar,
  NText,
  NEmpty,
  NBadge,
  NModal,
  NForm,
  NFormItem,
  NSelect,
  NIcon,
  NSpin
} from 'naive-ui'
import { MoodSmile as SmileIcon, Photo as ImageIcon, Microphone as MicrophoneIcon,  } from '@vicons/tabler'
import router from "@/router";
import {genMsg} from '@/utils/genMsg.js'
import EmojiPicker from 'vue3-emoji-picker'
import 'vue3-emoji-picker/css'
import { showImagePreview } from 'vant'
import 'vant/lib/index.css'

// 定义消息类型
interface Message {
  id: string
  fromUser: string
  toUser: string
  content: string
  createdAt: number
  isRead: boolean
  type: string
}

// 定义用户类型
interface User {
  username: string
  chinesename?: string
  avatarUrl?: string
}
const badge = ref({})
const  turnHome=()=>{
  router.push("/home")
}
const store = useStore()
const message = useMessage()
const show=()=>{
  
  showSendMessageModal.value = true
  console.log("showSendMessageModal.value:",showSendMessageModal.value);
  
}

const showAvatar=(item)=>{
  // if(!item) return `${baseURL()}${'/file/WechatIMG105.jpg'}`
  return `${baseURL()}${item}` 
}
// 响应式数据

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
const collapsed = ref(false)
const searchUser = ref('')
const selectedUser = ref<User | null>(null)
const messages = ref<Message[]>([])
const newMessage = ref('')
const showSendMessageModal = ref(false)
const sendingMessage = ref(false)
const userStatus = ref<Record<string, boolean>>({})

// 轮询相关
// 表单相关
const messageFormRef = ref()
const messageForm = ref({
  toUser: '',
  content: ''
})
const computedBadge=(item)=>{
  let count = 0
return badge.value[item]
}
const messageRules = {
  toUser: {
    required: true,
    message: '请选择接收者',
    trigger: 'blur'
  },
  content: {
    required: true,
    message: '请输入消息内容',
    trigger: 'blur'
  }
}

// 表情选择器状态
const showEmojiPicker = ref(false)

// 语音录制状态
const isRecording = ref(false)
const recordingStartTime = ref(0)
const mediaRecorder = ref(null)
const audioChunks = ref([])
const recordingTimer = ref(null)

// 当前用户
const currentUser = computed(() => store.state.user)

// 获取所有用户
const allUsers = ref<User[]>([])

// 过滤用户列表
const filteredUsers = computed(() => {
  if (!searchUser.value) {
    return allUsers.value
  }
  console.log("searchUser.value:",searchUser.value);
  
  const search = searchUser.value.trim()
  console.log("allUsers.value:",allUsers.value);
  
  return allUsers.value.filter(user => 
    user.username.includes(search) || 
    (user.chinesename && user.chinesename.includes(search))
  )
})

// 用户选项（用于选择器）
const userOptions = computed(() => {
  return allUsers.value
    .filter(user => user.username !== currentUser.value.username)
    .map(user => ({
      label: `${user.chinesename || user.username} (${user.username})`,
      value: user.username
    }))
})

// 格式化时间
const formatTime = (timestamp: number) => {
  try {
    const date = new Date(timestamp)
    const now = new Date()
    
    // 如果是今天，只显示时间
    if (date.toDateString() === now.toDateString()) {
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }
    
    // 如果是今年，显示月日和时间
    if (date.getFullYear() === now.getFullYear()) {
      return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }
    
    // 其他情况显示完整日期
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  } catch (error) {
    console.error('时间格式化失败:', error)
    return '未知时间'
  }
}

// 获取所有用户
const fetchAllUsers = async () => {
  try {
    // 调用获取用户列表的API
    const res = await getM('getOwerList')
    if (isSuccess(res)) {
      res.data.data.map(user=>{
        user.username=user.value,
        user.chinesename=user.text
        return user
      })
      allUsers.value = res.data.data.filter((user: User) => user.username !== currentUser.value.username)
      //默认选择第一个初始化时候
      selectUser(allUsers.value[0])
      return true
    } else {
      // 如果API调用失败，使用模拟数据
      console.warn('获取用户列表API失败，使用模拟数据:', res.data?.msg)
      const mockUsers = [
        { username: 'admin', chinesename: '管理员' },
        { username: 'linchengzhong', chinesename: '林成中' },
        { username: 'user1', chinesename: '用户一' },
        { username: 'user2', chinesename: '用户二' }
      ]
      allUsers.value = mockUsers.filter(user => user.username !== currentUser.value.username)
      return false
    }
  } catch (error: any) {
    // 如果出现异常，使用模拟数据
    console.warn('获取用户列表异常，使用模拟数据:', error)
    const mockUsers: User[] = [
     
    ]
    allUsers.value = mockUsers
    return false
  }
}

// 选择用户
const selectUser = async (user: User) => {
  try {
    // 停止之前的轮询
    // stopPolling()
    
    selectedUser.value = user
    await fetchMessages()
    scrollToBottom()
    
    // 开始新的轮询
    
  } catch (error) {
    console.error('选择用户失败:', error)
    message.error('选择用户失败')
  }
}

// 获取消息
const fetchMessages = async () => {
  if (!selectedUser.value) return
  
  try {
    // 获取与选中用户的所有消息（发送的和接收的）
    const [sentRes, receivedRes] = await Promise.all([
      getM('messages/sent'),
      getM('messages/received')
    ])
    
    if (isSuccess(sentRes) && isSuccess(receivedRes)) {
      const sentMessages = sentRes.data.data || []
      const receivedMessages = receivedRes.data.data || []
      
      // 过滤出与当前选中用户相关的消息
      const userMessages = [...sentMessages, ...receivedMessages].filter(
        (msg: Message) => 
          (msg.fromUser === currentUser.value.username && msg.toUser === selectedUser.value?.username) ||
          (msg.fromUser === selectedUser.value?.username && msg.toUser === currentUser.value.username) 
      )
      
      // 按时间排序
      messages.value = userMessages.sort((a: Message, b: Message) => a.createdAt - b.createdAt)
      //消息提醒 - 只对当前用户的新消息进行提醒
      console.log("messages.value:",messages.value);
      messages.value.forEach(msg => {
        // 只提醒接收的消息，且未读的消息
        // if(msg.toUser === currentUser.value.username && !msg.isRead){
        //     genMsg(`${msg.fromUserChinesename || msg.fromUser}: ${msg.content}`)
        // }
      })
      
      
      // 标记接收的消息为已读
      await markMessagesAsRead()
    } else {
      const errorMsg = sentRes.data?.msg || receivedRes.data?.msg || '获取消息失败'
      message.error(errorMsg)
    }
  } catch (error: any) {
    if (error?.message) {
      message.error(error.message)
    } else {
      message.error('获取消息失败')
    }
    console.error('获取消息失败:', error)
  }
}

// 标记消息为已读
const markMessagesAsRead = async () => {
  if (!selectedUser.value) return
  
  try {
    // 找出未读的接收消息并标记为已读
    const unreadMessages = messages.value.filter(
      msg => msg.toUser === currentUser.value.username && !msg.isRead
    )
    
    // 如果有未读消息，才调用接口
    if (unreadMessages.length > 0) {
      const messageIds = unreadMessages.map(msg => msg.id)
      const res = await postM('messages/markAsReadBatch', { messageIds })
      
      if (isSuccess(res)) {
        // 更新本地消息状态
        messages.value.forEach(msg => {
          if (messageIds.includes(msg.id)) {
            msg.isRead = true
          }
        })
        fetchUnreadCount()
        return true
      } else {
        message.error(res.data.msg || '标记消息为已读失败')
        console.error('标记消息为已读失败:', res.data.msg)
        return false
      }
    }
    return true
  } catch (error: any) {
    if (error?.message) {
      message.error(error.message)
    } else {
      message.error('标记消息为已读失败')
    }
    console.error('标记消息为已读失败:', error)
    return false
  }
}



// 发送消息
const sendMessage = async () => {
  if (!newMessage.value.trim() || !selectedUser.value) return
  
  try {
    const content = newMessage.value.trim()
    if (!content) {
      message.error('消息内容不能为空')
      return
    }
    
    const messageData = {
      toUser: selectedUser.value.username,
      content: content,
      type: 'text',
      fromUser: currentUser.value.username
    }
    
    const res = await postM('messages/send', messageData)
     sendMessageWeb(JSON.stringify(messageData))
    
    if (isSuccess(res)) {
      // 立即添加到消息列表
      messages.value.push(res.data.data)
      newMessage.value = ''
      scrollToBottom()
      // message.success('消息发送成功')
      
      // 立即刷新消息列表，确保显示最新消息
      await fetchMessages()
      fetchAllUsers()
    } else {
      message.error(res.data.msg || '消息发送失败')
    }
  } catch (error: any) {
    if (error?.message) {
      message.error(error.message)
    } else {
      message.error('消息发送失败')
    }
    console.error('消息发送失败:', error)
  }
}

// 直接发送消息（通过模态框）
const sendDirectMessage = async () => {
  try {
    await messageFormRef.value?.validate()
    
    const content = messageForm.value.content.trim()
    if (!content) {
      message.error('消息内容不能为空')
      return
    }
    
    if (!messageForm.value.toUser) {
      message.error('请选择接收者')
      return
    }
    
    sendingMessage.value = true
    const messageData = {
      toUser: messageForm.value.toUser,
      content: content,
      type: 'text'
    }
    
    const res = await postM('messages/send', messageData)
    
    if (isSuccess(res)) {
      message.success('消息发送成功')
      showSendMessageModal.value = false
      messageForm.value = {
        toUser: '',
        content: ''
      }
      
      // 如果正在与该用户聊天，刷新消息
      if (selectedUser.value?.username === messageForm.value.toUser) {
        await fetchMessages()
      }
    } else {
      message.error(res.data.msg || '消息发送失败')
    }
  } catch (error: any) {
    if (error?.message) {
      message.error(error.message)
    } else {
      message.error('消息发送失败')
    }
  } finally {
    sendingMessage.value = false
  }
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
      console.log('插入的表情:', emojiChar)
    } else {
      console.warn('无法获取表情字符:', emoji)
      message.error('表情格式错误')
    }
  } else {
    console.warn('表情数据为空:', emoji)
  }
}

// 图片选择处理
const selectImage = async () => {
  try {
    // 创建文件输入元素
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.multiple = false
    
    input.onchange = async (event) => {
      const file = event.target.files[0]
      if (file) {
        // 转换为 base64
        const reader = new FileReader()
        reader.onload = (e) => {
          const imageData = e.target.result
          sendImageMessage(imageData)
        }
        reader.readAsDataURL(file)
      }
    }
    
    input.click()
  } catch (error) {
    message.error('选择图片失败: ' + error.message)
  }
}

// 发送图片消息
const sendImageMessage = async (imageData) => {
  if (!selectedUser.value) return
  
  sendingMessage.value = true
  try {
    const messageData = {
      toUser: selectedUser.value.username,
      content: imageData,
      type: 'image',
      timestamp: Date.now()
    }
    
    const res = await postM('messages/send', messageData)
    sendMessageWeb(JSON.stringify(messageData))
    
    if (isSuccess(res)) {
      messages.value.push(res.data.data)
      scrollToBottom()
    }
    await fetchMessages()
    message.success('图片发送成功')
  } catch (error) {
    message.error('发送失败')
  } finally {
    sendingMessage.value = false
  }
}



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
    // 降级方案：在新窗口打开图片
    window.open(imageUrl, '_blank')
  }
}

// 切换语音录制
const toggleVoiceRecording = () => {
  if (isRecording.value) {
    stopVoiceRecording()
  } else {
    startVoiceRecording()
  }
}

// 开始录音
const startVoiceRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    
    mediaRecorder.value = new MediaRecorder(stream)
    audioChunks.value = []
    
    mediaRecorder.value.ondataavailable = (event) => {
      audioChunks.value.push(event.data)
    }
    
    mediaRecorder.value.onstop = async () => {
      const audioBlob = new Blob(audioChunks.value, { type: 'audio/wav' })
      await sendVoiceMessage(audioBlob)
      
      // 清理资源
      audioChunks.value = []
      mediaRecorder.value = null
      stream.getTracks().forEach(track => track.stop())
    }
    
    // 开始录音
    mediaRecorder.value.start()
    isRecording.value = true
    recordingStartTime.value = Date.now()
    
    // 设置最大录音时长 60 秒
    recordingTimer.value = setTimeout(() => {
      if (isRecording.value) {
        stopVoiceRecording()
        message.warning('录音时长已达上限')
      }
    }, 60000)
    
  } catch (error) {
    message.error('录音失败: ' + error.message)
  }
}

// 停止录音
const stopVoiceRecording = () => {
  if (!isRecording.value) return
  
  try {
    // 停止录音
    if (mediaRecorder.value && mediaRecorder.value.state === 'recording') {
      mediaRecorder.value.stop()
    }
    
    // 清除定时器
    if (recordingTimer.value) {
      clearTimeout(recordingTimer.value)
      recordingTimer.value = null
    }
    
    // 重置状态
    isRecording.value = false
    
  } catch (error) {
    message.error('停止录音失败: ' + error.message)
  }
}

// 发送语音消息
const sendVoiceMessage = async (audioBlob) => {
  if (!selectedUser.value) return
  
  try {
    // 将音频转换为 base64
    const reader = new FileReader()
    reader.onloadend = async () => {
      const base64Audio = reader.result
      
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
        scrollToBottom()
      }
      await fetchMessages()
    }
    reader.readAsDataURL(audioBlob)
  } catch (error) {
    message.error('语音发送失败')
  }
}

// 播放语音消息
const playVoice = async (msg) => {
  try {
    console.log('播放语音:', msg.content)
    
    // 检查是否为 base64 数据
    let audioSrc = msg.content
    
    // 如果是 base64 数据，需要转换为 blob URL
    if (msg.content && msg.content.startsWith('data:audio/')) {
      try {
        const audioBlob = await fetch(msg.content).then(r => r.blob())
        audioSrc = URL.createObjectURL(audioBlob)
        console.log('转换后的音频 URL:', audioSrc)
      } catch (blobError) {
        console.error('转换音频 blob 失败:', blobError)
        message.error('音频格式错误')
        return
      }
    }
    
    // 创建音频对象
    const audio = new Audio(audioSrc)
    
    // 设置音频属性以确保在浏览器上正常播放
    audio.preload = 'auto'
    
    // 添加事件监听
    audio.addEventListener('loadstart', () => {
      console.log('音频开始加载')
    })
    
    audio.addEventListener('canplay', () => {
      console.log('音频可以播放')
    })
    
    audio.addEventListener('play', () => {
      console.log('音频开始播放')
    })
    
    audio.addEventListener('ended', () => {
      console.log('音频播放结束')
      // 清理 blob URL
      if (audioSrc.startsWith('blob:')) {
        URL.revokeObjectURL(audioSrc)
      }
    })
    
    audio.addEventListener('error', (e) => {
      console.error('音频播放错误:', e)
      message.error('播放失败')
      // 清理 blob URL
      if (audioSrc.startsWith('blob:')) {
        URL.revokeObjectURL(audioSrc)
      }
    })
    
    // 尝试播放
    await audio.play()
    
  } catch (error) {
    console.error('播放语音失败:', error)
    message.error('播放失败')
  }
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

// 获取语音时长（简化版本）
const getVoiceDuration = (audioData) => {
  // 这里返回一个估算值，实际项目中可能需要更精确的计算
  return '0:05'
}

// 处理回车发送消息
const handleSendMessage = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 滚动到底部
const messageContainer = ref()
const scrollToBottom = () => {
  nextTick(() => {
    const container = messageContainer.value
    
    // 如果容器不存在（可能正在渲染），延迟重试
    if (!container) {
      // console.warn("messageContainer is null, retrying scroll in 100ms...");
      setTimeout(scrollToBottom, 300);
      return;
    }

    // 获取原生 DOM 元素
    const el = container.$el || container
    
    // 使用 setTimeout 确保 DOM 完全渲染
    setTimeout(() => {
      // 如果高度为 0，尝试延迟重试
      if (el.scrollHeight === 0) {
        // console.warn("scrollHeight is 0, retrying scroll...");
        setTimeout(() => {
          globalScrollPosition += 100;
          el.scrollTop = globalScrollPosition;
        }, 300);
        return;
      }

      // 增加偏移量，确保数值变化
      globalScrollPosition += 100
      // console.log("scrollToBottom target:", globalScrollPosition, "scrollHeight:", el.scrollHeight)
      
      // 直接设置 scrollTop 属性
      el.scrollTop = globalScrollPosition
      container.scrollTo({ top: globalScrollPosition, behavior: 'smooth' })
    }, 100)
  })
}

// 获取用户在线状态
const fetchUserStatus = async () => {
  try {
    const res = await getM('user-status/all')
    if (res && res.data) {
      userStatus.value = res.data
      console.log('用户在线状态:', userStatus.value)
    }
  } catch (error) {
    console.error('获取用户在线状态失败:', error)
  }
}

// 获取未读消息数量
const fetchUnreadCount = async () => {
  try {
    const res = await getM('messages/unreadCount')
    if (isSuccess(res)) {
      // 这里可以更新全局未读消息数
      // console.log('未读消息数量:', res.data.data)
      badge.value = res.data.data
      return res.data.data
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
  return 0
}

// 检查用户是否在线
const isUserOnline = (username: string) => {
  return userStatus.value[username] || false
}



let statusInterval: number | null = null

// 监听WebSocket用户状态更新事件
const handleUserStatusUpdate = (event) => {
  const onlineUsers = event.detail;
  // 更新用户状态
  allUsers.value.forEach(user => {
    userStatus.value[user.username] = onlineUsers.includes(user.username);
  });
};

// 监听重新加载消息事件
const handleReloadMessages = async () => {
  if (selectedUser.value) {
    await fetchMessages();
    scrollToBottom();
  }
  fetchUnreadCount();
};

// 初始化
onMounted(async () => {
  try {
    const usersLoaded = await fetchAllUsers()
    const unreadCount = await fetchUnreadCount()
    await fetchUserStatus() // 获取初始用户在线状态
    
    // 每5秒获取一次用户在线状态
    statusInterval = window.setInterval(fetchUserStatus, 5000)
    
    window.addEventListener('userStatusUpdate', handleUserStatusUpdate);
    window.addEventListener('reload-messages', handleReloadMessages);
    
    // if (!usersLoaded) {
    //   message.warning('用户列表加载失败，正在使用模拟数据')
    // }
    
    console.log('初始化完成，未读消息数量:', unreadCount)
    
  } catch (error: any) {
    console.error('初始化失败:', error)
    if (error?.message) {
      message.error('初始化失败: ' + error.message)
    } else {
      message.error('初始化失败')
    }
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (statusInterval) {
    clearInterval(statusInterval)
  }
  window.removeEventListener('reload-messages', handleReloadMessages);
})

// 监听消息变化，自动滚动到底部
// watch(messages, () => {
//   scrollToBottom()
// })

defineExpose({
  fetchMessages,
  scrollToBottom
});
</script>

<style scoped>
.input-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 100%;
}

.input-container {
  flex: 1;
}

.function-buttons {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  align-items: center;
}

.emoji-picker-container {
  position: absolute;
  bottom: 100%;
  right: 0;
  z-index: 1000;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  max-height: 300px;
  overflow-y: auto;
}

.emoji-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #e0e0e0;
  font-weight: bold;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #666;
}

.close-btn:hover {
  color: #333;
}

.recording-display {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  margin-top: 8px;
}

.recording-waves {
  display: flex;
  gap: 3px;
}

.wave {
  width: 3px;
  height: 16px;
  background: #409eff;
  border-radius: 2px;
  animation: wave 1s ease-in-out infinite;
}

.wave:nth-child(2) {
  animation-delay: 0.1s;
}

.wave:nth-child(3) {
  animation-delay: 0.2s;
}

.wave:nth-child(4) {
  animation-delay: 0.3s;
}

.wave:nth-child(5) {
  animation-delay: 0.4s;
}

@keyframes wave {
  0%, 100% {
    height: 8px;
  }
  50% {
    height: 16px;
  }
}

.recording-time {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.message-image {
  margin-bottom: 8px;
}

.msg-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.msg-image:hover {
  transform: scale(1.02);
}

.message-voice {
  margin-bottom: 8px;
}

.voice-player {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f0f0f0;
  border-radius: 20px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.voice-player:hover {
  background: #e0e0e0;
}

.voice-icon {
  font-size: 16px;
  color: #666;
}

.voice-duration {
  font-size: 12px;
  color: #666;
}

.message-item {
  margin-bottom: 16px;
  display: flex;
}

.message-item.sent {
  justify-content: flex-end;
}

.message-item.received {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 10px 12px;
  border-radius: 8px;
  position: relative;
}

.message-item.sent .message-content {
  background-color: #40ff6a9e;
  color: white;
}

.message-item.received .message-content {
  background-color: #f0f0f0;
  color: #333;
}

.message-text {
  word-wrap: break-word;
  line-height: 1.4;
}

.message-time {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.8;
  text-align: right;
}

.message-item.sent .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.message-item.received .message-time {
  color: rgba(0, 0, 0, 0.6);
}

.active-user {
background-color: #e6f4ff1c;
    //border-radius: 17px;
        padding: 9px 11px;
    margin: 3px;
}

/* 在线状态指示点样式 */
.status-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.status-indicator.online {
  background-color: #4CAF50; /* 绿色表示在线 */
  box-shadow: 0 0 4px rgba(76, 175, 80, 0.5);
}

.status-indicator.offline {
  background-color: #9E9E9E; /* 灰色表示离线 */
}
</style>
