<template>
  <div class="mobile-login-container">
    <!-- 动态背景层 -->
    <div class="animated-bg"></div>
    <div class="bg-overlay"></div>

    <div class="content-wrapper">
      <!-- 头部 Logo 区域 -->
      <div class="brand-section">
        <div class="logo-box">
          <img src="/icons/icon.png" alt="Logo" class="system-logo" />
        </div>
        <h1 class="app-title">FreeMix</h1>
        <p class="app-subtitle">目标管理系统</p>
      </div>

      <!-- 主体卡片区域 -->
      <div class="glass-card">
        
        <!-- 1. 登录表单 -->
        <transition name="fade-slide" mode="out-in">
          <div v-if="loginStep === 'login'" key="login-form">
            <van-form @submit="handleLogin" class="custom-form">
              <!-- 用户名 -->
              <div class="input-group">
                <van-field
                  v-model="user.username"
                  name="username"
                  placeholder="请输入用户名"
                  :rules="[{ required: true, message: '请填写用户名' }]"
                  class="glass-input"
                  autocomplete="off"
                  @blur="loadCaptcha"
                >
                  <template #left-icon>
                    <i class="iconfont icon-user"></i> <!-- 可以替换为 iconify 或 svg -->
                    <van-icon name="manager" class="field-icon" />
                  </template>
                </van-field>
              </div>

              <!-- 密码 -->
              <div class="input-group">
                <van-field
                  v-model="user.password"
                  type="password"
                  name="password"
                  placeholder="请输入密码"
                  :rules="[{ required: true, message: '请填写密码' }]"
                  class="glass-input"
                >
                  <template #left-icon>
                    <van-icon name="lock" class="field-icon" />
                  </template>
                </van-field>
              </div>

              <!-- 验证码 -->
              <div class="input-group captcha-group">
                <van-field
                  v-model="user.captcha"
                  name="captcha"
                  placeholder="计算结果"
                  :rules="[{ required: true, message: '必填' }]"
                  class="glass-input captcha-input"
                  maxlength="10"
                >
                  <template #left-icon>
                    <van-icon name="shield-o" class="field-icon" />
                  </template>
                </van-field>
                <div class="captcha-box" @click="loadCaptcha" v-ripple>
                  <span v-if="captchaExpression" class="captcha-text">{{ captchaExpression }}</span>
                  <span v-else class="captcha-text">点击获取</span>
                </div>
              </div>

              <!-- 登录按钮 -->
              <div class="action-area">
                <van-button 
                  round 
                  block 
                  type="primary" 
                  native-type="submit" 
                  class="neon-btn login-btn"
                  :loading="isLoading"
                  loading-text="登录中..."
                >
                  登 录
                </van-button>
                
                <div class="sub-actions">
                  <span @click="toRegister" class="link-text">注册账号</span>
                </div>
              </div>
            </van-form>

            <!-- 第三方登录 -->
            <div class="social-login">
              <div class="divider">
                <span>Or connect with</span>
              </div>
              <div class="social-icons">
                <div class="social-btn github" @click="handleGitHubLogin" v-ripple>
                  <svg viewBox="0 0 24 24" class="github-icon">
                    <path fill="currentColor" d="M12 .297c-6.63 0-12 5.373-12 12c0 5.303 3.438 9.8 8.205 11.385c.6.113.82-.258.82-.577c0-.285-.01-1.04-.015-2.04c-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729c1.205.084 1.838 1.236 1.838 1.236c1.07 1.835 2.809 1.305 3.495.998c.108-.776.417-1.305.76-1.605c-2.665-.3-5.466-1.332-5.466-5.93c0-1.31.465-2.38 1.235-3.22c-.135-.303-.54-1.523.105-3.176c0 0 1.005-.322 3.3 1.23c.96-.267 1.98-.399 3-.405c1.02.006 2.04.138 3 .405c2.28-1.552 3.285-1.23 3.285-1.23c.645 1.653.24 2.873.12 3.176c.765.84 1.23 1.91 1.23 3.22c0 4.61-2.805 5.625-5.475 5.92c.42.36.81 1.096.81 2.22c0 1.606-.015 2.896-.015 3.286c0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12" />
                  </svg>
                  <span>GitHub</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 2. 双因素认证验证 -->
          <div v-else-if="loginStep === '2fa-verify'" key="2fa-verify" class="step-container">
            <div class="step-header">
              <h3>安全验证</h3>
              <p>请输入 Google Authenticator 的6位动态码</p>
            </div>
            
            <div class="otp-container">
               <!-- 自定义独立验证码输入框 -->
               <div class="otp-box-wrapper">
                 <input
                   v-for="(digit, index) in 6"
                   :key="index"
                   ref="otpInputs"
                   v-model="otpDigits[index]"
                   type="tel"
                   inputmode="numeric"
                   maxlength="1"
                   class="otp-input"
                   @input="onOtpInput(index, $event)"
                   @keydown="onOtpKeydown(index, $event)"
                   @paste="onOtpPaste"
                   @focus="onOtpFocus"
                 />
               </div>
            </div>
            
            <div class="step-actions">
              <van-button block type="primary" class="neon-btn" @click="verifyTwoFactorAuth" :loading="isLoading">
                验证并登录
              </van-button>
              <van-button plain block type="default" class="glass-btn mt-10" @click="backToLogin">
                返回登录
              </van-button>
            </div>


          </div>

          <!-- 3. 双因素认证绑定 -->
          <div v-else-if="loginStep === '2fa-bind'" key="2fa-bind" class="step-container">
            <div class="step-header">
              <h3>安全绑定</h3>
              <p>为了您的账户安全，请绑定双因素认证</p>
            </div>
            
            <div class="bind-component-wrapper">
              <!-- 复用已有的组件，注意在移动端需要调整 TwoFactorAuth 的 CSS 适配 -->
              <TwoFactorAuth 
                :userId="tempUserData?.id" 
                parent="login" 
                @update:router="updateTwoFactorAuth" 
              />
            </div>
            
            <div class="step-actions">
              <van-button plain block type="default" class="glass-btn mt-10" @click="backToLogin">
                返回登录
              </van-button>
            </div>
          </div>
        </transition>
      </div>

      <div class="copyright">FreeMix &copy; {{ new Date().getFullYear() }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// @ts-ignore
import { useStore } from 'vuex';
import { postM, isSuccess } from '@/utils/request';
import { isDesktop } from '@/utils/device.js';
import { generateDesktopToken, saveLocalStorageDesktopToken } from '@/utils/desktopToken.js';
import { saveToken as saveTokenUtil } from '@/utils/tokenUtils.js';
import TwoFactorAuth from '@/components/TwoFactorAuth.vue';
import { showToast, showLoadingToast, closeToast } from 'vant';

// 如果没有 v-ripple 指令，可以移除 `v-ripple` 或者自己实现一个简单的
const vRipple = {
  mounted: (el: HTMLElement) => {
    el.style.position = 'relative';
    el.style.overflow = 'hidden';
    el.addEventListener('click', (e) => {
      const span = document.createElement('span');
      span.className = 'ripple-effect';
      const rect = el.getBoundingClientRect();
      const size = Math.max(rect.width, rect.height);
      span.style.width = span.style.height = `${size}px`;
      span.style.left = `${e.clientX - rect.left - size / 2}px`;
      span.style.top = `${e.clientY - rect.top - size / 2}px`;
      el.appendChild(span);
      setTimeout(() => span.remove(), 600);
    });
  }
};

const store = useStore();
const router = useRouter();
const isProduction = import.meta.env.PROD;

// 状态管理
const loginStep = ref<'login' | '2fa-verify' | '2fa-bind'>('login');
const isLoading = ref(false);
const user = ref({
  username: '',
  password: '',
  captcha: ''
});
const captchaExpression = ref('');
const tempUserData = ref<any>(null);

// 2FA 相关
const totpCodeStr = ref(''); 
const otpDigits = ref(['', '', '', '', '', '']);
const otpInputs = ref<HTMLInputElement[]>([]);

// Sync otpDigits -> totpCodeStr
watch(otpDigits, (newVal) => {
  totpCodeStr.value = newVal.join('');
}, { deep: true });

// Sync totpCodeStr -> otpDigits
watch(totpCodeStr, (newVal) => {
  if (newVal === '') {
     otpDigits.value = ['', '', '', '', '', ''];
     return;
  }
  // Auto submit
  if (newVal.length === 6 && loginStep.value === '2fa-verify') {
    verifyTwoFactorAuth();
  }
});

// 监听步骤变化
watch(loginStep, (newStep) => {
  if (newStep === '2fa-verify') {
    nextTick(() => {
      setTimeout(() => {
        otpInputs.value[0]?.focus();
      }, 300);
    });
  }
});

const onOtpInput = (index: number, event: Event) => {
  const input = event.target as HTMLInputElement;
  const val = input.value;
  
  if (val.length > 1) {
    const chars = val.split('').slice(0, 6);
    chars.forEach((char, i) => {
      if (index + i < 6) {
        otpDigits.value[index + i] = char;
      }
    });
    const nextIndex = Math.min(index + chars.length, 5);
    nextTick(() => otpInputs.value[nextIndex]?.focus());
    return;
  }

  otpDigits.value[index] = val;
  if (val && index < 5) {
    nextTick(() => otpInputs.value[index + 1]?.focus());
  }
};

const onOtpKeydown = (index: number, event: KeyboardEvent) => {
  const key = event.key;
  if (key === 'Backspace') {
    if (!otpDigits.value[index] && index > 0) {
      otpDigits.value[index - 1] = '';
      nextTick(() => otpInputs.value[index - 1]?.focus());
    }
  } else if (key === 'ArrowLeft') {
    if (index > 0) otpInputs.value[index - 1]?.focus();
  } else if (key === 'ArrowRight') {
    if (index < 5) otpInputs.value[index + 1]?.focus();
  }
};

const onOtpPaste = (event: ClipboardEvent) => {
  event.preventDefault();
  const text = event.clipboardData?.getData('text');
  if (text) {
    const chars = text.replace(/\D/g, '').split('').slice(0, 6);
    chars.forEach((char, i) => {
      otpDigits.value[i] = char;
    });
    nextTick(() => {
        const focusIndex = Math.min(chars.length, 5);
        otpInputs.value[focusIndex]?.focus();
    });
  }
};

const onOtpFocus = (event: FocusEvent) => {
  (event.target as HTMLInputElement).select();
};

// 加载验证码
const loadCaptcha = async () => {
  if (!user.value.username) return;
  try {
    const res = await postM('/captcha', { "username": user.value.username });
    if (isSuccess(res)) {
      captchaExpression.value = res.data.data.expression;
    }
  } catch (error) {
    console.error(error);
    showToast({ type: 'fail', message: '验证码加载失败', position: 'top' });
  }
};

// 处理开发环境登录
const handleDevLogin = async (userData: any) => {
  store.commit('saveUser', userData);
  
  if (isDesktop()) {
    const desktopToken = generateDesktopToken();
    saveTokenUtil(desktopToken);
    saveLocalStorageDesktopToken(desktopToken);
    try {
      await postM('verify-desktop-token', { desktopToken, username: userData.username });
    } catch (error) {
      console.error('保存桌面端token失败:', error);
    }
  } else {
    await saveTokenUtil(userData.token);
  }
  
  showToast({ type: 'success', message: '登录成功' });
  router.push('/home');
};

// 处理生产环境逻辑
const handleProdLogin = async (userData: any) => {
  if (userData.secretKey && userData.secretKey.trim()) {
    loginStep.value = '2fa-verify';
  } else {
    loginStep.value = '2fa-bind';
    showToast('请完成双因素认证绑定');
  }
};

// 点击登录
const handleLogin = async () => {
  if(!user.value.username || !user.value.password || !user.value.captcha) return;

  isLoading.value = true;
  const loginData = {
    username: user.value.username,
    password: user.value.password,
    captcha: user.value.captcha
  };

  try {
    const res = await postM('login', loginData);
    if (isSuccess(res)) {
      const userData = res.data.data;
      tempUserData.value = userData;
      
      if (!isProduction) {
        await handleDevLogin(userData);
      } else {
        await handleProdLogin(userData);
      }
    } else {
      showToast({ type: 'fail', message: res.data.msg || '登录失败', position: 'top' });
      await loadCaptcha();
    }
  } catch (error) {
    console.error('登录请求失败:', error);
    showToast({ type: 'fail', message: '服务连接失败', position: 'top' });
  } finally {
    isLoading.value = false;
  }
};

// 2FA绑定回调
const updateTwoFactorAuth = async (val: string, secretKeyval: string) => {
  tempUserData.value.secretKey = secretKeyval;
  totpCodeStr.value = val; // 将回调的验证码填入
  verifyTwoFactorAuth();
};

// 验证 2FA
const verifyTwoFactorAuth = async () => {
  if (totpCodeStr.value.length !== 6) {
    showToast('请输入6位验证码');
    return;
  }
  
  isLoading.value = true;
  try {
    const res = await postM('verify2fa', {
      userId: tempUserData.value.id,
      totpCode: totpCodeStr.value,
      secretKey: tempUserData.value.secretKey
    });

    if (isSuccess(res)) {
      const userData = res.data.data;
      store.commit('saveUser', userData);

      if (isDesktop()) {
        const desktopToken = generateDesktopToken();
        saveTokenUtil(desktopToken);
        saveLocalStorageDesktopToken(desktopToken);
        try {
          await postM('verify-desktop-token', { desktopToken, username: userData.username });
        } catch (error) {
          console.error(error);
        }
      } else {
        await saveTokenUtil(userData.token);
      }

      showToast({ type: 'success', message: '登录成功' });
      router.push('/home');
    } else {
      showToast({ type: 'fail', message: res.data.msg || '验证码错误' });
      totpCodeStr.value = ''; // 清空
    }
  } catch (error) {
    showToast({ type: 'fail', message: '验证失败' });
    totpCodeStr.value = '';
  } finally {
    isLoading.value = false;
  }
};

const backToLogin = () => {
  loginStep.value = 'login';
  totpCodeStr.value = '';
  tempUserData.value = null;
  // loadCaptcha(); // 可选：返回时刷新验证码
};

const toRegister = () => {
  router.replace('/register');
};

const handleGitHubLogin = () => {
  if (isDesktop()) {
    window.location.href = 'https://freemix.bond/oauth2/authorization/github';
  } else {
    window.location.href = '/oauth2/authorization/github';
  }
};
</script>

<style scoped lang="scss">
/* 引入 Google Fonts 提升质感 */
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
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  padding-top: env(safe-area-inset-top);
  box-sizing: border-box;
}

