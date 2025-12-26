<template>
  <div>
    <!-- 背景光效 (可选，增加高级感) -->
    <div class="ambient-light" v-if="isDesktopEnv"></div>

    <n-card :style="cardStyle" class="login-card">
      <!-- 系统图标 -->
      <div class="logo-container">
        <div class="logo-wrapper">
          <div class="logo-glow"></div>
          <img src="/icons/icon.png" alt="系统图标" class="system-logo" />
        </div>
        <h2 class="app-title">FreeMix-目标管理系统</h2>
      </div>

      <!-- 1. 初始登录表单 -->
      <div v-if="loginStep === 'login'" class="fade-in-up">
        <n-tabs
          class="card-tabs"
          v-model:value="activeLoginTab"
          size="large"
          animated
          pane-wrapper-style="margin: 0 -4px"
          pane-style="padding-left: 4px; padding-right: 4px; box-sizing: border-box;">
           <n-tab-pane name="qr-login" tab="扫码登录">
            <div class="qr-login-container">
              <div class="qr-login-left">
                <div class="qr-image-wrapper">
                  <img v-if="qrCodeDataUrl" :src="qrCodeDataUrl" alt="登录二维码" class="qr-image" />
                  <div v-else class="qr-placeholder">
                    <span>二维码生成中</span>
                  </div>
                  <div v-if="qrStatus === 'expired'" class="qr-overlay">
                    <span>二维码已过期</span>
                    <n-button size="small" type="primary" @click="refreshQrCode">刷新</n-button>
                  </div>
                </div>
                <div class="qr-tips">
                  <p>使用已登录的移动端 FreeMix 扫码确认登录</p>
                  <p v-if="qrCountdown > 0">二维码将在 {{ qrCountdown }} 秒后失效</p>
                </div>
              </div>
              <div class="qr-login-right">
                <ol class="qr-steps">
                  <li>打开移动端 FreeMix 应用</li>
                  <li>登录后进入个人中心或设置页</li>
                  <li>点击扫码登录，扫描左侧二维码</li>
                  <li>在手机上确认本次登录</li>
                </ol>
                <div class="qr-status-text">
                  <span v-if="qrStatus === 'pending'">等待手机确认登录...</span>
                  <span v-else-if="qrStatus === 'approved'">已确认，正在登录...</span>
                  <span v-else-if="qrStatus === 'error'">登录出错，请刷新二维码重试</span>
                </div>
              </div>
            </div>
          </n-tab-pane>
          <n-tab-pane name="signin" tab="登录">
            <n-form :rules="rules" ref="formRef" :model="user" @keydown.enter="prepareLogin">
              <n-form-item-row label="用户名" path="username">
                <n-input placeholder="请输入用户名" v-model:value="user.username" @blur="loadCaptcha">
                  <template #prefix><n-icon :component="PersonOutline" /></template>
                </n-input>
              </n-form-item-row>
              <n-form-item-row label="密码" path="password">
                <n-input placeholder="请输入密码" type="password" show-password-on="click" v-model:value="user.password">
                  <template #prefix><n-icon :component="LockClosedOutline" /></template>
                </n-input>
              </n-form-item-row>
              <n-form-item-row label="验证码" path="captcha">
                <div class="captcha-container">
                  <n-input placeholder="请输入计算结果" v-model:value="user.captcha" maxlength="10" style="flex: 1;">
                    <template #prefix><n-icon :component="KeyOutline" /></template>
                  </n-input>
                  <div class="captcha-expression" @click="loadCaptcha">
                    {{ captchaExpression || '点击刷新' }}
                  </div>
                </div>
              </n-form-item-row>
            </n-form>
            <n-button quaternary type="primary" style="margin: 10px;" @click="toRegister()">
              注册
            </n-button>

            <n-button type="primary" block secondary strong @click="prepareLogin" class="login-btn-gradient">
              登录
            </n-button>
            <n-divider dashed style="margin: 20px 0;">或</n-divider>
            <n-button block @click="handleGitHubLogin" class="github-login-btn"
              style="display: flex; align-items: center; justify-content: center;">
              <template #icon>
                <n-icon>
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24">
                    <path fill="currentColor"
                      d="M12 .297c-6.63 0-12 5.373-12 12c0 5.303 3.438 9.8 8.205 11.385c.6.113.82-.258.82-.577c0-.285-.01-1.04-.015-2.04c-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729c1.205.084 1.838 1.236 1.838 1.236c1.07 1.835 2.809 1.305 3.495.998c.108-.776.417-1.305.76-1.605c-2.665-.3-5.466-1.332-5.466-5.93c0-1.31.465-2.38 1.235-3.22c-.135-.303-.54-1.523.105-3.176c0 0 1.005-.322 3.3 1.23c.96-.267 1.98-.399 3-.405c1.02.006 2.04.138 3 .405c2.28-1.552 3.285-1.23 3.285-1.23c.645 1.653.24 2.873.12 3.176c.765.84 1.23 1.91 1.23 3.22c0 4.61-2.805 5.625-5.475 5.92c.42.36.81 1.096.81 2.22c0 1.606-.015 2.896-.015 3.286c0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12" />
                  </svg>
                </n-icon>
              </template>
              <span style="margin-left: 8px; font-weight: 500;">使用 GitHub 登录</span>
            </n-button>
          </n-tab-pane>
         
        </n-tabs>
      </div>

      <!-- 2. 点选验证 (Click Sequence Captcha) - 替换了原来的滑块 -->
      <div v-else-if="loginStep === 'human-verify'" class="verify-section fade-in-scale">
        <div class="verify-header">
          <n-icon size="36" color="#409eff" style="margin-bottom: 8px;">
            <finger-print-outline />
          </n-icon>
          <h3>安全验证</h3>
          <p v-if="clickStatus !== 'success'">
            请 <span class="highlight-text">顺序点击</span> 图中的：
            <span class="target-chars">
              <span v-for="(char, index) in targetChars" :key="index" class="target-char">
                「{{ char }}」
              </span>
            </span>
          </p>
          <p v-else class="success-text">验证通过</p>
        </div>

        <!-- 验证码容器 -->
        <div class="click-captcha-container" :class="{ 'is-shaking': isShake }">
          <!-- 图片 Canvas -->
          <canvas 
            ref="captchaCanvasRef" 
            width="320" 
            height="160" 
            class="captcha-canvas"
            @click="handleCanvasClick"
          ></canvas>

          <!-- 点击标记点 (Markers) -->
          <div 
            v-for="(point, index) in clickPoints" 
            :key="index"
            class="click-marker"
            :style="{ left: point.x + 'px', top: point.y + 'px' }"
          >
            {{ index + 1 }}
          </div>

          <!-- 刷新按钮 -->
          <div class="refresh-btn" @click="initClickCaptcha" title="看不清？换一张">
            <n-icon size="18"><refresh /></n-icon>
          </div>

          <!-- 成功覆盖层 -->
          <div class="success-overlay" v-if="clickStatus === 'success'">
            <n-icon size="48" color="#10b981"><checkmark-circle /></n-icon>
          </div>
        </div>

        <div class="verify-footer">
          <n-button quaternary size="small" @click="backToLogin" class="back-btn">
            <template #icon><n-icon><arrow-back /></n-icon></template>
            返回修改
          </n-button>
        </div>
      </div>

      <!-- 3. 双因素认证验证码输入 -->
      <div v-else-if="loginStep === '2fa-verify'" class="two-factor-auth-section fade-in-right">
        <div class="auth-icon-circle">
          <n-icon size="32"><lock-closed /></n-icon>
        </div>
        <h3>双因素认证</h3>
        <p>请输入Google Authenticator应用中的6位验证码：</p>
        <div class="otp-wrapper">
          <n-input-otp v-model:value="totpCode" ref="totpInputRef" size="large" :length="6"
          @keydown.enter="verifyTwoFactorAuth" />
        </div>
        <n-button type="primary" block @click="verifyTwoFactorAuth" class="verify-btn">
          验证并登录
        </n-button>
        <n-button quaternary @click="backToLogin">
          返回登录
        </n-button>
      </div>

      <!-- 4. 双因素认证绑定 -->
      <div v-else-if="loginStep === '2fa-bind'" class="two-factor-auth-section fade-in-right">
        <h3>设置双因素认证</h3>
        <p>请完成双因素认证绑定以提升账户安全性：</p>
        <TwoFactorAuth :userId="tempUserData.id" parent="login" @update:router="updateTwoFactorAuth" />
        <n-button quaternary @click="backToLogin">
          返回登录
        </n-button>
      </div>
    </n-card>
  </div>
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
import { 
  PersonOutline, 
  LockClosedOutline, 
  KeyOutline, 
  Refresh, 
  CheckmarkCircle, 
  ArrowBack, 
  FingerPrintOutline, 
  LockClosed 
} from '@vicons/ionicons5';
import { onMounted, nextTick, ref, watch, computed, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// @ts-ignore
import { useStore } from 'vuex';
import { postM, getM, isSuccess } from '@/utils/request';
import { isDesktop } from '@/utils/device.js';
import { generateDesktopToken, saveLocalStorageDesktopToken } from '@/utils/desktopToken.js';
import { saveToken as saveTokenUtil } from '@/utils/tokenUtils.js';
import TwoFactorAuth from '@/components/TwoFactorAuth.vue';
import QRCode from 'qrcode';

const store = useStore();
const route = useRoute();
const router = useRouter();
const message = useMessage();

// 检查是否为生产环境
const isProduction = import.meta.env.PROD;
const isDesktopEnv = isDesktop();

// 计算卡片样式
const cardStyle = computed(() => {
  if (isDesktopEnv) {
    // 桌面端环境下卡片占满整个窗口
    return {
      width: '100%',
      height: '100vh',
      margin: 0,
      borderRadius: 0
    };
  } else {
    // Web端保持原有样式
    return {
      width: '480px', // 稍微固定宽度以适应Canvas
      marginTop: '10vh',
      marginLeft: 'auto',
      marginRight: 'auto'
    };
  }
});

// 登录步骤状态
const loginStep = ref<'login' | 'human-verify' | '2fa-verify' | '2fa-bind'>('login');
const activeLoginTab = ref<'signin' | 'qr-login'>('qr-login');

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
const tempUserData = ref<any>(null);

// --- 点选验证相关状态 (Click Captcha) ---
const captchaCanvasRef = ref<HTMLCanvasElement | null>(null);
const clickPoints = ref<{x: number, y: number}[]>([]);
const targetChars = ref<string[]>([]); // 需要点击的字
const allChars = ref<{char: string, x: number, y: number}[]>([]); // 画布上所有的字坐标
const clickStatus = ref<'pending' | 'verifying' | 'success'>('pending');
const isShake = ref(false);

const clearQrTimers = () => {
  if (qrCountdownTimer !== null) {
    window.clearInterval(qrCountdownTimer);
    qrCountdownTimer = null;
  }
  if (qrStatusTimer !== null) {
    window.clearInterval(qrStatusTimer);
    qrStatusTimer = null;
  }
};

const initQrLogin = async () => {
  qrStatus.value = 'idle';
  qrCodeDataUrl.value = '';
  qrSessionId.value = '';
  qrCountdown.value = 0;
  clearQrTimers();
  try {
    const res = await postM('qr-login/create', {});
    if (!isSuccess(res)) {
      qrStatus.value = 'error';
      message.error(res.data.msg || '二维码创建失败');
      return;
    }
    const data = res.data.data;
    qrSessionId.value = data.sessionId;
    const sessionToken = data.sessionToken;
    const expiresIn = data.expiresIn || 120;
    qrCountdown.value = expiresIn;
    const payload = {
        type: 'freemix-qr-login',
        sessionId: qrSessionId.value,
        sessionToken: sessionToken,
        ts: Date.now()
      };
      
      const origin = window.location.origin;
      const pathname = window.location.pathname;
      // 构造完整URL，兼容hash路由
      const confirmUrl = `${origin}${pathname}#/mobile/qr-confirm?data=${encodeURIComponent(JSON.stringify(payload))}`;
      
      qrCodeDataUrl.value = await QRCode.toDataURL(confirmUrl);
      qrStatus.value = 'pending';
    qrCountdownTimer = window.setInterval(() => {
      if (qrCountdown.value > 0) {
        qrCountdown.value -= 1;
      } else {
        qrStatus.value = 'expired';
        clearQrTimers();
      }
    }, 1000);
    qrStatusTimer = window.setInterval(async () => {
      if (!qrSessionId.value || qrStatus.value !== 'pending') {
        return;
      }
      try {
        const statusRes = await getM('qr-login/status', { sessionId: qrSessionId.value });
        if (!isSuccess(statusRes)) {
          return;
        }
        const s = statusRes.data.data;
        const status = s.status;
        if (status === 'APPROVED') {
          qrStatus.value = 'approved';
          clearQrTimers();
          await handleQrApprovedLogin(s);
        } else if (status === 'EXPIRED') {
          qrStatus.value = 'expired';
          clearQrTimers();
        }
      } catch (e) {
        console.error('轮询扫码登录状态失败', e);
      }
    }, 2000);
  } catch (e) {
    console.error('创建扫码登录会话失败', e);
    qrStatus.value = 'error';
    message.error('二维码创建失败');
  }
};

const handleQrApprovedLogin = async (payload: any) => {
  const userData = payload.user || {};
  const token = payload.token;
  if (userData && userData.username) {
    store.commit('saveUser', userData);
  }
  if (token) {
    if (isDesktopEnv) {
      const desktopToken = generateDesktopToken();
      saveTokenUtil(desktopToken);
      saveLocalStorageDesktopToken(desktopToken);
      try {
        await postM('verify-desktop-token', { desktopToken, username: userData.username });
      } catch (error) {
        console.error('保存桌面端token失败:', error);
      }
    } else {
      await saveTokenUtil(token);
    }
  }
  message.success('扫码登录成功');
  router.push('/home');
};

const refreshQrCode = () => {
  initQrLogin();
};

watch(activeLoginTab, (val) => {
  if (val === 'qr-login') {
    initQrLogin();
  } else {
    clearQrTimers();
  }
});

const qrCodeDataUrl = ref('');
const qrSessionId = ref('');
const qrCountdown = ref(0);
const qrStatus = ref<'idle' | 'pending' | 'approved' | 'expired' | 'error'>('idle');
let qrCountdownTimer: number | null = null;
let qrStatusTimer: number | null = null;

// 第一步：点击登录按钮，先验证表单
const prepareLogin = async () => {
  const valid = await formRef.value?.validate();
  if (valid) {
    // 表单验证通过，进入点选验证步骤
    loginStep.value = 'human-verify';
    nextTick(initClickCaptcha);
  }
};

// --- Canvas 绘图与逻辑 (前端模拟生成验证码) ---
const initClickCaptcha = () => {
  clickPoints.value = [];
  clickStatus.value = 'pending';
  const canvas = captchaCanvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const width = 320;
  const height = 160;
  
  // 1. 绘制背景 (浅色带杂色)
  ctx.fillStyle = '#f0f2f5';
  ctx.fillRect(0, 0, width, height);
  
  // 2. 绘制干扰线
  for (let i = 0; i < 7; i++) {
    ctx.strokeStyle = `rgba(${Math.random()*255},${Math.random()*255},${Math.random()*255}, 0.3)`;
    ctx.beginPath();
    ctx.moveTo(Math.random() * width, Math.random() * height);
    ctx.lineTo(Math.random() * width, Math.random() * height);
    ctx.stroke();
  }
  // 3. 绘制干扰点
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = `rgba(${Math.random()*200},${Math.random()*200},${Math.random()*200}, 0.2)`;
    ctx.beginPath();
    ctx.arc(Math.random() * width, Math.random() * height, 1, 0, 2 * Math.PI);
    ctx.fill();
  }

  // 4. 生成随机汉字
  const chars = "山水火木土金日月星辰风雨雷电天地人一二三四五上下左右".split("");
  
  allChars.value = [];
  const charCount = 4; // 画布上总共生成4个字
  const verifyCount = 3; // 要求用户点击3个
  
  ctx.font = 'bold 26px "Microsoft YaHei"';
  ctx.textBaseline = 'middle';
  ctx.textAlign = 'center';
  
  for (let i = 0; i < charCount; i++) {
    const char = chars[Math.floor(Math.random() * chars.length)];
    // 分区布局防止重叠：将画布分为4块区域
    const margin = 30;
    const sectionW = (width - margin * 2) / charCount;
    const x = margin + i * sectionW + Math.random() * (sectionW - 30) + 15;
    const y = margin + Math.random() * (height - margin * 2);
    
    // 随机旋转
    const angle = (Math.random() - 0.5) * 0.8; // -0.4 ~ 0.4 rad
    
    ctx.save();
    ctx.translate(x, y);
    ctx.rotate(angle);
    ctx.fillStyle = `rgb(${Math.random()*150}, ${Math.random()*150}, ${Math.random()*150})`;
    ctx.fillText(char, 0, 0);
    ctx.restore();
    
    // 保存真实坐标用于校验
    allChars.value.push({ char, x, y });
  }

  // 5. 随机选取其中的 3 个作为目标
  const shuffled = [...allChars.value].sort(() => 0.5 - Math.random());
  targetChars.value = shuffled.slice(0, verifyCount).map(item => item.char);
};

