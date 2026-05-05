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
  background: rgba(129, 198, 131, 0.05);
  margin: 11px 16px -4px 16px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid #00c9a7;
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
  background: #00c9a7;
  margin-left: 70%;
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.message.ai {
  background: linear-gradient(135deg, #252525, #242424);
  margin-right: 30%;
  border: 1px solid rgba(129, 198, 131, 0.2);
}
.message.ai-gen {
  background: linear-gradient(135deg, #252525, #242424);
  border: 1px solid rgba(129, 198, 131, 0.2);
  height: 64vh;
  overflow-y: auto;
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

.message.highlighted {
  box-shadow: 0 0 0 2px rgba(129, 198, 131, 0.5);
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
  background-color: #1a1a1a;
  padding: 1em;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1em 0;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.answer-content :deep(pre code),
.thinking-process :deep(pre code) {
  background-color: transparent;
  color: #e0e0e0;
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
.dark .answer-content h1,
.dark .answer-content h2,
.dark .answer-content h3,
.dark .answer-content h4,
.dark .answer-content h5,
.dark .answer-content h6,
.dark .thinking-process h1,
.dark .thinking-process h2,
.dark .thinking-process h3,
.dark .thinking-process h4,
.dark .thinking-process h5,
.dark .thinking-process h6 {
  color: #58a6ff;
  border-bottom-color: #30363d;
}

.dark .answer-content h1 {
  border-bottom: 2px solid #30363d;
}

.dark .answer-content h2 {
  border-bottom: 1px solid #30363d;
}

.dark .answer-content h3,
.dark .answer-content h4 {
  border-left-color: #58a6ff;
}

.dark .answer-content code,
.dark .thinking-process code {
  background-color: rgba(110, 118, 129, 0.4);
}

.dark .answer-content pre,
.dark .thinking-process pre {
  background-color: #161b22;
}

.dark .answer-content blockquote,
.dark .thinking-process blockquote {
  color: #8b949e;
  border-left-color: #30363d;
}

.dark .answer-content tr:nth-child(2n),
.dark .thinking-process tr:nth-child(2n) {
  background-color: rgba(110, 118, 129, 0.1);
}

.dark .answer-content th,
.dark .answer-content td,
.dark .thinking-process th,
.dark .thinking-process td {
  border-color: #30363d;
}

.message-time {
  font-size: 12px;
  color: white;
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
  background: linear-gradient(135deg, #222523, #234224);
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