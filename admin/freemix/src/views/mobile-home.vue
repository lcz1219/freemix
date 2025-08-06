<template>
  <div class="mobile-home" :class="isDark ? 'dark' : 'light'">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="目标追踪者"
      fixed
      placeholder
      safe-area-inset-top
    >
      <template #right>
        <van-icon 
          :name="isDark ? 'underway' : 'underway-o'" 
          size="20" 
          @click="toggleTheme"
        />
      </template>
    </van-nav-bar>
    
    <!-- 主要内容 -->
    <div class="content">
      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <h1>掌控你的目标，衡量你的成功</h1>
        <p>可视化您的进度，庆祝每一个里程碑</p>
        
        <van-button 
          type="primary" 
          round 
          icon="plus" 
          @click="addGoal"
        >
          添加新目标
        </van-button>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stats-section">
        <van-grid :column-num="3" :border="false">
          <van-grid-item>
            <div class="stat-item">
              <div class="stat-value">{{ goalIngCount }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-item">
              <div class="stat-value">{{ goalFinishCount }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-item">
              <div class="stat-value">{{ goalExpireCount }}</div>
              <div class="stat-label">已过期</div>
            </div>
          </van-grid-item>
        </van-grid>
      </div>
      
      <!-- 目标列表 -->
      <div class="goals-section">
        <van-tabs v-model:active="activeTab" sticky offset-top="46">
          <van-tab title="全部" name="all">
            <van-list
              v-model:loading="loading"
              :finished="finished"
              finished-text="没有更多了"
              @load="onLoad"
            >
              <van-cell 
                v-for="goal in goals" 
                :key="goal.id"
                class="goal-item"
                is-link
                @click="viewGoalDetail(goal)"
              >
                <template #title>
                  <div class="goal-title">{{ goal.title }}</div>
                  <van-tag 
                    :type="getTagType(goal.tags[0])" 
                    class="goal-tag"
                  >
                    {{ goal.tags[0] }}
                  </van-tag>
                </template>
                <template #label>
                  <van-progress 
                    :percentage="goal.progress" 
                    :show-pivot="false" 
                    class="goal-progress"
                  />
                  <div class="goal-meta">
                    <span>截止: {{ formatDate(goal.deadline) }}</span>
                    <span>负责人: {{ goal.owner }}</span>
                  </div>
                </template>
              </van-cell>
            </van-list>
          </van-tab>
          
          <van-tab title="进行中" name="in-progress">
            <van-empty 
              v-if="inProgressGoals.length === 0" 
              description="暂无进行中的目标"
            />
            <van-cell 
              v-for="goal in inProgressGoals" 
              :key="goal.id"
              class="goal-item"
              is-link
              @click="viewGoalDetail(goal)"
            >
              <template #title>
                <div class="goal-title">{{ goal.title }}</div>
                <van-tag 
                  :type="getTagType(goal.tags[0])" 
                  class="goal-tag"
                >
                  {{ goal.tags[0] }}
                </van-tag>
              </template>
              <template #label>
                <van-progress 
                  :percentage="goal.progress" 
                  :show-pivot="false" 
                  class="goal-progress"
                />
                <div class="goal-meta">
                  <span>截止: {{ formatDate(goal.deadline) }}</span>
                  <span>负责人: {{ goal.owner }}</span>
                </div>
              </template>
            </van-cell>
          </van-tab>
          
          <van-tab title="已完成" name="completed">
            <van-empty 
              v-if="completedGoals.length === 0" 
              description="暂无已完成的目标"
            />
            <van-cell 
              v-for="goal in completedGoals" 
              :key="goal.id"
              class="goal-item"
              is-link
              @click="viewGoalDetail(goal)"
            >
              <template #title>
                <div class="goal-title">{{ goal.title }}</div>
                <van-tag 
                  :type="getTagType(goal.tags[0])" 
                  class="goal-tag"
                >
                  {{ goal.tags[0] }}
                </van-tag>
              </template>
              <template #label>
                <van-progress 
                  :percentage="goal.progress" 
                  :show-pivot="false" 
                  class="goal-progress"
                />
                <div class="goal-meta">
                  <span>截止: {{ formatDate(goal.deadline) }}</span>
                  <span>负责人: {{ goal.owner }}</span>
                </div>
              </template>
            </van-cell>
          </van-tab>
        </van-tabs>
      </div>
    </div>
    
    <!-- 底部导航 -->
    <van-tabbar v-model="activeTabbar" route>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/add-goal">添加</van-tabbar-item>
      <van-tabbar-item icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, inject } from 'vue';
import { useRouter } from 'vue-router';
import { Toast } from 'vant';

const router = useRouter();
const isDark = inject('isDark', ref(false));

// 标签页相关
const activeTab = ref('all');
const activeTabbar = ref(0);

// 目标数据
const goals = ref([
  { id: 1, title: '职业发展', progress: 65, status: 'in-progress', deadline: '2023-12-31', owner: '张三', tags: ['工作'] },
  { id: 2, title: '技能提升计划', progress: 80, status: 'in-progress', deadline: '2023-11-15', owner: '李四', tags: ['学习'] },
  { id: 3, title: '健身目标', progress: 45, status: 'in-progress', deadline: '2024-02-28', owner: '王五', tags: ['运动'] },
  { id: 4, title: '阅读计划', progress: 100, status: 'completed', deadline: '2023-12-20', owner: '赵六', tags: ['学习'] },
  { id: 5, title: '学习新语言', progress: 30, status: 'in-progress', deadline: '2024-03-15', owner: '钱七', tags: ['学习'] },
  { id: 6, title: '项目交付', progress: 90, status: 'in-progress', deadline: '2023-10-30', owner: '孙八', tags: ['工作'] }
]);

// 计算属性
const goalFinishCount = computed(() => {
  return goals.value.filter(goal => goal.progress === 100).length;
});

const goalExpireCount = computed(() => {
  return goals.value.filter(goal => Date.now() - Date.parse(goal.deadline) > 0 && goal.progress < 100).length;
});

const goalIngCount = computed(() => {
  return goals.value.filter(goal => goal.progress !== 100 && Date.now() - Date.parse(goal.deadline) < 0).length;
});

const inProgressGoals = computed(() => {
  return goals.value.filter(goal => goal.progress !== 100 && Date.now() - Date.parse(goal.deadline) < 0);
});

const completedGoals = computed(() => {
  return goals.value.filter(goal => goal.progress === 100);
});

// 列表相关
const loading = ref(false);
const finished = ref(false);

// 方法
const toggleTheme = () => {
  isDark.value = !isDark.value;
  localStorage.setItem('theme-dark', JSON.stringify(isDark.value));
};

const addGoal = () => {
  router.push('/add-goal');
};

const viewGoalDetail = (goal) => {
  // 这里可以跳转到目标详情页
  Toast.success(`查看目标: ${goal.title}`);
};

const getTagType = (tag) => {
  switch (tag) {
    case '学习':
      return 'success';
    case '工作':
      return 'primary';
    case '生活':
      return 'warning';
    case '运动':
      return 'danger';
    default:
      return 'default';
  }
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN');
};

const onLoad = () => {
  // 模拟加载更多数据
  setTimeout(() => {
    loading.value = false;
    finished.value = true;
  }, 1000);
};
</script>

<style scoped>
.mobile-home {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.mobile-home.dark {
  background-color: #1a1a1a;
  color: #ffffff;
}

.content {
  padding-bottom: 60px;
}

.welcome-section {
  padding: 20px;
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-section h1 {
  font-size: 22px;
  margin-bottom: 10px;
  font-weight: bold;
}

.welcome-section p {
  font-size: 14px;
  margin-bottom: 20px;
  opacity: 0.9;
}

:deep(.van-button) {
  height: 36px;
  font-size: 14px;
}

.stats-section {
  padding: 15px 0;
  background-color: white;
}

.dark .stats-section {
  background-color: #2d2d2d;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #8a2be2;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.dark .stat-label {
  color: #aaa;
}

.goals-section {
  padding: 10px 0;
}

.goal-item {
  margin-bottom: 10px;
}

.goal-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.goal-tag {
  font-size: 10px;
}

.goal-progress {
  margin: 8px 0;
}

.goal-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.dark .goal-meta {
  color: #aaa;
}

:deep(.van-tabbar) {
  background-color: white;
}

.dark :deep(.van-tabbar) {
  background-color: #2d2d2d;
}
</style>