<template>
  <div class="ai-sidebar">
    <div class="sidebar-header">
      <div class="new-chat-btn" @click="$emit('new-chat')">
        <n-icon size="18"><Add/></n-icon>
        <span>开启新对话</span>
      </div>
    </div>
    
    <div class="tab-switcher">
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'chat' }" 
        @click="activeTab = 'chat'"
      >聊天</div>
      <!-- <div 
        class="tab-item" 
        :class="{ active: activeTab === 'gen' }" 
        @click="activeTab = 'gen'"
      >生成历史</div> -->
    </div>

    <div class="session-list-container">
      <!-- 聊天会话列表 -->
      <div v-if="activeTab === 'chat'">
        <div 
          v-for="session in sessions" 
          :key="session.id"
          class="session-item"
          :class="{ active: currentSessionId === session.id }"
          @click="$emit('select-session', session.id)"
        >
          <div class="icon-wrap"><n-icon><ChatbubbleEllipsesOutline /></n-icon></div>
          <div class="session-info">
            <div class="session-title">{{ session.title || '无标题对话' }}</div>
            <div class="session-time">{{ formatTime(session.updatedAt || session.id) }}</div>
          </div>
          <n-icon 
            name="delete" 
            class="delete-icon" 
            @click.stop="$emit('delete-session', session.id)" 
          ><TrashOutline /></n-icon>
        </div>
      </div>

      <!-- AI 生成记录简易列表 -->
      <div v-else>
        <div 
          v-for="record in genHistory" 
          :key="record.id"
          class="session-item gen-record"
          @click="$emit('use-gen-record', record)"
        >
          <div class="icon-wrap"><n-icon><SparklesOutline /></n-icon></div>
          <div class="session-info">
            <div class="session-title">{{ record.goalTitle || '生成的计划' }}</div>
            <div class="session-time">{{ formatDate(record.createdAt) }}</div>
          </div>
        </div>
      </div>
      
      <n-empty v-if="isEmpty" description="空空如也" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { NIcon, NEmpty } from 'naive-ui';
import { 
   Add,
  ChatbubbleEllipsesOutline, 
  TrashOutline,
  SparklesOutline 
} from '@vicons/ionicons5';

const props = defineProps({
  sessions: { type: Array, default: () => [] },
  genHistory: { type: Array, default: () => [] },
  currentSessionId: { type: String, default: null }
});

const activeTab = ref('chat');
const isEmpty = computed(() => {
  return (activeTab.value === 'chat' && props.sessions.length === 0) ||
         (activeTab.value === 'gen' && props.genHistory.length === 0);
});

defineEmits(['new-chat', 'select-session', 'delete-session', 'use-gen-record']);

const formatTime = (ts) => {
  const date = new Date(Number(ts));
  return date.toLocaleDateString([], { month: '2-digit', day: '2-digit' });
};

const formatDate = (ts) => {
  if (!ts) return '';
  const date = new Date(ts);
  return `${date.getMonth() + 1}/${date.getDate()}`;
};
</script>

<style scoped lang="scss">
.ai-sidebar {
  width: 260px;
  height: 100%;
  background: rgba(var(--card-bg-rgb), 0.3);
  display: flex;
  flex-direction: column;
  color: var(--text-color);
  border-right: 1px solid var(--border-color);
  flex-shrink: 0;
  backdrop-filter: blur(10px);
}

.sidebar-header {
  padding: 24px 16px;
}

.new-chat-btn {
  background: linear-gradient(135deg, #00c9a7, #00897b);
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(0, 201, 167, 0.2);
  
  &:hover { 
    transform: translateY(-2px); 
    box-shadow: 0 8px 25px rgba(0, 201, 167, 0.4);
    filter: brightness(1.1);
  }
  &:active { transform: scale(0.96); }
}

.tab-switcher {
  display: flex;
  margin: 0 16px 20px;
  background: rgba(0, 0, 0, 0.05);
  padding: 4px;
  border-radius: 12px;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 8px;
    font-size: 13px;
    border-radius: 9px;
    cursor: pointer;
    color: var(--text-color);
    opacity: 0.6;
    transition: all 0.3s;
    
    &.active { 
      background: var(--card-bg); 
      color: #00c9a7; 
      font-weight: bold; 
      opacity: 1;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    }
  }
}

.session-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px 24px;

  /* 自定义滚动条 */
  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.05);
    border-radius: 10px;
  }
}

.session-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  margin-bottom: 10px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  background: transparent;

  &:hover { 
    background: var(--hover-color);
    .delete-icon { opacity: 0.6; }
  }
  
  &.active { 
    background: var(--hover-color); 
    border-color: rgba(0, 201, 167, 0.2);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
    .icon-wrap { color: #00c9a7; opacity: 1; }
    .session-title { color: #00c9a7; font-weight: 600; }
  }

  .icon-wrap { 
    font-size: 20px; 
    color: var(--text-color); 
    opacity: 0.4; 
    margin-right: 14px; 
    display: flex; 
    transition: all 0.3s ease;
  }
  
  .session-info {
    flex: 1;
    overflow: hidden;
    
    .session-title { 
      font-size: 14px; 
      white-space: nowrap; 
      overflow: hidden; 
      text-overflow: ellipsis; 
      color: var(--text-color);
      transition: all 0.3s ease;
    }
    .session-time { 
      font-size: 11px; 
      color: var(--text-color); 
      opacity: 0.4; 
      margin-top: 4px; 
    }
  }

  .delete-icon { 
    opacity: 0; 
    color: var(--text-color);
    font-size: 16px;
    padding: 4px;
    border-radius: 8px;
    transition: all 0.2s ease;
    
    &:hover { 
      color: #ff4d4f; 
      background: rgba(255, 77, 79, 0.1);
      opacity: 1 !important;
    }
  }
}

.dark {
  .tab-switcher {
    background: rgba(255, 255, 255, 0.05);
  }
  .session-list-container::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.05);
  }
}
</style>

