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
                placeholder="给目标起个名字"
                :rules="[{ required: true, message: '请输入目标标题' }]"
                required
                class="apple-field"
              />
              <van-field
                v-model="goal.description"
                name="description"
                label="目标描述"
                type="textarea"
                placeholder="详细描述一下你的目标"
                rows="3"
                autosize
                class="apple-field"
              />
            </van-cell-group>
          </div>

          <!-- 时间与负责人 -->
          <div class="apple-card">
            <van-cell-group inset title="时间与负责人">
              <van-field
                v-model="goal.deadline"
                name="deadline"
                label="截止日期"
                placeholder="选择一个截止日期"
                readonly
                is-link
                @click="showDatePicker = true"
                :rules="[{ required: true, message: '请选择截止日期' }]"
                required
                class="apple-field"
              />
              <van-field
                v-model="goal.owner"
                name="owner"
                label="负责人"
                placeholder="指定一个负责人"
                :rules="[{ required: true, message: '请输入负责人姓名' }]"
                required
                class="apple-field"
              />
            </van-cell-group>
          </div>

          <!-- 优先级 -->
          <div class="apple-card">
            <van-cell-group inset title="优先级">
              <van-field name="priority" label="优先级" class="apple-field">
                <template #input>
                  <van-radio-group v-model="goal.priority" direction="horizontal" class="apple-radio-group">
                    <van-radio name="low">低</van-radio>
                    <van-radio name="medium">中</van-radio>
                    <van-radio name="high">高</van-radio>
                    <van-radio name="urgent">紧急</van-radio>
                  </van-radio-group>
                </template>
              </van-field>
            </van-cell-group>
          </div>

          <!-- 标签 -->
          <div class="apple-card">
            <van-cell-group inset title="标签">
              <van-field name="tags" label="标签" class="apple-field">
                <template #input>
                  <van-checkbox-group v-model="goal.tags" direction="horizontal" class="apple-checkbox-group">
                    <van-checkbox name="学习" shape="square">学习</van-checkbox>
                    <van-checkbox name="工作" shape="square">工作</van-checkbox>
                    <van-checkbox name="生活" shape="square">生活</van-checkbox>
                    <van-checkbox name="运动" shape="square">运动</van-checkbox>
                  </van-checkbox-group>
                </template>
              </van-field>
              <van-field
                v-model="customTag"
                placeholder="添加自定义标签"
                clearable
                class="apple-field"
              >
                <template #button>
                  <van-button size="small" class="apple-gradient-button-small" @click="addCustomTag">
                    添加
                  </van-button>
                </template>
              </van-field>
            </van-cell-group>
          </div>

          <!-- 子目标 -->
          <div class="apple-card">
            <van-cell-group inset title="子目标">
              <div v-if="goal.childGoals.length === 0" class="apple-empty-placeholder">
                <van-empty description="暂无子目标" image-size="60" />
              </div>
              <div v-else class="apple-subgoal-list">
                <div v-for="(childGoal, index) in goal.childGoals" :key="childGoal.id" class="apple-subgoal-item">
                  <van-field
                    v-model="childGoal.title"
                    placeholder="子目标标题"
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

          <!-- 备注 -->
          <div class="apple-card">
            <van-cell-group inset title="备注">
              <van-field
                v-model="goal.notes"
                name="notes"
                label="备注"
                type="textarea"
                placeholder="有什么需要补充的吗？"
                rows="3"
                autosize
                class="apple-field"
              />
            </van-cell-group>
          </div>
        </van-form>
      </div>

      <!-- 日期选择器 -->
      <van-popup v-model:show="showDatePicker" position="bottom" round class="apple-popup">
        <van-date-picker
          v-model="selectedDate"
          @confirm="onDateConfirm"
          @cancel="showDatePicker = false"
          :min-date="minDate"
          title="选择截止日期"
        />
      </van-popup>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { useUser } from '@/hooks/useUser'
import { showToast, showLoadingToast, closeToast, showSuccessToast } from 'vant'
import { createGoal } from '@/utils/request'
import { useGoals } from '@/hooks'

// 路由和状态管理
const router = useRouter()
const { user } = useUser()
const { createGoal } = useGoals()

// 主题配置 - 使用默认的vant主题
const currentTheme = ref('light')
const store = useStore()
// 响应式数据
const saving = ref(false)
const showDatePicker = ref(false)
const selectedDate = ref(new Date())
const customTag = ref('')
const minDate = new Date()

