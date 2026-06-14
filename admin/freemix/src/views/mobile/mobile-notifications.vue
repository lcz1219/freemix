<template>
  <van-config-provider theme="dark">
    <div class="notification-container dark-mode">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        fixed
        placeholder
        class="glass-nav"
        :border="false"
        z-index="100"
        :safe-area-inset-top="true"
      >
        <template #left>
          <div class="nav-back" @click="goBack">
            <van-icon name="arrow-left" />
            <span>返回</span>
          </div>
        </template>
        <template #title>
          <span class="nav-title">通知消息</span>
        </template>
        <template #right>
          <div class="nav-actions">
            <van-icon name="delete-o" size="20" @click="clearAll" v-if="notifications.length > 0" />
          </div>
        </template>
      </van-nav-bar>

      <div class="content-wrapper">
        <!-- 全部已读按钮 -->
        <div class="action-bar" v-if="unreadCount > 0">
          <van-button round size="small" plain type="primary" @click="markAllRead">
            <template #icon>
              <van-icon name="checked" />
            </template>
            全部标为已读 ({{ unreadCount }})
          </van-button>
        </div>

        <!-- 通知列表 -->
        <div class="notification-list" v-if="notifications.length > 0">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-card"
            :class="{ unread: !item.read }"
            @click="handleClick(item)"
          >
            <!-- 左侧图标 -->
            <div class="notif-icon" :class="getTypeClass(item.type)">
              <van-icon :name="getTypeIcon(item.type)" size="20" />
            </div>
            <!-- 内容区 -->
            <div class="notif-content">
              <div class="notif-header">
                <span class="notif-title">{{ item.title }}</span>
                <span class="notif-time">{{ formatTime(item.createdAt) }}</span>
              </div>
              <div class="notif-body">{{ item.body }}</div>
              <!-- 未读圆点 -->
              <div class="unread-dot" v-if="!item.read"></div>
            </div>
            <!-- 操作 -->
            <div class="notif-actions" v-if="!item.read">
              <van-icon name="ellipsis" size="16" @click.stop="markRead(item)" />
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else>
          <div class="empty-icon"><van-icon name="bell-o" size="48" /></div>
          <p class="empty-title">暂无通知</p>
          <p class="empty-desc">系统通知消息会在这里展示</p>
        </div>
      </div>

      <!-- 通知详情弹窗 (毛玻璃风格，与AI晨报弹窗一致) -->
      <van-dialog
        v-model:show="showDetailDialog"
        class="notification-detail-dialog"
        :show-confirm-button="false"
      >
        <div class="glass-dialog-inner" v-if="selectedNotification">
          <div class="glass-dialog-header">
            <div class="header-icon" :class="getTypeClass(selectedNotification.type)">
              <van-icon :name="getTypeIcon(selectedNotification.type)" size="24" />
            </div>
            <div class="header-title">{{ selectedNotification.title }}</div>
            <div class="header-time">{{ formatFullTime(selectedNotification.createdAt) }}</div>
          </div>
          <div class="dialog-body">
            {{ selectedNotification.body }}
          </div>
          <div class="dialog-meta" v-if="selectedNotification.goalTitle">
            <van-icon name="flag-o" size="14" color="rgba(255,255,255,0.3)" />
            <span>相关目标：{{ selectedNotification.goalTitle }}</span>
          </div>
          <div class="glass-dialog-footer">
            <van-button class="glass-confirm-btn" round block @click="showDetailDialog = false">
              我知道了
            </van-button>
          </div>
        </div>
      </van-dialog>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { postM, isSuccess } from '@/utils/request'

const router = useRouter()
const notifications = ref<any[]>([])
const unreadCount = ref(0)

// 通知详情弹窗
const showDetailDialog = ref(false)
const selectedNotification = ref<any>(null)

const goBack = () => router.back()

// 获取通知列表
const fetchNotifications = async () => {
  const res = await postM('getNotifications')
  if (isSuccess(res)) {
    notifications.value = res.data.data || []
    unreadCount.value = notifications.value.filter((n: any) => !n.read).length
  }
}

// 获取未读数
const fetchUnreadCount = async () => {
  const res = await postM('getUnreadNotificationCount')
  if (isSuccess(res)) {
    unreadCount.value = res.data.data || 0
  }
}

// 标记单条已读
const markRead = async (item: any) => {
  if (item.read) return
  await postM('markNotificationRead', { id: item.id })
  item.read = true
  unreadCount.value = Math.max(0, unreadCount.value - 1)
}

// 全部已读
const markAllRead = async () => {
  await postM('markAllNotificationsRead')
  notifications.value.forEach((n: any) => n.read = true)
  unreadCount.value = 0
  showToast('已全部标为已读')
}

// 清空所有
const clearAll = () => {
  showConfirmDialog({
    title: '清空通知',
    message: '确认清空所有通知消息吗？',
    confirmButtonColor: '#00c9a7',
  }).then(async () => {
    await postM('clearNotifications')
    notifications.value = []
    unreadCount.value = 0
    showToast('已清空')
  }).catch(() => {})
}

