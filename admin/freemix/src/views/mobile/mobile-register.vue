<template>
  <div class="mobile-login-container">
    <!-- 动态背景层 (保留原风格) -->
    <div class="animated-bg"></div>
    <div class="bg-overlay"></div>

    <div class="content-wrapper">
      <!-- 头部 Logo 区域 -->
      <div class="brand-section">
        <div class="logo-box">
          <img src="/icons/icon.png" alt="Logo" class="system-logo" />
        </div>
        <h1 class="app-title">FreeMix</h1>
        <p class="app-subtitle">账号注册</p>
      </div>

      <!-- 主体卡片区域 -->
      <div class="glass-card">
        <!-- 步骤指示器 -->
        <div class="step-indicator">
          <div 
            v-for="i in 4" 
            :key="i" 
            class="dot" 
            :class="{ active: currentStep >= i, current: currentStep === i }"
          ></div>
        </div>

        <transition name="fade-slide" mode="out-in">
          <!-- 步骤 1: 基本信息 -->
          <div v-if="currentStep === 1" key="step-1" class="form-step">
            <h3 class="step-title">基本信息</h3>
            <van-form ref="step1Form" @submit="nextStep" class="custom-form">
              <!-- 用户名 -->
              <div class="input-group">
                <van-field
                  v-model="formData.username"
                  name="username"
                  placeholder="请输入用户名 (至少3位)"
                  :rules="[{ required: true, message: '请填写用户名' }, { validator: (val) => val.length >= 3, message: '至少3个字符' }]"
                  class="glass-input"
                  autocomplete="off"
                >
                  <template #left-icon>
                    <van-icon name="manager" class="field-icon" />
                  </template>
                </van-field>
              </div>

              <!-- 中文名 -->
              <div class="input-group">
                <van-field
                  v-model="formData.chinesename"
                  name="chinesename"
                  placeholder="请输入中文名"
                  class="glass-input"
                >
                  <template #left-icon>
                    <van-icon name="idcard" class="field-icon" />
                  </template>
                </van-field>
              </div>

              <!-- 邮箱 -->
              <div class="input-group">
                <van-field
                  v-model="formData.email"
                  name="email"
                  placeholder="请输入邮箱"
                  :rules="[{ required: true, message: '请填写邮箱' }, { pattern: /.+@.+\..+/, message: '格式不正确' }]"
                  class="glass-input"
                >
                  <template #left-icon>
                    <van-icon name="envelop-o" class="field-icon" />
                  </template>
                </van-field>
              </div>

              <!-- 密码 -->
              <div class="input-group">
                <van-field
                  v-model="formData.password"
                  type="password"
                  name="password"
                  placeholder="密码 (至少6位)"
                  :rules="[{ required: true, message: '请填写密码' }, { validator: (val) => val.length >= 6, message: '至少6个字符' }]"
                  class="glass-input"
                >
                  <template #left-icon>
                    <van-icon name="lock" class="field-icon" />
                  </template>
                </van-field>
              </div>

              <!-- 确认密码 -->
              <div class="input-group">
                <van-field
                  v-model="formData.confirmPassword"
                  type="password"
                  name="confirmPassword"
                  placeholder="再次输入密码"
                  :rules="[{ required: true, message: '请确认密码' }, { validator: validatePassMatch, message: '两次密码不一致' }]"
                  class="glass-input"
                >
                  <template #left-icon>
                    <van-icon name="checked" class="field-icon" />
                  </template>
                </van-field>
              </div>

              <div class="action-area">
                <van-button round block type="primary" native-type="submit" class="neon-btn">
                  下一步
                </van-button>
                <div class="sub-actions">
                  <span @click="toLogin" class="link-text">已有账号？去登录</span>
                </div>
              </div>
            </van-form>
          </div>

          <!-- 步骤 2: 安全问题一 -->
          <div v-else-if="currentStep === 2" key="step-2" class="form-step">
            <h3 class="step-title">安全验证 (1/3)</h3>
            <van-form ref="step2Form" @submit="nextStep" class="custom-form">
              <div class="input-group">
                <van-field
                  v-model="questionLabels.one"
                  is-link
                  readonly
                  name="saveQuestionOne"
                  placeholder="请选择安全问题一"
                  @click="openPicker('one')"
                  class="glass-input"
                  :rules="[{ required: true, message: '请选择问题' }]"
                >
                  <template #left-icon><van-icon name="question-o" class="field-icon" /></template>
                </van-field>
              </div>
              
              <div class="input-group">
                <van-field
                  v-model="formData.saveAnOne"
                  name="saveAnOne"
                  placeholder="请输入答案"
                  :rules="[{ required: true, message: '请输入答案' }]"
                  class="glass-input"
                >
                  <template #left-icon><van-icon name="edit" class="field-icon" /></template>
                </van-field>
              </div>

              <div class="action-area two-btns">
                <van-button round plain type="default" class="glass-btn" @click="prevStep">上一步</van-button>
                <van-button round type="primary" native-type="submit" class="neon-btn">下一步</van-button>
              </div>
            </van-form>
          </div>

          <!-- 步骤 3: 安全问题二 -->
          <div v-else-if="currentStep === 3" key="step-3" class="form-step">
            <h3 class="step-title">安全验证 (2/3)</h3>
            <van-form ref="step3Form" @submit="nextStep" class="custom-form">
              <div class="input-group">
                <van-field
                  v-model="questionLabels.twice"
                  is-link
                  readonly
                  placeholder="请选择安全问题二"
                  @click="openPicker('twice')"
                  class="glass-input"
                  :rules="[{ required: true, message: '请选择问题' }]"
                >
                   <template #left-icon><van-icon name="question-o" class="field-icon" /></template>
                </van-field>
              </div>

              <div class="input-group">
                <van-field
                  v-model="formData.saveAnTwice"
                  placeholder="请输入答案"
                  :rules="[{ required: true, message: '请输入答案' }]"
                  class="glass-input"
                >
                   <template #left-icon><van-icon name="edit" class="field-icon" /></template>
                </van-field>
              </div>

              <div class="action-area two-btns">
                <van-button round plain type="default" class="glass-btn" @click="prevStep">上一步</van-button>
                <van-button round type="primary" native-type="submit" class="neon-btn">下一步</van-button>
              </div>
            </van-form>
          </div>

          <!-- 步骤 4: 安全问题三 & 提交 -->
          <div v-else-if="currentStep === 4" key="step-4" class="form-step">
            <h3 class="step-title">安全验证 (3/3)</h3>
            <van-form ref="step4Form" @submit="handleRegister" class="custom-form">
              <div class="input-group">
                <van-field
                  v-model="questionLabels.three"
                  is-link
                  readonly
                  placeholder="请选择安全问题三"
                  @click="openPicker('three')"
                  class="glass-input"
                  :rules="[{ required: true, message: '请选择问题' }]"
                >
                   <template #left-icon><van-icon name="question-o" class="field-icon" /></template>
                </van-field>
              </div>

              <div class="input-group">
                <van-field
                  v-model="formData.saveAnThree"
                  placeholder="请输入答案"
                  :rules="[{ required: true, message: '请输入答案' }]"
                  class="glass-input"
                >
                   <template #left-icon><van-icon name="edit" class="field-icon" /></template>
                </van-field>
              </div>

              <div class="action-area two-btns">
                <van-button round plain type="default" class="glass-btn" @click="prevStep">上一步</van-button>
                <van-button 
                  round 
                  type="primary" 
                  native-type="submit" 
                  class="neon-btn"
                  :loading="loading"
                  loading-text="注册中..."
                >
                  完成注册
                </van-button>
              </div>
            </van-form>
          </div>
        </transition>
      </div>

      <div class="copyright">FreeMix &copy; {{ new Date().getFullYear() }}</div>
    </div>

    <!-- 底部弹出选择器 (Vant Picker) -->
    <van-popup v-model:show="showPicker" round position="bottom" class="glass-popup">
      <van-picker
        :columns="questionOptions"
        @cancel="showPicker = false"
        @confirm="onConfirmPicker"
        title="选择安全问题"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { postM } from '@/utils/request';
