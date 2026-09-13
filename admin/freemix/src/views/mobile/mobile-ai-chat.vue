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
        <!-- 快捷提问胶囊 -->
        <div class="quick-questions">
          <div class="quick-chip" @click="sendQuickQuestion('帮我分析一下我的目标完成情况')">
            <van-icon name="chart-trending-o" size="14" />分析我的目标
          </div>
          <div class="quick-chip" @click="sendQuickQuestion('帮我制定一个今天的工作计划')">
            <van-icon name="todo-list-o" size="14" />制定今日计划
          </div>
          <div class="quick-chip" @click="sendQuickQuestion('给我一些提升效率的建议')">
            <van-icon name="bulb-o" size="14" />效率提升建议
          </div>
        </div>
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
              <span class="loading-text">Freemix AI思考中</span>
              <span class="loading-dots"><i></i><i></i><i></i></span>
            </div>
            <div v-else>
              <div class="message-content" v-html="formatContent(message.content)"></div>
              
              <div v-if="message.isProcessing" class="generating-indicator">
                <span class="pulse-ring"></span>
                <span class="loading-text">Freemix AI正在处理中...</span>
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
    <!-- 历史记录面板（内嵌滑动，不外泄到其他 tab） -->
    <transition name="history-slide">
      <div v-if="showHistory" class="history-overlay">
        <div class="history-backdrop" @click="showHistory = false"></div>
        <div class="history-panel">
          <div class="sidebar-header">
            <h3>历史记录</h3>
            <van-icon name="cross" size="22" color="var(--text-primary)" @click="showHistory = false" />
          </div>
          
          <div v-if="historyList.length === 0" class="empty-history">
            <van-icon name="todo-list-o" size="48" color="#999" />
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
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { showToast } from 'vant'
import MarkdownIt from 'markdown-it'
import { postM, getM } from '@/utils/request.js'
import { chatPromptMobile, chatPrompt } from '@/utils/aiPrompts.js'
import { callCozeAPI } from '@/utils/aiService.js'

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

