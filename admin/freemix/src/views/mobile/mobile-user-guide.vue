<template>
  <!-- 直接应用暗黑模式类名，或者保留 isDark 逻辑 -->
  <div class="premium-guide-container dark-theme">
    <div class="premium-guide">
      
      <!-- 1. 沉浸式顶部导航 -->
      <van-nav-bar
        left-arrow
        @click-left="goBack"
        :border="false"
        class="glass-nav-bar"
        :safe-area-inset-top="true"
      >
        <template #title>
          <span class="nav-title">用户指南</span>
        </template>
      </van-nav-bar>

      <!-- 2. 主要内容区域 (带动画切换) -->
      <div class="content-viewport">
        <transition name="fade-slide" mode="out-in">
          
          <!-- 场景一：欢迎页面 -->
          <div v-if="currentSection === 'welcome'" key="welcome" class="content-card welcome-card">
            <div class="hero-section">
              <div class="avatar-glow"></div>
              <!-- <van-image
                src="https://api.dicebear.com/7.x/miniavs/svg?seed=welcome"
                width="120"
                height="120"
                round
                class="hero-image"
              /> -->
              <van-image
                src="/icons/icon.png"
                width="120"
                height="120"
                
                class="hero-image"
              />
            </div>
            <h1 class="main-title">欢迎来到 FreeMix</h1>
            <p class="subtitle">
              您的个人效能加速器。<br>
              在暗夜中，点亮目标达成之旅。
            </p>
            <div class="welcome-action">
              <van-button 
                type="primary" 
                block 
                round 
                size="large" 
                class="premium-btn shadow-btn"
                @click="nextSection"
              >
                开启探索
              </van-button>
            </div>
          </div>

          <!-- 场景二：创建目标 -->
          <div v-else-if="currentSection === 'create'" key="create" class="content-card">
            <div class="card-header">
              <h2>第一步：创建目标</h2>
              <p>万事开头难，但这里很简单</p>
            </div>
            
            <div class="step-container">
              <!-- active-color 改为适合暗黑模式的亮蓝色 -->
              <van-steps direction="vertical" :active="createStep" active-color="#0A84FF" inactive-color="#3A3A3C">
                <van-step>
                  <h3>点击添加</h3>
                  <p>主页右下角「+」悬浮按钮</p>
                </van-step>
                <van-step>
                  <h3>基本信息</h3>
                  <p>好的标题是成功的一半</p>
                </van-step>
                <van-step>
                  <h3>设定死线 (Deadline)</h3>
                  <p>产生紧迫感，拒绝拖延</p>
                </van-step>
                <van-step>
                  <h3>指派/协作</h3>
                  <p>独行快，众行远</p>
                </van-step>
                <van-step>
                  <h3>优先级</h3>
                  <p>要事第一 (Eisenhower矩阵)</p>
                </van-step>
              </van-steps>
            </div>

            <div class="inner-actions">
              <van-button 
                plain 
                round 
                size="small" 
                class="ghost-btn"
                @click="prevStep" 
                :disabled="createStep === 0"
              >上一步</van-button>
              <van-button 
                type="primary" 
                round 
                size="small" 
                class="premium-btn inner-next-btn"
                @click="nextStep"
              >
                {{ createStep === 4 ? '完成演示' : '下一步' }}
              </van-button>
            </div>
          </div>

          <!-- 场景三：跟踪进度 -->
          <div v-else-if="currentSection === 'track'" key="track" class="content-card">
            <div class="card-header">
              <h2>全方位跟踪</h2>
              <p>多视图切换，掌控全局</p>
            </div>
            
            <van-tabs v-model:active="activeTab" animated swipeable background="transparent" color="#0A84FF" title-active-color="#FFFFFF" title-inactive-color="#8E8E93">
              <van-tab title="仪表板">
                <div class="tab-inner">
                  <div class="feature-box">
                    <van-icon name="chart-trending-o" class="feature-icon" />
                    <div>
                      <strong>数据概览</strong>
                      <p>实时查看进行中、已完成及逾期目标。</p>
                    </div>
                  </div>
                  <van-image
                    src="https://api.dicebear.com/7.x/shapes/svg?seed=dashboard"
                    radius="12"
                    class="demo-img"
                  />
                </div>
              </van-tab>
              <van-tab title="列表">
                <div class="tab-inner">
                  <div class="feature-box">
                    <van-icon name="orders-o" class="feature-icon" />
                    <div>
                      <strong>清单管理</strong>
                      <p>通过进度条直观感受距离成功的距离。</p>
                    </div>
                  </div>
                </div>
              </van-tab>
              <van-tab title="详情">
                <div class="tab-inner">
                  <div class="feature-box">
                    <van-icon name="bullhorn-o" class="feature-icon" />
                    <div>
                      <strong>深度复盘</strong>
                      <p>更新进度、添加备注、拆解子任务。</p>
                    </div>
                  </div>
                </div>
              </van-tab>
            </van-tabs>
          </div>

          <!-- 场景四：数据分析 -->
          <div v-else-if="currentSection === 'analyze'" key="analyze" class="content-card">
            <div class="card-header">
              <h2>数据洞察</h2>
              <p>让数据指导你的成长</p>
            </div>
            
            <div class="collapse-wrapper">
              <van-collapse v-model="activeCollapse" :border="false">
                <van-collapse-item name="efficiency" :border="false" class="premium-collapse-item">
                  <template #title>
                    <div class="collapse-title">
                      <van-icon name="fire-o" color="#FF9F0A" /> 效率分析
                    </div>
                  </template>
                  <p>识别高效时间段，合理分配精力。</p>
                </van-collapse-item>
                
                <van-collapse-item name="types" :border="false" class="premium-collapse-item">
                  <template #title>
                    <div class="collapse-title">
                      <van-icon name="label-o" color="#64D2FF" /> 维度分布
                    </div>
                  </template>
                  <p>平衡工作、生活与个人成长。</p>
                </van-collapse-item>
                
                <van-collapse-item name="time" :border="false" class="premium-collapse-item">
                  <template #title>
                    <div class="collapse-title">
                      <van-icon name="clock-o" color="#FF453A" /> 时间管理
                    </div>
                  </template>
                  <p>提升对任务耗时的预估能力。</p>
                </van-collapse-item>
              </van-collapse>
            </div>
          </div>

          <!-- 场景五：协作 -->
          <div v-else-if="currentSection === 'collaborate'" key="collaborate" class="content-card">
            <div class="card-header">
              <h2>团队协作</h2>
              <p>连接你我，共创未来</p>
            </div>
            <div class="list-cards">
              <div class="list-item">
                <div class="icon-sq bg-blue"><van-icon name="manager" /></div>
                <div class="text">
                  <h4>指定负责人</h4>
                  <p>责任到人，避免推诿</p>
                </div>
              </div>
              <div class="list-item">
                <div class="icon-sq bg-green"><van-icon name="friends" /></div>
                <div class="text">
                  <h4>多方协作</h4>
                  <p>邀请伙伴共同推进目标</p>
                </div>
              </div>
              <div class="list-item">
                <div class="icon-sq bg-orange"><van-icon name="comment" /></div>
                <div class="text">
                  <h4>透明沟通</h4>
                  <p>备注与评论，信息实时同步</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 场景六：高级技巧 -->
          <div v-else-if="currentSection === 'advanced'" key="advanced" class="content-card advanced-card">
            
            <h2>准备就绪！</h2>
            <p class="final-text">掌握分解技巧与优先级管理，<br>你已经比 90% 的人更高效。</p>
            
            <div class="final-tips">
              <span> 目标分解</span>
              <span> 优先级</span>
              <span> 可视化</span>
            </div>

            <div class="final-action">
              <van-button type="primary" block round size="large" class="premium-btn shadow-btn" @click="goToHome">
                进入 FreeMix 主页
              </van-button>
            </div>
          </div>

        </transition>
      </div>

      <!-- 3. 悬浮底部导航 -->
      <div class="bottom-dock" v-if="renderBottomNav">
        <div class="progress-track">
          <div class="progress-bar" :style="{ width: progressPercentage + '%' }"></div>
        </div>
        
        <div class="dock-controls">
          <van-button 
            round 
            icon="arrow-left" 
            class="control-btn icon-only" 
            @click="prevSection" 
          />
          
          <div class="page-indicator">{{ currentPage }} / {{ totalSections }}</div>
          
          <van-button 
            type="primary" 
            round 
            class="control-btn premium-btn grow-btn" 
            @click="nextSection"
          >
            下一章 <van-icon name="arrow" />
          </van-button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/hooks/useUser'
