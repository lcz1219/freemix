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
                  placeholder="请输入验证码" 
                  v-model:value="user.captcha" 
                  maxlength="4"
                  style="flex: 1;"
                />
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
            </n-form-item-row>
          </n-form>
          <n-button quaternary type="primary" style="margin: 10px;" @click="toRegister()">
        注册
      </n-button>
      
          <n-button type="primary" block secondary strong @click="handleLogin">
            登录
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
  NInputOtp
} from 'naive-ui';
import { onMounted, nextTick, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// @ts-ignore
import { useStore } from 'vuex';
import { postM, isSuccess } from '@/utils/request';
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
const captchaImage = ref('');
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
      await localStorage.setItem('token', user.token);
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

// 加载验证码
const loadCaptcha = async () => {
  if (!user.value.username) {
    return;
  }
  try {
    const res = await postM('/captcha', {"username": user.value.username});
    if (isSuccess(res)) {
      captchaImage.value = res.data.data.image;
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

.login-card {
  background-color: rgba(8, 8, 8, 0.566) !important;
  backdrop-filter: blur(10px);
}

.login-card :deep(.n-card__content) {
  background-color: transparent !important;
}
</style>