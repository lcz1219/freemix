<template>
  <div class="ai-goal-generator">
    <!-- 用户输入区域 -->
    <div class="input-section">
      <n-input v-model:value="userInput" type="textarea" placeholder="请输入您想要生成的目标，例如：学习Web开发"
        :autosize="{ minRows: 3, maxRows: 6 }" :disabled="isGenerating" />
      <n-space justify="space-between" class="action-buttons">
         <n-button @click="showHistory" quaternary size="small" class="history-button">
        <template #icon>
          <n-icon><Time /></n-icon>
        </template>
        历史记录
      </n-button>
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


    <n-modal v-model:show="showChatContainer" title="创建目标" style="width: 50%;height: 65vh;">
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
          <n-button @click="saveForLater" :loading="isSaving">稍后决定</n-button>
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

    <!-- 历史记录弹窗 -->
    <n-modal v-model:show="showHistoryModal" title="AI生成记录" style="width: 90%; height: 80vh;">
      <div class="history-modal-content">
        <AIGenHistory @record-used="handleRecordUsed" @show-confirmation="handleShowConfirmation" />
      </div>
    </n-modal>
    
    <!-- 目标确认模态框（处理历史记录确认） -->
    <AIGoalConfirmation  v-model:show="showHistoryConfirmation" 
      :ai-response="historyRecord.aiResponse" :sub-goals="historyRecord.subGoals" 
      :user-question="historyRecord.userQuestion" @goal-created="handleHistoryGoalCreated" />
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { useRouter } from 'vue-router';
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
import AIGenHistory from '@/components/AIGenHistory.vue';
import { parseAIResponseToSubGoals,parseAIResponseToSubGoalsNoMarkDown, extractGoalTitle } from '@/utils/aiGoalParser.js';
import { ChatboxEllipsesSharp, Time } from '@vicons/ionicons5';
import { useStore } from 'vuex';
import { getM, postM, isSuccess, uploadFile, uploadGeneralFile } from '@/utils/request.js';
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
const store = useStore();
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
const isSaving = ref(false);
const showHistoryModal = ref(false);

// 处理历史记录确认的数据
const showHistoryConfirmation = ref(false);
const historyRecord = ref({});

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
    type: 'ai-gen',
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
      `请根据以下内容提取具体的执行步骤划分为 5-8 个核心阶段或里程碑，严禁细碎化，请将相关的学习点合并为一个大项：
  ${response.content}
  
  要求：
  1. 必须返回一个标准的 JSON 数组格式，例如: ["第一步内容", "第二步内容"]
  2. 只提取具体的、可执行的操作步骤
  3. 忽略所有的标题、层级说明（如"第一阶段"）、资源链接、时间描述或开场白
  4. 尽可能保留原始回复中具体的行动描述
  5. 不要返回任何 Markdown 标记或额外的文字解释，只返回 JSON 数组本身
  `,
      (updateData) => {
        // 实时更新处理
        console.log('实时更新:', updateData);
      }
    );
    console.log('subGoalsMsg:', subGoalsMsg.content);

    const subGoals = parseAIResponseToSubGoalsNoMarkDown(subGoalsMsg.content);

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

// 稍后决定：保存生成记录
const saveForLater = async () => {
  if (!generatedGoal.value) return;
  
  isSaving.value = true;
  try {
    const saveData = {
      userInput: userInput.value,
      aiResponse: aiResponse.value,
      goalTitle: generatedGoal.value.title,
      childGoals: generatedGoal.value.childGoals,
      chatMessages: chatMessages.value,
    };
    let data={
      username:store.state.user.username,
      saveData:saveData
    }

    const response = await postM('api/aiGen/save',data);

    const result = await response.data.data;
    
    if (isSuccess(response)) {
      window.$message?.success('记录已保存，30天内可随时决定是否使用');
      resetGeneration();
    } else {
      window.$message?.error(result.message || '保存失败，请重试');
    }
  } catch (error) {
    console.error('保存记录失败:', error);
    window.$message?.error('保存失败，请检查网络连接');
  } finally {
    isSaving.value = false;
  }
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

// 显示历史记录
const showHistory = () => {
  // 显示历史记录弹窗
  showHistoryModal.value = true;
};

// 处理历史记录被使用
const handleRecordUsed = (record) => {
  showHistoryModal.value = false;
  // 将历史记录填充到当前输入框
  userInput.value = record.userInput;
  // 可以选择自动开始生成或让用户手动点击生成
  // 这里选择不自动生成，保持用户体验
};

// 处理显示历史记录确认框
const handleShowConfirmation = (record) => {
  console.log('handleShowConfirmation, record:', record);
  
  // 组装传递给AIGoalConfirmation的数据格式
  historyRecord.value = {
    aiResponse: record.aiResponse ,
    subGoals: record.childGoals ? record.childGoals: [],
    userQuestion: record.userInput || record.goalTitle
  };
  
  // 显示确认模态框
  showHistoryConfirmation.value = true;
  showHistoryModal.value = false;
};

// 处理历史记录确认后创建目标
const handleHistoryGoalCreated = (goalObject) => {
  console.log('handleHistoryGoalCreated:', goalObject);
  
  // 关闭确认模态框
  showHistoryConfirmation.value = false;
  
  // 显示成功消息
  
  // 重置历史记录数据
  historyRecord.value = {};
  
  // 可以选择性地触发父组件的goal-created事件
  emit('goal-created', goalObject);
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

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.step-header .n-icon {
  color: #00c9a7;
}

.generator-container {
  max-width: 800px;
  margin: 0 auto;
}

.input-section {
  background: var(--card-bg);
  padding: 24px;
  border-radius: 16px;
  border: 1px solid var(--border-color);
}

.gen-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: #00c9a7;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 201, 167, 0.2);
}

.gen-button:hover {
  background: #00b899;
  box-shadow: 0 6px 16px rgba(0, 201, 167, 0.3);
}

.history-quick-access {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

.history-button {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 25px;
  background: #00c9a7;
  color: #fff;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.history-button:hover {
  background: rgba(7, 107, 57, 0.112);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

/* 暗色主题适配 */
.dark .history-button {
  background: rgba(45, 45, 45, 0.9);
  color: #e0e0e0;
}

.dark .history-button:hover {
  background: rgba(45, 45, 45, 1);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .history-quick-access {
    bottom: 15px;
    right: 15px;
  }
  
  .history-button {
    padding: 8px 12px;
    font-size: 14px;
  }
}

.history-modal-content {
  height: 100%;
  overflow: hidden;
}
</style>