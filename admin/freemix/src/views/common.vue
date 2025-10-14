<template>
  <div>
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>

    <!-- 顶部导航栏 -->
    <NavBar active-tab="dashboard" />
  </div>

  <slot name="content">

  </slot>
   <p>© 2025 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
</template>
<script setup>

import NavBar from '@/components/NavBar.vue';
</script>
