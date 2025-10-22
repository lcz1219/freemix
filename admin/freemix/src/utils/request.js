// src/utils/request.js
import axios from 'axios';
import store from '../stores/index.js';
import router from '../router/index.js';
import { isDesktop } from './device.js';
import { getLocalStorageDesktopToken } from './desktopToken.js';
import { saveToken, getToken } from './tokenUtils.js';

const request = axios.create({
  baseURL: import.meta.env.PROD ? 'http://8.148.242.131' : 'http://localhost:5173',
  headers: {
    'Content-Type': 'application/json'
  },
});

// 添加请求拦截器，动态设置token
const NO_AUTH_PATHS = ['/login', '/register', '/captcha','/enable2fa', '/file/upload','/verify2fa'];

request.interceptors.request.use(
 async config => {
    const needsAuth = !NO_AUTH_PATHS.some(path => 
      config.url.endsWith(path) // 精确匹配路径结尾
    );
    
    if (needsAuth) {
      // 检查是否为桌面端
      // if (isDesktop()) {
      //   // 桌面端使用桌面token和持久化存储的普通token
      //   const desktopToken = getLocalStorageDesktopToken();
        // 获取token
        const token = await getToken(); // 使用新工具函数
        if (!token) {
          return Promise.reject(new Error('缺少认证Token'));
        }
        
        // 根据设备类型设置相应的认证头
        if (isDesktop()) {
          // 桌面端优先使用本地存储的桌面token，如果没有则使用普通token
          const desktopToken = getLocalStorageDesktopToken();
          if (desktopToken) {
            config.headers['X-Desktop-Token'] = desktopToken;
          } else {
            config.headers['X-Desktop-Token'] = token;
          }
        } else {
          // 移动端使用普通token
          config.headers.Authorization = `Bearer ${token}`;
        }
    }
    return config;
  },
  error => {
    // 隐藏loading
    store.dispatch('loading/hideLoading');
    return Promise.reject(error);
  }
);  
const baseURL =()=> {
  let url = '';
  url= import.meta.env.PROD ? 'http://8.148.242.131/freemix' : 'http://localhost:5173/freemix'
  return url;
  };
// 创建一个用于文件上传的axios实例
const fileRequest = axios.create({
  baseURL: import.meta.env.PROD ? 'http://8.148.242.131' : 'http://localhost:5173',
});

fileRequest.interceptors.request.use(
  async config => {
    // 获取token
    const token = await getToken(); // 使用新工具函数
    if (!token) {
      return Promise.reject(new Error('缺少认证Token'));
    }
    
    // 根据设备类型设置相应的认证头
    if (isDesktop()) {
      // 桌面端优先使用本地存储的桌面token，如果没有则使用普通token
      const desktopToken = getLocalStorageDesktopToken();
      if (desktopToken) {
        config.headers['X-Desktop-Token'] = desktopToken;
      } else {
        config.headers['X-Desktop-Token'] = token;
      }
    } else {
      // 移动端使用普通token
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    // 隐藏loading
    store.dispatch('loading/hideLoading');
    return Promise.reject(error);
  }
);

 const postM=async function(val,data, loadingText = ''){
   // 显示loading
   if (loadingText != '') {
     store.dispatch('loading/showLoading', loadingText);
   }
   
   try {
     // 检查是否为FormData，如果是则让浏览器自动设置Content-Type
     const config = {};
     if (data instanceof FormData) {
       // 不设置Content-Type，让浏览器自动设置multipart/form-data和boundary
       config.headers = {'Content-Type': 'application/form-data'};
     } else {
       config.headers = {
         'Content-Type': 'application/json'
       };
     }
     
     const res= await request.post(`/freemix/${val}`,data, config)
      if(res.data.msg=='token过期'){
         await store.dispatch('logout')
         router.push('/login')
       }
       
       // 隐藏loading
       if (loadingText != '') {
         store.dispatch('loading/hideLoading');
       }
      return res
   } catch (error) {
     // 隐藏loading
     if (loadingText != '') {
       store.dispatch('loading/hideLoading');
     }
     throw error;
   }
}
 const getM=async function(val,data, loadingText = ''){
   // 显示loading
   if (loadingText != '') {
     store.dispatch('loading/showLoading', loadingText);
   }
   
   try {
     const res=await  request.get(`/freemix/${val}`,{params:data},)
      if(res.data.msg=='token过期'){
         await store.dispatch('logout')
         router.push('/login')
       }
       
       // 隐藏loading
       if (loadingText != '') {
         store.dispatch('loading/hideLoading');
       }
      return res
   } catch (error) {
     // 隐藏loading
     if (loadingText != '') {
       store.dispatch('loading/hideLoading');
     }
     throw error;
   }
}
const getMPaths=async function(val,data, loadingText = ''){
   // 显示loading
   if (loadingText != '') {
     store.dispatch('loading/showLoading', loadingText);
   }
   
   try {
     const res=await request.get(`/freemix/${val}/${data}`)
      if(res.data.msg=='token过期'){
        
        // console.log('token过期');
        
         await store.dispatch('logout')
         router.push('/login')
       }
       
       // 隐藏loading
       if (loadingText != '') {
         store.dispatch('loading/hideLoading');
       }
      return res
   } catch (error) {
     // 隐藏loading
     if (loadingText != '') {
       store.dispatch('loading/hideLoading');
     }
     throw error;
   }
}
 const isSuccess=( res)=>{
      if(res.data.code==200){
        return true
      }else{
        if(res.data.msg.includes('token不正确')){
          store.dispatch('logout')
        }
        return false
      }
 }

// 头像文件上传函数
const uploadFile = async function(file,user) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('user', user);
  
  try {
    const res = await fileRequest.post('/freemix/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return res;
  } catch (error) {
    return error.response;
  }
}

// 通用文件上传函数
const uploadGeneralFile = async function(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const res = await fileRequest.post('/freemix/file/upload-file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return res;
  } catch (error) {
    return error.response;
  }
}

// 检查当前用户是否为目标所有者的工具函数
const isGoalOwner = function(goal) {
  const currentUser = store.state.user;
  if (!currentUser || !currentUser.username) return false;
  
  // 检查当前用户是否为目标所有者
  if (goal.ownerName === currentUser.username) return true;
  if (goal.owner === currentUser.username) return true;
  
  // 检查协作人列表中是否有当前用户且角色为owner
  if (goal.collaborators && goal.collaborators.length > 0) {
    const ownerCollaborator = goal.collaborators.find(c => 
      c.username === currentUser.username && c.role === 'owner'
    );
    return !!ownerCollaborator;
  }
  
  return false;
}

export default request;
export { postM, getM, getMPaths, isSuccess, uploadFile, uploadGeneralFile, baseURL, isGoalOwner };