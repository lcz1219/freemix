<template>
  <n-layout :native-scrollbar="true" :class="isDark ? 'home-container' : 'home-container-light'">
    <!-- 装饰背景元素 -->
    <div class="background-elements">
      <div class="gradient-circle blue"></div>
      <div class="gradient-circle green"></div>
      <div class="gradient-circle purple"></div>
    </div>

    <!-- 顶部导航栏 -->
    <NavBar active-tab="goalmanagement" />

    <!-- 主内容区域 -->
    <n-layout-content class="main-content-wrapper">
      <div class="main-content">
        <!-- 页面标题 -->
        <section class="page-header">
          <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">目标管理</h1>
          <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
            管理您的所有目标，查看进度并进行编辑
          </p>
        </section>

        <!-- 控制面板 -->
        <section class="control-section">
          <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
            <n-space justify="space-between" align="center">
              <n-space>
                <n-button type="primary" @click="addNewGoal">
                  <template #icon>
                    <n-icon>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                        fill="currentColor">
                        <path d="M20,12H4v-1c0-0.6,0.4-1,1-1h14c0.6,0,1,0.4,1,1V12z"></path>
                        <path d="M4,12h16v1c0,0.6-0.4,1-1,1H5c-0.6,0-1-0.4-1-1V12z"></path>
                        <path
                          d="M20,10H4c-0.6,0-1,0.4-1,1v2c0,0.6,0.4,1,1,1h16c0.6,0,1-0.4,1-1v-2C21,10.4,20.6,10,20,10z M20,12H4v-1h16V12z">
                        </path>
                      </svg>
                    </n-icon>
                  </template>
                  添加新目标
                </n-button>

                <ExcelImport @import-success="refreshGoals" />

                <n-button @click="refreshGoals">
                  <template #icon>
                    <n-icon>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                        fill="currentColor">
                        <path
                          d="M17.6,6.4C16.2,5,14.2,4.2,12,4.2c-2.2,0-4.2,0.8-5.6,2.2l1.4,1.4C8.9,6.8,10.4,6.2,12,6.2 c1.6,0,3.1,0.6,4.2,1.6c1.1,1.1,1.7,2.6,1.7,4.2c0,1.6-0.6,3.1-1.7,4.2c-1.1,1.1-2.6,1.7-4.2,1.7c-1.6,0-3.1-0.6-4.2-1.7 L6.4,17.6c1.4,1.4,3.4,2.2,5.6,2.2c2.2,0,4.2-0.8,5.6-2.2c1.4-1.4,2.2-3.4,2.2-5.6C19.8,9.8,19,7.8,17.6,6.4z">
                        </path>
                      </svg>
                    </n-icon>
                  </template>
                  刷新
                </n-button>
              </n-space>

              <n-space>
                <n-input v-model:value="searchQuery" placeholder="搜索目标..." clearable style="width: 200px;">
                  <template #prefix>
                    <n-icon>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                        fill="currentColor">
                        <path
                          d="M21.7,20.3L18,16.6c1.2-1.5,1.9-3.4,1.9-5.4c0-4.4-3.6-8-8-8s-8,3.6-8,8s3.6,8,8,8c2,0,3.9-0.7,5.4-1.9 l3.7,3.7c0.2,0.2,0.5,0.3,0.7,0.3s0.5-0.1,0.7-0.3C22.1,21.3,22.1,20.7,21.7,20.3z M11,17c-3.3,0-6-2.7-6-6s2.7-6,6-6s6,2.7,6,6 S14.3,17,11,17z">
                        </path>
                      </svg>
                    </n-icon>
                  </template>
                </n-input>

                <n-select v-model:value="statusFilter" :options="statusOptions" clearable placeholder="状态筛选"
                  style="width: 120px;" />
              </n-space>
            </n-space>
          </n-card>
        </section>

        <!-- 目标列表 -->
        <section class="goals-section">
          <n-card :class="isDark ? 'feature-card' : 'feature-card-light'">
            <div class="card-header">
              <n-icon size="28" color="#8a2be2">
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
              <h2 class="card-title">我的目标</h2>
            </div>

            <el-table :data="filteredGoals" :class="isDark ? 'el-table-dark' : 'el-table-light'" style="width: 100%">
              <el-table-column type="expand">
                <template #default="props">
                  <div class="expanded-content-wrapper">
                    <div v-for="(childGoal, index) in props.row.childGoals" :key="index" class="child-goal-item">
                      <div class="child-goal-header">
                        <div class="child-goal-title">

                          <n-icon size="18" style="margin-right: 8px;" v-if="childGoal.finish">
                            <CheckmarkCircle/>
                          </n-icon>
                          <n-icon size="18" style="margin-right: 8px;" v-else>
                            <CheckmarkCircleOutline/>
                          </n-icon>
                          <n-ellipsis style="max-width: 300px">
                            {{ childGoal.message }}
                          </n-ellipsis>
                        </div>
                        <div class="child-goal-status">
                          <n-tag :type="childGoal.finish ? 'success' : 'warning'" size="small">
                            {{ childGoal.finish ? '已完成' : '进行中' }}
                          </n-tag>
                        </div>
                        <div class="child-goal-actions-inline">
                          <span class="finish-date" v-if="childGoal.finishDate">完成日期: {{ formatDate(childGoal.finishDate) }}</span>
                          <n-dropdown
                            trigger="click"
                            :options="getDropdownOptions(childGoal, props.row, index)"
                            @select="(key) => handleDropdownSelect(key, props.row, index, childGoal)"
                          >
                            <n-button type="info" secondary strong size="small">
                              操作
                              <n-icon>
                                <ChevronDownOutline />
                              </n-icon>
                            </n-button>
                          </n-dropdown>
                        </div>
                      </div>

                      <!-- <div class="child-goal-details">
                        <div class="child-goal-info-row">
                          <div class="info-item">
                            <n-icon size="16" class="info-icon">
                              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
                              </svg>
                            </n-icon>
                            <span v-if="childGoal.description">{{ childGoal.description }}</span>
                            <span v-else class="no-description">暂无描述</span>
                          </div>
                        </div>
                      </div> -->
                    </div>

                    <div class="summary-section">
                      <div class="summary-header">
                        <n-icon size="18">
                          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="1em" height="1em"
                            fill="currentColor">
                            <path
                              d="M12,2C6.5,2,2,6.5,2,12s4.5,10,10,10s10-4.5,10-10S17.5,2,12,2z M12,20c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8 S16.4,20,12,20z" />
                            <path
                              d="M13,11.6V7c0-0.6-0.4-1-1-1s-1,0.4-1,1v5.6c-0.6,0.3-1,1-1,1.7c0,1.1,0.9,2,2,2s2-0.9,2-2C14,12.6,13.6,11.9,13,11.6z" />
                          </svg>
                        </n-icon>
                        <span class="summary-title">子目标汇总</span>
                      </div>

                      <div class="summary-content">
                        <div class="summary-stats">
                          <div class="stat-item">
                            <div class="stat-value">{{ props.row.childGoals ? props.row.childGoals.length : 0 }}</div>
                            <div class="stat-label">总计</div>
                          </div>
                          <div class="stat-item">
                            <div class="stat-value" style="color: #00c9a7;">{{props.row.childGoals ?
                              props.row.childGoals.filter((c: any) => c.finish).length : 0}}</div>
                            <div class="stat-label">已完成</div>
                          </div>
                          <div class="stat-item">
                            <div class="stat-value" style="color: #ff6b6b;">{{props.row.childGoals ?
                              props.row.childGoals.filter((c: any) => !c.finish).length : 0}}</div>
                            <div class="stat-label">未完成</div>
                          </div>
                        </div>

                        <n-progress type="line" :percentage="props.row.childGoals && props.row.childGoals.length > 0
                          ? Math.round(props.row.childGoals.filter((c: any) => c.finish).length / props.row.childGoals.length * 100)
                          : 0" :indicator-placement="'inside'" :processing="true"
                          :color="getProgressColor(props.row)" />
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="目标名称" prop="title" />
              <el-table-column label="负责人" prop="owner" />
              <el-table-column label="截止日期" prop="deadlineString" />
              <el-table-column label="进度">
                <template #default="scope">
                  <el-progress :percentage="scope.row.status == 'expired' ? 100 : scope.row.progress" :stroke-width="10"
                    :color="getStatusColor(scope.row.status)"
                    :status="scope.row.status === 'completed' ? 'success' : scope.row.status === 'expired' ? 'exception' : ''" />
                </template>
              </el-table-column>
              <el-table-column label="状态">
                <template #default="scope">
                  <n-tag :type="getStatusTagType(scope.row.status)">
                    {{ getStatusLabel(scope.row.status) }}
                  </n-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center">
                <template #default="scope">
                  <n-button type="warning" circle secondary strong @click="viewGoalDetail(scope.row)"
                    style="margin-right: 10px;">
                    <n-icon size="20">
                      <EyeSharp />
                    </n-icon>

                  </n-button>
                  <n-button type="primary" circle secondary strong @click="editGoal(scope.row)">

                    <n-icon size="20">
                      <PencilOutline />
                    </n-icon>
                  </n-button>
                  <n-button type="primary" circle secondary strong @click="deleteGoal(scope.row)" v-if="isGoalOwner(scope.row)">

                    <n-icon size="20">
                      <ArchiveOutline />
                    </n-icon>
                  </n-button>
                </template>
              </el-table-column>
            </el-table>
          </n-card>
        </section>
      </div>
    </n-layout-content>

    <!-- 目标详情模态框 -->
    <GoalDetail v-model:show="showDetailModal" :goal="selectedGoal" @save="saveGoal" @updateGoal="refreshGoals" />

    <!-- 子目标文件上传模态框 -->
    <n-modal v-model:show="showChildGoalUploadModal" preset="card" style="max-width: 600px" title="上传文件" :mask-closable="false">
      <GeneralUpload ref="childGoalUploadRef" :file-list="currentChildGoalFiles" @uploadSuccess="handleChildGoalFileUploadSuccess" @fileRemove="handleChildGoalFileRemove" />
      <template #footer>
        <n-space justify="end">
          <n-button @click="closeChildGoalUploadModal">取消</n-button>
          <n-button type="primary" @click="saveChildGoalFiles">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 子目标文件查看模态框 -->
    <n-modal v-model:show="showChildGoalFilesModal" preset="card" style="max-width: 600px" title="查看文件" :mask-closable="true">
      <div v-if="viewChildGoalFilesList.length > 0">
        <n-list>
          <n-list-item v-for="(file, index) in viewChildGoalFilesList" :key="index">
            <n-thing>
              <template #description >
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

    <!-- 底部 -->
    <n-layout-footer class="footer" bordered>
      <p>© 2025 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
    </n-layout-footer>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue';
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
  NDropdown
} from 'naive-ui';
import { ElTable, ElTableColumn, ElButton, ElTag, ElProgress } from 'element-plus';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue';
import GoalDetail from '@/components/GoalDetail.vue';
import GeneralUpload from '@/components/GeneralUpload.vue';
import ExcelImport from '@/components/ExcelImport.vue';
import request, { postM, getMPaths, isSuccess, baseURL, isGoalOwner } from '@/utils/request';
import { EyeSharp, PencilOutline, CheckmarkOutline, ArchiveOutline, CloudUploadOutline, DocumentTextOutline, CloudDownloadOutline, TrashOutline, ChevronDownOutline } from '@vicons/ionicons5';
import type { DataTableColumns } from 'naive-ui';
import { AccessibilitySharp,CalendarSharp,CheckmarkCircle,CheckmarkCircleOutline } from '@vicons/ionicons5';
import { useStore } from 'vuex';

