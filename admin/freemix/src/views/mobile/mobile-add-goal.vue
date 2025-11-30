<template>
  <!-- 强制使用 dark 主题，确保 Vant 组件内部组件（如 Picker）变黑 -->
  <van-config-provider theme="dark">
    <div class="freemix-page dark-mode">
      
      <!-- 顶部背景装饰 (保持绿色渐变，但在暗黑下更显眼) -->
      <div class="header-bg"></div>

      <!-- 顶部导航栏 -->
      <van-nav-bar
        fixed
        placeholder
        :border="false"
        class="custom-nav"
        z-index="100"
        :safe-area-inset-top="true"
      >
        <template #left>
          <div class="nav-btn-text white" @click="router.back()">
            <van-icon name="arrow-left" /> 
          </div>
        </template>
        <template #title>
          <span class="nav-title white">新建目标</span>
        </template>
        <template #right>
          <van-loading v-if="saving" type="spinner" size="20" color="#fff" />
          <div v-else class="nav-btn-text white bold" @click="saveGoal">保存</div>
        </template>
      </van-nav-bar>

      <!-- 主要内容区域 -->
      <div class="content-wrapper">
        <van-form @submit="saveGoal" ref="formRef" scroll-to-error>
          
          <!-- 核心卡片：标题与描述 -->
          <div class="main-card">
            <div class="card-header">
              <van-icon name="flag-o" class="header-icon" />
              <span>核心目标</span>
            </div>
            <van-field
              v-model="goal.title"
              name="title"
              placeholder="定个小目标 (例如: 学习 Vue3)"
              :rules="formRules.title"
              class="freemix-input title-input"
              maxlength="30"
              :border="false"
            />
            <div class="divider"></div>
            <van-field
              v-model="goal.description"
              name="description"
              type="textarea"
              placeholder="添加详细描述，让目标更清晰..."
              rows="3"
              autosize
              class="freemix-input desc-input"
              :border="false"
            />
          </div>

          <!-- 属性设置卡片 -->
          <div class="info-card">
            <div class="card-title">属性设置</div>
            <div class="grid-fields">
              <van-field
                v-model="goal.owner"
                label="负责人"
                placeholder="点击选择"
                readonly
                is-link
                input-align="right"
                @click="showOwnerPicker = true"
                class="grid-field"
                :border="false"
              >
                <template #left-icon><van-icon name="manager-o" color="#00Cba5" /></template>
              </van-field>

              <van-field
                v-model="formattedDeadlineStr"
                label="截止日期"
                placeholder="点击选择"
                readonly
                is-link
                input-align="right"
                @click="showDatePicker = true"
                class="grid-field"
                :border="false"
              >
                 <template #left-icon><van-icon name="calender-o" color="#FF9F43" /></template>
              </van-field>

              <van-field
                v-model="levelLabel"
                label="优先级"
                placeholder="点击选择"
                readonly
                is-link
                input-align="right"
                @click="showLevelPicker = true"
                class="grid-field"
                :border="false"
              >
                <template #left-icon><van-icon name="fire-o" color="#FF6B6B" /></template>
                <template #right-icon>
                  <van-tag :color="getLevelColor(goal.level)" v-if="goal.level" round size="medium">{{ levelLabel }}</van-tag>
                  <van-icon name="arrow" class="van-badge__wrapper" style="margin-left: 4px; color: #555;" />
                </template>
              </van-field>

               <van-field
                v-model="goal.estimatedHours"
                label="预计工时"
                placeholder="0"
                type="number"
                input-align="right"
                class="grid-field"
                :border="false"
              >
                <template #left-icon><van-icon name="clock-o" color="#54A0FF" /></template>
                <template #extra>
                  <span class="suffix-text">小时</span>
                </template>
              </van-field>
            </div>
          </div>

          <!-- 子任务清单 -->
          <div class="info-card">
            <div class="section-header-row">
              <div class="card-title">子任务清单 <span class="count-badge" v-if="goal.childGoals.length">{{goal.childGoals.length}}</span></div>
              <div class="header-action" @click="addChildGoal">
                <van-icon name="plus" /> 添加任务
              </div>
            </div>
            
            <div class="subgoal-container">
              <transition-group name="list">
                <div v-for="(childGoal, index) in goal.childGoals" :key="index" class="subgoal-item">
                  <div class="subgoal-index">{{ index + 1 }}</div>
                  <div class="subgoal-content">
                    <van-field
                      v-model="childGoal.message"
                      :placeholder="`输入第 ${index + 1} 步计划`"
                      :border="false"
                      class="subgoal-input"
                    />
                  </div>
                  <div class="subgoal-del" @click="removeChildGoal(index)">
                    <van-icon name="delete-o" />
                  </div>
                </div>
              </transition-group>
              
              <div v-if="goal.childGoals.length === 0" class="empty-placeholder" @click="addChildGoal">
                <!-- 这里的图片建议替换为暗色系占位图，此处用 CSS 滤镜简单处理 -->
                <img src="https://fastly.jsdelivr.net/npm/@vant/assets/custom-empty-image.png" alt="empty" class="dark-invert" />
                <p>拆解目标是成功的第一步</p>
                <van-button size="small" round color="rgba(0, 203, 165, 0.2)" class="empty-btn">添加子任务</van-button>
              </div>
            </div>
          </div>

          <!-- 附件与标签 -->
          <div class="info-card">
            <div class="card-title">更多信息</div>
            <van-cell-group :border="false" style="background: transparent;">
              <div class="upload-section">
                <div class="sub-label">附件上传</div>
                <MobileUpload
                  @uploadSuccess="fileChange"
                  @fileRemove="fileChange"
                  @uploadError="handleFileUploadError"
                  :fileList="goal.fileList"
                />
              </div>
              
              <div class="tag-section">
                <div class="sub-label">标签</div>
                <div class="tag-input-row">
                   <van-field
                    v-model="goalTagsText"
                    placeholder="输入标签后点击添加"
                    class="freemix-input tag-field"
                    :border="false"
                    @keyup.enter="addCustomTag"
                  >
                    <template #button>
                      <van-button 
                        size="small" 
                        color="#00Cba5"
                        @click="addCustomTag"
                        :disabled="!goalTagsText"
                        round
                      >
                        添加
                      </van-button>
                    </template>
                  </van-field>
                </div>
                
                <div v-if="goal.tags.length > 0" class="tags-wrapper">
                  <van-tag
                    v-for="(tag, index) in goal.tags"
                    :key="index"
                    closeable
                    size="medium"
                    color="#2C2C2E"
                    text-color="#00Cba5"
                    @close="removeTag(index)"
                    class="custom-tag"
                  >
                    # {{ tag }}
                  </van-tag>
                </div>
              </div>
              
              <div class="notes-section">
                 <div class="sub-label">备注</div>
                 <van-field
                  v-model="goal.notes"
                  type="textarea"
                  placeholder="其他需要注意的事项..."
                  rows="2"
                  autosize
                  class="freemix-input note-input"
                  :border="false"
                />
              </div>
            </van-cell-group>
          </div>

          <!-- 底部占位 -->
          <div class="bottom-spacer"></div>

        </van-form>
      </div>

      <!-- 底部悬浮按钮栏 -->
      <div class="bottom-action-bar">
        <div class="action-btn secondary" @click="handleReset">
          <van-icon name="replay" /> 重置
        </div>
        <div class="action-btn primary" @click="saveGoal">
          <van-loading v-if="saving" type="spinner" size="20" color="#fff" />
          <span v-else>立即创建目标</span>
        </div>
      </div>

      <!-- 弹窗组件区 -->
      <van-popup v-model:show="showDatePicker" position="bottom" round>
        <van-date-picker
          v-model="selectedDate"
          title="选择截止日期"
          :min-date="new Date()"
          @confirm="onDateConfirm"
          @cancel="showDatePicker = false"
          confirm-button-text="确定"
          cancel-button-text="取消"
        />
      </van-popup>

      <van-popup v-model:show="showOwnerPicker" position="bottom" round>
        <van-picker
          title="选择负责人"
          :columns="owerOptions"
          @confirm="onOwnerConfirm"
          @cancel="showOwnerPicker = false"
        />
      </van-popup>

      <van-popup v-model:show="showLevelPicker" position="bottom" round>
        <van-picker
          title="目标优先级"
          :columns="levelOptions"
          @confirm="onLevelConfirm"
          @cancel="showLevelPicker = false"
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
import { showToast, showLoadingToast, closeToast, showSuccessToast, showDialog } from 'vant'
import { getM, postM, isSuccess } from '@/utils/request'
import type { FormInstance } from 'vant'
// @ts-ignore
import MobileUpload from '@/components/MobileUpload.vue'

