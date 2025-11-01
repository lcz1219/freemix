<template>
  <div class="github-profile-container" :class="{ dark: isDark }">
    <!-- Header Section -->
    <div class="profile-header">
      <div class="container-lg">
        <div class="profile-avatar-section">
          <div class="avatar-wrapper">
            <n-avatar
              round
              :size="260"
              :src="avatarUrl"
              class="user-avatar"
              @click="uploadAvatar"
            />
            <div class="avatar-overlay" @click="uploadAvatar">
              <n-icon size="24" class="camera-icon">
                <CameraOutline />
              </n-icon>
            </div>
          </div>
        </div>
        
        <div class="profile-info-section">
          <div class="profile-info-header">
            <h1 class="profile-username">{{ userProfile.username }}</h1>
            <n-button 
              class="edit-profile-btn" 
              @click="goToSettings"
              size="small"
              secondary
            >
              编辑资料
            </n-button>
          </div>
          
          <p class="profile-bio" v-if="userProfile.bio">{{ userProfile.bio }}</p>
          <p class="profile-bio empty" v-else>专注于目标管理与个人成长</p>
          
          <div class="profile-meta">
            <div class="meta-item">
              <!-- <n-icon><TargetOutline /></n-icon> -->
              <span>{{ stats.activeGoals }} 个进行中的目标</span>
            </div>
            <div class="meta-item">
              <n-icon><CheckmarkDoneOutline /></n-icon>
              <span>{{ stats.completedGoals }} 个已完成目标</span>
            </div>
            <div class="meta-item">
              <n-icon><TrendingUpOutline /></n-icon>
              <span>{{ stats.successRate }}% 完成率</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="profile-nav">
      <div class="container-lg">
        <nav class="underlined-nav">
          <div class="nav-links">
            <button 
              v-for="tab in tabs" 
              :key="tab.name"
              :class="['nav-link', { active: activeTab === tab.name }]"
              @click="activeTab = tab.name"
            >
              {{ tab.label }}
              <span class="counter" v-if="tab.count">{{ tab.count }}</span>
            </button>
          </div>
        </nav>
      </div>
    </div>

    <!-- Main Content -->
    <div class="profile-content">
      <div class="container-lg">
        <!-- Overview Tab -->
        <div v-if="activeTab === 'overview'" class="overview-tab">
          <div class="layout-grid">
            <!-- Left Sidebar -->
            <div class="layout-sidebar">
              <div class="profile-sidebar">
                <div class="sidebar-section">
                  <h3 class="sidebar-title">目标统计</h3>
                  <div class="stats-overview">
                    <div class="stat-item">
                      <span class="stat-label">今日完成</span>
                      <span class="stat-value">{{ dailyStats.todayCompleted }}/{{ dailyStats.todayTotal }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="stat-label">本周进度</span>
                      <span class="stat-value">{{ weeklyStats.weeklyProgress }}%</span>
                    </div>
                    <div class="stat-item">
                      <span class="stat-label">本月目标</span>
                      <span class="stat-value">{{ monthlyStats.monthlyGoals }} 个</span>
                    </div>
                  </div>
                </div>
                
                <div class="sidebar-section">
                  <h3 class="sidebar-title">目标分类</h3>
                  <div class="categories-list">
                    <div v-for="category in goalCategories" :key="category.id" class="category-item">
                      <span class="category-dot" :style="{ backgroundColor: category.color }"></span>
                      <span class="category-name">{{ category.name }}</span>
                      <span class="category-count">{{ category.count }}</span>
                    </div>
                  </div>
                </div>
                
                <div class="sidebar-section">
                  <h3 class="sidebar-title">快速操作</h3>
                  <div class="quick-actions">
                    <n-button @click="createNewGoal" block class="action-btn">
                      <n-icon><AddOutline /></n-icon>
                      创建新目标
                    </n-button>
                    <n-button @click="viewAllGoals" block class="action-btn secondary">
                      查看所有目标
                    </n-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Main Content -->
            <div class="layout-main">
              <!-- Progress Graph -->
              <div class="progress-section">
                <div class="section-header">
                  <h2 class="section-title">目标进度</h2>
                  <div class="progress-stats">
                    <span class="progress-count">最近30天完成 {{ recentStats.recentCompleted }} 个目标</span>
                    <div class="progress-settings">
                      <button class="settings-btn" @click="viewProgressReport">查看详细报告</button>
                    </div>
                  </div>
                </div>
                
                <div class="progress-graph">
                  <div class="graph-placeholder">
                    <div class="months-row">
                      <span v-for="month in progressMonths" :key="month" class="month-label">{{ month }}</span>
                    </div>
                    <div class="graph-grid">
                      <div 
                        v-for="day in 364" 
                        :key="day" 
                        class="progress-day"
                        :class="`level-${getProgressLevel(day)}`"
                        :title="`${getDayProgress(day)} 个目标完成于 ${getDateFromDay(day)}`"
                      ></div>
                    </div>
                    <div class="graph-legend">
                      <span>少</span>
                      <div class="legend-levels">
                        <div class="legend-level level-0"></div>
                        <div class="legend-level level-1"></div>
                        <div class="legend-level level-2"></div>
                        <div class="legend-level level-3"></div>
                      </div>
                      <span>多</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Recent Goals Activity -->
              <div class="activity-section">
                <div class="section-header">
                  <h2 class="section-title">最近目标动态</h2>
                </div>
                
                <div class="activity-timeline">
                  <div v-for="activity in goalActivities" :key="activity.id" class="activity-item">
                    <div class="activity-date">
                      {{ formatActivityDate(activity.time) }}
                    </div>
                    <div class="activity-content">
                      <div class="activity-icon">
                        <n-icon :component="activity.icon" />
                      </div>
                      <div class="activity-details">
                        <p class="activity-description">{{ activity.description }}</p>
                        <div class="activity-goal" v-if="activity.goal">
                          <span class="goal-name">{{ activity.goal }}</span>
                          <span class="goal-category">{{ activity.category }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Goals Tab -->
        <div v-if="activeTab === 'goals'" class="goals-tab">
          <div class="goals-header">
            <div class="search-box">
              <input type="text" placeholder="搜索目标..." class="search-input" v-model="searchQuery" />
            </div>
            <div class="goal-filters">
              <button 
                v-for="filter in goalFilters" 
                :key="filter.value"
                :class="['filter-btn', { active: currentFilter === filter.value }]"
                @click="currentFilter = filter.value"
              >
                {{ filter.label }}
              </button>
            </div>
            <n-button class="new-goal-btn" @click="createNewGoal">
              <n-icon><AddOutline /></n-icon>
              新建目标
            </n-button>
          </div>

          <div class="goals-list">
            <div v-for="goal in filteredGoals" :key="goal.id" class="goal-item">
              <div class="goal-header">
                <div class="goal-title-section">
                  <h3 class="goal-name">
                    <n-icon><BookOutline /></n-icon>
                    {{ goal.title }}
                  </h3>
                  <div class="goal-meta">
                    <n-tag :type="getGoalTagType(goal.status)" size="small">
                      {{ getGoalStatusText(goal.status) }}
                    </n-tag>
                    <span class="goal-deadline" v-if="goal.deadline">
                      截止: {{ formatDate(goal.deadline) }}
                    </span>
                  </div>
                </div>
                <div class="goal-actions">
                  <n-button size="small" @click="editGoal(goal)">编辑</n-button>
                  <n-button size="small" v-if="goal.status === 'active'" @click="completeGoal(goal)">
                    标记完成
                  </n-button>
                </div>
              </div>
              
              <p class="goal-description">{{ goal.description }}</p>
              
              <div class="goal-progress">
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: `${goal.progress}%` }"
                  ></div>
                </div>
                <span class="progress-text">{{ goal.progress }}% 完成</span>
              </div>
              
              <div class="goal-footer">
                <div class="goal-categories">
                  <span 
                    v-for="category in goal.categories" 
                    :key="category"
                    class="category-tag"
                    :style="{ backgroundColor: getCategoryColor(category) }"
                  >
                    {{ category }}
                  </span>
                </div>
                <div class="goal-stats">
                  <span class="stat">
                    <n-icon><CalendarOutline /></n-icon>
                    {{ formatRelativeTime(goal.createdAt) }}
                  </span>
                  <span class="stat" v-if="goal.milestones">
                    <n-icon><FlagOutline /></n-icon>
                    {{ goal.milestones }} 个里程碑
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Analytics Tab -->
        <div v-if="activeTab === 'analytics'" class="analytics-tab">
          <div class="analytics-grid">
            <div class="analytics-card">
              <h3 class="card-title">目标完成趋势</h3>
              <div class="chart-placeholder">
                <!-- 这里可以放置折线图 -->
                <div class="chart-content">
                  <p>月度目标完成趋势图表</p>
                </div>
              </div>
            </div>
            
            <div class="analytics-card">
              <h3 class="card-title">分类分布</h3>
              <div class="chart-placeholder">
                <!-- 这里可以放置饼图 -->
                <div class="chart-content">
                  <p>目标分类分布图表</p>
                </div>
              </div>
            </div>
            
            <div class="analytics-card full-width">
              <h3 class="card-title">目标统计</h3>
              <div class="stats-cards">
                <div class="stat-card">
                  <div class="stat-number">{{ analytics.totalGoals }}</div>
                  <div class="stat-label">总目标数</div>
                </div>
                <div class="stat-card">
                  <div class="stat-number">{{ analytics.avgCompletionTime }}</div>
                  <div class="stat-label">平均完成天数</div>
                </div>
                <div class="stat-card">
                  <div class="stat-number">{{ analytics.successRate }}%</div>
                  <div class="stat-label">成功率</div>
                </div>
                <div class="stat-card">
                  <div class="stat-number">{{ analytics.activeStreak }}</div>
                  <div class="stat-label">连续活跃天数</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted, h, computed } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { useDialog, useMessage } from 'naive-ui';
