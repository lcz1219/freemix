<template>
  <n-layout :native-scrollbar="true" :class="isDark?'home-container':'home-container-light'">
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>
    
    <!-- 顶部导航栏 -->
    <n-layout-header class="header" bordered>
      <div class="logo">
        <div class="logo-icon">
          <n-icon size="24" color="white">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M18.73,7.05l0,0c-0.25-0.63-0.86-1.04-1.55-1.04c-0.05,0-0.11,0-0.16,0.01c-0.25-0.64-0.87-1.07-1.61-1.07 c-0.1,0-0.19,0.01-0.29,0.03c-0.28-0.64-0.93-1.06-1.7-1.06c-0.85,0-1.58,0.53-1.87,1.3c-0.26-0.05-0.52-0.07-0.78-0.07 c-2.34,0-4.15,2.01-3.9,4.33c-1.56,0.51-2.72,1.95-2.72,3.66c0,1.14,0.51,2.16,1.32,2.83C4.13,15.38,4,15.68,4,16 c0,1.66,1.34,3,3,3h10c2.76,0,5-2.24,5-5c0-2.64-2.05-4.78-4.66-4.96C18.82,9.3,19.11,8.12,18.73,7.05z"/>
            </svg>
          </n-icon>
        </div>
        <span class="logo-text">目标追踪者</span>
      </div>
      
      <nav class="nav">
        <n-button text type="primary" class="nav-link" @click="goToHome">仪表盘</n-button>
        <n-button text type="primary" class="nav-link active">添加目标</n-button>
        <n-button text type="primary" class="nav-link">统计数据</n-button>
        <n-button text type="primary" class="nav-link">设置</n-button>
      </nav>
      
      <div class="header-actions">
        
        
        <n-avatar round size="medium" src="https://api.dicebear.com/7.x/miniavs/svg?seed=3"></n-avatar>
      </div>
    </n-layout-header>
    
    <!-- 主内容区域 -->
    <n-layout-content class="main-content-wrapper">
      <div class="main-content">
        <div class="content-wrapper">
          <!-- 页面标题 -->
          <section class="page-header">
            <h1 :class="isDark?'hero-title':'hero-title-light'">添加新目标</h1>
            <p :class="isDark?'hero-subtitle':'hero-subtitle-light'">设定一个新目标，开始您的成功之旅</p>
          </section>
          
          <!-- 添加目标表单 -->
          <section class="form-section">
            <n-card class="form-card">
              <n-form
                ref="formRef"
                :model="goalForm"
                :rules="formRules"
                label-placement="left"
                label-width="120"
                require-mark-placement="right-hanging"
              >
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
                
                <n-form-item label="负责人" path="owner">
                  <n-input 
                    v-model:value="goalForm.owner" 
                    placeholder="请输入负责人姓名"
                  />
                </n-form-item>
                
                <n-form-item label="截止日期" path="deadline">
                   <n-date-picker v-model:value="goalForm.deadline" type="date" />
                </n-form-item>
                
                <n-form-item label="优先级" path="priority">
                  <n-select
                    v-model:value="goalForm.priority"
                    placeholder="请选择优先级"
                    :options="priorityOptions"
                  />
                </n-form-item>
                
                <n-form-item label="分类标签" path="tags">
                  <n-dynamic-tags v-model:value="goalForm.tags" />
                </n-form-item>
                
                <n-form-item label="预计工时" path="estimatedHours">
                  <n-input-number 
                    v-model:value="goalForm.estimatedHours" 
                    placeholder="请输入预计工时"
                    :min="1"
                    :max="1000"
                  >
                    <template #suffix>
                      小时
                    </template>
                  </n-input-number>
                </n-form-item>
                
                <n-row :gutter="[0, 24]">
                  <n-col :span="24">
                    <div style="display: flex; justify-content: flex-end">
                      <n-button 
                        type="tertiary" 
                        @click="handleReset"
                        style="margin-right: 10px"
                      >
                        重置
                      </n-button>
                      <n-button 
                        type="primary" 
                        @click="handleSubmit"
                      >
                        创建目标
                      </n-button>
                    </div>
                  </n-col>
                </n-row>
              </n-form>
            </n-card>
          </section>
        </div>
      </div>
    </n-layout-content>
    
    <!-- 页脚 -->
    <n-layout-footer class="footer" bordered>
      <p>© 2023 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
    </n-layout-footer>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, watch } from 'vue';
