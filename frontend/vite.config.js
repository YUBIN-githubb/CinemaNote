import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/signup': 'http://localhost:8080',
      '/signin': 'http://localhost:8080',
      '/signout': 'http://localhost:8080',
    },
  },
})
