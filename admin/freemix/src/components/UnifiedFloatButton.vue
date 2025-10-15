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
        :is-date-disabled="isDateDisabled"
        @update:value="handleCalendarUpdate"
      />
    </n-modal>

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
      
      <div class="goals-content">
        <RecentGoals v-if="showGoals" :goals="goals" :formatDate="formatDate" :checktype="checktype" />
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue';
import RecentGoals from '@/components/RecentGoals.vue';
import FeedbackCenter from '@/components/FeedbackCenter.vue';
import { NFloatButton, NIcon, NButton, NModal, NCalendar } from 'naive-ui';
import { CalendarSharp } from '@vicons/ionicons5';

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

// 响应式数据
const showMenu = ref(false);
const showCalendar = ref(false);
const showGoals = ref(false);
const showFeedback = ref(false);
const calendarValue = ref(null);
const isDark = inject('isDark', ref(true));

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
}

.menu-button:hover {
  transform: scale(1.1);
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

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5em;
  color: #81c683;
}

.goals-content {
  padding: 20px 0;
}

/* 透明模态框样式 */
:deep(.n-modal) {
  background-color: transparent !important;
  box-shadow: none !important;
  border: none !important;
}

:deep(.n-modal-content) {
  background-color: transparent !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
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
</style>
<!-- </content> -->