import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  // 路径别名
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      '@features': path.resolve(__dirname, 'src/features'),
      '@shared': path.resolve(__dirname, 'src/shared'),
      '@api': path.resolve(__dirname, 'src/api'),
      '@store': path.resolve(__dirname, 'src/store'),
      '@layouts': path.resolve(__dirname, 'src/layouts'),
      '@routes': path.resolve(__dirname, 'src/routes'),
      '@config': path.resolve(__dirname, 'src/config'),
    },
  },

  // 开发服务器配置
  server: {
    port: 5173,
    host: true,
    open: true,
    // API 代理（连接后端）
    proxy: {
      '/auth': {
        target: 'http://localhost:9100',
        changeOrigin: true,
      },
      '/user': {
        target: 'http://localhost:9200',
        changeOrigin: true,
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },

  // 构建配置
  build: {
    outDir: 'dist',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('react') || id.includes('react-dom') || id.includes('react-router-dom')) {
            return 'vendor-react';
          }
          if (id.includes('zustand') || id.includes('react-query')) {
            return 'vendor-state';
          }
          if (id.includes('antd') || id.includes('ant-design')) {
            return 'vendor-ui';
          }
          if (id.includes('axios')) {
            return 'vendor-http';
          }
        },
      },
    },
  },
})