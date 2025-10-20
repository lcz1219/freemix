# 修改记录

## 2024-06-19

### 优化内容

1. **封装重复的token处理逻辑**
   - 创建了`src/utils/tokenUtils.js`工具函数文件，统一处理移动端和桌面端的token存储逻辑
   - 根据设备类型自动选择存储方式（web端使用localStorage，桌面端使用文件存储和localStorage）

2. **优化request.js请求模块**
   - 使用新的tokenUtils工具函数替换原有的重复token处理逻辑
   - 统一了移动端和桌面端的token获取方式
   - 优化了文件上传请求的token处理逻辑

3. **优化desktopToken.js桌面端token管理**
   - 移除了与tokenUtils工具函数重复的普通token处理逻辑
   - 保留了桌面端专用的token管理函数

4. **优化App.vue主应用组件**
   - 移除了重复的token处理逻辑
   - 使用新的工具函数简化代码

5. **优化NavBar.vue导航组件**
   - 使用tokenUtils工具函数替换重复的token处理逻辑
   - 统一了token检查方式

6. **优化login.vue登录组件**
   - 使用tokenUtils工具函数替换重复的token处理逻辑
   - 统一了token保存方式

### 技术细节

- 所有优化都保持了原有的功能逻辑不变
- 通过工具函数封装，提高了代码的可维护性和可读性
- 减少了代码重复，降低了维护成本