<template>
  <div class="mobile-login" :class="isDark ? 'dark' : 'light'">
    <!-- 背景装饰元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>
    
    <div class="login-container">
      <div class="login-header">
        <h1 class="app-title">目标追踪者</h1>
        <p class="app-subtitle">掌控你的目标，衡量你的成功</p>
      </div>
      
      <n-card class="login-card" :class="isDark ? 'card-dark' : 'card-light'">
        <n-form ref="formRef" :model="user" :rules="rules">
          <n-form-item label="用户名" path="username">
            <n-input 
              v-model:value="user.username" 
              placeholder="请输入用户名"
              size="large"
              clearable
              @blur="loadCaptcha"
            >
              <template #prefix>
                <n-icon :component="PersonOutline" />
              </template>
            </n-input>
          </n-form-item>
          
          <n-form-item label="密码" path="password">
            <n-input 
              v-model:value="user.password" 
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password-on="click"
            >
              <template #prefix>
                <n-icon :component="LockClosedOutline" />
              </template>
            </n-input>
          </n-form-item>
          
          <n-form-item label="验证码" path="captcha">
            <div class="captcha-container">
              <n-input 
                v-model:value="user.captcha" 
                placeholder="请输入验证码"
                size="large"
                maxlength="4"
              >
                <template #prefix>
                  <n-icon :component="ShieldCheckmarkOutline" />
                </template>
              </n-input>
              <img 
                v-if="captchaImage" 
                :src="captchaImage" 
                alt="验证码" 
                class="captcha-image"
                @click="loadCaptcha"
              />
              <div v-else class="captcha-placeholder" @click="loadCaptcha">
                <n-icon :component="RefreshOutline" />
              </div>
            </div>
          </n-form-item>
          
          <n-button 
            type="primary" 
            size="large" 
            block 
            :loading="loading"
            @click="onSubmit"
            class="login-button"
          >
            登录
          </n-button>
          
          <n-button 
            size="large" 
            block 
            @click="toRegister"
            class="register-button"
            :class="isDark ? 'register-button-dark' : 'register-button-light'"
          >
            注册账号
          </n-button>
        </n-form>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { 
  NCard, 
  NForm, 
  NFormItem, 
  NInput, 
  NButton, 
  NIcon,
  useMessage
} from 'naive-ui';
import { PersonOutline, LockClosedOutline, ShieldCheckmarkOutline, RefreshOutline } from '@vicons/ionicons5';
import { postM, getM, isSuccess } from '@/utils/request.js';

const router = useRouter();
const store = useStore();
const message = useMessage();

// 主题状态
const isDark = ref(false);

// 表单引用
const formRef = ref();

// 用户数据
const user = ref({ username: '', password: '', captcha: '' });
const loading = ref(false);
const captchaImage = ref('');

// 表单验证规则
const rules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur'
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur'
  },
  captcha: {
    required: true,
    message: '请输入验证码',
    trigger: 'blur'
  }
};

// 提交登录表单
const onSubmit = async () => {
  loading.value = true;
  
  try {
    await formRef.value?.validate();
    
    const res = await postM('login', user.value);
    if (isSuccess(res)) {
      store.commit('saveUser', res.data.data);
      localStorage.setItem('token', res.data.data.token);
      message.success('登录成功');
      router.push('/home');
    } else {
      message.error(res.data.msg);
      // 登录失败时刷新验证码
      await loadCaptcha();
    }
  } catch (error) {
    message.error('登录失败，请稍后重试');
    // 登录失败时刷新验证码
    await loadCaptcha();
  } finally {
    loading.value = false;
  }
};

// 跳转到注册页面
const toRegister = () => {
  router.replace('/register');
};

// 加载验证码
const loadCaptcha = async () => {
  if (!user.value.username) {
    return;
  }
  
  try {
    console.log(user.value.username);
    
    let username=user.value.username
    const res = await postM('/captcha',{"username":username});
    if (isSuccess(res)) {
      captchaImage.value = res.data.data.image;
    }
  } catch (error) {
    message.error('验证码加载失败：'+error);
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
.mobile-login {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.mobile-login.dark {
  background: linear-gradient(135deg, #1e1e2e 0%, #2d1b40 100%);
}

.mobile-login.light {
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

.login-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  padding: 30px 0;
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

.login-card {
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: none;
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

.login-button {
  margin-top: 20px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.register-button {
  margin-top: 15px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.register-button-dark {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(138, 43, 226, 0.5) !important;
  color: #8a2be2 !important;
}

.register-button-light {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid #8a2be2 !important;
  color: #8a2be2 !important;
}

:deep(.n-input__prefix) {
  margin-right: 8px;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
}

.captcha-placeholder {
  width: 120px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #d9d9d9;
}

.captcha-placeholder:hover {
  background-color: #e6e6e6;
}
</style>