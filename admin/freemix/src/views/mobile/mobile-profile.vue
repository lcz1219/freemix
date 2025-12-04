<template>
  <div class="mobile-profile">
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
              width="80"
              height="80"
              :src="avatarUrl"
              fit="cover"
              class="user-avatar"
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
    <van-tabs 
      v-model:active="activeTab" 
      sticky 
      animated 
      swipeable 
      background="transparent"
      line-width="20px"
      line-height="3px"
      color="var(--primary-color)"
      title-active-color="var(--primary-color)"
    >
      <!-- 概览 Tab -->
      <van-tab title="概览" name="overview">
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
              <HotMap />
            </div>
          </div>
        </div>
      </van-tab>

      <!-- 目标 Tab -->
      <van-tab title="目标" name="goals">
        <div class="tab-content">
          <!-- 搜索与筛选 -->
          <div class="filter-bar">
            <van-search 
              v-model="searchQuery" 
              placeholder="搜索目标..." 
              shape="round"
              background="transparent"
              class="goal-search"
            />
            <van-dropdown-menu :overlay="false" class="goal-filter">
              <van-dropdown-item v-model="currentFilter" :options="filterOptions" />
            </van-dropdown-menu>
          </div>

          <!-- 目标列表 -->
          <div class="goals-list">
            <van-empty v-if="filteredGoals.length === 0" description="暂无符合条件的目标" />
            
            <van-swipe-cell v-for="goal in filteredGoals" :key="goal.id" class="goal-swipe-item">
              <div class="goal-card" @click="editGoal(goal)">
                <div class="goal-card-header">
                  <div class="title-wrap">
                    <span class="goal-icon">🎯</span>
                    <span class="goal-title">{{ goal.title }}</span>
                  </div>
                  <van-tag :type="getGoalTagType(goal.status)" size="medium">{{ getGoalStatusText(goal.status) }}</van-tag>
                </div>
                
                <p class="goal-desc">{{ goal.description }}</p>
                
                <div class="goal-progress-row">
                  <van-progress 
                    :percentage="goal.progress" 
                    :color="getProgressColor(goal.progress)" 
                    stroke-width="6"
                    :show-pivot="false"
                    track-color="var(--bg-tertiary)"
                    class="goal-progress-bar"
                  />
                  <span class="progress-text">{{ goal.progress }}%</span>
                </div>
                
                <div class="goal-footer">
                  <div class="tags-list">
                    <span 
                      v-for="tag in goal.tags" 
                      :key="tag" 
                      class="mini-tag"
                    >#{{ tag }}</span>
                  </div>
                  <span class="deadline" v-if="goal.deadline">
                    截止: {{ formatDate(goal.deadline) }}
                  </span>
                </div>
              </div>
              
              <template #right>
                <van-button square text="编辑" type="primary" class="delete-button" @click="editGoal(goal)" />
                <van-button square text="完成" type="success" class="delete-button" v-if="goal.status === 'active'" @click="completeGoal(goal)" />
                <van-button square text="删除" type="danger" class="delete-button" />
              </template>
            </van-swipe-cell>
          </div>
        </div>
      </van-tab>

      <!-- 分析 Tab (简化版) -->
      <van-tab title="分析" name="analytics">
         <div class="tab-content">
            <van-empty description="移动端分析报表开发中" image="search" />
         </div>
      </van-tab>
    </van-tabs>

    <!-- 悬浮添加按钮 -->
    <div class="fab-btn" @click="createNewGoal">
      <van-icon name="plus" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, inject } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { showToast, showDialog } from 'vant';
import { useUser } from '@/hooks/useUser';
import { useGoals } from '@/hooks/useGoals';
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
.mobile-profile {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 80px;
  
  :deep(.van-nav-bar) {
    background-color: #121212;
    backdrop-filter: blur(10px);
    
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
    background: var(--bg-primary);
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
          border: 2px solid var(--bg-primary);
        }
        
        .edit-badge {
          position: absolute;
          bottom: 0;
          right: 0;
          background: var(--primary-color);
          color: white;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 2px solid var(--bg-secondary);
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
  }
  
  .section-card {
    background: var(--bg-primary);
    border-radius: 16px;
    padding: 16px;
    margin-bottom: 16px;
    box-shadow: var(--card-shadow);
    
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
          color: var(--primary-color);
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
      background: var(--bg-primary);
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
            background: var(--bg-primary);
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
    background: var(--primary-color);
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
</style>
