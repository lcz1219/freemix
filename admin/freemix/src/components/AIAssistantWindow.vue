<template>
  <div class="ai-menu-page">
    <div class="ai-menu-container">
      <!-- 1. 功能侧边栏 (一级菜单) -->
      <div class="menu-sidebar">
        <n-menu
          v-model:value="activeMenu"
          :options="menuOptions"
        />
      </div>

      <!-- 2. 内容侧边栏 (会话列表/生成记录) -->
      <AISidebar 
        v-if="activeMenu === 'ai-chat'"
        :sessions="sessions" 
        :gen-history="genHistory"
        :current-session-id="currentSessionId"
        @new-chat="createNewSession"
        @select-session="switchSession"
        @delete-session="deleteSession"
        @use-gen-record="handleUseGenRecord"
      />

      <!-- 3. 主内容区域 -->
      <div class="content-area">
         <AIAssistantMsg 
            v-show="false"
            
            ref="aiAssistantRef" 
          />
        <!-- AI 聊天场景 -->
        <div v-if="activeMenu === 'ai-chat'" class="chat-container">
          <AIAssistantMsg 
            v-if="currentSession"
            :key="currentSessionId"
            :currentSessionId="currentSessionId"
            :initial-messages="currentSession.messages"
            @update-messages="syncMessagesToSession"
            ref="aiAssistantRef" 
          />
          <div v-else class="empty-chat-state">
            <van-empty description="从左侧开启一段对话或查看生成历史" />
          </div>
        </div>

        <!-- AI 生产场景 (独立页面) -->
        <div v-else-if="activeMenu === 'ai-production'" class="production-page">
          <AIGoalGenerator :aiAssistantRef="aiAssistantRef" @goal-created="handleGoalCreated" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, h, computed } from 'vue';
import { NMenu, NIcon, useMessage } from 'naive-ui';
import AIAssistantMsg from '@/components/AIAssistantMsg.vue';
import AIGoalGenerator from '@/components/AIGoalGenerator.vue';
import AISidebar from '@/components/AISidebar.vue';
import AIAssistantIcon from '@/components/icons/AIAssistantIcon.vue';
import { getM, postM, isSuccess } from '@/utils/request';
import { useStore } from 'vuex';

const store = useStore();
const message = useMessage();
const activeMenu = ref('ai-chat');
const aiAssistantRef = ref(null);

// 数据状态
const sessions = ref([]);
const genHistory = ref([]);
const currentSessionId = ref(null);

const currentSession = computed(() => 
  sessions.value.find(s => s.id === currentSessionId.value)
);

const loadSessions = async () => {
  try {
    const res = await getM(`/ai-messages/sessions/${store.state.user.username}`);
    if (isSuccess(res)) {
      sessions.value = res.data.data || [];
      
      if (sessions.value.length > 0 && !currentSessionId.value) {
        currentSessionId.value = sessions.value[0].id;
        switchSession(currentSessionId.value)
      }
    }
  } catch (e) {
    console.error('加载会话失败:', e);
  }
};

const createNewSession = () => {
  // 前端直接创建一个 ID 即可，第一条消息发送时后端会根据这个 ID 分组
  const newId = Date.now().toString();
  const newSession = {
    id: newId,
    title: '新对话',
    messages: [],
    updatedAt: Date.now()
  };
  // sessions.value=[newSession]
  sessions.value.unshift(newSession);
  currentSessionId.value = newId;
};

const switchSession = async (id) => {
  currentSessionId.value = id;
  console.log("id",id);
  
  // 切换时如果消息为空，从后端拉取
  const session = sessions.value.find(s => s.id === id);
  if (session ) {
    try {
      const res = await getM(`/ai-messages/session/${id}`);
      if (isSuccess(res)) {
        // 转换后端消息格式为前端显示格式
        session.messages = res.data.data.map(m => ([
          { type: 'user', content: m.userQuestion, timestamp: new Date(m.createdAt) },
          { 
            type: 'ai', 
            messageType: m.messageType, 
            content: m.aiAnswer, 
            thinkingContent: m.thinkingContent,
            followUpQuestions: m.followUpQuestions,
            timestamp: new Date(m.createdAt) 
          }
        ])).flat();
        if(session.messages.length ==0) {
           createNewSession()
        }
      }
    } catch (e) {
      console.error('获取会话详情失败:', e);
    }
  }
};

const deleteSession = async (id) => {
  try {
    const res = await getM(`/ai-messages/delsession/${id}`, {}, 'DELETE');
    if (isSuccess(res)) {
      sessions.value = sessions.value.filter(s => s.id !== id);
      if (currentSessionId.value === id) {
        currentSessionId.value = sessions.value.length > 0 ? sessions.value[0].id : null;
      }
    }
  } catch (e) {
    console.error('删除会话失败:', e);
  }
};

