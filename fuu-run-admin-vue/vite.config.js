import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发环境通过 /api 前缀代理到后端 8081，避免跨域配置
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
