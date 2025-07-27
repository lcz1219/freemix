<template>
  <n-space vertical>
    <n-steps :current="props.current" :status="currentStatus">
     
      <n-step
        title="用户信息"
        description="填写您的个人信息"
      />
      <n-step
        title="安全问题一"
      />
      <n-step
        title="安全问题二"
      />
      <n-step
        title="安全问题三"
      />
    </n-steps>
  </n-space>
</template>

<script setup lang="ts">
import type { StepsProps } from 'naive-ui'
import { defineComponent, ref, defineProps, onUpdated } from 'vue'

 const props=defineProps({
    current: {
      type: Number,
      default: 1
    }
  })
  
    onUpdated(() => {
      console.log('Received current value in step.vue:', props.current)
    })
     const  currentStatus=ref<StepsProps['status']>('process')
      // 直接使用 props.current
</script>