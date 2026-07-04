<template>
  <div class="user-guide-wrapper" :style="cssVars">
    <n-layout class="user-guide-layout" has-sider position="absolute">
      <!-- 左侧悬浮导航 -->
    

      <!-- 右侧流式内容 -->
      <n-layout-content class="guide-main" :native-scrollbar="false" ref="contentRef" id="guide-scroll-container">
        <div class="content-container">
           <div class="section-header-large">
              <h1 class="gradient-title">FreeMix 文档中心</h1>
              <p class="subtitle">探索功能，释放潜能。您的全能目标管理助手。</p>
            </div>

          <!-- 1. 欢迎页面 (Bento Grid) -->
          <section id="welcome" class="guide-section hero-section">
           <h2 class="section-title"><n-icon>
              <LogoReddit />
            </n-icon> 首页</h2>
          <p class="section-desc">清晰的首页界面，帮助您快速了解 Freemix 的功能和操作。</p>
 <div class="mac-window simulator-window">
             <div class="mac-header">
                <div class="dot red"></div>
                <div class="dot yellow"></div>
                <div class="dot green"></div>
                <div class="address-bar">https://gofreemix.com/#/home</div>
              </div>
              <DashboardViewGuride />
          </div>
           
        
          </section>

          <n-divider />
          <h2 class="section-title"><n-icon>
              <LogoReddit />
            </n-icon> 登录</h2>
          <p class="section-desc">清晰的登录界面，帮助您快速登录 Freemix。</p>
          <!-- 2. 用户认证 (Interactive Simulator) -->
          <div class="mac-window simulator-window">
             <div class="mac-header">
                <div class="dot red"></div>
                <div class="dot yellow"></div>
                <div class="dot green"></div>
                <div class="address-bar">https://gofreemix.com/#/login</div>
              </div>
            <LoginViewGudie />
          </div>

          <n-divider />
          <h2 class="section-title"><n-icon>
              <LogoReddit />
            </n-icon> 目标管理</h2>
          <p class="section-desc">清晰的目标管理界面，帮助您快速创建、编辑和监控目标。</p>
          <!-- 3. 目标管理 (Kanban Simulator) -->
          <div class="mac-window simulator-window">
             <div class="mac-header">
                <div class="dot red"></div>
                <div class="dot yellow"></div>
                <div class="dot green"></div>
                <div class="address-bar">https://gofreemix.com/#/goal-management</div>
              </div>

            <HomeViewGuride />
          </div>

          <n-divider />

          <!-- 4. AI 助手 (Chat Simulator) -->
          <section id="ai" class="guide-section">
            <h2 class="section-title"><n-icon>
                <LogoReddit />
              </n-icon> Freemix AI </h2>
            <p class="section-desc">试着与 AI 对话，体验智能生成目标的魅力。</p>

            <div class="mac-window simulator-window">
              <div class="mac-header">
                <div class="dot red"></div>
                <div class="dot yellow"></div>
                <div class="dot green"></div>
                <div class="address-bar">https://gofreemix.com/#/AIAssistantWindow</div>
              </div>
              <div class="mac-content chat-layout">
                <div class="chat-messages" ref="chatScrollRef">
                  <div v-for="(msg, index) in chatHistory" :key="index" class="chat-bubble-row" :class="msg.role">
                    <div class="chat-avatar" v-if="msg.role === 'ai'">
                      <n-icon>
                        <SparklesOutline />
                      </n-icon>
                    </div>
                    <div class="chat-bubble-content">
                      <div v-if="msg.typing" class="typing-dots">
                        <span>.</span><span>.</span><span>.</span>
                      </div>
                      <span v-else>{{ msg.content }}</span>
                    </div>
                  </div>
                </div>
                <div class="chat-input-area">
                  <n-input v-model:value="chatInput" placeholder="输入'学习计划'试试..." @keydown.enter="sendChat"
                    :disabled="isTyping">
                    <template #suffix>
                      <n-button text @click="sendChat" :disabled="!chatInput || isTyping">
                        <n-icon size="18" :color="themeVars.primaryColor">
                          <PaperPlaneOutline />
                        </n-icon>
                      </n-button>
                    </template>
                  </n-input>
                </div>
              </div>
            </div>
          </section>

          <n-divider />

          <!-- 5. 数据统计 (CSS Charts) -->
          <section id="statistics" class="guide-section">
            <h2 class="section-title"><n-icon>
                <BarChartOutline />
              </n-icon> 数据分析</h2>
            <p class="section-desc">纯 CSS 构建的动态可视化图表，轻量且流畅。</p>

            <n-grid x-gap="24" cols="1 m:2">
              <n-grid-item>
                <div class="mac-window chart-window">
                  <div class="chart-header">周目标完成率</div>
                  <div class="css-bar-chart">
                    <div class="bar-group" v-for="i in 7" :key="i">
                      <div class="bar-fill" :style="{ height: Math.random() * 80 + 20 + '%' }"></div>
                      <div class="bar-label">{{ ['M', 'T', 'W', 'T', 'F', 'S', 'S'][i - 1] }}</div>
                    </div>
                  </div>
                </div>
              </n-grid-item>
              <n-grid-item>
                <div class="mac-window chart-window">
                  <div class="chart-header">专注度分布</div>
                  <div class="css-pie-chart-container">
                    <div class="breathing-circle">
                      <div class="inner-circle">
                        <span>85%</span>
                        <small>专注</small>
                      </div>
                    </div>
                    <div class="legend">
                      <div class="legend-item"><span class="dot" style="background: var(--primary-color)"></span> 工作
                      </div>
                      <div class="legend-item"><span class="dot" style="background: #2080f0"></span> 学习</div>
                    </div>
                  </div>
                </div>
              </n-grid-item>
            </n-grid>
          </section>

          <n-divider />

          <!-- 6. 团队协作 -->
          <section id="collaboration" class="guide-section">
            <h2 class="section-title"><n-icon>
                <PeopleOutline />
              </n-icon> 团队协作</h2>
            <p class="section-desc">多人实时协作，共同达成目标。</p>
            <div class="collab-showcase">
              <div class="collab-card">
                <div class="collab-header">
                  <div class="collab-title">项目冲刺 Alpha</div>
                  <div class="collab-avatars">
                    <n-avatar-group
                      :options="[{ src: 'https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg' }, { src: 'https://gw.alipayobjects.com/zos/antfincdn/aPkFc8Sj7n/method-draw-image.svg' }, { src: 'https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg' }]"
                      :size="32" :max="3" />
                    <div class="add-member-btn">+</div>
                  </div>
                </div>
                <div class="collab-body">
                  <div class="comment-bubble">
                    <n-avatar size="small" src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg" />
                    <div class="bubble-content">
                      <span class="user-name">Alex</span>
                      <p>前端开发进度已更新，请查看！🚀</p>
                    </div>
                  </div>
                  <div class="comment-bubble right">
                    <div class="bubble-content">
                      <span class="user-name">You</span>
                      <p>收到，我稍后合并代码。</p>
                    </div>
                    <n-avatar size="small"
                      src="https://gw.alipayobjects.com/zos/antfincdn/aPkFc8Sj7n/method-draw-image.svg" />
                  </div>
                </div>
                <div class="collab-footer">
                  <div class="permission-tag"><span class="dot green"></span> 实时同步中</div>
                  <div class="permission-tag"><n-icon>
                      <CheckmarkCircle />
                    </n-icon> 权限：管理员</div>
                </div>
              </div>
            </div>
          </section>

          <n-divider />

          <!-- 7. 个性化设置 -->
          <section id="settings" class="guide-section">
            <h2 class="section-title"><n-icon>
                <SettingsOutline />
              </n-icon> 个性化设置</h2>
            <p class="section-desc">定制您的工作流与界面。</p>
            <div class="settings-grid">
              <div class="setting-card theme-card">
                <div class="card-icon"><n-icon>
                    <SparklesOutline />
                  </n-icon></div>
                <h3>主题切换</h3>
                <p>深色模式与浅色模式自动切换，呵护您的双眼。</p>
                <div class="theme-toggle-demo">
                  <div class="toggle-track">
                    <div class="toggle-thumb">
                      <n-icon>
                        <BookOutline />
                      </n-icon>
                    </div>
                  </div>
                </div>
              </div>
              <div class="setting-card security-card">
                <div class="card-icon"><n-icon>
                    <CheckmarkCircle />
                  </n-icon></div>
                <h3>安全防护</h3>
                <p>支持 2FA 双因素认证，保障账户安全。</p>
                <div class="shield-icon">
                  <n-icon size="48">
                    <CheckmarkCircle />
                  </n-icon>
                </div>
              </div>
            </div>
          </section>

          <n-divider />



          <n-divider />

          <!-- 9. 快捷功能 -->
          <section id="shortcuts" class="guide-section">
            <h2 class="section-title"><n-icon>
                <FlashOutline />
              </n-icon> 快捷功能</h2>
            <div class="shortcuts-grid">
              <div class="shortcut-item">
                <div class="key-combo">
                  <kbd>Ctrl</kbd> <span class="plus">+</span> <kbd>N</kbd>
                </div>
                <span class="desc">新建目标</span>
              </div>
              <div class="shortcut-item">
                <div class="key-combo">
                  <kbd>Ctrl</kbd> <span class="plus">+</span> <kbd>F</kbd>
                </div>
                <span class="desc">全局搜索</span>
              </div>
              <div class="shortcut-item">
                <div class="key-combo">
                  <kbd>Esc</kbd>
                </div>
                <span class="desc">关闭弹窗</span>
              </div>
            </div>
          </section>

          <n-divider />

          <!-- 10. 常见问题 -->
          <section id="faq" class="guide-section">
            <h2 class="section-title"><n-icon>
                <HelpCircleOutline />
              </n-icon> 常见问题</h2>
            <n-collapse>
              <n-collapse-item title="如何找回密码？" name="1">
                <div>在登录页点击"忘记密码"，通过注册邮箱重置。</div>
              </n-collapse-item>
              <n-collapse-item title="数据可以导出吗？" name="2">
                <div>支持导出为 Excel 或 PDF 格式。</div>
              </n-collapse-item>
              <n-collapse-item title="如何联系客服？" name="3">
                <div>点击侧边栏底部的"联系支持"按钮。</div>
              </n-collapse-item>
            </n-collapse>
          </section>

          <div class="page-footer">
            <p>© 2025 FreeMix. All rights reserved.</p>
          </div>

        </div>
      </n-layout-content>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue';
