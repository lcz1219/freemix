<template>
  <n-layout has-sider class="user-guide-layout">
    <!-- 左侧导航栏 -->
    <n-layout-sider
      class="guide-sidebar"
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="260"
      :collapsed="sidebarCollapsed"
      show-trigger
      @collapse="sidebarCollapsed = true"
      @expand="sidebarCollapsed = false"
    >
      <!-- 左侧导航菜单 -->
        <div class="sidebar-header">
          <h3>功能导航</h3>
        </div>
        <n-menu
          v-model:value="activeMenu"
          :options="menuOptions"
          :collapsed="false"
          :collapsed-width="0"
          :indent="20"
          @update:value="handleMenuSelect"
        />
        
        <!-- 核心功能快捷入口 -->
        <div class="core-features-section">
          <div class="core-features-header">
            <h4>核心功能</h4>
            <p>快速访问主要功能</p>
          </div>
          <div class="core-features-grid">
            <div class="core-feature-item" @click="navigateTo('auth')">
              <div class="core-feature-icon">
                <n-icon size="20" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><PersonCircleOutline /></n-icon>
              </div>
              <div class="core-feature-content">
                <span>用户认证</span>
              </div>
            </div>
            <div class="core-feature-item" @click="navigateTo('goals')">
              <div class="core-feature-icon">
                <n-icon size="20" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><FlagOutline /></n-icon>
              </div>
              <div class="core-feature-content">
                <span>目标管理</span>
              </div>
            </div>
            <div class="core-feature-item" @click="navigateTo('ai')">
              <div class="core-feature-icon">
                <n-icon size="20" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><LogoReddit /></n-icon>
              </div>
              <div class="core-feature-content">
                <span>AI助手</span>
              </div>
            </div>
            <div class="core-feature-item" @click="navigateTo('statistics')">
              <div class="core-feature-icon">
                <n-icon size="20" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><BarChartOutline /></n-icon>
              </div>
              <div class="core-feature-content">
                <span>数据分析</span>
              </div>
            </div>
            <div class="core-feature-item" @click="navigateTo('collaboration')">
              <div class="core-feature-icon">
                <n-icon size="20" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><PeopleOutline /></n-icon>
              </div>
              <div class="core-feature-content">
                <span>团队协作</span>
              </div>
            </div>
            <div class="core-feature-item" @click="navigateTo('settings')">
              <div class="core-feature-icon">
                <n-icon size="20" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><SettingsOutline /></n-icon>
              </div>
              <div class="core-feature-content">
                <span>个性化设置</span>
              </div>
            </div>
          </div>
        </div>
        <div class="sidebar-footer">
          <div class="help-section">
            <h4>需要帮助？</h4>
            <p>联系客服或查看文档获取更多支持</p>
            <n-button type="primary" size="small" class="help-button" :color="isDark ? '#00c9a7' : '#008c72'">
              <template #icon>
                <n-icon><HelpCircleOutline /></n-icon>
              </template>
              获取帮助
            </n-button>
          </div>
        </div>
    </n-layout-sider>

    <!-- 右侧内容区域 -->
    <n-layout-content class="guide-main">
      <!-- <div class="content-header">
        <n-breadcrumb>
          <n-breadcrumb-item @click="activeMenu = 'welcome'">首页</n-breadcrumb-item>
          <n-breadcrumb-item>{{ getCurrentMenuTitle() }}</n-breadcrumb-item>
        </n-breadcrumb>
        <div class="header-actions">
          <n-button text @click="printGuide">
            <n-icon size="18"><PrintOutline /></n-icon>
            打印
          </n-button>
          <n-button text @click="downloadGuide">
            <n-icon size="18"><DownloadOutline /></n-icon>
            下载
          </n-button>
        </div>
      </div> -->

      <div class="guide-content-wrapper">
        <!-- 欢迎页面 -->
        <div v-if="activeMenu === 'welcome'" class="guide-section">
          <!-- 英雄区域 -->
        

            <!-- 功能特色展示 -->
            <div class="features-showcase">
              <div class="section-title text-center">
                <h2>为什么选择 FreeMix？</h2>
                <p>强大功能，简化您的目标管理之旅</p>
              </div>
              <div class="features-grid-enhanced">
                <div class="feature-card-enhanced">
                  <div class="feature-icon-enhanced">
                    <n-icon size="40" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><LogoReddit /></n-icon>
                  </div>
                  <h3>AI智能助手</h3>
                  <p>24/7智能问答，个性化目标生成</p>
                  <div class="feature-progress">
                    <n-progress type="line" :percentage="95" :show-indicator="false" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'" processing />
                  </div>
                </div>
                <div class="feature-card-enhanced">
                  <div class="feature-icon-enhanced">
                    <n-icon size="40" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><BarChartOutline /></n-icon>
                  </div>
                  <h3>数据可视化</h3>
                  <p>直观图表分析，进度一目了然</p>
                  <div class="feature-progress">
                    <n-progress type="line" :percentage="88" :show-indicator="false" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'" processing />
                  </div>
                </div>
                <div class="feature-card-enhanced">
                  <div class="feature-icon-enhanced">
                    <n-icon size="40" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><PeopleOutline /></n-icon>
                  </div>
                  <h3>团队协作</h3>
                  <p>多人协作，实时同步目标进度</p>
                  <div class="feature-progress">
                    <n-progress type="line" :percentage="92" :show-indicator="false" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'" processing />
                  </div>
                </div>
                <div class="feature-card-enhanced">
                  <div class="feature-icon-enhanced">
                    <n-icon size="40" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><SettingsOutline /></n-icon>
                  </div>
                  <h3>个性化定制</h3>
                  <p>主题切换，打造专属工作空间</p>
                  <div class="feature-progress">
                    <n-progress type="line" :percentage="85" :show-indicator="false" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'" processing />
                  </div>
                </div>
              </div>
            </div>



            
          </div>

          <!-- 其他页面内容保持不变 -->
          <!-- 用户认证指引 -->
          <div v-if="activeMenu === 'auth'" class="guide-section">
            <div class="section-header">
              <div class="section-icon">
                <n-icon size="48" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><PersonCircleOutline /></n-icon>
              </div>
              <div class="section-title-content">
                <h2 class="section-title">用户认证系统</h2>
                <p class="section-subtitle">安全便捷的账户管理系统</p>
              </div>
            </div>

            <div class="section-content">
              <!-- 流程图样式演示 -->
              <div class="flow-chart">
                <h4 class="section-subtitle" style="text-align: center; margin-bottom: 32px;">
                  📊 用户认证流程图
                </h4>
                
                <div class="flow-step" :class="{ active: authStep === 1 }" @click="authStep = 1">
                  <div class="flow-number">1</div>
                  <div class="flow-content">
                    <h4>用户注册</h4>
                    <p>创建新的FreeMix账户，完成邮箱验证和基本信息填写</p>
                  </div>
                  <div class="flow-arrow" v-if="authStep > 1">→</div>
                </div>
                
                <div class="flow-step" :class="{ active: authStep === 2 }" @click="authStep = 2">
                  <div class="flow-number">2</div>
                  <div class="flow-content">
                    <h4>账户激活</h4>
                    <p>验证邮箱地址，设置强密码，完成安全验证</p>
                  </div>
                  <div class="flow-arrow" v-if="authStep > 2">→</div>
                </div>
                
                <div class="flow-step" :class="{ active: authStep === 3 }" @click="authStep = 3">
                  <div class="flow-number">3</div>
                  <div class="flow-content">
                    <h4>安全设置</h4>
                    <p>启用双因素认证，设置安全问题，配置登录偏好</p>
                  </div>
                </div>
              </div>
              
              <!-- 模拟界面演示 -->
              <div class="mock-interface">
                <div class="mock-header">
                  <div class="mock-dot"></div>
                  <div class="mock-dot"></div>
                  <div class="mock-dot"></div>
                  <span style="margin-left: 12px; color: var(--text-secondary); font-size: 12px;">
                    {{ authStepTitle }}
                  </span>
                </div>
                <div class="mock-content">
                  <div v-if="authStep === 1">
                    <h5 style="color: var(--text-primary); margin-bottom: 16px;">📝 注册信息</h5>
                    <n-input class="mock-input" placeholder="邮箱地址" style="margin-bottom: 12px;" />
                    <n-input class="mock-input" placeholder="用户名" style="margin-bottom: 12px;" />
                    <n-input class="mock-input" placeholder="密码" type="password" style="margin-bottom: 16px;" />
                    <n-button type="primary" class="mock-button">
                      发送验证邮件
                    </n-button>
                  </div>
                  
                  <div v-else-if="authStep === 2">
                    <h5 style="color: var(--text-primary); margin-bottom: 16px;">🔐 邮箱验证</h5>
                    <p style="color: var(--text-secondary); margin-bottom: 16px;">请检查您的邮箱并点击验证链接</p>
                    <n-input class="mock-input" placeholder="输入验证码" style="margin-bottom: 12px;" />
                    <n-button type="primary" class="mock-button" style="margin-right: 8px;">
                      验证邮箱
                    </n-button>
                    <n-button class="mock-button">
                      重新发送
                    </n-button>
                  </div>
                  
                  <div v-else-if="authStep === 3">
                    <h5 style="color: var(--text-primary); margin-bottom: 16px;">🛡️ 安全设置</h5>
                    <p style="color: var(--text-secondary); margin-bottom: 16px;">配置您的账户安全选项</p>
                    <n-button type="primary" class="mock-button" style="margin-right: 8px; margin-bottom: 8px;">
                      启用双因素认证
                    </n-button>
                    <n-button type="primary" class="mock-button" style="margin-right: 8px; margin-bottom: 8px;">
                      设置安全问题
                    </n-button>
                    <n-button type="primary" class="mock-button" style="margin-bottom: 8px;">
                      登录通知设置
                    </n-button>
                  </div>
                </div>
              </div>
              
              <!-- 悬停提示系统 -->
              <div style="text-align: center; margin-top: 24px;">
                <p style="color: var(--text-secondary);">
                  💡 
                  <span class="tooltip-trigger">
                    点击上方步骤查看详细演示
                    <span class="tooltip">点击任意步骤可以快速跳转到该环节</span>
                  </span>
                </p>
              </div>

              <n-card class="content-card">
                <template #header>
                  <h4>注册账户</h4>
                </template>
                <h4>注册步骤</h4>
                <ol>
                  <li>访问注册页面</li>
                  <li>填写用户名、邮箱、密码等信息</li>
                  <li>完成验证码验证</li>
                  <li>提交注册信息</li>
                  <li>邮箱验证（如启用）</li>
                </ol>
                <n-alert type="info" title="提示" class="mt-3">
                  建议使用强密码，包含大小写字母、数字和特殊字符。
                </n-alert>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>登录系统</h4>
                </template>
                <h4>登录方式</h4>
                <n-tabs type="line">
                  <n-tab-pane name="password" tab="密码登录">
                    <p>使用用户名/邮箱和密码登录系统</p>
                    <ul>
                      <li>输入您的用户名或邮箱地址</li>
                      <li>输入密码（支持显示/隐藏）</li>
                      <li>完成验证码验证</li>
                      <li>点击登录按钮</li>
                    </ul>
                  </n-tab-pane>
                  <n-tab-pane name="github" tab="GitHub登录">
                    <p>使用GitHub账户快速登录</p>
                    <ul>
                      <li>点击"使用GitHub登录"按钮</li>
                      <li>授权FreeMix访问您的GitHub信息</li>
                      <li>系统自动完成登录</li>
                    </ul>
                  </n-tab-pane>
                </n-tabs>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>安全设置</h4>
                </template>
                <h4>两步验证 (2FA)</h4>
                <p>增强账户安全性的重要措施</p>
                <ul>
                  <li>在设置中启用两步验证</li>
                  <li>下载验证器应用（如Google Authenticator）</li>
                  <li>扫描二维码或输入密钥</li>
                  <li>保存备份验证码</li>
                </ul>
                <n-alert type="warning" title="重要提醒" class="mt-3">
                  请妥善保管您的备份验证码，它们是恢复账户访问的唯一方式。
                </n-alert>
              </n-card>
            </div>
          </div>

          <!-- 目标管理指引 -->
          <div v-if="activeMenu === 'goals'" class="guide-section">
            <div class="section-header">
              <div class="section-icon">
                <n-icon size="48" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><FlagOutline /></n-icon>
              </div>
              <div class="section-title-content">
                <h2 class="section-title">目标管理系统</h2>
                <p class="section-subtitle">科学的SMART目标管理方法</p>
              </div>
            </div>

            <div class="section-content">
              <!-- 可交互的流程图演示 -->
              <div class="interactive-flow">
                <h4 class="section-subtitle" style="text-align: center; margin-bottom: 32px;">
                  🎯 目标创建流程图
                </h4>
                
                <div class="flow-node" :class="{ selected: goalStep === 1 }" @click="goalStep = 1">
                <div style="display: flex; align-items: center; gap: 16px;">
                  <div style="width: 40px; height: 40px; background: var(--primary-green); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--white); font-weight: bold;">1</div>
                  <div style="flex: 1;">
                    <div class="flow-node-title">📝 基本信息设置</div>
                    <div class="flow-node-description">设定目标名称、描述、类型分类，建立目标框架</div>
                  </div>
                </div>
              </div>
                
                <div class="flow-connection">↓</div>
                
                <div class="flow-node" :class="{ selected: goalStep === 2 }" @click="goalStep = 2">
                <div style="display: flex; align-items: center; gap: 16px;">
                  <div style="width: 40px; height: 40px; background: var(--primary-green-dark); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--white); font-weight: bold;">2</div>
                  <div style="flex: 1;">
                    <div class="flow-node-title">⚙️ SMART目标设定</div>
                    <div class="flow-node-description">定义具体的、可衡量的、有时间限制的目标指标</div>
                  </div>
                </div>
              </div>
                
                <div class="flow-connection">↓</div>
                
                <div class="flow-node" :class="{ selected: goalStep === 3 }" @click="goalStep = 3">
                <div style="display: flex; align-items: center; gap: 16px;">
                  <div style="width: 40px; height: 40px; background: #00a085; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--white); font-weight: bold;">3</div>
                  <div style="flex: 1;">
                    <div class="flow-node-title">📅 时间规划</div>
                    <div class="flow-node-description">设定开始时间、截止日期和里程碑节点</div>
                  </div>
                </div>
              </div>
                
                <div class="flow-connection">↓</div>
                
                <div class="flow-node" :class="{ selected: goalStep === 4 }" @click="goalStep = 4">
                <div style="display: flex; align-items: center; gap: 16px;">
                  <div style="width: 40px; height: 40px; background: #007a65; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--white); font-weight: bold;">4</div>
                  <div style="flex: 1;">
                    <div class="flow-node-title">🏷️ 优先级设置</div>
                    <div class="flow-node-description">设定目标重要性和紧急程度，配置提醒机制</div>
                  </div>
                </div>
              </div>
              </div>
              
              <!-- 渐进式步骤演示 -->
              <div class="progressive-steps">
                <h5 style="color: var(--text-primary); margin-bottom: 24px; text-align: center;">
                  📊 当前步骤详情
                </h5>
                
                <div class="step-container" @click="goalStep = 1">
                  <div class="step-circle" :class="{ completed: goalStep > 1, active: goalStep === 1 }">1</div>
                  <div class="step-content">
                    <div class="step-title">{{ goalStepTitle }}</div>
                    <div class="step-description">{{ goalStepDescription }}</div>
                  </div>
                  <div class="step-connector" :class="{ completed: goalStep > 1 }"></div>
                </div>
                
                <div class="step-container" @click="goalStep = 2">
                  <div class="step-circle" :class="{ completed: goalStep > 2, active: goalStep === 2 }">2</div>
                  <div class="step-content">
                    <div class="step-title">设置具体指标</div>
                    <div class="step-description">定义可衡量的成功标准</div>
                  </div>
                  <div class="step-connector" :class="{ completed: goalStep > 2 }"></div>
                </div>
                
                <div class="step-container" @click="goalStep = 3">
                  <div class="step-circle" :class="{ completed: goalStep > 3, active: goalStep === 3 }">3</div>
                  <div class="step-content">
                    <div class="step-title">规划时间节点</div>
                    <div class="step-description">设定合理的完成期限</div>
                  </div>
                  <div class="step-connector" :class="{ completed: goalStep > 3 }"></div>
                </div>
                
                <div class="step-container" @click="goalStep = 4">
                  <div class="step-circle" :class="{ completed: goalStep > 4, active: goalStep === 4 }">4</div>
                  <div class="step-content">
                    <div class="step-title">配置优先级</div>
                    <div class="step-description">设置目标重要级别</div>
                  </div>
                </div>
              </div>
              
              <!-- 视频演示区域 -->
              <div class="video-demo">
                <div class="video-placeholder">
                  <div>
                    <div style="font-size: 16px; margin-bottom: 8px;">📹 目标创建演示</div>
                    <div style="font-size: 14px; opacity: 0.8;">点击观看完整的目标创建流程</div>
                  </div>
                </div>
                <div class="video-controls">
                  <n-button type="primary" class="enhanced-button" :color="isDark ? '#00c9a7' : '#008c72'">
                    <template #icon>
                      <n-icon><PlayOutline /></n-icon>
                    </template>
                    播放演示
                  </n-button>
                  <n-button type="primary" class="enhanced-button" style="margin-left: 12px;" :color="isDark ? '#00c9a7' : '#008c72'">
                    <template #icon>
                      <n-icon><DocumentTextOutline /></n-icon>
                    </template>
                    查看文档
                  </n-button>
                </div>
              </div>

              <n-card class="content-card">
                <template #header>
                  <h4>创建目标</h4>
                </template>
                <h4>创建步骤</h4>
                <n-steps :current="4" size="small" vertical>
                  <n-step title="基本信息" description="目标名称、描述、类别" />
                  <n-step title="目标设定" description="设定目标的具体指标和标准" />
                  <n-step title="时间规划" description="设定开始时间、截止日期" />
                  <n-step title="优先级设置" description="设置目标的重要性和紧急程度" />
                </n-steps>
                <n-alert type="success" title="AI助手提示" class="mt-3">
                  使用AI助手可以帮您快速生成符合SMART原则的目标。
                </n-alert>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>目标分类</h4>
                </template>
                <h4>目标类型</h4>
                <div class="goal-types">
                  <div class="type-item">
                    <strong>个人发展</strong>
                    <p>技能提升、学习计划、健康管理</p>
                  </div>
                  <div class="type-item">
                    <strong>工作项目</strong>
                    <p>工作任务、项目交付、职业发展</p>
                  </div>
                  <div class="type-item">
                    <strong>生活规划</strong>
                    <p>旅行计划、购物清单、生活改善</p>
                  </div>
                  <div class="type-item">
                    <strong>学习成长</strong>
                    <p>课程学习、考试准备、知识积累</p>
                  </div>
                </div>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>进度跟踪</h4>
                </template>
                <h4>跟踪方法</h4>
                <n-tabs type="line">
                  <n-tab-pane name="manual" tab="手动更新">
                    <p>手动更新目标进度</p>
                    <ul>
                      <li>在目标详情页更新进度百分比</li>
                      <li>添加备注和心得体会</li>
                      <li>标记子目标完成状态</li>
                    </ul>
                  </n-tab-pane>
                  <n-tab-pane name="automatic" tab="自动跟踪">
                    <p>系统自动跟踪目标进度</p>
                    <ul>
                      <li>子目标完成自动更新总体进度</li>
                      <li>截止日期临近自动提醒</li>
                      <li>完成状态智能判断</li>
                    </ul>
                  </n-tab-pane>
                </n-tabs>
              </n-card>
            </div>
          </div>

          <!-- AI助手功能 -->
          <div v-if="activeMenu === 'ai'" class="guide-section">
            <div class="section-header">
              <div class="section-icon">
                <n-icon size="48" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><LogoReddit /></n-icon>
              </div>
              <div class="section-title-content">
                <h2 class="section-title">AI智能助手</h2>
                <p class="section-subtitle">智能问答与目标生成的强大工具</p>
              </div>
            </div>

            <div class="section-content">
              <!-- 聊天模拟界面演示 -->
              <div class="mock-interface">
                <div class="mock-header">
                  <div class="mock-dot"></div>
                  <div class="mock-dot"></div>
                  <div class="mock-dot"></div>
                  <span style="margin-left: 12px; color: var(--text-secondary); font-size: 12px;">
                    AI助手聊天窗口
                  </span>
                </div>
                <div class="mock-content" style="height: 300px; overflow-y: auto;">
                  <div v-if="aiStep === 1">
                    <div style="margin-bottom: 16px;">
                      <div style="background: var(--card-bg); padding: 12px; border-radius: 8px; max-width: 80%; margin-bottom: 8px;">
                        <p style="color: var(--text-primary); margin: 0;">👋 您好！我是AI助手小Free</p>
                      </div>
                      <div style="background: var(--card-bg); padding: 12px; border-radius: 8px; max-width: 80%;">
                        <p style="color: var(--text-primary); margin: 0;">有什么我可以帮助您的吗？</p>
                      </div>
                    </div>
                    <div style="text-align: right;">
                      <div style="background: var(--accent-color); color: white; padding: 12px; border-radius: 8px; max-width: 80%; margin-left: auto;">
                        <p style="margin: 0;">我想要设定一个学习目标</p>
                      </div>
                    </div>
                  </div>
                  
                  <div v-else-if="aiStep === 2">
                    <div style="margin-bottom: 16px;">
                      <div style="background: var(--card-bg); padding: 12px; border-radius: 8px; max-width: 80%; margin-bottom: 8px;">
                        <p style="color: var(--text-primary); margin: 0;">好的！我可以帮您生成一个学习目标。</p>
                      </div>
                      <div style="background: var(--card-bg); padding: 12px; border-radius: 8px; max-width: 80%; margin-bottom: 8px;">
                        <p style="color: var(--text-primary); margin: 0;">请告诉我您想学习什么？</p>
                      </div>
                    </div>
                    <div style="text-align: right; margin-bottom: 12px;">
                      <div style="background: var(--accent-color); color: white; padding: 12px; border-radius: 8px; max-width: 80%; margin-left: auto;">
                        <p style="margin: 0;">我想学习TypeScript编程</p>
                      </div>
                    </div>
                    <div style="margin-bottom: 16px;">
                      <div style="background: var(--card-bg); padding: 12px; border-radius: 8px; max-width: 90%; margin-bottom: 8px;">
                        <p style="color: var(--text-primary); margin: 0;">太好了！根据您的需求，我为您生成了以下学习目标：</p>
                      </div>
                      <div style="background: var(--card-bg); padding: 16px; border-radius: 8px; max-width: 90%; border: 1px solid var(--accent-color);">
                        <div style="color: var(--accent-color); font-weight: bold; margin-bottom: 8px;">📚 6个月TypeScript精通计划</div>
                        <ul style="color: var(--text-secondary); margin: 0; padding-left: 20px;">
                          <li>第1-2月：基础语法和类型系统</li>
                          <li>第3-4月：高级特性和框架集成</li>
                          <li>第5-6月：项目实战和最佳实践</li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- AI功能特点展示 -->
              <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin: 24px 0;">
                <div style="background: var(--card-bg); padding: 20px; border-radius: 12px; border: 1px solid var(--border-color);">
                  <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
                    <div style="width: 40px; height: 40px; background: var(--primary-green); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white;">💬</div>
                    <h4 style="color: var(--text-primary); margin: 0;">智能对话</h4>
                  </div>
                  <p style="color: var(--text-secondary); margin: 0;">支持自然语言交互，理解上下文语境</p>
                </div>
                
                <div style="background: var(--card-bg); padding: 20px; border-radius: 12px; border: 1px solid var(--border-color);">
                  <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
                    <div style="width: 40px; height: 40px; background: var(--primary-green-dark); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white;">🎯</div>
                    <h4 style="color: var(--text-primary); margin: 0;">目标生成</h4>
                  </div>
                  <p style="color: var(--text-secondary); margin: 0;">基于SMART原则生成个性化目标</p>
                </div>
                
                <div style="background: var(--card-bg); padding: 20px; border-radius: 12px; border: 1px solid var(--border-color);">
                  <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
                    <div style="width: 40px; height: 40px; background: #00a085; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--white);">⚡</div>
                    <h4 style="color: var(--text-primary); margin: 0;">实时响应</h4>
                  </div>
                  <p style="color: var(--text-secondary); margin: 0;">快速提供准确的答案和建议</p>
                </div>
              </div>
              
              <!-- 交互式功能切换 -->
              <div style="text-align: center; margin: 24px 0;">
                <n-button 
                  type="primary" 
                  @click="aiStep = 1" 
                  :style="{ 
                    background: aiStep === 1 ? 'var(--primary-green)' : 'var(--n-card-color)', 
                    color: aiStep === 1 ? 'white' : 'var(--text-light)',
                    border: aiStep === 1 ? 'none' : '1px solid var(--border-light)',
                    padding: '8px 20px',
                    borderRadius: '8px',
                    fontWeight: '500'
                  }"
                  :focusable="false"
                >
                  <template #icon>
                    <n-icon><ChatbubbleOutline /></n-icon>
                  </template>
                  智能对话
                </n-button>
                <n-button 
                  type="primary" 
                  @click="aiStep = 2" 
                  :style="{ 
                    marginLeft: '12px',
                    background: aiStep === 2 ? 'var(--primary-green)' : 'var(--n-card-color)', 
                    color: aiStep === 2 ? 'white' : 'var(--text-light)',
                    border: aiStep === 2 ? 'none' : '1px solid var(--border-light)',
                    padding: '8px 20px',
                    borderRadius: '8px',
                    fontWeight: '500'
                  }"
                  :focusable="false"
                >
                  <template #icon>
                    <n-icon><SparklesOutline /></n-icon>
                  </template>
                  目标生成
                </n-button>
              </div>
              
              <!-- 悬停提示系统 -->
              <div style="text-align: center; margin-top: 24px;">
                <p style="color: var(--text-secondary);">
                  💡 
                  <span class="tooltip-trigger">
                    点击上方按钮切换AI功能演示
                    <span class="tooltip">体验不同的AI助手功能</span>
                  </span>
                </p>
              </div>

              <n-card class="content-card">
                <template #header>
                  <h4>AI问答助手</h4>
                </template>
                <h4>功能介绍</h4>
                <p>AI助手提供24/7智能问答服务，帮助您快速获得解答和建议</p>
                <ul>
                  <li>支持自然语言交互，理解上下文语境</li>
                  <li>涵盖技术、生活、学习等多领域问题</li>
                  <li>实时响应，提供准确的答案和建议</li>
                  <li>支持多轮对话，保持对话上下文</li>
                </ul>
                
                <h5>使用步骤</h5>
                <n-steps :current="4" size="small" vertical>
                  <n-step title="启动助手" description="点击聊天图标启动AI问答功能" />
                  <n-step title="输入问题" description="在对话框中输入您的问题" />
                  <n-step title="获得回答" description="AI分析问题并提供详细解答" />
                  <n-step title="继续对话" description="根据需要继续追问或讨论" />
                </n-steps>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>AI目标生成</h4>
                </template>
                <h4>目标生成器</h4>
                <p>基于您的需求和偏好，AI智能生成个性化目标</p>
                <ul>
                  <li>基于SMART原则生成科学目标</li>
                  <li>考虑个人能力和时间限制</li>
                  <li>提供实现路径和行动建议</li>
                  <li>自动分解大目标为可执行的小任务</li>
                </ul>

                <h5>功能特点</h5>
                <div class="feature-grid">
                  <div class="feature-item">
                    <strong>智能分析</strong>
                    <p>分析用户输入的需求和时间框架</p>
                  </div>
                  <div class="feature-item">
                    <strong>个性化定制</strong>
                    <p>根据用户偏好调整目标难度和方向</p>
                  </div>
                  <div class="feature-item">
                    <strong>实时优化</strong>
                    <p>根据进度反馈动态调整目标</p>
                  </div>
                </div>
              </n-card>
            </div>
          </div>

          <!-- 数据统计功能 -->
          <div v-if="activeMenu === 'statistics'" class="guide-section">
            <div class="section-header">
              <div class="section-icon">
                <n-icon size="48" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><BarChartOutline /></n-icon>
              </div>
              <div class="section-title-content">
                <h2 class="section-title">数据统计分析</h2>
                <p class="section-subtitle">可视化分析您的目标完成情况</p>
              </div>
            </div>

            <div class="section-content">
              <n-card class="content-card">
                <template #header>
                  <h4>概览面板</h4>
                </template>
                <h4>统计概览</h4>
                <p>直观展示您的目标完成情况和趋势分析</p>
                <ul>
                  <li>总目标数量、已完成、进行中、已完成百分比</li>
                  <li>本周/月度目标完成情况统计</li>
                  <li>目标完成率趋势图表</li>
                  <li>活跃目标分布情况</li>
                </ul>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>图表分析</h4>
                </template>
                <h4>可视化图表</h4>
                <n-tabs type="line">
                  <n-tab-pane name="trend" tab="趋势分析">
                    <p>目标完成趋势和时间轴分析</p>
                    <ul>
                      <li>完成率时间序列图</li>
                      <li>目标创建和完成数量对比</li>
                      <li>工作日和周末完成情况</li>
                    </ul>
                  </n-tab-pane>
                  <n-tab-pane name="category" tab="分类统计">
                    <p>不同类型目标的完成情况</p>
                    <ul>
                      <li>目标类别饼图分布</li>
                      <li>各类别完成率对比</li>
                      <li>优先级完成情况统计</li>
                    </ul>
                  </n-tab-pane>
                </n-tabs>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>详细数据</h4>
                </template>
                <h4>数据表格</h4>
                <p>详细的目标完成数据，支持筛选和导出</p>
                <ul>
                  <li>完整的目标历史记录</li>
                  <li>完成时间、用时统计</li>
                  <li>支持数据筛选和排序</li>
                  <li>导出Excel或PDF报告</li>
                </ul>
              </n-card>
            </div>
          </div>

          <!-- 团队协作功能 -->
          <div v-if="activeMenu === 'collaboration'" class="guide-section">
            <div class="section-header">
              <div class="section-icon">
                <n-icon size="48" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><PeopleOutline /></n-icon>
              </div>
              <div class="section-title-content">
                <h2 class="section-title">团队协作管理</h2>
                <p class="section-subtitle">高效的团队协作和目标共享</p>
              </div>
            </div>

            <div class="section-content">
              <n-card class="content-card">
                <template #header>
                  <h4>协作人管理</h4>
                </template>
                <h4>添加协作人</h4>
                <p>邀请团队成员参与目标管理和执行</p>
                <ul>
                  <li>通过邮箱或用户名邀请协作人</li>
                  <li>设置协作人的查看和编辑权限</li>
                  <li>支持批量添加团队成员</li>
                  <li>协作人角色自动识别</li>
                </ul>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>权限管理</h4>
                </template>
                <h4>协作权限</h4>
                <n-tabs type="line">
                  <n-tab-pane name="view" tab="查看权限">
                    <p>协作人仅可查看目标进展</p>
                    <ul>
                      <li>查看目标详情和进度</li>
                      <li>查看统计数据和图表</li>
                      <li>无法修改目标内容</li>
                    </ul>
                  </n-tab-pane>
                  <n-tab-pane name="edit" tab="编辑权限">
                    <p>协作人可参与目标编辑和更新</p>
                    <ul>
                      <li>更新目标进度和状态</li>
                      <li>添加备注和心得</li>
                      <li>协助完成子任务</li>
                    </ul>
                  </n-tab-pane>
                </n-tabs>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>协作功能</h4>
                </template>
                <h4>协作工具</h4>
                <ul>
                  <li><strong>实时更新：</strong>目标进度实时同步给所有协作人</li>
                  <li><strong>消息通知：</strong>重要更新自动通知相关人员</li>
                  <li><strong>评论讨论：</strong>在目标页面进行协作讨论</li>
                  <li><strong>任务分配：</strong>将子任务分配给特定协作人</li>
                </ul>
              </n-card>
            </div>
          </div>

          <!-- 个性化设置功能 -->
          <div v-if="activeMenu === 'settings'" class="guide-section">
            <div class="section-header">
              <div class="section-icon">
                <n-icon size="48" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><SettingsOutline /></n-icon>
              </div>
              <div class="section-title-content">
                <h2 class="section-title">个性化设置</h2>
                <p class="section-subtitle">打造专属的个人工作空间</p>
              </div>
            </div>

            <div class="section-content">
              <n-card class="content-card">
                <template #header>
                  <h4>个人资料</h4>
                </template>
                <h4>账户信息</h4>
                <ul>
                  <li>头像上传和个人信息编辑</li>
                  <li>邮箱、手机号等联系方式</li>
                  <li>个人简介和职业信息</li>
                  <li>时区和语言偏好设置</li>
                </ul>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>主题设置</h4>
                </template>
                <h4>界面定制</h4>
                <n-tabs type="line">
                  <n-tab-pane name="theme" tab="主题切换">
                    <p>选择喜欢的主题风格</p>
                    <ul>
                      <li>深色主题：护眼的暗色调界面</li>
                      <li>浅色主题：清新的明亮界面</li>
                      <li>自动切换：根据系统时间自动调整</li>
                    </ul>
                  </n-tab-pane>
                  <n-tab-pane name="layout" tab="布局设置">
                    <p>自定义界面布局和显示</p>
                    <ul>
                      <li>侧边栏展开/收缩</li>
                      <li>卡片样式偏好</li>
                      <li>字体大小调整</li>
                    </ul>
                  </n-tab-pane>
                </n-tabs>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>通知设置</h4>
                </template>
                <h4>消息通知</h4>
                <ul>
                  <li><strong>目标提醒：</strong>截止日期和重要节点提醒</li>
                  <li><strong>协作通知：</strong>团队成员操作和评论通知</li>
                  <li><strong>系统消息：</strong>功能更新和安全通知</li>
                  <li><strong>邮件通知：</strong>重要事项邮件提醒</li>
                </ul>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>隐私安全</h4>
                </template>
                <h4>安全设置</h4>
                <ul>
                  <li>两步验证(2FA)启用和管理</li>
                  <li>密码修改和强度要求</li>
                  <li>登录设备和位置管理</li>
                  <li>数据导出和账户删除</li>
                </ul>
                <n-alert type="info" title="安全提示" class="mt-3">
                  建议启用两步验证以增强账户安全性。
                </n-alert>
              </n-card>
            </div>
          </div>

          <!-- 移动端指南 -->
          <div v-if="activeMenu === 'mobile'" class="guide-section">
            <div class="section-header">
              <div class="section-icon">
                <n-icon size="48" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><PhonePortraitOutline /></n-icon>
              </div>
              <div class="section-title-content">
                <h2 class="section-title">移动端使用指南</h2>
                <p class="section-subtitle">随时随地管理您的目标</p>
              </div>
            </div>

            <div class="section-content">
              <n-card class="content-card">
                <template #header>
                  <h4>响应式设计</h4>
                </template>
                <h4>移动端适配</h4>
                <ul>
                  <li>完全响应式设计，适配所有屏幕尺寸</li>
                  <li>触控优化，支持手势操作</li>
                  <li>离线功能，支持离线浏览</li>
                  <li>推送通知，及时提醒重要事项</li>
                </ul>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>移动端设置</h4>
                </template>
                <h4>移动端特有功能</h4>
                <ul>
                  <li><strong>语音输入：</strong>支持语音创建和更新目标</li>
                  <li><strong>相机扫描：</strong>扫描二维码快速访问</li>
                  <li><strong>位置服务：</strong>基于位置的目标提醒</li>
                  <li><strong>快捷操作：</strong>桌面快捷方式和小组件</li>
                </ul>
              </n-card>
            </div>
          </div>

          <!-- 快捷功能 -->
          <div v-if="activeMenu === 'shortcuts'" class="guide-section">
            <div class="section-header">
              <div class="section-icon">
                <n-icon size="48" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><FlashOutline /></n-icon>
              </div>
              <div class="section-title-content">
                <h2 class="section-title">快捷功能</h2>
                <p class="section-subtitle">提升效率的快捷操作</p>
              </div>
            </div>

            <div class="section-content">
              <n-card class="content-card">
                <template #header>
                  <h4>键盘快捷键</h4>
                </template>
                <h4>常用快捷键</h4>
                <ul>
                  <li><kbd>Ctrl + N</kbd>：创建新目标</li>
                  <li><kbd>Ctrl + S</kbd>：保存当前编辑</li>
                  <li><kbd>Ctrl + F</kbd>：搜索目标</li>
                  <li><kbd>Ctrl + /</kbd>：显示帮助</li>
                  <li><kbd>Esc</kbd>：取消当前操作</li>
                </ul>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>批量操作</h4>
                </template>
                <h4>高效管理</h4>
                <ul>
                  <li>批量选择多个目标进行操作</li>
                  <li>批量更新目标状态和进度</li>
                  <li>批量导出目标数据</li>
                  <li>批量删除已完成的目标</li>
                </ul>
              </n-card>
            </div>
          </div>

          <!-- 常见问题 -->
          <div v-if="activeMenu === 'faq'" class="guide-section">
            <div class="section-header">
              <div class="section-icon">
                <n-icon size="48" :color="currentTheme === 'dark' ? '#00c9a7' : '#008c72'"><HelpCircleOutline /></n-icon>
              </div>
              <div class="section-title-content">
                <h2 class="section-title">常见问题</h2>
                <p class="section-subtitle">快速找到您需要的答案</p>
              </div>
            </div>

            <div class="section-content">
              <n-card class="content-card">
                <template #header>
                  <h4>账户相关</h4>
                </template>
                <h4>Q: 忘记密码怎么办？</h4>
                <p>A: 在登录页面点击"忘记密码"，输入邮箱后按提示重置密码。</p>
                
                <h4>Q: 如何更换邮箱？</h4>
                <p>A: 在设置-个人资料页面可以修改邮箱，需验证新邮箱地址。</p>
                
                <h4>Q: 账户被盗用怎么办？</h4>
                <p>A: 立即在设置-隐私安全中重置密码，启用两步验证。</p>
              </n-card>

              <n-card class="content-card">
                <template #header>
                  <h4>目标管理</h4>
                </template>
                <h4>Q: 如何恢复已删除的目标？</h4>
                <p>A: 已删除的目标可在回收站中恢复，保留30天。</p>
                
                <h4>Q: 目标进度不准确怎么办？</h4>
                <p>A: 可以手动调整进度百分比，系统会记录修改历史。</p>
                
                <h4>Q: 如何批量导入目标？</h4>
                <p>A: 支持Excel和CSV格式导入，在目标页面点击导入按钮。</p>
              </n-card>
            </div>
          </div>

        </div>
      </n-layout-content>
    </n-layout>
  <!-- </div> -->
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, h, watch    ,inject } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import {
  ArrowBackOutline,
  HomeOutline,
  PersonCircleOutline,
  FlagOutline,
  LogoReddit,
  BarChartOutline,
  PeopleOutline,
  SettingsOutline,
  PhonePortraitOutline,
  FlashOutline,
  HelpCircleOutline,
  PrintOutline,
  DownloadOutline,
  PlayOutline,
  DocumentTextOutline,
  ChatbubbleOutline,
  SparklesOutline
} from '@vicons/ionicons5';

