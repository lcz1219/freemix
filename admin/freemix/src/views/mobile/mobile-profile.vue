<template>
  <div class="app-container mobile-profile" :class="{ 'dark-mode': true }">
    <div class="profile-content">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      fixed
      placeholder
      :border="false"
      class="glass-nav"
      z-index="100"
      :safe-area-inset-top="true"
    >
      <template #right>
        <span class="nav-title">个人中心</span>
      </template>
      <template #left>
        <!-- <van-icon name="setting-o" size="24" color="var(--text-primary)" @click="goToSettings" /> -->
        <van-icon name="arrow-left" size="24" color="var(--text-primary)" @click="goBack"/>
      </template>
    </van-nav-bar>

    <!-- 个人信息区域 -->
    <div class="profile-header-card">
      <div class="user-info-row">
        <van-uploader :after-read="handleAvatarUpload" :max-count="1">
          <div class="avatar-wrapper">
            <van-image
             round
              width="50"
              height="50"
              fit="cover"
              :src="avatarUrl"
            
            
            />
            <div class="edit-badge">
              <van-icon name="photograph" size="12" />
            </div>
          </div>
        </van-uploader>
        
        <div class="info-content">
          <h2 class="username">{{ userProfile.username || '未设置昵称' }}</h2>
          <p class="bio">{{ userProfile.bio || '专注于目标管理与个人成长' }}</p>
          <van-button size="mini" round plain type="primary" class="edit-btn" @click="goToSettings('profile')">
            编辑资料
          </van-button>
        </div>
      </div>

      <!-- 数据统计 -->
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-num">{{ stats.activeGoals }}</span>
          <span class="stat-label">进行中</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.completedGoals }}</span>
          <span class="stat-label">已完成</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.successRate }}%</span>
          <span class="stat-label">完成率</span>
        </div>
      </div>
    </div>

    <!-- 主要内容标签页 -->
  
        <div class="tab-content">
          <!-- 进度概览 -->
          <div class="section-card">
            <h3 class="card-title">今日概览</h3>
            <van-grid :column-num="3" :border="false" class="progress-grid">
              <van-grid-item>
                <template #default>
                  <div class="grid-stat">
                    <span class="val">{{ dailyStats.todayCompleted }}/{{ dailyStats.todayTotal }}</span>
                    <span class="label">今日完成</span>
                  </div>
                </template>
              </van-grid-item>
              <van-grid-item>
                <template #default>
                  <div class="grid-stat">
                    <span class="val">{{ weeklyStats.weeklyProgress.toFixed(0) }}%</span>
                    <span class="label">本周进度</span>
                  </div>
                </template>
              </van-grid-item>
              <van-grid-item>
                <template #default>
                  <div class="grid-stat">
                    <span class="val">{{ monthlyStats.monthlyGoals }}</span>
                    <span class="label">本月目标</span>
                  </div>
                </template>
              </van-grid-item>
            </van-grid>
          </div>

          <!-- 目标分类 -->
          <div class="section-card">
            <h3 class="card-title">分类分布</h3>
            <div class="categories-scroll">
              <div 
                v-for="cat in goalCategories" 
                :key="cat.name" 
                class="category-pill"
                :style="{ borderColor: cat.color, color: cat.color, backgroundColor: hexToRgba(cat.color, 0.1) }"
              >
                <span class="cat-dot" :style="{ backgroundColor: cat.color }"></span>
                <span class="cat-name">{{ cat.name }}</span>
                <span class="cat-count">{{ cat.count }}</span>
              </div>
            </div>
          </div>

          <!-- 热力图 -->
          <div class="section-card heatmap-card">
            <h3 class="card-title">活跃热力图</h3>
            <div class="heatmap-wrapper">
              <HotMap  :goals="goalsStore.goals.value" />
            </div>
          </div>

        

         
        </div>



    </div>
  </div>
</template>

<script setup>
import { ref, onMounted,onUnmounted,nextTick,computed, inject } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { showToast, showDialog } from 'vant';
import { useUser } from '@/hooks/useUser';
import { useGoals } from '@/hooks/useGoals';
import { postM, isSuccess } from '@/utils/request';
import HotMap from '@/components/HotMap.vue';

