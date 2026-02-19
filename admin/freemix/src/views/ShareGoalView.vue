<template>
  <div class="share-page">
    <div class="share-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <div class="share-container">
      <div v-if="!loading && goal" class="goal-card glass-effect">
        <!-- 头部装饰 -->
        <div class="card-decoration"></div>
        
        <!-- 用户信息 -->
        <div class="card-header">
          <div class="user-profile">
            <n-avatar round :size="64" :src="getOwnerAvatar(goal)" class="user-avatar" />
            <div class="user-details">
              <div class="username">{{ getOwnerName(goal) }}</div>
              <div class="invite-text">邀请你一起见证成长</div>
            </div>
          </div>
          <div class="goal-date">
            <n-icon size="16"><CalendarOutline /></n-icon>
            <span>{{ formatDate(goal.createTime) }}</span>
          </div>
        </div>
        
        <!-- 目标内容 -->
        <div class="card-content">
          <div class="goal-main-info">
            <div class="goal-badges">
              <span class="badge priority-badge" :class="goal.level">{{ getLevelLabel(goal.level) }}优先级</span>
              <span class="badge deadline-badge" v-if="goal.deadline">
                <n-icon size="12"><TimeOutline /></n-icon> 截止: {{ formatDate(goal.deadline) }}
              </span>
            </div>
            <h1 class="goal-title">{{ goal.title }}</h1>
            <p class="goal-desc">{{ goal.description || '这个目标没有更多描述，但行动胜过言语。' }}</p>
          </div>
          
          <!-- 进度展示 -->
          <div class="progress-box">
            <div class="progress-info">
              <span class="label">当前进度</span>
              <span class="value">{{ goal.progress }}%</span>
            </div>
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: goal.progress + '%', background: getProgressGradient(goal.progress) }">
                <div class="progress-glow"></div>
              </div>
            </div>
          </div>
          
          <!-- 里程碑列表 -->
          <div v-if="goal.childGoals && goal.childGoals.length > 0" class="milestones-section">
            <h3 class="section-title">
              <n-icon size="18"><FlagOutline /></n-icon>
              里程碑清单
            </h3>
            <div class="milestones-list custom-scrollbar">
              <div v-for="(subGoal, index) in goal.childGoals" :key="index" class="milestone-item" :class="{ 'completed': subGoal.finish }">
                <div class="check-circle">
                  <n-icon v-if="subGoal.finish" size="14"><CheckmarkSharp /></n-icon>
                </div>
                <span class="milestone-text">{{ subGoal.message }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 底部操作 -->
        <div class="card-footer">
          <button class="action-button primary-btn" @click="handleReference">
            <span class="btn-icon"><n-icon size="20"><DuplicateOutline /></n-icon></span>
            <span class="btn-text">添加到我的目标</span>
          </button>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-else-if="loading" class="state-container loading">
        <n-spin size="large" stroke="#fff" />
        <p>正在加载精彩目标...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else class="state-container error">
        <div class="error-icon">
          <n-icon size="48"><AlertCircleOutline /></n-icon>
        </div>
        <h3>链接已失效</h3>
        <p>该分享链接可能不存在、已过期或已被删除</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage, useDialog, NAvatar, NIcon, NSpin } from 'naive-ui';
import { 
  CalendarOutline, TimeOutline, FlagOutline, CheckmarkSharp, 
  DuplicateOutline, AlertCircleOutline 
} from '@vicons/ionicons5';
import { getMPaths, postM, baseURL } from '@/utils/request.js';
import { getToken } from '@/utils/tokenUtils.js';

const route = useRoute();
const router = useRouter();
const message = useMessage();
const dialog = useDialog();
const loading = ref(true);
const goal = ref(null);