// 注入主题变量
const isDark = inject('isDark', ref(false));

// 路由和状态管理
const router = useRouter();
const store = useStore();
const message = useMessage();

// 数据状态
const goals = ref<any[]>([]);
const loading = ref(false);
const showDetailModal = ref(false);
const selectedGoal = ref<any>({});

// 子目标文件上传相关状态
const showChildGoalUploadModal = ref(false);
const showChildGoalFilesModal = ref(false);
const currentChildGoal = ref<any>(null);
const currentChildGoalIndex = ref(-1);
const currentChildGoalFiles = ref<any[]>([]);
const viewChildGoalFilesList = ref<any[]>([]);
const childGoalUploadRef = ref<any>(null);

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
      viewChildGoalFiles(childGoal);
      break;
  }
};

// 筛选和搜索
const searchQuery = ref('');
const statusFilter = ref<string | null>(null);
 const deleteGoal=(row)=>{

  postM('deleteGoal',{row}).then((res)=>{
    if(isSuccess(res)){
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
      return '#8a2be2'; // 紫色
    case 'expired':
      return '#ff6b6b'; // 红色
    default:
      return '#409eff'; // 蓝色
  }
};

// 获取进度条颜色
const getProgressColor = (goal: any) => {
  if (!goal.childGoals || goal.childGoals.length === 0) return '#8a2be2';

  const finishedCount = goal.childGoals.filter((c: any) => c.finish).length;
  const progress = Math.round(finishedCount / goal.childGoals.length * 100);

  if (progress === 100) return '#00c9a7';
  if (progress >= 50) return '#8a2be2';
  return '#409eff';
};

// 标记子目标为完成
const finishChildGoal = async (goal: any, index: number) => {
  try {
    const updatedGoal = { ...goal };
    updatedGoal.childGoals[index].finish = true;
    updatedGoal.childGoals[index].finishDate = new Date();
    // 更新进度
    const finishedCount = updatedGoal.childGoals.filter((c: any) => c.finish).length;
    updatedGoal.progress = Math.round(finishedCount / updatedGoal.childGoals.length * 100);

    // 如果所有子目标都完成了，更新目标状态
    if (finishedCount === updatedGoal.childGoals.length) {
      updatedGoal.status = 'completed';
    }

    const res = await postM('editGoal', updatedGoal);
    if (isSuccess(res)) {
      message.success('子目标已完成');
      // 更新本地数据
      // const goalIndex = goals.value.findIndex((g: any) => g.id === goal.id);
      // if (goalIndex !== -1) {
      //   goals.value[goalIndex] = updatedGoal;
      // }
      getGoals()
      
    } else {
      message.error('更新失败');
    }
  } catch (error) {
    message.error('操作失败');
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
      message.error('更新失败');
    }
  } catch (error) {
    message.error('操作失败');
    console.error(error);
  }
};

// 筛选后的目标列表
const filteredGoals = computed(() => {
  let result = [...goals.value];

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(goal =>
      goal.title.toLowerCase().includes(query) ||
      goal.description.toLowerCase().includes(query) ||
      goal.owner.toLowerCase().includes(query)
    );
  }

  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(goal => goal.status === statusFilter.value);
  }

  return result;
});

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