const router = useRouter();
const store = useStore();
const isDark = inject('isDark', ref(false));
const goalsStore = useGoals();
let { getGoals } = goalsStore;

// User Hooks
const { userInfo: userProfile, avatarUrl, initUserData, uploadAvatar } = useUser();

// Tabs
const activeTab = ref('overview');

// Stats Logic
const stats = ref({
  activeGoals: 0,
  completedGoals: 0,
  successRate: 0
});
const goBack=()=>{
    router.back();
}

// 跳转好友页面
const goToFriends = () => {
  router.push('/messages')
}

// Daily/Weekly/Monthly Stats Logic
function isSameWeek(date1, date2) {
    const getMonday = (date) => {
        const d = new Date(date);
        const day = d.getDay();
        const diff = day === 0 ? -6 : 1 - day;
        d.setDate(d.getDate() + diff);
        d.setHours(0, 0, 0, 0);
        return d;
    };
    const monday1 = getMonday(date1);
    const monday2 = getMonday(date2);
    return monday1.getTime() === monday2.getTime();
}

const dailyStatsCom = computed(() => { 
  if (!goalsStore.goals.value) return { todayCompleted: 0, todayTotal: 0, weekGoalsCompletedGoals: [], weekGoals: [], monthGoals: [] };
  
  let today = new Date();
  const todayStr = today.toDateString();
  let month = today.getMonth();

  const goalsValue = goalsStore.goals.value;

  const monthGoals = goalsValue.filter(goal => {
    const goalDate = new Date(goal.deadline);
    return goalDate.getMonth() == month;
  });

  const weekGoals = goalsValue.filter(goal => {
      return isSameWeek(goal.deadline, today);
  });
  const weekGoalsCompletedGoals = weekGoals.filter(goal => goal.status === 'completed');

  const todayGoals = goalsValue.filter(goal => {
    const goalDate = new Date(goal.deadline);
    return goalDate.toDateString() === todayStr;
  });
  
  const completedGoals = todayGoals.filter(goal => goal.status === 'completed');
  
  return {
    todayCompleted: completedGoals.length,
    todayTotal: todayGoals.length,
    weekGoalsCompletedGoals,
    weekGoals,
    monthGoals
  }
});

const dailyStats = computed(() => ({
    todayCompleted: dailyStatsCom.value.todayCompleted,
    todayTotal: dailyStatsCom.value.todayTotal
}));

const weeklyStats = computed(() => ({
    weeklyProgress: dailyStatsCom.value.weekGoals.length ? (dailyStatsCom.value.weekGoalsCompletedGoals.length / dailyStatsCom.value.weekGoals.length * 100) : 0,
}));

const monthlyStats = computed(() => ({
    monthlyGoals: dailyStatsCom.value.monthGoals.length
}));

// Update Stats Display
const updateStats = () => {
    if (!goalsStore.goals.value) return;
    const allGoals = goalsStore.goals.value;
    const active = allGoals.filter(g => g.status === 'active').length;
    const completed = allGoals.filter(g => g.status === 'completed').length;
    stats.value = {
        activeGoals: active,
        completedGoals: completed,
        successRate: allGoals.length ? Math.round((completed / allGoals.length) * 100) : 0
    };
};

// Categories
const goalCategories = computed(() => {
  const colors=['#1a7f37','#0969da','#8250df','#cf222e','#bf8700'];
  let tabsa = [];
  if(goalsStore.goals.value) {
      goalsStore.goals.value.forEach(goal => {
        if(goal.tags) tabsa.push(...goal.tags);
      });
  }
  
  let setTab = new Map(); // Use Map for easier lookup
  tabsa.forEach(tag => {
    if(!setTab.has(tag)){
      setTab.set(tag, {
          name: tag, 
          label: tag, 
          count: 1, 
          color: colors[setTab.size % colors.length]
      });
    } else {
      setTab.get(tag).count++;
    }
  });
  return Array.from(setTab.values());
});

// Search & Filter
const searchQuery = ref('');
const currentFilter = ref('all');
const filterOptions = [
  { text: '全部状态', value: 'all' },
  { text: '进行中', value: 'active' },
  { text: '已完成', value: 'completed' },
  { text: '已过期', value: 'overdue' }
];

