import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchCurrentUser, fetchPermissionTree, login as loginApi } from '@/api/auth'
import { STORAGE_KEYS } from '@/config'
import { usePermissionStore } from '@/stores/permission'
import type { PermissionNode } from '@/types/permission'
import type { UserInfo } from '@/types/user'
import { getItem, getJson, removeItem, setItem, setJson } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref(getItem(STORAGE_KEYS.token) || '')
  const userInfo = ref<UserInfo | null>(getJson<UserInfo>(STORAGE_KEYS.userInfo))
  const sessionReady = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))

  function setToken(value: string) {
    token.value = value
    setItem(STORAGE_KEYS.token, value)
  }

  function setUserInfo(value: UserInfo | null) {
    userInfo.value = value
    if (value) {
      setJson(STORAGE_KEYS.userInfo, value)
    } else {
      removeItem(STORAGE_KEYS.userInfo)
    }
  }

  function syncPermissionTree(tree: PermissionNode[]) {
    const permissionStore = usePermissionStore()
    permissionStore.setPermissionTree(tree)
    setJson(STORAGE_KEYS.menu, tree)
  }

  async function initSession(): Promise<boolean> {
    if (!token.value) {
      sessionReady.value = false
      return false
    }

    try {
      const user = await fetchCurrentUser()
      setUserInfo(user)

      const tree = await fetchPermissionTree()
      syncPermissionTree(Array.isArray(tree) ? tree : [])
      sessionReady.value = true
      return true
    } catch {
      logout()
      return false
    }
  }

  async function login(username: string, password: string, captcha: string) {
    const result = await loginApi({ username, password, captcha })
    const type = (result.token_type || 'Bearer').trim()
    const authToken = /^bearer$/i.test(type)
      ? `Bearer ${result.access_token}`
      : `${type} ${result.access_token}`
    setToken(authToken)
    sessionReady.value = false

    const user = await fetchCurrentUser()
    setUserInfo(user)

    const tree = await fetchPermissionTree()
    syncPermissionTree(Array.isArray(tree) ? tree : [])
    sessionReady.value = true
  }

  function logout() {
    const permissionStore = usePermissionStore()
    token.value = ''
    userInfo.value = null
    sessionReady.value = false
    permissionStore.reset()
    removeItem(STORAGE_KEYS.token)
    removeItem(STORAGE_KEYS.userInfo)
    removeItem(STORAGE_KEYS.menu)
  }

  return {
    token,
    userInfo,
    sessionReady,
    isLoggedIn,
    login,
    logout,
    initSession,
    setToken,
    setUserInfo,
    syncPermissionTree,
  }
})
