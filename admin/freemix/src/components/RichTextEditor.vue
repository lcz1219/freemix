<template>
  <div class="plain-text-editor">
    <div class="toolbar">
      <n-tag 
        v-if="statusText" 
        :type="statusType" 
        size="tiny" 
        round 
        :bordered="false" 
        style="margin-left: auto;"
      >
        {{ statusText }}
      </n-tag>
    </div>
    
    <div class="editor-main">
      <n-input
        v-model:value="content"
        type="textarea"
        placeholder="在此输入笔记内容..."
        :autosize="{ minRows: 10, maxRows: 20 }"
        class="editor-input"
        @update:value="handleInput"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { NInput, NText, NTag } from 'naive-ui';

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    default: 'saved' // 'saved', 'saving', 'unsaved'
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

const content = ref(props.modelValue || '');

const statusType = computed(() => {
  switch (props.status) {
    case 'saving': return 'warning';
    case 'unsaved': return 'error';
    case 'saved': return 'success';
    default: return 'default';
  }
});

const statusText = computed(() => {
  switch (props.status) {
    // 隐藏保存中的提示，因为自动保存是静默的
    case 'saving': return '';
    case 'unsaved': return '未保存';
    case 'saved': return '已保存';
    default: return '';
  }
});

// Sync from prop
watch(() => props.modelValue, (val) => {
  if (val !== content.value) {
    content.value = val || '';
  }
});

const handleInput = (val) => {
  emit('update:modelValue', val);
  emit('change', val);
};
</script>

<style scoped>
.plain-text-editor {
  border: 1px solid #e0e0e63b;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  background: #252525; /* 浅灰色背景，与白色卡片区分 */
}

.toolbar {
  padding: 4px 8px; /* 减小内边距 */
  border-bottom: 1px solid #e0e0e63b;
  background: #252525; /* 浅灰色背景，与白色卡片区分 */
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 28px; /* 固定高度 */
}

.editor-main {
  display: flex;
  flex-direction: column;
}

.editor-input {
  border: none !important;
  box-shadow: none !important;
  background-color: transparent !important;
}

:deep(.n-input__textarea-el) {
  padding: 12px;
  line-height: 1.6;
  background-color: transparent !important;
}

:deep(.n-input) {
  --n-border: none !important;
  --n-border-hover: none !important;
  --n-border-focus: none !important;
  --n-box-shadow: none !important;
  --n-box-shadow-focus: none !important;
  background-color: transparent !important;
}
</style>
