<template>
  <div class="two-factor-auth">
    <n-button quaternary type="info" v-if="props.parent == 'settings' && !ischange" @click="() => ischange = true">
      <n-icon>
        <AppsSharp />
      </n-icon>
      重新绑定双因素认证
    </n-button>
    <div class="form-group" v-else>

      <label>双因素认证</label>

      <div class="checkbox-group">
        <!-- <label class="checkbox-label">
          <input 
            v-model="twoFactorEnabled" 
            type="checkbox" 
            class="checkbox-input"
            @change="toggleTwoFactorAuth"
          />
          <span class="checkbox-text">启用双因素认证 (Google Authenticator)</span>
        </label> -->
      </div>

      <div class="qr-code-section">
        <p>请使用 Google Authenticator 应用扫描以下二维码：</p>
        <div class="qr-code-container">
          <!-- 直接使用原始的qrCodeUrl作为n-qr-code的value -->
          <!-- 移除图标以避免遮挡二维码内容，确保扫描正常 -->
          <span v-if="qrCodeUrl">
            <n-qr-code :value="qrCodeUrl" alt="QR Code"  class="qr-code" />
            <!-- <n-color-picker v-model:value="color"  /> -->
          </span>

          <p v-else>正在生成二维码...</p>
        </div>
        <p>或者手动输入密钥: <strong>{{ secretKey }}</strong></p>
        <div class="form-group">
          <label>验证码</label>
          <input v-model="totpCode" type="text" placeholder="输入6位验证码" class="form-input" maxlength="6"
            @input="onTotpCodeInput" />
          <button @click="verifyTwoFactorAuth" class="btn primary" style="margin-top: 10px;">验证并启用</button>
        </div>
      </div>

      <div v-if="twoFactorEnabled && isTwoFactorVerified" class="verification-success">
        <p style="color: #52c41a;">✓ 双因素认证已启用</p>
      </div>

      <div v-if="!twoFactorEnabled && hasTwoFactorEnabled" class="verification-success">
        <p style="color: #ff4d4f;">✗ 双因素认证已禁用</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
// @ts-ignore - 忽略vuex声明文件错误
import { useStore } from 'vuex';
import { AppsSharp } from '@vicons/ionicons5';


// @ts-ignore - 忽略qrcode声明文件错误
import QRCode from 'qrcode';
import { useMessage, NButton, NIcon, NQrCode,NColorPicker } from 'naive-ui';
import { postM, isSuccess } from '@/utils/request.js';
import { useRouter } from 'vue-router';
const router = useRouter();
const store = useStore();
const message = useMessage();
const ischange = ref(false);
const color = ref('#ff4d4f');
// 定义组件的事件
const emit = defineEmits(['update:enabled', 'update:router']);
onMounted(() => {
  // 获取用户信息
  enableTwoFactorAuth()
});
// 组件属性
const props = defineProps({
  initialEnabled: {
    type: Boolean,
    default: false
  },
  userId: {
    type: Number,
    default: 0
  },
  parent: {
    type: String,
    default: ''
  }
});

// 响应式数据
const twoFactorEnabled = ref(props.initialEnabled);
const totpCode = ref('');
const qrCodeUrl = ref('');
const qrCodeImage = ref('');
const secretKey = ref('');
const isTwoFactorVerified = ref(false);
const hasTwoFactorEnabled = ref(false);
const enableInProgress = ref(false); // 防止重复请求

// 生成二维码
const generateQRCode = async () => {
  try {
    console.log("二维码生成中");
    console.log("当前qrCodeUrl:", qrCodeUrl.value);

    // 确保qrCodeUrl有效
    if (!qrCodeUrl.value) {
      console.error('qrCodeUrl为空');
      message.error('生成二维码失败：URL为空');
      return;
    }

    console.log("二维码URL已准备好");
    // n-qr-code组件会自动处理qrCodeUrl生成二维码，无需手动转换
  } catch (error) {
    console.error('生成二维码失败', error);
    message.error('生成二维码失败，请尝试手动输入密钥');
  }
};

// 启用双因素认证
const enableTwoFactorAuth = async () => {
  if (enableInProgress.value) return; // 防止重复请求



  enableInProgress.value = true;
  try {
    // 启用双因素认证
    console.log("enableTwoFactorAuth");
    const res = await postM('enable2fa', {
      userId: store.state.user.id || props.userId
    });

    if (isSuccess(res)) {
      qrCodeUrl.value = res.data.data.qrCodeUrl;
      secretKey.value = res.data.data.secretKey;
      isTwoFactorVerified.value = false;
      hasTwoFactorEnabled.value = true;
      generateQRCode();
      emit('update:enabled', true);
    } else {
      message.error(res.data.msg);
      twoFactorEnabled.value = false;
    }
  } catch (error) {
    message.error('启用双因素认证失败');
    twoFactorEnabled.value = false;
  } finally {
    enableInProgress.value = false;
  }
};

