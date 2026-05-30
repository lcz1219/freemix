<template>
  <div class="unified-float-container">
    <!-- 主浮动按钮 -->
    <n-float-button
      :right="right"
      :bottom="bottom"
      :width="width"
      :height="height"
      :z-index="zIndex"
      @click="toggleMenu"
    >
      <n-icon>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
          <path d="M12,7c-2.8,0-5,2.2-5,5s2.2,5,5,5s5-2.2,5-5S14.8,7,12,7z M12,14.5c-1.4,0-2.5-1.1-2.5-2.5s1.1-2.5,2.5-2.5 s2.5,1.1,2.5,2.5S13.4,14.5,12,14.5z" />
          <path d="M12,5c-0.6,0-1-0.4-1-1V1c0-0.6,0.4-1,1-1s1,0.4,1,1v3C13,4.6,12.6,5,12,5z" />
          <path d="M12,19c-0.6,0-1,0.4-1,1v3c0,0.6,0.4,1,1,1s1-0.4,1-1v-3C13,19.4,12.6,19,12,19z" />
          <path d="M23,11h-3c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S23.6,11,23,11z" />
          <path d="M4,11H1c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S4.6,11,4,11z" />
          <path d="M18.7,6.3c0.4-0.4,0.4-1,0-1.4l-2.1-2.1c-0.4-0.4-1-0.4-1.4,0s-0.4,1,0,1.4l2.1,2.1C17.7,6.7,18.3,6.7,18.7,6.3z" />
          <path d="M5.3,17.7c-0.4,0.4-0.4,1,0,1.4l2.1,2.1c0.4,0.4,1,0.4,1.4,0s0.4-1,0-1.4l-2.1-2.1C6.3,17.3,5.7,17.3,5.3,17.7z" />
          <path d="M18.7,17.7c-0.4-0.4-1-0.4-1.4,0l-2.1,2.1c-0.4,0.4-0.4,1,0,1.4s1,0.4,1.4,0l2.1-2.1C19.1,18.7,19.1,18.1,18.7,17.7z" />
          <path d="M5.3,6.3c0.4,0.4,1,0.4,1.4,0l2.1-2.1c0.4-0.4,0.4-1,0-1.4s-1-0.4-1.4,0L5.3,4.9C4.9,5.3,4.9,5.9,5.3,6.3z" />
        </svg>
      </n-icon>
    </n-float-button>

    <!-- 展开菜单 -->
    <transition name="slide-up">
      <div v-if="showMenu" class="float-menu" :style="{ right: right + 'px', bottom: bottom + height + 10 + 'px' }">
        <!-- AI助手按钮 -->
        <n-button class="menu-button"
          circle 
              @click="openAIAssistant">
              <template #icon>
                <!-- <img :src="aiAssistantIcon" style="width: 24px; height: 24px;" /> -->
                <svg width="100%" height="100%" viewBox="0 0 512 512" version="1.1" xmlns="http://www.w3.org/2000/svg">
                  <defs>
                    <!-- 定义渐变让图标更有质感 -->
                    <linearGradient id="iconGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" style="stop-color:#2AF598;stop-opacity:1" />
                      <stop offset="100%" style="stop-color:#009EFD;stop-opacity:1" />
                    </linearGradient>
                  </defs>

                  <!-- 1. 背景容器：墨绿色 -->
                  <!-- 模仿原图的粗边框风格，stroke为深青色，fill为墨绿色 -->
                  <rect x="30" y="30" width="452" height="452" rx="100" ry="100" fill="#0f2e26" stroke="#065f56"
                    stroke-width="40" />

                  <!-- 2. AI 核心图标：星光 (Sparkles) -->
                  <g transform="translate(0, -20)" fill="#1de9b6">
                    <!-- 主星 -->
                    <path
                      d="M256,120 C270,200 280,220 360,235 C280,250 270,270 256,350 C242,270 232,250 152,235 C232,220 242,200 256,120 Z" />
                    <!-- 右上角小星 -->
                    <path
                      d="M380,100 C385,130 390,140 420,145 C390,150 385,160 380,190 C375,160 370,150 340,145 C370,140 375,130 380,100 Z" />
                  </g>

                  <!-- 3. 底部进度条容器 (灰色背景) -->
                  <rect x="136" y="350" width="240" height="40" rx="20" ry="20" fill="#1f4a40" />

                  <!-- 4. 底部进度条 (高亮部分) -->
                  <!-- 模仿原图的“进行中”状态 -->
                  <rect x="136" y="350" width="160" height="40" rx="20" ry="20" fill="#1de9b6" />
                </svg>
              </template>
              <!-- AI助手 -->
            </n-button>
        <n-button
          class="menu-button"
          circle
          :title="'更新发布'"
          size="large"
          @click="openUpdateNotification"
        >
          <n-icon size="24" >
            <LogoMicrosoft />
          </n-icon>
        </n-button>
        
        <!-- 反馈中心按钮 -->
        <n-button
          class="menu-button"
          circle
          :title="'反馈中心'"
          size="large"
          @click="toggleFeedback"
        >
          <n-icon size="24">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M20,2H4C2.9,2,2,2.9,2,4v18l4-4h14c1.1,0,2-0.9,2-2V4C22,2.9,21.1,2,20,2z M20,16H5.2L4,17.2V4h16V16z" />
              <path d="M11,8H9v2h2V8z M11,12H9v4h2V12z" />
              <path d="M15,8h-2v6h2V8z" />
            </svg>
          </n-icon>
        </n-button>
        
        <!-- 日历按钮 -->
        <n-button
          class="menu-button"
          circle
          size="large"
          :title="'日历'"
          @click="toggleCalendar"
        >
          <n-icon size="24">
            <CalendarSharp />
          </n-icon>
        </n-button>
        
        <!-- 近期目标按钮 -->
        <n-button
          class="menu-button"
          circle
          size="large"
          :title="'近期目标'"
          @click="toggleGoals"
        >
          <n-icon size="24">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M19,19H5V5h14V19z" />
              <path d="M14,12h-4c-0.6,0-1-0.4-1-1s0.4-1,1-1h4c0.6,0,1,0.4,1,1S14.6,12,14,12z" />
              <path d="M14,16h-4c-0.6,0-1-0.4-1-1s0.4-1,1-1h4c0.6,0,1,0.4,1,1S14.6,16,14,16z" />
              <path d="M14,8h-4C9.4,8,9,7.6,9,7s0.4-1,1-1h4c0.6,0,1,0.4,1,1S14.6,8,14,8z" />
            </svg>
          </n-icon>
        </n-button>
      </div>
    </transition>



    <!-- 日历弹窗 -->
    <n-modal
      v-model:show="showCalendar"
      :mask-closable="true"
      preset="card"
      draggable
      :closable="false"
      :style="{ width: calendarModalWidth, height: calendarModalHeight, overflowY: 'auto' }"
      :class="isDark ? 'modal-dark' : 'modal-light'"
    >
      <n-calendar
        v-model:value="calendarValue"
        #="{ year, month, date }"
        @update:value="handleCalendarUpdate"
      >
        <div class="calendar-cell-content">
          <div v-for="goal in getGoalsForDate(year, month, date)" :key="goal.id" class="goal-item" @click.stop="handleGoalClick(goal)">
            <n-tag :type="getGoalStatusType(goal.status)" size="small" round :bordered="false" class="goal-tag">
              {{ goal.title }}
            </n-tag>
          </div>
        </div>
      </n-calendar>
    </n-modal>

    <!-- 目标详情弹窗 (复用 GoalDetail 组件) -->
    <GoalDetail
      v-if="selectedGoal"
      v-model:show="showDetailModal"
      :goal="selectedGoal"
      @update="handleGoalUpdate"
    />

    <!-- 反馈中心抽屉 -->
    <FeedbackCenter v-model:show="showFeedback" />
    
    <!-- 近期目标弹窗 -->
    <n-modal
      v-model:show="showGoals"
      :mask-closable="true"
      draggable
      :closable="false"
      :style="{ width: goalsModalWidth, height: goalsModalHeight, overflowY: 'auto'}"
      :class="isDark ? 'modal-dark' : 'modal-light'"
    >
      <template #header>
        <div class="modal-header">
          <h2>近期目标</h2>
        </div>
      </template>
      
      <!-- <div class="goals-content"> -->
        <RecentGoals v-if="showGoals" :goals="goals" :formatDate="formatDate" :checktype="checktype" />
      <!-- </div> -->
    </n-modal>
  </div>
