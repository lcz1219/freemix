<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'home-container' : 'home-container-light'">
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>

    <!-- 顶部导航栏 -->
    <NavBar active-tab="goalmanagement" />

    <!-- 主内容区域 -->
    <n-layout-content class="main-content-wrapper">
      <div class="main-content">
        <!-- 页面标题 -->
        <section class="page-header">
          <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">目标管理</h1>
          <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
            管理您的所有目标，查看进度并进行编辑
          </p>
        </section>

        <!-- 控制面板 -->
        <section class="control-section">
          <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
            <n-space justify="space-between" align="center">
              <n-space>
                <n-button type="primary" @click="addNewGoal">
                  <template #icon>
                    <n-icon>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                        fill="currentColor">
                        <path d="M20,12H4v-1c0-0.6,0.4-1,1-1h14c0.6,0,1,0.4,1,1V12z"></path>
                        <path d="M4,12h16v1c0,0.6-0.4,1-1,1H5c-0.6,0-1-0.4-1-1V12z"></path>
                        <path
                          d="M20,10H4c-0.6,0-1,0.4-1,1v2c0,0.6,0.4,1,1,1h16c0.6,0,1-0.4,1-1v-2C21,10.4,20.6,10,20,10z M20,12H4v-1h16V12z">
                        </path>
                      </svg>
                    </n-icon>
                  </template>
                  添加新目标
                </n-button>

                <n-button @click="refreshGoals">
                  <template #icon>
                    <n-icon>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                        fill="currentColor">
                        <path
                          d="M17.6,6.4C16.2,5,14.2,4.2,12,4.2c-2.2,0-4.2,0.8-5.6,2.2l1.4,1.4C8.9,6.8,10.4,6.2,12,6.2 c1.6,0,3.1,0.6,4.2,1.6c1.1,1.1,1.7,2.6,1.7,4.2c0,1.6-0.6,3.1-1.7,4.2c-1.1,1.1-2.6,1.7-4.2,1.7c-1.6,0-3.1-0.6-4.2-1.7 L6.4,17.6c1.4,1.4,3.4,2.2,5.6,2.2c2.2,0,4.2-0.8,5.6-2.2c1.4-1.4,2.2-3.4,2.2-5.6C19.8,9.8,19,7.8,17.6,6.4z">
                        </path>
                      </svg>
                    </n-icon>
                  </template>
                  刷新
                </n-button>
              </n-space>

              <n-space>
                <n-input v-model:value="searchQuery" placeholder="搜索目标..." clearable style="width: 200px;">
                  <template #prefix>
                    <n-icon>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                        fill="currentColor">
                        <path
                          d="M21.7,20.3L18,16.6c1.2-1.5,1.9-3.4,1.9-5.4c0-4.4-3.6-8-8-8s-8,3.6-8,8s3.6,8,8,8c2,0,3.9-0.7,5.4-1.9 l3.7,3.7c0.2,0.2,0.5,0.3,0.7,0.3s0.5-0.1,0.7-0.3C22.1,21.3,22.1,20.7,21.7,20.3z M11,17c-3.3,0-6-2.7-6-6s2.7-6,6-6s6,2.7,6,6 S14.3,17,11,17z">
                        </path>
                      </svg>
                    </n-icon>
                  </template>
                </n-input>

                <n-select v-model:value="statusFilter" :options="statusOptions" clearable placeholder="状态筛选"
                  style="width: 120px;" />
              </n-space>
            </n-space>
          </n-card>
        </section>

        <!-- 目标列表 -->
        <section class="goals-section">
          <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
            <div class="card-header">
              <n-icon size="28" color="#8a2be2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                  fill="currentColor">
                  <path
                    d="M21,11.5c0,1.7-1.3,3-3,3s-3-1.3-3-3c0-0.5,0.1-0.9,0.3-1.3l-2-1.2C13.1,10.4,13,10.7,13,11c0,1.7,1.3,3,3,3 s3-1.3,3-3c0-0.7-0.3-1.4-0.7-2H21V11.5z" />
                  <path
                    d="M9,14C7.3,14,6,15.3,6,17s1.3,3,3,3s3-1.3,3-3s-1.3-3-3-3z M9,18c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S9.6,18,9,18 z" />
                  <path
                    d="M6,7C4.3,7,3,8.3,3,10s1.3,3,3,3s3-1.3,3-3S7.7,7,6,7z M6,11c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S6.6,11,6,11z" />
                  <path
                    d="M18,4c-1.7,0-3,1.3-3,3s1.3,3,3,3s3-1.3,3-3S19.7,4,18,4z M18,8c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S18.6,8,18,8z" />
                </svg>
              </n-icon>
              <h2 class="card-title">我的目标</h2>
            </div>

            <el-table :data="filteredGoals" :class="isDark ? 'el-table-dark' : 'el-table-light'" style="width: 100%">
              <el-table-column type="expand">
                <template #default="props">
                  <div class="expanded-content-wrapper">
                    <n-collapse>
                      <n-collapse-item v-for="(childGoal, index) in props.row.childGoals" :key="index" :name="index"
                        class="child-goal-collapse-item">
                        <template #header>
                          <div class="child-goal-header">
                            <div class="child-goal-title">
                              <n-ellipsis style="max-width: 300px">
                                {{ childGoal.message }}
                              </n-ellipsis>
                            </div>
                            <div class="child-goal-status">
                              <n-tag :type="childGoal.finish ? 'success' : 'warning'" size="small">
                                {{ childGoal.finish ? '已完成' : '进行中' }}
                              </n-tag>
                            </div>
                          </div>
                        </template>

                        <div class="child-goal-details">
                          <div class="child-goal-info-row">
                            <!-- <div class="info-item">
                              <n-icon size="16" class="info-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
                                </svg>
                              </n-icon>
                              <span v-if="childGoal.description">{{ childGoal.description }}</span>
                              <span v-else class="no-description">暂无描述</span>
                            </div> -->

                            <div class="info-item">
                              <n-icon size="16" class="info-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                                  fill="currentColor">
                                  <path
                                    d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z" />
                                </svg>
                              </n-icon>
                              <span>完成日期: {{ formatDate(childGoal.finishDate) }}</span>
                              <div>

                              </div>
                              <n-button v-if="!childGoal.finish" type="primary" size="small"
                                @click="finishChildGoal(props.row, index)">
                                <n-icon>
                                  <CheckmarkOutline />
                                </n-icon>
                                完成
                              </n-button>
                              <n-button v-else type="tertiary" size="small"
                                @click="unfinishChildGoal(props.row, index)">
                                取消完成
                              </n-button>
                            </div>
                          </div>

                          <div class="child-goal-actions">

                          </div>
                        </div>
                      </n-collapse-item>

                      <n-collapse-item name="summary" class="summary-collapse-item">
                        <template #header>
                          <div class="summary-header">
                            <n-icon size="18">
                              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                                fill="currentColor">
                                <path
                                  d="M12,2C6.5,2,2,6.5,2,12s4.5,10,10,10s10-4.5,10-10S17.5,2,12,2z M12,20c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8 S16.4,20,12,20z" />
                                <path
                                  d="M13,11.6V7c0-0.6-0.4-1-1-1s-1,0.4-1,1v5.6c-0.6,0.3-1,1-1,1.7c0,1.1,0.9,2,2,2s2-0.9,2-2C14,12.6,13.6,11.9,13,11.6z" />
                              </svg>
                            </n-icon>
                            <span class="summary-title">子目标汇总</span>
                          </div>
                        </template>

                        <div class="summary-content">
                          <div class="summary-stats">
                            <div class="stat-item">
                              <div class="stat-value">{{ props.row.childGoals ? props.row.childGoals.length : 0 }}</div>
                              <div class="stat-label">总计</div>
                            </div>
                            <div class="stat-item">
                              <div class="stat-value" style="color: #00c9a7;">{{props.row.childGoals ?
                                props.row.childGoals.filter((c: any) => c.finish).length : 0}}</div>
                              <div class="stat-label">已完成</div>
                            </div>
                            <div class="stat-item">
                              <div class="stat-value" style="color: #ff6b6b;">{{props.row.childGoals ?
                                props.row.childGoals.filter((c: any) => !c.finish).length : 0}}</div>
                              <div class="stat-label">未完成</div>
                            </div>
                          </div>

                          <n-progress type="line" :percentage="props.row.childGoals && props.row.childGoals.length > 0
                            ? Math.round(props.row.childGoals.filter((c: any) => c.finish).length / props.row.childGoals.length * 100)
                            : 0" :indicator-placement="'inside'" :processing="true"
                            :color="getProgressColor(props.row)" />
                        </div>
                      </n-collapse-item>
                    </n-collapse>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="目标名称" prop="title" />
              <el-table-column label="负责人" prop="owner" />
              <el-table-column label="截止日期" prop="deadlineString" />
              <el-table-column label="进度">
                <template #default="scope">
                  <el-progress :percentage="scope.row.status == 'expired' ? 100 : scope.row.progress" :stroke-width="10"
                    :color="getStatusColor(scope.row.status)"
                    :status="scope.row.status === 'completed' ? 'success' : scope.row.status === 'expired' ? 'exception' : ''" />
                </template>
              </el-table-column>
              <el-table-column label="状态">
                <template #default="scope">
                  <n-tag :type="getStatusTagType(scope.row.status)">
                    {{ getStatusLabel(scope.row.status) }}
                  </n-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center">
                <template #default="scope">
                  <n-button type="warning" circle secondary strong @click="viewGoalDetail(scope.row)"
                    style="margin-right: 10px;">
                    <n-icon size="20">
                      <EyeSharp />
                    </n-icon>

                  </n-button>
                  <n-button type="primary" circle secondary strong @click="editGoal(scope.row)">

                    <n-icon size="20">
                      <PencilOutline />
                    </n-icon>
                  </n-button>
                  <n-button type="primary" circle secondary strong @click="deleteGoal(scope.row)">

                    <n-icon size="20">
                      <ArchiveOutline />
                    </n-icon>
                  </n-button>
                </template>
              </el-table-column>
            </el-table>
          </n-card>
        </section>
      </div>
    </n-layout-content>

    <!-- 目标详情模态框 -->
    <GoalDetail v-model:show="showDetailModal" :goal="selectedGoal" @save="saveGoal" @updateGoal="refreshGoals" />

    <!-- 底部 -->
    <n-layout-footer class="footer" bordered>
      <p>© 2025 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
    </n-layout-footer>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue';
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NCard,
  NButton,
  NIcon,
  NSpace,
  NInput,
  NSelect,
  NTag,
  NProgress,
  NCollapse,
  NCollapseItem,
  useMessage,
  NEllipsis
} from 'naive-ui';
import { ElTable, ElTableColumn, ElButton, ElTag, ElProgress } from 'element-plus';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue';
import GoalDetail from '@/components/GoalDetail.vue';
import { getMPaths, isSuccess, postM } from '@/utils/request';
import { EyeSharp, PencilOutline, CheckmarkOutline,ArchiveOutline } from '@vicons/ionicons5';
import type { DataTableColumns } from 'naive-ui';
import { useStore } from 'vuex';

