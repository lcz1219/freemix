<template>
  <n-float-button
    :right="right"
    :bottom="bottom"
    :width="width"
    :height="height"
    :z-index="zIndex"
    @click="toggleGoals"
  >
    <n-icon>
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
        <path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M19,19H5V5h14V19z" />
        <path d="M14,12h-4c-0.6,0-1-0.4-1-1s0.4-1,1-1h4c0.6,0,1,0.4,1,1S14.6,12,14,12z" />
        <path d="M14,16h-4c-0.6,0-1-0.4-1-1s0.4-1,1-1h4c0.6,0,1,0.4,1,1S14.6,16,14,16z" />
        <path d="M14,8h-4C9.4,8,9,7.6,9,7s0.4-1,1-1h4c0.6,0,1,0.4,1,1S14.6,8,14,8z" />
      </svg>
    </n-icon>
   
  </n-float-button>

  <!-- 近期目标弹窗 -->
  <n-modal
    v-model:show="showGoals"
    :mask-closable="true"
    draggable
    :closable="false"
    :style="{ width:modalWidth, height: modalHeight, overflowY: 'auto'}"
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
</template>

<script setup>
import { ref, inject, computed } from 'vue';
import RecentGoals from '@/components/RecentGoals.vue';
import { NFloatButton, NIcon, NModal, NTimeline, NTimelineItem, NEmpty } from 'naive-ui';

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
    default: 100
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
  modalWidth: {
    type: String,
    default: '50%'
  },
  modalHeight: {
    type: String,
    default: '70vh'
  }
});

// 响应式数据
const showGoals = ref(false);
const isDark = inject('isDark', ref(true));

// 计算最近的5个目标
const recentGoals = computed(() => {
  return props.goals.slice(0, 5);
});

// 方法
const toggleGoals = () => {
  showGoals.value = !showGoals.value;
};
</script>

<style scoped>
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