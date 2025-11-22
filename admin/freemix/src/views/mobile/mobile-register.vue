<template>
  <van-config-provider :theme="currentTheme">
    <div class="mobile-register" :class="currentTheme">
      <!-- 背景装饰元素 -->
      <div class="background-elements">
        <div class="gradient-circle blue"></div>
        <div class="gradient-circle green"></div>
        <div class="gradient-circle purple"></div>
      </div>

      <div class="register-container">
        <van-nav-bar
          title="注册账号"
          left-text="返回"
          left-arrow
          @click-left="goBack"
        />

        <div class="register-header">
          <h1 class="app-title">创建账户</h1>
          <p class="app-subtitle">开启您的目标管理之旅</p>
        </div>

        <van-card class="register-card" :class="currentTheme">
          <van-steps :active="currentStep" active-color="#4CAF50" style="margin-bottom: 20px;">
            <van-step>基本信息</van-step>
            <van-step>安全问题</van-step>
            <van-step>完成</van-step>
          </van-steps>

          <van-form ref="formRef" :model="formData" :rules="rules">
            <!-- 第一步：基本信息 -->
            <div v-show="currentStep === 1">
              <van-cell-group inset>
                <!-- 蜜罐字段，防止自动填充 -->
                <input type="text" style="display:none" tabindex="-1" autocomplete="username" />
                <input type="password" style="display:none" tabindex="-1" autocomplete="current-password" />

                <van-field
                  v-model="formData.username"
                  name="username"
                  label="用户名"
                  placeholder="请输入用户名"
                  :rules="[{ required: true, message: '请输入用户名' }]"
                >
                  <template #left-icon>
                    <van-icon name="manager-o" />
                  </template>
                </van-field>

                <van-field
                  v-model="formData.chinesename"
                  name="chinesename"
                  label="中文名"
                  placeholder="请输入中文名"
                  :rules="[{ required: true, message: '请输入中文名' }]"
                >
                  <template #left-icon>
                    <van-icon name="user-o" />
                  </template>
                </van-field>

                <van-field
                  v-model="formData.email"
                  type="email"
                  name="email"
                  label="邮箱"
                  placeholder="请输入邮箱"
                  :rules="[
                    { required: true, message: '请输入邮箱' },
                    { pattern: /^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/, message: '邮箱格式不正确' }
                  ]"
                >
                  <template #left-icon>
                    <van-icon name="envelop-o" />
                  </template>
                </van-field>

                <van-field
                  v-model="formData.password"
                  type="password"
                  name="password"
                  label="密码"
                  placeholder="请输入密码（至少6位）"
                  :rules="[
                    { required: true, message: '请输入密码' },
                    { min: 6, message: '长度至少为6个字符' }
                  ]"
                >
                  <template #left-icon>
                    <van-icon name="lock" />
                  </template>
                </van-field>

                <van-field
                  v-model="formData.confirmPassword"
                  type="password"
                  name="confirmPassword"
                  label="确认密码"
                  placeholder="请再次输入密码"
                  :rules="[
                    { required: true, message: '请确认密码' },
                    { 
                      validator: validatePassword, 
                      message: '两次输入的密码不一致' 
                    }
                  ]"
                >
                  <template #left-icon>
                    <van-icon name="lock" />
                  </template>
                </van-field>
              </van-cell-group>
            </div>

            <!-- 第二步：安全问题 -->
            <div v-show="currentStep === 2">
              <van-cell-group inset>
                <van-field
                  v-model="formData.saveQuestionOne"
                  label="问题一"
                  placeholder="请选择安全问题"
                  is-link
                  readonly
                  @click="showQuestionPicker1 = true"
                >
                  <template #left-icon>
                    <van-icon name="question-o" />
                  </template>
                </van-field>

                <van-field
                  v-model="formData.saveAnOne"
                  label="答案一"
                  placeholder="请输入答案"
                  :rules="[{ required: true, message: '请输入答案' }]"
                >
                  <template #left-icon>
                    <van-icon name="edit" />
                  </template>
                </van-field>

                <van-field
                  v-model="formData.saveQuestionTwice"
                  label="问题二"
                  placeholder="请选择安全问题"
                  is-link
                  readonly
                  @click="showQuestionPicker2 = true"
                >
                  <template #left-icon>
                    <van-icon name="question-o" />
                  </template>
                </van-field>

                <van-field
                  v-model="formData.saveAnTwice"
                  label="答案二"
                  placeholder="请输入答案"
                  :rules="[{ required: true, message: '请输入答案' }]"
                >
                  <template #left-icon>
                    <van-icon name="edit" />
                  </template>
                </van-field>
              </van-cell-group>
            </div>

            <!-- 第三步：完成 -->
            <div v-show="currentStep === 3" class="registration-complete">
              <van-icon name="checked" size="60" color="#4CAF50" style="margin-bottom: 20px;" />
              <h3>注册成功</h3>
              <p>您的账户已创建完成，现在可以登录系统开始使用了。</p>
            </div>

            <div class="register-buttons">
              <van-button 
                v-if="currentStep < 3"
                round 
                block 
                type="primary" 
                native-type="submit"
                @click="handleRegister"
                :loading="loading"
                loading-text="提交中..."
                class="register-button"
              >
                {{ currentStep < 2 ? '下一步' : '完成注册' }}
              </van-button>

              <van-button 
                v-if="currentStep === 3"
                round 
                block 
                type="primary" 
                @click="toLogin"
                class="login-button"
              >
                立即登录
              </van-button>

              <van-button 
                v-if="currentStep > 1 && currentStep < 3"
                round 
                block 
                plain 
                @click="prevStep"
                class="prev-button"
              >
                上一步
              </van-button>
            </div>
          </van-form>
        </van-card>
      </div>

      <!-- 安全问题选择器1 -->
      <van-popup v-model:show="showQuestionPicker1" round position="bottom">
        <van-picker
          title="选择安全问题"
          :columns="securityQuestions"
          @confirm="onQuestion1Confirm"
          @cancel="showQuestionPicker1 = false"
        />
      </van-popup>

      <!-- 安全问题选择器2 -->
      <van-popup v-model:show="showQuestionPicker2" round position="bottom">
        <van-picker
          title="选择安全问题"
          :columns="securityQuestions"
          @confirm="onQuestion2Confirm"
          @cancel="showQuestionPicker2 = false"
        />
      </van-popup>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { 
  showToast,
  showLoadingToast,
  closeToast,
  showSuccessToast
} from 'vant'
import { postM, isSuccess } from '@/utils/request.js'

