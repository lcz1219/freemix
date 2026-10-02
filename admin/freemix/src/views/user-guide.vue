<template>
  <div class="guide-page">
    <!-- ── 左侧固定导航：滚动时自动高亮当前章节 ── -->
   

    <!-- ── 右侧内容区：独立滚动容器 ── -->
    <main class="guide-content" ref="contentRef" @scroll="onScroll" @mousemove="onGlowMove">
      <!-- 顶部阅读进度条（sticky 跟随滚动） -->
      <div class="progress-track">
        <div class="progress-bar" :style="{ width: scrollProgress + '%' }"></div>
      </div>

      <div class="content-inner">
        <!-- ▍Hero 区（:style 绑定视差位移，随滚动上移淡出） -->
        <header class="hero" :style="heroStyle">
         
          <h1 class="hero-title">FreeMix 文档中心</h1>
          <p class="hero-subtitle">探索功能，释放潜能。从目标创建到数据分析，一步步带您上手全部能力。</p>
          <div class="hero-stats">
            <div class="hero-stat">
              <span class="hero-stat-num">9</span>
              <span class="hero-stat-label">功能章节</span>
            </div>
            <div class="hero-stat">
              <span class="hero-stat-num">3</span>
              <span class="hero-stat-label">界面模拟</span>
            </div>
            <div class="hero-stat">
              <span class="hero-stat-num">24/7</span>
              <span class="hero-stat-label">在线支持</span>
            </div>
          </div>
        </header>

        <!-- ▍01 首页 -->
        <section id="welcome" class="guide-section">
          <div class="section-head">
            <span class="section-index">01</span>
            <div class="section-titles">
              <h2 class="section-title">首页</h2>
              <p class="section-desc">清晰的首页界面，帮助您快速了解 Freemix 的功能和操作。</p>
            </div>
          </div>
          <!-- 模拟窗口：顶部伪浏览器栏 + 内嵌真实界面组件 -->
          <div class="sim-frame">
            <div class="sim-bar">
              <div class="dots">
                <i class="dot red"></i>
                <i class="dot yellow"></i>
                <i class="dot green"></i>
              </div>
              <div class="address-bar">https://gofreemix.com/#/home</div>
            </div>
            <div class="sim-body">
              <DashboardViewGuride />
            </div>
          </div>
        </section>

        <!-- ▍02 登录 -->
        <section id="login" class="guide-section">
          <div class="section-head">
            <span class="section-index">02</span>
            <div class="section-titles">
              <h2 class="section-title">登录</h2>
              <p class="section-desc">支持扫码与账号密码两种方式，帮助您快速登录 Freemix。</p>
            </div>
          </div>
          <div class="sim-frame">
            <div class="sim-bar">
              <div class="dots">
                <i class="dot red"></i>
                <i class="dot yellow"></i>
                <i class="dot green"></i>
              </div>
              <div class="address-bar">https://gofreemix.com/#/login</div>
            </div>
            <div class="sim-body">
              <LoginViewGudie />
            </div>
          </div>
        </section>

        <!-- ▍03 目标管理 -->
        <section id="goal" class="guide-section">
          <div class="section-head">
            <span class="section-index">03</span>
            <div class="section-titles">
              <h2 class="section-title">目标管理</h2>
              <p class="section-desc">清晰的目标列表，帮助您快速创建、编辑和监控目标进度。</p>
            </div>
          </div>
          <div class="sim-frame">
            <div class="sim-bar">
              <div class="dots">
                <i class="dot red"></i>
                <i class="dot yellow"></i>
                <i class="dot green"></i>
              </div>
              <div class="address-bar">https://gofreemix.com/#/goal-management</div>
            </div>
            <div class="sim-body">
              <HomeViewGuride />
            </div>
          </div>
        </section>

        <!-- ▍04 Freemix AI -->
        <section id="ai" class="guide-section">
          <div class="section-head">
            <span class="section-index">04</span>
            <div class="section-titles">
              <h2 class="section-title">Freemix AI</h2>
              <p class="section-desc">试着与 AI 对话，体验智能生成子目标的魅力。</p>
            </div>
          </div>
          <div class="sim-frame">
            <div class="sim-bar">
              <div class="dots">
                <i class="dot red"></i>
                <i class="dot yellow"></i>
                <i class="dot green"></i>
              </div>
              <div class="address-bar">https://gofreemix.com/#/AIAssistantWindow</div>
            </div>
            <!-- 聊天模拟器 -->
            <div class="chat-layout">
              <div class="chat-messages" ref="chatScrollRef">
                <div v-for="(msg, index) in chatHistory" :key="index" class="chat-row" :class="msg.role">
                  <div v-if="msg.role === 'ai'" class="chat-avatar">
                    <n-icon :size="16">
                      <SparklesOutline />
                    </n-icon>
                  </div>
                  <div class="chat-bubble">
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
                      <n-icon size="18" color="#00c9a7">
                        <PaperPlaneOutline />
                      </n-icon>
                    </n-button>
                  </template>
                </n-input>
              </div>
            </div>
          </div>
        </section>

        <!-- ▍05 数据分析 -->
        <section id="statistics" class="guide-section">
          <div class="section-head">
            <span class="section-index">05</span>
            <div class="section-titles">
              <h2 class="section-title">数据分析</h2>
              <p class="section-desc">纯 CSS 构建的动态可视化图表，轻量且流畅。</p>
            </div>
          </div>
          <n-grid x-gap="24" cols="1 m:2">
            <n-grid-item>
              <!-- 柱状图 -->
              <div class="chart-card">
                <div class="chart-title">周目标完成率</div>
                <div class="bar-chart">
                  <div class="bar-col" v-for="(bar, index) in weekBars" :key="index">
                    <div class="bar-track">
                      <div class="bar-fill" :style="{ height: bar.value + '%' }"></div>
                    </div>
                    <span class="bar-label">{{ bar.label }}</span>
                  </div>
                </div>
              </div>
            </n-grid-item>
            <n-grid-item>
              <!-- 环形占比图 -->
              <div class="chart-card">
                <div class="chart-title">专注度分布</div>
                <div class="ring-chart">
                  <div class="breathing-circle">
                    <div class="inner-circle">
                      <span>85%</span>
                      <small>专注</small>
                    </div>
                  </div>
                  <div class="legend">
                    <div class="legend-item">
                      <span class="legend-dot" style="background: #00c9a7"></span> 工作
                    </div>
                    <div class="legend-item">
                      <span class="legend-dot" style="background: #2080f0"></span> 学习
                    </div>
                  </div>
                </div>
              </div>
            </n-grid-item>
          </n-grid>
        </section>

        <!-- ▍06 团队协作 -->
        <section id="collaboration" class="guide-section">
          <div class="section-head">
            <span class="section-index">06</span>
            <div class="section-titles">
              <h2 class="section-title">团队协作</h2>
              <p class="section-desc">多人实时协作，共同达成目标。</p>
            </div>
          </div>
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
              <div class="comment-row">
                <n-avatar size="small" src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg" />
                <div class="bubble">
                  <span class="user-name">Alex</span>
                  <p>前端开发进度已更新，请查看！🚀</p>
                </div>
              </div>
              <div class="comment-row right">
                <div class="bubble self">
                  <span class="user-name">You</span>
                  <p>收到，我稍后合并代码。</p>
                </div>
                <n-avatar size="small"
                  src="https://gw.alipayobjects.com/zos/antfincdn/aPkFc8Sj7n/method-draw-image.svg" />
              </div>
            </div>
            <div class="collab-footer">
              <div class="permission-tag">
                <span class="live-dot"></span> 实时同步中
              </div>
              <div class="permission-tag">
                <n-icon :size="14">
                  <CheckmarkCircle />
                </n-icon>
                权限：管理员
              </div>
            </div>
          </div>
        </section>

        <!-- ▍07 个性化设置 -->
        <section id="settings" class="guide-section">
          <div class="section-head">
            <span class="section-index">07</span>
            <div class="section-titles">
              <h2 class="section-title">个性化设置</h2>
              <p class="section-desc">定制您的工作流与界面，点击下方开关可立即切换主题。</p>
            </div>
          </div>
          <div class="settings-grid">
            <!-- 主题切换卡：点击即真实切换明暗主题 -->
            <div class="setting-card clickable" @click="handleToggleTheme">
              <div class="card-icon">
                <n-icon :size="26">
                  <component :is="isDark ? MoonOutline : SunnyOutline" />
                </n-icon>
              </div>
              <h3>主题切换</h3>
              <p>深色模式与浅色模式一键切换，呵护您的双眼。</p>
              <div class="theme-toggle" :class="{ on: isDark }">
                <div class="toggle-thumb">
                  <n-icon :size="14">
                    <component :is="isDark ? MoonOutline : SunnyOutline" />
                  </n-icon>
                </div>
              </div>
            </div>

            <!-- 安全防护卡 -->
            <div class="setting-card">
              <div class="card-icon">
                <n-icon :size="26">
                  <ShieldCheckmarkOutline />
                </n-icon>
              </div>
              <h3>安全防护</h3>
              <p>支持 2FA 双因素认证，为账户多加一道锁。</p>
              <div class="shield-icon">
                <n-icon :size="46">
                  <ShieldCheckmarkOutline />
                </n-icon>
              </div>
            </div>
          </div>
        </section>

        <!-- ▍08 快捷功能 -->
        <section id="shortcuts" class="guide-section">
          <div class="section-head">
            <span class="section-index">08</span>
            <div class="section-titles">
              <h2 class="section-title">快捷功能</h2>
              <p class="section-desc">常用操作都配有快捷键，熟练后效率翻倍。</p>
            </div>
          </div>
          <div class="shortcuts-grid">
            <div class="shortcut-item">
              <div class="key-combo">
                <kbd>Ctrl</kbd><span class="plus">+</span><kbd>N</kbd>
              </div>
              <span class="shortcut-desc">新建目标</span>
            </div>
            <div class="shortcut-item">
              <div class="key-combo">
                <kbd>Ctrl</kbd><span class="plus">+</span><kbd>F</kbd>
              </div>
              <span class="shortcut-desc">全局搜索</span>
            </div>
            <div class="shortcut-item">
              <div class="key-combo">
                <kbd>Esc</kbd>
              </div>
              <span class="shortcut-desc">关闭弹窗</span>
            </div>
          </div>
        </section>

        <!-- ▍09 常见问题 -->
        <section id="faq" class="guide-section">
          <div class="section-head">
            <span class="section-index">09</span>
            <div class="section-titles">
              <h2 class="section-title">常见问题</h2>
              <p class="section-desc">还有疑问？这里整理了最常被问到的几个问题。</p>
            </div>
          </div>
          <n-collapse>
            <n-collapse-item title="如何找回密码？" name="1">
              <div>在登录页点击"忘记密码"，通过注册邮箱重置。</div>
            </n-collapse-item>
            <n-collapse-item title="数据可以导出吗？" name="2">
              <div>支持导出为 Excel 或 PDF 格式。</div>
            </n-collapse-item>
            <n-collapse-item title="如何联系客服？" name="3">
              <div>点击侧边栏底部的"联系支持"按钮，或发送邮件至 3687649273@qq.com。</div>
            </n-collapse-item>
          </n-collapse>
        </section>

        <!-- 页脚 -->
        <footer class="page-footer">
          <p>© 2025 FreeMix. All rights reserved.</p>
        </footer>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject, markRaw, onMounted, onUnmounted, nextTick } from 'vue';
