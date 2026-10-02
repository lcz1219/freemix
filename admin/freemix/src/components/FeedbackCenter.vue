<template>
  <n-drawer 
    v-model:show="showFeedback" 
    :default-width="drawerWidth" 
    placement="right" 
    resizable

    :on-after-enter="initFeedBack"
    @resize="handleDrawerResize">
    <n-drawer-content title="反馈中心" closable body-content-class="feedback-body">
      <template #header>
        <!-- 头部：返回(仅详情页) + 品牌圆点标题 + 刷新 -->
        <div class="fb-header">
          <n-button
            v-if="isDetail"
            class="fb-header__back"
            quaternary
            circle
            size="small"
            @click="goBackToList">
            <template #icon>
              <n-icon>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20">
                  <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
                </svg>
              </n-icon>
            </template>
          </n-button>

          <div class="fb-header__title">
            <span class="fb-header__dot"></span>
            <span class="fb-header__text">{{ isDetail ? '反馈详情' : '反馈中心' }}</span>
          </div>

          <n-button
            class="fb-header__refresh"
            quaternary
            circle
            size="small"
            @click="refreshFeedback">
            <template #icon>
              <n-icon :component="Refresh" />
            </template>
          </n-button>
        </div>
      </template>
      
      <div class="feedback-container">
        <!-- 反馈表单 -->
        <section class="fb-panel" v-if="!isDetail">
          <div class="fb-panel__head">
            <div class="fb-panel__title">提交反馈</div>
            <p class="fb-panel__sub">你的建议会直接送达开发团队</p>
          </div>

          <n-form ref="formRef" :model="feedbackForm" :rules="formRules">
            <n-form-item label="反馈类型" path="type">
              <n-select 
                v-model:value="feedbackForm.type" 
                :options="feedbackTypes" 
                placeholder="请选择反馈类型" 
              />
            </n-form-item>
            
            <n-form-item label="主题" path="subject">
              <n-input 
                v-model:value="feedbackForm.subject" 
                placeholder="请输入反馈主题" 
                maxlength="50" 
                show-count 
              />
            </n-form-item>
            
            <n-form-item label="详细描述" path="content">
              <n-input 
                v-model:value="feedbackForm.content" 
                placeholder="请详细描述您的反馈或建议" 
                type="textarea"
                :autosize="{ minRows: 4, maxRows: 8 }"
                maxlength="500" 
                show-count
              />
            </n-form-item>
            
            <n-form-item label="联系方式（可选）" path="contact">
              <n-input 
                v-model:value="feedbackForm.contact" 
                placeholder="邮箱或手机号，方便我们联系您" 
              />
            </n-form-item>
            
            <n-form-item>
              <n-space justify="end">
                <n-button @click="resetForm">重置</n-button>
                <n-button type="primary" @click="submitFeedback" :loading="submitting">
                  {{ submitting ? '提交中...' : '提交反馈' }}
                </n-button>
              </n-space>
            </n-form-item>
          </n-form>
        </section>
        
        <!-- 历史反馈 -->
        <section class="fb-panel fb-panel--history" v-if="!isDetail">
          <div class="fb-panel__head">
            <div class="fb-panel__title">历史反馈</div>
            <span class="fb-panel__badge" v-if="feedbackList.length > 0">{{ feedbackList.length }}</span>
          </div>

          <div class="fb-list" v-if="feedbackList.length > 0">
            <article
              v-for="feedback in feedbackList"
              :key="feedback.id"
              class="fb-item clickable"
              @click="showFeedbackDetail(feedback)">
              <div class="fb-item__top">
                <span class="fb-item__subject">{{ feedback.subject }}</span>
                <n-tag :type="getFeedbackTypeTag(feedback.type)" size="small" :bordered="false">
                  {{ getFeedbackTypeText(feedback.type) }}
                </n-tag>
              </div>
              <p class="fb-item__desc">
                {{ feedback.content.substring(0, 100) + (feedback.content.length > 100 ? '...' : '') }}
              </p>
              <div class="fb-item__meta">
                <span class="fb-item__date">{{ formatDate(feedback.createdAt) }}</span>
                <n-tag :type="getStatusType(feedback.status)" size="small" :bordered="false" round>
                  {{ getStatusText(feedback.status) }}
                </n-tag>
              </div>
            </article>
          </div>

          <!-- 无记录时的占位 -->
          <div class="fb-empty" v-else>
            <n-icon :component="ChatboxEllipsesOutline" :size="34" />
            <p>还没有反馈记录</p>
          </div>
        </section>
        
        <!-- 反馈详情视图 -->
        <div v-else-if="isDetail && selectedFeedback">
          <FeedbackDetail :feedback="selectedFeedback" @updateFeedback="updateFeedback" />
        </div>
        
        <!-- 空状态 -->
        <n-empty v-else description="暂无反馈记录" style="margin-top: 20px;">
          <template #icon>
            <n-icon :component="ChatboxEllipsesOutline" />
          </template>
        </n-empty>
      </div>
    </n-drawer-content>
  </n-drawer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted,computed } from 'vue'
