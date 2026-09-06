<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'home-container' : 'home-container-light'">
    <common>
      <template #content>
        <!-- 主内容区域 -->
        <n-layout-content class="main-content-wrapper">
          <div class="main-content">
            <!-- 页面标题 -->
            <!-- <section class="page-header">
              <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">目标管理</h1>
              <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
                管理您的所有目标，查看进度并进行编辑
              </p>
            </section> -->

            <!-- 控制面板 (方案一：重构指挥部) -->
            <section class="command-deck-section">
              <div class="command-deck" :class="{ 'expanded': showAdvancedFilters }">
                <!-- 第一层：核心操作与快速搜索 -->
                <div class="command-main-row">
                  <div class="action-group">
                    <n-button type="primary" round secondary strong @click="addNewGoal" class="deck-btn main-action">
                      <template #icon>
                        <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg></n-icon>
                      </template>
                      添加新目标
                    </n-button>
                    <div class="divider-vertical"></div>
                    <ExcelImport @import-success="refreshGoals" />
                    <n-button circle quaternary @click="refreshGoals" title="刷新数据">
                      <template #icon>
                        <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M17.65 6.35A7.958 7.958 0 0 0 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0 1 12 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg></n-icon>
                      </template>
                    </n-button>
                  </div>

                  <div class="search-group">
                    <n-input v-model:value="searchQuery" round placeholder="快速搜索目标..." clearable class="deck-search">
                      <template #prefix>
                        <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M15.5 14h-.79l-.71-.71A6.471 6.471 0 0 0 16 9.5A6.5 6.5 0 1 0 9.5 16a6.471 6.471 0 0 0 4.21-1.21l.71.71v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5S14 7.01 14 9.5S11.99 14 9.5 14z"/></svg></n-icon>
                      </template>
                    </n-input>
                    <n-button 
                      round 
                      :secondary="!showAdvancedFilters" 
                      :type="showAdvancedFilters ? 'primary' : 'default'"
                      @click="showAdvancedFilters = !showAdvancedFilters"
                      class="filter-toggle-btn"
                    >
                      <template #icon>
                        <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"/></svg></n-icon>
                      </template>
                      筛选
                    </n-button>
                    <n-dropdown trigger="click" :options="exportOptions" @select="handleExport">
                      <n-button circle quaternary>
                        <template #icon>
                          <n-icon><CloudDownloadOutline /></n-icon>
                        </template>
                      </n-button>
                    </n-dropdown>
                  </div>
                </div>

                <!-- 第二层：高级筛选器 (折叠区域) -->
                <transition name="expand-fade">
                  <div v-if="showAdvancedFilters" class="command-advanced-row">
                    <div class="filter-item">
                      <span class="filter-label">日期范围</span>
                      <n-date-picker v-model:value="dateFilter" type="daterange" clearable placeholder="选择周期" />
                    </div>
                    <div class="filter-item">
                      <span class="filter-label">所有权</span>
                      <n-select v-model:value="ownershipFilter" :options="ownershipOptions" clearable placeholder="全部类型" />
                    </div>
                    <div class="filter-item">
                      <span class="filter-label">状态</span>
                      <n-select v-model:value="statusFilter" :options="statusOptions" clearable placeholder="全部状态" />
                    </div>
                    <div class="filter-item">
                      <span class="filter-label">标签</span>
                      <n-select v-model:value="tagFilter" :options="tagOptions" multiple clearable placeholder="选择标签" />
                    </div>
                  </div>
                </transition>
              </div>
            </section>

            <!-- 目标列表与详情双栏布局 -->
            <section class="goals-section">
              <n-grid x-gap="20" cols="24" item-responsive responsive="screen">
                <!-- 左侧目标列表 -->
                <n-grid-item span="24 m:10 l:9">
                  <n-card :class="[isDark ? 'feature-card' : 'feature-card-light', 'list-card']"
                    content-style="padding: 10px;">
                    <template #header>
                      <div class="card-header-inner">
                        <n-icon size="20" color="#00c9a7">
                          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                            fill="currentColor">
                            <path
                              d="M21,11.5c0,1.7-1.3,3-3,3s-3-1.3-3-3c0-0.5,0.1-0.9,0.3-1.3l-2-1.2C13.1,10.4,13,10.7,13,11c0,1.7,1.3,3,3,3 s3-1.3,3-3c0-0.7-0.3-1.4-0.7-2H21V11.5z" />
                            <path
                              d="M9,14C7.3,14,6,15.3,6,17s1.3,3,3,3s3-1.3,3-3s-1.3-3-3-3z M9,18c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S9.6,18,9,18 z" />
                            <path
                              d="M6,7C4.3,7,3,8.3,3,10s1.3,3,3,3s3-1.3,3-3S7.7,7,6,7z M6,11c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S6.6,11,6,11z" />
                            <path
                              d="M18,4c-1.7,0-3,1.3-3,3s1.3,3,3,3s3-1.3,3-3S19.7,4,18,4z M18,8c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S18.6,8,18,8z" />
                          </svg>
                        </n-icon>
                        <h2 class="card-title">目标列表</h2>
                        <!-- 统计信息：一行展示 -->
                        <span class="header-stat">{{ filteredGoals.length }} 个目标</span>
                        <span v-if="groupCount > 0" class="header-stat-sep">·</span>
                        <span v-if="groupCount > 0" class="header-stat header-stat-highlight">{{ groupCount }} 组</span>
                        <!-- 分组模式切换：按标题 / 按优先级 / 按时间 -->
                        <div class="group-toggle-wrapper">
                          <n-button-group size="tiny">
                            <n-button
                              :type="viewMode === 'title' ? 'primary' : 'default'"
                              size="tiny"
                              @click="viewMode = 'title'"
                            >
                              按标题
                            </n-button>
                            <n-button
                              :type="viewMode === 'priority' ? 'primary' : 'default'"
                              size="tiny"
                              @click="viewMode = 'priority'"
                            >
                              按优先级
                            </n-button>
                            <n-button
                              :type="viewMode === 'time' ? 'primary' : 'default'"
                              size="tiny"
                              @click="viewMode = 'time'"
                            >
                              按时间
                            </n-button>
                          </n-button-group>
                          <!-- 时间排序切换 -->
                          <n-button-group v-if="viewMode === 'time'" size="tiny" style="margin-left:6px;">
                            <n-button
                              :type="sortAsc ? 'primary' : 'default'"
                              size="tiny"
                              @click="sortAsc = true"
                            >
                              ↑ 升序
                            </n-button>
                            <n-button
                              :type="!sortAsc ? 'primary' : 'default'"
                              size="tiny"
                              @click="sortAsc = false"
                            >
                              ↓ 降序
                            </n-button>
                          </n-button-group>
                        </div>
                      </div>
                    </template>

                    <div class="stagger-list-container">
                      <el-table :data="groupedDisplayList" :class="isDark ? 'el-table-dark' : 'el-table-light'"
                        style="width: 100%; cursor: pointer;height: 100%;" @row-click="handleRowClick"
                        :row-class-name="tableRowClassName" highlight-current-row >
                        <!-- ... 列表内容保持不变 ... -->
                         <el-table-column label="操作" width="110" align="center">
                          <template #default="scope">
                            <template v-if="!scope.row._isGroupHeader">
                              <div class="table-actions">
                                <!-- 已完成 → 鼓励提示 -->
                                <n-tooltip v-if="scope.row.status === 'completed'" placement="bottom">
                                  <template #trigger>
                                    <span class="status-hint status-hint-done">
                                      <n-icon size="14"><CheckmarkCircle /></n-icon>
                                      很棒哦
                                    </span>
                                  </template>
                                  已完成
                                </n-tooltip>
                                <!-- 已过期 → 遗憾提示 -->
                                <n-tooltip v-else-if="scope.row.status === 'expired'" placement="bottom">
                                  <template #trigger>
                                    <span class="status-hint status-hint-expired">
                                      <n-icon size="14"><SadOutline /></n-icon>
                                      很可惜
                                    </span>
                                  </template>
                                  已过期
                                </n-tooltip>
                                <!-- 进行中 → 一键完成按钮 + 续签按钮 -->
                                <n-popconfirm
                                  v-if="scope.row.status === 'in-progress'"
                                  @positive-click="completeAllGoal(scope.row)"
                                  positive-text="确认完成"
                                  negative-text="取消"
                                  placement="left"
                                >
                                  <template #trigger>
                                    <n-button text class="complete-all-btn" title="一键完成所有任务">
                                      <n-icon size="16"><CheckmarkDoneOutline /></n-icon>
                                    </n-button>
                                  </template>
                                  确认将 "{{ scope.row.title }}" 所有子任务标记为完成？
                                </n-popconfirm>
                                <!-- 续签按钮：deadline +30天，最多续签3次 -->
                                <n-popconfirm
                                  v-if="scope.row.status === 'in-progress' && (scope.row.renewCount || 0) < 3"
                                  @positive-click="renewGoalDeadline(scope.row)"
                                  positive-text="确认续签"
                                  negative-text="取消"
                                  placement="right"
                                  
                                >
                                  <template #trigger>
                                    <n-button text class="renew-btn" :title="'续签截止日期（剩余' + (3 - (scope.row.renewCount || 0)) + '次）'">
                                      <!-- 自定义 SVG：时钟 + 加号，表示延长/续签时间 -->
                                      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="12" cy="12" r="9"/>
                                        <polyline points="12,7 12,12 15,12"/>
                                        <line x1="16" y1="3" x2="16" y2="8"/>
                                        <line x1="13.5" y1="5.5" x2="18.5" y2="5.5"/>
                                      </svg>
                                    </n-button>
                                  </template>
                                  确认将 "{{ scope.row.title }}" 的截止日期延长30天？<br/>（剩余{{ 3 - (scope.row.renewCount || 0) }}次）
                                </n-popconfirm>
                              </div>
                            </template>
                          </template>
                        </el-table-column>
                        <el-table-column label="目标名称" prop="title"  show-overflow-tooltip min-width="150">
                          <template #default="scope">
                            <!-- 时间组头（按截止月份分组） -->
                            <div v-if="scope.row._isTimeGroup"
                              style="display: flex; align-items: center; gap: 10px; font-weight: 600;"
                              @click.stop="toggleGroup(scope.row.title)">
                              <!-- 折叠箭头 -->
                              <span style="display:inline-flex;transition:transform .2s;font-size:10px;color:#999;width:10px;"
                                :style="{ transform: isGroupExpanded(scope.row.title) ? 'rotate(90deg)' : 'rotate(0deg)' }">
                                ▶
                              </span>
                              <!-- 月份文字 -->
                              <span style="font-size:13px;">{{ scope.row.title }}</span>
                              <!-- 计数 -->
                              <span style="font-size:12px;color:#999;font-weight:400;">·  {{ scope.row.count }} 个</span>
                            </div>
                            <!-- 优先级组头 -->
                            <div v-else-if="scope.row._isPriorityGroup"
                              style="display: flex; align-items: center; gap: 10px; font-weight: 600;"
                              @click.stop="toggleGroup(scope.row.title)">
                              <!-- 折叠箭头 -->
                              <span style="display:inline-flex;transition:transform .2s;font-size:10px;color:#999;width:10px;"
                                :style="{ transform: isGroupExpanded(scope.row.title) ? 'rotate(90deg)' : 'rotate(0deg)' }">
                                ▶
                              </span>
                              <!-- 优先级色点 -->
                              <span :style="{ display:'inline-block', width:'8px', height:'8px', borderRadius:'50%', background: getPriorityColor(scope.row.priority) }"></span>
                              <!-- 优先级名称 -->
                              <span>{{ scope.row.title }}</span>
                              <!-- 计数 -->
                              <span style="font-size:12px;color:#999;font-weight:400;">{{ scope.row.count }} 个</span>
                            </div>
                            <!-- 组头行（根分组「目标分组」或子分组） -->
                            <div v-else-if="scope.row._isGroupHeader" 
                              style="display: flex; align-items: center; gap: 8px; font-weight: 600;"
                              @click.stop="toggleGroup(scope.row.title)">
                              <span style="display:inline-flex;transition:transform .2s;font-size:12px;color:#00c9a7;"
                                :style="{ transform: isGroupExpanded(scope.row.title) ? 'rotate(90deg)' : 'rotate(0deg)' }">
                                ▶
                              </span>
                              <!-- 根分组：目标分组 -->
                              <n-tag v-if="scope.row._isRootGroup" size="small" type="success" :bordered="false" round
                                style="font-size:11px;height:22px;padding:0 8px;font-weight:600;">
                                {{ scope.row.count }} 个任务组
                              </n-tag>
                              <!-- 子分组：具体标题 -->
                              <template v-else>
                                <n-tag size="small" :bordered="false" round style="font-size:10px;height:20px;padding:0 6px;background:rgba(0,201,167,0.15);color:#00c9a7;">
                                  任务 ×{{ scope.row.count }}
                                </n-tag>
                              </template>
                              <n-tooltip v-if="!scope.row._isRootGroup && (scope.row.children || []).length > 0" trigger="hover">
                              <!-- <n-tooltip  trigger="hover"> -->
                                <template #trigger>
                                  <span class="series-chart-trigger" @click.stop="openSeriesChart(scope.row)">
                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round">
                                      <path d="M5 20V10" />
                                      <path d="M12 20V4" />
                                      <path d="M19 20v-7" />
                                    </svg>
                                  </span>
                                </template>
                                查看该任务从建立到现在整个周期的完成情况
                              </n-tooltip>
                              <span>{{ scope.row.title }}</span>
                              <!-- 周期可视化入口：子分组（定时任务系列）可一键查看整个周期的完成情况 -->
                              
                            </div>
                            <!-- 普通目标行 -->
                            <div v-else style="display: flex; align-items: center; gap: 6px;" class="stagger-item">
                              <n-tag v-if="isOwner(scope.row)" size="small" type="primary" :bordered="false" round
                                style="font-size: 10px; height: 20px; padding: 0 6px;">
                                我的
                              </n-tag>
                              <n-tag v-else size="small" type="warning" :bordered="false" round
                                style="font-size: 10px; height: 20px; padding: 0 6px;">
                                协作
                              </n-tag>
                              <span style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{
                                scope.row.title }}</span>
                            </div>
                          </template>
                        </el-table-column>
                        <!-- 进度 + 截止时间（列合并）：进度条下方显示截止日期 -->
                        <el-table-column label="进度｜截止时间" width="150" align="center">
                          <template #default="scope">
                            <div v-if="scope.row._isGroupHeader" style="color:#888;font-size:12px;text-align:center;">
                              {{ scope.row._isRootGroup ? '共' + scope.row.count + '组' : scope.row.count + '个目标' }}
                            </div>
                            <div v-else>
                              <el-progress :percentage="scope.row.progress"
                                :stroke-width="20" text-inside="true"  :color="getStatusColor(scope.row.status)"
                                :status="scope.row.status === 'completed' ? 'success' : scope.row.status === 'expired' ? 'exception' : ''" />
                              <!-- 截止日期小字 -->
                              <div style="font-size:11px;color:#888;text-align:center;margin-top:3px;white-space:nowrap;">{{ scope.row.deadlineString }}</div>
                            </div>
                          </template>
                        </el-table-column>
                        <!-- 状态 + 优先级（列合并）：紧凑色标标签，颜色 + 文字一目了然 -->
                        <el-table-column label="状态｜优先级" width="120"  align="center">
                          <template #default="scope">
                            <div v-if="scope.row._isGroupHeader" style="color:#888;font-size:12px;text-align:center;">
                              批量
                            </div>
                            <div v-else style="display:flex;align-items:center;gap:5px;">
                              <!-- 优先级色标：红底=高/紧, 橙底=中, 绿底=低，内置文字无需图例 -->
                              
                              <n-tag :type="getStatusTagType(scope.row.status)" size="small">
                                {{ getStatusLabel(scope.row.status) }}
                              </n-tag>｜
                              <span class="pri-badge" :class="'pri-badge-' + (scope.row.level || 'medium')">
                                {{ getPriorityText(scope.row.level) }}
                              </span>
                            </div>
                          </template>
                        </el-table-column>
                        <!-- 一键完成列 -->
                        
                      </el-table>
                    </div>
                  </n-card>
                </n-grid-item>

                <!-- 右侧详情面板 -->
                <n-grid-item span="24 m:14 l:15">
                  <n-card :class="isDark ? 'feature-card' : 'feature-card-light'" v-if="currentSelectedGoal"
                    class="detail-card" content-style="padding: 24px;">
                    <template #header>
                      <div class="detail-header-wrapper">
                        <div class="detail-meta-top">
                          <n-tag :type="isOwner(currentSelectedGoal) ? 'primary' : 'warning'" size="small" round
                            :bordered="false" style="margin-right: 8px;">
                            {{ isOwner(currentSelectedGoal) ? '我主导的' : '我协作的' }}
                          </n-tag>
                          <n-tag :type="getStatusTagType(currentSelectedGoal.status)" size="small" round
                            :bordered="false">
                            {{ getStatusLabel(currentSelectedGoal.status) }}
                          </n-tag>
                          <span class="goal-id-text" v-if="currentSelectedGoal._id">#{{
                            currentSelectedGoal._id.slice(-6).toUpperCase() }}</span>
                        </div>
                        <div class="detail-main-row">
                          <h2 class="detail-hero-title">{{ currentSelectedGoal.title }}</h2>
                          <div class="detail-actions">
                            <n-button type="primary" circle secondary strong @click="editGoal(currentSelectedGoal)"
                              class="action-btn">
                              <n-icon size="18">
                                <PencilOutline />
                              </n-icon>
                            </n-button>
                            <n-button type="error" circle secondary strong @click="deleteGoal(currentSelectedGoal)"
                              v-if="isGoalOwner(currentSelectedGoal)" class="action-btn">
                              <n-icon size="18">
                                <ArchiveOutline />
                              </n-icon>
                            </n-button>
                          </div>
                        </div>
                      </div>
                    </template>

                    <div class="detail-content">
                      <!-- 基本信息 -->
                      <div class="info-grid-modern">
                        <div class="info-item-modern">
                          <div class="info-label">负责人</div>
                          <div class="info-value">{{ currentSelectedGoal.owner }}</div>
                        </div>
                        <div class="info-item-modern">
                          <div class="info-label">截止日期</div>
                          <div class="info-value">{{ currentSelectedGoal.deadlineString }}</div>
                        </div>
                        <div class="info-item-modern">
                          <div class="info-label">优先级</div>
                          <div class="info-value">
                            <n-tag :type="getPriorityType(currentSelectedGoal.level)" :bordered="false" round size="small">
                              {{ getPriorityText(currentSelectedGoal.level) }}
                            </n-tag>
                          </div>
                        </div>
                        <div class="info-item-modern full-width">
                          <div class="info-label">总体进度</div>
                          <div class="progress-wrapper">
                            <n-progress type="line"
                              :percentage="currentSelectedGoal.status == 'expired' ? 100 : currentSelectedGoal.progress"
                              :color="getStatusColor(currentSelectedGoal.status)"
                              :processing="currentSelectedGoal.status === 'in-progress'" :height="8"
                              border-radius="4px" />
                          </div>
                        </div>
                        <div class="info-item-modern full-width" v-if="currentSelectedGoal.description">
                          <div class="info-label">描述</div>
                          <div class="info-value description-text">{{ currentSelectedGoal.description }}</div>
                        </div>


                      </div>

                      <div class="divider-line"></div>

                      <!-- 子目标列表 -->
                      <div class="sub-goals-list">
                        <div class="section-title-row">
                          <h3>子目标</h3>
                          <n-tag size="tiny" round type="primary">{{ currentSelectedGoal.childGoals ?
                            currentSelectedGoal.childGoals.length : 0 }}</n-tag>
                        </div>
 <div class="child-add-bar" v-if="currentSelectedGoal">
                        <input
                          v-model="newChildMessage"
                          class="child-add-input"
                          placeholder="输入新的子目标，回车添加..."
                          @keyup.enter="addChildGoal"
                        />
                        <n-button
                          size="tiny"
                          circle
                          type="primary"
                          :disabled="!newChildMessage.trim()"
                          @click="addChildGoal"
                          class="add-bar-btn"
                        >
                          <template #icon>
                            <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg></n-icon>
                          </template>
                        </n-button>
                      </div>
                        <div v-if="currentSelectedGoal.childGoals && currentSelectedGoal.childGoals.length > 0"
                          class="sub-goals-container">
                          <div v-for="(childGoal, index) in currentSelectedGoal.childGoals" :key="index"
                            class="child-goal-capsule" :class="{ 'completed': childGoal.finish }">

                            <div class="capsule-left">
                              <div class="checkbox-circle" :class="{ 'checked': childGoal.finish }"
                                @click="childGoal.finish ? unfinishChildGoal(currentSelectedGoal, index) : finishChildGoal(currentSelectedGoal, index)">
                                <n-icon size="14" v-if="childGoal.finish">
                                  <CheckmarkSharp />
                                </n-icon>
                              </div>
                              <div class="capsule-content">
                                <!-- 行内编辑模式：输入框 -->
                                <template v-if="editingChildIndex === index">
                                  <input
                                    v-model="editingChildText"
                                    class="child-edit-input"
                                    @keyup.enter="saveEditChild(currentSelectedGoal, index)"
                                    @keyup.esc="cancelEditChild"
                                    @blur="saveEditChild(currentSelectedGoal, index)"
                                  />
                                </template>
                                <!-- 展示模式：点击文本进入编辑 -->
                                <template v-else>
                                  <span v-if="currentSelectedGoal.status=='in-progress'" class="capsule-title" :class="{ 'strikethrough': childGoal.finish }"
                                    @click.stop="startEditChild(index, childGoal)">{{ childGoal.message }}</span>
                                  <span v-else class="capsule-title" :class="{ 'strikethrough': childGoal.finish }"
                                    >{{ childGoal.message }}</span>
                                </template>
                                <span class="finish-time" v-if="childGoal.finishDate">完成于 {{
                                  formatDate(childGoal.finishDate)
                                }}</span>
                              </div>
                            </div>

                            <div class="capsule-actions">
                              <n-dropdown trigger="hover"
                                :options="getDropdownOptions(childGoal, currentSelectedGoal, index)"
                                @select="(key) => handleDropdownSelect(key, currentSelectedGoal, index, childGoal)">
                                <n-button text class="more-btn">
                                  <n-icon size="20">
                                    <EllipsisHorizontal />
                                  </n-icon>
                                </n-button>
                              </n-dropdown>
                            </div>
                          </div>
                        </div>
                        <n-empty v-else description="暂无子目标" class="empty-sub-goals" />
                      </div>

                      <!-- 子目标添加栏（详情面板底部浮动） -->
                     

                      <!-- 目标笔记区域 -->

                      <div class="info-item-modern full-width rich-text-section">
                        <div class="info-label"
                          style="display: flex; align-items: center; justify-content: space-between;">
                          <span>
                            <n-icon :component="DocumentTextOutline"
                              style="margin-right: 4px; vertical-align: text-bottom;" />
                            目标笔记
                          </span>
                        </div>
                        <div class="rich-text-wrapper" style="margin-top: 8px;">
                          <RichTextEditor :model-value="richTextContent" :status="richTextStatus"
                            @update:model-value="handleRichTextChange" />
                        </div>
                      </div>
                      <!-- 汇总统计 -->
                      <div class="summary-modern"
                        v-if="currentSelectedGoal.childGoals && currentSelectedGoal.childGoals.length > 0">
                        <div class="stat-block">
                          <div class="stat-num">{{ currentSelectedGoal.childGoals.length }}</div>
                          <div class="stat-desc">总计任务</div>
                        </div>
                        <div class="stat-divider"></div>
                        <div class="stat-block">
                          <div class="stat-num success-text">{{currentSelectedGoal.childGoals.filter((c: any) =>
                            c.finish).length
                          }}</div>
                          <div class="stat-desc">已完成</div>
                        </div>
                        <div class="stat-divider"></div>
                        <div class="stat-block">
                          <div class="stat-num warning-text">{{currentSelectedGoal.childGoals.filter((c: any) =>
                            !c.finish).length
                          }}</div>
                          <div class="stat-desc">待处理</div>
                        </div>
                      </div>
                    </div>
                  </n-card>

                  <!-- 空状态 -->
                  <n-card :class="isDark ? 'feature-card' : 'feature-card-light'" v-else class="empty-selection">
                    <n-empty description="请点击左侧目标查看详情">
                      <template #icon>
                        <n-icon size="40">
                          <DocumentTextOutline />
                        </n-icon>
                      </template>
                    </n-empty>
                  </n-card>
                </n-grid-item>
              </n-grid>
            </section>
          </div>
        </n-layout-content>
      </template>
    </common>



    <!-- 目标详情模态框 -->
    <GoalDetail v-model:show="showDetailModal" :goal="selectedGoal" @save="saveGoal" @updateGoal="refreshGoals" />

    <!-- 庆祝动画全屏覆盖 -->
    <CelebrationOverlay :show="showCelebration" :title="celebrationGoalTitle" @close="showCelebration = false" />

    <!-- 定时任务周期完成情况可视化弹窗 -->
    <GoalSeriesChart
      v-model:show="showSeriesModal"
      :title="(activeSeries && activeSeries.title) || ''"
      :instances="(activeSeries && activeSeries.children) || []"
      :rule="(activeSeries && activeSeries.rule) || null"
    />

    <!-- 子目标文件上传模态框 -->
    <n-modal v-model:show="showChildGoalUploadModal" preset="card" style="max-width: 600px" title="上传文件"
      :mask-closable="false">
      <GeneralUpload ref="childGoalUploadRef" :file-list="currentChildGoalFiles"
        @uploadSuccess="handleChildGoalFileUploadSuccess" @fileRemove="handleChildGoalFileRemove" />
      <template #footer>
        <n-space justify="end">
          <n-button @click="closeChildGoalUploadModal">取消</n-button>
          <n-button type="primary" @click="saveChildGoalFiles">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 子目标文件查看模态框 -->
    <n-modal v-model:show="showChildGoalFilesModal" preset="card" style="max-width: 600px" title="查看文件"
      :mask-closable="true">
      <div v-if="viewChildGoalFilesList.length > 0">
        <n-list>
          <n-list-item v-for="(file, index) in viewChildGoalFilesList" :key="index">
            <n-thing>
              <template #description>
                <div style="display: flex; justify-content: space-between;padding: 5px;">
                  <div>
                    <n-text>{{ file.originalFilename || file.name || '未命名文件' }}</n-text>
                  </div>
                  <div>
                    <n-space>
                      <n-button size="tiny" type="primary" @click="downloadFile(file)">
                        <template #icon>
                          <n-icon>
                            <CloudDownloadOutline />
                          </n-icon>
                        </template>
                        下载
                      </n-button>
                      <n-button size="tiny" type="info" @click="watchfile(file)">
                        <template #icon>
                          <n-icon>
                            <EyeSharp />
                          </n-icon>
                        </template>
                        查看
                      </n-button>
                      <n-button size="tiny" type="error" @click="removeChildGoalFile(file)">
                        <template #icon>
                          <n-icon>
                            <TrashOutline />
                          </n-icon>
                        </template>
                        删除
                      </n-button>
                    </n-space>
                  </div>
                </div>
              </template>
              <!-- <template #description>
               
              </template> -->
            </n-thing>
          </n-list-item>
        </n-list>
      </div>
      <div v-else>
        <n-empty description="暂无文件" />
      </div>
    </n-modal>

    <!-- 完成确认弹窗（自动定位 + 偏好设置） -->
    <n-modal v-model:show="showFinishConfirm" preset="card" title="完成子目标" style="max-width: 460px" :mask-closable="false">
      <div class="finish-confirm-body">
        <!-- 自动定位区域 -->
        <div class="location-section">
          <div class="location-header">
            <n-icon size="18" color="#00c9a7"><MapOutline /></n-icon>
            <span>是否记录完成地点？</span>
          </div>
          <div class="location-status">
            <template v-if="locatingStatus === 'locating'">
              <n-spin size="small" />
              <span class="locating-text">正在定位...</span>
            </template>
            <template v-else-if="locatingStatus === 'success'">
              <div class="located-result">
                <svg class="located-dot" viewBox="0 0 16 16" width="14" height="14">
                  <circle cx="8" cy="6" r="2.5" fill="none" stroke="#00c9a7" stroke-width="1.5"/>
                  <path d="M8 15C8 15 13 10 13 6A5 5 0 1 0 3 6C3 10 8 15 8 15Z" fill="none" stroke="#00c9a7" stroke-width="1.2"/>
                </svg>
                <span class="located-addr">{{ autoLocationAddr }}</span>
              </div>
            </template>
            <template v-else>
              <span class="locating-fail">定位失败或拒绝访问</span>
            </template>
          </div>
        </div>

        <!-- 偏好设置 -->
        <div class="pref-section">
          <n-checkbox v-model:checked="prefSubGoals">
            记录此目标的所有子目标完成位置
          </n-checkbox>
          <n-checkbox v-model:checked="prefAllGoals">
            记录此用户的所有目标完成位置
          </n-checkbox>
        </div>

        <!-- 提示 -->
        <div class="finish-hint">
          勾选后后续完成子目标将自动记录位置，不再弹窗确认
        </div>
      </div>

      <template #footer>
        <div class="finish-footer">
          <n-button @click="cancelFinishConfirm">取消</n-button>
          <n-button type="primary" @click="confirmFinishWithLocation">确认完成</n-button>
        </div>
      </template>
    </n-modal>

  </n-layout>