import LoginViewGudie from '@/components/LoginViewGudie.vue';
import HomeViewGuride from '@/components/HomeViewGuride.vue';
import DashboardViewGuride from '@/components/DashboardViewGuride.vue';
import {
  LogoReddit,
  HomeOutline,
  PersonCircleOutline,
  FlagOutline,
  BarChartOutline,
  PeopleOutline,
  SettingsOutline,
  FlashOutline,
  HelpCircleOutline,
  SparklesOutline,
  CheckmarkCircle,
  PaperPlaneOutline,
  MoonOutline,
  SunnyOutline,
  ShieldCheckmarkOutline
} from '@vicons/ionicons5';

// ── 主题：直接复用 App.vue 通过 provide 注入的状态 ──
// isDark 用于「个性化设置」章节的演示开关；toggleTheme 用于点击时真实切换主题
const isDark = inject('isDark', ref(true));
const toggleTheme = inject<(() => void) | undefined>('toggleTheme', undefined);

// 点击主题卡：调用 App.vue 注入的切换方法（注入不到时静默忽略）
const handleToggleTheme = () => {
  if (typeof toggleTheme === 'function') toggleTheme();
};

// ── 章节导航配置 ──
// icon 用 markRaw 包裹：图标是组件对象，避免被 Vue 做成响应式代理产生性能开销与告警
const navItems = [
  { id: 'welcome', label: '首页', icon: markRaw(HomeOutline) },
  { id: 'login', label: '登录', icon: markRaw(PersonCircleOutline) },
  { id: 'goal', label: '目标管理', icon: markRaw(FlagOutline) },
  { id: 'ai', label: 'Freemix AI', icon: markRaw(SparklesOutline) },
  { id: 'statistics', label: '数据分析', icon: markRaw(BarChartOutline) },
  { id: 'collaboration', label: '团队协作', icon: markRaw(PeopleOutline) },
  { id: 'settings', label: '个性化设置', icon: markRaw(SettingsOutline) },
  { id: 'shortcuts', label: '快捷功能', icon: markRaw(FlashOutline) },
  { id: 'faq', label: '常见问题', icon: markRaw(HelpCircleOutline) }
];

