<template>
  <n-modal
    v-model:show="showModal"
    :mask-closable="false"
    preset="card"
    title="确认创建目标"
    style="width: 800px; max-width: 95vw; height: 80vh;"
    :bordered="false"
    class="ai-goal-confirmation-modal"
  >
    <div class="confirmation-content">
      <!-- 目标标题 -->
      <n-form-item label="目标标题">
        <n-input 
          v-model:value="goalData.title" 
          placeholder="请输入目标标题"
          maxlength="50"
          show-count
        />
      </n-form-item>
      
      <!-- 目标描述 -->
      <n-form-item label="目标描述">
        <n-input 
          v-model:value="goalData.description" 
          type="textarea"
          placeholder="请输入目标描述（可选）"
          :autosize="{ minRows: 2, maxRows: 3 }"
        />
      </n-form-item>
      
      <!-- 子目标列表 -->
      <n-form-item label="子目标">
        <div class="sub-goals-container">
          <n-dynamic-input
            v-model:value="goalData.childGoals"
            preset="pair"
            key-placeholder="子目标内容"
            :min="1"
            :max="10"
          >
            <template #default="{ value }">
              <n-input 
                v-model:value="value.message" 
                type="textarea"
                placeholder="请输入子目标内容"
                :autosize="{ minRows: 1, maxRows: 3 }"
              />
            </template>
          </n-dynamic-input>
        </div>
      </n-form-item>
      
      <!-- 其他设置 -->
      <div class="other-settings">
        <n-form-item label="截止日期" class="setting-item">
          <n-date-picker 
            v-model:value="goalData.deadline" 
            type="date" 
            clearable
            style="width: 100%;"
          />
        </n-form-item>
        
        <n-form-item label="优先级" class="setting-item">
          <n-select
            v-model:value="goalData.level"
            :options="priorityOptions"
            style="width: 100%;"
          />
        </n-form-item>
      </div>
      
      <n-form-item label="标签">
        <n-dynamic-tags v-model:value="goalData.tags" />
      </n-form-item>
    </div>
    
    <template #footer>
      <n-space justify="end">
        <n-button @click="cancel">取消</n-button>
        <n-button type="primary" @click="confirm" :loading="confirmLoading">
          确认创建
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { 
  NModal, 
  NButton, 
  NSpace, 
  NFormItem, 
  NInput, 
  NDynamicInput, 
  NGrid, 
  NGi, 
  NDatePicker, 
  NSelect, 
  NDynamicTags
} from 'naive-ui'
import { parseAIResponseToSubGoals, extractGoalTitle } from '@/utils/aiGoalParser.js'
import { createGoalObject } from '@/utils/goalUtils.js'
import { useStore } from 'vuex'
import { postM, isSuccess } from '@/utils/request.js'
import { useMessage } from 'naive-ui'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  aiResponse: {
    type: String,
    default: ''
  },
  userQuestion: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:show', 'goal-created'])

const store = useStore()
const message = useMessage()

// 响应式数据
const showModal = ref(false)
const confirmLoading = ref(false)

// 优先级选项
const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' }
]

// 目标数据
const goalData = ref({
  title: '',
  description: '',
  childGoals: [],
  deadline: null,
  level: 'medium',
  tags: []
})

// 监听show属性变化
watch(() => props.show, (newVal) => {
  showModal.value = newVal
  if (newVal) {
    // 初始化目标数据
    initializeGoalData()
  }
})

// 监听showModal变化
watch(showModal, (newVal) => {
  emit('update:show', newVal)
  if (!newVal) {
    // 重置表单数据
    resetFormData()
  }
})

// 初始化目标数据
const initializeGoalData = () => {
  // 解析AI响应为子目标
  const subGoals = parseAIResponseToSubGoals(props.aiResponse)
  
  // 提取目标标题
  const title = extractGoalTitle(props.aiResponse, props.userQuestion)
  
  // 设置默认截止日期（一周后）
  const oneWeekLater = new Date()
  oneWeekLater.setDate(oneWeekLater.getDate() + 7)
  
  // 初始化目标数据
  goalData.value = {
    title: title,
    description: props.userQuestion,
    childGoals: subGoals,
    deadline: oneWeekLater.getTime(),
    level: 'medium',
    tags: ['ai生成']
  }
}

// 重置表单数据
const resetFormData = () => {
  goalData.value = {
    title: '',
    description: '',
    childGoals: [],
    deadline: null,
    level: 'medium',
    tags: []
  }
}

// 取消操作
const cancel = () => {
  showModal.value = false
}

// 确认创建目标
const confirm = async () => {
  if (!goalData.value.title.trim()) {
    message.warning('请输入目标标题')
    return
  }
  
  if (!goalData.value.childGoals || goalData.value.childGoals.length === 0) {
    message.warning('请至少添加一个子目标')
    return
  }
  
  // 检查是否有空的子目标
  const hasEmptySubGoal = goalData.value.childGoals.some(goal => !goal.message || !goal.message.trim())
  if (hasEmptySubGoal) {
    message.warning('请填写所有子目标内容')
    return
  }
  
  confirmLoading.value = true
  
  try {
    // 创建完整的目标对象
    const goalObject = createGoalObject({
      title: goalData.value.title,
      description: goalData.value.description,
      childGoals: goalData.value.childGoals,
      deadline: goalData.value.deadline ? new Date(goalData.value.deadline) : null,
      level: goalData.value.level,
      tags: goalData.value.tags
    })
    
    // 发送创建目标请求
    const response = await postM('editGoal', goalObject)
    
    if (isSuccess(response)) {
      message.success('目标创建成功')
      showModal.value = false
      emit('goal-created', goalObject)
    } else {
      message.error('目标创建失败: ' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('创建目标时发生错误:', error)
    message.error('创建目标时发生错误: ' + error.message)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style scoped>
.ai-goal-confirmation-modal {
  border-radius: 12px;
  overflow: hidden;
}

.confirmation-content {
  padding: 16px 0;
  max-height: 60vh;
}

.confirmation-content :deep(.n-form-item) {
  margin-bottom: 16px;
}

.confirmation-content :deep(.n-form-item-label) {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.confirmation-content :deep(.n-form-item-label)::after {
  content: ":";
  margin-left: 4px;
}

.dark .confirmation-content :deep(.n-form-item-label) {
  color: #e0e0e0;
}

/* 子目标容器优化 */
.sub-goals-container {
  max-height: 300px;
  overflow-y: auto;
}

.sub-goals-container :deep(.n-dynamic-input) {
  width: 100%;
}

.sub-goals-container :deep(.n-dynamic-input-item) {
  margin-bottom: 8px;
}

/* 其他设置布局 */
.other-settings {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.setting-item {
  margin-bottom: 0;
}

.setting-item :deep(.n-form-item-label) {
  margin-bottom: 8px;
  display: block;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-goal-confirmation-modal {
    width: 95vw !important;
    height: 90vh !important;
  }
  
  .confirmation-content {
    padding: 12px 0;
    max-height: 65vh;
  }
  
  .other-settings {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .confirmation-content :deep(.n-form-item) {
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .ai-goal-confirmation-modal {
    width: 98vw !important;
    height: 95vh !important;
  }
  
  .confirmation-content {
    padding: 8px 0;
    max-height: 70vh;
  }
  
  .confirmation-content :deep(.n-form-item) {
    margin-bottom: 8px;
  }
}
</style>