// 快捷提问
const sendQuickQuestion = (question: string) => {
  userInput.value = question
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

// MQL 脱敏函数：流式输出时隐藏 [MQL_START]...[MQL_END] 之间的真实内容
const maskMQL = (text) => {
  let processed = text.replace(/\[MQL_START\][\s\S]*?\[MQL_END\]/g, () => {
    return '[MQL_START]\n正在查询你的专属数据\n[MQL_END]'
  })
  if (processed.includes('[MQL_START]') && !processed.includes('[MQL_END]')) {
    processed = processed.substring(0, processed.indexOf('[MQL_START]')) + '[MQL_START]\n正在查询你的专属数据'
  }
  return processed
}

// 调用自定义AI API（包含 MQL 截获与自动执行）
const callCustomAIAPI = async (question, onUpdate) => {
  // 包装 onUpdate 回调，对流式输出的 content 做 MQL 脱敏
  const originalOnUpdate = onUpdate
  onUpdate = (data) => {
    if (data && data.content) {
      data.content = maskMQL(data.content)
    }
    if (data && data.thinkingContent) {
      data.thinkingContent = maskMQL(data.thinkingContent)
    }
    if (originalOnUpdate) originalOnUpdate(data)
  }

  // 使用 chatPrompt（带 username 上下文，MQL 需要知道当前用户）
  const custQuestion = chatPrompt({ question, username: currentUser.value.username })

  try {
    const apiResult = await callCozeAPI(custQuestion, onUpdate)
    const fullResponse = apiResult.content || ''
    const thinkingContent = apiResult.thinkingContent || ''
    const followUpQuestions = apiResult.followUpQuestions || []

    // 构建最终响应对象（界面显示脱敏版）
    const result = {
      messageType: 'answer',
      success: true,
      content: maskMQL(fullResponse),
      thinkingContent: thinkingContent,
      followUpQuestions: followUpQuestions
    }

    // MQL 的截获、执行与二次总结已全部由后端编排完成，
    // 这里拿到的 content 就是最终总结，前端不再需要二次请求

    // 如果没有获取到有效响应，返回默认消息
    if (!fullResponse.trim() && followUpQuestions.length === 0 && !thinkingContent.trim()) {
      result.content = 'AI助手已处理您的问题，但未返回有效回复。'
    }

    return result
  } catch (error) {
    console.error('AI API调用失败:', error)
    throw new Error(`抱歉，AI助手暂时无法回应，请稍后再试。\n\n错误详情：${error.message}`)
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

// 暴露方法和状态供父组件调用
defineExpose({
  callCustomAIAPI
})
</script>

<style scoped lang="scss">
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 90vh; // Adjust for potential bottom nav
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
    overflow-x: hidden;
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
        margin: 0 0 20px;
        font-size: 14px;
      }

      /* 快捷提问胶囊 */
      .quick-questions {
        display: flex;
        flex-direction: column;
        gap: 10px;
        padding: 0 16px;
        margin-top: 20px;

        .quick-chip {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 12px 18px;
          background: rgba(0, 201, 167, 0.08);
          border: 1px solid rgba(0, 201, 167, 0.2);
          border-radius: 16px;
          color: var(--text-primary);
          font-size: 14px;
          cursor: pointer;
          transition: all 0.2s;

          &:active {
            background: rgba(0, 201, 167, 0.15);
            transform: scale(0.98);
          }
        }
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
          max-width: 100%;
          
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
          
          /* AI思考/处理中的动态加载效果（与PC端保持一致） */
          .loading-text {
            font-weight: 500;
            background: linear-gradient(90deg, #00c9a7 0%, #7ff5e0 50%, #00c9a7 100%);
            background-size: 200% 100%;
            -webkit-background-clip: text;
            background-clip: text;
            -webkit-text-fill-color: transparent;
            animation: textShimmer 1.8s linear infinite;
          }

          /* 流光高光从左到右扫过 */
          @keyframes textShimmer {
            0% { background-position: 200% 0; }
            100% { background-position: -200% 0; }
          }

          /* 三点跳动：依次上浮，形成波浪 */
          .loading-dots {
            display: inline-flex;
            align-items: flex-end;
            gap: 6px;
            height: 14px;
          }

          .loading-dots i {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #00c9a7;
            animation: dotBounce 1.2s ease-in-out infinite;
          }

          .loading-dots i:nth-child(2) { animation-delay: 0.15s; }
          .loading-dots i:nth-child(3) { animation-delay: 0.3s; }

          @keyframes dotBounce {
            0%, 80%, 100% { transform: translateY(0) scale(0.85); opacity: 0.5; }
            40% { transform: translateY(-6px) scale(1.15); opacity: 1; }
          }

          /* 脉冲光圈：中心实心点 + 向外扩散的波纹 */
          .pulse-ring {
            position: relative;
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background: #00c9a7;
            flex-shrink: 0;
          }

          .pulse-ring::before,
          .pulse-ring::after {
            content: '';
            position: absolute;
            inset: 0;
            border-radius: 50%;
            background: #00c9a7;
            animation: ringPulse 1.8s cubic-bezier(0.22, 0.61, 0.36, 1) infinite;
          }

          .pulse-ring::after {
            animation-delay: 0.9s;
          }

          @keyframes ringPulse {
            0% { transform: scale(1); opacity: 0.7; }
            100% { transform: scale(3.2); opacity: 0; }
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
            align-items: center;
            justify-content: center;
            gap: 8px;
            font-size: 14px;
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
    padding-bottom: calc(27px + env(safe-area-inset-bottom));
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

  /* 历史面板（内嵌在 ai-chat-container 内，不外泄到其他 tab） */
  .history-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 50;
    display: flex;
    flex-direction: row-reverse; /* 面板靠右 */
  }

  .history-backdrop {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.35);
    backdrop-filter: blur(2px);
    -webkit-backdrop-filter: blur(2px);
    z-index: 1;
  }

  .history-panel {
    position: relative;
    z-index: 2;
    width: 72%;
    height: 100%;
    background: var(--bg-primary);
    box-shadow: -4px 0 24px rgba(0, 0, 0, 0.25);
    display: flex;
    flex-direction: column;
    
    .sidebar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 18px 16px;
      border-bottom: 1px solid var(--border-line);
      flex-shrink: 0;
      
      h3 {
        margin: 0;
        font-size: 17px;
        font-weight: 700;
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
      padding: 12px 16px;
      
      .history-item {
        padding: 14px 16px;
        border-radius: 12px;
        background: rgba(125, 125, 125, 0.06);
        margin-bottom: 10px;
        cursor: pointer;
        transition: all 0.2s ease;
        
        &:active {
          transform: scale(0.97);
          background: rgba(0, 201, 167, 0.1);
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

/* 历史面板滑动过渡动画 */
.history-slide-enter-active {
  .history-backdrop { animation: fadeIn 0.25s ease both; }
  .history-panel   { animation: slideInRight 0.3s cubic-bezier(0.32, 0.72, 0, 1) both; }
}
.history-slide-leave-active {
  .history-backdrop { animation: fadeOut 0.2s ease both; }
  .history-panel   { animation: slideOutRight 0.25s ease both; }
}

@keyframes slideInRight  { from { transform: translateX(100%); } to { transform: translateX(0); } }
@keyframes slideOutRight { from { transform: translateX(0); } to { transform: translateX(100%); } }
@keyframes fadeIn  { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeOut { from { opacity: 1; } to { opacity: 0; } }
</style>
