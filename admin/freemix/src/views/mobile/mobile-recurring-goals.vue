<template>
  <van-config-provider theme="dark">
    <div class="app-container dark-mode">
      <!-- 顶部导航栏（毛玻璃） -->
      <van-nav-bar fixed placeholder class="glass-nav" :border="false" z-index="100" :safe-area-inset-top="true">
        <template #left>
          <div class="nav-back" @click="goBack">
            <van-icon name="arrow-left" />
            <span>返回</span>
          </div>
        </template>
        <template #title><span class="nav-title">定时自动目标</span></template>
        <template #right>
          <div class="icon-btn" @click="fetchData">
            <van-icon name="replay" size="18" />
          </div>
        </template>
      </van-nav-bar>

      <div class="content-wrapper">
        <!-- 说明卡片 -->
        <div class="intro-card">
          <div class="intro-icon"><van-icon name="clock-o" size="22" /></div>
          <div class="intro-text">
            <div class="intro-title">自动循环生成目标</div>
            <div class="intro-desc">设置循环规则，系统会按时帮您创建目标</div>
          </div>
        </div>

        <!-- 新建按钮 -->
        <van-button block round class="create-btn" icon="plus" @click="handleAdd">新建定时任务</van-button>

        <!-- 列表（支持下拉刷新） -->
        <van-pull-refresh v-model="isRefreshing" @refresh="onRefresh" class="rule-pull">
          <!-- 骨架屏 -->
          <div v-if="loading" class="skeleton-list">
            <div class="glass-skeleton" v-for="i in 2" :key="i">
              <div class="sk-line w-60"></div>
              <div class="sk-line w-40"></div>
              <div class="sk-line w-80"></div>
            </div>
          </div>

          <!-- 任务卡片列表（左滑可编辑/删除） -->
          <div v-else class="rule-list">
            <van-swipe-cell v-for="item in list" :key="item._id" :right-width="140">
              <div class="rule-card" @click="handleEdit(item)">
                <!-- 左侧状态色条 -->
                <div class="rule-dot" :class="item.isActive ? 'active' : 'paused'"></div>
                <div class="rule-main">
                  <div class="rule-header">
                    <span class="rule-title">{{ item.title }}</span>
                    <van-tag :type="getRecurrenceTagType(item.recurrenceType)" size="mini" round plain>
                      {{ getRecurrenceLabel(item.recurrenceType) }}
                    </van-tag>
                  </div>
                  <div class="rule-desc">{{ item.description || '无描述' }}</div>
                  <div class="rule-meta">
                    <span class="meta-item">
                      <van-icon name="underway-o" /> 下次: {{ formatDate(item.nextExecutionTime) }}
                    </span>
                    <!-- 启用开关：阻止冒泡，避免触发卡片的编辑点击 -->
                    <div class="meta-switch" @click.stop>
                      <van-switch v-model="item.isActive" size="20px" active-color="#00c9a7"
                        @update:model-value="(val) => handleToggle(item, val)" />
                    </div>
                  </div>
                </div>
              </div>
              <template #right>
                <van-button square class="swipe-btn edit" text="编辑" @click="handleEdit(item)" />
                <van-button square class="swipe-btn error" text="删除" @click="handleDelete(item)" />
              </template>
            </van-swipe-cell>
          </div>
        </van-pull-refresh>

        <!-- 空状态 -->
        <div v-if="!loading && list.length === 0" class="empty-glass-card">
          <div class="empty-icon-animated"><van-icon name="clock-o" size="36" /></div>
          <p class="empty-title">还没有设置任何定时目标</p>
          <p class="empty-desc">点击上方按钮创建第一个定时任务</p>
        </div>
      </div>
    </div>

    <!-- ========== 新建 / 编辑 弹窗（底部半屏） ========== -->
    <van-popup v-model:show="showModal" position="bottom" round closeable close-icon-position="top-right"
      class="form-popup" :style="{ height: '92%' }">
      <div class="form-wrapper">
        <div class="form-header">
          <span class="form-title">{{ form._id ? '编辑定时任务' : '新建定时任务' }}</span>
        </div>

        <div class="form-body">
          <!-- 基本信息 -->
          <van-cell-group inset>
            <van-field v-model="form.title" label="标题" placeholder="任务标题 (例如: 每日健身)" />
            <van-field v-model="form.description" label="描述" type="textarea" rows="2" autosize
              placeholder="任务模板描述" />
          </van-cell-group>

          <!-- 子目标模板（动态增删） -->
          <div class="block-section">
            <div class="block-label">子目标模板</div>
            <div v-for="(g, idx) in form.childGoals" :key="idx" class="child-goal-row">
              <van-field v-model="g.message" class="child-goal-field" placeholder="子目标内容" />
              <van-icon name="cross" class="child-goal-del" @click="removeChildGoal(idx)" />
            </div>
            <van-button size="small" round plain type="primary" icon="plus" class="add-child-btn"
              @click="addChildGoal">
              添加子目标模板
            </van-button>
          </div>

          <!-- 循环设置 -->
          <van-cell-group inset>
            <van-field :model-value="getRecurrenceLabel(form.recurrenceType)" is-link readonly label="循环类型"
              @click="showRecurrencePicker = true" />
          </van-cell-group>

          <!-- Cron 表达式（仅 cron 类型显示） -->
          <div v-if="form.recurrenceType === 'cron'" class="block-section">
            <van-field v-model="form.cronExpression" label="Cron 表达式" placeholder="例如：0 0 12 * * ?">
              <template #button>
                <van-button size="small" round plain type="primary" @click="showCronTemplates = true">
                  常用模板
                </van-button>
              </template>
            </van-field>

            <!-- Cron 预览反馈：可选模板 / 无效提示 / 格式说明 -->
            <div class="cron-feedback">
              <div v-if="cronNextExecutions.length > 0" class="cron-success">
                <div class="cron-rule-label">
                  <van-icon name="clock-o" /> {{ getCronTemplateLabel(form.cronExpression) }}
                </div>
                <div class="cron-times-title">最近 5 次执行时间:</div>
                <div class="cron-times">
                  <van-tag v-for="(time, idx) in cronNextExecutions" :key="idx" type="success" round size="medium"
                    class="cron-time-tag">
                    {{ formatDate(time) }}
                  </van-tag>
                </div>
              </div>
              <div v-else-if="form.cronExpression && !isCronValid" class="cron-error">
                <van-icon name="warning-o" /> 无效的 Cron 表达式
              </div>
              <div v-else class="cron-hint">
                <van-icon name="question-o" /> 支持 Spring Cron 格式: 秒 分 时 日 月 周
              </div>
            </div>
          </div>

          <!-- 优先级 -->
          <van-cell-group inset>
            <van-field :model-value="getOptionLabel(levelOptions, form.level)" is-link readonly label="优先级"
              @click="showLevelPicker = true" />
          </van-cell-group>

          <!-- 分类标签 -->
          <div class="block-section">
            <div class="block-label">分类标签</div>
            <div class="tag-chips">
              <van-tag v-for="tag in tagOptions" :key="tag.value" round size="medium" class="tag-chip"
                :type="form.tags.includes(tag.value) ? 'primary' : 'default'"
                :plain="!form.tags.includes(tag.value)" @click="toggleTag(tag.value)">
                {{ tag.label }}
              </van-tag>
              <span v-if="tagOptions.length === 0" class="tag-empty">暂无标签，可在下方自定义添加</span>
            </div>
            <van-field v-model="customTag" label="自定义标签" placeholder="输入后点击添加">
              <template #button>
                <van-button size="small" round plain type="primary" @click="addCustomTag">添加</van-button>
              </template>
            </van-field>
          </div>

          <!-- 可见性 -->
          <van-cell-group inset>
            <van-field label="可见性" class="visibility-field">
              <template #input>
                <van-radio-group v-model="form.isPublic" direction="horizontal">
                  <van-radio :name="false" checked-color="#00c9a7">私密</van-radio>
                  <van-radio :name="true" checked-color="#00c9a7">公开</van-radio>
                </van-radio-group>
              </template>
            </van-field>
          </van-cell-group>
        </div>

        <!-- 底部操作按钮 -->
        <div class="popup-footer">
          <van-button block round plain class="cancel-btn" @click="showModal = false">取消</van-button>
          <van-button block round class="save-btn" :loading="submitting" @click="handleSubmit">保存模板</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 循环类型选择器 -->
    <van-popup v-model:show="showRecurrencePicker" position="bottom" round class="glass-popup">
      <van-picker :columns="recurrenceColumns" title="循环类型" @confirm="onRecurrenceConfirm"
        @cancel="showRecurrencePicker = false" />
    </van-popup>

    <!-- 优先级选择器 -->
    <van-popup v-model:show="showLevelPicker" position="bottom" round class="glass-popup">
      <van-picker :columns="levelColumns" title="优先级" @confirm="onLevelConfirm"
        @cancel="showLevelPicker = false" />
    </van-popup>

    <!-- 常用 Cron 模板选择 -->
    <van-action-sheet v-model:show="showCronTemplates" title="常用 Cron 模板" :actions="cronActions"
      cancel-text="取消" close-on-click-action @select="onCronTemplateSelect" />
  </van-config-provider>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast, showConfirmDialog } from 'vant'