import { useSettings } from '@/hooks/useSettings'

// 路由和状态
const router = useRouter()
const { user } = useUser()
const { isDark } = useSettings()

const currentSection = ref('welcome')
const createStep = ref(0)
const activeCollapse = ref(['efficiency'])
const activeTab = ref(0)

const sections = [
  'welcome',
  'create', 
  'track',
  'analyze',
  'collaborate',
  'advanced'
]

// 计算属性
const totalSections = computed(() => sections.length)
const currentPage = computed(() => sections.indexOf(currentSection.value) + 1)
const progressPercentage = computed(() => (currentPage.value / totalSections.value) * 100)

// 控制底部导航显示
const renderBottomNav = computed(() => {
  return currentSection.value !== 'welcome' && currentSection.value !== 'advanced'
})

// 方法
const goBack = () => router.back()
const goToHome = () => router.push('/home')

const nextSection = () => {
  const currentIndex = sections.indexOf(currentSection.value)
  if (currentIndex < sections.length - 1) {
    currentSection.value = sections[currentIndex + 1]
    if (currentSection.value === 'create') createStep.value = 0
  }
}

const prevSection = () => {
  const currentIndex = sections.indexOf(currentSection.value)
  if (currentIndex > 0) {
    currentSection.value = sections[currentIndex - 1]
  }
}

