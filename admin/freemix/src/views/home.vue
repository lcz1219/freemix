<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'home-container' : 'home-container-light'">
    <!-- <common>
      <template #content> -->
        <n-layout-content class="main-content-wrapper">
          <div class="main-content">
            <!-- 英雄区域 -->
            <section class="hero-section">
              <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">掌控你的目标，衡量你的成功</h1>
              <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
                目标追踪者是一款强大的目标管理系统，帮助您设定、跟踪并完成个人和职业目标。可视化您的进度，庆祝每一个里程碑。</p>

              <div class="hero-actions">
                <n-button type="primary" size="large" round strong @click="addGoal">
                  <template #icon>
                    <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                        fill="currentColor">
                        <path d="M20,12H4v-1c0-0.6,0.4-1,1-1h14c0.6,0,1,0.4,1,1V12z"></path>
                        <path d="M4,12h16v1c0,0.6-0.4,1-1,1s-1-0.4-1-1V12z"></path>
                        <path
                          d="M20,10H4c-0.6,0-1,0.4-1,1v2c0,0.6,0.4,1,1,1s1-0.4,1-1v-2C21,10.4,20.6,10,20,10z M20,12H4v-1h16V12z">
                        </path>
                      </svg></n-icon>
                  </template>
                  添加新目标
                </n-button>
                <n-button size="large" round @click="showGuide">
                  <template #icon>
                    <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                        fill="currentColor">
                        <path
                          d="M13.5,4.5c0,0.8-0.7,1.5-1.5,1.5s-1.5-0.7-1.5-1.5S11.2,3,12,3S13.5,3.7,13.5,4.5z M13.5,12c0,0.8-0.7,1.5-1.5,1.5 s-1.5-0.7-1.5-1.5S11.2,10.5,12,10.5S13.5,11.2,13.5,12z M13.5,19.5c0,0.8-0.7,1.5-1.5,1.5s-1.5-0.7-1.5-1.5s0.7-1.5,1.5-1.5 S13.5,18.7,13.5,19.5z">
                        </path>
                      </svg></n-icon>
                  </template>
                  使用指南
                </n-button>
              </div>
            </section>
            <StatsOverview :total-goals="totalGoals" :completed-goals="completedGoals"
              :in-progress-goals="inProgressGoals" :expired-goals="expiredGoals" />

            <!-- 功能卡片区域 -->
            <section class="features-section">
              <n-card class="feature-card" v-show="false">
                <div class="card-header">
                  <div ref="progressChart" class="echart-icon"></div>
                  <h2 class="card-title">目标概览</h2>
                </div>

                <div class="stats-container">
                  <div class="stat-item">
                    <div class="stat-value">{{ goalIngCount }}</div>
                    <div :class="checkThemebyStat">进行中</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value" style="color: #00c9a7;">{{ goalFinishCount }}</div>
                    <div :class="checkThemebyStat">已完成</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value" style="color: #ff6b6b;">{{ goalExpireCount }}</div>
                    <div :class="checkThemebyStat">已过期</div>
                  </div>
                </div>

                <div class="chart-container">
                  <canvas ref="progressDetailChart"></canvas>
                </div>
              </n-card>


              <n-card class="feature-card">
                <div class="card-header">
                  <div ref="trendChartIcon" class="echart-icon"></div>
                  <h2 class="card-title">完成趋势</h2>
                </div>

                <div class="chart-container">
                  <canvas ref="trendChart"></canvas>
                </div>
              </n-card>
            </section>

            <!-- 目标卡片网格 -->
            <section class="targets-section">
              <h2 class="section-title">我的目标</h2>
              <div class="target-grid">
                <n-card v-for="(goal, index) in goals" :key="index" class="target-card" @click="showGoalDetail(goal)">
                  <div class="card-header">
                    <h3 class="goal-title">{{ goal.title }}</h3>
                    <n-tag
                      :type="goal.status === 'completed' ? 'success' : goal.status === 'expired' ? 'error' : 'warning'">
                      {{ goal.status === 'completed' ? '已完成' : goal.status === 'expired' ? '已过期' : '进行中' }}
                    </n-tag>
                  </div>

                  <n-progress type="line" :percentage="goal.progress" :indicator-placement="'inside'" :height="8"
                    processing :rail-color="isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgb(235 235 235)'"
                    :fill-color="'linear-gradient(90deg, #81c683, #4b0082)'" />

                  <div class="goal-details">
                    <div :class="checkThemebyDetail">
                      <n-icon size="16">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                          fill="currentColor">
                          <path
                            d="M12,7c-2.8,0-5,2.2-5,5s2.2,5,5,5s5-2.2,5-5S14.8,7,12,7z M12,14.5c-1.4,0-2.5-1.1-2.5-2.5s1.1-2.5,2.5-2.5 s2.5,1.1,2.5,2.5S13.4,14.5,12,14.5z" />
                          <path d="M12,5c-0.6,0-1-0.4-1-1V1c0-0.6,0.4-1,1-1s1,0.4,1,1v3C13,4.6,12.6,5,12,5z" />
                          <path d="M12,19c-0.6,0-1,0.4-1,1v3c0,0.6,0.4,1,1,1s1-0.4,1-1v-3C13,19.4,12.6,19,12,19z" />
                          <path d="M23,11h-3c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S23.6,11,23,11z" />
                          <path d="M4,11H1c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S4.6,11,4,11z" />
                          <path
                            d="M18.7,6.3c0.4-0.4,0.4-1,0-1.4l-2.1-2.1c-0.4-0.4-1-0.4-1.4,0s-0.4,1,0,1.4l2.1,2.1C17.7,6.7,18.3,6.7,18.7,6.3z" />
                          <path
                            d="M5.3,17.7c-0.4,0.4-0.4,1,0,1.4l2.1,2.1c0.4,0.4,1,0.4,1.4,0s0.4-1,0-1.4l-2.1-2.1C6.3,17.3,5.7,17.3,5.3,17.7z" />
                          <path
                            d="M18.7,17.7c-0.4-0.4-1-0.4-1.4,0l-2.1,2.1c-0.4,0.4-0.4,1,0,1.4s1,0.4,1.4,0l2.1-2.1C19.1,18.7,19.1,18.1,18.7,17.7z" />
                          <path
                            d="M5.3,6.3c0.4,0.4,1,0.4,1.4,0l2.1-2.1c0.4-0.4,0.4-1,0-1.4s-1-0.4-1.4,0L5.3,4.9C4.9,5.3,4.9,5.9,5.3,6.3z" />
                        </svg>
                      </n-icon>
                      <span>截止: {{ goal.deadlineString }}</span>
                    </div>
                    <div :class="checkThemebyDetail">
                      <n-icon size="16">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                          fill="currentColor">
                          <path
                            d="M12,12c2.2,0,4-1.8,4-4s-1.8-4-4-4s-4,1.8-4,4S9.8,12,12,12z M12,6c1.1,0,2,0.9,2,2s-0.9,2-2,2s-2-0.9-2-2 S10.9,6,12,6z" />
                          <path d="M12,14c-4.4,0-8,3.6-8,8h2c0-3.3,2.7-6,6-6s6,2.7,6,6h2C20,17.6,16.4,14,12,14z" />
                        </svg>
                      </n-icon>
                      <span>负责人: {{ goal.owner }}</span>
                    </div>
                  </div>
                </n-card>
              </div>
            </section>

            <!-- 目标详情模态框 -->
            <GoalDetail v-model:show="showDetailModal" :goal="selectedGoal" @save="saveGoal" @updateGoal="getGoals" />

            <!-- 使用指南模态框 -->
            <n-modal v-model:show="showGuideModal" preset="card" :style="{ width: '90%', maxWidth: '1200px' }"
              size="huge" :bordered="false" :segmented="{ content: true, footer: true }" title="使用指南"
              :class="isDark ? 'modal-dark' : 'modal-light'" @after-leave="resetGuide">
              <div class="guide-container">
                <!-- 侧边导航 -->
                <div class="guide-sidebar">
                  <n-menu v-model:value="currentGuideSection" :options="guideMenuOptions" :collapsed="false"
                    :collapsed-width="10" :collapsed-icon-size="22" :indent="18" mode="horizontal" />
                </div>

                <!-- 主内容区域 -->
                <div class="guide-content">
                  <!-- 欢迎页面 -->
                  <div v-if="currentGuideSection === 'welcome'" class="guide-section">
                    <h2>欢迎使用目标追踪系统</h2>
                    <div class="welcome-content">
                      <div class="welcome-text">
                        <p>这是一个功能强大的目标管理系统，帮助您设定、跟踪并完成个人和职业目标。</p>
                        <p>通过本指南，您将学会如何：</p>
                        <ul>
                          <li>创建和管理目标</li>
                          <li>跟踪目标进度</li>
                          <li>分析目标完成情况</li>
                          <li>与团队成员协作</li>
                        </ul>
                      </div>
                      <div class="welcome-image">
                        <n-image width="100" :src="welcomeImage" preview-disabled />
                      </div>
                    </div>
                    <n-alert title="提示" type="info">
                      您可以通过左侧导航菜单访问不同的指南章节，或使用底部的导航按钮逐步浏览。
                    </n-alert>
                  </div>

                  <!-- 创建目标 -->
                  <div v-else-if="currentGuideSection === 'create'" class="guide-section">
                    <h2>创建目标</h2>
                    <div class="section-content">
                      <p>创建目标是使用本系统的第一步。一个明确的目标是成功的一半。</p>

                      <h3>创建步骤</h3>
                      <n-steps :current="createStep" horizontal>
                        <n-step title="点击添加按钮">
                          <template #description>
                            <p>在主页点击"添加新目标"按钮，进入目标创建页面。</p>
                            <n-image width="400" :src="createStep1Image" preview-disabled />
                          </template>
                        </n-step>
                        <n-step title="填写基本信息">
                          <template #description>
                            <p>输入目标的标题和详细描述，确保描述清晰明确。</p>
                            <n-image width="400" :src="createStep2Image" preview-disabled />
                          </template>
                        </n-step>
                        <n-step title="设置截止日期">
                          <template #description>
                            <p>为您的目标设置一个合理的截止日期，这有助于您保持专注。</p>
                          </template>
                        </n-step>
                        <n-step title="指定负责人">
                          <template #description>
                            <p>如果这是一个团队目标，请指定负责人。</p>
                          </template>
                        </n-step>
                        <n-step title="设置优先级">
                          <template #description>
                            <p>根据重要性设置优先级：低、中、高或紧急。</p>
                          </template>
                        </n-step>
                        <n-step title="添加标签">
                          <template #description>
                            <p>使用标签对目标进行分类，如"学习"、"工作"、"健康"等。</p>
                          </template>
                        </n-step>
                      </n-steps>

                      <h3>最佳实践</h3>
                      <n-grid :cols="2" :x-gap="12" :y-gap="12">
                        <n-grid-item>
                          <n-card title="明确具体">
                            <p>目标应该具体明确，避免模糊不清的表述。</p>
                          </n-card>
                        </n-grid-item>
                        <n-grid-item>
                          <n-card title="可衡量">
                            <p>确保目标可以量化，便于跟踪进度。</p>
                          </n-card>
                        </n-grid-item>
                        <n-grid-item>
                          <n-card title="可实现">
                            <p>设定具有挑战性但可实现的目标。</p>
                          </n-card>
                        </n-grid-item>
                        <n-grid-item>
                          <n-card title="时限性">
                            <p>为每个目标设置明确的截止日期。</p>
                          </n-card>
                        </n-grid-item>
                      </n-grid>
                    </div>
                  </div>

                  <!-- 跟踪进度 -->
                  <div v-else-if="currentGuideSection === 'track'" class="guide-section">
                    <h2>跟踪进度</h2>
                    <div class="section-content">
                      <p>定期跟踪目标进度是确保成功的关键。</p>

                      <h3>进度查看方式</h3>
                      <n-tabs type="line" animated>
                        <n-tab-pane name="dashboard" tab="仪表板视图">
                          <p>在主页仪表板上，您可以一目了然地看到所有目标的状态：</p>
                          <ul>
                            <li><strong>目标概览</strong>：显示进行中、已完成和已过期的目标数量</li>
                            <li><strong>近期目标</strong>：按时间顺序展示即将到期的目标</li>
                            <li><strong>完成趋势</strong>：展示目标完成的历史趋势</li>
                          </ul>
                          <n-image width="100" :src="dashboardImage" preview-disabled />
                        </n-tab-pane>
                        <n-tab-pane name="list" tab="列表视图">
                          <p>在"我的目标"区域可以查看所有已创建的目标：</p>
                          <ul>
                            <li>点击任意目标卡片可以查看和编辑详细信息</li>
                            <li>通过进度条直观了解目标完成情况</li>
                            <li>目标状态会自动更新为"进行中"、"已完成"或"已过期"</li>
                          </ul>
                          <n-image width="100" :src="listViewImage" preview-disabled />
                        </n-tab-pane>
                        <n-tab-pane name="detail" tab="详情视图">
                          <p>在目标详情页面，您可以：</p>
                          <ul>
                            <li>更新目标进度</li>
                            <li>添加备注和心得</li>
                            <li>修改目标信息</li>
                            <li>查看子目标完成情况</li>
                          </ul>
                          <n-image width="100" :src="detailViewImage" preview-disabled />
                        </n-tab-pane>
                      </n-tabs>

                      <h3>进度更新</h3>
                      <n-space vertical>
                        <n-alert title="手动更新" type="info">
                          您可以随时手动更新目标进度，建议每天或每周定期更新。
                        </n-alert>
                        <n-alert title="自动更新" type="success">
                          系统会根据子目标完成情况自动计算整体进度。
                        </n-alert>
                      </n-space>
                    </div>
                  </div>

                  <!-- 数据分析 -->
                  <div v-else-if="currentGuideSection === 'analyze'" class="guide-section">
                    <h2>数据分析</h2>
                    <div class="section-content">
                      <p>通过数据分析，您可以更好地了解自己的目标完成情况，并做出相应调整。</p>

                      <h3>图表解读</h3>
                      <n-grid :cols="2" :x-gap="12" :y-gap="12">
                        <n-grid-item>
                          <n-card title="目标概览图">
                            <p>圆形图表显示了所有目标的状态分布：</p>
                            <ul>
                              <li><span style="color: #00c9a7;">绿色</span>表示已完成的目标</li>
                              <li><span style="color: #81c683;">紫色</span>表示进行中的目标</li>
                              <li><span style="color: #3a3a4a;">灰色</span>表示未开始的目标</li>
                            </ul>
                            <n-image width="100" :src="overviewChartImage" preview-disabled />
                          </n-card>
                        </n-grid-item>
                        <n-grid-item>
                          <n-card title="完成趋势图">
                            <p>折线图展示了您在不同时间段的目标完成情况：</p>
                            <ul>
                              <li>横轴表示时间（月份）</li>
                              <li>纵轴表示完成的目标数量</li>
                              <li>通过趋势线可以了解您的目标完成效率</li>
                            </ul>
                            <n-image width="100" :src="trendChartImage" preview-disabled />
                          </n-card>
                        </n-grid-item>
                      </n-grid>

                      <h3>数据洞察</h3>
                      <n-collapse default-expanded-names="insight1">
                        <n-collapse-item name="insight1" title="目标完成效率分析">
                          <p>通过观察完成趋势图，您可以：</p>
                          <ul>
                            <li>识别自己的高效期和低效期</li>
                            <li>调整目标设定策略</li>
                            <li>合理安排工作和休息时间</li>
                          </ul>
                        </n-collapse-item>
                        <n-collapse-item name="insight2" title="目标类型分析">
                          <p>通过标签分类，您可以：</p>
                          <ul>
                            <li>了解自己在不同领域投入的时间和精力</li>
                            <li>平衡各类目标的比重</li>
                            <li>发现自己的兴趣和优势领域</li>
                          </ul>
                        </n-collapse-item>
                        <n-collapse-item name="insight3" title="时间管理分析">
                          <p>通过截止日期分析，您可以：</p>
                          <ul>
                            <li>评估自己设定目标的合理性</li>
                            <li>改进时间预估能力</li>
                            <li>减少目标过期的情况</li>
                          </ul>
                        </n-collapse-item>
                      </n-collapse>
                    </div>
                  </div>

                  <!-- 协作功能 -->
                  <div v-else-if="currentGuideSection === 'collaborate'" class="guide-section">
                    <h2>协作功能</h2>
                    <div class="section-content">
                      <p>与团队成员协作完成目标，提升整体效率。</p>

                      <h3>团队目标创建</h3>
                      <n-space vertical>
                        <n-alert title="指定负责人" type="info">
                          创建团队目标时，需要明确指定负责人，他将负责协调和跟进目标进度。
                        </n-alert>
                        <n-alert title="设置协作者" type="info">
                          可以为目标添加多个协作者，他们都可以更新目标进度和添加备注。
                        </n-alert>
                      </n-space>

                      <h3>协作最佳实践</h3>
                      <n-timeline>
                        <n-timeline-item title="明确分工" content="为每个团队成员分配具体的子目标或任务" />
                        <n-timeline-item title="定期同步" content="建立定期会议机制，同步目标进度和遇到的问题" />
                        <n-timeline-item title="透明沟通" content="使用备注功能记录重要信息，确保所有成员都能看到" />
                        <n-timeline-item title="及时反馈" content="对团队成员的贡献给予及时认可和反馈" />
                      </n-timeline>

                      <h3>权限管理</h3>
                      <n-table :bordered="false" :single-line="false">
                        <thead>
                          <tr>
                            <th>角色</th>
                            <th>权限</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr>
                            <td>创建者</td>
                            <td>完全控制权限，可以编辑所有信息，删除目标</td>
                          </tr>
                          <tr>
                            <td>负责人</td>
                            <td>可以编辑目标信息，更新进度，管理协作者</td>
                          </tr>
                          <tr>
                            <td>协作者</td>
                            <td>可以更新进度，添加备注，查看所有信息</td>
                          </tr>
                          <tr>
                            <td>观察者</td>
                            <td>只能查看目标信息，无法进行任何修改</td>
                          </tr>
                        </tbody>
                      </n-table>
                    </div>
                  </div>

                  <!-- 高级技巧 -->
                  <div v-else-if="currentGuideSection === 'advanced'" class="guide-section">
                    <h2>高级技巧</h2>
                    <div class="section-content">
                      <p>掌握这些高级技巧，让您的目标管理更加高效。</p>

                      <n-grid :cols="1" :x-gap="12" :y-gap="12">
                        <n-grid-item>
                          <n-card title="目标分解技巧">
                            <p>将大目标分解为小的、可执行的子目标：</p>
                            <ul>
                              <li>每个子目标应该是独立的、可衡量的</li>
                              <li>子目标之间应该有清晰的依赖关系</li>
                              <li>为每个子目标设置合理的截止日期</li>
                            </ul>
                          </n-card>
                        </n-grid-item>
                        <n-grid-item>
                          <n-card title="优先级管理">
                            <p>使用 Eisenhower 矩阵来管理目标优先级：</p>
                            <n-image width="100" :src="eisenhowerMatrixImage" preview-disabled />
                            <ul>
                              <li><strong>重要且紧急</strong>：立即处理</li>
                              <li><strong>重要但不紧急</strong>：计划处理</li>
                              <li><strong>不重要但紧急</strong>：委派处理</li>
                              <li><strong>不重要且不紧急</strong>：减少或删除</li>
                            </ul>
                          </n-card>
                        </n-grid-item>
                        <n-grid-item>
                          <n-card title="进度可视化">
                            <p>利用系统的图表功能进行进度可视化：</p>
                            <ul>
                              <li>定期查看目标概览图，了解整体状态</li>
                              <li>通过完成趋势图，监控自己的效率变化</li>
                              <li>使用标签云，了解自己关注的重点领域</li>
                            </ul>
                          </n-card>
                        </n-grid-item>
                      </n-grid>

                      <h3>个性化设置</h3>
                      <n-space>
                        <n-button @click="toggleDarkMode">切换深色模式</n-button>
                        <n-button @click="changeTheme">切换主题色</n-button>
                      </n-space>
                    </div>
                  </div>
                </div>
              </div>

              <template #footer>
                <div class="guide-footer">
                  <n-button @click="prevGuideSection" :disabled="currentGuideSection === 'welcome'">
                    <template #icon>
                      <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                          fill="currentColor">
                          <path d="M15.4,16.6l-4-4l4-4l-1.4-1.4l-5.4,5.4l5.4,5.4L15.4,16.6z" />
                        </svg></n-icon>
                    </template>
                    上一章节
                  </n-button>

                  <!-- <n-pagination
                v-model:page="currentGuidePage"
                :page-count="guideTotalPages"
                :page-sizes="[1]"
                @update:page="handlePageChange"
              /> -->

                  <n-button @click="nextGuideSection" :disabled="currentGuideSection === 'advanced'">
                    下一章节
                    <template #icon>
                      <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                          fill="currentColor">
                          <path d="M8.6,16.6l5.4-5.4L8.6,5.8L10,4.4l6.6,6.6L10,17.6L8.6,16.6z" />
                        </svg></n-icon>
                    </template>
                  </n-button>

                  <n-button @click="showGuideModal = false" type="tertiary">关闭</n-button>
                </div>
              </template>
            </n-modal>

            <!-- <n-layout-footer class="footer" bordered>
          <p>© 2025 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
        </n-layout-footer> -->
          </div>
        </n-layout-content>
      <!-- </template>
    </common> -->

    <!-- 主内容区域 -->

    <!-- 整合浮动按钮组件 -->
    <UnifiedFloatButton :goals="goals" :formatDate="formatDate" :checktype="checktype"
      @dateSelected="handleCalendarUpdate" />
  </n-layout>