import { getM, postM, isSuccess } from '@/utils/request'

const router = useRouter()

// ===== 列表状态 =====
const list = ref<any[]>([])
const loading = ref(false)
const isRefreshing = ref(false)

// ===== 表单状态 =====
const showModal = ref(false)
const submitting = ref(false)
const showRecurrencePicker = ref(false)
const showLevelPicker = ref(false)
const showCronTemplates = ref(false)
const customTag = ref('')

// Cron 预览结果与校验标记
const cronNextExecutions = ref<any[]>([])
const isCronValid = ref(true)

const form = ref<any>({
  _id: null,
  title: '',
  description: '',
  recurrenceType: 'daily',
  cronExpression: '0 0 0 * * ?',
  level: 'medium',
  tags: [],
  childGoals: [],
  isPublic: false,
  isActive: true
})

// 循环类型 / 优先级选项
const recurrenceOptions = [
  { label: '每天', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' },
  { label: 'Cron 定制', value: 'cron' }
]

const levelOptions = [
  { label: '高优', value: 'high' },
  { label: '中优', value: 'medium' },
  { label: '低优', value: 'low' }
]

// Vant Picker 需要 { text, value } 结构的列数据
const recurrenceColumns = recurrenceOptions.map(o => ({ text: o.label, value: o.value }))
const levelColumns = levelOptions.map(o => ({ text: o.label, value: o.value }))

// 常用 Cron 模板
const cronTemplates = [
  { label: '每分钟', value: '0 * * * * ?' },
  { label: '每小时(整点)', value: '0 0 * * * ?' },
  { label: '每天(凌晨0点)', value: '0 0 0 * * ?' },
  { label: '每天(中午12点)', value: '0 0 12 * * ?' },
  { label: '每周一(凌晨0点)', value: '0 0 0 ? * MON' },
  { label: '每月1号(凌晨0点)', value: '0 0 0 1 * ?' },
  { label: '每工作日(早上9点)', value: '0 0 9 ? * MON-FRI' }
]
const cronActions = cronTemplates.map(t => ({ name: t.label, value: t.value }))

// 标签选项（来自后端已有标签）
const tagOptions = ref<any[]>([])

const goBack = () => router.back()

// ===== 数据获取 =====
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getM('/getRecurringGoals')
    if (res.data.operSucc) {
      list.value = res.data.data || []
    }
  } catch (error) {
    showFailToast('获取数据失败')
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

const onRefresh = async () => {
  isRefreshing.value = true
  await fetchData()
}

const getTags = async () => {
  const res = await getM('getTags')
  if (isSuccess(res)) {
    tagOptions.value = res.data.data.map((tag: string) => ({ label: tag, value: tag }))
  }
}

// ===== Cron 预览 =====
// 请求后端解析 Cron 并返回最近 5 次执行时间
const fetchCronPreview = async () => {
  const cron = form.value.cronExpression
  if (!cron || form.value.recurrenceType !== 'cron') {
    cronNextExecutions.value = []
    return
  }
  try {
    const res = await getM(`/previewCron?cron=${encodeURIComponent(cron)}`)
    if (res.data.operSucc) {
      cronNextExecutions.value = res.data.data || []
      isCronValid.value = true
    } else {
      cronNextExecutions.value = []
      isCronValid.value = false
    }
  } catch (error) {
    cronNextExecutions.value = []
    isCronValid.value = false
  }
}

// Cron 表达式变化时刷新预览（immediate 保证打开弹窗后也能拿到初始预览）
watch(() => form.value.cronExpression, fetchCronPreview, { immediate: true })

// 切换到非 cron 类型时清空预览
watch(() => form.value.recurrenceType, (val) => {
  if (val !== 'cron') cronNextExecutions.value = []
})

// ===== 表单操作 =====
const handleAdd = () => {
  form.value = {
    _id: null,
    title: '',
    description: '',
    recurrenceType: 'daily',
    cronExpression: '0 0 0 * * ?',
    level: 'medium',
    tags: [],
    childGoals: [
      { message: '1. ', finish: false, finishTime: '' },
      { message: '2. ', finish: false, finishTime: '' },
      { message: '3. ', finish: false, finishTime: '' }
    ],
    isPublic: false,
    isActive: true
  }
  isCronValid.value = true
  showModal.value = true
}

const handleEdit = (item: any) => {
  form.value = {
    ...item,
    tags: item.tags || [],
    childGoals: item.childGoals || [],
    isPublic: item.isPublic || false,
    cronExpression: item.cronExpression || '0 0 0 * * ?'
  }
  showModal.value = true
  // 编辑 Cron 规则时立即展示预览
  if (form.value.recurrenceType === 'cron') fetchCronPreview()
}

const addChildGoal = () => {
  form.value.childGoals.push({ message: '', finish: false, finishTime: '' })
}

const removeChildGoal = (idx: number) => {
  form.value.childGoals.splice(idx, 1)
}

// 标签选中/取消
const toggleTag = (tag: string) => {
  const idx = form.value.tags.indexOf(tag)
  if (idx > -1) {
    form.value.tags.splice(idx, 1)
  } else {
    form.value.tags.push(tag)
  }
}

// 添加自定义标签
const addCustomTag = () => {
  const tag = (customTag.value || '').trim()
  if (!tag) return
  if (!form.value.tags.includes(tag)) form.value.tags.push(tag)
  if (!tagOptions.value.some(t => t.value === tag)) tagOptions.value.push({ label: tag, value: tag })
  customTag.value = ''
}

const onRecurrenceConfirm = ({ selectedOptions }) => {
  form.value.recurrenceType = selectedOptions[0].value
  showRecurrencePicker.value = false
  // 切到 Cron 定制时立即请求一次预览
  if (form.value.recurrenceType === 'cron') fetchCronPreview()
}

const onLevelConfirm = ({ selectedOptions }) => {
  form.value.level = selectedOptions[0].value
  showLevelPicker.value = false
}

const onCronTemplateSelect = (action: any) => {
  form.value.cronExpression = action.value
}

const handleSubmit = async () => {
  if (!form.value.title) {
    showToast('请输入标题')
    return
  }

  // 过滤空的子目标模板
  const childGoals = (form.value.childGoals || [])
    .filter(g => g.message && g.message.trim() !== '')
    .map(g => ({
      _id: g._id,
      message: g.message,
      finish: false,
      finishTime: ''
    }))

  const submitData = { ...form.value, childGoals }

  submitting.value = true
  try {
    const res = await postM('/editRecurringGoal', submitData)
    if (res.data.operSucc) {
      showSuccessToast('保存成功')
      showModal.value = false
      fetchData()
    } else {
      showFailToast(res.data.msg || '保存失败')
    }
  } catch (error) {
    showFailToast('保存失败')
  } finally {
    submitting.value = false
  }
}

// 启用/停用
const handleToggle = async (item: any, val: boolean) => {
  try {
    const res = await postM(`/toggleRecurringGoal?id=${item._id}&active=${val}`)
    if (res.data.operSucc) {
      showToast(val ? '已启用' : '已停用')
    }
  } catch (error) {
    item.isActive = !val // 请求失败则恢复原状态
    showFailToast('操作失败')
  }
}

const handleDelete = (item: any) => {
  showConfirmDialog({
    title: '确认删除',
    message: `确定要删除定时任务 "${item.title}" 吗？`,
    confirmButtonColor: '#00c9a7'
  }).then(async () => {
    try {
      const res = await postM(`/deleteRecurringGoal?id=${item._id}`)
      if (res.data.operSucc) {
        showSuccessToast('已删除')
        fetchData()
      }
    } catch (error) {
      showFailToast('删除失败')
    }
  }).catch(() => { })
}

// ===== 展示辅助函数 =====
const getRecurrenceLabel = (type: string) => {
  const option = recurrenceOptions.find(o => o.value === type)
  return option ? option.label : type
}

const getOptionLabel = (options: any[], value: string) => {
  const option = options.find(o => o.value === value)
  return option ? option.label : value
}

const getRecurrenceTagType = (type: string) => {
  if (type === 'daily') return 'primary'
  if (type === 'weekly') return 'warning'
  if (type === 'monthly') return 'danger'
  if (type === 'cron') return 'success'
  return 'default'
}

const getCronTemplateLabel = (cron: string) => {
  const template = cronTemplates.find(t => t.value === cron)
  return template ? `定时规则: ${template.label}` : '自定义 Cron 规则'
}

const formatDate = (date: any) => {
  if (!date) return '未执行'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchData()
  getTags()
})
</script>

