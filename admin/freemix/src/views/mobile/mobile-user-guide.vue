<template>
  <van-config-provider :theme="currentTheme">
    <div class="mobile-user-guide">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="用户指南"
        left-text="返回"
        left-arrow
        @click-left="goBack"
      />

      <!-- 指南内容 -->
      <div class="guide-content">
        <!-- 欢迎页面 -->
        <van-section v-if="currentSection === 'welcome'" title="欢迎使用FreeMix">
          <div class="welcome-content">
            <van-image
              src="https://api.dicebear.com/7.x/miniavs/svg?seed=welcome"
              width="100"
              height="100"
              round
              class="welcome-image"
            />
            <p class="welcome-text">
              FreeMix是一款功能强大的目标管理系统，帮助您设定、跟踪并完成个人和职业目标。
            </p>
            <van-button type="primary" block round @click="nextSection">
              开始使用
            </van-button>
          </div>
        </van-section>

        <!-- 创建目标 -->
        <van-section v-else-if="currentSection === 'create'" title="创建目标">
          <van-steps direction="vertical" :active="createStep">
            <van-step>
              <h3>点击添加按钮</h3>
              <p>在主页点击"添加新目标"按钮，进入目标创建页面。</p>
              <van-image
                src="https://api.dicebear.com/7.x/miniavs/svg?seed=step1"
                width="200"
                class="step-image"
              />
            </van-step>
            <van-step>
              <h3>填写基本信息</h3>
              <p>输入目标的标题和详细描述，确保描述清晰明确。</p>
            </van-step>
            <van-step>
              <h3>设置截止日期</h3>
              <p>为您的目标设置一个合理的截止日期，这有助于您保持专注。</p>
            </van-step>
            <van-step>
              <h3>指定负责人</h3>
              <p>如果这是一个团队目标，请指定负责人。</p>
            </van-step>
            <van-step>
              <h3>设置优先级</h3>
              <p>根据重要性设置优先级：低、中、高或紧急。</p>
            </van-step>
          </van-steps>
          
          <div class="step-actions">
            <van-button size="small" @click="prevStep">上一步</van-button>
            <van-button size="small" type="primary" @click="nextStep">
              {{ createStep === 4 ? '完成' : '下一步' }}
            </van-button>
          </div>
        </van-section>

        <!-- 跟踪进度 -->
        <van-section v-else-if="currentSection === 'track'" title="跟踪进度">
          <van-tabs>
            <van-tab title="仪表板视图">
              <div class="tab-content">
                <p>在主页仪表板上，您可以一目了然地看到所有目标的状态：</p>
                <ul>
                  <li><strong>目标概览</strong>：显示进行中、已完成和已过期的目标数量</li>
                  <li><strong>近期目标</strong>：按时间顺序展示即将到期的目标</li>
                  <li><strong>完成趋势</strong>：展示目标完成的历史趋势</li>
                </ul>
                <van-image
                  src="https://api.dicebear.com/7.x/miniavs/svg?seed=dashboard"
                  width="200"
                  class="example-image"
                />
              </div>
            </van-tab>
            <van-tab title="列表视图">
              <div class="tab-content">
                <p>在"我的目标"区域可以查看所有已创建的目标：</p>
                <ul>
                  <li>点击任意目标卡片可以查看和编辑详细信息</li>
                  <li>通过进度条直观了解目标完成情况</li>
                  <li>目标状态会自动更新为"进行中"、"已完成"或"已过期"</li>
                </ul>
              </div>
            </van-tab>
            <van-tab title="详情视图">
              <div class="tab-content">
                <p>在目标详情页面，您可以：</p>
                <ul>
                  <li>更新目标进度</li>
                  <li>添加备注和心得</li>
                  <li>修改目标信息</li>
                  <li>查看子目标完成情况</li>
                </ul>
              </div>
            </van-tab>
          </van-tabs>
        </van-section>

        <!-- 数据分析 -->
        <van-section v-else-if="currentSection === 'analyze'" title="数据分析">
          <van-collapse v-model="activeCollapse">
            <van-collapse-item title="目标完成效率分析" name="efficiency">
              <p>通过观察完成趋势图，您可以：</p>
              <ul>
                <li>识别自己的高效期和低效期</li>
                <li>调整目标设定策略</li>
                <li>合理安排工作和休息时间</li>
              </ul>
            </van-collapse-item>
            <van-collapse-item title="目标类型分析" name="types">
              <p>通过标签分类，您可以：</p>
              <ul>
                <li>了解自己在不同领域投入的时间和精力</li>
                <li>平衡各类目标的比重</li>
                <li>发现自己的兴趣和优势领域</li>
              </ul>
            </van-collapse-item>
            <van-collapse-item title="时间管理分析" name="time">
              <p>通过截止日期分析，您可以：</p>
              <ul>
                <li>评估自己设定目标的合理性</li>
                <li>改进时间预估能力</li>
                <li>减少目标过期的情况</li>
              </ul>
            </van-collapse-item>
          </van-collapse>
        </van-section>

        <!-- 协作功能 -->
        <van-section v-else-if="currentSection === 'collaborate'" title="协作功能">
          <van-cell-group>
            <van-cell title="指定负责人" label="创建团队目标时，需要明确指定负责人" />
            <van-cell title="设置协作者" label="可以为目标添加多个协作者" />
            <van-cell title="定期同步" label="建立定期会议机制，同步目标进度" />
            <van-cell title="透明沟通" label="使用备注功能记录重要信息" />
          </van-cell-group>
        </van-section>

        <!-- 高级技巧 -->
        <van-section v-else-if="currentSection === 'advanced'" title="高级技巧">
          <van-cell-group>
            <van-cell title="目标分解技巧" label="将大目标分解为小的、可执行的子目标" />
            <van-cell title="优先级管理" label="使用Eisenhower矩阵来管理目标优先级" />
            <van-cell title="进度可视化" label="利用系统的图表功能进行进度可视化" />
          </van-cell-group>
          
          <div class="advanced-actions">
            <van-button type="primary" block round @click="goToHome">
              开始使用FreeMix
            </van-button>
          </div>
        </van-section>
      </div>

      <!-- 底部导航 -->
      <div class="bottom-navigation">
        <van-button 
          size="small" 
          @click="prevSection" 
          :disabled="currentSection === 'welcome'"
        >
          <van-icon name="arrow-left" />
          上一章节
        </van-button>
        
        <van-pagination
          v-model="currentPage"
          :page-count="totalSections"
          :show-page-size="3"
        />
        
        <van-button 
          size="small" 
          type="primary" 
          @click="nextSection"
          :disabled="currentSection === 'advanced'"
        >
          下一章节
          <van-icon name="arrow" />
        </van-button>
      </div>
    </div>
  </van-config-provider>
