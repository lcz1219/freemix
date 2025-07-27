// src/utils/request.js
import axios from 'axios';

const request = axios.create({
  baseURL: 'http://localhost:5173',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + localStorage.getItem('token') || ''
  },

});
 const postM=function(val,data){
    return request.post(`/freemix/${val}`,data)
}
 const getM=function(val,data){
    return request.get(`/freemix/${val}`,{params:data},)
}
 const isSuccess=( res)=>{
      if(res.data.code==200){
        return true
      }else{
        return false
      }
 }

export {request,postM,getM,isSuccess}; 