// 获取目标列表
const getGoals = async () => {
  loading.value = true;
  try {
    const res = await getMPaths("getGoals", store.state.user.username);
    if (isSuccess(res)) {
      goals.value = res.data.data || [];
      goals.value.forEach(goal => {
        goal.deadlineString = formatDate(goal.deadline);
      });
    } else {
      message.error('获取目标列表失败');
    }
  } catch (error) {
    message.error('获取目标列表时发生错误');
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
  });
  message.success('目标列表已刷新');
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
    //  getGoals()
      
      // 关闭模态框
      closeChildGoalUploadModal();
    } else {
      message.error('保存失败，请重试');
    }
  } catch (error) {
    message.error('保存失败，请重试');
    console.error(error);
  }
};

// 查看子目标文件
const viewChildGoalFiles = (childGoal: any): void => {
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
});
</script>

<style scoped>
/* 继承主页样式 */
.home-container {
  background-color: #0f0f13;
  color: #ffffff;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.home-container-light {
  background-color: #dadae3;
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
  opacity: 0.3;
}

.gradient-circle.blue {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #1e90ff, transparent 70%);
  top: -300px;
  right: -300px;
}

.gradient-circle.green {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #00c9a7, transparent 70%);
  bottom: -250px;
  left: -250px;
}