</template>

<script setup lang="ts">
import common from '@/views/common.vue';
import { ref, computed, onMounted, inject, watch, nextTick } from 'vue';
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NCard,
  NButton,
  NIcon,
  NSpace,
  NInput,
  NSelect,
  NTag,
  NProgress,
  NCollapse,
  NCollapseItem,
  useMessage,
  NEllipsis,
  NModal,
  NList,
  NListItem,
  NThing,
  NText,
  NEmpty,
  NDropdown,
  NGrid,
  NGridItem,
  NDivider,
  NDatePicker,
  NCheckbox,
  NSpin
} from 'naive-ui';
import { ElTable, ElTableColumn, ElButton, ElTag, ElProgress } from 'element-plus';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue';
import GoalDetail from '@/components/GoalDetail.vue';
import RichTextEditor from '@/components/RichTextEditor.vue';
import GeneralUpload from '@/components/GeneralUpload.vue';
import ExcelImport from '@/components/ExcelImport.vue';
import CelebrationOverlay from '@/components/CelebrationOverlay.vue';
import GoalSeriesChart from '@/components/GoalSeriesChart.vue';
import request, { postM, getMPaths, getM, isSuccess, baseURL, isGoalOwner } from '@/utils/request';
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import {
  EyeSharp,
  PencilOutline,
  CheckmarkOutline,
  ArchiveOutline,
  CloudUploadOutline,
  DocumentTextOutline,
  CloudDownloadOutline,
  TrashOutline,
  ChevronDownOutline,
  AccessibilitySharp,
  CalendarSharp,
  CheckmarkSharp,
  EllipsisHorizontal,
  CheckmarkCircle,
  CheckmarkCircleOutline,
  CheckmarkDoneOutline,
  SadOutline,
  MapOutline
} from '@vicons/ionicons5';
import type { DataTableColumns } from 'naive-ui';
import { useStore } from 'vuex';