import {
  CameraOutline,
  PersonOutline,
  MailOutline,
  BriefcaseOutline,
  BookOutline,
  StarOutline,
  CheckmarkCircleOutline,
  TimeOutline,
  AddOutline,
  CheckmarkDoneOutline,
  TrendingUpOutline,
  CalendarOutline,
  FlagOutline
} from '@vicons/ionicons5';
import upload from '@/components/upload.vue';
import { baseURL } from '@/utils/request.js';
import { useUser } from '@/hooks/useUser';
import { useDevice } from '@/hooks/useDevice';
import { useNavigation } from '@/hooks/useNavigation';
import { useGoals } from '@/hooks/useGoals';
import { useSettings } from '@/hooks/useSettings';

// 注入主题变量
const isDark = inject('isDark', ref(false));
const goalsStore = useGoals();
let {getGoals} = goalsStore;
// 路由和状态管理
const router = useRouter();
const store = useStore();
const dialog = useDialog();
const message = useMessage();

// 使用hooks
const { userInfo: userProfile, avatarUrl, initUserData, uploadAvatar } = useUser();

// 初始化用户数据
// 初始化
onMounted(async () => {
  initUserData();
  await getGoals();
});

// 跳转到设置页面
const goToSettings = (section = 'profile') => {
  router.push(`/settings?tab=${section}`);
};

