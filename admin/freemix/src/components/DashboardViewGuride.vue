<template>
    <div class="window-container">
        <div class="app-window">
            <!-- 左侧侧边栏 -->
            <aside class="sidebar">
                <!-- 窗口控制按钮 (红黄绿) -->


                <div class="app-title">FreeMix - Goal Tracker</div>

                <!-- 导航菜单 -->
                <nav class="nav-menu">
                    <div class="nav-item active">
                        <span class="icon">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <path d="M21.21 15.89A10 10 0 1 1 8 2.83"></path>
                                <path d="M22 12A10 10 0 0 0 12 2v10z"></path>
                            </svg>
                        </span>
                        仪表盘
                    </div>
                    <div class="nav-item">
                        <span class="icon">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                <polyline points="14 2 14 8 20 8"></polyline>
                                <line x1="16" y1="13" x2="8" y2="13"></line>
                                <line x1="16" y1="17" x2="8" y2="17"></line>
                                <polyline points="10 9 9 9 8 9"></polyline>
                            </svg>
                        </span>
                        目标管理
                    </div>
                    <div class="nav-item">
                        <span class="icon">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                            </svg>
                        </span>
                        统计数据
                    </div>
                    <div class="nav-item">
                        <span class="icon">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <line x1="18" y1="20" x2="18" y2="10"></line>
                                <line x1="12" y1="20" x2="12" y2="4"></line>
                                <line x1="6" y1="20" x2="6" y2="14"></line>
                            </svg>
                        </span>
                        目标结构
                    </div>
                </nav>

                <!-- 底部菜单 -->
                <div class="bottom-menu">
                    <div class="nav-item">
                        <span class="icon">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <path
                                    d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z">
                                </path>
                            </svg>
                        </span>
                        消息
                    </div>
                    <div class="nav-item">
                        <span class="icon">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                        </span>
                        用户
                    </div>
                </div>
            </aside>

            <!-- 右侧主内容 -->
            <main class="main-content">
                <!-- 滚动条轨道模拟 -->
                <div class="custom-scrollbar-track">
                    <div class="custom-scrollbar-thumb"></div>
                </div>

                <div class="content-wrapper">
                    <!-- 头部 Hero 区域 -->
                    <header class="hero-section">
                        <h1>掌控你的目标，衡量你的成功</h1>
                        <p class="subtitle">目标追踪者是一款强大的目标管理系统，帮助您设定、跟踪并完成个人和职业目标。</p>
                        <div class="action-buttons">
                            <button class="btn btn-dark">+ 添加新目标</button>
                            <button class="btn btn-primary">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                    stroke-width="2" style="margin-right:5px">
                                    <path
                                        d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8z">
                                    </path>
                                    <path d="M12 8v8"></path>
                                    <path d="M8 12h8"></path>
                                </svg>
                                AI 助手
                            </button>
                        </div>
                    </header>

                    <!-- 统计卡片行 -->
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-icon-bg gray">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                    stroke-width="2">
                                    <circle cx="12" cy="12" r="10"></circle>
                                    <polyline points="12 6 12 12 16 14"></polyline>
                                </svg>
                            </div>
                            <div class="stat-info">
                                <div class="stat-value">0</div>
                                <div class="stat-label">总目标数</div>
                            </div>
                        </div>

                        <div class="stat-card">
                            <div class="stat-icon-bg green">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                    stroke-width="3">
                                    <polyline points="20 6 9 17 4 12"></polyline>
                                </svg>
                            </div>
                            <div class="stat-info">
                                <div class="stat-value">0</div>
                                <div class="stat-label">已完成</div>
                            </div>
                        </div>

                        <div class="stat-card">
                            <div class="stat-icon-bg yellow">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                    stroke-width="3">
                                    <circle cx="12" cy="12" r="10"></circle>
                                    <line x1="12" y1="8" x2="12" y2="12"></line>
                                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                                </svg>
                            </div>
                            <div class="stat-info">
                                <div class="stat-value">0</div>
                                <div class="stat-label">进行中</div>
                            </div>
                        </div>

                        <div class="stat-card">
                            <div class="stat-icon-bg red">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                    stroke-width="2">
                                    <circle cx="12" cy="12" r="10"></circle>
                                    <line x1="15" y1="9" x2="9" y2="15"></line>
                                    <line x1="9" y1="9" x2="15" y2="15"></line>
                                </svg>
                            </div>
                            <div class="stat-info">
                                <div class="stat-value">0</div>
                                <div class="stat-label">已过期</div>
                            </div>
                        </div>
                    </div>

                    <!-- 底部面板区域 -->
                    <div class="dashboard-panels">
                        <!-- 完成趋势 -->
                        <div class="panel trend-panel">
                            <div class="panel-header">完成趋势</div>
                            <div class="panel-body empty-state-box">
                                <div class="dashed-border">
                                    暂无趋势数据
                                </div>
                            </div>
                        </div>

                        <!-- 详细统计数据 -->
                        <div class="panel details-panel">
                            <div class="panel-header">
                                <span class="red-icon">⊞</span> 详细统计数据
                                <span class="arrow-icon">➔</span>
                            </div>
                            <div class="panel-body">
                                <div class="table-header-row">
                                    <span>目标名称 ⌄</span>
                                    <span>负责人</span>
                                    <span>截止日期</span>
                                    <span>进度</span>
                                    <span>状态</span>
                                </div>
                                <div class="empty-table-content">
                                    <div class="file-icon">
                                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#333"
                                            stroke-width="1.5">
                                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                            <polyline points="14 2 14 8 20 8"></polyline>
                                            <line x1="10" y1="13" x2="14" y2="17"></line>
                                            <line x1="14" y1="13" x2="10" y2="17"></line>
                                        </svg>
                                    </div>
                                    <div class="empty-text">无数据</div>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </main>
        </div>
    </div>
