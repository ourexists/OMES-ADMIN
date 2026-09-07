import { computed } from 'vue'
import { usePermissionStore } from '@/stores/permission'

export function usePermission() {
  const permissionStore = usePermissionStore()

  const permissionCodes = computed(() => permissionStore.permissionCodes)
  const menuTree = computed(() => permissionStore.menuTree)

  function hasPermission(code?: string | string[]) {
    return permissionStore.hasPermission(code)
  }

  function canAccessPath(path: string) {
    return permissionStore.canAccessPath(path)
  }

  return {
    permissionCodes,
    menuTree,
    hasPermission,
    canAccessPath,
  }
}
