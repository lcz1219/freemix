<template>
  <div ref="chatContainerRef" class="chat-container">
    <div v-for="(message, index) in chatMessages" :key="index" :class="['message', message.type]">
      <div class="message-content">
        <!-- 显示不同类型的消息内容 -->
        <div v-if="message.messageType === 'answer'">
          <!-- 显示AI的回答内容 -->
          <div v-if="message.thinkingContent" class="thinking-content">
            <strong>AI思考过程：</strong>
            {{ message.thinkingContent }}
          </div>
          <div class="answer-content">
            {{ message.content }}
          </div>
        </div>
        <div v-if="message.followUpQuestions">
          <!-- 显示推荐问题 -->
          <div><strong>推荐问题：</strong></div>
          <div class="follow-up-buttons">
            <n-button 
              v-for="(question, qIndex) in message.followUpQuestions" 
              :key="qIndex" 
              type="info" 
              size="small" 
              secondary
              @click="sendFollowUpQuestion(question)"
              class="follow-up-button"
            >
              {{ question }}
            </n-button>
          </div>
        </div>
        <div v-else-if="message.messageType === 'verbose'">
          <!-- 显示AI思考过程 -->
          <div><strong>AI思考中...</strong></div>
          <div class="thinking-process">{{ message.content }}</div>
        </div>
        <div v-else-if="message.messageType === 'processing' || message.isProcessing">
          <!-- 显示AI正在处理的提示 -->
          <div class="processing-indicator">
            <n-spin size="small" />
            <span>AI正在处理中...</span>
          </div>
        </div>
        <div v-else>
          <!-- 默认显示内容 -->
          {{ message.content }}
        </div>
      </div>
      <div class="message-time">{{ formatTime(message.timestamp) }}</div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, ref, onMounted, nextTick } from 'vue';
import { NButton, NSpin } from 'naive-ui';

// 定义props
const props = defineProps({
  chatMessages: {
    type: Array,
    required: true
  },
  formatTime: {
    type: Function,
    required: true
  },
  isShowThinking: {
    type: Boolean,
    default: true
  }
});

// 定义emits
const emit = defineEmits(['send-follow-up-question']);

// 发送推荐问题
const sendFollowUpQuestion = (question) => {
  emit('send-follow-up-question', question);
};
const chatContainerRef = ref(null);
const scrollToBottom = () => {
  console.log("滚动到底部111");
  // 使用nextTick确保DOM已更新
  nextTick(() => {
    // 方法1: 使用模板ref
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight;
      return;
    }
    
    // 方法2: 作为后备方案
    const containers = document.querySelectorAll('.chat-container');
    const visibleContainer = Array.from(containers).find(container => {
      return container.offsetParent !== null; // 检查是否可见
    });
    
    if (visibleContainer) {
      visibleContainer.scrollTop = visibleContainer.scrollHeight;
    }
  }).then(() => {
    // 额外延迟确保滚动执行
    setTimeout(() => {
      const container = chatContainerRef.value || document.querySelector('.chat-container:not([style*="display: none"])');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }, 50);
  });
};
defineExpose({
  scrollToBottom
});
</script>

<style scoped>
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: rgba(129, 198, 131, 0.05);
  margin: 16px;
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
  background: #56b359;
  margin-left: 70%;
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.message.ai {
  background: linear-gradient(135deg, #252525, #242424);
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
  /* background: linear-gradient(135deg, #ffffff, #f8f9fa); */
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
.chat-container.dark {
  background: rgba(42, 42, 42, 0.7);
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.chat-container.dark .message.user {
  background: linear-gradient(135deg, #1565c0, #0d47a1);
  color: white;
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.chat-container.dark .message.ai {
  background: linear-gradient(135deg, #424242, #2d2d2d);
  color: #e0e0e0;
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.chat-container.dark .message.error {
  background: linear-gradient(135deg, #424242, #333333);
  color: #ef9a9a;
  border: 1px solid rgba(255, 0, 0, 0.3);
}

.chat-container.dark .message.processing {
  background: linear-gradient(135deg, #37474f, #263238);
  border: 1px solid rgba(129, 198, 131, 0.4);
}

.chat-container.dark .thinking-content {
  background: linear-gradient(135deg, #37474f, #263238);
  color: #e0e0e0;
  border: 1px solid rgba(255, 193, 7, 0.4);
}

.chat-container.dark .thinking-process {
  background: linear-gradient(135deg, #37474f, #263238);
  color: #e0e0e0;
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.chat-container.dark .follow-up-buttons {
  background: rgba(42, 42, 42, 0.7);
  border: 1px dashed rgba(129, 198, 131, 0.4);
}

.chat-container.dark .follow-up-button {
  background: linear-gradient(135deg, #424242, #333333);
  color: #e0e0e0;
  border: 1px solid rgba(129, 198, 131, 0.4);
}

.chat-container.dark .follow-up-button:hover {
  background: linear-gradient(135deg, #37474f, #263238);
  border: 1px solid rgba(129, 198, 131, 0.6);
}
</style>