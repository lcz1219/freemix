<template>
  <div class="mobile-register">
    <div class="register-header">
      <h1>注册账号</h1>
      <p>开启您的目标管理之旅</p>
    </div>
    
    <div class="register-form">
      <van-steps :active="currentStep - 1" active-color="#8a2be2">
        <van-step>基本信息</van-step>
        <van-step>安全问题</van-step>
        <van-step>完成</van-step>
      </van-steps>
      
      <van-form @submit="onSubmit">
        <van-cell-group inset v-if="currentStep === 1">
          <van-field
            v-model="formData.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
          />
          <van-field
            v-model="formData.email"
            type="email"
            name="email"
            label="邮箱"
            placeholder="请输入邮箱"
            :rules="[{ required: true, message: '请输入邮箱' }]"
          />
          <van-field
            v-model="formData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码（至少6位）"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
          <van-field
            v-model="formData.confirmPassword"
            type="password"
            name="confirmPassword"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="[{ required: true, validator: validatePassword }]"
          />
        </van-cell-group>
        
        <van-cell-group inset v-if="currentStep === 2">
          <van-field
            v-model="formData.saveQuestionOne"
            name="saveQuestionOne"
            label="安全问题1"
            placeholder="请选择安全问题"
            is-link
            readonly
            @click="showQuestionPicker(1)"
          />
          <van-field
            v-model="formData.saveAnOne"
            name="saveAnOne"
            label="答案"
            placeholder="请输入答案"
            :rules="[{ required: true, message: '请输入答案' }]"
          />
          
          <van-field
            v-model="formData.saveQuestionTwice"
            name="saveQuestionTwice"
            label="安全问题2"
            placeholder="请选择安全问题"
            is-link
            readonly
            @click="showQuestionPicker(2)"
          />
          <van-field
            v-model="formData.saveAnTwice"
            name="saveAnTwice"
            label="答案"
            placeholder="请输入答案"
            :rules="[{ required: true, message: '请输入答案' }]"
          />
        </van-cell-group>
        
        <van-cell-group inset v-if="currentStep === 3">
          <van-field
            v-model="formData.saveQuestionThree"
            name="saveQuestionThree"
            label="安全问题3"
            placeholder="请选择安全问题"
            is-link
            readonly
            @click="showQuestionPicker(3)"
          />
          <van-field
            v-model="formData.saveAnThree"
            name="saveAnThree"
            label="答案"
            placeholder="请输入答案"
            :rules="[{ required: true, message: '请输入答案' }]"
          />
        </van-cell-group>
        
        <div class="register-buttons">
          <van-button 
            v-if="currentStep > 1" 
            round 
            block 
            class="prev-btn"
            @click="prevStep"
          >
            上一步
          </van-button>
          
          <van-button 
            v-if="currentStep < 3" 
            round 
            block 
            type="primary" 
            native-type="submit"
          >
            下一步
          </van-button>
          
          <van-button 
            v-if="currentStep === 3" 
            round 
            block 
            type="primary" 
            native-type="submit"
            :loading="loading"
          >
            完成注册
          </van-button>
        </div>
      </van-form>
    </div>
    
    <van-popup v-model:show="showPicker" round position="bottom">
      <van-picker
        :columns="questionColumns"
        @confirm="onConfirmPicker"
        @cancel="showPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Toast } from 'vant';
import { postM, isSuccess } from '@/utils/request.js';

const router = useRouter();
const currentStep = ref(1);
const loading = ref(false);
const showPicker = ref(false);
const currentQuestionField = ref(1);

const formData = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  saveQuestionOne: '',
  saveAnOne: '',
  saveQuestionTwice: '',
  saveAnTwice: '',
  saveQuestionThree: '',
  saveAnThree: ''
});

const questionColumns = [
  { text: '出生的城市', value: 'birthcity' },
  { text: '大学的城市', value: 'county' },
  { text: '您的年龄', value: 'age' }
];

const validatePassword = (val) => {
  if (val !== formData.value.password) {
    return '两次输入的密码不一致';
  }
  return true;
};

const onSubmit = () => {
  if (currentStep.value < 3) {
    currentStep.value++;
  } else {
    handleRegister();
  }
};

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
};

const showQuestionPicker = (field) => {
  currentQuestionField.value = field;
  showPicker.value = true;
};

const onConfirmPicker = ({ selectedOptions }) => {
  const option = selectedOptions[0];
  switch (currentQuestionField.value) {
    case 1:
      formData.value.saveQuestionOne = option.text;
      break;
    case 2:
      formData.value.saveQuestionTwice = option.text;
      break;
    case 3:
      formData.value.saveQuestionThree = option.text;
      break;
  }
  showPicker.value = false;
};

const handleRegister = async () => {
  loading.value = true;
  try {
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
      Toast.success('注册成功');
      router.push('/login');
    } else {
      Toast.fail(res.data.msg);
    }
  } catch (error) {
    Toast.fail('注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.mobile-register {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  box-sizing: border-box;
}

.register-header {
  text-align: center;
  padding: 30px 0;
  color: white;
}

.register-header h1 {
  font-size: 28px;
  margin-bottom: 10px;
  font-weight: bold;
}

.register-header p {
  font-size: 16px;
  opacity: 0.9;
}

:deep(.van-cell-group) {
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  margin-top: 20px;
}

:deep(.van-field__label) {
  font-weight: 500;
}

.register-buttons {
  margin-top: 30px;
}

:deep(.van-button) {
  height: 48px;
  font-size: 16px;
  margin-bottom: 15px;
}

.prev-btn {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid #8a2be2 !important;
  color: #8a2be2 !important;
}
</style>