</template>

<script setup>
import { ref, inject, nextTick } from 'vue';
import RecentGoals from '@/components/RecentGoals.vue';
import FeedbackCenter from '@/components/FeedbackCenter.vue';
import GoalDetail from '@/components/GoalDetail.vue';
import { NFloatButton, NIcon, NButton, NModal, NCalendar, NInput, NTag } from 'naive-ui';
import { CalendarSharp,LogoDiscord,LogoReddit,LogoMicrosoft } from '@vicons/ionicons5';
import { createNewWindow, generateWindowId } from '@/utils/utilsWindowManager.js';
import router from '@/router';
import { useStore } from 'vuex';
import { callCozeAPI } from '@/utils/aiService.js';

const store = useStore();

// 定义属性
const props = defineProps({
  goals: {
    type: Array,
    required: true
  },
  formatDate: {
    type: Function,
    required: true
  },
  checktype: {
    type: Function,
    required: true
  },
  right: {
    type: Number,
    default: 10
  },
  bottom: {
    type: Number,
    default: 50
  },
  width: {
    type: Number,
    default: 40
  },
  height: {
    type: Number,
    default: 40
  },
  zIndex: {
    type: Number,
    default: 100
  },
  calendarModalWidth: {
    type: String,
    default: '50%'
  },
  calendarModalHeight: {
    type: String,
    default: '90vh'
  },
  goalsModalWidth: {
    type: String,
    default: '50%'
  },
  goalsModalHeight: {
    type: String,
    default: '70vh'
  }
});