// 点击通知
const handleClick = async (item: any) => {
  // 选中当前通知供弹窗展示
  selectedNotification.value = item
  showDetailDialog.value = true
  
  // 如果未读，先标记已读
  if (!item.read) {
    await postM('markNotificationRead', { id: item.id })
    item.read = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

// 通知类型图标
const getTypeIcon = (type: string) => {
  switch (type) {
    case 'goal_reminder': return 'clock-o'
    case 'ai_morning': return 'fire-o'
    case 'daily_summary': return 'notes-o'
    default: return 'info-o'
  }
}

// 通知类型样式
const getTypeClass = (type: string) => {
  switch (type) {
    case 'goal_reminder': return 'type-reminder'
    case 'ai_morning': return 'type-morning'
    case 'daily_summary': return 'type-daily'
    default: return 'type-system'
  }
}

// 格式化时间
const formatTime = (timestamp: number) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  if (date.getFullYear() === now.getFullYear()) return month + '/' + day
  return date.getFullYear() + '/' + month + '/' + day
}

// 完整时间格式（用于弹窗内展示）
const formatFullTime = (timestamp: number) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hour = date.getHours().toString().padStart(2, '0')
  const min = date.getMinutes().toString().padStart(2, '0')
  return `${year}年${month}月${day}日 ${hour}:${min}`
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
/* 暗黑容器 */
.notification-container {
  min-height: 100vh;
  background: linear-gradient(160deg, #0a0a0f 0%, #111118 30%, #0e0e1a 70%, #0a0a0f 100%);
  color: #e8e8f0;
}

.content-wrapper {
  padding: 12px 16px 40px;
}

/* 导航栏 */
.glass-nav {
  background: rgba(30, 30, 35, 0.7);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
}

.nav-back {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #00c9a7;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
}

.nav-actions {
  color: rgba(255, 255, 255, 0.5);
}

/* 操作栏 */
.action-bar {
  padding: 8px 0;
  display: flex;
  justify-content: flex-end;
}

/* 通知卡片 */
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notification-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(30, 30, 35, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 16px;
  backdrop-filter: blur(12px);
  transition: all 0.3s ease;
  position: relative;
}

.notification-card:active {
  transform: scale(0.98);
  background: rgba(30, 30, 35, 0.7);
}

.notification-card.unread {
  border-color: rgba(0, 201, 167, 0.15);
  background: rgba(0, 201, 167, 0.04);
}

/* 左侧图标 */
.notif-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notif-icon.type-reminder {
  background: rgba(255, 107, 107, 0.15);
  color: #ff6b6b;
}

.notif-icon.type-morning {
  background: rgba(255, 193, 7, 0.15);
  color: #ffc107;
}

.notif-icon.type-daily {
  background: rgba(0, 201, 167, 0.15);
  color: #00c9a7;
}

.notif-icon.type-system {
  background: rgba(99, 102, 241, 0.15);
  color: #818cf8;
}

/* 内容区 */
.notif-content {
  flex: 1;
  min-width: 0;
  position: relative;
}

.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.notif-title {
  font-size: 14px;
  font-weight: 600;
  color: #f0f0f0;
}

.notif-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.35);
  flex-shrink: 0;
  margin-left: 8px;
}

.notif-body {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 未读圆点 */
.unread-dot {
  position: absolute;
  top: 2px;
  right: -4px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #00c9a7;
  box-shadow: 0 0 6px rgba(0, 201, 167, 0.5);
}

/* 操作 */
.notif-actions {
  padding-top: 4px;
  color: rgba(255, 255, 255, 0.25);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(30, 30, 35, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.2);
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.4);
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.2);
}

/* ==========================================
 * 通知详情弹窗 (毛玻璃风格)
 * ========================================== */
:deep(.notification-detail-dialog) {
  background: transparent !important;
  width: 320px;
  border-radius: 24px;
  overflow: hidden;

  .van-dialog__header {
    display: none;
  }
  
  .van-dialog__content {
    background: transparent;
  }
}

.glass-dialog-inner {
  background: rgba(25, 25, 30, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  display: flex;
  flex-direction: column;
}

.glass-dialog-header {
  padding: 24px 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;

  .header-icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }

  .header-icon.type-reminder {
    background: rgba(255, 107, 107, 0.15);
    color: #ff6b6b;
    box-shadow: 0 0 20px rgba(255, 107, 107, 0.2);
  }

  .header-icon.type-morning {
    background: rgba(255, 193, 7, 0.15);
    color: #ffc107;
    box-shadow: 0 0 20px rgba(255, 193, 7, 0.2);
  }

  .header-icon.type-daily {
    background: rgba(0, 201, 167, 0.15);
    color: #00c9a7;
    box-shadow: 0 0 20px rgba(0, 201, 167, 0.2);
  }

  .header-icon.type-system {
    background: rgba(99, 102, 241, 0.15);
    color: #818cf8;
    box-shadow: 0 0 20px rgba(99, 102, 241, 0.2);
  }

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
    letter-spacing: 0.5px;
    text-align: center;
  }

  .header-time {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.3);
  }
}

/* 弹窗正文 */
.dialog-body {
  padding: 0 20px 16px;
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.85);
  text-align: left;
  word-break: break-word;
}

/* 关联目标信息 */
.dialog-meta {
  padding: 0 20px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
}

.glass-dialog-footer {
  padding: 10px 20px 24px;

  .glass-confirm-btn {
    background: linear-gradient(135deg, #00c9a7 0%, #00a88b 100%);
    border: none;
    color: #fff;
    font-weight: 600;
    font-size: 16px;
    height: 44px;
    box-shadow: 0 4px 15px rgba(0, 201, 167, 0.3);
    
    &:active {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }
}
</style>
