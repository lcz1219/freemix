<template>
  <n-config-provider :theme="isDark ? darkTheme : null" :theme-overrides="themeOverrides" :locale="locale"
    :date-locale="dateLocale" class="config-provider">
    <van-config-provider :theme="'dark'">
      <!-- 自定义拖动区域（仅桌面端）- 仅窗口顶部边缘 -->
      <div v-if="!isMobileDevice" class="drag-region">
        <!-- 仅用于窗口拖动的透明区域 -->
      </div>


      <!-- 全局样式 -->
      <n-global-style />
      <n-dialog-provider>
        <n-loading-bar-provider>
          <n-notification-provider>
            <!-- 消息提供器 -->
            <n-message-provider>
              <!-- 路由视图 - 应用主题类 -->
              <n-layout position="absolute" class="app-layout" has-sider v-if="isShowSidebar">
                <!-- 侧边栏导航 -->
                <n-layout-sider v-if="!isMobileDevice && isnAiPage" bordered collapse-mode="transform"
                  :collapsed-width="64" show-collapsed-content :width="isSidebarCollapsed ? 70 : 215"
                  :native-scrollbar="false" class="side-navbar">
                  <NavBar v-if="showContentByStoreUser && !isMobileDevice" :active-tab="activeTab"
                    @update:collapsed="isSidebarCollapsed = $event" />
                </n-layout-sider>

                <!-- 主内容区域 -->
                <n-layout class="main-layout">

                  <TabsView v-if="isnAiPage && !isMobileDevice"></TabsView>
                  <!-- 页面内容 -->
                  <n-layout-content :class="isnAiPage ? content - wrapper : content - wrappe - ai">

                    <router-view v-if="showContentByStoreUser || route.path == '/oauth/callback'" :class="themeClass" />
                    <!-- 应用加载页面 -->
                    <AppLoading v-else />
                  </n-layout-content>
                </n-layout>
              </n-layout>
              <router-view v-else :class="themeClass" />

              <!-- 全局加载提示 -->
              <n-modal v-model:show="isLoading" :show-icon="false" :closable="false" :mask-closable="false"
                class="global-loading-modal">
                <n-card :bordered="false" class="global-loading-card">
                  <div class="loading-content">
                    <n-spin size="large" />
                    <div class="loading-text">{{ loadingText }}</div>
                  </div>
                </n-card>
              </n-modal>

              <!-- OAuth 登录中提示 -->
              <n-modal v-model:show="oauthLoading" :show-icon="false" :closable="false" :mask-closable="false"
                class="global-loading-modal">
                <n-card :bordered="false" class="global-loading-card">
                  <div class="loading-content">
                    <n-spin size="large" />
                    <div class="loading-text">登录中...</div>
                  </div>
                </n-card>
              </n-modal>

              <!-- 移动端浮动导航组件 -->
              <MobileFloatingNav v-if="false" />
              <!-- <MessageCenter ref="mcter" v-show="false"/> -->
              <!-- 全局浮动按钮组件 -->
              <UnifiedFloatButton v-if="showContentByStoreUser && isnAiPage && !isMobileDevice" :goals="goals"
                :formatDate="formatDate" :checktype="checktype" @dateSelected="handleCalendarUpdate" />
              <UpdateNotification ref="updateNotification" v-show="showUpdateNotification" />
              <div style="display: flex;
              justify-content: flex-end;
              align-items: flex-end;
              position: fixed;
              bottom: 10px;
              color: #2c2c2c;
              right: 10px;" v-if="!isDesktop() && !isMobileDevice">
                <!-- 备案信息 -->
                <div class="">
                  <a href="https://beian.miit.gov.cn/" style="margin-top: 5px;" class="beian-link"
                    target="_blank">粤ICP备2025487297号-1</a>
                </div>｜
                <div class="">
                  <!-- <img src="/icons/beian.png" alt="粤公网安备" class="beian-icon" /> -->
                  <a href="https://beian.mps.gov.cn/#/query/webSearch?code=44010602014148" rel="noreferrer"
                    target="_blank" class="beian-link">粤公网安备44010602014148号</a>
                </div>
              </div>

              <!-- 全局 AI 助手抽屉 -->
              <n-drawer v-model:show="showAiDrawer" :default-width="1200" :default-height="100" resizable
                placement="right" class="ai-sidebar-drawer" :show-mask="true">
                <n-drawer-content closable>
                  <template #header>
                    <div class="ai-sidebar-header">
                      <n-icon size="24" color="#8a2be2">
                        <AIAssistantIcon />
                      </n-icon>
                      <span>Freemix AI </span>
                    </div>
                  </template>
                  <div class="ai-sidebar-body">
                    <AIAssistantWindow />
                  </div>
                </n-drawer-content>
              </n-drawer>
            </n-message-provider>
          </n-notification-provider>
        </n-loading-bar-provider>
      </n-dialog-provider>
      <!-- 全局成就庆祝动画 -->
      <CelebrationOverlay :show="showCelebration" :title="celebrationData.title" :heading="celebrationData.heading"
        :sub-heading="celebrationData.subHeading" @close="showCelebration = false" />
    </van-config-provider>

  </n-config-provider>