<style scoped lang="scss">
/* ============================================
   沉浸式毛玻璃主题 - 变量与背景
   ============================================ */
.app-container {
  --bg-glass: rgba(30, 30, 35, 0.5);
  --text-primary: #e8e8f0;
  --text-secondary: #888;
  --border-line: rgba(255, 255, 255, 0.06);
  --brand-color: #00c9a7;

  min-height: 100vh;
  background: linear-gradient(160deg, #0a0a0f 0%, #111118 30%, #0e0e1a 70%, #0a0a0f 100%);
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
}

.content-wrapper {
  padding: 16px;
  padding-bottom: 40px;
}

/* ---- 顶部导航 ---- */
.glass-nav {
  background: rgba(20, 20, 25, 0.8);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000 !important;

  :deep(.van-nav-bar__content) {
    height: 50px;
    background-color: transparent;
  }
}

.nav-back {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-primary);
  font-size: 15px;

  &:active { opacity: 0.7; }
}

.nav-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(125, 125, 125, 0.12);
  color: var(--text-primary);

  &:active {
    transform: scale(0.9);
    background: rgba(0, 201, 167, 0.15);
  }
}

/* ============================================
   说明卡片 + 新建按钮
   ============================================ */
.intro-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 20px;
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  border: 1px solid var(--border-line);
  margin-bottom: 14px;
  margin-top: 50px;
}