const router = useRouter();
const message = useMessage();
const isDark = inject('isDark', ref(true))

// 当前主题状态（与isDark保持同步）
const currentTheme = computed(() => isDark.value ? 'dark' : 'light');
const activeMenu = ref('welcome');

// 侧边栏折叠状态
const sidebarCollapsed = ref(false);

// 菜单配置
const menuOptions = computed(() => [
  {
    label: '欢迎页面',
    key: 'welcome',
    icon: () => h(HomeOutline)
  },
  {
    label: '用户认证',
    key: 'auth',
    icon: () => h(PersonCircleOutline)
  },
  {
    label: '目标管理',
    key: 'goals',
    icon: () => h(FlagOutline)
  },
  {
    label: 'AI助手',
    key: 'ai',
    icon: () => h(LogoReddit)
  },
  {
    label: '数据统计',
    key: 'statistics',
    icon: () => h(BarChartOutline)
  },
  {
    label: '团队协作',
    key: 'collaboration',
    icon: () => h(PeopleOutline)
  },
  {
    label: '个性化设置',
    key: 'settings',
    icon: () => h(SettingsOutline)
  },
  {
    label: '移动端指南',
    key: 'mobile',
    icon: () => h(PhonePortraitOutline)
  },
  {
    label: '快捷功能',
    key: 'shortcuts',
    icon: () => h(FlashOutline)
  },
  {
    label: '常见问题',
    key: 'faq',
    icon: () => h(HelpCircleOutline)
  }
]);

