<template>
  <div class="dashboard-container">
    <!-- 左侧侧边栏 -->
  

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部 Tab 栏 -->
     

      <!-- 页面内容 -->
      <div class="content-body">
        
        <!-- 标题区 -->
        <div class="page-header">
          <h1>目标管理</h1>
          <p class="subtitle">管理您的所有目标，查看进度并进行编辑</p>
        </div>

        <!-- 工具栏 -->
        <div class="toolbar-section">
          <div class="left-tools">
            <button class="btn btn-primary">
              <span class="icon-plus">+</span> 添加新目标
            </button>
            <button class="btn btn-success">
              <span class="icon-file">📄</span> 导入Excel
            </button>
            <button class="btn btn-dark">
              ↻ 刷新
            </button>
          </div>
          <div class="right-tools">
            <div class="search-box">
              <span class="search-icon">🔍</span>
              <input type="text" placeholder="搜索目标...">
            </div>
            <div class="filter-box">
              状态筛选 <span class="arrow">▼</span>
            </div>
          </div>
        </div>

        <!-- 表格卡片 -->
        <div class="table-card">
          <div class="card-title">
            <span class="green-dot-icon">Target</span>
            <h2>我的目标</h2>
          </div>

          <div class="table-responsive">
            <table>
              <thead>
                <tr>
                  <th style="width: 40px"></th> <!-- 展开箭头占位 -->
                  <th>目标名称</th>
                  <th>负责人</th>
                  <th>截止日期</th>
                  <th>进度</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in goals" :key="index">
                  <td class="expand-col">›</td>
                  <td>{{ item.name }}</td>
                  <td>{{ item.owner }}</td>
                  <td>{{ item.date }}</td>
                  
                  <!-- 进度条列 -->
                  <td class="progress-col">
                    <div class="progress-bar-bg">
                      <div 
                        class="progress-bar-fill" 
                        :class="item.status === 'completed' ? 'green' : 'red'"
                        :style="{ width: item.progress + '%' }"
                      ></div>
                    </div>
                    <span class="progress-icon">
                      <svg v-if="item.status === 'completed'" class="icon-check" viewBox="0 0 24 24"><path d="M20 6L9 17l-5-5" stroke="currentColor" fill="none" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/></svg>
                      <svg v-else class="icon-x" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                    </span>
                  </td>

                  <!-- 状态列 -->
                  <td>
                    <span class="badge" :class="item.status === 'completed' ? 'badge-green' : 'badge-red'">
                      {{ item.status === 'completed' ? '已完成' : '已过期' }}
                    </span>
                  </td>

                  <!-- 操作列 -->
                  <td class="actions-col">
                    <button class="action-btn" title="查看"><svg viewBox="0 0 24 24" width="14" height="14" stroke="currentColor" stroke-width="2" fill="none"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg></button>
                    <button class="action-btn" title="编辑"><svg viewBox="0 0 24 24" width="14" height="14" stroke="currentColor" stroke-width="2" fill="none"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg></button>
                    <button class="action-btn" title="删除"><svg viewBox="0 0 24 24" width="14" height="14" stroke="currentColor" stroke-width="2" fill="none"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg></button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </main>

    <!-- 右侧浮动工具 (保持与登录页一致) -->
   
  </div>
</template>

<script setup>
import { ref } from 'vue';

// 模拟表格数据
const goals = ref([
  {
    name: "测试项目",
    owner: "linchengzhong",
    date: "2026-01-26",
    progress: 100,
    status: "completed"
  },
  {
    name: "0000",
    owner: "linchengzhong",
    date: "2025-10-23",
    progress: 70,
    status: "overdue"
  },
  {
    name: "健身计划",
    owner: "linchengzhong",
    date: "2025-11-15",
    progress: 70,
    status: "overdue"
  },
  {
    name: "web开发",
    owner: "linchengzhong",
    date: "2025-11-15",
    progress: 65,
    status: "overdue"
  },
  {
    name: "学习Excel的有效方法",
    owner: "linchengzhong",
    date: "2025-11-15",
    progress: 60,
    status: "overdue"
  },
  {
    name: "2",
    owner: "linchengzhong",
    date: "2025-12-03",
    progress: 55,
    status: "overdue"
  },
  {
    name: "web",
    owner: "linchengzhong",
    date: "2025-12-10",
    progress: 55,
    status: "overdue"
  },
  {
    name: "222",
    owner: "linchengzhong",
    date: "2025-12-15",
    progress: 50,
    status: "overdue"
  },
  {
    name: "000",
    owner: "linchengzhong",
    date: "2025-12-15",
    progress: 40,
    status: "overdue"
  }
]);
</script>

<style scoped>
/* 全局重置与基础样式 */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.dashboard-container {
  display: flex;
  background-color: #0f0f0f;
  color: #ffffff;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  overflow-x: hidden;
}

