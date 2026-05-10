<template>
  <div ref="chatContainerRef" class="chat-container">
    <div v-for="(message, index) in chatMessages" :key="index" :class="['message', message.type]">
      <div class="message-content">
        <!-- 显示不同类型的消息内容 -->
        <div v-if="message.messageType === 'answer'">
          <!-- 显示AI的回答内容 -->
          <div v-if="message.thinkingContent" class="thinking-content">
            <strong>AI思考过程：</strong>
            <div v-html="parseMarkdown(message.thinkingContent)"></div>
          </div>
          <div class="answer-content" v-html="parseMarkdown(message.content)"></div>
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
            <div class="thinking-process" v-html="parseMarkdown(message.content)"></div>
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
            <div v-html="parseMarkdown(message.content)"></div>
          </div>
      </div>
      <div class="message-time">{{ formatTime(message.timestamp) }}</div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, ref, onMounted, nextTick, computed } from 'vue';
import { NButton, NSpin } from 'naive-ui';
import MarkdownIt from 'markdown-it';

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true
});

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

// 解析Markdown内容
const parseMarkdown = (content) => {
  if (!content) return '';
  return md.render(content);
};
const chatContainerRef = ref(null);
const scrollToBottom = () => {
  // console.log("滚动到底部111");
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

// 滚动到指定消息
const scrollToMessage = (messageIndex) => {
  nextTick(() => {
    if (chatContainerRef.value) {
      // 获取所有消息元素
      const messageElements = chatContainerRef.value.querySelectorAll('.message');
      
      if (messageIndex >= 0 && messageIndex < messageElements.length) {
        const targetElement = messageElements[messageIndex];
        
        // 滚动到目标元素
        targetElement.scrollIntoView({
          behavior: 'smooth',
          block: 'center'
        });
        
        // 添加高亮效果
        targetElement.classList.add('highlighted');
        
        // 3秒后移除高亮效果
        setTimeout(() => {
          targetElement.classList.remove('highlighted');
        }, 3000);
      }
    }
  });
};

defineExpose({
  scrollToBottom,
  scrollToMessage
});
</script>

<style scoped>
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: var(--bg-color);
  margin: 11px 16px -4px 16px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 201, 167, 0.3);
}

