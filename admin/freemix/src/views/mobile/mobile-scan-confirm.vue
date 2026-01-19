<template>
  <div class="mobile-scan-confirm">
    <van-nav-bar title="扫码登录确认" />
    <div class="content">
      <div class="icon-wrapper">
         <img src="/icons/icon.png" class="confirm-logo" />
      </div>
      <h2 class="title">确认登录网页版 FreeMix</h2>
      <p class="sub-title">请确认是否本人操作</p>
      
      <div class="actions">
        <van-button type="primary" block round @click="confirmLogin" :loading="loading">确认登录</van-button>
        <van-button block round @click="cancelLogin" style="margin-top: 12px; background-color: white; color: black; border: none;">取消</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { postM, isSuccess } from '@/utils/request';
import { getToken } from '@/utils/tokenUtils';
import { showToast } from 'vant';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const payload = ref<any>(null);

onMounted(async () => {
  const token = await getToken();
  if (!token) {
    showToast('请先登录');
    router.replace(`/login?redirect=${encodeURIComponent(route.fullPath)}`);
    return;
  }

  const data = route.query.data as string;
  if (!data) {
    showToast('无效的二维码');
    setTimeout(() => router.replace('/home'), 1500);
    return;
  }
  try {
    payload.value = JSON.parse(decodeURIComponent(data));
  } catch (e) {
    showToast('二维码解析失败');
    setTimeout(() => router.replace('/home'), 1500);
  }
});

const confirmLogin = async () => {
  if (!payload.value) return;
  loading.value = true;
  try {
    const res = await postM('qr-login/confirm', {
      sessionId: payload.value.sessionId,
      sessionToken: payload.value.sessionToken
    });
    if (isSuccess(res)) {
      showToast('登录成功');
      setTimeout(() => router.replace('/home'), 1500);
    } else {
      showToast(res.data.msg || '确认失败');
    }
  } catch (e) {
    showToast('网络请求失败');
  } finally {
    loading.value = false;
  }
};

const cancelLogin = () => {
  router.replace('/home');
};
</script>

<style scoped>
.mobile-scan-confirm {
  height: 100vh;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
}
.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0 32px;
  margin-top: -60px; /* visual adjustment */
}
.icon-wrapper {
  margin-bottom: 24px;
}
.confirm-logo {
  width: 80px;
  height: 80px;
  object-fit: contain;
  border-radius: 16px;
}
.title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
}
.sub-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 48px;
}
.actions {
  width: 100%;
}
</style>
