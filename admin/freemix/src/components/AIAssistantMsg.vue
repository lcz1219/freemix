<template>
  <div class="ai-assistant-window" :class="isDark ? 'dark' : 'light'">
    <!-- <div class="window-header"> -->
      <!-- <h2>AI助手</h2> -->
      <!-- <n-button @click="closeWindow" class="close-button" quaternary circle>
        <n-icon>
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </n-icon>
      </n-button> -->
    <!-- </div> -->
    
    <!-- AI目标确认对话框
    <AIGoalConfirmation 
      v-model:show="showGoalConfirmation"
      :ai-response="lastAIResponse"
      :user-question="lastUserQuestion"
      @goal-created="handleGoalCreated"
    /> -->
    
    <div class="chat-layout">
      <!-- 历史记录侧边栏 -->
      <!-- <HistorySidebar 
        :chat-messages="chatMessages"
        @scroll-to-history="handleScrollToHistory"
      /> -->
      
      <!-- 聊天容器 -->
      <div class="chat-main">
        <AIChatContainer 
        ref="chatContainerRef"
          :chat-messages="chatMessages" 
          :format-time="formatTime" 
          @send-follow-up-question="sendFollowUpQuestion" 
        />
        
        <div class="input-container">
          <n-input 
            v-model:value="userInput" 
            type="textarea" 
            placeholder="请输入您的问题..." 
            :autosize="{ minRows: 3, maxRows: 6 }"
            @keyup.enter="sendMessage"
          />
          <!-- <n-button 
            type="primary" 
            @click="sendMessage" 
            :disabled="isSending"
            class="send-button"
          >
            {{ isSending ? '发送中...' : '发送' }}
          </n-button> -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, nextTick, onMounted, computed, watch, defineProps, defineEmits } from 'vue';
import { NButton, NIcon, NInput, NSpin } from 'naive-ui';
import AIChatContainer from './AIChatContainer.vue';
import HistorySidebar from './HistorySidebar.vue';
import { useStore } from 'vuex';
import { postM, getM, isSuccess } from '@/utils/request.js';

// 响应式数据
const props = defineProps({
  initialMessages: {
    type: Array,
    default: () => []
  },
  currentSessionId: {
    type: String,
    default: null
  },
  isSessionLoading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update-messages']);

const isDark = inject('isDark', ref(true));
const userInput = ref('');
const isSending = ref(false);
const chatMessages = ref(props.initialMessages && props.initialMessages.length > 0 ? [...props.initialMessages] : []);
const chatContainerRef = ref(null);

// 监听 props 变化，切换会话时更新消息列表
watch(() => props.initialMessages, (newMsgs) => {
  // 只有当本地消息为空，或者不是正在发送状态时，才接受父组件的更新
  // 防止 sendMessage 过程中被旧的历史记录覆盖
  if (!isSending.value) {
    chatMessages.value = newMsgs && newMsgs.length > 0 ? [...newMsgs] : [];
    nextTick(() => {
      scrollToBottom();
    });
  }
}, { deep: true });

// 消息更新同步
const notifyUpdate = () => {
  emit('update-messages', [...chatMessages.value]);
};

// 获取用户信息
const store = useStore();

// 监听全局 AI 输入内容
watch( () => store.state.aiInputContent,  (newContent) => {
  if (newContent && newContent.trim() && props.currentSessionId) {
    // 如果正在加载会话，等待加载完成后再发送
    const unwatchLoading = watch(() => props.isSessionLoading, (isLoading) => {
      if (!isLoading) {
        nextTick(() => {
          userInput.value = newContent;
          sendMessage();
          store.commit('setAiInputContent', '');
          unwatchLoading(); // 停止监听
        });
      }
    }, { immediate: true });
  }
}, { immediate: true });

const currentUser = computed(() => {
  return store.state.user;
});

// AI对话历史管理
const STORAGE_KEY = 'ai_chat_history';

// 保存聊天记录到本地存储
const saveChatHistory = () => {
  try {
    const historyData = {
      messages: chatMessages.value,
      lastUpdated: new Date().toISOString()
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(historyData));
  } catch (error) {
    console.error('保存聊天记录失败:', error);
  }
};

// 从本地存储加载聊天记录
const loadChatHistory = () => {
  try {
    const savedData = localStorage.getItem(STORAGE_KEY);
    if (savedData) {
      const historyData = JSON.parse(savedData);
      if (historyData.messages && Array.isArray(historyData.messages)) {
        // 转换时间戳字符串回Date对象
        chatMessages.value = historyData.messages.map(msg => ({
          ...msg,
          timestamp: new Date(msg.timestamp)
        }));
      }
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error);
  }
};

// 清除聊天记录
const clearChatHistory = () => {
  try {
    localStorage.removeItem(STORAGE_KEY);
    chatMessages.value = [];
  } catch (error) {
    console.error('清除聊天记录失败:', error);
  }
};

// 保存AI消息到服务器
const saveAIMessageToServer = async (userQuestion, aiAnswer, thinkingContent,followUpQuestions,messageType ) => {
  try {
    const messageData = {
      sessionId: props.currentSessionId, // 关联当前会话 ID
      username: currentUser.value.username,
      userQuestion: userQuestion.userQuestion,
      aiAnswer: userQuestion.aiAnswer,
      thinkingContent: thinkingContent,
      followUpQuestions:JSON.parse(userQuestion.followUpQuestions),
      messageType:userQuestion.messageType,
      createdAt: Date.now()
    };
    
    const response = await postM('ai-messages/save', messageData);
    console.log('AI消息保存成功:', response);
    return response;
  } catch (error) {
    console.error('保存AI消息到服务器失败:', error);
    return null;
  }
};

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffTime = Math.abs(now - date);
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  } else if (diffDays === 1) {
    return '昨天';
  } else if (diffDays < 7) {
    return `${diffDays}天前`;
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
  }
};