</template>

<script setup>
import { ref, onMounted, inject, computed, watch, h } from 'vue';
import common from '@/views/common.vue';
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NButton,
  NIcon,
  NSwitch,
  NCard,
  NTimeline,
  NTimelineItem,
  NTag,
  NProgress,
  NAvatar,
  NModal,
  NSteps,
  NStep,
  useMessage,
  NMenu,
  NImage,
  NAlert,
  NTabs,
  NTabPane,
  NGrid,
  NGridItem,
  NSpace,
  NCollapse,
  NCollapseItem,
  NTable,
  NPagination
} from 'naive-ui';
import Chart from 'chart.js/auto';
import * as echarts from 'echarts/core';
import { PieChart, LineChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent
} from 'echarts/components';
import StatsOverview from '@/components/StatsOverview.vue';
import RecentGoals from '@/components/RecentGoals.vue';
import { CanvasRenderer } from 'echarts/renderers';
import { AccessibilitySharp, CalendarSharp } from '@vicons/ionicons5';
import { useRouter } from 'vue-router'
import { getMPaths, isSuccess } from '@/utils/request'
import { useStore } from 'vuex'
import NavBar from '@/components/NavBar.vue';
import GoalDetail from '@/components/GoalDetail.vue';
import UnifiedFloatButton from '@/components/UnifiedFloatButton.vue';
import { useUser } from '@/hooks/useUser';
import { useSettings } from '@/hooks/useSettings';
// import { log } from 'echarts/types/src/util/log.js';