</template>

<script setup lang="ts">
import CelebrationOverlay from '@/components/CelebrationOverlay.vue';
import { isMobile } from '@/utils/device.js'
import { ref, computed, onMounted, watch, type CSSProperties, provide, onUnmounted, nextTick } from 'vue';
import { useStore } from 'vuex'
import TabsView from '@/components/TabsView.vue';
import { saveToken, getToken } from '@/utils/tokenUtils.js';
import { useRoute, useRouter } from 'vue-router';
import globalMessageListener from '@/utils/globalMessageListener.js';
import { App } from '@capacitor/app';
import { Browser } from '@capacitor/browser';
import { Toast } from '@capacitor/toast';
import {
  NConfigProvider,
  darkTheme,
  NSwitch,
  NMessageProvider,
  NNotificationProvider,
  type GlobalThemeOverrides,
  NGlobalStyle,
  NDialogProvider,
  NLoadingBarProvider,
  NTooltip,
  NIcon,
  zhCN,
  dateZhCN,
  NLayout,
  NLayoutSider,
  NLayoutHeader,
  NLayoutContent,
  NModal,
  NSpin,
  NCard
} from 'naive-ui';
import upload from '@/components/upload.vue';
import AIAssistantIcon from '@/components/icons/AIAssistantIcon.vue';
import { SunnyOutline, MoonOutline } from '@vicons/ionicons5';
import MobileFloatingNav from '@/components/MobileFloatingNav.vue';
import NavBar from '@/components/NavBar.vue';
import MessageCenter from './views/MessageCenter.vue';
import AppLoading from '@/components/AppLoading.vue';
import UpdateNotification from '@/components/UpdateNotification.vue';
import UnifiedFloatButton from '@/components/UnifiedFloatButton.vue'; // 导入加载页面组件
import AIAssistantWindow from '@/components/AIAssistantWindow.vue';
import { SparklesOutline } from '@vicons/ionicons5';
import request, { postM, isSuccess, getM, getMPaths } from '@/utils/request'
import { isDesktop } from '@/utils/device.js'
import { saveToken as saveTokenUtil } from '@/utils/tokenUtils.js';
import { getLocalStorageDesktopToken } from '@/utils/desktopToken.js'
import { connect, disconnect } from '@/utils/websocket.js'
import { genMsg } from '@/utils/genMsg.js'
const store = useStore()

const showContentByStoreUser = computed(() => {
  return store.state.user && Object.keys(store.state.user).length !== 0
});

// 监听用户状态，登出时断开 WebSocket
watch(showContentByStoreUser, (newValue) => {
  if (!newValue) {
    console.log('User logged out, disconnecting WebSocket...');
    disconnect();
  }
});

// 添加桌面token加载状态

