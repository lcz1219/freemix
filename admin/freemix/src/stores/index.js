import { createStore } from 'vuex';
import router from '../router';
import loadingStore from './loading.js';


export default createStore({
  state() {
    // 从 localStorage 恢复用户状态
    const savedUser = localStorage.getItem('user');
    return {
      user: savedUser ? JSON.parse(savedUser) : {}        // 数据源
    }
  },
  mutations: {                // 同步修改
    saveUser(state, user) {
      state.user = user;
      // 将用户信息保存到 localStorage
      localStorage.setItem('user', JSON.stringify(user));
    },
    clearUser(state) {
      state.user = {};
      // 从 localStorage 中移除用户信息
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      router.push('/login');
    }
  },
  actions: {                  // 异步操作
    login({ commit }) {
      
    },
    logout({ commit }) {
      commit('clearUser');
    }
  },
  getters: {                  // 计算属性
    user: state => state.user
  },
  modules: {
    loading: loadingStore
  }                 // 模块化（可选）
});