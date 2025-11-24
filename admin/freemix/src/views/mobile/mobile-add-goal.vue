<template>
  <van-config-provider :theme="currentTheme" class="apple-style-provider">
    <div class="mobile-add-goal">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="添加目标"
        left-arrow
        @click-left="goBack"
        class="apple-nav-bar"
      >
        <template #left>
          <van-icon name="arrow-left" size="18" color="var(--van-text-color)" />
        </template>
        <template #right>
          <van-button
            round
            size="small"
            class="apple-gradient-button"
            @click="saveGoal"
            :loading="saving"
          >
            <van-icon name="success" style="margin-right: 4px;" />
            保存
          </van-button>
        </template>
      </van-nav-bar>

      <!-- 表单内容 -->
      <div class="form-container">
        <van-form @submit="saveGoal">
          <!-- 基本信息 -->
          <div class="apple-card">
            <van-cell-group inset title="基本信息">
              <van-field
                v-model="goal.title"
                name="title"
                label="目标标题"
                placeholder="请输入目标标题"
                :rules="formRules.title"
                required
                class="apple-field"
                maxlength="30"
                show-word-limit
              />
              <van-field
                v-model="goal.description"
                name="description"
                label="目标描述"
                type="textarea"
                placeholder="请输入目标描述"
                rows="3"
                autosize
                class="apple-field"
              />
            </van-cell-group>
          </div>

          <!-- 子目标 -->
          <div class="apple-card">
            <van-cell-group inset title="子目标">
              <div v-if="goal.childGoals.length === 0" class="apple-empty-placeholder">
                <van-empty description="暂无子目标" image-size="60" />
              </div>
              <div v-else class="apple-subgoal-list">
                <div v-for="(childGoal, index) in goal.childGoals" :key="index" class="apple-subgoal-item">
                  <van-field
                    v-model="childGoal.message"
                    :placeholder="`子目标 ${index + 1}`"
                    class="apple-subgoal-field"
                  />
                  <van-icon
                    name="minus"
                    class="apple-remove-icon"
                    @click="removeChildGoal(index)"
                  />
                </div>
              </div>
              <div class="apple-add-button-container">
                <van-button
                  round
                  icon="plus"
                  class="apple-gradient-button-small"
                  @click="addChildGoal"
                >
                  添加子目标
                </van-button>
              </div>
            </van-cell-group>
          </div>

          <!-- 文件上传 -->
          <div class="apple-card">
            <van-cell-group inset title="文件上传">
              <van-cell title="附件" class="apple-field">
                <template #value>
                  <MobileUpload
                    @uploadSuccess="fileChange"
                    @fileRemove="fileChange"
                    @uploadError="handleFileUploadError"
                    :fileList="goal.fileList"
                  />
                </template>
              </van-cell>
            </van-cell-group>
          </div>

          <!-- 负责人与时间 -->
          <div class="apple-card">
            <van-cell-group inset title="负责人与时间">
              <van-field
                v-model="goal.owner"
                name="owner"
                label="负责人"
                placeholder="请选择负责人"
                :rules="formRules.owner"
                required
                readonly
                is-link
                @click="showOwnerPicker = true"
                class="apple-field"
              />
              <van-field
                v-model="formattedDeadline"
                name="deadline"
                label="截止日期"
                placeholder="请选择截止日期"
                :rules="formRules.deadline"
                required
                readonly
                is-link
                @click=" showDatePicker = true"
                class="apple-field"
              />
            </van-cell-group>
          </div>

          <!-- 优先级与分类 -->
          <div class="apple-card">
            <van-cell-group inset title="优先级与分类">
              <van-field
                v-model="goal.level"
                name="level"
                label="优先级"
                placeholder="请选择优先级"
                :rules="formRules.level"
                required
                is-link
                readonly
                @click="showLevelPicker = true"
                class="apple-field"
              />
              <van-field
                v-model="goalTagsText"
                name="tags"
                label="分类标签"
                placeholder="添加标签（回车确认）"
                class="apple-field"
              >
                <template #button>
                  <van-button 
                    size="small" 
                    class="apple-gradient-button-small" 
                    @click="addCustomTag"
                  >
                    添加
                  </van-button>
                </template>
              </van-field>
              <div v-if="goal.tags.length > 0" class="apple-tags-container">
                <van-tag
                  v-for="(tag, index) in goal.tags"
                  :key="index"
                  type="primary"
                  size="medium"
                  closeable
                  @close="removeTag(index)"
                  class="apple-tag"
                >
                  {{ tag }}
                </van-tag>
              </div>
            </van-cell-group>
          </div>

          <!-- 预计工时 -->
          <div class="apple-card">
            <van-cell-group inset title="工时估算">
              <van-field
                v-model="goal.estimatedHours"
                name="estimatedHours"
                label="预计工时"
                placeholder="请输入预计工时"
                type="number"
                class="apple-field"
              >
                <template #extra>
                  小时
                </template>
              </van-field>
            </van-cell-group>
          </div>

          <!-- 备注 -->
          <div class="apple-card">
            <van-cell-group inset title="备注">
              <van-field
                v-model="goal.notes"
                name="notes"
                label="备注"
                type="textarea"
                placeholder="有什么需要补充的吗？"
                rows="2"
                autosize
                class="apple-field"
              />
            </van-cell-group>
          </div>

          <!-- 操作按钮 -->
          <div class="apple-card">
            <van-cell-group inset>
              <div class="apple-action-buttons">
                <van-button 
                  type="default" 
                  size="large" 
                  @click="handleReset"
                  class="apple-reset-button"
                >
                  重置
                </van-button>
                <van-button 
                  type="primary" 
                  size="large" 
                  @click="saveGoal"
                  :loading="saving"
                  class="apple-submit-button"
                >
                  创建目标
                </van-button>
              </div>
            </van-cell-group>
          </div>
        </van-form>
      </div>

      <!-- 日期选择器 -->
      <van-popup v-model:show="showDatePicker" position="bottom" round class="apple-popup">
        <van-date-picker
          v-model="selectedDate"
          @confirm="onDateConfirm"
          @cancel=" showDatePicker = false"
          title="选择截止日期"
        />
      </van-popup>

      <!-- 负责人选择器 -->
      <van-popup v-model:show="showOwnerPicker" position="bottom" round class="apple-popup">
        <van-picker
          :columns="owerOptions"
          @confirm="onOwnerConfirm"
          @cancel="showOwnerPicker = false"
          title="选择负责人"
        />
      </van-popup>

      <!-- 优先级选择器 -->
      <van-popup v-model:show="showLevelPicker" position="bottom" round class="apple-popup">
        <van-picker
          :columns="levelOptions"
          @confirm="onLevelConfirm"
          @cancel="showLevelPicker = false"
          title="选择优先级"
        />
      </van-popup>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { useUser } from '@/hooks/useUser'