// 注入主题变量
const isDark = inject('isDark', ref(false));

// 路由和状态管理
const router = useRouter();
const store = useStore();
const message = useMessage();

// 用户信息
const currentUser = computed(() => store.state.user?.username || '');

// 判断是否为目标所有者
const isOwner = (goal: any) => {
  return goal && goal.owner === currentUser.value;
};

// 数据状态
const goals = ref<any[]>([]);
const loading = ref(false);
const showDetailModal = ref(false);
// const searchQuery = ref('');
// const statusFilter = ref(null);
const tagFilter = ref([]);
// const ownershipFilter = ref(null);
// const dateFilter = ref(null);
const selectedGoal = ref<any>({});
const currentSelectedGoal = ref<any>(null);

// 高级筛选折叠状态
const showAdvancedFilters = ref(false);

// 分组模式：'title' = 按标题分组（默认），'priority' = 按优先级分组，'time' = 按截止月份分组
const viewMode = ref<'title' | 'priority' | 'time'>('title');

// 时间分组排序方向：true = 升序（从远到近），false = 降序（从近到远）
const sortAsc = ref(false);

// 切换到优先级/时间模式时自动展开所有组
watch(viewMode, (mode) => {
  // if (mode === 'priority') {
  //   const set = new Set(expandedGroups.value)
  //   for (const opt of priorityOptions) {
  //     set.add(opt.label)
  //   }
  //   expandedGroups.value = set
  // }
})

// 庆祝动画状态
const showCelebration = ref(false);
const celebrationGoalTitle = ref('');

// 处理行点击
const handleRowClick = (row: any) => {
  // 组头行：切换展开/折叠
  if (row._isGroupHeader) {
    toggleGroup(row.title)
    return
  }
  currentSelectedGoal.value = row;
};

// const expiredGoalToast = (res: any) => {
//   message.error(res.data.message || '操作失败');
// };

// const finishChildGoal = async (row: any, index: number) => {
//   try {
//     const updatedGoal = JSON.parse(JSON.stringify(row));
//     updatedGoal.childGoals[index].finish = true;
//     updatedGoal.childGoals[index].finishDate = new Date();
//     const res = await postM('editGoal', updatedGoal);
//     if (isSuccess(res)) {
//       message.success('已完成子目标');
//       getGoals();
//     } else {
//       expiredGoalToast(res);
//     }
//   } catch (error) {
//     message.error('操作失败');
//   }
// };

// const unfinishChildGoal = async (row: any, index: number) => {
//   try {
//     const updatedGoal = JSON.parse(JSON.stringify(row));
//     updatedGoal.childGoals[index].finish = false;
//     updatedGoal.childGoals[index].finishDate = null;
//     const res = await postM('editGoal', updatedGoal);
//     if (isSuccess(res)) {
//       message.success('已取消完成子目标');
//       getGoals();
//     } else {
//       expiredGoalToast(res);
//     }
//   } catch (error) {
//     message.error('操作失败');
//   }
// };

// 表格行样式
const tableRowClassName = ({ row }: { row: any }) => {
  if (row._isGroupHeader) {
    // 时间组头
    if (row._isTimeGroup) {
      return 'time-group-header-row';
    }
    // 优先级组头：按优先级返回不同的行样式
    if (row.priority === 'urgent') {
      return 'urgent-group-header-row';
    } else if (row.priority === 'high') {
      return 'high-group-header-row';
    } else if (row.priority === 'medium') {
      return 'medium-group-header-row';
    } else if (row.priority === 'low') {
      return 'low-group-header-row';
    }
    // 默认的标题分组组头
    return 'group-header-row';
  }
  if (currentSelectedGoal.value && row._id === currentSelectedGoal.value._id) {
    return 'selected-row';
  }
  return '';
};

// 子目标文件上传相关状态
const showChildGoalUploadModal = ref(false);
const showChildGoalFilesModal = ref(false);
const currentChildGoal = ref<any>(null);
const currentChildGoalIndex = ref(-1);
const currentChildGoalFiles = ref<any[]>([]);

// 子目标行内编辑状态
const editingChildIndex = ref(-1)
const editingChildText = ref('')

// 子目标添加栏状态
const newChildMessage = ref('')
const viewChildGoalFilesList = ref<any[]>([]);
const childGoalUploadRef = ref<any>(null);
const watchfile = (file: any) => {
  const a = document.createElement('a')
  a.href = file.url || file.fileUrl || `${baseURL()}/file/${file.name}`;
  a.target = '_blank';
  a.click();
}

// 下拉菜单相关状态
const showDropdown = ref(false);

