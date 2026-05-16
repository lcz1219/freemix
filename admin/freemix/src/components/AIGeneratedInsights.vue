<template>
  <n-card :class="isDark ? 'feature-card' : 'feature-card-light'" class="bento-card ai-insights-card">
    <div class="card-header-ai">
      <div class="header-left">
        <div class="ai-icon-wrapper">
          <n-icon size="24"><AIAssistantIcon /></n-icon>
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
       <n-icon size="24"><AIAssistantIcon /></n-icon>
        <span>AI 正在分析你的目标数据...</span>
      </div>
</div>
      <div v-else class="insight-content-wrapper">
        <div class="insight-card-inner">
          <div class="insight-glow"></div>
          <div class="insight-pattern"></div>
          <!-- <div class="insight-quote-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M14.017 21L14.017 18C14.017 16.8954 14.9124 16 16.017 16H19.017C19.5693 16 20.017 15.5523 20.017 15V9C20.017 8.44772 19.5693 8 19.017 8H16.017C14.9124 8 14.017 7.10457 14.017 6V5C14.017 3.34315 15.3601 2 17.017 2H19.017C20.6739 2 22.017 3.34315 22.017 5V15C22.017 18.3137 19.3307 21 16.017 21H14.017ZM2.01697 21L2.01697 18C2.01697 16.8954 2.9124 16 4.01697 16H7.01697C7.56925 16 8.01697 15.5523 8.01697 15V9C8.01697 8.44772 7.56925 8 7.01697 8H4.01697C2.9124 8 2.01697 7.10457 2.01697 6V5C2.01697 3.34315 3.36012 2 5.01697 2H7.01697C8.67382 2 10.017 3.34315 10.017 5V15C10.017 18.3137 7.33068 21 4.01697 21H2.01697Z"/></svg>
          </div> -->
          <div class="answer-content" v-html="parseMarkdown(currentInsight)"></div>
        </div>
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
import { insightsPrompt } from '@/utils/aiPrompts.js';

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
    const randomIndex = Math.floor(Math.random() * list.length)
    currentInsightIndex.value = randomIndex
    // currentInsight.value = list[randomIndex].currentInsight
    currentInsight.value = list[2].currentInsight
    setTimeout(() => {
      loading.value = false;
    }, 800);
    return;
  }
  nextTick(async () => {
   const res = await aiAssistantMsg.value.callCustomAIAPI(insightsPrompt({ examples: insights.value.join("\n") }), (res1) => {
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
  justify-content: center;
  /* overflow-y: auto; */
}

.insight-card-inner {
  position: relative;
  padding: 1.5rem;
  min-height: 0;
  /* background: linear-gradient(135deg, rgba(0, 201, 167, 0.08) 0%, rgba(0, 201, 167, 0.01) 100%); */
  border: 1px solid rgba(0, 201, 167, 0.2);
  /* border-left: 4px solid #00c9a7; */
  border-radius: 12px 24px 24px 12px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.3);
}

.dark .insight-card-inner {
  background: linear-gradient(135deg, rgba(0, 201, 167, 0.12) 0%, rgba(0, 0, 0, 0.2) 100%);
  border-color: rgba(0, 201, 167, 0.25);
}

.insight-card-inner:hover {
  border-color: rgba(0, 201, 167, 0.5);
  box-shadow: 0 15px 40px -12px rgba(0, 201, 167, 0.2);
  transform: translateY(-4px) scale(1.01);
}

.insight-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(rgba(0, 201, 167, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.5;
  pointer-events: none;
}

.insight-quote-icon {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 80px;
  height: 80px;
  color: rgba(0, 201, 167, 0.05);
  transform: rotate(-15deg);
  pointer-events: none;
  z-index: 0;
}

.insight-glow {
  position: absolute;
  top: -20%;
  right: -10%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(0, 201, 167, 0.15) 0%, transparent 70%);
  filter: blur(40px);
  pointer-events: none;
  z-index: 0;
  animation: pulse-glow 4s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

.answer-content {
  position: relative;
  z-index: 1;
  line-height: 1.8;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.95);
  letter-spacing: 0.01em;
}

.feature-card-light .answer-content {
  color: #1a1a1a;
}

.answer-content :deep(strong) {
  color: #00c9a7;
  font-weight: 700;
  padding: 0 2px;
  background: linear-gradient(transparent 70%, rgba(0, 201, 167, 0.15) 30%);
}

.answer-content :deep(ul),
.answer-content :deep(ol) {
  margin: 0.5em 0;
  padding-left: 1.25em;
}

.answer-content :deep(li) {
  margin: 0.6em 0;
  position: relative;
  list-style-type: none;
}

.answer-content :deep(li)::before {
  content: "•";
  color: #00c9a7;
  font-weight: bold;
  display: inline-block;
  width: 1em;
  margin-left: -1em;
  font-size: 1.2em;
  vertical-align: middle;
}

.answer-content :deep(code) {
  background-color: rgba(0, 201, 167, 0.15);
  color: #00c9a7;
  padding: 0.15em 0.4em;
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 0.9em;
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.answer-content :deep(blockquote) {
  margin: 1em 0;
  padding: 0.75em 1.25rem;
  color: rgba(255, 255, 255, 0.7);
  border-left: 3px solid #00c9a7;
  background: rgba(0, 201, 167, 0.08);
  border-radius: 0 12px 12px 0;
  font-style: italic;
}

.feature-card-light .answer-content :deep(blockquote) {
  color: #57606a;
  background: rgba(0, 201, 167, 0.05);
}

.insight-text, .insight-text-light, .insight-footer, .more-btn, .thinking-process, .answer-content :deep(h1), .answer-content :deep(h2), .answer-content :deep(h3), .answer-content :deep(h4), .answer-content :deep(pre), .answer-content :deep(table) {
  display: none;
}

</style>