// 标签页控制
const activeTab = ref('overview');
const tabs = ref([
  { name: 'overview', label: '概览', count: null },
  { name: 'goals', label: '目标', count: 12 },
  { name: 'analytics', label: '分析', count: null },
  { name: 'achievements', label: '成就', count: 5 }
]);

// 搜索和筛选
const searchQuery = ref('');
const currentFilter = ref('all');
const goalFilters = ref([
  { value: 'all', label: '全部' },
  { value: 'active', label: '进行中' },
  { value: 'completed', label: '已完成' },
  { value: 'overdue', label: '已过期' }
]);

// 统计数据
const stats = ref({
  activeGoals: 8,
  completedGoals: 24,
  successRate: 75
});

// const dailyStats = ref({
//   todayCompleted: 3,
//   todayTotal: 5
// });
function isSameWeek(date1, date2, startOfWeek = 1) {
    // 获取日期所在周的周一日期
    const getMonday = (date) => {
        const d = new Date(date);
        const day = d.getDay(); // 0=周日, 1=周一, ..., 6=周六
        // 计算与周一的差值
        const diff = day === 0 ? -6 : 1 - day;
        d.setDate(d.getDate() + diff);
        d.setHours(0, 0, 0, 0); // 归一化时间
        return d;
    };
    
    const monday1 = getMonday(date1);
    const monday2 = getMonday(date2);
    
    return monday1.getTime() === monday2.getTime();
}
const dailyStats=computed(() => {
  return {
    todayCompleted: dailyStatsCom.value.todayCompleted,
    todayTotal: dailyStatsCom.value.todayTotal
  }
});
const dailyStatsCom=computed(() => { 
  // 确保goals已经加载
  if (!goalsStore.goals.value) return { todayCompleted: 0, todayTotal: 0 };
  
  let today = new Date();
  today.setDate(today.getDate()+1);

  const todayStr = today.toDateString();

let month = today.getMonth();

const monthGoals = goalsStore.goals.value.filter(goal => {
    const goalDate = new Date(goal.deadline);
    return goalDate.getMonth() == month;
  });

    const weekGoals = goalsStore.goals.value.filter(goal => {
      return isSameWeek(goal.deadline, todayStr);
  });
   const weekGoalsCompletedGoals = weekGoals.filter(goal => goal.status === 'completed');
  

  // 过滤今日目标
  const todayGoals = goalsStore.goals.value.filter(goal => {
    const goalDate = new Date(goal.deadline);
    return goalDate.toDateString() === todayStr;
  });
  
  // 计算今日完成的目标
  const completedGoals = todayGoals.filter(goal => goal.status === 'completed');
  
  return {
    todayCompleted: completedGoals.length,
    todayTotal: todayGoals.length,
    weekGoalsCompletedGoals,
    weekGoals,
    monthGoals
    
  }
});



