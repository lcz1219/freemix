<template>
  <n-layout-header class="bigConent">
    <n-layout-header class="header" bordered>
      <div class="logo">
        <div class="logo-icon">
          <n-icon size="24" color="white">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
              <path
                d="M18.73,7.05l0,0c-0.25-0.63-0.86-1.04-1.55-1.04c-0.05,0-0.11,0-0.16,0.01c-0.25-0.64-0.87-1.07-1.61-1.07 c-0.1,0-0.19,0.01-0.29,0.03c-0.28-0.64-0.93-1.06-1.7-1.06c-0.85,0-1.58,0.53-1.87,1.3c-0.26-0.05-0.52-0.07-0.78-0.07 c-2.34,0-4.15,2.01-3.9,4.33c-1.56,0.51-2.72,1.95-2.72,3.66c0,1.14,0.51,2.16,1.32,2.83C4.13,15.38,4,15.68,4,16 c0,1.66,1.34,3,3,3h10c2.76,0,5-2.24,5-5c0-2.64-2.05-4.78-4.66-4.96C18.82,9.3,19.11,8.12,18.73,7.05z" />
            </svg>
          </n-icon>
        </div>
        <span class="logo-text">目标追踪者</span>
      </div>

      <nav class="nav">
        <n-button text type="primary" class="nav-link" :class="{ active: activeTab === 'dashboard' }"
          @click="goTo('/home')">
          <NIcon class="icon">
            <Desktop />
          </NIcon>
          仪表盘
        </n-button>
        <n-button text type="primary" class="nav-link" :class="{ active: activeTab === 'goalmanagement' }"
          @click="goTo('/goal-management')">
          <NIcon class="icon">
            <ClipboardSharp />
          </NIcon>
          {{ '目标管理' }}
        </n-button>
        <n-button text type="primary" class="nav-link" :class="{ active: activeTab === 'statistics' }"
          @click="goTo('/statistics')">
          <NIcon class="icon">
            <Analytics />
          </NIcon>
          统计数据
        </n-button>
        <n-button text type="primary" class="nav-link" :class="{ active: activeTab === 'goalstructure' }"
          @click="goTo('/goal-structure')">
          <NIcon class="icon">
            <Podium />
          </NIcon>
          目标结构
        </n-button>
        <!-- <n-button text type="primary" class="nav-link" :class="{ active: activeTab === 'settings' }"
          @click="goTo('/settings')">
          设置
        </n-button> -->
      </nav>

    </n-layout-header>
    <div style="width: 10%;">
     <n-dropdown animated :options="options" trigger="hover" @select="handleSelect" :loading="logoutLoading">
      <n-button><n-icon>
          <PersonCircle />
        </n-icon></n-button>
    </n-dropdown>
    <!-- </div> -->
    <div class="theme-switch" v-if="!isMobileDevice">
      <n-tooltip placement="left">
        <template #trigger>
          <n-switch v-model:value="isDark" :rail-style="railStyle" @update:value="toggleTheme">
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
 <!-- 标签页组件 (非移动端) -->
      
  </n-layout-header>
  
  <TabsView v-if="!isMobileDevice" />
</template>

<script setup>
import { ref, inject, onMounted, render, h, computed,watch } from 'vue';
import { NLayoutHeader, NIcon, NButton, NAvatar, useDialog, NSwitch, NTooltip, NPopselect, NText, NDropdown, useMessage, NInput, useLoadingBar } from 'naive-ui';
import { useRouter, useRoute } from 'vue-router';
import { SunnyOutline, IdCardSharp,MoonOutline, Settings, LogInOutline, LogIn,GitCompareOutline, PersonCircle,Podium,Analytics,ClipboardSharp,Desktop } from '@vicons/ionicons5';
import { isMobile } from '@/utils/device.js';
import { useStore } from 'vuex';
import upload from './upload.vue';
import TabsView from '@/components/TabsView.vue';
import { baseURL, isSuccess, postM } from '@/utils/request.js'

const dialog = useDialog();
const loadingBar = useLoadingBar()
const router = useRouter();
const route = useRoute();
const loading = () => {
  loadingBar.start()
}
onMounted(() => {
  console.log("初始路由:", route.path)
  loading()
  setTimeout(() => {
    loadingfinish()
  }, 1000)
})
const loadingfinish = () => {
  loadingBar.finish()
}
const props = defineProps({
  activeTab: {
    type: String,
    default: 'dashboard'
  }
});
const message = useMessage();
const selectValue = ref('');
const avatarUrl = ref(''); // 存储头像URL

// 页面加载时检查本地存储的头像
onMounted(() => {
  const saveAvatarUrl = store.state.user.avatarUrl;
  console.log('saveAvatarUrl', saveAvatarUrl);

  if (saveAvatarUrl) {
    avatarUrl.value = `${baseURL()}${saveAvatarUrl}`;
  } else {
    // 默认头像
    avatarUrl.value = 'https://api.dicebear.com/7.x/miniavs/svg?seed=3';
  }
});

const uploadAvatar = () => {
  dialog.info({
    title: "头像上传",
    style: { padding: '10px' },
    content: () => h(upload, {
      onUploadSuccess: (filename) => {
        console.log("baseURL", baseURL());

        // 更新头像URL
        avatarUrl.value = `${baseURL()}/file/${filename}`;
        store.commit('saveUser', { ...store.state.user, avatarUrl: "/file/" + filename });
        // 保存到本地存储
        // localStorage.setItem('userAvatar', avatarUrl.value);
      }
    })
  })
}

