<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'mobile-home dark' : 'mobile-home light'">
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>

    <!-- 顶部导航栏 -->
    <n-layout-header bordered class="mobile-header">
      <div class="header-content">
        <h1 class="header-title">目标追踪者</h1>
        <n-button quaternary circle @click="toggleTheme" class="theme-toggle-button">
          <n-icon size="20">
            <svg v-if="isDark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M12,9c1.7,0,3,1.3,3,3s-1.3,3-3,3s-3-1.3-3-3S10.3,9,12,9z M12,7c-2.8,0-5,2.2-5,5s2.2,5,5,5s5-2.2,5-5 S14.8,7,12,7z M2,13l2,0c0.6,0,1-0.4,1-1s-0.4-1-1-1l-2,0c-0.6,0-1,0.4-1,1S1.4,13,2,13z M20,13l2,0c0.6,0,1-0.4,1-1s-0.4-1-1-1l-2,0 c-0.6,0-1,0.4-1,1S19.4,13,20,13z M12,2l0,2c0,0.6,0.4,1,1,1s1-0.4,1-1l0-2c0-0.6-0.4-1-1-1S12,1.4,12,2z M12,20l0,2 c0,0.6,0.4,1,1,1s1-0.4,1-1l0-2c0-0.6-0.4-1-1-1C12.4,19,12,19.4,12,20z M6.3,6.3L7.7,7.7c0.4,0.4,1,0.4,1.4,0s0.4-1,0-1.4 L7.7,4.9C7.3,4.5,6.7,4.5,6.3,4.9S5.9,5.9,6.3,6.3z M16.3,16.3L17.7,17.7c0.4,0.4,1,0.4,1.4,0s0.4-1,0-1.4l-1.4-1.4 c-0.4-0.4-1-0.4-1.4,0S15.9,15.9,16.3,16.3z M17.7,6.3L16.3,4.9c-0.4-0.4-1-0.4-1.4,0s-0.4,1,0,1.4l1.4,1.4 c0.4,0.4,1,0.4,1.4,0S18.1,6.7,17.7,6.3z M7.7,16.3L6.3,17.7c-0.4,0.4-0.4,1,0,1.4c0.4,0.4,1,0.4,1.4,0l1.4-1.4 c0.4-0.4,0.4-1,0-1.4S8.1,15.9,7.7,16.3z"/>
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M12,9c-1.7,0-3,1.3-3,3s1.3,3,3,3s3-1.3,3-3S13.7,9,12,9z M12,15c-1.7,0-3-1.3-3-3s1.3-3,3-3s3,1.3,3,3 S13.7,15,12,15z M12,7c-2.8,0-5,2.2-5,5s2.2,5,5,5s5-2.2,5-5S14.8,7,12,7z M12,3c-5,0-9,4-9,9s4,9,9,9s9-4,9-9S17,3,12,3z M12,1c6.1,0,11,4.9,11,11s-4.9,11-11,11S1,18.1,1,12S5.9,1,12,1z"/>
            </svg>
          </n-icon>
        </n-button>
      </div>
    </n-layout-header>
    
    <!-- 主要内容 -->
    <n-layout-content class="main-content-wrapper">
      <div class="content">
        <!-- 欢迎区域 -->
        <section class="welcome-section">
          <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">掌控你的目标，衡量你的成功</h1>
          <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
            可视化您的进度，庆祝每一个里程碑
          </p>
          
          <n-button 
            type="primary" 
            size="large"
            round 
            strong
            @click="addGoal"
          >
            <template #icon>
              <n-icon>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                  <path d="M20,12H4v-1c0-0.6,0.4-1,1-1h14c0.6,0,1,0.4,1,1V12z"></path>
                  <path d="M4,12h16v1c0,0.6-0.4,1-1,1H5c-0.6,0-1-0.4-1-1V12z"></path>
                  <path d="M20,10H4c-0.6,0-1,0.4-1,1v2c0,0.6,0.4,1,1,1h16c0.6,0,1-0.4,1-1v-2C21,10.4,20.6,10,20,10z M20,12H4v-1h16V12z"></path>
                </svg>
              </n-icon>
            </template>
            添加新目标
          </n-button>
        </section>
        
        <!-- 统计卡片 -->
        <section class="stats-section">
          <n-grid :cols="3" :x-gap="10" :y-gap="10">
            <n-grid-item>
              <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
                <div class="stat-item">
                  <div class="stat-value">{{ goalIngCount }}</div>
                  <div :class="isDark ? 'stat-label' : 'stat-label-light'">进行中</div>
                </div>
              </n-card>
            </n-grid-item>
            <n-grid-item>
              <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
                <div class="stat-item">
                  <div class="stat-value" style="color: #00c9a7;">{{ goalFinishCount }}</div>
                  <div :class="isDark ? 'stat-label' : 'stat-label-light'">已完成</div>
                </div>
              </n-card>
            </n-grid-item>
            <n-grid-item>
              <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
                <div class="stat-item">
                  <div class="stat-value" style="color: #ff6b6b;">{{ goalExpireCount }}</div>
                  <div :class="isDark ? 'stat-label' : 'stat-label-light'">已过期</div>
                </div>
              </n-card>
            </n-grid-item>
          </n-grid>
        </section>
        
        <!-- 目标列表 -->
        <section class="goals-section">
          <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
            <n-tabs v-model:value="activeTab" type="segment" animated>
              <n-tab-pane name="all" tab="全部">
                <div v-if="goals.length === 0" class="empty-state">
                  <n-empty description="暂无目标">
                    <template #extra>
                      <n-button size="small" @click="addGoal">
                        添加目标
                      </n-button>
                    </template>
                  </n-empty>
                </div>
                <n-list v-else>
                  <n-list-item 
                    v-for="goal in goals" 
                    :key="goal.id"
                    class="goal-item"
                    @click="viewGoalDetail(goal)"
                  >
                    <div class="goal-content" >
                      <div class="goal-header">
                        <h3 class="goal-title">{{ goal.title }}</h3>
                        <n-tag 
                          :type="getTagType(goal.tags[0])" 
                          size="small"
                          class="goal-tag"
                        >
                          {{ goal.tags[0]||'无标签' }}
                        </n-tag>
                      </div>
                      <div class="goal-progress-container">
                        <n-progress 
                          type="line" 
                          :percentage="goal.progress" 
                          :indicator-placement="'inside'"
                          :height="10"
                          :color="getStatusColor(goal.status)"
                          :status="goal.status === 'completed' ? 'success' : goal.status === 'expired' ? 'error' : ''"
                        />
                      </div>
                      <div class="goal-meta">
                        <span>截止: {{ formatDate(goal.deadline) }}</span>
                        <span>负责人: {{ goal.owner }}</span>
                      </div>
                    </div>
                  </n-list-item>
                </n-list>
              </n-tab-pane>
              
              <n-tab-pane name="in-progress" tab="进行中">
                <div v-if="inProgressGoals.length === 0" class="empty-state">
                  <n-empty description="暂无进行中的目标" />
                </div>
                <n-list v-else>
                  <n-list-item 
                    v-for="goal in inProgressGoals" 
                    :key="goal.id"
                    class="goal-item"
                    @click="viewGoalDetail(goal)"
                  >
                    <div class="goal-content">
                      <div class="goal-header">
                        <h3 class="goal-title">{{ goal.title }}</h3>
                        <n-tag 
                          :type="getTagType(goal.tags[0])" 
                          size="small"
                          class="goal-tag"
                        >
                          {{ goal.tags[0] }}
                        </n-tag>
                      </div>
                      <div class="goal-progress-container">
                        <n-progress 
                          type="line" 
                          :percentage="goal.progress" 
                          :indicator-placement="'inside'"
                          :height="10"
                          :color="getStatusColor(goal.status)"
                        />
                      </div>
                      <div class="goal-meta">
                        <span>截止: {{ formatDate(goal.deadline) }}</span>
                        <span>负责人: {{ goal.owner }}</span>
                      </div>
                    </div>
                  </n-list-item>
                </n-list>
              </n-tab-pane>
              
              <n-tab-pane name="completed" tab="已完成">
                <div v-if="completedGoals.length === 0" class="empty-state">
                  <n-empty description="暂无已完成的目标" />
                </div>
                <n-list v-else>
                  <n-list-item 
                    v-for="goal in completedGoals" 
                    :key="goal.id"
                    class="goal-item"
                    @click="viewGoalDetail(goal)"
                  >
                    <div class="goal-content">
                      <div class="goal-header">
                        <h3 class="goal-title">{{ goal.title }}</h3>
                        <n-tag 
                          :type="getTagType(goal.tags[0])" 
                          size="small"
                          class="goal-tag"
                        >
                          {{ goal.tags[0] }}
                        </n-tag>
                      </div>
                      <div class="goal-progress-container">
                        <n-progress 
                          type="line" 
                          :percentage="goal.progress" 
                          :indicator-placement="'inside'"
                          :height="10"
                          status="success"
                        />
                      </div>
                      <div class="goal-meta">
                        <span>截止: {{ formatDate(goal.deadline) }}</span>
                        <span>负责人: {{ goal.owner }}</span>
                      </div>
                    </div>
                  </n-list-item>
                </n-list>
              </n-tab-pane>
            </n-tabs>
          </n-card>
        </section>
      </div>
    </n-layout-content>
  </n-layout>
  <van-action-sheet v-model:show="showDetailModal"  title="标题">
   <div v-if="selectedGoal" class="action-sheet">
      <n-descriptions label-placement="left" :column="1" bordered>
        <n-descriptions-item label="目标标题">
          {{ selectedGoal.title }}
        </n-descriptions-item>
        <n-descriptions-item label="目标描述">
          {{ selectedGoal.description || '暂无描述' }}
        </n-descriptions-item>
        <n-descriptions-item label="负责人">
          {{ selectedGoal.owner }}
        </n-descriptions-item>
        <n-descriptions-item label="截止日期">
          {{ formatDate(selectedGoal.deadline) }}
        </n-descriptions-item>
        <n-descriptions-item label="优先级">
          <n-tag :type="getPriorityType(selectedGoal.level)">
            {{ getPriorityText(selectedGoal.level) }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="状态">
          <n-tag :type="getStatusType(selectedGoal.status)">
            {{ getStatusText(selectedGoal.status) }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="进度">
          <n-progress 
            type="line" 
            :percentage="selectedGoal.progress" 
            :indicator-placement="'inside'"
            :height="20"
            :color="getStatusColor(selectedGoal.status)"
            :status="selectedGoal.status === 'completed' ? 'success' : selectedGoal.status === 'expired' ? 'error' : ''"
          />
        </n-descriptions-item>
        <n-descriptions-item label="标签">
          <n-space>
            <n-tag v-for="tag in selectedGoal.tags" :key="tag" type="primary">
              {{ tag }}
            </n-tag>
          </n-space>
        </n-descriptions-item>
      </n-descriptions>

      <n-space v-if="selectedGoal.childGoals && selectedGoal.childGoals.length" vertical class="mt-4">
        <h3>子目标</h3>
        <n-data-table 
          :columns="subGoalColumns" 
          :data="selectedGoal.childGoals" 
          :pagination="false"
          size="small"
        />
      </n-space>
    </div>
</van-action-sheet>
  <!-- 目标详情 -->
  
</template>

<script setup >
import { ref, computed, inject, onMounted, h } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { 
  NLayout, 
  NLayoutHeader, 
  NLayoutContent, 
  NButton, 
  NIcon, 
  NGrid, 
  NGridItem, 
  NCard, 
  NTabs, 
  NTabPane, 
  NList, 
  NListItem, 
  NTag, 
  NProgress, 
  NEmpty,
  useMessage,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NSpace,
  NDataTable
} from 'naive-ui';
import { getMPaths, isSuccess,postM } from '@/utils/request';

const router = useRouter();
const store = useStore();
const isDark = inject('isDark');
const message = useMessage();

// 标签页相关
const activeTab = ref('all');
// 目标数据
const goals = ref([]);

// 目标详情模态框相关
const showDetailModal = ref(false);
const selectedGoal = ref(null);

// 子目标表格列定义
const subGoalColumns = [
  {
    title: '子目标',
    key: 'message'
  },
  {
    title: '状态',
    key: 'finish',
    render(row) {
      return row.finish ? '已完成' : '进行中';
    }
  }, {
    title: '操作',
    key: '',
    render(row,index) {
      return h(NButton, {
        type: 'primary',
        size: 'small',
        disabled: row.finish,
        onClick: () => {
         finishChildGoal(null,index)
        }
      }, () => '完成');
    }
  }
];
// 标记子目标为完成
const finishChildGoal = async (goal, index) => {
  try {
    console.log("goal",selectedGoal.value);
    console.log("index",index);
    
    const updatedGoal = { ...selectedGoal.value };
    updatedGoal.childGoals[index].finish = true;
    updatedGoal.childGoals[index].finishDate = new Date();
    // 更新进度
    const finishedCount = updatedGoal.childGoals.filter((c) => c.finish).length;
    updatedGoal.progress = Math.round(finishedCount / updatedGoal.childGoals.length * 100);

    // 如果所有子目标都完成了，更新目标状态
    if (finishedCount === updatedGoal.childGoals.length) {
      updatedGoal.status = 'completed';
    }

    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      message.success('子目标已完成');
      // 更新本地数据
      // const goalIndex = goals.value.findIndex((g) => g.id === selectedGoal.value.id);
      // console.log(goalIndex);
      
      // if (goalIndex !== -1) {
      //   goals.value[goalIndex] = updatedGoal;
      // }
      getGoals()
      selectedGoal.value = updatedGoal;
    } else {
      message.error('更新失败');
    }
  } catch (error) {
    message.error('操作失败');
    console.error(error);
  }
};

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

// 计算 popup 样式
const popupStyle = computed(() => {
  return {
    backgroundColor: isDark.value ? '#1e1e2e' : '#ffffff',
    color: isDark.value ? '#ffffff' : '#000000'
  };
});

// 方法
const toggleTheme = () => {
  isDark.value = !isDark.value;
  localStorage.setItem('theme-dark', JSON.stringify(isDark.value));
};

const addGoal = () => {
  router.push('/add-goal');
};

const viewGoalDetail = (goal) => {
  selectedGoal.value = goal;
  showDetailModal.value = true;
};

// 获取优先级文本
const getPriorityText = (priority) => {
  switch (priority) {
    case 'low': return '低';
    case 'medium': return '中';
    case 'high': return '高';
    case 'urgent': return '紧急';
    default: return '未设置';
  }
};

// 获取优先级类型
const getPriorityType = (level) => {
  switch (level) {
    case 'low': return 'success';
    case 'medium': return 'info';
    case 'high': return 'warning';
    case 'urgent': return 'error';
    default: return 'default';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'completed': return '已完成';
    case 'in-progress': return '进行中';
    case 'expired': return '已过期';
    default: return '未知';
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'completed': return 'success';
    case 'in-progress': return 'warning';
    case 'expired': return 'error';
    default: return 'default';
  }
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
      return 'error';
    default:
      return 'default';
  }
};