</template>

<script setup>
// 这里可以添加逻辑，例如 active 菜单的切换等
</script>

<style scoped>
/* 容器背景，用于展示窗口效果 */
.window-container {
    background-color: #000;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    /* padding: 20px; */
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    color: #fff;
}

/* 主窗口框架 */
.app-window {
    width: 1200px;
    height: 800px;
    background-color: #050505;
    border-radius: 12px;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
    display: flex;
    overflow: hidden;
    border: 1px solid #222;
}

/* -------------------- 侧边栏 -------------------- */
.sidebar {
    width: 240px;
    background-color: #0a0a0a;
    display: flex;
    flex-direction: column;
    padding: 20px;
    border-right: 1px solid #1a1a1a;
}

.window-controls {
    display: flex;
    gap: 8px;
    margin-bottom: 20px;
}

.control {
    width: 12px;
    height: 12px;
    border-radius: 50%;
}

.red {
    background-color: #ff5f56;
}

.yellow {
    background-color: #ffbd2e;
}

.green {
    background-color: #27c93f;
}

.app-title {
    color: #666;
    font-size: 13px;
    margin-bottom: 30px;
    padding-left: 5px;
}

.nav-menu {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 15px;
    border-radius: 8px;
    color: #888;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.2s;
}

.nav-item:hover {
    background-color: #1a1a1a;
    color: #fff;
}

.nav-item.active {
    background-color: #0f291e;
    /* 深绿色背景 */
    color: #10b981;
    /* 亮绿色文字 */
    border-left: 3px solid #10b981;
    padding-left: 12px;
    /* 调整因border增加的偏移 */
}

.bottom-menu {
    margin-top: auto;
    display: flex;
    flex-direction: column;
    gap: 5px;
    border-top: 1px solid #1a1a1a;
    padding-top: 20px;
}

/* -------------------- 主内容区 -------------------- */
.main-content {
    flex: 1;
    background-color: #050505;
    position: relative;
    display: flex;
    flex-direction: column;
}

/* 自定义滚动条样式 (模拟右侧那个白条) */
.custom-scrollbar-track {
    position: absolute;
    right: 5px;
    top: 50px;
    bottom: 50px;
    width: 6px;
    background-color: #1a1a1a;
    border-radius: 3px;
    z-index: 10;
}

.custom-scrollbar-thumb {
    width: 100%;
    height: 100px;
    /* 模拟长度 */
    background-color: #444;
    border-radius: 3px;
}

.content-wrapper {
    padding: 40px 60px;
    height: 100%;
    overflow-y: auto;
    /* 允许内容滚动 */
}

/* 头部 Hero */
.hero-section {
    text-align: center;
    margin-bottom: 50px;
}

.hero-section h1 {
    font-size: 28px;
    font-weight: 600;
    margin-bottom: 15px;
    letter-spacing: 1px;
}

.subtitle {
    color: #666;
    font-size: 14px;
    margin-bottom: 30px;
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
}

.action-buttons {
    display: flex;
    justify-content: center;
    gap: 15px;
}

.btn {
    padding: 10px 20px;
    border-radius: 6px;
    border: none;
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    font-weight: 500;
}

.btn-dark {
    background-color: #262626;
    color: #fff;
    transition: background 0.2s;
}

.btn-dark:hover {
    background-color: #333;
}

.btn-primary {
    background-color: #10b981;
    color: #000;
    font-weight: 600;
}

.btn-primary:hover {
    background-color: #059669;
}

/* 统计卡片 Grid */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 30px;
}

.stat-card {
    background-color: #0f0f0f;
    border: 1px solid #1a1a1a;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 15px;
}

.stat-icon-bg {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.stat-icon-bg.gray {
    background-color: #262626;
    color: #888;
}

.stat-icon-bg.green {
    background-color: #064e3b;
    color: #10b981;
}

.stat-icon-bg.yellow {
    background-color: #451a03;
    color: #f59e0b;
}

.stat-icon-bg.red {
    background-color: #450a0a;
    color: #ef4444;
}

.stat-value {
    font-size: 24px;
    font-weight: 600;
    line-height: 1.2;
}

.stat-label {
    font-size: 12px;
    color: #666;
}

/* 底部面板 */
.dashboard-panels {
    display: grid;
    grid-template-columns: 1fr 1.5fr;
    gap: 20px;
    height: 300px;
}

.panel {
    background-color: #0f0f0f;
    border: 1px solid #1a1a1a;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
}

.panel-header {
    padding: 15px 20px;
    font-size: 14px;
    font-weight: 600;
    border-bottom: 1px solid #1a1a1a;
    display: flex;
    align-items: center;
    gap: 10px;
}

.red-icon {
    color: #ef4444;
    font-size: 16px;
}

.arrow-icon {
    margin-left: auto;
    color: #666;
    font-size: 12px;
}

.panel-body {
    flex: 1;
    padding: 20px;
    position: relative;
}

/* 趋势图空状态 */
.trend-panel .empty-state-box {
    display: flex;
    justify-content: center;
    align-items: center;
}

.dashed-border {
    width: 100%;
    height: 100%;
    border: 1px dashed #333;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #444;
    font-size: 14px;
}

/* 详细表格空状态 */
.table-header-row {
    display: flex;
    justify-content: space-between;
    background-color: #1a1a1a;
    padding: 10px 15px;
    border-radius: 6px;
    color: #666;
    font-size: 12px;
    margin-bottom: 40px;
}

.empty-table-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #444;
    height: 120px;
}

.file-icon {
    margin-bottom: 10px;
}

.empty-text {
    font-size: 14px;
}
</style>