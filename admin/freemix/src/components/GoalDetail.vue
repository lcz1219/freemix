<template>
  <n-modal v-model:show="showModal" preset="card" :style="{ width: '80%',height:'85vh',overflow: 'auto', maxHeight: '90vh'}" title="目标详情" :bordered="false"
    :segmented="{ content: true, footer: 'soft' }" draggable :on-after-leave="closeModal" >
    <template #header-extra>
      <n-space>
        <n-button quaternary circle @click="copyGoal">
          <template #icon>
           <n-icon :component="Copy" />
          </template>
        </n-button>
        <n-button quaternary circle @click="editGoal" v-if="!isEditing">
          <template #icon>
            
             <n-icon :component="Pencil" />
          </template>
        </n-button>
        <n-button quaternary circle @click="()=>isEditing=false" v-else>
          <template #icon>
            
             <n-icon :component="Eye" />
          </template>
        </n-button>
      </n-space>
    </template>

    <div v-if="!isEditing" class="view-mode-container">
      <n-card class="goal-card" :bordered="false">
        <n-space vertical size="large">
          <!-- 标题区域 -->
          <div class="goal-header">
            <n-h2 style="margin: 0;">{{ goal.title }}</n-h2>
            <n-space align="center">
              <n-avatar circle size="small" :src="avatarUrl" />
              <n-text depth="3">{{ goal.owner }}</n-text>
            </n-space>
          </div>
          
          <!-- 描述区域 -->
          <n-p v-if="goal.description" class="goal-description">
            {{ goal.description }}
          </n-p>
          
          <!-- 元信息区域 -->
          <div class="meta-grid">
            <div class="meta-item">
              <n-text depth="3" class="meta-label">截止日期</n-text>
              <div class="meta-content">
                <n-icon :component="CalendarOutline" color="#18a058" />
                <n-text>{{ goal.deadlineString }}</n-text>
              </div>
            </div>
            
            <div class="meta-item">
              <n-text depth="3" class="meta-label">优先级</n-text>
              <div class="meta-content">
                <n-tag :type="getPriorityType(goal.level)" :bordered="false">
                  {{ getPriorityText(goal.level) }}
                </n-tag>
              </div>
            </div>
            
            <div class="meta-item">
              <n-text depth="3" class="meta-label">状态</n-text>
              <div class="meta-content">
                <n-tag :type="getStatusType(goal.status)" :bordered="false">
                  {{ getStatusText(goal.status) }}
                </n-tag>
              </div>
            </div>
            
            <div class="meta-item progress-item">
              <n-text depth="3" class="meta-label">进度</n-text>
              <div class="meta-content">
                <n-progress type="line" status="success" :percentage="goal.progress" :indicator-placement="'inside'" :height="24" processing />
                <n-text class="progress-text">{{ goal.progress }}%</n-text>
              </div>
            </div>
          </div>
          
          <!-- 标签区域 -->
          <div v-if="goal.tags && goal.tags.length > 0" class="meta-item">
            <n-text depth="3" class="meta-label">标签</n-text>
            <n-space>
              <n-tag v-for="tag in goal.tags" :key="tag" type="primary" :bordered="false" round>
                {{ tag }}
              </n-tag>
            </n-space>
          </div>
          
          <!-- 文件区域 -->
          <div v-if="goal.fileList && goal.fileList.length > 0" class="files-section">
            <n-text depth="3" class="meta-label">目标文件</n-text>
            <n-list hoverable clickable>
              <n-list-item v-for="(file, index) in goal.fileList" :key="index">
                <n-thing :title="file.originalFilename || file.name || '未命名文件'">
                  <template #header-extra>
                    <n-space>
                      <n-tag v-if="file.size" type="info" size="small" :bordered="false">
                        {{ formatFileSize(file.size) }}
                      </n-tag>
                      <n-button size="tiny" type="primary" text @click="getDownloadUrl(file)">
                        <template #icon>
                          <n-icon :component="CloudDownloadSharp" />
                        </template>
                      </n-button>
                    </n-space>
                  </template>
                </n-thing>
              </n-list-item>
            </n-list>
          </div>
          
          <!-- 子目标区域 -->
          <div v-if="goal.childGoals && goal.childGoals.length" class="subgoals-section">
            <div class="section-header">
              <n-h3 style="margin: 0;">子目标</n-h3>
              <n-tag type="info" :bordered="false">{{ goal.childGoals.length }}个子目标</n-tag>
            </div>
            <n-data-table 
              :columns="subGoalColumns" 
              :data="goal.childGoals" 
              :pagination="false"
              :striped="true"
              :single-line="false"
              class="subgoals-table"
              :max-height="300"
            />
          </div>
        </n-space>
      </n-card>
    </div>

    <div v-else>
      <!-- <n-modal :show="true"
    title="card 预设拖拽"
    preset="card"
    draggable
    :style="{ width: '800px',height:'800px' }"> -->
      <n-form ref="formRef" :model="editForm" :rules="formRules" label-placement="left" label-width="120"
        require-mark-placement="right-hanging" >
        <n-grid :gutter="24">
          <n-gi :span="12">
            <n-form-item label="目标标题" path="title">
              <n-input v-model:value="editForm.title" placeholder="请输入目标标题" maxlength="30" show-count />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="目标描述" path="description">
              <n-input v-model:value="editForm.description" placeholder="请输入目标描述" type="textarea"
                :autosize="{ minRows: 3, maxRows: 5 }" />
            </n-form-item>
          </n-gi>
         
          <n-gi :span="12">
            <n-form-item label="负责人" path="owner">
              <n-input v-model:value="editForm.owner" placeholder="请输入负责人姓名" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="截止日期" path="deadline">
              <n-date-picker v-model:value="editForm.deadline" type="date" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="优先级" path="priority">
              <n-select v-model:value="editForm.level" :options="priorityOptions" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="状态" path="status">
              <n-select v-model:value="editForm.status" :options="statusOptions" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="进度" path="progress">
             <n-progress type="circle" status="success" :percentage="editForm.progress" processing/>
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="标签" path="tags">
              <n-dynamic-tags v-model:value="editForm.tags" />
            </n-form-item>
          </n-gi>
           <n-gi :span="24">
            <n-form-item label="文件上传" path="description">
               <!-- <n-button type="primary" @click="() => fileupload = true">上传文件</n-button> -->
                          <!-- <n-modal v-model:show="fileupload" title="文件上传" preset="card" draggable -->
                            <!-- :style="{ width: '800px' }"> -->
    <!-- :fileList="goalForm.fileList" -->
                            <GeneralUpload @uploadSuccess="fileChange" 
                            @fileRemove="fileChange"
                            @uploadError="handleFileUploadError"
                            
                             :fileList="editForm.fileList"
                             />
                          <!-- </n-modal> -->
            </n-form-item>
          </n-gi>
          <n-gi :span="24">
            <n-form-item label="子目标" path="subGoals">
              <n-data-table :columns="editableSubGoalColumns" :data="editForm.childGoals" :pagination="false" />
            </n-form-item>
          </n-gi>
        </n-grid>
      </n-form>
      <!-- </n-modal> -->
    </div>

    <template #footer>
      <n-space justify="end">
        <n-button @click="closeModal">
          关闭
        </n-button>
        <n-button v-if="isEditing" type="primary" @click="saveGoal">
          保存
        </n-button>
      </n-space>
    </template>
  </n-modal>
  
  <!-- 子目标文件上传模态框 -->
  <n-modal v-model:show="showChildGoalUploadModal" preset="card" :style="{ width: '600px' }" title="上传子目标文件" draggable>
    <div v-if="currentChildGoalIndex !== -1">
      <p style="margin-bottom: 16px;">为子目标 "{{ editForm.childGoals[currentChildGoalIndex]?editForm.childGoals[currentChildGoalIndex].message:'' }}" 上传相关文件：</p>
      <GeneralUpload 
        @uploadSuccess="handleChildGoalFileChange"
        @fileRemove="handleChildGoalFileChange"
        @uploadError="handleFileUploadError"
        :fileList="childGoalFiles[currentChildGoalIndex] || []"
      />
    </div>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showChildGoalUploadModal = false">
          取消
        </n-button>
        <n-button type="primary" @click="saveChildGoalFiles">
          保存文件
        </n-button>
      </n-space>
    </template>
  </n-modal>
  
  <!-- 子目标文件查看模态框 -->
  <n-modal v-model:show="showChildGoalFilesModal" preset="card" :style="{ width: '700px' }" title="子目标文件" draggable>
    <div v-if="viewChildGoalIndex !== -1">
      <p style="margin-bottom: 16px; font-weight: bold;">
        子目标: "{{ isEditing ? (editForm.childGoals[viewChildGoalIndex] ? editForm.childGoals[viewChildGoalIndex].message : '') : (props.goal.childGoals && props.goal.childGoals[viewChildGoalIndex] ? props.goal.childGoals[viewChildGoalIndex].message : '') }}"
      </p>
      
      <div v-if="viewChildGoalFilesList.length > 0">
        <n-list hoverable clickable>
          <n-list-item v-for="(file, index) in viewChildGoalFilesList" :key="index">
            <n-thing :title="file.originalFilename || file.name || '未命名文件'">
              <template #header-extra>
                <n-space>
                  <n-button 
                    type="primary" 
                    size="small" 
                    tag="a" 
                    :href="file.url || file.fileUrl || `${baseURL()}/file/${file.name}`" 
                    target="_blank"
                  >
                    <n-icon size="16">
                      <Eye />
                    </n-icon>
                  </n-button>
                  <n-button 
                    type="primary" 
                    size="small"
                    @click="getDownloadUrl(file)"
                  >
                    <n-icon size="16">
                      <CloudDownloadSharp />
                    </n-icon>
                  </n-button>
                </n-space>
              </template>
              <template #description>
                <n-space>
                  <n-tag v-if="file.size" type="info" size="small">
                    {{ formatFileSize(file.size) }}
                  </n-tag>
                  <n-tag v-if="file.status" :type="file.status === 'finished' ? 'success' : 'warning'" size="small">
                    {{ file.status === 'finished' ? '上传完成' : '上传中' }}
                  </n-tag>
                </n-space>
              </template>
            </n-thing>
          </n-list-item>
        </n-list>
      </div>
      
      <n-empty v-else description="该子目标暂无文件" />
    </div>
    
    <template #footer>
      <n-space justify="end">
        <n-button @click="showChildGoalFilesModal = false">
          关闭
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import GeneralUpload from '@/components/GeneralUpload.vue';