const isnAiPage = computed(() => {
  return route.path != '/AIAssistantWindow' && !route.path.includes('/share/')
})
const getDeskToken = async () => {
  // 将多个值拼接成一个字符串
  const res = await getToken()
  // alert(`用户信息: ${localStorage.getItem("user")}, Token: ${localStorage.getItem("token")}, deskToken: ${res}`);
  if (isDesktop()) {
    console.log('getDeskTokenStrore')
    const res = await postM('/getDeskTokenStrore');
    let user = res.data.data
    if (user) {
      await store.commit('saveUser', user);

    } else {
      await store.dispatch('logout');
      router.push('/login');
    }
    // 验证成功，完成登录流程
    // 使用tokenUtils工具函数保存token
    // await saveToken(user.deskToken);
  }
  // 设置加载完成

}


// 主题状态管理
const isDark = ref(true);
const isMobileDevice = isMobile(); // 调用函数获取是否为移动端

// 侧边栏折叠状态
const isSidebarCollapsed = ref(true);
const mcter = ref(null)
// 计算当前激活的标签
const activeTab = computed(() => {
  const path = route.path;
  if (path === '/home' || path === '/') return 'dashboard';
  if (path.includes('/goal-management')) return 'goalmanagement';
  if (path.includes('/statistics')) return 'statistics';
  if (path.includes('/goal-structure')) return 'goalstructure';
  if (path.includes('/goal-library')) return 'goallibrary';
  if (path.includes('/messages')) return 'messages';
  if (path.includes('/recurring-goals')) return 'recurring-goals';
  return 'dashboard';
});

// 路由信息
const route = useRoute();
const router = useRouter();

const locale = computed(() => {
  return zhCN;
});
const dateLocale = computed(() => {
  return dateZhCN;
});

// 计算主题类
const themeClass = computed(() => {
  return isDark.value ? 'dark-theme' : 'light-theme';
});

// 导航到指定路径
const navigateTo = (path: string) => {
  showNavigation.value = false;
  router.push(path);
};

// 切换主题并保存状态
const toggleTheme = (value: boolean) => {
  isDark.value = value;
  localStorage.setItem('theme-dark', JSON.stringify(value));
  updateBodyTheme();
};

// 更新body主题类
const updateBodyTheme = () => {
  if (isDark.value) {
    document.body.classList.add('dark-theme');
    document.body.classList.remove('light-theme');
  } else {
    document.body.classList.add('light-theme');
    document.body.classList.remove('dark-theme');
  }
};
const currentUser = computed(() => store.state.user)
import { h } from 'vue'
import { ElNotification } from 'element-plus'
const handleIncomingMessage = (messageStr) => {
  try {
    // 解析接收到的消息
    const message = JSON.parse(messageStr);
    console.log("handleIncomingMessage,message:", message);

    // 检查是否是发给当前用户的消息
    if (message.toUser === currentUser.value.username) {
      // 检查是否已经显示过该消息，使用消息ID或创建时间+内容作为唯一标识
      const messageId = message.id || `${message.createdAt}_${message.content}`;
      const processedMessages = JSON.parse(sessionStorage.getItem('processed_message_ids') || '[]');

      if (processedMessages.includes(messageId)) {
        console.log("Message already processed, skipping notification:", messageId);
        return;
      }

      // 限制存储最近的50条消息ID，防止 sessionStorage 过大
      processedMessages.push(messageId);
      if (processedMessages.length > 50) processedMessages.shift();
      sessionStorage.setItem('processed_message_ids', JSON.stringify(processedMessages));

      if (isDesktop()) {
        genMsg(`${message.fromUserChinesename || message.fromUser}: ${message.content}`);
      } else {
        // 根据当前主题设置通知样式
        const isDarkTheme = isDark.value;
        const notificationStyle = isDarkTheme ? {
          background: '#1e1e1e',
          border: '1px solid #333333',
          color: '#e0e0e0'
        } : {
          background: '#ffffff',
          border: '1px solid #e0e0e0',
          color: '#333333'
        };

        ElNotification({
          title: message.fromUserChinesename || message.fromUser,
          message: h('i', {
            style: `color: ${isDarkTheme ? '#00c9a7' : '#00c9a7'}; font-style: normal; font-weight: 500;`
          }, message.content),
          dangerouslyUseHTMLString: true,
          duration: 4500,
          customClass: isDarkTheme ? 'websocket-notification-dark' : 'websocket-notification',
          style: notificationStyle
        });
      }
      // 显示消息到消息中心
      // console.log('messageCenter.value', messageCenter.value);
      // nextTick(async ()=>{
      //  await mcter.value.fetchMessages()
      //   mcter.value.scrollToBottom()
      // })
      // 触发全局事件通知其他组件（如MessageCenter视图）刷新消息
      window.dispatchEvent(new CustomEvent('reload-messages'));


    }
  } catch (error) {
    console.error('解析WebSocket消息失败:', error);
  }
}

