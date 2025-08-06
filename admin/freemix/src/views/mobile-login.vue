<template>
  <div class="mobile-login">
    <div class="login-header">
      <h1>目标追踪者</h1>
      <p>掌控你的目标，衡量你的成功</p>
    </div>
    
    <div class="login-form">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="user.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
          />
          <van-field
            v-model="user.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>
        
        <div class="login-buttons">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
          <van-button round block class="register-btn" @click="toRegister">
            注册账号
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { Toast } from 'vant';
import { postM, isSuccess } from '@/utils/request.js';
import 'vant/es/toast/style';

const router = useRouter();
const store = useStore();

const user = ref({ username: '', password: '' });
const loading = ref(false);

const onSubmit = async (values) => {
  loading.value = true;
  try {
    const res = await postM('login', user.value);
    if (isSuccess(res)) {
      store.commit('saveUser', res.data.data);
      localStorage.setItem('token', res.data.data.token);
      Toast.success('登录成功');
      router.push('/home');
    } else {
      Toast.fail(res.data.msg);
    }
  } catch (error) {
    Toast.fail('登录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const toRegister = () => {
  router.replace('/register');
};
</script>

<style scoped>
.mobile-login {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
}

.login-header {
  text-align: center;
  padding: 60px 0 40px;
  color: white;
}

.login-header h1 {
  font-size: 28px;
  margin-bottom: 10px;
  font-weight: bold;
}

.login-header p {
  font-size: 16px;
  opacity: 0.9;
}

.login-form {
  flex: 1;
}

:deep(.van-cell-group) {
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
}

:deep(.van-field__label) {
  font-weight: 500;
}

.login-buttons {
  margin-top: 30px;
}

:deep(.van-button) {
  height: 48px;
  font-size: 16px;
  margin-bottom: 15px;
}

.register-btn {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid #8a2be2 !important;
  color: #8a2be2 !important;
}
</style>