// 获取当前菜单标题
const getCurrentMenuTitle = () => {
  const menu = menuOptions.value.find(item => item.key === activeMenu.value);
  return menu ? menu.label : '未知页面';
};

// 处理菜单选择
const handleMenuSelect = (key: string) => {
  activeMenu.value = key;
};

// 导航到特定章节
const navigateTo = (key: string) => {
  activeMenu.value = key;
};

// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 设置主题
const setTheme = (theme: string) => {
  currentTheme.value = theme;
  
  // 更新body类名
  document.body.className = theme === 'dark' ? 'dark-theme' : 'light-theme';
  
  // 保存主题偏好
  localStorage.setItem('user-guide-theme', theme);
  
  message.success(`已切换到${theme === 'dark' ? '深色' : '浅色'}主题`);
};

// 获取图标颜色
const getIconColor = () => {
  return currentTheme.value === 'dark' ? 'rgba(255, 255, 255, 0.8)' : 'rgba(0, 0, 0, 0.8)';
};

// 打印指南
const printGuide = () => {
  message.info('打印功能开发中');
};

// 下载指南
const downloadGuide = () => {
  message.info('下载功能开发中');
};

// 交互式演示状态
const demoStep = ref(1);

// 下一步
const nextDemoStep = () => {
  if (demoStep.value < 4) {
    demoStep.value++;
    message.success(`已切换到第 ${demoStep.value} 步`);
  }
};

