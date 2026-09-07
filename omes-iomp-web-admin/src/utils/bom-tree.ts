import type { BomClassifyNode } from '@/api/bom'

export interface BomTreeIndex {
  nodeByCode: Map<string, BomClassifyNode>
  nodeCount: number
  expandableKeys: string[]
}

export function buildBomTreeIndex(nodes: BomClassifyNode[]): BomTreeIndex {
  const nodeByCode = new Map<string, BomClassifyNode>()
  const expandableKeys: string[] = []
  let nodeCount = 0

  function walk(list: BomClassifyNode[]) {
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

export function collectBomExpandableKeys(
  nodes: BomClassifyNode[],
  maxDepth = Infinity,
  depth = 0,
): string[] {
  if (depth >= maxDepth) {
    return []
  }
  const keys: string[] = []
  for (const node of nodes) {
    if (node.children?.length) {
      keys.push(node.selfCode)
      keys.push(...collectBomExpandableKeys(node.children, maxDepth, depth + 1))
    }
  }
  return keys
}

export function filterBomTree(nodes: BomClassifyNode[], keyword: string): BomClassifyNode[] {
  const result: BomClassifyNode[] = []
  for (const node of nodes) {
    const children = node.children?.length ? filterBomTree(node.children, keyword) : []
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

export function collectBomSearchMatchKeys(nodes: BomClassifyNode[], keyword: string): Set<string> {
  const matches = new Set<string>()
  function walk(list: BomClassifyNode[]) {
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
