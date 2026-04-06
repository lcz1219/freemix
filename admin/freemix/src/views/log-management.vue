<template>
  <div class="log-management">
    <div class="page-header">
      <div class="header-left">
        <h2 class="title">系统运行日志</h2>
        <p class="subtitle">全量监控系统 API 调用及异常记录</p>
      </div>
      <div class="header-right">
        <n-space>
          <!-- <n-button type="warning" ghost @click="handleClearLogs">
            <template #icon><n-icon><TrashOutline /></n-icon></template>
            清空日志
          </n-button> -->
          <n-button type="primary" @click="fetchLogs">
            <template #icon><n-icon><RefreshOutline /></n-icon></template>
            刷新数据
          </n-button>
        </n-space>
      </div>
    </div>

    <n-grid :cols="24" :x-gap="16" class="stats-grid">
      <n-grid-item :span="16">
        <n-card class="stats-card" title="请求状态分布" :bordered="false">
          <template #header-extra>
            <n-icon size="20" color="#aaa"><BarChartOutline /></n-icon>
          </template>
          <div ref="chartRef" style="height: 200px;"></div>
        </n-card>
      </n-grid-item>
      <n-grid-item :span="8">
        <n-card class="stats-card" title="实时概览" :bordered="false">
          <n-space vertical size="large">
            <n-statistic label="成功请求" :value="stats.success">
              <template #prefix>
                <n-icon color="#18a058"><CheckmarkCircleOutline /></n-icon>
              </template>
            </n-statistic>
            <n-statistic label="失败请求" :value="stats.fail">
              <template #prefix>
                <n-icon color="#d03050"><CloseCircleOutline /></n-icon>
              </template>
            </n-statistic>
            <n-statistic label="成功率" :value="stats.ratio"></n-statistic>
          </n-space>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card class="filter-card" :bordered="false">
      <n-form inline :model="queryParams" label-placement="left" :show-feedback="false">
        <!-- <n-form-item label="操作人">
          <n-input v-model:value="queryParams.username" placeholder="搜索用户名" clearable />
        </n-form-item> -->
        <n-form-item label="请求路径">
          <n-input v-model:value="queryParams.url" placeholder="请求路径关键字" clearable />
        </n-form-item>
        <n-form-item label="请求状态">
          <n-select
            v-model:value="queryParams.status"
            placeholder="请选择"
            :options="statusOptions"
            clearable
            style="width: 160px"
          />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" @click="handleSearch">查询</n-button>
        </n-form-item>
      </n-form>
    </n-card>

    <n-data-table
      remote
      ref="table"
      :columns="columns"
      :data="logList"
      :loading="loading"
      :pagination="pagination"
      :row-key="(row) => row.id"
      @update:page="handlePageChange"
      class="log-table"
    />

    <!-- 日志详情抽屉 -->
    <n-drawer v-model:show="showDrawer" :width="600" placement="right">
      <n-drawer-content title="日志详细报文" closable>
        <div v-if="selectedLog" class="detail-container">
          <n-descriptions label-placement="left" :column="1" bordered>
            <n-descriptions-item label="请求 ID">{{ selectedLog.id }}</n-descriptions-item>
            <!-- <n-descriptions-item label="操作用户">
              <n-tag type="info">{{ selectedLog.username }}</n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="请求 IP">{{ selectedLog.ip }}</n-descriptions-item> -->
            <!-- <n-descriptions-item label="请求地址">
              <n-text code>{{ selectedLog.method }} {{ selectedLog.url }}</n-text>
            </n-descriptions-item> -->
            <n-descriptions-item label="执行方法">{{ selectedLog.classMethod }}</n-descriptions-item>
            <n-descriptions-item label="消耗时间">
              <n-text :type="getSpendTimeType(selectedLog.spendTime)">
                {{ selectedLog.spendTime }} ms
              </n-text>
            </n-descriptions-item>
            <n-descriptions-item label="请求时间">{{ selectedLog.createTimeStr || selectedLog.createTime }}</n-descriptions-item>
          </n-descriptions>

          <div class="json-box">
            <p class="json-label">请求参数 (Arguments):</p>
            <pre class="json-content">{{ formatJson(selectedLog.args) }}</pre>
          </div>

          <div class="json-box">
            <p class="json-label">响应结果 (Result):</p>
            <pre class="json-content" :class="{ 'error-result': isErrorResult(selectedLog.result) }">{{ formatJson(selectedLog.result) }}</pre>
          </div>
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, watch, nextTick, onUnmounted } from 'vue';
import { 
  NButton, NDataTable, NCard, NForm, NFormItem, NInput, NSelect, 
  NTag, NSpace, NIcon, NDrawer, NDrawerContent, NDescriptions, 
  NDescriptionsItem, NText, useDialog, useMessage, NGrid, NGridItem, NStatistic
} from 'naive-ui';
import { 
  RefreshOutline, 
  TrashOutline, 
  EyeOutline,
  CheckmarkCircleOutline,
  CloseCircleOutline,
  BarChartOutline
} from '@vicons/ionicons5';
import { getM, postM, isSuccess } from '@/utils/request';
import * as echarts from 'echarts';

