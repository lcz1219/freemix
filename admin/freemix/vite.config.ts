import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'


// https://vite.dev/config/
export default defineConfig({
  base: './', // 使用相对路径，确保打包后资源能正确加载
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
      },
      '/ws': {
        target: 'http://localhost:8888/ws', // 真实后端地址
        changeOrigin: true,
        ws: true, // 必须！启用 WebSocket 代理
        rewrite: (path) => path.replace(/^\/ws/, '')
      },
    }
  },
  
  define: {
    global: 'globalThis',
  },
  
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  
})