import { showToast, showLoadingToast, closeToast, showSuccessToast, showDialog } from 'vant'
import { createGoal, postM, getM, isSuccess } from '@/utils/request'
import { useGoals } from '@/hooks'
import type { FormInstance } from 'vant'
// @ts-ignore
import MobileUpload from '@/components/MobileUpload.vue'

// 路由和状态管理
const router = useRouter()
const { user } = useUser()
const { createGoal } = useGoals()
const store = useStore()

// 表单引用
const formRef = ref<FormInstance>()

// 主题配置 - 使用默认的vant主题
const currentTheme = ref('light')

// 响应式数据
const saving = ref(false)
const showDatePicker = ref(false)
const showTimePicker = ref(false)
const showLevelPicker = ref(false)
const selectedDate = ref([
  `${new Date().getFullYear()}`,
  `${new Date().getMonth() + 1}`.padStart(2, '0'),
  `${new Date().getDate()}`.padStart(2, '0')
]) // 改为数组格式，Vant DatePicker 要求
const customTag = ref('')
const goalTagsText = ref('')
const owerOptions = ref<Array<{ label: string; value: string }>>([])
const showOwnerPicker = ref(false)

// 目标数据 - 迁移电脑端完整表单结构
const goal = reactive({
  title: '',
  description: '',
  deadline: null as number | null,
  owner: '',
  level: null as string | null,
  childGoals: [] as Array<{ message: string; finish: boolean; finishTime: string }>,
  tags: [] as string[],
  estimatedHours: null as number | null,
  fileList: [] as any[],
  notes: ''
})

