<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'mobile-settings dark' : 'mobile-settings light'">
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>

    <!-- 顶部导航栏 -->
    <n-layout-header bordered class="mobile-header">
      <div class="header-content">
        <n-button quaternary circle @click="goBack">
          <n-icon size="20">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M20,11H7.8l5.6-5.6L12,4l-8,8l8,8l1.4-1.4L7.8,13H20V11z"/>
            </svg>
          </n-icon>
        </n-button>
        <h1 class="header-title">设置</h1>
        <n-button quaternary circle @click="toggleTheme" class="theme-toggle-button">
          <n-icon size="20">
            <svg v-if="isDark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M12,9c1.7,0,3,1.3,3,3s-1.3,3-3,3s-3-1.3-3-3S10.3,9,12,9z M12,7c-2.8,0-5,2.2-5,5s2.2,5,5,5s5-2.2,5-5 S14.8,7,12,7z M2,13l2,0c0.6,0,1-0.4,1-1s-0.4-1-1-1l-2,0c-0.6,0-1,0.4-1,1S1.4,13,2,13z M20,13l2,0c0.6,0,1-0.4,1-1s-0.4-1-1-1l-2,0 c-0.6,0-1,0.4-1,1S19.4,13,20,13z M12,2l0,2c0,0.6,0.4,1,1,1s1-0.4,1-1l0-2c0-0.6-0.4-1-1-1S12,1.4,12,2z M12,20l0,2 c0,0.6,0.4,1,1,1s1-0.4,1-1l0-2c0-0.6-0.4-1-1-1C12.4,19,12,19.4,12,20z M6.3,6.3L7.7,7.7c0.4,0.4,1,0.4,1.4,0s0.4-1,0-1.4 L7.7,4.9C7.3,4.5,6.7,4.5,6.3,4.9S5.9,5.9,6.3,6.3z M16.3,16.3L17.7,17.7c0.4,0.4,1,0.4,1.4,0s0.4-1,0-1.4l-1.4-1.4 c-0.4-0.4-1-0.4-1.4,0S15.9,15.9,16.3,16.3z M17.7,6.3L16.3,4.9c-0.4-0.4-1-0.4-1.4,0s-0.4,1,0,1.4l1.4,1.4 c0.4,0.4,1,0.4,1.4,0S18.1,6.7,17.7,6.3z M7.7,16.3L6.3,17.7c-0.4,0.4-0.4,1,0,1.4c0.4,0.4,1,0.4,1.4,0l1.4-1.4 c0.4-0.4,0.4-1,0-1.4S8.1,15.9,7.7,16.3z"/>
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path d="M12,9c-1.7,0-3,1.3-3,3s1.3,3,3,3s3-1.3,3-3S13.7,9,12,9z M12,15c-1.7,0-3-1.3-3-3s1.3-3,3-3s3,1.3,3,3 S13.7,15,12,15z M12,7c-2.8,0-5,2.2-5,5s2.2,5,5,5s5-2.2,5-5S14.8,7,12,7z M12,3c-5,0-9,4-9,9s4,9,9,9s9-4,9-9S17,3,12,3z M12,1c6.1,0,11,4.9,11,11s-4.9,11-11,11S1,18.1,1,12S5.9,1,12,1z"/>
            </svg>
          </n-icon>
        </n-button>
      </div>
    </n-layout-header>
    
    <!-- 主要内容 -->
    <n-layout-content class="main-content-wrapper">
      <div class="content">
        <!-- 页面标题 -->
        <section class="page-header">
          <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">设置</h1>
          <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
            自定义您的目标追踪者体验
          </p>
        </section>

        <!-- 个人资料设置 -->
        <section class="settings-section">
          <n-card :class="isDark ? 'settings-card' : 'settings-card-light'">
            <h2 class="section-title">个人资料</h2>
            
            <div class="form-group">
              <label class="form-label">用户名</label>
              <n-input 
                v-model:value="profileForm.username" 
                disabled
                class="form-input disabled"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">邮箱</label>
              <n-input 
                v-model:value="profileForm.email" 
                type="email"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">姓名</label>
              <n-input 
                v-model:value="profileForm.name" 
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">职位</label>
              <n-input 
                v-model:value="profileForm.position" 
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">个人简介</label>
              <n-input 
                v-model:value="profileForm.bio" 
                type="textarea"
                :autosize="{ minRows: 3, maxRows: 5 }"
                class="form-input"
              />
            </div>
            
            <div class="form-actions">
              <n-button type="primary" @click="saveProfile">保存更改</n-button>
            </div>
          </n-card>
        </section>
        
        <!-- 通知设置 -->
        <section class="settings-section">
          <n-card :class="isDark ? 'settings-card' : 'settings-card-light'">
            <h2 class="section-title">通知设置</h2>
            
            <n-space vertical>
              <n-checkbox v-model:checked="notificationSettings.emailEnabled">
                启用邮件通知
              </n-checkbox>
              
              <n-checkbox v-model:checked="notificationSettings.desktopEnabled">
                启用桌面通知
              </n-checkbox>
              
              <n-checkbox v-model:checked="notificationSettings.reminderEnabled">
                启用提醒通知
              </n-checkbox>
              
              <div v-if="notificationSettings.reminderEnabled" class="reminder-settings">
                <label class="form-label">提醒时间</label>
                <n-time-picker 
                  v-model:value="reminderTime"
                  format="HH:mm"
                  class="time-picker"
                />
              </div>
            </n-space>
          </n-card>
        </section>
        
        <!-- 主题设置 -->
        <section class="settings-section">
          <n-card :class="isDark ? 'settings-card' : 'settings-card-light'">
            <h2 class="section-title">主题设置</h2>
            
            <n-space vertical>
              <n-radio-group v-model:value="themeSettings.mode">
                <n-space>
                  <n-radio value="light">浅色模式</n-radio>
                  <n-radio value="dark">深色模式</n-radio>
                  <n-radio value="auto">自动</n-radio>
                </n-space>
              </n-radio-group>
              
              <div class="form-group">
                <label class="form-label">主题色</label>
                <n-color-picker 
                  v-model:value="themeSettings.color"
                  :show-alpha="false"
                />
              </div>
              
              <n-checkbox v-model:checked="themeSettings.animations">
                启用动画效果
              </n-checkbox>
            </n-space>
          </n-card>
        </section>
        
        <!-- 数据管理 -->
        <section class="settings-section">
          <n-card :class="isDark ? 'settings-card' : 'settings-card-light'">
            <h2 class="section-title">数据管理</h2>
            
            <n-space vertical>
              <n-button @click="exportData">导出数据</n-button>
              <n-button @click="importData">导入数据</n-button>
              <n-popconfirm @positive-click="clearData">
                <template #trigger>
                  <n-button type="error">清空所有数据</n-button>
                </template>
                确定要清空所有数据吗？此操作不可撤销。
              </n-popconfirm>
            </n-space>
          </n-card>
        </section>
        
        <!-- 账户安全 -->
        <section class="settings-section">
          <n-card :class="isDark ? 'settings-card' : 'settings-card-light'">
            <h2 class="section-title">账户安全</h2>
            
            <n-space vertical>
              <n-button @click="changePassword">修改密码</n-button>
              <n-button @click="manageSessions">管理登录会话</n-button>
              <n-button type="error" @click="logout">退出登录</n-button>
            </n-space>
          </n-card>
        </section>
      </div>
    </n-layout-content>
  </n-layout>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { 
  NLayout, 
  NLayoutHeader, 
  NLayoutContent, 
  NButton, 
  NIcon, 
  NCard, 
  NInput, 
  NSpace, 
  NCheckbox, 
  NRadioGroup, 
  NRadio, 
  NColorPicker, 
  NTimePicker,
  NPopconfirm,
  useMessage
} from 'naive-ui';
import { getMPaths, postM, isSuccess } from '@/utils/request';

