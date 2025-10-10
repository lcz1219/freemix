// electron/main.js
import { app, BrowserWindow } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';

// 获取 __dirname 的 ES 模块替代方案
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

function createWindow() {
  // 创建浏览器窗口
  const mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'), // 预加载脚本
      contextIsolation: true, // 启用上下文隔离（安全）
      nodeIntegration: false, // 禁用Node集成（安全）
    },
  });

  // 判断环境并加载页面
  const isDev = !app.isPackaged;
  if (isDev) {
    // 开发环境：加载Vue开发服务器
    mainWindow.loadURL('http://localhost:5173'); // Vite默认端口5173
    // 自动打开开发者工具（可选）
    mainWindow.webContents.openDevTools();
  } else {
    // 生产环境：加载打包后的静态文件
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'));
  }
}

// 应用准备就绪后创建窗口
app.whenReady().then(createWindow);

// 处理所有窗口关闭事件（在macOS上应用通常保持活动状态）
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

// macOS上当点击Dock图标且没有其他窗口时重新创建窗口
app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});