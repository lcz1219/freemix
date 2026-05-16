<template>
  <n-card :class="isDark ? 'feature-card' : 'feature-card-light'" class="bento-card ai-insights-card">
    <div class="card-header-ai">
      <div class="header-left">
        <div class="ai-icon-wrapper">
          <n-icon size="22" color="#00c9a7">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2L4.5 20.29L5.21 21L12 18L18.79 21L19.5 20.29L12 2Z"/>
            </svg>
          </n-icon>
        </div>
        <h2 class="card-title-ai">AI 智能洞察</h2>
      </div>
      <n-button quaternary circle size="small" @click="refreshInsights" class="refresh-btn">
        <template #icon>
          <n-icon :class="{ 'spinning': loading }">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M23 4v6h-6"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
            </svg>
          </n-icon>
        </template>
      </n-button>
    </div>

    <div class="insights-body">
      <div v-if="loading" class="skeleton-wrapper">
      <n-skeleton text :repeat="2" style="margin-bottom: 12px" />
      <n-skeleton text style="width: 60%" />
      <div class="loading-hint">
        <n-icon size="18" class="pulse-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2L4.5 20.29L5.21 21L12 18L18.79 21L19.5 20.29L12 2Z"/></svg>
        </n-icon>
        <span>AI 正在分析你的目标数据...</span>
      </div>
</div>
      <div v-else class="insight-content-wrapper">
        <!-- <p :class="isDark ? 'insight-text' : 'insight-text-light'">
          {{ currentInsight }}
        </p> -->
        <div class="answer-content" v-html="parseMarkdown(currentInsight)"></div>
        <!-- <div class="insight-footer">
          <n-button text type="primary" size="small" @click="showMoreInsights" class="more-btn">
            <template #icon>
              <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M16.17 11l-5.38-5.38L12.21 4.21L19 11l-6.79 6.79l-1.41-1.41L16.17 13H4v-2h12.17z"/></svg></n-icon>
            </template>
            查看更多建议
          </n-button>
        </div> -->
      </div>
    </div>
  </n-card>
  <AIAssistantMsg v-show="false" ref="aiAssistantMsg" />
</template>

<script setup>
import { ref, onMounted, computed, inject } from 'vue';
import { NCard, NIcon, NButton, NSkeleton } from 'naive-ui';
import { useStore } from 'vuex';
import { postM } from '@/utils/request.js';
import MarkdownIt from 'markdown-it';

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true
});
const store = useStore();
// import AIAssistantMsg from './AIAssistantMsg.vue';
// import {callCustomAIAPI} from './AIAssistantMsg.vue';
const parseMarkdown = (content) => {
  if (!content) return '';
  return md.render(content);
};
const isDark = inject('isDark', ref(true));
const aiAssistantMsg = ref(null);
const loading = ref(false);
const insights = ref([
  "3 个目标将在 2 天内到期，建议优先处理。",
  "本月有 1 个关键目标未完成，已进入最后 24 小时。",
  "还有 5 个目标尚未开始，请尽快启动。",
  "2 个目标已 7 天无进展，是否继续跟进？",
  "1 个目标进度严重落后（仅完成 20%），需重点关注。",
  "最近 7 天你完成了 4 个目标，继续保持！",
  "今日无到期目标，可提前规划下周任务。",

]);
const currentInsightIndex = ref(0);
// const currentInsight = computed(() => insights.value[currentInsightIndex.value]);
const currentInsight = ref("");
const saveAIInsightsToServer = async (currentInsight) => {
  try {
    const messageData = {
      username: currentUser.value.username,
      currentInsight: currentInsight,
      createdAt: Date.now()
    };
    
    const response = await postM('ai-messages/saveInsights', messageData);
    console.log('AI消息保存成功:', response);
    return response;
  } catch (error) {
    console.error('保存AI消息到服务器失败:', error);
    return null;
  }
};
const getInsightsFromServer = async () => {
  try {
   
    const response = await postM('ai-messages/getInsights');
    console.log('AI消息获取成功:', response);
    return response;
  } catch (error) {
    console.error('获取AI消息失败:', error);
    return null;
  }
};
const refreshInsights = async () => {
  loading.value = true;
  // setTimeout(() => {
  //   currentInsightIndex.value = (currentInsightIndex.value + 1) % insights.value.length;
  //   loading.value = false;
  // }, 800);
  const getres = await getInsightsFromServer();
  const list=getres.data.data;
  if(list.length >= 3){
    currentInsightIndex.value = (currentInsightIndex.value + 1) % list.length
    currentInsight.value = list[currentInsightIndex.value].currentInsight;
    setTimeout(() => {
      loading.value = false;
    }, 800);
    return;
  }
  nextTick(async () => {
   const res = await aiAssistantMsg.value.callCustomAIAPI(`请根据用户 ${currentUser.value.username} 的目标和历史数据，
    生成三条简短的智能建议。类似这种这么简短的建议：${insights.value.join("\n")} 用一句话描述（
    不超过90字,总字数不能超过300个字,直接引用具体目标名称,给出可执行的行动建议`, (res1) => {
      // console.log(res1);
    });
    // console.log("res",res);
     if(res.messageType == "answer"){
      // console.log("res.content",res.content);
        currentInsight.value = res.content;
        if(res.success){
          saveAIInsightsToServer(currentInsight.value);
        }
      }
      loading.value = false;

  });
};
const currentUser = computed(() => {
  return store.state.user;
});
const showMoreInsights = () => {
  console.log("查看更多 AI 建议");
};

