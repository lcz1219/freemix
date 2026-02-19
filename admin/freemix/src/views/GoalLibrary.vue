<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'home-container' : 'home-container-light'">
    <common>
      <template #content>
        <n-layout-content class="main-content-wrapper">
          <div class="main-content">
            <!-- 页面标题 -->
            <section class="page-header">
              <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">目标朋友圈</h1>
              <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
                发现、借鉴与分享目标，与他人共同进步
              </p>
            </section>

            <!-- 目标列表 -->
            <section class="goals-feed">
              <n-spin :show="loading">
                <n-empty v-if="!loading && goals.length === 0" description="暂无公开目标，快去分享你的第一个目标吧！" />
                <div v-else class="goal-list">
                  <n-grid :x-gap="24" :y-gap="24" cols="1 m:2 l:3" responsive="screen">
                    <n-grid-item v-for="goal in goals" :key="goal._id">
                      <!-- Flat Design Card -->
                      <div class="feed-card" :class="{ 'dark-mode': isDark }">
                        <!-- Card Header -->
                        <div class="feed-header">
                          <n-avatar round size="small" :src="getOwnerAvatar(goal)" fallback-src="https://api.dicebear.com/7.x/miniavs/svg?seed=1" />
                          <div class="feed-user-info">
                            <div class="feed-username">{{ getOwnerName(goal) }}</div>
                            <div class="feed-time">{{ formatDate(goal.createTime) }}</div>
                          </div>
                        </div>
                        
                        <!-- Card Content -->
                        <div class="feed-content">
                          <h3 class="feed-title">{{ goal.title }}</h3>
                          <p class="feed-desc">{{ goal.description || '暂无描述' }}</p>
                          
                          <!-- Child Goals Preview -->
                          <div v-if="goal.childGoals && goal.childGoals.length > 0" class="feed-subgoals">
                            <!-- First 3 items always shown -->
                            <div v-for="(sub, idx) in goal.childGoals.slice(0, 3)" :key="idx" class="subgoal-preview-item">
                              <n-icon size="14" :color="sub.finish ? '#00c9a7' : '#ccc'" class="subgoal-icon">
                                <CheckmarkCircle v-if="sub.finish" />
                                <EllipseOutline v-else />
                              </n-icon>
                              <span class="subgoal-text" :class="{ completed: sub.finish }">{{ sub.message }}</span>
                            </div>
                            
                            <!-- Remaining items in collapse transition -->
                            <n-collapse-transition :show="goal.expandSubgoals">
                              <div v-for="(sub, idx) in goal.childGoals.slice(3)" :key="idx + 3" class="subgoal-preview-item">
                                <n-icon size="14" :color="sub.finish ? '#00c9a7' : '#ccc'" class="subgoal-icon">
                                  <CheckmarkCircle v-if="sub.finish" />
                                  <EllipseOutline v-else />
                                </n-icon>
                                <span class="subgoal-text" :class="{ completed: sub.finish }">{{ sub.message }}</span>
                              </div>
                            </n-collapse-transition>

                            <!-- Toggle Button -->
                            <div v-if="goal.childGoals.length > 3" class="subgoal-toggle" @click="goal.expandSubgoals = !goal.expandSubgoals">
                              <span class="toggle-text">{{ goal.expandSubgoals ? '收起' : `查看剩余 ${goal.childGoals.length - 3} 个里程碑` }}</span>
                              <n-icon size="12" class="toggle-icon" :class="{ rotated: goal.expandSubgoals }">
                                <ChevronDownOutline />
                              </n-icon>
                            </div>
                          </div>

                          <div class="feed-progress-wrapper">
                             <n-progress 
                               type="line" 
                               :percentage="goal.progress" 
                               :color="getProgressColor(goal.progress)" 
                               :height="6" 
                               :show-indicator="false"
                               border-radius="3"
                             />
                             <span class="feed-progress-text">{{ goal.progress }}%</span>
                          </div>
                        </div>

                        <!-- Card Actions -->
                        <div class="feed-actions">
                          <div class="left-actions">
                            <button class="icon-btn" @click="handleLike(goal)" :class="{ active: isLiked(goal), 'dark-btn': isDark }">
                              <n-icon size="18">
                                <Heart v-if="isLiked(goal)" />
                                <HeartOutline v-else />
                              </n-icon>
                              <span>{{ goal.likedBy?.length || 0 }}</span>
                            </button>
                            <button class="icon-btn" @click="toggleComments(goal)" :class="{ 'dark-btn': isDark }">
                              <n-icon size="18"><ChatbubbleOutline /></n-icon>
                              <span>{{ goal.comments?.length || '评论' }}</span>
                            </button>
                            <button class="icon-btn" @click="handleShare(goal)" :class="{ 'dark-btn': isDark }">
                              <n-icon size="18"><ShareSocialOutline /></n-icon>
                              <span>{{ goal.sharedBy?.length || 0 }}</span>
                            </button>
                          </div>
                          <div class="right-actions">
                            <n-button size="tiny" round secondary type="primary" @click="handleReference(goal)">
                              借鉴
                            </n-button>
                          </div>
                        </div>

                        <!-- Comments Section -->
                        <n-collapse-transition :show="goal.showComments">
                          <div class="comments-area" :class="{ 'dark-area': isDark }">
                            <n-spin :show="goal.loadingComments">
                              <div v-if="goal.comments && goal.comments.length > 0" class="comments-list">
                                <div v-for="comment in goal.comments" :key="comment._id" class="comment-item">
                                  <n-avatar round size="small" :src="comment.userAvatar" class="comment-avatar" />
                                  <div class="comment-content">
                                    <div class="comment-user">{{ comment.username }}</div>
                                    <div class="comment-text">{{ comment.content }}</div>
                                  </div>
                                </div>
                              </div>
                              <div v-else class="no-comments">暂无评论，快来抢沙发</div>
                              
                              <div class="comment-input-wrapper">
                                <n-input v-model:value="goal.newComment" placeholder="写下你的想法..." size="small" round @keyup.enter="submitComment(goal)">
                                  <template #suffix>
                                    <n-button text type="primary" size="tiny" @click="submitComment(goal)" :disabled="!goal.newComment">
                                      发送
                                    </n-button>
                                  </template>
                                </n-input>
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
  NSpace,
  NAvatar,
  NProgress,
  NButton,
  NIcon,
  NCollapseTransition,
  NInput
} from 'naive-ui';
import { 
  HeartOutline, Heart, ChatbubbleOutline, ShareSocialOutline, CheckmarkCircle, EllipseOutline, ChevronDownOutline
} from '@vicons/ionicons5';
import common from '@/views/common.vue';
import { postM, getM, getMPaths, baseURL } from '@/utils/request.js';
import { getToken } from '@/utils/tokenUtils.js';