import { 
  NDrawer, 
  NDrawerContent, 
  NCard, 
  NForm, 
  NFormItem, 
  NInput, 
  NSelect, 
  NButton, 
  NSpace, 
  NList, 
  NListItem, 
  NThing, 
  NTag, 
  NText, 
  NEmpty,
  useMessage,

  NIcon,
  type FormRules,
  type FormInst
} from 'naive-ui'
import { Refresh, ChatboxEllipsesOutline } from '@vicons/ionicons5'
import type { Component } from 'vue'
import feedstypes from '@/components/json/feedstypes.json'
import { postM, isSuccess, baseURL,getM } from '@/utils/request'
import FeedbackDetail from '@/components/FeedbackDetail.vue'

// 定义反馈类型
interface Feedback {
  id?: string
  type: string
  subject: string
  content: string
  contact?: string
  status: string
  feedStatus?: string
  createdAt: string
}

// 抽屉显示状态
const showFeedback = defineModel<boolean>('show', { default: false })
const message = useMessage()
// 抽屉宽度
const drawerWidth = ref('35%')
const handleDrawerResize = (width: number) => {
  drawerWidth.value = width + 'px'
}
const isDetail=computed(()=>{
  return currentView.value==='detail'
})

// 视图状态管理 (list 或 detail)
const currentView = ref<'list' | 'detail'>('list')
const selectedFeedback = ref<Feedback | null>(null)

// 表单引用
const formRef = ref<FormInst | null>(null)

// 提交状态
const submitting = ref(false)

// 反馈表单数据
const feedbackForm = reactive({
  type: null as string | null,
  subject: '',
  content: '',
  contact: '',
  status: ''

})

// 反馈类型选项
const feedbackTypes = feedstypes.feedbackTypes

// 表单验证规则
const formRules: FormRules = {
  type: {
    required: true,
    message: '请选择反馈类型',
    trigger: 'change'
  },
  subject: {
    required: true,
    message: '请输入反馈主题',
    trigger: 'blur'
  },
  content: {
    required: true,
    message: '请输入详细描述',
    trigger: 'blur'
  }
}

// 反馈列表
const feedbackList = ref<Feedback[]>([])

// 获取反馈类型标签类型
const getFeedbackTypeTag = (type: string) => {
  switch (type) {
    case 'feature': return 'info'
    case 'bug': return 'error'
    case 'ux': return 'warning'
    default: return 'default'
  }
}

// 获取反馈类型文本
const getFeedbackTypeText = (type: string) => {
  switch (type) {
    case 'feature': return '功能建议'
    case 'bug': return '问题报告'
    case 'ux': return '用户体验'
    default: return '其他'
  }
}

// 获取状态标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'processing': return 'info'
    case 'resolved': return 'success'
    default: return 'default'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'pending': return '待处理'
    case 'processing': return '处理中'
    case 'resolved': return '已解决'
    default: return '未知'
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 提交反馈
const submitFeedback = (e: MouseEvent) => {
  e.preventDefault()
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      submitting.value = true
      feedbackForm.status = 'pending'
      // 模拟提交反馈
      const res= await postM('addFeedback',feedbackForm)
      if(isSuccess(res)){
        // 添加到反馈列表顶部
        // feedbackList.value.unshift({
        //   type: feedbackForm.type || 'other',
        //   subject: feedbackForm.subject,
        //   content: feedbackForm.content,
        //   contact: feedbackForm.contact,
        //   status: 'pending',
        //   createdAt: new Date().toLocaleString()
        // })
        submitting.value = false
        resetForm()
        initFeedBack()
        // 显示成功消息
        message.success('反馈提交成功，感谢您的建议！')
    
    } else {
      message.error('请填写必填项')
    }
  }
  })
}

// 重置表单
const resetForm = () => {
  feedbackForm.type = null
  feedbackForm.subject = ''
  feedbackForm.content = ''
  feedbackForm.contact = ''
}

// 刷新反馈
const refreshFeedback = () => {
  initFeedBack()
  // 模拟刷新数据
  message.success('刷新成功')
}
const updateFeedback=async(item)=>{
  
 await initFeedBack()
 console.log("feedbackList.value.filter(e=>item._id==e._id)[0]",feedbackList.value.filter(e=>item._id==e._id)[0]);
 
 selectedFeedback.value=feedbackList.value.filter(e=>item._id==e._id)[0]

  
}
const initFeedBack=async()=>{
  console.log("initFeedBack");
  
 const res= await getM("findFeedBack")
 if(isSuccess(res)){
  feedbackList.value=res.data.data
 }
}
// 显示反馈详情
const showFeedbackDetail = (feedback: Feedback) => {
  selectedFeedback.value = feedback
  currentView.value = 'detail'
}

