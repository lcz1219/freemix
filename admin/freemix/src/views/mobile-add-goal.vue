<template>
  <div class="mobile-add-goal" :class="isDark ? 'dark' : 'light'">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="添加目标"
      left-text="返回"
      right-text="保存"
      fixed
      placeholder
      safe-area-inset-top
      @click-left="onClickLeft"
      @click-right="onClickRight"
    />
    
    <!-- 表单内容 -->
    <div class="form-content">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="goalForm.title"
            name="title"
            label="目标标题"
            placeholder="请输入目标标题"
            :rules="[{ required: true, message: '请输入目标标题' }]"
          />
          
          <van-field
            v-model="goalForm.description"
            name="description"
            label="目标描述"
            type="textarea"
            autosize
            placeholder="请输入目标描述"
          />
          
          <van-field
            v-model="goalForm.owner"
            name="owner"
            label="负责人"
            placeholder="请输入负责人姓名"
            :rules="[{ required: true, message: '请输入负责人姓名' }]"
          />
          
          <van-field
            v-model="goalForm.deadline"
            name="deadline"
            label="截止日期"
            is-link
            readonly
            placeholder="请选择截止日期"
            @click="showCalendar = true"
          />
          
          <van-field
            v-model="goalForm.level"
            name="level"
            label="优先级"
            is-link
            readonly
            placeholder="请选择优先级"
            @click="showLevelPicker = true"
          />
          
          <van-field
            v-model="goalForm.status"
            name="status"
            label="状态"
            is-link
            readonly
            placeholder="请选择状态"
            @click="showStatusPicker = true"
          />
          
          <van-field
            name="progress"
            label="进度"
          >
            <template #input>
              <van-slider 
                v-model="goalForm.progress" 
                :max="100" 
                :min="0"
                active-color="#8a2be2"
              />
            </template>
          </van-field>
          
          <van-field
            name="tags"
            label="标签"
          >
            <template #input>
              <van-tag 
                v-for="tag in goalForm.tags" 
                :key="tag"
                class="tag-item"
                closeable
                @close="removeTag(tag)"
              >
                {{ tag }}
              </van-tag>
              <van-button 
                size="small" 
                class="add-tag-btn"
                @click="showTagDialog = true"
              >
                添加标签
              </van-button>
            </template>
          </van-field>
        </van-cell-group>
      </van-form>
    </div>
    
    <!-- 日期选择器 -->
    <van-calendar 
      v-model:show="showCalendar" 
      @confirm="onConfirmCalendar"
    />
    
    <!-- 优先级选择器 -->
    <van-popup v-model:show="showLevelPicker" round position="bottom">
      <van-picker
        :columns="levelColumns"
        @confirm="onConfirmLevel"
        @cancel="showLevelPicker = false"
      />
    </van-popup>
    
    <!-- 状态选择器 -->
    <van-popup v-model:show="showStatusPicker" round position="bottom">
      <van-picker
        :columns="statusColumns"
        @confirm="onConfirmStatus"
        @cancel="showStatusPicker = false"
      />
    </van-popup>
    
    <!-- 标签对话框 -->
    <van-dialog 
      v-model:show="showTagDialog" 
      title="添加标签"
      show-cancel-button
      @confirm="addTag"
    >
      <van-field 
        v-model="newTag" 
        placeholder="请输入标签名称"
        style="margin: 10px;"
      />
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, inject } from 'vue';
import { useRouter } from 'vue-router';
import { Toast } from 'vant';

const router = useRouter();
const isDark = inject('isDark', ref(false));

// 表单数据
const goalForm = reactive({
  title: '',
  description: '',
  owner: '',
  deadline: '',
  level: '',
  status: '',
  progress: 0,
  tags: []
});

// 弹窗控制
const showCalendar = ref(false);
const showLevelPicker = ref(false);
const showStatusPicker = ref(false);
const showTagDialog = ref(false);
const newTag = ref('');

// 选择器数据
const levelColumns = [
  { text: '低', value: 'low' },
  { text: '中', value: 'medium' },
  { text: '高', value: 'high' }
];

const statusColumns = [
  { text: '未开始', value: 'not-started' },
  { text: '进行中', value: 'in-progress' },
  { text: '已完成', value: 'completed' },
  { text: '已暂停', value: 'paused' }
];

// 方法
const onClickLeft = () => {
  router.go(-1);
};

const onClickRight = () => {
  // 保存目标
  if (!goalForm.title || !goalForm.owner) {
    Toast.fail('请填写必填项');
    return;
  }
  
  // 这里应该调用API保存目标
  Toast.success('目标保存成功');
  router.go(-1);
};

const onConfirmCalendar = (date) => {
  goalForm.deadline = date.toLocaleDateString('zh-CN');
  showCalendar.value = false;
};

const onConfirmLevel = ({ selectedOptions }) => {
  goalForm.level = selectedOptions[0].text;
  showLevelPicker.value = false;
};

const onConfirmStatus = ({ selectedOptions }) => {
  goalForm.status = selectedOptions[0].text;
  showStatusPicker.value = false;
};

const addTag = () => {
  if (newTag.value && !goalForm.tags.includes(newTag.value)) {
    goalForm.tags.push(newTag.value);
    newTag.value = '';
  }
};

const removeTag = (tag) => {
  const index = goalForm.tags.indexOf(tag);
  if (index > -1) {
    goalForm.tags.splice(index, 1);
  }
};

const onSubmit = (values) => {
  console.log('submit', values);
};
</script>

<style scoped>
.mobile-add-goal {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.mobile-add-goal.dark {
  background-color: #1a1a1a;
  color: #ffffff;
}

.form-content {
  padding: 10px 0;
}

:deep(.van-cell-group) {
  background-color: white;
  margin: 10px;
  border-radius: 12px;
  overflow: hidden;
}

.dark :deep(.van-cell-group) {
  background-color: #2d2d2d;
}

.tag-item {
  margin-right: 5px;
  margin-bottom: 5px;
}

.add-tag-btn {
  margin-top: 5px;
}
</style>