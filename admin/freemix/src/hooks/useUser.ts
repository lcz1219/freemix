import { ref, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { useMessage, useDialog } from 'naive-ui';
import { postM, isSuccess } from '@/utils/request.js';
import { removeToken } from '@/utils/tokenUtils.js';
import upload from '@/components/upload.vue';
import { h } from 'vue';
import { NAvatar, NText, NInput } from 'naive-ui';
import { baseURL } from '@/utils/request.js';

// 用户信息类型定义
export interface UserInfo {
  username?: string;
  email?: string;
  name?: string;
  position?: string;
  bio?: string;
  chinesename?: string;
  fashionTitle?: string;
  avatarUrl?: string;
  twoFactorEnabled?: boolean;
}

export function useUser() {
  const store = useStore();
  const router = useRouter();
  const message = useMessage();
  const dialog = useDialog();
  
  // 用户信息
  const userInfo = ref<UserInfo>({});
  
  // 头像URL
  const avatarUrl = ref<string>('');
  
  // 个性签名
  const fashionTitle = ref<string>('');
  
  // 初始化用户数据
  const initUserData = () => {
    const user = store.state.user;
    if (user) {
      userInfo.value = { ...user };
      fashionTitle.value = user.fashionTitle || '';
      
      // 设置头像URL
      if (user.avatarUrl) {
        avatarUrl.value = `${baseURL()}${user.avatarUrl}`;
      } else {
        // 默认头像
        avatarUrl.value = 'https://api.dicebear.com/7.x/miniavs/svg?seed=3';
      }
    }
  };
  
  // 编辑个性签名
  const editFashionTitle = async () => {
    const res = await postM('edituserinfo', { 
      ...store.state.user, 
      fashionTitle: fashionTitle.value 
    });
    
    if (isSuccess(res)) {
      store.commit('saveUser', { 
        ...store.state.user, 
        fashionTitle: fashionTitle.value 
      });
      message.success('保存成功');
    }
  };
  
  // 上传头像
  const uploadAvatar = () => {
    dialog.info({
      title: "头像上传",
      style: { padding: '10px' },
      content: () => h(upload, {
        onUploadSuccess: (filename: string) => {
          // 更新头像URL
          avatarUrl.value = `${baseURL()}/file/${filename}`;
          store.commit('saveUser', { 
            ...store.state.user, 
            avatarUrl: "/file/" + filename 
          });
        }
      })
    });
  };
  
  // 渲染自定义头部
  const renderCustomHeader = () => {
    return h(
      'div',
      {
        style: 'display: flex; align-items: center; padding: 8px 12px;'
      },
      [
        h(NAvatar, {
          round: true,
          style: 'margin-right: 12px; cursor: pointer;',
          src: avatarUrl.value,
          onClick: uploadAvatar
        }),
        h('div', null, [
          h('div', null, [
            h(NText, { depth: 2 }, { 
              default: () => store.state.user.chinesename 
            })
          ]),
          h('div', { style: 'font-size: 12px;' }, {
            default: () => [
              store.state.user.fashionTitle ?
                h(
                  NText,
                  { depth: 3 },
                  { default: () => store.state.user.fashionTitle }
                ) : h(
                  NInput,
                  {
                    onBlur: editFashionTitle,
                    depth: 3,
                    placeholder: '添加你的座右铭⛽️',
                    value: fashionTitle.value,
                    onUpdateValue: (newValue: string) => {
                      fashionTitle.value = newValue;
                    },
                  },
                  { default: () => '' }
                )
            ]
          })
        ])
      ]
    );
  };
  
  // 退出登录
  const logout = async () => {
    try {
      // 清除用户信息
      store.commit('clearUser');
      await removeToken();
      // 跳转到登录页面
      router.push('/login');
      message.success('已退出登录');
    } catch (error) {
      console.error('退出登录失败:', error);
      message.error('退出登录失败，请稍后重试');
    }
  };
  
  // 页面加载时初始化用户数据
  onMounted(() => {
    initUserData();
  });
  
  return {
    userInfo,
    avatarUrl,
    fashionTitle,
    initUserData,
    editFashionTitle,
    uploadAvatar,
    renderCustomHeader,
    logout
  };
}