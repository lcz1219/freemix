<template>
  <div class="update-log-manager">
    <n-card title="系统更新管理" class="manager-card">
      <template #header-extra>
        <n-button type="primary" @click="showCreateModal = true">
          <template #icon><n-icon><AddOutline /></n-icon></template>
          发布新版本
        </n-button>
      </template>

      <n-data-table
        :columns="columns"
        :data="updateLogs"
        :loading="loading"
        :pagination="pagination"
      />
    </n-card>

    <!-- 创建/编辑模态框 -->
    <n-modal v-model:show="showCreateModal" preset="card" title="发布新版本" style="width: 800px">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="版本号" path="version">
          <n-input v-model:value="formModel.version" placeholder="例如: 1.0.0" />
        </n-form-item>
        <n-form-item label="重要程度" path="importance">
          <n-select v-model:value="formModel.importance" :options="importanceOptions" />
        </n-form-item>
        <n-form-item label="影响范围" path="scope">
          <n-input v-model:value="formModel.scope" placeholder="例如: 全体用户, 移动端模块" />
        </n-form-item>
        <n-form-item label="更新内容" path="content">
          <n-input
            v-model:value="formModel.content"
            type="textarea"
            placeholder="支持 Markdown 格式"
            :autosize="{ minRows: 5, maxRows: 15 }"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="modal-actions">
          <n-button @click="showCreateModal = false">取消</n-button>
          <n-button type="primary" @click="handleCreate" :loading="submitting">发布</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue';
import { 
  NButton, NTag, useMessage, NSpace, NCard, NDataTable, 
  NModal, NForm, NFormItem, NInput, NSelect, NIcon 
} from 'naive-ui';
import { AddOutline } from '@vicons/ionicons5';
import { postM, getM,isSuccess } from '@/utils/request';
import { useStore } from 'vuex';

const message = useMessage();
const store = useStore();
const loading = ref(false);
const submitting = ref(false);
const showCreateModal = ref(false);
const updateLogs = ref([]);

const formModel = ref({
  version: '',
  importance: 'medium',
  scope: '',
  content: ''
});

const rules = {
  version: { required: true, message: '请输入版本号', trigger: 'blur' },
  content: { required: true, message: '请输入更新内容', trigger: 'blur' }
};

const importanceOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' }
];

const columns = [
  { title: '版本', key: 'version', width: 100 },
  { 
    title: '发布时间', 
    key: 'updateTime',
    render: (row) => new Date(row.updateTime).toLocaleString()
  },
  { 
    title: '重要程度', 
    key: 'importance',
    render: (row) => {
      const types = { low: 'info', medium: 'warning', high: 'error' };
      const labels = { low: '低', medium: '中', high: '高' };
      return h(NTag, { type: types[row.importance] }, { default: () => labels[row.importance] });
    }
  },
  { title: '影响范围', key: 'scope' },
  { 
    title: '状态', 
    key: 'published',
    render: (row) => h(NTag, { type: row.published ? 'success' : 'default' }, { default: () => row.published ? '已发布' : '草稿' })
  },
  {
    title: '操作',
    key: 'actions',
    render: (row) => {
      return h(NSpace, null, {
        default: () => [
          !row.published && h(NButton, { 
            size: 'small', 
            type: 'primary',
            onClick: () => handlePublish(row)
          }, { default: () => '发布' }),
          h(NButton, { 
            size: 'small', 
            type: 'error',
            onClick: () => handleDelete(row)
          }, { default: () => '删除' })
        ]
      });
    }
  }
];

const pagination = { pageSize: 10 };

const fetchLogs = async () => {
  loading.value = true;
  try {
    const res = await getM('/api/updates/list');
    if (res.data.code === 200) {
      updateLogs.value = JSON.parse(JSON.stringify(res.data.data));
    let resq=  res.data.data[0];
  //  delete formModel.value.version
   delete resq.readUsers
   delete resq.createdAt
   delete resq.updateTime
   delete resq.published
   delete resq.id
   formModel.value=resq

    }
  } catch (e) {
    message.error('获取列表失败'+e);
  } finally {
    loading.value = false;
  }
};

const handleCreate = async () => {
  submitting.value = true;
  try {
    const username = store.state.user.username;
    const res = await postM(`/api/updates/create?username=${username}`, formModel.value);
    if (res.data.code === 200) {
      message.success('创建成功');
      showCreateModal.value = false;
      formModel.value = { version: '', importance: 'medium', scope: '', content: '' };
      fetchLogs();
    } else {
      message.error(res.data.msg || '创建失败');
    }
  } catch (e) {
    message.error('创建失败');
  } finally {
    submitting.value = false;
  }
};

const handlePublish = async (row) => {
  try {
    const username = store.state.user.username;
    const res = await postM(`/api/updates/publish/${row.id}?username=${username}`);
    if (res.data.code === 200) {
      message.success('发布成功');
      fetchLogs();
    }
  } catch (e) {
    message.error('发布失败');
  }
};

const handleDelete = async (row) => {
  try {
    console.log("删除更新日志", row);
    
    const username = store.state.user.username;
    const res = await getM(`/api/updates/${row.id}?username=${username}`);
    if (isSuccess(res)) {
      message.success('删除成功');
      fetchLogs();
    }
  } catch (e) {
    message.error('删除失败');
  }
};

onMounted(() => {
  fetchLogs();
});
</script>

<style scoped>
.update-log-manager {
  padding: 24px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
