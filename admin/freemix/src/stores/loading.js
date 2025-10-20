const loadingStore = {
  namespaced: true,
  state: {
    loading: false,
    loadingText: '加载中...'
  },
  mutations: {
    SHOW_LOADING(state, text = '加载中...') {
      state.loading = true;
      state.loadingText = text;
    },
    HIDE_LOADING(state) {
      setTimeout(() => {
        state.loading = false;
        state.loadingText = '加载中...';
      }, 500);
    }
  },
  actions: {
    showLoading({ commit }, text) {
      commit('SHOW_LOADING', text);
    },
    hideLoading({ commit }) {
      commit('HIDE_LOADING');
    }
  },
  getters: {
    isLoading: state => state.loading,
    loadingText: state => state.loadingText
  }
};

export default loadingStore;