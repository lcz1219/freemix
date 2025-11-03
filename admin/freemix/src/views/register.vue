<template>
  
    <n-card class="register-card" :style="cardStyle">
      <step :current="currentStep"></step>
      <n-tabs type="line" animated>
        <n-tab-pane name="register" tab="注册">
          <n-form ref="formRef" :model="formData" :rules="rules" v-if="currentStep==1">
            <n-form-item label="用户名" path="username">
              <n-input 
                v-model:value="formData.username" 
                placeholder="请输入用户名"
              />
            </n-form-item>
            <n-form-item label="中文名" path="chinesename">
              <n-input 
                v-model:value="formData.chinesename" 
                placeholder="请输入中文名"
              />
            </n-form-item>
  
            <n-form-item label="邮箱" path="email">
              <n-input 
                type="email"
                v-model:value="formData.email"
                placeholder="请输入邮箱"
              />
            </n-form-item>
  
            <n-form-item label="密码" path="password">
              <n-input
                type="password"
                v-model:value="formData.password"
                placeholder="至少6位字符"
              />
            </n-form-item>
  
            <n-form-item label="确认密码" path="confirmPassword">
              <n-input
                type="password"
                v-model:value="formData.confirmPassword"
                placeholder="再次输入密码"
              />
            </n-form-item>
  
            
  
            <n-button 
              type="primary" 
              block 
              @click="handleRegister"
              :loading="loading"
            >
              下一步
            </n-button>
          </n-form>
          <n-form  :model="formData"  v-if="currentStep==2">
            <n-form-item label="安全问题一" >
              <n-select v-model:value="formData.saveQuestionOne" :options="options" placeholder="请选择安全问题"/>
              
            </n-form-item>
            <n-form-item>
              <n-input 
                v-model:value="formData.saveAnOne"
                :placeholder="'请输入答案'"
              />
            </n-form-item>
  
           
  
            
  
            <n-button 
              type="primary" 
              block 
              @click="handleRegister"
              :loading="loading"
            >
              下一步
            </n-button>
          </n-form>
          <n-form  :model="formData"  v-if="currentStep==3">
            <n-form-item label="安全问题二" >
              <n-select v-model:value="formData.saveQuestionTwice" :options="options" :placeholder="'请选择安全问题'"/>
              
            </n-form-item>
            <n-form-item>
              <n-input 
                v-model:value="formData.saveAnTwice"
                :placeholder="''"
              />
            </n-form-item>
  
           
  
            
  
            <n-button 
              type="primary" 
              block 
              @click="handleRegister"
              :loading="loading"
            >
              下一步
            </n-button>
          </n-form>
          <n-form  :model="formData"  v-if="currentStep==4">
            <n-form-item label="安全问题三" >
              <n-select v-model:value="formData.saveQuestionThree" :options="options" :placeholder="'请选择安全问题'"/>
              
            </n-form-item>
            <n-form-item>
              <n-input 
                v-model:value="formData.saveAnThree"
                :placeholder="''"
              />
            </n-form-item>
  
           
  
            
  
            <n-button 
              type="primary" 
              block 
              @click="handleRegister"
              :loading="loading"
            >
              完成
            </n-button>
          </n-form>
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </template>
  
  <script setup>
  import { ref, computed } from 'vue'
  import { useRouter } from 'vue-router'
  import step from '@/views/step.vue'
  import { 
    NCard, 
    NTabs, 
    NTabPane, 
    NForm, 
    NFormItem, 
    NInput,
    NSelect,
    NButton,
    useMessage 
  } from 'naive-ui'
  import request from '@/utils/request'
  import { postM, isSuccess } from '@/utils/request'
  import { isDesktop } from '@/utils/device'

  const router = useRouter()
  const message = useMessage()
  const formRef = ref(null)
  const loading = ref(false)
  const currentStep = ref(1)
 const options=ref([
  {label:"出生的城市",value:'birthcity'},
  {label:"大学的城市",value:'county'},
  {label:"您的年龄",value:'age'},
  
])
  
  const formData = ref({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    saveQuestionOne:'',
    saveAnOne:'',
    saveQuestionTwice:'',
    saveAnTwice:'',
    saveQuestionThree:'',
    saveAnThree:''
  })
  
  const validatePassword = (_, value) => {
    if (value !== formData.value.password) {
      return new Error('两次输入的密码不一致')
    }
    return true
  }
  
  const rules = {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, message: '长度至少为3个字符', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '长度至少为6个字符', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请确认密码', trigger: 'blur' },
      { validator: validatePassword, trigger: ['input', 'blur'] }
    ]
  }
  
  // 计算属性：根据是否为桌面端返回不同的卡片样式
  const cardStyle = computed(() => {
    if (isDesktop()) {
      return {
        width: '100%',
        height: '100vh',
        margin: '0',
        borderRadius: '0',
        boxShadow: 'none'
      }
    } else {
      return {
        maxWidth: '1400px',
        margin: '50px auto',
        boxShadow: '0 3px 10px rgba(0, 0, 0, 0.1)'
      }
    }
  })
  
  const handleRegister = async () => {
    try {
      await formRef.value?.validate()
     
      
      // 模拟API请求（实际项目替换为axios调用）
      if(currentStep.value==4){
         loading.value = true
        const res=await postM('register',formData.value);
          console.log("handleRegister",res);
          
        if(res.data.code==200){
           message.success('注册成功')
           router.replace('/login')
        }else{
          message.error(res.data.msg || '注册失败')
           router.replace('/register')
           currentStep.value=1
           return
        }
      }
        currentStep.value++;
        console.log("fromData",formData.value);
        
       
        
        // router.push('/login') // 跳转到登录页
      
    } catch (e) {
      // message.error('表单验证失败，请检查输入')
    } finally {
      loading.value = false
    }
  }
  </script>
  
  <style scoped>
  .register-card {
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  
  .register-card :deep(.n-card__content) {
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  
  /* 桌面端全屏显示样式 */
  .register-card.desktop-fullscreen {
    border-radius: 0;
    box-shadow: none;
  }
  
  .form-footer {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 24px;
  }
  
  /* 响应式设计：在小屏幕上添加一些内边距 */
  @media (max-width: 768px) {
    .register-card :deep(.n-card__content) {
      padding: 10px;
    }
  }
  </style>