// 发送推荐问题
const sendFollowUpQuestion = (question) => {
  userInput.value = question;
  sendMessage();
};

// 发送消息到AI
const sendMessage = async () => {
  if (!userInput.value.trim() || isSending.value) return;
  
  // 添加用户消息到聊天记录
  const userMessage = {
    type: 'user',
    content: userInput.value,
    timestamp: new Date()
  };
  chatMessages.value.push(userMessage);
  notifyUpdate();
  
  // 清空输入框
  const userQuestion = userInput.value;
  userInput.value = '';
  
  // 设置发送状态
  isSending.value = true;
  
  // 添加一个初始的AI处理中消息
  const processingMessage = {
    type: 'ai',
    messageType: 'processing',
    content: '',
    isProcessing: true,
    timestamp: new Date()
  };
  const processingMessageIndex = chatMessages.value.length;
  chatMessages.value.push(processingMessage);
  notifyUpdate();
  
  // 滚动到底部
  scrollToBottom();
  
  try {
    // 调用自定义AIAPI
    const aiResponse = await callCustomAIAPI(userQuestion, (messageData) => {
      // 实时更新处理中的消息
      if (chatMessages.value[processingMessageIndex]) {
        const message = chatMessages.value[processingMessageIndex];
        message.messageType = messageData.messageType;
        message.content = messageData.content || message.content;
        message.thinkingContent = messageData.thinkingContent;
        message.followUpQuestions = messageData.followUpQuestions;
        message.isProcessing = messageData.isProcessing;
      }
      
      // 每次更新消息时都滚动到底部
      scrollToBottom();
    });
    
    // 更新最终的AI回复到聊天记录
    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex];
      message.messageType = aiResponse.messageType;
      message.content = aiResponse.content;
      message.thinkingContent = aiResponse.thinkingContent;
      message.followUpQuestions = aiResponse.followUpQuestions;
      message.isProcessing = false;
    }
    
    // 保存AI对话历史到后端
    try {
      await saveAIMessageToServer({
        userQuestion: userQuestion,
        aiAnswer: aiResponse.content,
        thinkingContent: aiResponse.thinkingContent,
        followUpQuestions: aiResponse.followUpQuestions ? JSON.stringify(aiResponse.followUpQuestions) : null,
        messageType: aiResponse.messageType
      });
    } catch (error) {
      console.error('保存AI对话历史失败:', error);
    }
    
    // 保存聊天记录到本地存储
    saveChatHistory();
    notifyUpdate();
    
    // 滚动到底部
    scrollToBottom();
  } catch (error) {
    // 更新错误消息到聊天记录
    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex];
      message.type = 'error';
      message.messageType = 'error';
      message.content = '抱歉，AI助手暂时无法回应，请稍后再试。';
      message.isProcessing = false;
    }
    console.error('AI API调用失败:', error);
    
    // 滚动到底部
    scrollToBottom();
  } finally {
    isSending.value = false;
  }
};

// 滚动聊天容器到底部的函数
const scrollToBottom = () => {
  // 使用nextTick确保DOM已更新
  nextTick(() => {
    // 方法1: 使用模板ref
    if (chatContainerRef.value) {
      console.log("滚动到底部",chatContainerRef.value);
      
      chatContainerRef.value.scrollToBottom()
      return;
    }
    
    
  })
};

import { handleMQLResponse } from '../utils/MQLHandler';
import { chatPrompt, mqlSummaryPrompt } from '@/utils/aiPrompts.js';
import { callCozeAPI } from '@/utils/aiService.js';