// 目标数据
const goal = reactive({
  title: '',
  description: '',
  deadline: '',
  owner: store.state.user.username || '',
  priority: 'medium',
  tags: [],
  childGoals: [],
  notes: '',
  status: 'in-progress',
  progress: 0
})

// 方法
const goBack = () => {
  router.back()
}

const onDateConfirm = (date) => {
  goal.deadline = formatDate(date)
  showDatePicker.value = false
}

const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const addCustomTag = () => {
  if (customTag.value.trim() && !goal.tags.includes(customTag.value.trim())) {
    goal.tags.push(customTag.value.trim())
    customTag.value = ''
  }
}

const addChildGoal = () => {
  goal.childGoals.push({
    id: Date.now().toString(),
    title: '',
    completed: false
  })
}

const removeChildGoal = (index) => {
  goal.childGoals.splice(index, 1)
}

const validateGoal = () => {
  if (!goal.title.trim()) {
    showToast('请输入目标标题')
    return false
  }
  
  if (!goal.deadline) {
    showToast('请选择截止日期')
    return false
  }
  
  if (!goal.owner.trim()) {
    showToast('请输入负责人姓名')
    return false
  }
  
  // 验证子目标
  const hasEmptyChildGoal = goal.childGoals.some(child => !child.title.trim())
  if (hasEmptyChildGoal) {
    showToast('请填写所有子目标标题')
    return false
  }
  
  return true
}

const saveGoal = async () => {
  if (!validateGoal()) {
    return
  }

  try {
    saving.value = true
    showLoadingToast('保存中...')
    
    const goalData = {
      ...goal,
      createdBy: user.value?.id,
      createdAt: new Date().toISOString()
    }
    
    await createGoal(goalData)
    
    showSuccessToast('目标创建成功')
    router.back()
    
  } catch (error) {
    showToast('保存失败，请重试')
    console.error('保存目标失败:', error)
  } finally {
    saving.value = false
    closeToast()
  }
}

// 生命周期
onMounted(() => {
  // 设置默认截止日期为30天后
  const defaultDeadline = new Date()
  defaultDeadline.setDate(defaultDeadline.getDate() + 30)
  selectedDate.value = defaultDeadline
  goal.deadline = formatDate(defaultDeadline)
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
}

/* 导航栏 */
.apple-nav-bar {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--van-border-color);
}

.van-theme-dark .apple-nav-bar {
  background-color: rgba(28, 28, 30, 0.7);
}

:deep(.van-nav-bar__title) {
  font-weight: 600;
  color: var(--van-text-color);
}

/* 表单容器 */
.form-container {
  padding: 16px;
}

/* Apple 卡片样式 */
.apple-card {
  background-color: var(--van-background-color-light);
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.van-theme-dark .apple-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

:deep(.van-cell-group) {
  background: none;
}

:deep(.van-cell-group__title) {
  padding: 12px 16px 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--van-text-color);
}

.apple-field {
  --van-field-label-width: 80px;
  background-color: transparent;
}

:deep(.van-field__label) {
  color: var(--van-text-color-2);
}

/* 控件组 */
.apple-radio-group,
.apple-checkbox-group {
  gap: 16px;
}

/* 渐变按钮 */
.apple-gradient-button,
.apple-gradient-button-small {
  border: none;
  color: white;
  font-weight: 500;
  background: linear-gradient(135deg, #00c9a7, #00a98f);
  transition: all 0.3s ease;
}

.apple-gradient-button:active,
.apple-gradient-button-small:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.apple-gradient-button-small {
  padding: 0 12px;
  height: 30px;
}

/* 子目标 */
.apple-subgoal-list {
  padding: 8px 16px;
}

.apple-subgoal-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.apple-subgoal-field {
  flex-grow: 1;
  background-color: var(--van-background-color);
  border-radius: 8px;
}

.apple-remove-icon {
  font-size: 20px;
  color: #ff4d4f;
  cursor: pointer;
}

.apple-add-button-container {
  text-align: center;
  padding: 8px 0;
}

.apple-empty-placeholder {
  padding: 20px;
}

/* 弹出层 */
.apple-popup {
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(15px);
}

.van-theme-dark .apple-popup {
  background-color: rgba(44, 44, 46, 0.8);
}

:deep(.van-picker__toolbar) {
  background-color: transparent;
}

:deep(.van-picker__title) {
  font-weight: 600;
  color: var(--van-text-color);
}
</style>