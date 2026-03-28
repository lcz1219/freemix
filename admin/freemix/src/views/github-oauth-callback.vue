<template>
  <div class="oauth-callback">
    <n-spin size="large" v-if="loading">
      <template #description>
        正在处理登录...
      </template>
    </n-spin>
    
    <n-result v-if="error" status="error" title="登录失败" :description="errorMessage">
      <template #footer>
        <n-button @click="retryLogin">返回登录页</n-button>
      </template>
    </n-result>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { NSpin, NResult, NButton } from 'naive-ui'
import { postM, isSuccess } from '@/utils/request';
import { saveToken as saveTokenUtil } from '@/utils/tokenUtils.js';
import { generateDesktopToken, saveLocalStorageDesktopToken } from '@/utils/desktopToken.js';
import { isDesktop } from '@/utils/device.js'

const route = useRoute()
const router = useRouter()
const store = useStore()

const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')

// 重新登录
const retryLogin = () => {
  router.replace('/login')
}

const callback = async () => {
  try {
    console.log("callback===>", route.query);
    
    // 检查是否有错误参数
    const errorParam = route.query.error
    if (errorParam) {
      error.value = true
      errorMessage.value = '登录失败: ' + errorParam
      loading.value = false
      return
    }

    // 获取token参数
    const token = route.query.token as string
    if (!token) {
      error.value = true
      errorMessage.value = '登录失败：缺少访问令牌'
      loading.value = false
      return
    }
    
    let user;
    if (route.query.githubId) {
      console.log("githubIdFindUser")
      const res = await postM("githubIdFindUser", { githubId: route.query.githubId })
      user = res.data.data
    } else if (route.query.qqOpenId) {
      console.log("qqIdFindUser")
      const res = await postM("qqIdFindUser", { qqOpenId: route.query.qqOpenId })
      user = res.data.data
    }

    if (!user) {
      error.value = true
      errorMessage.value = '登录失败：未找到关联用户'
      loading.value = false
      return
    }

    // 验证成功，完成登录流程
    store.commit('saveUser', user);
    
    // 检查是否为桌面端
    const isDesktopDevice = route.query.isDesktop === 'true' || isDesktop()
    
    // 如果是桌面端，生成并保存桌面端token
    if (isDesktopDevice) {
      // 生成并保存桌面端token
      const desktopToken = generateDesktopToken();
      saveTokenUtil(desktopToken);
      saveLocalStorageDesktopToken(desktopToken);
      try {
        await postM('verify-desktop-token', { desktopToken, username: user.username });
        console.log('桌面端token已保存到服务器');
      } catch (error) {
        console.error('保存桌面端token失败:', error);
      }
    } else {
      await saveTokenUtil(user.token);
    }
    
    loading.value = false
    router.replace('/home');
  } catch (err) {
    console.error('OAuth处理失败:', err)
    error.value = true
    errorMessage.value = '处理登录请求时发生错误'
    loading.value = false
  }
}

onMounted(() => {
  callback()
})
</script>

<style scoped>
.oauth-callback {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100%;
}
</style>