const filteredGoals = computed(() => {
  if (!goalsStore.goals.value) return [];
  let filtered = goalsStore.goals.value;
  
  if (currentFilter.value !== 'all') {
    filtered = filtered.filter(goal => goal.status === currentFilter.value);
  }
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(goal => 
      (goal.title && goal.title.toLowerCase().includes(query)) ||
      (goal.description && goal.description.toLowerCase().includes(query))
    );
  }
  
  return filtered;
});

// Methods
const handleAvatarUpload = async (file) => {
    // Mock upload implementation
    showToast('头像上传功能待对接');
    // In real app, use uploadAvatar(file.file)
};

const createNewGoal = () => {
    router.push('/add-goal'); // Assuming this route exists
};

const goToSettings = (tab = 'profile') => {
    router.push('/settings');
};

const editGoal = (goal) => {
    showToast(`编辑: ${goal.title}`);
    // router.push(`/mobile/goal/${goal.id}`);
};

const completeGoal = (goal) => {
    showDialog({
        title: '确认完成',
        message: `确定将 "${goal.title}" 标记为完成吗？`,
        showCancelButton: true
    }).then(() => {
        // Call API to update status
        showToast('已标记为完成');
        // Mock update local
        goal.status = 'completed';
        goal.progress = 100;
    }).catch(() => {});
};

// Helpers
const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return `${date.getMonth() + 1}/${date.getDate()}`;
};

const getGoalTagType = (status) => {
    const map = { active: 'primary', completed: 'success', overdue: 'danger' };
    return map[status] || 'default';
};

const getGoalStatusText = (status) => {
    const map = { active: '进行中', completed: '已完成', overdue: '已过期' };
    return map[status] || '未知';
};

const getProgressColor = (percentage) => {
    if (percentage >= 100) return '#07c160';
    if (percentage >= 50) return '#1989fa';
    return '#ff976a';
};

const hexToRgba = (hex, alpha) => {
    // Simple hex to rgba
    let r = 0, g = 0, b = 0;
    if (hex.length === 4) {
        r = parseInt(hex[1] + hex[1], 16);
        g = parseInt(hex[2] + hex[2], 16);
        b = parseInt(hex[3] + hex[3], 16);
    } else if (hex.length === 7) {
        r = parseInt(hex[1] + hex[2], 16);
        g = parseInt(hex[3] + hex[4], 16);
        b = parseInt(hex[5] + hex[6], 16);
    }
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
};

onMounted(async () => {
  initUserData();
  await getGoals();
  updateStats();
});

</script>