// 注入主题变量
const isDark = inject('isDark', ref(false));

// 路由和状态管理
const router = useRouter();
const store = useStore();
const message = useMessage();

// 数据状态
const goals = ref<any[]>([]);
const loading = ref(false);
const showDetailModal = ref(false);
const selectedGoal = ref({});

// 筛选和搜索
const searchQuery = ref('');
const statusFilter = ref<string | null>(null);
 const deleteGoal=(row)=>{

  postM('deleteGoal',{row}).then((res)=>{
    if(isSuccess(res)){
      message.success('删除成功');
      refreshGoals();
    }
  })
 }
// 状态筛选选项
const statusOptions = [
  { label: '进行中', value: 'in-progress' },
  { label: '已完成', value: 'completed' },
  { label: '已过期', value: 'expired' }
];

// 分页配置
const pagination = {
  pageSize: 10
};

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'completed':
      return 'success';
    case 'in-progress':
      return 'warning';
    case 'expired':
      return 'error';
    default:
      return 'info';
  }
};

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'completed':
      return '已完成';
    case 'in-progress':
      return '进行中';
    case 'expired':
      return '已过期';
    default:
      return '未知';
  }
};

// 获取状态对应的颜色
const getStatusColor = (status: string) => {
  switch (status) {
    case 'completed':
      return '#00c9a7'; // 绿色
    case 'in-progress':
      return '#8a2be2'; // 紫色
    case 'expired':
      return '#ff6b6b'; // 红色
    default:
      return '#409eff'; // 蓝色
  }
};

