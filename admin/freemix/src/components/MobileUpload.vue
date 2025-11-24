<template>
  <div class="mobile-upload">
    <!-- 上传按钮 -->
    <van-uploader
      v-model="internalFileList"
      :max-count="5"
      :after-read="afterRead"
      :before-delete="beforeDelete"
      :deletable="true"
      :preview-image="true"
      :preview-full-image="true"
      :preview-options="{ closeable: true }"
      multiple
      result-type="file"
      class="apple-uploader"
    >
      <van-button 
        icon="plus" 
        class="apple-gradient-button-small"
        round
      >
        上传文件
      </van-button>
    </van-uploader>

    <!-- 上传状态提示 -->
    <van-loading v-if="uploading" size="24px" class="upload-loading">
      上传中...
    </van-loading>

    <van-notice-bar 
      v-if="uploadMessage" 
      :type="uploadMessageType" 
      class="upload-message"
    >
      {{ uploadMessage }}
    </van-notice-bar>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { showToast } from 'vant'
import { uploadGeneralFile, baseURL } from '@/utils/request'

const props = defineProps({
  fileList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['uploadSuccess', 'uploadError', 'fileRemove'])

// 内部文件列表状态
const internalFileList = ref<any[]>([])
const uploading = ref(false)
const uploadMessage = ref('')
const uploadMessageType = ref<'success' | 'danger' | 'warning'>('success')

// 初始化文件列表
onMounted(() => {
  internalFileList.value = props.fileList.map((file: any) => ({
    ...file,
    status: file.status || 'finished',
    url: file.url || (file.name ? `${baseURL()}/file/${file.name}` : void 0),
    isImage: isImageFile(file.name || file.file?.name)
  }))
})

// 监听props变化
watch(() => props.fileList, (newList) => {
  internalFileList.value = newList.map((file: any) => ({
    ...file,
    status: file.status || 'finished',
    url: file.url || (file.name ? `${baseURL()}/file/${file.name}` : void 0),
    isImage: isImageFile(file.name || file.file?.name)
  }))
}, { deep: true })

// 判断是否为图片文件
const isImageFile = (fileName: string) => {
  if (!fileName) return false
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  return imageExtensions.some(ext => fileName.toLowerCase().endsWith(ext))
}

// 文件上传处理
const afterRead = async (file: any) => {
  uploading.value = true
  uploadMessage.value = ''

  try {
    const response = await uploadGeneralFile(file.file)
    
    if (response && response.data && response.data.code === 200) {
      uploadMessageType.value = 'success'
      uploadMessage.value = `文件${response.data.data.originalFilename || response.data.data.name}上传成功`
      
      const uploadedFile = {
        name: response.data.data.name,
        fileUrl: response.data.data.fileUrl,
        id: response.data.data.id,
        status: 'finished',
        response: response.data,
        url: `${baseURL()}/file/${response.data.data.name}`,
        isImage: isImageFile(response.data.data.name)
      }
      
      // 更新文件列表
      internalFileList.value = internalFileList.value.map(f => 
        f.file === file.file ? uploadedFile : f
      )
      
      // 发出上传成功事件
      emit('uploadSuccess', internalFileList.value)
      
    } else {
      uploadMessageType.value = 'danger'
      const errorMsg = response?.data?.msg || '上传失败'
      uploadMessage.value = `上传失败: ${errorMsg}`
      emit('uploadError', errorMsg)
    }
  } catch (error: any) {
    uploadMessageType.value = 'danger'
    uploadMessage.value = `上传失败: ${error.message}`
    emit('uploadError', error.message)
  } finally {
    uploading.value = false
  }
}

// 文件删除确认
const beforeDelete = (file: any, { index }: { index: number }) => {
  // 从列表中移除文件
  internalFileList.value.splice(index, 1)
  
  // 发出文件删除事件
  emit('fileRemove', internalFileList.value)
  
  // 返回false阻止默认删除行为，因为我们手动处理了
  return false
}

// 获取文件预览URL
const getPreviewUrl = (file: any) => {
  if (file.url) return file.url
  if (file.file && file.file instanceof File) {
    return URL.createObjectURL(file.file)
  }
  return ''
}
</script>

<style scoped>
.mobile-upload {
  width: 100%;
}

.apple-uploader {
  width: 100%;
}

.upload-loading {
  margin-top: 12px;
  text-align: center;
  color: var(--van-text-color-2);
}

.upload-message {
  margin-top: 12px;
  border-radius: 8px;
}

:deep(.van-uploader__preview-image) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.van-uploader__preview) {
  margin: 4px;
}

:deep(.van-uploader__upload) {
  margin: 4px;
  border-radius: 8px;
  background: var(--van-background-color-light);
  border: 1px dashed var(--van-border-color);
}

:deep(.van-uploader__upload:hover) {
  border-color: var(--van-primary-color);
}
</style>