// 上一步
const previousDemoStep = () => {
  if (demoStep.value > 1) {
    demoStep.value--;
    message.success(`已切换到第 ${demoStep.value} 步`);
  }
};

// 重置演示
const resetDemo = () => {
  demoStep.value = 1;
  message.info('演示已重置');
};

// 用户认证演示状态
const authStep = ref(1);

// 认证步骤标题
const authStepTitle = computed(() => {
  const titles = ['填写账户信息', '设置安全验证', '完成认证绑定'];
  return titles[authStep.value - 1];
});

// 认证步骤描述
const authStepDescription = computed(() => {
  const descriptions = [
    '填写用户名、邮箱、密码等基本信息，完成账户注册',
    '启用两步验证，设置安全保护措施',
    '完成邮箱或手机验证，激活账户'
  ];
  return descriptions[authStep.value - 1];
});

// 认证步骤特性
const authStepFeatures = computed(() => {
  const features = [
    ['实时验证输入格式', '支持邮箱和用户名', '密码强度检测'],
    ['Google身份验证器', '短信验证支持', '备份验证码保存'],
    ['邮箱验证激活', '手机号验证', '账户安全检查']
  ];
  return features[authStep.value - 1];
});

// 用户认证演示控制
const nextAuthStep = () => {
  if (authStep.value < 3) {
    authStep.value++;
    message.success(`已切换到第 ${authStep.value} 步`);
  }
};