// 处理历史记录导航
const handleScrollToHistory = (historyIndex) => {
  // 获取历史记录中对应的用户消息索引
  const userMessages = chatMessages.value
    .map((message, index) => ({ message, index }))
    .filter(item => item.message.type === 'user');
  
  if (historyIndex >= 0 && historyIndex < userMessages.length) {
    const messageIndex = userMessages[historyIndex].index;
    
    // 调用AIChatContainer的scrollToMessage方法
    if (chatContainerRef.value && chatContainerRef.value.scrollToMessage) {
      chatContainerRef.value.scrollToMessage(messageIndex);
    }
  }
};

// 关闭窗口
const closeWindow = () => {
  if (window.electronAPI && window.electronAPI.closeWindow) {
    // 在Electron环境中关闭当前窗口
    window.electronAPI.closeWindow('ai-assistant');
  } else {
    // 在浏览器环境中关闭窗口
    window.close();
  }
};
const maskMQL = (text) => {
   // 1. 匹配完整的标签
  let processed = text.replace(/\[MQL_START\][\s\S]*?\[MQL_END\]/g, () => {
    return '[MQL_START]\n正在查询你的专属数据\n[MQL_END]';
  });
  
  // 2. 匹配已开始但未结束的标签（防止流式输出过程中闪现真实内容）
  if (processed.includes('[MQL_START]') && !processed.includes('[MQL_END]')) {
    processed = processed.substring(0, processed.indexOf('[MQL_START]')) + '[MQL_START]\n正在查询你的专属数据';
  }
  
  return processed;
};
// 调用自定义AI API
const callCustomAIAPI = async (question, onUpdate) => {
  const originalOnUpdate = onUpdate;
  onUpdate = (data) => {
    if (data && data.content) {
      data.content = maskMQL(data.content);
    }
    if (data && data.thinkingContent) {
      data.thinkingContent = maskMQL(data.thinkingContent);
    }
    if (originalOnUpdate) originalOnUpdate(data);
  };
  const custQuestion = chatPrompt({ question, username: currentUser.value.username })
  try {
    const apiResult = await callCozeAPI(custQuestion, onUpdate);
    const fullResponse = apiResult.content || '';
    const thinkingContent = apiResult.thinkingContent || '';
    const followUpQuestions = apiResult.followUpQuestions || [];
    console.log("fullResponse",fullResponse);
    
    // 构建最终响应对象
    const result = {
      messageType: 'answer',
      success: true,
      content: maskMQL(fullResponse), // 界面显示脱敏版, // 传给 UI 的是脱敏后的内容
      thinkingContent: thinkingContent,
      followUpQuestions: followUpQuestions
    };
    
    // 方案二：截获 MQL 并自动执行
    const MQL_START = '[MQL_START]';
    const startIndex = fullResponse.indexOf(MQL_START);
    if(startIndex != -1){
      
    
    const mqlResult = await handleMQLResponse(fullResponse,question);
    if (mqlResult && mqlResult.success) {
      // 触发二次对话：让 AI 总结结果
      const summaryPrompt = mqlSummaryPrompt({ question, rawData: mqlResult.rawData })
      
      // 递归调用 callCustomAIAPI 获取最终总结
      const finalResult = await callCustomAIAPI(summaryPrompt, onUpdate);
      return finalResult;
    }else{
      const failResult = {
        messageType: 'answer',
        success: false,
        content: "AI正在打瞌睡，请重新刷新",
        thinkingContent: thinkingContent,
        followUpQuestions: followUpQuestions
      }
      console.log("fail qlResult",failResult);

     return failResult;
    }
    }
    
    // 如果没有获取到有效响应，返回默认消息
    if (!fullResponse.trim() && followUpQuestions.length === 0 && !thinkingContent.trim()) {
      result.content = 'AI助手已处理您的问题，但未返回有效回复。';
    }
    
    return result;
  } catch (error) {
    console.error('AI API调用失败:', error);
    // 返回更友好的错误提示
    throw new Error(`抱歉，AI助手暂时无法回应，请稍后再试。
    
错误详情：${error.message}`);
  }
};

// 初始化欢迎消息
onMounted(() => {
  if (chatMessages.value.length === 0) {
    // 如果没有历史记录，显示欢迎消息
    chatMessages.value.push({
      type: 'ai',
      content: '您好！我是您的Freemix AI助手，请问有什么我可以帮助您的吗？',
      timestamp: new Date()
    });
  }
  // 初始滚动到底部
  nextTick(() => {
    scrollToBottom();
  });
});

// 暴露callCustomAIAPI方法供外部使用
defineExpose({
  callCustomAIAPI
});
</script>

<style scoped>
.ai-assistant-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-color);
  color: var(--text-color);
  overflow: hidden;
}

.ai-assistant-window.dark {
  background: linear-gradient(135deg, #1a1a1a, #121212);
  color: #ffffff;
}

.window-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2px 2px 2px 16px;

  border-bottom: 1px solid rgba(0, 201, 167, 0.3);
  background: #00c9a7;
}