// 优先级选项
const levelOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' }
]

// 表单验证规则
const formRules = {
  title: [{ required: true, message: '请输入目标标题', trigger: 'onBlur' }],
  owner: [{ required: true, message: '请选择负责人', trigger: 'onBlur' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'onBlur' }],
  level: [{ required: true, message: '请选择优先级', trigger: 'onBlur' }]
}

// 计算属性
const formattedDeadline = computed(() => {
  const date = new Date(goal.deadline)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return goal.deadline ? [`${year}`, `${month}`, `${day}`] : []
})

const levelLabel = computed(() => {
  const option = levelOptions.find(opt => opt.value === goal.level)
  return option ? option.label : ''
})

// 获取负责人列表
const getOwerList = async () => {
  try {
    const res = await getM('getOwerList')
    if (isSuccess(res)) {
      owerOptions.value = res.data.data.map((e: any) => ({
        text: e.text || e.label,
        value: e.value || e.id
      }))
    }
  } catch (error) {
    console.error('获取负责人列表失败:', error)
    showToast('获取负责人列表失败')
  }
}

// 日期处理
const onDateConfirm = (date: { getValues: () => Date[] }) => {
  // Vant DatePicker 返回的是包含日期数组的对象
  const selectedDates = date.getValues()
  if (selectedDates && selectedDates.length > 0) {
    goal.deadline = selectedDates[0].getTime()
  }
  showDatePicker.value = false
}

// 负责人选择
const onOwnerConfirm = (value: any, index: number) => {
  goal.owner = value.label || value
  showOwnerPicker.value = false
}

// 优先级选择
const onLevelConfirm = (value: any, index: number) => {
  goal.level = value.value || value
  showLevelPicker.value = false
}

const formatDate = (timestamp: number | null) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 标签管理
const addCustomTag = () => {
  if (goalTagsText.value.trim()) {
    const newTag = goalTagsText.value.trim()
    if (!goal.tags.includes(newTag)) {
      goal.tags.push(newTag)
    }
    goalTagsText.value = ''
  }
}

const removeTag = (index: number) => {
  goal.tags.splice(index, 1)
}

// 子目标管理 - 迁移电脑端格式
const addChildGoal = () => {
  goal.childGoals.push({
    message: '',
    finish: false,
    finishTime: ''
  })
}

const removeChildGoal = (index: number) => {
  goal.childGoals.splice(index, 1)
}

// 文件上传处理
const fileChange = (files: any[]) => {
  goal.fileList = files
}

const handleFileUploadError = (errorMsg: string) => {
  showToast(`文件上传失败: ${errorMsg}`)
}

// 表单验证
const validateGoal = async () => {
  try {
    await formRef.value?.validate()
    
    // 验证子目标
    if (goal.childGoals.length === 0) {
      showToast('请至少添加一个子目标')
      return false
    }
    
    const hasEmptyChildGoal = goal.childGoals.some(child => !child.message.trim())
    if (hasEmptyChildGoal) {
      showToast('请填写所有子目标内容')
      return false
    }
    
    return true
  } catch (error) {
    return false
  }
}