import { useThemeVars,NAnchor,NAnchorLink } from 'naive-ui';
import LoginViewGudie from '@/components/LoginViewGudie.vue';
import HomeViewGuride from '@/components/HomeViewGuride.vue';
import DashboardViewGuride from '@/components/DashboardViewGuride.vue';
import {
  BookOutline,
  PersonCircleOutline,
  FlagOutline,
  LogoReddit,
  BarChartOutline,
  PeopleOutline,
  SettingsOutline,
  PhonePortraitOutline,
  FlashOutline,
  HelpCircleOutline,
  SparklesOutline,
  SchoolOutline,
  BriefcaseOutline,
  FitnessOutline,
  CartOutline,
  TrendingUpOutline,
  PieChartOutline,
  CheckmarkCircle,
  PaperPlaneOutline
} from '@vicons/ionicons5';

const themeVars = useThemeVars();

const cssVars = computed(() => {
  const vars = themeVars.value;
  return {
    '--bg-color': vars.bodyColor,
    '--text-color': vars.textColorBase,
    '--text-secondary': vars.textColor2,
    '--primary-color': vars.primaryColor,
    '--border-color': vars.borderColor,
    '--card-bg': vars.cardColor,
    '--hover-color': vars.hoverColor
  };
});

// --- Login Simulator ---
const loginForm = reactive({ email: '', password: '' });
const loginLoading = ref(false);
const loginSuccess = ref(false);