</template>
<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/hooks/useUser'
import { useSettings } from '@/hooks/useSettings'

// 路由和状态管理
const router = useRouter()
const { user } = useUser()
const { isDark } = useSettings()

// 主题配置

// 响应式数据
const currentSection = ref('welcome')
const createStep = ref(0)
const activeCollapse = ref(['efficiency'])
const currentPage = ref(1)

const totalSections = 6
const sections = [
  'welcome',
  'create', 
  'track',
  'analyze',
  'collaborate',
  'advanced'
]

// 方法
const goBack = () => {
  router.back()
}

const goToHome = () => {
  router.push('/home')
}

const nextSection = () => {
  const currentIndex = sections.indexOf(currentSection.value)
  if (currentIndex < sections.length - 1) {
    currentSection.value = sections[currentIndex + 1]
    currentPage.value = currentIndex + 2
  }
}

const prevSection = () => {
  const currentIndex = sections.indexOf(currentSection.value)
  if (currentIndex > 0) {
    currentSection.value = sections[currentIndex - 1]
    currentPage.value = currentIndex
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

// 生命周期
onMounted(() => {
  // 初始化页面
})
</script>

<style scoped>
.mobile-user-guide {
  min-height: 100vh;
  background-color: var(--van-background-color);
  display: flex;
  flex-direction: column;
}

.guide-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.welcome-content {
  text-align: center;
  padding: 20px;
}

.welcome-image {
  margin-bottom: 20px;
}

.welcome-text {
  font-size: 16px;
  line-height: 1.6;
  color: var(--van-text-color-2);
  margin-bottom: 30px;
}

.step-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  padding: 0 16px;
}

.tab-content {
  padding: 16px 0;
}

.tab-content p {
  margin-bottom: 12px;
  color: var(--van-text-color);
}

.tab-content ul {
  margin: 12px 0;
  padding-left: 20px;
}

.tab-content li {
  margin-bottom: 8px;
  color: var(--van-text-color-2);
}

.step-image,
.example-image {
  display: block;
  margin: 16px auto;
  border-radius: 8px;
}

.advanced-actions {
  margin-top: 30px;
  padding: 0 16px;
}

.bottom-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: var(--van-background-color-light);
  border-top: 1px solid var(--van-border-color);
}

/* 深色主题适配 */
:deep(.van-theme-dark) {
  --van-background-color: #1a1a1a;
  --van-background-color-light: #2a2a2a;
  --van-text-color: #f5f5f5;
  --van-text-color-2: #a0a0a0;
}

:deep(.van-section) {
  background-color: var(--van-background-color-light);
}

:deep(.van-cell-group) {
  background-color: var(--van-background-color-light);
}

:deep(.van-cell) {
  background-color: var(--van-background-color-light);
}

:deep(.van-collapse-item__content) {
  background-color: var(--van-background-color-light);
}
</style>
