<template>
  <!-- 定时任务「周期完成情况」可视化弹窗：统计卡 + 热力色块 + 进度柱 + echarts 组合趋势 -->
  <n-modal
    :show="show"
    preset="card"
    :title="modalTitle"
    style="width: min(1100px, 94vw)"
    content-style="padding: 16px 20px;"
    :mask-closable="true"
    @update:show="onShowChange"
  >
    <div v-if="items.length === 0" class="gsc-empty">
      该分组暂无目标实例数据
    </div>

    <div v-else class="gsc-body" :class="isDark ? 'gsc-dark' : 'gsc-light'">
      <!-- 顶部统计 -->
      <div class="gsc-stats">
        <div class="gsc-stat">
          <span class="gsc-stat-num">{{ items.length }}</span>
          <span class="gsc-stat-label">总期数</span>
        </div>
        <div class="gsc-stat">
          <span class="gsc-stat-num" style="color:#00c9a7">{{ stat.completed }}</span>
          <span class="gsc-stat-label">完成</span>
        </div>
        <div class="gsc-stat">
          <span class="gsc-stat-num" style="color:#f5a524">{{ stat.running }}</span>
          <span class="gsc-stat-label">进行中</span>
        </div>
        <div class="gsc-stat">
          <span class="gsc-stat-num" style="color:#ff6b6b">{{ stat.expired }}</span>
          <span class="gsc-stat-label">过期/漏掉</span>
        </div>
        <div class="gsc-stat">
          <span class="gsc-stat-num" style="color:#00c9a7">{{ stat.rate }}%</span>
          <span class="gsc-stat-label">整体完成率</span>
        </div>
        <div class="gsc-stat">
          <span class="gsc-stat-num" style="color:#00c9a7">{{ stat.longest }}</span>
          <span class="gsc-stat-label">最长连续</span>
        </div>
        <div class="gsc-stat">
          <span class="gsc-stat-num" style="color:#00c9a7">{{ stat.current }}</span>
          <span class="gsc-stat-label">当前连续</span>
        </div>
      </div>

      <!-- 规则信息（命中定时任务时展示） -->
      <div v-if="rule" class="gsc-rule">
        <span class="gsc-rule-item">规则：{{ recurrenceLabel(rule.recurrenceType) }}</span>
        <span class="gsc-rule-item">建立：{{ fmtFull(rule.createTime) }}</span>
        <span class="gsc-rule-item">下一期：{{ fmtFull(rule.nextExecutionTime) }}</span>
        <span class="gsc-rule-item">状态：{{ rule.isActive ? '启用中' : '已停用' }}</span>
      </div>

      <!-- ① 热力色块：每期一格，按结果着色 -->
      <section class="gsc-section">
        <div class="gsc-section-title">① 周期完成热力条（每格 = 一期，悬停看明细）</div>
        <div class="gsc-heat">
          <div
            v-for="(it, idx) in items"
            :key="it.goal._id || idx"
            class="gsc-heat-cell"
            :style="{ backgroundColor: statusColor(it) }"
            :title="cellTip(it)"
          ></div>
        </div>
        <div class="gsc-legend">
          <span class="gsc-legend-item"><i class="gsc-dot" style="background:#00c9a7"></i>已完成</span>
          <span class="gsc-legend-item"><i class="gsc-dot" style="background:#f5a524"></i>进行中</span>
          <span class="gsc-legend-item"><i class="gsc-dot" style="background:#ff6b6b"></i>过期/漏掉</span>
        </div>
      </section>

      <!-- ② 进度柱：每期一根，高度 = 当期完成度 -->
      <!-- <section class="gsc-section">
        <div class="gsc-section-title">② 每期完成度柱状时间轴（高度 = 完成百分比）</div>
        <div class="gsc-bars">
          <div
            v-for="(it, idx) in items"
            :key="'b' + (it.goal._id || idx)"
            class="gsc-bar-col"
            :title="cellTip(it)"
          >
            <div class="gsc-bar-wrap" :class="it.st === 'expired' ? 'bar-expired' : ''">
              <div
                class="gsc-bar-fill"
                :class="it.st"
                :style="{ height: Math.max(it.st === 'in-progress' && it.progress === 0 ? 3 : it.progress, 3) + '%' }"
              ></div>
            </div>
          </div>
        </div>
        <div class="gsc-date-axis">
          <span>{{ items[0].startLabel }}</span>
          <span>{{ items[items.length - 1].startLabel }}（最近一期）</span>
        </div>
      </section> -->

      <!-- ③ echarts 组合图：每期已完成/未完成子目标堆叠 + 累计完成率折线 -->
      <section class="gsc-section">
        <div class="gsc-section-title">② 组合趋势图（堆叠柱 = 每期子目标完成数，折线 = 累计完成率）</div>
        <div ref="chartEl" class="gsc-chart"></div>
      </section>
    </div>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, computed, inject, watch, nextTick, onBeforeUnmount } from 'vue';
