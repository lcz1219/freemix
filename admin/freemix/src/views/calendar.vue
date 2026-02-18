<template>
  <div class="calendar-view" :class="isDark ? 'dark-theme' : 'light-theme'">
    <div class="page-header">
      <!-- <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">日历视图</h1> -->
      <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
        在日历上查看您的目标分布，合理规划时间
      </p>
    </div>

    <n-card :class="isDark ? 'feature-card' : 'feature-card-light'" class="calendar-card">
      <n-calendar
        v-model:value="value"
        #="{ year, month, date }"
        @update:value="handleUpdateValue"
      >
        <div class="calendar-cell-content">
          <div v-for="goal in getGoalsForDate(year, month, date)" :key="goal.id" class="goal-item" @click.stop="handleGoalClick(goal)">
            <n-tag :type="getGoalStatusType(goal.status)" size="small" round :bordered="false" class="goal-tag">
              {{ goal.title }}
            </n-tag>
          </div>
        </div>
      </n-calendar>
    </n-card>
    
    <!-- 目标详情弹窗 (复用 GoalDetail 组件) -->
    <GoalDetail
      v-if="selectedGoal"
      v-model:show="showDetailModal"
      :goal="selectedGoal"
      @update="handleGoalUpdate"
    />
  </div>
</template>

<script setup>
import { ref, computed, inject, onMounted } from 'vue';
import { NCalendar, NCard, NTag, useMessage } from 'naive-ui';
import { useStore } from 'vuex';
import { getMPaths, isSuccess } from '@/utils/request.js';
import { useUser } from '@/hooks/useUser';
import GoalDetail from '@/components/GoalDetail.vue';

const store = useStore();
const message = useMessage();
const { userInfo } = useUser();
const isDark = inject('isDark', ref(true));
const value = ref(Date.now());
const showDetailModal = ref(false);
const selectedGoal = ref(null);

// 目标数据
const goals = ref([]);

// 获取目标数据
const getGoals = async () => {
  if (!userInfo.value || !userInfo.value.username) return;
  
  console.log("userInfo.value.username", userInfo.value.username);
  const res = await getMPaths("getGoals", userInfo.value.username, "正在获取目标数据...");
  if (isSuccess(res)) {
    goals.value = res.data.data;

  }
};

// 初始化加载目标
onMounted(() => {
  getGoals();
});

// 根据日期获取目标
const getGoalsForDate = (year, month, date) => {
  const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(date).padStart(2, '0')}`;
  return goals.value.filter(goal => {
    if (!goal.deadline) return false;
    const goalDate = new Date(goal.deadline);
    const goalDateStr = `${goalDate.getFullYear()}-${String(goalDate.getMonth() + 1).padStart(2, '0')}-${String(goalDate.getDate()).padStart(2, '0')}`;
    return goalDateStr === dateStr;
  });
};

// 获取目标状态对应的 Tag 类型
const getGoalStatusType = (status) => {
  switch (status) {
    case 'completed': return 'success';
    case 'in_progress': return 'info';
    case 'pending': return 'warning';
    case 'expired': return 'error';
    default: return 'default';
  }
};

const handleUpdateValue = (_, { year, month, date }) => {
  message.info(`${year}-${month}-${date}`);
};

const handleGoalClick = (goal) => {
  selectedGoal.value = goal;
  showDetailModal.value = true;
};

const handleGoalUpdate = (updatedGoal) => {
  // 更新本地数据
  const index = goals.value.findIndex(g => g.id === updatedGoal.id);
  if (index !== -1) {
    goals.value[index] = { ...updatedGoal };
  }
  showDetailModal.value = false;
};

</script>

<style scoped>
.calendar-view {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 20px;
}

.hero-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-title-light {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  background: linear-gradient(120deg, #2b32b2 0%, #1488cc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 1.1rem;
  color: #a0a0a0;
  max-width: 600px;
}

.hero-subtitle-light {
  font-size: 1.1rem;
  color: #666;
  max-width: 600px;
}

.calendar-card {
  flex: 1;
  border-radius: 16px;
  overflow: hidden;
  border: none;
}

.feature-card {
  background: rgba(30, 30, 30, 0.6);
  backdrop-filter: blur(10px);
}

.feature-card-light {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.calendar-cell-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 4px;
  overflow-y: auto;
  max-height: 80px;
}

.goal-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.goal-item:hover {
  transform: scale(1.02);
}

.goal-tag {
  width: 100%;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  padding: 0 8px;
  height: 24px;
}

/* 适配深色模式日历 */
.dark-theme :deep(.n-calendar) {
  --n-text-color: #fff;
  --n-title-font-weight: 600;
  --n-border-color: rgba(255, 255, 255, 0.1);
  --n-day-text-color: #eee;
  --n-date-color-current: rgba(129, 198, 131, 0.2);
}

.light-theme :deep(.n-calendar) {
  --n-border-color: rgba(0, 0, 0, 0.1);
}

/* 隐藏滚动条但保留功能 */
.calendar-cell-content::-webkit-scrollbar {
  width: 4px;
}

.calendar-cell-content::-webkit-scrollbar-thumb {
  background-color: rgba(128, 128, 128, 0.3);
  border-radius: 2px;
}
</style>