/* 动态背景动画 */
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

/* Logo 区域 */
.brand-section {
  text-align: center;
  margin-bottom: 30px;
  animation: float 6s ease-in-out infinite;

  .logo-box {
    width: 80px;
    height: 80px;
    margin: 0 auto 10px;
    
    .system-logo {
      width: 100%;
      height: 100%;
      border-radius: 20px;
      box-shadow: 0 0 20px rgba(0, 242, 254, 0.5);
    }
  }

  .app-title {
    font-size: 32px;
    font-weight: 700;
    margin: 0;
    background: linear-gradient(to right, $primary-color, #fff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    letter-spacing: 2px;
  }

  .app-subtitle {
    margin: 5px 0 0;
    font-size: 14px;
    color: $text-sub;
    text-transform: uppercase;
    letter-spacing: 4px;
  }
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}

/* 玻璃卡片主体 */
.glass-card {
  width: 100%;
  background: $glass-bg;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid $glass-border;
  border-radius: 24px;
  padding: 30px 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.4);
}

/* Vant 表单定制 - 暗黑风格 */
.custom-form {
  .input-group {
    margin-bottom: 20px;
    position: relative;
    transition: all 0.3s;

    &:focus-within {
      transform: scale(1.02);
    }
  }

  .glass-input {
    background: rgba(0, 0, 0, 0.3) !important;
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.08); // 默认边框
    padding: 10px 15px;
    color: #fff;
    align-items: center;
    transition: border-color 0.3s;

    /* 输入框得到焦点时的边框颜色 */
    &:focus-within {
      border-color: $primary-color;
    }

    /* 覆盖 Vant 默认背景 */
    :deep(.van-field__cell) {
      background: transparent;
    }
    :deep(.van-field__control) {
      color: #fff;
      font-size: 16px;
    
      &::placeholder {
        color: rgba(255, 255, 255, 0.4);
      }
    }
    
    .field-icon {
      font-size: 20px;
      color: $secondary-color;
      margin-right: 10px;
    }
  }
}