// 路由和状态
const router = useRouter()
const store = useStore()
const { user } = useUser()

// 主题与表单引用
const formRef = ref<FormInstance>()

// UI 状态控制
const saving = ref(false)
const showDatePicker = ref(false)
const showOwnerPicker = ref(false)
const showLevelPicker = ref(false)

// 临时状态
const goalTagsText = ref('')
const selectedDate = ref<string[]>([]) 
const owerOptions = ref<Array<{ text: string; value: string }>>([])

// 数据模型
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

// 常量定义
const levelOptions = [
  { text: '低优先级', value: 'low' },
  { text: '中优先级', value: 'medium' },
  { text: '高优先级', value: 'high' },
  { text: '紧急', value: 'urgent' }
]

const formRules = {
  title: [{ required: true, message: '请输入目标标题', trigger: 'onBlur' }],
}

// 计算属性
const formattedDeadlineStr = computed(() => {
  return goal.deadline ? formatDate(goal.deadline) : ''
})

const levelLabel = computed(() => {
  const option = levelOptions.find(opt => opt.value === goal.level)
  return option ? option.text : ''
})

// 辅助函数
const formatDate = (timestamp: number | null) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const getLevelColor = (level: string | null) => {
  if(level === 'urgent') return '#FF6B6B'
  if(level === 'high') return '#FF9F43'
  if(level === 'medium') return '#54A0FF'
  return '#1DD1A1' 
}

