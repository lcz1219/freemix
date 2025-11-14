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
    <div class="tabs-header">
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
      <div class="theme-switch-container">
        <n-tooltip placement="bottom">
          <template #trigger>
            <n-switch 
              v-model:value="isDark" 
              :rail-style="railStyle" 
              @update:value="toggleTheme"
              class="theme-switch"
            >
              <template #icon>
                <n-icon v-if="isDark" :component="MoonIcon" />
                <n-icon v-else :component="SunIcon" />
              </template>
            </n-switch>
          </template>
          <span>{{ isDark ? '深色模式' : '浅色模式' }}</span>
        </n-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick, inject,  } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NTabs, NTab, NDropdown, NSwitch, NTooltip, NIcon } from 'naive-ui'
import { useMessage } from 'naive-ui'
import { SunnyOutline, MoonOutline } from '@vicons/ionicons5'

// 获取路由和路由器实例
const route = useRoute()
const router = useRouter()
const message = useMessage()

// 主题相关
const isDark = inject('isDark', ref(false))
const toggleTheme = inject('toggleTheme', (value) => {})
const railStyle = inject('railStyle', ({ focused, checked }) => {
  const style = {}
  if (checked) {
    style.background = '#81c683'
    if (focused) style.boxShadow = '0 0 0 2px rgba(129, 198, 131, 0.3)'
  } else {
    style.background = '#e0e0e0'
    if (focused) style.boxShadow = '0 0 0 2px rgba(224, 224, 224, 0.3)'
  }
  return style
})

// 图标组件
const SunIcon = SunnyOutline
const MoonIcon = MoonOutline

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
  'LoginLog': '登录日志',
  "Messages": "消息",
  "Profile": "个人信息",
  "UserGuide": "使用指引"
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
  console.log("close:",name);
  
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
const closeOauthCallback = () => {
  handleClose("/oauth/callback")
}

// 组件挂载时初始化
onMounted(() => {
 
  // 添加当前路由到标签页
  addTab(route)
  activeTab.value = route.path
    // 延迟执行删除，确保标签页已添加
  setTimeout(() => {
    closeOauthCallback()
  }, 200)
})
defineExpose({
  handleClose
});
</script>

<style scoped>
.tabs-view-container {
  background-color: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(10px);
  background-color: rgba(var(--card-bg-rgb), 0.85);
}

.tabs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.tabs-container {
  flex: 1;
}

.theme-switch-container {
  margin-left: 20px;
  display: flex;
  align-items: center;
}

.theme-switch {
  width: 40px;
  height: 20px;
}

:deep(.n-tabs-nav) {
  border-bottom: 1px solid var(--border-color);
  padding: 8px 0;
}

:deep(.n-tabs-tab) {
  color: var(--text-color);
  background-color: transparent;
  border: none;
  border-radius: 8px 8px 0 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 10px 20px;
  font-weight: 500;
  position: relative;
  margin-right: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(10px);
}

:deep(.n-tabs-tab:hover) {
  background-color: var(--hover-color);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

:deep(.n-tabs-tab.n-tabs-tab--active) {
  background: linear-gradient(145deg, var(--card-bg), var(--hover-color));
  color: var(--text-color);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

:deep(.n-tabs-tab.n-tabs-tab--active)::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #81c683, #5baa73);
  border-radius: 3px;
}

:deep(.n-tabs-tab__close) {
  margin-left: 8px;
  border-radius: 6px;
  padding: 3px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0.7;
  background-color: transparent;
}

:deep(.n-tabs-tab__close:hover) {
  background-color: rgba(129, 198, 131, 0.2);
  color: #81c683;
  opacity: 1;
  transform: scale(1.1);
}

:deep(.n-tabs-tab):hover .n-tabs-tab__close {
  opacity: 1;
}

:deep(.n-dropdown) {
  border-radius: 12px;
  padding: 6px 0;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--border-color);
  background-color: var(--card-bg);
  backdrop-filter: blur(20px);
}

:deep(.n-dropdown-option) {
  padding: 10px 16px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 0;
}

:deep(.n-dropdown-option:hover) {
  background-color: var(--hover-color);
  color: var(--text-color);
}

:deep(.n-dropdown-option__label) {
  color: var(--text-color);
  font-weight: 500;
}

/* 添加深色模式下的特殊效果 */
.dark-theme :deep(.n-tabs-tab.n-tabs-tab--active) {
  background: linear-gradient(145deg, #2a2a2a, #1e1e1e);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

/* 添加浅色模式下的特殊效果 */
.light-theme :deep(.n-tabs-tab.n-tabs-tab--active) {
  background: linear-gradient(145deg, #ffffff, #f5f5f5);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  .tabs-container {
    padding: 0 10px;
  }
  
  :deep(.n-tabs-tab) {
    padding: 8px 16px;
    font-size: 14px;
    margin-right: 4px;
  }
  
  :deep(.n-tabs-tab__close) {
    margin-left: 6px;
  }
}
</style>