// 返回反馈列表
const goBackToList = () => {
  currentView.value = 'list'
  selectedFeedback.value = null
}
// 初始化数据
onMounted(() => {
  // 模拟获取历史反馈数据
  // feedbackList.value = [
  //   {
  //     id: '1',
  //     type: 'feature',
  //     subject: '增加数据导出功能',
  //     content: '希望可以增加数据导出为Excel的功能，方便数据分析。',
  //     status: 'processing',
  //     createdAt: '2023-05-15T10:30:00Z'
  //   },
  //   {
  //     id: '2',
  //     type: 'bug',
  //     subject: '移动端页面显示异常',
  //     content: '在iPhone Safari浏览器中，目标详情页面的布局有错位现象。',
  //     status: 'resolved',
  //     createdAt: '2023-05-10T14:20:00Z'
  //   }
  // ]
  
  initFeedBack()
})
</script>

<style scoped>
/* ============================================================
   反馈中心 UI —— 适配系统主题
   配色统一走系统设计令牌，令牌本身已随明暗主题切换，
   所以不必为两套主题各写一份样式：
   · 文字 / 描边 / 悬停 → --text-color / --border-color / --hover-color
   · 品牌主色           → #00c9a7（对应 --fm-primary-500）
   ============================================================ */

/* ── 抽屉外壳 ──
   feedback-body 是 n-drawer-content 的 body-content-class，
   用来清掉 Naive 默认内边距，改由 .feedback-container 自己控制。
   头部用卡片色、内容区用页面底色，拉开层次 */
:deep(.feedback-body) {
  padding: 0;
}

:deep(.n-drawer-header) {
  background: var(--card-bg);
}

:deep(.n-drawer-body) {
  background: var(--bg-color);
}

/* ── 头部 ── */
.fb-header {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.fb-header__title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

/* 品牌色圆点 + 辉光，作为标题的视觉锚点 */
.fb-header__dot {
  flex: none;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #00c9a7;
  box-shadow: 0 0 8px rgba(0, 201, 167, 0.8);
}

.fb-header__text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.2px;
  color: var(--text-color);
}

/* 头部圆形按钮悬停时染上品牌色 */
.fb-header :deep(.n-button:hover) {
  color: #00c9a7;
  background-color: rgba(0, 201, 167, 0.1);
}

.feedback-container {
  padding: 20px;
}

/* ── 面板卡片 ── */
.fb-panel {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 16px;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}

/* 暗色下卡片色与页面底色同为深灰，单独提亮一档拉开层次 */
:global(html.dark-theme) .fb-panel {
  background: #1a1a1a;
}

.fb-panel:hover {
  border-color: rgba(0, 201, 167, 0.35);
  box-shadow: 0 10px 30px rgba(0, 201, 167, 0.08);
}

.fb-panel__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

/* 面板标题左侧的品牌色竖条 */
.fb-panel__title {
  position: relative;
  padding-left: 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
}

.fb-panel__title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: #00c9a7;
}

.fb-panel__sub {
  font-size: 12px;
  color: rgba(var(--fm-base-text-rgb), 0.5);
}

/* 历史反馈条数徽标 */
.fb-panel__badge {
  margin-left: auto;
  min-width: 22px;
  height: 20px;
  padding: 0 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #00c9a7;
  background: rgba(0, 201, 167, 0.12);
  border-radius: 10px;
}

:deep(.n-form-item-label) {
  font-weight: 500;
}

/* ── 历史反馈列表 ── */
.fb-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.fb-item {
  position: relative;
  padding: 12px 14px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  transition:
    transform 0.18s ease,
    border-color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.fb-item:hover {
  transform: translateY(-2px);
  border-color: rgba(0, 201, 167, 0.4);
  background: rgba(0, 201, 167, 0.06);
  box-shadow: 0 8px 20px rgba(0, 201, 167, 0.12);
}

/* 悬停时从中间展开的品牌色竖条 */
.fb-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: #00c9a7;
  transform: scaleY(0);
  transition: transform 0.2s ease;
}

.fb-item:hover::before {
  transform: scaleY(1);
}

.fb-item__top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.fb-item__subject {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fb-item__desc {
  margin-bottom: 10px;
  font-size: 13px;
  line-height: 1.5;
  color: rgba(var(--fm-base-text-rgb), 0.6);
  /* 最多两行，超出省略，保证每张卡片高度一致 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.fb-item__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.fb-item__date {
  font-size: 12px;
  color: rgba(var(--fm-base-text-rgb), 0.45);
}

/* ── 空状态 ── */
.fb-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 26px 0 14px;
  color: rgba(var(--fm-base-text-rgb), 0.38);
}

.fb-empty p {
  font-size: 13px;
}
</style>