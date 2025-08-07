<template>
  <n-modal v-model:show="showModal" preset="card" :style="{ width: '800px',height:'600px',overflow: 'auto'}" title="目标详情" :bordered="false"
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

    <div v-if="!isEditing">
      <n-descriptions label-placement="left" :column="1" bordered>
        <n-descriptions-item label="目标标题">
          {{ goal.title }}
        </n-descriptions-item>
        <n-descriptions-item label="目标描述">
          {{ goal.description }}
        </n-descriptions-item>
        <n-descriptions-item label="负责人">
          {{ goal.owner }}
        </n-descriptions-item>
        <n-descriptions-item label="截止日期">
          {{ goal.deadlineString }}
        </n-descriptions-item>
        <n-descriptions-item label="优先级">
          <n-tag :type="getPriorityType(goal.level)">
            {{ getPriorityText(goal.level) }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="状态">
          <n-tag :type="getStatusType(goal.status)">
            {{ getStatusText(goal.status) }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="进度">
          <n-progress type="circle" status="success" :percentage="isEditing ? editForm.progress : goal.progress" processing/>
          <!-- <n-progress type="line" :percentage="goal.progress" :indicator-placement="'inside'" :height="20"
            :rail-color="'rgba(255, 255, 255, 0.1)'" :fill-color="'linear-gradient(90deg, #8a2be2, #4b0082)'" processing /> -->
        </n-descriptions-item>
        <n-descriptions-item label="标签">
          <n-space>
            <n-tag v-for="tag in goal.tags" :key="tag" type="primary">
              {{ tag }}
            </n-tag>
          </n-space>
        </n-descriptions-item>
      </n-descriptions>

      <n-space v-if="goal.subGoals && goal.subGoals.length" vertical>
        <h3>子目标</h3>
        <n-data-table :columns="subGoalColumns" :data="goal.subGoals" :pagination="false" />
      </n-space>
    </div>

    <div v-else>
      <!-- <n-modal :show="true"
    title="card 预设拖拽"
    preset="card"
    draggable
    :style="{ width: '800px',height:'800px' }"> -->
      <n-form ref="formRef" :model="editForm" :rules="formRules" label-placement="left" label-width="120"
        require-mark-placement="right-hanging" >
        <n-form-item label="目标标题" path="title">
          <n-input v-model:value="editForm.title" placeholder="请输入目标标题" maxlength="30" show-count />
        </n-form-item>

        <n-form-item label="目标描述" path="description">
          <n-input v-model:value="editForm.description" placeholder="请输入目标描述" type="textarea"
            :autosize="{ minRows: 3, maxRows: 5 }" />
        </n-form-item>

        <n-form-item label="负责人" path="owner">
          <n-input v-model:value="editForm.owner" placeholder="请输入负责人姓名" />
        </n-form-item>

        <n-form-item label="截止日期" path="deadline">
          <n-date-picker v-model:value="editForm.deadline" type="date" />
        </n-form-item>

        <n-form-item label="优先级" path="priority">
          <n-select v-model:value="editForm.level" :options="priorityOptions" />
        </n-form-item>

        <n-form-item label="状态" path="status">
          <n-select v-model:value="editForm.status" :options="statusOptions" />
        </n-form-item>

        <n-form-item label="进度" path="progress">
         <n-progress type="circle" status="success" :percentage="editForm.progress" processing/>
        </n-form-item>

        <n-form-item label="标签" path="tags">
          <n-dynamic-tags v-model:value="editForm.tags" />
        </n-form-item>

        <n-form-item label="子目标" path="subGoals">
          <n-data-table :columns="editableSubGoalColumns" :data="editForm.childGoals" :pagination="false" />
          <!-- <n-button @click="addSubGoal" class="mt-2">
            添加子目标
          </n-button> -->
        </n-form-item>
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
</template>

<script setup>
import { ref, computed, watch,h } from 'vue';
import { Pencil, Copy,Eye } from '@vicons/ionicons5';
import { postM,isSuccess } from '@/utils/request'
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
  useMessage 
} from 'naive-ui';

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

// 编辑表单数据
const editForm = ref({ ...props.goal });

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
    // type: 'number',
          required: true,
          trigger: ['blur', 'change'],
          message: '请输入截止日期'
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
  { title: '子目标', key: 'title' },
  { title: '进度', key: 'progress', render: (row) => h('div', {}, `${row.progress}%`) }
];

// 可编辑子目标表格列定义
const editableSubGoalColumns = [
  { title: '子目标', key: 'message' },
  // { title: '进度', key: 'progress' },
  {
    title: '操作',
    key: 'finish',
    render: (row, index) => {
      return h('div', {}, [
        h(NButton, {
          size: 'small',
          disabled: row.finish,
          onClick: () => finishChild(index)
        }, { default: () => '完成' })
      ]);
    }
  }
];
const finishChild=(index)=>{
  // console.log("editForm.value.childGoals",editForm.value.childGoals[index]);
  let tmpEdit=JSON.parse(JSON.stringify(editForm.value));
  tmpEdit.childGoals[index].finish = true;
  let finishCount=tmpEdit.childGoals.filter(childGoal => childGoal.finish).length;
  // console.log("finishCount",finishCount);
  
  tmpEdit.progress=Math.round(finishCount/tmpEdit.childGoals.length*100);
  // console.log("editForm.value.progress",tmpEdit.progress);
  editForm.value={...tmpEdit};
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
  editForm.value.subGoals.push({
    title: '',
    progress: 0
  });
};

// 删除子目标
const removeSubGoal = (index) => {
  editForm.value.subGoals.splice(index, 1);
};

// 保存目标
const saveGoal = async () => {
  const valid = await formRef.value?.validate();
  if (valid) {
   const res= await postM('editGoal', editForm.value);
       if(isSuccess(res)){
        message.success('目标创建成功')
        showModal.value=false
        emit('updateGoal');
       }
  }
};

// 关闭模态框
const closeModal = () => {
  showModal.value = false;
  isEditing.value = false;
  editForm.value = {...props.goal};
};

// 监听目标数据变化
watch(() => props.goal, (newGoal) => {
  editForm.value = { ...newGoal };
}, { deep: true });
</script>

<style scoped>
.mt-2 {
  margin-top: 8px;
}
</style>