.message {
  margin-bottom: 16px;
  padding: 16px;
  border-radius: 12px;
  animation: fadeIn 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  background: #00c9a7;
  color: white;
  margin-left: 70%;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.message.ai {
  background: var(--card-bg);
  color: var(--text-color);
  margin-right: 20%;
  border: 1px solid var(--border-color);
}
.message.ai-gen {
  background: var(--card-bg);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  height: 64vh;
  overflow-y: auto;
}

.message.error {
  background: #fff5f5;
  color: #c53030;
  border: 1px solid rgba(245, 101, 101, 0.2);
}

.message.processing {
  background: var(--card-bg);
  color: var(--text-color);
  border: 1px solid rgba(0, 201, 167, 0.3);
  text-align: center;
}

.message.highlighted {
  box-shadow: 0 0 0 2px #00c9a7;
  transform: scale(1.02);
  transition: all 0.3s ease;
}

.message-content {
  margin-bottom: 8px;
  line-height: 1.6;
}

/* Markdown内容样式优化 */
.answer-content,
.thinking-process {
  line-height: 1.8;
  font-size: 14px;
  color: inherit;
}

.answer-content :deep(h1),
.answer-content :deep(h2),
.answer-content :deep(h3),
.answer-content :deep(h4),
.thinking-process :deep(h1),
.thinking-process :deep(h2),
.thinking-process :deep(h3),
.thinking-process :deep(h4) {
  margin: 1.5em 0 1em;
  font-weight: 600;
  line-height: 1.3;
}

.answer-content :deep(h1),
.thinking-process :deep(h1) {
  font-size: 1.8em;
  border-bottom: 2px solid rgba(0, 201, 167, 0.2);
  padding-bottom: 0.3em;
}

.answer-content :deep(h2),
.thinking-process :deep(h2) {
  font-size: 1.5em;
  border-bottom: 1px solid rgba(0, 201, 167, 0.1);
  padding-bottom: 0.2em;
}

.answer-content :deep(h3),
.thinking-process :deep(h3) {
  font-size: 1.25em;
  color: #00c9a7;
}

.answer-content :deep(p),
.thinking-process :deep(p) {
  margin: 1em 0;
}

.answer-content :deep(ul),
.answer-content :deep(ol),
.thinking-process :deep(ul),
.thinking-process :deep(ol) {
  margin: 1em 0;
  padding-left: 1.5em;
}

.answer-content :deep(li),
.thinking-process :deep(li) {
  margin: 0.5em 0;
}

.answer-content :deep(code),
.thinking-process :deep(code) {
  background-color: rgba(0, 201, 167, 0.1);
  color: #00c9a7;
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 0.9em;
}

.answer-content :deep(pre),
.thinking-process :deep(pre) {
  background-color: var(--card-bg);
  padding: 1em;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1em 0;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.answer-content :deep(pre code),
.thinking-process :deep(pre code) {
  background-color: transparent;
  color: var(--text-color);
  padding: 0;
  border-radius: 0;
  font-size: 0.9em;
}

.answer-content :deep(blockquote),
.thinking-process :deep(blockquote) {
  margin: 1em 0;
  padding: 0.5em 1em;
  color: #666;
  border-left: 4px solid #00c9a7;
  background: rgba(0, 201, 167, 0.05);
  border-radius: 0 4px 4px 0;
}

.answer-content :deep(table),
.thinking-process :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
  font-size: 0.9em;
}

.answer-content :deep(th),
.answer-content :deep(td),
.thinking-process :deep(th),
.thinking-process :deep(td) {
  border: 1px solid rgba(0, 201, 167, 0.2);
  padding: 0.6em;
  text-align: left;
}

.answer-content :deep(th),
.thinking-process :deep(th) {
  background: rgba(0, 201, 167, 0.1);
  font-weight: 600;
}

.message.user .answer-content :deep(code) {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
}

.message.ai .answer-content :deep(h1),
.message.ai .answer-content :deep(h2),
.message.ai .answer-content :deep(h3) {
  color: #00c9a7;
}

.dark .answer-content :deep(blockquote),
.dark .thinking-process :deep(blockquote) {
  color: #aaa;
  background: rgba(0, 201, 167, 0.1);
}

/* 暗色主题下的Markdown样式 */
.dark .answer-content :deep(h1),
.dark .answer-content :deep(h2),
.dark .answer-content :deep(h3),
.dark .answer-content :deep(h4),
.dark .answer-content :deep(h5),
.dark .answer-content :deep(h6),
.dark .thinking-process :deep(h1),
.dark .thinking-process :deep(h2),
.dark .thinking-process :deep(h3),
.dark .thinking-process :deep(h4),
.dark .thinking-process :deep(h5),
.dark .thinking-process :deep(h6) {
  color: #00c9a7;
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.dark .answer-content :deep(code),
.dark .thinking-process :deep(code) {
  background-color: rgba(0, 201, 167, 0.2);
}

.dark .answer-content :deep(pre),
.dark .thinking-process :deep(pre) {
  background-color: #161b22;
  border-color: rgba(255, 255, 255, 0.1);
}

.dark .answer-content :deep(pre code),
.dark .thinking-process :deep(pre code) {
  color: #e0e0e0;
}

.dark .answer-content :deep(blockquote),
.dark .thinking-process :deep(blockquote) {
  color: #8b949e;
  border-left-color: #00c9a7;
  background: rgba(0, 201, 167, 0.1);
}

.dark .answer-content :deep(th),
.dark .answer-content :deep(td),
.dark .thinking-process :deep(th),
.dark .thinking-process :deep(td) {
  border-color: rgba(255, 255, 255, 0.1);
}

.message-time {
  font-size: 12px;
  color: var(--text-color);
  opacity: 0.5;
  text-align: right;
  margin-top: 8px;
}

.thinking-content {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid rgba(0, 201, 167, 0.3);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  background: var(--bg-color);
}

.thinking-content strong {
  color: #00c9a7;
  display: block;
  margin-bottom: 8px;
}

.thinking-process {
  padding: 16px;
  border-radius: 8px;
  white-space: pre-wrap;
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.thinking-process strong {
  color: #00c9a7;
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

.follow-up-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  padding: 16px;
  background: var(--hover-color);
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
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-color);
  border-radius: 8px;
  padding: 12px 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.follow-up-button:hover {
  background: var(--hover-color);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 201, 167, 0.2);
}

/* 暗色主题适配 */
.chat-container.dark {
  background: rgba(30, 30, 30, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-container.dark .message.ai {
  background: #252525;
  color: #e0e0e0;
}

.chat-container.dark .message.error {
  background: #331111;
  color: #ef9a9a;
  border: 1px solid rgba(255, 0, 0, 0.2);
}

.chat-container.dark .message.processing {
  background: #252525;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.chat-container.dark .thinking-content {
  background: #252525;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.chat-container.dark .thinking-process {
  background: #252525;
  color: #e0e0e0;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-container.dark .follow-up-buttons {
  background: rgba(255, 255, 255, 0.05);
  border: 1px dashed rgba(255, 255, 255, 0.1);
}

.chat-container.dark .follow-up-button {
  background: #252525;
  color: #e0e0e0;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-container.dark .follow-up-button:hover {
  background: #333333;
  border: 1px solid #00c9a7;
}
</style>