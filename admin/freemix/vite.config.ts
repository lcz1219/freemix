import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'


// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
  ],
   server: {
    proxy: {
      '/freemix': { // 拦截所有以 /api 开头的请求
        target: 'http://localhost:8888', // 后端地址
        changeOrigin: true, // 修改请求源为 target
        rewrite: (path) => path.replace(/^\/freemix/, '') // 移除 /api 前缀
      }
    }
  },
  
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  
})
