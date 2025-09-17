<template>
  <n-modal v-model:show="showModal" preset="card" :style="{ width: '700px' }" title="协作人管理" draggable>
    <div class="collaborators-modal-content">
      <n-tabs type="line" default-value="current">
        <n-tab-pane name="current" tab="当前协作人">
          <div v-if="collaborators && collaborators.length > 0">
            <n-list hoverable clickable>
              <n-list-item v-for="collaborator in collaborators" :key="collaborator.id">
                <n-thing :title="collaborator.chinesename || collaborator.username" :description="collaborator.email">
                  <template #avatar>
                    <n-avatar 
                      circle 
                      size="large" 
                      :src="collaborator.avatarUrl ? `${baseURL()}${collaborator.avatarUrl}` : 'https://api.dicebear.com/7.x/miniavs/svg?seed=' + collaborator.username"
                      :class="{ 'owner-collaborator': collaborator.role === 'owner', 'normal-collaborator': collaborator.role !== 'owner' }"
                    />
                  </template>
                  <template #header-extra>
                    <n-space>
                      <n-tag :type="collaborator.role === 'owner' ? 'warning' : 'info'" size="small">
                        {{ collaborator.role === 'owner' ? '创建者' : '协作者' }}
                      </n-tag>
                      <n-button 
                        v-if="collaborator.role !== 'owner'" 
                        size="tiny" 
                        type="error" 
                        @click="removeCollaborator(collaborator)"
                      >
                        移除
                      </n-button>
                    </n-space>
                  </template>
                </n-thing>
              </n-list-item>
            </n-list>
          </div>
          <n-empty v-else description="暂无协作人" />
        </n-tab-pane>
        
        <n-tab-pane name="add" tab="添加协作人">
          <n-space vertical>
            <n-alert type="info" title="添加协作人">
              从系统用户列表中选择用户作为协作人，协作人可以查看和更新目标进度。
            </n-alert>
            
            <n-select
              v-model:value="selectedCollaborator"
              :options="userOptions"
              placeholder="请选择用户"
              :loading="loadingUsers"
              filterable
              clearable
            />
            
            <n-button 
              type="primary" 
              :disabled="!selectedCollaborator" 
              :loading="addingCollaborator"
              @click="addCollaborator"
              block
            >
              添加协作人
            </n-button>
          </n-space>
        </n-tab-pane>
      </n-tabs>
    </div>
    
    <template #footer>
      <n-space justify="end">
        <n-button @click="closeModal">
          关闭
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { NModal, NList, NListItem, NThing, NAvatar, NTag, NButton, NSpace, NTabs, NTabPane, NSelect, NAlert, NEmpty, useMessage } from 'naive-ui'
import { baseURL } from '@/utils/request'
// import { getOwerList } from '@/api/login'
import { postM, isSuccess } from '@/utils/request'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  goal: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:show', 'collaborators-updated'])

const message = useMessage()

// 模态框显示状态
const showModal = computed({
  get: () => props.show,
  set: (value) => {
    emit('update:show', value)
  }
})

// 协作人管理相关状态
const collaborators = ref([])
const userOptions = ref([])
const selectedCollaborator = ref(null)
const loadingUsers = ref(false)
const addingCollaborator = ref(false)

// 打开模态框时加载协作人列表和用户选项
onMounted(async () => {
  if (props.show) {
    await loadCollaborators()
    await loadUserOptions()
  }
})

// 监听show属性变化
watch(() => props.show, async (newShow) => {
  if (newShow) {
    await loadCollaborators()
    await loadUserOptions()
  }
})

// 加载协作人列表
const loadCollaborators = async () => {
  try {
    // 这里假设有一个API可以获取目标的协作人列表
    // 如果没有，我们可以从目标数据中提取
    if (props.goal) {
      // 假设目标数据中有collaborators字段
      if (props.goal.collaborators) {
        collaborators.value = props.goal.collaborators
      } else {
        // 如果没有，我们可以从owner和负责人中构建
        collaborators.value = [
          {
            id: props.goal.ownerId,
            username: props.goal.ownerName,
            chinesename: props.goal.ownerChinesename,
            email: props.goal.ownerEmail,
            role: 'owner'
          }
        ]
        
        // 如果有负责人且不是所有者，添加到协作人列表
        if (props.goal.principalId && props.goal.principalId !== props.goal.ownerId) {
          collaborators.value.push({
            id: props.goal.principalId,
            username: props.goal.principalName,
            chinesename: props.goal.principalChinesename,
            email: props.goal.principalEmail,
            role: 'collaborator'
          })
        }
      }
    }
  } catch (error) {
    console.error('加载协作人列表失败:', error)
    message.error('加载协作人列表失败')
  }
}

// 加载用户选项
const loadUserOptions = async () => {
  try {
    loadingUsers.value = true
    const response = await getOwerList()
    if (response && response.data) {
      // 过滤掉已经是协作人的用户
      const collaboratorIds = collaborators.value.map(c => c.id)
      userOptions.value = response.data
        .filter(user => !collaboratorIds.includes(user.value))
        .map(user => ({
          label: user.text || user.username || user.chinesename,
          value: user.value || user.id
        }))
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    message.error('加载用户列表失败')
  } finally {
    loadingUsers.value = false
  }
}

// 添加协作人
const addCollaborator = async () => {
  if (!selectedCollaborator.value) return
  
  try {
    addingCollaborator.value = true
    
    // 找到选中的用户信息
    const selectedUser = userOptions.value.find(option => option.value === selectedCollaborator.value)
    if (!selectedUser) return
    
    // 调用API添加协作人
    const response = await postM('/api/goal/addCollaborator', {
      goalId: props.goal.id,
      userId: selectedCollaborator.value
    })
    
    if (isSuccess(response)) {
      // 添加到协作人列表
      collaborators.value.push({
        id: selectedCollaborator.value,
        username: selectedUser.label,
        role: 'collaborator'
      })
      
      // 重置选择
      selectedCollaborator.value = null
      
      // 重新加载用户选项（过滤掉已添加的协作人）
      await loadUserOptions()
      
      message.success('添加协作人成功')
      // 通知父组件协作人列表已更新
      emit('collaborators-updated', collaborators.value)
    } else {
      message.error('添加协作人失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('添加协作人失败:', error)
    message.error('添加协作人失败：' + (error.message || '未知错误'))
  } finally {
    addingCollaborator.value = false
  }
}

// 移除协作人
const removeCollaborator = async (collaborator) => {
  try {
    // 调用API移除协作人
    const response = await postM('/api/goal/removeCollaborator', {
      goalId: props.goal.id,
      userId: collaborator.id
    })
    
    if (isSuccess(response)) {
      // 从协作人列表中移除
      collaborators.value = collaborators.value.filter(c => c.id !== collaborator.id)
      
      // 重新加载用户选项（添加回被移除的用户）
      await loadUserOptions()
      
      message.success('移除协作人成功')
      // 通知父组件协作人列表已更新
      emit('collaborators-updated', collaborators.value)
    } else {
      message.error('移除协作人失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('移除协作人失败:', error)
    message.error('移除协作人失败：' + (error.message || '未知错误'))
  }
}

// 关闭模态框
const closeModal = () => {
  showModal.value = false
}

// 导出方法供父组件调用
defineExpose({
  loadCollaborators,
  closeModal
})
</script>

<style scoped>
.collaborators-modal-content {
  max-height: 500px;
  overflow-y: auto;
}

.owner-collaborator {
  border: 2px solid #f0a020;
}

.normal-collaborator {
  border: 1px solid #d9d9d9;
}
</style>