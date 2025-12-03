<template>
  <div class="ai-goal-generator">
    <!-- 用户输入区域 -->
    <div class="input-section">
      <van-field
        v-model="userInput"
        type="textarea"
        placeholder="请输入您想要生成的目标，例如：学习Web开发"
        :autosize="{ minRows: 3, maxRows: 6 }"
        :disabled="isGenerating"
        class="user-input"
      />
      <div class="action-buttons">
        <van-button @click="showHistory" size="small" plain class="history-button">
          <van-icon name="clock-o" /> 历史记录
        </van-button>
        <van-button 
          @click="generateGoal" 
          :loading="isGenerating" 
          :disabled="!userInput.trim() || isGenerating"
          type="primary" 
          round
          class="generate-button"
        >
          {{ isGenerating ? '生成中...' : '生成目标' }}
        </van-button>
      </div>
    </div>

    <!-- 错误提示 -->
    <van-notice-bar v-if="errorMessage" type="error" :text="errorMessage" closable @close="errorMessage = ''" class="error-message" />

    <!-- AI过程查看按钮 -->
    <div v-if="chatMessages.length > 0" class="process-button-container">
      <van-button @click="showChatProcess = true" plain type="primary" size="small" block>
        查看AI思考过程
      </van-button>
    </div>

    <!-- AI生成结果展示 -->
    <div v-if="generatedGoal" class="result-section">
      <div class="result-card">
        <div class="card-header">
          <span class="card-title">AI生成的目标</span>
        </div>
        <div class="card-content">
          <div class="goal-preview">
            <h3 class="goal-title">{{ generatedGoal.title }}</h3>
            <p class="goal-description">{{ generatedGoal.description }}</p>

            <div class="sub-goals">
              <h4 class="sub-goals-title">子目标：</h4>
              <van-list :border="true" class="sub-goals-list">
                <van-cell 
                  v-for="(subGoal, index) in generatedGoal.childGoals" 
                  :key="index"
                  :title="`步骤 ${index + 1}`"
                  :value="subGoal.message"
                  :bordered="true"
                  class="sub-goal-item"
                >
                  <template #right-icon>
                    <van-icon name="arrow-right" color="#ccc" />
                  </template>
                </van-cell>
              </van-list>
            </div>
          </div>

          <div class="confirmation-buttons">
            <van-button @click="resetGeneration" plain size="small">重新生成</van-button>
            <van-button @click="saveForLater" :loading="isSaving" size="small">稍后决定</van-button>
            <van-button @click="confirmGoal" type="primary" size="small">确认创建</van-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 初始状态提示 -->
    <div v-else-if="!isGenerating && !errorMessage" class="initial-state">
      <van-empty description="请输入目标描述，AI将为您生成详细的执行步骤">
        <template #image>
          <van-icon name="chat-o" size="64" color="#00c9a7" />
        </template>
        <template #description>
          <div class="empty-description">
            <h3>AI目标生成</h3>
            <p>输入您的目标，AI将为您生成详细的执行步骤</p>
            <div class="examples">
              <p>例如：</p>
              <div class="example-tags">
                <van-tag size="small" plain>学习Web开发</van-tag>
                <van-tag size="small" plain>制定健身计划</van-tag>
                <van-tag size="small" plain>准备旅行攻略</van-tag>
              </div>
            </div>
          </div>
        </template>
      </van-empty>
    </div>

    <!-- AI思考过程弹窗 -->
    <van-popup v-model:show="showChatProcess" position="bottom" :style="{ height: '60%', left: '50%', width: '50%' }" round closeable>
      <div class="chat-process-modal">
        <div class="modal-header">
          <h3>AI思考过程</h3>
        </div>
        <div class="chat-messages">
           <div v-for="(msg, index) in chatMessages" :key="index" class="chat-message" :class="msg.type">
             <div class="message-content">
               <div v-if="msg.thinkingContent" class="thinking-content">
                 <div class="thinking-header">思考过程:</div>
                 <div v-html="formatContent(msg.thinkingContent)"></div>
               </div>
               <div class="main-content" v-html="formatContent(msg.content)"></div>
             </div>
           </div>
        </div>
      </div>
    </van-popup>

    <!-- 目标确认模态框 -->
    <van-popup v-model:show="showConfirmationModal" position="bottom" :style="{ height: '80%',left: '50%', width: '50%' }" round closeable>
      <div class="confirmation-modal">
        <h3 class="modal-title">确认创建目标</h3>
        <div class="modal-content">
          <van-form @submit="handleGoalCreated">
            <van-cell-group inset title="基本信息">
              <van-field
                v-model="goalData.title"
                name="title"
                label="目标标题"
                placeholder="目标标题"
                :rules="[{ required: true, message: '请填写目标标题' }]"
              />
              <van-field
                v-model="goalData.description"
                name="description"
                label="目标描述"
                type="textarea"
                placeholder="目标描述"
                autosize
              />
            </van-cell-group>
            
            <van-cell-group inset title="设置">
              <van-field
                v-model="goalData.deadlineStr"
                is-link
                readonly
                name="deadline"
                label="截止日期"
                placeholder="点击选择日期"
                @click="showDatePicker = true"
              />
              <van-popup v-model:show="showDatePicker" position="bottom">
                <van-date-picker
                  @confirm="onConfirmDate"
                  @cancel="showDatePicker = false"
                  :min-date="new Date()"
                />
              </van-popup>

              <van-field
                v-model="goalData.levelLabel"
                is-link
                readonly
                name="level"
                label="优先级"
                placeholder="点击选择优先级"
                @click="showLevelPicker = true"
              />
              <van-popup v-model:show="showLevelPicker" position="bottom">
                <van-picker
                  :columns="priorityOptions"
                  @confirm="onConfirmLevel"
                  @cancel="showLevelPicker = false"
                />
              </van-popup>
            </van-cell-group>

            <van-cell-group inset title="子目标" class="sub-goals-group">
              <div v-for="(subGoal, index) in goalData.childGoals" :key="index" class="sub-goal-input">
                <van-field
                  v-model="subGoal.message"
                  :label="`步骤 ${index + 1}`"
                  placeholder="子目标内容"
                  :rules="[{ required: true, message: '请填写子目标内容' }]"
                />
              </div>
            </van-cell-group>

            <div class="modal-actions">
              <van-button round block type="primary" native-type="submit" :loading="confirmLoading">
                确认创建
              </van-button>
            </div>
          </van-form>
        </div>
      </div>
    </van-popup>

    <!-- 历史记录弹窗 -->
    <van-popup v-model:show="showHistoryModal" position="bottom" :style="{ height: '80%' }" round>
      <div class="history-modal">
        <div class="history-modal-header">
          <h3>AI生成记录</h3>
          <van-icon name="close" @click="showHistoryModal = false" class="close-icon" />
        </div>
        
        <!-- 搜索和筛选 -->
        <div class="history-filter">
          <van-search v-model="searchKeyword" placeholder="搜索记录..." shape="round" background="transparent" />
        </div>

        <div class="history-modal-content">
          <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
            <van-list
              v-model:loading="loading"
              :finished="finished"
              finished-text="没有更多了"
              @load="onLoadHistory"
            >
              <div v-if="historyRecords.length === 0 && !loading" class="history-empty">
                <van-empty description="暂无历史记录" />
              </div>
              
              <van-swipe-cell v-for="(record, index) in historyRecords" :key="record.id" class="history-swipe-item">
                <van-cell 
                  class="history-item"
                  @click="useHistoryRecord(record)"
                >
                  <template #title>
                    <div class="history-title">
                      {{ record.goalTitle || '未命名目标' }}
                      <van-tag :type="getStatusTagType(record.status)" size="mini" class="status-tag">
                        {{ getStatusText(record.status) }}
                      </van-tag>
                    </div>
                  </template>
                  <template #label>
                    <div class="history-description">{{ record.userInput }}</div>
                    <div class="history-time">{{ formatDate(record.createdAt) }}</div>
                  </template>
                  <template #right-icon>
                     <van-icon name="arrow" color="#ccc" />
                  </template>
                </van-cell>
                <template #right>
                  <van-button square text="分享" type="primary" class="delete-button" @click.stop="shareRecord(record)" />
                  <van-button square text="删除" type="danger" class="delete-button" @click.stop="deleteRecord(record)" />
                </template>
              </van-swipe-cell>
            </van-list>
          </van-pull-refresh>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, reactive, computed } from 'vue'