<style scoped lang="scss">
.app-container {
  --bg-primary: #f0f2f5;
  --bg-secondary: rgba(255, 255, 255, 0.72);
  --bg-glass: rgba(255, 255, 255, 0.55);
  --text-primary: #1a1a2e;
  --text-secondary: #8e8e9a;
  --text-tertiary: #a0a0a0;
  --card-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  --brand-color: #00c9a7;
  --glass-bg: rgba(255, 255, 255, 0.78);
  --border-line: rgba(0, 0, 0, 0.06);

  min-height: 100vh;
  background: linear-gradient(160deg, #f0fdf9 0%, #f0f2f5 30%, #f8f4ff 70%, #f0fdf9 100%);
  color: var(--text-primary);
  transition: background 0.5s ease, color 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
}

.app-container.dark-mode {
  --bg-primary: #0a0a0f;
  --bg-secondary: rgba(30, 30, 35, 0.7);
  --bg-glass: rgba(30, 30, 35, 0.5);
  --text-primary: #e8e8f0;
  --text-secondary: #888;
  --text-tertiary: #666;
  --card-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
  --glass-bg: rgba(30, 30, 35, 0.75);
  --border-line: rgba(255, 255, 255, 0.06);

  background: transparent;
}

.mobile-profile {
  padding-bottom: 80px;
  position: relative;
  z-index: 10;
  
  :deep(.van-nav-bar) {
    background: var(--glass-bg);
    backdrop-filter: blur(20px) saturate(180%);
    -webkit-backdrop-filter: blur(20px) saturate(180%);
    
    .nav-title {
      font-weight: 600;
      font-size: 18px;
      color: var(--text-primary);
    }
    
    &::after {
      display: none;
    }
  }
  
  .profile-header-card {
    padding: 20px 16px;
    background: var(--bg-glass);
    backdrop-filter: blur(12px) saturate(150%);
    -webkit-backdrop-filter: blur(12px) saturate(150%);
    border-bottom: 1px solid var(--border-line);
    border-radius: 0 0 24px 24px;
    box-shadow: var(--card-shadow);
    margin-bottom: 16px;
    
    .user-info-row {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      
      .avatar-wrapper {
        position: relative;
        margin-right: 16px;
        
        .user-avatar {
          border: 2px solid var(--border-line);
        }
        
        .edit-badge {
          position: absolute;
          bottom: 0;
          right: 0;
          background: var(--brand-color);
          color: white;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 2px solid var(--border-line);
        }
      }
      
      .info-content {
        flex: 1;
        
        .username {
          font-size: 20px;
          font-weight: bold;
          margin: 0 0 4px 0;
          color: var(--text-primary);
        }
        
        .bio {
          font-size: 13px;
          color: var(--text-secondary);
          margin: 0 0 8px 0;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .edit-btn {
            height: 24px;
            padding: 0 10px;
        }
      }
    }
    
    .stats-row {
      display: flex;
      justify-content: space-around;
      
      .stat-item {
        text-align: center;
        
        .stat-num {
          display: block;
          font-size: 18px;
          font-weight: 700;
          color: var(--text-primary);
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }
    }
  }
  
  .tab-content {
    padding: 0 16px;
  .section-card {
    background: var(--bg-glass);
    backdrop-filter: blur(12px) saturate(150%);
    -webkit-backdrop-filter: blur(12px) saturate(150%);
    border: 1px solid var(--border-line);
    border-radius: 16px;
    padding:2%;
    margin-bottom: 16px;
    box-shadow: var(--card-shadow);
  }

  /* 好友入口 */
  .friend-entry {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 0 6px;
  }

  .friend-entry-info {
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--text-primary);
    font-size: 14px;
  }   
    .card-title {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 12px 0;
      color: var(--text-primary);
    }
    
    .progress-grid {
      .grid-stat {
        text-align: center;
        .val {
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: var(--brand-color);
          margin-bottom: 4px;
        }
        .label {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }
    }
    
    .categories-scroll {
      display: flex;
      overflow-x: auto;
      gap: 8px;
      padding-bottom: 4px;
      scrollbar-width: none;
      &::-webkit-scrollbar {
        display: none;
      }
      
      .category-pill {
        flex: 0 0 auto;
        display: flex;
        align-items: center;
        padding: 6px 12px;
        border-radius: 20px;
        border: 1px solid;
        font-size: 12px;
        
        .cat-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          margin-right: 6px;
        }
        
        .cat-count {
          margin-left: 6px;
          opacity: 0.8;
          font-size: 11px;
        }
      }
    }
    
    &.heatmap-card {
        .heatmap-wrapper {
            overflow-x: auto;
            scrollbar-width: none;
            &::-webkit-scrollbar {
              display: none;
            }
        }
    }
  }
  
  .filter-bar {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    gap: 8px;
    
    .goal-search {
      flex: 1;
      padding: 0;
      background: transparent;
      
      :deep(.van-search__content) {
        background: var(--bg-glass);
        backdrop-filter: blur(12px) saturate(150%);
        -webkit-backdrop-filter: blur(12px) saturate(150%);
        border: 1px solid var(--border-line);
        border-radius: 100px;
      }
      
      :deep(.van-field__control) {
        color: var(--text-primary);
      }
    }
    
    .goal-filter {
      flex: 0 0 100px;
      
      :deep(.van-dropdown-menu__bar) {
        background: transparent;
        box-shadow: none;
        height: 34px;
      }
      :deep(.van-dropdown-menu__title) {
        font-size: 13px;
      }
    }
  }
  
  .goals-list {
    .goal-swipe-item {
      margin-bottom: 12px;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: var(--card-shadow);
    }
    
    .goal-card {
      background: var(--bg-glass);
      backdrop-filter: blur(12px) saturate(150%);
      -webkit-backdrop-filter: blur(12px) saturate(150%);
      border: 1px solid var(--border-line);
      padding: 16px;
      
      .goal-card-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 8px;
        
        .title-wrap {
          display: flex;
          align-items: center;
          gap: 6px;
          
          .goal-title {
            font-weight: 600;
            font-size: 16px;
            color: var(--text-primary);
          }
        }
      }
      
      .goal-desc {
        font-size: 13px;
        color: var(--text-secondary);
        margin-bottom: 12px;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .goal-progress-row {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 12px;
        
        .goal-progress-bar {
          flex: 1;
        }
        
        .progress-text {
          font-size: 12px;
          color: var(--text-secondary);
          min-width: 30px;
          text-align: right;
        }
      }
      
      .goal-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .tags-list {
          display: flex;
          gap: 6px;
          
          .mini-tag {
            font-size: 10px;
            padding: 2px 6px;
            background: rgba(125, 125, 125, 0.15);
            color: var(--text-secondary);
            border-radius: 4px;
          }
        }
        
        .deadline {
          font-size: 11px;
          color: var(--text-tertiary);
        }
      }
    }
    
    .delete-button {
      height: 100%;
    }
  }
  
  .fab-btn {
    position: fixed;
    bottom: 90px;
    right: 20px;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: var(--brand-color);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
    z-index: 99;
    
    &:active {
      transform: scale(0.95);
    }
  }
}

.qr-scan-sheet {
  padding: 16px 16px 24px;
}

.qr-scan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.qr-video-wrapper {
  position: relative;
  width: 100%;
  max-width: 360px;
  margin: 12px auto 0;
  border-radius: 16px;
  overflow: hidden;
  background: #000;
}

.qr-video {
  width: 100%;
  height: auto;
  display: block;
}

.qr-video-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.qr-frame {
  width: 220px;
  height: 220px;
  border-radius: 16px;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 0 100vmax rgba(0, 0, 0, 0.4);
  box-sizing: border-box;
}

.qr-tip-text {
  margin-top: 12px;
  font-size: 13px;
  color: var(--text-secondary);
  text-align: center;
}

.qr-error-text {
  margin-top: 8px;
  font-size: 12px;
  color: #ff4d4f;
  text-align: center;
}
</style>

<style>
/* Global styles for camera transparency */
body.camera-active,
body.camera-active html,
body.camera-active #app {
  background: transparent !important;
}
body.camera-active .mobile-profile {
  background: transparent !important;
}