import { ref, computed, watch,h,onMounted } from 'vue';
import { Pencil, Copy,Eye,CloudDownloadSharp,CalendarOutline,AlertCircleOutline,CheckmarkCircleOutline,BarChartOutline,TimeOutline,FlagOutline,CheckboxOutline,TrendingUpOutline } from '@vicons/ionicons5';
import { postM,isSuccess, baseURL } from '@/utils/request'
import { 
  NModal, 
  NButton, 
  NIcon, 
  NSpace, 
  NDescriptions, 
  NDescriptionsItem, 
  NTag, 
  NProgress, 
  NForm, 
  NFormItem, 
  NInput, 
  NDatePicker, 
  NSelect, 
  NSlider, 
  NDynamicTags, 
  NDataTable, 
  NList, 
  NListItem, 
  NThing, 
  NEmpty, 
  NGrid,
  NGi,
  NH2,
  NH3,
  NP,
  NText,
  NAvatar,
  NCard,
  useMessage 
} from 'naive-ui';
import { useStore } from 'vuex';
const store = useStore();

const props = defineProps({
  goal: {
    type: Object,
    default: () => ({})
  },
  show: {
    type: Boolean,
    default: false
  }
});
const fileupload=ref(false);
const avatarUrl = ref('');
onMounted(() => {
  const saveAvatarUrl = store.state.user.avatarUrl;
  console.log('saveAvatarUrl', saveAvatarUrl);

  if (saveAvatarUrl) {
    avatarUrl.value = `${baseURL()}${saveAvatarUrl}`;
  } else {
    // 默认头像
    avatarUrl.value = 'https://api.dicebear.com/7.x/miniavs/svg?seed=3';
  }
});
const getDownloadUrl = (file) => {
  console.log("生成下载URL:", file);
  if (!file) {
    return void 0;
  }
  
  // 对于本地文件对象，创建下载链接
  if (file.file instanceof File) {
    const url = URL.createObjectURL(file.file);
    const a = document.createElement('a');
    a.href = url;
    a.download = file.originalFilename || file.file.name || '未命名文件';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    return;
  }
  
  // 对于已上传的文件，使用name生成下载URL
  const downloadUrl = file.url || file.fileUrl || `${baseURL()}/file/${file.name}`;
  console.log("生成下载URL:", downloadUrl);
  
  const a = document.createElement('a');
  a.href = downloadUrl;
  a.download = file.originalFilename || file.name || '未命名文件';
  a.target = '_blank';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}
