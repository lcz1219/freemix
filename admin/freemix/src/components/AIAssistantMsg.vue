<template>
  <div class="ai-assistant-window" :class="isDark ? 'dark' : 'light'">
    <div class="window-header">
      <h2>AI助手</h2>
      <!-- <n-button @click="closeWindow" class="close-button" quaternary circle>
        <n-icon>
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </n-icon>
      </n-button> -->
    </div>
    
    <!-- AI目标确认对话框
    <AIGoalConfirmation 
      v-model:show="showGoalConfirmation"
      :ai-response="lastAIResponse"
      :user-question="lastUserQuestion"
      @goal-created="handleGoalCreated"
    /> -->
    
    <div class="chat-layout">
      <!-- 历史记录侧边栏 -->
      <HistorySidebar 
        :chat-messages="chatMessages"
        @scroll-to-history="handleScrollToHistory"
      />
      
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
import { ref, inject, nextTick, onMounted, computed } from 'vue';
import { NButton, NIcon, NInput, NSpin } from 'naive-ui';
import AIChatContainer from './AIChatContainer.vue';
import HistorySidebar from './HistorySidebar.vue';
import { useStore } from 'vuex';
import { postM, getM } from '@/utils/request.js';

// 响应式数据
const isDark = inject('isDark', ref(true));
const userInput = ref('');
const isSending = ref(false);
const chatMessages = ref([]);
const chatContainerRef = ref(null);

// 获取用户信息
const store = useStore();
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
    // 不抛出错误，避免影响用户体验
    return null;
  }
};

// 从服务器获取用户历史AI记录
const loadAIMessagesFromServer = async () => {
  try {
    const response = await getM(`ai-messages/${currentUser.value.username}/history`);
    if (response && response.data) {
      const historyMessages = response.data.data.map(item => ({
        type: 'user',
        content: item.userQuestion,
        timestamp: new Date(item.createdAt)
      }));
      
      // 添加AI回复
      response.data.data.forEach(item => {
        if (item.aiAnswer) {
          historyMessages.push({
            messageType: 'answer',
            type: 'ai',
            content: item.aiAnswer,
            thinkingContent: item.thinkingContent,
            followUpQuestions: item.followUpQuestions,
            timestamp: new Date(item.createdAt)
          });
        }
      });
      
      // 按时间排序
      historyMessages.sort((a, b) => a.timestamp - b.timestamp);
      console.log('历史消息:', historyMessages);
      
      return historyMessages;
    }
    return [];
  } catch (error) {
    console.error('从服务器加载AI历史记录失败:', error);
    return [];
  }
};

