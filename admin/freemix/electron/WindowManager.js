// windows/WindowManager.js
import { app, BrowserWindow, ipcMain,Notification } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';

// 获取 __dirname 的 ES 模块替代方案
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 可选：用于持久化窗口状态，如大小和位置
// const Store = require('electron-store');

class WindowManager {
  constructor() {
    this.windows = new Map(); // 用于存储所有窗口的引用，键为窗口ID或自定义标识
    // this.store = new Store(); // 初始化存储
  }

  // 创建新窗口的核心方法
  createWindow(id, options = {}, pageUrl) {
    // 检查窗口是否已存在，避免重复打开（单例模式）
    // 注意：如果需要允许多个相同类型的窗口，应该注释掉这个检查
    // if (this.has(id)) {
    //   const existingWindow = this.get(id);
    //   existingWindow.focus(); // 如果窗口已存在，则将其聚焦并返回
    //   return existingWindow;
    // }

    // 合并默认配置、持久化配置和传入的配置
    const defaultBounds = { width: 800, height: 600 };
    // 尝试从存储中读取上次的窗口状态
    // const storedBounds = this.store.get(`windowBounds.${id}`, defaultBounds);

    const winOptions = {
      ...defaultBounds,
      // ...storedBounds,
      show: false, // 先不显示，contents加载完毕再显示以避免白屏
      webPreferences: {
        contextIsolation: true, // 启用上下文隔离，重要安全设置
        nodeIntegration: false,  // 禁用Node集成，重要安全设置
        // preload: path.join(__dirname, './preload.js'), // 指定预加载脚本
        ...(options.webPreferences || {})
      },
      ...options // 用户传入的配置可覆盖以上默认配置
    };

    // 创建窗口实例
    const win = new BrowserWindow(winOptions);

    // 加载指定的页面
    if (pageUrl) {
      // 开发环境下可能使用开发服务器URL，生产环境使用文件路径
      const isDev = !app.isPackaged;
      if (isDev) {
        // 开发环境：加载Vue开发服务器
        win.loadURL(`http://localhost:5173${pageUrl.startsWith('/') ? pageUrl : `/${pageUrl}`}`);
      } else {
        // 生产环境：加载打包后的静态文件，并使用hash导航到指定页面
        const distPath = path.join(__dirname, '../dist/index.html');
        win.loadFile(distPath, { hash: pageUrl.startsWith('/') ? pageUrl : `/${pageUrl}` });
      }
    }

    // 窗口内容加载完成后显示窗口
    win.once('ready-to-show', () => {
      win.show();
    });

    // 窗口关闭事件，清理引用
    win.on('closed', () => {
      this.windows.delete(id);
      // 可选的：保存窗口状态，例如是否最大化
      // this.store.set(`windowState.${id}`, { isMaximized: false });
    });

    // 监听窗口大小和位置变化，用于持久化（可节流）
    // win.on('resize', () => this.saveWindowBounds(id, win));
    // win.on('move', () => this.saveWindowBounds(id, win));

    // 将窗口存入Map
    this.windows.set(id, win);
    return win;
  }

  // 检查窗口是否存在且未被销毁
  has(id) {
    const win = this.windows.get(id);
    return win && !win.isDestroyed();
  }

  // 获取指定窗口
  get(id) {
    return this.has(id) ? this.windows.get(id) : null;
  }

  // 聚焦指定窗口
  focus(id) {
    const win = this.get(id);
    if (win) {
      if (win.isMinimized()) win.restore();
      win.focus();
    }
    return win;
  }

  // 关闭指定窗口
  close(id) {
    const win = this.get(id);
    if (win && !win.isDestroyed()) {
      win.close();
    }
  }

  // 关闭所有窗口
  closeAll() {
    this.windows.forEach((win, id) => {
      if (!win.isDestroyed()) {
        win.close();
      }
    });
    this.windows.clear();
  }

  // 获取所有窗口
  getAll() {
    return Array.from(this.windows.entries()).map(([id, win]) => ({
      id,
      win
    }));
  }

  // 获取窗口数量
  getCount() {
    return this.windows.size;
  }

  // 保存窗口边界（示例方法）
  // saveWindowBounds(id, win) {
  //   if (!win.isMaximized() && !win.isMinimized()) {
  //     this.store.set(`windowBounds.${id}`, win.getBounds());
  //   }
  //   this.store.set(`windowState.${id}`, { isMaximized: win.isMaximized() });
  // }
}

export default WindowManager;