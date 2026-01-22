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
          总活跃: {{ dataStats.total }}
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
    
    <div class="heatmap-wrapper">
      <div class="week-days-labels">
        <div class="week-label" style="margin-top: 15px;">Mon</div>
        <div class="week-label" style="margin-top: 14px;">Wed</div>
        <div class="week-label" style="margin-top: 14px;">Fri</div>
      </div>
      <div class="heatmap-scroll-container">
        <div class="month-labels">
            <div 
              v-for="(label, index) in monthLabels" 
              :key="index" 
              class="month-label"
              :style="{ left: label.left + 'px' }"
            >
              {{ label.text }}
            </div>
        </div>
        <div class="heatmap-grid" :class="size">
          <div v-for="(week, weekIndex) in calendarGrid" :key="weekIndex" class="heatmap-column">
            <n-tooltip trigger="hover" v-for="(day, dayIndex) in week" :key="dayIndex">
              <template #trigger>
                <div 
                  class="heatmap-cell" 
                  :class="['level-' + day.level]"
                  :style="day.date ? {} : { opacity: 0, pointerEvents: 'none' }"
                ></div>
              </template>
              {{ day.date }}: {{ day.count }} 次活跃
            </n-tooltip>
          </div>
        </div>
      </div>
    </div>
    
    <div class="heatmap-legend">
        <span>Less</span>
        <div class="legend-item level-0"></div>
        <div class="legend-item level-1"></div>
        <div class="legend-item level-2"></div>
        <div class="legend-item level-3"></div>
        <div class="legend-item level-4"></div>
        <span>More</span>
    </div>
  </n-flex>
</template>

<script setup>
import { computed, ref, toRefs, inject } from "vue";
import { NFlex, NDivider, NSelect, NRadioGroup, NRadioButton, NAlert, NTag, NTooltip } from "naive-ui";

const props = defineProps({
  goals: {
    type: Array,
    default: () => []
  }
});

const { goals } = toRefs(props);
// Inject isDark if provided, otherwise default ref
const isDark = inject('isDark', ref(false));

const firstDayOfWeek = ref(1); // 0 = Sunday, 1 = Monday
const weekStartOptions = [
  { label: '周日', value: 0 },
  { label: '周一', value: 1 },
  { label: '周六', value: 6 }
];

const size = ref('medium');
const sizeOptions = [
  { label: '小', value: 'small' },
  { label: '中', value: 'medium' },
  { label: '大', value: 'large' }
];

const value = ref("recent");
const dateRanges = [
  { value: "recent", label: "最近一年" },
  { value: 2025, label: "2025" },
  { value: 2024, label: "2024" },
  { value: 2023, label: "2023" },
  { value: 2022, label: "2022" }
];