// 定义事件
const emit = defineEmits(['dateSelected']);
const openUpdateNotification = () => {
  router.push('/admin/updates')
};
// 响应式数据
const showMenu = ref(false);
const showCalendar = ref(false);
const showGoals = ref(false);
const showFeedback = ref(false);
const showAIAssistant = ref(false);
const calendarValue = ref(Date.now());
const isDark = inject('isDark', ref(true));
const userInput = ref('');
const isSending = ref(false);
const chatMessages = ref([]);
const showDetailModal = ref(false);
const selectedGoal = ref(null);

// 根据日期获取目标
const getGoalsForDate = (year, month, date) => {
  const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(date).padStart(2, '0')}`;
  return props.goals.filter(goal => {
    if (!goal.deadline) return false;
    const goalDate = new Date(goal.deadline);
    const goalDateStr = `${goalDate.getFullYear()}-${String(goalDate.getMonth() + 1).padStart(2, '0')}-${String(goalDate.getDate()).padStart(2, '0')}`;
    return goalDateStr === dateStr;
  });
};

// 获取目标状态对应的 Tag 类型
const getGoalStatusType = (status) => {
  switch (status) {
    case 'completed': return 'success';
    case 'in_progress': return 'info';
    case 'pending': return 'warning';
    case 'expired': return 'error';
    default: return 'default';
  }
};

const handleGoalClick = (goal) => {
  selectedGoal.value = goal;
  showDetailModal.value = true;
};

const handleGoalUpdate = (updatedGoal) => {
  store.commit('updateGoal', updatedGoal);
  showDetailModal.value = false;
};

// 方法
const toggleMenu = () => {
  showMenu.value = !showMenu.value;
};

const toggleFeedback = () => {
  showMenu.value = false;
  showFeedback.value = !showFeedback.value;
};

const toggleCalendar = () => {
  showMenu.value = false;
  showCalendar.value = !showCalendar.value;
};

const toggleGoals = () => {
  showMenu.value = false;
  showGoals.value = !showGoals.value;
};

const openAIAssistant = async () => {
  store.commit('setAiDrawer', true)
  // showMenu.value = false;
 
  
  // const windowId = generateWindowId();
  // try {
  //   await createNewWindow(windowId, {
  //     width: 1100,
  //     height: 600,
  //     minWidth: 400,
  //     minHeight: 500
  //   }, '/AIAssistantWindow');
    
  // } catch (error) {
  //   console.error('创建AI助手窗口失败:', error);
  // }
};

const isDateDisabled = (timestamp) => {
  // 禁用过去的日期
  const date = new Date(timestamp);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date < today;
};

const handleCalendarUpdate = (value) => {
  console.log('选择的日期:', value);
  // 触发事件，将选择的日期传递给父组件
  emit('dateSelected', value);
};

// 格式化时间
const formatTime = (timestamp) => {
  return timestamp.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

// 发送推荐问题
const sendFollowUpQuestion = (question) => {
  userInput.value = question;
  sendMessage();
};

// 发送消息到AI
const sendMessage = async () => {
  if (!userInput.value.trim() || isSending.value) return;
  
  // 添加用户消息到聊天记录
  const userMessage = {
    type: 'user',
    content: userInput.value,
    timestamp: new Date()
  };
  chatMessages.value.push(userMessage);
  
  // 清空输入框
  const userQuestion = userInput.value;
  userInput.value = '';
  
  // 设置发送状态
  isSending.value = true;
  
  // 添加一个初始的AI处理中消息
  const processingMessage = {
    type: 'ai',
    messageType: 'processing',
    content: '',
    isProcessing: true,
    timestamp: new Date()
  };
  const processingMessageIndex = chatMessages.value.length;
  chatMessages.value.push(processingMessage);
  
  // 滚动到底部
  scrollToBottom();
  
  try {
    // 调用自定义AIAPI
    const aiResponse = await callCustomAIAPI(userQuestion, (messageData) => {
      // 实时更新处理中的消息
      if (chatMessages.value[processingMessageIndex]) {
        const message = chatMessages.value[processingMessageIndex];
        message.messageType = messageData.messageType;
        message.content = messageData.content || message.content;
        message.thinkingContent = messageData.thinkingContent;
        message.followUpQuestions = messageData.followUpQuestions;
        message.isProcessing = messageData.isProcessing;
      }
      
      // 每次更新消息时都滚动到底部
      scrollToBottom();
    });
    
    // 更新最终的AI回复到聊天记录
    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex];
      message.messageType = aiResponse.messageType;
      message.content = aiResponse.content;
      message.thinkingContent = aiResponse.thinkingContent;
      message.followUpQuestions = aiResponse.followUpQuestions;
      message.isProcessing = false;
    }
    
    // 滚动到底部
    scrollToBottom();
  } catch (error) {
    // 更新错误消息到聊天记录
    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex];
      message.type = 'error';
      message.messageType = 'error';
      message.content = '抱歉，AI助手暂时无法回应，请稍后再试。';
      message.isProcessing = false;
    }
    console.error('AI API调用失败:', error);
    
    // 滚动到底部
    scrollToBottom();
  } finally {
    isSending.value = false;
  }
};

// 滚动聊天容器到底部的函数
const scrollToBottom = () => {
  // 使用nextTick确保DOM已更新
  nextTick(() => {
    const chatContainer = document.querySelector('.chat-container');
    if (chatContainer) {
      chatContainer.scrollTop = chatContainer.scrollHeight;
    }
  });
};

// 调用自定义AI API
const callCustomAIAPI = async (question, onUpdate) => {
  try {
    const result = await callCozeAPI(question, onUpdate);
    
    // 如果没有获取到有效响应，返回默认消息
    if (!result.content.trim() && (!result.followUpQuestions || result.followUpQuestions.length === 0) && !result.thinkingContent.trim()) {
      result.content = 'AI助手已处理您的问题，但未返回有效回复。';
    }
    console.log("result:===>",result);
    
    return result;
  } catch (error) {
    console.error('AI API调用失败:', error);
    // 返回更友好的错误提示
    throw new Error(`抱歉，AI助手暂时无法回应，请稍后再试。
    
