import { getM, isSuccess } from '@/utils/request'
import { genMsg } from '@/utils/genMsg'

// 全局消息监听器
class GlobalMessageListener {
  constructor() {
    this.pollingInterval = null
    this.pollingIntervalTime = 2000 // 5秒轮询一次
    this.lastMessageIds = new Set() // 记录已处理的消息ID，避免重复提示
    this.isEnabled = true
  }

  // 开始监听消息
  startListening() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval)
    }

    this.pollingInterval = setInterval(async () => {
      if (this.isEnabled) {
        await this.checkNewMessages()
      }
    }, this.pollingIntervalTime)
  }

  // 停止监听消息
  stopListening() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval)
      this.pollingInterval = null
    }
  }

  // 检查新消息
  async checkNewMessages() {
    try {
      // 获取未读消息
      const res = await getM('messages/unreadMessages')
      
      if (isSuccess(res) && res.data.data && res.data.data.length > 0) {
        const newMessages = res.data.data
        
        // 过滤出未处理过的消息
        const unprocessedMessages = newMessages.filter(msg => !this.lastMessageIds.has(msg.id))
        
        // 处理新消息
        for (const msg of unprocessedMessages) {
          // 添加到已处理集合
          this.lastMessageIds.add(msg.id)
          
          // 生成消息提示
          const senderName = msg.fromUserChinesename || msg.fromUser
          genMsg(`${senderName}: ${msg.content}`)
        }
        
        // 限制已处理消息集合大小，避免内存泄漏
        if (this.lastMessageIds.size > 1000) {
          // 保留最新的500个ID
          const idsArray = Array.from(this.lastMessageIds).slice(-500)
          this.lastMessageIds = new Set(idsArray)
        }
      }
    } catch (error) {
      console.error('检查新消息失败:', error)
    }
  }

  // 启用/禁用监听
  toggleListening(enabled) {
    this.isEnabled = enabled
  }
}

// 创建全局实例
const globalMessageListener = new GlobalMessageListener()

export default globalMessageListener