// 获取进度条颜色
const getProgressColor = (goal: any) => {
  if (!goal.childGoals || goal.childGoals.length === 0) return '#8a2be2';

  const finishedCount = goal.childGoals.filter((c: any) => c.finish).length;
  const progress = Math.round(finishedCount / goal.childGoals.length * 100);

  if (progress === 100) return '#00c9a7';
  if (progress >= 50) return '#8a2be2';
  return '#409eff';
};

// 标记子目标为完成
const finishChildGoal = async (goal: any, index: number) => {
  try {
    const updatedGoal = { ...goal };
    updatedGoal.childGoals[index].finish = true;
    updatedGoal.childGoals[index].finishDate = new Date();
    // 更新进度
    const finishedCount = updatedGoal.childGoals.filter((c: any) => c.finish).length;
    updatedGoal.progress = Math.round(finishedCount / updatedGoal.childGoals.length * 100);

    // 如果所有子目标都完成了，更新目标状态
    if (finishedCount === updatedGoal.childGoals.length) {
      updatedGoal.status = 'completed';
    }

    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      message.success('子目标已完成');
      // 更新本地数据
      const goalIndex = goals.value.findIndex((g: any) => g.id === goal.id);
      if (goalIndex !== -1) {
        goals.value[goalIndex] = updatedGoal;
      }
    } else {
      message.error('更新失败');
    }
  } catch (error) {
    message.error('操作失败');
    console.error(error);
  }
};