/* 验证码特殊布局 */
.captcha-group {
  display: flex !important;
  gap: 10px;
  
  .captcha-input {
    flex: 6;
  }
  
  .captcha-box {
    flex: 4;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    border: 1px solid rgba(255, 255, 255, 0.08);
    transition: background 0.3s;

    &:active {
      background: rgba(255, 255, 255, 0.2);
    }

    .captcha-text {
      font-weight: bold;
      color: $primary-color;
      font-size: 18px;
      text-shadow: 0 0 5px rgba(0, 242, 254, 0.3);
    }
  }
}

/* 按钮样式 */
.login-btn {
  margin-top: 10px;
  height: 48px;
  font-size: 18px;
  font-weight: bold;
  border: none;
  background: linear-gradient(90deg, #24c6dc, #514a9d);
  box-shadow: 0 4px 15px rgba(81, 74, 157, 0.4);
  
  &:active {
    opacity: 0.9;
  }
}

.sub-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  
  .link-text {
    color: $text-sub;
    font-size: 14px;
    cursor: pointer;
    transition: color 0.2s;
    
    &:hover {
      color: $primary-color;
    }
  }
}

/* 第三方登录 */
.social-login {
  margin-top: 30px;
  text-align: center;

  .divider {
    position: relative;
    margin-bottom: 20px;
    
    span {
      position: relative;
      z-index: 1;
      background: transparent; /* 复杂背景下不需要背景色遮挡 */
      padding: 0 10px;
      color: rgba(255, 255, 255, 0.4);
      font-size: 12px;
    }
    
    /* 使用伪元素画线 */
    &::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 10%;
      right: 10%;
      height: 1px;
      background: rgba(255, 255, 255, 0.1);
      z-index: 0;
    }
  }

  .github {
    background: #24292e;
    color: white;
    padding: 10px 20px;
    border-radius: 30px;
    display: inline-flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    box-shadow: 0 5px 15px rgba(0,0,0,0.3);
    border: 1px solid rgba(255,255,255,0.1);

    .github-icon {
      width: 20px;
      height: 20px;
    }

    &:active {
      transform: scale(0.95);
    }
  }
}

