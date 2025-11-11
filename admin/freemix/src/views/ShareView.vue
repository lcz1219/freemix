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
import { postM,getM ,isSuccess} from '@/utils/request';

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
    const response = await getM(`/api/aiGen/share/user/${props.token}`);
    
    const result = await response.data;
    
    if (isSuccess(response)) {
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
  background: linear-gradient(135deg, #1a1a1a 0%, #121212 50%, #0d1b0d 100%);
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.share-view::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(129, 198, 131, 0.1) 0%, transparent 50%, rgba(76, 175, 80, 0.05) 100%);
  pointer-events: none;
}

.share-container {
  width: 100%;
  max-width: 800px;
  position: relative;
  z-index: 1;
}

.share-card {
  max-height: 90vh;
  overflow-y: auto;
  background: rgba(37, 37, 37, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(129, 198, 131, 0.2);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(129, 198, 131, 0.1);
}

.share-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(129, 198, 131, 0.2);
  background: linear-gradient(90deg, rgba(129, 198, 131, 0.1), transparent);
  border-radius: 12px;
  padding: 20px;
  margin: -16px -16px 30px -16px;
}

.share-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(90deg, #81c683, #4CAF50);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
}

.share-subtitle {
  color: #a0a0a0;
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
  background: rgba(129, 198, 131, 0.1);
  padding: 16px;
  border-radius: 12px;
  border-left: 4px solid #4CAF50;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(129, 198, 131, 0.2);
}

.goal-description h4 {
  margin: 0 0 8px 0;
  color: #81c683;
  font-size: 16px;
  font-weight: 600;
}

.goal-description p {
  margin: 0;
  color: #e0e0e0;
  font-size: 16px;
  line-height: 1.6;
}

.ai-response-section {
  margin-bottom: 30px;
}

.ai-response-section h4 {
  color: #81c683;
  font-size: 18px;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.ai-response {
  background: rgba(37, 37, 37, 0.6);
  padding: 20px;
  border-radius: 12px;
  color: #e0e0e0;
  line-height: 1.7;
  border: 1px solid rgba(129, 198, 131, 0.1);
  backdrop-filter: blur(10px);
}

.subgoals-section {
  margin-bottom: 30px;
}

.subgoals-section h4 {
  color: #81c683;
  font-size: 18px;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.share-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid rgba(129, 198, 131, 0.2);
  text-align: center;
  background: rgba(129, 198, 131, 0.05);
  border-radius: 12px;
  padding: 24px 20px;
  margin-left: -16px;
  margin-right: -16px;
  margin-bottom: -16px;
}

.share-time {
  color: #a0a0a0;
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
  background: rgba(37, 37, 37, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(129, 198, 131, 0.2);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.loading-content {
  text-align: center;
  padding: 40px 20px;
}

.loading-text {
  margin: 16px 0 0 0;
  color: #a0a0a0;
  font-size: 16px;
}

.error-content {
  padding: 20px;
  color: #e0e0e0;
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

/* 亮色主题适配（可选） */
.light .share-card {
  background: rgba(248, 249, 250, 0.95);
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.light .share-title {
  background: linear-gradient(90deg, #4CAF50, #388E3C);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.light .share-subtitle {
  color: #666;
}

.light .goal-description {
  background: rgba(129, 198, 131, 0.1);
  border: 1px solid rgba(129, 198, 131, 0.3);
}

.light .goal-description h4 {
  color: #4CAF50;
}

.light .goal-description p {
  color: #333;
}

.light .ai-response-section h4,
.light .subgoals-section h4 {
  color: #4CAF50;
}

.light .ai-response {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(129, 198, 131, 0.2);
  color: #333;
}

.light .share-time {
  color: #666;
}

.light .share-footer {
  background: rgba(129, 198, 131, 0.05);
  border-top: 1px solid rgba(129, 198, 131, 0.3);
}

/* Markdown样式 */
.ai-response :deep(h1),
.ai-response :deep(h2),
.ai-response :deep(h3) {
  margin: 16px 0 8px 0;
  color: #81c683;
  font-weight: 600;
}

.ai-response :deep(h1) {
  font-size: 1.5em;
  border-bottom: 1px solid rgba(129, 198, 131, 0.3);
  padding-bottom: 8px;
}

.ai-response :deep(h2) {
  font-size: 1.3em;
}

.ai-response :deep(h3) {
  font-size: 1.1em;
}

.ai-response :deep(p) {
  margin: 12px 0;
  line-height: 1.7;
}

.ai-response :deep(ul),
.ai-response :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.ai-response :deep(li) {
  margin: 6px 0;
  line-height: 1.6;
}

.ai-response :deep(strong) {
  color: #81c683;
  font-weight: 600;
}

.ai-response :deep(em) {
  color: #a0a0a0;
  font-style: italic;
}

.ai-response :deep(code) {
  background: rgba(129, 198, 131, 0.2);
  color: #81c683;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
  font-size: 0.9em;
}

.ai-response :deep(pre) {
  background: rgba(37, 37, 37, 0.8);
  border: 1px solid rgba(129, 198, 131, 0.3);
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  margin: 16px 0;
}

.ai-response :deep(pre code) {
  background: transparent;
  padding: 0;
  color: #e0e0e0;
}

.ai-response :deep(blockquote) {
  border-left: 4px solid rgba(129, 198, 131, 0.5);
  margin: 16px 0;
  padding: 8px 16px;
  background: rgba(129, 198, 131, 0.1);
  border-radius: 0 8px 8px 0;
  color: #a0a0a0;
}

.ai-response :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  border: 1px solid rgba(129, 198, 131, 0.3);
  border-radius: 8px;
  overflow: hidden;
}

.ai-response :deep(th),
.ai-response :deep(td) {
  border: 1px solid rgba(129, 198, 131, 0.2);
  padding: 8px 12px;
  text-align: left;
}

.ai-response :deep(th) {
  background: rgba(129, 198, 131, 0.1);
  color: #81c683;
  font-weight: 600;
}

.ai-response :deep(tr:nth-child(even)) {
  background: rgba(129, 198, 131, 0.05);
}

/* 亮色主题的Markdown样式 */
.light .ai-response :deep(h1),
.light .ai-response :deep(h2),
.light .ai-response :deep(h3) {
  color: #4CAF50;
}

.light .ai-response :deep(code) {
  background: rgba(129, 198, 131, 0.2);
  color: #388E3C;
}

.light .ai-response :deep(pre) {
  background: rgba(248, 249, 250, 0.9);
  border: 1px solid rgba(129, 198, 131, 0.3);
  color: #333;
}

.light .ai-response :deep(pre code) {
  color: #333;
}

.light .ai-response :deep(blockquote) {
  border-left-color: #4CAF50;
  background: rgba(129, 198, 131, 0.1);
  color: #666;
}

.light .ai-response :deep(th) {
  background: rgba(129, 198, 131, 0.1);
  color: #4CAF50;
}

.light .ai-response :deep(tr:nth-child(even)) {
  background: rgba(129, 198, 131, 0.05);
}
</style>