// 取消子目标完成状态
const unfinishChildGoal = async (goal: any, index: number) => {
  try {
    const updatedGoal = { ...goal };
    updatedGoal.childGoals[index].finish = false;

    // 更新进度
    const finishedCount = updatedGoal.childGoals.filter((c: any) => c.finish).length;
    updatedGoal.progress = Math.round(finishedCount / updatedGoal.childGoals.length * 100);

    // 如果之前是完成状态，现在需要改为进行中
    if (updatedGoal.status === 'completed') {
      updatedGoal.status = 'in-progress';
    }

    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      message.success('已取消完成状态');
      // 更新本地数据
      const goalIndex = goals.value.findIndex((g: any) => g.id === goal.id);
      if (goalIndex !== -1) {
        goals.value[goalIndex] = updatedGoal;
      }
    } else {
      message.error('更新失败');
    }
  } catch (error) {
    message.error('操作失败');
    console.error(error);
  }
};

// 筛选后的目标列表
const filteredGoals = computed(() => {
  let result = [...goals.value];

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(goal =>
      goal.title.toLowerCase().includes(query) ||
      goal.description.toLowerCase().includes(query) ||
      goal.owner.toLowerCase().includes(query)
    );
  }

  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(goal => goal.status === statusFilter.value);
  }

  return result;
});

// 格式化日期
const formatDate = (dateString: string) => {
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
  loading.value = true;
  try {
    const res = await getMPaths("getGoals", store.state.user.username);
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
  } finally {
    loading.value = false;
  }
};

