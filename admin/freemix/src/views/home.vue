<template>
  <n-layout :native-scrollbar="true" :class="isDark?'home-container':'home-container-light'">
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>
    
    <!-- 顶部导航栏 -->
    <n-layout-header class="header" bordered>
      <div class="logo">
        <div class="logo-icon">
          <n-icon size="24" color="white">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M18.73,7.05l0,0c-0.25-0.63-0.86-1.04-1.55-1.04c-0.05,0-0.11,0-0.16,0.01c-0.25-0.64-0.87-1.07-1.61-1.07 c-0.1,0-0.19,0.01-0.29,0.03c-0.28-0.64-0.93-1.06-1.7-1.06c-0.85,0-1.58,0.53-1.87,1.3c-0.26-0.05-0.52-0.07-0.78-0.07 c-2.34,0-4.15,2.01-3.9,4.33c-1.56,0.51-2.72,1.95-2.72,3.66c0,1.14,0.51,2.16,1.32,2.83C4.13,15.38,4,15.68,4,16 c0,1.66,1.34,3,3,3h10c2.76,0,5-2.24,5-5c0-2.64-2.05-4.78-4.66-4.96C18.82,9.3,19.11,8.12,18.73,7.05z"/>
            </svg>
          </n-icon>
        </div>
        <span class="logo-text">目标追踪者</span>
      </div>
      
      <nav class="nav">
        <n-button text type="primary" class="nav-link active">仪表盘</n-button>
        <n-button text type="primary" class="nav-link">目标管理</n-button>
        <n-button text type="primary" class="nav-link">统计数据</n-button>
        <n-button text type="primary" class="nav-link">设置</n-button>
      </nav>
      
      <div class="header-actions">
        <!-- <n-button text>
          <n-icon size="24">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M12.7,16.3c-0.2-0.2-0.2-0.5,0-0.7l2.8-2.8H4.5c-0.3,0-0.5-0.2-0.5-0.5s0.2-0.5,0.5-0.5H15l-2.8-2.8 c-0.2-0.2-0.2-0.5,0-0.7s0.5-0.2,0.7,0l3.5,3.5c0.1,0.1,0.1,0.2,0.1,0.4c0,0.1,0,0.3-0.1,0.4l-3.5,3.5 C13.2,16.4,12.9,16.4,12.7,16.3z"/>
            </svg>
          </n-icon>
        </n-button> -->
        <n-avatar round size="medium" src="https://api.dicebear.com/7.x/miniavs/svg?seed=3"></n-avatar>
      </div>
    </n-layout-header>
    
    <!-- 主内容区域 -->
    <n-layout-content class="main-content-wrapper">
      <div class="main-content">
        <!-- 英雄区域 -->
        <section class="hero-section">
          <h1 :class="isDark?'hero-title':'hero-title-light'">掌控你的目标，衡量你的成功</h1>
          <p :class="isDark?'hero-subtitle':'hero-subtitle-light'">目标追踪者是一款强大的目标管理系统，帮助您设定、跟踪并完成个人和职业目标。可视化您的进度，庆祝每一个里程碑。</p>
          
          <div class="hero-actions">
            <n-button type="primary" size="large" round strong @click="addGoal">
              <template #icon>
                <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M20,12H4v-1c0-0.6,0.4-1,1-1h14c0.6,0,1,0.4,1,1V12z"></path><path d="M4,12h16v1c0,0.6-0.4,1-1,1H5c-0.6,0-1-0.4-1-1V12z"></path><path d="M20,10H4c-0.6,0-1,0.4-1,1v2c0,0.6,0.4,1,1,1h16c0.6,0,1-0.4,1-1v-2C21,10.4,20.6,10,20,10z M20,12H4v-1h16V12z"></path></svg></n-icon>
              </template>
              添加新目标
            </n-button>
            <n-button size="large" round>
              <template #icon>
                <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M13.5,4.5c0,0.8-0.7,1.5-1.5,1.5s-1.5-0.7-1.5-1.5S11.2,3,12,3S13.5,3.7,13.5,4.5z M13.5,12c0,0.8-0.7,1.5-1.5,1.5 s-1.5-0.7-1.5-1.5S11.2,10.5,12,10.5S13.5,11.2,13.5,12z M13.5,19.5c0,0.8-0.7,1.5-1.5,1.5s-1.5-0.7-1.5-1.5s0.7-1.5,1.5-1.5 S13.5,18.7,13.5,19.5z"></path></svg></n-icon>
              </template>
              使用指南
            </n-button>
          </div>
        </section>
        
        <!-- 功能卡片区域 -->
        <section class="features-section">
          <n-card class="feature-card">
            <div class="card-header">
              <div ref="progressChart" class="echart-icon"></div>
              <h2 class="card-title">目标概览</h2>
            </div>
            
            <div class="stats-container">
              <div class="stat-item">
                <div class="stat-value">12</div>
                <div class="stat-label">进行中</div>
              </div>
              <div class="stat-item">
                <div class="stat-value" style="color: #00c9a7;">7</div>
                <div class="stat-label">已完成</div>
              </div>
              <div class="stat-item">
                <div class="stat-value" style="color: #ff6b6b;">2</div>
                <div class="stat-label">已过期</div>
              </div>
            </div>
            
            <div class="chart-container">
              <canvas ref="progressDetailChart"></canvas>
            </div>
          </n-card>
          
          <n-card class="feature-card">
            <div class="card-header">
              <div ref="timelineChart" class="echart-icon"></div>
              <h2 class="card-title">近期目标</h2>
            </div>
            
            <n-timeline>
              <n-timeline-item type="success" title="完成项目提案" time="2小时前" />
              <n-timeline-item type="warning" title="设计评审会议" time="明天 10:00" />
              <n-timeline-item type="info" title="团队周会" time="明天 14:00" />
              <n-timeline-item type="info" title="客户汇报" time="后天 11:00" />
            </n-timeline>
          </n-card>
          
          <n-card class="feature-card">
            <div class="card-header">
              <div ref="trendChartIcon" class="echart-icon"></div>
              <h2 class="card-title">完成趋势</h2>
            </div>
            
            <div class="chart-container">
              <canvas ref="trendChart"></canvas>
            </div>
          </n-card>
        </section>
        
        <!-- 目标卡片网格 -->
        <section class="targets-section">
          <h2 class="section-title">我的目标</h2>
          <div class="target-grid">
            <n-card v-for="(goal, index) in goals" :key="index" class="target-card">
              <div class="card-header">
                <h3 class="goal-title">{{ goal.title }}</h3>
                <n-tag :type="goal.status === 'completed' ? 'success' : goal.status === 'expired' ? 'error' : 'warning'">
                  {{ goal.status === 'completed' ? '已完成' : goal.status === 'expired' ? '已过期' : '进行中' }}
                </n-tag>
              </div>
              
              <n-progress
                type="line"
                :percentage="goal.progress"
                :indicator-placement="'inside'"
                :height="8"
                :rail-color="'rgba(255, 255, 255, 0.1)'"
                :fill-color="'linear-gradient(90deg, #8a2be2, #4b0082)'"
              />
              
              <div class="goal-details">
                <div class="detail-item">
                  <n-icon size="16">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                      <path d="M12,2C6.5,2,2,6.5,2,12s4.5,10,10,10s10-4.5,10-10S17.5,2,12,2z M12,20c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8 S16.4,20,12,20z"/>
                      <path d="M13,11.6V7c0-0.6-0.4-1-1-1s-1,0.4-1,1v5.6c-0.6,0.3-1,1-1,1.7c0,1.1,0.9,2,2,2s2-0.9,2-2C14,12.6,13.6,11.9,13,11.6z"/>
                    </svg>
                  </n-icon>
                  <span>截止: {{ goal.deadline }}</span>
                </div>
                <div class="detail-item">
                  <n-icon size="16">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                      <path d="M12,12c2.2,0,4-1.8,4-4s-1.8-4-4-4s-4,1.8-4,4S9.8,12,12,12z M12,6c1.1,0,2,0.9,2,2s-0.9,2-2,2s-2-0.9-2-2 S10.9,6,12,6z"/>
                      <path d="M12,14c-4.4,0-8,3.6-8,8h2c0-3.3,2.7-6,6-6s6,2.7,6,6h2C20,17.6,16.4,14,12,14z"/>
                    </svg>
                  </n-icon>
                  <span>负责人: {{ goal.owner }}</span>
                </div>
              </div>
            </n-card>
          </div>
        </section>
         <n-layout-footer class="footer" bordered>
      <p>© 2023 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
    </n-layout-footer>
      </div>
    </n-layout-content>
    
    <!-- 页脚 -->
    <n-layout-footer class="footer" bordered>
      <p>© 2023 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
    </n-layout-footer>
  </n-layout>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue';
