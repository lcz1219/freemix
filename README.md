# 🌟 Freemix - 您的智能目标管理伙伴 🌟

## 🎯 项目是什么？

**Freemix** 是一个功能强大、设计优雅的**全栈目标管理系统（Goal Management System）**。它采用**跨平台架构**，既提供现代化的 Web 应用，又通过 **Electron 框架**打包成**原生桌面应用**，让用户在任何环境下都能享受流畅的目标管理体验！

## 💻 跨平台特性 - Electron 桌面端 🖥️

**Freemix 创新地集成了 Electron 框架**，实现了真正的"一次开发，多端部署"：

### 🔹 原生桌面体验
- 独立的桌面应用程序，无需打开浏览器
- 系统托盘集成，快速访问和提醒功能
- 原生菜单栏和快捷键支持
- 离线数据存储，随时随地管理目标

### 🔹 跨平台兼容性
- **Windows** - 完整的 Windows 桌面应用体验
- **macOS** - 原生 macOS 风格，完美适配
- **Linux** - 支持主流 Linux 发行版

### 🔹 自动更新机制
- 内置自动更新功能，及时获取最新特性
- 静默安装，用户体验无缝衔接

## 🛠 技术架构 (Software Architecture)

- **前端 (Frontend)**: 位于 `/admin` 目录，使用现代化的 **Vue.js** 框架构建，提供流畅且响应式的用户界面。
- **后端 (Backend)**: 位于 `/server` 目录，使用 **Java (Spring Boot)** 提供稳健的 API 服务。
- **桌面端 (Desktop)**: 基于 **Electron 框架**，将 Web 应用打包成原生桌面应用程序。
- **数据存储 (Data Storage)**: 创新地使用 **JSON 文件** 作为数据库，简化了开发流程。
- **开发工具**: 项目包含 `.idea` 目录，使用 **JetBrains IntelliJ IDEA** 进行专业开发。

## 🚀 多平台安装与使用

### 🌐 Web 版本使用:
- 访问在线网址，立即开始使用
- 无需安装，随时随地通过浏览器访问
- 数据云端同步，多设备无缝切换

### 💻 桌面端安装:

#### 方法一：从源码构建
- git clone https://github.com/lcz1219/freemix.git
- cd freemix
- npm install
- npm run electron:dev # 开发模式
- ./build-mac.sh/./build-win.sh # 构建安装包
#### 方法二：直接下载安装包
- 前往 Releases 页面下载对应系统的安装文件
- 安装完毕后需要在终端执行sudo xattr -rd com.apple.quarantine /Applications/Freemix.app(绕过macos的外来下载源限制)



### 📱 移动端规划:
- 未来版本将推出 React Native 移动应用
- 支持 iOS 和 Android 平台

## 🎨 Electron 特性亮点

### ✨ 系统集成
- 桌面通知提醒，不错过任何目标截止日期
- 文件系统访问，支持目标附件管理
- 硬件加速，性能媲美原生应用

### ✨ 用户体验优化
- 启动速度快，响应迅速
- 内存占用优化，运行流畅
- 自定义主题，个性化设置

## 🔄 开发工作流
- Vue.js 前端 → Spring Boot 后端 → Electron 打包 → 多平台分发

## 📦 发布渠道

- **GitHub Releases**: 提供最新版本的安装包下载
- **自动更新**: 应用内一键更新到最新版本
- **多语言支持**: 国际化准备，支持多语言界面

## 🚀 立即体验

-无论您是喜欢**轻量级的 Web 版本**，还是偏好**功能完整的桌面应用**，Freemix 都能满足您的需求！选择最适合您的方式，开始高效的目标管理之旅吧！

⭐ **Star 这个项目，支持开源开发！** ⭐