const emit = defineEmits(['update:show', 'update:goal', 'save']);

const message = useMessage();

const showModal = computed({
  get: () => props.show,
  set: (value) => {
    emit('update:show', value);
  }
});

const isEditing = ref(false);
const formRef = ref(null);
const fileChange = (file) => {
  console.log("fileChange",file);
  // 深拷贝editForm以避免直接修改引用
  let tmpEdit = JSON.parse(JSON.stringify(editForm.value));
  // 确保fileList字段存在
  if (!tmpEdit.fileList) {
    tmpEdit.fileList = [];
  }
  // 更新文件列表
  tmpEdit.fileList = file;
  // 更新editForm
  editForm.value = tmpEdit;
  console.log("更新后的文件列表:", editForm.value.fileList);
}
// 编辑表单数据
const editForm = ref({});

// 初始化表单数据
const initFormData = () => {
  // 深拷贝目标数据
  let goalData = JSON.parse(JSON.stringify(props.goal));
  
  // 处理日期数据，确保日期格式正确
  if (props.goal.deadline) {
    if (typeof props.goal.deadline === 'number') {
      // 如果是时间戳，转换为Date对象
      goalData.deadline = new Date(props.goal.deadline);
    } else if (typeof props.goal.deadline === 'string') {
      // 如果是字符串，尝试转换为Date对象
      const date = new Date(props.goal.deadline);
      if (!isNaN(date.getTime())) {
        goalData.deadline = date;
      }
    }
  }
  
  // 确保主目标的文件列表存在
  if (!goalData.fileList) {
    goalData.fileList = [];
  }
  
  // 确保子目标数组存在
  if (!goalData.childGoals) {
    goalData.childGoals = [];
  }
  
  // 确保子目标的文件列表存在
  goalData.childGoals.forEach(child => {
    if (!child.fileList) {
      child.fileList = [];
    }
    // 确保finish字段存在
    if (typeof child.finish === 'undefined') {
      child.finish = false;
    }
  });
  
  editForm.value = goalData;
  
  // // 添加一个空行用于添加新子目标
  // editForm.value.childGoals.push({
  //   message: '',
  //   finish: false,
  //   fileList: []
  // });
};