import { 
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NButton, 
  NIcon, 
  NSwitch, 
  NCard, 
  NTimeline, 
  NTimelineItem, 
  NTag,
  NProgress,
  NAvatar
} from 'naive-ui';
import Chart from 'chart.js/auto';
import * as echarts from 'echarts/core';
import { PieChart, BarChart, LineChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import{useRouter} from'vue-router'

// 注册 ECharts 组件
echarts.use([
  PieChart,
  BarChart,
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
  CanvasRenderer
]);
const router = useRouter()
// 图标组件
const SunIcon = {
  template: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
    <path d="M12,7c-2.8,0-5,2.2-5,5s2.2,5,5,5s5-2.2,5-5S14.8,7,12,7z M12,14.5c-1.4,0-2.5-1.1-2.5-2.5s1.1-2.5,2.5-2.5 s2.5,1.1,2.5,2.5S13.4,14.5,12,14.5z" />
    <path d="M12,5c-0.6,0-1-0.4-1-1V1c0-0.6,0.4-1,1-1s1,0.4,1,1v3C13,4.6,12.6,5,12,5z" />
    <path d="M12,19c-0.6,0-1,0.4-1,1v3c0,0.6,0.4,1,1,1s1-0.4,1-1v-3C13,19.4,12.6,19,12,19z" />
    <path d="M23,11h-3c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S23.6,11,23,11z" />
    <path d="M4,11H1c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S4.6,11,4,11z" />
    <path d="M18.7,6.3c0.4-0.4,0.4-1,0-1.4l-2.1-2.1c-0.4-0.4-1-0.4-1.4,0s-0.4,1,0,1.4l2.1,2.1C17.7,6.7,18.3,6.7,18.7,6.3z" />
    <path d="M5.3,17.7c-0.4,0.4-0.4,1,0,1.4l2.1,2.1c0.4,0.4,1,0.4,1.4,0s0.4-1,0-1.4l-2.1-2.1C6.3,17.3,5.7,17.3,5.3,17.7z" />
    <path d="M18.7,17.7c-0.4-0.4-1-0.4-1.4,0l-2.1,2.1c-0.4,0.4-0.4,1,0,1.4s1,0.4,1.4,0l2.1-2.1C19.1,18.7,19.1,18.1,18.7,17.7z" />
    <path d="M5.3,6.3c0.4,0.4,1,0.4,1.4,0l2.1-2.1c0.4-0.4,0.4-1,0-1.4s-1-0.4-1.4,0L5.3,4.9C4.9,5.3,4.9,5.9,5.3,6.3z" />
  </svg>`
};

const MoonIcon = {
  template: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
    <path d="M20.9,15.3c-0.5-0.9-1.2-1.7-2-2.2c-0.2-0.1-0.4-0.1-0.6-0.1c-0.3,0-0.6,0.1-0.9,0.3c-0.8,0.4-1.3,1.1-1.5,1.9 c-0.2,0.8,0,1.7,0.6,2.4c0.9,1.1,2.2,1.6,3.5,1.4c0.2,0,0.5-0.1,0.7-0.2C21.6,18.4,21.8,16.8,20.9,15.3z M19.5,18.5 c-0.9,0.1-1.8-0.2-2.5-0.8c-0.3-0.3-0.5-0.7-0.6-1.1c-0.1-0.4-0.1-0.8,0.1-1.2c0.2-0.4,0.5-0.7,0.9-0.9 c0.3-0.1,0.5-0.2,0.8-0.1c0.7,0.2,1.3,0.6,1.7,1.2C20.4,16.5,20.3,17.7,19.5,18.5z" />
    <path d="M9,22c5.5,0,10-4.5,10-10c0-0.8-0.1-1.6-0.3-2.4c-0.1-0.4-0.5-0.7-0.9-0.6c-0.4,0.1-0.7,0.5-0.6,0.9 c0.2,0.7,0.3,1.4,0.3,2.1c0,4.4-3.6,8-8,8s-8-3.6-8-8c0-4.4,3.6-8,8-8c0.7,0,1.3,0.1,2,0.2c0.4,0.1,0.8-0.1,1-0.5 C13.1,2.6,13,2.2,12.6,2C11.7,1.7,10.8,1.6,9.9,1.6C4.4,1.6,0,6,0,11.5C0,17,4.5,22,9,22z" />
  </svg>`
};

// 响应式数据
const darkMode = ref(true);
const progressDetailChart = ref(null);
const trendChart = ref(null);
const progressChart = ref(null);
const timelineChart = ref(null);
const trendChartIcon = ref(null);
const isDark=inject('isDark')

// 目标数据
const goals = ref([
  { title: '职业发展', progress: 65, status: 'in-progress', deadline: '2023-12-31', owner: '张三' },
  { title: '技能提升计划', progress: 80, status: 'in-progress', deadline: '2023-11-15', owner: '李四' },
  { title: '健身目标', progress: 45, status: 'in-progress', deadline: '2024-02-28', owner: '王五' },
  { title: '阅读计划', progress: 75, status: 'completed', deadline: '2023-12-20', owner: '赵六' },
  { title: '学习新语言', progress: 30, status: 'in-progress', deadline: '2024-03-15', owner: '钱七' },
  { title: '项目交付', progress: 90, status: 'in-progress', deadline: '2023-10-30', owner: '孙八' }
]);

// 开关轨道样式
const railStyle = ({ focused, checked }) => {
  const style = {};
  if (checked) {
    style.background = '#8a2be2';
    if (focused) style.boxShadow = '0 0 0 2px #d0305040';
  } else {
    style.background = '#2080f0';
    if (focused) style.boxShadow = '0 0 0 2px #2080f040';
  }
  return style;
};

// 添加新目标
const addGoal = () => {
  router.push('/add-goal');
  console.log('添加新目标');
};

// 初始化图表
onMounted(() => {
  // 进度图表 (ECharts)
  const progressChartInstance = echarts.init(progressChart.value);
  progressChartInstance.setOption({
    series: [{
      type: 'pie',
      radius: ['70%', '90%'],
      data: [
        {value: 7, name: '已完成'},
        {value: 12, name: '进行中'},
        {value: 8, name: '未开始'}
      ],
      label: { show: false },
      emphasis: { scale: false },
      animationType: 'scale',
      animationEasing: 'elasticOut'
    }],
    color: ['#00c9a7', '#8a2be2', '#3a3a4a']
  });

  // 时间线图表 (ECharts)
  const timelineChartInstance = echarts.init(timelineChart.value);
  timelineChartInstance.setOption({
    series: [{
      type: 'bar',
      data: [10, 15, 12, 8],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
          offset: 0,
          color: '#8a2be2'
        }, {
          offset: 1,
          color: '#4b0082'
        }])
      },
      barWidth: 20
    }],
    xAxis: {
      type: 'category',
      data: ['', '', '', ''],
      axisTick: { show: false },
      axisLabel: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false }
    },
    grid: {
      left: 0,
      right: 0,
      top: 0,
      bottom: 0
    }
  });

  // 趋势图表图标 (ECharts)
  const trendChartIconInstance = echarts.init(trendChartIcon.value);
  trendChartIconInstance.setOption({
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: { show: false },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false }
    },
    series: [{
      data: [820, 932, 901, 934, 1290, 1330, 1320],
      type: 'line',
      smooth: true,
      lineStyle: {
        color: '#00c9a7',
        width: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
          offset: 0,
          color: 'rgba(0, 201, 167, 0.3)'
        }, {
          offset: 1,
          color: 'rgba(0, 201, 167, 0)'
        }])
      },
      symbol: 'none'
    }],
    grid: {
      left: 0,
      right: 0,
      top: 0,
      bottom: 0
    },
    tooltip: { show: false }
  });

  // 进度详情图表
  new Chart(progressDetailChart.value, {
    type: 'doughnut',
    data: {
      labels: ['已完成', '进行中', '未开始'],
      datasets: [{
        data: [7, 12, 8],
        backgroundColor: ['#00c9a7', '#8a2be2', '#3a3a4a'],
        borderWidth: 0
      }]
    },
    options: {
      cutout: '70%',
      plugins: {
        legend: {
          display: false
        }
      }
    }
  });
  
  // 趋势图表
  new Chart(trendChart.value, {
    type: 'line',
    data: {
      labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月'],
      datasets: [{
        label: '目标完成率',
        data: [45, 52, 38, 65, 58, 70, 63, 78, 85, 90],
        borderColor: '#00c9a7',
        backgroundColor: 'rgba(0, 201, 167, 0.1)',
        tension: 0.4,
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        x: {
          grid: {
            display: false
          },
          ticks: {
            color: 'rgba(255, 255, 255, 0.6)'
          }
        },
        y: {
          min: 0,
          max: 100,
          grid: {
            color: 'rgba(255, 255, 255, 0.05)'
          },
          ticks: {
            color: 'rgba(255, 255, 255, 0.6)',
            callback: function(value) {
              return value + '%';
            }
          }
        }
      }
    }
  });
});
</script>