const syncMessagesToSession = (newMessages) => {
  if (currentSession.value) {
    currentSession.value.messages = newMessages;
    currentSession.value.updatedAt = Date.now();
    
    const firstUserMsg = newMessages.find(m => m.type === 'user');
    if (firstUserMsg && (currentSession.value.title === '新对话' || !currentSession.value.title)) {
      currentSession.value.title = firstUserMsg.content.substring(0, 15);
    }
    // 注意：这里不再需要 saveSessions 到 localStorage，因为 sendMessage 会实时发给后端 save
  }
};

// --- 生成历史管理 (从 MongoDB 加载) ---
const loadGenHistory = async () => {
  try {
    const res = await getM('/api/aiGen/list', {
      page: 0,
      size: 50,
      username: store.state.user.username
    });
    if (isSuccess(res)) {
      genHistory.value = res.data.data.records || [];
    }
  } catch (error) {
    console.error('加载生成历史失败:', error);
  }
};

const handleUseGenRecord = (record) => {
  // 交互逻辑：当点击生成历史时，创建一个新会话并把生成结果作为“已完成”的对话放入其中
  const newId = 'gen-' + record.id;
  const existing = sessions.value.find(s => s.id === newId);
  
  if (!existing) {
    const newSession = {
      id: newId,
      title: '历史生成: ' + (record.goalTitle || '未命名'),
      messages: [
        { type: 'user', content: `我想生成关于“${record.userInput}”的目标`, timestamp: new Date(record.createdAt) },
        { 
          type: 'ai', 
          messageType: 'answer', 
          content: `已为您生成目标：**${record.goalTitle}**\n\n子目标包括：\n${record.childGoals.map(g => '- ' + g.message).join('\n')}`,
          timestamp: new Date(record.createdAt)
        }
      ],
      updatedAt: record.createdAt
    };
    sessions.value.unshift(newSession);
    currentSessionId.value = newId;
  } else {
    currentSessionId.value = newId;
  }
  saveSessions();
};

// 菜单选项
const menuOptions = [
  {
    label: 'AI问答',
    key: 'ai-chat',
    icon: () => h(NIcon, null, {
      default: () => h(AIAssistantIcon)
    })
  },
  {
    label: 'AI生产',
    key: 'ai-production',
    icon: () => h(NIcon, null, {
      default: () => h('svg', { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24', width: '1em', height: '1em', fill: 'currentColor' }, [
        h('path', { d: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z' }),
        h('path', { d: 'M12 7c-.55 0-1 .45-1 1v8c0 .55.45 1 1 1s1-.45 1-1V8c0-.55-.45-1-1-1zm-4 4c-.55 0-1 .45-1 1v4c0 .55.45 1 1 1s1-.45 1-1v-4c0-.55-.45-1-1-1zm8 0c-.55 0-1 .45-1 1v4c0 .55.45 1 1 1s1-.45 1-1v-4c0-.55-.45-1-1-1z' })
      ])
    })
  }
];

// 处理菜单切换
const handleMenuChange = (key) => {
  activeMenu.value = key;
};

// 处理目标创建完成
const handleGoalCreated = (goal) => {
  // 可以在这里添加其他处理逻辑，比如显示成功消息等
  console.log('目标已创建:', goal);
};

// 在组件挂载时初始化
onMounted(() => {
  loadSessions();
});
</script>

<style scoped>
.ai-menu-page {
 
  padding-top: 5px;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: row;
}

.ai-menu-container {
  display: flex;
  flex: 1;
}

.menu-sidebar {
  width: 80px;
  background: var(--card-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  border-right: 1px solid var(--border-color);
}

:deep(.n-menu) {
  .n-menu-item-content {
    padding: 0 !important;
    display: flex;
    justify-content: center;
    
    &.n-menu-item-content--selected {
      color: #00c9a7 !important;
      .n-menu-item-content__icon { color: #00c9a7 !important; }
    }
    
    .n-menu-item-content__icon { margin-right: 0 !important; font-size: 24px; }
    .n-menu-item-content-header { display: none; }
  }
}

.content-area {
  flex: 1;
  background: var(--bg-color);
  border-radius: 8px;
  /* padding: 20px; */
  padding-left: 20px;
  padding-right: 20px;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
}

.empty-chat-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 暗色主题适配 */
.dark .menu-sidebar,
.dark .content-area {
  background: rgba(42, 42, 42, 0.7);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.ai-menu-page {
  background-color: var(--bg-color);
}
</style>