const renderCustomHeader = () => {
  return h(
    'div',
    {
      style: 'display: flex; align-items: center; padding: 8px 12px;'
    },
    [
      h(NAvatar, {
        round: true,
        style: 'margin-right: 12px;',
        src: avatarUrl.value,
        onClick: uploadAvatar
      }),
      h('div', null, [
        h('div', null, [h(NText, { depth: 2 }, { default: () => store.state.user.chinesename })]),
        h('div', { style: 'font-size: 12px;' }, {
          default: () => [
            store.state.user.fashionTitle ?
              h(
                NText,
                { depth: 3 },
                { default: () => store.state.user.fashionTitle }
              ) : h(
                NInput,
                {
                  onBlur: editFashionTitle,
                  depth: 3,
                  placeholder: '添加你的座右铭⛽️',
                  value: fashionTitle.value,       // 绑定数据
                  onUpdateValue: (newValue) => {   // 监听输入事件
                    fashionTitle.value = newValue; // 更新数据源
                  },

                },
                { default: () => '' }
              )
          ]
        })
      ])
    ]
  )
}
const fashionTitle = ref('')
const editFashionTitle = async () => {
  console.log("fashionTitle.value", fashionTitle.value);


  const res = await postM('edituserinfo', { ...store.state.user, fashionTitle: fashionTitle.value });
  if (isSuccess(res)) {
    store.commit('saveUser', { ...store.state.user, fashionTitle: fashionTitle.value });
    console.log("res", res);
  }
  message.success('保存成功')
}

const IconLogout = () => {
  return h(NButton, { onClick: logout, style: 'width:100%' }, { default: () => [h(NIcon, null, { default: () => h(LogInOutline) }), '退出登录'] })
}
const IconSetting = () => {
  return h(NButton, { onClick: () => { loading(); router.push("/settings");loadingfinish() }, style: 'width:100%' }, { default: () => [h(NIcon, null, { default: () => h(Settings) }), '个人设置'] })
}
const IconRecycle = () => {
  return h(NButton,
   { onClick: () => { loading(); router.push("/recycle");loadingfinish() }, style: 'width:100%' },
    { default: () => [h(NIcon, null, { default: () => h(GitCompareOutline) }), '回收站'] })
}
const IconLoginLog = () => {
  return h(NButton,
   { onClick: () => { loading(); router.push("/login-log");loadingfinish() }, style: 'width:100%' },
    { default: () => [h(NIcon, null, { default: () => h(IdCardSharp) }), '登录日志'] })
}
const options = [
  {
    key: 'header',
    type: 'render',
    render: renderCustomHeader
  },
  {
    key: 'header-divider',
    type: 'divider'
  },
  {
    type: 'render',
    key: 'logout',
    props: {
      type: 'error'
    },
    render: IconLogout
  },
  {
    type: 'render',
    key: ' setting',
    props: {
      type: 'error'
    },
    render: IconSetting
  },
  {
    type: 'render',
    key: 'recycle',
    props: {
      type: 'error'
    },
    render: IconRecycle
  },
  {
    type: 'render',
    key: 'recycle',
    props: {
      type: 'error'
    },
    render: IconLoginLog
  },

];

const store = useStore();
const logoutLoading = ref(false);
const handleSelect = (key) => {
  console.log(key);

  if (key === 'logout') {
    logout();
  }
}
const logout = () => {
  try {
    logoutLoading.value = true;
    store.dispatch('logout');
    message.success('已退出登录');
    router.push('/login');
  } catch (error) {
    console.log(error);

    logoutLoading.value = false;
    message.error('退出登录失败，请稍后重试');
  }

  finally {
    logoutLoading.value = false;
  }
};
// 检查是否为移动设备
const isMobileDevice = ref(isMobile());

// 注入主题变量
const isDark = inject('isDark', ref(false));
const toggleTheme = inject('toggleTheme', () => { 
  emit('toggleTheme');
});

// 图标组件
const SunIcon = SunnyOutline;
const MoonIcon = MoonOutline;

// 路由


// 开关轨道样式
const railStyle = ({ focused, checked }) => {
  const style = {};
  if (checked) {
    style.background = '#8a2be2';
    if (focused) style.boxShadow = '0 0 0 2px #d0305040';
  } else {
    style.background = '#2080f0';
    if (focused) style.boxShadow = '0 0 0 2px #2080f040';
  }
  return style;
};

const goTo = (path) => {
  // 检查用户是否已登录
  const token = localStorage.getItem('token');
  if (!token && path !== '/login' && path !== '/register') {
    router.push('/login');
    return;
  }

  router.push(path);
};
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  width: 70%;
  align-items: center;
  padding: 20px 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  width: 40%;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #8a2be2, #4b0082);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  background: linear-gradient(to right, #8a2be2, #4b0082);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  font-size: 16px;
  font-weight: 500;
  padding: 10px;
  position: relative;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #8a2be2, #4b0082);
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}
.icon{
  margin:3px
}

.bigConent {
  display: flex;
  justify-content: space-between;
  align-items: center;
  /* background-color: #1e1e1e; */
}
</style>