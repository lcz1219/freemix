<template>
  <n-card style=" width: 50%; margin: 25vh auto;">
    <n-tabs class="card-tabs" default-value="signin" size="large" animated pane-wrapper-style="margin: 0 -4px"
      pane-style="padding-left: 4px; padding-right: 4px; box-sizing: border-box;">
      <n-tab-pane name="signin" tab="登录">
        <n-form :rules="rules" ref="formRef" :model="user">
          <n-form-item-row label="用户名" path="username">
            <n-input placeholder="请输入用户名" v-model:value="user.username" />
          </n-form-item-row>
          <n-form-item-row label="密码" path="password">
            <n-input placeholder="请输入密码" type="password" show-password-on="click" v-model:value="user.password" />
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
import { NCard, NTabs, NTabPane, NForm, NFormItemRow, NInput, NButton, type FormInst,useMessage } from 'naive-ui'
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
})
  const formRef = ref<FormInst | null>(null)
  const message=useMessage()
  const registerFormRef = ref<FormInst | null>(null)
  const user=ref({username:'',password:''});
  const registerUser=ref({username:'',password:'',confirmPassword:''});
  const handleLogin=async()=>{
   
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
        }
       
      } else {
        console.log('登录失败');
      }
    }

    const toRegister=()=>{
        router.replace('/register');
    }
</script>


<style scoped>
.card-tabs .n-tabs-nav--bar-type {
  padding-left: 4px;
}
</style>