// 表单提交
const saveGoal = async () => {
  if (!await validateGoal()) {
    return
  }

  try {
    saving.value = true
    showLoadingToast('保存中...')
    
    // 格式化子目标数据
    const childGoalEndList = goal.childGoals.map(childGoal => ({
      message: childGoal.message,
      finish: false,
      finishTime: ''
    }))
    
    const goalData = {
      ...goal,
      childGoals: childGoalEndList,
      deadline: goal.deadline ? formatDate(goal.deadline) : null
    }
    
    const res = await postM('editGoal', goalData)
    if (isSuccess(res)) {
      showSuccessToast('目标创建成功')
      // 延迟返回，让用户看到成功提示
      setTimeout(() => {
        router.back()
      }, 1500)
    } else {
      showToast('保存失败，请重试')
    }
    
  } catch (error) {
    console.error('保存目标失败:', error)
    showToast('保存失败，请重试')
  } finally {
    saving.value = false
    closeToast()
  }
}

// 重置表单
const resetForm = () => {
  goal.title = ''
  goal.description = ''
  goal.deadline = null
  goal.owner = ''
  goal.level = null
  goal.childGoals = []
  goal.tags = []
  goal.estimatedHours = null
  goal.fileList = []
  goal.notes = ''
}

const handleReset = () => {
  showDialog({
    title: '确认重置',
    message: '确定要重置所有表单内容吗？',
    showCancelButton: true
  }).then(() => {
    resetForm()
    showToast('表单已重置')
  })
}

// 生命周期
onMounted(() => {
  // 设置默认截止日期为30天后
  const defaultDeadline = new Date()
  defaultDeadline.setDate(defaultDeadline.getDate() + 30)
  selectedDate.value = [
    `${defaultDeadline.getFullYear()}`,
    `${defaultDeadline.getMonth() + 1}`.padStart(2, '0'),
    `${defaultDeadline.getDate()}`.padStart(2, '0')
  ]
  goal.deadline = defaultDeadline.getTime()
  
  // 获取负责人列表
  getOwerList()
  
  // 设置默认负责人为当前用户
  if (store.state.user?.username) {
    goal.owner = store.state.user.username
  }
})
</script>

<style scoped>
/* 全局和根元素 */
.apple-style-provider {
  --van-background-color: #f7f8fa;
  --van-background-color-light: #ffffff;
  --van-text-color: #333;
  --van-text-color-2: #666;
  --van-border-color: #ebedf0;
}

.van-theme-dark .apple-style-provider {
  --van-background-color: #1c1c1e;
  --van-background-color-light: #2c2c2e;
  --van-text-color: #f5f5f7;
  --van-text-color-2: #a9a9b0;
  --van-border-color: #3a3a3c;
}

.mobile-add-goal {
  min-height: 100vh;
  background-color: var(--van-background-color);
  color: var(--van-text-color);
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  user-select: none;
}

/* 导航栏 - 优化移动端触摸区域 */
.apple-nav-bar {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--van-border-color);
  padding-top: env(safe-area-inset-top);
}

.van-theme-dark .apple-nav-bar {
  background-color: rgba(28, 28, 30, 0.7);
}

:deep(.van-nav-bar__title) {
  font-weight: 600;
  color: var(--van-text-color);
  font-size: 18px;
}

:deep(.van-nav-bar__left),
:deep(.van-nav-bar__right) {
  padding: 0 16px;
}

:deep(.van-nav-bar .van-button) {
  min-height: 44px;
  min-width: 44px;
}

/* 表单容器 - 响应式设计 */
.form-container {
  padding: 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
}