const dialog = useDialog();
const message = useMessage();

const loading = ref(false);
const logList = ref([]);
const showDrawer = ref(false);
const selectedLog = ref(null);
const chartRef = ref(null);
let myChart = null;

const stats = reactive({
  success: 0,
  fail: 0,
  total: 0,
  ratio: '0%'
});

const queryParams = reactive({
  username: '',
  status: null,
  url: ''
});

const pagination = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [20, 50, 100]
});

const statusOptions = [
  { label: '请求成功 (200)', value: 200 },
  { label: '请求失败', value: 500 }
];

const initChart = () => {
  if (!chartRef.value) return;
  if (myChart) {
    myChart.dispose();
  }
  myChart = echarts.init(chartRef.value, 'dark');
  updateChart();
};

const updateChart = () => {
  if (!myChart) return;
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: { color: '#aaa' }
    },
    series: [
      {
        name: '请求状态',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#121212',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold',
            color: '#fff'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: stats.success, name: '成功', itemStyle: { color: '#18a058' } },
          { value: stats.fail, name: '失败', itemStyle: { color: '#d03050' } }
        ]
      }
    ]
  };
  myChart.setOption(option);
};

const fetchLogs = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...queryParams
    };
    const res = await getM('/api/logs/list', params);
    if (res.data.code === 200) {
      const { list, total, successCount, failCount } = res.data.data;
      logList.value = list;
      pagination.itemCount = total;
      
      stats.success = successCount || 0;
      stats.fail = failCount || 0;
      stats.total = (successCount || 0) + (failCount || 0);
      stats.ratio = stats.total > 0 ? ((stats.success / stats.total) * 100).toFixed(1) + '%' : '0%';
      
      nextTick(() => {
        if (!myChart) {
          initChart();
        } else {
          updateChart();
        }
      });
    } else {
      message.error(res.data.msg || '获取日志失败');
    }
  } catch (err) {
    console.error(err);
    message.error('网络错误');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchLogs();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (myChart) {
    myChart.dispose();
  }
});

const handleResize = () => {
  if (myChart) {
    myChart.resize();
  }
};

const columns = [
  {
    title: '请求时间',
    key: 'createTimeStr',
    width: 160,
    render(row) {
      return row.createTimeStr || row.createTime;
    }
  },
  // {
  //   title: '用户',
  //   key: 'username',
  //   width: 120,
  //   render(row) {
  //     return h(NTag, { type: 'info', bordered: false }, { default: () => row.username });
  //   }
  // },
  {
    title: '请求路径',
    width: 180,

    key: 'url',
    // 开启超出省略和悬浮提示
    ellipsis: { tooltip: true },
    render(row) {
      return h('div', { 
        style: 'display: flex; align-items: center; gap: 8px; width: 100%; ' 
      }, [
        h(NTag, { 
          type: getMethodTagType(row.result), 
          size: 'small',
          // 增加 min-width: 60px 防止标签被过长的 URL 挤压变形
          style: ' min-width: 60px; justify-content: center'
        }, { default: () => row.classMethod }),
        
        // h(NText, { 
        //   code: true,
        //   // 增加 flex: 1 占据剩余空间，并设置文字溢出显示省略号
        //   style: 'flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;'
        // }, { default: () => row.url })
      ]);
    }
  },
  {
    title: '耗时',
    key: 'spendTime',
    width: 100,
    render(row) {
      return h(NText, { type: getSpendTimeType(row.spendTime) }, { default: () => `${row.spendTime}ms` });
    }
  },
  // {
  //   title: 'IP',
  //   key: 'ip',
  //   width: 140
  // },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right',
    render(row) {
      return h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          ghost: true,
          onClick: () => handleViewDetail(row)
        },
        { 
          default: () => '查看详情',
          icon: () => h(NIcon, null, { default: () => h(EyeOutline) })
        }
      );
    }
  }
];

