<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'mobile-statistics dark' : 'mobile-statistics light'">
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>

    <!-- 顶部导航栏 -->
    <n-layout-header bordered class="mobile-header">
      <div class="header-content">
        <n-button quaternary circle @click="goBack">
          <n-icon size="20">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M20,11H7.8l5.6-5.6L12,4l-8,8l8,8l1.4-1.4L7.8,13H20V11z"/>
            </svg>
          </n-icon>
        </n-button>
        <h1 class="header-title">统计数据</h1>
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
        <!-- 页面标题 -->
        <section class="page-header">
          <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">统计数据</h1>
          <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
            查看您的目标完成情况和趋势分析
          </p>
        </section>

        <!-- 统计概览 -->
        <section class="stats-overview">
          <n-grid :cols="2" :x-gap="12" :y-gap="12">
            <n-grid-item>
              <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
                <div class="stat-item">
                  <div class="stat-value">{{ totalGoals }}</div>
                  <div :class="isDark ? 'stat-label' : 'stat-label-light'">总目标数</div>
                </div>
              </n-card>
            </n-grid-item>
            
            <n-grid-item>
              <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
                <div class="stat-item">
                  <div class="stat-value" style="color: #00c9a7;">{{ completedGoals }}</div>
                  <div :class="isDark ? 'stat-label' : 'stat-label-light'">已完成</div>
                </div>
              </n-card>
            </n-grid-item>
            
            <n-grid-item>
              <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
                <div class="stat-item">
                  <div class="stat-value" style="color: #81c683;">{{ inProgressGoals }}</div>
                  <div :class="isDark ? 'stat-label' : 'stat-label-light'">进行中</div>
                </div>
              </n-card>
            </n-grid-item>
            
            <n-grid-item>
              <n-card :class="isDark ? 'stat-card' : 'stat-card-light'">
                <div class="stat-item">
                  <div class="stat-value" style="color: #ff6b6b;">{{ expiredGoals }}</div>
                  <div :class="isDark ? 'stat-label' : 'stat-label-light'">已过期</div>
                </div>
              </n-card>
            </n-grid-item>
          </n-grid>
        </section>
        
        <!-- 图表区域 -->
        <section class="charts-section">
          <n-card :class="isDark ? 'chart-card' : 'chart-card-light'">
            <h2 class="section-title">目标状态分布</h2>
            <div class="chart-container">
              <div ref="pieChartRef" class="chart"></div>
            </div>
          </n-card>
          
          <n-card :class="isDark ? 'chart-card' : 'chart-card-light'">
            <h2 class="section-title">月度完成趋势</h2>
            <div class="chart-container">
              <div ref="lineChartRef" class="chart"></div>
            </div>
          </n-card>
          
          <n-card :class="isDark ? 'chart-card' : 'chart-card-light'">
            <h2 class="section-title">目标分类统计</h2>
            <div class="chart-container">
              <div ref="barChartRef" class="chart"></div>
            </div>
          </n-card>
        </section>
        
        <!-- 详细数据 -->
        <section class="details-section">
          <n-card :class="isDark ? 'details-card' : 'details-card-light'">
            <h2 class="section-title">详细数据</h2>
            <n-data-table 
              :columns="columns" 
              :data="tableData" 
              :pagination="pagination"
              :bordered="false"
              :single-line="false"
            />
          </n-card>
        </section>
      </div>
    </n-layout-content>
  </n-layout>
</template>

