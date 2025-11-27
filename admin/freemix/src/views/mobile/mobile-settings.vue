<template>
  <van-config-provider theme="dark">
    <div class="settings-container">
      <!-- 动态背景层 (改为纯暗黑风格) -->
      <div class="animated-bg"></div>
      <div class="bg-overlay"></div>

      <!-- 顶部导航栏 -->
      <van-nav-bar
        fixed
        placeholder
        class="glass-nav"
        :border="false"
        z-index="100"
      >
        <template #left>
          <div class="nav-back" @click="goBack">
            <van-icon name="arrow-left" />
            <span>返回</span>
          </div>
        </template>
        <template #title>
          <span class="nav-title">系统设置</span>
        </template>
      </van-nav-bar>

      <div class="content-wrapper">
        <!-- 用户信息卡片 -->
        <div class="glass-card user-card" @click="router.push('/profile')">
          <div class="user-info">
            <div class="avatar-box">
              <van-image
                :src="userAvatar"
                round
                fit="cover"
                class="avatar-img"
              />
            </div>
            <div class="user-details">
              <div class="username">{{ user?.username || '未登录用户' }}</div>
              <div class="user-email">{{ user?.email || 'user@example.com' }}</div>
            </div>
            <div class="user-arrow">
              <van-icon name="arrow" />
            </div>
          </div>
        </div>

        <!-- 基本设置 -->
        <div class="section-title">基本设置</div>
        <div class="glass-group">
          <van-cell title="深色模式" center class="glass-cell">
            <template #right-icon>
              <!-- 激活色改为白色，符合黑白极简风 -->
              <van-switch 
                :model-value="isDark" 
                @update:model-value="toggleTheme" 
                active-color="#ffffff" 
                inactive-color="rgba(255,255,255,0.15)"
                size="24px"
              />
            </template>
          </van-cell>

          <van-cell
            title="多语言"
            :value="currentLanguage"
            is-link
            class="glass-cell"
            @click="showLanguagePicker = true"
          />

          <van-cell
            title="通知设置"
            is-link
            class="glass-cell"
            @click="goToNotificationSettings"
          />
        </div>

        <!-- 数据管理 -->
        <div class="section-title">数据管理</div>
        <div class="glass-group">
          <van-cell
            title="导入数据"
            label="支持 Excel / CSV 格式"
            is-link
            class="glass-cell"
            @click="importData"
          />

          <van-cell
            title="清空缓存"
            label="释放本地存储空间"
            is-link
            class="glass-cell"
            @click="clearCache"
          />
        </div>

        <!-- 隐私与安全 -->
        <div class="section-title">隐私安全</div>
        <div class="glass-group">
          <van-cell
            title="修改密码"
            is-link
            class="glass-cell"
            @click="changePassword"
          />

          <van-cell
            title="登录日志"
            is-link
            class="glass-cell"
            @click="goToLoginLog"
          />

          <van-cell
            title="隐私设置"
            is-link
            class="glass-cell"
            @click="goToPrivacySettings"
          />
        </div>

        <!-- 关于 -->
        <div class="section-title">关于我们</div>
        <div class="glass-group">
          <van-cell
            title="当前版本"
            :value="appVersion"
            class="glass-cell"
          />

          <van-cell
            title="用户协议"
            is-link
            class="glass-cell"
            @click="showUserAgreement"
          />

          <van-cell
            title="隐私政策"
            is-link
            class="glass-cell"
            @click="showPrivacyPolicy"
          />

          <van-cell
            title="意见反馈"
            is-link
            class="glass-cell"
            @click="goToFeedback"
          />
        </div>

        <!-- 退出登录 -->
        <div class="logout-section">
          <van-button
            block
            round
            class="dark-btn-danger"
            @click="showLogoutConfirm = true"
          >
            退出登录
          </van-button>
        </div>
      </div>

      <!-- 语言选择器 -->
      <van-popup 
        v-model:show="showLanguagePicker" 
        position="bottom" 
        round 
        class="glass-popup"
      >
        <van-picker
          :columns="languageOptions"
          @confirm="onLanguageConfirm"
          @cancel="showLanguagePicker = false"
          title="选择语言"
        />
      </van-popup>

      <!-- 退出登录确认弹窗 -->
      <van-dialog
        v-model:show="showLogoutConfirm"
        title="确认退出"
        show-cancel-button
        class="glass-dialog"
        confirm-button-color="#ff3b30"
        cancel-button-color="#fff"
        @confirm="logout"
      >
        <div class="dialog-content">
          确定要退出登录吗？本地未同步的数据可能会丢失。
        </div>
      </van-dialog>

      <!-- 用户协议弹窗 -->
      <van-dialog
        v-model:show="showUserAgreementDialog"
        title="用户协议"
        class="glass-dialog"
        :show-cancel-button="false"
        confirm-button-text="我知道了"
        confirm-button-color="#fff"
      >
        <div class="agreement-content glass-scroll">
          <h4>1. 服务条款</h4>
          <p>欢迎使用FreeMix目标管理系统。使用本应用即表示您同意以下条款...</p>
          <h4>2. 用户责任</h4>
          <p>用户应当遵守相关法律法规，不得利用本服务从事违法违规活动...</p>
          <h4>3. 知识产权</h4>
          <p>本应用的所有内容均受知识产权法保护，未经授权不得复制或传播...</p>
        </div>
      </van-dialog>

      <!-- 隐私政策弹窗 -->
      <van-dialog
        v-model:show="showPrivacyPolicyDialog"
        title="隐私政策"
        class="glass-dialog"
        :show-cancel-button="false"
        confirm-button-text="我知道了"
        confirm-button-color="#fff"
      >
        <div class="policy-content glass-scroll">
          <h4>1. 信息收集</h4>
          <p>我们收集您主动提供的信息，包括但不限于用户名、邮箱等...</p>
          <h4>2. 信息使用</h4>
          <p>收集的信息仅用于改善服务质量，不会向第三方泄露您的个人信息...</p>
          <h4>3. 信息保护</h4>
          <p>我们采用行业标准的安全措施保护您的个人信息安全...</p>
        </div>
      </van-dialog>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { useUser } from '@/hooks/useUser'
