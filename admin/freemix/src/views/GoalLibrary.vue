<template>
  <div class="page-container">
    <!-- Dynamic Background -->
    <div class="ambient-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
      <div class="noise-overlay"></div>
    </div>

    <n-layout :native-scrollbar="true" class="main-layout">
      <common>
        <template #content>
          <n-layout-content class="content-transparent">
            <div class="main-content">
              <!-- Header Section -->
              <section class="page-header">
                <div class="header-content">
                  <h1 class="hero-title">
                    <span class="gradient-text">星际航道</span>
                    <n-tag type="success" size="small" round class="beta-tag">BETA</n-tag>
                  </h1>
                  <p class="hero-subtitle">
                    探索 · 共鸣 · 进化 — 连接每一个向上的灵魂
                  </p>
                </div>
                
                <!-- Search & Filter -->
                <div class="filter-bar">
                  <div class="search-wrapper">
                    <n-input 
                      v-model:value="searchQuery" 
                      round 
                      placeholder="搜索感兴趣的目标..." 
                      class="search-input"
                    >
                      <template #prefix>
                        <n-icon :component="Search" />
                      </template>
                    </n-input>
                  </div>
                  
                  <div class="filter-tabs">
                    <button 
                      class="filter-tab" 
                      :class="{ active: currentTab === 'latest' }"
                      @click="currentTab = 'latest'"
                    >
                      最新发布
                    </button>
                    <button 
                      class="filter-tab" 
                      :class="{ active: currentTab === 'hot' }"
                      @click="currentTab = 'hot'"
                    >
                      热门探索
                    </button>
                  </div>
                </div>
              </section>

              <!-- Goals Feed -->
              <section class="goals-feed">
                <n-spin :show="loading">
                  <n-empty v-if="!loading && filteredGoals.length === 0" description="暂无信号，发射你的第一个目标吧！" class="empty-state">
                    <template #icon>
                      <n-icon size="48" color="rgba(255,255,255,0.3)">
                        <PlanetOutline />
                      </n-icon>
                    </template>
                  </n-empty>
                  
                  <div v-else class="goal-list">
                    <n-grid :x-gap="24" :y-gap="24" cols="1 m:2 l:3" responsive="screen">
                      <n-grid-item v-for="goal in filteredGoals" :key="goal._id">
                        <!-- Glassmorphism Card -->
                        <div class="glass-card">
                          <div class="card-glow"></div>
                          
                          <!-- Card Header -->
                          <div class="card-header">
                            <div class="user-profile">
                              <n-avatar 
                                round 
                                :size="40" 
                                :src="getOwnerAvatar(goal)" 
                                class="user-avatar"
                                fallback-src="https://api.dicebear.com/7.x/miniavs/svg?seed=1" 
                              />
                              <div class="user-meta">
                                <span class="username">{{ getOwnerName(goal) }}</span>
                                <span class="timestamp">{{ formatDate(goal.createTime) }}</span>
                              </div>
                            </div>
                            <div class="header-actions">
                              <n-tag size="small" :type="getLevelType(goal.level)" round class="level-tag" v-if="goal.level">
                                {{ getLevelLabel(goal.level) }}
                              </n-tag>
                              <n-dropdown trigger="click" :options="dropdownOptions" @select="(key) => handleDropdownSelect(key, goal)">
                                <n-button size="tiny" secondary circle class="more-btn">
                                  <template #icon>
                                    <n-icon><EllipsisHorizontal /></n-icon>
                                  </template>
                                </n-button>
                              </n-dropdown>
                            </div>
                          </div>

                          <!-- Card Body -->
                          <div class="card-body">
                            <h3 class="goal-title">{{ goal.title }}</h3>
                            <p class="goal-desc">{{ goal.description || '探索未知...' }}</p>

                            <!-- Progress Bar -->
                            <div class="progress-mini">
                              <div class="progress-track">
                                <div class="progress-fill" :style="{ width: (goal.progress || 0) + '%' }"></div>
                              </div>
                              <span class="progress-text">{{ goal.progress || 0 }}%</span>
                            </div>

                            <!-- Milestones -->
                            <div v-if="goal.childGoals && goal.childGoals.length > 0" class="milestones-container">
                              <div class="milestone-list">
                                <div v-for="(sub, idx) in goal.childGoals.slice(0, 3)" :key="idx" class="milestone-item">
                                  <n-tag type="success" round :bordered="false">
                               {{ idx + 1 }}
                              </n-tag>
                              <span>{{ sub.message }}</span>
                                </div>
                                
                                <n-collapse-transition :show="goal.expandSubgoals">
                                  <div v-for="(sub, idx) in goal.childGoals.slice(3)" :key="idx + 3" class="milestone-item">
                                     <n-tag type="success" round :bordered="false">
                               {{ idx + 4 }}
                              </n-tag>
                              <span>{{ sub.message }}</span>
                                  </div>
                                </n-collapse-transition>
                              </div>
                              
                              <div v-if="goal.childGoals.length > 3" class="expand-trigger" @click="goal.expandSubgoals = !goal.expandSubgoals">
                                <span>{{ goal.expandSubgoals ? '收起列表' : `还有 ${goal.childGoals.length - 3} 个里程碑` }}</span>
                                <n-icon :class="{ 'rotate-180': goal.expandSubgoals }"><ChevronDown /></n-icon>
                              </div>
                            </div>
                          </div>

                          <!-- Card Footer -->
                          <div class="card-footer">
                            <div class="action-group">
                              <button class="action-btn" :class="{ 'is-active': isLiked(goal) }" @click="handleLike(goal)">
                                <n-icon size="20">
                                  <Heart v-if="isLiked(goal)" />
                                  <HeartOutline v-else />
                                </n-icon>
                                <span class="count">{{ goal.likedBy?.length || 0 }}</span>
                              </button>
                              
                              <button class="action-btn" @click="toggleComments(goal)">
                                <n-icon size="20"><ChatbubbleOutline /></n-icon>
                                <span class="count">{{ goal.comments?.length || 0 }}</span>
                              </button>
                              
                              <button class="action-btn" @click="handleShare(goal)">
                                <n-icon size="20"><ShareSocialOutline /></n-icon>
                                <span class="count">{{ goal.sharedBy?.length || 0 }}</span>
                              </button>
                            </div>
                            
                            <n-button size="small" class="ref-btn" @click="handleReference(goal)">
                              <template #icon><n-icon><CopyOutline /></n-icon></template>
                              借鉴
                            </n-button>
                          </div>

                          <!-- Comments Section -->
                          <n-collapse-transition :show="goal.showComments">
                            <div class="comments-panel">
                              <n-spin :show="goal.loadingComments">
                                <div v-if="goal.comments?.length > 0" class="comments-scroll">
                                  <div v-for="comment in goal.comments" :key="comment._id" class="comment-row">
                                    <n-avatar round size="small" :src="comment.userAvatar" class="cmt-avatar" />
                                    <div class="cmt-body">
                                      <div class="cmt-user">{{ comment.username }}</div>
                                      <div class="cmt-text">{{ comment.content }}</div>
                                    </div>
                                  </div>
                                </div>
                                <div v-else class="empty-comments">暂无评论，成为第一个支持者吧</div>
                                
                                <div class="comment-input-box">
                                  <input 
                                    v-model="goal.newComment" 
                                    placeholder="输入评论..." 
                                    class="glass-input"
                                    @keyup.enter="submitComment(goal)"
                                  />
                                  <button class="send-btn" @click="submitComment(goal)" :disabled="!goal.newComment">
                                    <n-icon><PaperPlaneOutline /></n-icon>
                                  </button>
                                </div>
                              </n-spin>
                            </div>
                          </n-collapse-transition>
                        </div>
                      </n-grid-item>
                    </n-grid>
                  </div>
                </n-spin>
              </section>
            </div>
          </n-layout-content>
        </template>
      </common>
    </n-layout>

    <!-- Match Result Modal -->
    <n-modal v-model:show="showMatchModal">
      <n-card
        style="width: 600px; max-width: 90%; background: #1a1a1a; border: 1px solid #333;"
        :title="`与 ${matchTargetUser} 的默契度匹配`"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <div class="match-content">
          <div v-if="isMatching && !matchResult" class="matching-state">
            <n-spin size="large" />
            <p>正在分析双方目标基因...</p>
          </div>
          
          <div v-else class="match-result">
            <div class="score-circle">
              <div class="score-value">{{ matchScore }}%</div>
              <div class="score-label">相似度</div>
            </div>
            
            <div class="analysis-text">
              <p style="white-space: pre-wrap;">{{ matchResult }}</p>
            </div>
          </div>
        </div>
        
        <template #footer>
          <n-button @click="showMatchModal = false">关闭</n-button>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, inject } from 'vue';
