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
import { setToken } from '@/utils/tokenUtils'

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

    // 保存token到localStorage
    localStorage.setItem('token', token)

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
  handleOAuthCallback()
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