// ── 滚动联动：当前高亮章节 + 顶部阅读进度 ──
const contentRef = ref<HTMLElement | null>(null);
const activeId = ref('welcome');
const scrollProgress = ref(0);
const scrollTop = ref(0);

// Hero 视差：向下滚动时 Hero 缓慢上移并淡出，形成"退场"层次感
const heroStyle = computed(() => ({
  opacity: String(Math.max(0, 1 - scrollTop.value / 420)),
  transform: `translateY(${scrollTop.value * 0.22}px)`
}));

const handleScroll = () => {
  const el = contentRef.value;
  if (!el) return;

  scrollTop.value = el.scrollTop;

  // 1. 阅读进度 = 已滚动距离 / 可滚动总距离
  const maxScroll = el.scrollHeight - el.clientHeight;
  scrollProgress.value = maxScroll > 0 ? Math.min(100, (el.scrollTop / maxScroll) * 100) : 0;

  // 2. 当前章节：取最后一个「顶部已越过分隔线」的章节
  let current = navItems[0].id;
  for (const item of navItems) {
    const section = document.getElementById(item.id);
    if (section && section.offsetTop - 140 <= el.scrollTop) {
      current = item.id;
    }
  }
  activeId.value = current;
};

// 点击导航：平滑滚动到目标章节
const scrollToSection = (id: string) => {
  const container = contentRef.value;
  const section = document.getElementById(id);
  if (!container || !section) return;
  container.scrollTo({ top: section.offsetTop - 24, behavior: 'smooth' });
};

// 滚动事件节流：滚动会高频触发，用 requestAnimationFrame 保证每帧最多执行一次，
// 避免在同一帧里反复读取布局属性（offsetTop 等）造成强制重排
let scrollTicking = false;
const onScroll = () => {
  if (scrollTicking) return;
  scrollTicking = true;
  requestAnimationFrame(() => {
    handleScroll();
    scrollTicking = false;
  });
};

// ── 鼠标跟随光晕 ──
// 原理：卡片上用 radial-gradient 画一层以 --mx/--my 为圆心的品牌色光斑，
// 默认 --mx/--my 落在卡片中心（CSS 里的兜底值），鼠标移动时实时改写这两个变量，
// 光斑就跟着指针走。这里用「事件委托」——只在内容区监听一次 mousemove，
// 通过 closest() 反查最近的卡片，避免给每张卡片单独绑事件。
const GLOW_SELECTOR = '.sim-frame, .chart-card, .collab-card, .setting-card, .shortcut-item';

const applyGlow = (e: MouseEvent) => {
  const card = (e.target as HTMLElement | null)?.closest<HTMLElement>(GLOW_SELECTOR);
  if (!card) return;
  // getBoundingClientRect 拿卡片在视口里的位置，换算成指针相对卡片的坐标
  const rect = card.getBoundingClientRect();
  card.style.setProperty('--mx', `${e.clientX - rect.left}px`);
  card.style.setProperty('--my', `${e.clientY - rect.top}px`);
};

// 与滚动同理做 rAF 节流：mousemove 触发极频繁，且 getBoundingClientRect 会读取布局，
// 每帧只处理最后一次事件，防止连续读写布局造成卡顿
let glowTicking = false;
let glowEvent: MouseEvent | null = null;
const onGlowMove = (e: MouseEvent) => {
  glowEvent = e;
  if (glowTicking) return;
  glowTicking = true;
  requestAnimationFrame(() => {
    if (glowEvent) applyGlow(glowEvent);
    glowTicking = false;
  });
};

