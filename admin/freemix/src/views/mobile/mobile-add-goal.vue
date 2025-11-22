<template>
  <van-config-provider :theme="currentTheme">
    <div class="mobile-add-goal">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="添加目标"
        left-text="返回"
        left-arrow
        @click-left="goBack"
        right-text="保存"
        @click-right="saveGoal"
      />

      <!-- 表单内容 -->
      <div class="form-container">
        <van-form @submit="saveGoal">
          <!-- 基本信息 -->
          <van-cell-group inset title="基本信息">
            <van-field
              v-model="goal.title"
              name="title"
              label="目标标题"
              placeholder="请输入目标标题"
              :rules="[{ required: true, message: '请输入目标标题' }]"
              required
            />
            
            <van-field
              v-model="goal.description"
              name="description"
              label="目标描述"
              type="textarea"
              placeholder="请详细描述您的目标"
              rows="3"
              autosize
            />
          </van-cell-group>

          <!-- 时间设置 -->
          <van-cell-group inset title="时间设置">
            <van-field
              v-model="goal.deadline"
              name="deadline"
              label="截止日期"
              placeholder="请选择截止日期"
              readonly
              is-link
              @click="showDatePicker = true"
              :rules="[{ required: true, message: '请选择截止日期' }]"
              required
            />
          </van-cell-group>

          <!-- 责任人 -->
          <van-cell-group inset title="责任人">
            <van-field
              v-model="goal.owner"
              name="owner"
              label="负责人"
              placeholder="请输入负责人姓名"
              :rules="[{ required: true, message: '请输入负责人姓名' }]"
              required
            />
          </van-cell-group>

          <!-- 优先级 -->
          <van-cell-group inset title="优先级">
            <van-field name="priority" label="优先级">
              <template #input>
                <van-radio-group v-model="goal.priority" direction="horizontal">
                  <van-radio name="low">低</van-radio>
                  <van-radio name="medium">中</van-radio>
                  <van-radio name="high">高</van-radio>
                  <van-radio name="urgent">紧急</van-radio>
                </van-radio-group>
              </template>
            </van-field>
          </van-cell-group>

          <!-- 标签 -->
          <van-cell-group inset title="标签">
            <van-field name="tags" label="标签">
              <template #input>
                <van-checkbox-group v-model="goal.tags" direction="horizontal">
                  <van-checkbox name="学习">学习</van-checkbox>
                  <van-checkbox name="工作">工作</van-checkbox>
                  <van-checkbox name="生活">生活</van-checkbox>
                  <van-checkbox name="运动">运动</van-checkbox>
                </van-checkbox-group>
              </template>
            </van-field>
            
            <van-field
              v-model="customTag"
              placeholder="自定义标签"
              clearable
            >
              <template #button>
                <van-button size="small" type="primary" @click="addCustomTag">
                  添加
                </van-button>
              </template>
            </van-field>
          </van-cell-group>

          <!-- 子目标 -->
          <van-cell-group inset title="子目标">
            <div v-if="goal.childGoals.length === 0" class="empty-child-goals">
              <van-empty description="暂无子目标" image-size="60">
                <van-button type="primary" size="small" @click="addChildGoal">
                  添加子目标
                </van-button>
              </van-empty>
            </div>
            
            <div v-else>
              <div v-for="(childGoal, index) in goal.childGoals" :key="childGoal.id">
                <van-swipe-cell>
                  <van-cell>
                    <template #title>
                      <div class="child-goal-item">
                        <van-icon name="bars" class="drag-handle" />
                        <van-field
                          v-model="childGoal.title"
                          placeholder="子目标标题"
                          border="none"
                          input-align="center"
                        />
                      </div>
                    </template>
                    <template #right-icon>
                      <van-icon 
                        name="cross" 
                        @click="removeChildGoal(index)"
                        class="remove-icon"
                      />
                    </template>
                  </van-cell>
                  <template #right>
                    <van-button 
                      square 
                      type="danger" 
                      text="删除" 
                      @click="removeChildGoal(index)"
                    />
                  </template>
                </van-swipe-cell>
              </div>
              
              <van-button 
                type="primary" 
                size="small" 
                @click="addChildGoal"
                style="margin: 8px 16px;"
              >
                添加子目标
              </van-button>
            </div>
          </van-cell-group>

          <!-- 备注 -->
          <van-cell-group inset title="备注">
            <van-field
              v-model="goal.notes"
              name="notes"
              label="备注"
              type="textarea"
              placeholder="添加备注信息..."
              rows="3"
              autosize
            />
          </van-cell-group>
        </van-form>
      </div>

      <!-- 底部操作按钮 -->
      <div class="bottom-actions">
        <van-button 
          type="primary" 
          block 
          round 
          @click="saveGoal"
          :loading="saving"
        >
          保存目标
        </van-button>
      </div>

      <!-- 日期选择器 -->
      <van-popup v-model:show="showDatePicker" position="bottom">
        <van-date-picker
          v-model="selectedDate"
          @confirm="onDateConfirm"
          @cancel="showDatePicker = false"
          :min-date="minDate"
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
.mobile-add-goal {
  min-height: 100vh;
  background-color: var(--van-background-color);
  padding-bottom: 80px;
}

.form-container {
  padding: 12px 0;
}

.empty-child-goals {
  padding: 20px;
  text-align: center;
}

.child-goal-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.drag-handle {
  cursor: grab;
  color: var(--van-text-color-2);
}

.drag-handle:active {
  cursor: grabbing;
}

.remove-icon {
  color: var(--van-danger-color);
  cursor: pointer;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background-color: var(--van-background-color);
  border-top: 1px solid var(--van-border-color);
  z-index: 100;
}

/* 深色主题适配 */
:deep(.van-theme-dark) {
  --van-background-color: #1a1a1a;
  --van-background-color-light: #2a2a2a;
  --van-text-color: #f5f5f5;
  --van-text-color-2: #a0a0a0;
}

:deep(.van-cell-group) {
  background-color: var(--van-background-color-light);
}

:deep(.van-cell) {
  background-color: var(--van-background-color-light);
}

:deep(.van-field__control) {
  color: var(--van-text-color);
}

:deep(.van-swipe-cell__wrapper) {
  background-color: var(--van-background-color-light);
}
</style>