import { showToast, showSuccessToast, showFailToast } from 'vant';

const router = useRouter();
const loading = ref(false);
const currentStep = ref(1);

// 表单数据
const formData = reactive({
  username: '',
  chinesename: '', // 新增中文名字段
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

// 用于显示的选中问题文本（因为 select options 是 value/label 分离的）
const questionLabels = reactive({
  one: '',
  twice: '',
  three: ''
});

// 安全问题选项
const questionOptions = [
  { text: '出生的城市', value: 'birthcity' },
  { text: '大学的城市', value: 'county' },
  { text: '您的年龄', value: 'age' },
  { text: '您的父亲姓名', value: 'father' },
  { text: '您的母亲姓名', value: 'mother' }
];

// Picker 相关状态
const showPicker = ref(false);
const currentPickerType = ref<'one' | 'twice' | 'three'>('one');

const openPicker = (type: 'one' | 'twice' | 'three') => {
  currentPickerType.value = type;
  showPicker.value = true;
};

const onConfirmPicker = ({ selectedOptions }: any) => {
  const option = selectedOptions[0];
  const type = currentPickerType.value;
  
  if (type === 'one') {
    formData.saveQuestionOne = option.value;
    questionLabels.one = option.text;
  } else if (type === 'twice') {
    formData.saveQuestionTwice = option.value;
    questionLabels.twice = option.text;
  } else if (type === 'three') {
    formData.saveQuestionThree = option.value;
    questionLabels.three = option.text;
  }
  showPicker.value = false;
};

// 校验密码
const validatePassMatch = (val: string) => {
  return val === formData.password;
};

// 步骤控制
const nextStep = () => {
  currentStep.value++;
};

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
};

const toLogin = () => {
  router.push('/login');
};

// 最终注册逻辑
const handleRegister = async () => {
  loading.value = true;
  try {
    const res = await postM('register', formData);
    console.log("Register Res:", res);

    if (res.data.code === 200) {
      showSuccessToast('注册成功');
      setTimeout(() => {
        router.replace('/login');
      }, 1000);
    } else {
      showFailToast(res.data.msg || '注册失败');
      // 如果失败，可以选择是否重置步骤，或者停留在当前页
      // currentStep.value = 1; 
    }
  } catch (error) {
    console.error(error);
    showFailToast('网络请求失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
/* 复用 Source A 的样式并扩展 */
@import url('https://fonts.googleapis.com/css2?family=Rajdhani:wght@500;700&display=swap');

$primary-color: #00f2fe;
$secondary-color: #4facfe;
$bg-dark: #0f0c29;
$glass-bg: rgba(255, 255, 255, 0.05);
$glass-border: rgba(255, 255, 255, 0.1);
$text-main: #ffffff;
$text-sub: #b0bac9;

.mobile-login-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
  background: $bg-dark;
  font-family: 'Rajdhani', sans-serif;
  color: $text-main;
  display: flex;
  justify-content: center;
  align-items: center;
}

.animated-bg {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at center, #302b63 0%, #0f0c29 70%);
  animation: bgRotate 20s linear infinite;
  z-index: 0;
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%239C92AC' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  z-index: 1;
}

@keyframes bgRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.content-wrapper {
  position: relative;
  z-index: 10;
  width: 90%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.brand-section {
  text-align: center;
  margin-bottom: 25px;
  animation: float 6s ease-in-out infinite;

  .logo-box {
    width: 60px;
    height: 60px;
    margin: 0 auto 10px;
    
    .system-logo {
      width: 100%;
      height: 100%;
      border-radius: 15px;
      box-shadow: 0 0 20px rgba(0, 242, 254, 0.5);
    }
  }

  .app-title {
    font-size: 28px;
    font-weight: 700;
    margin: 0;
    background: linear-gradient(to right, $primary-color, #fff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    letter-spacing: 2px;
  }

  .app-subtitle {
    margin: 2px 0 0;
    font-size: 12px;
    color: $text-sub;
    text-transform: uppercase;
    letter-spacing: 3px;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-8px); }
}

.glass-card {
  width: 100%;
  background: $glass-bg;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid $glass-border;
  border-radius: 24px;
  padding: 30px 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.4);
  overflow: hidden; /* 防止滑动动画溢出 */
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 25px;

  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;

    &.active {
      background: $secondary-color;
    }
    
    &.current {
      background: $primary-color;
      transform: scale(1.3);
      box-shadow: 0 0 10px rgba(0, 242, 254, 0.6);
    }
  }
}

.step-title {
  text-align: center;
  color: #fff;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 500;
}

.custom-form {
  .input-group {
    margin-bottom: 18px;
    position: relative;
    transition: all 0.3s;
    
    &:focus-within {
      transform: scale(1.02);
    }
  }

  .glass-input {
    background: rgba(0, 0, 0, 0.3) !important;
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.08);
    padding: 10px 15px;
    color: #fff;
    align-items: center;
    transition: border-color 0.3s;

    &:focus-within {
      border-color: $primary-color;
    }

    /* 修复 Vant 在暗黑模式下的一些默认样式 */
    :deep(.van-field__cell) { background: transparent; }
    :deep(.van-field__control) {
      color: #fff;
      font-size: 16px;
      &::placeholder { color: rgba(255, 255, 255, 0.4); }
    }
    /* 去除 Vant 默认下划线 */
    &::after { display: none; }
    
    .field-icon {
      font-size: 20px;
      color: $secondary-color;
      margin-right: 10px;
    }
    
    /* 解决 Picker 图标对其问题 */
    :deep(.van-field__right-icon) {
      color: rgba(255,255,255,0.4);
    }
  }
}

.action-area {
  margin-top: 25px;
  
  &.two-btns {
    display: flex;
    gap: 15px;
    
    button {
      flex: 1;
    }
  }
}

.neon-btn {
  height: 46px;
  font-size: 16px;
  font-weight: bold;
  border: none;
  background: linear-gradient(90deg, #24c6dc, #514a9d);
  box-shadow: 0 4px 15px rgba(81, 74, 157, 0.4);
  border-radius: 24px;
  
  &:active { opacity: 0.9; }
}

.glass-btn {
  height: 46px;
  background: rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  
  &:active { background: rgba(255, 255, 255, 0.2) !important; }
}

.sub-actions {
  display: flex;
  justify-content: center;
  margin-top: 15px;
  
  .link-text {
    color: $text-sub;
    font-size: 14px;
    cursor: pointer;
    transition: color 0.2s;
    
    &:hover { color: $primary-color; }
  }
}

.copyright {
  margin-top: 30px;
  color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
}

/* 弹窗样式定制 */
.glass-popup {
  background: #1a1a2e; /* 深色背景 */
  
  :deep(.van-picker) {
    background: transparent;
    color: #fff;
  }
  :deep(.van-picker__mask) {
    background-image: linear-gradient(180deg, rgba(26, 26, 46, 0.9), rgba(26, 26, 46, 0.4)), linear-gradient(0deg, rgba(26, 26, 46, 0.9), rgba(26, 26, 46, 0.4));
  }
  :deep(.van-picker-column__item) {
    color: rgba(255, 255, 255, 0.6);
  }
  :deep(.van-picker-column__item--selected) {
    color: $primary-color;
    font-weight: bold;
  }
  :deep(.van-picker__toolbar) {
    background: rgba(255, 255, 255, 0.05);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  :deep(.van-picker__cancel), :deep(.van-picker__confirm) {
    color: $secondary-color;
  }
}

/* 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>