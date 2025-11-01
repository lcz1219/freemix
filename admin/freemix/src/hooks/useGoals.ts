import { ref, onMounted } from 'vue';
import { useStore } from 'vuex';
import { getMPaths, isSuccess } from '@/utils/request.js';

export interface Goal {
  _id?: string;
  title: string;
  progress: number;
  status: 'completed' | 'in-progress' | 'expired';
  deadline: string;
  deadlineString?: string;
  owner: string;
  [key: string]: any;
}

export function useGoals() {
  const store = useStore();
  const goals = ref<Goal[]>([]);
  const selectedGoal = ref<Goal>({});
  const showDetailModal = ref(false);
  
  // 格式化日期
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  };
  
  // 获取目标数据
  const getGoals = async () => {
    const res = await getMPaths("getGoals", store.state.user.username, "正在获取目标数据...");
    if (isSuccess(res)) {
      goals.value = res.data.data;
      goals.value?.forEach(goal => {
        goal.deadlineString = formatDate(goal.deadline);
      });
      
      // 如果当前有选中的目标，更新选中的目标数据
      if (selectedGoal.value && selectedGoal.value._id) {
        const updatedGoal = goals.value.find(g => g._id === selectedGoal.value._id);
        if (updatedGoal) {
          selectedGoal.value = { ...updatedGoal };
        }
      }
    }
  };
  
  // 显示目标详情
  const showGoalDetail = (goal: Goal) => {
    selectedGoal.value = { ...goal };
    showDetailModal.value = true;
  };
  
  // 保存目标
  const saveGoal = (updatedGoal: Goal) => {
    // 更新本地数据
    const index = goals.value.findIndex(g => g._id === updatedGoal._id);
    if (index !== -1) {
      goals.value[index] = { ...updatedGoal };
    }
  };
  
  // 按月份过滤目标
  const filterMonth = () => {
    const monthList = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    goals.value.forEach(e => {
      const date = new Date(e.deadline);
      monthList[date.getMonth()]++;
    });
    return monthList;
  };
  
  // 页面加载时获取目标数据
  onMounted(async () => {
    await getGoals();
  });
  
  return {
    goals,
    selectedGoal,
    showDetailModal,
    getGoals,
    showGoalDetail,
    saveGoal,
    filterMonth,
    formatDate
  };
}