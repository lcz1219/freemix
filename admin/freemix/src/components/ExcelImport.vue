<template>
  <n-button type="primary" @click="showImportModal">
    <template #icon>
      <n-icon>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
          <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-8 14H7v-2h4v2zm0-3H7v-2h4v2zm0-3H7V9h4v2zm6 6h-4v-2h4v2zm0-3h-4v-2h4v2zm0-3h-4V9h4v2z"/>
        </svg>
      </n-icon>
    </template>
    导入Excel
  </n-button>

  <n-modal v-model:show="showModal" preset="card" style="width: 600px;" title="导入Excel目标" :mask-closable="false">
    <n-space vertical>
      <div class="import-instructions">
        <h3>导入说明</h3>
        <p>请确保您的Excel文件格式如下：</p>
        <n-table :bordered="false" :single-line="false">
          <thead>
            <tr>
              <th>列名</th>
              <th>说明</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>目标名称</td>
              <td>目标的标题（必填）</td>
            </tr>
            <tr>
              <td>目标描述</td>
              <td>目标的详细描述</td>
            </tr>
            <tr>
              <td>截止日期</td>
              <td>目标截止日期（格式：yyyy-MM-dd）</td>
            </tr>
            <tr>
              <td>优先级</td>
              <td>目标优先级（如：高、中、低）</td>
            </tr>
            <tr>
              <td>标签</td>
              <td>目标标签，多个标签用逗号分隔</td>
            </tr>
          </tbody>
        </n-table>
        <p>第一行为标题行，从第二行开始为数据行。支持.xlsx和.xls格式的Excel文件。</p>
      </div>

      <n-upload
        ref="uploadRef"
        :default-file-list="fileList"
        :custom-request="customRequest"
        :max="1"
        @change="handleUploadChange"
      >
        <n-upload-dragger>
          <div style="margin-bottom: 12px">
            <n-icon size="48" :depth="3">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
              </svg>
            </n-icon>
          </div>
          <n-text style="font-size: 16px">
            点击或拖拽文件到该区域上传
          </n-text>
          <n-p depth="3" style="margin: 8px 0 0 0">
            请上传.xlsx或.xls格式的Excel文件
          </n-p>
        </n-upload-dragger>
      </n-upload>
    </n-space>

    <template #footer>
      <n-space justify="end">
        <n-button @click="closeModal">取消</n-button>
        <n-button type="primary" @click="importGoals" :disabled="!canImport" :loading="loading">
          {{ loading ? '导入中...' : '导入目标' }}
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { 
  NButton, 
  NIcon, 
  NModal, 
  NSpace, 
  NUpload, 
  NUploadDragger, 
  NText, 
  NP, 
  NTable,
  useMessage
} from 'naive-ui'
import { postM, isSuccess } from '@/utils/request'
import { useStore } from 'vuex'

const emits = defineEmits(['import-success'])

const store = useStore()
const message = useMessage()

const showModal = ref(false)
const fileList = ref<any[]>([])
const selectedFile = ref<File | null>(null)
const loading = ref(false)
const uploadRef = ref<any>(null)

const canImport = computed(() => {
  return selectedFile.value !== null && !loading.value
})

const showImportModal = () => {
  showModal.value = true
  fileList.value = []
  selectedFile.value = null
}

const closeModal = () => {
  showModal.value = false
  fileList.value = []
  selectedFile.value = null
  loading.value = false
}

const handleUploadChange = (data: any) => {
  const { file, fileList: currentFileList } = data
  if (file.status === 'removed') {
    selectedFile.value = null
    return
  }
  
  // 检查文件类型
  if (file.file && (file.file.name.endsWith('.xlsx') || file.file.name.endsWith('.xls'))) {
    selectedFile.value = file.file
  } else {
    message.error('请上传.xlsx或.xls格式的Excel文件')
    // 移除不支持的文件
    if (uploadRef.value) {
      uploadRef.value.clear()
    }
    selectedFile.value = null
  }
}

const customRequest = (options: any) => {
  // 自定义上传请求，这里我们只需要文件对象
  return {
    abort: () => {}
  }
}

const importGoals = async () => {
  if (!selectedFile.value) {
    message.warning('请先选择一个Excel文件')
    return
  }

  loading.value = true
  
  try {
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('owner', store.state.user.username)
    
    // 使用项目中一致的请求方式
    const response = await postM('importGoalsFromExcel', formData)
    
    if (isSuccess(response)) {
      message.success(response.data.msg || '目标导入成功')
      emits('import-success')
      closeModal()
    } else {
      message.error(response.data.msg || '导入失败')
    }
  } catch (error) {
    console.error('导入失败:', error)
    message.error('导入过程中发生错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.import-instructions {
  margin-bottom: 20px;
}

.import-instructions h3 {
  margin-bottom: 10px;
}

.import-instructions p {
  margin: 5px 0;
}
</style>