const previousAuthStep = () => {
  if (authStep.value > 1) {
    authStep.value--;
    message.success(`已切换到第 ${authStep.value} 步`);
  }
};

const resetAuthDemo = () => {
  authStep.value = 1;
  message.info('用户认证演示已重置');
};

// 目标管理演示状态
const goalStep = ref(1);

// 目标步骤标题
const goalStepTitle = computed(() => {
  const titles = ['基本信息设置', '目标详细设定', '时间规划安排', '优先级确定'];
  return titles[goalStep.value - 1];
});

// 目标步骤描述
const goalStepDescription = computed(() => {
  const descriptions = [
    '填写目标名称、详细描述，选择目标类别',
    '设定具体可衡量的目标指标和完成标准',
    '安排合理的开始时间和截止日期',
    '确定目标的重要性和紧急程度'
  ];
  return descriptions[goalStep.value - 1];
});

// 目标步骤特性
const goalStepFeatures = computed(() => {
  const features = [
    ['智能名称建议', '分类自动匹配', '描述模板提供'],
    ['SMART原则检查', '指标量化建议', '完成标准定义'],
    ['智能时间规划', '里程碑提醒', '进度跟踪设置'],
    ['重要性评分', '紧急度评级', '资源分配建议']
  ];
  return features[goalStep.value - 1];
});