const loginStrength = computed(() => {
  const len = loginForm.password.length;
  if (len === 0) return { score: 0, color: '#eee', label: '' };
  if (len < 6) return { score: 30, color: '#d03050', label: '弱' };
  if (len < 10) return { score: 70, color: '#f0a020', label: '中' };
  return { score: 100, color: '#18a058', label: '强' };
});

const handleLogin = () => {
  if (!loginForm.email || !loginForm.password) return;
  loginLoading.value = true;
  setTimeout(() => {
    loginLoading.value = false;
    loginSuccess.value = true;
  }, 1500);
};

const resetLogin = () => {
  loginSuccess.value = false;
  loginForm.email = '';
  loginForm.password = '';
};

// --- Kanban Simulator ---
interface Task {
  id: number;
  title: string;
  tag: string;
  type: 'info' | 'success' | 'warning' | 'error' | 'default';
}

const todoTasks = ref<Task[]>([
  { id: 1, title: '编写项目文档', tag: '工作', type: 'info' },
  { id: 2, title: '健身 30 分钟', tag: '健康', type: 'success' },
  { id: 3, title: '购买生日礼物', tag: '生活', type: 'warning' }
]);

const doneTasks = ref<Task[]>([]);

const moveTask = (task: Task) => {
  const inTodo = todoTasks.value.find(t => t.id === task.id);
  if (inTodo) {
    todoTasks.value = todoTasks.value.filter(t => t.id !== task.id);
    doneTasks.value.unshift(task);
  } else {
    doneTasks.value = doneTasks.value.filter(t => t.id !== task.id);
    todoTasks.value.unshift(task);
  }
};

