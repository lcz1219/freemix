<template>
  <div class="ai-chat-container">
    <!-- 顶部标题栏 -->
    <div class="chat-header">
      <div class="header-title">AI助手</div>
      <div class="header-actions">
        <van-icon name="clock-o" size="24" color="var(--text-primary)" @click="showHistory = true" />
      </div>
    </div>

    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="chatContainerRef">
      <!-- 欢迎消息 -->
      <div v-if="chatMessages.length === 0" class="welcome-message">
        <van-icon name="chat-o" size="40" color="#00c9a7" />
        <h3>欢迎使用AI助手</h3>
        <p>有什么可以帮助您的吗？</p>
      </div>
      
      <!-- 消息列表 -->
      <div 
        v-for="(message, index) in chatMessages" 
        :key="index"
        :class="['message-item', message.type]"
      >
        <!-- 用户消息 -->
        <div v-if="message.type === 'user'" class="user-message">
          <div class="message-content">
            {{ message.content }}
          </div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
        
        <!-- AI消息 -->
        <div v-else-if="message.type === 'ai'" class="ai-message">
          <div class="message-avatar">
            <van-icon name="robot" size="24" color="#00c9a7" />
          </div>
          <div class="message-main">
            <div v-if="message.isProcessing && !message.content && !message.thinkingContent" class="processing-message">
              <van-loading type="spinner" color="#00c9a7" size="16" vertical />
              <span>AI正在思考中...</span>
            </div>
            <div v-else>
              <div v-if="message.thinkingContent" class="thinking-content">
                <div class="thinking-label">AI思考过程</div>
                <div class="thinking-body" v-html="formatContent(message.thinkingContent)"></div>
              </div>
              
              <div class="message-content" v-html="formatContent(message.content)"></div>
              
              <div v-if="message.isProcessing" class="generating-indicator">
                <van-loading type="spinner" color="#00c9a7" size="16" />
              </div>
              
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              
              <!-- 推荐问题 -->
              <div v-if="message.followUpQuestions && message.followUpQuestions.length > 0" class="follow-up-questions">
                <div class="follow-up-title">您可能还想问：</div>
                <div class="follow-up-list">
                  <div 
                    v-for="(question, qIndex) in message.followUpQuestions" 
                    :key="qIndex"
                    @click="sendFollowUpQuestion(question)"
                    class="follow-up-item"
                  >
                    <div class="follow-up-content">{{ question }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="input-area" :safe-area-inset-bottom="true">
      <van-field
        v-model="userInput"
        type="textarea"
        placeholder="请输入您的问题..."
        :autosize="{ minRows: 2, maxRows: 5 }"
        @keydown.enter="handleEnterKey"
        class="chat-input"
        :disabled="isSending"
      />
      <van-button 
        type="primary" 
        @click="sendMessage" 
        :disabled="!userInput.trim() || isSending"
        :loading="isSending"
        class="send-button"
        round
      >
        发送
      </van-button>
    </div>
    <!-- 历史记录侧边栏 -->
    <van-popup
      v-model:show="showHistory"
      position="right"
      :style="{ width: '70%', height: '100%' }"
    >
      <div class="history-sidebar">
        <div class="sidebar-header">
          <h3>历史记录</h3>
          <van-icon name="cross" size="20" @click="showHistory = false" />
        </div>
        
        <div v-if="historyList.length === 0" class="empty-history">
          <van-icon name="todo-list-o" size="48" color="#ccc" />
          <span>暂无历史记录</span>
        </div>
        
        <div v-else class="history-list">
          <div 
            v-for="(item, index) in historyList" 
            :key="item.id" 
            class="history-item"
            @click="handleHistoryClick(item.messageIndex)"
          >
            <div class="history-content">{{ item.title }}</div>
            <div class="history-time">{{ formatHistoryTime(item.timestamp) }}</div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { showToast } from 'vant'
import MarkdownIt from 'markdown-it'
import { postM, getM } from '@/utils/request.js'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true
})

// Props and Emits
const emit = defineEmits(['goal-created'])

// State
const userInput = ref('')
const isSending = ref(false)
const chatMessages = ref([])
const chatContainerRef = ref(null)
const showHistory = ref(false)