onMounted(() => {
  setTimeout(() => {
    refreshInsights();
  }, 1000);
});
</script>

<style scoped>
.ai-insights-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  /* background: rgba(30, 30, 42, 0.4) !important; */
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 20px !important;
}
.loading-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
}

.light .loading-hint {
  color: rgba(0, 0, 0, 0.5);
}

.pulse-icon {
  animation: soft-pulse 1.5s infinite;
  color: #00c9a7;
}

@keyframes soft-pulse {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.15); }
}
.card-header-ai {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-icon-wrapper {
  width: 36px;
  height: 36px;
  /* background: #00c9a7; */
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-title-ai {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  background: white;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.feature-card-light .card-title-ai {
  background: none;
  -webkit-text-fill-color: initial;
  color: #1a1a1a;
}

.refresh-btn {
  color: rgba(255, 255, 255, 0.4);
}

.refresh-btn:hover {
  color: #a855f7;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.insights-body {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.insight-content-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.insight-text {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  font-weight: 400;
}

.insight-text-light {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(0, 0, 0, 0.75);
  margin: 0;
}

.insight-footer {
  margin-top: 20px;
}

.more-btn {
  font-weight: 600;
  letter-spacing: 0.5px;
  opacity: 0.8;
  transition: all 0.3s ease;
}

.more-btn:hover {
  opacity: 1;
  transform: translateX(4px);
}

.skeleton-wrapper {
  padding-top: 4px;
}
.answer-content,
.thinking-process {
  line-height: 1.8;
  font-size: 14px;
  color: inherit;
}

.answer-content :deep(h1),
.answer-content :deep(h2),
.answer-content :deep(h3),
.answer-content :deep(h4),
.thinking-process :deep(h1),
.thinking-process :deep(h2),
.thinking-process :deep(h3),
.thinking-process :deep(h4) {
  margin: 1.5em 0 1em;
  font-weight: 600;
  line-height: 1.3;
}

.answer-content :deep(h1),
.thinking-process :deep(h1) {
  font-size: 1.8em;
  border-bottom: 2px solid rgba(0, 201, 167, 0.2);
  padding-bottom: 0.3em;
}

.answer-content :deep(h2),
.thinking-process :deep(h2) {
  font-size: 1.5em;
  border-bottom: 1px solid rgba(0, 201, 167, 0.1);
  padding-bottom: 0.2em;
}

.answer-content :deep(h3),
.thinking-process :deep(h3) {
  font-size: 1.25em;
  color: #00c9a7;
}

.answer-content :deep(p),
.thinking-process :deep(p) {
  margin: 1em 0;
}

.answer-content :deep(ul),
.answer-content :deep(ol),
.thinking-process :deep(ul),
.thinking-process :deep(ol) {
  margin: 1em 0;
  padding-left: 1.5em;
}

.answer-content :deep(li),
.thinking-process :deep(li) {
  margin: 0.5em 0;
}

.answer-content :deep(code),
.thinking-process :deep(code) {
  background-color: rgba(0, 201, 167, 0.1);
  color: #00c9a7;
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 0.9em;
}

.answer-content :deep(pre),
.thinking-process :deep(pre) {
  background-color: var(--card-bg);
  padding: 1em;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1em 0;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.answer-content :deep(pre code),
.thinking-process :deep(pre code) {
  background-color: transparent;
  color: var(--text-color);
  padding: 0;
  border-radius: 0;
  font-size: 0.9em;
}

.answer-content :deep(blockquote),
.thinking-process :deep(blockquote) {
  margin: 1em 0;
  padding: 0.5em 1em;
  color: #666;
  border-left: 4px solid #00c9a7;
  background: rgba(0, 201, 167, 0.05);
  border-radius: 0 4px 4px 0;
}

.answer-content :deep(table),
.thinking-process :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
  font-size: 0.9em;
}

.answer-content :deep(th),
.answer-content :deep(td),
.thinking-process :deep(th),
.thinking-process :deep(td) {
  border: 1px solid rgba(0, 201, 167, 0.2);
  padding: 0.6em;
  text-align: left;
}

.answer-content :deep(th),
.thinking-process :deep(th) {
  background: rgba(0, 201, 167, 0.1);
  font-weight: 600;
}

.message.user .answer-content :deep(code) {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
}

.message.ai .answer-content :deep(h1),
.message.ai .answer-content :deep(h2),
.message.ai .answer-content :deep(h3) {
  color: #00c9a7;
}

.dark .answer-content :deep(blockquote),
.dark .thinking-process :deep(blockquote) {
  color: #aaa;
  background: rgba(0, 201, 167, 0.1);
}

/* 暗色主题下的Markdown样式 */
.dark .answer-content :deep(h1),
.dark .answer-content :deep(h2),
.dark .answer-content :deep(h3),
.dark .answer-content :deep(h4),
.dark .answer-content :deep(h5),
.dark .answer-content :deep(h6),
.dark .thinking-process :deep(h1),
.dark .thinking-process :deep(h2),
.dark .thinking-process :deep(h3),
.dark .thinking-process :deep(h4),
.dark .thinking-process :deep(h5),
.dark .thinking-process :deep(h6) {
  color: #00c9a7;
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.dark .answer-content :deep(code),
.dark .thinking-process :deep(code) {
  background-color: rgba(0, 201, 167, 0.2);
}

.dark .answer-content :deep(pre),
.dark .thinking-process :deep(pre) {
  background-color: #161b22;
  border-color: rgba(255, 255, 255, 0.1);
}

.dark .answer-content :deep(pre code),
.dark .thinking-process :deep(pre code) {
  color: #e0e0e0;
}

.dark .answer-content :deep(blockquote),
.dark .thinking-process :deep(blockquote) {
  color: #8b949e;
  border-left-color: #00c9a7;
  background: rgba(0, 201, 167, 0.1);
}

.dark .answer-content :deep(th),
.dark .answer-content :deep(td),
.dark .thinking-process :deep(th),
.dark .thinking-process :deep(td) {
  border-color: rgba(255, 255, 255, 0.1);
}
</style>