const store = useStore();
const message = useMessage();
const dialog = useDialog();
const isDark = inject('isDark', ref(true));
const loading = ref(false);
const goals = ref([]);
const currentUser = computed(() => store.state.user);

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  // Simple relative time format or clean date
  const now = new Date();
  const diff = now - date;
  if (diff < 1000 * 60 * 60 * 24) {
    return '今天 ' + date.toLocaleTimeString().slice(0, 5);
  }
  return date.toLocaleDateString();
};

const getProgressColor = (percentage) => {
  if (percentage < 30) return '#ff6b6b';
  if (percentage < 70) return '#f0a020';
  return '#00c9a7';
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
  return goal.owner || '未知用户';
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
    if (res && res.data) {
      if (res.data.data) {
          goals.value = res.data.data.map(g => ({
            ...g,
            showComments: false,
            loadingComments: false,
            comments: [],
            newComment: '',
            expandSubgoals: false
          }));
      } else {
           goals.value = res.data.map(g => ({
            ...g,
            showComments: false,
            loadingComments: false,
            comments: [],
            newComment: '',
            expandSubgoals: false
          }));
      }
    }
  } catch (error) {
    console.error('Failed to fetch goals', error);
    message.error('获取目标列表失败');
  } finally {
    loading.value = false;
  }
};

const handleLike = async (goal) => {
  const userId = store.state.user?.id || store.state.user?._id;
  if (!userId) {
    message.warning('请先登录');
    return;
  }
  
  try {
    const res = await postM('/likeGoal', { goalId: goal._id, userId });
    // Update local state
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
    // 1. Generate share token
    const res = await getMPaths('share/generate', goal._id);
    if (res && res.data && res.data.code === 200) {
      const token = res.data.data.shareToken;
      const shareUrl = `${window.location.origin}/#/share/goal/${token}`;
      
      // Copy to clipboard with fallback
      if (navigator.clipboard) {
        await navigator.clipboard.writeText(shareUrl);
        message.success('分享链接已复制到剪贴板');
      } else {
        message.info(`分享链接：${shareUrl}`);
      }
      
      // 2. Track share action (optional, for social count)
      if (!goal.sharedBy) goal.sharedBy = [];
      if (userId && !goal.sharedBy.includes(userId)) {
         await postM('/shareGoal', { goalId: goal._id, userId });
         goal.sharedBy.push(userId);
      }
    } else {
      message.error('生成分享链接失败');
    }
  } catch (error) {
    console.error(error);
    message.error('操作失败');
  }
};