// --- AI Chat Simulator ---
interface ChatMsg {
  role: 'user' | 'ai';
  content: string;
  typing?: boolean;
}

const chatHistory = ref<ChatMsg[]>([
  { role: 'ai', content: '你好！我是 AI 助手。想制定什么目标？' }
]);
const chatInput = ref('');
const isTyping = ref(false);
const chatScrollRef = ref<HTMLElement | null>(null);

const scrollToBottom = () => {
  nextTick(() => {
    if (chatScrollRef.value) {
      chatScrollRef.value.scrollTop = chatScrollRef.value.scrollHeight;
    }
  });
};

const typeWriterEffect = (text: string) => {
  const msgIndex = chatHistory.value.length;
  chatHistory.value.push({ role: 'ai', content: '', typing: true });
  scrollToBottom();

  // Wait a bit before typing starts
  setTimeout(() => {
    chatHistory.value[msgIndex].typing = false;
    let i = 0;
    const interval = setInterval(() => {
      chatHistory.value[msgIndex].content += text.charAt(i);
      i++;
      scrollToBottom();
      if (i >= text.length) clearInterval(interval);
    }, 50);
  }, 800);
};

const sendChat = () => {
  if (!chatInput.value.trim() || isTyping.value) return;

  const userText = chatInput.value;
  chatHistory.value.push({ role: 'user', content: userText });
  chatInput.value = '';
  scrollToBottom();

  isTyping.value = true;

  // Simulate AI processing
  setTimeout(() => {
    isTyping.value = false;
    let response = '';
    if (userText.includes('学习')) response = '为您推荐：1. 设定具体的学习时间表；2. 寻找优质的学习资源；3. 定期复习巩固。';
    else if (userText.includes('减肥')) response = '健康减肥建议：1. 制造热量缺口；2. 保证蛋白质摄入；3. 规律的有氧运动。';
    else response = `关于"${userText}"，我可以帮您拆解为更小的子目标，是否需要？`;

    typeWriterEffect(response);
  }, 500);
};

</script>

<style scoped>
.app-main {
  flex: 1;
  padding: 30px;
  background: #09090b;
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow-y: auto;
  /* 允许内容滚动 */
}