// ── 苹果式滚动演示 ──
// 原理：给元素打上 data-reveal 标记后，样式表让它们默认「透明 + 下移」；
// 元素进入视口时由观察器加上 .in-view，CSS transition 便播放浮现动画；
// 完全离开视口时移除 .in-view 复位，因此往回滚或再次向下滚都会重新演示。
let revealObserver: IntersectionObserver | null = null;

// 单个块元素：整块浮现
const REVEAL_SELECTOR = [
  '.hero-badge',
  '.hero-title',
  '.hero-subtitle',
  '.hero-stats',
  '.sim-frame',
  '.chart-card',
  '.collab-card',
  '.page-footer'
].join(',');

// 章节标题组：序号 → 标题 → 描述 依次浮现（节奏在样式表里单独编排）
const HEAD_SELECTOR = '.section-head';

// 容器：内部子元素依次错峰浮现（卡片逐个出现）
const STAGGER_SELECTOR = '.settings-grid, .shortcuts-grid, .n-collapse';

const setupReveal = () => {
  const container = contentRef.value;
  if (!container) return;

  // 打标记：样式表通过 [data-reveal] / [data-reveal-stagger] / [data-reveal-head] 命中这些元素
  container.querySelectorAll<HTMLElement>(REVEAL_SELECTOR).forEach((el) => {
    el.setAttribute('data-reveal', '');
  });
  container.querySelectorAll<HTMLElement>(HEAD_SELECTOR).forEach((el) => {
    el.setAttribute('data-reveal-head', '');
  });
  container.querySelectorAll<HTMLElement>(STAGGER_SELECTOR).forEach((el) => {
    el.setAttribute('data-reveal-stagger', '');
  });

  // root 指定为内容区：因为滚动的不是浏览器窗口，而是这个 overflow 容器
  revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        const el = entry.target as HTMLElement;
        if (entry.isIntersecting) {
          el.classList.add('in-view');
        } else {
          el.classList.remove('in-view');
        }
      });
    },
    {
      root: container,
      // 底部收缩 10%：让元素滚过视口底部一小段后再触发，避免一露头就播完
      rootMargin: '0px 0px -10% 0px',
      threshold: 0
    }
  );

  container
    .querySelectorAll('[data-reveal], [data-reveal-head], [data-reveal-stagger]')
    .forEach((el) => {
      revealObserver?.observe(el);
    });
};

onMounted(() => {
  handleScroll(); // 初始化一次，保证刚进入时状态正确
  setupReveal();  // 注册滚动演示观察器
  window.addEventListener('resize', handleScroll);
});

onUnmounted(() => {
  revealObserver?.disconnect(); // 断开观察器，防止内存泄漏
  window.removeEventListener('resize', handleScroll);
});

// ── 数据分析章节：柱状图静态数据（固定值，避免重渲染时高度跳变） ──
const weekBars = [
  { label: 'M', value: 45 },
  { label: 'T', value: 72 },
  { label: 'W', value: 58 },
  { label: 'T', value: 90 },
  { label: 'F', value: 66 },
  { label: 'S', value: 38 },
  { label: 'S', value: 82 }
];

// ── AI 聊天模拟器 ──
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

// 聊天区滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatScrollRef.value) {
      chatScrollRef.value.scrollTop = chatScrollRef.value.scrollHeight;
    }
  });
};

// 打字机效果：先显示"正在输入"，再逐字输出
const typeWriterEffect = (text: string) => {
  const msgIndex = chatHistory.value.length;
  chatHistory.value.push({ role: 'ai', content: '', typing: true });
  scrollToBottom();

  // 稍作停顿后再开始逐字输出，模拟思考过程
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

// 发送消息：按关键词返回预设回复
const sendChat = () => {
  if (!chatInput.value.trim() || isTyping.value) return;

  const userText = chatInput.value;
  chatHistory.value.push({ role: 'user', content: userText });
  chatInput.value = '';
  scrollToBottom();

  isTyping.value = true;

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
/* ═══════════════════════════════════════════════
   配色体系
   全部基于 base.css 的 --fm-* 设计令牌推导，
   令牌由 html.dark-theme / html.light-theme 切换，
   所以本页无需写两套样式即可自动适配明暗主题。
   ═══════════════════════════════════════════════ */
.guide-page {
  --g-bg: rgb(var(--fm-layout-bg-rgb));
  /* 页面底色 */
  --g-surface: rgb(var(--fm-container-bg-rgb));
  /* 卡片底色 */
  --g-text: rgb(var(--fm-base-text-rgb));
  /* 主文字 */
  --g-text-sub: rgba(var(--fm-base-text-rgb), 0.62);
  /* 次要文字 */
  --g-text-muted: rgba(var(--fm-base-text-rgb), 0.4);
  /* 更弱的说明文字 */
  --g-line: rgba(var(--fm-base-text-rgb), 0.09);
  /* 常规描边 */
  --g-line-strong: rgba(var(--fm-base-text-rgb), 0.16);
  /* 强调描边 */
  --g-fill: rgba(var(--fm-base-text-rgb), 0.04);
  /* 浅底/悬停 */
  --g-fill-strong: rgba(var(--fm-base-text-rgb), 0.07);
  /* 稍深的浅底 */
  --g-primary: #00c9a7;
  /* 品牌主色 */
  --g-primary-soft: rgba(0, 201, 167, 0.12);

  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  /* position + isolation 建立独立层叠上下文：
     极光伪元素的 z-index:-1 才会被"关"在本页里，
     不会穿到页面底色之下，也不会影响外部布局 */
  position: relative;
  isolation: isolate;
  background-color: var(--g-bg);
  color: var(--g-text);
  transition: background-color 0.3s, color 0.3s;
}

/* ─────────────── 左侧导航 ─────────────── */
.guide-nav {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 28px 20px 20px;
  background-color: var(--g-surface);
  border-right: 1px solid var(--g-line);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px 24px;
}

.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #04211d;
  background: linear-gradient(135deg, #00c9a7 0%, #00e0c0 100%);
  box-shadow: 0 6px 16px rgba(0, 201, 167, 0.28);
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
}

.brand-sub {
  font-size: 12px;
  color: var(--g-text-sub);
}

.nav-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 14px;
  color: var(--g-text-sub);
  text-decoration: none;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  /* 左侧高亮竖条，默认透明占位避免选中时文字位移 */
  border-left: 3px solid transparent;
}

.nav-link:hover {
  background-color: var(--g-fill);
  color: var(--g-text);
}

.nav-link.active {
  background-color: var(--g-primary-soft);
  color: var(--g-primary);
  border-left-color: var(--g-primary);
  font-weight: 600;
}

.nav-footer {
  padding-top: 20px;
}

.support-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px;
  border-radius: 12px;
  background-color: var(--g-primary-soft);
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.support-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--g-primary);
}