const handleReference = (goal) => {
  const userId = store.state.user?.id || store.state.user?._id;
  if (!userId) return;

  dialog.info({
    title: '借鉴目标',
    content: `确定要将目标 "${goal.title}" 添加到你的目标列表吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await postM('/referenceGoal', { goalId: goal._id, userId });
        message.success('借鉴成功，已添加到你的目标列表');
      } catch (error) {
        message.error('借鉴失败');
      }
    }
  });
};

const toggleComments = async (goal) => {
  goal.showComments = !goal.showComments;
  if (goal.showComments && goal.comments.length === 0) {
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
  } catch (error) {
    console.error(error);
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
      message.success('评论成功');
    }
  } catch (error) {
    message.error('评论失败');
  }
};

onMounted(() => {
  fetchGoals();
});
</script>

<style scoped>
.home-container, .home-container-light {
  height: 100vh;
}
.main-content-wrapper {
  padding: 20px;
}
.main-content {
  max-width: 1200px;
  margin: 0 auto;
}
.page-header {
  margin-bottom: 30px;
  text-align: center;
}
.hero-title {
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}
.hero-title-light {
  color: #333;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}
.hero-subtitle, .hero-subtitle-light {
  font-size: 16px;
  opacity: 0.8;
}
.hero-subtitle {
  color: #ccc;
}
.hero-subtitle-light {
  color: #666;
}

/* Flat Feed Card Styles */
.feed-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #f0f0f0;
  padding: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feed-card.dark-mode {
  background: #1d1d1d;
  border: 1px solid #3a3a40;
}

.feed-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.05);
}

.feed-card.dark-mode:hover {
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
}

.feed-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feed-user-info {
  display: flex;
  flex-direction: column;
}

.feed-username {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.dark-mode .feed-username {
  color: #fff;
}

.feed-time {
  font-size: 12px;
  color: #999;
}

.feed-content {
  flex: 1;
}

.feed-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 8px;
  color: #222;
  line-height: 1.4;
}

.dark-mode .feed-title {
  color: #eee;
}

.feed-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dark-mode .feed-desc {
  color: #aaa;
}

.feed-subgoals {
  margin-bottom: 16px;
  background: #f9f9fb;
  border-radius: 8px;
  padding: 10px;
}

.dark-mode .feed-subgoals {
  background: #36363c;
}

.subgoal-preview-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.subgoal-preview-item:last-child {
  margin-bottom: 0;
}

.subgoal-icon {
  flex-shrink: 0;
  display: flex;
}

.subgoal-text {
  font-size: 13px;
  color: #555;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.dark-mode .subgoal-text {
  color: #ccc;
}

.subgoal-text.completed {
  color: #999;
  text-decoration: line-through;
}

.subgoal-more {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  padding-left: 22px;
}

.subgoal-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #eee;
  cursor: pointer;
  transition: color 0.2s;
}

.dark-mode .subgoal-toggle {
  border-top-color: #444;
  color: #777;
}

.subgoal-toggle:hover {
  color: #666;
}

.dark-mode .subgoal-toggle:hover {
  color: #aaa;
}

.toggle-icon {
  transition: transform 0.3s ease;
}

.toggle-icon.rotated {
  transform: rotate(180deg);
}

.feed-progress-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.feed-progress-text {
  font-size: 12px;
  color: #999;
  width: 35px;
  text-align: right;
}

.feed-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f5f5f5;
  margin-top: 4px;
}

.dark-mode .feed-actions {
  border-top-color: #3a3a40;
}

.left-actions {
  display: flex;
  gap: 16px;
}

.icon-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s, color 0.2s;
}

.dark-mode .icon-btn {
  color: #aaa;
}

.icon-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.dark-mode .icon-btn:hover {
  background: #3a3a40;
  color: #fff;
}

.icon-btn.active {
  color: #ff6b6b;
}

.icon-btn span {
  font-weight: 500;
}

/* Comments Area */
.comments-area {
  background: #f9f9fb;
  border-radius: 12px;
  padding: 16px;
  margin-top: 12px;
}

.comments-area.dark-area {
  background: #36363c;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-item {
  display: flex;
  gap: 10px;
  font-size: 13px;
}

.comment-avatar {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
}

.comment-content {
  flex: 1;
}

.comment-user {
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.dark-mode .comment-user {
  color: #ddd;
}

.comment-text {
  color: #555;
  line-height: 1.4;
}

.dark-mode .comment-text {
  color: #aaa;
}

.no-comments {
  text-align: center;
  color: #999;
  font-size: 12px;
  padding: 10px 0;
}

.comment-input-wrapper {
  margin-top: 8px;
}
</style>