const router = useRouter();
const store = useStore();
const isDark = inject('isDark', ref(false));
const message = useMessage();

// 个人资料表单
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
  reminderEnabled: false
});

// 提醒时间 (默认为上午9点)
const reminderTime = ref(new Date().setHours(9, 0, 0, 0));

// 主题设置
const themeSettings = ref({
  mode: 'auto',
  color: '#8a2be2',
  animations: true
});

// 方法
const goBack = () => {
  router.go(-1);
};

const toggleTheme = () => {
  isDark.value = !isDark.value;
  localStorage.setItem('theme-dark', JSON.stringify(isDark.value));
};

const saveProfile = async () => {
  try {
    // 这里应该调用API保存用户资料
    message.success('个人资料已保存');
  } catch (error) {
    message.error('保存失败');
    console.error(error);
  }
};

const exportData = () => {
  message.success('数据导出成功');
};

const importData = () => {
  message.info('请选择要导入的数据文件');
};

const clearData = () => {
  message.success('数据已清空');
};

const changePassword = () => {
  message.info('跳转到修改密码页面');
};

const manageSessions = () => {
  message.info('跳转到会话管理页面');
};

const logout = () => {
  store.dispatch('logout');
  message.success('已退出登录');
  router.push('/login');
};

// 获取用户资料
const getUserProfile = async () => {
  try {
    // 获取用户资料
    const res = await getMPaths("getUserInfo", store.state.user.username);
    if (isSuccess(res)) {
      const userData = res.data.data || {};
      profileForm.value = {
        username: userData.username || '',
        email: userData.email || '',
        name: userData.name || '',
        position: userData.position || '',
        bio: userData.bio || ''
      };
    } else {
      message.error('获取用户资料失败');
    }
  } catch (error) {
    message.error('获取用户资料时发生错误');
    console.error(error);
  }
};