.intro-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 201, 167, 0.12);
  color: var(--brand-color);
  flex-shrink: 0;
}

.intro-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 3px;
}

.intro-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.create-btn {
  height: 46px;
  border: none;
  background: linear-gradient(135deg, #00c9a7 0%, #00a88b 100%);
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 16px;
  box-shadow: 0 4px 16px rgba(0, 201, 167, 0.3);

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }
}

/* ============================================
   任务卡片列表
   ============================================ */
.rule-pull {
  min-height: 200px;
}

.rule-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 20px;
}

.rule-card {
  position: relative;
  display: flex;
  align-items: stretch;
  gap: 12px;
  padding: 14px;
  border-radius: 18px;
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  border: 1px solid var(--border-line);
  animation: cardFadeIn 0.35s ease both;

  &:active {
    background: rgba(0, 201, 167, 0.06);
  }
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧状态色条 */
.rule-dot {
  width: 3px;
  border-radius: 2px;
  flex-shrink: 0;
  background: #666;

  &.active {
    background: var(--brand-color);
    box-shadow: 0 0 8px rgba(0, 201, 167, 0.5);
  }

  &.paused {
    background: #555;
  }
}

.rule-main {
  flex: 1;
  min-width: 0;
}

.rule-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.rule-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 65%;
}

