import type { WorkshopNode } from '@/api/device'

export interface WorkshopTreeIndex {
  nodeByCode: Map<string, WorkshopNode>
  nodeCount: number
  expandableKeys: string[]
}

export function buildWorkshopTreeIndex(nodes: WorkshopNode[]): WorkshopTreeIndex {
  const nodeByCode = new Map<string, WorkshopNode>()
  const expandableKeys: string[] = []
  let nodeCount = 0

  function walk(list: WorkshopNode[]) {
    for (const node of list) {
      nodeCount += 1
      nodeByCode.set(node.selfCode, node)
      if (node.children?.length) {
        expandableKeys.push(node.selfCode)
        walk(node.children)
      }
    }
  }

  walk(nodes)
  return { nodeByCode, nodeCount, expandableKeys }
}

export function collectExpandableKeys(nodes: WorkshopNode[], maxDepth = Infinity, depth = 0): string[] {
  if (depth >= maxDepth) {
    return []
  }
  const keys: string[] = []
  for (const node of nodes) {
    if (node.children?.length) {
      keys.push(node.selfCode)
      keys.push(...collectExpandableKeys(node.children, maxDepth, depth + 1))
    }
  }
  return keys
}

export function filterWorkshopTree(nodes: WorkshopNode[], keyword: string): WorkshopNode[] {
  const result: WorkshopNode[] = []
  for (const node of nodes) {
    const children = node.children?.length ? filterWorkshopTree(node.children, keyword) : []
    const selfMatch =
      node.name?.toLowerCase().includes(keyword) ||
      node.selfCode?.toLowerCase().includes(keyword)
    if (selfMatch || children.length) {
      result.push({
        ...node,
        children: children.length ? children : selfMatch ? node.children : undefined,
      })
    }
  }
  return result
}

export function collectSearchMatchKeys(nodes: WorkshopNode[], keyword: string): Set<string> {
  const matches = new Set<string>()
  function walk(list: WorkshopNode[]) {
    for (const node of list) {
      if (
        node.name?.toLowerCase().includes(keyword) ||
        node.selfCode?.toLowerCase().includes(keyword)
      ) {
        matches.add(node.selfCode)
      }
      if (node.children?.length) {
        walk(node.children)
      }
    }
  }
  walk(nodes)
  return matches
}

/** 从根到目标节点的路径（用于面包屑） */
export function findWorkshopNodePath(
  nodes: WorkshopNode[],
  selfCode: string,
  trail: WorkshopNode[] = [],
): WorkshopNode[] | null {
  for (const node of nodes) {
    const nextTrail = [...trail, node]
    if (node.selfCode === selfCode) {
      return nextTrail
    }
    if (node.children?.length) {
      const found = findWorkshopNodePath(node.children, selfCode, nextTrail)
      if (found) {
        return found
      }
    }
  }
  return null
}

/** 按 pcode 向上回溯路径，树 walk 失败时的兜底 */
export function getWorkshopBreadcrumbByPcode(
  nodeByCode: Map<string, WorkshopNode>,
  selfCode?: string | null,
): WorkshopNode[] {
  if (!selfCode || !nodeByCode.has(selfCode)) {
    return []
  }
  const path: WorkshopNode[] = []
  const visited = new Set<string>()
  let code: string | undefined = selfCode
  while (code && nodeByCode.has(code) && !visited.has(code)) {
    visited.add(code)
    const node: WorkshopNode = nodeByCode.get(code)!
    path.unshift(node)
    code = node.pcode || undefined
  }
  return path
}

export function resolveWorkshopBreadcrumb(
  nodes: WorkshopNode[],
  nodeByCode: Map<string, WorkshopNode>,
  selfCode?: string | null,
): WorkshopNode[] {
  if (!selfCode) {
    return []
  }
  const byTree = findWorkshopNodePath(nodes, selfCode)
  if (byTree?.length) {
    return byTree
  }
  return getWorkshopBreadcrumbByPcode(nodeByCode, selfCode)
}
