<template>
  <div :class="isDark ? 'settings-container dark' : 'settings-container light'">
    <!-- 顶部导航栏 -->
    <NavBar active-tab="settings" />

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>设置</h1>
        <p>自定义您的目标追踪者体验</p>
      </div>

      <!-- 设置内容 -->
      <div class="settings-content">
          <div class="settings-section">
          <h2>安全设置</h2>
          <div class="settings-card">
            <TwoFactorAuth 
            :parent="'settings'"
              :initial-enabled="securitySettings.twoFactorEnabled"
              @update:enabled="onTwoFactorUpdate"
            />
            
            <div class="form-actions">
              <button @click="saveSecuritySettings" class="btn primary">保存安全设置</button>
            </div>
          </div>
        </div>
        <div class="settings-section">
          <h2>个人资料</h2>
          <div class="settings-card">
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
                v-model="profileForm.name" 
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
                v-model="profileForm.bio" 
                rows="3" 
                class="form-input"
              ></textarea>
            </div>
            
            <div class="form-actions">
              <button @click="saveProfile" class="btn primary">保存更改</button>
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
  </div>
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
import { useMessage,NQrCode } from 'naive-ui';
import { postM, isSuccess } from '@/utils/request.js';

// 注入主题变量
const isDark = inject('isDark', ref(false));
const toggleTheme = inject('toggleTheme', () => {});

// 路由和状态管理
const router = useRouter();
const store = useStore();
const message = useMessage();

// 表单数据
const profileForm = ref({
  username: '',
  email: '',
  name: '',
  position: '',
  bio: ''
});

// 通知设置
const notificationSettings = ref({
  emailEnabled: true,
  desktopEnabled: true,
  reminderEnabled: true
});

const reminderTime = ref({
  hours: 9,
  minutes: 0
});

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

// 主题设置
const themeSettings = ref({
  mode: 'auto',
  animationsEnabled: true
});

// 安全设置
const securitySettings = ref({
  twoFactorEnabled: false
});

// 处理双因素认证状态更新
const onTwoFactorUpdate = (enabled: boolean) => {
  securitySettings.value.twoFactorEnabled = enabled;
};

// 保存个人资料
const saveProfile = () => {
  // 这里应该调用API保存用户资料
  console.log('保存个人资料:', profileForm.value);
  message.success('个人资料已保存');
};

// 保存通知设置
const saveNotifications = () => {
  // 这里应该调用API保存通知设置
  console.log('保存通知设置:', notificationSettings.value, reminderTime.value);
  message.success('通知设置已保存');
};

// 保存主题设置
const saveTheme = () => {
  // 这里应该调用API保存主题设置
  console.log('保存主题设置:', themeSettings.value);
  
  // 应用主题设置
  if (themeSettings.value.mode === 'dark') {
    // @ts-ignore
    toggleTheme();
    isDark.value = true;
  } else if (themeSettings.value.mode === 'light') {
    // @ts-ignore
    toggleTheme();
    isDark.value = false;
  }
  
  message.success('主题设置已保存');
};


// 保存安全设置
const saveSecuritySettings = () => {
  message.success('安全设置已保存');
};

// 导出数据
const exportData = () => {
  message.info('正在导出数据...');
  // 这里应该实现数据导出逻辑
  setTimeout(() => {
    message.success('数据导出成功');
  }, 1000);
};

// 导入数据
const importData = () => {
  message.info('请选择要导入的数据文件');
  // 这里应该实现数据导入逻辑
};

// 清空所有数据
const clearAllData = () => {
  if (confirm('确定要清空所有数据吗？此操作不可撤销。')) {
    message.success('所有数据已清空');
    // 这里应该实现清空数据逻辑
  }
};

// 修改密码
const changePassword = () => {
  message.info('跳转到修改密码页面');
  // 这里应该跳转到修改密码页面
};

// 管理登录会话
const manageSessions = () => {
  message.info('跳转到会话管理页面');
  // 这里应该跳转到会话管理页面
};

// 退出登录
const logout = () => {
  // 清除用户信息
  store.commit('clearUser');
  // 跳转到登录页面
  router.push('/login');
  message.success('已退出登录');
};

// 初始化用户数据
const initUserData = () => {
  // 从store获取用户信息
  const user = store.state.user;
  if (user) {
    profileForm.value.username = user.username || '';
    profileForm.value.email = user.email || '';
    profileForm.value.name = user.name || '';
    profileForm.value.position = user.position || '';
    profileForm.value.bio = user.bio || '';
    
    // 初始化双因素认证状态
    securitySettings.value.twoFactorEnabled = user.twoFactorEnabled || false;
    // 通过组件内部维护hasTwoFactorEnabled和isTwoFactorVerified状态
    // TwoFactorAuth组件会自己处理这些状态
  }
};
 
  

// 初始化
onMounted(() => {
  initUserData();
  
});
</script>

<style scoped>
/* 强制滚动样式 */
.settings-container {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

/* .main-content {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - 80px);
  overflow-y: auto !important;
  -webkit-overflow-scrolling: touch;
  scroll-behavior: smooth;
} */

/* 自定义滚动条样式 */
.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

.settings-container.dark .main-content::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.settings-container.dark .main-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
}

.settings-container.dark .main-content::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
.settings-container.dark {
  background-color: #0f0f13;
  color: #ffffff;
}

.settings-container.light {
  background-color: #dadae3;
  color: #000000;
}

.main-content {
  padding: 20px;
  /* max-width: 1200px; */
  /* margin: 0 auto; */
  max-height: 100%;
  overflow-y: auto;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  opacity: 0.8;
}
:deep .n-qr-code{
  padding: 0% !important;
}

.settings-content {
  /* 将Grid布局改为简单的垂直堆叠布局 */
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 40px;
  padding-left: 20%;
  padding-right: 20%;
}

.settings-section h2 {
  font-size: 24px;
  margin-bottom: 15px;
  font-weight: 600;
}

.settings-card {
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.settings-container.dark .settings-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.settings-container.light .settings-card {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
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

.form-input.disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.radio-group {
  display: flex;
  gap: 20px;
  flex-wrap: wrap; /* 允许换行 */
}

.radio-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.radio-input {
  margin-right: 8px;
  width: 18px;
  height: 18px;
}

.radio-text {
  font-size: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
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

.btn.danger {
  background-color: #ff4d4f;
  color: white;
}

.btn {
  background-color: #6c757d;
  color: white;
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

/* 确保在小屏幕上也能正常显示 */
@media (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
  
  .settings-content {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .page-header h1 {
    font-size: 28px;
  }
  
  .settings-section h2 {
    font-size: 20px;
  }
  
  .radio-group {
    flex-direction: column;
    gap: 10px;
  }
}
</style>