import { 
  showToast, 
  showLoadingToast, 
  closeToast, 
  showSuccessToast,
  showConfirmDialog 
} from 'vant'

// 路由和状态管理
const router = useRouter()
const store = useStore()
const { user } = useUser()

// 主题配置
const currentTheme = ref('dark')
const isDark = ref(true) 

const toggleTheme = (val) => {
  isDark.value = val
  showToast(val ? '已开启深色模式' : '已关闭深色模式')
}

// 响应式数据
const showLanguagePicker = ref(false)
const showLogoutConfirm = ref(false)
const showUserAgreementDialog = ref(false)
const showPrivacyPolicyDialog = ref(false)
const appVersion = ref('1.0.0')

// 语言选项
const languageOptions = [
  { text: '简体中文', value: 'zh-CN' },
  { text: 'English', value: 'en-US' },
  { text: '繁體中文', value: 'zh-TW' }
]

// 计算属性
const currentLanguage = computed(() => {
  const currentLang = 'zh-CN' 
  const option = languageOptions.find(opt => opt.value === currentLang)
  return option ? option.text : '简体中文'
})

const userAvatar = computed(() => {
  return store.state.user?.avatar || 'https://api.dicebear.com/7.x/miniavs/svg?seed=user'
})

// 方法
const goBack = () => {
  router.back()
}

const setLanguage = (lang) => {
  console.log('设置语言为:', lang)
}

const onLanguageConfirm = ({ selectedValues }) => {
  setLanguage(selectedValues[0])
  showLanguagePicker.value = false
  showToast('语言设置已更新')
}

const goToNotificationSettings = () => {
  router.push('/notification-settings')
}

const importData = () => {
  router.push('/import-data')
}

const clearCache = async () => {
  try {
    await showConfirmDialog({
      title: '确认清空',
      message: '确定要清空应用缓存吗？这不会删除您的目标数据。',
      className: 'glass-dialog',
      confirmButtonColor: '#ffffff',
      cancelButtonColor: '#888888'
    })
    
    showLoadingToast('清理中...')
    setTimeout(() => {
        showSuccessToast('缓存清理完成')
    }, 1000)
    
  } catch (error) {
    if (error !== 'cancel') {
      showToast('清理失败，请重试')
      console.error('清理缓存失败:', error)
    }
  }
}

const changePassword = () => {
  router.push('/change-password')
}

const goToLoginLog = () => {
  router.push('/login-log')
}

const goToPrivacySettings = () => {
  router.push('/privacy-settings')
}

const showUserAgreement = () => {
  showUserAgreementDialog.value = true
}

const showPrivacyPolicy = () => {
  showPrivacyPolicyDialog.value = true
}

const goToFeedback = () => {
  router.push('/feedback')
}

const logout = async () => {
  try {
    showLoadingToast('退出中...')
    
    await store.dispatch('logout')
    localStorage.clear()
    router.replace('/login')
    
    showSuccessToast('已退出登录')
  } catch (error) {
    showToast('退出失败，请重试')
    console.error('退出登录失败:', error)
  } finally {
    closeToast()
  }
}

// 生命周期
onMounted(() => {
  try {
    appVersion.value = '1.0.0'
  } catch (error) {
    console.error('获取版本信息失败:', error)
    appVersion.value = '1.0.0'
  }
})
</script>

<style scoped lang="scss">
/* 引入字体 */
@import url('https://fonts.googleapis.com/css2?family=Rajdhani:wght@500;700&display=swap');

.settings-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  background: #000000; /* 纯黑背景 */
  font-family: 'Rajdhani', sans-serif;
  color: #fff;
  
  /* 定义单色调变量 */
  --primary: #ffffff; /* 高亮色：纯白 */
  --secondary: #888888; /* 次级色：灰色 */
  --glass-bg: rgba(30, 30, 30, 0.6); /* 玻璃背景：深灰半透明 */
  --glass-border: rgba(255, 255, 255, 0.08); /* 边框：极淡白 */
}