// 初始化表单数据
initFormData();

// 表单验证规则
const formRules = {
  title: {
    required: true,
    message: '请输入目标标题',
    trigger: 'blur'
  },
  owner: {
    required: true,
    message: '请输入负责人',
    trigger: 'blur'
  },
  deadline: {
    required: true,
    trigger: ['blur', 'change'],
    validator: (rule, value) => {
      // 如果值为空、null或undefined
      if (!value) {
        return new Error('请输入截止日期');
      }
      
      // 检查是否为有效的日期对象
      if (value instanceof Date) {
        return !isNaN(value.getTime()) ? true : new Error('请输入有效的截止日期');
      }
      
      // 检查是否为有效的时间戳
      if (typeof value === 'number') {
        return !isNaN(value) ? true : new Error('请输入有效的截止日期');
      }
      
      // 检查是否为有效的日期字符串
      if (typeof value === 'string') {
        const date = new Date(value);
        return !isNaN(date.getTime()) ? true : new Error('请输入有效的截止日期');
      }
      
      return new Error('请输入有效的截止日期');
    }
  },
  level: {
    required: true,
    message: '请选择优先级',
    trigger: 'change'
  },
  status: {
    required: true,
    message: '请选择状态',
    trigger: 'change'
  }
};

// 优先级选项
const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' }
];

// 状态选项
const statusOptions = [
  { label: '进行中', value: 'in-progress' },
  { label: '已完成', value: 'completed' },
  { label: '已过期', value: 'expired' }
];

