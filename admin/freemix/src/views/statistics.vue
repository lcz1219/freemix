<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'home-container' : 'home-container-light'">
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>

    <!-- 顶部导航栏 -->
    <NavBar active-tab="statistics" />

    <!-- 主内容区域 -->
    <n-layout-content class="main-content-wrapper">
      <div class="main-content">
        <!-- 页面标题 -->
        <section class="page-header">
          <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">统计数据</h1>
          <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
            查看目标完成情况的统计分析，帮助您更好地了解自己的目标完成趋势
          </p>
        </section>

        <!-- 统计概览 -->
        <StatsOverview 
          :total-goals="totalGoals"
          :completed-goals="completedGoals"
          :in-progress-goals="inProgressGoals"
          :expired-goals="expiredGoals"
        />

        <!-- 图表区域 -->
        <section class="charts-section">
          <n-grid :cols="2" :x-gap="16" :y-gap="16" responsive="screen">
            <!-- 目标状态分布饼图 -->
            <n-grid-item>
              <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
                <div class="card-header">
                  <n-icon size="28" color="#8a2be2">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                      <path d="M12,2C6.5,2,2,6.5,2,12s4.5,10,10,10s10-4.5,10-10S17.5,2,12,2z M12,20c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8 S16.4,20,12,20z"/>
                      <path d="M12,8c-2.2,0-4,1.8-4,4s1.8,4,4,4s4-1.8,4-4S14.2,8,12,8z M12,14c-1.1,0-2-0.9-2-2s0.9-2,2-2s2,0.9,2,2 S13.1,14,12,14z"/>
                    </svg>
                  </n-icon>
                  <h2 class="card-title">目标状态分布</h2>
                </div>
                <div ref="statusChart" class="chart-container"></div>
              </n-card>
            </n-grid-item>
            
            <!-- 月度目标完成趋势 -->
            <n-grid-item>
              <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
                <div class="card-header">
                  <n-icon size="28" color="#4682b4">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                      <path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M19,19H5V5h14V19z"/>
                      <path d="M16,10H8c-0.6,0-1,0.4-1,1s0.4,1,1,1h8c0.6,0,1-0.4,1-1S16.6,10,16,10z"/>
                      <path d="M16,7H8C7.4,7,7,7.4,7,8s0.4,1,1,1h8c0.6,0,1-0.4,1-1S16.6,7,16,7z"/>
                      <path d="M12,13c-0.6,0-1,0.4-1,1s0.4,1,1,1s1-0.4,1-1S12.6,13,12,13z"/>
                    </svg>
                  </n-icon>
                  <h2 class="card-title">月度目标完成趋势</h2>
                </div>
                <div ref="monthlyChart" class="chart-container"></div>
              </n-card>
            </n-grid-item>
            
            <!-- 目标类型分布 -->
            <n-grid-item span="2">
              <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
                <div class="card-header">
                  <n-icon size="28" color="#00c9a7">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                      <path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M19,19H5V5h14V19z"/>
                      <rect x="7" y="7" width="10" height="2"/>
                      <rect x="7" y="11" width="10" height="2"/>
                      <rect x="7" y="15" width="6" height="2"/>
                    </svg>
                  </n-icon>
                  <h2 class="card-title">目标类型分布</h2>
                </div>
                <div ref="typeChart" class="chart-container"></div>
              </n-card>
            </n-grid-item>
          </n-grid>
        </section>
        
        <!-- 详细统计数据 -->
        <section class="details-section">
          <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
            <div class="card-header">
              <n-icon size="28" color="#ff6b6b">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                  <path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M19,19H5V5h14V19z"/>
                  <path d="M16,10H8c-0.6,0-1,0.4-1,1s0.4,1,1,1h8c0.6,0,1-0.4,1-1S16.6,10,16,10z"/>
                  <path d="M16,7H8C7.4,7,7,7.4,7,8s0.4,1,1,1h8c0.6,0,1-0.4,1-1S16.6,7,16,7z"/>
                  <path d="M12,13c-0.6,0-1,0.4-1,1s0.4,1,1,1s1-0.4,1-1S12.6,13,12,13z"/>
                </svg>
              </n-icon>
              <h2 class="card-title">详细统计数据</h2>
            </div>
            
            <n-data-table
              :columns="columns"
              :data="goals"
              :pagination="pagination"
              :bordered="false"
              :single-line="false"
              striped
            />
          </n-card>
        </section>
      </div>
    </n-layout-content>

    <!-- 底部 -->
    <n-layout-footer class="footer" bordered>
      <p>© 2025 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
    </n-layout-footer>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, inject, computed, watch, h } from 'vue';