import { 
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NButton, 
  NIcon, 
  NSwitch, 
  NCard, 
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NDatePicker,
  NSelect,
  NDynamicTags,
  NRow,
  NCol,
  NAvatar,
  useMessage
} from 'naive-ui';
import { useRouter } from 'vue-router';
import request, { postM,isSuccess } from '@/utils/request'

// 图标组件
const SunIcon = {
  template: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
    <path d="M12,7c-2.8,0-5,2.2-5,5s2.2,5,5,5s5-2.2,5-5S14.8,7,12,7z M12,14.5c-1.4,0-2.5-1.1-2.5-2.5s1.1-2.5,2.5-2.5 s2.5,1.1,2.5,2.5S13.4,14.5,12,14.5z" />
    <path d="M12,5c-0.6,0-1-0.4-1-1V1c0-0.6,0.4-1,1-1s1,0.4,1,1v3C13,4.6,12.6,5,12,5z" />
    <path d="M12,19c-0.6,0-1,0.4-1,1v3c0,0.6,0.4,1,1,1s1-0.4,1-1v-3C13,19.4,12.6,19,12,19z" />
    <path d="M23,11h-3c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S23.6,11,23,11z" />
    <path d="M4,11H1c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S4.6,11,4,11z" />
    <path d="M18.7,6.3c0.4-0.4,0.4-1,0-1.4l-2.1-2.1c-0.4-0.4-1-0.4-1.4,0s-0.4,1,0,1.4l2.1,2.1C17.7,6.7,18.3,6.7,18.7,6.3z" />
    <path d="M5.3,17.7c-0.4,0.4-0.4,1,0,1.4l2.1,2.1c0.4,0.4,1,0.4,1.4,0s0.4-1,0-1.4l-2.1-2.1C6.3,17.3,5.7,17.3,5.3,17.7z" />
    <path d="M18.7,17.7c-0.4-0.4-1-0.4-1.4,0l-2.1,2.1c-0.4,0.4-0.4,1,0,1.4s1,0.4,1.4,0l2.1-2.1C19.1,18.7,19.1,18.1,18.7,17.7z" />
    <path d="M5.3,6.3c0.4,0.4,1,0.4,1.4,0l2.1-2.1c0.4-0.4,0.4-1,0-1.4s-1-0.4-1.4,0L5.3,4.9C4.9,5.3,4.9,5.9,5.3,6.3z" />
  </svg>`
};

const MoonIcon = {
  template: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
    <path d="M20.9,15.3c-0.5-0.9-1.2-1.7-2-2.2c-0.2-0.1-0.4-0.1-0.6-0.1c-0.3,0-0.6,0.1-0.9,0.3c-0.8,0.4-1.3,1.1-1.5,1.9 c-0.2,0.8,0,1.7,0.6,2.4c0.9,1.1,2.2,1.6,3.5,1.4c0.2,0,0.5-0.1,0.7-0.2C21.6,18.4,21.8,16.8,20.9,15.3z M19.5,18.5 c-0.9,0.1-1.8-0.2-2.5-0.8c-0.3-0.3-0.5-0.7-0.6-1.1c-0.1-0.4-0.1-0.8,0.1-1.2c0.2-0.4,0.5-0.7,0.9-0.9 c0.3-0.1,0.5-0.2,0.8-0.1c0.7,0.2,1.3,0.6,1.7,1.2C20.4,16.5,20.3,17.7,19.5,18.5z" />
    <path d="M9,22c5.5,0,10-4.5,10-10c0-0.8-0.1-1.6-0.3-2.4c-0.1-0.4-0.5-0.7-0.9-0.6c-0.4,0.1-0.7,0.5-0.6,0.9 c0.2,0.7,0.3,1.4,0.3,2.1c0,4.4-3.6,8-8,8s-8-3.6-8-8c0-4.4,3.6-8,8-8c0.7,0,1.3,0.1,2,0.2c0.4,0.1,0.8-0.1,1-0.5 C13.1,2.6,13,2.2,12.6,2C11.7,1.7,10.8,1.6,9.9,1.6C4.4,1.6,0,6,0,11.5C0,17,4.5,22,9,22z" />
  </svg>`
};

