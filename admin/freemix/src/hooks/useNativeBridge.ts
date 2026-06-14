/**
 * useNativeBridge.ts
 * 桥接 iOS 原生功能：快捷操作 + 通知操作 + AI 晨报
 *
 * 完全通过原生 evaluateJavaScript + window 事件 + WKScriptMessageHandler 通信
 * - 快捷操作：原生 → dispatchEvent('quick-action') → Vue router.push
 * - 通知操作：原生 → dispatchEvent('notification-action') → Vue 调 API / 跳转
 * - 通知调度：Vue → window.freemixGoalsData → 原生进后台时读取
 * - AI 晨报：原生 8:00 通知 → dispatchEvent('ai-morning') → Vue 调 AI → 回传原生通知
 */
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showLoadingToast, showSuccessToast, closeToast } from 'vant'
import { postM, isSuccess } from '@/utils/request'
import { callCozeAPI } from '@/utils/aiService.js'

// ============================================================
// 快捷操作事件监听（热启动 + 冷启动均走事件）
// ============================================================

export function useQuickActions() {
  const router = useRouter()

  function handleQuickAction(event: Event) {
    const detail = (event as CustomEvent).detail as { route: string; type: string }
    if (detail?.route) {
      const currentPath = router.currentRoute.value.path
      if (currentPath === detail.route) return
      router.push(detail.route)
    }
  }

  onMounted(() => {
    window.addEventListener('quick-action', handleQuickAction)
  })

  onUnmounted(() => {
    window.removeEventListener('quick-action', handleQuickAction)
  })
}

// ============================================================
// 通知操作事件监听
// ============================================================

export function useNotificationActions() {
  const router = useRouter()

  async function handleNotificationAction(event: Event) {
    const detail = (event as CustomEvent).detail as {
      goalId?: string
      goalTitle?: string
      action?: string
      route?: string
    }

    if (!detail) return

    switch (detail.action) {
      case 'complete':
        if (detail.goalId) {
          try {
            const res = await postM('finishGoal', { goalId: detail.goalId })
            if (isSuccess(res)) {
              console.log('✅ 目标已完成:', detail.goalTitle)
              router.push('/home')
            }
          } catch (e) {
            console.error('完成目标失败:', e)
          }
        }
        break

      case 'view':
        router.push('/home')
        break

      case 'navigate':
        if (detail.route) router.push(detail.route)
        break
    }
  }

  onMounted(() => {
    window.addEventListener('notification-action', handleNotificationAction)
  })

  onUnmounted(() => {
    window.removeEventListener('notification-action', handleNotificationAction)
  })
}

// ============================================================
// 通知调度：将目标数据暴露到 window，原生端进后台时读取
// ============================================================

/**
 * 更新 window.freemixGoalsData，AppDelegate 进后台时会 evaluateJavaScript 读取
 * 包含通知设置字段 notifyEnabled / notifyTime
 * @param goals 目标数组
 */
export function updateGoalsData(goals: any[]) {
  ;(window as any).freemixGoalsData = goals.map((g: any) => ({
    _id: g._id,
    title: g.title,
    status: g.status,
    notifyEnabled: g.notifyEnabled ?? false,
    notifyTime: g.notifyTime ?? '09:00',
    deadline: typeof g.deadline === 'string'
      ? new Date(g.deadline).getTime()
      : g.deadline
  }))
}



// ============================================================
// 初始化
// ============================================================

export function initNativeBridge() {
  useQuickActions()
  useNotificationActions()
}
