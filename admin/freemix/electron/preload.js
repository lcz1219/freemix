const { contextBridge, ipcRenderer } = require('electron');

// 安全地暴露API给渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制API
  minimizeWindow: () => ipcRenderer.send('window-control', 'minimize'),
  maximizeWindow: () => ipcRenderer.send('window-control', 'maximize'),
  closeWindow: () => ipcRenderer.send('window-control', 'close'),
  
  // 窗口拖动API
  startWindowDrag: () => ipcRenderer.send('window-drag'),
  
  // 窗口大小设置API
  setWindowSize: (options) => ipcRenderer.send('set-window-size', options),
  getWindowSize: () => ipcRenderer.invoke('get-window-size'),
  
  // 新窗口API
  createNewWindow: (winId, options, pageUrl) => ipcRenderer.invoke('create-new-window', { winId, options, pageUrl }),
  
  // 窗口管理API
  closeWindowById: (winId) => ipcRenderer.send('close-window', winId),
  focusWindowById: (winId) => ipcRenderer.send('focus-window', winId),
  
  // Token管理API
  saveToken: (tokenData) => ipcRenderer.send('save-token', tokenData),
  getToken: () => ipcRenderer.invoke('get-token'),
  removeToken: () => ipcRenderer.send('remove-token')
});