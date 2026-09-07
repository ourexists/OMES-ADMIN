import type { App } from 'vue'
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'

export function setupAdminComponents(app: App) {
  app.component('CompactSearchActions', CompactSearchActions)
}