// 获取下拉菜单选项
const getDropdownOptions = (childGoal, row, index) => {
  const options = [
    {
      label: childGoal.finish ? '取消完成' : '完成',
      key: 'finish'
    }
  ];

  // 只有未完成的子目标才显示上传文件选项
  if (!childGoal.finish) {
    options.push({
      label: '上传文件',
      key: 'upload'
    });
  }

  // 如果有文件则显示查看文件选项
  if (childGoal.fileList && childGoal.fileList.length > 0) {
    options.push({
      label: '查看文件',
      key: 'view'
    });
  }

  return options;
};

// 处理下拉菜单选择
const handleDropdownSelect = (key, row, index, childGoal) => {
  switch (key) {
    case 'finish':
      if (childGoal.finish) {
        unfinishChildGoal(row, index);
      } else {
        finishChildGoal(row, index);
      }
      break;
    case 'upload':
      showChildGoalUpload(row, index);
      break;
    case 'view':
      viewChildGoalFiles(row, index);
      break;
  }
};

// 筛选和搜索
const searchQuery = ref('');
const statusFilter = ref<string | null>(null);
const dateFilter = ref<[number, number] | null>(null);
const ownershipFilter = ref<string | null>(null);

const deleteGoal = (row) => {

  postM('deleteGoal', { row }).then((res) => {
    if (isSuccess(res)) {
      message.success('删除成功');
      refreshGoals();
    }
  })
}
// 状态筛选选项
const statusOptions = [
  { label: '进行中', value: 'in-progress' },
  { label: '已完成', value: 'completed' },
  { label: '已过期', value: 'expired' }
];

// 导出选项
const exportOptions = [
  { label: '导出 Excel', key: 'excel' },
  { label: '导出 PDF', key: 'pdf' }
];

// 标签筛选选项
const tagOptions = computed(() => {
  const tags = new Set<string>();
  goals.value.forEach(goal => {
    if (goal.tags && Array.isArray(goal.tags)) {
      goal.tags.forEach(tag => tags.add(tag));
    }
  });
  return Array.from(tags).map(tag => ({ label: tag, value: tag }));
});

// 所有权筛选选项
const ownershipOptions = [
  { label: '我的目标', value: 'mine' },
  { label: '协作目标', value: 'collab' }
];

// 导出处理
const handleExport = (key: string) => {
  if (key === 'excel') {
    exportToExcel();
  } else if (key === 'pdf') {
    exportToPDF();
  }
};

// 富文本笔记相关
const richTextContent = ref('');
const richTextStatus = ref('saved');
let saveTimeout: any = null;

// 监听选中的目标变化，更新笔记内容
watch(() => currentSelectedGoal.value, (newVal) => {
  if (newVal) {
    richTextContent.value = newVal.richText || '';
    richTextStatus.value = 'saved';
  } else {
    richTextContent.value = '';
    richTextStatus.value = 'saved';
  }
}, { immediate: true });

const handleRichTextChange = (val: string) => {
  richTextContent.value = val;
  richTextStatus.value = 'unsaved';

  if (saveTimeout) clearTimeout(saveTimeout);
  saveTimeout = setTimeout(saveRichText, 2000);
};

const saveRichText = async () => {
  if (!currentSelectedGoal.value || (!currentSelectedGoal.value.id && !currentSelectedGoal.value._id)) return;

  richTextStatus.value = 'saving';
  try {
    // 构造更新数据
    const data = JSON.parse(JSON.stringify(currentSelectedGoal.value));
    data.richText = richTextContent.value;

    // 确保必要字段存在
    if (!data.fileList) data.fileList = [];
    if (!data.childGoals) data.childGoals = [];
    if (!data.collaborators) data.collaborators = [];

    const res = await postM('editGoal', data);
    if (isSuccess(res)) {
      richTextStatus.value = 'saved';
      // 更新本地数据，避免切换回来时内容丢失
      currentSelectedGoal.value.richText = richTextContent.value;

      // 同时更新列表中的数据
      const index = goals.value.findIndex(g => g._id === currentSelectedGoal.value._id || g.id === currentSelectedGoal.value.id);
      if (index !== -1) {
        goals.value[index].richText = richTextContent.value;
      }
    } else {
      richTextStatus.value = 'unsaved';
      message.error('保存笔记失败');
    }
  } catch (e) {
    console.error('保存笔记出错:', e);
    richTextStatus.value = 'unsaved';
  }
};

// 导出 Excel
const exportToExcel = () => {
  try {
    const dataToExport = filteredGoals.value.map(goal => ({
      '目标名称': goal.title,
      '描述': goal.description || '',
      '状态': getStatusLabel(goal.status),
      '进度': goal.progress + '%',
      '负责人': goal.owner,
      '截止日期': goal.deadlineString,
      '所有权': isOwner(goal) ? '我的目标' : '协作目标'
    }));

    const worksheet = XLSX.utils.json_to_sheet(dataToExport);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, '目标列表');

    // 设置列宽
    const wscols = [
      { wch: 20 }, // 目标名称
      { wch: 30 }, // 描述
      { wch: 10 }, // 状态
      { wch: 10 }, // 进度
      { wch: 15 }, // 负责人
      { wch: 15 }, // 截止日期
      { wch: 10 }  // 所有权
    ];
    worksheet['!cols'] = wscols;

    XLSX.writeFile(workbook, `目标列表_${new Date().toISOString().split('T')[0]}.xlsx`);
    message.success('Excel 导出成功');
  } catch (error) {
    console.error('导出 Excel 失败:', error);
    message.error('导出 Excel 失败');
  }
};

// 导出 PDF
const exportToPDF = async () => {
  try {
    message.loading('正在生成 PDF...');

    // 创建一个临时的 DOM 元素用于渲染表格
    const tempDiv = document.createElement('div');
    tempDiv.style.position = 'absolute';
    tempDiv.style.left = '-9999px';
    tempDiv.style.top = '0';
    tempDiv.style.width = '800px';
    tempDiv.style.padding = '20px';
    tempDiv.style.backgroundColor = '#ffffff';
    tempDiv.style.color = '#000000';

    let tableHtml = `
      <h2 style="text-align: center; margin-bottom: 20px;">目标列表</h2>
      <table style="width: 100%; border-collapse: collapse; font-family: Arial, sans-serif;">
        <thead>
          <tr style="background-color: #f2f2f2;">
            <th style="border: 1px solid #ddd; padding: 8px;">目标名称</th>
            <th style="border: 1px solid #ddd; padding: 8px;">状态</th>
            <th style="border: 1px solid #ddd; padding: 8px;">进度</th>
            <th style="border: 1px solid #ddd; padding: 8px;">负责人</th>
            <th style="border: 1px solid #ddd; padding: 8px;">截止日期</th>
          </tr>
        </thead>
        <tbody>
    `;

    filteredGoals.value.forEach(goal => {
      tableHtml += `
        <tr>
          <td style="border: 1px solid #ddd; padding: 8px;">${goal.title}</td>
          <td style="border: 1px solid #ddd; padding: 8px;">${getStatusLabel(goal.status)}</td>
          <td style="border: 1px solid #ddd; padding: 8px;">${goal.progress}%</td>
          <td style="border: 1px solid #ddd; padding: 8px;">${goal.owner}</td>
          <td style="border: 1px solid #ddd; padding: 8px;">${goal.deadlineString}</td>
        </tr>
      `;
    });

    tableHtml += `
        </tbody>
      </table>
      <div style="margin-top: 10px; text-align: right; font-size: 12px;">导出日期: ${new Date().toLocaleDateString()}</div>
    `;

    tempDiv.innerHTML = tableHtml;
    document.body.appendChild(tempDiv);

    const canvas = await html2canvas(tempDiv);
    const imgData = canvas.toDataURL('image/png');

    const pdf = new jsPDF('p', 'mm', 'a4');
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = (canvas.height * pdfWidth) / canvas.width;

    pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
    pdf.save(`目标列表_${new Date().toISOString().split('T')[0]}.pdf`);

    document.body.removeChild(tempDiv);
    message.success('PDF 导出成功');
  } catch (error) {
    console.error('导出 PDF 失败:', error);
    message.error('导出 PDF 失败');
  }
};


// 分页配置
const pagination = {
  pageSize: 10
};

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'completed':
      return 'success';
    case 'in-progress':
      return 'warning';
    case 'expired':
      return 'error';
    default:
      return 'info';
  }
};

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'completed':
      return '已完成';
    case 'in-progress':
      return '进行中';
    case 'expired':
      return '已过期';
    default:
      return '未知';
  }
};

// 获取状态对应的颜色
const getStatusColor = (status: string) => {
  switch (status) {
    case 'completed':
      return '#00c9a7'; // 绿色
    case 'in-progress':
      return '#00c9a7'; // 紫色
    case 'expired':
      return '#ff6b6b'; // 红色
    default:
      return '#409eff'; // 蓝色
  }
};

// 优先级选项
const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '紧', value: 'urgent' },
  { label: '高', value: 'high' }
];

// 获取优先级文本
const getPriorityText = (level: string) => {
  const option = priorityOptions.find(opt => opt.value === level);
  return option ? option.label : '中';
};

// 获取优先级标签类型
const getPriorityType = (level: string) => {
  switch (level) {
    case 'low': return 'success';
    case 'high': return 'warning';
    case 'urgent': return 'error';
    case 'medium': default: return 'success';
  }
};

// 获取优先级对应的颜色值，用于组头色点
const getPriorityColor = (level: string): string => {
  const colorMap: Record<string, string> = {
    urgent: '#ef4444',
    high: '#f87171',
    medium: '#f59e0b',
    low: '#4ade80'
  };
  return colorMap[level] || '#999';
};

// 获取进度条颜色
const getProgressColor = (goal: any) => {
  if (!goal.childGoals || goal.childGoals.length === 0) return '#00c9a7';

  const finishedCount = goal.childGoals.filter((c: any) => c.finish).length;
  const progress = Math.round(finishedCount / goal.childGoals.length * 100);

  if (progress === 100) return '#00c9a7';
  if (progress >= 50) return '#00c9a7';
  return '#409eff';
};

// 完成确认弹窗状态
const showFinishConfirm = ref(false);
const pendingFinishGoal = ref<{ goal: any; index: number } | null>(null);
const isCompleteAllMode = ref(false);  // 是否为"一键全部完成"模式

// 自动定位状态
const locatingStatus = ref<'idle' | 'locating' | 'success' | 'fail'>('idle');
const autoLocationAddr = ref('');
const autoLocationCoord = ref<{ lng: number; lat: number } | null>(null);

// 偏好设置
const prefSubGoals = ref(false);
const prefAllGoals = ref(false);

// 偏好前缀（localStorage）
const LOC_PREF_ALL = 'fm_auto_loc_all';
const LOC_PREF_GOAL_PREFIX = 'fm_auto_loc_goal_';

// 弹窗关闭时没有任何完成操作
const cancelFinishConfirm = () => {
  showFinishConfirm.value = false;
  pendingFinishGoal.value = null;
  isCompleteAllMode.value = false;
  locatingStatus.value = 'idle';
  autoLocationAddr.value = '';
  autoLocationCoord.value = null;
};

// 自动定位（浏览器 Geolocation API）
const startGeolocation = () => {
  if (!navigator.geolocation) {
    locatingStatus.value = 'fail';
    return;
  }
  locatingStatus.value = 'locating';
  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      const { latitude, longitude } = pos.coords;
      autoLocationCoord.value = { lng: longitude, lat: latitude };
      // 通过后端逆地理编码获取地址名
      try {
        const res = await getM('amap/regeo', { location: `${longitude},${latitude}` });
        if (isSuccess(res)) {
          const data = res.data.data || res.data || res;
          autoLocationAddr.value = data?.formatted_address || '已定位';
        } else {
          autoLocationAddr.value = `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`;
        }
      } catch (error) {
        console.log(error);
        
        // autoLocationAddr.value = `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`;
      }
      locatingStatus.value = 'success';
    },
    () => {
      locatingStatus.value = 'fail';
    },
    { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
  );
};

