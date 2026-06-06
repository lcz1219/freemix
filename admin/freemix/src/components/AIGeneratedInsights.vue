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
          <template v-if="currentInsight && currentInsight.trim() !== ''">
            <div class="answer-content" v-html="parseMarkdown(currentInsight)"></div>
          </template>
          <div v-else class="empty-insight-state">
            <div class="empty-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9.663 17H4.337C2.435 17 1 15.45 1 13.5C1 11.55 2.435 10 4.337 10H4.5C4.5 6.13 7.635 3 11.5 3C14.894 3 17.774 5.324 18.624 8.5C20.112 8.5 21.5 9.88 21.5 11.5C21.5 13.12 20.112 14.5 18.624 14.5H17.5M12 17L12 21M9 20L15 20" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 13V9M12 13L10 11M12 13L14 11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <p class="empty-title">暂无智能洞察</p>
            <p class="empty-desc">点击下方按钮，让 AI 分析你的目标数据</p>
            <n-button size="small" tertiary @click="refreshInsights" class="empty-refresh-btn">
              <template #icon>
                <n-icon>
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M23 4v6h-6"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
                  </svg>
                </n-icon>
              </template>
              生成洞察
            </n-button>
          </div>
        </div>
      </div>
    </div>
  </n-card>
  <AIAssistantMsg v-show="false" ref="aiAssistantMsg" />
</template>

<script setup>
import { ref, onMounted, computed, inject, nextTick } from 'vue';
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

  if(!currentInsight){
    refreshInsights()
    return;
  }
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
    currentInsight.value = list[randomIndex].currentInsight
    // currentInsight.value = list[2].currentInsight
    setTimeout(() => {
      loading.value = false;
    }, 800);
    return;
  }
  nextTick(async () => {
    let time =0
   const res = await aiAssistantMsg.value.callCustomAIAPI(insightsPrompt({ examples: insights.value.join("\n") }), (res1) => {
      // console.log(res1);
    });
    // console.log("res",res);
     if(res.messageType == "answer"){
      // console.log("res.content",res.content);
        
        if(res.success){
          currentInsight.value = res.content;
          saveAIInsightsToServer(currentInsight.value);
        }else{
          time++
          if(time < 2){
            refreshInsights()
          }
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

});
</script>

<style scoped>
.ai-insights-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 20px !important;
  transition: all 0.3s ease;
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
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00c9a7;
}

.card-title-ai {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  background: white;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-image: linear-gradient(135deg, #fff 0%, #a0a0c0 100%);
}

.feature-card-light .card-title-ai {
  background: none;
  -webkit-text-fill-color: initial;
  color: #1a1a1a;
  background-image: none;
}

.refresh-btn {
  color: rgba(255, 255, 255, 0.4);
  transition: all 0.2s;
}

.refresh-btn:hover {
  color: #00c9a7;
  transform: rotate(15deg);
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
}

.insight-card-inner {
  position: relative;
  padding: 1.5rem;
  min-height: 180px;
  border: 1px solid rgba(0, 201, 167, 0.2);
  border-radius: 12px 24px 24px 12px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.3);
}

.dark .insight-card-inner {
  background: linear-gradient(135deg, rgba(0, 201, 167, 0.08) 0%, rgba(0, 0, 0, 0.2) 100%);
  border-color: rgba(0, 201, 167, 0.25);
}

.feature-card-light .insight-card-inner {
  background: linear-gradient(135deg, rgba(0, 201, 167, 0.04) 0%, rgba(0, 0, 0, 0.02) 100%);
  border-color: rgba(0, 201, 167, 0.15);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05);
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
  background-image: radial-gradient(rgba(0, 201, 167, 0.08) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.3;
  pointer-events: none;
}

.insight-glow {
  position: absolute;
  top: -20%;
  right: -10%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(0, 201, 167, 0.12) 0%, transparent 70%);
  filter: blur(40px);
  pointer-events: none;
  z-index: 0;
  animation: pulse-glow 4s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.2); }
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

/* 空状态样式 - 扁平化设计 */
.empty-insight-state {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 0.5rem 0;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  color: rgba(0, 201, 167, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.empty-insight-state:hover .empty-icon {
  color: #00c9a7;
  transform: scale(1.05);
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: rgba(255, 255, 255, 0.9);
}

.feature-card-light .empty-title {
  color: #1a1a1a;
}

.empty-desc {
  font-size: 13px;
  margin: 0 0 20px 0;
  color: rgba(255, 255, 255, 0.5);
}

.feature-card-light .empty-desc {
  color: rgba(0, 0, 0, 0.5);
}

.empty-refresh-btn {
  border-radius: 20px;
  padding: 4px 16px;
  font-size: 13px;
  transition: all 0.2s;
}

.empty-refresh-btn:hover {
  transform: translateY(-2px);
}

.insight-text, .insight-text-light, .insight-footer, .more-btn, .thinking-process {
  display: none;
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