import { NModal } from 'naive-ui';
import * as echarts from 'echarts';

const props = defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: '' },
  // 该标题分组下的所有目标实例（同一定时任务产生的每一期）
  instances: { type: Array as () => any[], default: () => [] },
  // 命中的定时任务规则（可能为空）
  rule: { type: Object as () => any | null, default: null }
});
const emit = defineEmits(['update:show']);

const isDark = inject('isDark', ref(false));

const modalTitle = computed(() => {
  const t = props.title || '该目标';
  const rule = props.rule;
  if (rule) {
    const typeMap: Record<string, string> = { daily: '每天', weekly: '每周', monthly: '每月', cron: 'Cron' };
    return `${t} · 周期完成情况（${typeMap[rule.recurrenceType] || rule.recurrenceType}）`;
  }
  return `${t} · 周期完成情况`;
});

const onShowChange = (v: boolean) => emit('update:show', v);

// ---------------- 数据归一化 ----------------
const fmtDate = (d: Date) => {
  if (!d || isNaN(d.getTime())) return '';
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

const fmtFull = (v: any) => {
  if (!v) return '未设置';
  const d = new Date(v);
  if (isNaN(d.getTime())) return '未设置';
  return `${fmtDate(d)} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
};

// 解析一条目标实例的展示状态 / 进度 / 子目标完成数
const resolveItem = (g: any) => {
  const children = g.childGoals || [];
  const total = children.length;
  const done = children.filter((c: any) => c.finish).length;
  const end = g.deadline ? new Date(g.deadline) : null;
  const start = g.createTime ? new Date(g.createTime) : end;
  const prog =
    typeof g.progress === 'number' && !isNaN(g.progress)
      ? Math.min(Math.max(Math.round(g.progress), 0), 100)
      : total
        ? Math.round((done / total) * 100)
        : 0;

  let st: 'completed' | 'in-progress' | 'expired';
  if (prog >= 100 || g.status === 'completed') {
    st = 'completed';
  } else if (g.status === 'expired' || (end && end.getTime() < Date.now() && g.status !== 'in-progress')) {
    st = 'expired';
  } else {
    st = 'in-progress';
  }

  return {
    goal: g,
    start,
    end,
    startLabel: start ? fmtDate(start) : '未知',
    total,
    done,
    prog,
    st
  };
};

const items = computed(() => {
  const arr = (props.instances || []).slice();
  const list = arr.map(resolveItem);
  // 按周期起点升序排列（从左到右 = 从最早到最近）
  list.sort((a, b) => {
    const t1 = a.start ? a.start.getTime() : Number.MAX_SAFE_INTEGER;
    const t2 = b.start ? b.start.getTime() : Number.MAX_SAFE_INTEGER;
    return t1 - t2;
  });
  return list;
});

// ---------------- 统计 ----------------
const stat = computed(() => {
  const list = items.value;
  const completed = list.filter(i => i.st === 'completed').length;
  const running = list.filter(i => i.st === 'in-progress').length;
  const expired = list.filter(i => i.st === 'expired').length;
  const rate = list.length ? Math.round((completed / list.length) * 100) : 0;

  // 最长连续完成 / 当前连续（从最近一期往回数）
  let longest = 0;
  let cur = 0;
  for (const it of list) {
    if (it.st === 'completed') {
      cur++;
      longest = Math.max(longest, cur);
    } else {
      cur = 0;
    }
  }
  let current = 0;
  for (let i = list.length - 1; i >= 0; i--) {
    if (list[i].st === 'completed') current++;
    else break;
  }
  return { completed, running, expired, rate, longest, current };
});

// ---------------- 颜色与提示 ----------------
const statusColor = (it: any) => {
  if (it.st === 'completed') return '#00c9a7';
  if (it.st === 'expired') return '#ff6b6b';
  return '#f5a524';
};

const statusLabel: Record<string, string> = {
  completed: '已完成',
  'in-progress': '进行中',
  expired: '过期/漏掉'
};

const cellTip = (it: any) => {
  const lines = [
    `周期：${it.startLabel}${it.end ? ' ~ ' + fmtDate(it.end) : ''}`,
    `状态：${statusLabel[it.st]}`,
    `完成子目标：${it.done} / ${it.total}`,
    `进度：${it.prog}%`
  ];
  if (it.st === 'completed' && it.goal.completedDate) lines.push(`完成于：${fmtDate(new Date(it.goal.completedDate))}`);
  return lines.join('\n');
};

const recurrenceLabel = (t: string) => {
  const map: Record<string, string> = { daily: '每天', weekly: '每周', monthly: '每月', cron: 'Cron 定制' };
  return map[t] || t || '未知';
};

// ---------------- echarts 组合图 ----------------
const chartEl = ref<HTMLElement | null>(null);
let chartInst: echarts.ECharts | null = null;

const disposeChart = () => {
  if (chartInst) {
    chartInst.dispose();
    chartInst = null;
  }
};

const initChart = () => {
  disposeChart();
  if (!props.show || !chartEl.value) return;

  const list = items.value;
  if (list.length === 0) return;

  const inst = echarts.init(chartEl.value);
  const dark = !!isDark.value;
  const axisColor = dark ? '#888' : '#666';
  const splitColor = dark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)';

  const xData: string[] = [];
  const doneData: number[] = [];
  const undoneData: number[] = [];
  const rateData: (number | null)[] = [];
  let accDone = 0;
  list.forEach((it, i) => {
    xData.push(it.startLabel);
    doneData.push(it.done);
    undoneData.push(Math.max(it.total - it.done, 0));
    if (it.st === 'completed') accDone++;
    rateData.push(list.length ? Number(((accDone / (i + 1)) * 100).toFixed(1)) : null);
  });

  // 左侧子目标数轴量程向上取整，避免柱顶贴边
  const maxTotal = Math.max(...list.map(i => i.total), 1);
  const yMax = Math.max(5, Math.ceil(maxTotal / 5) * 5);
  const many = list.length > 30;

  inst.setOption({
    backgroundColor: 'transparent',
    color: ['#00c9a7', 'rgba(155,155,155,0.35)', '#f5a524'],
    tooltip: {
      trigger: 'axis',
      backgroundColor: dark ? 'rgba(30,30,30,0.9)' : 'rgba(255,255,255,0.95)',
      borderColor: dark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)',
      textStyle: { color: dark ? '#fff' : '#333' },
      axisPointer: { type: 'shadow' }
    },
    legend: {
      top: 0,
      textStyle: { color: dark ? '#aaa' : '#666' }
    },
    grid: { left: '6%', right: '6%', top: '10%', bottom: many ? '18%' : '12%', containLabel: true },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: { lineStyle: { color: splitColor } },
      axisTick: { show: false },
      axisLabel: {
        color: axisColor,
        interval: many ? 'auto' : 0,
        rotate: many ? 40 : 0,
        fontSize: 10
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '子目标数',
        min: 0,
        max: yMax,
        splitLine: { lineStyle: { color: splitColor } },
        axisLabel: { color: axisColor }
      },
      {
        type: 'value',
        name: '完成率',
        min: 0,
        max: 100,
        splitLine: { show: false },
        axisLabel: { color: axisColor, formatter: '{value}%' }
      }
    ],
    series: [
      {
        name: '已完成子目标',
        type: 'bar',
        stack: 'period',
        barMaxWidth: 18,
        itemStyle: { color: '#00c9a7' },
        data: doneData
      },
      {
        name: '未完成子目标',
        type: 'bar',
        stack: 'period',
        barMaxWidth: 18,
        itemStyle: { color: dark ? 'rgba(150,150,150,0.28)' : 'rgba(150,150,150,0.25)' },
        data: undoneData
      },
      {
        name: '累计完成率',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#f5a524', type: 'dashed' },
        itemStyle: { color: '#f5a524' },
        data: rateData
      }
    ],
    animationDuration: 800
  });

  chartInst = inst;
  // 图表在弹窗动画结束后尺寸才稳定，稍后强制刷新一次
  setTimeout(() => inst.resize(), 120);
};

// 弹窗打开 / 数据 / 主题变化时刷新图表
watch(
  () => [props.show, props.instances, isDark.value],
  async () => {
    if (props.show) {
      await nextTick();
      setTimeout(() => initChart(), 60);
    } else {
      disposeChart();
    }
  },
  { deep: false }
);

const handleResize = () => {
  if (chartInst) chartInst.resize();
};
window.addEventListener('resize', handleResize);
onBeforeUnmount(() => {
  disposeChart();
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.gsc-body {
  max-height: 74vh;
  overflow-y: auto;
}
.gsc-empty {
  padding: 60px 0;
  text-align: center;
  color: #888;
}

/* ---------- 统计卡 ---------- */
.gsc-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}
.gsc-stat {
  flex: 1 1 96px;
  min-width: 96px;
  background: rgba(0, 201, 167, 0.06);
  border: 1px solid rgba(0, 201, 167, 0.18);
  border-radius: 10px;
  padding: 8px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.gsc-stat-num {
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
}
.gsc-stat-label {
  font-size: 11px;
  color: #888;
}

/* ---------- 规则信息 ---------- */
.gsc-rule {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 12px;
  color: #00c9a7;
  background: rgba(0, 201, 167, 0.05);
  border: 1px dashed rgba(0, 201, 167, 0.3);
  border-radius: 8px;
  padding: 6px 12px;
  margin-bottom: 8px;
}

/* ---------- 区块 ---------- */
.gsc-section {
  margin-top: 16px;
}
.gsc-section-title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 10px;
  padding-left: 8px;
  border-left: 3px solid #00c9a7;
  color: var(--gsc-text);
}
.gsc-light .gsc-section-title { --gsc-text: #1f2937; }
.gsc-dark .gsc-section-title { --gsc-text: #e5e5e5; }

/* ---------- ① 热力色块 ---------- */
.gsc-heat {
     display: flex;
    align-items: flex-end;
    gap: 3px;
    overflow-x: auto;
    padding: 6px 2px;
    min-height: 30px;
    flex-wrap: wrap;
}
.gsc-heat-cell {
  width: 18px;
  height: 18px;
  min-width: 18px;
  border-radius: 3px;
  cursor: pointer;
  transition: transform 0.15s ease;
}
.gsc-heat-cell:hover {
  transform: scale(1.25);
}
.gsc-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 8px;
  font-size: 12px;
  color: #888;
  margin-bottom: 44px;
}
.gsc-legend-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.gsc-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 2px;
}

/* ---------- ② 完成度柱 ---------- */
.gsc-bars {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  overflow-x: auto;
  padding: 6px 2px;
  min-height: 70px;
}
.gsc-bar-col {
  min-width: 14px;
  flex: 0 0 14px;
  height: 58px;
  cursor: pointer;
}
.gsc-bar-wrap {
  position: relative;
  height: 100%;
  width: 100%;
  background: rgba(128, 128, 128, 0.12);
  border-radius: 3px;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
}
.gsc-bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #00c9a7, rgba(0, 201, 167, 0.45));
  border-radius: 3px;
  transition: height 0.3s ease;
}
.gsc-bar-fill.expired {
  background: linear-gradient(180deg, #ff6b6b, rgba(255, 107, 107, 0.5));
}
.gsc-bar-fill.in-progress {
  background: linear-gradient(180deg, #f5a524, rgba(245, 165, 36, 0.45));
}
.gsc-date-axis {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #888;
  margin-top: 2px;
}

/* ---------- ③ echarts ---------- */
.gsc-chart {
  width: 100%;
  height: 320px;
}
</style>
