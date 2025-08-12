<template>
  <div class="mobile-register" :class="isDark ? 'dark' : 'light'">
    <!-- 背景装饰元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>
    
    <div class="register-container">
      <div class="register-header">
        <h1 class="app-title">注册账号</h1>
        <p class="app-subtitle">开启您的目标管理之旅</p>
      </div>
      
      <n-card class="register-card" :class="isDark ? 'card-dark' : 'card-light'">
        <n-steps :current="currentStep" :status="currentStatus" size="small">
          <n-step title="基本信息" description="创建账户" />
          <n-step title="安全问题" description="设置安全问题" />
          <n-step title="完成" description="完成注册" />
        </n-steps>
        
        <n-form ref="formRef" :model="formData" :rules="rules">
          <!-- 第一步：基本信息 -->
          <div v-show="currentStep === 1">
            <n-form-item label="用户名" path="username">
              <n-input 
                v-model:value="formData.username" 
                placeholder="请输入用户名"
                size="large"
              >
                <template #prefix>
                  <n-icon :component="PersonOutline" />
                </template>
              </n-input>
            </n-form-item>
            
            <n-form-item label="邮箱" path="email">
              <n-input 
                v-model:value="formData.email" 
                type="email"
                placeholder="请输入邮箱"
                size="large"
              >
                <template #prefix>
                  <n-icon :component="MailOutline" />
                </template>
              </n-input>
            </n-form-item>
            
            <n-form-item label="密码" path="password">
              <n-input 
                v-model:value="formData.password" 
                type="password"
                placeholder="请输入密码（至少6位）"
                size="large"
                show-password-on="click"
              >
                <template #prefix>
                  <n-icon :component="LockClosedOutline" />
                </template>
              </n-input>
            </n-form-item>
            
            <n-form-item label="确认密码" path="confirmPassword">
              <n-input 
                v-model:value="formData.confirmPassword" 
                type="password"
                placeholder="请再次输入密码"
                size="large"
                show-password-on="click"
              >
                <template #prefix>
                  <n-icon :component="LockClosedOutline" />
                </template>
              </n-input>
            </n-form-item>
          </div>
          
          <!-- 第二步：安全问题1和2 -->
          <div v-show="currentStep === 2">
            <n-form-item label="安全问题1" path="saveQuestionOne">
              <n-select
                v-model:value="formData.saveQuestionOne"
                :options="questionOptions"
                placeholder="请选择安全问题"
                size="large"
              />
            </n-form-item>
            
            <n-form-item label="答案1" path="saveAnOne">
              <n-input 
                v-model:value="formData.saveAnOne" 
                placeholder="请输入答案"
                size="large"
              />
            </n-form-item>
            
            <n-form-item label="安全问题2" path="saveQuestionTwice">
              <n-select
                v-model:value="formData.saveQuestionTwice"
                :options="questionOptions"
                placeholder="请选择安全问题"
                size="large"
              />
            </n-form-item>
            
            <n-form-item label="答案2" path="saveAnTwice">
              <n-input 
                v-model:value="formData.saveAnTwice" 
                placeholder="请输入答案"
                size="large"
              />
            </n-form-item>
          </div>
          
          <!-- 第三步：安全问题3 -->
          <div v-show="currentStep === 3">
            <n-form-item label="安全问题3" path="saveQuestionThree">
              <n-select
                v-model:value="formData.saveQuestionThree"
                :options="questionOptions"
                placeholder="请选择安全问题"
                size="large"
              />
            </n-form-item>
            
            <n-form-item label="答案3" path="saveAnThree">
              <n-input 
                v-model:value="formData.saveAnThree" 
                placeholder="请输入答案"
                size="large"
              />
            </n-form-item>
          </div>
          
          <div class="register-buttons">
            <n-button 
              v-if="currentStep > 1" 
              size="large" 
              block 
              @click="prevStep"
              class="prev-button"
              :class="isDark ? 'prev-button-dark' : 'prev-button-light'"
            >
              上一步
            </n-button>
            
            <n-button 
              v-if="currentStep < 3" 
              type="primary" 
              size="large" 
              block 
              @click="nextStep"
              class="next-button"
            >
              下一步
            </n-button>
            
            <n-button 
              v-if="currentStep === 3" 
              type="primary" 
              size="large" 
              block 
              :loading="loading"
              @click="handleRegister"
              class="register-submit-button"
            >
              完成注册
            </n-button>
          </div>
        </n-form>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { 
  NCard, 
  NForm, 
  NFormItem, 
  NInput, 
  NButton, 
  NSteps, 
  NStep,
  NSelect,
  NIcon,
  useMessage
} from 'naive-ui';
import { PersonOutline, MailOutline, LockClosedOutline } from '@vicons/ionicons5';
import { getMPaths,isSuccess,postM } from '@/utils/request'

const router = useRouter();
const message = useMessage();


// 主题状态
const isDark = ref(false);

// 表单引用
const formRef = ref(null);

// 步骤状态
const currentStep = ref(1);
const currentStatus = ref('process');
const loading = ref(false);

// 表单数据
const formData = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  saveQuestionOne: null,
  saveAnOne: '',
  saveQuestionTwice: null,
  saveAnTwice: '',
  saveQuestionThree: null,
  saveAnThree: ''
});