// 子目标表格列定义
const subGoalColumns = [
  { title: '子目标', key: 'message' },
  { 
    title: '文件', 
    key: 'fileList', 
    render: (row, index) => {
      // 检查是否有文件，同时兼容fileList和files字段
      const files = row.fileList || row.files || [];
      if (files.length > 0) {
        return h('div', {}, [
          // 显示文件数量
          h(NTag, { type: 'info', size: 'small' }, {
            default: () => `${files.length}个文件`
          }),
          // 显示文件列表（直接展示，无需点击查看）
          h('div', { style: { marginTop: '8px' } }, [
            files.map((file, idx) => {
              const fileName = file.originalFilename || file.name || '未命名文件';
              return h('div', { key: idx, style: { display: 'flex', alignItems: 'center', marginBottom: '4px' } }, [
                h('a', {
                  href: file.url || file.fileUrl || `${baseURL()}/file/${file.name}`,
                  target: '_blank',
                  style: { color: '#1890ff', marginRight: '8px', fontSize: '12px' }
                }, fileName),
                h(NButton, {
                  size: 'tiny',
                  type: 'primary',
                  text: true,
                  onClick: () => getDownloadUrl(file)
                }, {
                  default: () => h(NIcon, { size: 14 }, { default: () => h(CloudDownloadSharp) })
                })
              ]);
            })
          ])
        ]);
      } else {
        return h('span', { style: { color: '#999' } }, '无文件');
      }
    }
  },
  {
    title: '操作',
    key: 'actions',
    render: (row, index) => {
      return h(NButton, {
        size: 'small',
        type: 'primary',
        onClick: () => viewChildGoalFilesInViewMode(index)
      }, { default: () => '查看文件' });
    }
  }
];

// 可编辑子目标表格列定义
const editableSubGoalColumns = [
  { 
    title: '子目标', 
    key: 'message',
    render: (row, index) => {
    console.log("index=>>",index);
    console.log("editForm.value.childGoals.length=>>",editForm.value.childGoals.length);
    
      // 如果是最后一行，显示添加新子目标的输入框
      if (index === editForm.value.childGoals.length-1) {
        return h(NInput, {
          value: row.message,
          onUpdateValue: (val) => {
            // 如果是新行，创建新的子目标
            // if (index === editForm.value.childGoals.length-1) {
            //   editForm.value.childGoals.push({
            //     message: val,
            //     finish: false,
            //     fileList: []
            //   });
            // } else {
              editForm.value.childGoals[index].message = val;
            // }
          },
          placeholder: '输入新子目标后按回车添加',
          onKeyup: (e) => {
            if (e.key === 'Enter' ) {
              editForm.value.childGoals.push({
                message: '',
                finish: false,
                fileList: []
              });
            }
          }
        });
      }
      
      return h(NInput, {
        value: row.message,
        onUpdateValue: (val) => {
          editForm.value.childGoals[index].message = val;
        },
        placeholder: '请输入子目标'
      });
    }
  },
  {
    title: '文件',
    key: 'fileList',
    render: (row, index) => {
      // 如果是最后一行，显示添加按钮
      if (index === editForm.value.childGoals.length-1) {
        return h(NButton, {
          size: 'small',
          type: 'primary',
          dashed: true,
          onClick: () => {
          
              editForm.value.childGoals.push({
                message: '',
                finish: false,
                fileList: []
              });
            
          }
        }, { default: () => '添加子目标' });
      }
      
      // 检查是否有文件，同时兼容fileList和files字段
      const files = row.fileList || row.files || [];
      if (files.length > 0) {
        return h('div', {}, [
          // 显示文件数量
          h(NTag, { type: 'info', size: 'small' }, {
            default: () => `${files.length}个文件`
          }),
          // 显示文件列表（直接展示，无需点击查看）
          h('div', { style: { marginTop: '8px' } }, [
            files.map((file, idx) => {
              const fileName = file.originalFilename || file.name || '未命名文件';
              return h('div', { key: idx, style: { display: 'flex', alignItems: 'center', marginBottom: '4px' } }, [
                h('a', {
                  href: file.url || file.fileUrl || `${baseURL()}/file/${file.name}`,
                  target: '_blank',
                  style: { color: '#1890ff', marginRight: '8px', fontSize: '12px' }
                }, fileName),
                h(NButton, {
                  size: 'tiny',
                  type: 'primary',
                  text: true,
                  onClick: () => getDownloadUrl(file)
                }, {
                  default: () => h(NIcon, { size: 14 }, { default: () => h(CloudDownloadSharp) })
                })
              ]);
            })
          ])
        ]);
      } else {
        return h('span', { style: { color: '#999' } }, '无文件');
      }
    }
  },
  {
    title: '操作',
    key: 'actions',
    render: (row, index) => {
      // 如果是最后一行，不显示操作按钮
      if (index === editForm.value.childGoals.length-1) {
        return null;
      }
      
      return h(NSpace, {}, [
        h(NButton, {
          size: 'small',
          type: 'primary',
          disabled: row.finish,
          onClick: () => finishChild(index)
        }, { default: () => '完成' }),
        h(NButton, {
          size: 'small',
          type: 'info',
          onClick: () => viewChildGoalFiles(index)
        }, { default: () => '查看文件' }),
        h(NButton, {
          size: 'small',
          type: 'error',
          disabled: row.finish,
          onClick: () => removeSubGoal(index)
        }, { default: () => '删除' })
      ]);
    }
  }
];
// 子目标文件上传相关状态
const showChildGoalUploadModal = ref(false);
const currentChildGoalIndex = ref(-1);
const childGoalFiles = ref({}); // 存储每个子目标的文件列表

