import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 后端服务端口配置（Gateway 未实现，直接代理到各服务）
const services = {
  auth: 'http://localhost:9100',
  post: 'http://localhost:9300',
  search: 'http://localhost:9400',
  message: 'http://localhost:9500'
};

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/auth': services.auth,
      '/post': services.post,
      '/category': services.post,
      '/tag': services.post,
      '/comment': services.post,
      '/search': services.search,
      '/message': services.message
    }
  }
});
