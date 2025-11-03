<template>
  <n-flex vertical>
    <n-flex align="center" justify="start">
     
      <n-divider vertical />
      <span>周开始日：</span>
      <n-select
        v-model:value="firstDayOfWeek"
        :options="weekStartOptions"
        style="width: 120px"
      />
      <n-divider vertical />
    </n-flex>
    <n-flex>
      <n-radio-group v-model:value="size" name="size">
        <n-radio-button
          v-for="option in sizeOptions"
          :key="option.value"
          :value="option.value"
          :label="option.label"
        />
      </n-radio-group>
      <n-divider vertical />
      <n-radio-group v-model:value="value" name="year">
        <n-radio-button
          v-for="range in dateRanges"
          :key="range.value"
          :value="range.value"
          :label="range.label"
        />
      </n-radio-group>
    </n-flex>
    <n-alert type="success" title="数据统计">
      <n-flex>
        <n-tag round type="info">
          总天数: {{ dataStats.total }}
        </n-tag>
        <n-tag round type="warning">
          空白天: {{ dataStats.zeros }} ({{ dataStats.zeroPercent }}%)
        </n-tag>
        <n-tag round type="success">
          最大值: {{ dataStats.maxValue }}
        </n-tag>
        <n-tag round type="primary">
          平均值: {{ dataStats.avgValue }}
        </n-tag>
      </n-flex>
    </n-alert>
    <n-scrollbar x-scrollable style="max-width: 100%">
      <n-heatmap
        :data="yearData"
        :loading-data="yearData"
        :first-day-of-week="firstDayOfWeek"
        :loading="loading"
        :size="size"
        :show-week-labels="showWeekLabels"
        :show-month-labels="showMonthLabels"
        :show-color-indicator="showColorIndicator"
        :fill-calendar-leading="value === 'recent'"
      />
    </n-scrollbar>
  </n-flex>
</template>

<script setup>
import { heatmapMockData,NHeatmap } from "naive-ui";
import { computed, ref,onMounted } from "vue";

const init=onMounted(()=>{
    loading.value=true
    setTimeout(()=>{
      loading.value=false
    },2000)
  
  
})

const value = ref("recent");
const dateRanges = [
  {
    value: "recent",
    label: "最近一年"
  },
  {
    value: 2025,
    label: 2025
  },
  {
    value: 2024,
    label: 2024
  },
  {
    value: 2023,
    label: 2023
  },
  {
    value: 2022,
    label: 2022
  }
].map((r) => {
  return {
    ...r,
    label: r.label.toString()
  };
});

const yearData = computed(() => {
  return heatmapMockData("recent");
});

const dataStats = computed(() => {
  const data = yearData.value;
  const total = data.length;
  const zeros = data.filter((d) => d.value === 0).length;
  const maxValue = Math.max(...data.map((d) => d.value ?? 0));
  const avgValue = Math.round(
    data.reduce((sum, d) => sum + (d.value ?? 0), 0) / total * 100
  ) / 100;
  
  return {
    total,
    zeros,
    maxValue,
    avgValue,
    zeroPercent: Math.round(zeros / total * 100)
  };
});

const showWeekLabels = ref(true);
const showMonthLabels = ref(true);
const showColorIndicator = ref(true);
const loading = ref(false);
const firstDayOfWeek = ref(0);
const size = ref("medium");

const weekStartOptions = [
  { label: "周一", value: 0 },
  { label: "周二", value: 1 },
  { label: "周三", value: 2 },
  { label: "周四", value: 3 },
  { label: "周五", value: 4 },
  { label: "周六", value: 5 },
  { label: "周日", value: 6 }
];

const sizeOptions = [
  { label: "小", value: "small" },
  { label: "中", value: "medium" },
  { label: "大", value: "large" }
];
</script>