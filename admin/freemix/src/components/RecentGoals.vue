<template>
  <n-card class="feature-card">
    <div class="card-header">
      <div ref="timelineChart" class="echart-icon"></div>
      <h2 class="card-title">近期目标</h2>
    </div>

    <div class="timeline-container">
      <n-timeline>
        <n-timeline-item 
          v-for="(item, index) in recentGoals" 
          :title="item.title" 
          :time="formatDate(item.deadline)"
          :type="checktype(item)"
          :key="index">
        </n-timeline-item>
      </n-timeline>
    </div>
  </n-card>
</template>

<script setup>
import { ref, onMounted,computed } from 'vue';
import { NTimeline, NTimelineItem, NCard } from 'naive-ui';
import * as echarts from 'echarts/core';
import { BarChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 注册 ECharts 组件
echarts.use([
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
  CanvasRenderer
]);

// 定义 props
const props = defineProps({
  goals: {
    type: Array,
    required: true
  },
  formatDate: {
    type: Function,
    required: true
  },
  checktype: {
    type: Function,
    required: true
  }
});

// 图表引用
const timelineChart = ref(null);
// 计算最近的5个目标
const recentGoals = computed(() => {
  const res=props.goals.sort((a, b) => new Date(a.deadline) - new Date(b.deadline)).slice(0, 5)
  console.log("res",res);
  
  return res;
});



// 初始化图表
onMounted(() => {
  // 时间线图表图标 (ECharts)
  const timelineChartInstance = echarts.init(timelineChart.value);
  timelineChartInstance.setOption({
    series: [{
      type: 'bar',
      data: [10, 15, 12, 8],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
          offset: 0,
          color: '#81c683'
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
});
</script>

<style scoped>
:deep.n_card_content {
}
.feature-card {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  height: 100%;
  padding: 0;
  width: 100%;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(129, 198, 131, 0.4);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.echart-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #81c683, #4b0082);
  border-radius: 10px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #ffffff;
}

.timeline-container {
  max-height: 300px;
  overflow-y: auto;
  padding-right: 10px;
}

.timeline-container::-webkit-scrollbar {
  width: 6px;
}

.timeline-container::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.timeline-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #81c683, #4b0082);
  border-radius: 3px;
}

.timeline-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #9b4dff, #5c00b3);
}
</style>