.rule-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.rule-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 0.5px solid var(--border-line);
  padding-top: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

/* 左滑操作按钮 */
.swipe-btn {
  height: 100%;
  font-size: 12px;
  color: #fff;

  &.edit { background: #4f8ef7; }
  &.error { background: #c91b00; }
}

/* ---- 骨架屏 ---- */
.skeleton-list {
  padding: 4px 0;
}

.glass-skeleton {
  background: var(--bg-glass);
  padding: 18px 16px;
  border-radius: 18px;
  margin-bottom: 12px;
  border: 1px solid var(--border-line);
}

.sk-line {
  height: 12px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  margin-bottom: 10px;

  &.w-40 { width: 40%; }
  &.w-60 { width: 60%; }
  &.w-80 { width: 80%; }

  &:last-child { margin-bottom: 0; }
}

/* ---- 空状态 ---- */
.empty-glass-card {
  text-align: center;
  padding: 40px 24px;
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  border: 1px solid var(--border-line);
  border-radius: 20px;
  margin-top: 8px;
}

.empty-icon-animated {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 201, 167, 0.08);
  color: rgba(255, 255, 255, 0.25);
  animation: emptyIconFloat 2.5s ease-in-out infinite;
}

@keyframes emptyIconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.empty-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px;
}

.empty-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
}

