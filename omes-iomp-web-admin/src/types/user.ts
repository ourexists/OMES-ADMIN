import type { PermissionNode } from '@/types/permission'

export type MenuItem = PermissionNode

export interface UserInfo {
  id?: string
  username?: string
  accName?: string
  nickName?: string
  nickname?: string
  avatar?: string
  avatarUrl?: string
  email?: string
  mobile?: string
  tenantRoles?: Record<string, string>
  [key: string]: unknown
}

export function getDisplayName(user: UserInfo | null | undefined): string {
  if (!user) {
    return '管理员'
  }
  return user.nickName || user.nickname || user.accName || user.username || '管理员'
}
