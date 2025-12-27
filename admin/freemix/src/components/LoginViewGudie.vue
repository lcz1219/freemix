<template>
  <div class="login-container">
    <!-- 主登录卡片 -->
    <div class="login-card">
      <!-- Logo 区域 -->
      <div class="logo-section">
        <div class="logo-icon-wrapper">
          <div class="logo-icon"></div>
        </div>
        <h1>FreeMix-目标管理系统</h1>
      </div>

      <!-- 标签页切换 -->
      <div class="tabs">
        <div 
          class="tab-item" 
          :class="{ active: currentTab === 'scan' }"
          @click="currentTab = 'scan'"
        >
          扫码登录
        </div>
        <div 
          class="tab-item" 
          :class="{ active: currentTab === 'account' }"
          @click="currentTab = 'account'"
        >
          登录
        </div>
      </div>

      <!-- 内容区域：根据Tab显示不同内容，这里主要展示扫码页 -->
      <div class="content-area" v-if="currentTab === 'scan'">
        <!-- 左侧二维码部分 -->
        <div class="qr-section">
          <div class="qr-box">
            <!-- 使用示例二维码API -->
            <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=FreeMixLoginExample&color=000000&bgcolor=ffffff" alt="登录二维码">
          </div>
          <div class="qr-hint">
            使用已登录的移动端 FreeMix 扫码<br>确认登录
          </div>
          <div class="qr-timer" :class="{ expired: countdown <= 0 }">
            {{ countdown > 0 ? `二维码将在 ${countdown} 秒后失效` : '二维码已失效，请刷新' }}
          </div>
        </div>

        <!-- 右侧文字说明部分 -->
        <div class="text-section">
          <p>打开移动端 FreeMix 应用</p>
          <p>登录后进入个人中心或设置页</p>
          <p>点击扫码登录，扫描左侧二维码</p>
          <p>在手机上确认本次登录</p>
          
          <div class="status-text">等待手机确认登录...</div>
        </div>
      </div>

      <!-- 这是一个占位符，用于演示Tab切换 -->
      <div class="content-area account-login" v-else>
        <p style="color: #888;">这里是账号密码登录表单区域</p>
      </div>
    </div>

    <!-- 右侧浮动工具栏 -->
  

    <!-- 底部备案号 -->
   
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// 当前选中的标签页
const currentTab = ref('scan');

// 倒计时逻辑
const countdown = ref(114);
let timer = null;

onMounted(() => {
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      clearInterval(timer);
    }
  }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
/* 容器设置：占据全屏，Flex居中 */
.login-container {
  background-color: #0f0f0f;
  color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

/* 主登录卡片 */
.login-card {
  background-color: #141414;
  width: 600px;
  border: 1px solid #333;
  border-radius: 12px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  z-index: 10;
}

/* Logo 区域 */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.logo-icon-wrapper {
  padding: 4px;
  border: 1px solid #00bfa5;
  border-radius: 14px;
  box-shadow: 0 0 10px rgba(0, 191, 165, 0.3);
  margin-bottom: 15px;
}

.logo-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #ffffff 0%, #e0e0e0 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

/* 使用CSS伪元素模拟图标 */
.logo-icon::after {
  content: '';
  width: 24px;
  height: 24px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%2300bfa5' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='20 6 9 17 4 12'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: center;
}

h1 {
  font-size: 24px;
  font-weight: 500;
  letter-spacing: 1px;
  color: #eee;
}

/* 标签页 */
.tabs {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  width: 100%;
  border-bottom: 1px solid #333;
  justify-content: flex-start;
  padding-left: 20px;
}

.tab-item {
  padding-bottom: 10px;
  cursor: pointer;
  font-size: 16px;
  color: #888;
  transition: color 0.3s;
  position: relative;
}

.tab-item:hover {
  color: #ccc;
}

.tab-item.active {
  color: #fff;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #00bfa5;
}

/* 内容布局 */
.content-area {
  display: flex;
  width: 100%;
  justify-content: center;
  gap: 40px;
  min-height: 200px; /* 保持高度稳定 */
}

.account-login {
  align-items: center;
  justify-content: center;
}

/* 二维码部分 */
.qr-section {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-box {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  width: 160px;
  height: 160px;
}

.qr-box img {
  width: 100%;
  height: 100%;
  display: block;
}

.qr-hint {
  font-size: 12px;
  color: #888;
  line-height: 1.6;
  margin-top: 5px;
}

.qr-timer {
  font-size: 12px;
  color: #888;
  margin-top: 5px;
}
.qr-timer.expired {
  color: #d46b7a;
}

/* 文字说明部分 */
.text-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  font-size: 13px;
  color: #aaa;
}

.text-section p {
  margin-bottom: 8px;
  line-height: 1.5;
}

.status-text {
  margin-top: 20px;
  color: #666;
}

/* 浮动按钮 */
.floating-tools {
  position: fixed;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 15px;
  z-index: 20;
}

.tool-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #2c2c2c;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.3s;
  color: #888;
}

.tool-btn:hover {
  background-color: #3c3c3c;
  color: #fff;
}

.tool-btn.pink {
  background-color: #d46b7a;
  color: white;
  font-weight: bold;
  font-size: 12px;
}

/* 底部版权 */
.footer {
  position: absolute;
  bottom: 20px;
  right: 30px;
  font-size: 12px;
  color: #444;
  z-index: 10;
}
</style>