const updateNotification = ref(null);
const error = ref(false)
const errorMessage = ref('')
const oauthLoading = ref(false) // OAuth 登录 loading 状态

// 初始化时读取保存的主题状态
onMounted(async () => {
  // Capacitor 应用唤回监听 (处理 QQ 登录等外部认证后的 Token 回传)
  if ((window as any).Capacitor) {
    console.log('注册 appUrlOpen 监听器...');
    App.addListener('appUrlOpen', async (data: any) => {
      oauthLoading.value = true // 显示登录中
      const logMsg = `App 唤回 URL: ${data.url}`;
      console.log(logMsg);
      // await Toast.show({ text: logMsg, duration: 'long' });

      try {
        const url = new URL(data.url);
        // 匹配 Scheme: mobile.freemix.app://oauth/callback?token=xxx&qqOpenId=xxx
        if (url.host === 'oauth' || url.pathname.includes('/oauth')) {
          const params = url.searchParams;
          const token = params.get('token');
          const qqOpenId = params.get('qqOpenId');
          const githubId = params.get('githubId');

          const queryMsg = `解析参数: token=${token}, qqOpenId=${qqOpenId}, githubId=${githubId}`;
          console.log(queryMsg);
          // await Toast.show({ text: queryMsg, duration: 'long' });

          if (token) {
            // 构造跳转参数并导航到回调页面处理逻辑
            const query: any = { token };
            if (qqOpenId) query.qqOpenId = qqOpenId;
            if (githubId) query.githubId = githubId;

            console.log('准备跳转到回调页面:', query);

            // 尝试关闭应用内浏览器
            try {
              await Browser.close();
            } catch (e) {
              console.warn('关闭浏览器失败:', e);
            }

            // 导航到专门的回调处理组件
            router.push({
              path: '/oauth/callback',
              query: query
            });
            oauthLoading.value = false; // 关闭登录中提示
          } else {
            const noTokenMsg = '唤回成功但未找到 Token';
            console.error(noTokenMsg);
            // await Toast.show({ text: noTokenMsg, duration: 'long' });
            oauthLoading.value = false; // 关闭登录中提示
          }
        } else {
          const unknownHostMsg = `未知的唤回路径: ${url.host}`;
          console.warn(unknownHostMsg);
          // await Toast.show({ text: unknownHostMsg, duration: 'short' });
          oauthLoading.value = false; // 关闭登录中提示
        }
      } catch (err: any) {
        const errClsMsg = `解析唤回 URL 失败: ${err.message}`;
        console.error(errClsMsg, err);
        // await Toast.show({ text: errClsMsg, duration: 'long' });
        oauthLoading.value = false; // 关闭登录中提示
      }
    });
  }

  getDeskToken();
  const savedTheme = localStorage.getItem('theme-dark');
  if (savedTheme) {
    isDark.value = JSON.parse(savedTheme);
  }
  updateBodyTheme();

  // 先绑定回调函数，防止连接建立后消息丢失或回调未定义
  window.handleWebSocketMessage = handleIncomingMessage;

  // 确保用户信息存在后再连接
  if (store.state.user && store.state.user.username) {
    await connect();
  } else {
    console.warn('User info not found in store, waiting for auth...');
    // 可以添加一个 watcher 或在登录成功后调用 connect
    // 这里简单处理：如果 store 里没用户，尝试 1 秒后连接（假设是恢复延迟）
    setTimeout(async () => {
      if (store.state.user && store.state.user.username) {
        await connect();
      }
    }, 1000);
  }

  // 启动全局消息监听器
  // globalMessageListener.startListening();

  // 获取目标数据
  await getGoals();
  isUpdateNotification()

});
const showUpdateNotification = ref(false);
const isUpdateNotification = async () => {
  const res = await getToken()
  if (res && router.path !== '/login') {
    updateNotification.value.checkLatestUpdate()
    showUpdateNotification.value = true
  } else {
    showUpdateNotification.value = false
  }
}