const getMethodTagType = (result) => {
  console.log("getMethodTagType",result);
  
  switch (result.code) {
    case 200: return 'success';
    case undefined: return 'success';
  
    default: return 'error';
  }
};

const getSpendTimeType = (time) => {
  if (time > 1000) return 'error';
  if (time > 500) return 'warning';
  return 'success';
};

// const fetchLogs = async () => {
//   loading.value = true;
//   try {
//     const params = {
//       page: pagination.page,
//       size: pagination.pageSize,
//       ...queryParams
//     };
//     const res = await getM('/api/logs/list', params);
//     if (res.data.code === 200) {
//       const { list, total } = res.data.data;
//       logList.value = list;
//       pagination.itemCount = total;
//     } else {
//       message.error(res.data.msg || '获取日志失败');
//     }
//   } catch (err) {
//     console.error(err);
//     message.error('网络错误');
//   } finally {
//     loading.value = false;
//   }
// };

const handlePageChange = (page) => {
  pagination.page = page;
  fetchLogs();
};

const handleSearch = () => {
  pagination.page = 1;
  fetchLogs();
};

const handleViewDetail = (row) => {
  selectedLog.value = row;
  showDrawer.value = true;
};

const handleClearLogs = () => {
  dialog.warning({
    title: '确认清空',
    content: '此操作将永久删除所有系统日志，确定继续吗？',
    positiveText: '确定清空',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await postM('/api/logs/clear');
        if (isSuccess(res)) {
          message.success('日志已清空');
          fetchLogs();
        }
      } catch (err) {
        message.error('操作失败');
      }
    }
  });
};

const formatJson = (obj) => {
  if (!obj) return 'N/A';
  try {
    if (typeof obj === 'string') {
      const parsed = JSON.parse(obj);
      return JSON.stringify(parsed, null, 2);
    }
    return JSON.stringify(obj, null, 2);
  } catch (e) {
    return String(obj);
  }
};

const isErrorResult = (result) => {
  if (!result) return false;
  const str = String(result);
  return str.includes('"code":500') || str.includes('"code":40') || str.includes('Error');
};

onMounted(fetchLogs);
</script>

<style scoped lang="scss">
.log-management {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;

  .stats-grid {
    margin-bottom: 8px;
  }

  .stats-card {
    background: rgba(255, 255, 255, 0.03);
    border-radius: 12px;
    height: 100%;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .title {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
    }

    .subtitle {
      margin: 4px 0 0;
      color: #888;
      font-size: 14px;
    }
  }

  .filter-card {
    background: rgba(255, 255, 255, 0.03);
    border-radius: 8px;
  }

  .log-table {
    background: transparent;
  }

  .detail-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .json-box {
    .json-label {
      font-weight: 600;
      margin-bottom: 8px;
      color: #aaa;
      font-size: 13px;
    }

    .json-content {
      background: #1e1e1e;
      color: #d4d4d4;
      padding: 12px;
      border-radius: 6px;
      font-family: 'Fira Code', 'Cascadia Code', monospace;
      font-size: 12px;
      overflow-x: auto;
      max-height: 300px;
      margin: 0;
      border: 1px solid #333;

      &.error-result {
        border-left: 4px solid #f87171;
      }
    }
  }
}

:deep(.n-data-table-td) {
  padding: 12px 16px;
}
</style>