// 格式化时间
const formatTime = (timestamp) => {
  return timestamp.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
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

// 调用自定义AI API
const callCustomAIAPI = async (question, onUpdate) => {
  // 使用Coze平台的官方API端点
  const API_ENDPOINT = 'https://api.coze.cn/open_api/v1/chat';
  
  // 注意：您需要在Coze平台获取有效的API密钥
  const PERSONAL_ACCESS_TOKEN = 'sat_alIbwyaIhODXfXtTHCuj74C3swKTZd08L82jZDfMsfzplbENrkX5bu3ddTU5VHdn'; // 请替换为您的实际API密钥
  const BOT_ID = '7569182284998524934'; // 您的Bot ID
  
  try {
    // 使用标准的Bearer Token认证方式
    const response = await fetch(API_ENDPOINT, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${PERSONAL_ACCESS_TOKEN}`,
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({
        bot_id: BOT_ID,
        user: "ea16730874-single_user", // 用户标识
        query: `${question}用markdown的格式返回`,
        stream: true // 启用流式响应
      })
    });

    if (!response.ok) {
      // 获取详细的错误信息
      const errorText = await response.text();
      console.error('API请求失败详情:', errorText);
      
      // 如果是认证错误，提供更具体的错误信息
      if (response.status === 401 || response.status === 403) {
        throw new Error(`认证失败：${errorText}。请检查您的Personal Access Token是否正确且未过期。`);
      }
      
      throw new Error(`API请求失败: ${response.status} ${response.statusText} - ${errorText}`);
    }

    // 处理流式响应
    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let done = false;
    let fullResponse = '';
    let thinkingContent = '';
    let followUpQuestions = [];
    let buffer = ''; // 用于累积不完整的数据
    
    while (!done) {
      const { value, done: readerDone } = await reader.read();
      done = readerDone;
      
      if (value) {
        const chunk = decoder.decode(value, { stream: true });
        buffer += chunk; // 将新块添加到缓冲区
        
        // 按行分割缓冲区内容
        const lines = buffer.split('\n');
        buffer = lines.pop() || ''; // 保留最后一个可能不完整的行在缓冲区中
        
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim(); // 移除 "data:" 前缀
            
            if (data === '[DONE]') {
              // 流结束标记
              done = true;
              break;
            }
            
            try {
              const jsonData = JSON.parse(data);
              
              // 检查是否是消息类型的响应
              if (jsonData.message) {
                // 根据消息类型处理不同内容
                switch (jsonData.message.type) {
                  case 'answer':
                    // 处理AI的回答内容
                    if (jsonData.message.content) {
                      fullResponse += jsonData.message.content;
                    }
                    // 处理AI的思考过程
                    if (jsonData.message.reasoning_content) {
                      thinkingContent += jsonData.message.reasoning_content;
                    }
                    // 实时更新聊天界面
                    if (onUpdate) {
                      onUpdate({
                        messageType: 'answer',
                        content: fullResponse,
                        thinkingContent: thinkingContent,
                        isProcessing: true
                      });
                    }
                    break;
                    
                  case 'follow_up':
                    // 处理推荐问题
                    if (jsonData.message.content) {
                      try {
                        const followUpData = JSON.parse(jsonData.message.content);
                        if (Array.isArray(followUpData)) {
                          followUpQuestions = followUpData;
                        }
                      } catch (e) {
                        // 如果不是JSON格式，直接使用内容
                        followUpQuestions.push(jsonData.message.content);
                      }
                      // 实时更新聊天界面
                      if (onUpdate) {
                        onUpdate({
                          messageType: 'follow_up',
                          followUpQuestions: followUpQuestions,
                          isProcessing: true
                        });
                      }
                    }
                    break;
                    
                  case 'verbose':
                    // 处理AI思考过程
                    if (jsonData.message.reasoning_content) {
                      thinkingContent += jsonData.message.reasoning_content + '\n';
                    } else if (jsonData.message.content) {
                      try {
                        const verboseData = JSON.parse(jsonData.message.content);
                        if (verboseData.msg_type === 'reasoning') {
                          thinkingContent += verboseData.data + '\n';
                        }
                      } catch (e) {
                        thinkingContent += jsonData.message.content + '\n';
                      }
                      // 实时更新聊天界面
                      if (onUpdate) {
                        onUpdate({
                          messageType: 'verbose',
                          content: thinkingContent,
                          thinkingContent: thinkingContent,
                          isProcessing: true
                        });
                      }
                    }
                    break;
                }
              } else if (jsonData.event === 'done') {
                // 对话完成
                done = true;
                break;
              }
            } catch (parseError) {
              // 忽略无法解析的行
              console.warn('无法解析的响应数据:', data);
            }
          }
        }
      }
    }
    
    // 处理缓冲区中剩余的数据
    if (buffer.startsWith('data:')) {
      const data = buffer.slice(5).trim();
      if (data !== '[DONE]') {
        try {
          const jsonData = JSON.parse(data);
          if (jsonData.message) {
            switch (jsonData.message.type) {
              case 'answer':
                if (jsonData.message.content) {
                  fullResponse += jsonData.message.content;
                }
                break;
              case 'follow_up':
                if (jsonData.message.content) {
                  try {
                    const followUpData = JSON.parse(jsonData.message.content);
                    if (Array.isArray(followUpData)) {
                      followUpQuestions = followUpData;
                    }
                  } catch (e) {
                    followUpQuestions.push(jsonData.message.content);
                  }
                }
                break;
              case 'verbose':
                if (jsonData.message.reasoning_content) {
                  thinkingContent += jsonData.message.reasoning_content + '\n';
                } else if (jsonData.message.content) {
                  try {
                    const verboseData = JSON.parse(jsonData.message.content);
                    if (verboseData.msg_type === 'reasoning') {
                      thinkingContent += verboseData.data + '\n';
                    }
                  } catch (e) {
                    thinkingContent += jsonData.message.content + '\n';
                  }
                }
                break;
            }
          }
        } catch (parseError) {
          console.warn('无法解析的响应数据:', data);
        }
      }
    }
    
    // 构建最终响应对象
    const result = {
      messageType: 'answer',
      content: fullResponse,
      thinkingContent: thinkingContent,
      followUpQuestions: followUpQuestions
    };
    
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

// 初始化欢迎消息和加载历史记录
onMounted(async () => {
  try {
    // 尝试从服务器加载用户的AI历史记录
    const historyMessages = await loadAIMessagesFromServer();
    console.log('历史记录:', historyMessages);
    if (historyMessages.length > 0) {
      // 如果有历史记录，加载到聊天界面
      chatMessages.value = historyMessages;
      // 滚动到底部
      nextTick(() => {
        scrollToBottom();
      });
    } else {
      // 如果没有历史记录，显示欢迎消息
      chatMessages.value.push({
        type: 'ai',
        content: '您好！我是您的Freemix AI助手，请问有什么我可以帮助您的吗？',
        timestamp: new Date()
      });
    }
  } catch (error) {
    console.error('初始化AI助手失败:', error);
    // 如果加载历史记录失败，至少显示欢迎消息
    chatMessages.value.push({
      type: 'ai',
      content: '您好！我是您的Freemix AI助手，请问有什么我可以帮助您的吗？',
      timestamp: new Date()
    });
  }
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
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  color: #333333;
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

  border-bottom: 1px solid rgba(129, 198, 131, 0.3);
  background: linear-gradient(90deg, rgba(129, 198, 131, 0.1), transparent);
}

.window-header h2 {
  margin: 0;
  font-size: 1.5em;
  background: linear-gradient(90deg, #81c683, #4CAF50);
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
  padding: 16px;
  background: rgba(129, 198, 131, 0.05);
  margin: 11px 16px -4px 16px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(129, 198, 131, 0.1);
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
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  margin-left: 70%;
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.message.ai {
  background: linear-gradient(135deg, #f5f5f5, #eeeeee);
  margin-right: 30%;
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.message.error {
  background: linear-gradient(135deg, #ffebee, #ffcdd2);
  color: #c62828;
  border: 1px solid rgba(255, 0, 0, 0.2);
}

.message.processing {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  border: 1px solid rgba(129, 198, 131, 0.3);
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
  color: #4CAF50;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  margin: 16px 16px 0px 16px;
  background: rgba(129, 198, 131, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(129, 198, 131, 0.1);
}

.send-button {
  align-self: flex-end;
  width: 100px;
  background: linear-gradient(135deg, #81c683, #4CAF50);
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(129, 198, 131, 0.3);
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(129, 198, 131, 0.5);
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
  background: rgba(129, 198, 131, 0.05);
  border-radius: 8px;
  border: 1px dashed rgba(129, 198, 131, 0.3);
}

.follow-up-buttons strong {
  color: #4CAF50;
  margin-bottom: 8px;
  display: block;
}

.follow-up-button {
  text-align: left;
  justify-content: flex-start;
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  border: 1px solid rgba(129, 198, 131, 0.3);
  border-radius: 8px;
  padding: 12px 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.follow-up-button:hover {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(129, 198, 131, 0.3);
}

/* 暗色主题适配 */
.ai-assistant-window.dark .chat-container {
  background: rgba(42, 42, 42, 0.7);
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.ai-assistant-window.dark .message.user {
  background: linear-gradient(135deg, #1565c0, #0d47a1);
  color: white;
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.ai-assistant-window.dark .message.ai {
  background: linear-gradient(135deg, #424242, #2d2d2d);
  color: #e0e0e0;
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.ai-assistant-window.dark .message.error {
  background: linear-gradient(135deg, #424242, #333333);
  color: #ef9a9a;
  border: 1px solid rgba(255, 0, 0, 0.3);
}

.ai-assistant-window.dark .message.processing {
  background: linear-gradient(135deg, #37474f, #263238);
  border: 1px solid rgba(129, 198, 131, 0.4);
}

.ai-assistant-window.dark .thinking-content {
  background: linear-gradient(135deg, #37474f, #263238);
  color: #e0e0e0;
  border: 1px solid rgba(255, 193, 7, 0.4);
}

.ai-assistant-window.dark .thinking-process {
  background: linear-gradient(135deg, #37474f, #263238);
  color: #e0e0e0;
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.ai-assistant-window.dark .input-container {
  background: rgba(42, 42, 42, 0.7);
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.ai-assistant-window.dark .follow-up-buttons {
  background: rgba(42, 42, 42, 0.7);
  border: 1px dashed rgba(129, 198, 131, 0.4);
}

.ai-assistant-window.dark .follow-up-button {
  background: linear-gradient(135deg, #424242, #333333);
  color: #e0e0e0;
  border: 1px solid rgba(129, 198, 131, 0.4);
}

.ai-assistant-window.dark .follow-up-button:hover {
  background: linear-gradient(135deg, #37474f, #263238);
  border: 1px solid rgba(129, 198, 131, 0.6);
}
</style>


































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































