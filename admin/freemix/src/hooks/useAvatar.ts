import { ref, computed, unref, type Ref } from 'vue'
import { baseURL } from '@/utils/request'

// localStorage 中头像缓存的 key，外部也可引用，用于清除缓存
export const AVATAR_CACHE_KEY = 'freemix_avatar_cache'

// DiceBear 兜底头像
export const FALLBACK_AVATAR = 'https://api.dicebear.com/7.x/miniavs/svg?seed=user'

/**
 * 头像缓存 hook
 * 将服务端头像图片缓存为 base64 存入 localStorage，
 * 下次再访问时直接从本地读取，避免网络请求。
 *
 * @param avatarUrlPath 用户头像路径（如 /file/xxx.jpg），支持传入 ref 保持响应式
 * @returns userAvatar - 最终头像 URL（computed，自动按 缓存 > 服务端 > 兜底 优先级返回）
 * @returns refreshAvatarCache - 手动刷新缓存（拉取服务端图片转 base64）
 * @returns clearAvatarCache - 清除本地缓存
 */
export function useAvatar(avatarUrlPath?: Ref<string | undefined> | string) {
  // 先从 localStorage 读取已有的 base64 缓存
  const cachedBase64 = ref(localStorage.getItem(AVATAR_CACHE_KEY) || '')

  // 最终头像地址：缓存 > 服务端 > 兜底
  const userAvatar = computed(() => {
    // 1. 有本地 base64 缓存 → 直接使用（零网络请求）
    if (cachedBase64.value) {
      return cachedBase64.value
    }

    // 2. 有服务端头像路径 → 拼接完整 URL
    const path = unref(avatarUrlPath)
    if (path) {
      return `${baseURL()}${path}`
    }

    // 3. 都没有 → 兜底头像
    return FALLBACK_AVATAR
  })

  /**
   * 从服务端拉取头像图片，转为 base64 后存入 localStorage
   * 通常在用户头像首次加载、或头像更新后调用
   */
  const fetchAndCache = async () => {
    const path = unref(avatarUrlPath)
    if (!path) return

    try {
      const response = await fetch(`${baseURL()}${path}`)
      const blob = await response.blob()
      const reader = new FileReader()
      reader.onloadend = () => {
        const dataUrl = reader.result as string
        localStorage.setItem(AVATAR_CACHE_KEY, dataUrl)
        cachedBase64.value = dataUrl
      }
      reader.readAsDataURL(blob)
    } catch (e) {
      console.warn('头像缓存失败，不影响使用:', e)
    }
  }

  /**
   * 清除本地头像缓存
   * 当用户更换头像后调用，下次访问时会重新从服务端加载并缓存
   */
  const clearAvatarCache = () => {
    localStorage.removeItem(AVATAR_CACHE_KEY)
    cachedBase64.value = ''
  }

  return {
    userAvatar,
    fetchAndCache,
    clearAvatarCache,
  }
}