import { showToast, showDialog } from 'vant'
import { useStore } from 'vuex'
import MarkdownIt from 'markdown-it'
import { parseAIResponseToSubGoals, extractGoalTitle } from '@/utils/aiGoalParser.js'
import { getM, postM, isSuccess } from '@/utils/request.js'
import { createGoalObject } from '@/utils/goalUtils.js'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true
})

// 格式化内容（使用markdown渲染）
const formatContent = (content) => {
  if (!content) return ''
  return md.render(content)
}

// Props定义
const props = defineProps({
  aiAssistantRef: {
    type: Object,
    default: null
  }
})

// Emits定义
const emit = defineEmits(['goal-created'])

// Store
const store = useStore()

// 响应式数据
const userInput = ref('')
const isGenerating = ref(false)
const generatedGoal = ref(null)
const aiResponse = ref('')
const errorMessage = ref('')
const isSaving = ref(false)
const showConfirmationModal = ref(false)
const showHistoryModal = ref(false)
const showChatProcess = ref(false)
const chatMessages = ref([])
const confirmLoading = ref(false)

// 历史记录相关
const historyRecords = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const searchKeyword = ref('')
const page = ref(0)
const pageSize = 20

// 目标确认表单数据
const goalData = reactive({
  title: '',
  description: '',
  childGoals: [],
  deadline: null,
  deadlineStr: '',
  level: 'medium',
  levelLabel: '中',
  tags: []
})