/* 动态背景 - 改为灰黑色系 */
.animated-bg {
  position: fixed;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  /* 黑色与深炭色的渐变 */
  background: radial-gradient(circle at center, #1a1a1a 0%, #000000 70%);
  animation: bgRotate 20s linear infinite;
  z-index: 0;
  pointer-events: none;
}

.bg-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* 噪点纹理保持质感 */
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  z-index: 1;
  pointer-events: none;
}

@keyframes bgRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 顶部导航 */
.glass-nav {
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(20px);
  
  :deep(.van-nav-bar__content) {
    height: 50px;
  }
}

.nav-back {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #fff; /* 纯白 */
  font-size: 16px;
  cursor: pointer;
  
  &:active { opacity: 0.7; }
}

.nav-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.content-wrapper {
  position: relative;
  z-index: 10;
  padding: 16px;
}

/* 用户卡片 */
.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(15px);
  border: 1px solid var(--glass-border);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5); /* 更深的阴影 */
  margin-bottom: 30px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-box {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  padding: 2px;
  /* 边框改为黑灰渐变 */
  background: linear-gradient(135deg, #ffffff, #333333); 
  
  .avatar-img {
    width: 100%;
    height: 100%;
    border: 2px solid #000;
    border-radius: 50%;
  }
}

.user-details {
  flex: 1;
}

.username {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4px;
}

.user-email {
  font-size: 13px;
  color: #999;
}

.user-arrow {
  color: #555;
}

/* 列表分组通用样式 */
.section-title {
  margin: 20px 0 10px 10px;
  font-size: 14px;
  color: #666; /* 标题颜色变暗 */
  font-weight: bold;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.glass-group {
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 10px;
}

.glass-cell {
  background: rgba(30, 30, 30, 0.4) !important;
  backdrop-filter: blur(10px);
  color: #fff !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background 0.2s;
  
  &:active {
    background: rgba(255, 255, 255, 0.1) !important;
  }
  
  &::after { display: none; }
  
  :deep(.van-cell__title) {
    color: #eeeeee;
    font-size: 15px;
  }
  
  :deep(.van-cell__value) {
    color: #888;
  }
  
  :deep(.van-cell__label) {
    color: #666;
  }
  
  :deep(.van-icon) {
    color: #666;
  }
}

/* 退出按钮 */
.logout-section {
  margin-top: 40px;
  margin-bottom: 20px;
}

.dark-btn-danger {
  background: #1a1a1a !important; /* 深黑底色 */
  border: 1px solid #333 !important;
  color: #ff3b30 !important; /* iOS 红 */
  font-weight: bold;
  height: 48px;
  font-size: 16px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
  
  &:active { 
    background: #000 !important;
    opacity: 0.9; 
    transform: scale(0.98); 
  }
}

/* 弹窗通用样式 - 全黑风格 */
.glass-popup {
  background: #121212;
  border-top: 1px solid #333;
  
  :deep(.van-picker) {
    background: transparent;
    color: #fff;
  }
  :deep(.van-picker__mask) {
    background-image: linear-gradient(180deg, rgba(18, 18, 18, 0.9), rgba(18, 18, 18, 0.4)), linear-gradient(0deg, rgba(18, 18, 18, 0.9), rgba(18, 18, 18, 0.4));
  }
  :deep(.van-picker-column__item) { color: #666; }
  :deep(.van-picker-column__item--selected) { color: #fff; font-weight: bold; } /* 选中为白 */
  :deep(.van-picker__toolbar) { background: #1a1a1a; border-bottom: 1px solid #222; }
  :deep(.van-picker__cancel) { color: #888; }
  :deep(.van-picker__confirm) { color: #fff; }
}

.glass-dialog {
  background: rgba(20, 20, 20, 0.95) !important;
  backdrop-filter: blur(20px);
  border: 1px solid #333;
  
  :deep(.van-dialog__header) { color: #fff; padding-top: 20px; }
  :deep(.van-dialog__content) { color: #aaa; }
  :deep(.van-dialog__footer) { display: flex; border-top: 1px solid #333; }
  :deep(.van-button) { background: transparent; height: 50px; border: none; }
  :deep(.van-dialog__cancel) { color: #888; background: transparent; }
  :deep(.van-dialog__confirm) { background: transparent; }
}

.dialog-content {
  padding: 20px;
  text-align: center;
  font-size: 15px;
  color: #aaa;
}

/* 协议内容样式 */
.agreement-content, .policy-content {
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
  text-align: left;
  
  h4 { margin: 15px 0 8px; color: #fff; font-size: 16px; }
  p { margin: 0 0 10px; color: #999; font-size: 13px; line-height: 1.6; }
}

/* 自定义滚动条 - 暗色 */
.glass-scroll::-webkit-scrollbar {
  width: 4px;
}
.glass-scroll::-webkit-scrollbar-thumb {
  background: #333;
  border-radius: 2px;
}
</style>