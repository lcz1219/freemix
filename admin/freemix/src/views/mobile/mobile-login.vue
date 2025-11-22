<template>
  <van-config-provider :theme="currentTheme">
    <div class="mobile-login" :class="currentTheme">
      <!-- 背景装饰元素 -->
      <div class="background-elements">
        <div class="gradient-circle blue"></div>
        <div class="gradient-circle green"></div>
        <div class="gradient-circle purple"></div>
      </div>

      <div class="login-container">
        <div class="login-header">
          <h1 class="app-title">FreeMix</h1>
          <p class="app-subtitle">目标管理系统</p>
        </div>

        <van-card class="login-card" :class="currentTheme">
          <van-form ref="formRef" :model="user" :rules="rules" @submit="onSubmit">
            <van-cell-group inset>
              <van-field
                v-model="user.username"
                name="username"
                label="用户名"
                placeholder="请输入用户名"
                :rules="[{ required: true, message: '请输入用户名' }]"
                @blur="loadCaptcha"
              >
                <template #left-icon>
                  <van-icon name="manager-o" />
                </template>
              </van-field>

              <van-field
                v-model="user.password"
                type="password"
                name="password"
                label="密码"
                placeholder="请输入密码"
                :rules="[{ required: true, message: '请输入密码' }]"
              >
                <template #left-icon>
                  <van-icon name="lock" />
                </template>
              </van-field>

              <van-field
                v-model="user.captcha"
                name="captcha"
                label="验证码"
                placeholder="请输入计算结果"
                :rules="[{ required: true, message: '请输入验证码' }]"
              >
                <template #left-icon>
                  <van-icon name="shield-o" />
                </template>
                <template #button>
                  <div class="captcha-expression" @click="loadCaptcha">
                    {{ captchaExpression || '点击刷新' }}
                  </div>
                </template>
              </van-field>
            </van-cell-group>

            <div class="login-buttons">
              <van-button 
                round 
                block 
                type="primary" 
                native-type="submit"
                :loading="loading"
                loading-text="登录中..."
                class="login-button"
              >
                登录
              </van-button>

              <van-button 
                round 
                block 
                plain 
                @click="toRegister"
                class="register-button"
              >
                注册账号
              </van-button>

              <van-button 
                round 
                block 
                plain 
                @click="handleGitHubLogin"
                class="github-login-button"
              >
                <svg slot="icon" viewBox="0 0 24 24" width="16" height="16" style="margin-right: 5px; vertical-align: middle;">
                  <path fill="currentColor" d="M12 .297c-6.63 0-12 5.373-12 12c0 5.303 3.438 9.8 8.205 11.385c.6.113.82-.258.82-.577c0-.285-.01-1.04-.015-2.04c-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729c1.205.084 1.838 1.236 1.838 1.236c1.07 1.835 2.809 1.305 3.495.998c.108-.776.417-1.305.76-1.605c-2.665-.3-5.466-1.332-5.466-5.93c0-1.31.465-2.38 1.235-3.22c-.135-.303-.54-1.523.105-3.176c0 0 1.005-.322 3.3 1.23c.96-.267 1.98-.399 3-.405c1.02.006 2.04.138 3 .405c2.28-1.552 3.285-1.23 3.285-1.23c.645 1.653.24 2.873.12 3.176c.765.84 1.23 1.91 1.23 3.22c0 4.61-2.805 5.625-5.475 5.92c.42.36.81 1.096.81 2.22c0 1.606-.015 2.896-.015 3.286c0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12" />
                </svg>
                GitHub登录
              </van-button>
            </div>
          </van-form>
        </van-card>
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
      router.push('/mobile/home')
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
  router.replace('/mobile/register')
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
.mobile-login {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.mobile-login.light {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 背景装饰元素 */
.background-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.gradient-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.3;
}

.gradient-circle.blue {
  width: 300px;
  height: 300px;
  background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%);
  top: -100px;
  right: -100px;
}

.gradient-circle.green {
  width: 200px;
  height: 200px;
  background: linear-gradient(45deg, #43e97b 0%, #38f9d7 100%);
  bottom: -80px;
  left: 10%;
}

.gradient-circle.purple {
  width: 250px;
  height: 250px;
  background: linear-gradient(45deg, #fa709a 0%, #fee140 100%);
  top: 30%;
  left: -80px;
}

.login-container {
  position: relative;
  z-index: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  justify-content: center;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.app-title {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 10px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.light .app-title {
  color: #333;
}

.app-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.light .app-subtitle {
  color: #666;
}

.login-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.85) !important;
}

.login-card.dark {
  background: rgba(30, 30, 30, 0.85) !important;
}

:deep(.van-field__label) {
  font-weight: 500;
  width: 60px;
}

.captcha-expression {
  min-width: 80px;
  padding: 5px 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  font-size: 14px;
  font-weight: bold;
  color: #333;
  text-align: center;
}

.captcha-expression:hover {
  background-color: #e6e6e6;
}

.login-buttons {
  padding: 20px;
}

.login-button {
  margin-bottom: 15px;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.register-button,
.github-login-button {
  margin-bottom: 15px;
  height: 44px;
  font-size: 16px;
}

.github-login-button :deep(.van-icon) {
  margin-right: 5px;
  width: 16px;
  height: 16px;
  vertical-align: middle;
}

/* 深色主题样式 */
.dark .login-card {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.dark .captcha-expression {
  background-color: #444;
  border-color: #555;
  color: #fff;
}

.dark .captcha-expression:hover {
  background-color: #555;
}
</style>