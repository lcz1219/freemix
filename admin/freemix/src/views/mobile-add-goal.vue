<template>
  <van-config-provider :theme="isDark ? 'dark' : 'light'">
    <div :class="isDark ? 'mobile-add-goal dark' : 'mobile-add-goal light'" style="touch-action: manipulation; min-height: 100vh;">
      <!-- 装饰背景元素 -->
      <div class="background-elements">
        <div class="gradient-circle blue"></div>
        <div class="gradient-circle green"></div>
        <div class="gradient-circle purple"></div>
      </div>

      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="添加目标"
        left-text="返回"
        right-text="保存"
        @click-left="onClickLeft"
        @click-right="onClickRight"
      />

      <!-- 表单内容 -->
      <div class="main-content-wrapper">
        <div class="content">
          <van-cell-group :class="isDark ? 'form-card' : 'form-card-light'">
            <van-form ref="formRef" :model="goalForm">
              <van-field
                v-model="goalForm.title"
                label="目标标题"
                placeholder="请输入目标标题"
                maxlength="30"
                show-word-limit
                :rules="[{ required: true, message: '请输入目标标题' }]"
              />

              <van-field
                v-model="goalForm.description"
                label="目标描述"
                placeholder="请输入目标描述"
                type="textarea"
                autosize
                rows="3"
              />

              <van-field label="子目标" />
              <div class="child-goals-wrapper">
                <van-cell-group inset class="child-goals-group">
                  <van-field
                    v-for="(childGoal, index) in goalForm.childGoals"
                    :key="index"
                    v-model="childGoal.value"
                    placeholder="每一步小目标都是成功的开始🏅"
                    class="child-goal-item"
                  >
                    <template #right-icon>
                      <van-icon name="cross" @click="removeChildGoal(index)" />
                    </template>
                  </van-field>
                  <div class="add-child-goal">
                    <van-button 
                      v-if="goalForm.childGoals.length < 10" 
                      size="small" 
                      plain 
                      block 
                      icon="plus" 
                      @click="addChildGoal"
                    >
                      添加子目标
                    </van-button>
                  </div>
                </van-cell-group>
              </div>

              <van-field
                v-model="goalForm.owner"
                is-link
                readonly
                name="owner"
                label="负责人"
                placeholder="请选择负责人"
                :rules="[{ required: true, message: '请选择负责人' }]"
                @click="openOwener"
              />

              <van-field
                v-model="formattedDeadline"
                is-link
                readonly
                name="deadline"
                label="截止日期"
                placeholder="请选择截止日期"
                @click="showDatePicker = true"
              />

              <van-field
                v-model="goalForm.level"
                is-link
                readonly
                name="level"
                label="优先级"
                placeholder="请选择优先级"
                @click="showOwnerLevel = true"
              />

              <van-field
                v-model="goalForm.status"
                is-link
                readonly
                name="status"
                label="状态"
                placeholder="请选择状态"
                @click="showOwnerStatus = true"
              />

              <van-field label="标签" />
              <div class="tags-wrapper">
                <van-tag 
                  v-for="(tag, index) in goalForm.tags" 
                  :key="index" 
                  class="tag-item"
                  closeable
                  @close="removeTag(index)"
                >
                  {{ tag }}
                </van-tag>
                <div v-if="goalForm.tags.length < 5" class="tag-input-wrapper">
                  <van-field 
                    v-model="newTag" 
                    placeholder="请输入标签" 
                    class="tag-input"
                  >
                    <template #button>
                      <van-button size="small" type="primary" @click="addTag">添加</van-button>
                    </template>
                  </van-field>
                </div>
              </div>
            </van-form>
          </van-cell-group>
        </div>
      </div>
    </div>

    <!-- 负责人选择器 -->
    <van-popup v-model:show="showOwnerPicker" round position="bottom">
      <van-picker
        :columns="owerOptions"
        @confirm="onConfirmOwner"
        @cancel="showOwnerPicker = false"
      />
    </van-popup>
    
    <van-popup v-model:show="showOwnerLevel" round position="bottom">
      <van-picker
        :columns="levelOptions"
        @confirm="onConfirmLevel"
        @cancel="showOwnerLevel = false"
      />
    </van-popup>
    
    <van-popup v-model:show="showOwnerStatus" round position="bottom">
      <van-picker
        :columns="statusOptions"
        @confirm="onConfirmStatus"
        @cancel="showOwnerStatus = false"
      />
    </van-popup>

    <!-- 日期选择器 -->
    <van-popup v-model:show="showDatePicker" round position="bottom">
      <van-date-picker
        :model-value="currentDateArray"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onConfirmDate"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </van-config-provider>
</template>

