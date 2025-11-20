# Freemix 项目开发文档

## 项目概述

Freemix 是一个功能强大、设计优雅的跨平台目标管理系统。它采用全栈架构，既提供现代化的 Web 应用，又通过 Electron 框架打包成原生桌面应用，让用户在任何环境下都能享受流畅的目标管理体验。

### 核心特性
- **跨平台目标管理**：支持 Web 端和桌面端
- **多端协作**：支持团队协作和消息通知
- **AI 助手集成**：提供智能目标管理功能
- **主题切换**：支持明暗主题切换
- **响应式设计**：支持移动端和桌面端
- **实时通知**：WebSocket 实时消息推送

## 项目架构

### 目录结构
```
freemix/
├── admin/                 # 前端代码
│   └── freemix/           # Vue.js 前端应用
│       ├── src/           # 源代码
│       ├── electron/      # Electron 桌面应用相关
│       └── package.json   # 前端依赖配置
├── server/                # Spring Boot 后端
│   ├── src/main/java/     # Java 源代码
│   ├── src/main/resources/# 配置文件
│   └── pom.xml           # 后端依赖配置
├── README.md             # 项目说明
└── IFLOW.md              # 本开发文档
```

### 技术栈

#### 前端技术栈
- **框架**: Vue.js 3 + TypeScript
- **UI 库**: Naive UI、Element Plus、Vant
- **状态管理**: Vuex + Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios
- **图表库**: Chart.js、ECharts
- **桌面应用**: Electron
- **工作流**: LogicFlow、Vue Flow
- **其他**: Markdown-it、QRCode、SockJS Client

#### 后端技术栈
- **框架**: Spring Boot 3.5.3
- **语言**: Java 17
- **数据访问**: MongoDB、Redis、JDBC
- **数据库**: MySQL
- **安全**: JWT、OAuth2
- **文档**: Swagger 3、Knife4j
- **Excel处理**: Apache POI
- **其他**: Lombok、AspectJ、Alipay SDK

## 前端架构详解

### 核心组件
- `App.vue`: 主应用组件，负责主题切换、布局管理和全局状态
- `router/index.ts`: 路由配置，支持移动端/桌面端组件切换
- `stores/`: Pinia 状态管理
- `utils/`: 工具函数 (设备检测、token管理、WebSocket等)

### 主要视图
- `home.vue`: 主页视图
- `goal-management.vue`: 目标管理视图
- `statistics.vue`: 统计视图
- `goal-structure.vue`: 目标结构视图
- `MessageCenter.vue`: 消息中心
- `AIAssistantWindow.vue`: AI助手窗口
- `user-guide.vue`: 用户指南

### 核心功能模块

#### 1. 设备适配
```javascript
// 根据设备类型选择组件
const getComponent = (desktopComponent: any, mobileComponent: any) => {
  return isMobile() ? mobileComponent : desktopComponent
}
```

#### 2. 主题管理
- 支持明暗主题切换
- 通过 CSS 变量管理主题样式
- 主题状态持久化存储

#### 3. 用户认证
- Token 管理系统
- 路由守卫验证
- OAuth2 支持

#### 4. WebSocket 通信
- 实时消息推送
- 通知系统集成
- 桌面端和 Web 端统一处理

## 后端架构详解

### 核心功能
- RESTful API 设计
- WebSocket 实时通信
- 文件上传/下载
- OAuth2 认证
- JWT Token 管理
- 数据库操作 (MongoDB、MySQL)

### 依赖管理
- Spring Boot Web
- Spring Data JPA (MongoDB)
- Spring Data Redis
- Spring WebSocket
- Swagger/Knife4j 文档
- JWT 认证
- Apache POI (Excel处理)

## Electron 桌面应用

### 核心功能
- **系统托盘**: 支持系统托盘图标和菜单
- **窗口管理**: 自定义窗口大小和位置记忆
- **通知系统**: 桌面通知集成
- **文件系统**: 本地数据存储和 Token 管理
- **自动更新**: 支持自动更新机制