// 图片占位符
const welcomeImage = "https://api.dicebear.com/7.x/miniavs/svg?seed=8";
const createStep1Image = "https://api.dicebear.com/7.x/miniavs/svg?seed=1";
const createStep2Image = "https://api.dicebear.com/7.x/miniavs/svg?seed=2";
const dashboardImage = "https://api.dicebear.com/7.x/miniavs/svg?seed=3";
const listViewImage = "https://api.dicebear.com/7.x/miniavs/svg?seed=4";
const detailViewImage = "https://api.dicebear.com/7.x/miniavs/svg?seed=5";
const overviewChartImage = "https://api.dicebear.com/7.x/miniavs/svg?seed=6";
const trendChartImage = "https://api.dicebear.com/7.x/miniavs/svg?seed=7";
const eisenhowerMatrixImage = "https://api.dicebear.com/7.x/miniavs/svg?seed=9";

const checkThemebyDetail = computed(() => {
  return isDark.value ? 'detail-item' : 'detail-item-light'
})
const checkThemebyStat = computed(() => {
  return isDark.value ? 'stat-label' : 'stat-label-light'
})
const totalGoals = computed(() => goals.value.length);
const completedGoals = computed(() => goals.value.filter(g => g.status === 'completed').length);
const inProgressGoals = computed(() => goals.value.filter(g => g.status === 'in-progress').length);
const expiredGoals = computed(() => goals.value.filter(g => g.status === 'expired').length);
const checktype = (val) => {
  switch (val.tags[0]) {
    case '学习':
      return 'success';
    case '工作':
      return 'info';
    case '生活':
      return 'warning';
    case '运动':
      return 'error';
  }
}
const goalFinishCount = computed(() => {
  const tmpGoals = JSON.parse(JSON.stringify(goals.value))
  return tmpGoals.filter(goal => goal.progress === 100).length
})
const goalExpireCount = computed(() => {
  const tmpGoals = JSON.parse(JSON.stringify(goals.value))
  return tmpGoals.filter(goal => Date.now() - Date.parse(goal.deadline) > 0).length
})
const goalIngCount = computed(() => {
  const tmpGoals = JSON.parse(JSON.stringify(goals.value))
  return tmpGoals.filter(goal => goal.progress !== 100 && Date.now() - Date.parse(goal.deadline) < 0).length
})
// 注册 ECharts 组件
echarts.use([
  PieChart,
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
  CanvasRenderer
]);