// 判断当前用户是否为开发者 (linchengzhong)
const isShowSidebar = computed(() => {
  const list = ['/login', '/register']
  return !list.includes(route.path)

})

// 目标数据
const goals = ref([]);

// 获取目标数据
const getGoals = async () => {
  console.log("store.state.user ", store.state.user.username);

  if (!store.state.user || !store.state.user.username) return;

  const res = await getMPaths("getGoals", store.state.user.username, "正在获取目标数据...");
  if (isSuccess(res)) {
    goals.value = res.data.data;
    goals.value?.forEach(goal => {
      goal.deadlineString = formatDate(goal.deadline);
    });
  }
}

// 格式化日期
const formatDate = (goal) => {
  // 检查deadline是否存在且为有效日期
  if (!goal) {
    return '未设置';
  }

  const date = new Date(goal);

  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    return '日期无效';
  }

  return `${date.getFullYear()}年${(date.getMonth() + 1).toString().padStart(2, '0')}月${date.getDate().toString().padStart(2, '0')}日`;
}

// 检查目标类型
const checktype = (val) => {
  if (!val || !val.tags || !val.tags[0]) return 'default';

  switch (val.tags[0]) {
    case '学习':
      return 'success';
    case '工作':
      return 'info';
    case '生活':
      return 'warning';
    case '运动':
      return 'error';
    default:
      return 'default';
  }
}

// 处理日历日期选择
const handleCalendarUpdate = (value) => {
  console.log('选择的日期:', value);
  // 可以在这里添加处理日期选择的逻辑
  // 例如：显示该日期的目标或任务
}

// loading 状态
const isLoading = computed(() => store.state.loading.loading);
const loadingText = computed(() => store.state.loading.loadingText);

// AI 抽屉状态
const showAiDrawer = computed({
  get: () => store.state.showAiDrawer,
  set: (val) => store.commit('setAiDrawer', val)
});

// 主题样式覆盖配置
const themeOverrides = computed<GlobalThemeOverrides>(() => {
  const commonOverrides = {
    // 统一字体
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
    // 统一圆角
    borderRadius: '8px'
  };

  if (isDark.value) {
    return {
      common: {
        ...commonOverrides,
        bodyColor: '#121212',
        cardColor: '#1e1e1e',
        modalColor: '#252525',
        popoverColor: '#252525',
        tableColor: '#1e1e1e',
        textColorBase: '#e0e0e0',
        borderColor: '#333333',
        hoverColor: '#2a2a2a',
        primaryColor: '#00c9a7',
        primaryColorHover: '#00c9a7',
        primaryColorPressed: '#00c9a7',
        primaryColorSuppl: '#00c9a7'
      },
      Switch: {
        railColorActive: '#00c9a7'
      }
    };
  } else {
    return {
      common: {
        ...commonOverrides,
        bodyColor: '#f8f9fa',
        cardColor: '#ffffff',
        modalColor: '#ffffff',
        popoverColor: '#ffffff',
        tableColor: '#ffffff',
        textColorBase: '#333333',
        borderColor: '#e0e0e0',
        hoverColor: '#f5f5f5',
        primaryColor: '#00c9a7',
        primaryColorHover: '#00c9a7',
        primaryColorPressed: '#00c9a7',
        primaryColorSuppl: '#00c9a7'
      },
      Switch: {
        railColorActive: '#00c9a7'
      }
    };
  }
});
provide('isDark', isDark);
provide('toggleTheme', toggleTheme);
// 开关轨道样式
const railStyle = ({
  focused,
  checked
}: {
  focused: boolean;
  checked: boolean;
}) => {
  const style: CSSProperties = {};
  if (checked) {
    style.background = '#00c9a7';
    if (focused) style.boxShadow = '0 0 0 2px rgba(129, 198, 131, 0.3)';
  } else {
    style.background = '#e0e0e0';
    if (focused) style.boxShadow = '0 0 0 2px rgba(224, 224, 224, 0.3)';
  }
  return style;
};

