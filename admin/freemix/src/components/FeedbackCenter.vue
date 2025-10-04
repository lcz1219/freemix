<template>
  <n-drawer 
    v-model:show="showFeedback" 
    :default-width="drawerWidth" 
    placement="right" 
    resizable

    :on-after-enter="initFeedBack"
    @resize="handleDrawerResize">
    <n-drawer-content title="反馈中心" closable>
      <template #header>
        <n-space style="justify-content: space-between; width: 100%;">
          <div v-if="isDetail">
            <n-button quaternary circle @click="goBackToList">
              <template #icon>
                <n-icon>
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
                    <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
                  </svg>
                </n-icon>
              </template>
            </n-button>
          </div>
          <div v-else style="flex: 1;"></div>
          <n-button quaternary circle @click="refreshFeedback">
            <template #icon>
              <n-icon :component="Refresh" />
            </template>
          </n-button>
        </n-space>
      </template>
      
      <div class="feedback-container">
        <!-- 反馈表单 -->
        <n-card class="feedback-card" title="提交反馈" :bordered="false" v-if="!isDetail">
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
        </n-card>
        
        <!-- 历史反馈和反馈详情视图 -->
        <div v-if="!isDetail">
          <n-card class="feedback-history" title="历史反馈" :bordered="false" v-if="feedbackList.length > 0">
            <n-list>
              <n-list-item v-for="feedback in feedbackList" :key="feedback.id" @click="showFeedbackDetail(feedback)" style="cursor: pointer;">
                <n-thing :title="feedback.subject" :description="feedback.content.substring(0, 100) + (feedback.content.length > 100 ? '...' : '')">
                  <template #header-extra>
                    <n-tag :type="getFeedbackTypeTag(feedback.type)" size="small">
                      {{ getFeedbackTypeText(feedback.type) }}
                    </n-tag>
                  </template>
                  <template #description>
                    <n-space size="small" style="margin-top: 4px;">
                      <n-text depth="3">{{ formatDate(feedback.createdAt) }}</n-text>
                      <n-tag :type="getStatusType(feedback.status)" size="small">
                        {{ getStatusText(feedback.status) }}
                      </n-tag>
                    </n-space>
                  </template>
                </n-thing>
              </n-list-item>
            </n-list>
          </n-card>
        </div>
        
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
.feedback-container {
  padding: 16px;
}

.feedback-card,
.feedback-history {
  margin-bottom: 20px;
}

:deep(.n-card__content) {
  padding: 16px !important;
}

:deep(.n-form-item-label) {
  font-weight: 500;
}
</style>