const showDatePicker = ref(false)
const showLevelPicker = ref(false)

const priorityOptions = [
  { text: '低', value: 'low' },
  { text: '中', value: 'medium' },
  { text: '高', value: 'high' }
]

// 本地AI API调用方法（当没有aiAssistantRef prop时使用）
const callLocalAIAPI = async (question, onUpdate) => {
  const API_ENDPOINT = 'https://api.coze.cn/open_api/v1/chat'
  const PERSONAL_ACCESS_TOKEN = 'sat_alIbwyaIhODXfXtTHCuj74C3swKTZd08L82jZDfMsfzplbENrkX5bu3ddTU5VHdn'
  const BOT_ID = '7569182284998524934'
  
  try {
    const response = await fetch(API_ENDPOINT, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${PERSONAL_ACCESS_TOKEN}`,
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({
        bot_id: BOT_ID,
        user: 'ea16730874-single_user',
        query: `${question}用markdown的格式返回`,
        stream: true
      })
    })

    if (!response.ok) {
      throw new Error(`API请求失败: ${response.status} ${response.statusText}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let done = false
    let fullResponse = ''
    let thinkingContent = ''
    let buffer = ''
    
    while (!done) {
      const { value, done: readerDone } = await reader.read()
      done = readerDone
      
      if (value) {
        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk
        const lines = buffer.split('\n')
        // 保留最后一行可能不完整的buffer
        buffer = lines.pop() || ''
        
        for (const line of lines) {
          if (line.trim() === '') continue
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data === '[DONE]') {
              done = true
              break
            }
            
            try {
              const jsonData = JSON.parse(data)
              if (jsonData.message) {
                switch (jsonData.message.type) {
                  case 'answer':
                    if (jsonData.message.content) {
                      fullResponse += jsonData.message.content
                    }
                    if (onUpdate) {
                      onUpdate({
                        messageType: 'answer',
                        content: fullResponse,
                        thinkingContent: thinkingContent,
                        isProcessing: true
                      })
                    }
                    break
                  case 'verbose':
                  case 'thinking': // 兼容不同模型的思考过程类型
                    if (jsonData.message.reasoning_content || jsonData.message.content) {
                      thinkingContent += (jsonData.message.reasoning_content || jsonData.message.content)
                    }
                    if (onUpdate) {
                      onUpdate({
                        messageType: 'verbose',
                        content: fullResponse,
                        thinkingContent: thinkingContent,
                        isProcessing: true
                      })
                    }
                    break
                }
              }
            } catch (e) {
              // ignore parse error
            }
          }
        }
      }
    }
    
    return {
      messageType: 'answer',
      content: fullResponse,
      thinkingContent: thinkingContent
    }
  } catch (error) {
    console.error('AI API调用失败:', error)
    throw error
  }
};

