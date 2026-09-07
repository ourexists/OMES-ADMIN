import {
  PermissionStrategy,
  PermissionType,
  type PermissionNode,
} from '@/types/permission'

export function isVisibleMenu(node: PermissionNode): boolean {
  return node.type === PermissionType.MENU && node.strategy === PermissionStrategy.ENABLE_SHOW
}

export function isEnabledPermission(node: PermissionNode): boolean {
  return node.strategy !== PermissionStrategy.DISABLED
}

export function filterMenuTree(nodes: PermissionNode[] = []): PermissionNode[] {
  return nodes
    .filter(isVisibleMenu)
    .map((node) => ({
      ...node,
      children: node.children?.length ? filterMenuTree(node.children) : [],
    }))
    .sort(sortByOrder)
}

export function flattenPermissions(nodes: PermissionNode[] = []): PermissionNode[] {
  const result: PermissionNode[] = []
  const walk = (list: PermissionNode[]) => {
    list.forEach((node) => {
      result.push(node)
      if (node.children?.length) {
        walk(node.children)
      }
    })
  }
  walk(nodes)
  return result
}

export function collectPermissionCodes(nodes: PermissionNode[] = []): Set<string> {
  const codes = new Set<string>()
  flattenPermissions(nodes).forEach((node) => {
    if (!isEnabledPermission(node)) {
      return
    }
    if (node.code) {
      codes.add(node.code)
    }
    if (node.id) {
      codes.add(node.id)
    }
  })
  return codes
}

export const LOCAL_FILE_VIEW_PATHS = ['/view/local_file', '/view/local_file_tables'] as const

export function permissionGrantsLocalFile(nodes: PermissionNode[] = []): boolean {
  return flattenPermissions(nodes).some((node) => {
    if (!isEnabledPermission(node)) {
      return false
    }
    const haystack = `${node.url || ''} ${node.code || ''} ${node.i18n || ''} ${node.name || ''}`.toLowerCase()
    return (
      haystack.includes('local_file')
      || haystack.includes('localfile')
      || haystack.includes('文件管理')
    )
  })
}

export function collectAccessiblePaths(nodes: PermissionNode[] = []): Set<string> {
  const paths = new Set<string>(['/view/overview'])
  flattenPermissions(filterMenuTree(nodes)).forEach((node) => {
    if (!node.url) {
      return
    }
    const raw = normalizeRoutePath(node.url)
    paths.add(raw)
    if (!raw.startsWith('/view/')) {
      paths.add(`/view/${raw.replace(/^\//, '')}`)
    }
  })
  if (permissionGrantsLocalFile(nodes)) {
    LOCAL_FILE_VIEW_PATHS.forEach((path) => paths.add(path))
  }
  return paths
}

export function canAccessLocalFilePath(
  path: string,
  nodes: PermissionNode[] = [],
  accessiblePaths?: Set<string>,
): boolean {
  const normalized = normalizeRoutePath(path)
  if (!LOCAL_FILE_VIEW_PATHS.includes(normalized as (typeof LOCAL_FILE_VIEW_PATHS)[number])) {
    return false
  }
  if (permissionGrantsLocalFile(nodes)) {
    return true
  }
  const systemAdminPaths = ['/view/task_tables', '/view/sync_tables']
  return systemAdminPaths.some((item) => accessiblePaths?.has(item))
}

export function findPermissionByUrl(nodes: PermissionNode[] = [], url: string): PermissionNode | null {
  const normalized = normalizeRoutePath(url)
  for (const node of flattenPermissions(filterMenuTree(nodes))) {
    if (node.url && normalizeRoutePath(node.url) === normalized) {
      return node
    }
  }
  return null
}

export function normalizeRoutePath(url: string): string {
  const path = url.trim()
  if (!path) {
    return '/'
  }
  return path.startsWith('/') ? path : `/${path}`
}

export function hasPermissionCode(codes: Set<string>, value?: string | string[]): boolean {
  if (!value) {
    return true
  }
  const targets = Array.isArray(value) ? value : [value]
  return targets.some((item) => codes.has(item))
}

function sortByOrder(a: PermissionNode, b: PermissionNode): number {
  return (a.sortNo ?? 0) - (b.sortNo ?? 0)
}
