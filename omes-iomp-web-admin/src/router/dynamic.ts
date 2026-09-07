import type { Router } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'
import { addDynamicRoutes, addNotFoundRoute, addStandaloneRoutes, resetDynamicRoutes } from './helper'

export async function setupDynamicRoutes(router: Router): Promise<void> {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  if (!userStore.isLoggedIn || permissionStore.routesRegistered) {
    return
  }

  if (!permissionStore.permissionTree.length) {
    await userStore.initSession()
  }

  resetDynamicRoutes(router)
  addDynamicRoutes(router, permissionStore.buildDynamicRoutes())
  addStandaloneRoutes(router, permissionStore.buildStandaloneRoutes())
  addNotFoundRoute(router)
  permissionStore.markRoutesRegistered()
}

export async function reloadDynamicRoutes(router: Router): Promise<void> {
  const permissionStore = usePermissionStore()
  permissionStore.routesRegistered = false
  await setupDynamicRoutes(router)
}