<template>
  <n-layout >
    <!-- 顶部导航栏 -->
    <NavBar active-tab="login-log" />

    <!-- 主内容区域 -->
    <n-card class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>登录日志</h1>
        <p>查看您的账户登录历史记录</p>
      </div>

      <!-- 筛选条件 -->
      <n-layout class="filter-section">
        <n-form :model="filter" class="filter-form">
          <n-space>
            <n-form-item label="开始日期" path="startDate">
              <n-date-picker
                v-model:value="filter.startDate"
                type="date"
                placeholder="选择开始日期"
                format="yyyy-MM-dd"
                class="form-input"
              />
            </n-form-item>

            <n-form-item label="结束日期" path="endDate">
              <n-date-picker
                v-model:value="filter.endDate"
                type="date"
                placeholder="选择结束日期"
                format="yyyy-MM-dd"
                class="form-input"
              />
            </n-form-item>

            <n-form-item label="状态" path="status">
              <n-select v-model:value="filter.status" placeholder="请选择状态" class="form-input">
                <n-option label="全部" value="all" />
                <n-option label="成功" value="success" />
                <n-option label="失败" value="failed" />
              </n-select>
            </n-form-item>
<n-form-item label="" path="status">
            <n-space style="margin: auto;">
              <n-button type="primary" @click="searchLoginLogs">
                <n-icon><SearchIcon /></n-icon>
                <span>搜索</span>
              </n-button>
              <n-button @click="resetFilter">
                <!-- <n-icon><RefreshIcon /></n-icon> -->
                <span>重置</span>
              </n-button>
            </n-space>
            </n-form-item>
          </n-space>
        </n-form>
      </n-layout>

      <!-- 日志列表 -->
      <div class="login-log-list">
        <!-- 表格 -->
        <n-data-table
          v-if="!loading"
          :data="loginLogs"
          :columns="columns"
          :pagination="pagination"
          :loading="loading"
          :remote="true"
          :row-key="(row) => row.id || row.loginTime"
          class="login-log-table"
        />

        <!-- 加载状态 -->
        <n-spin v-else>
          <div style="height: 400px; width: 100%; display: flex; align-items: center; justify-content: center;">
            加载中...
          </div>
        </n-spin>

        <!-- 空状态 -->
        <n-empty
          v-if="!loading && loginLogs.length === 0"
          description="暂无登录记录"
          class="empty-state"
        />
      </div>
       </n-card>
  
      </n-layout>
  
</template>

<script setup>
import NavBar from '@/components/NavBar.vue'
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
  NEmpty, 
  NSpace,
  NLayout,
  NTag,
  NIcon,
  NSpin
} from 'naive-ui'

const userStore = useStore()
const currentUser = computed(() => {
  // console.log("userStore.user",useStore.userId);
  
 return userStore.state.user
})

// 响应式数据
const loginLogs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 筛选条件
const filter = ref({
  startDate: null,
  endDate: null,
  status: 'all'
})

// 表格列配置
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
    width: 140
  },
  {
    title: '设备',
    key: 'deviceType',
    width: 120
  },
  {
    title: '浏览器',
    key: 'browser',
    width: 160
  },
  {
    title: '操作系统',
    key: 'os',
    width: 160
  },
  {
    title: '状态',
    key: 'loginSuccess',
    width: 100,
    render(row) {
      return h(NTag, {
        type: row.loginSuccess ? 'success' : 'error'
      }, () => (row.loginSuccess ? '成功' : '失败'))
    }
  },
  {
    title: '错误信息',
    key: 'errorMessage',
    ellipsis: true,
    render(row) {
      return row.errorMessage || '-'
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
  showTotal: (total) => `共 ${total} 条数据`,
  onUpdatePage: (page) => {
    currentPage.value = page
    fetchLoginLogs()
  },
  onUpdatePageSize: (size) => {
    pageSize.value = size
    currentPage.value = 1
    fetchLoginLogs()
  }
};

// 图标组件
const SearchIcon = Search;
// const RefreshIcon = RefreshLeft;

// 深色模式状态
const isDark = computed(() => {
  // 如果项目中有深色模式状态，可以在这里获取
  return false
})

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date)
}

// 搜索登录日志
const searchLoginLogs = async () => {
  currentPage.value = 1
  await fetchLoginLogs()
}

// 重置筛选条件
const resetFilter = () => {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  
  filter.value = {
    startDate: startDate,
    endDate: endDate,
    status: 'all'
  }
  currentPage.value = 1
  fetchLoginLogs()
}

// 分页处理已集成到pagination配置中

// 获取登录日志
const fetchLoginLogs = async () => {
  if (!currentUser.value?.id) {
    console.error('用户未登录')
    return
  }

  loading.value = true
  try {
    // 构建请求参数
    const params = {
      userId: currentUser.value.id,
      page: currentPage.value,
      size: pageSize.value
    }

    // 根据筛选条件调整API调用
    let response
    if (filter.value.startDate && filter.value.endDate) {
      // 按日期范围查询 - 将Date对象转换为API需要的字符串格式
      response = await getM('api/login-log/date-range', {
        ...params,
        startDate: filter.value.startDate.toISOString().split('T')[0],
        endDate: filter.value.endDate.toISOString().split('T')[0]
      })
    } else if (currentUser.value?.id) {
      // 按用户ID查询
      response = await getM('api/login-log/user', params)
    } else {
      // 按用户ID查询
      response = await getM('api/login-log/user', params)
    }

    if (response.data && response.data.code === 200) {
      // 应用状态筛选
      let logs = response.data.data || []
      if (filter.value.status !== 'all') {
        logs = logs.filter(log =>
          filter.value.status === 'success' ? log.loginSuccess : !log.loginSuccess
        )
      }
      loginLogs.value = logs
      total.value = logs.length > 0 ? (logs.length === pageSize.value ? logs.length + 1 : logs.length) : 0
    } else {
      console.error('获取登录日志失败', response.data?.message)
      loginLogs.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取登录日志时发生错误', error)
    loginLogs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取登录日志
onMounted(() => {
  // 设置默认的日期范围（最近30天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)

  // 对于Naive UI的日期选择器，直接使用Date对象
  filter.value.startDate = startDate
  filter.value.endDate = endDate

  fetchLoginLogs()
})
</script>

<style scoped>
.login-log-container {
  min-height: 100vh;
  padding: 20px;
  background-color: #f5f5f5;
}

.login-log-container.dark {
  background-color: #1a1a1a;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px 0;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  /* color: #333; */
}

.page-header p {
  margin: 10px 0 0;
  /* color: #666; */
}

.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  /* background-color: #fafafa; */
  border-radius: 4px;
}

.filter-form {
  display: flex;
  align-items: center;
}

.form-input {
  min-width: 200px;
}

.login-log-list {
  margin-bottom: 20px;
}

.empty-state {
  margin: 40px 0;
}

.login-log-table {
  margin-bottom: 10px;
}

/* 适配深色模式 */
.login-log-container.dark .page-header h1 {
  color: #e0e0e0;
}

.login-log-container.dark .page-header p {
  color: #aaa;
}

.login-log-container.dark .filter-section {
  /* background-color: #2a2a2a; */
}
</style>