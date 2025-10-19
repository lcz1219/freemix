# 前端请求添加 Loading 状态功能实现总结

## 功能概述
为项目中的所有异步请求添加全局 Loading 状态显示，提升用户体验。

## 实现步骤

### 1. 创建 Loading 状态管理模块
- 在 `/src/stores/loading.js` 中创建基于 Vuex 的 Loading 状态管理模块
- 包含状态：loading（布尔值）、loadingText（字符串）
- 包含操作：showLoading、hideLoading

### 2. 注册 Loading 模块
- 在 `/src/stores/index.js` 中导入并注册 loading 模块

### 3. 修改请求工具函数
- 在 `/src/utils/request.js` 中修改 postM、getM、getMPaths 函数
- 添加 loadingText 参数支持
- 在请求开始时调用 store.dispatch('loading/showLoading', loadingText)
- 在请求结束时调用 store.dispatch('loading/hideLoading')

### 4. 添加全局 Loading 组件
- 在 `/src/App.vue` 中添加基于 n-modal 的全局 Loading 组件
- 添加相关样式定义

### 5. 更新所有使用 getMPaths 的文件
为所有调用 getMPaths 的文件添加自定义 Loading 文本：

1. `/src/views/home.vue` - "正在获取目标数据..."
2. `/src/views/mobile-home.vue` - "正在获取目标数据..."
3. `/src/views/statistics.vue` - "正在获取统计数据..."
4. `/src/views/goal-structure.vue` - "正在获取目标数据..."
5. `/src/views/goal-management.vue` - "正在获取目标数据..."
6. `/src/views/mobile-statistics.vue` - "正在获取统计数据..."
7. `/src/views/recycle.vue` - "正在获取回收数据..." (接口名称：recycle)
8. `/src/views/mobile-settings.vue` - "正在获取用户信息..." (接口名称：getUserInfo)

## 验证
所有修改已完成，Loading 状态功能已实现并应用到所有相关请求中。