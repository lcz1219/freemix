<template>
  <div class="oauth-callback">
    <n-spin size="large" v-if="loading">
      <template #description>
        正在处理GitHub登录...
      </template>
    </n-spin>
    <n-result
      v-else-if="error"
      status="error"
      title="登录失败"
      :description="errorMessage"
    >
      <template #footer>
        <n-button @click="retryLogin">重新登录</n-button>
      </template>
    </n-result>
    <n-result
      v-else
      status="success"
      title="登录成功"
      description="正在跳转到首页..."
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { NSpin, NResult, NButton } from 'naive-ui'
import { postM, isSuccess } from '@/utils/request';
import { saveToken as saveTokenUtil } from '@/utils/tokenUtils.js'; // 导入token工具函数
import { saveToken } from '@/utils/tokenUtils'
import { generateDesktopToken, saveLocalStorageDesktopToken } from '@/utils/desktopToken.js';
import { isDesktop } from '@/utils/device'

const route = useRoute()
const router = useRouter()
const store = useStore()

const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')

// 重新登录
const retryLogin = () => {
  window.location.href = '/#/login'
}
const callback=async ()=>{
  try {
    // 检查是否有错误参数
    const errorParam = route.query.error
    if (errorParam) {
      error.value = true
      errorMessage.value = 'GitHub登录失败: ' + errorParam
      loading.value = false
      return
    }

    // 获取token参数
    const token = route.query.token
    if (!token) {
      error.value = true
      errorMessage.value = '登录失败：缺少访问令牌'
      loading.value = false
      return
    }
    
    console.log("githubIdFindUser")
    const res= await  postM("githubIdFindUser",{githubId:route.query.githubId})
    let user=res.data.data
    // 验证成功，完成登录流程
    store.commit('saveUser', user);
    
    // 检查是否为桌面端
    const isDesktopDevice = route.query.isDesktop === 'true' || isDesktop()
    
    // 如果是桌面端，生成并保存桌面端token
    if (isDesktopDevice) {
      // 桌面端使用持久化存储
      
      // 生成并保存桌面端token
      const desktopToken = user.deskToken;
      saveTokenUtil(desktopToken);//本地token保存
      saveLocalStorageDesktopToken(desktopToken);
      // 发送请求到服务器验证并保存桌面端token
      try {
        await postM('verify-desktop-token', { desktopToken, username: user.username });
        console.log('桌面端token已保存到服务器');
      } catch (error) {
        console.error('保存桌面端token失败:', error);
      }
    } else {
      // 使用新的工具函数保存token
      await saveTokenUtil(user.token);
    }
    
    // 设置用户已登录状态
    // store.commit('setIsLoggedIn', true)
    
    // 跳转到首页
    loading.value = false
    router.push('/home');
  } catch (err) {
    console.error('GitHub OAuth处理失败:', err)
    error.value = true
    errorMessage.value = '处理登录请求时发生错误'
    loading.value = false
  }
}

// 处理GitHub OAuth回调
const handleOAuthCallback = async () => {
  try {
    // 检查是否有错误参数
    const errorParam = route.query.error
    if (errorParam) {
      error.value = true
      errorMessage.value = 'GitHub登录失败: ' + errorParam
      loading.value = false
      return
    }

    // 获取token参数
    const token = route.query.token
    if (!token) {
      error.value = true
      errorMessage.value = '登录失败：缺少访问令牌'
      loading.value = false
      return
    }

    // 检查是否为桌面端
    const isDesktopDevice = route.query.isDesktop === 'true' || isDesktop()

    // 使用tokenUtils保存token（根据设备类型自动选择存储方式）
    saveToken(token)

    // 设置用户已登录状态
    store.commit('setIsLoggedIn', true)

    // 跳转到首页
    loading.value = false
    router.push('/home')
  } catch (err) {
    console.error('GitHub OAuth处理失败:', err)
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