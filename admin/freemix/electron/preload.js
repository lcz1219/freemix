// electron/preload.js
const { contextBridge, ipcRenderer } = require('electron');
// import { contextBridge, ipcRenderer } from 'electron';

// 为 sockjs-client 等库提供 global 对象支持
if (typeof global === 'undefined') {
  global = typeof window !== 'undefined' ? window : typeof self !== 'undefined' ? self : {};
}

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
  
  // 保存 token 到文件
  saveToken: (tokenData) => ipcRenderer.send('save-token', tokenData),
  
  // 从文件获取 token
  getToken: () => ipcRenderer.invoke('get-token'),
  
  // 从文件删除 token
  removeToken: () => ipcRenderer.send('remove-token'),
  
  // 监听窗口拖动开始事件
  onWindowDragStart: (callback) => ipcRenderer.on('window-drag-start', callback),
  
  // 移除窗口拖动开始事件监听
  removeWindowDragStartListener: (callback) => ipcRenderer.removeListener('window-drag-start', callback),
  genMsg: (msg) => ipcRenderer.send('gen-msg', msg)

});