// 业务逻辑
const getOwerList = async () => {
  try {
    const res = await getM('getOwerList')
    if (isSuccess(res)) {
      owerOptions.value = res.data.data.map((e: any) => ({
        text: e.text || e.label || e.username || e.name,
        value: e.value || e.id || e.username
      }))
    }
  } catch (error) {
    console.error('获取负责人失败', error)
  }
}

// 事件处理
const onDateConfirm = ({ selectedValues }: { selectedValues: string[] }) => {
  const [year, month, day] = selectedValues
  goal.deadline = new Date(`${year}-${month}-${day}`).getTime()
  showDatePicker.value = false
}

const onOwnerConfirm = ({ selectedOptions }: any) => {
  goal.owner = selectedOptions[0]?.value
  showOwnerPicker.value = false
}

const onLevelConfirm = ({ selectedOptions }: any) => {
  goal.level = selectedOptions[0]?.value
  showLevelPicker.value = false
}

// 标签操作
const addCustomTag = () => {
  const val = goalTagsText.value.trim()
  if (val && !goal.tags.includes(val)) {
    goal.tags.push(val)
  }
  goalTagsText.value = ''
}

const removeTag = (index: number) => {
  goal.tags.splice(index, 1)
}

// 子目标操作
const addChildGoal = () => {
  goal.childGoals.push({ message: '', finish: false, finishTime: '' })
}

const removeChildGoal = (index: number) => {
  goal.childGoals.splice(index, 1)
}

// 文件回调
const fileChange = (files: any[]) => {
  goal.fileList = files
}
const handleFileUploadError = (msg: string) => showToast(msg)

// 表单提交
const validateData = () => {
  if(!goal.owner) {
    showToast('请选择负责人')
    return false
  }
  if(!goal.deadline) {
    showToast('请选择截止日期')
    return false
  }
  if(goal.childGoals.length > 0 && goal.childGoals.some(c => !c.message.trim())) {
    showToast('请完善子任务内容')
    return false
  }
  return true
}