### 重要文件
- `electron/main.js`: 主进程代码，负责窗口创建、系统托盘、IPC通信
- `electron/preload.js`: 预加载脚本
- `electron/WindowManager.js`: 窗口管理器

### 构建配置
- 使用 electron-builder 打包
- 支持 macOS、Windows、Linux
- 提供多个构建脚本

## 开发工作流

### 项目启动

#### 前端开发
```bash
# 进入前端目录
cd admin/freemix

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 启动 Electron 开发模式
npm run electron:dev
```

#### 后端开发
```bash
# 进入后端目录
cd server

# 启动 Spring Boot 应用
./mvnw spring-boot:run
```

### 构建命令

#### 前端构建
```bash
# 构建 Web 版本
npm run build

# 构建 Electron 应用
npm run electron:build

# 构建特定平台
npm run electron:build:mac      # macOS
npm run electron:build:win      # Windows
npm run electron:build:linux    # Linux
npm run electron:build:all      # 所有平台
```

#### 代码检查
```bash
# 类型检查
npm run type-check

# 代码格式化
npm run format
```

## 配置文件

### 前端配置
- `package.json`: 依赖和脚本配置
- `vite.config.ts`: Vite 构建配置
- `tsconfig.json`: TypeScript 配置

### 后端配置
- `pom.xml`: Maven 依赖配置
- `application.yml`: 应用配置文件
- `application-dev.yml`: 开发环境配置
- `application-prod.yml`: 生产环境配置

## 部署指南

### Web 版本部署
1. 构建前端应用: `npm run build`
2. 将 `dist` 目录内容部署到 Web 服务器
3. 后端部署 Spring Boot 应用

### 桌面应用部署
1. 构建前端: `npm run build`
2. 构建桌面应用: `npm run electron:build`
3. 分发生成的安装包

## 开发规范

### 代码风格
- 使用 TypeScript 进行类型安全开发
- 遵循 Vue 3 Composition API 模式
- 使用 ESLint 和 Prettier 进行代码格式化

### 组件命名
- 使用帕斯卡命名法 (PascalCase) 命名 Vue 组件
- 文件名与组件名保持一致

### 状态管理
- 使用 Pinia 进行组件状态管理
- 使用 Vuex 进行全局状态管理

## API 接口

### 认证接口
- `/login`: 用户登录
- `/register`: 用户注册
- `/logout`: 用户登出

### 目标管理接口
- `/api/goals`: 目标 CRUD 操作
- `/api/statistics`: 统计数据获取

### 消息接口
- WebSocket 连接: `/websocket`
- 消息推送: 实时通信

## 安全特性

### 前端安全
- Context Isolation 启用
- Node Integration 禁用
- IPC 通信安全验证

### 后端安全
- JWT Token 认证
- OAuth2 集成
- 输入验证和过滤

## 测试策略

### 前端测试
- 组件单元测试
- 端到端测试 (待完善)

### 后端测试
- 单元测试 (JUnit)
- 集成测试
- API 测试

## 维护和监控

### 日志管理
- 前端: 浏览器控制台
- 后端: SLF4J + Logback
- 桌面端: electron-log

### 性能优化
- 代码分割和懒加载
- 图片优化
- 缓存策略

## 扩展功能

### AI 功能
- AI 助手集成
- 智能目标建议
- 自然语言处理

### 移动端支持
- Vant UI 组件库
- 响应式设计
- 移动端路由适配

### 数据可视化
- Chart.js 集成
- ECharts 图表
- 统计数据分析

## 项目维护

### 版本管理
- Git 分支管理
- 语义化版本控制
- 发布流程

### 依赖更新
- 定期更新依赖库
- 安全漏洞检查
- 兼容性测试

## 常见问题

### 构建问题
- 确保 Node.js 版本兼容
- 检查依赖安装完整性
- 验证构建配置正确性

### 运行时问题
- 检查环境变量配置
- 验证 API 端点连接
- 确认数据库连接状态

## 未来发展

### 功能规划
- 移动端应用 (React Native)
- 多语言国际化
- 高级数据分析
- 团队协作功能增强

### 技术改进
- 性能优化
- 安全增强
- 用户体验改进
- 可访问性支持