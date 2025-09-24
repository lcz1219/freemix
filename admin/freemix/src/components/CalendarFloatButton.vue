<template>
  <n-float-button
    :right="right"
    :bottom="bottom"
    :width="width"
    :height="height"
    :z-index="zIndex"
    @click="toggleCalendar"
  >
    <n-icon>
      <CalendarSharp />
    </n-icon>
  </n-float-button>

  <!-- 日历弹窗 -->
  <n-modal
    v-model:show="showCalendar"
    :mask-closable="true"
    preset="card"
    draggable
    :closable="false"
    :style="{ width: modalWidth, height: modalHeight, overflowY: 'auto' }"
    :class="isDark ? 'modal-dark' : 'modal-light'"
  >
    <n-calendar
      v-model:value="calendarValue"
      :is-date-disabled="isDateDisabled"
      @update:value="handleCalendarUpdate"
    />
  </n-modal>
</template>

<script setup>
import { ref, inject } from 'vue';
import { NFloatButton, NIcon, NModal, NCalendar } from 'naive-ui';
import { CalendarSharp } from '@vicons/ionicons5';

// 定义属性
const props = defineProps({
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
  modalWidth: {
    type: String,
    default: '50%'
  },
  modalHeight: {
    type: String,
    default: '90vh'
  }
});

// 定义事件
const emit = defineEmits(['dateSelected']);

// 响应式数据
const showCalendar = ref(false);
const calendarValue = ref(null);
const isDark = inject('isDark', ref(true));

// 方法
const toggleCalendar = () => {
  showCalendar.value = !showCalendar.value;
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
/* 日历样式 */
:deep(.n-calendar) {
  --n-color: rgba(30, 30, 42, 0.7);
  --n-text-color: #ffffff;
}

.modal-light :deep(.n-calendar) {
  --n-color: rgba(255, 255, 255, 0.9);
  --n-text-color: #000000;
}

:deep(.n-calendar-header) {
  margin-bottom: 12px;
}

:deep(.n-calendar-cell) {
  border-radius: 8px;
}

:deep(.n-calendar-cell--selected) {
  background-color: rgba(138, 43, 226, 0.3) !important;
}

:deep(.n-calendar-cell--today) {
  border: 2px solid #8a2be2;
}

.modal-light :deep(.n-calendar-cell--selected) {
  background-color: rgba(138, 43, 226, 0.2) !important;
}

.modal-light :deep(.n-calendar-cell--today) {
  border: 2px solid #8a2be2;
}
</style>