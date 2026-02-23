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
              <div class="message-text">{{ message.content }}</div>
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
          <div style="display: flex; gap: 10px; height: 100%;">
            <n-input 
              v-model:value="newMessage" 
              type="textarea" 
              placeholder="输入消息..." 
              :autosize="{ minRows: 2, maxRows: 4 }"
              @keydown.enter="handleSendMessage"
              style="flex: 1;"
            />
            <!-- <n-button 
              type="primary" 
              @click="sendMessage"
              :disabled="!newMessage.trim()"
              style="height: fit-content;"
            >
              发送
            </n-button> -->
          </div>
        </n-layout-footer>
        
        <n-layout-content v-else style="display: flex; justify-content: center; align-items: center; height: 100%;">
          <n-empty description="请选择一个用户开始聊天">
            <template #icon>
              <n-icon>
                <Chat />
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
import router from "@/router";
import {sendMessageWeb} from '@/utils/websocket.js'
import {genMsg} from '@/utils/genMsg.js'
// import defaultAvatar from '@/assets/images/default-avatar.png'

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
      message.success('消息发送成功')
      
      // 立即刷新消息列表，确保显示最新消息
      await fetchMessages()
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

// 处理回车发送消息
const handleSendMessage = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 滚动到底部
const messageContainer = ref()
const bottomscroll = ref(9999999)
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      // 每次滚动增加一个偏移量，避免滚动到相同位置
      bottomscroll.value=bottomscroll.value+1
      console.log("bottomscroll.value:",bottomscroll.value);
    messageContainer.value.scrollTo({ top: bottomscroll.value, behavior: 'smooth' })
    }
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
const handleReloadMessages = () => {
  if (selectedUser.value) {
    fetchMessages();
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
    
    if (!usersLoaded) {
      message.warning('用户列表加载失败，正在使用模拟数据')
    }
    
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
  fetchMessages
});
</script>

<style scoped>
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