// 处理画布点击
const handleCanvasClick = (e: MouseEvent) => {
  if (clickStatus.value !== 'pending') return;
  if (clickPoints.value.length >= targetChars.value.length) return;

  const canvas = captchaCanvasRef.value;
  if (!canvas) return;
  
  const rect = canvas.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;

  // 添加标记点
  clickPoints.value.push({ x, y });

  // 如果点够了，触发校验
  if (clickPoints.value.length === targetChars.value.length) {
    verifyCaptcha();
  }
};

// 校验逻辑
const verifyCaptcha = () => {
  clickStatus.value = 'verifying';
  
  // 模拟网络延迟和校验过程
  setTimeout(() => {
    let isCorrect = true;
    
    for (let i = 0; i < clickPoints.value.length; i++) {
      const targetChar = targetChars.value[i];
      // 找到这个字在图上的真实位置
      const realPos = allChars.value.find(c => c.char === targetChar);
      
      if (realPos) {
        const dx = clickPoints.value[i].x - realPos.x;
        const dy = clickPoints.value[i].y - realPos.y;
        const dist = Math.sqrt(dx * dx + dy * dy);
        
        // 允许误差范围 (35px半径)
        if (dist > 35) {
          isCorrect = false;
          break;
        }
      }
    }

    if (isCorrect) {
      clickStatus.value = 'success';
      message.success('验证通过');
      setTimeout(executeRealLogin, 800); // 成功后执行真正的登录
    } else {
      // 失败处理：抖动、提示、重置
      clickStatus.value = 'pending';
      isShake.value = true;
      message.error('验证失败，请按顺序点击');
      setTimeout(() => {
        isShake.value = false;
        clickPoints.value = []; // 清空标记
        initClickCaptcha(); // 刷新图片
      }, 500);
    }
  }, 600);
};
// --- 点选验证逻辑结束 ---