// 标记子目标为完成（主入口：先判断是否需要弹窗）
const finishChildGoal = async (goal: any, index: number) => {
  const currentGoalId = goal?._id || goal?.id;

  // 检查是否已开启全局自动记录或当前目标的自动记录
  const autoAll = localStorage.getItem(LOC_PREF_ALL) === 'true';
  const autoGoal = currentGoalId ? localStorage.getItem(LOC_PREF_GOAL_PREFIX + currentGoalId) === 'true' : false;

  if (autoAll || autoGoal) {
    // 自动模式：直接定位并完成，不弹窗
    await doFinishWithLocation(goal, index);
    return;
  }

  // 非自动模式：弹出确认框
  pendingFinishGoal.value = { goal, index };
  prefSubGoals.value = false;
  prefAllGoals.value = false;
  locatingStatus.value = 'idle';
  autoLocationAddr.value = '';
  autoLocationCoord.value = null;
  showFinishConfirm.value = true;

  // 弹窗渲染后自动开始定位
  await nextTick();
  startGeolocation();
};

// 弹窗确认：记录（或不记录）地点并完成
const confirmFinishWithLocation = async () => {
  if (!pendingFinishGoal.value) return;
  const { goal, index } = pendingFinishGoal.value;

  // 保存偏好设置
  const currentGoalId = goal?._id || goal?.id;
  if (prefSubGoals.value && currentGoalId) {
    localStorage.setItem(LOC_PREF_GOAL_PREFIX + currentGoalId, 'true');
  }
  if (prefAllGoals.value) {
    localStorage.setItem(LOC_PREF_ALL, 'true');
  }

  showFinishConfirm.value = false;
  const isAll = isCompleteAllMode.value;
  pendingFinishGoal.value = null;
  isCompleteAllMode.value = false;

  if (locatingStatus.value === 'success' && autoLocationCoord.value) {
    if (isAll) {
      await doFinishAllWithLocation(goal);
    } else {
      await doFinishWithLocation(goal, index);
    }
  } else {
    if (isAll) {
      await doFinishAllSimple(goal);
    } else {
      await doFinishSimple(goal, index);
    }
  }
};

// 带地点的完成逻辑
const doFinishWithLocation = async (goal: any, index: number) => {
  try {
    const updatedGoal = JSON.parse(JSON.stringify(goal));
    const coord = autoLocationCoord.value || { lng: 0, lat: 0 };

    updatedGoal.childGoals[index].locationName = autoLocationAddr.value || '已定位';
    updatedGoal.childGoals[index].locationCoord = [coord.lng, coord.lat];
    updatedGoal.childGoals[index].finish = true;
    updatedGoal.childGoals[index].finishDate = new Date();

    const finishedCount = updatedGoal.childGoals.filter((c: any) => c.finish).length;
    updatedGoal.progress = Math.round(finishedCount / updatedGoal.childGoals.length * 100);

    if (finishedCount === updatedGoal.childGoals.length) {
      updatedGoal.status = 'completed';
      celebrationGoalTitle.value = updatedGoal.title;
    }

    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      if (updatedGoal.status === 'completed') {
        showCelebration.value = true;
      }
      message.success('子目标已完成，地点已记录');
      getGoals();
    } else {
      expiredGoalToast(res);
    }
  } catch (error) {
    message.error('操作失败');
    console.error(error);
  }
};

// 不带地点，只完成子目标
const doFinishSimple = async (goal: any, index: number) => {
  try {
    const updatedGoal = JSON.parse(JSON.stringify(goal));
    updatedGoal.childGoals[index].finish = true;
    updatedGoal.childGoals[index].finishDate = new Date();

    const finishedCount = updatedGoal.childGoals.filter((c: any) => c.finish).length;
    updatedGoal.progress = Math.round(finishedCount / updatedGoal.childGoals.length * 100);

    if (finishedCount === updatedGoal.childGoals.length) {
      updatedGoal.status = 'completed';
      celebrationGoalTitle.value = updatedGoal.title;
    }

    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      if (updatedGoal.status === 'completed') {
        showCelebration.value = true;
      }
      message.success('子目标已完成');
      getGoals();
    } else {
      expiredGoalToast(res);
    }
  } catch (error) {
    message.error('操作失败');
    console.error(error);
  }
};

const expiredGoalToast = (res) => {
  if (res.data.msg) {
    message.error(res.data.msg);
  } else {
    message.error('更新失败');
  }
  getGoals()
}

// 一键全部完成 + 统一记录地点（全部子目标使用同一个定位地址）
const doFinishAllWithLocation = async (goal: any) => {
  try {
    const updatedGoal = JSON.parse(JSON.stringify(goal));
    const coord = autoLocationCoord.value || { lng: 0, lat: 0 };

    updatedGoal.childGoals.forEach((child: any) => {
      if (!child.finish) {
        child.locationName = autoLocationAddr.value || '已定位';
        child.locationCoord = [coord.lng, coord.lat];
        child.finish = true;
        child.finishDate = new Date();
      }
    });

    updatedGoal.progress = 100;
    updatedGoal.status = 'completed';
    if (updatedGoal.completedDate === undefined) {
      updatedGoal.completedDate = new Date();
    }

    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      celebrationGoalTitle.value = updatedGoal.title;
      showCelebration.value = true;
      getGoals();
    } else {
      expiredGoalToast(res);
    }
  } catch (error) {
    message.error('一键完成操作失败');
    console.error(error);
  }
};

// 一键全部完成 + 不记录地点
const doFinishAllSimple = async (goal: any) => {
  try {
    const updatedGoal = JSON.parse(JSON.stringify(goal));

    updatedGoal.childGoals.forEach((child: any, idx: number) => {
      if (!child.finish) {
        child.finish = true;
        child.finishDate = new Date();
      }
    });

    updatedGoal.progress = 100;
    updatedGoal.status = 'completed';
    if (updatedGoal.completedDate === undefined) {
      updatedGoal.completedDate = new Date();
    }

    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      celebrationGoalTitle.value = updatedGoal.title;
      showCelebration.value = true;
      getGoals();
    } else {
      expiredGoalToast(res);
    }
  } catch (error) {
    message.error('一键完成操作失败');
    console.error(error);
  }
};

// 取消子目标完成状态
const unfinishChildGoal = async (goal: any, index: number) => {
  try {
    const updatedGoal = { ...goal };
    updatedGoal.childGoals[index].finish = false;

    // 更新进度
    const finishedCount = updatedGoal.childGoals.filter((c: any) => c.finish).length;
    updatedGoal.progress = Math.round(finishedCount / updatedGoal.childGoals.length * 100);

    // 如果之前是完成状态，现在需要改为进行中
    if (updatedGoal.status === 'completed') {
      updatedGoal.status = 'in-progress';
    }

    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      message.success('已取消完成状态');
      // 更新本地数据
      // const goalIndex = goals.value.findIndex((g: any) => g.id === goal.id);
      // if (goalIndex !== -1) {
      //   goals.value[goalIndex] = updatedGoal;
      // }
      getGoals()
    } else {
      expiredGoalToast(res);
    }
  } catch (error) {
    message.error('操作失败');
    console.error(error);
  }
};

// 子目标行内编辑：开始编辑
const startEditChild = (index: number, childGoal: any) => {
  editingChildIndex.value = index
  editingChildText.value = childGoal.message || ''
  nextTick(() => {
    // 让输入框自动获取焦点
    const input = document.querySelector('.child-edit-input') as HTMLInputElement
    if (input) input.focus()
  })
}

// 子目标行内编辑：保存
const saveEditChild = async (goal: any, index: number) => {
  const newText = editingChildText.value.trim()
  if (!newText) return
  // 内容没变，直接退出编辑模式
  if (newText === goal.childGoals[index].message) {
    editingChildIndex.value = -1
    return
  }

  try {
    const updatedGoal = JSON.parse(JSON.stringify(goal))
    updatedGoal.childGoals[index].message = newText

    const res = await postM('editGoal', updatedGoal)
    if (isSuccess(res)) {
      getGoals()
    } else {
      expiredGoalToast(res)
    }
  } catch (error) {
    message.error('操作失败')
    console.error(error)
  } finally {
    editingChildIndex.value = -1
  }
}

// 子目标行内编辑：取消
const cancelEditChild = () => {
  editingChildIndex.value = -1
  editingChildText.value = ''
}

// 在详情面板底部添加子目标
const addChildGoal = async () => {
  const msg = newChildMessage.value.trim()
  if (!msg || !currentSelectedGoal.value) return

  try {
    const updatedGoal = JSON.parse(JSON.stringify(currentSelectedGoal.value))
    if (!updatedGoal.childGoals) updatedGoal.childGoals = []

    // 追加新子目标
    updatedGoal.childGoals.unshift({
      message: msg,
      finish: false,
      finishDate: null,
      fileList: []
    })

    const res = await postM('editGoal', updatedGoal)
    if (isSuccess(res)) {
      newChildMessage.value = ''
      getGoals()
    } else {
      expiredGoalToast(res)
    }
  } catch (error) {
    message.error('添加失败')
    console.error(error)
  }
}

// 一键完成整个目标（弹出确认弹窗，可选记录地点）
const completeAllGoal = async (goal: any) => {
  const currentGoalId = goal?._id || goal?.id;

  // 检查是否已开启全局自动记录或当前目标的自动记录
  const autoAll = localStorage.getItem(LOC_PREF_ALL) === 'true';
  const autoGoal = currentGoalId ? localStorage.getItem(LOC_PREF_GOAL_PREFIX + currentGoalId) === 'true' : false;

  if (autoAll || autoGoal) {
    // 自动模式：直接定位并全部完成，不弹窗
    // 先在自动模式下获取位置
    if (!navigator.geolocation) {
      await doFinishAllSimple(goal);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const { latitude, longitude } = pos.coords;
        autoLocationCoord.value = { lng: longitude, lat: latitude };
        try {
          const res = await getM('amap/regeo', { location: `${longitude},${latitude}` });
          if (isSuccess(res)) {
            const data = res.data.data || res.data || res;
            autoLocationAddr.value = data?.formatted_address || '已定位';
          }
        } catch {}
        await doFinishAllWithLocation(goal);
      },
      async () => {
        await doFinishAllSimple(goal);
      },
      { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
    );
    return;
  }

  // 非自动模式：弹出确认框
  isCompleteAllMode.value = true;
  pendingFinishGoal.value = { goal, index: -1 };
  prefSubGoals.value = false;
  prefAllGoals.value = false;
  locatingStatus.value = 'idle';
  autoLocationAddr.value = '';
  autoLocationCoord.value = null;
  showFinishConfirm.value = true;

  await nextTick();
  startGeolocation();
};

/**
 * 续签目标截止日期：调用后端接口，deadline +30天，每个目标最多续签3次
 * 续签成功后刷新列表，更新 deadlineString 显示
 */
const renewGoalDeadline = async (goal: any) => {
  try {
    const res = await postM('renewGoalDeadline', { goalId: goal._id })
    if (isSuccess(res)) {
      message.success(`续签成功！截止日期已延长30天（剩余${3 - (res.data.data?.renewCount || 0)}次）`)
      getGoals()
    } else {
      expiredGoalToast(res)
    }
  } catch (error) {
    message.error('续签失败')
    console.error(error)
  }
}

// 筛选后的目标列表
const filteredGoals = computed(() => {
  let result = [...goals.value];

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(goal =>
      goal.title.toLowerCase().includes(query) ||
      (goal.description && goal.description.toLowerCase().includes(query))
    );
  }

  // 所有人过滤
  if (ownershipFilter.value) {
    result = result.filter(goal => {
      const isOwned = goal.owner === store.state.user.username;
      return ownershipFilter.value === 'mine' ? isOwned : !isOwned;
    });
  }

  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(goal => goal.status === statusFilter.value);
  }

  // 标签过滤
  if (tagFilter.value && tagFilter.value.length > 0) {
    result = result.filter(goal => {
      if (!goal.tags) return false;
      return tagFilter.value.every(t => goal.tags.includes(t));
    });
  }

  // 时间过滤
  if (dateFilter.value && dateFilter.value.length === 2) {
    const [startTs, endTs] = dateFilter.value;
    const startDate = new Date(startTs);
    const endDate = new Date(endTs);

    // 调整结束时间到当天的 23:59:59
    endDate.setHours(23, 59, 59, 999);

    result = result.filter(goal => {
      if (!goal.deadline) return false;
      const deadline = new Date(goal.deadline);
      return deadline >= startDate && deadline <= endDate;
    });
  }

  return result;
});

