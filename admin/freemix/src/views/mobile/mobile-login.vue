<template>
  <van-config-provider :theme="currentTheme" class="apple-style-provider">
    <div class="mobile-login">
      <!-- 背景装饰 -->
      <div class="background-blur-circle-1"></div>
      <div class="background-blur-circle-2"></div>

      <div class="login-container">
        <div class="header-section">
          <!-- <img src="@/assets/logo.png" alt="App Logo" class="app-logo" /> -->
          <h1 class="app-title">欢迎回来</h1>
          <p class="app-subtitle">登录以继续管理您的目标</p>
        </div>

        <div class="form-card">
          <van-form ref="formRef" @submit="onSubmit">
            <van-cell-group inset class="apple-cell-group">
              <van-field
                v-model="user.username"
                name="username"
                placeholder="用户名"
                :rules="[{ required: true, message: '请输入用户名' }]"
                left-icon="user-o"
                clearable
                class="apple-field"
                @blur="loadCaptcha"
              />
              <van-field
                v-model="user.password"
                type="password"
                name="password"
                placeholder="密码"
                :rules="[{ required: true, message: '请输入密码' }]"
                left-icon="lock"
                clearable
                class="apple-field"
              />
              <van-field
                v-model="user.captcha"
                name="captcha"
                placeholder="验证码"
                :rules="[{ required: true, message: '请输入验证码' }]"
                left-icon="shield-o"
                clearable
                class="apple-field"
              >
                <template #button>
                  <div 
                    class="captcha-container"
                    @click="loadCaptcha"
                    v-html="captchaExpression || '获取验证码'"
                  ></div>
                </template>
              </van-field>
            </van-cell-group>

            <div class="action-buttons">
              <van-button
                round
                block
                type="primary"
                native-type="submit"
                :loading="loading"
                class="apple-gradient-button login-button"
              >
                {{ loading ? '登录中...' : '登录' }}
              </van-button>
            </div>
          </van-form>

          <div class="social-login-divider">或通过以下方式登录</div>

          <div class="social-login-buttons">
            <van-button round block class="social-button github-button" @click="handleGitHubLogin">
              <van-icon name="github" />
              使用 GitHub 登录
            </van-button>
          </div>

          <div class="footer-links">
            <a href="#" @click.prevent="toRegister">没有帐户？<span class="link-highlight">立即注册</span></a>
          </div>
        </div>
      </div>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { 
  showToast,
  showLoadingToast,
  closeToast,
  showSuccessToast
} from 'vant'
import { postM, isSuccess } from '@/utils/request.js'
import { isDesktop } from '@/utils/device.js'
import { generateDesktopToken, saveLocalStorageDesktopToken } from '@/utils/desktopToken.js'
import { saveToken } from '@/utils/tokenUtils.js'

// 路由和状态管理
const router = useRouter()
const store = useStore()

// 主题配置
const isDark = ref(false)
const currentTheme = computed(() => isDark.value ? 'dark' : 'light')

// 表单引用
const formRef = ref()

// 用户数据
const user = ref({ username: '', password: '', captcha: '' })
const loading = ref(false)
const captchaExpression = ref('')

// 表单验证规则
const rules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'onBlur'
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'onBlur'
  },
  captcha: {
    required: true,
    message: '请输入验证码',
    trigger: 'onBlur'
  }
}