// 执行真正的后端登录请求
const executeRealLogin = async () => {
  const loginData: any = {
    username: user.value.username,
    password: user.value.password,
    captcha: user.value.captcha
  };

  try {
    const res = await postM('login', loginData);
    if (isSuccess(res)) {
      const userData = res.data.data;
      tempUserData.value = userData; // 临时保存用户数据

      // 根据环境变量决定登录处理方式
      if (!isProduction) {
        // 开发环境
        await handleDevLogin(userData);
      } else {
        // 生产环境
        await handleProdLogin(userData);
      }
    } else {
      message.error(res.data.msg);
      await loadCaptcha(); // 刷新验证码
      loginStep.value = 'login'; // 失败则退回
    }
  } catch (error) {
    console.error('登录请求失败:', error);
    message.error('登录失败，请稍后重试');
    loginStep.value = 'login';
  }
};

// 处理开发环境登录（跳过双因素认证）
const handleDevLogin = async (userData) => {
  store.commit('saveUser', userData);
  
  if (isDesktopEnv) {
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
  
  message.success('登录成功');
  router.push('/home');
};

// 处理生产环境登录（需要双因素认证）
const handleProdLogin = async (userData) => {
  if (userData.secretKey && userData.secretKey.trim()) {
    loginStep.value = '2fa-verify';
    message.info('请输入双因素认证码');
  } else {
    loginStep.value = '2fa-bind';
    message.info('请完成双因素认证绑定');
  }
};

const updateTwoFactorAuth = async (val, secretKeyval) => {
  const charArray = val.split('');
  tempUserData.value.secretKey = secretKeyval
  totpCode.value = await charArray.map(char => Number(char));
  verifyTwoFactorAuth();
};

// 验证双因素认证码
const verifyTwoFactorAuth = async () => {
  if (!totpCode.value) {
    message.error('请输入完整的6位验证码');
    return;
  }

  try {
    const code = totpCode.value.join('');
    const res = await postM('verify2fa', {
      userId: tempUserData.value.id,
      totpCode: code,
      secretKey: tempUserData.value.secretKey
    });

    if (isSuccess(res)) {
      let user = res.data.data
      store.commit('saveUser', user);

      if (isDesktopEnv) {
        const desktopToken = generateDesktopToken();
        saveTokenUtil(desktopToken);
        saveLocalStorageDesktopToken(desktopToken);
        try {
          await postM('verify-desktop-token', { desktopToken, username: user.username });
        } catch (error) {
          console.error('保存桌面端token失败:', error);
        }
      } else {
        await saveTokenUtil(user.token);
      }

      message.success('登录成功');
      router.push('/home');
    } else {
      message.error(res.data.msg || '验证码错误');
      totpCode.value = [];
    }
  } catch (error) {
    console.error('双因素认证验证失败:', error);
    message.error('验证失败，请稍后重试');
    totpCode.value = [];
  }
};

// 返回登录页面
const backToLogin = () => {
  loginStep.value = 'login';
  totpCode.value = [];
  clickPoints.value = [];
  tempUserData.value = null;
};

onUnmounted(() => {
  clearQrTimers();
});

// 跳转到注册页面
const toRegister = () => {
  router.replace('/register');
};

// 处理GitHub登录
const handleGitHubLogin = () => {
  if (isDesktopEnv) {
    window.location.href = 'https://freemix.bond/oauth2/authorization/github';
  } else {
    window.location.href = '/oauth2/authorization/github';
  }
};

// 加载验证码
const loadCaptcha = async () => {
  if (!user.value.username) {
    return;
  }
  try {
    const res = await postM('captcha', { "username": user.value.username });
    if (isSuccess(res)) {
      captchaExpression.value = res.data.data.expression;
    }
  } catch (error) {
    console.log(error);
    message.error('验证码加载失败');
  }
};

// 监听登录步骤变化
watch(loginStep, (newStep) => {
  if (newStep === '2fa-verify') {
    nextTick(() => {
      setTimeout(() => {
        if (totpInputRef.value) {
          totpInputRef.value.inputRefList[0].focus();
        }
      }, 100);
    });
  }
});
</script>

<style scoped>
/* 增加氛围光效 */
.ambient-light {
  position: absolute;
  top: -20%; left: 20%;
  width: 600px; height: 600px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.15) 0%, transparent 70%);
  filter: blur(80px);
  animation: floatLight 10s infinite alternate;
  pointer-events: none;
  z-index: -1;
}
@keyframes floatLight { from { transform: translate(0,0); } to { transform: translate(-50px, 50px); } }

