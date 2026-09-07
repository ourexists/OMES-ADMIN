import type { PermissionNode } from '@/types/permission'
import { translateText } from '@/i18n'

export function resolvePermissionLabel(node: Pick<PermissionNode, 'i18n' | 'name'>): string {
  return translateText(node.i18n, node.name)
}