错误详情：${error.message}
    
解决建议：
1. 检查您的网络连接是否正常
2. 验证Personal Access Token是否正确且未过期（必须以"pat_"开头）
3. 确保您已在Coze平台正确配置了Bot
4. 查看浏览器控制台获取更多调试信息
    
获取新的Personal Access Token的步骤：
1. 访问Coze平台 (https://www.coze.cn)
2. 登录您的账户
3. 进入个人设置或开发者设置
4. 找到API密钥或Personal Access Token部分
5. 删除旧的令牌并生成新的令牌
6. 将新令牌替换代码中的当前令牌`);
  }
};
</script>

<style scoped>
.float-menu {
  position: fixed;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 99;
}

.menu-button {
  width: 40px;
  height: 40px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #00c9a7b6, #366237);
  border: none;
  box-shadow: 0 4px 12px rgba(129, 198, 131, 0.132);
}

.menu-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(129, 198, 131, 0.5);
}

/* 滑动动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.modal-dark {
  background: linear-gradient(135deg, #1a1a1a, #121212);
  color: #ffffff;
  border: 1px solid rgba(129, 198, 131, 0.2);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.modal-light {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  color: #333333;
  border: 1px solid rgba(129, 198, 131, 0.3);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid rgba(129, 198, 131, 0.3);
  background: linear-gradient(90deg, #00c9a7, transparent);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5em;
  background: linear-gradient(90deg, #00c9a7, #4CAF50);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

.goals-content {
  padding: 20px 0;
}

.ai-assistant-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: rgba(129, 198, 131, 0.05);
  border-radius: 12px;
  margin-bottom: 16px;
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
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  margin-left: 70%;
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.message.ai {
  background: linear-gradient(135deg, #f5f5f5, #eeeeee);
  margin-right: 30%;
  border: 1px solid rgba(129, 198, 131, 0.2);
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

.message-content {
  margin-bottom: 8px;
  line-height: 1.6;
}

.message-time {
  font-size: 12px;
  color: #999;
  text-align: right;
  margin-top: 8px;
}

.thinking-content {
  background: linear-gradient(135deg, #fff8e1, #ffecb3);
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
  background: linear-gradient(135deg, #eceff1, #cfd8dc);
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

.input-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: rgba(129, 198, 131, 0.05);
  border-radius: 12px;
  border: 1px solid #00c9a7;
}

.send-button {
  align-self: flex-end;
  width: 100px;
  background: linear-gradient(135deg, #00c9a7, #4CAF50);
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(129, 198, 131, 0.3);
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(129, 198, 131, 0.5);
}

.send-button:disabled {
  background: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
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
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  border: 1px solid rgba(129, 198, 131, 0.3);
  border-radius: 8px;
  padding: 12px 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.follow-up-button:hover {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(129, 198, 131, 0.3);
}

/* 暗色主题适配 */
.modal-dark .chat-container {
  background: rgba(42, 42, 42, 0.7);
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.modal-dark .message.user {
  background: linear-gradient(135deg, #1565c0, #0d47a1);
  color: white;
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.modal-dark .message.ai {
  background: linear-gradient(135deg, #424242, #2d2d2d);
  color: #e0e0e0;
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.modal-dark .message.error {
  background: linear-gradient(135deg, #424242, #333333);
  color: #ef9a9a;
  border: 1px solid rgba(255, 0, 0, 0.3);
}

.modal-dark .message.processing {
  background: linear-gradient(135deg, #37474f, #263238);
  border: 1px solid rgba(129, 198, 131, 0.4);
}

.modal-dark .thinking-content {
  background: linear-gradient(135deg, #37474f, #263238);
  color: #e0e0e0;
  border: 1px solid rgba(255, 193, 7, 0.4);
}

.modal-dark .thinking-process {
  background: linear-gradient(135deg, #37474f, #263238);
  color: #e0e0e0;
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.modal-dark .input-container {
  background: rgba(42, 42, 42, 0.7);
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.modal-dark .follow-up-buttons {
  background: rgba(42, 42, 42, 0.7);
  border: 1px dashed rgba(129, 198, 131, 0.4);
}

.modal-dark .follow-up-button {
  background: linear-gradient(135deg, #424242, #333333);
  color: #e0e0e0;
  border: 1px solid rgba(129, 198, 131, 0.4);
}

.modal-dark .follow-up-button:hover {
  background: linear-gradient(135deg, #37474f, #263238);
  border: 1px solid rgba(129, 198, 131, 0.6);
}

/* 透明模态框样式 */
:deep(.n-modal) {
  background-color: transparent !important;
  box-shadow: none !important;
  border: none !important;
}

:deep(.n-modal-content) {
  background-color: transparent !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.n-modal-body) {
  background-color: transparent !important;
  padding: 20px;
}

:deep(.n-timeline) {
  padding-left: 20px;
}

:deep(.n-timeline-item) {
  margin-bottom: 15px;
}

:deep(.n-timeline-item-content__title) {
  font-weight: 600;
}

:deep(.n-timeline-item-content__time) {
  font-size: 0.9em;
  opacity: 0.8;
}

/* 添加一些装饰性元素 */
.chat-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00c9a7, transparent);
}

.modal-dark .chat-container::before {
  background: linear-gradient(90deg, transparent, #00c9a7, transparent);
}

.calendar-cell-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 4px;
  overflow-y: auto;
  max-height: 80px;
}

.goal-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.goal-item:hover {
  transform: scale(1.02);
}

.goal-tag {
  width: 100%;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  padding: 0 8px;
  height: 24px;
}

/* 隐藏滚动条但保留功能 */
.calendar-cell-content::-webkit-scrollbar {
  width: 4px;
}

.calendar-cell-content::-webkit-scrollbar-thumb {
  background-color: rgba(128, 128, 128, 0.3);
  border-radius: 2px;
}
</style>
<!-- </content> -->