// 安全问题选项
const questionOptions = [
  { label: '出生的城市', value: '出生的城市' },
  { label: '大学的城市', value: '大学的城市' }, 
  { label: '您的年龄', value: '您的年龄' },
  { label: '您母亲的姓名', value: '您母亲的姓名' },
  { label: '您父亲的姓名', value: '您父亲的姓名' },
  { label: '您的小学校名', value: '您的小学校名' }
];

// 表单验证规则
const rules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur'
  },
  email: [
    {
      required: true,
      message: '请输入邮箱',
      trigger: 'blur'
    },
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur'
    },
    {
      min: 6,
      message: '密码至少6位',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      required: true,
      message: '请确认密码',
      trigger: 'blur'
    },
    // {
    //   validator: validatePassword,
    //   trigger: 'blur'
    // }
  ],
  saveQuestionOne: {
    required: true,
    message: '请选择安全问题',
    trigger: 'blur'
  },
  saveAnOne: {
    required: true,
    message: '请输入答案',
    trigger: 'blur'
  },
  saveQuestionTwice: {
    required: true,
    message: '请选择安全问题',
    trigger: 'blur'
  },
  saveAnTwice: {
    required: true,
    message: '请输入答案',
    trigger: 'blur'
  },
  saveQuestionThree: {
    required: true,
    message: '请选择安全问题',
    trigger: 'blur'
  },
  saveAnThree: {
    required: true,
    message: '请输入答案',
    trigger: 'blur'
  }
};

// 密码验证函数
function validatePassword(rule, value) {
  if (value !== formData.value.password) {
    return new Error('两次输入的密码不一致');
  }
  return true;
}

// 下一步
const nextStep = async () => {
  try {
    // 验证当前步骤的表单
    const validateFields = getStepFields(currentStep.value);
    await formRef.value?.validate(validateFields, (errors) => {
      if (errors) {
        message.error('请填写必填项');
        return Promise.reject(errors);
      }
      return Promise.resolve();
    });
    
    // 进入下一步
    if (currentStep.value < 3) {
      currentStep.value++;
    }
  } catch (error) {
    // 错误已在验证回调中处理
  }
};

// 上一步
const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
};

// 获取当前步骤需要验证的字段
const getStepFields = (step) => {
  switch (step) {
    case 1:
      return ['username', 'email', 'password', 'confirmPassword'];
    case 2:
      return ['saveQuestionOne', 'saveAnOne', 'saveQuestionTwice', 'saveAnTwice'];
    case 3:
      return ['saveQuestionThree', 'saveAnThree'];
    default:
      return [];
  }
};

// 处理注册
const handleRegister = async () => {
  loading.value = true;
  try {
    // 验证最后一步的表单
    await formRef.value?.validate((errors) => {
      if (errors) {
        message.error('请填写必填项');
        return Promise.reject(errors);
      }
      return Promise.resolve();
    });
    
    // 构造注册数据
    const registerData = {
      username: formData.value.username,
      password: formData.value.password,
      email: formData.value.email,
      saveQuestionOne: formData.value.saveQuestionOne,
      saveAnOne: formData.value.saveAnOne,
      saveQuestionTwice: formData.value.saveQuestionTwice,
      saveAnTwice: formData.value.saveAnTwice,
      saveQuestionThree: formData.value.saveQuestionThree,
      saveAnThree: formData.value.saveAnThree
    };
    
    const res = await postM('register', registerData);
    if (isSuccess(res)) {
      message.success('注册成功');
      router.push('/login');
    } else {
      message.error(res.data.msg);
    }
  } catch (error) {
    console.log(error);
    
    message.error('注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 初始化主题
onMounted(() => {
  const savedTheme = localStorage.getItem('theme-dark');
  if (savedTheme) {
    isDark.value = JSON.parse(savedTheme);
  }
});
</script>

<style scoped>
.mobile-register {
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.mobile-register.dark {
  background: linear-gradient(135deg, #1e1e2e 0%, #2d1b40 100%);
}

.mobile-register.light {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.background-elements {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
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

.register-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.register-header {
  text-align: center;
  padding: 20px 0;
  color: white;
}

.app-title {
  font-size: 28px;
  margin-bottom: 10px;
  font-weight: bold;
}

.app-subtitle {
  font-size: 16px;
  opacity: 0.9;
}

.register-card {
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: none;
  margin-top: 10px;
}

.card-dark {
  background: rgba(30, 30, 40, 0.7);
}

.card-light {
  background: rgba(255, 255, 255, 0.85);
}

:deep(.n-form-item-label) {
  font-weight: 500;
}

:deep(.n-form-item-label .n-form-item-label__text) {
  color: #8a2be2;
}

:deep(.n-steps) {
  margin-bottom: 20px;
}

.register-buttons {
  margin-top: 20px;
}

.prev-button {
  margin-bottom: 15px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.prev-button-dark {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(138, 43, 226, 0.5) !important;
  color: #8a2be2 !important;
}

.prev-button-light {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid #8a2be2 !important;
  color: #8a2be2 !important;
}

.next-button,
.register-submit-button {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

:deep(.n-input__prefix) {
  margin-right: 8px;
}
</style>