// 注入主题变量
const isDark = inject('isDark', ref(true));

// 响应式数据
const darkMode = ref(true);
const formRef = ref(null);
const message = useMessage();
const router = useRouter();

// 表单数据
const goalForm = ref({
  title: '',
  description: '',
  owner: '',
  deadline: null,
  priority: null,
  tags: [],
  estimatedHours: null
});

// 优先级选项
const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' }
];

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
    type: 'number',
          required: true,
          trigger: ['blur', 'change'],
          message: '请输入截止日期'
  },
  priority: {
    required: true,
    message: '请选择优先级',
    trigger: 'change'
  }
};

// 开关轨道样式
const railStyle = ({ focused, checked }) => {
  const style = {};
  if (checked) {
    style.background = '#8a2be2';
    if (focused) style.boxShadow = '0 0 0 2px #d0305040';
  } else {
    style.background = '#2080f0';
    if (focused) style.boxShadow = '0 0 0 2px #2080f040';
  }
  return style;
};

// 处理表单提交
const handleSubmit = (e) => {
  e.preventDefault();
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      message.success('目标创建成功');
      // 这里可以添加实际的提交逻辑
      console.log('提交的表单数据:', goalForm.value);
       const res= await postM('editGoal',goalForm.value);
       if(isSuccess(res)){
        message.success('目标创建成功')
       }
      // 提交后跳转到主页
      router.push('/home');
    } else {
      message.error('请检查表单信息');
    }
  });
};

// 重置表单
const handleReset = () => {
  goalForm.value = {
    title: '',
    description: '',
    owner: '',
    deadline: null,
    priority: null,
    tags: [],
    estimatedHours: null
  };
  message.info('表单已重置');
};

// 跳转到主页
const goToHome = () => {
  router.push('/home');
};

// 初始化
onMounted(() => {
  darkMode.value = isDark.value;
});

// 监听主题变化
watch(isDark, (newVal) => {
  darkMode.value = newVal;
});
</script>

<style scoped>
/* 继承主页样式 */
.home-container {
  background-color: #0f0f13;
  color: #ffffff;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.home-container-light {
  background-color: #dadae3;
  color: #000000;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
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
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #1e90ff, transparent 70%);
  top: -300px;
  right: -300px;
}

.gradient-circle.green {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #00c9a7, transparent 70%);
  bottom: -250px;
  left: -250px;
}

.gradient-circle.purple {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #8a2be2, transparent 70%);
  top: 50%;
  left: 30%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #8a2be2, #4b0082);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  background: linear-gradient(to right, #8a2be2, #4b0082);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  font-size: 16px;
  font-weight: 500;
  position: relative;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #8a2be2, #4b0082);
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.main-content-wrapper {
  height: calc(100vh - 160px); /* 减去头部和底部的高度 */
  overflow-y: auto;
}

/* 滚动条样式 - Webkit内核浏览器 */
.main-content-wrapper::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.main-content-wrapper::-webkit-scrollbar-thumb {
  background-color: rgba(138, 43, 226, 0.5);
  border-radius: 4px;
  transition: background-color 0.3s;
}

.main-content-wrapper::-webkit-scrollbar-track {
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.main-content-wrapper::-webkit-scrollbar-corner {
  background-color: transparent;
}

/* 亮色模式下的滚动条样式 */
.home-container-light .main-content-wrapper::-webkit-scrollbar-thumb {
  background-color: rgba(138, 43, 226, 0.3);
}

.home-container-light .main-content-wrapper::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.05);
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px;
}

.content-wrapper {
  min-height: 100%;
}

.page-header {
  text-align: center;
  padding: 40px 0 60px;
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 20px;
  background: linear-gradient(to right, #fff, #8a2be2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-title-light {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #333;
}

.hero-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  max-width: 600px;
  margin: 0 auto;
}

.hero-subtitle-light {
  font-size: 18px;
  color: #666;
  max-width: 600px;
  margin: 0 auto;
}

.form-section {
  max-width: 800px;
  margin: 0 auto;
}

.form-card {
  background: rgba(30, 30, 42, 0.7);
  border-radius: 16px;
  padding: 30px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.home-container-light .form-card {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.footer {
  text-align: center;
  padding: 40px 0;
  margin-top: 60px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.home-container-light .footer {
  color: rgba(0, 0, 0, 0.5);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}
</style>