/* -------------------- 侧边栏 -------------------- */
.sidebar {
  width: 60px;
  background-color: #1a1a1a;
  border-right: 1px solid #333;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20px;
  padding-bottom: 20px;
  position: sticky;
  top: 0;
  height: 100vh;
  z-index: 100;
}

.logo-box {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #fff 0%, #ddd 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  border: 1px solid #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.3);
}

.nav-icons {
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 1;
}

.bottom-icons {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.nav-item {
  color: #666;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

.nav-item:hover, .nav-item.active {
  background-color: #2c2c2c;
  color: #10b981;
}

/* -------------------- 主内容区 -------------------- */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #0f0f0f;
  overflow-x: hidden;
}

/* 顶部标签栏 */
.top-bar {
  height: 50px;
  border-bottom: 1px solid #222;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: #141414;
}

.tabs-scroll {
  display: flex;
  gap: 5px;
  overflow-x: auto;
  align-items: center;
}

.tab-item {
  padding: 8px 15px;
  font-size: 13px;
  color: #888;
  background-color: #1a1a1a;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid transparent;
}

.tab-item:hover {
  background-color: #252525;
}

.tab-item.active {
  color: #fff;
  border-bottom: 2px solid #10b981;
  background-color: #252525;
}

.close-x {
  font-size: 14px;
  opacity: 0.5;
}
.close-x:hover { opacity: 1; }

.switch-track {
  width: 44px;
  height: 24px;
  background-color: #10b981;
  border-radius: 12px;
  position: relative;
  cursor: pointer;
}
.switch-thumb {
  width: 20px;
  height: 20px;
  background-color: #fff;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  right: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

/* 内容主体 */
.content-body {
  padding: 30px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

/* 工具栏 */
.toolbar-section {
  background-color: #141414;
  padding: 15px 20px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border: 1px solid #222;
}

.left-tools {
  display: flex;
  gap: 10px;
}

.right-tools {
  display: flex;
  gap: 15px;
  align-items: center;
}

.btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 500;
  transition: opacity 0.2s;
}
.btn:hover { opacity: 0.9; }

.btn-primary {
  background-color: #4ade80; /* 绿色 */
  color: #000;
}
.btn-success {
  background-color: #86efac; /* 浅绿 */
  color: #000;
}
.btn-dark {
  background-color: #2c2c2c;
  color: #fff;
  border: 1px solid #444;
}

.search-box {
  position: relative;
}
.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  font-size: 12px;
}
.search-box input {
  background-color: #1f1f1f;
  border: 1px solid #333;
  padding: 8px 10px 8px 30px;
  border-radius: 4px;
  color: #fff;
  outline: none;
  width: 200px;
}

.filter-box {
  background-color: #2c2c2c;
  padding: 8px 15px;
  border-radius: 4px;
  font-size: 13px;
  color: #aaa;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 表格卡片 */
.table-card {
  background-color: #141414;
  border-radius: 12px;
  border: 1px solid #222;
  padding: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.green-dot-icon {
  color: #10b981;
  font-weight: bold;
  letter-spacing: 2px;
  font-family: monospace;
}
.card-title h2 {
  font-size: 18px;
  font-weight: 500;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

th {
  text-align: left;
  color: #888;
  font-weight: 400;
  padding: 15px;
  border-bottom: 1px solid #2c2c2c;
}

td {
  padding: 15px;
  color: #ddd;
  border-bottom: 1px solid #1f1f1f;
  vertical-align: middle;
}

/* 斑马纹/悬停 */
tr:hover td {
  background-color: #1a1a1a;
}

.expand-col {
  color: #666;
  cursor: pointer;
}

/* 进度条样式 */
.progress-col {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 50px; /* 强制高度以便垂直居中 */
}

.progress-bar-bg {
  width: 120px;
  height: 8px;
  background-color: #2c2c2c;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  border-radius: 4px;
}
.progress-bar-fill.green { background-color: #10b981; }
.progress-bar-fill.red { background-color: #f87171; }

.progress-icon svg {
  width: 16px;
  height: 16px;
}
.icon-check { color: #10b981; }
.icon-x { color: #f87171; }

/* 状态徽标 */
.badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid;
}

.badge-green {
  color: #10b981;
  border-color: #064e3b;
  background-color: rgba(16, 185, 129, 0.1);
}

.badge-red {
  color: #f87171;
  border-color: #450a0a;
  background-color: rgba(248, 113, 113, 0.1);
}

/* 操作按钮 */
.actions-col {
  display: flex;
  gap: 10px;
}

.action-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  background-color: #2c2c2c;
  color: #aaa;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: #3c3c3c;
  color: #fff;
}

/* 右侧浮动 */
.floating-tools {
  position: fixed;
  right: 20px;
  bottom: 40px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  z-index: 200;
}

.tool-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #2c2c2c;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.3s;
  color: #888;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.tool-btn:hover {
  background-color: #3c3c3c;
  color: #fff;
}

.tool-btn.pink {
  background-color: #d46b7a;
  color: white;
  font-weight: bold;
  font-size: 12px;
}
</style>