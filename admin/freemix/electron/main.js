// electron/main.js
import { app, BrowserWindow, ipcMain, Notification, Menu, Tray, nativeImage, screen } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import fs from 'fs';
import WindowManager from './WindowManager.js';
// 获取 __dirname 的 ES 模块替代方案
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 获取应用数据目录
const userDataPath = app.getPath('userData');
const tokenFilePath = path.join(userDataPath, 'token.json');
const windowConfigPath = path.join(userDataPath, 'window-config.json');

// 窗口配置管理函数
function getWindowConfig() {
  try {
    if (fs.existsSync(windowConfigPath)) {
      const data = fs.readFileSync(windowConfigPath, 'utf8');
      return JSON.parse(data);
    }
  } catch (error) {
    console.error('读取窗口配置失败:', error);
  }
  // 默认配置
  return null;
}

function saveWindowConfig(config) {
  try {
    fs.writeFileSync(windowConfigPath, JSON.stringify(config, null, 2));
    console.log('窗口配置已保存:', config);
  } catch (error) {
    console.error('保存窗口配置失败:', error);
  }
}

// 全局窗口管理器实例
let windowManager;
// 在文件顶部，其他全局变量附近添加
let tray = null; // 全局保存 Tray 实例的引用

// 创建窗口函数
function createWindow() {
  // 获取保存的窗口配置
  const windowConfig = getWindowConfig();
  
  // 获取屏幕尺寸以设置默认窗口大小
  const { width: screenWidth, height: screenHeight } = screen.getPrimaryDisplay().workAreaSize;
  
  // 默认窗口配置
  const defaultWindowConfig = {
    width: 900,
    height: 700,
    isMaximized: false
  };
  
  // 合并配置
  const config = windowConfig || defaultWindowConfig;
  
  // 创建浏览器窗口
  const mainWindow = new BrowserWindow({
    width: config.width,
    height: config.height,
    titleBarStyle: 'hiddenInset', // macOS: 隐藏标题栏但保留窗口控制按钮
    frame: process.platform === 'darwin' ? false : true, // macOS: 无边框，其他平台保留边框
    title: '', // 清空窗口标题
    // 增加窗口拖动区域设置
    transparent: false, // 确保窗口不透明
    thickFrame: true, // 增加窗口边框厚度
   
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'), // 预加载脚本
      // nodeIntegration: true,
      contextIsolation: true, // 启用上下文隔离（安全）
      nodeIntegration: false, // 禁用Node集成（安全）
    },
  });

  // 如果配置中保存了最大化状态，则最大化窗口
  if (config.isMaximized) {
    mainWindow.maximize();
  }

  // 监听窗口关闭事件，保存窗口配置
  mainWindow.on('close', () => {
    const bounds = mainWindow.getBounds();
    const isMaximized = mainWindow.isMaximized();
    
    const configToSave = {
      width: bounds.width,
      height: bounds.height,
      x: bounds.x,
      y: bounds.y,
      isMaximized: isMaximized
    };
    
    saveWindowConfig(configToSave);
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

  return mainWindow;
}

// 显示通知
function showNotification(title, body) {
  console.log('显示通知:', title, body);
  new Notification({ title, body }).show()
}


ipcMain.on("gen-msg",(event,msg)=>{
  showNotification('Freemix', msg);
})

// 创建系统托盘图标
 // 在文件顶部声明全局变量

function createTrayIcon() {
  try {
    console.log('开始创建系统托盘图标...');
    
    // 根据平台选择合适的图标尺寸
    const iconSize = process.platform === 'darwin' ? { width: 22, height: 22 } : { width: 20, height: 20 };
    
    // 尝试从文件创建图标
    let icon;
    try {
      const iconPath = path.join(__dirname, 'icon.png');
      icon = nativeImage.createFromPath(iconPath);
      
      // 检查图标是否有效
      if (icon.isEmpty()) {
        throw new Error('图标文件为空或无效');
      }
      
      console.log('使用文件图标:', iconPath);
    } catch (iconError) {
      console.log('无法加载文件图标，使用默认base64图标:', iconError.message);
      // 使用内嵌base64图标作为备选方案
      // icon = nativeImage.createFromDataURL('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAABIUlEQVRYhe2WQQ6CMBBF/9SExIUbD+DJXHkAJS48gB6AG3gz8QIKuPAEehdwoe2k0E4hYJmZdpN/0zG0/dNpOwVijDHG/A0A9wDeAO5f6hXAPQaQpQBwAVD9qCsApwDgFMAVQN2jrgGc/YJTAHcATY+6AXDxCc4AtD3qFsDNF7gA0PWoOwAPX+ASQN+j7gE8fYArAIlB/QKQ+gDXAJaG+AfAygDfAFga4hvA2hDfADaG+AawNcQ3gJ0hvgHsDfEN4GCITwBHQ3wBOBviC8DFEF8Arob4APAxxAeAryE+AHwO8QHge4gPAN9DfADEMCQGQCyGDAWIyZAhADEa0hcgVkP6AMRsyC+AXAz5BJCTIe8AcjPkFSBnQxqA3A15AOT+DXkYYowxxvwxT4W5p6ZcXf5lAAAAAElFTkSuQmCC');
    }
    
    // 调整图标尺寸
    icon = icon.resize(iconSize);
    console.log('图标创建成功，尺寸:', icon.getSize());
    
    // 创建托盘实例并保存到全局变量
    tray = new Tray(icon);
    console.log('Tray实例创建成功');

    // 创建右键菜单
    const contextMenu = Menu.buildFromTemplate([
      { 
        label: '显示主窗口', 
        click: () => {
          const mainWindow = BrowserWindow.getAllWindows()[0];
          if (mainWindow) {
            mainWindow.show();
            mainWindow.focus();
          }
        }
      },
      // { 
      //   label: '窗口设置', 
      //   click: () => {
      //     const mainWindow = BrowserWindow.getAllWindows()[0];
      //     if (mainWindow) {
      //       // 创建窗口大小设置窗口
      //       createWindowSizeSettingsWindow(mainWindow);
      //     }
      //   }
      // },
      { type: 'separator' },
      { 
        label: '退出', 
        click: () => {
          app.quit();
        }
      }
    ]);

    // 设置托盘悬停提示
    tray.setToolTip('FreeMix - 目标跨端协作平台');
    
    // 在macOS上设置标题（可选）
    if (process.platform === 'darwin') {
      // tray.setTitle('FreeMix');
    }

    // 绑定右键菜单
    tray.setContextMenu(contextMenu);
    
    // 添加点击事件（显示/隐藏窗口）
    tray.on('click', () => {
      const mainWindow = BrowserWindow.getAllWindows()[0];
      if (mainWindow) {
      
          mainWindow.show();
          mainWindow.focus();
        
      }
    });
    
    console.log('系统托盘图标创建完成');
  } catch (error) {
    console.error('创建系统托盘图标时发生错误:', error);
  }
}// 全局保存 Tray 实例的引用


// 显示关于对话框
function showAboutDialog() {
  const aboutWindow = new BrowserWindow({
    width: 400,
    height: 500,
    resizable: false,
    minimizable: false,
    maximizable: false,
    fullscreenable: false,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true
    }
  });
    // 创建HTML文件路径
  const htmlFilePath = path.join(__dirname, 'about.html');

  // 创建关于对话框的HTML内容
  const aboutHtml = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>关于 FreeMix</title>
      <style>
        body {
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
          text-align: center;
          padding: 20px;
          background-color:rgba(11, 11, 12, 0.89);
          margin: 0;
        }
        .logo {
          width: 80px;
          height: 80px;
          margin: 20px auto;
          background-color: #81c683;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 36px;
          font-weight: bold;
        }
        h1 {
          color: white;
          margin: 10px 0;
        }
        .version {
          color: white;
          margin-bottom: 20px;
        }
        .description {
          color: white;
          line-height: 1.5;
          margin-bottom: 20px;
        }
        .close-btn {
          background-color: #81c683;
          color: white;
          border: none;
          padding: 10px 20px;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
        }
        .close-btn:hover {
          background-color: #6ab06d;
        }
      </style>
    </head>
    <body>
      <img src="./icon.png" alt="Freemix" width="48" height="48">
      <h1>FreeMix</h1>
      <div class="version">版本 ${app.getVersion() || '1.0.0'}</div>
      <div class="description">
        <p>FreeMix 是一个创新的目标跨端协作平台</p>
        <p>帮助团队更好地管理目标和沟通</p>
      </div>

    </body>
    </html>
  `;
  fs.writeFileSync(htmlFilePath, aboutHtml);
  
  // 加载HTML文件
  aboutWindow.loadFile(htmlFilePath);
  
  aboutWindow.on('closed', () => {
    // 清理临时文件
    try {
      fs.unlinkSync(htmlFilePath);
    } catch (error) {
      console.error('删除临时文件失败:', error);
    }
  });

}

//   aboutWindow.loadURL(`data:text/html;charset=UTF-8,${encodeURIComponent(aboutHtml)}`);
  
//   // 窗口关闭时清理
//   aboutWindow.on('closed', () => {
//     // 清理引用
//   });
// }

// 创建窗口大小设置窗口
function createWindowSizeSettingsWindow(mainWindow) {
  const settingsWindow = new BrowserWindow({
    width: 450,
    height: 550,
    resizable: false,
    minimizable: false,
    maximizable: false,
    fullscreenable: false,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js') // 添加preload脚本
    }
  });

  // 获取当前窗口大小
  const currentBounds = mainWindow.getBounds();
  // 创建HTML文件路径
  const htmlFilePath = path.join(__dirname, 'window-settings.html');
  
  // 创建窗口设置的HTML内容
  const settingsHtml = `
   <!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>窗口设置</title>
  <style>
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji'; /* GitHub标准字体栈 */
    }
    
    body {
      background-color: #0d1117; /* GitHub深色主题背景色 */
      color: #e6edf3; /* 浅色文本 */
      line-height: 1.5;
      padding: 20px;
      min-height: 100vh;
    }
    
    .container {
      max-width: 320px; /* 稍微缩窄以符合GitHub紧凑风格 */
      margin: 0 auto;
      background-color: #161b22; /* GitHub卡片背景色 */
      border: 1px solid #21262d; /* 细微边框 */
      border-radius: 6px; /* GitHub标准圆角 */
      padding: 24px; /* 内边距调整 */
      box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.1); /* 更克制的阴影 */
    }
    
    .logo {
      text-align: center;
      margin-bottom: 24px;
    }
    
    .logo-circle {
      width: 48px; /* 缩小尺寸 */
      height: 48px;
      background-color: #238636; /* GitHub绿色 */
      border-radius: 50%;
      margin: 0 auto 12px; /* 调整间距 */
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 18px; /* 字体大小调整 */
      font-weight: 600;
      border: 1px solid #2ea043; /* 绿色边框 */
    }
    
    h1 {
      text-align: center;
      color: #e6edf3; /* 浅色标题 */
      margin-bottom: 16px;
      font-size: 20px; /* 调整字体大小 */
      font-weight: 600;
    }
    
    .form-group {
      margin-bottom: 16px;
    }
    
    label {
      display: block;
      margin-bottom: 6px; /* 减小间距 */
      font-weight: 500;
      color: #7d8590; /* GitHub标签灰色 */
      font-size: 14px;
    }
    
    input {
      width: 100%;
      padding: 8px 12px; /* 调整内边距 */
      background-color: #0d1117; /* 深色输入背景 */
      border: 1px solid #21262d;
      border-radius: 6px;
      font-size: 14px;
      color: #e6edf3;
      transition: border-color 0.2s ease; /* 更快的过渡 */
    }
    
    input:focus {
      outline: none;
      border-color: #1f6feb; /* GitHub蓝色 */
      box-shadow: 0 0 0 3px rgba(31, 111, 235, 0.15); /* GitHub风格聚焦阴影 */
      background-color: #0d1117;
    }
    
    .button-group {
      display: flex;
      gap: 8px; /* 减小间距 */
      margin-top: 20px; /* 调整顶部间距 */
    }
    
    button {
      flex: 1;
      padding: 8px 12px; /* 调整内边距 */
      border: 1px solid;
      border-radius: 6px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease; /* 更快的过渡 */
    }
    
    button:hover {
      transform: none; /* 移除上移效果 */
      box-shadow: none; /* 移除阴影变化 */
    }
    
    .btn-cancel {
      background-color: #161b22;
      border-color: #30363d;
      color: #e6edf3;
    }
    
    .btn-cancel:hover {
      background-color: #30363d;
      border-color: #7d8590;
    }
    
    .btn-save {
      background-color: #238636; /* GitHub绿色 */
      border-color: #2ea043;
      color: white;
    }
    
    .btn-save:hover {
      background-color: #2ea043;
      border-color: #3fb950;
    }
    
    .footer {
      text-align: center;
      margin-top: 20px;
      color: #7d8590;
      font-size: 12px;
      border-top: 1px solid #21262d;
      padding-top: 16px;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="logo">
      <img src="./icon.png" alt="Freemix" width="48" height="48">
      <h1>窗口设置</h1>
    </div>
    
    <div class="form-group">
      <label for="width">宽度 (像素)</label>
      <input type="number" id="width" value="${currentBounds.width}" min="800" max="2000" placeholder="输入窗口宽度">
    </div>
    
    <div class="form-group">
      <label for="height">高度 (像素)</label>
      <input type="number" id="height" value="${currentBounds.height}" min="600" max="1500" placeholder="输入窗口高度">
    </div>
    
    <div class="button-group">
      <button class="btn-cancel" onclick="window.close()">取消</button>
      <button class="btn-save" onclick="saveSettings()">保存</button>
    </div>
    
    <div class="footer">
      <p>调整窗口大小以获得最佳体验</p>
    </div>
  </div>
  
  <script>
    // JavaScript逻辑保持不变
    function saveSettings() {
      const width = document.getElementById('width').value;
      const height = document.getElementById('height').value;
      
      if (!width || !height) {
        alert('请输入有效的宽度和高度值');
        return;
      }
      
      if (width < 800 || height < 600) {
        alert('窗口尺寸不能小于 800x600');
        return;
      }
      
      // 发送IPC消息到主进程
      window.api.setWindowSize({
        width: parseInt(width),
        height: parseInt(height),
        saveConfig: true
      });
      
      window.close();
    }
    
    document.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        saveSettings();
      }
    });
  </script>