// 子目标文件查看相关状态
const showChildGoalFilesModal = ref(false);
const viewChildGoalIndex = ref(-1);
const viewChildGoalFilesList = ref([]); // 存储要查看的子目标文件列表

const finishChild=(index)=>{
  // 先标记子目标为完成
  let tmpEdit=JSON.parse(JSON.stringify(editForm.value));
  
  // 确保子目标存在
  if (tmpEdit.childGoals && tmpEdit.childGoals[index]) {
    tmpEdit.childGoals[index].finish = true;
    
    // 初始化该子目标的文件列表（如果不存在）
    if (!tmpEdit.childGoals[index].fileList) {
      tmpEdit.childGoals[index].fileList = [];
    }
    
    let finishCount=tmpEdit.childGoals.filter(childGoal => childGoal.finish).length;
    tmpEdit.progress=Math.round(finishCount/tmpEdit.childGoals.length*100);
    
    // 更新editForm
    editForm.value = tmpEdit;
    
    // 弹出文件上传模态框
    currentChildGoalIndex.value = index;
    
    // 如果该子目标已有文件，加载到上传组件
    if (editForm.value.childGoals[index].fileList && editForm.value.childGoals[index].fileList.length > 0) {
      // 深拷贝文件列表以避免引用问题
      childGoalFiles.value[index] = JSON.parse(JSON.stringify(editForm.value.childGoals[index].fileList));
    } else {
      childGoalFiles.value[index] = [];
    }
    
    console.log("子目标完成，初始化文件列表:", index, childGoalFiles.value[index]);
    showChildGoalUploadModal.value = true;
  } else {
    message.error('找不到对应的子目标');
  }
}

// 处理子目标文件上传成功
const handleChildGoalFileChange = (files) => {
  if (currentChildGoalIndex.value !== -1) {
    // 深拷贝文件列表以避免引用问题
    childGoalFiles.value[currentChildGoalIndex.value] = JSON.parse(JSON.stringify(files));
    console.log("子目标文件更新:", currentChildGoalIndex.value, childGoalFiles.value[currentChildGoalIndex.value]);
  }
}

// 查看子目标文件（编辑模式）
const viewChildGoalFiles = (index) => {
  if (editForm.value.childGoals && editForm.value.childGoals[index]) {
    viewChildGoalIndex.value = index;
    // 深拷贝文件列表以避免引用问题
    viewChildGoalFilesList.value = JSON.parse(JSON.stringify(
      editForm.value.childGoals[index].fileList || []
    ));
    showChildGoalFilesModal.value = true;
  }
}

// 查看子目标文件（查看模式）
const viewChildGoalFilesInViewMode = (index) => {
  if (props.goal.childGoals && props.goal.childGoals[index]) {
    viewChildGoalIndex.value = index;
    // 深拷贝文件列表以避免引用问题
    viewChildGoalFilesList.value = JSON.parse(JSON.stringify(
      props.goal.childGoals[index].fileList || []
    ));
    showChildGoalFilesModal.value = true;
  }
}