/* 基础卡片样式 */
.card-tabs .n-tabs-nav--bar-type {
  padding-left: 4px;
}

.login-card {
  background-color: transparent !important;
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  justify-content: center;
  /* 增加边框发光感 */
  border: 1px solid rgba(255, 255, 255, 0.1); 
}

.login-card.desktop-fullscreen {
  border-radius: 0;
  box-shadow: none;
}

.login-card :deep(.n-card__content) {
  background-color: transparent !important;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* Logo 区域 */
.logo-container {
  text-align: center;
  margin-bottom: 7px;
  padding: 2px 0;
}

.logo-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 5px;
  position: relative;
}

.logo-glow {
  position: absolute; top: 50%; left: 50%; width: 70px; height: 70px;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(64, 158, 255, 0.5) 0%, transparent 70%);
  filter: blur(10px); z-index: 0;
}

.system-logo {
  width: 60px;
  height: 60px;
  object-fit: contain;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  z-index: 1;
}

.system-logo:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.app-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #d6d2d2;
  letter-spacing: 1px;
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

/* 登录按钮渐变 */
.login-btn-gradient {
  background: linear-gradient(92deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  box-shadow: 0 4px 10px rgba(37,99,235,0.3);
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

.qr-login-container {
  display: flex;
  gap: 24px;
  margin-top: 8px;
}

.qr-login-left {
  flex: 0 0 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-image-wrapper {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-image {
  width: 180px;
  height: 180px;
}

.qr-placeholder {
  color: #999;
  font-size: 14px;
}

.qr-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
}

.qr-tips {
  margin-top: 12px;
  font-size: 13px;
  color: #b3b3b3;
  text-align: center;
}

.qr-login-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.qr-steps {
  margin: 0 0 12px;
  padding-left: 20px;
  color: #b3b3b3;
  font-size: 13px;
}

.qr-status-text {
  font-size: 13px;
  color: #999;
}

/* --- 2FA 区域 --- */
.two-factor-auth-section {
  text-align: center;
}
.auth-icon-circle {
  width: 60px; height: 60px; border-radius: 50%;
  background: rgba(16, 185, 129, 0.1); color: #10b981;
  display: flex; align-items: center; justify-content: center; margin: 0 auto 15px;
}
.otp-wrapper { margin: 20px auto; max-width: 300px; display: flex; justify-content: center; }

/* --- 验证区域样式 (Click Captcha) --- */
.verify-section {
  text-align: center;
  padding: 10px 20px 30px;
}

.verify-header { margin-bottom: 20px; }
.verify-header h3 { margin: 0 0 10px; color: #d6d2d2; font-size: 20px; }
.verify-header p { color: #999; font-size: 14px; margin-top: 5px; }

.highlight-text { color: #409eff; font-weight: bold; }
.target-chars {
  display: inline-flex; gap: 5px; background: rgba(255,255,255,0.1);
  padding: 2px 8px; border-radius: 4px; margin-left: 5px;
  vertical-align: middle;
}
.target-char { color: #fff; font-weight: bold; font-size: 16px; }
.success-text { color: #10b981; font-weight: bold; font-size: 16px; }

/* 验证码容器 */
.click-captcha-container {
  position: relative; 
  width: 320px; 
  height: 160px; 
  margin: 0 auto 20px;
  border-radius: 8px; 
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
  border: 2px solid rgba(255,255,255,0.1);
  user-select: none;
}
.captcha-canvas { display: block; cursor: crosshair; width: 100%; height: 100%; }

/* 点击标记点 */
.click-marker {
  position: absolute; width: 24px; height: 24px;
  background: #409eff; color: #fff; border-radius: 50%;
  border: 2px solid #fff;
  transform: translate(-50%, -50%) scale(0);
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: bold;
  pointer-events: none;
  box-shadow: 0 2px 5px rgba(0,0,0,0.3);
  animation: popMarker 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}
@keyframes popMarker { to { transform: translate(-50%, -50%) scale(1); } }

/* 刷新按钮 */
.refresh-btn {
  position: absolute; top: 10px; right: 10px;
  width: 30px; height: 30px; background: rgba(0,0,0,0.6);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #fff; transition: all 0.3s;
}
.refresh-btn:hover { background: #409eff; transform: rotate(180deg); }

/* 成功动画层 */
.success-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255,255,255,0.9);
  display: flex; align-items: center; justify-content: center;
  animation: fadeIn 0.3s;
}

/* 错误抖动 */
.is-shaking { animation: shake 0.4s cubic-bezier(.36,.07,.19,.97) both; border-color: #f56c6c; }
@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}

/* 动画工具类 */
.fade-in-up { animation: fadeInUp 0.5s ease-out; }
.fade-in-scale { animation: fadeInScale 0.4s ease-out; }
.fade-in-right { animation: fadeInRight 0.4s ease-out; }

@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInScale { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
@keyframes fadeInRight { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }

.back-btn { color: #999; }
.back-btn:hover { color: #fff; }
</style>
