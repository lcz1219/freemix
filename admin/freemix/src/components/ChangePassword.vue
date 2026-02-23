<template>
  <div class="change-password">
    <!-- Mobile View -->
    <div v-if="isMobileDevice">
      <van-cell-group inset>
        <van-field
          v-model="form.oldPassword"
          type="password"
          label="旧密码"
          placeholder="请输入旧密码"
          required
        />
        <van-field
          v-model="form.newPassword"
          type="password"
          label="新密码"
          placeholder="请输入新密码"
          required
        />
        <van-field
          v-model="form.confirmPassword"
          type="password"
          label="确认密码"
          placeholder="请再次输入新密码"
          required
        />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" @click="submitChange">
          确认修改
        </van-button>
        <van-button round block plain type="default" @click="cancelEditing" v-if="!props.alwaysOpen" style="margin-top: 10px;">
          取消
        </van-button>
      </div>
    </div>

    <!-- Desktop View -->
    <div v-else>
      <n-button quaternary type="info" v-if="!isEditing && !props.alwaysOpen" @click="startEditing">
        <n-icon>
          <LockClosedSharp />
        </n-icon>
        修改密码
      </n-button>
      
      <div class="form-group-container" v-else>
        <label class="section-title">修改密码</label>
        
        <div class="form-group">
          <label>旧密码</label>
          <input 
            v-model="form.oldPassword" 
            type="password" 
            placeholder="请输入旧密码" 
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label>新密码</label>
          <input 
            v-model="form.newPassword" 
            type="password" 
            placeholder="请输入新密码" 
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label>确认新密码</label>
          <input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码" 
            class="form-input"
          />
        </div>
        
        <div class="button-group">
          <button @click="submitChange" class="btn primary">确认修改</button>
          <button @click="cancelEditing" class="btn secondary" v-if="!props.alwaysOpen">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { LockClosedSharp } from '@vicons/ionicons5';
import { useMessage, NButton, NIcon } from 'naive-ui';
import { postM, isSuccess } from '@/utils/request.js';
import { isMobile } from '@/utils/device.js';
import { showSuccessToast, showFailToast, showToast } from 'vant';
import router from '@/router';

const message = useMessage();
const isMobileDevice = computed(() => isMobile());

const props = defineProps({
  alwaysOpen: {
    type: Boolean,
    default: false
  }
});

const isEditing = ref(props.alwaysOpen);

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const startEditing = () => {
  isEditing.value = true;
  resetForm();
};

const cancelEditing = () => {
  isEditing.value = false;
  resetForm();
};

const resetForm = () => {
  form.oldPassword = '';
  form.newPassword = '';
  form.confirmPassword = '';
};

const showMessage = (type: 'success' | 'error' | 'warning', content: string) => {
  if (isMobileDevice.value) {
    if (type === 'success') {
      showSuccessToast(content);
    } else if (type === 'error') {
      showFailToast(content);
    } else {
      showToast(content);
    }
  } else {
    if (type === 'success') {
      message.success(content);
    } else if (type === 'error') {
      message.error(content);
    } else {
      message.warning(content);
    }
  }
};

const submitChange = async () => {
  if (!form.oldPassword) {
    showMessage('warning', '请输入旧密码');
    return;
  }
  if (!form.newPassword) {
    showMessage('warning', '请输入新密码');
    return;
  }
  if (form.newPassword !== form.confirmPassword) {
    showMessage('warning', '两次输入的密码不一致');
    return;
  }
  
  try {
    const res = await postM('updatePassword', {
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    });
    
    if (isSuccess(res)) {
      showMessage('success', '密码修改成功');
      isEditing.value = false;
      resetForm();
    } else {
      showMessage('error', res.data.msg || '密码修改失败');
    }
  } catch (error) {
    console.error('修改密码失败:', error);
    showMessage('error', '修改密码失败，请稍后重试');
  }
  if(isMobileDevice){
    router.push("/settings")
  }
};
</script>

<style scoped>
.change-password {
  margin-top: 10px;
}

.form-group-container {
  margin-top: 20px;
  padding: 15px;
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  display: block;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 14px;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #81c683;
}

.button-group {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:hover {
  opacity: 0.9;
}

.btn.primary {
  background-color: #81c683;
  color: white;
}

.btn.secondary {
  background-color: #f0f0f0;
  color: #333;
}

/* Dark mode support */
:global(.dark) .form-group-container {
  background-color: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

:global(.dark) .form-input {
  background-color: rgba(20, 20, 30, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

:global(.dark) .btn.secondary {
  background-color: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}
</style>