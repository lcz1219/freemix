<template>
  <div>
    
    <n-upload
      multiple
      directory-dnd
      list-type="image"
      
      :max="5"
      @change="handleChange"
      :file-list="internalFileList"
      :show-file-list="true"
      :create-thumbnail-url="createThumbnailUrl"
      :download-url="getDownloadUrl"
      :show-download-button="true"
      :show-remove-button="true"
      @remove="handleRemove"
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
import { ref,defineProps,onMounted,watch,onBeforeMount } from 'vue'
import { ArchiveOutline } from '@vicons/ionicons5'
import { uploadGeneralFile,baseURL} from '../utils/request.js'
const props = defineProps({
  fileList: {
    type: Array,
    default: () => []
  }
})

// 定义内部fileList状态，用于管理上传后的文件
const internalFileList = ref([])

// 初始化时格式化props.fileList
onBeforeMount(() => {
  // 确保初始化的文件列表格式正确
  internalFileList.value = props.fileList.map(file => ({
    ...file,
    // 确保文件对象有status属性，这对显示按钮很重要
    status: file.status || 'finished',
    // 确保文件对象有url属性
    url: file.url || (file.name ? `${baseURL()}/file/${file.name}` : void 0)
  }))
})

// 监听props.fileList变化，同步到内部状态
watch(() => props.fileList, (newList) => {
  // 同步时格式化文件列表，确保所有文件格式正确
  internalFileList.value = newList.map(file => ({
    ...file,
    status: file.status || 'finished',
    url: file.url || (file.name ? `${baseURL()}/file/${file.name}` : void 0)
  }))
  imagegen()
}, { deep: true })
const imagegen=async ()=>{
  for (const e of internalFileList.value) {
    const thumbnailUrl = await createThumbnailUrl(e);
    // 将生成的缩略图URL设置到文件对象上
    e.url = thumbnailUrl;
  }
}
onMounted(async () => {
  console.log('props.fileList:', props.fileList);
  console.log('internalFileList:', internalFileList.value);
  imagegen()
  // 确保初始文件能够显示缩略图
  
})
const emit = defineEmits(['uploadSuccess', 'uploadError', 'fileRemove'])
const uploading = ref(false)
const uploadMessage = ref('')
const uploadMessageType = ref('success')
const createThumbnailUrl = (file) => {
  console.log("file", file);
  
  if (!file) {
    return void 0;
  }
  
  // 使用file.name拼接URL
  return new Promise((resolve) => {
    setTimeout(() => {
      console.log("生成的缩略图URL:", `${baseURL()}/file/${file.id}`);
      resolve(`${baseURL()}/file/${file.name}`);
    }, 100);
  });
}

// 生成下载链接
const getDownloadUrl = (file) => {
  console.log("生成下载URL:", file);
  if (!file) {
    return void 0;
  }
  
  // 对于本地文件对象，直接返回文件对象
  if (file.file instanceof File) {
    return file.file;
  }
  
  // 对于已上传的文件，使用name生成下载URL
  return `${baseURL()}/file/${file.name}`;
}

// 处理文件删除
const handleRemove = (file) => {
  console.log('删除文件:', file.file);
  
  try {
    // 从内部fileList中移除文件，考虑多种匹配条件以增加健壮性
    internalFileList.value = internalFileList.value.filter(f => 
      (f.name && f.name != file['file']['name'])  
      
    );
    console.log("handleRemove",internalFileList.value);
    // 发出文件删除事件，通知父组件
    emit('fileRemove', internalFileList.value);
  } catch (error) {
    console.error('删除文件时出错:', error);
  }
}
const handleChange = async ({ file, fileList }) => {
  // 重置状态
  
  file.percentage=0
  if (file.status === 'pending') {
    uploading.value = true
    
    try {
      const response = await uploadGeneralFile(file.file)
      
      if (response && response.data && response.data.code === 200) {
        
        uploadMessageType.value = 'success'
        uploadMessage.value = `文件${response.data.data.originalFilename || response.data.data.name}上传成功`
        
        // 准备要添加到fileList的文件信息
        const uploadedFile = {
          name: response.data.data.name,
          fileUrl: response.data.data.fileUrl,
          id: response.data.data.id,
          // 为了让n-upload组件能够正确显示上传成功的状态
          status: 'finished',
          response: response.data,
          url: `${baseURL()}/file/${response.data.data.name}`
        }
       
        // 将上传成功的文件添加到内部fileList
        internalFileList.value.push(uploadedFile)
        
        // 发出上传成功事件，传递文件路径
        emit('uploadSuccess', internalFileList.value)
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