</body>
</html>
  `;

  fs.writeFileSync(htmlFilePath, settingsHtml);
  
  // 加载HTML文件
  settingsWindow.loadFile(htmlFilePath);
  
  settingsWindow.on('closed', () => {
    // 清理临时文件
    try {
      fs.unlinkSync(htmlFilePath);
    } catch (error) {
      console.error('删除临时文件失败:', error);
    }
  });

}

// 创建Dock菜单
// function createDockMenu() {
//   if (process.platform !== 'darwin') return;
  
//   const dockMenu = Menu.buildFromTemplate([
//     {
//       label: '新建窗口',
//       click() {
//         const winId = `window_${Date.now()}`;
//         windowManager.createWindow(winId, {
//           width: 1200,
//           height: 800
//         }, '/');
//       }
//     },
    
   
//   ]);
  
//   app.dock.setMenu(dockMenu);
// }

// 创建应用菜单
function createAppMenu() {
  const template = [
    {
      label: '应用',
      submenu: [
        // {
        //   label: '新建窗口',
        //   accelerator: 'CmdOrCtrl+N',
        //   click() {
        //     const winId = `window_${Date.now()}`;
        //     windowManager.createWindow(winId, {
        //       width: 1200,
        //       height: 800
        //     }, '/');
        //   }
        // },
        {
          label: '关闭窗口',
          accelerator: 'CmdOrCtrl+W',
          click() {
            const focusedWindow = BrowserWindow.getFocusedWindow();
            if (focusedWindow) {
              focusedWindow.close();
            }
          }
        },
        { type: 'separator' },
        {
          label: '退出',
          accelerator: 'CmdOrCtrl+Q',
          click() {
            app.quit();
          }
        }
      ]
    },
    {
      label: '编辑',
      submenu: [
        { label: '撤销', accelerator: 'CmdOrCtrl+Z', selector: 'undo:' },
        { label: '重做', accelerator: 'Shift+CmdOrCtrl+Z', selector: 'redo:' },
        { type: 'separator' },
        { label: '剪切', accelerator: 'CmdOrCtrl+X', selector: 'cut:' },
        { label: '复制', accelerator: 'CmdOrCtrl+C', selector: 'copy:' },
        { label: '粘贴', accelerator: 'CmdOrCtrl+V', selector: 'paste:' },
        { label: '全选', accelerator: 'CmdOrCtrl+A', selector: 'selectAll:' }
      ]
    },
    {
      label: '窗口',
      submenu: [
        {
          label: '最小化',
          accelerator: 'CmdOrCtrl+M',
          click() {
            const focusedWindow = BrowserWindow.getFocusedWindow();
            if (focusedWindow) {
              focusedWindow.minimize();
            }
          }
        },
        {
          label: '缩放',
          click() {
            const focusedWindow = BrowserWindow.getFocusedWindow();
            if (focusedWindow) {
              if (focusedWindow.isMaximized()) {
                focusedWindow.unmaximize();
              } else {
                focusedWindow.maximize();
              }
            }
          }
        }
      ]
    },
    {
      label: '关于',
      submenu: [
        {
          label: '关于 FreeMix',
          click() {
            showAboutDialog();
          }
        }
      ]
      
      
    }
  ];
  
  const menu = Menu.buildFromTemplate(template);
  Menu.setApplicationMenu(menu);
}

// 窗口控制事件处理
ipcMain.on('window-control', (event, action) => {
  const window = BrowserWindow.getFocusedWindow();
  if (!window) return;
  
  switch (action) {
    case 'minimize':
      window.minimize();
      break;
    case 'maximize':
      if (window.isMaximized()) {
        window.unmaximize();
      } else {
        window.maximize();
      }
      break;
    case 'close':
      window.close();
      break;
  }
});

// 窗口拖动事件处理
ipcMain.on('window-drag', (event, action) => {
  const window = BrowserWindow.getFocusedWindow();
  if (window) {
    window.webContents.send('window-drag-start');
  }
});

// IPC 监听：接收创建新窗口的请求
ipcMain.handle('create-new-window', (event, { winId, options, pageUrl }) => {
  windowManager.createWindow(winId, options, pageUrl);
  // 不返回 BrowserWindow 对象，因为它是不可序列化的
  return { success: true };
});

// IPC 监听：接收关闭窗口的请求
ipcMain.on('close-window', (event, winId) => {
  const win = windowManager.get(winId);
  if (win) {
    win.close();
  }
});

// IPC 监听：接收聚焦窗口的请求
ipcMain.on('focus-window', (event, winId) => {
  windowManager.focus(winId);
});

// IPC 监听：保存 token 到文件
ipcMain.on('save-token', (event, tokenData) => {
  try {
  //  let tokenArray = [];

  //   // 1. 检查文件是否存在，并读取现有内容
  //   if (fs.existsSync(tokenFilePath)) {
  //     const existingData = fs.readFileSync(tokenFilePath, 'utf8');
  //     tokenArray = JSON.parse(existingData);
  //   }

  //   // 2. 将新的 token 数据添加到数组中
  //   tokenArray.push(tokenData);

    // 3. 将整个数组重新写入文件（这会覆盖原文件）
    fs.writeFileSync(tokenFilePath, JSON.stringify(tokenData, null, 2)); // null, 2 参数用于美化输出，便于阅读
    console.log('Token 已成功添加到JSON数组文件', tokenFilePath);
  } catch (error) {
    console.error('保存 token 到文件失败:', error);
  }
});

// IPC 监听：从文件获取 token
ipcMain.handle('get-token', async (event) => {
  try {
    if (fs.existsSync(tokenFilePath)) {
      const data = fs.readFileSync(tokenFilePath, 'utf8');
      console.log('从文件获取 token 成功',data);
      const tokenData = JSON.parse(data);
      return tokenData;
    } else {
      return null;
    }
  } catch (error) {
    console.error('从文件获取 token 失败:', error);
    return null;
  }
});

// IPC 监听：从文件删除 token
ipcMain.on('remove-token', (event) => {
  try {
    if (fs.existsSync(tokenFilePath)) {
      fs.unlinkSync(tokenFilePath);
    }
  } catch (error) {
    console.error('从文件删除 token 失败:', error);
  }
});

// IPC 监听：设置窗口大小
ipcMain.on('set-window-size', (event, { width, height, saveConfig = false }) => {
  // 获取主窗口而不是当前焦点窗口
  const windows = BrowserWindow.getAllWindows();
  const mainWindow = windows.length > 0 ? windows[0] : null;
  
  if (mainWindow) {
    // 确保宽度和高度是数字
    const parsedWidth = parseInt(width);
    const parsedHeight = parseInt(height);
    
    // 验证尺寸参数
    if (isNaN(parsedWidth) || isNaN(parsedHeight) || parsedWidth < 800 || parsedHeight < 600) {
      console.error('无效的窗口尺寸参数:', { width, height });
      return;
    }
    
    // 设置窗口大小
    mainWindow.setSize(parsedWidth, parsedHeight);
    
    // 居中显示窗口
    mainWindow.center();
    
    // 如果需要保存配置，则保存当前窗口配置
    if (saveConfig) {
      const bounds = mainWindow.getBounds();
      const isMaximized = mainWindow.isMaximized();
      
      const configToSave = {
        width: bounds.width,
        height: bounds.height,
        x: bounds.x,
        y: bounds.y,
        isMaximized: isMaximized
      };
      
      saveWindowConfig(configToSave);
    }
  } else {
    console.error('未找到主窗口');
  }
});

// IPC 监听：获取当前窗口大小
ipcMain.handle('get-window-size', async (event) => {
  const window = BrowserWindow.getFocusedWindow();
  if (window) {
    const bounds = window.getBounds();
    return {
      width: bounds.width,
      height: bounds.height
    };
  }
  return null;
});

// 应用准备就绪后创建窗口
app.whenReady().then(() => {
  // 创建系统托盘图标
  createTrayIcon();
  // 创建主窗口
  createWindow();
  
  // 初始化窗口管理器
  windowManager = new WindowManager();
  
  // 创建应用菜单
  createAppMenu();
   
 
  
  // 显示通知
  showNotification('应用已启动', 'FreeMix 正在运行');
  
 
});

// 处理所有窗口关闭事件（在macOS上应用通常保持活动状态）
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

// macOS上当点击Dock图标且没有其他窗口时重新创建窗口
app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});