.gradient-circle.purple {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #8a2be2, transparent 70%);
  top: 50%;
  left: 30%;
}

.main-content-wrapper {
  height: calc(100vh - 160px);
  overflow-y: auto;
}

.main-content {
  padding: 20px 40px;
}

/* 子目标操作区域样式 */
.child-goal-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: flex-end;
}

/* 子目标详情区域样式 */
.child-goal-details {
  padding: 12px 0;
}

/* 子目标信息行样式 */
.child-goal-info-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 子目标头部样式 */
.child-goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

/* 子目标标题样式 */
.child-goal-title {
  flex: 1;
}

/* 子目标状态样式 */
.child-goal-status {
  margin-left: 12px;
}

/* 展开内容包装器样式 */
.expanded-content-wrapper {
  padding: 16px;
  background-color: var(--n-color-modal);
  border-radius: 8px;
}

/* 子目标折叠项样式 */
.child-goal-collapse-item {
  margin-bottom: 8px;
  border-radius: 6px;
  overflow: hidden;
}

/* 汇总折叠项样式 */
.summary-collapse-item {
  margin-top: 12px;
  border-radius: 6px;
  overflow: hidden;
}

/* 汇总头部样式 */
.summary-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 汇总标题样式 */
.summary-title {
  font-weight: 500;
}