const nextStep = () => {
  if (createStep.value < 4) {
    createStep.value++
  } else {
    nextSection()
  }
}

const prevStep = () => {
  if (createStep.value > 0) {
    createStep.value--
  }
}

onMounted(() => {
  // Init logic
})
</script>

<style scoped>
/* 
  CSS 变量定义 - 强制暗黑主题 (Premium Dark)
  参考 iOS Dark Mode 规范
*/
.premium-guide-container {
  /* 基础背景色 - 纯黑 (OLED优化) */
  --bg-color: #000000;
  
  /* 卡片背景色 - 浅黑/深灰 */
  --card-bg: #1C1C1E;
  
  /* 辅助背景色 */
  --dock-bg: #2C2C2E;
  
  /* 文字颜色 */
  --text-primary: #FFFFFF;
  --text-secondary: #98989D;
  
  /* 强调色 - iOS Dark Mode Blue */
  --accent-color: #0A84FF;
  
  /* 阴影 - 暗色模式下更微妙或使用光晕 */
  --shadow-card: 0 0 0 1px rgba(255, 255, 255, 0.05), 0 8px 24px rgba(0, 0, 0, 0.4);
  --shadow-btn: 0 4px 12px rgba(10, 132, 255, 0.4);
  
  /* 线条颜色 */
  --step-line: #38383A;
  
  /* 按钮背景 */
  --btn-ghost-bg: rgba(255, 255, 255, 0.1);
}

/* 基础布局 */
.premium-guide {
  min-height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 顶部导航毛玻璃 — 统一风格 */
.glass-nav-bar {
  background: rgba(28, 28, 30, 0.75);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  position: sticky;
  top: 0;
  z-index: 10;
}

.nav-title {
  font-weight: 600;
  color: var(--text-primary);
}
/* 覆盖 Vant 导航栏图标颜色 */
:deep(.van-nav-bar .van-icon) {
  color: var(--accent-color);
}
:deep(.van-nav-bar__text) {
  color: var(--accent-color);
}

/* 内容可视区 */
.content-viewport {
  flex: 1;
  padding: 16px 20px 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow-x: hidden;
}

/* 通用卡片样式 - 深色高级感 */
.content-card {
  background: var(--card-bg);
  border-radius: 24px;
  padding: 30px 24px;
  box-shadow: var(--shadow-card);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.05); /* 微弱的边框增加质感 */
}

.card-header {
  margin-bottom: 24px;
  text-align: left;
}
.card-header h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px 0;
  /* 渐变白字 */
  background: linear-gradient(135deg, #FFFFFF 0%, #E5E5EA 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: #FFFFFF;
}
.card-header p {
  color: var(--text-secondary);
  font-size: 15px;
  margin: 0;
}

