import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  // Load environment variables based on mode
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    optimizeDeps: {
      include: ['naive-ui']
    },
    server: {
      port: parseInt(env.VITE_DEV_SERVER_PORT || '5173'),
      host: env.VITE_DEV_SERVER_HOST || 'localhost',
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true
        }
      }
    },
    build: {
      // Production build configuration
      sourcemap: mode !== 'production',
      rollupOptions: {
        output: {
          manualChunks: {
            'vendor': ['vue', 'vue-router', 'pinia'],
            'ui': ['naive-ui'],
            'charts': ['echarts']
          }
        }
      }
    },
    define: {
      // Make app version available at build time
      __APP_VERSION__: JSON.stringify(process.env.npm_package_version || '1.0.0')
    }
  }
})