/* 2FA 步骤样式 */
.step-container {
  color: #fff;
  text-align: center;

  .step-header {
    margin-bottom: 30px;
    h3 { margin: 0 0 10px 0; font-size: 24px; }
    p { margin: 0; color: $text-sub; font-size: 14px; }
  }

  /* 自定义独立 OTP 输入框 */
   .otp-box-wrapper {
     display: flex;
     justify-content: space-between;
     gap: 8px;
     margin-bottom: 30px;
     padding: 0; /* 移除内边距以获得更多空间 */
     width: 100%;
     
     .otp-input {
       flex: 1; /* 自适应宽度 */
       max-width: 48px; /* 限制最大宽度 */
       min-width: 0; /* 允许缩小 */
       aspect-ratio: 6 / 7; /* 保持宽高比 48:56 */
       height: auto; /* 高度随宽度自动调整 */
       
       background: rgba(0, 0, 0, 0.3);
       color: $primary-color;
       border: 1px solid rgba(255, 255, 255, 0.1);
       border-radius: 12px;
       font-size: 24px;
       font-weight: bold;
       text-align: center;
       outline: none;
       transition: all 0.3s ease;
       
       &:focus {
         border-color: $primary-color;
         box-shadow: 0 0 15px rgba(0, 242, 254, 0.3);
         background: rgba(0, 0, 0, 0.5);
         transform: translateY(-2px);
       }
       
       &::selection {
         background: rgba(0, 242, 254, 0.3);
         color: #fff;
       }
     }
   }

   /* 小屏设备适配 */
   @media (max-width: 375px) {
     .otp-box-wrapper {
       gap: 4px;
       
       .otp-input {
         font-size: 20px;
         border-radius: 8px;
       }
     }
   }
}

.mt-10 {
  margin-top: 10px;
}

.glass-btn {
  background: rgba(255, 255, 255, 0.05) !important;
  color: #fff !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

.copyright {
  margin-top: 30px;
  color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
}

/* 波纹动画 */
:deep(.ripple-effect) {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  transform: scale(0);
  animation: ripple 0.6s linear;
  pointer-events: none;
}

@keyframes ripple {
  to { transform: scale(4); opacity: 0; }
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>