.support-desc {
  font-size: 12px;
  color: var(--g-text-sub);
  word-break: break-all;
}

.copyright {
  margin-top: 14px;
  font-size: 11px;
  text-align: center;
  color: var(--g-text-muted);
}

/* ─────────────── 右侧内容区 ─────────────── */
.guide-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  scroll-behavior: smooth;
}

/* 自定义滚动条，跟随主题 */
.guide-content::-webkit-scrollbar {
  width: 8px;
}

.guide-content::-webkit-scrollbar-thumb {
  background-color: var(--g-line-strong);
  border-radius: 4px;
}

.guide-content::-webkit-scrollbar-track {
  background-color: transparent;
}

/* 顶部阅读进度条 */
.progress-track {
  position: sticky;
  top: 0;
  height: 3px;
  z-index: 20;
}

.progress-bar {
  height: 100%;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(90deg, #00c9a7 0%, #00e0c0 100%);
  transition: width 0.1s linear;
}

.content-inner {
  max-width: 1120px;
  margin: 0 auto;
  padding: 56px 48px 120px;
}

/* ─────────────── Hero 区 ─────────────── */
.hero {
  padding-bottom: 56px;
  border-bottom: 1px solid var(--g-line);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--g-primary);
  background-color: var(--g-primary-soft);
  border: 1px solid rgba(0, 201, 167, 0.22);
}

.hero-title {
  margin: 20px 0 16px;
  font-size: 44px;
  font-weight: 800;
  letter-spacing: -1.5px;
  line-height: 1.15;
  /* 渐变首尾同色（都是主色），配合 background-size 放大后平移，
     才能在循环时无缝衔接；240% 即平铺一块的宽度 */
  background: linear-gradient(120deg, var(--g-primary) 0%, #00e0c0 38%, #2080f0 62%, var(--g-primary) 100%);
  background-size: 240% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  /* 位移一整块宽度后回到起点，肉眼看到的是流光反复扫过文字 */
  animation: titleShine 7s linear infinite;
}

.hero-subtitle {
  max-width: 620px;
  font-size: 17px;
  line-height: 1.7;
  color: var(--g-text-sub);
}

.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 32px;
}

.hero-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 120px;
  padding: 14px 20px;
  border-radius: 14px;
  background-color: var(--g-surface);
  border: 1px solid var(--g-line);
}

.hero-stat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--g-primary);
}

.hero-stat-label {
  font-size: 12px;
  color: var(--g-text-sub);
}

/* ═══════════════════════════════════════════════
   苹果式滚动演示
   元素默认「透明 + 下移」，进入视口后由 JS 加上 .in-view，
   transition 便播放浮现动画；完全离开视口会复位，方便重播。
   ═══════════════════════════════════════════════ */
[data-reveal] {
  opacity: 0;
  transform: translateY(28px);
  transition: opacity 0.7s ease, transform 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  transition-delay: var(--rd, 0ms);
  /* 同类元素用 --rd 拉开先后顺序 */
  will-change: opacity, transform;
}

[data-reveal].in-view {
  opacity: 1;
  transform: none;
}

/* Hero 内部元素逐个出现 */
.hero .hero-badge {
  --rd: 0ms;
}

.hero .hero-title {
  --rd: 90ms;
}

.hero .hero-subtitle {
  --rd: 180ms;
}

.hero .hero-stats {
  --rd: 280ms;
}

/* 模拟窗口：以 3D 透视从「后仰」状态展平，模拟产品演示的出场方式。
   perspective 写进 transform 才能生效（相当于给元素设一个视距） */
.sim-frame[data-reveal] {
  transform: perspective(1400px) rotateX(9deg) translateY(46px) scale(0.955);
  transform-origin: center top;
  transition: opacity 0.8s ease, transform 1s cubic-bezier(0.16, 1, 0.3, 1);
}

.sim-frame[data-reveal].in-view {
  transform: perspective(1400px) rotateX(0deg) translateY(0) scale(1);
}