const token = route.params.token;

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`;
};

const getProgressGradient = (percentage) => {
  if (percentage < 30) return 'linear-gradient(90deg, #ff9a9e 0%, #ff6b6b 100%)';
  if (percentage < 70) return 'linear-gradient(90deg, #f6d365 0%, #fda085 100%)';
  return 'linear-gradient(90deg, #84fab0 0%, #8fd3f4 100%)';
};

const getLevelLabel = (level) => {
  switch (level) {
    case 'high': return '高';
    case 'medium': return '中';
    case 'low': return '低';
    case 'urgent': return '紧急';
    default: return '普通';
  }
};

const getOwnerAvatar = (g) => {
  if (g.collaborators) {
    const owner = g.collaborators.find(c => c.type === 'owner' || c.role === 'owner');
    if (owner && owner.avatarUrl) return baseURL() + owner.avatarUrl;
    if (owner && owner.avatar) return owner.avatar;
  }
  return 'https://api.dicebear.com/7.x/miniavs/svg?seed=' + (g.owner || 'default');
};

const getOwnerName = (g) => {
  if (g.collaborators) {
    const owner = g.collaborators.find(c => c.type === 'owner' || c.role === 'owner');
    if (owner && owner.username) return owner.username;
    if (owner && owner.name) return owner.name;
  }
  return g.owner || '未知用户';
};

const fetchSharedGoal = async () => {
  loading.value = true;
  try {
    const res = await getMPaths('share/view', token);
    if (res && res.data && res.data.data) {
      goal.value = res.data.data;
    } else {
      goal.value = null;
    }
  } catch (error) {
    console.error('Fetch shared goal error:', error);
    goal.value = null;
  } finally {
    loading.value = false;
  }
};

const handleReference = async () => {
  const token = await getToken();
  if (!token) {
    message.warning('请登录后添加目标');
    router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`);
    return;
  }
  
  const userId = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).id : null;
  
  if (!userId) {
     message.warning('请先登录');
     router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`);
     return;
  }

  dialog.info({
    title: '确认添加',
    content: `将 "${goal.value.title}" 添加到你的目标列表？`,
    positiveText: '立即添加',
    negativeText: '再想想',
    onPositiveClick: async () => {
      try {
        await postM('/referenceGoal', { goalId: goal.value._id, userId });
        message.success('添加成功！开启你的目标之旅');
        router.push('/');
      } catch (error) {
        message.error('添加失败，请重试');
      }
    }
  });
};

onMounted(() => {
  fetchSharedGoal();
});
</script>

<style scoped>
/* 全屏背景布局 */
.share-page {
  position: relative;
  min-height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #000000;  /* fallback */
  background: linear-gradient(135deg, #000000 0%, #031f15 50%, #001a10 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 动态背景图形 */
.share-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.bg-shape {
  position: absolute;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  top: -10%;
  left: -10%;
  width: 50vw;
  height: 50vw;
  background: radial-gradient(circle, #00c9a7, transparent 70%);
  animation-delay: 0s;
}

.shape-2 {
  bottom: -10%;
  right: -10%;
  width: 60vw;
  height: 60vw;
  background: radial-gradient(circle, #054d38, transparent 70%);
  animation-delay: -5s;
}

.shape-3 {
  top: 40%;
  left: 40%;
  width: 40vw;
  height: 40vw;
  background: radial-gradient(circle, #083d2e, transparent 70%);
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

/* 内容容器 */
.share-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  perspective: 1000px;
}

/* 玻璃拟态卡片 */
.goal-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 30px;
  box-shadow: 
    0 20px 50px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  overflow: hidden;
  transform-style: preserve-3d;
  animation: cardEntry 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes cardEntry {
  from { opacity: 0; transform: translateY(30px) rotateX(5deg); }
  to { opacity: 1; transform: translateY(0) rotateX(0); }
}

.card-decoration {
  height: 8px;
  background: linear-gradient(90deg, #00c9a7, #84fab0, #8fd3f4, #00c9a7);
  background-size: 200% 200%;
  animation: gradientFlow 5s ease infinite;
}

@keyframes gradientFlow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 卡片头部 */
.card-header {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.invite-text {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.goal-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  background: rgba(0,0,0,0.03);
  padding: 4px 8px;
  border-radius: 12px;
}

/* 卡片内容区 */
.card-content {
  padding: 24px;
}

.goal-badges {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.badge {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.priority-badge.high { background: #ffebeb; color: #ff4d4f; }
.priority-badge.medium { background: #fff7e6; color: #fa8c16; }
.priority-badge.low { background: #e6f7ff; color: #1890ff; }
.priority-badge.urgent { background: #fff1f0; color: #f5222d; }
.priority-badge { background: #f5f5f5; color: #666; }

.deadline-badge {
  background: #f0f5ff;
  color: #2f54eb;
}

.goal-title {
  font-size: 26px;
  font-weight: 800;
  color: #1a1a1a;
  line-height: 1.3;
  margin: 0 0 12px;
}

.goal-desc {
  font-size: 15px;
  color: #555;
  line-height: 1.6;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 24px;
}

/* 进度条样式 */
.progress-box {
  margin-bottom: 24px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 14px;
}

.progress-info .label { color: #888; }
.progress-info .value { color: #333; }

.progress-track {
  height: 10px;
  background: #eee;
  border-radius: 5px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  border-radius: 5px;
  position: relative;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.progress-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  animation: shine 2s infinite;
}

@keyframes shine {
  from { transform: translateX(-100%); }
  to { transform: translateX(100%); }
}

/* 里程碑样式 */
.section-title {
  font-size: 14px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.milestones-list {
  max-height: 200px;
  overflow-y: auto;
  padding-right: 4px;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 2px;
}

.milestone-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}

.milestone-item:last-child {
  border-bottom: none;
}

.check-circle {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
  transition: all 0.3s;
  color: #fff;
}

.milestone-item.completed .check-circle {
  background: #52c41a;
  border-color: #52c41a;
  box-shadow: 0 2px 6px rgba(82, 196, 26, 0.3);
}

.milestone-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  transition: all 0.3s;
}

.milestone-item.completed .milestone-text {
  color: #aaa;
  text-decoration: line-through;
}

/* 底部按钮 */
.card-footer {
  padding: 20px 24px 28px;
  background: linear-gradient(to top, rgba(255,255,255,1), rgba(255,255,255,0.8));
}

.action-button {
  width: 100%;
  border: none;
  border-radius: 16px;
  padding: 16px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.primary-btn {
  background: #111;
  color: #fff;
  box-shadow: 0 8px 20px rgba(0,0,0,0.2);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.3);
  background: #000;
}

.primary-btn:active {
  transform: translateY(1px);
}

/* 状态展示 */
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #fff;
  height: 60vh;
}

.loading p {
  margin-top: 16px;
  font-size: 16px;
  opacity: 0.8;
}

.error-icon {
  margin-bottom: 16px;
  color: #ff6b6b;
}

.error h3 {
  font-size: 24px;
  margin-bottom: 8px;
}

.error p {
  opacity: 0.7;
}

/* 移动端适配 */
@media (max-width: 480px) {
  .share-container {
    padding: 0;
  }
  
  .goal-card {
    border-radius: 20px;
  }
  
  .goal-title {
    font-size: 22px;
  }
}
</style>