/* 标题区 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.dashboard-title h2 {
  font-size: 1.5rem;
  color: #fff;
  margin-bottom: 5px;
}

.dashboard-title p {
  color: #71717a;
  font-size: 0.9rem;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.btn-sm {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-dark {
  background: #27272a;
  color: #fff;
}

.btn-accent {
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-box {
  background: #18181b;
  border: 1px solid #27272a;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

/* 卡片颜色变体 */
.stat-box.gray .stat-icon {
  background: rgba(113, 113, 122, 0.2);
  color: #a1a1aa;
}

.stat-box.green .stat-icon {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.stat-box.yellow .stat-icon {
  background: rgba(234, 179, 8, 0.2);
  color: #eab308;
}

.stat-box.red .stat-icon {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.stat-info h4 {
  font-size: 1.5rem;
  color: #fff;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-info span {
  font-size: 0.8rem;
  color: #71717a;
}

/* 底部两栏布局 */
.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1.8fr;
  /* 左侧趋势图窄一点，右侧表格宽一点 */
  gap: 16px;
  flex: 1;
}

.panel {
  background: #18181b;
  border: 1px solid #27272a;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  color: #e4e4e7;
  font-weight: 600;
}

/* 模拟空状态图表 */
.empty-chart {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #52525b;
  font-size: 0.9rem;
  border: 1px dashed #27272a;
  border-radius: 8px;
  min-height: 150px;
}

/* 模拟表格头 */
.mock-table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  background: #27272a;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.th {
  color: #a1a1aa;
  font-size: 0.8rem;
  font-weight: 500;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #52525b;
  gap: 10px;
}

.empty-state i {
  font-size: 2rem;
  opacity: 0.5;
}

.user-guide-wrapper {
  height: 100vh;
  width: 100vw;

  background-color: var(--bg-color);
  color: var(--text-color);
  transition: background-color 0.3s, color 0.3s;
}

.user-guide-layout {
  height: 100%;
  
 
}

.guide-sidebar {
  background-color: var(--bg-color);
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
  padding-left: 12px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(90deg, var(--primary-color), #2080f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.sidebar-footer {
  position: absolute;
  bottom: 24px;
  left: 16px;
  right: 16px;
}

.support-card {
  background: rgba(var(--primary-color), 0.1);
}

.support-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-bottom: 8px;
}

.guide-main {
  padding: 0;
  scroll-behavior: smooth;
}

.content-container {
  /* max-width: 960px; */
  margin: 0 14%;
  padding: 48px 24px 100px;
}

.guide-section {
  margin-bottom: 100px;
  scroll-margin-top: 60px;
}

.section-header-large {
  /* margin-bottom: 64px; */
  text-align: left;
  padding: 30px;
}

