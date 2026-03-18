<template>
  <!-- <n-loading-bar-provider> -->
  <n-layout :native-scrollbar="true" :class="isDark ? 'home-container' : 'home-container-light'">
    <common>
      <template #content>
        <n-layout-content :loading="loading" class="main-content-wrapper">
          <div class="main-content">
            <section class="page-header">
              <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">定时自动目标</h1>
              <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
                设置自动循环生成的目标，让系统助您养成良好习惯
              </p>
            </section>

            <section class="control-section">
              <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
                <n-space justify="space-between" align="center">
                  <n-button type="primary" @click="handleAdd">
                    <template #icon><n-icon><Add /></n-icon></template>
                    新建定时任务
                  </n-button>
                  <n-button @click="fetchData">
                    <template #icon><n-icon><RefreshOutline /></n-icon></template>
                    刷新
                  </n-button>
                </n-space>
              </n-card>
            </section>

            <section class="list-section" style="margin-top: 20px;">
              <n-grid :x-gap="20" :y-gap="20" cols="1 s:2 m:3 l:4" responsive="screen">
                <n-gi v-for="item in list" :key="item._id">
                  <n-card :class="isDark ? 'feature-card' : 'feature-card-light'" hoverable>
                    <template #header>
                      <n-space justify="space-between" align="center">
                        <span style="font-weight: bold;">{{ item.title }}</span>
                        <n-tag :type="getRecurrenceTagType(item.recurrenceType)" size="small">
                          {{ getRecurrenceLabel(item.recurrenceType) }}
                        </n-tag>
                      </n-space>
                    </template>
                    
                    <p class="description">{{ item.description || '无描述' }}</p>
                    
                    <div class="meta-info">
                      <div class="info-item">
                        <n-text depth="3">下次执行: </n-text>
                        <n-text>{{ formatDate(item.nextExecutionTime) }}</n-text>
                      </div>
                      <div class="info-item">
                        <n-text depth="3">状态: </n-text>
                        <n-switch v-model:value="item.isActive" @update:value="(val) => handleToggle(item, val)" size="small" />
                      </div>
                    </div>

                    <template #action>
                      <n-space justify="end">
                        <n-button size="small" quaternary circle @click="handleEdit(item)">
                          <template #icon><n-icon><CreateOutline /></n-icon></template>
                        </n-button>
                        <n-button size="small" quaternary circle type="error" @click="handleDelete(item)">
                          <template #icon><n-icon><TrashOutline /></n-icon></template>
                        </n-button>
                      </n-space>
                    </template>
                  </n-card>
                </n-gi>
              </n-grid>
              <n-empty v-if="list.length === 0" description="还没有设置任何定时目标" style="margin-top: 100px;" />
            </section>
          </div>
        </n-layout-content>
      </template>
    </common>

    <!-- 编辑模态框 -->
    <n-modal v-model:show="showModal" preset="card" :title="form._id ? '编辑定时任务' : '新建定时任务'" style="width: 800px">
      <n-form :model="form" label-placement="left" label-width="100">
        <n-grid :gutter="24">
          <n-gi :span="24">
            <n-form-item label="标题" path="title">
              <n-input v-model:value="form.title" placeholder="任务标题 (例如: 每日健身)" />
            </n-form-item>
          </n-gi>
          <n-gi :span="24">
            <n-form-item label="描述" path="description">
              <n-input v-model:value="form.description" type="textarea" placeholder="任务模板描述" :autosize="{ minRows: 2, maxRows: 4 }" />
            </n-form-item>
          </n-gi>
          
          <n-gi :span="24">
            <n-form-item label="子目标模板">
              <n-dynamic-input v-model:value="form.childGoals" placeholder="自动生成的子目标内容" :min="0" :max="10" show-sort-button @create="onCreateChildGoal">
                <template v-slot="{ value }">
                  <div style="display: flex; align-items: center; width: 100%">
                    <n-input v-model:value="value.message" placeholder="子目标内容" />
                  </div>
                </template>
                <template #create-button-default>
                  添加子目标模板
                </template>
              </n-dynamic-input>
            </n-form-item>
          </n-gi>

          <n-gi :span="12">
            <n-form-item label="循环类型">
              <n-select v-model:value="form.recurrenceType" :options="recurrenceOptions" />
            </n-form-item>
          </n-gi>
          <n-gi :span="24" v-if="form.recurrenceType === 'cron'">
            <n-form-item label="Cron 表达式">
              <n-input-group>
                <n-input v-model:value="form.cronExpression" placeholder="例如：0 0 12 * * ?" style="flex: 1;" />
                <n-popselect v-model:value="form.cronExpression" :options="cronTemplates" scrollable trigger="click">
                  <n-button type="primary" ghost>
                    <template #icon><n-icon><ClipboardOutline /></n-icon></template>
                    常用模板
                  </n-button>
                </n-popselect>
              </n-input-group>
              <template #feedback>
                <div v-if="cronNextExecutions.length > 0" class="cron-feedback-success">
                  <div class="cron-rule-label">
                    <n-icon :component="ClipboardOutline" />
                    {{ getCronTemplateLabel(form.cronExpression) }}
                  </div>
                  <div class="cron-execution-times">
                    <div class="execution-times-title">最近 5 次执行时间:</div>
                    <ul class="execution-times-list">
                      <li v-for="(time, idx) in cronNextExecutions" :key="idx" class="execution-time-item">
                        <n-tag type="success" size="small" round>{{ formatDate(time) }}</n-tag>
                      </li>
                    </ul>
                  </div>
                </div>
                <div v-else-if="form.cronExpression && !isCronValid" class="cron-feedback-error">
                  <n-icon :component="CloseCircleOutline" />
                  无效的 Cron 表达式
                </div>
                <div v-else class="cron-feedback-hint">
                  <n-icon :component="HelpCircleOutline" />
                  支持 Spring Cron 格式: 秒 分 时 日 月 周
                </div>
              </template>
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="优先级">
              <n-select v-model:value="form.level" :options="levelOptions" />
            </n-form-item>
          </n-gi>
          
          <n-gi :span="12">
            <n-form-item label="分类标签">
              <n-select v-model:value="form.tags" multiple filterable tag placeholder="选择或输入标签" :options="tagOptions" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="可见性">
              <n-radio-group v-model:value="form.isPublic" name="visibility">
                <n-radio-button :value="false" label="私密" />
                <n-radio-button :value="true" label="公开" />
              </n-radio-group>
            </n-form-item>
          </n-gi>
        </n-grid>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">保存模板</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-layout>
  <!-- </n-loading-bar-provider> -->