/* 汇总内容样式 */
.summary-content {
  padding: 16px 0;
}

/* 汇总统计样式 */
.summary-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
}

/* 统计项样式 */
.stat-item {
  text-align: center;
}

/* 统计值样式 */
.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

/* 统计标签样式 */
.stat-label {
  font-size: 14px;
  color: var(--n-text-color-3);
}

.page-header {
  margin-bottom: 20px;
}

.hero-title,
.hero-title-light {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
}

.hero-title {
  color: #ffffff;
}

.hero-title-light {
  color: #000000;
}

.hero-subtitle,
.hero-subtitle-light {
  font-size: 16px;
  opacity: 0.8;
}

.hero-subtitle {
  color: #cccccc;
}

.hero-subtitle-light {
  color: #333333;
}

.control-section {
  margin-bottom: 20px;
}

.feature-card,
.feature-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.feature-card-light {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
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
  margin: 0;
}

.goals-section {
  margin-bottom: 20px;
}

.footer {
  text-align: center;
  padding: 20px;
  font-size: 14px;
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer p {
  margin: 0;
  color: #aaaaaa;
}

/* 滚动条样式 - Webkit内核浏览器 */
.main-content-wrapper::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(138, 43, 226, 0.5);
  border-radius: 4px;
}

.main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 43, 226, 0.7);
}

/* 浅色模式滚动条 */
.home-container-light .main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
}

.home-container-light .main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(138, 43, 226, 0.3);
}

.home-container-light .main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 43, 226, 0.5);
}

/* Element UI 表格主题适配 */
.el-table-dark {
  --el-table-border-color: rgba(255, 255, 255, 0.05);
  --el-table-bg-color: rgba(30, 30, 40, 0.6);
  --el-table-tr-bg-color: rgba(30, 30, 40, 0.6);
  --el-table-header-bg-color: rgba(40, 40, 50, 0.8);
  --el-table-text-color: #ffffff;
  --el-table-header-text-color: #ffffff;
  --el-table-row-hover-bg-color: rgba(50, 50, 60, 0.8);
  --el-table-current-row-bg-color: rgba(40, 40, 50, 0.8);
}

.el-table-dark.el-table {
  background-color: rgba(30, 30, 40, 0.6);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: #ffffff;
}

