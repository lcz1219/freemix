<template>
  <div>
    <n-upload
      multiple
      directory-dnd
      :max="5"
      @change="handleChange"
      :show-file-list="false"
    >
      <n-upload-dragger>
        <div style="margin-bottom: 12px">
          <n-icon size="48" :depth="3">
            <ArchiveOutline />
          </n-icon>
        </div>
        <n-text style="font-size: 16px">
          点击或者拖动文件到该区域来上传
        </n-text>
        <n-p depth="3" style="margin: 8px 0 0 0">
          请不要上传敏感数据，比如你的银行卡号和密码，信用卡号有效期和安全码
        </n-p>
      </n-upload-dragger>
    </n-upload>
    
    <div v-if="uploading" style="margin-top: 10px">
      <n-spin size="small" />
      <span style="margin-left: 10px">文件上传中...</span>
    </div>
    
    <n-alert v-if="uploadMessage" :type="uploadMessageType" style="margin-top: 10px">
      {{ uploadMessage }}
    </n-alert>
  </div>
</template>

<script setup>
import { NUpload, NUploadDragger, NText, NP, NIcon, NSpin, NAlert } from 'naive-ui'
import { ref } from 'vue'
import { ArchiveOutline } from '@vicons/ionicons5'
import { uploadFile } from '../utils/request.js'
import { useStore } from 'vuex';

const emit = defineEmits(['uploadSuccess', 'uploadError'])
const store = useStore()
const uploading = ref(false)
const uploadMessage = ref('')
const uploadMessageType = ref('success')

const handleChange = async ({ file, fileList }) => {
  // 重置状态
  uploadMessage.value = ''
  
  if (file.status === 'pending') {
    uploading.value = true
    
    try {
      const response = await uploadFile(file.file,JSON.stringify(store.state.user))
      
      if (response && response.data && response.data.operSucc) {
        uploadMessageType.value = 'success'
        uploadMessage.value = `文件上传成功: ${response.data.data}`
        // 发出上传成功事件，传递文件名
        emit('uploadSuccess', response.data.data)
      } else {
        uploadMessageType.value = 'error'
        const errorMsg = response?.data?.msg || '上传失败'
        uploadMessage.value = `上传失败: ${errorMsg}`
        // 发出上传失败事件
        emit('uploadError', errorMsg)
      }
    } catch (error) {
      uploadMessageType.value = 'error'
      uploadMessage.value = `上传失败: ${error.message}`
      // 发出上传失败事件
      emit('uploadError', error.message)
    } finally {
      uploading.value = false
    }
  }
}
</script>

<style scoped>
</style>