const saveGoal = async () => {
  try {
    await formRef.value?.validate()
    if(!validateData()) return

    saving.value = true
    showLoadingToast({ message: '创建中...', forbidClick: true })

    const payload = {
      ...goal,
      deadline: formatDate(goal.deadline) // 保持原有格式化逻辑
    }

    const res = await postM('editGoal', payload)
    
    if (isSuccess(res)) {
      showSuccessToast('目标已创建')
      setTimeout(() => router.back(), 1000)
    } else {
      showToast(res.msg || '保存失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
    closeToast()
  }
}

const handleReset = () => {
  showDialog({
    title: '重置',
    message: '确定清空当前所有输入吗？',
    showCancelButton: true,
    confirmButtonColor: '#00Cba5',
    theme: 'round-button', // 使用 Vant 的圆角按钮主题
  }).then(() => {
    Object.assign(goal, {
      title: '',
      description: '',
      deadline: null,
      owner: store.state.user?.username || '',
      level: null,
      childGoals: [],
      tags: [],
      estimatedHours: null,
      fileList: [],
      notes: ''
    })
    initDate()
  })
}

const initDate = () => {
  const d = new Date()
  d.setDate(d.getDate() + 7)
  goal.deadline = d.getTime()
  selectedDate.value = [
    String(d.getFullYear()),
    String(d.getMonth() + 1).padStart(2, '0'),
    String(d.getDate()).padStart(2, '0')
  ]
}

// 生命周期
onMounted(() => {
  initDate()
  getOwerList()
  if (store.state.user?.username) {
    goal.owner = store.state.user.username
  }
  // 设置文档根节点主题为 dark
  document.documentElement.setAttribute('data-theme', 'dark')
})
</script>

<style scoped lang="scss">
/* --- 纯暗黑模式变量 --- */
$theme-green: #00Cba5;
$theme-green-dark: #008f72;
$bg-color: #050505; /* 页面深黑背景 */
$card-bg: #1C1C1E; /* 卡片背景 */
$field-bg: #2C2C2E; /* 输入框/列表项背景 */
$text-main: #FFFFFF; /* 主文字白色 */
$text-sub: #8E8E93; /* 辅助文字灰色 */
$border-color: #2C2C2E;
$card-radius: 16px;

.freemix-page {
  min-height: 100vh;
  background-color: $bg-color;
  position: relative;
  font-family: 'PingFang SC', 'Helvetica Neue', Helvetica, sans-serif;
  padding-bottom: 20px;
  color: $text-main;
}

/* --- 头部区域 --- */
.header-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 220px;
  /* 调整渐变让其在暗黑模式下不刺眼但有氛围 */
  background: linear-gradient(135deg, rgba(0, 210, 180, 0.4) 0%, rgba(0, 203, 165, 0.1) 100%);
  mask-image: linear-gradient(to bottom, black 0%, transparent 100%);
  -webkit-mask-image: linear-gradient(to bottom, black 0%, transparent 100%);
  z-index: 0;
}

.custom-nav {
  background: transparent !important;
  
  .nav-title {
    font-size: 18px;
    font-weight: 600;
  }
  
  .white {
    color: #FFFFFF !important;
  }
  
  .bold {
    font-weight: 600;
    font-size: 16px;
    background: rgba(255, 255, 255, 0.1);
    padding: 4px 12px;
    border-radius: 20px;
    backdrop-filter: blur(5px);
  }
}

/* --- 内容区域 --- */
.content-wrapper {
  position: relative;
  z-index: 10;
  padding: 12px 16px;
}

/* 卡片通用样式 */
.main-card, .info-card {
  background: $card-bg;
  border-radius: $card-radius;
  /* 暗黑模式阴影更深更虚 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  margin-bottom: 16px;
  overflow: hidden;
  transition: transform 0.2s;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.main-card {
  margin-top: 50px;
  padding: 24px 20px;
  text-align: center;
  
  .card-header {
    display: flex;
    justify-content: center;
    align-items: center;
    color: $theme-green;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
    
    .header-icon {
      font-size: 18px;
      margin-right: 6px;
    }
  }

  .title-input {
    padding: 10px 0;
    background: transparent;
    
    :deep(.van-field__control) {
      font-size: 22px;
      font-weight: bold;
      text-align: center;
      color: $text-main !important; /* 强制白色 */
      
      &::placeholder {
        color: #555555;
        font-weight: 400;
      }
    }
  }

  .divider {
    height: 1px;
    background: $border-color;
    margin: 10px 40px;
  }

  .desc-input {
    background: transparent;
    :deep(.van-field__control) {
      text-align: center;
      color: $text-sub !important;
      font-size: 15px;
    }
  }
}

