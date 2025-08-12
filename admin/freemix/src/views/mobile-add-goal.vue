<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'mobile-add-goal dark' : 'mobile-add-goal light'">
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>

    <!-- 顶部导航栏 -->
    <n-layout-header bordered class="mobile-header">
      <div class="header-content">
        <n-button quaternary circle @click="onClickLeft">
          <n-icon size="20">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M20,11H7.8l5.6-5.6L12,4l-8,8l8,8l1.4-1.4L7.8,13H20V11z"/>
            </svg>
          </n-icon>
        </n-button>
        <h1 class="header-title">添加目标</h1>
        <n-button quaternary type="primary" @click="onClickRight">
          保存
        </n-button>
      </div>
    </n-layout-header>
    
    <!-- 表单内容 -->
    <n-layout-content class="main-content-wrapper">
      <div class="content">
        <n-card :class="isDark ? 'form-card' : 'form-card-light'">
          <n-form ref="formRef" :model="goalForm" :rules="formRules">
            <n-form-item label="目标标题" path="title">
              <n-input 
                v-model:value="goalForm.title" 
                placeholder="请输入目标标题"
                maxlength="30"
                show-count
              />
            </n-form-item>
            
            <n-form-item label="目标描述" path="description">
              <n-input 
                v-model:value="goalForm.description" 
                placeholder="请输入目标描述"
                type="textarea"
                :autosize="{
                  minRows: 3,
                  maxRows: 5
                }"
              />
            </n-form-item>
            
            <n-form-item label="子目标" path="childGoals">
              <n-dynamic-input
                v-model:value="goalForm.childGoals"
                placeholder="每一步小目标都是成功的开始🏅"
                :min="1"
                :max="10"
              >
                <template #create-button-default>
                  添加子目标
                </template>
              </n-dynamic-input>
            </n-form-item>
            
            <n-form-item label="负责人" path="owner">
              <n-input 
                v-model:value="goalForm.owner" 
                placeholder="请输入负责人姓名"
              />
            </n-form-item>
            
            <n-form-item label="截止日期" path="deadline">
              <n-date-picker 
                v-model:value="goalForm.deadline" 
                type="date" 
                style="width: 100%"
              />
            </n-form-item>
            
            <n-form-item label="优先级" path="level">
              <n-select 
                v-model:value="goalForm.level" 
                :options="levelOptions" 
                placeholder="请选择优先级"
              />
            </n-form-item>
            
            <n-form-item label="状态" path="status">
              <n-select 
                v-model:value="goalForm.status" 
                :options="statusOptions" 
                placeholder="请选择状态"
              />
            </n-form-item>
            
            <n-form-item label="进度" path="progress">
              <n-slider 
                v-model:value="goalForm.progress" 
                :max="100" 
                :min="0"
                :step="1"
              />
              <div class="progress-text">{{ goalForm.progress }}%</div>
            </n-form-item>
            
            <n-form-item label="标签" path="tags">
              <n-dynamic-tags 
                v-model:value="goalForm.tags" 
                :max="5"
              />
            </n-form-item>
             <n-button 
        type="primary" 
        block 
        size="large"
        @click="onClickRight"
      >
        保存目标
      </n-button>
          </n-form>
        </n-card>
      </div>
       
    </n-layout-content>
    
    <!-- 底部固定保存按钮 -->
    <n-layout-footer bordered class="mobile-footer">
    
    </n-layout-footer>
  </n-layout>
</template>

<script setup>
import { ref, reactive, inject } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { 
  NLayout, 
  NLayoutHeader, 
  NLayoutContent, 
  NLayoutFooter,
  NButton, 
  NIcon, 
  NCard, 
  NForm, 
  NFormItem, 
  NInput, 
  NDatePicker, 
  NSelect, 
  NSlider, 
  NDynamicTags, 
  NDynamicInput,
  useMessage
} from 'naive-ui';
import { postM, isSuccess } from '@/utils/request';

const router = useRouter();
const store = useStore();
const isDark = inject('isDark', ref(false));
const message = useMessage();
const formRef = ref(null);

// 表单数据
const goalForm = reactive({
  title: '',
  description: '',
  childGoals: [{ value: '' }],
  owner: '',
  deadline: null,
  level: '',
  status: '',
  progress: 0,
  tags: []
});

// 表单验证规则
const formRules = {
  title: {
    required: true,
    message: '请输入目标标题',
    trigger: 'blur'
  },
  owner: {
    required: true,
    message: '请输入负责人姓名',
    trigger: 'blur'
  }
};

// 选择器选项
const levelOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' }
];

const statusOptions = [
  { label: '未开始', value: 'not-started' },
  { label: '进行中', value: 'in-progress' },
  { label: '已完成', value: 'completed' },
  { label: '已暂停', value: 'paused' }
];

// 方法
const onClickLeft = () => {
  router.go(-1);
};

const onClickRight = async () => {
  // 验证表单
  try {
    await formRef.value?.validate();
    
    // 准备提交的数据
    const submitData = {
      ...goalForm,
      username: store.state.user.username, // 添加当前用户名
      childGoals: goalForm.childGoals.map(item => ({
        value: item.value,
        finish: false
      }))
    };
    
    // 调用API保存目标
    const res = await postM('editGoal', submitData);
    if (isSuccess(res)) {
      message.success('目标保存成功');
      router.go(-1);
    } else {
      message.error(res.data.msg || '保存失败');
    }
  } catch (error) {
    message.error('请检查必填项');
    console.error(error);
  }
};
</script>

<style scoped>
.mobile-add-goal {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
}

.mobile-add-goal.dark {
  background-color: #0f0f13;
  color: #ffffff;
}

.mobile-add-goal.light {
  background-color: #dadae3;
  color: #000000;
}

.background-elements {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.gradient-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
}

.gradient-circle.blue {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #1e90ff, transparent 70%);
  top: -150px;
  right: -150px;
}

.gradient-circle.green {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, #00c9a7, transparent 70%);
  bottom: -125px;
  left: -125px;
}

.gradient-circle.purple {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, #8a2be2, transparent 70%);
  top: 30%;
  left: 20%;
}

.mobile-header {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  padding: 10px 16px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.mobile-header.light {
  background-color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #ffffff;
  flex: 1;
  text-align: center;
}

.mobile-header.light .header-title {
  color: #000000;
}

.main-content-wrapper {
  height: 100vh; /* 减去顶部和底部导航栏的高度 */
  overflow-y: auto;
  padding-bottom: 20px;
}

.content {
  padding: 16px;
}

.form-card,
.form-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.form-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.form-card-light {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.progress-text {
  text-align: right;
  font-size: 14px;
  margin-top: 5px;
  color: #8a2be2;
}

.mobile-footer {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  padding: 12px 16px;
  /* position: fixed; */
  bottom: 60px; /* 调整位置使其在底部导航栏上方 */
  width: 100%;
  z-index: 100;
}

.mobile-footer.light {
  background-color: rgba(255, 255, 255, 0.8);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

/* 滚动条样式 - Webkit内核浏览器 */
.main-content-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(138, 43, 226, 0.5);
  border-radius: 3px;
}

.main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 43, 226, 0.7);
}

/* 浅色模式滚动条 */
.mobile-add-goal.light .main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
}

.mobile-add-goal.light .main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(138, 43, 226, 0.3);
}

.mobile-add-goal.light .main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 43, 226, 0.5);
}
</style>