// Store
const store = useStore()
const currentUser = computed(() => {
  return store.state.user
})


// 历史记录列表
const historyList = computed(() => {
  const history = []
  let messageId = 0
  
  chatMessages.value.forEach((message, index) => {
    if (message.type === 'user') {
      history.push({
        id: messageId++,
        title: message.content.length > 20 
          ? message.content.substring(0, 20) + '...' 
          : message.content,
        timestamp: message.timestamp,
        messageIndex: index
      })
    }
  })
  
  return history.reverse() // 显示最新的在最上面
})

// 格式化历史记录时间
const formatHistoryTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diffTime = Math.abs(now.getTime() - date.getTime())
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === 1) {
    return '昨天'
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

// 点击历史记录
const handleHistoryClick = (index) => {
  showHistory.value = false
  scrollToMessage(index)
}

// 滚动到指定消息
const scrollToMessage = (index) => {
  nextTick(() => {
    if (chatContainerRef.value) {
      const messageElements = chatContainerRef.value.querySelectorAll('.message-item')
      if (index >= 0 && index < messageElements.length) {
        const targetElement = messageElements[index]
        targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
        
        // 添加高亮效果
        targetElement.classList.add('highlight')
        setTimeout(() => {
          targetElement.classList.remove('highlight')
        }, 2000)
      }
    }
  })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 格式化内容（使用markdown渲染）
const formatContent = (content) => {
  if (!content) return ''
  return md.render(content)
}

// 处理回车键
const handleEnterKey = (e) => {
  if (e.ctrlKey || e.metaKey) {
    // 按住Ctrl/Cmd+Enter换行
    return
  }
  e.preventDefault()
  sendMessage()
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isSending.value) return
  
  // 添加用户消息
  const userMessage = {
    type: 'user',
    content: userInput.value,
    timestamp: new Date()
  }
  chatMessages.value.push(userMessage)
  
  // 清空输入框
  const userQuestion = userInput.value
  userInput.value = ''
  
  // 设置发送状态
  isSending.value = true
  
  // 添加AI处理消息
  const processingMessage = {
    type: 'ai',
    messageType: 'processing',
    content: '',
    isProcessing: true,
    timestamp: new Date()
  }
  const processingMessageIndex = chatMessages.value.length
  chatMessages.value.push(processingMessage)
  
  // 滚动到底部
  scrollToBottom()
  
  try {
    // 调用AI API
    const aiResponse = await callCustomAIAPI(userQuestion, (messageData) => {
      // 实时更新AI消息
      if (chatMessages.value[processingMessageIndex]) {
        const message = chatMessages.value[processingMessageIndex]
        message.messageType = messageData.messageType
        message.content = messageData.content || message.content
        message.thinkingContent = messageData.thinkingContent
        message.followUpQuestions = messageData.followUpQuestions
        message.isProcessing = messageData.isProcessing
      }
      scrollToBottom()
    })
    
    // 更新最终AI消息
    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex]
      message.messageType = aiResponse.messageType
      message.content = aiResponse.content
      message.thinkingContent = aiResponse.thinkingContent
      message.followUpQuestions = aiResponse.followUpQuestions
      message.isProcessing = false
    }
    
    // 保存消息到服务器（可选）
    // await saveAIMessageToServer(userQuestion, aiResponse)
    
  } catch (error) {
    console.error('AI API调用失败:', error)
    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex]
      message.content = '抱歉，AI助手暂时无法回应，请稍后再试。'
      message.isProcessing = false
    }
    showToast({
      type: 'error',
      message: 'AI助手暂时无法回应，请稍后再试。'
    })
  } finally {
    isSending.value = false
    scrollToBottom()
  }
}

// 发送推荐问题
const sendFollowUpQuestion = (question) => {
  userInput.value = question
  sendMessage()
}

