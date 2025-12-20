<template>
  <div class="ai-menu-page">
    
    
    <div class="ai-menu-container">
      <!-- 左侧菜单 -->
      <div class="menu-sidebar">
        <n-menu
          v-model:value="activeMenu"
          :options="menuOptions"
          @update:value="handleMenuChange"
        />
      </div>
      <AIAssistantMsg ref="aiAssistantRef" v-show="false" />
      <!-- 右侧内容区域 -->
      <div class="content-area">
        <!-- AI问答页面 -->
        <div v-if="activeMenu === 'ai-chat'" class="chat-container">
          <AIAssistantMsg ref="aiAssistantRef" />
        </div>

        <!-- AI生产页面 -->
        <div v-else-if="activeMenu === 'ai-production'" class="production-page">
          <AIGoalGenerator :aiAssistantRef="aiAssistantRef" @goal-created="handleGoalCreated" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue';
import { 
  NMenu, 
  NIcon 
} from 'naive-ui';
import AIAssistantMsg from '@/components/AIAssistantMsg.vue';
import AIGoalGenerator from '@/components/AIGoalGenerator.vue';
import AIAssistantIcon from '@/components/icons/AIAssistantIcon.vue';

// 响应式数据
const activeMenu = ref('ai-chat');
const aiAssistantRef = ref(null);

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
  // 可以在这里添加初始化逻辑
});
</script>

<style scoped>
.ai-menu-page {
 
  padding-top: 5px;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: row;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: bold;
  color: var(--text-color-primary);
}

.ai-menu-container {
  display: flex;
  flex: 1;
}

.menu-sidebar {
  width: 200px;
  background: var(--card-background);
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.content-area {
  flex: 1;
  background: var(--card-background);
  border-radius: 8px;
  /* padding: 20px; */
  padding-left: 20px;
  padding-right: 20px;
  padding-top: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
}

.production-page {
  flex: 1;
  overflow-y: auto;
}

/* 暗色主题适配 */
.dark .menu-sidebar,
.dark .content-area {
  background: rgba(42, 42, 42, 0.7);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
</style>