<template>
  <van-config-provider :theme="currentTheme">
    <div class="app-container" :class="{ 'dark-mode': true }">

      <!-- 顶部导航栏 -->
      <van-nav-bar fixed placeholder class="glass-nav" :border="false" z-index="100" :safe-area-inset-top="true">
        <template #left>
          <van-icon name="arrow-left" @click="goBack" />
        </template>
        <template #title>
          <div class="nav-title">AI助手</div>
        </template>
      </van-nav-bar>

      <div class="content-wrapper">
        <!-- 主要内容区域 -->
        <van-tabs v-model:active="activeTab" background="transparent" line-width="20px" line-height="3px" color="#00c9a7" title-active-color="#00c9a7" :title-inactive-color="'#666'" class="custom-tabs" :animated="true" :swipeable="true">
          <!-- AI聊天标签页 -->
          <van-tab title="AI问答" class="full-height-tab">
            <MobileAIChat ref="aiChatRef" />
          </van-tab>
          
          <!-- AI目标生成标签页 -->
          <van-tab title="AI生产" class="full-height-tab">
            <div class="scrollable-content">
              <MobileAIGoalGenerator :aiAssistantRef="aiChatRef" @goal-created="handleGoalCreated" />
            </div>
          </van-tab>
        </van-tabs>
      </div>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import MobileAIChat from './mobile-ai-chat.vue'
import MobileAIGoalGenerator from './mobile-ai-goal-generator.vue'

const router = useRouter()

// State
const activeTab = ref(0)
const currentTheme = ref('dark')
const aiChatRef = ref(null)

// Methods
const goBack = () => {
  router.back()
}

const handleGoalCreated = (goal) => {
  showToast({
    type: 'success',
    message: '目标创建成功'
  })
  console.log('目标已创建:', goal)
}
</script>

<style scoped lang="scss">
.app-container {
  --bg-primary: #f7f8fa;
  --bg-secondary: #ffffff;
  --text-primary: #2c3e50;
  --text-secondary: #8590a6;
  --card-shadow: 0 8px 20px rgba(100, 100, 100, 0.06);
  --brand-color: #00c9a7;
  --glass-bg: rgba(255, 255, 255, 0.85);
  --border-line: #f0f2f5;

  height: 100vh;
  height: 100dvh; /* 适配移动端动态视口 */
  display: flex;
  flex-direction: column;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
}

/* --- 暗黑模式覆盖 --- */
.app-container.dark-mode {
  --bg-primary: #121212;
  --bg-secondary: #1e1e1e;
  --text-primary: #e2e2ea;
  --text-secondary: #888;
  --card-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
  --glass-bg: rgba(30, 30, 30, 0.85);
  --border-line: #2c2c2c;
}

.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0; /* 移除padding，由内部组件控制 */
}

/* 顶部导航 */
.glass-nav {
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);

  ::v-deep(.van-nav-bar__content) {
    height: 50px;
  }
}

.nav-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

/* 标签页样式 */
.custom-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  overflow: hidden;
  
  ::v-deep(.van-tabs__content) {
    flex: 1;
    background: var(--bg-primary);
    overflow: hidden; /* 内容区域不滚动，由内部元素决定 */
    display: flex;
    flex-direction: column;
  }
  
  ::v-deep(.van-tab__pane) {
    height: 100%;
    width: 100%;
  }
  
  ::v-deep(.van-tab__title) {
    font-size: 15px;
    font-weight: 500;
  }
}

.full-height-tab {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.scrollable-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: 16px;
  padding-bottom: 20px;
}
</style>