// 生成目标
const generateGoal = async () => {
  if (!userInput.value.trim()) return

  isGenerating.value = true
  errorMessage.value = ''
  generatedGoal.value = null
  chatMessages.value = []

  // 创建处理中的消息
  const processingMessageIndex = chatMessages.value.length
  chatMessages.value.push({
    type: 'ai-gen',
    messageType: 'processing',
    content: '',
    thinkingContent: '',
    isProcessing: true,
    timestamp: Date.now()
  })

  try {
    let response;
    const updateCallback = (messageData) => {
      if (chatMessages.value[processingMessageIndex]) {
        const message = chatMessages.value[processingMessageIndex]
        message.messageType = messageData.messageType
        message.content = messageData.content || message.content
        message.thinkingContent = messageData.thinkingContent
        message.isProcessing = messageData.isProcessing
      }
    }

    if (props.aiAssistantRef && typeof props.aiAssistantRef.callCustomAIAPI === 'function') {
      response = await props.aiAssistantRef.callCustomAIAPI(userInput.value, updateCallback)
    } else {
      response = await callLocalAIAPI(userInput.value, updateCallback)
    }

    // 确保有内容返回
    if (!response || !response.content) {
       throw new Error('AI未返回有效内容')
    }

    aiResponse.value = response.content

    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex]
      message.messageType = 'answer'
      message.content = response.content
      message.isProcessing = false
    }

    // 解析子目标
    let subGoalsMsgContent = ''
    const subGoalsPrompt = `请分析以下内容并提取步骤，以序号列表形式返回：\n${response.content}\n\n要求：\n1. 只返回步骤列表，不要额外解释\n2. 格式为：(1) 第一步 (2) 第二步 ...\n3.不需要概括，尽可能的复制AI回复中的内容\n4. 步骤的序号必须连续且递增`

    try {
      if (props.aiAssistantRef && typeof props.aiAssistantRef.callCustomAIAPI === 'function') {
         const subGoalsMsg = await props.aiAssistantRef.callCustomAIAPI(subGoalsPrompt, () => {})
        subGoalsMsgContent = subGoalsMsg.content
      } else {
        const subGoalsMsg = await callLocalAIAPI(subGoalsPrompt, () => {})
        subGoalsMsgContent = subGoalsMsg.content
      }
    } catch (e) {
      console.warn('提取子目标失败，使用原始内容', e)
      subGoalsMsgContent = response.content
    }

    const subGoals = parseAIResponseToSubGoals(subGoalsMsgContent)
    
    // 如果解析结果为空，尝试直接使用原始内容构建一个步骤
    if (subGoals.length === 0) {
      subGoals.push({
         _id: `node-${Date.now()}-1`,
         message: response.content.substring(0, 100) + (response.content.length > 100 ? '...' : ''),
         finish: false,
         fileList: []
      })
    }

    const title = extractGoalTitle(response.content, userInput.value)

    generatedGoal.value = {
      title: title,
      description: userInput.value,
      childGoals: subGoals
    }
   generatedGoal.value= JSON.parse(JSON.stringify(generatedGoal.value))
    
    showToast({ type: 'success', message: '目标生成成功' })
  } catch (error) {
    console.error('生成目标时发生错误:', error)
    errorMessage.value = error.message || '生成目标时发生错误，请稍后再试'
    
    if (chatMessages.value[processingMessageIndex]) {
      const message = chatMessages.value[processingMessageIndex]
      message.type = 'error'
      message.content = errorMessage.value
      message.isProcessing = false
    }
    
    showToast({ type: 'error', message: errorMessage.value })
  } finally {
    isGenerating.value = false
  }
}