const weeklyStats =computed(() => { 
  return {
    weeklyProgress: dailyStatsCom.value.weekGoalsCompletedGoals.length / dailyStatsCom.value.weekGoals.length * 100
  }
});



const monthlyStats  =computed(()=> { 
  return {monthlyGoals: dailyStatsCom.value.monthGoals.length}
})

const recentStats = ref({
  recentCompleted: 12
});

const analytics = ref({
  totalGoals: 32,
  avgCompletionTime: 14,
  successRate: 75,
  activeStreak: 28
});

// 目标分类
const goalCategories = ref([
  { id: 1, name: '工作职业', color: '#1a7f37', count: 8 },
  { id: 2, name: '学习成长', color: '#0969da', count: 6 },
  { id: 3, name: '健康生活', color: '#8250df', count: 5 },
  { id: 4, name: '财务理财', color: '#cf222e', count: 4 },
  { id: 5, name: '人际关系', color: '#bf8700', count: 3 }
]);

// 月份标签
const progressMonths = ref(['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']);

// 目标数据
const goals = ref([
  {
    id: 1,
    title: '完成Vue3项目开发',
    description: '完成个人目标管理系统的Vue3版本开发与部署',
    status: 'active',
    progress: 75,
    categories: ['工作职业', '学习成长'],
    deadline: '2025-12-31',
    createdAt: new Date('2025-10-01'),
    milestones: 4
  },
  {
    id: 2,
    title: '每周健身3次',
    description: '坚持每周至少3次健身锻炼，提升身体素质',
    status: 'active',
    progress: 90,
    categories: ['健康生活'],
    deadline: '2025-12-31',
    createdAt: new Date('2025-09-15'),
    milestones: 12
  },
  {
    id: 3,
    title: '阅读12本书',
    description: '年度阅读计划，涵盖技术、文学、心理学等领域',
    status: 'completed',
    progress: 100,
    categories: ['学习成长'],
    deadline: '2025-12-31',
    createdAt: new Date('2025-01-01'),
    milestones: 12
  }
]);