// 提供函数给子组件使用
provide('isDark', isDark);
provide('toggleTheme', toggleTheme);
provide('railStyle', railStyle);
// 图标组件
const SunIcon = SunnyOutline;
const MoonIcon = MoonOutline;

// 庆祝动画状态
const showCelebration = ref(false);
const celebrationData = ref({
  title: '',
  heading: 'ACHIEVEMENT UNLOCKED!',
  subHeading: '恭喜！你已解锁成就'
});

const handleAchievementUnlocked = (event: CustomEvent) => {
  const achievements = event.detail;
  if (achievements && achievements.length > 0) {
    const first = achievements[0];
    celebrationData.value = {
      title: first.title || first.name || '未命名成就',
      heading: 'ACHIEVEMENT UNLOCKED!',
      subHeading: first.description || '恭喜！你已解锁成就'
    };
    showCelebration.value = true;
  }
}
const handleKeydown = (event: KeyboardEvent) => {


  // 同时按下 Alt 键和 K 键
  if (event.altKey && event.key === '˚') {
    event.preventDefault(); // 阻止浏览器默认行为（某些组合键可能触发浏览器菜单）
    store.state.showAiDrawer = !store.state.showAiDrawer;
    store.commit('setAiDrawer', store.state.showAiDrawer);
  }
}



onMounted(() => {
  window.addEventListener('achievement-unlocked', handleAchievementUnlocked as EventListener);
  window.addEventListener('keydown', handleKeydown);

});

onUnmounted(() => {
  window.removeEventListener('achievement-unlocked', handleAchievementUnlocked as EventListener);
  window.removeEventListener('keydown', handleKeydown);

  disconnect();
});
</script>

<style>
/* 全局基础样式 */
body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  margin: 0;
  padding: 0;
  transition: background-color 0.3s, color 0.3s;
}

/* 明亮主题样式 */
.light-theme {
  --bg-color: #f8f9fa;
  --text-color: #333333;
  --card-bg: #ffffff;
  --card-bg-rgb: 255, 255, 255;
  --border-color: #e0e0e0;
  --hover-color: #f5f5f5;

  background-color: var(--bg-color);
  color: var(--text-color);
}

/* 暗黑主题样式 */
.dark-theme {
  --bg-color: #121212;
  --text-color: #e0e0e0;
  --card-bg: #1e1e1e;
  --card-bg-rgb: 30, 30, 30;
  --border-color: #333333;
  --hover-color: #2a2a2a;

  background-color: var(--bg-color);
  color: var(--text-color);
}

/* 页面切换动画 */
.fade-slide-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(1.02);
}

/* 全局排版规范 */
:root {
  --font-heading: 'Inter', 'MiSans', -apple-system, sans-serif;
  --font-body: 'Inter', 'MiSans', -apple-system, sans-serif;
}

h1, h2, h3, h4, h5, h6 {
  font-family: var(--font-heading);
  font-weight: 600;
  letter-spacing: -0.02em;
}

h1 { font-size: 2.5rem; line-height: 1.2; margin-bottom: 1.5rem; }
h2 { font-size: 2rem; line-height: 1.3; margin-bottom: 1.25rem; }
h3 { font-size: 1.5rem; line-height: 1.4; margin-bottom: 1rem; }

body {
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
}

.text-caption {
  font-size: 12px;
  color: var(--text-color);
  opacity: 0.6;
}

/* 全局空状态美化 (专属 Freemix 几何风格) */
:deep(.n-empty) {
  padding: 40px 0;
  --n-icon-size: 80px !important;
}

