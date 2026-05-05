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
          :class="{ active: currentSessionId === session._id }"
          @click="$emit('select-session', session._id)"
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
  width: 280px;
  height: 100%;
  background: #141414;
  display: flex;
  flex-direction: column;
  color: #e0e0e0;
}

.sidebar-header {
  padding: 20px 16px;
}

.new-chat-btn {
  background: #00c9a7;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 15px rgba(0, 201, 167, 0.2);
  
  &:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0, 201, 167, 0.3); }
  &:active { transform: scale(0.96); }
}

.tab-switcher {
  display: flex;
  margin: 0 16px 15px;
  background: #252525;
  padding: 4px;
  border-radius: 10px;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 6px;
    font-size: 13px;
    border-radius: 7px;
    cursor: pointer;
    color: #888;
    transition: all 0.3s;
    
    &.active { background: #333; color: #00c9a7; font-weight: bold; }
  }
}

.session-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
  border: 1px solid transparent;

  &:hover { 
    background: #202020;
    .delete-icon { opacity: 1; }
  }
  
  &.active { 
    background: #202020; 
    border-color: rgba(0, 201, 167, 0.5);
    .icon-wrap { color: #00c9a7; }
  }

  .icon-wrap { font-size: 18px; color: #555; margin-right: 12px; display: flex; }
  
  .session-info {
    flex: 1;
    overflow: hidden;
    
    .session-title { font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #ccc; }
    .session-time { font-size: 11px; color: #555; margin-top: 2px; }
  }

  .delete-icon { 
    opacity: 0; 
    color: #444; 
    font-size: 16px;
    &:hover { color: #ff4d4f; }
  }
}

.gen-record {
  .icon-wrap { color: #ff9800 !important; }
}
</style>