import { 
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NCard,
  NIcon,
  NGrid,
  NGridItem,
  NTag,
  NProgress,
  NDataTable
} from 'naive-ui';
import * as echarts from 'echarts';
import NavBar from '@/components/NavBar.vue';
import StatsOverview from '@/components/StatsOverview.vue';
import { getMPaths, isSuccess } from '@/utils/request';
import { useStore } from 'vuex';
import type { DataTableColumns } from 'naive-ui';

// 注入主题变量
const isDark = inject('isDark', ref(false));

// 状态管理
const store = useStore();

// 图表引用
const statusChart = ref<HTMLDivElement | null>(null);
const monthlyChart = ref<HTMLDivElement | null>(null);
const typeChart = ref<HTMLDivElement | null>(null);

// 数据状态
const goals = ref<any[]>([]);
const loading = ref(false);

// 分页配置
const pagination = {
  pageSize: 10
};

// 表格列定义
const columns: DataTableColumns<any> = [
  {
    title: '目标名称',
    key: 'title',
    sorter: 'default'
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
        h(NProgress, {
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
        h(NTag, {
          type: statusInfo.type,
          size: 'small'
        }, {
          default: () => statusInfo.label
        })
      ]);
    }
  }
];

// 计算统计数据
const totalGoals = computed(() => goals.value.length);
const completedGoals = computed(() => goals.value.filter(g => g.status === 'completed').length);
const inProgressGoals = computed(() => goals.value.filter(g => g.status === 'in-progress').length);
const expiredGoals = computed(() => goals.value.filter(g => g.status === 'expired').length);

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
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 初始化状态分布图表
const initStatusChart = () => {
  if (!statusChart.value) return;
  
  const chart = echarts.init(statusChart.value);
  
  const option = {
    title: {
      text: '目标状态分布',
      left: 'center',
      textStyle: {
        color: isDark.value ? '#ffffff' : '#000000'
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'bottom',
      textStyle: {
        color: isDark.value ? '#cccccc' : '#333333'
      }
    },
    series: [
      {
        name: '目标状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: isDark.value ? '#0f0f13' : '#dadae3',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: completedGoals.value, name: '已完成', itemStyle: { color: '#00c9a7' } },
          { value: inProgressGoals.value, name: '进行中', itemStyle: { color: '#8a2be2' } },
          { value: expiredGoals.value, name: '已过期', itemStyle: { color: '#ff6b6b' } }
        ]
      }
    ]
  };
  
  chart.setOption(option);
  
  // 监听主题变化
  watch(isDark, () => {
    chart.dispose();
    initStatusChart();
  });
};
const filterDeadline = (deadline: string | number | Date) => {
  if (!deadline) return '未设置';
  
 const date = new Date(deadline);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  return `${year}-${month}`;
}
// 初始化月度趋势图表
const initMonthlyChart = () => {
  if (!monthlyChart.value) return;
  
  const chart = echarts.init(monthlyChart.value);
  
  // 生成过去12个月的数据
  const months = [];
  const completedData = [];
  const inProgressData = [];
  const expiredData = [];
  
  const now = new Date();
  for (let i = 11; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
    let data=`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
    months.push(data);
    // console.log("goals",goals.value);
    // console.log("months",months);

    
    // 模拟数据
    completedData.push(goals.value.filter(g => g.status === 'completed'&&filterDeadline(g.deadline)==data).length);
    inProgressData.push(goals.value.filter(g => g.status === 'in-progress'&&filterDeadline(g.deadline)==data).length);
    expiredData.push(goals.value.filter(g => g.status === 'expired'&&filterDeadline(g.deadline)==data).length);
  }
  
  const option = {
    title: {
      text: '月度目标完成趋势',
      left: 'center',
      textStyle: {
        color: isDark.value ? '#ffffff' : '#000000'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      top: 'bottom',
      textStyle: {
        color: isDark.value ? '#cccccc' : '#333333'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months,
      axisLabel: {
        color: isDark.value ? '#aaaaaa' : '#666666'
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: isDark.value ? '#aaaaaa' : '#666666'
      }
    },
    series: [
      {
        name: '已完成',
        type: 'line',
        stack: '总量',
        smooth: true,
        lineStyle: {
          width: 0
        },
        showSymbol: false,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: '#00c9a7'
            },
            {
              offset: 1,
              color: 'rgba(0, 201, 167, 0.1)'
            }
          ])
        },
        emphasis: {
          focus: 'series'
        },
        data: completedData
      },
      {
        name: '进行中',
        type: 'line',
        stack: '总量',
        smooth: true,
        lineStyle: {
          width: 0
        },
        showSymbol: false,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: '#8a2be2'
            },
            {
              offset: 1,
              color: 'rgba(138, 43, 226, 0.1)'
            }
          ])
        },
        emphasis: {
          focus: 'series'
        },
        data: inProgressData
      },
      {
        name: '已过期',
        type: 'line',
        stack: '总量',
        smooth: true,
        lineStyle: {
          width: 0
        },
        showSymbol: false,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: '#ff6b6b'
            },
            {
              offset: 1,
              color: 'rgba(255, 107, 107, 0.1)'
            }
          ])
        },
        emphasis: {
          focus: 'series'
        },
        data: expiredData
      }
    ]
  };
  
  chart.setOption(option);
  
  // 监听主题变化
  watch(isDark, () => {
    chart.dispose();
    initMonthlyChart();
  });
};

// 初始化类型分布图表
const initTypeChart = () => {
  if (!typeChart.value) return;
  
  const chart = echarts.init(typeChart.value);
  
  // 统计标签分布
  const tagCount: Record<string, number> = {};
  goals.value.forEach(goal => {
    if (goal.tags && Array.isArray(goal.tags)) {
      goal.tags.forEach((tag: string) => {
        tagCount[tag] = (tagCount[tag] || 0) + 1;
      });
    }
  });
  
  const tagData = Object.entries(tagCount).map(([name, value]) => ({ name, value }));
  
  const option = {
    title: {
      text: '目标类型分布',
      left: 'center',
      textStyle: {
        color: isDark.value ? '#ffffff' : '#000000'
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'bottom',
      textStyle: {
        color: isDark.value ? '#cccccc' : '#333333'
      }
    },
    series: [
      {
        name: '目标类型',
        type: 'pie',
        radius: '50%',
        data: tagData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  
  chart.setOption(option);
  
  // 监听主题变化
  watch(isDark, () => {
    chart.dispose();
    initTypeChart();
  });
};

// 初始化所有图表
const initCharts = () => {
  initStatusChart();
  initMonthlyChart();
  initTypeChart();
};

// 初始化
onMounted(async () => {
  await getGoals();
  initCharts();
  
  // 监听窗口大小变化，重新调整图表大小
  window.addEventListener('resize', () => {
    echarts.getInstanceByDom(statusChart.value!)?.resize();
    echarts.getInstanceByDom(monthlyChart.value!)?.resize();
    echarts.getInstanceByDom(typeChart.value!)?.resize();
  });
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

.stats-overview {
  margin-bottom: 20px;
}

.stat-card, .stat-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  height: 100%;
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

.stat-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

.charts-section {
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

.chart-container {
  width: 100%;
  height: 300px;
}

.details-section {
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