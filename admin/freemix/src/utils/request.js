// src/utils/request.js
import axios from 'axios';
import store from '../stores/index.js';
import router from '../router/index.js';

const request = axios.create({
  baseURL: 'http://localhost:5173',
  headers: {
    'Content-Type': 'application/json'
  },
});

// 添加请求拦截器，动态设置token
request.interceptors.request.use(
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
        return false
      }
 }

export {request,postM,getM,getMPaths,isSuccess};