// 提交登录表单
const onSubmit = async () => {
  loading.value = true
  
  try {
    // 验证表单
    await formRef.value?.validate()
    
    // 执行登录请求
    const res = await postM('login', user.value)
    if (isSuccess(res)) {
      const userData = res.data.data
      
      // 保存用户信息到 Vuex
      store.commit('saveUser', userData)
      
      // 根据设备类型保存 token
      if (isDesktop()) {
        // 桌面端使用持久化存储
        saveToken(userData.token)
        // 生成并保存桌面端token
        const desktopToken = generateDesktopToken()
        saveLocalStorageDesktopToken(desktopToken)
        // 发送请求到服务器验证并保存桌面端token
        try {
          await postM('verify-desktop-token', { desktopToken, username: userData.username })
        } catch (error) {
          console.error('保存桌面端token失败:', error)
        }
      } else {
        // 移动端使用 localStorage
        saveToken(userData.token)
      }
      
      showSuccessToast('登录成功')
      router.push('/home')
    } else {
      showToast(res.data.msg || '登录失败')
      await loadCaptcha() // 刷新验证码
    }
  } catch (error) {
    showToast('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 跳转到注册页面
const toRegister = () => {
  router.replace('/register')
}

// 处理GitHub登录
const handleGitHubLogin = () => {
  // 检查是否为桌面端
  if (isDesktop()) {
    // 桌面端直接访问后端服务器的完整URL
    window.location.href = 'https://freemix.bond/oauth2/authorization/github'
  } else {
    // Web端保持原有行为
    window.location.href = '/oauth2/authorization/github'
  }
}

// 加载验证码
const loadCaptcha = async () => {
  if (!user.value.username) {
    return
  }
  try {
    const res = await postM('/captcha', { "username": user.value.username })
    if (isSuccess(res)) {
      captchaExpression.value = res.data.data.expression
    }
  } catch (error) {
    console.log(error)
    showToast('验证码加载失败')
  }
}

// 页面加载时初始化
onMounted(() => {
  // 可以在这里添加初始化逻辑
})
</script>

<style scoped>
/* 全局和根元素 */
.apple-style-provider {
  --van-background-color: #f7f8fa;
  --van-background-color-light: #ffffff;
  --van-text-color: #333;
  --van-text-color-2: #666;
  --van-border-color: #ebedf0;
  --apple-gradient-color: linear-gradient(135deg, #00c9a7, #00a98f);
}

.van-theme-dark .apple-style-provider {
  --van-background-color: #1c1c1e;
  --van-background-color-light: #2c2c2e;
  --van-text-color: #f5f5f7;
  --van-text-color-2: #a9a9b0;
  --van-border-color: #3a3a3c;
}

.mobile-login {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background-color: var(--van-background-color);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

/* 背景装饰 */
.background-blur-circle-1, .background-blur-circle-2 {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.background-blur-circle-1 {
  width: 250px;
  height: 250px;
  background: rgba(0, 201, 167, 0.3);
  top: -50px;
  right: -80px;
}
.background-blur-circle-2 {
  width: 200px;
  height: 200px;
  background: rgba(118, 75, 162, 0.3);
  bottom: -60px;
  left: -70px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  z-index: 1;
}

/* 头部 */
.header-section {
  text-align: center;
  margin-bottom: 32px;
}

.app-logo {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  border-radius: 12px;
}

.app-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--van-text-color);
  margin: 0 0 8px;
}

.app-subtitle {
  font-size: 16px;
  color: var(--van-text-color-2);
  margin: 0;
}

/* 表单卡片 */
.form-card {
  background-color: var(--van-background-color-light);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.van-theme-dark .form-card {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.apple-cell-group {
  background: none;
}

.apple-field {
  background-color: var(--van-background-color);
  border-radius: 10px;
  margin-bottom: 16px;
  padding: 12px;
}

:deep(.van-field__left-icon) {
  margin-right: 8px;
  color: var(--van-text-color-2);
}

/* 验证码 */
.captcha-container {
  cursor: pointer;
  min-width: 90px;
  text-align: center;
}

.captcha-container :deep(svg) {
  vertical-align: middle;
}

/* 按钮 */
.action-buttons {
  margin-top: 24px;
}

.apple-gradient-button {
  border: none;
  color: white;
  font-weight: 500;
  background: var(--apple-gradient-color);
  height: 48px;
  font-size: 16px;
}

.social-login-divider {
  margin: 24px 0;
  text-align: center;
  font-size: 12px;
  color: var(--van-text-color-2);
  display: flex;
  align-items: center;
}

.social-login-divider::before, .social-login-divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid var(--van-border-color);
}

.social-login-divider::before {
  margin-right: 8px;
}

.social-login-divider::after {
  margin-left: 8px;
}

.social-button {
  height: 48px;
  font-size: 16px;
  background-color: var(--van-background-color);
  border: 1px solid var(--van-border-color);
  color: var(--van-text-color);
}

.social-button .van-icon {
  font-size: 20px;
  margin-right: 8px;
}

/* 底部链接 */
.footer-links {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
}

.footer-links a {
  color: var(--van-text-color-2);
  text-decoration: none;
}

.link-highlight {
  color: #00c9a7;
  font-weight: 500;
}
</style>