const router = useRouter()
// 图标组件
const SunIcon = {
  template: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
    <path d="M12,7c-2.8,0-5,2.2-5,5s2.2,5,5,5s5-2.2,5-5S14.8,7,12,7z M12,14.5c-1.4,0-2.5-1.1-2.5-2.5s1.1-2.5,2.5-2.5 s2.5,1.1,2.5,2.5S13.4,14.5,12,14.5z" />
    <path d="M12,5c-0.6,0-1-0.4-1-1V1c0-0.6,0.4-1,1-1s1,0.4,1,1v3C13,4.6,12.6,5,12,5z" />
    <path d="M12,19c-0.6,0-1,0.4-1,1v3c0,0.6,0.4,1,1,1s1-0.4,1-1v-3C13,19.4,12.6,19,12,19z" />
    <path d="M23,11h-3c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S23.6,11,23,11z" />
    <path d="M4,11H1c-0.6,0-1,0.4-1,1s0.4,1,1,1h3c0.6,0,1-0.4,1-1S4.6,11,4,11z" />
    <path d="M18.7,6.3c0.4-0.4,0.4-1,0-1.4l-2.1-2.1c-0.4-0.4-1-0.4-1.4,0s-0.4,1,0,1.4l2.1,2.1C17.7,6.7,18.3,6.7,18.7,6.3z" />
    <path d="M5.3,17.7c-0.4,0.4-0.4,1,0,1.4l2.1,2.1c0.4,0.4,1,0.4,1.4,0s0.4-1,0-1.4l-2.1-2.1C6.3,17.3,5.7,17.3,5.3,17.7z" />
    <path d="M18.7,17.7c-0.4-0.4-1-0.4-1.4,0l-2.1,2.1c-0.4,0.4-0.4,1,0,1.4s1,0.4,1.4,0l2.1-2.1C19.1,18.7,19.1,18.1,18.7,17.7z" />
    <path d="M5.3,6.3c0.4,0.4,1,0.4,1.4,0l2.1-2.1c0.4-0.4,0.4-1,0-1.4s-1-0.4-1.4,0L5.3,4.9C4.9,5.3,4.9,5.9,5.3,6.3z" />
  </svg>`
};

const MoonIcon = {
  template: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
    <path d="M20.9,15.3c-0.5-0.9-1.2-1.7-2-2.2c-0.2-0.1-0.4-0.1-0.6-0.1c-0.3,0-0.6,0.1-0.9,0.3c-0.8,0.4-1.3,1.1-1.5,1.9 c-0.2,0.8,0,1.7,0.6,2.4c0.9,1.1,2.2,1.6,3.5,1.4c0.2,0,0.5-0.1,0.7-0.2C21.6,18.4,21.8,16.8,20.9,15.3z M19.5,18.5 c-0.9,0.1-1.8-0.2-2.5-0.8c-0.3-0.3-0.5-0.7-0.6-1.1c-0.1-0.4-0.1-0.8,0.1-1.2c0.2-0.4,0.5-0.7,0.9-0.9 c0.3-0.1,0.5-0.2,0.8-0.1c0.7,0.2,1.3,0.6,1.7,1.2C20.4,16.5,20.3,17.7,19.5,18.5z" />
    <path d="M9,22c5.5,0,10-4.5,10-10c0-0.8-0.1-1.6-0.3-2.4c-0.1-0.4-0.5-0.7-0.9-0.6c-0.4,0.1-0.7,0.5-0.6,0.9 c0.2,0.7,0.3,1.4,0.3,2.1c0,4.4-3.6,8-8,8s-8-3.6-8-8c0-4.4,3.6-8,8-8c0.7,0,1.3,0.1,2,0.2c0.4,0.1,0.8-0.1,1-0.5 C13.1,2.6,13,2.2,12.6,2C11.7,1.7,10.8,1.6,9.9,1.6C4.4,1.6,0,6,0,11.5C0,17,4.5,22,9,22z" />
  </svg>`
};
const formatDate = (goal) => {
  // 检查deadline是否存在且为有效日期
  if (!goal) {

    return '未设置';
  }


  const date = new Date(goal);

  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    return '日期无效';
  }

  return `${date.getFullYear()}年${(date.getMonth() + 1).toString().padStart(2, '0')}月${date.getDate().toString().padStart(2, '0')}日`;
}