.window-header h2 {
  margin: 0;
  font-size: 1.5em;
  background: white;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

.close-button {
  width: 32px;
  height: 32px;
}

.chat-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: var(--bg-color);
  margin: 0;
  border-radius: 0;
  backdrop-filter: none;
  border: none;
}

.message {
  margin-bottom: 16px;
  padding: 16px;
  border-radius: 12px;
  animation: fadeIn 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  background: linear-gradient(135deg, #e0f2f1, #b2dfdb);
  margin-left: 70%;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.message.ai {
  background: linear-gradient(135deg, #f5f5f5, #eeeeee);
  margin-right: 30%;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.message.error {
  background: linear-gradient(135deg, #ffebee, #ffcdd2);
  color: #c62828;
  border: 1px solid rgba(255, 0, 0, 0.2);
}

.message.processing {
  background: linear-gradient(135deg, #e0f2f1, #b2dfdb);
  border: 1px solid rgba(0, 201, 167, 0.3);
  text-align: center;
}

.message-content {
  margin-bottom: 8px;
  line-height: 1.6;
}

.message-time {
  font-size: 12px;
  color: #999;
  text-align: right;
  margin-top: 8px;
}

.thinking-content {
  background: linear-gradient(135deg, #fff8e1, #ffecb3);
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid rgba(255, 193, 7, 0.3);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.thinking-content strong {
  color: #ff9800;
  display: block;
  margin-bottom: 8px;
}

.thinking-process {
  background: linear-gradient(135deg, #eceff1, #cfd8dc);
  padding: 16px;
  border-radius: 8px;
  white-space: pre-wrap;
  border: 1px solid rgba(129, 198, 131, 0.2);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.thinking-process strong {
  color: #4CAF50;
  display: block;
  margin-bottom: 8px;
}

.processing-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
}

.processing-indicator span {
  font-weight: 500;
  color: #00c9a7;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  background: var(--card-bg);
  border-top: 1px solid var(--border-color);
  margin: 0;
  border-radius: 0;
}

.send-button {
  align-self: flex-end;
  width: 100px;
  background: #00c9a7;
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 201, 167, 0.3);
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 201, 167, 0.5);
}

.send-button:disabled {
  background: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.follow-up-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  padding: 16px;
  background: rgba(0, 201, 167, 0.05);
  border-radius: 8px;
  border: 1px dashed rgba(0, 201, 167, 0.3);
}

.follow-up-buttons strong {
  color: #00c9a7;
  margin-bottom: 8px;
  display: block;
}

.follow-up-button {
  text-align: left;
  justify-content: flex-start;
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  border: 1px solid rgba(0, 201, 167, 0.3);
  border-radius: 8px;
  padding: 12px 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.follow-up-button:hover {
  background: linear-gradient(135deg, #e0f2f1, #b2dfdb);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 201, 167, 0.3);
}

/* 暗色主题适配 */
.ai-assistant-window.dark .chat-container {
  background: #121212;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.ai-assistant-window.dark .message.user {
  background: #00c9a7;
  color: white;
  border: 1px solid rgba(0, 201, 167, 0.3);
}

.ai-assistant-window.dark .message.ai {
  background: linear-gradient(135deg, #424242, #2d2d2d);
  color: #e0e0e0;
  border: 1px solid rgba(0, 201, 167, 0.3);
}

.ai-assistant-window.dark .message.error {
  background: linear-gradient(135deg, #424242, #333333);
  color: #ef9a9a;
  border: 1px solid rgba(255, 0, 0, 0.3);
}

.ai-assistant-window.dark .message.processing {
  background: linear-gradient(135deg, #37474f, #263238);
  border: 1px solid rgba(0, 201, 167, 0.4);
}

.ai-assistant-window.dark .thinking-content {
  background: linear-gradient(135deg, #37474f, #263238);
  color: #e0e0e0;
  border: 1px solid rgba(255, 193, 7, 0.4);
}

.ai-assistant-window.dark .thinking-process {
  background: linear-gradient(135deg, #37474f, #263238);
  color: #e0e0e0;
  border: 1px solid rgba(0, 201, 167, 0.3);
}

.ai-assistant-window.dark .input-container {
  background: #121212;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.ai-assistant-window.dark .follow-up-buttons {
  background: #121212;
  border: 1px dashed rgba(0, 201, 167, 0.4);
}

.ai-assistant-window.dark .follow-up-button {
  background: linear-gradient(135deg, #424242, #333333);
  color: #e0e0e0;
  border: 1px solid rgba(0, 201, 167, 0.4);
}

.ai-assistant-window.dark .follow-up-button:hover {
  background: linear-gradient(135deg, #37474f, #263238);
  border: 1px solid rgba(0, 201, 167, 0.6);
}
</style>

































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































