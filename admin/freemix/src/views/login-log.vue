<template>
  <!-- 最外层使用 n-layout 确保背景色随主题自动切换 -->
  <n-layout class="login-log-container" content-style="min-height: 100vh;">
    <div class="main-content">
      
      <!-- 头部区域 -->
      <div class="page-header">
        <div class="header-icon-box">
          <n-icon size="32" class="header-icon">
             <SearchIcon />
          </n-icon>
        </div>
        <div class="header-text">
          <h1 class="title">登录日志</h1>
          <p class="subtitle">查看您的账户安全登录历史记录</p>
        </div>
      </div>

      <!-- 主体卡片 -->
      <n-card class="content-card" :bordered="false">
        
        <!-- 筛选区域：使用 n-layout 或自定义 div 配合 var(--n-action-color) -->
        <div class="filter-section">
          <n-form :model="filter" label-placement="left" :show-feedback="false">
            <div class="filter-grid">
              <!-- 左侧输入 -->
              <div class="filter-inputs">
                <n-space align="center">
                  <span class="label-text">时间范围：</span>
                  <n-date-picker
                    v-model:value="filter.startDate"
                    type="date"
                    placeholder="开始日期"
                    class="date-picker"
                    clearable
                  />
                  <span class="separator">-</span>
                  <n-date-picker
                    v-model:value="filter.endDate"
                    type="date"
                    placeholder="结束日期"
                    class="date-picker"
                    clearable
                  />
                  
                  <span class="label-text" style="margin-left: 12px;">状态：</span>
                  <n-select 
                    v-model:value="filter.status" 
                    placeholder="全部" 
                    class="status-select"
                    :options="[
                      { label: '全部', value: 'all' },
                      { label: '成功', value: 'success' },
                      { label: '失败', value: 'failed' }
                    ]"
                  />
                </n-space>
              </div>

              <!-- 右侧按钮 -->
              <div class="filter-actions">
                <n-space>
                  <n-button type="primary" @click="searchLoginLogs">
                    <template #icon><n-icon><SearchIcon /></n-icon></template>
                    搜索
                  </n-button>
                  <n-button @click="resetFilter">重置</n-button>
                </n-space>
              </div>
            </div>
          </n-form>
        </div>

        <!-- 表格区域 -->
        <div class="table-container">
          <n-data-table
            :bordered="false"
            :single-line="false"
            :columns="columns"
            :data="loginLogs"
            :pagination="pagination"
            :loading="loading"
            :remote="true"
            striped
          />
        </div>

      </n-card>
    </div>
  </n-layout>
</template>

<script setup>
import { ref, onMounted, computed, h } from 'vue'
import { getM } from '@/utils/request.js'
import { useStore } from 'vuex'
import { Search } from '@vicons/ionicons5'
import { 
  NCard, 
  NForm, 
  NFormItem, 
  NDatePicker, 
  NSelect,
  NButton, 
  NDataTable, 
  NSpace,
  NLayout,
  NTag,
  NIcon
} from 'naive-ui'

const userStore = useStore()
const currentUser = computed(() => userStore.state.user)

// 图标
const SearchIcon = Search

// 数据定义
const loginLogs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filter = ref({
  startDate: null,
  endDate: null,
  status: 'all'
})

// 表格列定义
const columns = [
  {
    title: '登录时间',
    key: 'loginTime',
    width: 180,
    render(row) {
      return formatDateTime(row.loginTime)
    }
  },
  {
    title: 'IP地址',
    key: 'ipAddress',
    width: 150,
    render(row) {
        // 使用 monospace 字体让 IP 对齐更好看
        return h('span', { style: 'font-family: v-mono, monospace' }, row.ipAddress)
    }
  },
  {
    title: '浏览器',
    key: 'browser',
    width: 150
  },
  {
    title: '操作系统',
    key: 'os',
    width: 150
  },
  {
    title: '状态',
    key: 'loginSuccess',
    width: 100,
    align: 'center',
    render(row) {
      return h(NTag, {
        type: row.loginSuccess ? 'success' : 'error',
        size: 'small',
        round: true,
        bordered: false
      }, () => (row.loginSuccess ? '成功' : '失败'))
    }
  },
  {
    title: '备注',
    key: 'errorMessage',
    ellipsis: true,
    render(row) {
       // 根据是否有错误信息显示不同颜色，使用 CSS 变量适配
       if (!row.errorMessage) return '-'
       return h('span', { style: 'color: var(--n-error-color)' }, row.errorMessage)
    }
  }
]