/* 2. 强制 Vant Popup 的所有层级透明 (关键) */
body.camera-active .van-popup {
  background: transparent !important; 
  box-shadow: none !important;
}

/* 针对 HTML 元素也进行透明处理 (防止 iOS 默认白色背景) */
html.camera-active {
  background: transparent !important;
}

/* 3. 确保你的主容器也是透明的 */
body.camera-active .mobile-profile {
  background: transparent !important;
}

/* 4. 关键：隐藏 #app 内容，防止其背景色遮挡相机 */
body.camera-active #app {

  opacity: 0.2;
  transition: opacity 0.3s;
  /* pointer-events: none; */
}

/* 4. 扫码框的 wrapper 在 native 下必须透明 */
body.camera-active .qr-video-wrapper {
  background: transparent !important;
}
/* 新增：确保 APP 容器本身是透明的，但里面的页面要有背景色 */
body.camera-active #app {
  background: transparent !important;
}

/* 新增：给你的主页面强制加上背景色，为了遮挡住相机 */
body.camera-active .mobile-profile {
  background-color: var(--bg-primary) !important; /* 或者是 #fff / #000 */
  /* 确保主页面不透明，这样除了弹窗区域外，用户看不到底下的相机 */
}

/* 强制扫码框所在的容器透明 (这就是那个"洞") */
body.camera-active .qr-video-wrapper {
  background: transparent !important;
  box-shadow: 0 0 0 4000px rgba(0, 0, 0, 0.6); /* 可选：用超大阴影模拟遮罩效果 */
}
</style>
