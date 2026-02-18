<template>
  <div :class="isDark ? 'settings-container dark' : 'settings-container light'">
    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="settings-layout">
        <!-- 侧边栏导航 -->
        <div class="sidebar">
          <nav class="settings-nav">
            <ul class="nav-list">
              <li class="nav-item" :class="{ active: activeSection === 'profile' }">
                <a href="#" @click.prevent="setActiveSection('profile')">个人资料</a>
              </li>
              <li class="nav-item" :class="{ active: activeSection === 'security' }">
                <a href="#" @click.prevent="setActiveSection('security')">安全设置</a>
              </li>
              <li class="nav-item" :class="{ active: activeSection === 'system' }">
                <a href="#" @click.prevent="setActiveSection('system')">下载设置</a>
              </li>
              <!-- <li class="nav-item" :class="{ active: activeSection === 'token' }">
                <a href="#" @click.prevent="setActiveSection('token')">Token信息</a>
              </li> -->
            </ul>
          </nav>
        </div>

        <!-- 主内容区域 -->
        <div class="content-area">
          <!-- 页面标题 -->
          <div class="page-header">
            <h1>{{ getPageTitle() }}</h1>
            <p>{{ getPageSubtitle() }}</p>
          </div>

          <!-- 个人资料设置 -->
          <div v-show="activeSection === 'profile'" class="settings-section">
            <div class="settings-card">
              <h2>个人资料</h2>
              <div class="form-group">
                <label>用户名</label>
                <input 
                  v-model="profileForm.username" 
                  type="text" 
                  disabled 
                  class="form-input disabled"
                />
              </div>
              
              <div class="form-group">
                <label>邮箱</label>
                <input 
                  v-model="profileForm.email" 
                  type="email" 
                  class="form-input"
                />
              </div>
              
              <div class="form-group">
                <label>姓名</label>
                <input 
                  v-model="profileForm.chinesename" 
                  type="text" 
                  class="form-input"
                />
              </div>
              
              <div class="form-group">
                <label>职位</label>
                <input 
                  v-model="profileForm.position" 
                  type="text" 
                  class="form-input"
                />
              </div>
              
              <div class="form-group">
                <label>个人简介</label>
                <textarea 
                  v-model="profileForm.fashionTitle" 
                  rows="3" 
                  class="form-input"
                ></textarea>
              </div>
              
              <div class="form-actions">
                <button @click="saveProfile" class="btn primary">保存更改</button>
              </div>
            </div>
          </div>

          <!-- 安全设置 -->
          <div v-show="activeSection === 'security'" class="settings-section">
            <div class="settings-card">
              <h2>安全设置</h2>
              <TwoFactorAuth 
                :parent="'settings'"
                :initial-enabled="securitySettings.twoFactorEnabled"
                @update:enabled="onTwoFactorUpdate"
                @update:secretKey="onTwoFactorUpdateSecretKey"
              />
              
              <div class="form-actions">
                <button @click="saveSecuritySettings" class="btn primary">保存安全设置</button>
              </div>
            </div>
          </div>

          <!-- 系统设置 (管理员) -->
          <div v-show="activeSection === 'system'" class="settings-section">
            <div class="settings-card">
              <h2>系统设置</h2>
              <div class="form-group">
                <label>Mac 下载链接</label>
                <input 
                  v-model="downloadConfig.macDownloadUrl" 
                  type="text" 
                  class="form-input"
                  placeholder="https://..."
                  :disabled="isnAdmin"
                />
              </div>
              <div class="form-group">
                <label>Windows 下载链接</label>
                <input 
                  v-model="downloadConfig.winDownloadUrl" 
                  type="text" 
                  class="form-input"
                  placeholder="https://..."
                  :disabled="isnAdmin"
                />
              </div>
              
              <div class="form-actions">
                <button @click="handleSaveDownloadConfig" class="btn primary">保存系统设置</button>
              </div>
            </div>
          </div>

          <!-- Token信息 -->
          <div v-show="false" class="settings-section">
            <div class="settings-card token-info-card">
              <h2>Token信息</h2>
              <div class="token-item">
                <span class="token-label">用户:</span>
                <span class="token-value">{{ user }}</span>
              </div>
              <div class="token-item">
                <span class="token-label">Token:</span>
                <span class="token-value">{{ desktopToken }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
        
        <!-- <div class="settings-section">
          <h2>通知设置</h2>
          <div class="settings-card">
            <div class="checkbox-group">
              <label class="checkbox-label">
                <input 
                  v-model="notificationSettings.emailEnabled" 
                  type="checkbox" 
                  class="checkbox-input"
                />
                <span class="checkbox-text">启用邮件通知</span>
              </label>
            </div>
            
            <div class="checkbox-group">
              <label class="checkbox-label">
                <input 
                  v-model="notificationSettings.desktopEnabled" 
                  type="checkbox" 
                  class="checkbox-input"
                />
                <span class="checkbox-text">启用桌面通知</span>
              </label>
            </div>
            
            <div class="checkbox-group">
              <label class="checkbox-label">
                <input 
                  v-model="notificationSettings.reminderEnabled" 
                  type="checkbox" 
                  class="checkbox-input"
                />
                <span class="checkbox-text">启用目标提醒</span>
              </label>
            </div>
            
            <div class="form-group" v-if="notificationSettings.reminderEnabled">
              <label>提醒时间</label>
              <input 
                v-model="reminderTimeString" 
                type="time" 
                class="form-input"
              />
            </div>
            
            <div class="form-actions">
              <button @click="saveNotifications" class="btn primary">保存通知设置</button>
            </div>
          </div>
        </div> -->
        
        <!-- <div class="settings-section">
          <h2>主题设置</h2>
          <div class="settings-card">
            <div class="form-group">
              <label>主题模式</label>
              <div class="radio-group">
                <label class="radio-label">
                  <input 
                    v-model="themeSettings.mode" 
                    type="radio" 
                    value="light" 
                    class="radio-input"
                  />
                  <span class="radio-text">浅色</span>
                </label>
                <label class="radio-label">
                  <input 
                    v-model="themeSettings.mode" 
                    type="radio" 
                    value="dark" 
                    class="radio-input"
                  />
                  <span class="radio-text">深色</span>
                </label>
                <label class="radio-label">
                  <input 
                    v-model="themeSettings.mode" 
                    type="radio" 
                    value="auto" 
                    class="radio-input"
                  />
                  <span class="radio-text">自动</span>
                </label>
              </div>
            </div>
            
            <div class="checkbox-group">
              <label class="checkbox-label">
                <input 
                  v-model="themeSettings.animationsEnabled" 
                  type="checkbox" 
                  class="checkbox-input"
                />
                <span class="checkbox-text">启用动画效果</span>
              </label>
            </div>
            
            <div class="form-actions">
              <button @click="saveTheme" class="btn primary">保存主题设置</button>
            </div>
          </div>
        </div> -->
        
       
        
       
        
       
      </div>
    </div>
    
    <!-- 2FA验证弹窗 -->
    <n-modal v-model:show="show2FAModal">
      <n-card
        style="width: 400px"
        title="双因素认证验证"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button text @click="show2FAModal = false">
            ×
          </n-button>
        </template>
        <div class="auth-modal-content">
          <p style="margin-bottom: 15px;">为了确认您的身份，请输入6位验证码：</p>
          <n-input
            v-model:value="twoFactorCode"
            type="text"
            placeholder="请输入6位验证码"
            maxlength="6"
            @keydown.enter="verifyAndSaveConfig"
          />
        </div>
        <template #footer>
          <n-space justify="end">
            <n-button @click="show2FAModal = false">取消</n-button>
            <n-button type="primary" @click="verifyAndSaveConfig" :loading="verifying">
              确认
            </n-button>
          </n-space>
        </template>
      </n-card>
    </n-modal>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue';
import TwoFactorAuth from '@/components/TwoFactorAuth.vue';
// @ts-ignore - 忽略vuex声明文件错误
import { useStore } from 'vuex';
// @ts-ignore - 忽略qrcode声明文件错误
import QRCode from 'qrcode'
import { useMessage,NQrCode, NModal, NCard, NInput, NButton, NSpace } from 'naive-ui';
import { postM, isSuccess, getM } from '@/utils/request.js';
import { removeToken } from '@/utils/tokenUtils.js'; // 导入token工具函数
import { getToken } from '@/utils/tokenUtils.js'; // 导入token工具函数
import { useSettings } from '@/hooks/useSettings';
import adminJson from '@/views/json/adminJson.json';

const desktopToken = computed(() => localStorage.getItem('deskop_token'));
const user = computed(() => JSON.parse(localStorage.getItem('user') || '{}'));
// 注入主题变量
const isDark = inject('isDark', ref(false));
const toggleTheme = inject('toggleTheme', () => {});
const isProduction = import.meta.env.PROD;
// 路由和状态管理
const router = useRouter();
const store = useStore();
const message = useMessage();
const isnAdmin = computed(() => {
 
    if (!isProduction) {
      
      return false;
    }
  return !adminJson.admin.includes(user.value.username) 
});
// 使用useSettings hook
const {
  profileForm,
  securitySettings,
  notificationSettings,
  themeSettings,
  reminderTime,
  getPageTitle,
  getPageSubtitle,
  saveProfile,
  saveSecuritySettings,
  saveNotifications,
  saveTheme,
  exportData,
  importData,
  clearAllData,
  changePassword,
  manageSessions,
  onTwoFactorUpdate,
  initUserData
} = useSettings();

const downloadConfig = ref({
  macDownloadUrl: '',
  winDownloadUrl: ''
});

const fetchDownloadConfig = async () => {
  try {
    const res = await getM('/api/config/downloads');
    if (isSuccess(res)) {
      downloadConfig.value = res.data.data;
    }
  } catch (error) {
    console.error('Failed to fetch download config:', error);
  }
};

const show2FAModal = ref(false);
const twoFactorCode = ref('');
const verifying = ref(false);

const handleSaveDownloadConfig = () => {
  // 1. 检查是否为管理员
  // const isAdmin = adminJson.admin.includes(user.value.username);
  if (isnAdmin) {
    message.error('您不是当前系统管理员，无法修改下载设置');
    return;
  }
  
  // 2. 显示2FA验证弹窗
  twoFactorCode.value = '';
  show2FAModal.value = true;
};
const secretKey = ref('');
const onTwoFactorUpdateSecretKey = (newSecretKey) => {
  secretKey.value = newSecretKey;
};
const verifyAndSaveConfig = async () => {
  if (!twoFactorCode.value || twoFactorCode.value.length !== 6) {
    message.error('请输入完整的6位验证码');
    return;
  }

  verifying.value = true;
  try {
    // 调用 verify2fa 接口
    // 注意：如果是登录用户验证，通常不需要 secretKey，或者后端需要从数据库获取
    // 这里假设后端支持仅传 userId 和 totpCode
    const res = await postM('verify2fa', {
      userId: user.value.id || store.state.user.id,
      totpCode: twoFactorCode.value,
      secretKey: secretKey.value,
      // 如果必须 secretKey，且本地没有存储，这里可能会失败，需根据实际情况调整
    });

    if (isSuccess(res)) {
      message.success('验证通过');
      show2FAModal.value = false;
      // 执行真正的保存逻辑
      await saveDownloadConfig();
    } else {
      message.error(res.data.msg || '验证失败');
    }
  } catch (error) {
    console.error('2FA verification failed:', error);
    message.error('验证过程出错');
  } finally {
    verifying.value = false;
  }
};

const saveDownloadConfig = async () => {
  try {
    const res = await postM('/api/config/downloadsp', downloadConfig.value);
    if (isSuccess(res)) {
      message.success('系统设置已保存');
    } else {
      message.error(res.data.msg || '保存失败');
    }
  } catch (error) {
    message.error('保存失败');
  }
};

// 侧边栏导航控制
const activeSection = ref('profile');

const setActiveSection = (section) => {
  activeSection.value = section;
  if (section === 'system') {
    fetchDownloadConfig();
  }
};

const reminderTimeString = computed({
  get: () => {
    const hours = reminderTime.value.hours.toString().padStart(2, '0');
    const minutes = reminderTime.value.minutes.toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  },
  set: (value) => {
    const [hours, minutes] = value.split(':').map(Number);
    reminderTime.value.hours = hours;
    reminderTime.value.minutes = minutes;
  }
});
 
  

// 初始化
onMounted(() => {
  initUserData();
  
});
</script>

<style scoped>
.settings-container {
  min-height: 100vh;
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* 浅色主题变量 - 使用与App.vue一致的主题变量 */
.settings-container.light {
  --card-bg: var(--card-bg, #ffffff);
  --border-color: var(--border-color, #e0e0e0);
  --input-bg: var(--card-bg, #ffffff);
  --text-color: var(--text-color, #333333);
  --disabled-bg: var(--hover-color, #f5f5f5);
  --nav-hover-bg: var(--hover-color, #f5f5f5);
  --nav-hover-color: var(--text-color, #333333);
  --nav-active-bg: var(--hover-color, #f5f5f5);
  --nav-active-color: #66b278;
  background-color: var(--background-color, #f5f5f5);
  color: var(--text-color, #333333);
}

/* 深色主题变量 - 使用与App.vue一致的主题变量 */
.settings-container.dark {
  --card-bg: var(--card-bg, #1e1e1e);
  --border-color: var(--border-color, #333333);
  --input-bg: var(--card-bg, #1e1e1e);
  --text-color: var(--text-color, #e0e0e0);
  --disabled-bg: var(--hover-color, #2a2a2a);
  --nav-hover-bg: var(--hover-color, #2a2a2a);
  --nav-hover-color: var(--text-color, #e0e0e0);
  --nav-active-bg: var(--hover-color, #2a2a2a);
  --nav-active-color: #66b278;
  background-color: var(--background-color, #121212);
  color: var(--text-color, #e0e0e0);
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.settings-layout {
  display: flex;
  gap: 2rem;
  margin-top: 1rem;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  flex-shrink: 0;
}

.settings-nav {
  position: sticky;
  top: 2rem;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-bottom: 0.25rem;
}

.nav-item a {
  display: block;
  padding: 0.75rem 1rem;
  text-decoration: none;
  color: var(--text-color);
  border-radius: 6px;
  transition: all 0.2s ease;
}

.nav-item a:hover {
  background-color: var(--nav-hover-bg);
  color: var(--nav-hover-color);
}

.nav-item.active a {
  background-color: var(--nav-active-bg);
  color: var(--nav-active-color);
  font-weight: 600;
}

/* 主内容区域样式 */
.content-area {
  flex: 1;
  min-width: 0;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.page-header p {
  font-size: 1rem;
  color: #666;
  margin-bottom: 2rem;
}

.settings-section {
  margin-bottom: 2rem;
}

.settings-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.settings-card h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-color);
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-color);
}

.form-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--input-bg);
  color: var(--text-color);
  font-size: 0.875rem;
}

.form-input:focus {
  outline: none;
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.2);
}

.form-input.disabled {
  background-color: var(--disabled-bg);
  cursor: not-allowed;
}

.form-actions {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  color: var(--text-color);
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
}

.btn.primary {
  background-color: #2da44e;
  color: white;
}

.btn.primary:hover {
  background-color: #2c974b;
}

.btn:hover {
  background-color: var(--hover-color);
}

.token-info-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 1.5rem;
}

.token-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.token-label {
  font-weight: 600;
}

.token-value {
  font-family: monospace;
  word-break: break-all;
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }
  
  .page-header h1 {
    font-size: 1.5rem;
  }
  
  .settings-card {
    padding: 1rem;
  }
  
  .settings-layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    order: 2;
  }
  
  .content-area {
    order: 1;
  }
  
  .settings-nav {
    position: static;
  }
  
  .nav-list {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .nav-item {
    margin-bottom: 0;
  }
  
  .nav-item a {
    padding: 0.5rem 0.75rem;
  }
}
</style>