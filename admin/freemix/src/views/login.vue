<template>
  <n-card style=" width: 50%; margin: 25vh auto;">
    <n-tabs class="card-tabs" default-value="signin" size="large" animated pane-wrapper-style="margin: 0 -4px"
      pane-style="padding-left: 4px; padding-right: 4px; box-sizing: border-box;">
      <n-tab-pane name="signin" tab="登录">
        <n-form :rules="rules" ref="formRef" :model="user">
          <n-form-item-row label="用户名" path="username">
            <n-input placeholder="请输入用户名" v-model:value="user.username" @blur="loadCaptcha" />
          </n-form-item-row>
          <n-form-item-row label="密码" path="password">
            <n-input placeholder="请输入密码" type="password" show-password-on="click" v-model:value="user.password" />
          </n-form-item-row>
          <n-form-item-row label="验证码" path="captcha">
            <div class="captcha-container">
              <n-input 
                placeholder="请输入验证码" 
                v-model:value="user.captcha" 
                maxlength="4"
                style="flex: 1;"
              />
              <img 
                v-if="captchaImage" 
                :src="captchaImage" 
                alt="验证码" 
                class="captcha-image"
                @click="loadCaptcha"
              />
              <div v-else class="captcha-placeholder" @click="loadCaptcha">
                <n-icon :component="RefreshOutline" />
              </div>
            </div>
          </n-form-item-row>
        </n-form>
        <n-button quaternary type="primary" style="margin: 10px;" @click="toRegister()">
      注册
    </n-button>
    
        <n-button type="primary" block secondary strong @click="handleLogin">
          登录
        </n-button>
      </n-tab-pane>
     
    </n-tabs>
  </n-card>
</template>

<script setup lang="ts">
import { NCard, NTabs, NTabPane, NForm, NFormItemRow, NInput, NButton, type FormInst,useMessage, NIcon } from 'naive-ui'
import { ref } from 'vue';
import{useRoute,useRouter} from 'vue-router'
import request from '@/utils/request'
import { isSuccess,postM } from '@/utils/request.js'

// @ts-ignore
import { useStore } from 'vuex';
const store = useStore();
const route=useRoute();
const router=useRouter()
const rules = ref({
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
  captcha: { required: true, message: '请输入验证码', trigger: 'blur' },
})
  const formRef = ref<FormInst | null>(null)
  const message = useMessage()
  const registerFormRef = ref<FormInst | null>(null)
  const user = ref({username:'',password:'', captcha:''});
  const registerUser = ref({username:'',password:'',confirmPassword:''});
  const captchaImage = ref('');
  
  const handleLogin = async () => {
      const valid = await formRef.value?.validate();
      console.log("handleLogin",valid);
      if (valid) {
        const res=await postM('login',user.value);
        console.log("login,res",res);
        if(isSuccess(res)) {
          
          store.commit('saveUser',res.data.data);
          await localStorage.setItem('token',res.data.data.token);
          message.success('登录成功')
          console.log('登录成功');
          router.push('/home');
        }else{
          message.error(res.data.msg)
          // 登录失败时刷新验证码
          await loadCaptcha();
        }
      } else {
        console.log('登录失败');
      }
    }

    const toRegister = () => {
        router.replace('/register');
    }
    
    // 加载验证码
    const loadCaptcha = async () => {
      if (!user.value.username) {
        return;
      }
      let username=user.value.username
      try {
        const res = await postM('/captcha',{"username":username});
        if (isSuccess(res)) {
          captchaImage.value = res.data.data.image;
        }
      } catch (error) {
        console.log(error);
        
        message.error('验证码加载失败');
      }
    }
</script>


<style scoped>
.card-tabs .n-tabs-nav--bar-type {
  padding-left: 4px;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
}

.captcha-placeholder {
  width: 120px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #d9d9d9;
}

.captcha-placeholder:hover {
  background-color: #e6e6e6;
}
</style>