// 禁用双因素认证
const disableTwoFactorAuth = async () => {
  try {
    // 禁用双因素认证
    const res = await postM('disable2fa', {
      userId: store.state.user.id
    });

    if (isSuccess(res)) {
      isTwoFactorVerified.value = false;
      qrCodeUrl.value = '';
      secretKey.value = '';
      hasTwoFactorEnabled.value = false;
      message.success('双因素认证已禁用');
      emit('update:enabled', false);
    } else {
      message.error(res.data.msg);
      twoFactorEnabled.value = true;
    }
  } catch (error) {
    message.error('禁用双因素认证失败');
    twoFactorEnabled.value = true;
  }
};

// 切换双因素认证
// const toggleTwoFactorAuth = async () => {
//   if (twoFactorEnabled.value) {
//     await enableTwoFactorAuth();
//   } else {
//     await disableTwoFactorAuth();
//   }
// };


// 验证双因素认证
const verifyTwoFactorAuth = async (): Promise<void> => {
  if (props.parent == 'login') {
    console.log('登录成功');
    emit('update:router', totpCode.value, secretKey.value);
    // router.push('/home');
    return;
  }
  if (!totpCode.value || totpCode.value.length !== 6 || !/^\d+$/.test(totpCode.value)) {
    message.error('请输入有效的6位数字验证码');
    return;
  }

  try {
    const res = await postM('verify2fa', {
      userId: store.state.user.id || props.userId,
      totpCode: totpCode.value,
      secretKey: secretKey.value
    });

    if (isSuccess(res)) {

      isTwoFactorVerified.value = true;
      message.success('双因素认证已启用');
    } else {
      // 细化错误提示
      if (res.data.msg.includes('验证码错误')) {
        message.error('验证码错误，请重新输入');
      } else if (res.data.msg.includes('过期')) {
        message.error('验证码已过期，请重新获取');
      } else {
        message.error(`验证失败: ${res.data.msg}`);
      }
      // 添加重试逻辑
      setTimeout(() => {
        totpCode.value = '';
      }, 1500);
    }
  } catch (error) {
    console.error('验证双因素认证时发生错误:', error);
    message.error('验证失败，请检查网络连接或尝试重新生成密钥');
    // 1.5秒后清空输入框，方便用户重试
    setTimeout(() => {
      totpCode.value = '';
    }, 1500);
  }
};

// 处理TOTP代码输入
const onTotpCodeInput = () => {
  // 自动验证6位数字
  if (totpCode.value.length === 6 && /^\d+$/.test(totpCode.value)) {
    // 可以自动验证，但为了用户体验，我们还是保留手动点击按钮
  }

  // 限制只能输入数字
  if (totpCode.value && !/^\d*$/.test(totpCode.value)) {
    totpCode.value = totpCode.value.replace(/\D/g, '');
  }
};

// 暴露方法给父组件
defineExpose({
  twoFactorEnabled,
  isTwoFactorVerified
});

// 监听初始启用状态变化
watch(() => props.initialEnabled, (newVal) => {
  twoFactorEnabled.value = newVal;
});
</script>

<style scoped>
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.checkbox-group {
  margin-bottom: 15px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-input {
  margin-right: 10px;
  width: 18px;
  height: 18px;
}

.checkbox-text {
  font-size: 16px;
}

.qr-code-section {
  margin-top: 20px;
  padding: 15px;
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.05);
}

.settings-container.dark .qr-code-section {
  background-color: rgba(255, 255, 255, 0.1);
}

.qr-code-container {
  display: flex;
  justify-content: center;
  margin: 15px 0;
}

.qr-code {
  max-width: 200px;
  height: auto;
}

.verification-success {
  margin-top: 10px;
  padding: 10px;
  border-radius: 5px;
  background-color: rgba(82, 196, 26, 0.1);
}

.form-input {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #ccc;
  font-size: 16px;
  box-sizing: border-box;
}

.settings-container.dark .form-input {
  background-color: rgba(20, 20, 30, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.settings-container.light .form-input {
  background-color: #ffffff;
  border: 1px solid #ddd;
  color: #000000;
}

.form-input:focus {
  outline: none;
  border-color: #8a2be2;
}

.btn {
  padding: 12px 20px;
  border-radius: 8px;
  border: none;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.btn.primary {
  background-color: #8a2be2;
  color: white;
}
</style>