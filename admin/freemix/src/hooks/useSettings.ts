import { ref, computed } from 'vue';
import { useStore } from 'vuex';
import { useMessage } from 'naive-ui';
import { postM, isSuccess } from '@/utils/request.js';
import { useUser } from './useUser';

export function useSettings() {
  const store = useStore();
  const message = useMessage();
  const { userInfo } = useUser();
  
  // 表单数据
  const profileForm = ref({
    name: '',
    email: '',
    position: '',
    bio: ''
  });
  
  const securitySettings = ref({
    twoFactorEnabled: false,
    password: '',
    newPassword: '',
    confirmPassword: ''
  });
  
  const notificationSettings = ref({
    emailNotifications: true,
    pushNotifications: false,
    reminderTime: '09:00'
  });
  
  const themeSettings = ref({
    mode: 'auto' as 'light' | 'dark' | 'auto',
    animationsEnabled: true
  });
  
  // 计算属性
  const reminderTime = computed(() => notificationSettings.value.reminderTime);
  
  // 页面标题
  const getPageTitle = (section: string) => {
    switch (section) {
      case 'profile': return '个人资料';
      case 'security': return '安全设置';
      case 'token': return 'Token信息';
      default: return '';
    }
  };
  
  // 页面副标题
  const getPageSubtitle = (section: string) => {
    switch (section) {
      case 'profile': return '管理您的个人资料信息';
      case 'security': return '管理您的账户安全设置';
      case 'token': return '查看和管理您的API Token';
      default: return '';
    }
  };
  
  // 保存个人资料
  const saveProfile = async () => {
    const res = await postM('edituserinfo', profileForm.value);
    if (isSuccess(res)) {
      store.commit('saveUser', { ...store.state.user, ...profileForm.value });
      message.success('个人资料保存成功');
    } else {
      message.error('个人资料保存失败');
    }
  };
  
  // 保存安全设置
  const saveSecuritySettings = async () => {
    if (securitySettings.value.newPassword !== securitySettings.value.confirmPassword) {
      message.error('新密码和确认密码不匹配');
      return;
    }
    
    const res = await postM('changePassword', {
      oldPassword: securitySettings.value.password,
      newPassword: securitySettings.value.newPassword
    });
    
    if (isSuccess(res)) {
      message.success('密码修改成功');
      securitySettings.value.password = '';
      securitySettings.value.newPassword = '';
      securitySettings.value.confirmPassword = '';
    } else {
      message.error('密码修改失败');
    }
  };
  
  // 保存通知设置
  const saveNotifications = () => {
    message.success('通知设置已保存');
  };
  
  // 保存主题设置
  const saveTheme = () => {
    message.success('主题设置已保存');
  };
  
  // 导出数据
  const exportData = () => {
    message.info('数据导出功能开发中...');
  };
  
  // 导入数据
  const importData = () => {
    message.info('数据导入功能开发中...');
  };
  
  // 清除所有数据
  const clearAllData = () => {
    message.info('清除所有数据功能开发中...');
  };
  
  // 更改密码
  const changePassword = () => {
    message.info('更改密码功能开发中...');
  };
  
  // 管理会话
  const manageSessions = () => {
    message.info('会话管理功能开发中...');
  };
  
  // 两步验证更新
  const onTwoFactorUpdate = (value: boolean) => {
    securitySettings.value.twoFactorEnabled = value;
    message.info(`两步验证已${value ? '启用' : '禁用'}`);
  };
  
  // 初始化用户数据
  const initUserData = () => {
    const user = store.state.user;
    console.log("initUserData",user);
    if (user) {
      // profileForm.value.name = user.chinesename || '';
      // profileForm.value.username = user.username || '';
      // profileForm.value.email = user.email || '';
      // profileForm.value.position = user.position || '';
      // profileForm.value.bio = user.fashionTitle || '';
      // profileForm.value.bio = user.fashionTitle || '';
      profileForm.value = user;
      
      securitySettings.value.twoFactorEnabled = user.twoFactorEnabled || false;
    }
  };
  
  return {
    profileForm,
    securitySettings,
    notificationSettings,
    themeSettings,
    reminderTime,
    getPageTitle,
    getPageSubtitle,
    saveProfile,
    saveSecuritySettings,
    saveNotifications,
    saveTheme,
    exportData,
    importData,
    clearAllData,
    changePassword,
    manageSessions,
    onTwoFactorUpdate,
    initUserData
  };
}