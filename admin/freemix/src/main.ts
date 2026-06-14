import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './stores'
import { ConfigProvider, setDialogDefaultOptions } from 'vant';
import './assets/main.css'

// 引入 Vant 组件库
import Vant from 'vant'
import 'vant/lib/index.css'
import './assets/vant-theme.less'

// 引入 Element Plus 组件库
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 创建 Vue 应用
const app = createApp(App)
app.use(ConfigProvider);
// 使用 Vant
app.use(Vant)

// 全局设置 Dialog 默认选项：确认按钮颜色统一为系统品牌色
setDialogDefaultOptions({
  confirmButtonColor: '#00c9a7'
})

// 使用 Element Plus
app.use(ElementPlus)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用路由和状态管理
app.use(router)
app.use(store)

// 挂载应用
app.mount('#app')