.gradient-title {
  font-size: 40px;
  font-weight: 800;
  margin-bottom: 24px;
  background: linear-gradient(120deg, var(--primary-color), #00c9a7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -1.5px;
  line-height: 1.1;
}

.subtitle {
  font-size: 20px;
  color: var(--text-secondary);
  max-width: 600px;
  /* margin: 0 auto; */
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-desc {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 40px;
  max-width: 700px;
  line-height: 1.6;
}

/* --- Simulator Windows (Mac Style) --- */
.mac-window {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  transition: transform 0.3s ease;
}

.mac-window:hover {
  transform: translateY(-5px);
}

.mac-header {
  background: rgba(128, 128, 128, 0.1);
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid var(--border-color);
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot.red {
  background: #ff5f56;
}

.dot.yellow {
  background: #ffbd2e;
}

.dot.green {
  background: #27c93f;
}

.address-bar {
  margin-left: 12px;
  flex: 1;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--text-secondary);
  max-width: 300px;
  margin: 0 auto;
}

.mac-content {
  padding: 0;
  min-height: 300px;
  position: relative;
}

.centered-content {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

/* Login Simulator */
.login-box {
  width: 100%;
  max-width: 320px;
  text-align: center;
}

.login-logo {
  margin-bottom: 16px;
}

.password-strength {
  margin-top: 8px;
  text-align: left;
  opacity: 0;
  transition: opacity 0.3s;
}

.password-strength.visible {
  opacity: 1;
}

.strength-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 4px;
  color: var(--text-secondary);
}

.success-animation {
  animation: fadeIn 0.5s ease;
}

.checkmark-icon {
  animation: scaleIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* Kanban Simulator */
.kanban-content {
  display: flex;
  height: 400px;
  padding: 24px;
  gap: 24px;
  background: rgba(128, 128, 128, 0.05);
}

.kanban-column {
  flex: 1;
  background: var(--card-bg);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-color);
}

.column-header {
  font-weight: 600;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.todo {
  background: #f0a020;
}

.status-dot.done {
  background: #18a058;
}

.task-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-card {
  background: var(--bg-color);
  padding: 12px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: var(--primary-color);
}

.task-card.done {
  opacity: 0.7;
  text-decoration: line-through;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-meta {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.due-date {
  font-size: 12px;
  color: var(--text-secondary);
}

/* AI Chat Simulator */
.chat-layout {
  display: flex;
  flex-direction: column;
  height: 450px;
  background: var(--bg-color);
}

.chat-messages {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-bubble-row {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.chat-bubble-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-avatar {
  width: 32px;
  height: 32px;
  background: var(--primary-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.chat-bubble-content {
  background: var(--card-bg);
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  font-size: 14px;
  line-height: 1.5;
}

.chat-bubble-row.user .chat-bubble-content {
  background: var(--primary-color);
  color: white;
  border: none;
}

.typing-dots span {
  animation: blink 1.4s infinite both;
  margin: 0 1px;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

.chat-input-area {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
  background: var(--card-bg);
}

/* CSS Charts */
.chart-window {
  height: 300px;
  display: flex;
  flex-direction: column;
}

.chart-header {
  padding: 16px;
  font-weight: 600;
  text-align: center;
}

.css-bar-chart {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  padding: 20px 40px;
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  height: 100%;
  justify-content: flex-end;
  width: 20px;
}

.bar-fill {
  width: 100%;
  background: linear-gradient(to top, var(--primary-color), #2080f0);
  border-radius: 4px;
  transition: height 1s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation: growBar 1s ease-out backwards;
}

.bar-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.css-pie-chart-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
}

.breathing-circle {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: conic-gradient(var(--primary-color) 0% 65%, #2080f0 65% 85%, #f0a020 85% 100%);
  position: relative;
  animation: spin 20s linear infinite;
  box-shadow: 0 0 30px rgba(var(--primary-color), 0.3);
}

.inner-circle {
  position: absolute;
  top: 15px;
  left: 15px;
  right: 15px;
  bottom: 15px;
  background: var(--card-bg);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  font-weight: bold;
  font-size: 24px;
  animation: counterSpin 20s linear infinite;
  /* Keeps text upright */
}

.inner-circle small {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: normal;
}

.legend {
  margin-top: 20px;
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

/* Animations */
@keyframes blink {
  0% {
    opacity: 0.2;
  }

  20% {
    opacity: 1;
  }

  100% {
    opacity: 0.2;
  }
}

@keyframes growBar {
  from {
    height: 0;
    opacity: 0;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@keyframes counterSpin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(-360deg);
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }

  to {
    transform: scale(1);
  }
}

/* Mobile Preview Refinement */
.mobile-app-ui {
  width: 100%;
  height: 100%;
  padding: 16px;
  position: relative;
}

.mobile-header {
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 20px;
}

.mobile-card {
  height: 80px;
  background: rgba(128, 128, 128, 0.1);
  border-radius: 12px;
  margin-bottom: 12px;
}

.mobile-fab {
  position: absolute;
  bottom: 20px;
  right: 20px;
  width: 48px;
  height: 48px;
  background: var(--primary-color);
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* Bento Grid */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 240px);
  gap: 24px;
}

.bento-item {
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 24px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
  position: relative;
}

.bento-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.main-feature {
  grid-column: span 2;
  grid-row: span 2;
  background: linear-gradient(145deg, var(--card-bg), rgba(var(--primary-color), 0.05));
}

.wide-feature {
  grid-column: span 3;
  height: 120px;
  display: flex;
  align-items: center;
}

.bento-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.bento-icon {
  margin-bottom: 24px;
  color: var(--primary-color);
}

.bento-item h3 {
  font-size: 24px;
  margin-bottom: 8px;
  font-weight: 600;
}

.bento-item p {
  color: var(--text-secondary);
}

/* Collaboration Showcase */
.collab-showcase {
  margin-top: 24px;
}

.collab-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.collab-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(128, 128, 128, 0.02);
}

.collab-title {
  font-weight: 600;
  font-size: 16px;
}

.collab-avatars {
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-member-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px dashed var(--text-secondary);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
}

.collab-body {
  padding: 24px;
  background: var(--bg-color);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-bubble {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.comment-bubble.right {
  align-self: flex-end;
  text-align: right;
  flex-direction: row;
  /* Avatar on right handled by order or flex-direction: row-reverse if preferred, but structure implies flex order */
  justify-content: flex-end;
}

.bubble-content {
  background: var(--card-bg);
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.user-name {
  font-size: 12px;
  color: var(--text-secondary);
  display: block;
  margin-bottom: 4px;
}

.comment-bubble.right .bubble-content {
  background: rgba(var(--primary-color), 0.1);
  border-color: transparent;
}

.collab-footer {
  padding: 12px 16px;
  background: var(--card-bg);
  border-top: 1px solid var(--border-color);
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-secondary);
}

.permission-tag {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Settings Grid */
.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.setting-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  transition: transform 0.3s ease;
  overflow: hidden;
  position: relative;
}

.setting-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
}

.card-icon {
  font-size: 32px;
  color: var(--primary-color);
  margin-bottom: 16px;
}

.setting-card h3 {
  margin-bottom: 8px;
  font-size: 18px;
}

.setting-card p {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 24px;
}

/* Theme Toggle Demo */
.theme-toggle-demo {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.toggle-track {
  width: 60px;
  height: 32px;
  background: #333;
  border-radius: 16px;
  position: relative;
  cursor: pointer;
  transition: background 0.3s;
}

.toggle-thumb {
  width: 26px;
  height: 26px;
  background: #fff;
  border-radius: 50%;
  position: absolute;
  top: 3px;
  left: 3px;
  transition: left 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
}

.setting-card:hover .toggle-thumb {
  left: 31px;
}

.setting-card:hover .toggle-track {
  background: var(--primary-color);
}

/* Security Shield */
.shield-icon {
  margin-top: 10px;
  color: #18a058;
  opacity: 0.8;
  transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.setting-card:hover .shield-icon {
  transform: scale(1.2);
  opacity: 1;
}

/* Shortcuts SaaS Style */
.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.shortcut-item {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  transition: border-color 0.3s;
}

.shortcut-item:hover {
  border-color: var(--primary-color);
}

.key-combo {
  display: flex;
  align-items: center;
  gap: 6px;
}

kbd {
  background: linear-gradient(to bottom, #f9f9f9, #e9e9e9);
  border: 1px solid #ccc;
  border-bottom-color: #bbb;
  border-radius: 6px;
  box-shadow: 0 2px 0 #bbb;
  color: #333;
  display: inline-block;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  padding: 6px 10px;
  white-space: nowrap;
}

/* Dark mode adaptation for kbd */
.user-guide-wrapper[style*="--bg-color: #101014"] kbd,
.user-guide-wrapper[style*="--bg-color: rgb(16, 16, 20)"] kbd {
  background: linear-gradient(to bottom, #444, #333);
  border-color: #555;
  border-bottom-color: #222;
  box-shadow: 0 2px 0 #222;
  color: #eee;
}

.plus {
  color: var(--text-secondary);
  font-size: 12px;
}

.desc {
  font-size: 14px;
  color: var(--text-secondary);
}

@media (max-width: 1024px) {
  .bento-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }

  .main-feature,
  .wide-feature {
    grid-column: span 1;
    grid-row: span 1;
  }

  .ai-demo-container {
    grid-template-columns: 1fr;
  }
}
</style>