<script setup>
import { ref, inject, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import * as echarts from 'echarts';
import { 
  NLayout, 
  NLayoutHeader, 
  NLayoutContent, 
  NButton, 
  NIcon, 
  NGrid, 
  NGridItem, 
  NCard, 
  NDataTable,
  useMessage
} from 'naive-ui';
import { getMPaths, isSuccess } from '@/utils/request';

const router = useRouter();
const store = useStore();
const isDark = inject('isDark', ref(false));
const message = useMessage();

// 统计数据
const totalGoals = ref(0);
const completedGoals = ref(0);
const inProgressGoals = ref(0);
const expiredGoals = ref(0);

// 表格列定义
const columns = [
  {
    title: '目标名称',
    key: 'name'
  },
  {
    title: '状态',
    key: 'status'
  },
  {
    title: '进度',
    key: 'progress'
  },
  {
    title: '截止日期',
    key: 'deadline'
  }
];

// 表格数据
const tableData = ref([]);

// 分页配置
const pagination = {
  pageSize: 5
};

// 图表引用
const pieChartRef = ref(null);
const lineChartRef = ref(null);
const barChartRef = ref(null);
let pieChart = null;
let lineChart = null;
let barChart = null;

// 方法
const goBack = () => {
  router.go(-1);
};

const toggleTheme = () => {
  isDark.value = !isDark.value;
  localStorage.setItem('theme-dark', JSON.stringify(isDark.value));
  // 重新渲染图表以适应主题变化
  setTimeout(() => {
    initCharts();
  }, 100);
};

// 获取统计数据
const getStatisticsData = async () => {
  try {
    const res = await getMPaths("getGoals", store.state.user.username, "正在获取统计数据...");
    if (isSuccess(res)) {
      const goals = res.data.data || [];
      
      // 更新统计数据
      totalGoals.value = goals.length;
      completedGoals.value = goals.filter(goal => goal.progress === 100).length;
      expiredGoals.value = goals.filter(goal => Date.now() - Date.parse(goal.deadline) > 0 && goal.progress < 100).length;
      inProgressGoals.value = goals.filter(goal => goal.progress !== 100 && Date.now() - Date.parse(goal.deadline) < 0).length;
      
      // 更新表格数据
      tableData.value = goals.map(goal => ({
        key: goal.id,
        name: goal.title,
        status: getStatusLabel(goal.status),
        progress: `${goal.progress}%`,
        deadline: formatDate(goal.deadline)
      }));
      
      // 重新初始化图表
      setTimeout(() => {
        initCharts();
      }, 100);
    } else {
      message.error('获取统计数据失败');
    }
  } catch (error) {
    message.error('获取统计数据时发生错误');
    console.error(error);
  }
};

// 获取状态标签文本
const getStatusLabel = (status) => {
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

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未设置';
  
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 初始化图表
const initCharts = () => {
  // 销毁已存在的图表实例
  if (pieChart) pieChart.dispose();
  if (lineChart) lineChart.dispose();
  if (barChart) barChart.dispose();
  
  // 饼图 - 目标状态分布
  pieChart = echarts.init(pieChartRef.value, isDark.value ? 'dark' : null);
  pieChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: '0',
      left: 'center',
      textStyle: {
        color: isDark.value ? '#ffffff' : '#000000'
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
          borderColor: isDark.value ? '#1e1e2e' : '#ffffff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: completedGoals.value, name: '已完成', itemStyle: { color: '#00c9a7' } },
          { value: inProgressGoals.value, name: '进行中', itemStyle: { color: '#81c683' } },
          { value: expiredGoals.value, name: '已过期', itemStyle: { color: '#ff6b6b' } }
        ]
      }
    ]
  });
  
  // 折线图 - 月度完成趋势（模拟数据）
  lineChart = echarts.init(lineChartRef.value, isDark.value ? 'dark' : null);
  lineChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLine: {
        lineStyle: {
          color: isDark.value ? '#ffffff' : '#000000'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: isDark.value ? '#ffffff' : '#000000'
        }
      }
    },
    series: [
      {
        name: '完成目标数',
        type: 'line',
        smooth: true,
        data: [2, 3, 1, 4, 2, 3, 5, 4, 6, 5, 7, 6],
        itemStyle: {
          color: '#81c683'
        },
        areaStyle: {
          color: '#81c683',
          opacity: 0.3
        }
      }
    ]
  });
  
  // 柱状图 - 目标分类统计（模拟数据）
  barChart = echarts.init(barChartRef.value, isDark.value ? 'dark' : null);
  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: ['工作', '学习', '生活', '运动', '兴趣'],
      axisLine: {
        lineStyle: {
          color: isDark.value ? '#ffffff' : '#000000'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: isDark.value ? '#ffffff' : '#000000'
        }
      }
    },
    series: [
      {
        name: '目标数',
        type: 'bar',
        barWidth: '60%',
        data: [8, 6, 4, 3, 3],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#81c683' },
            { offset: 1, color: '#00c9a7' }
          ])
        }
      }
    ]
  });
};

// 组件挂载时初始化数据和图表
onMounted(() => {
  getStatisticsData();
  
  // 监听窗口大小变化，重新调整图表大小
  window.addEventListener('resize', () => {
    if (pieChart) pieChart.resize();
    if (lineChart) lineChart.resize();
    if (barChart) barChart.resize();
  });
});

// 组件卸载前销毁图表实例
onBeforeUnmount(() => {
  if (pieChart) pieChart.dispose();
  if (lineChart) lineChart.dispose();
  if (barChart) barChart.dispose();
});
</script>

<style scoped>
.mobile-statistics {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
}

.mobile-statistics.dark {
  background-color: #0f0f13;
  color: #ffffff;
}

.mobile-statistics.light {
  background-color: #dadae3;
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

.page-header {
  text-align: center;
  margin-bottom: 20px;
}

.hero-title,
.hero-title-light {
  font-size: 24px;
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
  font-size: 14px;
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

.stat-card,
.stat-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
  font-size: 24px;
  font-weight: bold;
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

.charts-section {
  margin-bottom: 20px;
}

.chart-card,
.chart-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 16px;
}

.chart-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chart-card-light {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.dark .section-title {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.light .section-title {
  color: #000000;
}

.chart-container {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart {
  width: 100%;
  height: 100%;
}

.details-section {
  margin-bottom: 20px;
}

.details-card,
.details-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.details-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.details-card-light {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
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
.mobile-statistics.light .main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
}

.mobile-statistics.light .main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(129, 198, 131, 0.3);
}

.mobile-statistics.light .main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(129, 198, 131, 0.5);
}
</style>