.info-card {
  padding: 20px;
  
  .card-title {
    font-size: 16px;
    font-weight: 700;
    color: $text-main;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      display: inline-block;
      width: 4px;
      height: 16px;
      background: $theme-green;
      border-radius: 2px;
      margin-right: 8px;
    }
  }
}

/* 属性网格 */
.grid-fields {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.grid-field {
  background: $field-bg;
  border-radius: 12px;
  padding: 14px 12px;
  align-items: center;
  
  :deep(.van-field__label) {
    color: $text-sub;
    font-weight: 500;
  }
  
  /* 修复属性输入框文字颜色为白 */
  :deep(.van-field__control) {
    font-weight: 500;
    color: $text-main !important;
    &::placeholder { color: #555; }
  }
}

/* 子任务部分 */
.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  
  .count-badge {
    background: rgba(0, 203, 165, 0.2);
    color: $theme-green;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 10px;
    margin-left: 6px;
  }

  .header-action {
    color: $theme-green;
    font-size: 14px;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
  }
}

.subgoal-item {
  display: flex;
  align-items: center;
  background: $field-bg;
  border-radius: 10px;
  margin-bottom: 10px;
  padding: 8px 12px;
  
  .subgoal-index {
    width: 20px;
    height: 20px;
    background: $theme-green;
    color: #000;
    border-radius: 50%;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 10px;
    flex-shrink: 0;
    font-weight: bold;
  }
  
  .subgoal-content {
    flex: 1;
    .subgoal-input {
      background: transparent;
      padding: 0;
      
      /* 修复子目标输入框文字颜色为白 */
      :deep(.van-field__control) {
        color: $text-main !important; 
        &::placeholder { color: #666; }
      }
    }
  }
  
  .subgoal-del {
    color: #FF453A; /* iOS Dark Mode Red */
    padding: 5px;
    font-size: 18px;
  }
}

.empty-placeholder {
  text-align: center;
  padding: 20px 0;
  
  .dark-invert {
    width: 80px;
    margin-bottom: 10px;
    opacity: 0.3;
    filter: invert(1) grayscale(1); /* 反转颜色使其在黑背景下合适 */
  }
  p {
    color: $text-sub;
    font-size: 13px;
    margin-bottom: 12px;
  }
  .empty-btn {
    color: $theme-green !important;
    border: 1px solid rgba(0, 203, 165, 0.3);
    font-weight: 600;
  }
}

/* 附件与标签 */
.sub-label {
  font-size: 14px;
  color: $text-sub;
  margin-bottom: 8px;
  margin-top: 16px;
  &:first-child { margin-top: 0; }
}

.tag-input-row {
  margin-bottom: 12px;
  .tag-field {
    background: $field-bg;
    border-radius: 20px;
    padding: 4px 4px 4px 16px;
    
    /* 修复标签输入框文字颜色为白 */
    :deep(.van-field__control) {
      color: $text-main !important;
      &::placeholder { color: #666; }
    }
  }
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.custom-tag {
  /* 覆盖 tag 内部样式 */
  border: 1px solid rgba(0, 203, 165, 0.3);
}

.note-input {
  background: $field-bg;
  border-radius: 12px;
  padding: 12px;
  
  /* 修复备注输入框文字颜色为白 */
  :deep(.van-field__control) {
    color: $text-main !important;
    min-height: 60px;
    &::placeholder { color: #666; }
  }
}

/* --- 底部悬浮操作栏 --- */
.bottom-spacer {
  height: 80px;
}

.bottom-action-bar {
  position: fixed;
  bottom: 20px;
  left: 16px;
  right: 16px;
  height: 60px;
  background: #1C1C1E; /* 纯暗黑背景 */
  border: 1px solid #333;
  border-radius: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px;
  z-index: 99;
  
  .action-btn {
    height: 48px;
    border-radius: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 15px;
    cursor: pointer;
    transition: all 0.2s;
    
    &.secondary {
      width: 30%;
      color: $text-sub;
      background: transparent;
      &:active { background: #333; }
    }
    
    &.primary {
      flex: 1;
      background: $theme-green;
      color: #000; /* 绿色背景用黑色文字对比度更高 */
      margin-left: 8px;
      box-shadow: 0 4px 12px rgba(0, 203, 165, 0.2);
      &:active { transform: scale(0.98); }
    }
  }
}

/* 列表动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>