:deep(.n-empty__icon) {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

:deep(.n-empty__icon)::before {
  content: '';
  position: absolute;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(0, 201, 167, 0.1) 0%, transparent 70%);
  animation: pulse 4s infinite ease-in-out;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.3); opacity: 0.8; }
}

/* 开关位置样式 */
.theme-switch {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  background: var(--card-bg);
  border-radius: 50%;
  padding: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
}

/* 应用布局样式 */
.app-layout {
  /* height: 100vh; */
}

/* 侧边栏样式 */
.side-navbar {
  height: 100vh;
  padding: 20px 0;
  background-color: var(--card-bg);
}

/* 顶部导航栏样式 */
.top-navbar {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  background-color: var(--card-bg);
}

/* 内容区域样式 */
.content-wrapper {
  /* padding: 10px; */
  height: calc(100vh - 64px);
  overflow-y: auto;
}

.content-wrappe-ai {
  /* padding: 10px; */
  height: 100vh;
  overflow-y: auto;
}

/* WebSocket通知样式 */
.websocket-notification {
  border-radius: 8px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  backdrop-filter: blur(10px);
}

.websocket-notification .el-notification__title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 4px;
}

.websocket-notification .el-notification__content {
  font-size: 14px;
}

/* WebSocket通知样式 */
.websocket-websocket-notification-dark {
  border-radius: 8px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  backdrop-filter: blur(10px);
}

:deep .websocket-websocket-notification-dark .el-notification__title {
  font-weight: 600;
  font-size: 15px;
  color: white !important;
  margin-bottom: 4px;
}

.websocket-websocket-notification-dark .el-notification__content {
  font-size: 14px;
}

/* 确保所有Naive UI组件使用主题变量 */
.n-card {
  background-color: var(--card-bg) !important;
  color: var(--text-color) !important;
  border-color: var(--border-color) !important;
}

.n-button {
  color: var(--text-color) !important;
}

.n-input {
  background-color: var(--card-bg) !important;
  color: var(--text-color) !important;
  border-color: var(--border-color) !important;
}

.n-tabs-tab {
  color: var(--text-color) !important;
}

/* 添加平滑过渡 */
.n-config-provider,
.n-config-provider * {
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background-color: rgba(164, 160, 170, 0.26);
  border-radius: 3px;
  transition: background-color 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background-color: rgba(129, 198, 131, 0);
}

::-webkit-scrollbar-track {
  background-color: transparent;
  border-radius: 3px;
}

::-webkit-scrollbar-corner {
  background-color: transparent;
}

/* 亮色模式下的全局滚动条样式 */
.light-theme ::-webkit-scrollbar-thumb {
  background-color: rgba(129, 198, 131, 0.3);
}

.light-theme ::-webkit-scrollbar-thumb:hover {
  background-color: rgba(129, 198, 131, 0.5);
}

.light-theme ::-webkit-scrollbar-track {
  background-color: transparent;
}

/* 全局加载模态框样式 */
.global-loading-modal {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 50px;
}

.global-loading-card {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-text {
  font-size: 16px;
  font-weight: 500;
}

body {
  overflow-y: auto;
}

/* 备案信息样式 */
.beian-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1000;
}

.beian-icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
}

.beian-link {
  color: #aaaaaa40;
  font-size: 10px;
}

.beian-link:hover {
  color: #00c9a7;
  text-decoration: underline;
}

/* 暗黑主题下的备案信息样式 */
.dark-theme .beian-link {
  color: #aaaaaa40;
  font-size: 10px;
}

.dark-theme .beian-link:hover {
  color: #00c9a7;
}

/* AI 侧边栏样式 */
.ai-sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.ai-sidebar-drawer :deep(.n-drawer-content) {
  background: var(--bg-color);
}

.ai-sidebar-drawer :deep(.n-drawer-body-content-wrapper) {
  padding: 0 !important;
}

.ai-sidebar-body {
  height: 100%;
}

.ai-sidebar-body :deep(.ai-menu-page) {
  height: 100%;
}
</style>