// 用于控制按标题分组的展开/折叠状态
const expandedGroups = ref<Set<string>>(new Set())

// 切换分组的展开/折叠
const toggleGroup = (title: string) => {
  const set = new Set(expandedGroups.value)
  if (set.has(title)) {
    set.delete(title)
  } else {
    set.add(title)
  }
  expandedGroups.value = set
}

// 判断当前分组是否展开
const isGroupExpanded = (title: string) => {
  return expandedGroups.value.has(title)
}

/**
 * 对 filteredGoals 进行分组，生成扁平列表：
 * - 按标题模式：同标题合并为组（现有逻辑）
 * - 按优先级模式：按 urgent/high/medium/low 分成 4 组
 */
const groupedDisplayList = computed(() => {
  const list = filteredGoals.value

  // ======== 按优先级分组模式 ========
  if (viewMode.value === 'priority') {
    // 优先级显示顺序（高优先级在前）
    const priorityOrder = ['urgent', 'high', 'medium', 'low']

    // 按 level 字段分组
    const groups = new Map<string, any[]>()
    for (const goal of list) {
      const key = goal.level || 'medium'
      if (!groups.has(key)) groups.set(key, [])
      groups.get(key)!.push(goal)
    }

    // 按优先级顺序构建扁平列表：组头行 → 子目标行
    const result: any[] = []
    for (const pri of priorityOrder) {
      const goalsInGroup = groups.get(pri) || []
      if (goalsInGroup.length === 0) continue

      const label = priorityOptions.find(o => o.value === pri)?.label || pri
      result.push({
        _isGroupHeader: true,       // 复用组头行样式
        _isPriorityGroup: true,     // 标记为优先级组头，用于样式区分
        priority: pri,
        title: label,
        count: goalsInGroup.length,
        children: goalsInGroup
      })
      // 根据折叠状态决定是否显示子目标
      if (isGroupExpanded(label)) {
        result.push(...goalsInGroup)
      }
    }
    return result
  }

  // ======== 按截止月份分组模式 ========
  if (viewMode.value === 'time') {
    // 按 deadline 的年月分组
    const groups = new Map<string, any[]>()
    for (const goal of list) {
      if (!goal.deadline) continue
      const date = new Date(goal.deadline)
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      if (!groups.has(key)) groups.set(key, [])
      groups.get(key)!.push(goal)
    }

    // 按年月排序（升序 = 从远到近，降序 = 从近到远）
    const sortedKeys = Array.from(groups.keys()).sort()
    if (!sortAsc.value) sortedKeys.reverse()

    const result: any[] = []
    for (const key of sortedKeys) {
      const [year, month] = key.split('-')
      const label = `${year}年${parseInt(month)}月`
      const goalsInGroup = groups.get(key)!
      result.push({
        _isGroupHeader: true,
        _isTimeGroup: true,
        title: label,
        count: goalsInGroup.length,
        children: goalsInGroup
      })
      // 根据折叠状态决定是否显示子目标
      if (isGroupExpanded(label)) {
        result.push(...goalsInGroup)
      }
    }
    return result
  }

  // ======== 原有按标题分组逻辑 ========
  // 1. 按 title 分组
  const groups = new Map<string, any[]>()
  for (const goal of list) {
    const key = goal.title
    if (!groups.has(key)) groups.set(key, [])
    groups.get(key)!.push(goal)
  }

  // 2. 分开收集：单个目标和子分组信息
  const singles: any[] = []
  const subGroups: any[] = []   // 每个具体标题的子分组组头

  for (const [title, goalsInGroup] of groups.entries()) {
    if (goalsInGroup.length === 1) {
      singles.push(goalsInGroup[0])
    } else {
      subGroups.push({
        _isGroupHeader: true,       // 子分组组头
        title,
        count: goalsInGroup.length,
        children: goalsInGroup
      })
    }
  }

  // 3. 构建扁平列表：单个目标 → 根分组头 → （展开后子分组组头 → 子目标）
  const result: any[] = []

  if (subGroups.length > 0) {
    // 插入根分组头（目标分组）
    result.push({
      _isGroupHeader: true,
      _isRootGroup: true,
      title: '分组目标',
      count: subGroups.length,
      children: subGroups
    })

    // 根分组展开时，展平子分组组头
    if (isGroupExpanded('分组目标')) {
      for (const sg of subGroups) {
        result.push(sg)
        // 子分组再展开时，展平其子目标
        if (isGroupExpanded(sg.title)) {
          result.push(...sg.children)
        }
      }
    }
  }
  result.push(...singles)
  console.log("result",result);
  

  return result
})

// 统计有多少组（即有多少个同标题的目标分组）
const groupCount = computed(() => {
  // 优先级模式：统计有目标的优先级组数
  if (viewMode.value === 'priority') {
    const levels = new Set<string>()
    for (const goal of filteredGoals.value) {
      levels.add(goal.level || 'medium')
    }
    return levels.size
  }
  // 时间模式：统计有目标的月份组数
  if (viewMode.value === 'time') {
    const months = new Set<string>()
    for (const goal of filteredGoals.value) {
      if (!goal.deadline) continue
      const date = new Date(goal.deadline)
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      months.add(key)
    }
    return months.size
  }
  // 标题模式：统计有多于一个目标的标题组数
  const groups = new Map<string, number>()
  for (const goal of filteredGoals.value) {
    const key = goal.title
    groups.set(key, (groups.get(key) || 0) + 1)
  }
  let count = 0
  for (const [, total] of groups.entries()) {
    if (total > 1) count++
  }
  return count
})

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '未设置';

  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// ================= 定时任务周期完成可视化 =================
// 弹窗开关 + 当前选中的“系列”（同一标题的所有目标实例 + 命中的定时规则）
const showSeriesModal = ref(false);
const activeSeries = ref<any>(null);
// 当前用户的定时任务规则列表，用于把同标题折叠分组与定时规则关联起来
const recurringRules = ref<any[]>([]);

// 拉取定时任务规则（供周期可视化关联规则元信息）
const fetchRecurringRules = async () => {
  try {
    const res = await getM('/getRecurringGoals');
    if (res.data?.operSucc) {
      recurringRules.value = res.data.data || [];
    }
  } catch (error) {
    console.error('获取定时任务规则失败', error);
  }
};

// 点击折叠组头的图表小按钮：为该系列打开周期完成可视化弹窗
const openSeriesChart = (row: any) => {
  if (!row || !row.children || row.children.length === 0) return;
  // 尝试按标题匹配当前用户的定时任务规则（匹配不上也允许查看，只少展示规则元信息）
  const rule = recurringRules.value.find((r: any) => r.title === row.title);
  activeSeries.value = { title: row.title, children: row.children, rule: rule || null };
  showSeriesModal.value = true;
};
// ================= 定时任务周期完成可视化 END =================

// 获取目标列表
const getGoals = async () => {
  loading.value = true;
  try {
    const res = await getMPaths("getGoals", store.state.user.username, "正在获取目标数据...");
    if (isSuccess(res)) {
      goals.value = res.data.data || [];
      
      goals.value.forEach(goal => {
        goal.deadlineString = formatDate(goal.deadline);
      });

      // 同步当前选中的目标数据
      if (currentSelectedGoal.value && currentSelectedGoal.value._id) {
        const updatedCurrentGoal = goals.value.find(g => g._id === currentSelectedGoal.value._id);
        if (updatedCurrentGoal) {
          currentSelectedGoal.value = { ...updatedCurrentGoal };
        }
      }
    } else {
      message.error('获取目标列表失败');
    }
  } catch (error) {
    message.error('获取目标列表时发生错误', error);
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 添加新目标
const addNewGoal = () => {
  router.push('/add-goal');
};

// 刷新目标列表
const refreshGoals = () => {
  getGoals().then(() => {
    // 如果当前有选中的目标，更新选中的目标数据
    if (selectedGoal.value && selectedGoal.value._id) {
      const updatedGoal = goals.value.find(g => g._id === selectedGoal.value._id);
      if (updatedGoal) {
        selectedGoal.value = { ...updatedGoal };
      }
    }
    console.log("currentSelectedGoal.value",currentSelectedGoal.value);
    
    // 同步更新右侧详情面板的数据
    if (currentSelectedGoal.value && currentSelectedGoal.value._id) {
      const updatedCurrentGoal = goals.value.find(g => g._id === currentSelectedGoal.value._id);
      if (updatedCurrentGoal) {
        currentSelectedGoal.value = { ...updatedCurrentGoal };
      }else{
        currentSelectedGoal.value =goals.value[0];

      }
    }
  });
  // message.success('目标列表已刷新');
};

// 查看目标详情
const viewGoalDetail = (goal: any) => {
  selectedGoal.value = { ...goal };
  showDetailModal.value = true;
};

// 编辑目标
const editGoal = (goal: any) => {
  // 这里可以跳转到编辑页面，或者在模态框中编辑
  selectedGoal.value = { ...goal };
  showDetailModal.value = true;
};

// 保存目标
const saveGoal = (updatedGoal: any) => {
  // 这里可以实现保存目标的逻辑
  console.log('保存目标:', updatedGoal);
  message.success('目标已保存');
  refreshGoals();
};

// 处理排序变化
const handleSorterChange = (sorter: any) => {
  // 实现排序逻辑
  console.log('排序器变化:', sorter);
};

// 显示子目标文件上传模态框
const showChildGoalUpload = (goal: any, index: number): void => {
  currentChildGoal.value = goal;
  currentChildGoalIndex.value = index;

  // 初始化文件列表
  if (goal.childGoals && goal.childGoals[index] && goal.childGoals[index].fileList) {
    currentChildGoalFiles.value = [...goal.childGoals[index].fileList];
  } else {
    currentChildGoalFiles.value = [];
  }

  showChildGoalUploadModal.value = true;
};

// 关闭子目标文件上传模态框
const closeChildGoalUploadModal = (): void => {
  showChildGoalUploadModal.value = false;
  currentChildGoal.value = null;
  currentChildGoalIndex.value = -1;
  currentChildGoalFiles.value = [];
};

// 处理子目标文件上传成功
const handleChildGoalFileUploadSuccess = (files: any[]): void => {
  currentChildGoalFiles.value = files;
};

// 处理子目标文件删除
const handleChildGoalFileRemove = (files: any[]): void => {
  currentChildGoalFiles.value = files;
};

// 保存子目标文件
const saveChildGoalFiles = async (): Promise<void> => {
  if (!currentChildGoal.value || currentChildGoalIndex.value === -1) {
    message.error('保存失败，请重试');
    return;
  }

  try {
    // 创建目标副本
    const updatedGoal = { ...JSON.parse(JSON.stringify(currentChildGoal.value)) };

    // 确保子目标存在
    if (!updatedGoal.childGoals) {
      updatedGoal.childGoals = [];
    }

    // 确保当前子目标存在
    if (!updatedGoal.childGoals[currentChildGoalIndex.value]) {
      updatedGoal.childGoals[currentChildGoalIndex.value] = {};
    }

    // 更新子目标的文件列表
    updatedGoal.childGoals[currentChildGoalIndex.value].fileList = [...currentChildGoalFiles.value];

    // 保存到服务器
    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      message.success('文件保存成功');

      // 更新本地数据
      getGoals()

      // 关闭模态框
    } else {
      expiredGoalToast(res)
    }
    closeChildGoalUploadModal();

  } catch (error) {
    message.error('保存失败，请重试');
    closeChildGoalUploadModal();

    console.error(error);
  }
};

// 查看子目标文件
const viewChildGoalFiles = (row: any, index): void => {
  currentChildGoal.value = row;
  currentChildGoalIndex.value = index;
  let childGoal = row.childGoals[index];
  // 获取子目标的文件列表
  if (childGoal.fileList && childGoal.fileList.length > 0) {
    viewChildGoalFilesList.value = [...childGoal.fileList];
  } else {
    viewChildGoalFilesList.value = [];
  }

  showChildGoalFilesModal.value = true;
};

// 下载文件
const downloadFile = (file: any): void => {
  if (!file) return;

  const fileName = file.name || file.originalFilename;
  const url = file.url || file.fileUrl || `${baseURL()}/file/${fileName}`;

  // 创建一个隐藏的a标签来下载文件
  const a = document.createElement('a');
  a.href = url;
  a.download = fileName || 'download';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
};

// 删除子目标文件
const removeChildGoalFile = async (file: any): Promise<void> => {
  console.log("0o", currentChildGoal.value);

  if (!file || !currentChildGoal.value || currentChildGoalIndex.value === -1) return;

  try {
    // 创建目标副本
    const updatedGoal = { ...currentChildGoal.value };

    // 确保子目标存在
    if (!updatedGoal.childGoals) {
      updatedGoal.childGoals = [];
    }

    // 确保当前子目标存在
    if (!updatedGoal.childGoals[currentChildGoalIndex.value]) {
      updatedGoal.childGoals[currentChildGoalIndex.value] = {};
    }

    // 获取当前子目标的文件列表
    const fileList = updatedGoal.childGoals[currentChildGoalIndex.value].fileList || [];

    // 从文件列表中删除指定文件
    const updatedFileList = fileList.filter((f: any) => {
      const fileId = f.id || f.name;
      const targetFileId = file.id || file.name;
      return fileId !== targetFileId;
    });

    // 更新子目标的文件列表
    updatedGoal.childGoals[currentChildGoalIndex.value].fileList = updatedFileList;

    // 保存到服务器
    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      message.success('文件删除成功');

      // 更新本地数据
      const goalIndex = goals.value.findIndex((g: any) => g.id === updatedGoal.id);
      if (goalIndex !== -1) {
        goals.value[goalIndex] = updatedGoal;
      }

      // 更新查看文件列表
      viewChildGoalFilesList.value = updatedFileList;
      closeChildGoalUploadModal();
    } else {
      message.error('删除失败，请重试');
    }
  } catch (error) {
    message.error('删除失败，请重试');
    console.error(error);
  }
};

