<template>
  <div class="ai-menu-page">
    <div class="page-header">
      <h1>AI功能</h1>
    </div>
    
    <div class="ai-menu-container">
      <!-- 左侧菜单 -->
      <div class="menu-sidebar">
        <n-menu
          v-model:value="activeMenu"
          :options="menuOptions"
          @update:value="handleMenuChange"
        />
      </div>
      
      <!-- 右侧内容区域 -->
         <div class="content-area">
          <AIAssistantMsg ref="aiAssistantRef" v-show="false" />
      <!-- AI问答页面 -->
    <div v-if="activeMenu === 'ai-chat'" class="chat-container">
      <AIAssistantMsg ref="aiAssistantRef" />
    </div>

      <!-- AI生产页面 -->
      <div v-else-if="activeMenu === 'ai-production'" class="production-page">
        <div class="ai-goal-generator">
          <!-- 用户输入区域 -->
          <div class="input-section">
            <n-input
              v-model:value="userInput"
              type="textarea"
              placeholder="请输入您想要生成的目标，例如：学习Web开发"
              :autosize="{ minRows: 3, maxRows: 6 }"
              :disabled="isGenerating"
            />
            <n-space justify="end" class="action-buttons">
              <n-button
                @click="generateGoal"
                :loading="isGenerating"
                :disabled="!userInput.trim() || isGenerating"
                type="primary"
              >
                {{ isGenerating ? '生成中...' : '生成目标' }}
              </n-button>
            </n-space>
          </div>

          <!-- 错误提示 -->
          <n-alert v-if="errorMessage" type="error" closable class="error-message">
            {{ errorMessage }}
          </n-alert>

          <!-- AI生成结果展示 -->
          <div v-if="generatedGoal" class="result-section">
            <n-card title="AI生成的目标" class="result-card">
              <div class="goal-preview">
                <h3>{{ generatedGoal.title }}</h3>
                <p class="goal-description">{{ generatedGoal.description }}</p>
                
                <div class="sub-goals">
                  <h4>子目标：</h4>
                  <n-list bordered>
                    <n-list-item v-for="(subGoal, index) in generatedGoal.childGoals" :key="index">
                      <n-thing :title="`步骤 ${index + 1}`" :description="subGoal.message" />
                    </n-list-item>
                  </n-list>
                </div>
              </div>

              <n-space justify="end" class="confirmation-buttons">
                <n-button @click="resetGeneration">重新生成</n-button>
                <n-button @click="confirmGoal" type="primary">确认创建</n-button>
              </n-space>
            </n-card>
          </div>

          <!-- 初始状态提示 -->
          <div v-else-if="!isGenerating && !errorMessage" class="initial-state">
            <n-empty description="请输入目标描述，AI将为您生成详细的执行步骤">
              <template #extra>
                <p>例如：学习Web开发、制定健身计划、准备旅行攻略等</p>
              </template>
            </n-empty>
          </div>
        </div>
      </div>
    </div>

    <!-- 目标确认模态框 -->
    <AIGoalConfirmation
      v-if="generatedGoal"
      v-model:show="showConfirmationModal"
      :ai-response="aiResponse"
      :user-question="userInput"
      @goal-created="handleGoalCreated"
    />
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted,h } from 'vue';
import { 
  NMenu, 
  NIcon, 
  NEmpty, 
  NInput, 
  NButton, 
  NCard, 
  NList, 
  NListItem, 
  NThing, 
  NSpace, 
  NAlert 
} from 'naive-ui';
import AIAssistantMsg from '@/components/AIAssistantMsg.vue';
import AIGoalConfirmation from '@/components/AIGoalConfirmation.vue';
import { parseAIResponseToSubGoals, extractGoalTitle, createGoalObject } from '@/utils/aiGoalParser.js';

// 解析AI响应为子目标的辅助函数
// const parseAIResponseToSubGoals = (response) => {
//   // 移除可能的markdown格式符号
//   const cleanResponse = response.replace(/\*\*/g, '').replace(/__/g, '');
  
