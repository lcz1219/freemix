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

                <n-select 
                  v-model:value="statusFilter" 
                  :options="statusOptions" 
                  clearable 
                  placeholder="状态筛选"
                  style="width: 120px;"
                />
              </n-space>
            </n-space>
          </n-card>
        </section>

        <!-- 目标列表 -->
        <section class="goals-section">
          <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
            <div class="card-header">
              <n-icon size="28" color="#8a2be2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                  <path d="M21,11.5c0,1.7-1.3,3-3,3s-3-1.3-3-3c0-0.5,0.1-0.9,0.3-1.3l-2-1.2C13.1,10.4,13,10.7,13,11c0,1.7,1.3,3,3,3 s3-1.3,3-3c0-0.7-0.3-1.4-0.7-2H21V11.5z"/>
                  <path d="M9,14C7.3,14,6,15.3,6,17s1.3,3,3,3s3-1.3,3-3s-1.3-3-3-3z M9,18c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S9.6,18,9,18 z"/>
                  <path d="M6,7C4.3,7,3,8.3,3,10s1.3,3,3,3s3-1.3,3-3S7.7,7,6,7z M6,11c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S6.6,11,6,11z"/>
                  <path d="M18,4c-1.7,0-3,1.3-3,3s1.3,3,3,3s3-1.3,3-3S19.7,4,18,4z M18,8c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S18.6,8,18,8z"/>
                </svg>
              </n-icon>
              <h2 class="card-title">我的目标</h2>
            </div>

            <n-data-table
              :columns="columns"
              :data="filteredGoals"
              :loading="loading"
              :pagination="pagination"
              :bordered="false"
              :single-line="false"
              striped
              @update:sorter="handleSorterChange"
            />
          </n-card>
        </section>
      </div>
    </n-layout-content>

    <!-- 目标详情模态框 -->
    <GoalDetail 
      v-model:show="showDetailModal" 
      :goal="selectedGoal" 
      @save="saveGoal" 
      @updateGoal="refreshGoals" 
    />

    <!-- 底部 -->
    <n-layout-footer class="footer" bordered>
      <p>© 2023 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
    </n-layout-footer>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject, h } from 'vue';
import { 
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NCard,
  NButton,
  NIcon,
  NDataTable,
  NSpace,
  NInput,
  NSelect,
  useMessage
} from 'naive-ui';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue';
import GoalDetail from '@/components/GoalDetail.vue';
import { getMPaths, isSuccess } from '@/utils/request';
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

// 表格列定义
const columns: DataTableColumns<any> = [
  {
    title: '目标名称',
    key: 'title',
    sorter: 'default',
    render(row) {
      return h('span', { style: { fontWeight: '500' } }, row.title);
    }
  },
  {
    title: '负责人',
    key: 'owner',
    sorter: 'default'
  },
  {
    title: '截止日期',
    key: 'deadlineString',
    sorter: 'default',
    render(row) {
      return h('div', {}, formatDate(row.deadline));
    }
  },
  {
    title: '进度',
    key: 'progress',
    sorter: 'default',
    render(row) {
      return h('div', {}, [
        h('n-progress', {
          type: 'line',
          percentage: row.progress,
          indicatorPlacement: 'inside',
          processing: row.status === 'in-progress',
          color: row.status === 'completed' ? '#00c9a7' : row.status === 'expired' ? '#ff6b6b' : '#8a2be2'
        })
      ]);
    }
  },
  {
    title: '状态',
    key: 'status',
    sorter: 'default',
    render(row) {
      const statusMap = {
        'in-progress': { label: '进行中', type: 'info' },
        'completed': { label: '已完成', type: 'success' },
        'expired': { label: '已过期', type: 'error' }
      };
      
      const statusInfo = statusMap[row.status as keyof typeof statusMap] || { label: '未知', type: 'default' };
      
      return h('div', {}, [
        h('n-tag', {
          type: statusInfo.type,
          size: 'small'
        }, {
          default: () => statusInfo.label
        })
      ]);
    }
  },
  {
    title: '操作',
    key: 'actions',
    render(row) {
      return h('div', {}, [
        h(NSpace, {}, {
          default: () => [
            h(NButton, {
              size: 'small',
              type: 'primary',
              ghost: true,
              onClick: () => viewGoalDetail(row)
            }, { default: () => '查看' }),
            h(NButton, {
              size: 'small',
              type: 'info',
              ghost: true,
              onClick: () => editGoal(row)
            }, { default: () => '编辑' })
          ]
        })
      ]);
    }
  }
];

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

.hero-title, .hero-title-light {
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

.hero-subtitle, .hero-subtitle-light {
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

.feature-card, .feature-card-light {
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
</style>