// 重置生成状态
const resetGeneration = () => {
  userInput.value = ''
  generatedGoal.value = null
  aiResponse.value = ''
  errorMessage.value = ''
  chatMessages.value = []
}

// 稍后决定：保存生成记录
const saveForLater = async () => {
  if (!generatedGoal.value) return
  
  isSaving.value = true
  try {
    const saveData = {
      userInput: userInput.value,
      aiResponse: aiResponse.value,
      goalTitle: generatedGoal.value.title,
      childGoals: generatedGoal.value.childGoals,
      chatMessages: chatMessages.value,
    }
    
    const response = await postM('api/aiGen/save', {
      username: store.state.user.username,
      saveData: saveData
    })
    
    if (isSuccess(response)) {
      showToast({ type: 'success', message: '记录已保存，30天内可随时决定是否使用' })
      resetGeneration()
    } else {
      showToast({ type: 'error', message: response.data?.msg || '保存失败' })
    }
  } catch (error) {
    console.error('保存记录失败:', error)
    showToast({ type: 'error', message: '保存失败，请检查网络连接' })
  } finally {
    isSaving.value = false
  }
}

// 确认创建目标
const confirmGoal = () => {
  // 初始化表单数据
  const oneWeekLater = new Date()
  oneWeekLater.setDate(oneWeekLater.getDate() + 7)
  
  goalData.title = generatedGoal.value.title
  goalData.description = generatedGoal.value.description
  goalData.childGoals = JSON.parse(JSON.stringify(generatedGoal.value.childGoals))
  goalData.deadline = oneWeekLater
  const dateStr = oneWeekLater.toISOString().split('T')[0].split('-')
  goalData.deadlineStr = `${dateStr[0]}/${dateStr[1]}/${dateStr[2]}`
  goalData.level = 'medium'
  goalData.levelLabel = '中'
  goalData.tags = ['ai生成']
  
  showConfirmationModal.value = true
}

// 日期选择确认
const onConfirmDate = ({ selectedValues }) => {
  goalData.deadlineStr = selectedValues.join('/')
  goalData.deadline = new Date(selectedValues.join('-'))
  showDatePicker.value = false
}

// 优先级选择确认
const onConfirmLevel = ({ selectedOptions }) => {
  goalData.level = selectedOptions[0].value
  goalData.levelLabel = selectedOptions[0].text
  showLevelPicker.value = false
}

// 处理目标创建完成
const handleGoalCreated = async () => {
  confirmLoading.value = true
  try {
    const goalObject = createGoalObject({
      title: goalData.title,
      description: goalData.description,
      childGoals: goalData.childGoals,
      deadline: goalData.deadline,
      level: goalData.level,
      tags: goalData.tags
    })

    const response = await postM('editGoal', goalObject)
    
    if (isSuccess(response)) {
      showToast({ type: 'success', message: '目标创建成功' })
      showConfirmationModal.value = false
      emit('goal-created', goalObject)
      resetGeneration()
    } else {
      showToast({ type: 'error', message: '目标创建失败: ' + (response.data?.message || '未知错误') })
    }
  } catch (error) {
    console.error('创建目标时发生错误:', error)
    showToast({ type: 'error', message: '创建目标时发生错误: ' + error.message })
  } finally {
    confirmLoading.value = false
  }
}

// 显示历史记录
const showHistory = () => {
  showHistoryModal.value = true
  historyRecords.value = []
  page.value = 0
  finished.value = false
  loading.value = true
  onLoadHistory()
}