// 保存子目标文件
const saveChildGoalFiles = () => {
  if (currentChildGoalIndex.value !== -1) {
    let tmpEdit=JSON.parse(JSON.stringify(editForm.value));
    // 确保子目标存在且fileList字段存在
    if (tmpEdit.childGoals && tmpEdit.childGoals[currentChildGoalIndex.value]) {
      if (!tmpEdit.childGoals[currentChildGoalIndex.value].fileList) {
        tmpEdit.childGoals[currentChildGoalIndex.value].fileList = [];
      }
      // 确保使用fileList字段来存储子目标的文件列表，与后端实体类保持一致
      tmpEdit.childGoals[currentChildGoalIndex.value].fileList = 
        childGoalFiles.value[currentChildGoalIndex.value] || [];
      
      // 更新editForm
      editForm.value = tmpEdit;
      
      console.log("保存子目标文件:", currentChildGoalIndex.value, 
        editForm.value.childGoals[currentChildGoalIndex.value].fileList);
      saveGoal('no')
      message.success('子目标文件已保存');
    } else {
      message.error('找不到对应的子目标');
    }
    showChildGoalUploadModal.value = false;
  }
}

// 获取优先级文本
const getPriorityText = (priority) => {
  const option = priorityOptions.find(opt => opt.value === priority);
  return option ? option.label : '';
};

// 获取优先级类型
const getPriorityType = (level) => {
  switch (level) {
    case 'low': return 'success';
    case 'medium': return 'info';
    case 'high': return 'warning';
    case 'urgent': return 'error';
    default: return 'default';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  const option = statusOptions.find(opt => opt.value === status);
  return option ? option.label : '';
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'completed': return 'success';
    case 'in-progress': return 'warning';
    case 'expired': return 'error';
    default: return 'default';
  }
};

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '';
  if (bytes < 1024) return bytes + ' B';
  else if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
  else return (bytes / 1048576).toFixed(1) + ' MB';
};

// 复制目标
const copyGoal = async () => {
  try {
    // 创建一个更友好的目标信息文本
    const goalInfo = `
目标标题: ${props.goal.title}
目标描述: ${props.goal.description || '无'}
负责人: ${props.goal.owner}
截止日期: ${props.goal.deadlineString || '未设置'}
优先级: ${getPriorityText(props.goal.level)}
状态: ${getStatusText(props.goal.status)}
进度: ${props.goal.progress}%
标签: ${props.goal.tags && props.goal.tags.length > 0 ? props.goal.tags.join(', ') : '无'}
    `.trim();
    
    await navigator.clipboard.writeText(goalInfo);
    message.success('目标信息已复制到剪贴板');
  } catch (error) {
    console.error('复制失败:', error);
    message.error('复制失败，请手动复制');
  }
};


// 编辑目标
const editGoal = () => {
  isEditing.value = true;
};

// 添加子目标
const addSubGoal = () => {
  // 确保childGoals数组存在
  if (!editForm.value.childGoals) {
    editForm.value.childGoals = [];
  }
  
  // 移除最后一行的空行（如果存在）
  if (editForm.value.childGoals.length > 0 && 
      editForm.value.childGoals[editForm.value.childGoals.length - 1].message === '') {
    editForm.value.childGoals.pop();
  }
  
  // 添加新的空行
  editForm.value.childGoals.push({
    message: '',
    finish: false,
    fileList: []
  });
};

// 删除子目标
const removeSubGoal = (index) => {
  if (editForm.value.childGoals && editForm.value.childGoals.length > index) {
    // 删除指定索引的子目标
    editForm.value.childGoals.splice(index, 1);
    message.success('子目标已删除');
    
    // 如果删除后没有空行，添加一个空行用于添加新子目标
    const hasEmptyRow = editForm.value.childGoals.some(child => child.message === '');
    if (!hasEmptyRow) {
      editForm.value.childGoals.push({
        message: '',
        finish: false,
        fileList: []
      });
    }
  }
};