// Helper to format date YYYY-MM-DD
const formatDate = (date) => {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// Generate activity map
const activityMap = computed(() => {
  const map = new Map();
  if (!goals.value || !Array.isArray(goals.value)) return map;

  goals.value.forEach(goal => {
    // 1. Created
    if (goal.createdAt) {
      const date = formatDate(goal.createdAt);
      map.set(date, (map.get(date) || 0) + 1);
    }
    // 2. Child goals finished
    if (goal.childGoals && Array.isArray(goal.childGoals)) {
      goal.childGoals.forEach(child => {
        if (child.finish && (child.finishDate || child.finishTime)) {
          try {
            const date = formatDate(child.finishDate || child.finishTime);
            map.set(date, (map.get(date) || 0) + 1);
          } catch (e) {}
        }
      });
    }
    // 3. Goal completed
    if (goal.status === 'completed' && goal.updatedAt) {
      const date = formatDate(goal.updatedAt);
      map.set(date, (map.get(date) || 0) + 1);
    }
  });
  return map;
});

// Calculate start and end dates
const dateRange = computed(() => {
  let startDate, endDate;
  if (value.value === 'recent') {
    endDate = new Date();
    startDate = new Date(endDate);
    startDate.setFullYear(endDate.getFullYear() - 1);
  } else {
    const year = parseInt(value.value);
    startDate = new Date(year, 0, 1);
    endDate = new Date(year, 11, 31);
  }
  return { startDate, endDate };
});

// Generate calendar grid
const calendarGrid = computed(() => {
  const weeks = [];
  const { startDate, endDate } = dateRange.value;

  // Adjust start date to align with firstDayOfWeek
  // We want the first column to potentially have empty cells at top
  // But usually contribution graphs start with the column that contains startDate
  // And we fill backwards or just start from startDate?
  // Github starts 1 year ago exactly, aligned to week.
  
  const startDay = startDate.getDay(); // 0-6
  const diff = (startDay - firstDayOfWeek.value + 7) % 7;
  const gridStartDate = new Date(startDate);
  gridStartDate.setDate(startDate.getDate() - diff);

  const current = new Date(gridStartDate);
  // Ensure we cover enough weeks. 
  
  // We will generate weeks until we pass endDate
  while (current <= endDate || weeks.length < 52) {
    const week = [];
    for (let i = 0; i < 7; i++) {
      // Check if current date is within valid range (mostly for year view)
      // For "recent", we show everything from gridStartDate
      // For specific year, we might want to hide days outside that year?
      // Usually Github shows gray squares for days outside range but inside week.
      
      const dateStr = formatDate(current);
      const count = activityMap.value.get(dateStr) || 0;
      
      let level = 0;
      if (count > 0) level = 1;
      if (count > 2) level = 2;
      if (count > 5) level = 3;
      if (count > 8) level = 4;

      // Hide days if we are in specific year mode and day is outside year
      const year = current.getFullYear();
      let isValid = true;
      if (value.value !== 'recent') {
          if (year !== parseInt(value.value)) {
              isValid = false; // Or just make it level 0 and unclickable?
              // Better to show empty cell
          }
      } else {
          // recent mode: don't show future days
          if (current > new Date()) isValid = false;
      }

      week.push({
        date: isValid ? dateStr : '',
        count: isValid ? count : 0,
        level: isValid ? level : 0,
        obj: new Date(current)
      });
      current.setDate(current.getDate() + 1);
    }
    weeks.push(week);
    if (current > endDate && weeks.length >= 52) break;
  }
  
  return weeks;
});

// Month labels
const monthLabels = computed(() => {
    const labels = [];
    const weeks = calendarGrid.value;
    let lastMonth = -1;
    
    // Cell width + gap
    let cellWidth = 12; // medium
    if (size.value === 'small') cellWidth = 9;
    if (size.value === 'large') cellWidth = 18;
    
    weeks.forEach((week, index) => {
        // Check the first valid day in the week to decide month
        const day = week.find(d => d.date);
        if (day) {
            const date = day.obj;
            const month = date.getMonth();
            if (month !== lastMonth) {
                labels.push({
                    text: date.toLocaleString('default', { month: 'short' }),
                    left: index * cellWidth
                });
                lastMonth = month;
            }
        }
    });
    return labels;
});

const dataStats = computed(() => {
  let total = 0;
  let zeros = 0;
  let maxValue = 0;
  let dayCount = 0;

  calendarGrid.value.forEach(week => {
    week.forEach(day => {
       if (day.date) {
           dayCount++;
           total += day.count;
           if (day.count === 0) zeros++;
           if (day.count > maxValue) maxValue = day.count;
       }
    });
  });

  const avgValue = dayCount > 0 ? (total / dayCount).toFixed(2) : 0;
  const zeroPercent = dayCount > 0 ? ((zeros / dayCount) * 100).toFixed(1) : 0;

  return {
    total,
    zeros,
    zeroPercent,
    maxValue,
    avgValue
  };
});
</script>

<style scoped>
.heatmap-wrapper {
  display: flex;
  margin-top: 10px;
}

.week-days-labels {
  display: flex;
  flex-direction: column;
  padding-top: 20px; /* Space for month labels */
  margin-right: 8px;
  font-size: 12px;
  color: #888;
}

.week-label {
  height: 12px; /* Match cell height approx */
  line-height: 12px;
}

.heatmap-scroll-container {
  overflow-x: auto;
  position: relative;
  flex: 1;
}

.month-labels {
  position: relative;
  height: 20px;
  margin-bottom: 4px;
}

.month-label {
  position: absolute;
  font-size: 12px;
  color: #888;
}

.heatmap-grid {
  display: flex;
  gap: 2px;
}

.heatmap-grid.small { gap: 1px; }
.heatmap-grid.large { gap: 4px; }

.heatmap-column {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.heatmap-grid.small .heatmap-column { gap: 1px; }
.heatmap-grid.large .heatmap-column { gap: 4px; }

.heatmap-cell {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  background-color: #ebedf0;
  border: 1px solid rgba(27, 31, 35, 0.06);
  box-sizing: border-box;
}

.heatmap-grid.small .heatmap-cell { width: 8px; height: 8px; }
.heatmap-grid.medium .heatmap-cell { width: 10px; height: 10px; }
.heatmap-grid.large .heatmap-cell { width: 14px; height: 14px; }

/* Light Theme Colors */
.level-0 { background-color: #ebedf0; }
.level-1 { background-color: #9be9a8; }
.level-2 { background-color: #40c463; }
.level-3 { background-color: #30a14e; }
.level-4 { background-color: #216e39; }

/* Dark Theme overrides via global class or prefers-color-scheme */
@media (prefers-color-scheme: dark) {
  .heatmap-cell { border-color: rgba(255, 255, 255, 0.05); }
  .level-0 { background-color: #161b22; }
  .level-1 { background-color: #0e4429; }
  .level-2 { background-color: #006d32; }
  .level-3 { background-color: #26a641; }
  .level-4 { background-color: #39d353; }
}

/* Explicit dark mode support if 'dark' class is on body */
:global(.dark) .heatmap-cell { border-color: rgba(255, 255, 255, 0.05); }
:global(.dark) .level-0 { background-color: #161b22; }
:global(.dark) .level-1 { background-color: #0e4429; }
:global(.dark) .level-2 { background-color: #006d32; }
:global(.dark) .level-3 { background-color: #26a641; }
:global(.dark) .level-4 { background-color: #39d353; }

.heatmap-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  font-size: 12px;
  color: #888;
  margin-top: 8px;
}

.legend-item {
  width: 10px;
  height: 10px;
  border-radius: 2px;
}
</style>