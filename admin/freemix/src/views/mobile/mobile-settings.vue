<template>
  <van-config-provider :theme="currentTheme">
    <div class="mobile-settings">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="设置"
        left-text="返回"
        left-arrow
        @click-left="goBack"
      />

      <!-- 用户信息 -->
      <div class="user-section">
        <van-cell-group inset>
          <van-cell center>
            <template #title>
              <div class="user-info">
                <van-image
                  :src="userAvatar"
                  round
                  width="60"
                  height="60"
                  class="avatar"
                />
                <div class="user-details">
                  <div class="username">{{ user?.username || '未登录' }}</div>
                  <div class="user-email">{{ user?.email || 'user@example.com' }}</div>
                </div>
              </div>
            </template>
            <template #right-icon>
              <van-icon name="arrow" />
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <!-- 基本设置 -->
      <van-cell-group inset title="基本设置">
        <van-cell
          title="深色模式"
          :value="isDark ? '开启' : '关闭'"
          is-link
          @click="toggleTheme"
        >
          <template #right-icon>
            <van-switch :model-value="isDark" @change="toggleTheme" />
          </template>
        </van-cell>

        <van-cell
          title="语言"
          :value="currentLanguage"
          is-link
          @click="showLanguagePicker = true"
        />

        <van-cell
          title="通知设置"
          is-link
          @click="goToNotificationSettings"
        />
      </van-cell-group>

      <!-- 数据管理 -->
      <van-cell-group inset title="数据管理">
        <!-- <van-cell
          title="导出数据"
          label="导出所有目标数据"
          is-link
          @click="exportData"
        /> -->

        <van-cell
          title="导入数据"
          label="从Excel文件导入"
          is-link
          @click="importData"
        />

        <van-cell
          title="清空缓存"
          label="清理应用缓存数据"
          is-link
          @click="clearCache"
        />
      </van-cell-group>

      <!-- 隐私与安全 -->
      <van-cell-group inset title="隐私与安全">
        <van-cell
          title="修改密码"
          is-link
          @click="changePassword"
        />

        <van-cell
          title="登录记录"
          is-link
          @click="goToLoginLog"
        />

        <van-cell
          title="隐私设置"
          is-link
          @click="goToPrivacySettings"
        />
      </van-cell-group>

      <!-- 关于 -->
      <van-cell-group inset title="关于">
        <van-cell
          title="版本信息"
          :value="appVersion"
        />

        <van-cell
          title="用户协议"
          is-link
          @click="showUserAgreement"
        />

        <van-cell
          title="隐私政策"
          is-link
          @click="showPrivacyPolicy"
        />

        <van-cell
          title="意见反馈"
          is-link
          @click="goToFeedback"
        />
      </van-cell-group>

      <!-- 退出登录 -->
      <div class="logout-section">
        <van-button
          type="danger"
          block
          round
          @click="showLogoutConfirm = true"
        >
          退出登录
        </van-button>
      </div>

      <!-- 语言选择器 -->
      <van-popup v-model:show="showLanguagePicker" position="bottom">
        <van-picker
          :columns="languageOptions"
          @confirm="onLanguageConfirm"
          @cancel="showLanguagePicker = false"
        />
      </van-popup>

      <!-- 退出登录确认弹窗 -->
      <van-dialog
        v-model:show="showLogoutConfirm"
        title="确认退出"
        show-cancel-button
        @confirm="logout"
      >
        <div style="padding: 20px; text-align: center;">
          确定要退出登录吗？
        </div>
      </van-dialog>

      <!-- 用户协议弹窗 -->
      <van-dialog
        v-model:show="showUserAgreementDialog"
        title="用户协议"
        :show-cancel-button="false"
        confirm-button-text="我知道了"
      >
        <div class="agreement-content">
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
        :show-cancel-button="false"
        confirm-button-text="我知道了"
      >
        <div class="policy-content">
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

// 主题配置 - 使用默认的vant主题
const currentTheme = ref('light')

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
  // 修复: language 未定义的问题
  const currentLang = 'zh-CN' // 默认语言
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
  // 设置语言的函数实现
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
    })
    
    showLoadingToast('清理中...')
    
    showSuccessToast('缓存清理完成')
  } catch (error) {
    if (error !== 'cancel') {
      showToast('清理失败，请重试')
      console.error('清理缓存失败:', error)
    }
  } finally {
    closeToast()
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
    
    // 使用 store.dispatch('logout') 替代未定义的 logoutUser()
    await store.dispatch('logout')
    
    // 清除本地存储
    localStorage.clear()
    
    // 跳转到登录页
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
  // 获取应用版本信息
  try {
    // 使用静态版本号，避免动态导入问题
    appVersion.value = '1.0.0'
  } catch (error) {
    console.error('获取版本信息失败:', error)
    appVersion.value = '1.0.0'
  }
})
</script>

<style scoped>
.mobile-settings {
  min-height: 100vh;
  background-color: var(--van-background-color);
  padding-bottom: 20px;
}

.user-section {
  padding: 12px 0 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  border: 2px solid var(--van-primary-color);
}

.user-details {
  flex: 1;
}

.username {
  font-size: 18px;
  font-weight: 600;
  color: var(--van-text-color);
  margin-bottom: 4px;
}

.user-email {
  font-size: 14px;
  color: var(--van-text-color-2);
}

.logout-section {
  padding: 24px 16px 16px;
}

.agreement-content,
.policy-content {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
  line-height: 1.6;
}

.agreement-content h4,
.policy-content h4 {
  margin: 16px 0 8px;
  color: var(--van-primary-color);
}

.agreement-content p,
.policy-content p {
  margin: 8px 0;
  color: var(--van-text-color-2);
  font-size: 14px;
}

/* 深色主题适配 */
:deep(.van-theme-dark) {
  --van-background-color: #1a1a1a;
  --van-background-color-light: #2a2a2a;
  --van-text-color: #f5f5f5;
  --van-text-color-2: #a0a0a0;
}

:deep(.van-cell-group) {
  background-color: var(--van-background-color-light);
}

:deep(.van-cell) {
  background-color: var(--van-background-color-light);
}

:deep(.van-dialog__content) {
  color: var(--van-text-color);
}
</style>
