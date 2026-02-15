<template>
  <div class="ai-gen-history">
    <n-card title="AI生成记录" class="history-card">
      <!-- 搜索和筛选 -->
      <n-space class="search-section" align="center">
        <n-input
          v-model:value="searchKeyword"
          placeholder="搜索记录..."
          style="width: 200px"
        />
        <n-select
          v-model:value="statusFilter"
          :options="statusOptions"
          placeholder="状态筛选"
          style="width: 120px"
        />
        <n-button @click="loadHistory" :loading="loading" circle>
          <template #icon>
            <n-icon><Refresh /></n-icon>
          </template>
        </n-button>
      </n-space>

      <!-- 记录列表 -->
      <div class="history-list" v-if="!loading">
        <n-empty v-if="filteredHistory.length === 0" description="暂无历史记录">
          <template #extra>
            <n-text depth="3">请先使用AI目标生成器创建一些记录</n-text>
          </template>
        </n-empty>

        <div v-else class="records-container">
          <div
            v-for="record in paginatedHistory"
            :key="record.id"
            class="record-item"
            :class="{ expired: isExpired(record) }"
          >
            <div class="record-header">
              <h4 class="record-title">{{ record.goalTitle || '未命名目标' }}</h4>
              <div class="record-actions">
                <n-tag
                  :type="getStatusTagType(record.status)"
                  size="small"
                  class="status-tag"
                >
                  {{ getStatusText(record.status) }}
                </n-tag>
                <n-dropdown
                  :options="getActionOptions(record)"
                  @select="(key) => handleAction(key, record)"
                >
                  <n-button quaternary circle size="small">
                    <template #icon>
                      <n-icon><More /></n-icon>
                    </template>
                  </n-button>
                </n-dropdown>
              </div>
            </div>

            <p class="record-description">{{ record.userInput }}</p>
            
            <div class="record-meta">
              <span class="record-time">
                {{ formatDate(record.createdAt) }}
              </span>
              <span v-if="!isExpired(record)" class="record-remaining">
                剩余 {{ getRemainingDays(record.expireAt) }} 天
              </span>
              <span v-else class="record-expired">已过期</span>
            </div>

            <!-- 子目标预览 -->
            <div v-if="record.childGoals && record.childGoals.length > 0" class="subgoals-preview">
              <n-collapse>
                <n-collapse-item title="查看详细步骤" name="1">
                  <n-list bordered>
                    <n-list-item
                      v-for="(subGoal, index) in record.childGoals"
                      :key="index"
                    >
                      <n-thing
                        :title="`步骤 ${index + 1}`"
                        :description="subGoal.message"
                      />
                    </n-list-item>
                  </n-list>
                </n-collapse-item>
              </n-collapse>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <n-space v-if="filteredHistory.length > pageSize" justify="center" class="pagination-section">
          <n-pagination
            v-model:page="currentPage"
            :page-count="Math.ceil(filteredHistory.length / pageSize)"
            :page-size="pageSize"
            show-size-picker
            :page-sizes="[5, 10, 20]"
            @update:page="currentPage = $event"
            @update:page-size="pageSize = $event"
          />
        </n-space>
      </div>

      <!-- 加载状态 -->
      <div v-else class="loading-state">
        <n-skeleton text :repeat="3" />
        <n-skeleton text :repeat="3" />
        <n-skeleton text :repeat="3" />
      </div>
    </n-card>

    <!-- 使用AIGoalConfirmation组件显示确认框 -->
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import {
  NCard,
  NInput,
  NButton,
  NIcon,
  NSelect,
  NDropdown,
  NTag,
  NEmpty,
  NText,
  NCollapse,
  NCollapseItem,
  NList,
  NListItem,
  NThing,
  NModal,
  NForm,
  NFormItem,
  NSpace,
  NPagination,
  NSkeleton,
  useMessage
} from 'naive-ui';
import { Refresh } from '@vicons/ionicons5';
import {useStore} from 'vuex'
import {
  postM,
  getMPaths,
  uploadFile,
  baseURL,
  isSuccess,
  getM
} from '@/utils/request';

// Props
const props = defineProps({
  // 可以传入特定的用户ID，默认为当前用户
  userId: {
    type: String,
    default: null
  }
});
const store = useStore()

// Emits
const emit = defineEmits(['record-used', 'show-confirmation']);

// 响应式数据
const history = ref([]);
const loading = ref(false);
const searchKeyword = ref('');
const statusFilter = ref(null);
const currentPage = ref(1);
const pageSize = ref(10);
const showConfirmModal = ref(false);
const selectedRecord = ref(null);
const message = useMessage();

// 状态选项
const statusOptions = [
  { label: '全部', value: null },
  { label: '待确认', value: 'pending' },
  { label: '已确认', value: 'confirmed' },
  { label: '已删除', value: 'deleted' }
];

// 计算属性：过滤后的历史记录
const filteredHistory = computed(() => {
  let result = [...history.value];
  
  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(record => 
      record.goalTitle?.toLowerCase().includes(keyword) ||
      record.userInput?.toLowerCase().includes(keyword)
    );
  }
  
  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(record => record.status === statusFilter.value);
  }
  
  return result;
});

// 计算属性：分页后的历史记录
const paginatedHistory = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredHistory.value.slice(start, end);
});