// 目标动态
const goalActivities = ref([
  { 
    id: 1, 
    icon: CheckmarkCircleOutline, 
    description: '完成了目标 "学习Vue3高级特性"', 
    time: new Date('2025-11-01'),
    goal: '学习Vue3高级特性',
    category: '学习成长'
  },
  { 
    id: 2, 
    icon: BookOutline, 
    description: '创建了新目标 "完成项目部署"', 
    time: new Date('2025-10-28'),
    goal: '完成项目部署',
    category: '工作职业'
  },
  { 
    id: 3, 
    // icon: TargetOutline, 
    description: '开始了目标 "每周健身计划"', 
    time: new Date('2025-10-15'),
    goal: '每周健身计划',
    category: '健康生活'
  }
]);

// 计算属性：筛选目标
const filteredGoals = computed(() => {
  let filtered = goals.value;
  
  // 根据状态筛选
  if (currentFilter.value !== 'all') {
    filtered = filtered.filter(goal => goal.status === currentFilter.value);
  }
  
  // 根据搜索词筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(goal => 
      goal.title.toLowerCase().includes(query) ||
      goal.description.toLowerCase().includes(query)
    );
  }
  
  return filtered;
});



// 上传头像


// 目标操作函数
const createNewGoal = () => {
  message.info('创建新目标功能');
  // 这里可以跳转到创建目标页面或打开模态框
};

const viewAllGoals = () => {
  activeTab.value = 'goals';
};

const viewProgressReport = () => {
  message.info('查看进度报告功能');
};

const editGoal = (goal) => {
  message.info(`编辑目标: ${goal.title}`);
};

const completeGoal = (goal) => {
  goal.status = 'completed';
  goal.progress = 100;
  message.success(`目标 "${goal.title}" 已完成！`);
};

// 工具函数
const getGoalTagType = (status) => {
  const types = {
    active: 'info',
    completed: 'success',
    overdue: 'error'
  };
  return types[status] || 'default';
};

const getGoalStatusText = (status) => {
  const texts = {
    active: '进行中',
    completed: '已完成',
    overdue: '已过期'
  };
  return texts[status] || '未知';
};

const getCategoryColor = (categoryName) => {
  const category = goalCategories.value.find(cat => cat.name === categoryName);
  return category ? category.color : '#666666';
};

const getProgressLevel = (day) => {
  // 模拟进度数据
  return Math.floor(Math.random() * 4);
};

const getDayProgress = (day) => {
  return Math.floor(Math.random() * 5);
};



// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

// 格式化动态日期
const formatActivityDate = (date) => {
  return date.toLocaleDateString('zh-CN', {
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  });
};

// 格式化相对时间
const formatRelativeTime = (date) => {
  const now = new Date();
  const diffMs = now - date;
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    return '今天';
  } else if (diffDays === 1) {
    return '昨天';
  } else if (diffDays < 30) {
    return `${diffDays} 天前`;
  } else {
    const diffMonths = Math.floor(diffDays / 30);
    return `${diffMonths} 个月前`;
  }
};

// 获取贡献图的日期
const getDateFromDay = (day) => {
  const date = new Date();
  date.setDate(date.getDate() - (364 - day));
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
};

// 初始化
onMounted(async () => {
  initUserData();
  await getGoals();
});
</script>

<style scoped>
.github-profile-container {
  min-height: 100vh;
  background-color: #ffffff;
  color: #1f2328;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
}

.github-profile-container.dark {
  background-color: #0d1117;
  color: #e6edf3;
}