/* ============================================
   表单弹窗
   ============================================ */
.form-popup {
  background: #14141a !important;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.form-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.form-header {
  padding: 18px 20px 12px;
  text-align: center;
  flex-shrink: 0;
}

.form-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
}

.form-body {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 16px;

  /* 输入框/单元格深色适配 */
  :deep(.van-cell-group--inset) {
    background: rgba(255, 255, 255, 0.04);
    margin: 0 12px 14px;
    border-radius: 14px;
  }

  :deep(.van-field) {
    background: transparent;
    color: var(--text-primary);
  }

  :deep(.van-field__label) {
    color: var(--text-secondary);
  }

  :deep(.van-field__control) {
    color: var(--text-primary);

    &::placeholder {
      color: #555;
    }
  }

  :deep(.van-radio__label) {
    color: var(--text-primary);
    font-size: 14px;
  }
}

/* 区块（子目标 / Cron / 标签） */
.block-section {
  margin: 0 12px 14px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.04);
}

.block-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

/* ---- 子目标模板 ---- */
.child-goal-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;

  :deep(.van-field) {
    padding: 8px 12px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.05);
  }

  :deep(.van-field__control) {
    color: var(--text-primary);
  }
}

.child-goal-field {
  flex: 1;
}

.child-goal-del {
  color: #ff6b6b;
  font-size: 16px;
  padding: 4px;

  &:active { opacity: 0.6; }
}

.add-child-btn {
  margin-top: 4px;
}

/* ---- Cron 预览反馈 ---- */
.cron-feedback {
  margin-top: 10px;
}

.cron-success {
  padding: 12px;
  border-radius: 10px;
  background: rgba(0, 201, 167, 0.08);
  border-left: 3px solid var(--brand-color);
}

.cron-rule-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--brand-color);
  margin-bottom: 8px;
}

.cron-times-title {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.cron-times {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.cron-time-tag {
  font-size: 11px;
}

.cron-error {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(208, 48, 80, 0.1);
  border-left: 3px solid #d03050;
  color: #d03050;
  font-size: 13px;
  font-weight: 600;
}

.cron-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
  opacity: 0.8;
}

/* ---- 标签选择 ---- */
.tag-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}

.tag-chip {
  &:active { opacity: 0.7; }
}

.tag-empty {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ---- 可见性单选（去掉单元格内边距偏移） ---- */
.visibility-field {
  :deep(.van-field__control) {
    display: flex;
    align-items: center;
  }
}

/* ---- 弹窗底部按钮 ---- */
.popup-footer {
  display: flex;
  gap: 12px;
  padding: 12px 16px calc(16px + env(safe-area-inset-bottom));
  border-top: 0.5px solid var(--border-line);
  background: #14141a;
  flex-shrink: 0;

  .cancel-btn {
    flex: 1;
    height: 46px;
    border-color: var(--border-line);
    color: var(--text-secondary);
    background: transparent;
  }

  .save-btn {
    flex: 1;
    height: 46px;
    border: none;
    background: linear-gradient(135deg, #00c9a7 0%, #00a88b 100%);
    color: #fff;
    font-weight: 600;
    box-shadow: 0 4px 16px rgba(0, 201, 167, 0.3);
  }
}

/* ---- Picker / ActionSheet 深色适配 ---- */
.glass-popup {
  background: #121212;

  :deep(.van-picker) {
    background: transparent;
    color: #fff;
  }

  :deep(.van-picker__toolbar) {
    background: #1a1a1a;
    border-bottom: 1px solid #222;
  }

  :deep(.van-picker__cancel) { color: #888; }
  :deep(.van-picker__confirm) { color: var(--brand-color); }
  :deep(.van-picker-column__item) { color: #666; }
  :deep(.van-picker-column__item--selected) { color: #fff; font-weight: bold; }
}

:deep(.van-action-sheet) {
  background: #121212;
}

:deep(.van-action-sheet__header) {
  color: #fff;
  background: #1a1a1a;
}

:deep(.van-action-sheet__item) {
  background: #121212;
  color: #ddd;
}
</style>