const getStatusColor = (status) => {
  switch (status) {
    case 'completed':
      return '#00c9a7'; // 绿色
    case 'in-progress':
      return '#81c683'; // 紫色
    case 'expired':
      return '#ff6b6b'; // 红色
    default:
      return '#409eff'; // 蓝色
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '未设置';
  
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 获取目标列表
const getGoals = async () => {
  try {
    const res = await getMPaths("getGoals", store.state.user.username, "正在获取目标数据...");
    if (isSuccess(res)) {
      goals.value = res.data.data || [];
      goals.value.forEach(goal => {
        goal.deadlineString = formatDate(goal.deadline);
      });
    } else {
      message.error('获取目标列表失败');
    }
  } catch (error) {
    message.error('获取目标列表时发生错误');
    console.error(error);
  }
};

// 初始化
onMounted(() => {
  getGoals();
});
</script>

<style scoped>
.action-sheet{
  padding: 20px;
}
.mobile-home {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  /* padding-bottom: 60px; 为底部导航栏留出空间 */
}

.mobile-home.dark {
  background: linear-gradient(135deg, #1e1e2e 0%, #2d1b40 100%);
  color: #ffffff;
}

.mobile-home.light {
  background: linear-gradient(135deg, #f0f0f0 0%, #e0e0e0 100%);
  color: #000000;
}


.background-elements {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.gradient-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
}

.gradient-circle.blue {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #1e90ff, transparent 70%);
  top: -150px;
  right: -150px;
}

.gradient-circle.green {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, #00c9a7, transparent 70%);
  bottom: -125px;
  left: -125px;
}

.gradient-circle.purple {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, #81c683, transparent 70%);
  top: 30%;
  left: 20%;
}

.mobile-header {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  padding: 10px 16px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.mobile-header.light {
  background-color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #ffffff;
}

.mobile-header.light .header-title {
  color: #000000;
}

.theme-toggle-button {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.mobile-header.light .theme-toggle-button {
  background-color: rgba(0, 0, 0, 0.05) !important;
}

.main-content-wrapper {
  height: 100vh; /* 减去顶部和底部导航栏的高度 */
  overflow-y: auto;
}

.content {
  padding: 16px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.hero-title,
.hero-title-light {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 10px;
}

.hero-title {
  color: #ffffff;
}

.hero-title-light {
  color: #ffffff;
}

.hero-subtitle,
.hero-subtitle-light {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 20px;
}

.hero-subtitle {
  color: #ffffff;
}

.hero-subtitle-light {
  color: #ffffff;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card,
.stat-card-light {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-card-light {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #81c683;
  margin-bottom: 5px;
}

.stat-label,
.stat-label-light {
  font-size: 12px;
}

.stat-label {
  color: #cccccc;
}

.stat-label-light {
  color: #333333;
}

.goals-section {
  margin-bottom: 20px;
}

.feature-card,
.feature-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.feature-card-light {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
}

.goal-item {
  cursor: pointer;
  transition: background-color 0.3s;
  padding: 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.goal-item:last-child {
  border-bottom: none;
}

.dark .goal-item {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.goal-content {
  padding: 0 12px;
}

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.goal-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  flex: 1;
  margin-right: 10px;
}

.dark .goal-title {
  color: #ffffff;
}

.light .goal-title {
  color: #000000;
}

.goal-tag {
  flex-shrink: 0;
}

.goal-progress-container {
  margin-bottom: 8px;
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

/* 滚动条样式 - Webkit内核浏览器 */
.main-content-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(129, 198, 131, 0.5);
  border-radius: 3px;
}

.main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(129, 198, 131, 0.7);
}

/* 浅色模式滚动条 */
.mobile-home.light .main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
}

.mobile-home.light .main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(129, 198, 131, 0.3);
}

.mobile-home.light .main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(129, 198, 131, 0.5);
}
</style>