// 响应式数据
const darkMode = ref(true);
const progressDetailChart = ref(null);
const trendChart = ref(null);
const progressChart = ref(null);
const trendChartIcon = ref(null);
const isDark = inject('isDark', ref(true))
const toggleTheme = inject('toggleTheme', (value) => {
  isDark.value = value;
  localStorage.setItem('theme-dark', JSON.stringify(value));
  updateBodyTheme
})

// 使用指南相关数据
const showGuideModal = ref(false);
const currentGuideSection = ref('welcome');
const currentGuidePage = ref(1);
const guideTotalPages = ref(6);

// 指南菜单选项
const guideMenuOptions = [
  {
    label: '欢迎',
    key: 'welcome',
    icon: () => h(AccessibilitySharp)
  },
  {
    label: '创建目标',
    key: 'create',
    icon: () => h(AccessibilitySharp)
  },
  {
    label: '跟踪进度',
    key: 'track',
    icon: () => h(AccessibilitySharp)
  },
  {
    label: '数据分析',
    key: 'analyze',
    icon: () => h(AccessibilitySharp)
  },
  {
    label: '协作功能',
    key: 'collaborate',
    icon: () => h(AccessibilitySharp)
  },
  {
    label: '高级技巧',
    key: 'advanced',
    icon: () => h(AccessibilitySharp)
  }
];

