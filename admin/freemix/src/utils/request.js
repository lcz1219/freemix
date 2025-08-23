// src/utils/request.js
import axios from 'axios';
import store from '../stores/index.js';
import router from '../router/index.js';

const request = axios.create({
  baseURL: import.meta.env.PROD ? 'http://8.134.84.105' : 'http://localhost:5173',
  headers: {
    'Content-Type': 'application/json'
  },
});

// 添加请求拦截器，动态设置token
const NO_AUTH_PATHS = ['/login', '/register', '/captcha', '/file/upload'];

request.interceptors.request.use(
  config => {
    const needsAuth = !NO_AUTH_PATHS.some(path => 
      config.url.endsWith(path) // 精确匹配路径结尾
    );
    
    if (needsAuth) {
      const token = localStorage.getItem('token');
      if (!token) {
        return Promise.reject(new Error('缺少认证Token'));
      }
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);  
const baseURL =()=> {
  let url = '';
  url= import.meta.env.PROD ? 'http://8.134.84.105/freemix' : 'http://localhost:5173/freemix'
  return url;
  };
// 创建一个用于文件上传的axios实例
const fileRequest = axios.create({
  baseURL: import.meta.env.PROD ? 'http://8.134.84.105' : 'http://localhost:5173',
});

fileRequest.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

 const postM=async function(val,data){
   const res= await request.post(`/freemix/${val}`,data)
    if(res.data.msg=='token过期'){
       await store.dispatch('logout')
       router.push('/login')
     }
     return res
}
 const getM=async function(val,data){
   const res=await  request.get(`/freemix/${val}`,{params:data},)
    if(res.data.msg=='token过期'){
       await store.dispatch('logout')
       router.push('/login')
     }
     return res
}
const getMPaths=async function(val,data){
     const res=await request.get(`/freemix/${val}/${data}`)
     if(res.data.msg=='token过期'){
      
      // console.log('token过期');
      
       await store.dispatch('logout')
       router.push('/login')
     }
     return res
}
 const isSuccess=( res)=>{
      if(res.data.code==200){
        return true
      }else{
        if(res.data.msg=='token不正确'){
          store.dispatch('logout')
        }
        return false
      }
 }

// 文件上传函数
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

export default request;
export { postM, getM, getMPaths, isSuccess, uploadFile,baseURL };