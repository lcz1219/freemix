// electron/preload.js
import { contextBridge, ipcRenderer } from 'electron';

// 安全地暴露API到渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制
  windowControl: (action) => ipcRenderer.send('window-control', action),
  
  // 窗口拖动
  windowDrag: () => ipcRenderer.send('window-drag'),
  
  // 创建新窗口
  createNewWindow: (options) => ipcRenderer.invoke('create-new-window', options),
  
  // 关闭窗口
  closeWindow: (winId) => ipcRenderer.send('close-window', winId),
  
  // 聚焦窗口
  focusWindow: (winId) => ipcRenderer.send('focus-window', winId),
  
  // 监听窗口拖动开始事件
  onWindowDragStart: (callback) => ipcRenderer.on('window-drag-start', callback),
  
  // 移除窗口拖动开始事件监听
  removeWindowDragStartListener: (callback) => ipcRenderer.removeListener('window-drag-start', callback)
});