/* Layout Containers */
.container-lg {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

/* Header Section */
.profile-header {
  background: linear-gradient(135deg, #f6f8fa 0%, #ffffff 100%);
  padding: 40px 0 20px 0;
  border-bottom: 1px solid #d0d7de;
}

.github-profile-container.dark .profile-header {
  background: linear-gradient(135deg, #010409 0%, #0d1117 100%);
  border-bottom-color: #30363d;
}

.profile-avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.user-avatar {
  border: 1px solid #d0d7de;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.github-profile-container.dark .user-avatar {
  border-color: #30363d;
}

.avatar-overlay {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 40px;
  height: 40px;
  background: #1a7f37;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.profile-info-section {
  text-align: center;
}

.profile-info-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 8px;
}

.profile-username {
  font-size: 2rem;
  font-weight: 600;
  margin: 0;
  color: #1f2328;
}

.github-profile-container.dark .profile-username {
  color: #e6edf3;
}

.edit-profile-btn {
  background: #f6f8fa;
  border: 1px solid #d0d7de;
  color: #1f2328;
  font-weight: 500;
}

.github-profile-container.dark .edit-profile-btn {
  background: #21262d;
  border-color: #363b42;
  color: #e6edf3;
}

.profile-bio {
  font-size: 1.25rem;
  margin: 0 0 16px 0;
  color: #656d76;
}

.github-profile-container.dark .profile-bio {
  color: #7d8590;
}

.profile-bio.empty {
  font-style: italic;
  opacity: 0.7;
}

.profile-meta {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.875rem;
  color: #656d76;
}

.github-profile-container.dark .meta-item {
  color: #7d8590;
}

/* Navigation */
.profile-nav {
  background: #ffffff;
  border-bottom: 1px solid #d0d7de;
}

.github-profile-container.dark .profile-nav {
  background: #0d1117;
  border-bottom-color: #21262d;
}

.underlined-nav {
  display: flex;
  align-items: center;
}

.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: none;
  border: none;
  color: #656d76;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  position: relative;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.nav-link:hover {
  color: #1f2328;
  border-bottom-color: #d0d7de;
}

.github-profile-container.dark .nav-link:hover {
  color: #e6edf3;
  border-bottom-color: #363b42;
}

.nav-link.active {
  color: #1f2328;
  border-bottom-color: #1a7f37;
  font-weight: 600;
}

.github-profile-container.dark .nav-link.active {
  color: #e6edf3;
  border-bottom-color: #3fb950;
}

.counter {
  background: #d0d7de;
  color: #656d76;
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.github-profile-container.dark .counter {
  background: #363b42;
  color: #7d8590;
}

/* Main Content */
.profile-content {
  padding: 40px 0;
}

.layout-grid {
  display: grid;
  grid-template-columns: 296px 1fr;
  gap: 32px;
}

.layout-sidebar {
  position: sticky;
  top: 32px;
  height: fit-content;
}

.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-section {
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
}

.github-profile-container.dark .sidebar-section {
  border-color: #30363d;
  background: #0d1117;
}

.sidebar-title {
  font-size: 0.875rem;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #1f2328;
}

.github-profile-container.dark .sidebar-title {
  color: #e6edf3;
}

/* 统计数据样式 */
.stats-overview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.stat-label {
  font-size: 0.875rem;
  color: #656d76;
}

.github-profile-container.dark .stat-label {
  color: #7d8590;
}

.stat-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2328;
}

.github-profile-container.dark .stat-value {
  color: #e6edf3;
}

/* 分类列表样式 */
.categories-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.category-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.category-name {
  flex: 1;
  font-size: 0.875rem;
  color: #656d76;
}

.github-profile-container.dark .category-name {
  color: #7d8590;
}

.category-count {
  font-size: 0.75rem;
  color: #656d76;
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 12px;
}

.github-profile-container.dark .category-count {
  color: #7d8590;
  background: #21262d;
}

/* 快速操作按钮 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-btn {
  justify-content: flex-start;
}

.action-btn.secondary {
  background: #f6f8fa;
  color: #1f2328;
}

.github-profile-container.dark .action-btn.secondary {
  background: #21262d;
  color: #e6edf3;
}

/* Progress Graph */
.progress-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  color: #1f2328;
}

.github-profile-container.dark .section-title {
  color: #e6edf3;
}

.progress-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.progress-count {
  font-size: 0.875rem;
  color: #656d76;
}