/* 章节标题组：序号 → 标题 → 描述 依次浮现 */
.section-head[data-reveal-head] > .section-index,
.section-head[data-reveal-head] .section-title,
.section-head[data-reveal-head] .section-desc {
  opacity: 0;
  transform: translateY(26px);
  transition: opacity 0.7s ease, transform 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

.section-head[data-reveal-head] > .section-index {
  transition-delay: 0ms;
}

.section-head[data-reveal-head] .section-title {
  transition-delay: 90ms;
}

.section-head[data-reveal-head] .section-desc {
  transition-delay: 170ms;
}

/* 序号本身带 0.28 的装饰性透明度，动画终点要回到该值而不是 1 */
.section-head[data-reveal-head].in-view > .section-index {
  opacity: 0.28;
  transform: none;
}

.section-head[data-reveal-head].in-view .section-title,
.section-head[data-reveal-head].in-view .section-desc {
  opacity: 1;
  transform: none;
}

/* 容器内的卡片逐个浮现 */
[data-reveal-stagger] > * {
  opacity: 0;
  transform: translateY(26px);
  transition: opacity 0.7s ease, transform 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

[data-reveal-stagger].in-view > * {
  opacity: 1;
  transform: none;
}

[data-reveal-stagger] > *:nth-child(1) {
  transition-delay: 0ms;
}

[data-reveal-stagger] > *:nth-child(2) {
  transition-delay: 100ms;
}

[data-reveal-stagger] > *:nth-child(3) {
  transition-delay: 200ms;
}

[data-reveal-stagger] > *:nth-child(4) {
  transition-delay: 300ms;
}

/* 柱状图：卡片进入视口后柱子从底部逐根"长"出来 */
.chart-card[data-reveal] .bar-fill {
  transform: scaleY(0);
  transform-origin: bottom;
}

.chart-card[data-reveal].in-view .bar-fill {
  transform: scaleY(1);
}

.chart-card[data-reveal].in-view .bar-col:nth-child(1) .bar-fill {
  transition-delay: 0ms;
}

.chart-card[data-reveal].in-view .bar-col:nth-child(2) .bar-fill {
  transition-delay: 70ms;
}

.chart-card[data-reveal].in-view .bar-col:nth-child(3) .bar-fill {
  transition-delay: 140ms;
}

.chart-card[data-reveal].in-view .bar-col:nth-child(4) .bar-fill {
  transition-delay: 210ms;
}

.chart-card[data-reveal].in-view .bar-col:nth-child(5) .bar-fill {
  transition-delay: 280ms;
}

.chart-card[data-reveal].in-view .bar-col:nth-child(6) .bar-fill {
  transition-delay: 350ms;
}

.chart-card[data-reveal].in-view .bar-col:nth-child(7) .bar-fill {
  transition-delay: 420ms;
}

/* 环形图：进入视口才开始转动，离开即暂停 */
.chart-card[data-reveal] .breathing-circle,
.chart-card[data-reveal] .inner-circle {
  animation-play-state: paused;
}

.chart-card[data-reveal].in-view .breathing-circle,
.chart-card[data-reveal].in-view .inner-circle {
  animation-play-state: running;
}

/* ─────────────── 章节通用 ─────────────── */
.guide-section {
  padding-top: 72px;
  scroll-margin-top: 24px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 28px;
}

/* 章节序号：大号浅色数字，纯装饰 */
.section-index {
  font-size: 34px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -1px;
  color: var(--g-primary);
  opacity: 0.28;
}

.section-titles {
  flex: 1;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
}

.section-desc {
  font-size: 15px;
  line-height: 1.6;
  color: var(--g-text-sub);
}

/* ─────────────── 模拟窗口（伪浏览器窗口） ─────────────── */
.sim-frame {
  border-radius: 16px;
  overflow: hidden;
  background-color: var(--g-surface);
  border: 1px solid var(--g-line);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
}

.sim-frame:hover {
  box-shadow: 0 26px 60px rgba(0, 201, 167, 0.12);
}

.sim-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background-color: var(--g-fill);
  border-bottom: 1px solid var(--g-line);
}

.dots {
  display: flex;
  gap: 8px;
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

/* 地址栏：浅底 + 次要文字，明暗主题下都不会"发白" */
.address-bar {
  flex: 1;
  max-width: 320px;
  height: 24px;
  margin: 0 auto;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  color: var(--g-text-sub);
  background-color: var(--g-fill-strong);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 内嵌界面容器：横向超出时允许滚动，避免固定宽度被裁切 */
.sim-body {
  overflow-x: auto;
}

/* ─────────────── AI 聊天模拟器 ─────────────── */
.chat-layout {
  display: flex;
  flex-direction: column;
  height: 450px;
  background-color: var(--g-bg);
}

.chat-messages {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-row {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.chat-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #04211d;
  background: linear-gradient(135deg, #00c9a7 0%, #00e0c0 100%);
}

.chat-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  background-color: var(--g-surface);
  border: 1px solid var(--g-line);
}

.chat-row.user .chat-bubble {
  background-color: var(--g-primary);
  color: #04211d;
  border-color: transparent;
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
  border-top: 1px solid var(--g-line);
  background-color: var(--g-surface);
}

/* ─────────────── 数据分析图表 ─────────────── */
.chart-card {
  height: 300px;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  background-color: var(--g-surface);
  border: 1px solid var(--g-line);
}

.chart-title {
  padding: 16px;
  text-align: center;
  font-weight: 600;
}

.bar-chart {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  padding: 0 40px 24px;
}

.bar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 22px;
}

.bar-track {
  height: 160px;
  width: 100%;
  display: flex;
  align-items: flex-end;
}

.bar-fill {
  width: 100%;
  /* 从品牌色到亮青色的竖向渐变，柱体更有质感 */
  background: linear-gradient(to top, #00c9a7 0%, #00e0c0 100%);
  border-radius: 6px 6px 2px 2px;
  /* 高度由 :style 内联控制，生长动画改用 scaleY 缩放，交给滚动演示触发 */
  transform-origin: bottom;
  transition: transform 0.85s cubic-bezier(0.16, 1, 0.3, 1);
}

.bar-label {
  font-size: 12px;
  color: var(--g-text-sub);
}

/* 环形占比图 */
.ring-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding-bottom: 16px;
}

.breathing-circle {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  /* conic-gradient：按角度分配颜色，做出环形占比效果 */
  background: conic-gradient(#00c9a7 0% 65%, #2080f0 65% 85%, #f0a020 85% 100%);
  animation: spin 20s linear infinite;
  box-shadow: 0 0 30px rgba(0, 201, 167, 0.3);
}

.inner-circle {
  position: absolute;
  inset: 15px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  background-color: var(--g-surface);
  /* 反向旋转，保证中间文字始终正立 */
  animation: counterSpin 20s linear infinite;
}

.inner-circle small {
  font-size: 12px;
  font-weight: 400;
  color: var(--g-text-sub);
}

.legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--g-text-sub);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* ─────────────── 团队协作 ─────────────── */
.collab-card {
  border-radius: 16px;
  overflow: hidden;
  background-color: var(--g-surface);
  border: 1px solid var(--g-line);
}

.collab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--g-line);
  background-color: var(--g-fill);
}

.collab-title {
  font-size: 16px;
  font-weight: 600;
}

.collab-avatars {
  display: flex;
  align-items: center;
  gap: 10px;
}

.add-member-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--g-text-sub);
  cursor: pointer;
  border: 1px dashed var(--g-line-strong);
  transition: color 0.2s, border-color 0.2s;
}