// 方法：加载历史记录
const loadHistory = async () => {
  loading.value = true;
  try {
    // const params = new URLSearchParams();
    // params.append('page', '0');
    // params.append('size', '100');
    // params.append('username',store.state.user.username) // 加载较多数据，前端分页
    let data={
      page:0,
      size:100,
      username:store.state.user.username
    }
    const response = await getM('/api/aiGen/list',data);
    
    if (isSuccess(response)) {
      history.value = response.data.data.records || [];
    } else {
      message.error(response.data.msg || '加载历史记录失败');
    }
  } catch (error) {
    console.error('加载历史记录失败:', error);
    message.error('加载历史记录失败');
  } finally {
    loading.value = false;
  }
};

// 方法：获取操作选项
const getActionOptions = (record) => {
  const options = [];
  
  // 确认使用（仅限未过期的待确认记录）
  if (record.status === 'pending') {
    options.push({ label: '确认使用', key: 'confirm' });
  }
  
  // 分享
  options.push({ label: '生成分享链接', key: 'share' });
  
  // 查看详情
  // options.push({ label: '查看详情', key: 'view' });
  
  // 删除
  if (record.status === 'pending') {
    options.push({ label: '删除', key: 'delete' });
  }
  
  return options;
};

// 方法：处理操作选择
const handleAction = async (action, record) => {
  selectedRecord.value = record;
  
  switch (action) {
    case 'confirm':
     await confirmRecord()
      break;
    case 'share':
      await shareRecord(record);
      break;
    case 'view':
      viewRecord(record);
      break;
    case 'delete':
      await deleteRecord(record);
      break;
  }
};

// 方法：确认使用记录
const confirmRecord = async () => {
  if (!selectedRecord.value) return;
  
  try {
    const response = await postM(`/api/aiGen/confirm/${selectedRecord.value.id}`, { 
      username: store.state.user.username 
    });
    
    if (isSuccess(response)) {
      message.success('记录已确认');
      showConfirmModal.value = false;
      loadHistory();
      emit('record-used', selectedRecord.value);
    } else {
      message.error(response.data.msg || '确认失败');
    }
  } catch (error) {
    console.error('确认记录失败:', error);
    message.error('确认失败');
  }
  // 显示确认模态框传递数据给AIGoalConfirmation组件
  showConfirmModal.value = false;
  
  // 触发父组件显示AIGoalConfirmation组件
  emit('show-confirmation', selectedRecord.value);
};

// 方法：分享记录
const shareRecord = async (record) => {
  try {
    const response = await getM(`/api/aiGen/share/${record.id}`, { 
      username: store.state.user.username 
    });
    
    if (isSuccess(response)) {
      const shareToken = response.data.data.shareToken;
      const shareUrl = `${window.location.origin}/#/share/${shareToken}`;
      
      // 复制到剪贴板
      if (navigator.clipboard) {
        await navigator.clipboard.writeText(shareUrl);
        message.success('分享链接已复制到剪贴板');
      } else {
        // 降级方案：显示对话框
        message.info(`分享链接：${shareUrl}`);
      }
    } else {
      message.error(response.data.msg || '生成分享链接失败');
    }
  } catch (error) {
    console.error('生成分享链接失败:', error);
    message.error('生成分享链接失败');
  }
};

// 方法：查看记录详情
const viewRecord = (record) => {
  // 这里可以打开详情模态框或跳转到详情页面
  message.info('查看详情功能待实现');
};

// 方法：删除记录
const deleteRecord = async (record) => {
  try {
    // 这里需要修改后端API来支持DELETE请求传递username参数
    // 暂时使用一个通用的删除方法，或者您可以修改后端来处理这个问题
    const response = await postM(`/api/aiGen/delete/${record.id}`, { 
      username: store.state.user.username 
    });
    
    if (isSuccess(response)) {
      message.success('记录已删除');
      loadHistory();
    } else {
      message.error(response.data.msg || '删除失败');
    }
  } catch (error) {
    console.error('删除记录失败:', error);
    message.error('删除失败');
  }
};

// 方法：检查记录是否过期
const isExpired = (record) => {
  return record.expireAt && Date.now() > record.expireAt;
};

// 方法：获取剩余天数
const getRemainingDays = (expireAt) => {
  if (!expireAt) return 0;
  const diff = expireAt - Date.now();
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
};

// 方法：格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 方法：获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    pending: 'warning',
    confirmed: 'success',
    deleted: 'error'
  };
  return typeMap[status] || 'default';
};

// 方法：获取状态文本
const getStatusText = (status) => {
  const textMap = {
    pending: '待确认',
    confirmed: '已确认',
    deleted: '已删除'
  };
  return textMap[status] || '未知';
};

// 生命周期
onMounted(() => {
  loadHistory();
});
</script>

<style scoped>
.ai-gen-history {
  height: 100%;
}

.history-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.search-section {
  margin-bottom: 20px;
}

.history-list {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.records-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-item {
  border: 1px solid var(--border-color);
  background-color: var(--card-color);
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
}

.record-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: var(--n-color-hover, #81c6831f);
}

.record-item.expired {
  opacity: 0.6;
  background-color: var(--n-color-modal, #f5f5f5);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.record-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.record-description {
  color: var(--text-color-secondary);
  margin: 8px 0;
  line-height: 1.5;
}

.record-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.record-remaining {
  color: #e6a23c;
}

.record-expired {
  color: #f56c6c;
}

.subgoals-preview {
  margin-top: 12px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.loading-state {
  padding: 20px;
}

/* 暗色主题适配 */
.dark .record-item {
  border-color: #333;
  background-color: #1a1a1a;
}

.dark .record-title {
  color: #e0e0e0;
}

.dark .record-description {
  color: #a0a0a0;
}

.dark .record-meta {
  color: #666;
}
</style>