// 目标数据
const goals = ref([
  // { title: '职业发展', progress: 65, status: 'in-progress', deadline: '2023-12-31', owner: '张三' },
  // { title: '技能提升计划', progress: 80, status: 'in-progress', deadline: '2023-11-15', owner: '李四' },
  // { title: '健身目标', progress: 45, status: 'in-progress', deadline: '2024-02-28', owner: '王五' },
  // { title: '阅读计划', progress: 75, status: 'completed', deadline: '2023-12-20', owner: '赵六' },
  // { title: '学习新语言', progress: 30, status: 'in-progress', deadline: '2024-03-15', owner: '钱七' },
  // { title: '项目交付', progress: 90, status: 'in-progress', deadline: '2023-10-30', owner: '孙八' }
]);
const store = useStore();
const { userInfo } = useUser();
const { themeSettings } = useSettings();

// 目标详情相关
const showDetailModal = ref(false);
const selectedGoal = ref({});
const getGoalsMoth = ref([])
const getGoals = async () => {
  console.log("userInfo.value.username", userInfo.value.username);

  const res = await getMPaths("getGoals", userInfo.value.username, "正在获取目标数据...");
  if (isSuccess(res)) {
    goals.value = res.data.data;
    console.log("goals.value", goals.value);
    goals.value?.forEach(goal => {
      goal.deadlineString = formatDate(goal.deadline);
    });
    getGoalsMoth.value = filterMoth()


    // 如果当前有选中的目标，更新选中的目标数据
    if (selectedGoal.value && selectedGoal.value._id) {
      const updatedGoal = goals.value.find(g => g._id === selectedGoal.value._id);
      console.log("updatedGoal", updatedGoal);

      if (updatedGoal) {
        selectedGoal.value = { ...updatedGoal };
      }
    }

    getGoalsMoth.value = filterMoth()
  }
}

