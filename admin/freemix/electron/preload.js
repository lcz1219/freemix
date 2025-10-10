// electron/preload.js
import { contextBridge, ipcRenderer } from 'electron';

// 通过 contextBridge 安全地将API暴露给渲染进程（Vue组件）
contextBridge.exposeInMainWorld('electronAPI', {
  // 这里可以暴露一些方法，例如：
  sendMessage: (message) => ipcRenderer.send('message-to-main', message),
  onReply: (callback) => ipcRenderer.on('reply-from-main', callback)
});