import { useStore } from 'vuex';
import {
  useMessage,
  useDialog,
  NLayout,
  NLayoutContent,
  NSpin,
  NEmpty,
  NGrid,
  NGridItem,
  NAvatar,
  NButton,
  NIcon,
  NCollapseTransition,
  NTag,
  NInput,
  NDropdown,
  NModal,
  NCard
} from 'naive-ui';
import {
  HeartOutline, 
  Heart, 
  ChatbubbleOutline, 
  ShareSocialOutline, 
  ChevronDownOutline as ChevronDown,
  EllipsisHorizontal,
  Checkmark,
  CopyOutline,
  PaperPlaneOutline,
  PlanetOutline,
  SearchOutline as Search,
  PersonOutline
} from '@vicons/ionicons5';
import common from '@/views/common.vue';
import { postM, getM, getMPaths, baseURL } from '@/utils/request.js';
import { callCozeAPI } from '@/utils/aiService.js';

const store = useStore();
const message = useMessage();
const dialog = useDialog();
const isDark = inject('isDark', ref(true)); // Keeping for compatibility, but UI is now dark-themed by default
const loading = ref(false);
const goals = ref([]);
const searchQuery = ref('');
const currentTab = ref('latest');

const showMatchModal = ref(false);
const isMatching = ref(false);
const matchResult = ref('');
const matchScore = ref(0);
const matchTargetUser = ref('');

