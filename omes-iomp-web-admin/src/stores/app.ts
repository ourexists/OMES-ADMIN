import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loadFrontendConfig } from '@/config/frontend-config'

export const useAppStore = defineStore('app', () => {
  const collapsed = ref(false)
  const title = ref(import.meta.env.VITE_APP_TITLE)
  const gatewayPort = ref(Number(import.meta.env.VITE_GATEWAY_PORT || 9400))
  const sasBaseUrl = ref('')
  const ready = ref(false)

  async function bootstrap() {
    const cfg = await loadFrontendConfig()
    gatewayPort.value = cfg.gatewayPort
    sasBaseUrl.value = cfg.sasBaseUrl
    ready.value = true
  }

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  return {
    collapsed,
    title,
    gatewayPort,
    sasBaseUrl,
    ready,
    bootstrap,
    toggleCollapsed,
  }
})