/* Apple 卡片样式 - 优化移动端显示 */
.apple-card {
  background-color: var(--van-background-color-light);
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.apple-card:active {
  transform: translateY(2px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.van-theme-dark .apple-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
}

:deep(.van-cell-group) {
  background: none;
}

:deep(.van-cell-group__title) {
  padding: 20px 20px 12px;
  font-size: 18px;
  font-weight: 700;
  color: var(--van-text-color);
}

.apple-field {
  --van-field-label-width: 100px;
  background-color: transparent;
  padding: 16px 20px;
}

:deep(.van-field__label) {
  color: var(--van-text-color-2);
  font-size: 16px;
  font-weight: 500;
}

:deep(.van-field__control) {
  font-size: 16px;
}

:deep(.van-field__body) {
  min-height: 48px;
}

/* 渐变按钮 - 优化触摸区域 */
.apple-gradient-button,
.apple-gradient-button-small {
  border: none;
  color: white;
  font-weight: 600;
  background: linear-gradient(135deg, #00c9a7, #00a98f);
  transition: all 0.2s ease;
  min-height: 44px;
  min-width: 44px;
  font-size: 16px;
}

.apple-gradient-button:active,
.apple-gradient-button-small:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.apple-gradient-button-small {
  padding: 0 20px;
  height: 36px;
  border-radius: 18px;
}

/* 子目标样式优化 */
.apple-subgoal-list {
  padding: 0 20px 12px;
}

.apple-subgoal-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px;
  background-color: var(--van-background-color);
  border-radius: 12px;
  transition: background-color 0.2s ease;
}

.apple-subgoal-item:active {
  background-color: var(--van-active-color);
}

.apple-subgoal-field {
  flex-grow: 1;
  background-color: transparent;
}

.apple-remove-icon {
  font-size: 22px;
  color: #ff4d4f;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.apple-remove-icon:active {
  background-color: rgba(255, 77, 79, 0.1);
}

.apple-add-button-container {
  text-align: center;
  padding: 16px 0;
}

.apple-empty-placeholder {
  padding: 30px 20px;
}

/* 标签容器 */
.apple-tags-container {
  padding: 0 20px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.apple-tag {
  margin: 4px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

/* 操作按钮组 */
.apple-action-buttons {
  display: flex;
  gap: 16px;
  padding: 20px;
}

.apple-reset-button,
.apple-submit-button {
  flex: 1;
  height: 52px;
  border-radius: 26px;
  font-size: 17px;
  font-weight: 600;
}

.apple-reset-button {
  border: 2px solid var(--van-border-color);
  background: transparent;
}

/* 弹出层优化 */
.apple-popup {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px 24px 0 0;
}

.van-theme-dark .apple-popup {
  background-color: rgba(44, 44, 46, 0.95);
}

:deep(.van-picker__toolbar) {
  background-color: transparent;
  height: 60px;
  padding: 0 20px;
}

:deep(.van-picker__title) {
  font-weight: 700;
  color: var(--van-text-color);
  font-size: 18px;
}

:deep(.van-picker__confirm),
:deep(.van-picker__cancel) {
  font-size: 17px;
  font-weight: 600;
}

:deep(.van-picker-column__item) {
  font-size: 18px;
  padding: 16px 0;
}

/* 移动端优化 - 防止输入框被键盘遮挡 */
@media (max-height: 600px) {
  .form-container {
    padding-bottom: 300px;
  }
}

/* 横屏适配 */
@media (orientation: landscape) {
  .form-container {
    max-width: 600px;
    margin: 0 auto;
  }
  
  .apple-action-buttons {
    max-width: 400px;
    margin: 0 auto;
  }
}

/* 大屏设备适配 */
@media (min-width: 768px) {
  .form-container {
    max-width: 600px;
    margin: 0 auto;
  }
}

/* 手势优化 - 滑动关闭支持 */
.apple-popup {
  overflow: hidden;
}

:deep(.van-popup) {
  touch-action: pan-y;
}

/* 输入优化 - 移动端键盘类型 */
:deep(input[type="number"]) {
  -webkit-appearance: none;
  -moz-appearance: textfield;
}

:deep(input[type="number"])::-webkit-outer-spin-button,
:deep(input[type="number"])::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* 加载状态优化 */
:deep(.van-loading__spinner) {
  width: 24px;
  height: 24px;
}
</style>