// 添加新目标
const addNewGoal = () => {
  router.push('/add-goal');
};

// 刷新目标列表
const refreshGoals = () => {
  getGoals();
  message.success('目标列表已刷新');
};

// 查看目标详情
const viewGoalDetail = (goal: any) => {
  selectedGoal.value = { ...goal };
  showDetailModal.value = true;
};

// 编辑目标
const editGoal = (goal: any) => {
  // 这里可以跳转到编辑页面，或者在模态框中编辑
  selectedGoal.value = { ...goal };
  showDetailModal.value = true;
};

// 保存目标
const saveGoal = (updatedGoal: any) => {
  // 这里可以实现保存目标的逻辑
  console.log('保存目标:', updatedGoal);
  message.success('目标已保存');
  refreshGoals();
};

// 处理排序变化
const handleSorterChange = (sorter: any) => {
  // 实现排序逻辑
  console.log('排序器变化:', sorter);
};

// 初始化
onMounted(() => {
  getGoals();
});
</script>

<style scoped>
/* 继承主页样式 */
.home-container {
  background-color: #0f0f13;
  color: #ffffff;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.home-container-light {
  background-color: #dadae3;
  color: #000000;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
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
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #1e90ff, transparent 70%);
  top: -300px;
  right: -300px;
}

.gradient-circle.green {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #00c9a7, transparent 70%);
  bottom: -250px;
  left: -250px;
}

.gradient-circle.purple {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #8a2be2, transparent 70%);
  top: 50%;
  left: 30%;
}

.main-content-wrapper {
  height: calc(100vh - 160px);
  overflow-y: auto;
}

.main-content {
  padding: 20px 40px;
}

.page-header {
  margin-bottom: 20px;
}

.hero-title,
.hero-title-light {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
}

.hero-title {
  color: #ffffff;
}

.hero-title-light {
  color: #000000;
}

.hero-subtitle,
.hero-subtitle-light {
  font-size: 16px;
  opacity: 0.8;
}

.hero-subtitle {
  color: #cccccc;
}

.hero-subtitle-light {
  color: #333333;
}

.control-section {
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

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.goals-section {
  margin-bottom: 20px;
}

.footer {
  text-align: center;
  padding: 20px;
  font-size: 14px;
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer p {
  margin: 0;
  color: #aaaaaa;
}

/* 滚动条样式 - Webkit内核浏览器 */
.main-content-wrapper::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(138, 43, 226, 0.5);
  border-radius: 4px;
}

.main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 43, 226, 0.7);
}

/* 浅色模式滚动条 */
.home-container-light .main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
}

.home-container-light .main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(138, 43, 226, 0.3);
}

.home-container-light .main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 43, 226, 0.5);
}

/* Element UI 表格主题适配 */
.el-table-dark {
  --el-table-border-color: rgba(255, 255, 255, 0.05);
  --el-table-bg-color: rgba(30, 30, 40, 0.6);
  --el-table-tr-bg-color: rgba(30, 30, 40, 0.6);
  --el-table-header-bg-color: rgba(40, 40, 50, 0.8);
  --el-table-text-color: #ffffff;
  --el-table-header-text-color: #ffffff;
  --el-table-row-hover-bg-color: rgba(50, 50, 60, 0.8);
  --el-table-current-row-bg-color: rgba(40, 40, 50, 0.8);
}

.el-table-dark.el-table {
  background-color: rgba(30, 30, 40, 0.6);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: #ffffff;
}

