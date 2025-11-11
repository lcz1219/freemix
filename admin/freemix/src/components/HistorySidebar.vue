<template>
  <div class="history-sidebar" :class="{ 'collapsed': isCollapsed }">
    <div class="sidebar-header">
      <h3 v-if="!isCollapsed">历史记录</h3>
      <n-button 
        @click="toggleCollapse" 
        quaternary 
        circle 
        size="small"
        class="toggle-button"
      >
        <n-icon>
          <svg v-if="isCollapsed" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
            <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/>
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
            <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/>
          </svg>
        </n-icon>
      </n-button>
    </div>
    
    <div v-if="!isCollapsed" class="history-list">
      <div v-if="isLoading" class="loading-indicator">
        <n-spin size="small" />
        <span>加载中...</span>
      </div>
      
      <div v-else-if="historyList.length === 0" class="empty-history">
        <span>暂无历史记录</span>
      </div>
      
      <div v-else>
        <div 
          v-for="(item, index) in historyList" 
          :key="item.id" 
          class="history-item"
          :class="{ 'active': activeIndex === index }"
          @click="scrollToHistoryItem(index)"
        >
          <div class="history-title">{{ item.title }}</div>
          <div class="history-time">{{ formatTime(item.timestamp) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject, watch, toRefs } from 'vue';
import { NButton, NIcon, NSpin } from 'naive-ui';

// 定义props
const props = defineProps({
  chatMessages: {
    type: Array,
    required: true
  }
});

// 定义emits
const emit = defineEmits(['scroll-to-history']);

// 响应式数据
const isCollapsed = ref(false);
const isLoading = ref(true);
const historyList = ref([]);
const activeIndex = ref(-1);

// 注入依赖
const isDark = inject('isDark', ref(true));

// 切换折叠状态
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
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

// 滚动到指定的历史记录项
const scrollToHistoryItem = (index) => {
  activeIndex.value = index;
  emit('scroll-to-history', index);
};

// 从聊天消息中提取历史记录
const extractHistoryFromMessages = () => {
  const history = [];
  let currentConversation = null;
  let messageId = 0;
  
  // 遍历所有消息，提取用户消息作为历史记录项
  props.chatMessages.forEach((message, index) => {
    if (message.type === 'user') {
      // 创建新的历史记录项
      currentConversation = {
        id: messageId++,
        title: message.content.length > 30 
          ? message.content.substring(0, 30) + '...' 
          : message.content,
        timestamp: message.timestamp,
        messageIndex: index
      };
      history.push(currentConversation);
    }
  });
  
  return history;
};

// 更新历史记录列表
const updateHistoryList = () => {
  historyList.value = extractHistoryFromMessages();
  isLoading.value = false;
};

// 监听聊天消息变化
const { chatMessages } = toRefs(props);
watch(chatMessages, () => {
  updateHistoryList();
}, { deep: true });

// 初始化
onMounted(() => {
  updateHistoryList();
});
</script>

<style scoped>
.history-sidebar {
  display: flex;
  flex-direction: column;
  width: 220px;
  background: rgba(129, 198, 131, 0.05);
  border-right: 1px solid rgba(129, 198, 131, 0.1);
  transition: width 0.3s ease;
  height: 100%;
  overflow: hidden;
}

.history-sidebar.collapsed {
  width: 40px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px;
  border-bottom: 1px solid rgba(129, 198, 131, 0.1);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.toggle-button {
  flex-shrink: 0;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.loading-indicator,
.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: #999;
  gap: 8px;
}

.history-item {
  padding: 10px;
  margin-bottom: 6px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.history-item:hover {
  background: rgba(129, 198, 131, 0.1);
}

.history-item.active {
  background: rgba(129, 198, 131, 0.2);
}

.history-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-time {
  font-size: 12px;
  color: #999;
}

/* 暗色主题适配 */
.dark .history-sidebar {
  background: rgba(42, 42, 42, 0.7);
  border-right-color: rgba(129, 198, 131, 0.2);
}

.dark .sidebar-header {
  border-bottom-color: rgba(129, 198, 131, 0.2);
}

.dark .sidebar-header h3 {
  color: #e0e0e0;
}

.dark .history-item:hover {
  background: rgba(129, 198, 131, 0.15);
}

.dark .history-item.active {
  background: rgba(129, 198, 131, 0.25);
}

.dark .history-title {
  color: #e0e0e0;
}

.dark .history-time {
  color: #999;
}

.dark .loading-indicator,
.dark .empty-history {
  color: #999;
}
</style>