const filteredGoals = computed(() => {
  let result = [...goals.value];
  
  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(g => 
      (g.title && g.title.toLowerCase().includes(query)) || 
      (g.description && g.description.toLowerCase().includes(query))
    );
  }
  
  // Sort by tab
  if (currentTab.value === 'hot') {
    result.sort((a, b) => {
      const likesA = a.likedBy?.length || 0;
      const likesB = b.likedBy?.length || 0;
      return likesB - likesA;
    });
  } else {
    // Default to latest (already sorted by backend, but ensure here)
    result.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
  }
  
  return result;
});

const getLevelLabel = (level) => {
  switch (level) {
    case 'high': return '高优';
    case 'medium': return '中优';
    case 'low': return '低优';
    case 'urgent': return '紧急';
    default: return '普通';
  }
};

const getLevelType = (level) => {
  switch (level) {
    case 'high': return 'error';
    case 'medium': return 'warning';
    case 'low': return 'info';
    case 'urgent': return 'error';
    default: return 'default';
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  if (diff < 1000 * 60 * 60 * 24) {
    return '今天 ' + date.toLocaleTimeString().slice(0, 5);
  }
  return date.toLocaleDateString();
};

const getOwnerAvatar = (goal) => {
  if (goal.collaborators) {
    const owner = goal.collaborators.find(c => c.type === 'owner' || c.role === 'owner');
    if (owner && owner.avatarUrl) return baseURL() + owner.avatarUrl;
    if (owner && owner.avatar) return owner.avatar;
  }
  return 'https://api.dicebear.com/7.x/miniavs/svg?seed=' + (goal.owner || 'default');
};

const getOwnerName = (goal) => {
  if (goal.collaborators) {
    const owner = goal.collaborators.find(c => c.type === 'owner' || c.role === 'owner');
    if (owner && owner.username) return owner.username;
    if (owner && owner.name) return owner.name;
  }
  return goal.owner || '未知探索者';
};

const isLiked = (goal) => {
  const userId = store.state.user?.id || store.state.user?._id;
  if (!userId) return false;
  return goal.likedBy && goal.likedBy.includes(userId);
};

const fetchGoals = async () => {
  loading.value = true;
  try {
    const res = await getM('/getPublicGoals', {});
    const rawData = (res && res.data && res.data.data) ? res.data.data : (res.data || []);
    goals.value = rawData.map(g => ({
      ...g,
      showComments: false,
      loadingComments: false,
      comments: [],
      newComment: '',
      expandSubgoals: false
    }));
  } catch (error) {
    console.error('Failed to fetch goals', error);
    message.error('信号连接失败');
  } finally {
    loading.value = false;
  }
};

const handleLike = async (goal) => {
  const userId = store.state.user?.id || store.state.user?._id;
  if (!userId) {
    message.warning('请先登录连接');
    return;
  }

  try {
    await postM('/likeGoal', { goalId: goal._id, userId });
    if (goal.likedBy?.includes(userId)) {
      goal.likedBy = goal.likedBy.filter(id => id !== userId);
    } else {
      if (!goal.likedBy) goal.likedBy = [];
      goal.likedBy.push(userId);
    }
  } catch (error) {
    message.error('操作失败');
  }
};

const handleShare = async (goal) => {
  const userId = store.state.user?.id || store.state.user?._id;
  try {
    const res = await getMPaths('share/generate', goal._id);
    if (res && res.data && res.data.code === 200) {
      const token = res.data.data.shareToken;
      let shareUrl = import.meta.env.PROD 
        ? `https://freemix.bond/#/share/goal/${token}`
        : `${window.location.origin}/#/share/goal/${token}`;

      if (navigator.clipboard) {
        await navigator.clipboard.writeText(shareUrl);
        message.success('链接已捕获，去传播吧');
      } else {
        message.info(`链接：${shareUrl}`);
      }

      if (!goal.sharedBy) goal.sharedBy = [];
      if (userId && !goal.sharedBy.includes(userId)) {
        await postM('/shareGoal', { goalId: goal._id, userId });
        goal.sharedBy.push(userId);
      }
    } else {
      message.error('生成链接失败');
    }
  } catch (error) {
    message.error('操作失败');
  }
};

const handleReference = (goal) => {
  const userId = store.state.user?.id || store.state.user?._id;
  if (!userId) return;

  dialog.info({
    title: '借鉴目标',
    content: `将 "${goal.title}" 加入你的航道？`,
    positiveText: '确认接入',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await postM('/referenceGoal', { goalId: goal._id, userId });
        message.success('目标已同步至你的列表');
      } catch (error) {
        message.error('同步失败');
      }
    }
  });
};