// 显示目标详情
const showGoalDetail = (goal) => {
  // console.log("goal", goal);
  // goal.deadline = formatDate(goal.deadline);
  selectedGoal.value = { ...goal };
  // console.log("selectedGoal.value", selectedGoal.value);

  showDetailModal.value = true;
};

// 保存目标
const saveGoal = (updatedGoal) => {
  // 这里可以实现保存目标的逻辑
  console.log('保存目标:', updatedGoal);
  // 更新本地数据
  const index = goals.value.findIndex(g => g.id === updatedGoal.id);
  if (index !== -1) {
    goals.value[index] = { ...updatedGoal };
  }
};

// 开关轨道样式
const railStyle = ({ focused, checked }) => {
  const style = {};
  if (checked) {
    style.background = '#81c683';
    if (focused) style.boxShadow = '0 0 0 2px #d0305040';
  } else {
    style.background = '#2080f0';
    if (focused) style.boxShadow = '0 0 0 2px #2080f040';
  }
  return style;
};

// 添加新目标
const addGoal = () => {
  router.push('/add-goal');
  console.log('添加新目标');
};

// 显示使用指南
const showGuide = () => {
  showGuideModal.value = true;
};

// 重置指南
const resetGuide = () => {
  currentGuideSection.value = 'welcome';
  currentGuidePage.value = 1;
};

// 上一章节
const prevGuideSection = () => {
  const sections = ['welcome', 'create', 'track', 'analyze', 'collaborate', 'advanced'];
  const currentIndex = sections.indexOf(currentGuideSection.value);
  if (currentIndex > 0) {
    currentGuideSection.value = sections[currentIndex - 1];
    currentGuidePage.value = 1;
  }
};

// 下一章节
const nextGuideSection = () => {
  const sections = ['welcome', 'create', 'track', 'analyze', 'collaborate', 'advanced'];
  const currentIndex = sections.indexOf(currentGuideSection.value);
  if (currentIndex < sections.length - 1) {
    currentGuideSection.value = sections[currentIndex + 1];
    currentGuidePage.value = 1;
  }
};

// 处理页面变化
const handlePageChange = (page) => {
  currentGuidePage.value = page;
};

// 处理日历日期选择
const handleCalendarUpdate = (value) => {
  console.log('选择的日期:', value);
  // 可以在这里添加处理日期选择的逻辑
  // 例如：显示该日期的目标或任务
};

const filterMoth = () => {
  const mothList = ref([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0])
  goals.value.forEach(e => {
    const date = new Date(e.deadline)
    date.getMonth()
    mothList.value[date.getMonth()]++

  })
  console.log("mothList", mothList.value);

  return mothList.value
}

// 初始化图表
onMounted(async () => {
  await getGoals()
  // 进度图表 (ECharts)
  const progressChartInstance = echarts.init(progressChart.value);
  progressChartInstance.setOption({
    series: [{
      type: 'pie',
      radius: ['70%', '90%'],
      data: [
        { value: 7, name: '已完成' },
        { value: 12, name: '进行中' },
        { value: 8, name: '未开始' }
      ],
      label: { show: false },
      emphasis: { scale: false },
      animationType: 'scale',
      animationEasing: 'elasticOut'
    }],
    color: ['#00c9a7', '#81c683', '#3a3a4a']
  });

  // 趋势图表图标 (ECharts)
  const trendChartIconInstance = echarts.init(trendChartIcon.value);
  trendChartIconInstance.setOption({
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: { show: false },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false }
    },
    series: [{
      data: [820, 932, 901, 934, 1290, 1330, 1320],
      type: 'line',
      smooth: true,
      lineStyle: {
        color: '#00c9a7',
        width: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
          offset: 0,
          color: 'rgba(0, 201, 167, 0.3)'
        }, {
          offset: 1,
          color: 'rgba(0, 201, 167, 0)'
        }])
      },
      symbol: 'none'
    }],
    grid: {
      left: 0,
      right: 0,
      top: 0,
      bottom: 0
    },
    tooltip: { show: false }
  });

  // 进度详情图表
  new Chart(progressDetailChart.value, {
    type: 'doughnut',
    data: {
      labels: ['已完成', '进行中', '未开始'],
      datasets: [{
        data: [goalFinishCount, goalIngCount, goalExpireCount],
        backgroundColor: ['#00c9a7', '#81c683', '#3a3a4a'],
        borderWidth: 0
      }]
    },
    options: {
      cutout: '70%',
      plugins: {
        legend: {
          display: false
        }
      }
    }
  });

  // 趋势图表
  new Chart(trendChart.value, {
    type: 'line',
    data: {
      labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      datasets: [{
        label: '目标完成率',
        data: getGoalsMoth.value,
        borderColor: '#00c9a7',
        backgroundColor: 'rgba(0, 201, 167, 0.1)',
        tension: 0.4,
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        x: {
          grid: {
            display: false
          },
          ticks: {
            color: 'grey'
          }
        },
        y: {
          min: 0,
          max: 100,
          grid: {
            color: isDark.value ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)'
          },
          ticks: {
            color: 'grey',
            callback: function (value) {
              return value + '%';
            }
          }
        }
      }
    }
  });

});
</script>