</template>

<script setup lang="ts">
import { ref, onMounted, inject, watch } from 'vue';
import { 
  Add, RefreshOutline, CreateOutline, TrashOutline,
  ClipboardOutline, CloseCircleOutline, HelpCircleOutline
} from '@vicons/ionicons5';
import common from '@/views/common.vue';
import { getM, postM, isSuccess } from '@/utils/request';
import {
  NLayout,
  NLoadingBarProvider,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NButton,
  NIcon,
  NInput,
  NInputGroup,
  NSwitch,
  NCard,
  NTimeline,
  NTimelineItem,
  NTag,
  NText,
  NProgress,
  NAvatar,
  NModal,
  NSteps,
  NStep,
  useMessage,
  NMenu,
  NImage,
  NAlert,
  NTabs,
  NDataTable,
  NTabPane,
  NGrid,
  NGi,
  NGridItem,
  NSpace,
  NSelect,
  NPopselect,
  NCollapse,
  NForm,
  NFormItem,
  NDynamicInput,
  NRadioGroup,
  NRadioButton,
  NEmpty,
  NCollapseItem,
  NTable,
  NPagination,
  useLoadingBar,
  useDialog
} from 'naive-ui';
const loadingBar = useLoadingBar()
const form = ref({
  _id: null,
  title: '',
  description: '',
  recurrenceType: 'daily',
  cronExpression: '0 0 0 * * ?',
  level: 'medium',
  tags: [],
  childGoals: [],
  isPublic: false,
  isActive: true
});
const isDark = inject('isDark', ref(true));
const message = useMessage();
const dialog = useDialog();

const list = ref([]);
const loading = ref(false);
const showModal = ref(false);
const submitting = ref(false);

const cronNextExecutions = ref([]);
const isCronValid = ref(true);

const cronTemplates = [
  { label: '每分钟', value: '0 * * * * ?' },
  { label: '每小时(整点)', value: '0 0 * * * ?' },
  { label: '每天(凌晨0点)', value: '0 0 0 * * ?' },
  { label: '每天(中午12点)', value: '0 0 12 * * ?' },
  { label: '每周一(凌晨0点)', value: '0 0 0 ? * MON' },
  { label: '每月1号(凌晨0点)', value: '0 0 0 1 * ?' },
  { label: '每工作日(早上9点)', value: '0 0 9 ? * MON-FRI' }
];

const getCronTemplateLabel = (cron) => {
  const template = cronTemplates.find(t => t.value === cron);
  return template ? `定时规则: ${template.label}` : '自定义 Cron 规则';
};

watch(() => form.value.cronExpression, async (newVal) => {
  if (!newVal || form.value.recurrenceType !== 'cron') {
    cronNextExecutions.value = [];
    return;
  }
  try {
    const res = await getM(`/previewCron?cron=${encodeURIComponent(newVal)}`);
    if (res.data.operSucc) {
      cronNextExecutions.value = res.data.data;
      isCronValid.value = true;
    } else {
      cronNextExecutions.value = [];
      isCronValid.value = false;
    }
  } catch (error) {
    cronNextExecutions.value = [];
    isCronValid.value = false;
  }
}, { immediate: true });

watch(() => form.value.recurrenceType, (newVal) => {
  if (newVal !== 'cron') {
    cronNextExecutions.value = [];
  }
});

const tagOptions = ref([]);
const getTags = async () => {
  const res = await getM('getTags');
  if (isSuccess(res)) {
    tagOptions.value = res.data.data.map((tag) => ({ label: tag, value: tag }));
  }
}



const onCreateChildGoal = () => {
  return {
    message: '',
    finish: false,
    finishTime: '',
  }
}