// 目标管理演示控制
const nextGoalStep = () => {
  if (goalStep.value < 4) {
    goalStep.value++;
    message.success(`已切换到第 ${goalStep.value} 步`);
  }
};

const previousGoalStep = () => {
  if (goalStep.value > 1) {
    goalStep.value--;
    message.success(`已切换到第 ${goalStep.value} 步`);
  }
};

const resetGoalDemo = () => {
  goalStep.value = 1;
  message.info('目标管理演示已重置');
};

// AI助手演示状态
const aiStep = ref(1);

// AI步骤标题
const aiStepTitle = computed(() => {
  const titles = ['AI智能问答', 'AI目标生成'];
  return titles[aiStep.value - 1];
});

// AI步骤描述
const aiStepDescription = computed(() => {
  const descriptions = [
    '24/7智能问答助手，提供实时响应和多轮对话',
    '基于SMART原则的个性化目标生成器'
  ];
  return descriptions[aiStep.value - 1];
});

// AI步骤特性
const aiStepFeatures = computed(() => {
  const features = [
    ['自然语言理解', '多领域知识覆盖', '上下文记忆', '实时响应'],
    ['智能分析需求', '个性化定制', '实时优化', '行动建议']
  ];
  return features[aiStep.value - 1];
});

// AI助手演示控制
const nextAiStep = () => {
  if (aiStep.value < 2) {
    aiStep.value++;
    message.success(`已切换到第 ${aiStep.value} 步`);
  }
};

const previousAiStep = () => {
  if (aiStep.value > 1) {
    aiStep.value--;
    message.success(`已切换到第 ${aiStep.value} 步`);
  }
};

const resetAiDemo = () => {
  aiStep.value = 1;
  message.info('AI助手演示已重置');
};

// 页面初始化
onMounted(() => {
  // 可以在这里添加一些初始化逻辑
});
</script>

<style scoped>
/* 整体布局 */
.user-guide-layout {
  height: calc(100vh - 94px);
  display: flex;
  flex-direction: row;
  overflow: hidden;
  background: var(--n-layout-color);
  transition: background-color 0.3s ease;
}

/* 黑绿/白绿主题变量 */
:root {
  --primary-green: #00c9a7; /* 项目主要绿色 - 深色主题 */
  --primary-green-light: #3cb371;
  --primary-green-dark: #008c72; /* 项目主要绿色 - 浅色主题 */
  --text-light: #333333;
  --text-dark: #f0f0f0;
  --bg-light: #ffffff;
  --bg-dark: #1a1a1a;
  --border-light: #e0e0e0;
  --border-dark: #333333;
  --card-bg-light: #fafafa;
  --card-bg-dark: #2a2a2a;
  --hover-light: #f5f5f5;
  --hover-dark: #333333;
}

/* 英雄区域动画样式 */
.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

.floating-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
  background: var(--primary-green);
}

.shape-2 {
  width: 120px;
  height: 120px;
  top: 50%;
  right: 15%;
  animation-delay: 2s;
  background: var(--primary-green);
}

.shape-3 {
  width: 60px;
  height: 60px;
  bottom: 30%;
  left: 20%;
  animation-delay: 4s;
  background: var(--primary-green);
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.animate-bounce {
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 53%, 80%, 100% { transform: translateY(0); }
  40%, 43% { transform: translateY(-10px); }
  70% { transform: translateY(-5px); }
  90% { transform: translateY(-2px); }
}

.animate-fade-in-up {
  opacity: 0;
  transform: translateY(30px);
  animation: fadeInUp 0.8s forwards;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.pulse-animation {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin: 32px 0;
  max-width: 600px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  background: rgba(46, 139, 87, 0.1);
  border: 1px solid rgba(46, 139, 87, 0.2);
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 201, 167, 0.2);
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
  color: var(--primary-green);
}

.stat-label {
  font-size: 14px;
  color: var(--text-light);
}

.btn-primary-gradient {
  background: linear-gradient(135deg, var(--primary-green), var(--primary-green-light));
  border: none;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 201, 167, 0.3);
}

.btn-primary-gradient:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 201, 167, 0.4);
}

.btn-primary-gradient::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s;
}

.btn-primary-gradient:hover::before {
  left: 100%;
}

.btn-secondary-outline {
  background: transparent;
  border: 2px solid var(--primary-green);
  color: var(--primary-green);
  padding: 10px 22px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-secondary-outline:hover {
  background: var(--primary-green);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 139, 87, 0.2);
}

.features-showcase {
  margin-top: 80px;
  padding: 0 20px;
}

.features-grid-enhanced {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 32px;
  margin-top: 48px;
}

.feature-card-enhanced {
  background: var(--card-bg-light);
  border: 1px solid var(--border-light);
  border-radius: 16px;
  text-align: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  padding: 32px 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.feature-card-enhanced::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-green);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.feature-card-enhanced:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 201, 167, 0.15);
}

.feature-card-enhanced:hover::before {
  transform: scaleX(1);
}

