import type { App, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'

function checkPermission(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
  const permissionStore = usePermissionStore()
  const allowed = permissionStore.hasPermission(binding.value)

  if (!allowed) {
    el.style.display = 'none'
  } else {
    el.style.display = ''
  }
}

export function setupPermissionDirective(app: App) {
  app.directive('permission', {
    mounted: checkPermission,
    updated: checkPermission,
  })
}