// 路由和状态管理
const router = useRouter()
const store = useStore()

// 主题配置
const isDark = ref(false)
const currentTheme = computed(() => isDark.value ? 'dark' : 'light')

// 表单引用
const formRef = ref()

// 步骤控制
const currentStep = ref(1)

// 表单数据
const formData = ref({
  username: '',
  chinesename: '',
  email: '',
  password: '',
  confirmPassword: '',
  saveQuestionOne: '',
  saveAnOne: '',
  saveQuestionTwice: '',
  saveAnTwice: ''
})

// 加载状态
const loading = ref(false)

// 安全问题选择器显示控制
const showQuestionPicker1 = ref(false)
const showQuestionPicker2 = ref(false)

// 安全问题选项
const securityQuestions = [
  '您的出生地是哪里？',
  '您母亲的姓名是？',
  '您父亲的姓名是？',
  '您最喜欢的颜色是？',
  '您最喜欢的食物是？',
  '您就读的第一所学校名称是？',
  '您最喜欢的一本书是？',
  '您最难忘的经历是？'
]

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名' }
  ],
  chinesename: [
    { required: true, message: '请输入中文名' }
  ],
  email: [
    { required: true, message: '请输入邮箱' },
    { pattern: /^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/, message: '邮箱格式不正确' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, message: '长度至少为6个字符' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码' },
    { validator: validatePassword, message: '两次输入的密码不一致' }
  ]
}

// 密码确认验证器
const validatePassword = (rule, value, callback) => {
  if (value !== formData.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 处理注册
const handleRegister = async () => {
  try {
    await formRef.value?.validate()
    
    if (currentStep.value === 1) {
      // 第一步验证通过，进入第二步
      currentStep.value++
    } else if (currentStep.value === 2) {
      // 第二步验证通过，提交注册
      loading.value = true
      
      const res = await postM('register', formData.value)
      if (isSuccess(res) && res.data.code === 200) {
        showSuccessToast('注册成功')
        currentStep.value++
      } else {
        showToast(res.data.msg || '注册失败')
      }
    }
  } catch (e) {
    console.error('注册失败:', e)
    showToast('注册失败，请检查输入信息')
  } finally {
    loading.value = false
  }
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

// 跳转到登录页面
const toLogin = () => {
  router.replace('/mobile/login')
}

// 返回上一页
const goBack = () => {
  if (currentStep.value > 1) {
    prevStep()
  } else {
    router.back()
  }
}

// 安全问题选择确认
const onQuestion1Confirm = ({ selectedOptions }) => {
  formData.value.saveQuestionOne = selectedOptions[0]
  showQuestionPicker1.value = false
}

const onQuestion2Confirm = ({ selectedOptions }) => {
  formData.value.saveQuestionTwice = selectedOptions[0]
  showQuestionPicker2.value = false
}

// 页面加载时初始化
onMounted(() => {
  // 可以在这里添加初始化逻辑
})
</script>

<style scoped>
.mobile-register {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.mobile-register.light {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 背景装饰元素 */
.background-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.gradient-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.3;
}

.gradient-circle.blue {
  width: 300px;
  height: 300px;
  background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%);
  top: -100px;
  right: -100px;
}

.gradient-circle.green {
  width: 200px;
  height: 200px;
  background: linear-gradient(45deg, #43e97b 0%, #38f9d7 100%);
  bottom: -80px;
  left: 10%;
}

.gradient-circle.purple {
  width: 250px;
  height: 250px;
  background: linear-gradient(45deg, #fa709a 0%, #fee140 100%);
  top: 30%;
  left: -80px;
}

.register-container {
  position: relative;
  z-index: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.register-header {
  text-align: center;
  margin: 20px 0 30px 0;
}

.app-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 10px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.light .app-title {
  color: #333;
}

.app-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.light .app-subtitle {
  color: #666;
}

.register-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.85) !important;
  margin-bottom: 20px;
}

.register-card.dark {
  background: rgba(30, 30, 30, 0.85) !important;
}

:deep(.van-field__label) {
  font-weight: 500;
  width: 70px;
}

.register-buttons {
  padding: 20px 0;
}

.register-button,
.login-button,
.prev-button {
  margin-bottom: 15px;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.registration-complete {
  text-align: center;
  padding: 30px 20px;
}

.registration-complete h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #333;
}

.registration-complete p {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.5;
}

.light .registration-complete h3 {
  color: #333;
}

.light .registration-complete p {
  color: #666;
}

.dark .registration-complete h3 {
  color: #fff;
}

.dark .registration-complete p {
  color: #ccc;
}

/* 深色主题样式 */
.dark .register-card {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}
</style>