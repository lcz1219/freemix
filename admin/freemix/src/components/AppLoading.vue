<template>
  <div class="app-loading">
    <div class="loading-content">
      <div class="spinner"></div>
      <div class="loading-text">正在加载中...</div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 这是一个简单的加载页面组件，用于在应用启动时显示
</script>

<style scoped>
.app-loading {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: var(--bg-color, #f8f9fa);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.dark-theme .app-loading {
  background-color: var(--bg-color, #121212);
}

.loading-content {
  text-align: center;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 5px solid var(--border-color, #e0e0e0);
  border-top: 5px solid var(--primary-color, #81c683);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

.dark-theme .spinner {
  border: 5px solid var(--border-color, #333333);
  border-top: 5px solid var(--primary-color, #81c683);
}

.loading-text {
  font-size: 18px;
  color: var(--text-color, #333333);
  font-weight: 500;
}

.dark-theme .loading-text {
  color: var(--text-color, #e0e0e0);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>