.add-member-btn:hover {
  color: var(--g-primary);
  border-color: var(--g-primary);
}

.collab-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  background-color: var(--g-bg);
}

.comment-row {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.comment-row.right {
  align-self: flex-end;
  justify-content: flex-end;
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  background-color: var(--g-surface);
  border: 1px solid var(--g-line);
}

/* 自己的发言：品牌色浅底 + 去掉描边 */
.bubble.self {
  background-color: var(--g-primary-soft);
  border-color: transparent;
}

.bubble p {
  font-size: 14px;
  line-height: 1.5;
}

.user-name {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  color: var(--g-text-sub);
}

.collab-footer {
  display: flex;
  gap: 16px;
  padding: 12px 20px;
  font-size: 12px;
  color: var(--g-text-sub);
  background-color: var(--g-surface);
  border-top: 1px solid var(--g-line);
}

.permission-tag {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 实时同步小绿点 */
.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #00c9a7;
  box-shadow: 0 0 0 3px rgba(0, 201, 167, 0.18);
}

/* ─────────────── 个性化设置 ─────────────── */
.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.setting-card {
  padding: 28px 24px;
  text-align: center;
  border-radius: 16px;
  background-color: var(--g-surface);
  border: 1px solid var(--g-line);
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
}

.setting-card:hover {
  transform: translateY(-4px);
  border-color: rgba(0, 201, 167, 0.35);
  box-shadow: 0 16px 40px rgba(0, 201, 167, 0.1);
}

/* 可点击的卡片（主题卡） */
.setting-card.clickable {
  cursor: pointer;
  user-select: none;
}

.card-icon {
  margin-bottom: 14px;
  color: var(--g-primary);
}

.setting-card h3 {
  margin-bottom: 8px;
  font-size: 18px;
  font-weight: 600;
}

.setting-card p {
  margin-bottom: 22px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--g-text-sub);
}

/* 主题开关：轨道 + 圆形滑块，on 时滑块右移并变成品牌色 */
.theme-toggle {
  position: relative;
  width: 64px;
  height: 32px;
  margin: 0 auto;
  border-radius: 999px;
  background-color: var(--g-fill-strong);
  border: 1px solid var(--g-line);
  transition: background 0.3s, border-color 0.3s;
}

.theme-toggle.on {
  background-color: var(--g-primary-soft);
  border-color: rgba(0, 201, 167, 0.4);
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8a939c;
  background-color: var(--g-surface);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  transition: left 0.3s ease, color 0.3s;
}

.theme-toggle.on .toggle-thumb {
  left: 34px;
  color: #04211d;
  background-color: var(--g-primary);
}

.shield-icon {
  color: var(--g-primary);
  opacity: 0.75;
  transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275), opacity 0.3s;
}

.setting-card:hover .shield-icon {
  transform: scale(1.15);
  opacity: 1;
}

/* ─────────────── 快捷功能 ─────────────── */
.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 22px;
  border-radius: 14px;
  background-color: var(--g-surface);
  border: 1px solid var(--g-line);
  transition: border-color 0.3s, transform 0.3s;
}

.shortcut-item:hover {
  transform: translateY(-3px);
  border-color: var(--g-primary);
}