const onLoadHistory = async () => {
  try {
    const response = await getM('/api/aiGen/list', {
      page: page.value,
      size: pageSize,
      username: store.state.user.username
    })
    
    if (isSuccess(response)) {
      const records = response.data.data.records || []
      if (refreshing.value) {
        historyRecords.value = records
        refreshing.value = false
      } else {
        historyRecords.value.push(...records)
      }
      
      page.value++
      if (records.length < pageSize) {
        finished.value = true
      }
    } else {
      finished.value = true
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    finished.value = true
  } finally {
    loading.value = false
  }
}

const onRefresh = () => {
  finished.value = false
  page.value = 0
  loading.value = true
  onLoadHistory()
}

// 使用历史记录
const useHistoryRecord = (record) => {
  showHistoryModal.value = false
  userInput.value = record.userInput
  
  if (record.status === 'pending') {
    // 恢复生成的目标视图
    generatedGoal.value = {
      title: record.goalTitle || '未命名目标',
      description: record.userInput,
      childGoals: record.childGoals || []
    }
    aiResponse.value = record.aiResponse
    // 如果有聊天记录也可以恢复，这里暂时不恢复chatMessages以简化逻辑
    // chatMessages.value = record.chatMessages || [] 
    
    // 提示用户
    showToast('已恢复历史生成记录')
  }
}

// 分享记录
const shareRecord = async (record) => {
  try {
    const response = await getM(`/api/aiGen/share/${record.id}`, { 
      username: store.state.user.username 
    });
    
    if (isSuccess(response)) {
      const shareToken = response.data.data.shareToken;
      const shareUrl = `${window.location.origin}/#/share/${shareToken}`;
      
      if (navigator.clipboard) {
        await navigator.clipboard.writeText(shareUrl);
        showToast({ type: 'success', message: '分享链接已复制' });
      } else {
        showDialog({ title: '分享链接', message: shareUrl });
      }
    } else {
      showToast({ type: 'error', message: '生成分享链接失败' });
    }
  } catch (error) {
    showToast({ type: 'error', message: '生成分享链接失败' });
  }
}

// 删除记录
const deleteRecord = async (record) => {
  showDialog({
    title: '确认删除',
    message: '确定要删除这条记录吗？',
    showCancelButton: true,
  }).then(async (action) => {
    if (action === 'confirm') {
      try {
        const response = await postM(`/api/aiGen/delete/${record.id}`, { 
          username: store.state.user.username 
        });
        if (isSuccess(response)) {
          showToast({ type: 'success', message: '记录已删除' });
          historyRecords.value = historyRecords.value.filter(r => r.id !== record.id);
        } else {
          showToast({ type: 'error', message: '删除失败' });
        }
      } catch (error) {
        showToast({ type: 'error', message: '删除失败' });
      }
    }
  });
}

// 工具函数
const formatDate = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

const getStatusTagType = (status) => {
  const typeMap = {
    pending: 'warning',
    confirmed: 'success',
    deleted: 'danger'
  }
  return typeMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待确认',
    confirmed: '已确认',
    deleted: '已删除'
  }
  return textMap[status] || '未知'
}
</script>

