<template>
  <div class="ai-goal-generator">
    <!-- 用户输入区域 -->
    <div class="input-section">
      <n-input v-model:value="userInput" type="textarea" placeholder="请输入您想要生成的目标，例如：学习Web开发"
        :autosize="{ minRows: 3, maxRows: 6 }" :disabled="isGenerating" />
      <n-space justify="end" class="action-buttons">
        <n-button @click="generateGoal" :loading="isGenerating" :disabled="!userInput.trim() || isGenerating"
          type="primary">
          {{ isGenerating ? '生成中...' : '生成目标' }}
        </n-button>
      </n-space>
    </div>

    <!-- 错误提示 -->
    <n-alert v-if="errorMessage" type="error" closable class="error-message">
      {{ errorMessage }}
    </n-alert>

    <!-- ai响应 -->
    <n-popover trigger="hover">
      <template #trigger>
        <n-button @click="showChatContainer = true" :disabled="chatMessages.length == 0" style="margin: 10px;">
          <n-icon>
            <ChatboxEllipsesSharp />
          </n-icon>
        </n-button>
      </template>
      <span v-if="chatMessages.length == 0">先生成一个目标🙇,才能看到ai的回答</span>
      <span v-if="chatMessages.length > 0">点击按钮查看ai的回答</span>
    </n-popover>


    <n-modal v-model:show="showChatContainer" title="创建目标" style="width: 50%;height: 50vh;">
      <div>
        <AIChatContainer v-if="chatMessages.length > 0" :chat-messages="chatMessages" :format-time="formatTime"
          :isShowThinking="false" />
      </div>
    </n-modal>
    <!-- AI聊天容器 -->


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

    <!-- 目标确认模态框 -->
    <AIGoalConfirmation v-if="generatedGoal" v-model:show="showConfirmationModal" :ai-response="aiResponse"
      :user-question="userInput" @goal-created="handleGoalCreated" :subGoals="generatedGoal.childGoals" />
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import AIChatContainer from './AIChatContainer.vue';
import {
  NEmpty,
  NInput,
  NButton,
  NCard,
  NList,
  NIcon,
  NListItem,
  NThing,
  NSpace,
  NAlert,
  NModal,
  NPopover,
  useDialog
} from 'naive-ui';
import AIGoalConfirmation from '@/components/AIGoalConfirmation.vue';
import { parseAIResponseToSubGoals, extractGoalTitle } from '@/utils/aiGoalParser.js';
import { ChatboxEllipsesSharp } from '@vicons/ionicons5';
// Props定义
const props = defineProps({
  aiAssistantRef: {
    type: Object,
    default: null
  }
});

// Emits定义
const emit = defineEmits(['goal-created']);
const chatMessages = ref([]);
const showChatContainer = ref(false);
// 时间格式化函数
const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
};

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
const userInput = ref('');
const isGenerating = ref(false);
const generatedGoal = ref(null);
const aiResponse = ref('');
const errorMessage = ref('');
const showConfirmationModal = ref(false);

// 生成目标
const generateGoal = async () => {
  if (!userInput.value.trim()) return;

  isGenerating.value = true;
  errorMessage.value = '';
  generatedGoal.value = null;

  // 添加用户消息到聊天记录


  // 创建处理中的消息索引
  const processingMessageIndex = chatMessages.value.length;
  chatMessages.value.push({
    type: 'ai',
    messageType: 'processing',
    content: '',
    isProcessing: true,
    timestamp: Date.now()
  });

  try {
    // 调用AI API生成目标
    let response;
    if (props.aiAssistantRef && typeof props.aiAssistantRef.callCustomAIAPI === 'function') {
      // 通过AI助手组件调用API，并实现实时更新
      response = await props.aiAssistantRef.callCustomAIAPI(userInput.value, (messageData) => {
        // 实时更新处理中的消息
        if (chatMessages.value[processingMessageIndex]) {
          const message = chatMessages.value[processingMessageIndex];
          message.messageType = messageData.messageType;
          message.content = messageData.content || message.content;
          message.thinkingContent = messageData.thinkingContent;
          message.isProcessing = messageData.isProcessing;
        }
      });
    } else {
      // 如果无法通过组件调用，则使用备选方案
      response = await alternativeAICall(userInput.value);
    }

    aiResponse.value = response.content;

    // 更新最终的AI回复到聊天记录
    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex];
      message.messageType = 'answer';
      message.content = response.content;
      message.isProcessing = false;
    }

    // 解析AI响应为子目标

    const subGoalsMsg = await props.aiAssistantRef.callCustomAIAPI(
      `请分析以下内容并提取步骤，以序号列表形式返回：
  ${response.content}
  
  要求：
  1. 只返回步骤列表，不要额外解释
  2. 格式为：(1) 第一步 (2) 第二步 ...
  3.不需要概括，尽可能的复制AI回复中的内容
  4. 步骤的序号必须连续且递增
  `,
      (updateData) => {
        // 实时更新处理
        console.log('实时更新:', updateData);
      }
    );
    console.log('subGoalsMsg:', subGoalsMsg.content);

    const subGoals = parseAIResponseToSubGoals(subGoalsMsg.content);

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

    // 更新错误消息到聊天记录
    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex];
      message.type = 'error';
      message.messageType = 'error';
      message.content = errorMessage.value;
      message.isProcessing = false;
    }
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
  // 触发父组件的事件
  emit('goal-created', goal);
};
</script>

<style scoped>
.ai-goal-generator {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* .input-section {
  margin-bottom: 20px;
} */

.action-buttons {
  margin-top: 10px;
}

.error-message {
  margin-bottom: 20px;
}

.result-section {
  flex: 1;
  overflow-y: auto;
}

.result-card {
  height: 100%;
  display: flex;
  overflow: auto;
  flex-direction: column;
}

.goal-preview {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
}

.goal-description {
  margin: 10px 0;
  color: var(--text-color-secondary);
}

.sub-goals {
  margin-top: 20px;
}

.sub-goals h4 {
  margin-bottom: 10px;
}

.confirmation-buttons {
  margin-top: 20px;
}

.initial-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 暗色主题适配 */
.dark .goal-description {
  color: #e0e0e0;
}

.ai-chat-container {
  /* margin: 20px 0; */
  /* height: 400px; */
  border-radius: 12px;
  /* overflow: hidden; */
}
</style>