// 保存目标
const saveGoal = async (val) => {
  const valid = await formRef.value?.validate();
  if (valid) {
    // 确保fileList字段存在且格式正确
    let saveData = JSON.parse(JSON.stringify(editForm.value));
    
    // 确保主目标的文件列表存在
    if (!saveData.fileList) {
      saveData.fileList = [];
    }
    
    // 确保子目标数组存在
    if (!saveData.childGoals) {
      saveData.childGoals = [];
    }
    
    // 移除空行（message为空的子目标）
    saveData.childGoals = saveData.childGoals.filter(child => child.message && child.message.trim() !== '');
    
    // 确保子目标的文件列表存在
    saveData.childGoals.forEach(child => {
      if (!child.fileList) {
        child.fileList = [];
      }
      // 确保finish字段存在
      if (typeof child.finish === 'undefined') {
        child.finish = false;
      }
    });
    
    console.log('保存数据:', saveData);
    const res = await postM('editGoal', saveData);
    if(isSuccess(res)){
      message.success('目标保存成功')
     
      if(val!='no'){
        closeModal()
      }
      emit('updateGoal');
    }
  }
};

// 关闭模态框
const closeModal = () => {
  // 重置表单数据
  isEditing.value = false;
  initFormData();
  emit('update:show', false);
};

// 监听目标数据变化
watch(() => props.goal, (newGoal) => {
  // 重新初始化表单数据
  initFormData();
}, { deep: true });
</script>

<style scoped>
.mt-2 {
  margin-top: 8px;
}

.goal-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.goal-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
}

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--n-border-color);
}

.goal-description {
  color: var(--n-text-color-2);
  line-height: 1.6;
  margin: 0;
  padding: 8px 0;
}

.meta-card {
  margin-bottom: 16px;
}

.meta-item-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
  height: 100%;
}

.meta-item-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.date-card {
  background: linear-gradient(135deg, rgba(24, 160, 88, 0.05) 0%, rgba(24, 160, 88, 0.1) 100%);
  border-left: 4px solid #18a058;
}

.priority-card {
  background: linear-gradient(135deg, rgba(240, 160, 32, 0.05) 0%, rgba(240, 160, 32, 0.1) 100%);
  border-left: 4px solid #f0a020;
}

.status-card {
  background: linear-gradient(135deg, rgba(24, 160, 88, 0.05) 0%, rgba(24, 160, 88, 0.1) 100%);
  border-left: 4px solid #18a058;
}

.progress-card {
  background: linear-gradient(135deg, rgba(24, 160, 88, 0.05) 0%, rgba(24, 160, 88, 0.1) 100%);
  border-left: 4px solid #18a058;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin: 16px 0;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  background-color: var(--n-color-modal);
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.meta-content {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 32px;
}

.meta-content .n-icon {
  flex-shrink: 0;
}

.progress-item .meta-content {
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.progress-text {
  text-align: center;
  font-weight: 500;
  font-size: 14px;
}

.meta-item:hover {
  background-color: var(--n-color-hover);
}

.meta-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--n-text-color-3);
}

.files-section, .subgoals-section {
  margin-top: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--n-border-color);
}

.subgoals-table {
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

:deep(.subgoals-table .n-data-table-th) {
  background-color: var(--n-color-target);
  font-weight: 600;
}

:deep(.subgoals-table .n-data-table-tr:hover) {
  background-color: var(--n-color-hover);
}

:deep(.n-list-item) {
  border-radius: 6px;
  transition: background-color 0.2s ease;
  margin-bottom: 4px;
}

:deep(.n-list-item:hover) {
  background-color: var(--n-color-hover);
}

:deep(.n-thing-main__content) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.n-thing-header__title) {
  font-weight: 500;
}

:deep(.n-progress-content) {
  font-weight: 500;
}

:deep(.n-data-table-td) {
  vertical-align: middle;
}

@media (max-width: 768px) {
  .goal-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  :deep(.n-grid) {
    grid-template-columns: 1fr !important;
  }
  
  .meta-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .meta-item {
    padding: 12px;
  }
  
  .meta-item-card {
    border-radius: 8px;
  }
  
  .date-card, .priority-card, .status-card, .progress-card {
    border-left-width: 3px;
  }
}

@media (max-width: 480px) {
  .goal-header {
    align-items: stretch;
  }
  
  :deep(.n-space) {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>