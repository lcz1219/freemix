<template>
  <n-card class="feedback-detail" title="反馈详情" :bordered="false">
    <n-descriptions label-placement="left" :column="1" bordered>
      <n-descriptions-item label="反馈类型">
        <n-tag :type="getFeedbackTypeTag(feedback.type)" size="small">
          {{ getFeedbackTypeText(feedback.type) }}
        </n-tag>
      </n-descriptions-item>
      <n-descriptions-item label="主题">
        {{ feedback.subject }}
      </n-descriptions-item>
      <n-descriptions-item label="详细描述">
        {{ feedback.content }}
      </n-descriptions-item>
      <n-descriptions-item label="联系方式" v-if="feedback.contact">
        {{ feedback.contact }}
      </n-descriptions-item>
      <n-descriptions-item label="状态">
        <n-tag :type="getStatusType(feedback.status)" size="small">
          {{ getStatusText(feedback.status) }}
        </n-tag>
      </n-descriptions-item>
      <n-descriptions-item label="提交时间">
        {{ formatDate(feedback.createdAt) }}
      </n-descriptions-item>
    </n-descriptions>

    <!-- 聊天风格的反馈对话 -->
    <div class="feedback-chat">
      <!-- 用户留言 -->
      <div class="chat-message user-message">
        <div class="message-header">
          <span class="sender">{{feedback.createdBy}}</span>
          <span class="time">{{ formatDate(feedback.createdAt) }}</span>
        </div>
        <div class="message-content">
          {{ feedback.content }}
        </div>
      </div>

      <!-- 开发者回复 -->
      <div class="chat-message developer-message" v-if="feedback.feedResult">
        <div class="message-header-developer">
          <span class="sender">开发者</span>
          <!-- 如果有回复时间字段，可以在这里显示 -->
          <!-- <span class="time">{{ formatReplyTime(feedback.replyTime) }}</span> -->
        </div>
        <div class="message-content">
          {{ feedback.feedResult }}
        </div>
      </div>

      <!-- 如果没有开发者回复，根据用户身份显示不同内容 -->
      <div class="no-reply" v-else>
        <!-- 如果是开发者账号，显示回复输入框 -->
        <div v-if="isDeveloper" class="developer-reply">
          <div style="width: 100%;text-align: left;">
          <n-input v-model:value="replyContent" type="textarea" placeholder="请输入回复内容..."
            :autosize="{ minRows: 3, maxRows: 6 }" maxlength="500" show-count />
            </div>
          <div style="width: 100%;text-align: right;">
            <n-button type="primary" @click="submitReply" :loading="replyLoading" style="margin-top: 10px;">
              {{ replyLoading ? '提交中...' : '发送回复' }}
            </n-button>
          </div>

        </div>
        <!-- 如果是普通用户，显示等待提示 -->
        <n-alert type="info" :show-icon="false" v-else>
          开发者尚未回复，敬请期待...
        </n-alert>
      </div>
    </div>
  </n-card>
</template>

<script setup lang="ts">
import {
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NTag,
  NAlert,
  NInput,
  NButton,
  useMessage
} from 'naive-ui'
import { ref, computed, onMounted, defineEmits } from 'vue'
import { useStore } from 'vuex'
import { postM, isSuccess } from '@/utils/request'

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
  feedResult?: string
}

// 定义组件属性
const props = defineProps<{
  feedback: Feedback
}>()
const emit = defineEmits(['updateFeedback'])

const store = useStore()
const message = useMessage()

// 回复内容
const replyContent = ref('')
const replyLoading = ref(false)

// 判断当前用户是否为开发者 (linchengzhong)
const isDeveloper = computed(() => {
  const currentUser = store.state.user
  return currentUser && currentUser.username === 'linchengzhong'
})

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
const getStatusType = (status: string | undefined) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'processing': return 'info'
    case 'resolved': return 'success'
    default: return 'default'
  }
}

// 获取状态文本
const getStatusText = (status: string | undefined) => {
  switch (status) {
    case 'pending': return '待处理'
    case 'processing': return '处理中'
    case 'resolved': return '已解决'
    default: return '未知'
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  // 1. 创建Date对象，传入毫秒级时间戳
  const date = new Date(dateString);

  // 2. 分别获取年、月、日、时、分、秒
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0'); // 月份从0开始，需+1并补零
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  // 3. 拼接成所需格式
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    message.warning('请输入回复内容')
    return
  }

  replyLoading.value = true
  try {
    // 这里应该调用实际的API来提交回复
    const res = await postM('replyFeedback', {
      feedbackId: props.feedback._id,
      replyContent: replyContent.value
    })
    replyContent.value = ''
    if (isSuccess(res)) {
      emit('updateFeedback', res.data.data)
      // await new Promise(resolve => setTimeout(resolve, 1000))

      // 如果成功，更新反馈状态和回复内容
      // 这里我们只是更新本地状态作为示例
      message.success('回复提交成功')

    } else {
      message.error(res.data.msg)
    }

    // 模拟API调用

  } catch (error) {
    message.error('回复提交失败')
  } finally {
    replyLoading.value = false
  }
}
</script>

<style scoped>
.feedback-detail :deep(.n-descriptions) {
  margin-top: 16px;
}

.feedback-detail :deep(.n-descriptions-table-header) {
  font-weight: 500;
  width: 100px;
}

.feedback-chat {
  margin-top: 24px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.chat-message {
  margin-bottom: 16px;
  max-width: 100%;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.message-header-developer {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.sender {
  margin-right: 8px;
  font-weight: 500;
}

.time {
  color: #999;
}

.message-content {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.5;
  word-wrap: break-word;
}

.user-message .message-content {
  background-color: #e8f4ffe3;
  color: black;
  border: 1px solid #d0e8ff;
  margin-left: 5%;
}

.developer-message .message-content {
  background-color: #5fbc229c;
  border: 1px solid #e0e0e0;
  margin-right: 5%;
}

.no-reply {
  margin-top: 16px;
  text-align: center;
}
</style>