const toggleComments = async (goal) => {
  goal.showComments = !goal.showComments;
  if (goal.showComments && (!goal.comments || goal.comments.length === 0)) {
    await fetchComments(goal);
  }
};

const fetchComments = async (goal) => {
  goal.loadingComments = true;
  try {
    const res = await getMPaths('getComments', goal._id);
    if (res && res.data && res.data.data) {
      goal.comments = res.data.data;
    }
  } finally {
    goal.loadingComments = false;
  }
};

const submitComment = async (goal) => {
  if (!goal.newComment.trim()) return;

  const userId = store.state.user?.id || store.state.user?._id;
  const username = store.state.user?.username || store.state.user?.name;
  const userAvatar = store.state.user?.avatarUrl ? (baseURL() + store.state.user.avatarUrl) : '';

  try {
    const commentData = {
      goalId: goal._id,
      userId,
      username,
      userAvatar,
      content: goal.newComment
    };

    const res = await postM('/addComment', commentData);
    if (res && res.data && res.data.data) {
      if (!goal.comments) goal.comments = [];
      goal.comments.push(res.data.data);
      goal.newComment = '';
      message.success('发送成功');
    }
  } catch (error) {
    message.error('发送失败');
  }
};

const dropdownOptions = [
  {
    label: '与该英雄匹配',
    key: 'match',
    icon: () => h(NIcon, null, { default: () => h(PersonOutline) })
  }
];

import { h } from 'vue';