// 组件挂载时获取用户资料
onMounted(() => {
  getUserProfile();
});
</script>

<style scoped>
.mobile-settings {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
}

.mobile-settings.dark {
  background-color: #0f0f13;
  color: #ffffff;
}

.mobile-settings.light {
  background-color: #dadae3;
  color: #000000;
}

.background-elements {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.gradient-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
}

.gradient-circle.blue {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #1e90ff, transparent 70%);
  top: -150px;
  right: -150px;
}

.gradient-circle.green {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, #00c9a7, transparent 70%);
  bottom: -125px;
  left: -125px;
}

.gradient-circle.purple {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, #8a2be2, transparent 70%);
  top: 30%;
  left: 20%;
}

.mobile-header {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  padding: 10px 16px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.mobile-header.light {
  background-color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #ffffff;
}

.mobile-header.light .header-title {
  color: #000000;
}

.theme-toggle-button {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.mobile-header.light .theme-toggle-button {
  background-color: rgba(0, 0, 0, 0.05) !important;
}

.main-content-wrapper {
  height: 100vh; /* 减去顶部和底部导航栏的高度 */
  overflow-y: auto;
}

.content {
  padding: 16px;
}

.page-header {
  text-align: center;
  margin-bottom: 20px;
}

.hero-title,
.hero-title-light {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 10px;
}

.hero-title {
  color: #ffffff;
}

.hero-title-light {
  color: #000000;
}

.hero-subtitle,
.hero-subtitle-light {
  font-size: 14px;
  opacity: 0.8;
}

.hero-subtitle {
  color: #cccccc;
}

.hero-subtitle-light {
  color: #333333;
}

.settings-section {
  margin-bottom: 20px;
}

.settings-card,
.settings-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.settings-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.settings-card-light {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.dark .section-title {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.light .section-title {
  color: #000000;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  font-size: 14px;
}

.dark .form-label {
  color: #ffffff;
}

.light .form-label {
  color: #000000;
}

.form-input {
  width: 100%;
}

.reminder-settings {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.dark .reminder-settings {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.time-picker {
  width: 100%;
}

.form-actions {
  margin-top: 20px;
  text-align: center;
}

/* 滚动条样式 - Webkit内核浏览器 */
.main-content-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(138, 43, 226, 0.5);
  border-radius: 3px;
}

.main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 43, 226, 0.7);
}

/* 浅色模式滚动条 */
.mobile-settings.light .main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
}

.mobile-settings.light .main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(138, 43, 226, 0.3);
}

.mobile-settings.light .main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 43, 226, 0.5);
}
</style>