// 初始化
onMounted(() => {
  getGoals();
  fetchRecurringRules();
});
</script>

<style scoped>
/* ----------------------------------
   1. 全局容器与背景 (Global Container)
   ---------------------------------- */
.home-container {
  background-color: #0f0f13;
  color: #ffffff;
  max-height: 120vh;
  position: relative;
  overflow-x: hidden;
}

.home-container-light {
  background-color: #f0f2f5;
  color: #1f2937;
  max-height: 90%;
  position: relative;
  overflow-x: hidden;
}

.main-content-wrapper {
  height: 60%;
  overflow-y: auto;
}

/* 列表入场动效 */
.stagger-list-container :deep(.el-table__row) {
  animation: staggerIn 0.6s cubic-bezier(0.22, 1, 0.36, 1) backwards;
}

@keyframes staggerIn {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 为前10行设置交错延迟 */
.stagger-list-container :deep(.el-table__row:nth-child(1)) { animation-delay: 0.05s; }
.stagger-list-container :deep(.el-table__row:nth-child(2)) { animation-delay: 0.1s; }
.stagger-list-container :deep(.el-table__row:nth-child(3)) { animation-delay: 0.15s; }
.stagger-list-container :deep(.el-table__row:nth-child(4)) { animation-delay: 0.2s; }
.stagger-list-container :deep(.el-table__row:nth-child(5)) { animation-delay: 0.25s; }
.stagger-list-container :deep(.el-table__row:nth-child(6)) { animation-delay: 0.3s; }
.stagger-list-container :deep(.el-table__row:nth-child(7)) { animation-delay: 0.35s; }
.stagger-list-container :deep(.el-table__row:nth-child(8)) { animation-delay: 0.4s; }
.stagger-list-container :deep(.el-table__row:nth-child(9)) { animation-delay: 0.45s; }
.stagger-list-container :deep(.el-table__row:nth-child(10)) { animation-delay: 0.5s; }

.main-content {
  padding: 8px 15px;
  max-width: 1600px;
  margin: 0 auto;
}

/* ----------------------------------
   2. 头部区域 (Page Header)
   ---------------------------------- */
.page-header {
  margin-bottom: 32px;
}

.hero-title,
.hero-title-light {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

/* 方案一：控制面板样式重构 */
.command-deck-section {
  margin-bottom: 24px;
}

.command-deck {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid var(--border-color);
  padding: 12px 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.command-deck.expanded {
  padding-bottom: 20px;
}

.command-main-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.action-group, .search-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.divider-vertical {
  width: 1px;
  height: 24px;
  background: var(--border-color);
  margin: 0 4px;
}

.deck-search {
  width: 240px;
  transition: all 0.3s ease;
}

.deck-search:focus-within {
  width: 300px;
}

.command-advanced-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--border-color);
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-color);
  opacity: 0.6;
  padding-left: 4px;
}

/* 动画效果 */
.expand-fade-enter-active, .expand-fade-leave-active {
  transition: all 0.3s ease;
  max-height: 200px;
  overflow: hidden;
}

.expand-fade-enter-from, .expand-fade-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-10px);
}

.hero-title {
  background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-title-light {
  color: #111827;
}

.hero-subtitle,
.hero-subtitle-light {
  font-size: 16px;
  opacity: 0.8;
}

/* ----------------------------------
   3. 卡片通用样式 (Cards)
   ---------------------------------- */
.feature-card,
.detail-card,
.list-card,
.empty-selection {
  background: rgba(30, 30, 35, 0.6);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  border-radius: 16px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  margin-bottom: 5px;
  /* max-height: 20vh; */

}

.feature-card-light {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  margin-bottom: 5px;
  /* max-height: 70%; */
}

/* 统一左右卡片高度 */
.list-card,
.detail-card,
.empty-selection {
  max-height: 75vh;
}

.list-card :deep(.n-card__content),
.detail-card :deep(.n-card__content),
.empty-selection :deep(.n-card__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
  /* 防止内容溢出撑破圆角 */
  padding: 0;
  /* 接管 padding */
}

/* 移动端适配 */
@media screen and (max-width: 1023px) {

  .list-card,
  .detail-card,
  .empty-selection {
    height: auto !important;
    min-height: 400px;
  }
}

/* 卡片 Header 微调 */
.card-header-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

/* 头部统计信息（目标数 / 分组数） */
.header-stat {
  font-size: 12px;
  font-weight: 400;
  color: #888;
}
.header-stat-sep {
  color: #555;
  font-size: 12px;
}
.header-stat-highlight {
  color: #00c9a7;
  font-weight: 500;
}

/* 分组模式切换按钮 */
.group-toggle-wrapper {
  margin-left: auto;
}
.group-toggle-wrapper .n-button-group .n-button {
  font-size: 11px;
  height: 24px;
  padding: 0 8px;
}

/* ----------------------------------
   4. 表格样式优化 (Element Table)
   ---------------------------------- */
:deep(.el-table) {
  background-color: transparent !important;
  overflow: visible; /* 方案B：放通内部 overflow，见下方说明 */
  --el-table-border-color: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0, 0, 0, 0.2);
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.03);
}

/* ==========================================================
   方案B：让 x 轴横向滚动条"贴底常驻"，无需纵向滚到最后一行才出现
   原理：横向滚动条默认 absolute 定位在表格内容最底部（随最后一行被外层
   纵向滚动带出可视区）。把它改成 position:sticky，即可在纵向滚动时
   吸附在滚动容器的可视底部。
   注意：sticky 只对"最近的滚动容器"生效，而 .el-table、.el-table__body-wrapper、
   .el-scrollbar 默认都是 overflow:hidden（自身不滚动），会把 sticky 截获在内部；
   因此必须把这三层放通为 visible，让 sticky 一路上溯到真正发生纵向滚动的
   卡片内容区（.n-card__content）。
   ========================================================== */
:deep(.el-table__body-wrapper) {
  overflow: visible;
}
:deep(.el-table .el-scrollbar) {
  overflow: visible;
}
:deep(.el-table .el-scrollbar__bar.is-horizontal) {
  position: sticky !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  z-index: 3;
}

.home-container-light :deep(.el-table) {
  --el-table-header-bg-color: rgba(0, 0, 0, 0.03);
  --el-table-row-hover-bg-color: rgba(0, 0, 0, 0.02);
  color: #333;
}

/* 去除所有边框和底部白线 */
:deep(.el-table__inner-wrapper::before),
:deep(.el-table__border-left-patch) {
  display: none !important;
}

:deep(.el-table td.el-table__cell),
:deep(.el-table th.el-table__cell) {
  border-bottom: none !important;
}

/* 表头优化 */
:deep(.el-table th.el-table__cell) {
  text-transform: uppercase;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #6b7280;
  /* Muted text */
  padding: 12px 0;
}

/* 单元格优化 */
:deep(.el-table td.el-table__cell) {
  padding: 16px 0;
}

/* 选中行样式 */
:deep(.el-table .selected-row) {
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.08) 0%, rgba(16, 185, 129, 0.01) 100%) !important;
}

:deep(.el-table .selected-row td) {
  border-left: 3px solid #10b981 !important;
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.08) 0%, rgba(16, 185, 129, 0.01) 100%) !important;
}

/* 组头行样式 - 同标题目标折叠 */
:deep(.el-table .group-header-row) {
  background: rgba(0, 201, 167, 0.06) !important;
  cursor: pointer !important;
}
:deep(.el-table .group-header-row td) {
  border-bottom: 1px solid rgba(0, 201, 167, 0.15) !important;
}
:deep(.el-table .group-header-row:hover) {
  background: rgba(0, 201, 167, 0.1) !important;
}

/* 时间组头行 - 按截止月份分组 */
:deep(.el-table .time-group-header-row) {
  /* background: rgba(79, 255, 193, 0.06) !important; */
  cursor: pointer !important;
}
:deep(.el-table .time-group-header-row td) {
  /* border-bottom: 1px solid rgba(79, 137, 255, 0.15) !important; */
  /* border-left: 3px solid #4fffd0 !important; */
  color: #00c9a7;
}
:deep(.el-table .time-group-header-row:hover) {
  background: rgba(79, 137, 255, 0.1) !important;
}

/* 优先级组头行 - 左边框颜色区分 */
/* :deep(.el-table .urgent-group-header-row) {
  background: rgba(239, 68, 68, 0.06) !important;
  cursor: pointer !important;
}
:deep(.el-table .urgent-group-header-row td) {
  border-bottom: 1px solid rgba(239, 68, 68, 0.15) !important;
  border-left: 3px solid #ef4444 !important;
}
:deep(.el-table .urgent-group-header-row:hover) {
  background: rgba(239, 68, 68, 0.1) !important;
}

:deep(.el-table .high-group-header-row) {
  background: rgba(248, 113, 113, 0.06) !important;
  cursor: pointer !important;
}
:deep(.el-table .high-group-header-row td) {
  border-bottom: 1px solid rgba(248, 113, 113, 0.15) !important;
  border-left: 3px solid #f87171 !important;
}
:deep(.el-table .high-group-header-row:hover) {
  background: rgba(248, 113, 113, 0.1) !important;
}

:deep(.el-table .medium-group-header-row) {
  background: rgba(245, 158, 11, 0.06) !important;
  cursor: pointer !important;
}
:deep(.el-table .medium-group-header-row td) {
  border-bottom: 1px solid rgba(245, 158, 11, 0.15) !important;
  border-left: 3px solid #f59e0b !important;
}
:deep(.el-table .medium-group-header-row:hover) {
  background: rgba(245, 158, 11, 0.1) !important;
}

:deep(.el-table .low-group-header-row) {
  background: rgba(74, 222, 128, 0.06) !important;
  cursor: pointer !important;
}
:deep(.el-table .low-group-header-row td) {
  border-bottom: 1px solid rgba(74, 222, 128, 0.15) !important;
  border-left: 3px solid #4ade80 !important;
}
:deep(.el-table .low-group-header-row:hover) {
  background: rgba(74, 222, 128, 0.1) !important;
}

:deep(.el-table .low-group-header-row) {
  background: rgba(0, 201, 7, 0.06) !important;
  cursor: pointer !important;
}
:deep(.el-table .low-group-header-row td) {
  border-bottom: 1px solid rgba(0, 201, 167, 0.15) !important;
}
:deep(.el-table .low-group-header-row:hover) {
  background: rgba(0, 201, 167, 0.1) !important;
} */