<style scoped lang="scss">
.ai-goal-generator {
  padding: 16px;
  background-color: var(--bg-primary);
  height: 100vh;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 100px;
  box-sizing: border-box;
  
  .input-section {
    background-color: var(--bg-secondary);
    padding: 16px;
    border-radius: 16px;
    box-shadow: var(--card-shadow);
    margin-bottom: 16px;
    
    .user-input {
      margin-bottom: 12px;
      
      :deep(.van-field__textarea-wrap) {
        border-radius: 12px;
      }
      
      :deep(.van-field__control) {
        font-size: 15px;
      }
    }
    
    .action-buttons {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .history-button {
        color: var(--text-secondary);
        font-size: 13px;
      }
      
      .generate-button {
        font-size: 14px;
        padding: 10px 20px;
      }
    }
  }
  
  .error-message {
    margin-bottom: 16px;
  }
  
  .process-button-container {
    margin-bottom: 16px;
  }

  .result-section {
    margin-bottom: 16px;
    
    .result-card {
      background-color: var(--bg-secondary);
      border-radius: 16px;
      box-shadow: var(--card-shadow);
      overflow: hidden;
      
      .card-header {
        padding: 12px 16px;
        border-bottom: 1px solid var(--border-line);
        background-color: rgba(255, 255, 255, 0.02);
        
        .card-title {
          font-size: 16px;
          font-weight: 600;
          color: var(--text-primary);
        }
      }

      .card-content {
        padding: 16px;
      }
      
      .goal-preview {
        .goal-title {
          font-size: 18px;
          font-weight: 600;
          margin: 0 0 8px;
          color: var(--text-primary);
        }
        
        .goal-description {
          font-size: 14px;
          color: var(--text-secondary);
          margin: 0 0 16px;
          line-height: 1.5;
        }
        
        .sub-goals {
          .sub-goals-title {
            font-size: 16px;
            font-weight: 500;
            margin: 0 0 8px;
            color: var(--text-primary);
          }
          
          .sub-goals-list {
            background-color: transparent;
            
            .sub-goal-item {
              background-color: transparent;
              
              :deep(.van-cell__title) {
                font-weight: 500;
              }
              
              :deep(.van-cell__value) {
                color: var(--text-secondary);
                font-size: 14px;
              }
            }
          }
        }
        
        .confirmation-buttons {
          display: flex;
          justify-content: flex-end;
          gap: 8px;
          margin-top: 16px;
        }
      }
    }
  }
  
  .initial-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    background-color: var(--bg-secondary);
    border-radius: 16px;
    box-shadow: var(--card-shadow);
    
    .empty-description {
      text-align: center;
      
      h3 {
        margin: 16px 0 8px;
        color: var(--text-primary);
        font-size: 18px;
      }
      
      p {
        margin: 0 0 16px;
        font-size: 14px;
        color: var(--text-secondary);
      }
      
      .examples {
        text-align: left;
        font-size: 14px;
        color: var(--text-secondary);
        
        p {
          margin-bottom: 8px;
        }
        
        .example-tags {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }
      }
    }
  }
  
  .confirmation-modal {
    padding-bottom: 20px;
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .modal-title {
      font-size: 18px;
      font-weight: 600;
      margin: 16px 0;
      text-align: center;
      color: var(--text-primary);
    }
    
    .modal-content {
      flex: 1;
      overflow-y: auto;
      padding-bottom: 20px;
      
      .sub-goals-group {
        margin-top: 12px;
        
        .sub-goal-input {
          margin-bottom: 8px;
        }
      }
    }
    
    .modal-actions {
      padding: 16px;
      background-color: var(--bg-primary);
    }
  }

  .chat-process-modal {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .modal-header {
      padding: 16px;
      border-bottom: 1px solid var(--border-line);
      h3 { margin: 0; text-align: center; }
    }
    
    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      
      .chat-message {
        margin-bottom: 16px;
        padding: 12px;
        border-radius: 8px;
        background-color: var(--bg-secondary);
        
        .thinking-content {
          font-size: 12px;
          color: #999;
          padding: 8px;
          background: rgba(0,0,0,0.05);
          border-radius: 4px;
          margin-bottom: 8px;
          
          .thinking-header {
            font-weight: bold;
            margin-bottom: 4px;
          }
        }
        
        .main-content {
          white-space: pre-wrap;
          line-height: 1.5;
        }
      }
    }
  }
  
  .history-modal {
    height: 100%;
    display: flex;
    flex-direction: column;
    background-color: var(--bg-primary);
    
    .history-modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      border-bottom: 1px solid var(--border-line);
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
      }
      
      .close-icon {
        font-size: 20px;
        color: var(--text-secondary);
      }
    }
    
    .history-filter {
      padding: 8px 16px;
      background-color: var(--bg-secondary);
    }
    
    .history-modal-content {
      flex: 1;
      overflow-y: auto;
      padding: 0 16px 16px;
      
      .history-empty {
        padding: 40px 20px;
        text-align: center;
      }
      
      .history-swipe-item {
        margin-bottom: 8px;
        border-radius: 12px;
        overflow: hidden;
      }
      
      .history-item {
        background-color: var(--bg-secondary);
        
        .history-title {
          font-weight: 500;
          color: var(--text-primary);
          display: flex;
          align-items: center;
          gap: 8px;
        }
        
        .history-description {
          color: var(--text-secondary);
          font-size: 13px;
          margin-top: 4px;
        }
        
        .history-time {
          font-size: 12px;
          color: #999;
          margin-top: 4px;
        }
      }
      
      .delete-button {
        height: 100%;
      }
    }
  }
}
</style>