import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    proxy: {
      '/admin': {
        target: 'http://localhost:9700',
        changeOrigin: true
      }
    }
  }
})