.el-table-dark .el-table__header-wrapper {
  background-color: rgba(40, 40, 50, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.el-table-dark .el-table__body-wrapper {
  background-color: rgba(30, 30, 40, 0.6);
}

.el-table-dark .el-table__cell {
  background-color: transparent;
  color: #ffffff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.el-table-dark .el-table__row.hover-row td {
  background-color: rgba(50, 50, 60, 0.8);
}

.el-table-dark .el-table__row.current-row td {
  background-color: rgba(40, 40, 50, 0.8);
}

.el-table-dark .el-table__inner-wrapper::before {
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-table-dark .el-table__expanded-cell) {
  background-color: rgba(30, 30, 40, 0.6) !important;
}

.el-table-dark .el-table__inner-wrapper {
  background-color: transparent;
}

.el-table-light {
  --el-table-border-color: rgba(0, 0, 0, 0.1);
  --el-table-bg-color: rgba(255, 255, 255, 0.8);
  --el-table-tr-bg-color: rgba(255, 255, 255, 0.8);
  --el-table-header-bg-color: rgba(245, 245, 245, 0.9);
  --el-table-text-color: #000000;
  --el-table-header-text-color: #000000;
  --el-table-row-hover-bg-color: rgba(235, 235, 235, 0.9);
  --el-table-current-row-bg-color: rgba(245, 245, 245, 0.9);
}

.el-table-light.el-table {
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.1);
  color: #000000;
}

.el-table-light .el-table__header-wrapper {
  background-color: rgba(245, 245, 245, 0.9);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.el-table-light .el-table__body-wrapper {
  background-color: rgba(255, 255, 255, 0.8);
}

.el-table-light .el-table__cell {
  background-color: transparent;
  color: #000000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.el-table-light .el-table__row.hover-row td {
  background-color: rgba(235, 235, 235, 0.9);
}

:deep(.el-table-light .el-table__expanded-cell) {
  background-color: rgba(235, 235, 235, 0.9) !important;
}

.el-table-light .el-table__row.current-row td {
  background-color: rgba(245, 245, 245, 0.9);
}

.el-table-light .el-table__inner-wrapper::before {
  background-color: rgba(0, 0, 0, 0.1);
}

.el-table-light .el-table__inner-wrapper {
  background-color: transparent;
}

.expanded-content {
  padding: 20px;
  background-color: rgba(0, 0, 0, 0.05);
}

.home-container-light .expanded-content {
  background-color: rgba(0, 0, 0, 0.05);
}

.expanded-content p {
  margin: 5px 0;
}

/* .tag-dark {
  color: white;
}

.tag-light {
  color: black;
} */

/* 子目标展开区域样式 */
.expanded-content-wrapper {
  padding: 10px 30px;
  background: rgb(30, 30, 40);
  border-radius: 12px;
  /* margin: 16px 0; */
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.home-container-light .expanded-content-wrapper {
  background: linear-gradient(135deg, rgba(138, 43, 226, 0.05), rgba(138, 43, 226, 0.05));
  background-color: rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.child-goal-item {
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
  /* background: linear-gradient(145deg, rgba(138, 43, 226, 0.1), rgba(75, 0, 130, 0.1)); */
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.child-goal-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  /* background: linear-gradient(145deg, rgba(138, 43, 226, 0.15), rgba(75, 0, 130, 0.15)); */
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.home-container-light .child-goal-item {
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.home-container-light .child-goal-item:hover {
  /* background: linear-gradient(145deg, rgba(138, 43, 226, 0.1), rgba(75, 0, 130, 0.1)); */
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.15);
}

.child-goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  flex-wrap: wrap;
  gap: 16px;
  padding: 20px 24px;
  /* background: rgba(138, 43, 226, 0.15); */
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.home-container-light .child-goal-header {
  background: white;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.child-goal-title {
  font-weight: 600;
  font-size: 17px;
  flex: 1;
  min-width: 200px;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
}

.home-container-light .child-goal-title {
  color: #333333;
  text-shadow: none;
}

.child-goal-status {
  flex-shrink: 0;
}

.child-goal-actions-inline {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.finish-date {
  font-size: 13px;
  opacity: 0.9;
  white-space: nowrap;
  color: #e0e0e0;
  font-weight: 500;
  background: rgba(0, 0, 0, 0.2);
  padding: 4px 10px;
  border-radius: 12px;
}

.home-container-light .finish-date {
  color: #666666;
  background: rgba(0, 0, 0, 0.05);
}

.child-goal-details {
  padding: 24px;
  background-color: rgba(0, 0, 0, 0.05);
}

.home-container-light .child-goal-details {
  background-color: rgba(0, 0, 0, 0.02);
}

.child-goal-info-row {
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #e0e0e0;
  padding: 12px 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
}

.home-container-light .info-item {
  color: #666666;
  background: rgba(0, 0, 0, 0.03);
}

.info-icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: #8a2be2;
  background: rgba(138, 43, 226, 0.2);
  border-radius: 50%;
  padding: 6px;
}

.no-description {
  color: #999;
  font-style: italic;
}

.child-goal-actions {
  display: flex;
  justify-content: flex-end;
}

.summary-section {
  margin-top: 24px;
  border-radius: 12px;
  overflow: hidden;
  /* background: linear-gradient(145deg, rgba(138, 43, 226, 0.1), rgba(75, 0, 130, 0.1)); */
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.home-container-light .summary-section {
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.home-container-light .summary-header {
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  color: #333333;
  text-shadow: none;
}

.summary-title {
  font-size: 17px;
}

.summary-content {
  padding: 24px 0 16px 0;
}

.summary-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
  justify-content: center;
}

.stat-item {
  text-align: center;
  min-width: 90px;
  padding: 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.05);
}

.home-container-light .stat-item {
  background: rgba(0, 0, 0, 0.03);
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.home-container-light .stat-value {
  color: #333333;
  text-shadow: none;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 8px;
  color: #e0e0e0;
  font-weight: 500;
}

.home-container-light .stat-label {
  color: #666666;
}
</style>