/* ----------------------------------
   5. 详情页排版 (Detail Layout)
   ---------------------------------- */
.detail-header-wrapper {
  margin-bottom: 24px;
}

.detail-meta-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.goal-id-text {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 0.5px;
}

.home-container-light .goal-id-text {
  color: rgba(0, 0, 0, 0.4);
}

.detail-main-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.detail-hero-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
  background: linear-gradient(120deg, #fff 0%, #e2e8f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.home-container-light .detail-hero-title {
  background: linear-gradient(120deg, #111827 0%, #374151 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

/* ----------------------------------
   6. 信息网格 (Info Grid)
   ---------------------------------- */
.info-grid-modern {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.info-item-modern {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item-modern.full-width {
  grid-column: span 2;
}

.info-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 500;
}

.home-container-light .info-label {
  color: rgba(0, 0, 0, 0.5);
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.home-container-light .info-value {
  color: #111827;
}

.description-text {
  font-size: 14px;
  line-height: 1.6;
  font-weight: 400;
  opacity: 0.9;
}

.divider-line {
  height: 1px;
  background: rgba(255, 255, 255, 0.08);
  margin: 0 0 32px 0;
}

.home-container-light .divider-line {
  background: rgba(0, 0, 0, 0.06);
}

/* ----------------------------------
   7. 子目标胶囊样式 (Capsule Sub-goals)
   ---------------------------------- */
.sub-goals-list {
  margin-bottom: 32px;
}

.section-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.section-title-row h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.sub-goals-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.child-goal-capsule {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 50px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.home-container-light .child-goal-capsule {
  background: rgba(255, 255, 255, 0.5);
  border-color: rgba(0, 0, 0, 0.05);
}

.child-goal-capsule:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.06);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.home-container-light .child-goal-capsule:hover {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.child-goal-capsule.completed {
  opacity: 0.6;
  background: transparent;
  border-style: dashed;
}

.child-goal-capsule.completed:hover {
  opacity: 0.8;
  transform: none;
}

.capsule-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.checkbox-circle {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.home-container-light .checkbox-circle {
  border-color: rgba(0, 0, 0, 0.2);
}

.checkbox-circle.checked {
  background: #10b981;
  border-color: #10b981;
  color: #fff;
}

.checkbox-circle:hover:not(.checked) {
  border-color: #10b981;
}

.capsule-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.capsule-title {
  font-size: 14px;
  font-weight: 500;
  cursor: text;
  transition: color 0.2s;
}
.capsule-title:hover {
  color: #00c9a7;
}

.capsule-title.strikethrough {
  text-decoration: line-through;
  opacity: 0.7;
}

/* 子目标行内编辑输入框 */
.child-edit-input {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid #00c9a7;
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  outline: none;
  width: 100%;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.child-edit-input:focus {
  border-color: #00c9a7;
  box-shadow: 0 0 0 2px rgba(0, 201, 167, 0.2);
}
.home-container-light .child-edit-input {
  background: rgba(0, 0, 0, 0.04);
  border-color: #00c9a7;
  color: #1f2937;
}
.home-container-light .child-edit-input:focus {
  box-shadow: 0 0 0 2px rgba(0, 201, 167, 0.15);
}

.finish-time {
  font-size: 11px;
  opacity: 0.5;
}

.more-btn {
  opacity: 0;
  transition: opacity 0.2s;
}

.child-goal-capsule:hover .more-btn {
  opacity: 1;
}

/* 子目标添加栏（详情面板底部浮动） */
.child-add-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 16px 24px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px dashed rgba(255, 255, 255, 0.12);
  border-radius: 50px;
  transition: all 0.2s;
}
.child-add-bar:focus-within {
  border-color: #00c9a7;
  background: rgba(0, 201, 167, 0.04);
}
.home-container-light .child-add-bar {
  background: rgba(0, 0, 0, 0.02);
  border-color: rgba(0, 0, 0, 0.1);
}
.home-container-light .child-add-bar:focus-within {
  border-color: #00c9a7;
  background: rgba(0, 201, 167, 0.04);
}

.child-add-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-size: 13px;
  color: #fff;
  padding: 4px 0;
}
.child-add-input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}
.home-container-light .child-add-input {
  color: #1f2937;
}
.home-container-light .child-add-input::placeholder {
  color: rgba(0, 0, 0, 0.25);
}

.add-bar-btn {
  flex-shrink: 0;
}

/* ----------------------------------
   8. 统计区域 (Summary)
   ---------------------------------- */
.summary-modern {
  margin-top: auto;
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: transparent;
  /* 透明背景 */
}

.home-container-light .summary-modern {
  border-top-color: rgba(0, 0, 0, 0.06);
}

.stat-block {
  text-align: center;
}

.stat-num {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
  font-family: 'Inter', sans-serif;
}

.stat-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.home-container-light .stat-desc {
  color: rgba(0, 0, 0, 0.4);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
}

.home-container-light .stat-divider {
  background: rgba(0, 0, 0, 0.1);
}

.success-text {
  color: #10b981;
}

.warning-text {
  color: #f59e0b;
}

/* 滚动条美化 */
.detail-content {
  padding: 24px;
  overflow-y: auto;
  height: 100%;
}

.detail-content::-webkit-scrollbar {
  width: 4px;
}

.detail-content::-webkit-scrollbar-track {
  background: transparent;
}

.detail-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

/* ----------------------------------
   9. 优先级标签 (简洁嵌入风格)
   ---------------------------------- */
.priority-tag {
  display: inline-block;
  padding: 1px 10px;
  border-radius: 100px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.3px;
  line-height: 20px;
}

/* 【列合并方案A】优先级色点 — 替代原来的文字标签，融合到状态列中 */
.pri-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
/* 低优先级 — 柔和绿 */
.pri-dot.pri-dot-low  { background: #4ade80; }
/* 中优先级 — 柔和橙 */
.pri-dot.pri-dot-medium { background: #fbbf24; }
/* 高优先级 — 柔和红 */
.pri-dot.pri-dot-high { background: #f87171; }
/* 紧急优先级 — 深红 */
.pri-dot.pri-dot-urgent { background: #ef4444; }

/* 【列合并优化】优先级紧凑色标 — 内置文字，无需图例即可辨识 */
.pri-badge {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 100px;
  font-size: 10px;
  font-weight: 600;
  line-height: 16px;
  letter-spacing: 0.3px;
  color: #fff;
  flex-shrink: 0;
}
/* 低 — 绿底白字 */
.pri-badge.pri-badge-low    { background: #4ade80; }
/* 中 — 橙底白字 */
.pri-badge.pri-badge-medium { background: #f59e0b; }
/* 高 — 红底白字 */
.pri-badge.pri-badge-high   { background: #f87171; }
/* 紧急 — 深红底白字 */
.pri-badge.pri-badge-urgent { background: #ef4444; }

/* 低 - 柔和绿 */
.priority-tag.pri-low {
  background: rgba(34, 197, 94, 0.1);
  color: #4ade80;
}

/* 中 - 柔和橙 */
.priority-tag.pri-medium {
  background: rgba(251, 191, 36, 0.1);
  color: #4ade80;
}

/* 高 - 柔和红 */
.priority-tag.pri-high {
  background: rgba(248, 113, 113, 0.1);
  color: #fbbf24;
}
.priority-tag.pri-urgent {
  background: rgba(248, 113, 113, 0.1);
  color: #f87171;
}

/* ===== 一键完成按钮样式 ===== */

/* 表格操作列容器 */
.table-actions {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 一键完成按钮 — 品牌色图标，hover 放大 + 微光 */
.complete-all-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: rgba(var(--fm-primary-rgb), 0.55);
  transition: all 0.25s ease;
  background-color: rgb(0 201 167 / 73%);;
}

.complete-all-btn:hover {
  color: rgb(var(--fm-primary-rgb));
  background-color: rgba(var(--fm-primary-rgb), 0.15);
  transform: scale(1.15);
  box-shadow: 0 0 12px rgba(var(--fm-primary-rgb), 0.25);
}

/* 续签按钮 — 日历图标，橘色系视觉区分 */
.renew-btn {
  width: 30px;
  height: 30px;
  margin-left: 4%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: rgba(255, 152, 0, 0.55);
  transition: all 0.25s ease;
  background-color: rgb(255 152 0 / 64%);
}

.renew-btn:hover {
  color: rgb(255, 152, 0);
  background-color: rgba(255, 152, 0, 0.15);
  transform: scale(1.15);
  box-shadow: 0 0 12px rgba(255, 152, 0, 0.25);
}

/* 已完成状态的徽标 — 绿色对勾 */
.already-done-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: #52c41a;
  background-color: rgba(82, 196, 26, 0.1);
  animation: badge-pop 0.3s ease;
}

@keyframes badge-pop {
  0% { transform: scale(0); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

/* 操作列状态提示文字（已完成/已过期） */
.status-hint {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  font-weight: 500;
  cursor: default;
}
/* 已完成 — 绿色 */
.status-hint-done {
  color: #4ade80;
}
/* 已过期 — 柔和红色 */
.status-hint-expired {
  color: #f87171;
}

/* ===== 完成确认弹窗 ===== */
.finish-confirm-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.location-section {
  background: rgba(0, 201, 167, 0.06);
  border: 1px solid rgba(0, 201, 167, 0.15);
  border-radius: 10px;
  padding: 14px;
}

.location-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--fm-base-text, #e6edf3);
  margin-bottom: 10px;
}

.location-status {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 24px;
}

.locating-text {
  font-size: 13px;
  color: var(--fm-secondary-text, #8b949e);
}

.located-result {
  display: flex;
  align-items: center;
  gap: 6px;
}

.located-dot {
  flex-shrink: 0;
}

.located-addr {
  font-size: 13px;
  color: #00c9a7;
}

.locating-fail {
  font-size: 12px;
  color: var(--fm-secondary-text, #8b949e);
}

.pref-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.finish-hint {
  font-size: 11px;
  color: var(--fm-secondary-text, #8b949e);
  line-height: 1.4;
}

.finish-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 周期可视化入口小按钮（折叠组头上的一键图表） */
.series-chart-trigger {
  display: inline-flex;
  align-items: center;
  padding: 2px;
  margin-left: 6px;
  border-radius: 4px;
  color: #00c9a7;
  cursor: pointer;
  transition: background 0.2s ease;
}

.series-chart-trigger:hover {
  background: rgba(0, 201, 167, 0.15);
}

/* ===== Element Plus 固定列修复 ===== */

/* 固定列外层容器 — 设置背景，不透传下层内容 */



</style>

<!-- 【原理说明】Element Plus 固定列渲染在独立 DOM 层中，Vue scoped 样式的 [data-v-xxx] 属性选择器
     匹配不到这个分离的 DOM 树，所以 :deep() 规则无法生效。
     这里使用非 scoped 的 <style> 块直接全局覆盖，确保固定列背景不透明。 -->
<style>
/* 固定列整个容器 — 不透明的纯色背景，遮住下方滚动过来的列 */
.el-table__fixed,
.el-table__fixed-right {
  background-color: var(--el-table-bg-color, #fff) !important;
}

/* 固定列 body 中每一行 — 确保覆盖 row 级别的透明样式 */
.el-table__fixed-right .el-table__body tr.el-table__row td,
.el-table__fixed .el-table__body tr.el-table__row td {
  background-color: var(--el-table-bg-color, #fff) !important;
}

/* 暗色模式下的固定列背景（与 n-card 暗色背景 #121212 一致） */
html.dark .el-table__fixed,
html.dark .el-table__fixed-right {
  background-color: #121212 !important;
}
html.dark .el-table__fixed-right .el-table__body tr.el-table__row td,
html.dark .el-table__fixed .el-table__body tr.el-table__row td {
  background-color: #121212 !important;
}

/* 固定列表头背景 */
.el-table__fixed-right .el-table__header th,
.el-table__fixed .el-table__header th {
  background-color: var(--el-table-header-bg-color, #f5f7fa) !important;
}
html.dark .el-table__fixed-right .el-table__header th,
html.dark .el-table__fixed .el-table__header th {
  background-color: #121212 !important;
}
</style>
