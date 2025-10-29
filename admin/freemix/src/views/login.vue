<template>
  <n-card style="width: 50%; margin: 25vh auto;" class="login-card">
    <!-- 登录表单 -->
    <div v-if="loginStep === 'login'">
      <n-tabs class="card-tabs" default-value="signin" size="large" animated pane-wrapper-style="margin: 0 -4px"
        pane-style="padding-left: 4px; padding-right: 4px; box-sizing: border-box;">
        <n-tab-pane name="signin" tab="登录">
          <n-form :rules="rules" ref="formRef" :model="user" @keydown.enter="handleLogin">
            <n-form-item-row label="用户名" path="username">
              <n-input placeholder="请输入用户名" v-model:value="user.username" @blur="loadCaptcha" />
            </n-form-item-row>
            <n-form-item-row label="密码" path="password">
              <n-input placeholder="请输入密码" type="password" show-password-on="click" v-model:value="user.password" />
            </n-form-item-row>
            <n-form-item-row label="验证码" path="captcha">
              <div class="captcha-container">
                <n-input 
                  placeholder="请输入计算结果" 
                  v-model:value="user.captcha" 
                  maxlength="10"
                  style="flex: 1;"
                />
                <div class="captcha-expression" @click="loadCaptcha">
                  {{ captchaExpression || '点击刷新' }}
                </div>
              </div>
            </n-form-item-row>
          </n-form>
          <n-button quaternary type="primary" style="margin: 10px;" @click="toRegister()">
        注册
      </n-button>
      
          <n-button type="primary" block secondary strong @click="handleLogin">
            登录
          </n-button>
          <n-divider dashed style="margin: 20px 0;">或</n-divider>
          <n-button 
            block 
            @click="handleGitHubLogin"
            class="github-login-btn"
            style="display: flex; align-items: center; justify-content: center;"
          >
            <template #icon>
              <n-icon>
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"><path fill="currentColor" d="M12 .297c-6.63 0-12 5.373-12 12c0 5.303 3.438 9.8 8.205 11.385c.6.113.82-.258.82-.577c0-.285-.01-1.04-.015-2.04c-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729c1.205.084 1.838 1.236 1.838 1.236c1.07 1.835 2.809 1.305 3.495.998c.108-.776.417-1.305.76-1.605c-2.665-.3-5.466-1.332-5.466-5.93c0-1.31.465-2.38 1.235-3.22c-.135-.303-.54-1.523.105-3.176c0 0 1.005-.322 3.3 1.23c.96-.267 1.98-.399 3-.405c1.02.006 2.04.138 3 .405c2.28-1.552 3.285-1.23 3.285-1.23c.645 1.653.24 2.873.12 3.176c.765.84 1.23 1.91 1.23 3.22c0 4.61-2.805 5.625-5.475 5.92c.42.36.81 1.096.81 2.22c0 1.606-.015 2.896-.015 3.286c0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/></svg>
              </n-icon>
            </template>
            <span style="margin-left: 8px; font-weight: 500;">使用 GitHub 登录</span>
          </n-button>
        </n-tab-pane>
       
      </n-tabs>
    </div>

    <!-- 双因素认证验证码输入 -->
    <div v-else-if="loginStep === '2fa-verify'" class="two-factor-auth-section">
      <h3>双因素认证</h3>
      <p>请输入Google Authenticator应用中的6位验证码：</p>
      <n-input-otp
      v-model:value="totpCode"
      ref="totpInputRef"
        style="margin-bottom: 20px;"
        @keydown.enter="verifyTwoFactorAuth"
      />
      <n-button type="primary" block @click="verifyTwoFactorAuth">
        验证并登录
      </n-button>
      <n-button quaternary @click="backToLogin">
        返回登录
      </n-button>
    </div>

    <!-- 双因素认证绑定 -->
    <div v-else-if="loginStep === '2fa-bind'" class="two-factor-auth-section">
      <h3>设置双因素认证</h3>
      <p>请完成双因素认证绑定以提升账户安全性：</p>
      <TwoFactorAuth :userId="tempUserData.id" parent="login" @update:router="updateTwoFactorAuth" />
      <n-button quaternary @click="backToLogin">
        返回登录
      </n-button>
    </div>
  </n-card>
