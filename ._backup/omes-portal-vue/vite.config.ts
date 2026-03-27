import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// 后端 API 路径前缀（与 omes-portal static/js/global.js 中的 router 一致）
const API_PREFIXES = [
  'open',
  'oauth2',
  'acc',
  'mat',
  'mc',
  'mps',
  'mps_detail',
  'flow',
  'BOMC',
  'BOMD',
  'BOM',
  'mo',
  'line',
  'tf',
  'sync',
  'qa',
  'plc',
  'task',
  'devg',
  'device',
  'report',
  'fzdata',
  'lmrecord',
  'platform',
  'permission',
  'role',
  'notify',
  'workshop',
  'ecattr',
  'equipRecordRun',
  'equipRecordAlarm',
  'equipRecordOnline',
  'equip',
  'equipStateSnapshot',
  'equipHealth',
  'message',
  'gateway',
  'product',
  'inspection',
]

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const isProd = mode === 'production'
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://localhost:10010'

  const proxy: Record<string, { target: string; changeOrigin: boolean }> = {}
  for (const p of API_PREFIXES) {
    proxy[`/${p}`] = { target: proxyTarget, changeOrigin: true }
  }

  return {
    plugins: [vue()],
    base: isProd ? '/vue/' : '/',
    build: {
      outDir: '../omes-portal/src/main/resources/static/vue',
      emptyOutDir: true,
    },
    server: {
      proxy,
    },
  }
})