const recurrenceOptions = [
  { label: '每天', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' },
  { label: 'Cron 定制', value: 'cron' }
];

const levelOptions = [
  { label: '高优', value: 'high' },
  { label: '中优', value: 'medium' },
  { label: '低优', value: 'low' }
];

const fetchData = async () => {
  loadingBar.start()
  loading.value = true;
  try {
    const res = await getM('/getRecurringGoals');
    if (res.data.operSucc) {
      list.value = res.data.data;
    }
  } catch (error) {
    message.error('获取数据失败');
  } finally {
    loading.value = false;
    loadingBar.finish()
  }
};

const handleAdd = () => {
  form.value = {
    _id: null,
    title: '',
    description: '',
    recurrenceType: 'daily',
    cronExpression: '0 0 0 * * ?',
    level: 'medium',
    tags: [],
    childGoals: [
      { message: '1. ', finish: false, finishTime: '' },
      { message: '2. ', finish: false, finishTime: '' },
      { message: '3. ', finish: false, finishTime: '' }
    ],
    isPublic: false,
    isActive: true
  };
  showModal.value = true;
};

const handleEdit = (item) => {
  form.value = { 
    ...item,
    tags: item.tags || [],
    childGoals: item.childGoals || [],
    isPublic: item.isPublic || false,
    cronExpression: item.cronExpression || '0 0 0 * * ?'
  };
  showModal.value = true;
};

const handleSubmit = async () => {
  if (!form.value.title) {
    message.warning('请输入标题');
    return;
  }
  
  // 过滤空的子目标
  const childGoals = form.value.childGoals
    .filter(g => g.message && g.message.trim() !== '')
    .map(g => ({
      message: g.message,
      finish: false,
      finishTime: ''
    }));
    
  const submitData = {
    ...form.value,
    childGoals
  };

  submitting.value = true;
  try {
    const res = await postM('/editRecurringGoal', submitData);
    if (res.data.operSucc) {
      message.success('保存成功');
      showModal.value = false;
      fetchData();
    }
  } catch (error) {
    message.error('保存失败');
  } finally {
    submitting.value = false;
  }
};

const handleToggle = async (item, val) => {
  try {
    const res = await postM(`/toggleRecurringGoal?id=${item._id}&active=${val}`);
    if (res.data.operSucc) {
      message.success(val ? '已启用' : '已停用');
    }
  } catch (error) {
    item.isActive = !val; // 恢复状态
    message.error('操作失败');
  }
};

const handleDelete = (item) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除定时任务 "${item.title}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await postM(`/deleteRecurringGoal?id=${item._id}`);
        if (res.data.operSucc) {
          message.success('已删除');
          fetchData();
        }
      } catch (error) {
        message.error('删除失败');
      }
    }
  });
};

const getRecurrenceLabel = (type) => {
  const option = recurrenceOptions.find(o => o.value === type);
  return option ? option.label : type;
};

const getRecurrenceTagType = (type) => {
  if (type === 'daily') return 'info';
  if (type === 'weekly') return 'warning';
  if (type === 'monthly') return 'error';
  if (type === 'cron') return 'success';
  return 'default';
};

const formatDate = (date) => {
  if (!date) return '未执行';
  return new Date(date).toLocaleString();
};

onMounted(() => {
  fetchData();
  getTags();
});
</script>

<style scoped>
.main-content-wrapper {
  padding: 24px;
}
.hero-subtitle{
  margin-bottom: 15px;
  margin-top: 5px;
}
.hero-subtitle-light{
  margin-bottom: 15px;
  margin-top: 5px;
}
.description {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.meta-info {
  font-size: 12px;
  border-top: 1px solid rgba(128, 128, 128, 0.1);
  padding-top: 8px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

/* Cron 表达式反馈样式 */
.cron-feedback-success {
  margin-top: 12px;
  padding: 12px;
  background: linear-gradient(135deg, rgb(29 33 31 / 10%) 0%, rgba(65, 184, 131, 0.05) 100%);
  border-radius: 8px;
  border-left: 3px solid #41b883;
}

.cron-rule-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #41b883;
  font-size: 14px;
  margin-bottom: 10px;
}

.cron-rule-label .n-icon {
  font-size: 16px;
}

.cron-execution-times {
  margin-top: 8px;
}

.execution-times-title {
  font-weight: 600;
  color: #dfd7d7;
  font-size: 13px;
  margin-bottom: 8px;
}

.execution-times-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.execution-time-item {
  display: inline-block;
}

.execution-time-item .n-tag {
  font-size: 12px;
  padding: 4px 12px;
}

.cron-feedback-error {
  margin-top: 12px;
  padding: 10px 12px;
  background: linear-gradient(135deg, rgba(208, 48, 80, 0.1) 0%, rgba(208, 48, 80, 0.05) 100%);
  border-radius: 8px;
  border-left: 3px solid #d03050;
  color: #d03050;
  font-weight: 600;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cron-feedback-error .n-icon {
  font-size: 16px;
}

.cron-feedback-hint {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 12px;
  opacity: 0.8;
}

.cron-feedback-hint .n-icon {
  font-size: 14px;
}
</style>