//   // 匹配带有编号或项目符号的行
//   const lines = cleanResponse.split('\n').filter(line => 
//     line.trim() !== '' && 
//     (line.match(/^[\d\-\*\•\+]/) || line.includes('：')) // 包含特殊字符或冒号
//   );
  
//   // 将匹配的行转换为子目标对象
//   return lines.map((line, index) => {
//     // 移除行首的编号或项目符号
//     const cleanLine = line.replace(/^[\d\-\*\•\+\s]*[\.\:\：]?\s*/, '').trim();
//     return {
//       id: Date.now() + index, // 简单的唯一ID生成
//       title: cleanLine || `子目标 ${index + 1}`, // 使用清理后的文本或默认标题
//       completed: false
//     };
//   });
// };

// 备选的AI API调用方法
const alternativeAICall = async (input) => {
  // 模拟API调用延迟
  await new Promise(resolve => setTimeout(resolve, 1000));
  
  // 返回模拟的响应数据
  return {
    content: `根据您的需求"${input}"，我为您制定了以下计划：
    
1. 确定具体目标
2. 制定执行步骤
3. 分配时间资源
4. 定期检查进度
5. 根据实际情况调整策略`
  };
};

// 响应式数据
const activeMenu = ref('ai-chat');
const userInput = ref('');
const isGenerating = ref(false);
const generatedGoal = ref(null);
const aiResponse = ref('');
const errorMessage = ref('');
const showConfirmationModal = ref(false);
const aiAssistantRef = ref(null);

// 菜单选项
const menuOptions = [
  {
    label: 'AI问答',
    key: 'ai-chat',
    icon: () => h(NIcon, null, {
      default: () => h('svg', { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24', width: '1em', height: '1em', fill: 'currentColor' }, [
        h('path', { d: 'M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.2L4 17.2V4h16v12z' }),
        h('path', { d: 'M6 12h12v2H6zM6 9h12v2H6zM6 6h12v2H6z' })
      ])
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

// 生成目标
const generateGoal = async () => {
  if (!userInput.value.trim()) return;
  
  isGenerating.value = true;
  errorMessage.value = '';
  generatedGoal.value = null;
  
  try {
    // 调用AI API生成目标
    let response;
    if (aiAssistantRef.value && typeof aiAssistantRef.value.callCustomAIAPI === 'function') {
      // 通过AI助手组件调用API
      response = await aiAssistantRef.value.callCustomAIAPI(userInput.value);
    } else {
      // 如果无法通过组件调用，则使用备选方案
      // 这里应该实现一个备选的API调用方法
      response = await alternativeAICall(userInput.value);
    }
    
    aiResponse.value = response.content;
    
    // 解析AI响应为子目标
    const subGoals = parseAIResponseToSubGoals(response.content);
    
    // 提取目标标题
    const title = extractGoalTitle(response.content, userInput.value);
    
    // 创建生成的目标对象
    generatedGoal.value = {
      title: title,
      description: userInput.value,
      childGoals: subGoals
    };
  } catch (error) {
    console.error('生成目标时发生错误:', error);
    errorMessage.value = error.message || '生成目标时发生错误，请稍后再试';
  } finally {
    isGenerating.value = false;
  }
};

// 重置生成状态
const resetGeneration = () => {
  userInput.value = '';
  generatedGoal.value = null;
  aiResponse.value = '';
  errorMessage.value = '';
};

// 确认创建目标
const confirmGoal = () => {
  showConfirmationModal.value = true;
};

// 处理目标创建完成
const handleGoalCreated = (goal) => {
  showConfirmationModal.value = false;
  resetGeneration();
  // 可以在这里添加其他处理逻辑，比如显示成功消息等
};

// 在组件挂载时初始化
onMounted(() => {
  // 可以在这里添加初始化逻辑
});
</script>

<style scoped>
.ai-menu-page {
  padding: 20px;
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
  gap: 20px;
  overflow: hidden;
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
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  /* overflow: hidden; */
  overflow-y: auto;
}

.production-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder {
  text-align: center;
  color: var(--text-color-secondary);
}

/* 暗色主题适配 */
.dark .menu-sidebar,
.dark .content-area {
  background: rgba(42, 42, 42, 0.7);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.dark .placeholder {
  color: #e0e0e0;
}
</style>