// electron/preload.js
import { contextBridge, ipcRenderer } from 'electron';

// 通过 contextBridge 安全地将API暴露给渲染进程（Vue组件）
contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制功能
  minimizeWindow: () => ipcRenderer.send('window-control', 'minimize'),
  maximizeWindow: () => ipcRenderer.send('window-control', 'maximize'),
  closeWindow: () => ipcRenderer.send('window-control', 'close'),
  
  // 窗口拖动功能
  startDrag: () => ipcRenderer.send('window-drag', 'start'),
  
  // 其他功能
  sendMessage: (message) => ipcRenderer.send('message-to-main', message),
  onReply: (callback) => ipcRenderer.on('reply-from-main', callback)
});