// 分页配置
const pagination = {
  page: currentPage,
  pageSize,
  pageSizes: [10, 20, 50, 100],
  total,
  showSizePicker: true,
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`, // 简化文案
  onUpdatePage: (page) => {
    currentPage.value = page
    fetchLoginLogs()
  },
  onUpdatePageSize: (size) => {
    pageSize.value = size
    currentPage.value = 1
    fetchLoginLogs()
  }
}

// 辅助函数
const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  }).format(date)
}

// 业务逻辑
const searchLoginLogs = () => {
  currentPage.value = 1
  fetchLoginLogs()
}

const resetFilter = () => {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  
  filter.value = { startDate, endDate, status: 'all' }
  currentPage.value = 1
  fetchLoginLogs()
}

const fetchLoginLogs = async () => {
  if (!currentUser.value?.id) return
  
  loading.value = true
  try {
    const params = {
      userId: currentUser.value.id,
      page: currentPage.value,
      size: pageSize.value
    }

    let response
    if (filter.value.startDate && filter.value.endDate) {
      response = await getM('api/login-log/date-range', {
        ...params,
        startDate: filter.value.startDate.toISOString().split('T')[0],
        endDate: filter.value.endDate.toISOString().split('T')[0]
      })
    } else {
      response = await getM('api/login-log/user', params)
    }

    if (response.data?.code === 200) {
      let logs = response.data.data || []
      if (filter.value.status !== 'all') {
        logs = logs.filter(log => 
          filter.value.status === 'success' ? log.loginSuccess : !log.loginSuccess
        )
      }
      loginLogs.value = logs
      total.value = logs.length > 0 ? (logs.length === pageSize.value ? logs.length + 1 : logs.length) : 0
    } else {
      loginLogs.value = []
      total.value = 0
    }
  } catch (error) {
    console.error(error)
    loginLogs.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  resetFilter()
})
</script>

<style scoped>
/* 
  核心：使用 Naive UI 提供的 CSS 变量 
  这些变量在 n-config-provider 内部会自动根据主题变化
*/

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* --- 头部样式 --- */
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.header-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  /* 使用 action-color 作为背景，主色变淡 */
  background-color: var(--n-action-color);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.header-icon {
  /* 使用主色变量 */
  color: var(--n-primary-color);
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  /* 使用一级文字颜色变量 */
  color: var(--n-text-color);
  line-height: 1.2;
}

.subtitle {
  margin: 4px 0 0 0;
  font-size: 14px;
  /* 使用二级文字颜色变量 */
  color: var(--n-text-color-2);
}

/* --- 卡片样式 --- */
.content-card {
  /* 
    n-card 默认会有背景色，但我们可以通过 CSS 变量微调圆角等
    这里不需要设置 background-color，n-card 会自动处理
  */
  border-radius: 8px;
}

/* --- 筛选区域样式 --- */
.filter-section {
  /* 关键：使用 --n-action-color 实现浅灰色背景（暗黑模式下为深灰色） */
  background-color: var(--n-action-color);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 24px;
  /* 加上边框，使用主题边框色 */
  border: 1px solid var(--n-border-color);
}

.filter-grid {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.label-text {
  font-size: 14px;
  color: var(--n-text-color-2);
}

.separator {
  color: var(--n-text-color-3);
  margin: 0 8px;
}

.date-picker {
  width: 140px;
}

.status-select {
  width: 120px;
}

/* --- 表格区域 --- */
.table-container {
  /* 可以添加一些特定间距 */
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-grid {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-inputs .n-space {
    flex-wrap: wrap;
  }
  
  .date-picker, .status-select {
    width: 100% !important;
  }
  
  .filter-actions {
    display: flex;
    justify-content: flex-end;
  }
}
</style>