.el-table-dark .el-table__header-wrapper {
  background-color: rgba(40, 40, 50, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.el-table-dark .el-table__body-wrapper {
  background-color: rgba(30, 30, 40, 0.6);
}

.el-table-dark .el-table__cell {
  background-color: transparent;
  color: #ffffff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.el-table-dark .el-table__row.hover-row td {
  background-color: rgba(50, 50, 60, 0.8);
}

.el-table-dark .el-table__row.current-row td {
  background-color: rgba(40, 40, 50, 0.8);
}

.el-table-dark .el-table__inner-wrapper::before {
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-table-dark .el-table__expanded-cell) {
  background-color: rgba(30, 30, 40, 0.6) !important;
}

.el-table-dark .el-table__inner-wrapper {
  background-color: transparent;
}

.el-table-light {
  --el-table-border-color: rgba(0, 0, 0, 0.1);
  --el-table-bg-color: rgba(255, 255, 255, 0.8);
  --el-table-tr-bg-color: rgba(255, 255, 255, 0.8);
  --el-table-header-bg-color: rgba(245, 245, 245, 0.9);
  --el-table-text-color: #000000;
  --el-table-header-text-color: #000000;
  --el-table-row-hover-bg-color: rgba(235, 235, 235, 0.9);
  --el-table-current-row-bg-color: rgba(245, 245, 245, 0.9);
}

.el-table-light.el-table {
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.1);
  color: #000000;
}

.el-table-light .el-table__header-wrapper {
  background-color: rgba(245, 245, 245, 0.9);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.el-table-light .el-table__body-wrapper {
  background-color: rgba(255, 255, 255, 0.8);
}

.el-table-light .el-table__cell {
  background-color: transparent;
  color: #000000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.el-table-light .el-table__row.hover-row td {
  background-color: rgba(235, 235, 235, 0.9);
}

:deep(.el-table-light .el-table__expanded-cell) {
  background-color: rgba(235, 235, 235, 0.9) !important;
}

.el-table-light .el-table__row.current-row td {
  background-color: rgba(245, 245, 245, 0.9);
}

.el-table-light .el-table__inner-wrapper::before {
  background-color: rgba(0, 0, 0, 0.1);
}

.el-table-light .el-table__inner-wrapper {
  background-color: transparent;
}

.expanded-content {
  padding: 20px;
  background-color: rgba(0, 0, 0, 0.05);
}

.home-container-light .expanded-content {
  background-color: rgba(0, 0, 0, 0.05);
}

.expanded-content p {
  margin: 5px 0;
}

/* .tag-dark {
  color: white;
}

.tag-light {
  color: black;
} */

/* 子目标展开区域样式 */
.expanded-content-wrapper {
  padding: 1px 1px;
  background: rgb(30, 30, 40);
  border-radius: 8px;
  margin: 12px;
  backdrop-filter: blur(10px);
}

.home-container-light .expanded-content-wrapper {
  background: linear-gradient(rgba(138, 43, 226, 0.03), rgba(138, 43, 226, 0.03));
  background-color: rgba(255, 255, 255, 0.214);
}

.child-goal-collapse-item {
  margin-bottom: 8px;
  border-radius: 8px;
  overflow: hidden;
}

.child-goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.child-goal-title {
  font-weight: 500;
  font-size: 14px;
  flex: 1;
  margin-right: 12px;
}

.child-goal-status {
  flex-shrink: 0;
}

.child-goal-details {
  padding: 16px 0;
}

.child-goal-info-row {
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.info-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.no-description {
  color: #999;
  font-style: italic;
}

.child-goal-actions {
  display: flex;
  justify-content: flex-end;
}

.summary-collapse-item {
  margin-top: 16px;
  border-radius: 8px;
  overflow: hidden;
  border: none;
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.summary-title {
  font-size: 14px;
}

.summary-content {
  padding: 16px 0;
}

.summary-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
}

.n-collapse-item :deep(.n-collapse-item__header) {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  padding: 12px 16px;
}

.home-container-light .n-collapse-item :deep(.n-collapse-item__header) {
  background-color: rgba(0, 0, 0, 0.03);
}

.n-collapse-item :deep(.n-collapse-item__content-wrapper) {
  background-color: transparent;
}

.n-collapse-item :deep(.n-collapse-item__content-inner) {
  padding: 0 16px 16px 16px;
}
</style>