.github-profile-container.dark .progress-count {
  color: #7d8590;
}

.settings-btn {
  background: none;
  border: none;
  color: #0969da;
  font-size: 0.875rem;
  cursor: pointer;
}

.github-profile-container.dark .settings-btn {
  color: #2f81f7;
}

.progress-graph {
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
}

.github-profile-container.dark .progress-graph {
  border-color: #30363d;
  background: #0d1117;
}

.graph-placeholder {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.months-row {
  display: flex;
  justify-content: space-between;
  padding-left: 24px;
}

.month-label {
  font-size: 0.75rem;
  color: #656d76;
}

.github-profile-container.dark .month-label {
  color: #7d8590;
}

.graph-grid {
  display: grid;
  grid-template-columns: repeat(53, 12px);
  grid-template-rows: repeat(7, 12px);
  gap: 2px;
}

.progress-day {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  background: #ebedf0;
  cursor: pointer;
  transition: transform 0.2s;
}

.progress-day:hover {
  transform: scale(1.2);
}

.github-profile-container.dark .progress-day {
  background: #161b22;
}

.progress-day.level-1 { background: #9be9a8; }
.progress-day.level-2 { background: #40c463; }
.progress-day.level-3 { background: #30a14e; }
.progress-day.level-4 { background: #216e39; }

.github-profile-container.dark .progress-day.level-1 { background: #0e4429; }
.github-profile-container.dark .progress-day.level-2 { background: #006d32; }
.github-profile-container.dark .progress-day.level-3 { background: #26a641; }
.github-profile-container.dark .progress-day.level-4 { background: #39d353; }

.graph-legend {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-end;
  font-size: 0.75rem;
  color: #656d76;
}

.github-profile-container.dark .graph-legend {
  color: #7d8590;
}

.legend-levels {
  display: flex;
  gap: 2px;
}

.legend-level {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-level.level-0 { background: #ebedf0; }
.legend-level.level-1 { background: #9be9a8; }
.legend-level.level-2 { background: #40c463; }
.legend-level.level-3 { background: #30a14e; }

.github-profile-container.dark .legend-level.level-0 { background: #161b22; }
.github-profile-container.dark .legend-level.level-1 { background: #0e4429; }
.github-profile-container.dark .legend-level.level-2 { background: #006d32; }
.github-profile-container.dark .legend-level.level-3 { background: #26a641; }

/* Activity Section */
.activity-section {
  border: 1px solid #d0d7de;
  border-radius: 6px;
}

.github-profile-container.dark .activity-section {
  border-color: #30363d;
  background: #0d1117;
}

.activity-timeline {
  padding: 16px;
}

.activity-item {
  padding: 16px 0;
  border-bottom: 1px solid #d0d7de;
}

.github-profile-container.dark .activity-item {
  border-bottom-color: #21262d;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-date {
  font-size: 0.75rem;
  color: #656d76;
  margin-bottom: 8px;
  font-weight: 600;
}

.github-profile-container.dark .activity-date {
  color: #7d8590;
}

.activity-content {
  display: flex;
  gap: 12px;
}

.activity-icon {
  color: #1a7f37;
  margin-top: 2px;
}

.github-profile-container.dark .activity-icon {
  color: #3fb950;
}

.activity-details {
  flex: 1;
}

.activity-description {
  margin: 0 0 4px 0;
  color: #1f2328;
  font-size: 0.875rem;
}

.github-profile-container.dark .activity-description {
  color: #e6edf3;
}

.activity-goal {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.75rem;
  color: #656d76;
}

.github-profile-container.dark .activity-goal {
  color: #7d8590;
}

.goal-name {
  color: #0969da;
  font-weight: 500;
}

.github-profile-container.dark .goal-name {
  color: #2f81f7;
}

.goal-category {
  color: #656d76;
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 12px;
}

.github-profile-container.dark .goal-category {
  color: #7d8590;
  background: #21262d;
}

/* Goals Tab */
.goals-tab {
  padding: 16px 0;
}

.goals-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
}

.search-box {
  flex: 1;
  max-width: 320px;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  background: #ffffff;
  color: #1f2328;
  font-size: 0.875rem;
}

.github-profile-container.dark .search-input {
  background: #0d1117;
  border-color: #30363d;
  color: #e6edf3;
}

.goal-filters {
  display: flex;
  gap: 8px;
}

.filter-btn {
  padding: 6px 12px;
  border: 1px solid #d0d7de;
  background: #f6f8fa;
  color: #1f2328;
  border-radius: 6px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
}

.github-profile-container.dark .filter-btn {
  background: #21262d;
  border-color: #363b42;
  color: #e6edf3;
}

.filter-btn.active {
  background: #1a7f37;
  border-color: #1a7f37;
  color: #ffffff;
}

.new-goal-btn {
  background: #1a7f37;
  color: #ffffff;
  border: none;
}

.goals-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.goal-item {
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
  transition: border-color 0.2s;
}

.github-profile-container.dark .goal-item {
  border-color: #30363d;
  background: #0d1117;
}

.goal-item:hover {
  border-color: #1a7f37;
}

.github-profile-container.dark .goal-item:hover {
  border-color: #3fb950;
}

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.goal-title-section {
  flex: 1;
}

.goal-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.25rem;
  font-weight: 600;
  color: #0969da;
  margin: 0 0 8px 0;
}

.github-profile-container.dark .goal-name {
  color: #2f81f7;
}

.goal-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.goal-deadline {
  font-size: 0.875rem;
  color: #656d76;
}

.github-profile-container.dark .goal-deadline {
  color: #7d8590;
}

.goal-actions {
  display: flex;
  gap: 8px;
}

.goal-description {
  margin: 0 0 16px 0;
  color: #656d76;
  font-size: 0.875rem;
  line-height: 1.4;
}

.github-profile-container.dark .goal-description {
  color: #7d8590;
}

.goal-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #f6f8fa;
  border-radius: 4px;
  overflow: hidden;
}

.github-profile-container.dark .progress-bar {
  background: #21262d;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #1a7f37, #3fb950);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.875rem;
  color: #656d76;
  font-weight: 500;
}

.github-profile-container.dark .progress-text {
  color: #7d8590;
}

.goal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goal-categories {
  display: flex;
  gap: 8px;
}

.category-tag {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  color: white;
}

.goal-stats {
  display: flex;
  gap: 16px;
  font-size: 0.75rem;
  color: #656d76;
}

.github-profile-container.dark .goal-stats {
  color: #7d8590;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Analytics Tab */
.analytics-tab {
  padding: 16px 0;
}

.analytics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.analytics-card {
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
}

.github-profile-container.dark .analytics-card {
  border-color: #30363d;
  background: #0d1117;
}

.analytics-card.full-width {
  grid-column: 1 / -1;
}

.card-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #1f2328;
}

.github-profile-container.dark .card-title {
  color: #e6edf3;
}

.chart-placeholder {
  height: 200px;
  background: #f6f8fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #656d76;
}

.github-profile-container.dark .chart-placeholder {
  background: #161b22;
  color: #7d8590;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  text-align: center;
  padding: 16px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
}

.github-profile-container.dark .stat-card {
  border-color: #30363d;
  background: #161b22;
}

.stat-number {
  font-size: 2rem;
  font-weight: 600;
  color: #1a7f37;
  margin-bottom: 4px;
}

.github-profile-container.dark .stat-number {
  color: #3fb950;
}

.stat-label {
  font-size: 0.875rem;
  color: #656d76;
}

.github-profile-container.dark .stat-label {
  color: #7d8590;
}

/* Responsive Design */
@media (max-width: 768px) {
  .layout-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .layout-sidebar {
    position: static;
  }
  
  .profile-info-header {
    flex-direction: column;
    gap: 8px;
  }
  
  .profile-meta {
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .goals-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .search-box {
    max-width: 100%;
  }
  
  .goal-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .goal-actions {
    align-self: flex-end;
  }
  
  .goal-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .analytics-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .goal-filters {
    flex-wrap: wrap;
  }
}
</style>