.feature-icon-enhanced {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 201, 167, 0.1);
  color: var(--primary-green);
  transition: all 0.3s ease;
}

.feature-card-enhanced:hover .feature-icon-enhanced {
  background: var(--primary-green);
  color: white;
}

.feature-card-enhanced h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-light);
}

.feature-card-enhanced p {
  line-height: 1.6;
  margin-bottom: 24px;
  color: #666;
}

.feature-progress {
  margin-top: auto;
}

/* 交互式演示控制台样式 */
.demo-console {
  background: var(--card-bg-light);
  border: 1px solid var(--border-light);
  border-radius: 16px;
  margin: 32px 0;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.demo-console:hover {
  box-shadow: 0 8px 24px rgba(0, 201, 167, 0.1);
}

.demo-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(46, 139, 87, 0.05);
}

.demo-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  color: var(--text-light);
}

.demo-controls {
  display: flex;
  gap: 8px;
}

.demo-content {
  padding: 32px 24px;
  text-align: center;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.demo-step {
  opacity: 0;
  transform: translateY(20px);
  animation: slideInUp 0.5s forwards;
}

@keyframes slideInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.demo-step-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(46, 139, 87, 0.1);
  color: var(--primary-green);
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-8px); }
}

.demo-step h4 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-light);
}

.demo-step p {
  line-height: 1.6;
  margin-bottom: 24px;
  font-size: 14px;
  color: #666;
}

.demo-features {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 300px;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  padding: 8px 0;
  color: #666;
}

.feature-item .n-icon {
  flex-shrink: 0;
  color: var(--primary-green);
}

.demo-progress {
  padding: 20px 24px;
  border-top: 1px solid var(--border-light);
}

.progress-label {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  text-align: center;
  color: #666;
}

/* 快速开始步骤样式 */
.quick-steps {
  margin-bottom: 24px;
}

/* 主容器 */
.guide-container {
  display: flex;
  flex: 1;
  max-width: 100%;
  min-height: calc(100vh - 60px);
}

/* 左侧边栏 */
.guide-sidebar {
  width: 260px;
  padding: 6% 0;
  overflow-x: hidden;
  height: 100vh;
  top: 0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: var(--n-layout-color);
  border-right: 1px solid var(--n-border-color);
}

.sidebar-header {
  padding: 20px 20px 16px;
  border-bottom: 1px solid var(--n-border-color);
  margin: 0 16px 16px;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-light);
  font-weight: 600;
}

.sidebar-footer {
  margin-top: auto;
  padding: 20px;
  border-top: 1px solid var(--n-border-color);
  flex-shrink: 0;
  margin: 0 16px;
}

.help-section h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-light);
}

.help-section p {
  font-size: 14px;
  margin: 0 0 16px 0;
  color: #666;
}

.help-button {
  width: 100%;
  background: var(--primary-green);
  color: white;
  border: none;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.help-button:hover {
  background: var(--primary-green-dark);
  transform: translateY(-1px);
}

/* 核心功能快捷入口 */
.core-features-section {
  margin: 20px 16px;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid var(--n-border-color);
  background: var(--n-card-color);
}

.core-features-header {
  margin-bottom: 16px;
}

.core-features-header h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-light);
}

.core-features-header p {
  font-size: 12px;
  margin: 0;
  color: #666;
}

.core-features-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.core-feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--n-border-color);
  background: var(--n-card-color);
}

.core-feature-item:hover {
  transform: translateY(-2px);
  border-color: var(--primary-green);
  box-shadow: 0 4px 12px rgba(46, 139, 87, 0.1);
}

.core-feature-icon {
  flex-shrink: 0;
}

.core-feature-content span {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-light);
}

/* 右侧主内容区域 */
.guide-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--n-layout-color);
}

/* 确保n-layout-content样式正确 */
:deep(.n-layout-content) {
  flex: 1;
  overflow: hidden;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 30px;
  border-bottom: 1px solid var(--n-border-color);
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 内容包装器 */
.guide-content-wrapper {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background: var(--n-layout-color);
}

/* 欢迎页面样式 */
.hero-section {
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  margin-bottom: 50px;
  background: linear-gradient(135deg, rgba(46, 139, 87, 0.05), rgba(255, 255, 255, 0.1));
  border: 1px solid rgba(46, 139, 87, 0.1);
  position: relative;
  overflow: hidden;
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-icon {
  margin-bottom: 24px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: var(--text-light);
  background: linear-gradient(to right, var(--primary-green), #008c72);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 20px;
  margin: 0 0 32px 0;
  line-height: 1.6;
  color: #666;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 功能区域 */
.features-section {
  margin-bottom: 60px;
}

.section-title {
  text-align: center;
  margin-bottom: 40px;
}

.section-title h2 {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: var(--text-light);
  background: linear-gradient(to right, var(--primary-green), #008c72);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-title p {
  font-size: 18px;
  margin: 0;
  color: #666;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
}

.feature-card {
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--card-bg-light);
  border: 1px solid var(--border-light);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 201, 167, 0.1);
}

.feature-icon {
  flex-shrink: 0;
  background: rgba(46, 139, 87, 0.1);
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-green);
  transition: all 0.3s ease;
}

.feature-card:hover .feature-icon {
  background: var(--primary-green);
  color: white;
}

.feature-content h3 {
  font-size: 20px;
  margin: 0 0 8px 0;
  font-weight: 600;
  color: var(--text-light);
}

.feature-content p {
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
  color: #666;
}

/* 快速开始区域 */
.quick-start-section {
  border-radius: 16px;
  padding: 40px;
  background: var(--card-bg-light);
  border: 1px solid var(--border-light);
  margin-bottom: 40px;
}

.steps-container {
  max-width: 600px;
  margin: 0 auto;
}

.step-actions {
  text-align: center;
  margin-top: 32px;
}

/* 章节头部 */
.section-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid var(--n-border-color);
}

.section-title-content {
  flex: 1;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: var(--text-light);
}

.section-subtitle {
  font-size: 18px;
  margin: 0;
  color: #666;
}

/* 章节内容 */
.guide-section {
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  background: var(--n-card-color);
  border: 1px solid var(--n-border-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.content-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 201, 167, 0.08);
}

/* 目标类型卡片 */
.goal-types {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.type-item {
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  background: var(--card-bg-light);
  border: 1px solid var(--border-light);
  text-align: center;
}

.type-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 201, 167, 0.1);
}

.type-item strong {
  display: block;
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-light);
}

.type-item p {
  font-size: 14px;
  margin: 0;
  line-height: 1.4;
  color: #666;
}

/* 功能特点网格 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.feature-item {
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
  text-align: center;
  background: var(--card-bg-light);
  border: 1px solid var(--border-light);
}

.feature-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 201, 167, 0.1);
}

.feature-item strong {
  display: block;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-light);
}

.feature-item p {
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
  color: #666;
}

/* 键盘按键样式 */
kbd {
  background: var(--card-bg-light);
  border: 1px solid var(--border-light);
  border-radius: 4px;
  padding: 2px 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  display: inline-block;
  margin: 0 2px;
  color: var(--text-light);
}

/* 增强的标题样式 */
.section-content h4 {
  font-size: 18px;
  margin: 0 0 16px 0;
  font-weight: 600;
  border-bottom: 1px solid var(--n-border-color);
  padding-bottom: 8px;
  color: var(--text-light);
}

.section-content h5 {
  font-size: 16px;
  margin: 24px 0 12px 0;
  font-weight: 600;
  color: var(--text-light);
}

/* 增强的列表样式 */
.section-content ul {
  padding-left: 20px;
  margin: 12px 0;
}

.section-content li {
  line-height: 1.6;
  margin-bottom: 8px;
  position: relative;
  color: #666;
}

/* 增强的段落样式 */
.section-content p {
  line-height: 1.6;
  margin: 12px 0;
  color: #666;
}

/* 优化内容间距 */
.guide-section {
  margin-bottom: 40px;
}

.section-content > * + * {
  margin-top: 24px;
}

/* 样式覆盖 */
:deep(.n-menu-item) {
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.n-menu-item:hover) {
  background: rgba(46, 139, 87, 0.1) !important;
  color: var(--primary-green) !important;
}

:deep(.n-card) {
  border-radius: 12px;
  background: var(--n-card-color);
  border: 1px solid var(--n-border-color);
}

:deep(.n-card__header) {
  border-radius: 12px 12px 0 0;
  padding: 20px !important;
  background: rgba(46, 139, 87, 0.03);
}