/* 欢迎页样式 */
.welcome-card {
  text-align: center;
  border: none;
  background: transparent;
  box-shadow: none;
  padding-top: 10px;
}
.hero-section {
  position: relative;
  display: inline-block;
  margin-bottom: 30px;
}
/* 暗黑模式特供：光晕更亮 */
.avatar-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140px;
  height: 140px;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  filter: blur(40px);
  border-radius: 50%;
  opacity: 0.6; /* 提高不透明度 */
  z-index: 0;
}
.hero-image {
  position: relative;
  z-index: 1;

}
.main-title {
  font-size: 28px;
  margin-bottom: 12px;
  color: var(--text-primary);
  text-shadow: 0 2px 10px rgba(0,0,0,0.5);
}
.subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 40px;
}

/* 步骤条深度定制 */
.step-container {
  margin: 20px 0;
  background: transparent;
  border: none;
}
:deep(.van-step--vertical) {
  padding: 10px 10px 10px 0;
}
:deep(.van-step--vertical::after) {
  border-bottom-width: 0;
}
/* 步骤标题颜色 */
:deep(.van-step__title h3) {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
/* 步骤描述颜色 */
:deep(.van-step__title p) {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.inner-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}
.inner-next-btn {
  padding: 0 24px;
}
/* 幽灵按钮样式 */
.ghost-btn {
  background: var(--btn-ghost-bg) !important;
  color: var(--text-primary) !important;
  border: none !important;
}

/* Tabs 内容区 */
.tab-inner {
  padding: 20px 0 0;
  min-height: 200px;
}
.feature-box {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}
.feature-icon {
  font-size: 28px;
  color: var(--accent-color);
  padding: 10px;
  background: rgba(10, 132, 255, 0.15); /* 更透的蓝色背景 */
  border-radius: 12px;
}
.feature-box strong {
  display: block;
  font-size: 16px;
  margin-bottom: 4px;
  color: var(--text-primary);
}
.feature-box p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
}
.demo-img {
  width: 100%;
  border: 1px solid rgba(255,255,255,0.1);
}

/* 折叠面板 (Collapse) 定制 */
.collapse-wrapper {
  margin: 0 -16px; 
}
.premium-collapse-item {
  margin-bottom: 12px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255,255,255,0.05); /* 每个条目微弱背景 */
}
.collapse-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
}
/* 覆盖 Vant Cell 背景 */
:deep(.van-cell) {
  background: transparent;
  padding: 16px;
  color: var(--text-primary);
}
:deep(.van-cell::after) {
  display: none;
}
:deep(.van-collapse-item__content) {
  background: transparent;
  color: var(--text-secondary);
  line-height: 1.6;
  padding-top: 0;
}

/* 协作列表 */
.list-cards .list-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(44, 44, 46, 0.6); /* 半透明深灰 */
  border-radius: 16px;
  margin-bottom: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}
.icon-sq {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}
.bg-blue { background: #5E5CE6; }
.bg-green { background: #30D158; }
.bg-orange { background: #FF9F0A; }
.list-item .text h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: var(--text-primary);
}
.list-item .text p {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
}

/* 完成页 */
.advanced-card {
  text-align: center;
}
.congrats-icon {
  font-size: 60px;
  margin-bottom: 20px;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.2));
}
.final-text {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 30px;
}
.final-tips {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 40px;
}
.final-tips span {
  padding: 6px 12px;
  background: rgba(10, 132, 255, 0.15);
  color: var(--accent-color);
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid rgba(10, 132, 255, 0.2);
}

/* 底部悬浮 Dock */
.bottom-dock {
  position: fixed;
  bottom: 30px;
  left: 20px;
  right: 20px;
  background: var(--dock-bg);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.5); /* 更重的阴影 */
  padding: 6px;
  z-index: 99;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(255,255,255,0.08); /* 增加边缘光感 */
}
.progress-track {
  height: 3px;
  background: rgba(255,255,255,0.1);
  border-radius: 3px;
  margin: 10px 16px 0;
  overflow: hidden;
}
.progress-bar {
  height: 100%;
  background: var(--accent-color);
  transition: width 0.3s ease;
  box-shadow: 0 0 10px var(--accent-color); /* 进度条发光 */
}
.dock-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 10px;
}
.page-indicator {
  font-size: 13px;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
  font-weight: 500;
}
.control-btn {
  height: 44px;
  border: none;
  background: rgba(255,255,255,0.1);
  color: var(--text-primary);
}
.premium-btn {
  background: var(--accent-color);
  border: none;
  color: #fff;
}
.shadow-btn {
  box-shadow: var(--shadow-btn);
}
.grow-btn {
  padding: 0 24px;
  min-width: 120px;
}

/* 动画定义 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>