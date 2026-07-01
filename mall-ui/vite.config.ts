import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // 开发期把 /api 反代到各后端服务，规避跨域；生产经网关/同源部署，前端只用相对路径
    proxy: {
      '/api/wallet': { target: 'http://localhost:10002', changeOrigin: true },
      '/api/open/user': { target: 'http://localhost:10001', changeOrigin: true },
      '/api/open/content': { target: 'http://localhost:10000', changeOrigin: true },
      '/api/open/leaderboard': { target: 'http://localhost:10000', changeOrigin: true },
      '/api/open/mission': { target: 'http://localhost:10003', changeOrigin: true },
      '/api/open/promotion': { target: 'http://localhost:10004', changeOrigin: true },
    },
  },
})
