<template>
  <!-- Renderless component: 仅用于逻辑处理和弹窗唤起 -->
</template>

<script setup>
import { onMounted, onUnmounted, h, ref } from 'vue';
import { useNotification, NButton, NTag, NSpace,NAvatar } from 'naive-ui';
import MarkdownIt from 'markdown-it';
import { getM, isSuccess, postM } from '@/utils/request';
import { useStore } from 'vuex';

const store = useStore();
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
});
const notification = useNotification();
const lasteUpdteLog = ref({});

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString();
};

const showUpdateNotification = (updateData) => {
  // 映射重要程度到 Naive UI 的类型
  const typeMap = {
    low: 'info',
    medium: 'warning',
    high: 'error'
  };

  const type = typeMap[updateData.importance] || 'info';
  
  // 映射重要程度的中文标签
  const importanceLabelMap = {
    low: '一般更新',
    medium: '重要更新',
    high: '紧急修复'
  };

  const contentHtml = updateData.content ? md.render(updateData.content) : '<p>暂无详细说明</p>';

  const n = notification.create({
    title: '系统版本更新通知', // 标题固定，版本号移入内容区展示
    content: () => h('div', { class: 'update-notification-wrapper' }, [
      // 1. 头部元数据区域：版本号、标签、时间
      h(NSpace, { align: 'center', size: 'small', style: { marginBottom: '12px' } }, {
        default: () => [
          h(NTag, { type: type, bordered: false, size: 'small' }, { default: () => `v${updateData.version}` }),
          h(NTag, { type: 'default', bordered: true, size: 'small' }, { default: () => importanceLabelMap[updateData.importance] || '更新' }),
        ]
      }),
      
      // 2. Markdown 内容区域
      h('div', {
        class: 'markdown-pretty-body custom-scrollbar',
        innerHTML: contentHtml
      }),

      // 3. 底部时间 (可选，因为 notification meta 也有，但这里可以放更详细的)
      h('div', { 
        style: { marginTop: '8px', fontSize: '12px', color: '#9ca3af', textAlign: 'right' } 
      }, `发布时间: ${formatDate(updateData.updateTime)}`)
    ]),
    avatar: () =>
      h(NAvatar, {
        size: 'small',
        round: true,
        src: '../../public/icons/icon.png'
      }),
    
    // meta: formatDate(updateData.updateTime), // 可以在这里保留，也可以在 content 里自定义
    type: type,
    duration: 0, // 保持不自动关闭
    closable: true,
    style: { width: '400px' }, // 稍微加宽一点以便阅读
    // action: () => h(
    //   NButton,
    //   {
    //     size: 'small',
    //     type: 'primary',
    //     secondary: true, // 使用次级按钮样式，不那么抢眼但依然明显
    //     onClick: () => {
    //       markAsRead(updateData.version);
    //       n.destroy();
    //     }
    //   },
    //   { default: () => '我已了解本次更新' }
    // ),
    onClose: () => {
      // Optional: Logic when closed via X
       markAsRead(updateData.version);
          n.destroy();
    }
  });
};

const checkLatestUpdate = async () => {
  try {
    const res = await getM('/api/updates/latest');
    if (isSuccess(res)) {
      const latest = res.data.data;
      
      if (!latest.readUsers.includes(store.state.user.username)) {
        lasteUpdteLog.value = latest;
        showUpdateNotification(latest);
      }
    }
  } catch (e) {
    console.error('Failed to check for updates', e);
  }
};

const markAsRead = async (version) => {
  lasteUpdteLog.value['readUsers'].push(store.state.user.username);
  const res = await postM('/api/updates/isread', lasteUpdteLog.value);
  if (isSuccess(res)) {
    // checkLatestUpdate()
  }
};

const onSystemUpdate = (event) => {
  const newUpdate = event.detail;
  if (newUpdate) {
    console.log('onSystemUpdate', newUpdate);
    showUpdateNotification(newUpdate);
  }
};

onMounted(() => {
  checkLatestUpdate();
  window.addEventListener('system-update', onSystemUpdate);
});

onUnmounted(() => {
  window.removeEventListener('system-update', onSystemUpdate);
});
</script>

<style>
/* 注意：因为 Notification 是渲染在 body 下的， scoped 样式可能无法渗透到 innerHTML 中。
   这里使用非 scoped 样式，但通过特定类名 .markdown-pretty-body 隔离影响 */

.markdown-pretty-body {
  max-height: 350px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
  color: #cdd0d5;
  padding-right: 8px; /* 给滚动条留位置 */
}

/* 深色模式适配 (可选) */
.dark .markdown-pretty-body {
  color:black;
}

/* Markdown 元素样式优化 */
.markdown-pretty-body h1,
.markdown-pretty-body h2,
.markdown-pretty-body h3 {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: 600;
  line-height: 1.25;
  color: #1f2225;
}
.markdown-pretty-body h1 { font-size: 1.3em; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }
.markdown-pretty-body h2 { font-size: 1.1em; }
.markdown-pretty-body p { margin-bottom: 1em; }
.markdown-pretty-body ul, 
.markdown-pretty-body ol { padding-left: 1.5em; margin-bottom: 1em; }
.markdown-pretty-body li { margin-bottom: 0.25em; }

.markdown-pretty-body blockquote {
  padding: 0 1em;
  color: #6a737d;
  border-left: 0.25em solid #dfe2e5;
  margin: 0 0 1em 0;
  background-color: #f9f9fa;
  padding-top: 4px;
  padding-bottom: 4px;
}

.markdown-pretty-body code {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(175, 184, 193, 0.2);
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, Liberation Mono, monospace;
}

/* 自定义滚动条样式 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.3);
}
</style>