.key-combo {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 键帽：完全基于主题变量，明暗主题自动适配（无需再写属性选择器 hack） */
kbd {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
  color: var(--g-text);
  background-color: var(--g-fill-strong);
  border: 1px solid var(--g-line-strong);
  border-bottom-width: 2px;
}

.plus {
  font-size: 12px;
  color: var(--g-text-muted);
}

.shortcut-desc {
  font-size: 14px;
  color: var(--g-text-sub);
}

/* ─────────────── 页脚 ─────────────── */
.page-footer {
  margin-top: 80px;
  padding-top: 28px;
  border-top: 1px solid var(--g-line);
  text-align: center;
  font-size: 13px;
  color: var(--g-text-muted);
}

/* ═══════════════════════════════════════════════
   高级感增强：极光背景 / 卡片光晕 / 霓虹描边
   色相全部取自品牌色 #00c9a7（--g-primary），
   只在明暗主题间调整透明度，因此切换主题不会出现"突兀的绿"。
   ═══════════════════════════════════════════════ */

/* ── 1. 极光背景 ──
   两个 z-index:-1 的伪元素落在页面底色之上、内容之下；
   用 blur() 把纯色圆斑虚化成柔和光团，再用 keyframes 缓慢平移缩放，
   形成"极光缓缓流动"的观感。blur 只作用于光团本身，不影响页面清晰度。 */
.guide-page::before,
.guide-page::after {
  content: '';
  position: absolute;
  z-index: -1;
  border-radius: 50%;
  filter: blur(90px);
  pointer-events: none;
  /* 提前提升为合成层，动画期间不会反复重绘整页 */
  will-change: transform;
}

/* 青绿光团：右上角起，缓慢向左下漂移并放大 */
.guide-page::before {
  width: 52vw;
  height: 52vw;
  top: -18vw;
  left: 34vw;
  background: radial-gradient(circle, rgba(0, 201, 167, 0.2), transparent 66%);
  animation: auroraDriftA 18s ease-in-out infinite alternate;
}

/* 蓝色光团：左下角起，向右上漂移，与主色形成冷暖对比、层次更立体 */
.guide-page::after {
  width: 46vw;
  height: 46vw;
  bottom: -20vw;
  left: -10vw;
  background: radial-gradient(circle, rgba(32, 128, 240, 0.16), transparent 66%);
  animation: auroraDriftB 24s ease-in-out infinite alternate;
}

/* 暗色主题底色接近纯黑，需要更高的光团不透明度才"看得见" */
:global(html.dark-theme) .guide-page::before {
  background: radial-gradient(circle, rgba(0, 201, 167, 0.3), transparent 66%);
}

:global(html.dark-theme) .guide-page::after {
  background: radial-gradient(circle, rgba(32, 128, 240, 0.24), transparent 66%);
}

@keyframes auroraDriftA {
  from {
    transform: translate3d(0, 0, 0) scale(1);
  }

  to {
    transform: translate3d(-12vw, 10vw, 0) scale(1.18);
  }
}

@keyframes auroraDriftB {
  from {
    transform: translate3d(0, 0, 0) scale(1.1);
  }

  to {
    transform: translate3d(14vw, -8vw, 0) scale(1);
  }
}

/* ── 2. 卡片鼠标跟随光晕 ──
   --mx/--my 由脚本实时写入（默认兜底在中心），
   radial-gradient 以它为圆心画一层品牌色柔光，hover 时才浮现。 */
.sim-frame,
.chart-card,
.collab-card,
.setting-card,
.shortcut-item {
  /* 光晕是绝对定位的伪元素，需要卡片自身作为定位参照 */
  position: relative;
}

.sim-frame::after,
.chart-card::after,
.collab-card::after,
.setting-card::after,
.shortcut-item::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  /* 覆盖在卡片内容之上但不可点击，避免遮挡内部交互 */
  z-index: 3;
  opacity: 0;
  transition: opacity 0.35s ease;
  background: radial-gradient(260px circle at var(--mx, 50%) var(--my, 50%),
      rgba(0, 201, 167, 0.16), transparent 62%);
}

.sim-frame:hover::after,
.chart-card:hover::after,
.collab-card:hover::after,
.setting-card:hover::after,
.shortcut-item:hover::after {
  opacity: 1;
}

/* ── 3. 霓虹描边 ──
   hover 时用 1px 内描边 + 品牌色投影，让卡片边缘"亮起来"。
   投影用低透明度多层叠加，比单纯加粗边框更通透。 */
.sim-frame:hover,
.chart-card:hover,
.collab-card:hover,
.setting-card:hover,
.shortcut-item:hover {
  border-color: rgba(0, 201, 167, 0.4);
  box-shadow: 0 0 0 1px rgba(0, 201, 167, 0.18), 0 18px 46px rgba(0, 201, 167, 0.16);
}

/* ─────────────── 动画 ─────────────── */
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

/* 环形图整体顺时针旋转 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

/* 内圈反向旋转，让文字保持正立 */
@keyframes counterSpin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(-360deg);
  }
}

/* Hero 标题流光：背景图横向平移一个平铺周期（240%），首尾同色故无缝循环 */
@keyframes titleShine {
  from {
    background-position: 0% center;
  }

  to {
    background-position: 240% center;
  }
}

/* 尊重系统「减弱动态效果」设置：直接显示内容，不播放滚动动画 */
@media (prefers-reduced-motion: reduce) {

  [data-reveal],
  [data-reveal-stagger] > *,
  [data-reveal-head] .section-title,
  [data-reveal-head] .section-desc {
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }

  [data-reveal-head] > .section-index {
    opacity: 0.28 !important;
    transform: none !important;
    transition: none !important;
  }

  .sim-frame[data-reveal],
  .chart-card[data-reveal] .bar-fill {
    transform: none !important;
  }

  .chart-card[data-reveal] .breathing-circle,
  .chart-card[data-reveal] .inner-circle {
    animation-play-state: running;
  }

  /* 极光光团与标题流光同样停掉，只保留静态渐变 */
  .guide-page::before,
  .guide-page::after {
    animation: none !important;
  }

  .hero-title {
    animation: none !important;
    background-position: 0% center;
  }
}

/* ─────────────── 响应式 ─────────────── */
@media (max-width: 1280px) {
  .content-inner {
    padding: 48px 32px 100px;
  }
}

/* 窄屏隐藏左侧导航，内容区占满 */
@media (max-width: 900px) {
  .guide-nav {
    display: none;
  }

  .content-inner {
    padding: 32px 20px 80px;
  }

  .hero-title {
    font-size: 32px;
  }

  .section-title {
    font-size: 20px;
  }
}
</style>