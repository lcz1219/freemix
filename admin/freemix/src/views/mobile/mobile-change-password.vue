<template>
  <van-config-provider theme="dark">
    <div class="mobile-page">
      <van-nav-bar
        title="修改密码"
        left-text="返回"
        left-arrow
        @click-left="onClickLeft"
        fixed
        placeholder
        class="glass-nav"
      />
      
      <div class="content">
        <ChangePassword :always-open="true" />
      </div>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import ChangePassword from '@/components/ChangePassword.vue';

const router = useRouter();

const onClickLeft = () => {
  router.back();
};
</script>

<style scoped>
.mobile-page {
  min-height: 100vh;
  background-color: #000;
  color: #fff;
}

.content {
  padding: 16px;
}

.glass-nav {
  background: rgba(30, 30, 30, 0.8);
  backdrop-filter: blur(20px);
}

:deep(.van-nav-bar) {
  background-color: transparent;
}

:deep(.van-nav-bar__title) {
  color: #fff;
}

:deep(.van-nav-bar__text) {
  color: #fff;
}

:deep(.van-icon) {
  color: #fff;
}
</style>