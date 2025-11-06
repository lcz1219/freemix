// src/utils/windowManager.js
// Electron 窗口管理工具

/**
 * 创建新窗口
 * @param {string} winId - 窗口唯一标识
 * @param {Object} options - 窗口配置选项
 * @param {string} pageUrl - 窗口加载的页面路径
 * @returns {Promise} 返回窗口创建结果的Promise
 */
export async function createNewWindow(winId, options = {}, pageUrl = '/AIAssistantWindow') {
  try {
    // 检查是否在Electron环境中
    console.log('window',window);
    console.log('pageUrl',pageUrl);
    
    if (window.api) {
      const result = await window.api.createNewWindow(winId, options, pageUrl);
      return result;
    } else {
      console.warn('Electron API 不可用，可能不在Electron环境中');
      console.log('模拟窗口打开...',window.location);
      console.log('模拟窗口打开...',pageUrl);
      // 在浏览器环境中模拟窗口打开
      const url = `${window.location.origin}/#${pageUrl}`;
      window.open(url, '_blank');
      return null;
    }
  } catch (error) {
    console.error('创建窗口失败:', error);
    throw error;
  }
}

/**
 * 关闭指定窗口
 * @param {string} winId - 要关闭的窗口ID
 */
export function closeWindow(winId) {
  try {
    if (window.api && window.api.closeWindow) {
      window.api.closeWindow(winId);
    } else {
      console.warn('Electron API 不可用，可能不在Electron环境中');
      // 在浏览器环境中无操作
    }
  } catch (error) {
    console.error('关闭窗口失败:', error);
  }
}

/**
 * 聚焦指定窗口
 * @param {string} winId - 要聚焦的窗口ID
 */
export function focusWindow(winId) {
  try {
    if (window.api && window.api.focusWindow) {
      window.api.focusWindow(winId);
    } else {
      console.warn('Electron API 不可用，可能不在Electron环境中');
    }
  } catch (error) {
    console.error('聚焦窗口失败:', error);
  }
}

/**
 * 生成唯一的窗口ID
 * @returns {string} 唯一的窗口ID
 */
export function generateWindowId() {
  return `window_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
}

export default {
  createNewWindow,
  closeWindow,
  focusWindow,
  generateWindowId
};