<script setup>
import { ref, reactive, inject, computed, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { 
  Form as VanForm,
  Field as VanField,
  CellGroup as VanCellGroup,
  Button as VanButton,
  NavBar as VanNavBar,
  Popup as VanPopup,
  Picker as VanPicker,
  DatePicker as VanDatePicker,
  Tag as VanTag,
  Icon as VanIcon,
  ConfigProvider as VanConfigProvider,
  showToast
} from 'vant';
import { postM, getM, isSuccess } from '@/utils/request';

const router = useRouter();
const store = useStore();
const isDark = inject('isDark', ref(false));
const formRef = ref(null);

// 弹窗控制
const showOwnerPicker = ref(false);
const showDatePicker = ref(false);
const showOwnerLevel = ref(false);
const showOwnerStatus = ref(false);

// 新增标签输入
const newTag = ref('');

// 日期范围
const minDate = new Date(2020, 0, 1);
const maxDate = new Date(2099, 5, 1);
const currentDate = computed(() => {
  // 如果已有选择的日期，则使用该日期，否则使用当前日期
  if (goalForm.deadline) {
    return new Date(goalForm.deadline);
  }
  return new Date();
});

const openOwener = async () => {
  await getOwerList()
  showOwnerPicker.value = true;
}

const currentDateArray = computed(() => {
  // 为 van-date-picker 提供正确的数组格式值 [year, month, day]
  const date = currentDate.value;
  return [date.getFullYear(), date.getMonth() + 1, date.getDate()];
});

// 格式化显示的日期
const formattedDeadline = computed(() => {
  if (goalForm.deadline) {
    const date = new Date(goalForm.deadline);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
  return '';
});

// 表单数据
const goalForm = reactive({
  title: '',
  description: '',
  childGoals: [{ message: '', finish: false, finishTime: '' }], // 初始化时就有一个空的子目标
  owner: '',
  deadline: null, // 初始化为null而不是空数组
  level: '',
  status: '',
  progress: 0,
  tags: []
});

// 添加子目标
const addChildGoal = () => {
  if (goalForm.childGoals.length < 10) {
    goalForm.childGoals.push({ message: '', finish: false, finishTime: '' });
  }
};

// 删除子目标
const removeChildGoal = (index) => {
  if (goalForm.childGoals.length > 1) {
    goalForm.childGoals.splice(index, 1);
  }
};

// 添加标签
const addTag = () => {
  if (newTag.value.trim() && goalForm.tags.length < 5) {
    goalForm.tags.push(newTag.value.trim());
    newTag.value = '';
  }
};

// 删除标签
const removeTag = (index) => {
  goalForm.tags.splice(index, 1);
};

// 选择器选项
const levelOptions = [
  { text: '低', value: 'low' },
  { text: '中', value: 'medium' },
  { text: '高', value: 'high' },
  { text: '紧急', value: 'urgent' }
];

const owerOptions = ref([]);
const getOwerList = async () => {
  const res = await getM('getOwerList');
  if (isSuccess(res)) {
    owerOptions.value = res.data.data;
  }
}

const statusOptions = [
  { text: '未开始', value: 'not-started' },
  { text: '进行中', value: 'in-progress' },
  { text: '已完成', value: 'completed' },
  { text: '已暂停', value: 'paused' }
];

// 日期确认处理函数
const onConfirmDate = ({ selectedValues }) => {
  const [year, month, day] = selectedValues;
  goalForm.deadline = new Date(year, month - 1, day).getTime();
  showDatePicker.value = false;
};

const onConfirmLevel = ({ selectedOptions }) => {
  goalForm.level = selectedOptions[0]?.text || '';
  showOwnerLevel.value = false;
};

const onConfirmStatus = ({ selectedOptions }) => {
  goalForm.status = selectedOptions[0]?.text || '';
  showOwnerStatus.value = false;
};

// 负责人确认处理函数
const onConfirmOwner = ({ selectedOptions }) => {
  goalForm.owner = selectedOptions[0]?.text || '';
  showOwnerPicker.value = false;
};

// 方法
const onClickLeft = () => {
  router.go(-1);
};

const onClickRight = async () => {
  // 验证表单
  try {
    // 手动验证必填字段
    if (!goalForm.title) {
      showToast('请输入目标标题');
      return;
    }
    
    if (!goalForm.owner) {
      showToast('请选择负责人');
      return;
    }
    
    // 准备提交的数据
    const submitData = {
      ...goalForm,
      username: store.state.user.username, // 添加当前用户名
      childGoals: goalForm.childGoals.filter(item => item.value.trim() !== '').map(item => ({
        message: item.value,
        finish: false
      }))
    };
    
    // 调用API保存目标
    const res = await postM('editGoal', submitData);
    if (isSuccess(res)) {
      showToast('目标保存成功');
      router.go(-1);
    } else {
      showToast(res.data.msg || '保存失败');
    }
  } catch (error) {
    showToast('保存失败');
    console.error(error);
  }
};
</script>

<style scoped>
.mobile-add-goal {
  position: relative;
  overflow-x: hidden;
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
}

.mobile-add-goal.dark {
  background-color: #0f0f13;
  color: #ffffff;
}

.mobile-add-goal.light {
  background-color: #dadae3;
  color: #000000;
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
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #1e90ff, transparent 70%);
  top: -150px;
  right: -150px;
}

.gradient-circle.green {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, #00c9a7, transparent 70%);
  bottom: -125px;
  left: -125px;
}

.gradient-circle.purple {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, #81c683, transparent 70%);
  top: 30%;
  left: 20%;
}

.main-content-wrapper {
  height: calc(100vh - 56px); /* 减去顶部导航栏的高度 */
  overflow-y: auto;
  padding-bottom: 20px;
}

.content {
  padding: 16px;
}

.form-card,
.form-card-light {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.form-card {
  background-color: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.form-card-light {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.child-goals-wrapper {
  padding: 0 16px 16px;
}

.child-goals-group {
  border-radius: 8px;
  overflow: hidden;
}

.child-goal-item {
  padding: 8px 0;
}

.add-child-goal {
  padding: 8px 0;
}

.tags-wrapper {
  padding: 0 16px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin-bottom: 4px;
}

.tag-input-wrapper {
  margin-top: 8px;
}

/* 滚动条样式 - Webkit内核浏览器 */
.main-content-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(129, 198, 131, 0.5);
  border-radius: 3px;
}

.main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(129, 198, 131, 0.7);
}

/* 浅色模式滚动条 */
.mobile-add-goal.light .main-content-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
}

.mobile-add-goal.light .main-content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(129, 198, 131, 0.3);
}

.mobile-add-goal.light .main-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(129, 198, 131, 0.5);
}
</style>