<style scoped>
.home-container {
  background-color: #0f0f13;
  color: #ffffff;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.home-container-light {
  background-color: #f5f5f7;
  color: #000000;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.background-elements {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.gradient-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.2;
}

.gradient-circle.blue {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #1e90ff, transparent 70%);
  top: -200px;
  right: -200px;
}

.gradient-circle.green {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #00c9a7, transparent 70%);
  bottom: -200px;
  left: -200px;
}

.gradient-circle.purple {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #81c683, transparent 70%);
  top: 40%;
  left: 40%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #81c683, #4b0082);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  background: linear-gradient(to right, #81c683, #4b0082);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  font-size: 16px;
  font-weight: 500;
  position: relative;
}


.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #81c683, #4b0082);
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.main-content-wrapper {
  height: 100%;
  overflow-y: auto;
}

/* 滚动条样式 - Webkit内核浏览器 */
.main-content-wrapper::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.main-content-wrapper::-webkit-scrollbar-thumb {
  background-color: rgba(129, 198, 131, 0.4);
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background-color: rgba(129, 198, 131, 0.7);
}

.main-content-wrapper::-webkit-scrollbar-track {
  background-color: transparent;
  border-radius: 4px;
}

.main-content-wrapper::-webkit-scrollbar-corner {
  background-color: transparent;
}

/* 亮色模式下的滚动条样式 */
.home-container-light .main-content-wrapper::-webkit-scrollbar-thumb {
  background-color: rgba(129, 198, 131, 0.3);
}

.home-container-light .main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background-color: rgba(129, 198, 131, 0.5);
}

.home-container-light .main-content-wrapper::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.05);
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px;
}

.content-wrapper {
  min-height: 100%;
}

.hero-section {
  text-align: center;
  padding: 30px 0 40px;
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 40px;
  font-weight: 800;
  background: linear-gradient(to right, #fff, #d3c1ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1.2;
  margin-bottom: 20px;
}

.hero-title-light {
  font-size: 40px;
  font-weight: 800;
  background: linear-gradient(to right, #180202, #070116);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1.2;
  margin-bottom: 20px;
}

.hero-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-subtitle-light {
  font-size: 18px;
  color: rgba(27, 2, 2, 0.7);
  line-height: 1.6;
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.features-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 25px;
  margin-bottom: 50px;
}

.feature-card {
  background: rgba(30, 30, 42, 0.7);
  border-radius: 16px;
  padding: 25px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
  border-color: rgba(129, 198, 131, 0.3);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
}

.echart-icon {
  width: 28px;
  height: 28px;
}

.stats-container {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.stat-label-light {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.6);
}

.chart-container {
  position: relative;
  height: 18vh;
  width: 100%;
  margin-top: auto;
  flex-grow: 1;
}

.targets-section {
  margin-top: 40px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 30px;
  position: relative;
  padding-left: 20px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 32px;
  background: linear-gradient(to bottom, #81c683, #4b0082);
  border-radius: 4px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.timeline-container {
  max-height: 250px;
  overflow-y: auto;
  padding-right: 10px;
}

/* 时间线容器滚动条样式 */
.timeline-container::-webkit-scrollbar {
  width: 6px;
}

.timeline-container::-webkit-scrollbar-thumb {
  background-color: rgba(129, 198, 131, 0.3);
  border-radius: 3px;
}

.timeline-container::-webkit-scrollbar-thumb:hover {
  background-color: rgba(129, 198, 131, 0.5);
}

.timeline-container::-webkit-scrollbar-track {
  background-color: transparent;
  border-radius: 3px;
}

.target-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 25px;
}

.target-card {
  background: rgba(30, 30, 42, 0.7);
  border-radius: 16px;
  padding: 25px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.target-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
  border-color: rgba(129, 198, 131, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.goal-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  word-break: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goal-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.detail-item-light {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.7);
}

.footer {
  text-align: center;
  padding: 40px 0;
  margin-top: 60px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* 使用指南样式 */
.guide-content {
  padding: 20px 0;
}

.step-description {
  padding: 10px 0;
}

.step-description p {
  margin-bottom: 10px;
  line-height: 1.6;
}

.step-description ul {
  padding-left: 20px;
  margin: 10px 0;
}

.step-description li {
  margin-bottom: 8px;
  line-height: 1.5;
}

.guide-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 10px 0;
}

.modal-dark {
  background-color: rgba(30, 30, 42, 0.95);
  color: #ffffff;
}

.modal-light {
  background-color: rgba(245, 245, 247, 0.95);
  color: #000000;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    padding: 20px;
  }

  .hero-section {
    padding: 50px 0 60px;
  }

  .hero-title,
  .hero-title-light {
    font-size: 42px;
  }

  .hero-subtitle,
  .hero-subtitle-light {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .features-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .target-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .hero-section {
    padding: 40px 0 50px;
  }

  .hero-title,
  .hero-title-light {
    font-size: 36px;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .hero-actions .n-button {
    width: 100%;
    max-width: 300px;
  }

  .stats-container {
    flex-direction: column;
    gap: 15px;
  }
}
</style>