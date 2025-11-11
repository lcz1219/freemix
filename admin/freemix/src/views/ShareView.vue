<template>
  <div class="share-view">
    <div class="share-container">
      <n-card v-if="!loading && sharedRecord" class="share-card">
        <div class="share-header">
          <h2 class="share-title">AI目标生成分享</h2>
          <p class="share-subtitle">由 {{ sharedRecord.username || '匿名用户' }} 分享的目标</p>
        </div>
        
        <div class="share-content">
          <div class="goal-section">
            <h3 class="goal-title">{{ sharedRecord.goalTitle }}</h3>
            <div class="goal-description">
              <h4>用户需求：</h4>
              <p>{{ sharedRecord.userInput }}</p>
            </div>
          </div>
          
          <div class="ai-response-section">
            <h4>AI建议：</h4>
            <div class="ai-response" v-html="formatMarkdown(sharedRecord.aiResponse)"></div>
          </div>
          
          <div v-if="sharedRecord.childGoals && sharedRecord.childGoals.length > 0" class="subgoals-section">
            <h4>详细步骤：</h4>
            <n-list bordered>
              <n-list-item v-for="(subGoal, index) in sharedRecord.childGoals" :key="index">
                <n-thing
                  :title="`步骤 ${index + 1}`"
                  :description="subGoal.message"
                />
              </n-list-item>
            </n-list>
          </div>
          
          <div class="share-footer">
            <p class="share-time">创建时间：{{ formatDate(sharedRecord.createdAt) }}</p>
            <div class="share-actions">
              <n-button @click="generateSimilar" type="primary">
                <template #icon>
                  <n-icon><Sparkles /></n-icon>
                </template>
                生成类似目标
              </n-button>
              <n-button @click="goBack" quaternary>
                <template #icon>
                  <n-icon><ArrowBack /></n-icon>
                </template>
                返回首页
              </n-button>
            </div>
          </div>
        </div>
      </n-card>
      
      <!-- 加载状态 -->
      <div v-else-if="loading" class="loading-container">
        <n-card class="loading-card">
          <div class="loading-content">
            <n-spin size="large" />
            <p class="loading-text">加载分享内容中...</p>
          </div>
        </n-card>
      </div>
      
      <!-- 错误状态 -->
      <div v-else class="error-container">
        <n-card class="error-card">
          <div class="error-content">
            <n-result
              status="error"
              title="分享链接已失效"
              description="该分享链接可能不存在、已过期或已被删除"
            >
              <template #footer>
                <n-space justify="center">
                  <n-button @click="goBack" type="primary">返回首页</n-button>
                  <n-button @click="generateNew" quaternary>生成新目标</n-button>
                </n-space>
              </template>
            </n-result>
          </div>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage, NCard, NList, NListItem, NThing, NButton, NIcon, NSpace, NSpin, NResult } from 'naive-ui';
import { Sparkles, ArrowBack } from '@vicons/ionicons5';
import { marked } from 'marked';

// Props
const props = defineProps({
  token: {
    type: String,
    required: true
  }
});

// 响应式数据
const sharedRecord = ref(null);
const loading = ref(true);
const message = useMessage();
const route = useRoute();
const router = useRouter();

// 方法：加载分享内容
const loadShareContent = async () => {
  try {
    const response = await fetch(`/api/aiGen/share/${props.token}`);
    const result = await response.json();
    
    if (result.success) {
      sharedRecord.value = result.data;
    } else {
      message.error(result.message || '加载分享内容失败');
    }
  } catch (error) {
    console.error('加载分享内容失败:', error);
    message.error('加载分享内容失败');
  } finally {
    loading.value = false;
  }
};

// 方法：格式化Markdown
const formatMarkdown = (content) => {
  if (!content) return '';
  try {
    return marked(content);
  } catch (error) {
    console.error('Markdown解析失败:', error);
    return content;
  }
};

// 方法：格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 方法：生成类似目标
const generateSimilar = () => {
  if (sharedRecord.value) {
    // 跳转到目标生成页面，并预填类似的需求
    router.push({
      path: '/',
      query: {
        prefill: sharedRecord.value.userInput
      }
    });
  }
};

// 方法：返回首页
const goBack = () => {
  router.push('/');
};

// 方法：生成新目标
const generateNew = () => {
  router.push('/');
};

// 生命周期
onMounted(() => {
  loadShareContent();
});
</script>

<style scoped>
.share-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.share-container {
  width: 100%;
  max-width: 800px;
}

.share-card {
  max-height: 90vh;
  overflow-y: auto;
}

.share-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.share-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.share-subtitle {
  color: #666;
  font-size: 16px;
  margin: 0;
}

.share-content {
  line-height: 1.6;
}

.goal-section {
  margin-bottom: 30px;
}

.goal-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.goal-description {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.goal-description h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.goal-description p {
  margin: 0;
  color: #555;
  font-size: 16px;
}

.ai-response-section {
  margin-bottom: 30px;
}

.ai-response-section h4 {
  color: #333;
  font-size: 18px;
  margin: 0 0 12px 0;
}

.ai-response {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  color: #444;
  line-height: 1.7;
}

.subgoals-section {
  margin-bottom: 30px;
}

.subgoals-section h4 {
  color: #333;
  font-size: 18px;
  margin: 0 0 12px 0;
}

.share-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
  text-align: center;
}

.share-time {
  color: #666;
  font-size: 14px;
  margin: 0 0 20px 0;
}

.share-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.loading-container,
.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.loading-card,
.error-card {
  width: 100%;
  max-width: 500px;
}

.loading-content {
  text-align: center;
  padding: 40px 20px;
}

.loading-text {
  margin: 16px 0 0 0;
  color: #666;
}

.error-content {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .share-view {
    padding: 10px;
  }
  
  .share-title {
    font-size: 24px;
  }
  
  .share-actions {
    flex-direction: column;
  }
  
  .share-actions .n-button {
    width: 100%;
  }
}

/* 暗色主题适配 */
.dark .share-view {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
}

.dark .share-title {
  color: #e0e0e0;
}

.dark .share-subtitle {
  color: #a0a0a0;
}

.dark .goal-description {
  background: #2a2a2a;
  border-left-color: #667eea;
}

.dark .goal-description h4,
.dark .ai-response-section h4,
.dark .subgoals-section h4 {
  color: #e0e0e0;
}

.dark .goal-description p {
  color: #a0a0a0;
}

.dark .ai-response {
  background: #2a2a2a;
  color: #a0a0a0;
}

.dark .share-time {
  color: #a0a0a0;
}

/* Markdown样式 */
.ai-response :deep(h1),
.ai-response :deep(h2),
.ai-response :deep(h3) {
  margin: 16px 0 8px 0;
  color: #333;
}

.ai-response :deep(p) {
  margin: 8px 0;
}

.ai-response :deep(ul),
.ai-response :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.ai-response :deep(li) {
  margin: 4px 0;
}

.ai-response :deep(code) {
  background: #f1f1f1;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}

.ai-response :deep(pre) {
  background: #f1f1f1;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 12px 0;
}

.dark .ai-response :deep(code) {
  background: #3a3a3a;
  color: #e0e0e0;
}

.dark .ai-response :deep(pre) {
  background: #3a3a3a;
}
</style>