const handleDropdownSelect = (key, goal) => {
  if (key === 'match') {
    handleMatch(goal);
  }
};

const handleMatch = async (goal) => {
  const currentUser = store.state.user;
  if (!currentUser) {
    message.warning('请先登录连接');
    return;
  }

  const targetUsername = getOwnerName(goal);
  if (targetUsername === currentUser.username) {
    message.info('这是你自己的目标，无需匹配');
    return;
  }

  showMatchModal.value = true;
  isMatching.value = true;
  matchTargetUser.value = targetUsername;
  matchResult.value = '';
  matchScore.value = 0;

  try {
    // Fetch goals
    const [myGoalsRes, targetGoalsRes] = await Promise.all([
      getM('/getGoals/' + currentUser.username),
      getM('/getGoals/' + targetUsername) // Note: This assumes targetUsername is valid for this API. Ideally should use ID but backend uses username.
    ]);

    const myGoals = (myGoalsRes.data.data || []).map(g => g.title).join(', ');
    const targetGoals = (targetGoalsRes.data.data || []).map(g => g.title).join(', ');

    if (!myGoals) {
      matchResult.value = '你还没有设定任何目标，无法进行匹配。';
      isMatching.value = false;
      return;
    }
    
    if (!targetGoals) {
      matchResult.value = '对方还没有公开的目标，无法进行匹配。';
      isMatching.value = false;
      return;
    }

    const prompt = `
    请分析以下两位用户的目标相似度：
    用户A（我）的目标：${myGoals}
    用户B（${targetUsername}）的目标：${targetGoals}
    
    请输出：
    1. 相似度评分（0-100分）
    2. 用中文简要总结两人的共同点和差异，以及是否适合一起努力。
    
    格式要求：
    [评分]: XX
    [总结]: ...
    `;

    await callCozeAPI(prompt, (data) => {
      matchResult.value = data.content;
      console.log("匹配的回答",matchResult.value);
      
      // Try to extract score
      const scoreMatch = data.content.match(/\[评分\]:\s*(\d+)/) || data.content.match(/评分：\s*(\d+)/);
      if (scoreMatch) {
        matchScore.value = parseInt(scoreMatch[1]);
      }
    });

  } catch (error) {
    console.error('Match failed:', error);
    matchResult.value = '匹配计算失败，请稍后重试。';
  } finally {
    isMatching.value = false;
  }
};

onMounted(() => {
  fetchGoals();
});
</script>

<style scoped>
/* Reset & Layout */
.page-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background-color: #050505;
  color: #e0e0e0;
}

.main-layout {
  background: transparent !important;
  height: 100%;
  z-index: 2;
}

.content-transparent {
  background: transparent !important;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  position: relative;
}

/* Ambient Background */
.ambient-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s infinite alternate;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: #0c3725;
  top: -100px;
  left: -100px;
  opacity: 0.2;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: #183812;
  bottom: -50px;
  right: -50px;
  opacity: 0.15;
  animation-delay: -5s;
}

.shape-3 {
  width: 300px;
  height: 300px;
  background: #41b883;
  top: 40%;
  left: 30%;
  opacity: 0.1;
  animation-duration: 30s;
}

.noise-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.05'/%3E%3C/svg%3E");
  opacity: 0.4;
  z-index: 0;
}

@keyframes float {
  0% { transform: translate(0, 0); }
  100% { transform: translate(30px, 50px); }
}