:deep(.n-card__content) {
  padding: 20px !important;
}

:deep(.n-steps) {
  margin-top: 20px;
}

:deep(.n-alert) {
  margin-top: 16px;
  border-radius: 8px;
}

:deep(.n-input) {
  margin-bottom: 12px;
  border-radius: 8px;
}

:deep(.n-button) {
  margin-right: 8px;
  margin-bottom: 8px;
  border-radius: 8px;
}

/* 主题适配样式 - 使用Naive UI变量 */
:deep(.n-card) {
  backdrop-filter: blur(10px);
}

:deep(.n-card__header) {
  backdrop-filter: blur(10px);
}

:deep(.n-card__content) {
  backdrop-filter: blur(10px);
}

/* 流程图组件样式 */
.flow-chart {
  border-radius: 12px;
  padding: 24px;
  margin: 24px 0;
  background: var(--card-bg-light);
  border: 1px solid var(--border-light);
}

.flow-step {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid var(--border-light);
}

.flow-step:hover {
  transform: translateY(-2px);
  border-color: var(--primary-green);
  box-shadow: 0 4px 12px rgba(46, 139, 87, 0.1);
}

.flow-step.active {
  background: rgba(46, 139, 87, 0.05);
  border-color: var(--primary-green);
}

.flow-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
  background: var(--primary-green);
  color: white;
}

.flow-content h4 {
  margin-bottom: 8px;
  font-size: 16px;
  color: var(--text-light);
}

.flow-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: #666;
}

.flow-arrow {
  margin: 0 16px;
  font-size: 20px;
  color: var(--primary-green);
}

/* 渐进式步骤样式 */
.progressive-steps {
  margin: 24px 0;
}

.step-container {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 12px;
  border-radius: 8px;
}

.step-container:hover {
  background: rgba(46, 139, 87, 0.05);
}

.step-container:hover .step-circle {
  transform: scale(1.1);
}

.step-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 20px;
  position: relative;
  transition: all 0.3s ease;
  background: var(--primary-green-light);
  color: white;
}

.step-content {
  flex: 1;
}

.step-title {
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 16px;
  color: var(--text-light);
}

.step-description {
  line-height: 1.5;
  font-size: 14px;
  color: #666;
}

.step-connector {
  position: absolute;
  left: 50%;
  top: 50px;
  width: 2px;
  height: 30px;
  margin-left: -1px;
  background: var(--primary-green);
}

/* 模拟界面样式 */
.mock-interface {
  border-radius: 12px;
  overflow: hidden;
  margin: 24px 0;
  border: 1px solid var(--border-light);
  background: var(--card-bg-light);
}

.mock-header {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f0f0f0;
}

.mock-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ff5f56;
}

.mock-dot:nth-child(2) {
  background: #ffbd2e;
}

.mock-dot:nth-child(3) {
  background: #27c93f;
}

.mock-content {
  padding: 20px;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mock-button {
  background: var(--primary-green);
  border: none;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-right: 8px;
  margin-bottom: 8px;
  font-weight: 500;
}

.mock-button:hover {
  background: var(--primary-green-dark);
  transform: translateY(-1px);
}

.mock-input {
  border-radius: 6px;
  margin: 4px 0;
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border-light);
}

/* 悬停提示系统 */
.tooltip-trigger {
  position: relative;
  cursor: help;
  border-bottom: 1px dotted var(--primary-green);
  color: var(--primary-green);
}

.tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%) translateY(8px);
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  z-index: 1000;
  background: var(--primary-green);
  color: white;
}

.tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 4px solid transparent;
  border-top-color: var(--primary-green);
}

.tooltip-trigger:hover .tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(-8px);
}

/* 可交互的流程图样式 */
.interactive-flow {
  margin: 24px 0;
}

.flow-node {
  border-radius: 12px;
  padding: 20px;
  margin: 16px 0;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  background: var(--card-bg-light);
  border: 1px solid var(--border-light);
}

.flow-node:hover {
  transform: scale(1.02);
  border-color: var(--primary-green);
  box-shadow: 0 6px 16px rgba(0, 201, 167, 0.15);
}

.flow-node.selected {
  background: rgba(0, 201, 167, 0.1);
  border-color: var(--primary-green);
  box-shadow: 0 0 0 2px rgba(0, 201, 167, 0.2);
}

.flow-node-title {
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 16px;
  color: var(--text-light);
}

.flow-node-description {
  line-height: 1.5;
  font-size: 14px;
  color: #666;
}

.flow-connection {
  display: flex;
  justify-content: center;
  margin: 16px 0;
  font-size: 24px;
  color: var(--primary-green);
}

/* 视频演示区域 */
.video-demo {
  border-radius: 12px;
  overflow: hidden;
  margin: 24px 0;
  border: 1px solid var(--border-light);
}

.video-placeholder {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, var(--primary-green), var(--primary-green-light));
  color: white;
  font-size: 18px;
}

.video-placeholder::before {
  content: '▶';
  font-size: 48px;
  opacity: 0.8;
  margin-right: 16px;
}

.video-controls {
  padding: 16px;
  text-align: center;
  background: var(--card-bg-light);
}

/* 增强的按钮样式 */
.enhanced-button {
  background: linear-gradient(135deg, var(--primary-green), var(--primary-green-light));
  border: none;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 201, 167, 0.3);
}

.enhanced-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 201, 167, 0.4);
}

.enhanced-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  transition: left 0.5s;
}

.enhanced-button:hover::before {
  left: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .guide-sidebar {
    width: 70px;
  }
  
  .sidebar-header, .core-features-header, .core-feature-content, .help-section {
    display: none;
  }
  
  .core-features-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-title {
    font-size: 36px;
  }
  
  .hero-subtitle {
    font-size: 18px;
  }
  
  .features-grid-enhanced {
    grid-template-columns: 1fr;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-section {
    padding: 40px 20px;
  }
  
  .guide-content-wrapper {
    padding: 20px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .section-icon {
    align-self: center;
  }
  
  .hero-stats {
    grid-template-columns: 1fr;
  }
}

/* 暗色主题适配 */
:deep(.n-theme--dark) {
  --text-light: #f0f0f0;
  --text-dark: #f0f0f0;
  --bg-light: #1a1a1a;
  --bg-dark: #1a1a1a;
  --border-light: #333333;
  --border-dark: #333333;
  --card-bg-light: #2a2a2a;
  --card-bg-dark: #2a2a2a;
  --hover-light: #333333;
  --hover-dark: #333333;
}

:deep(.n-theme--dark) .stat-item {
  background: rgba(0, 201, 167, 0.15);
  border: 1px solid rgba(0, 201, 167, 0.3);
}

:deep(.n-theme--dark) .feature-card-enhanced {
  background: var(--card-bg-dark);
  border: 1px solid var(--border-dark);
}

:deep(.n-theme--dark) .feature-icon-enhanced {
  background: rgba(0, 201, 167, 0.2);
}

:deep(.n-theme--dark) .demo-console {
  background: var(--card-bg-dark);
  border: 1px solid var(--border-dark);
}

:deep(.n-theme--dark) .content-card {
  background: var(--n-card-color);
  border: 1px solid var(--n-border-color);
}

:deep(.n-theme--dark) .flow-chart {
  background: var(--card-bg-dark);
  border: 1px solid var(--border-dark);
}

:deep(.n-theme--dark) .flow-step {
  border: 1px solid var(--border-dark);
}

:deep(.n-theme--dark) .mock-interface {
  border: 1px solid var(--border-dark);
  background: var(--card-bg-dark);
}

:deep(.n-theme--dark) .mock-header {
  background: #333;
}

:deep(.n-theme--dark) .type-item,
:deep(.n-theme--dark) .feature-item {
  background: var(--card-bg-dark);
  border: 1px solid var(--border-dark);
}

:deep(.n-theme--dark) .hero-section {
  background: linear-gradient(135deg, rgba(0, 201, 167, 0.1), rgba(0, 0, 0, 0.2));
  border: 1px solid rgba(0, 201, 167, 0.2);
}

:deep(.n-theme--dark) .quick-start-section {
  background: var(--card-bg-dark);
  border: 1px solid var(--border-dark);
}

:deep(.n-theme--dark) .feature-card {
  background: var(--card-bg-dark);
  border: 1px solid var(--border-dark);
}

:deep(.n-theme--dark) .core-feature-item {
  background: var(--n-card-color);
}
</style>