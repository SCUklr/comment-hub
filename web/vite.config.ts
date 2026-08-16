import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 开发环境通过 Vite 代理把 /api 转发到后端，规避 CORS
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