</template>

<script setup lang="ts">
import { 
  NCard, 
  NTabs, 
  NTabPane, 
  NForm, 
  NFormItemRow, 
  NInput, 
  NButton, 
  type FormInst,
  useMessage, 
  NIcon,
  NInputOtp,
  NDivider
} from 'naive-ui';
import { onMounted, nextTick, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// @ts-ignore
import { useStore } from 'vuex';
import { postM, isSuccess } from '@/utils/request';
import { isDesktop } from '@/utils/device.js';
import { generateDesktopToken, saveLocalStorageDesktopToken } from '@/utils/desktopToken.js';
import { saveToken as saveTokenUtil } from '@/utils/tokenUtils.js'; // 导入token工具函数
import { RefreshOutline } from '@vicons/ionicons5';
import TwoFactorAuth from '@/components/TwoFactorAuth.vue';

const store = useStore();
const route = useRoute();
const router = useRouter();
const message = useMessage();

// 登录步骤状态
const loginStep = ref<'login' | '2fa-verify' | '2fa-bind'>('login');

// 表单验证规则
const rules = ref({
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
  captcha: { required: true, message: '请输入验证码', trigger: 'blur' }
});

const formRef = ref<FormInst | null>(null);
const totpInputRef = ref<any>(null);
const user = ref({
  username: '',
  password: '',
  captcha: ''
});
const totpCode = ref<string[]>([]);
const captchaExpression = ref('');
const tempUserData = ref<any>(null); // 临时存储登录成功但未完成2FA验证的用户数据

// 处理登录
const handleLogin = async () => {
  const valid = await formRef.value?.validate();
  if (valid) {
    // 准备登录数据
    const loginData = {
      username: user.value.username,
      password: user.value.password,
      captcha: user.value.captcha
    };
    
    try {
      const res = await postM('login', loginData);
      if (isSuccess(res)) {
        const userData = res.data.data;
        tempUserData.value = userData; // 临时保存用户数据
        
        // 检查用户是否有secretKey
        if (userData.secretKey && userData.secretKey.trim()) {
          // 有secretKey，显示验证码输入框
          loginStep.value = '2fa-verify';
          message.info('请输入双因素认证码');
        } else {
          // 没有secretKey，显示绑定组件
          loginStep.value = '2fa-bind';
          message.info('请完成双因素认证绑定');
        }
      } else {
        message.error(res.data.msg);
        await loadCaptcha(); // 刷新验证码
      }
    } catch (error) {
      console.error('登录请求失败:', error);
      message.error('登录失败，请稍后重试');
    }
  }
};
const updateTwoFactorAuth = async (val,secretKeyval) => { 
  console.log(val,secretKeyval);
  
  // 方法1：使用 split('') 将字符串拆成字符数组
    const charArray = val.split(''); // ["6", "0", "6", "0", "6"]
    console.log("charArray:",charArray);
    
    tempUserData.value.secretKey=secretKeyval
    // 方法2：将每个字符转为数字
    totpCode.value = await charArray.map(char => Number(char));
    console.log("tot",totpCode.value);
    
     verifyTwoFactorAuth();
};
// 验证双因素认证码
const verifyTwoFactorAuth = async () => {
  // 检查验证码是否完整
  if (!totpCode.value ) {
    console.log(!totpCode.value);
    console.log(totpCode.value.filter(Boolean).length !== 6);
    
    message.error('请输入完整的6位验证码');
    return;
  }
  
  try {
    // 合并验证码
    const code = totpCode.value.join('');
    console.log("tempUserData",tempUserData.value);
    
    // 发送验证请求
    const res = await postM('verify2fa', {
      userId: tempUserData.value.id,
      totpCode: code,
      secretKey: tempUserData.value.secretKey
    });
    
    if (isSuccess(res)) {
      let user=res.data.data
      // 验证成功，完成登录流程
      store.commit('saveUser', user);
  
      
      // 如果是桌面端，生成并保存桌面端token
      if (isDesktop()) {
        // 桌面端使用持久化存储
        
        // 生成并保存桌面端token
        const desktopToken = generateDesktopToken();
        saveTokenUtil(desktopToken);//本地token保存
        saveLocalStorageDesktopToken(desktopToken);
        // 发送请求到服务器验证并保存桌面端token
        try {
          await postM('verify-desktop-token', { desktopToken,username:user.username });
          console.log('桌面端token已保存到服务器');
        } catch (error) {
          console.error('保存桌面端token失败:', error);
        }
      }else{
         // 使用新的工具函数保存token
      await saveTokenUtil(user.token);
      }
      
      message.success('登录成功');
      router.push('/home');
    } else {
      message.error(res.data.msg || '验证码错误');
      // 清空验证码输入框
      totpCode.value = [];
    }
  } catch (error) {
    console.error('双因素认证验证失败:', error);
    message.error('验证失败，请稍后重试');
    totpCode.value = [];
  }
};

// 处理双因素认证绑定完成
// const handleTwoFactorBind = (enabled: boolean) => {
//   if (enabled) {
//     message.success('双因素认证绑定成功');
//     // 绑定成功后，保存用户数据并跳转到主页
//     if (tempUserData.value) {
//       store.commit('saveUser', tempUserData.value);
//       localStorage.setItem('token', tempUserData.value.token);
//       router.push('/home');
//     }
//   }
// };

// 返回登录页面
const backToLogin = () => {
  loginStep.value = 'login';
  totpCode.value = [];
  tempUserData.value = null;
};

// 跳转到注册页面
const toRegister = () => {
  router.replace('/register');
};

// 处理GitHub登录
      const handleGitHubLogin = () => {
        // 检查是否为桌面端
        if (isDesktop()) {
          // 桌面端直接访问后端服务器的完整URL
          window.location.href = 'https://freemix.bond/oauth2/authorization/github';
        } else {
          // Web端保持原有行为
          window.location.href = '/oauth2/authorization/github';
        }
      };

// 加载验证码
const loadCaptcha = async () => {
  if (!user.value.username) {
    return;
  }
  try {
    const res = await postM('/captcha', {"username": user.value.username});
    if (isSuccess(res)) {
      captchaExpression.value = res.data.data.expression;
    }
  } catch (error) {
    console.log(error);
    message.error('验证码加载失败');
  }
};


// 监听登录步骤变化，当切换到双因素认证时自动聚焦
watch(loginStep, (newStep) => {
  if (newStep === '2fa-verify') {
    nextTick(() => {
      setTimeout(() => {
        if (totpInputRef.value) {
          console.log("Focusing 2FA input via watch",totpInputRef.value.inputRefList[0].focus);
          
          totpInputRef.value.inputRefList[0].focus();
          console.log("Focused on 2FA input via watch");
        }
      }, 100);
    });
  }
});
</script>


<style scoped>
.card-tabs .n-tabs-nav--bar-type {
  padding-left: 4px;
  
}


.captcha-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-expression {
  min-width: 120px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #d9d9d9;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.captcha-placeholder:hover {
  background-color: #e6e6e6;
}

.login-card {
  background-color: transparent !important;
  backdrop-filter: blur(10px);
}

.login-card :deep(.n-card__content) {
  background-color: transparent !important;
}

.github-login-btn {
  background-color: #24292e !important;
  border-color: #24292e !important;
  color: white !important;
  transition: all 0.3s ease;
}

.github-login-btn:hover {
  background-color: #2c3238 !important;
  border-color: #2c3238 !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.github-login-btn:active {
  background-color: #1b1f23 !important;
  border-color: #1b1f23 !important;
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
</style>