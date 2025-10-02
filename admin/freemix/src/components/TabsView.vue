<template>
  <div class="tabs-view-container">
    <n-dropdown 
      :show="showDropdown" 
      :x="dropdownX" 
      :y="dropdownY"
      :options="dropdownOptions"
      @select="handleDropdownSelect"
      @clickoutside="showDropdown = false"
    >
    </n-dropdown>
    <n-tabs 
      v-model:value="activeTab" 
      type="card" 
      closable
      @close="handleClose"
      @update:value="handleTabClick"
      class="tabs-container"
    >
      <n-tab 
        v-for="tab in tabs" 
        :key="tab.path" 
        :name="tab.path"
        @contextmenu="handleContextMenu($event, tab.path)"
      >
        {{ tab.title }}
      </n-tab>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NTabs, NTab, NDropdown } from 'naive-ui'
import { useMessage } from 'naive-ui'

// 获取路由和路由器实例
const route = useRoute()
const router = useRouter()
const message = useMessage()

// 标签页数据
const tabs = ref<Array<{ path: string; title: string }>>([])

// 右键菜单相关数据
const showDropdown = ref(false)
const dropdownX = ref(0)
const dropdownY = ref(0)
const currentTabPath = ref('')

const dropdownOptions = ref([
  {
    label: '重新加载',
    key: 'reload'
  },
  {
    label: '关闭',
    key: 'close'
  },
  {
    label: '关闭其他',
    key: 'closeOthers'
  },
  {
    label: '关闭所有',
    key: 'closeAll'
  }
])

// 从 sessionStorage 恢复标签页数据
const savedTabs = sessionStorage.getItem('tabsView')
if (savedTabs) {
  tabs.value = JSON.parse(savedTabs)
}

const activeTab = ref('')

// 路由名称到标签标题的映射
const routeTitleMap: Record<string, string> = {
  'Home': '首页',
  'GoalStructure': '目标结构',
  'recycle': '回收站',
  'Login': '登录',
  'Register': '注册',
  'AddGoal': '添加目标',
  'GoalManagement': '目标管理',
  'Statistics': '统计',
  'Settings': '设置',
  'LoginLog': '登录日志'
}

// 获取路由标题
const getRouteTitle = (routeName: string) => {
  return routeTitleMap[routeName] || routeName
}

// 保存标签页数据到 sessionStorage
const saveTabsToStorage = () => {
  sessionStorage.setItem('tabsView', JSON.stringify(tabs.value))
}

// 添加标签页
const addTab = (route: any) => {
  // 过滤掉不需要显示标签页的路由
  if (!route.name || route.name === 'Login' || route.name === 'Register') {
    return
  }
  
  const existingTab = tabs.value.find(tab => tab.path === route.path)
  
  if (!existingTab) {
    // 限制标签页数量，最多保留10个
    if (tabs.value.length >= 10) {
      tabs.value.shift() // 移除第一个标签页
    }
    
    tabs.value.push({
      path: route.path,
      title: getRouteTitle(route.name as string)
    })
  }
  
  // 保存到sessionStorage
  saveTabsToStorage()
  
  // 设置当前激活的标签
  activeTab.value = route.path
}

// 关闭标签页
const handleClose = (name: string) => {
  const index = tabs.value.findIndex(tab => tab.path === name)
  
  if (index !== -1) {
    // 如果关闭的是当前激活的标签
    if (activeTab.value === name) {
      // 如果还有其他标签，切换到相邻标签
      if (tabs.value.length > 1) {
        const nextIndex = index === 0 ? 1 : index - 1
        const nextTab = tabs.value[nextIndex]
        activeTab.value = nextTab.path
        router.push(nextTab.path)
      } else {
        // 如果这是最后一个标签，跳转到首页
        activeTab.value = '/home'
        router.push('/home')
      }
    }
    
    // 移除标签
    tabs.value.splice(index, 1)
    saveTabsToStorage()
  }
}

// 关闭所有标签页
const closeAllTabs = () => {
  tabs.value = []
  activeTab.value = '/home'
  router.push('/home')
  saveTabsToStorage()
}

// 关闭其他标签页
const closeOtherTabs = (currentPath: string) => {
  tabs.value = tabs.value.filter(tab => tab.path === currentPath)
  activeTab.value = currentPath
  saveTabsToStorage()
}

// 处理右键菜单事件
const handleContextMenu = (e: MouseEvent, path: string) => {
  e.preventDefault()
  showDropdown.value = true
  dropdownX.value = e.clientX
  dropdownY.value = e.clientY
  currentTabPath.value = path
}

// 处理下拉菜单选择
const handleDropdownSelect = (key: string) => {
  showDropdown.value = false
  
  switch (key) {
    case 'reload':
      // 重新加载当前路由
      router.go(0)
      break
    case 'close':
      handleClose(currentTabPath.value)
      break
    case 'closeOthers':
      closeOtherTabs(currentTabPath.value)
      break
    case 'closeAll':
      closeAllTabs()
      break
  }
}

// 点击标签页
const handleTabClick = (name: string) => {
  const tab = tabs.value.find(tab => tab.path === name)
  if (tab) {
    router.push(tab.path)
  }
}

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    addTab(route)
    // activeTab.value = route.path
  },
//   {immediate: true}
)

// 监听标签页变化并保存到 sessionStorage
watch(
  () => tabs.value,
  () => {
    saveTabsToStorage()
  },
  { deep: true }
)

// 组件挂载时初始化
onMounted(() => {
  // 添加当前路由到标签页
  addTab(route)
  activeTab.value = route.path
})
</script>

<style scoped>
.tabs-view-container {
  background-color: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
}

.tabs-container {
  padding: 0 20px;
}

:deep(.n-tabs-tab) {
  color: var(--text-color);
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-bottom: none;
  transition: all 0.3s;
}

:deep(.n-tabs-tab:hover) {
  background-color: var(--hover-color);
}

:deep(.n-tabs-tab.n-tabs-tab--active) {
  background-color: var(--bg-color);
  border-bottom: 1px solid var(--bg-color);
  font-weight: 500;
}

:deep(.n-tabs-nav) {
  border-bottom: 1px solid var(--border-color);
}
</style>