/* Header */
.page-header {
  margin-bottom: 48px;
  text-align: center;
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.gradient-text {
  background: linear-gradient(135deg, #fff 30%, #41b883 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 300;
  letter-spacing: 1px;
}

.beta-tag {
  background: rgba(0, 220, 130, 0.1) !important;
  color: #00dc82 !important;
  border: 1px solid rgba(0, 220, 130, 0.3) !important;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  max-width: 600px;
  margin: 0 auto;
}

.search-wrapper {
  width: 100%;
}

:deep(.search-input) {
  background-color: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  transition: all 0.3s;
}

:deep(.search-input .n-input__input-el) {
  color: #fff !important;
}

:deep(.search-input:hover), :deep(.search-input:focus-within) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border-color: #00dc82 !important;
  box-shadow: 0 0 15px rgba(0, 220, 130, 0.2);
}

.filter-tabs {
  display: flex;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 30px;
  padding: 4px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.filter-tab {
  padding: 8px 24px;
  border-radius: 24px;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.filter-tab.active {
  background: #00dc82;
  color: #000;
  box-shadow: 0 4px 12px rgba(0, 220, 130, 0.3);
}

/* Header Actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.level-tag {
  height: 20px;
  font-size: 10px;
  padding: 0 8px;
}

/* Progress Bar */
.progress-mini {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.progress-track {
  flex: 1;
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00dc82, #39ff14);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 12px;
  color: #00dc82;
  font-family: monospace;
}

/* Glass Card */
.glass-card {
  position: relative;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.glass-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(65, 184, 131, 0.3);
  box-shadow: 0 15px 30px -10px rgba(0, 0, 0, 0.5);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00dc82, transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.glass-card:hover .card-glow {
  opacity: 1;
}

/* Card Header */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.user-profile {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.1);
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 600;
  color: #fff;
  font-size: 15px;
}

.timestamp {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.more-btn {
  color: rgba(255, 255, 255, 0.5);
}

.more-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* Card Body */
.card-body {
  flex: 1;
  margin-bottom: 20px;
}

.goal-title {
  font-size: 18px;
  font-weight: 700;
  color: #f0f0f0;
  margin-bottom: 10px;
  line-height: 1.4;
}

.goal-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Milestones */
.milestones-container {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.milestone-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.status-indicator {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-indicator.is-done {
  border-color: #00dc82;
  background: rgba(0, 220, 130, 0.2);
  color: #00dc82;
}

.milestone-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.milestone-text.text-done {
  color: rgba(255, 255, 255, 0.4);
  text-decoration: line-through;
}

.expand-trigger {
  font-size: 12px;
  color: #00dc82;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(255, 255, 255, 0.1);
  transition: opacity 0.2s;
}

.expand-trigger:hover {
  opacity: 0.8;
}

.rotate-180 {
  transform: rotate(180deg);
}

/* Card Footer */
.card-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-group {
  display: flex;
  gap: 16px;
}

.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  transition: all 0.2s;
  padding: 0;
}

.action-btn:hover {
  color: #fff;
}

.action-btn.is-active {
  color: #ff4757;
}

.ref-btn {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: none;
}

.ref-btn:hover {
  background: #00dc82;
  color: #000;
}

/* Comments Panel */
.comments-panel {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.comments-scroll {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.comment-row {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.cmt-avatar {
  width: 24px;
  height: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.cmt-body {
  flex: 1;
}

.cmt-user {
  font-size: 12px;
  font-weight: 600;
  color: #ccc;
  margin-bottom: 2px;
}

.cmt-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.4;
}

.empty-comments {
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: 12px;
}

.comment-input-box {
  display: flex;
  gap: 8px;
}

.glass-input {
  flex: 1;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 8px 16px;
  color: #fff;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}

.glass-input:focus {
  border-color: #00dc82;
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #00dc82;
  color: #000;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}

.send-btn:disabled {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.3);
  cursor: not-allowed;
}

.send-btn:not(:disabled):hover {
  transform: scale(1.1);
}

/* Match Modal */
.match-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  min-height: 200px;
  color: #eee;
}

.matching-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-top: 40px;
}

.match-result {
  width: 100%;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid #00dc82;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto 24px;
  box-shadow: 0 0 20px rgba(0, 220, 130, 0.3);
}

.score-value {
  font-size: 36px;
  font-weight: 800;
  color: #00dc82;
}

.score-label {
  font-size: 12px;
  color: #aaa;
}

.analysis-text {
  background: rgba(255, 255, 255, 0.05);
  padding: 16px;
  border-radius: 8px;
  line-height: 1.6;
  font-size: 14px;
  color: #ccc;
  max-height: 300px;
  overflow-y: auto;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .hero-title {
    font-size: 28px;
  }
  
  .glass-card {
    padding: 20px;
  }
}
</style>
