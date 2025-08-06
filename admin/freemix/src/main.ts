import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './stores'
import './assets/main.css'

// 引入 Vant 组件库
import Vant from 'vant'
import 'vant/lib/index.css'

// 创建 Vue 应用
const app = createApp(App)

// 使用 Vant
app.use(Vant)

// 使用路由和状态管理
app.use(router)
app.use(store)

// 挂载应用
app.mount('#app')