// 调用自定义AI API
const callCustomAIAPI = async (question, onUpdate) => {
  // 使用Coze平台的官方API端点
  const API_ENDPOINT = 'https://api.coze.cn/open_api/v1/chat'
  
  // 注意：这里使用了硬编码的API密钥和Bot ID，实际项目中应该从配置中获取
  const PERSONAL_ACCESS_TOKEN = 'sat_alIbwyaIhODXfXtTHCuj74C3swKTZd08L82jZDfMsfzplbENrkX5bu3ddTU5VHdn'
  const BOT_ID = '7569182284998524934'
  
  try {
    const response = await fetch(API_ENDPOINT, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${PERSONAL_ACCESS_TOKEN}`,
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({
        bot_id: BOT_ID,
        user: 'ea16730874-single_user',
        query: `${question}用markdown的格式返回`,
        stream: true
      })
    })

    if (!response.ok) {
      throw new Error(`API请求失败: ${response.status} ${response.statusText}`)
    }

    // 处理流式响应
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let done = false
    let fullResponse = ''
    let thinkingContent = ''
    let followUpQuestions = []
    let buffer = ''
    
    while (!done) {
      const { value, done: readerDone } = await reader.read()
      done = readerDone
      
      if (value) {
        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk
        
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''
        
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            
            if (data === '[DONE]') {
              done = true
              break
            }
            
            try {
              const jsonData = JSON.parse(data)
              
              if (jsonData.message) {
                switch (jsonData.message.type) {
                  case 'answer':
                    if (jsonData.message.content) {
                      fullResponse += jsonData.message.content
                    }
                    if (onUpdate) {
                      onUpdate({
                        messageType: 'answer',
                        content: fullResponse,
                        thinkingContent: thinkingContent,
                        isProcessing: true
                      })
                    }
                    break
                    
                  case 'follow_up':
                    if (jsonData.message.content) {
                      try {
                        const followUpData = JSON.parse(jsonData.message.content)
                        if (Array.isArray(followUpData)) {
                          followUpQuestions = followUpData
                        }
                      } catch (e) {
                        followUpQuestions.push(jsonData.message.content)
                      }
                      if (onUpdate) {
                        onUpdate({
                          messageType: 'follow_up',
                          followUpQuestions: followUpQuestions,
                          isProcessing: true
                        })
                      }
                    }
                    break
                    
                  case 'verbose':
                    if (jsonData.message.reasoning_content) {
                      thinkingContent += jsonData.message.reasoning_content + '\n'
                    } else if (jsonData.message.content) {
                      try {
                        const verboseData = JSON.parse(jsonData.message.content)
                        if (verboseData.msg_type === 'reasoning') {
                          thinkingContent += verboseData.data + '\n'
                        }
                      } catch (e) {
                        thinkingContent += jsonData.message.content + '\n'
                      }
                      if (onUpdate) {
                        onUpdate({
                          messageType: 'verbose',
                          content: fullResponse,
                          thinkingContent: thinkingContent,
                          isProcessing: true
                        })
                      }
                    }
                    break
                }
              }
            } catch (parseError) {
              console.warn('无法解析的响应数据:', data)
            }
          }
        }
      }
    }
    
    // 构建最终响应
    const result = {
      messageType: 'answer',
      content: fullResponse,
      thinkingContent: thinkingContent,
      followUpQuestions: followUpQuestions
    }
    
    if (!fullResponse.trim() && followUpQuestions.length === 0 && !thinkingContent.trim()) {
      result.content = 'AI助手已处理您的问题，但未返回有效回复。'
    }
    
    return result
  } catch (error) {
    console.error('AI API调用失败:', error)
    throw new Error(`AI调用失败: ${error.message}`)
  }
}

// 从服务器获取用户历史AI记录
const loadAIMessagesFromServer = async () => {
  try {
    const response = await getM(`ai-messages/${currentUser.value.username}/history`)
    if (response && response.data) {
      const historyMessages = []
      
      // 遍历历史记录，构建消息列表
      response.data.data.forEach(item => {
        // 添加用户消息
        historyMessages.push({
          type: 'user',
          content: item.userQuestion,
          timestamp: new Date(item.createdAt)
        })
        
        // 添加AI回复
        if (item.aiAnswer) {
          historyMessages.push({
            messageType: 'answer',
            type: 'ai',
            content: item.aiAnswer,
            thinkingContent: item.thinkingContent,
            followUpQuestions: item.followUpQuestions,
            timestamp: new Date(item.createdAt)
          })
        }
      })
      
      // 按时间排序
      historyMessages.sort((a, b) => a.timestamp - b.timestamp)
      return historyMessages
    }
    return []
  } catch (error) {
    console.error('从服务器加载AI历史记录失败:', error)
    return []
  }
}

// 初始化欢迎消息
onMounted(async () => {
  // 加载历史记录
  const historyMessages = await loadAIMessagesFromServer()
  if (historyMessages.length > 0) {
    chatMessages.value = historyMessages
  } else {
    chatMessages.value.push({
      type: 'ai',
      content: '您好！我是您的Freemix AI助手，请问有什么我可以帮助您的吗？',
      timestamp: new Date()
    })
  }
  
  scrollToBottom()
})

// 暴露方法供父组件调用
defineExpose({
  callCustomAIAPI
})
</script>

<style scoped lang="scss">
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 90px); // Adjust for potential bottom nav
  background-color: var(--bg-primary);
  position: relative;
  
  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 16px;
    background-color: var(--bg-primary);
    border-bottom: 1px solid var(--border-line);
    z-index: 10;
    
    .header-title {
      font-size: 18px;
      font-weight: bold;
      color: var(--text-primary);
    }
    
    .header-actions {
      display: flex;
      align-items: center;
    }
  }
  
  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background-color: var(--bg-primary);
    
    .welcome-message {
      text-align: center;
      padding: 40px 20px;
      color: var(--text-secondary);
      
      h3 {
        margin: 16px 0 8px;
        color: var(--text-primary);
        font-size: 18px;
      }
      
      p {
        margin: 0;
        font-size: 14px;
      }
    }
    
    .message-item {
      margin-bottom: 16px;
      transition: all 0.3s ease;
      
      &.highlight {
        transform: scale(1.02);
        position: relative;
        z-index: 1;
        
        &::after {
          content: '';
          position: absolute;
          top: -4px;
          left: -4px;
          right: -4px;
          bottom: -4px;
          border: 2px solid #00c9a7;
          border-radius: 12px;
          pointer-events: none;
          animation: pulse 2s infinite;
        }
      }
      
      &.user {
        display: flex;
        justify-content: flex-end;
        
        .user-message {
          max-width: 75%;
          background-color: #00c9a7;
          color: white;
          border-radius: 16px 16px 4px 16px;
          padding: 12px 16px;
          box-shadow: var(--card-shadow);
          
          .message-content {
            margin-bottom: 4px;
            line-height: 1.5;
            font-size: 15px;
          }
          
          .message-time {
            font-size: 11px;
            opacity: 0.8;
            text-align: right;
          }
        }
      }
      
      &.ai {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        
        .message-avatar {
          margin-top: 4px;
          flex-shrink: 0;
        }
        
        .message-main {
          max-width: 75%;
          
          .processing-message {
            display: flex;
            align-items: center;
            gap: 8px;
            color: var(--text-secondary);
            font-size: 14px;
            padding: 12px 16px;
            background-color: var(--bg-secondary);
            border-radius: 16px 16px 16px 4px;
            box-shadow: var(--card-shadow);
          }
          
          :deep(.thinking-content) {
            background-color: rgba(0, 0, 0, 0.03);
            border-radius: 8px;
            padding: 12px;
            margin-bottom: 12px;
            font-size: 13px;
            color: var(--text-secondary);
            border-left: 3px solid #00c9a7;
            
            .thinking-label {
              font-weight: bold;
              margin-bottom: 4px;
              color: #00c9a7;
            }
            
            .thinking-body {
              white-space: pre-wrap;
              line-height: 1.4;
              
              p { margin: 4px 0; }
            }
          }

          .message-content {
            background-color: var(--bg-secondary);
            border-radius: 16px 16px 16px 4px;
            padding: 12px 16px;
            box-shadow: var(--card-shadow);
            line-height: 1.5;
            font-size: 15px;
            color: var(--text-primary);
            margin-bottom: 4px;
            width: 92vw;
            overflow-wrap: break-word;
            
            :deep(p) { margin: 8px 0; &:first-child { margin-top: 0; } &:last-child { margin-bottom: 0; } }
            :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) { margin: 12px 0 8px; font-weight: bold; color: var(--text-primary); }
            :deep(h1) { font-size: 1.4em; }
            :deep(h2) { font-size: 1.3em; }
            :deep(h3) { font-size: 1.2em; }
            :deep(ul), :deep(ol) { padding-left: 20px; margin: 8px 0; }
            :deep(li) { margin: 4px 0; }
            :deep(code) {
              background-color: rgba(0, 0, 0, 0.05);
              padding: 2px 4px;
              border-radius: 4px;
              font-family: monospace;
              font-size: 0.9em;
            }
            :deep(pre) {
              background-color: #282c34;
              color: #abb2bf;
              padding: 12px;
              border-radius: 8px;
              overflow-x: auto;
              margin: 8px 0;
              
              code {
                background-color: transparent;
                padding: 0;
                color: inherit;
                font-size: 13px;
              }
            }
            :deep(blockquote) {
              border-left: 4px solid #dfe2e5;
              color: #6a737d;
              padding-left: 12px;
              margin: 8px 0;
              font-style: italic;
            }
            :deep(a) {
              color: #00c9a7;
              text-decoration: none;
            }
            :deep(table) {
              border-collapse: collapse;
              width: 100%;
              margin: 8px 0;
              font-size: 14px;
              
              th, td {
                border: 1px solid var(--border-line);
                padding: 6px 10px;
              }
              th {
                background-color: rgba(0, 0, 0, 0.02);
                font-weight: bold;
              }
            }
          }
          
          .generating-indicator {
            padding: 4px 0 8px;
            display: flex;
            justify-content: center;
          }
          
          .message-time {
            font-size: 11px;
            color: var(--text-secondary);
            margin-left: 4px;
          }
          
          .follow-up-questions {
            margin-top: 12px;
            
            .follow-up-title {
              font-size: 13px;
              color: var(--text-secondary);
              margin-bottom: 8px;
            }
            
            .follow-up-list {
              display: flex;
              flex-direction: column;
              gap: 8px;
              
              .follow-up-item {
                background-color: var(--bg-secondary);
                border-radius: 8px;
                padding: 12px;
                box-shadow: var(--card-shadow);
                font-size: 14px;
                color: var(--text-primary);
                cursor: pointer;
                transition: all 0.2s ease;
                
                &:active {
                  transform: scale(0.98);
                }
              }
            }
          }
        }
      }
    }
  }
  
  .input-area {
    display: flex;
    align-items: flex-end;
    gap: 12px;
    padding: 16px;
    background-color: var(--bg-primary);
    border-top: 1px solid var(--border-line);
    
    .chat-input {
      flex: 1;
      
      :deep(.van-field__control) {
        min-height: 44px;
        font-size: 15px;
      }
      
      :deep(.van-field__textarea-wrap) {
        border-radius: 22px;
      }
      
      :deep(.van-cell) {
        background-color: var(--bg-secondary);
        border-radius: 22px;
      }
    }
    
    .send-button {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #00c9a7;
      border: none;
      
      &:disabled {
        background-color: #ccc;
      }
    }
  }
  @keyframes pulse {
    0% { box-shadow: 0 0 0 0 rgba(0, 201, 167, 0.4); }
    70% { box-shadow: 0 0 0 10px rgba(0, 201, 167, 0); }
    100% { box-shadow: 0 0 0 0 rgba(0, 201, 167, 0); }
  }

  .history-sidebar {
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: var(--bg-primary);
    
    .sidebar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid var(--border-line);
      
      h3 {
        margin: 0;
        font-size: 18px;
        color: var(--text-primary);
      }
    }
    
    .empty-history {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: var(--text-secondary);
      gap: 12px;
      
      span {
        font-size: 14px;
      }
    }
    
    .history-list {
      flex: 1;
      overflow-y: auto;
      padding: 12px;
      
      .history-item {
        padding: 12px;
        border-radius: 8px;
        background-color: var(--bg-secondary);
        margin-bottom: 10px;
        cursor: pointer;
        transition: all 0.2s ease;
        box-shadow: var(--card-shadow);
        
        &:active {
          transform: scale(0.98);
          background-color: var(--bg-tertiary, #f0f0f0);
        }
        
        .history-content {
          font-size: 14px;
          color: var(--text-primary);
          margin-bottom: 6px;
          line-height: 1.4;
          font-weight: 500;
        }
        
        .history-time {
          font-size: 12px;
          color: var(--text-secondary);
          text-align: right;
        }
      }
    }
  }
}
</style>