<style scoped>
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

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #8a2be2, #4b0082);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  background: linear-gradient(to right, #8a2be2, #4b0082);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  font-size: 16px;
  font-weight: 500;
  position: relative;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #8a2be2, #4b0082);
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}






.main-content-wrapper {
  height: calc(100vh - 160px); /* 减去头部和底部的高度 */
  overflow-y: auto;
}

/* 滚动条样式 - Webkit内核浏览器 */
.main-content-wrapper::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.main-content-wrapper::-webkit-scrollbar-thumb {
  background-color: rgba(138, 43, 226, 0.5);
  border-radius: 4px;
  transition: background-color 0.3s;
}

.main-content-wrapper::-webkit-scrollbar-track {
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.main-content-wrapper::-webkit-scrollbar-corner {
  background-color: transparent;
}

/* 亮色模式下的滚动条样式 */
.home-container-light .main-content-wrapper::-webkit-scrollbar-thumb {
  background-color: rgba(138, 43, 226, 0.3);
}

.home-container-light .main-content-wrapper::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.05);
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px;
}

.content-wrapper {
  min-height: 100%;
}

.hero-section {
  text-align: center;
  padding: 80px 0 120px;
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 52px;
  font-weight: 800;
  background: linear-gradient(to right, #fff, #d3c1ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1.2;
  margin-bottom: 20px;
}
.hero-title-light {
  font-size: 52px;
  font-weight: 800;
  background: linear-gradient(to right, #180202, #070116);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1.2;
  margin-bottom: 20px;
}

.hero-subtitle {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin-bottom: 40px;
}
.hero-subtitle-light {
  font-size: 20px;
  color: rgba(27, 2, 2, 0.7);
  line-height: 1.6;
  margin-bottom: 40px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.features-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
  margin-bottom: 60px;
}

.feature-card {
  background: rgba(30, 30, 42, 0.7);
  border-radius: 16px;
  padding: 30px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.card-title {
  font-size: 22px;
  font-weight: 600;
}

.echart-icon {
  width: 28px;
  height: 28px;
}

.stats-container {
  display: flex;
  gap: 30px;
  margin-top: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.chart-container {
  position: relative;
  height: 500px;
  width: 100%;
  margin-top: 20px;
}

.targets-section {
  margin-top: 40px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 30px;
  position: relative;
  padding-left: 20px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 32px;
  background: linear-gradient(to bottom, #8a2be2, #4b0082);
  border-radius: 4px;
}

.target-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
}

.target-card {
  background: rgba(30, 30, 42, 0.7);
  border-radius: 16px;
  padding: 25px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.target-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.goal-title {
  font-size: 18px;
  font-weight: 600;
}

.goal-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.footer {
  text-align: center;
  padding: 40px 0;
  margin-top: 60px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}
</style>