export const PermissionType = {
  MENU: 0,
  BUTTON: 1,
  OTHER: 2,
} as const

export type PermissionTypeValue = (typeof PermissionType)[keyof typeof PermissionType]

export const PermissionStrategy = {
  ENABLE_SHOW: 0,
  ENABLE_HIDE: 1,
  DISABLED: 2,
} as const

export type PermissionStrategyValue = (typeof PermissionStrategy)[keyof typeof PermissionStrategy]

export interface PermissionNode {
  id: string
  code?: string
  pcode?: string
  name: string
  i18n?: string
  icon?: string
  url?: string
  component?: string
  type?: number
  strategy?: number
  sortNo?: number
  keepAlive?: boolean
  description?: string
  platform?: string
  children?: PermissionNode[]
}
