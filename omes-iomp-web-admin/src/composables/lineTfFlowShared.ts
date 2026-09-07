import type { Edge, Node } from '@vue-flow/core'
import { MarkerType, Position } from '@vue-flow/core'
import type { TfEdgeRecord, TfRecord } from '@/types/line'

export const LINE_TF_START_NODE_ID = '__line_tf_start__'
export const LINE_TF_END_NODE_ID = '__line_tf_end__'

export const TF_NODE_W = 148
export const TF_NODE_H = 72
export const TF_TERMINAL_NODE_W = 148
export const TF_TERMINAL_NODE_H = 72

/** 无工序时，开始/结束节点默认左右间距 */
const LINE_TF_EMPTY_TERMINAL_SPAN = 560
const LINE_TF_EMPTY_TERMINAL_Y = 140

const LINE_TF_EDGE_STYLE = { stroke: '#91a4b7', strokeWidth: 2 }

export function isLineTfTerminalNodeId(id: string | null | undefined): boolean {
  const value = String(id || '')
  return value === LINE_TF_START_NODE_ID || value === LINE_TF_END_NODE_ID
}

export function buildNodeById(nodes: TfRecord[]) {
  const out: Record<string, TfRecord> = {}
  nodes.forEach((n) => {
    if (n?.id != null) {
      out[String(n.id)] = n
    }
  })
  return out
}

export function dedupeTfRecords(nodes: TfRecord[]): TfRecord[] {
  const seen = new Set<string>()
  const out: TfRecord[] = []
  nodes.forEach((node) => {
    const id = String(node.id)
    if (!id || seen.has(id)) {
      return
    }
    seen.add(id)
    out.push(node)
  })
  return out
}

/** 会话内拖拽覆盖自动布局；重新打开时仅依赖此函数计算位置 */
export function resolveLayoutCollisions(
  nodeIds: string[],
  sessionPositions: Record<string, { x: number; y: number }>,
  layout: Record<string, { x: number; y: number }>,
): Record<string, { x: number; y: number }> {
  const used = new Map<string, string>()
  const resolved: Record<string, { x: number; y: number }> = {}
  nodeIds.forEach((id, index) => {
    let pos =
      sessionPositions[id] ??
      layout[id] ??
      { x: 48 + index * (TF_NODE_W + 40), y: 48 + index * (TF_NODE_H + 36) }
    let key = `${Math.round(pos.x)}:${Math.round(pos.y)}`
    let attempts = 0
    while (used.has(key) && used.get(key) !== id && attempts < 32) {
      pos = { x: pos.x, y: pos.y + TF_NODE_H + 36 }
      key = `${Math.round(pos.x)}:${Math.round(pos.y)}`
      attempts += 1
    }
    used.set(key, id)
    resolved[id] = pos
  })
  return resolved
}

/**
 * 含开始/结束节点的分层布局：入口工序靠左，有向边决定层级，同层纵向展开。
 */
export function computeFlowLayout(tfNodes: TfRecord[], tfEdges: TfEdgeRecord[]) {
  const nodeIds = tfNodes.map((n) => String(n.id))
  const idSet = new Set(nodeIds)
  const nodeOrder: Record<string, number> = {}
  nodeIds.forEach((id, idx) => {
    nodeOrder[id] = idx
  })

  if (!nodeIds.length) {
    return {
      [LINE_TF_START_NODE_ID]: { x: 48, y: LINE_TF_EMPTY_TERMINAL_Y },
      [LINE_TF_END_NODE_ID]: {
        x: 48 + LINE_TF_EMPTY_TERMINAL_SPAN,
        y: LINE_TF_EMPTY_TERMINAL_Y,
      },
    }
  }

  const incoming = new Map<string, number>()
  const outgoing = new Map<string, number>()
  nodeIds.forEach((id) => {
    incoming.set(id, 0)
    outgoing.set(id, 0)
  })
  tfEdges.forEach((edge) => {
    const from = String(edge.fromTfId)
    const to = String(edge.toTfId)
    if (!idSet.has(from) || !idSet.has(to) || from === to) {
      return
    }
    outgoing.set(from, (outgoing.get(from) || 0) + 1)
    incoming.set(to, (incoming.get(to) || 0) + 1)
  })

  const START = LINE_TF_START_NODE_ID
  const END = LINE_TF_END_NODE_ID
  const adj: Record<string, string[]> = {}
  ;[START, ...nodeIds, END].forEach((id) => {
    adj[id] = []
  })

  nodeIds.forEach((id) => {
    if ((incoming.get(id) || 0) === 0) {
      adj[START].push(id)
    }
  })
  nodeIds.forEach((id) => {
    if ((outgoing.get(id) || 0) === 0) {
      adj[id].push(END)
    }
  })
  tfEdges.forEach((edge) => {
    const from = String(edge.fromTfId)
    const to = String(edge.toTfId)
    if (!idSet.has(from) || !idSet.has(to) || from === to) {
      return
    }
    adj[from].push(to)
  })

  const layerMap: Record<string, number> = { [START]: 0 }
  for (let round = 0; round <= nodeIds.length + 2; round += 1) {
    Object.keys(adj).forEach((from) => {
      if (layerMap[from] == null) {
        return
      }
      adj[from].forEach((to) => {
        layerMap[to] = Math.max(layerMap[to] ?? 0, layerMap[from] + 1)
      })
    })
  }

  nodeIds.forEach((id, idx) => {
    if (layerMap[id] == null) {
      layerMap[id] = 1 + idx
    }
  })
  if (layerMap[END] == null) {
    layerMap[END] = Math.max(...Object.values(layerMap), 0) + 1
  }

  const maxLayer = Math.max(...Object.values(layerMap))
  const layerToNodes: Record<number, string[]> = {}
  ;[START, ...nodeIds, END].forEach((id) => {
    const layer = layerMap[id] ?? 0
    if (!layerToNodes[layer]) {
      layerToNodes[layer] = []
    }
    layerToNodes[layer].push(id)
  })

  const padX = TF_NODE_W + 110
  const padY = TF_NODE_H + 48
  const top = 56
  const left = 48
  const pos: Record<string, { x: number; y: number }> = {}

  const stepNoOf = (id: string) =>
    tfNodes.find((node) => String(node.id) === id)?.stepNo ?? nodeOrder[id] ?? 0

  for (let layer = 0; layer <= maxLayer; layer += 1) {
    const arr = layerToNodes[layer] || []
    arr.sort((a, b) => {
      if (a === START) {
        return -1
      }
      if (b === START) {
        return 1
      }
      if (a === END) {
        return 1
      }
      if (b === END) {
        return -1
      }
      const stepDiff = stepNoOf(a) - stepNoOf(b)
      if (stepDiff !== 0) {
        return stepDiff
      }
      return (nodeOrder[a] || 0) - (nodeOrder[b] || 0)
    })
    const count = arr.length
    arr.forEach((id, index) => {
      const x = left + layer * padX
      const clusterHeight = Math.max(0, count - 1) * padY
      const y = top + index * padY - clusterHeight / 2 + 72
      pos[id] = { x, y }
    })
  }

  return pos
}

export function edgeKey(from: string, to: string) {
  return `${String(from)}\0${String(to)}`
}

export function buildFlowEdge(fromTfId: string, toTfId: string): Edge {
  const from = String(fromTfId)
  const to = String(toTfId)
  return {
    id: `e-${from}-${to}`,
    source: from,
    target: to,
    type: 'smoothstep',
    sourceHandle: 'right',
    targetHandle: 'left',
    animated: true,
    markerEnd: MarkerType.ArrowClosed,
    style: { ...LINE_TF_EDGE_STYLE },
  }
}

export function formatTfDisplayName(tf: Pick<TfRecord, 'name' | 'selfCode' | 'id'>): string {
  const name = tf.name?.trim()
  if (name) {
    return name
  }
  const code = tf.selfCode?.trim()
  if (code) {
    return code
  }
  return String(tf.id || '')
}

export function nextTfStepNo(nodes: TfRecord[]): number {
  const nums = nodes
    .map((node) => node.stepNo)
    .filter((value): value is number => value != null && !Number.isNaN(value))
  if (!nums.length) {
    return 10
  }
  return Math.max(...nums) + 10
}

export function joinTfResourceNames(
  items: Array<{ equipmentName?: string; equipmentCode?: string; toolingName?: string; toolingCode?: string }> | undefined,
  nameKey: 'equipmentName' | 'toolingName',
  codeKey: 'equipmentCode' | 'toolingCode',
): string {
  if (!items?.length) {
    return ''
  }
  return items
    .map((item) => item[nameKey] || item[codeKey] || '')
    .filter(Boolean)
    .join('、')
}

export function formatTfStepSummary(tf: TfRecord): string {
  const content = tf.stepContent?.trim()
  if (content) {
    return content.length > 32 ? `${content.slice(0, 32)}…` : content
  }
  const equip = joinTfResourceNames(tf.equipments, 'equipmentName', 'equipmentCode')
  if (equip) {
    return equip
  }
  return '双击编辑工序'
}

export function formatTfStepLabel(tf: TfRecord): string {
  const name = formatTfDisplayName(tf)
  if (tf.stepNo != null && tf.stepNo > 0) {
    return `${tf.stepNo} · ${name}`
  }
  return name
}

export function buildFlowNode(tf: TfRecord, position: { x: number; y: number }): Node {
  const id = String(tf.id)
  return {
    id,
    type: 'dag',
    position,
    sourcePosition: Position.Right,
    targetPosition: Position.Left,
    data: {
      title: formatTfStepLabel(tf),
      summary: formatTfStepSummary(tf),
    },
  }
}

export function buildTerminalFlowNode(
  type: 'start' | 'end',
  position: { x: number; y: number },
): Node {
  const id = type === 'start' ? LINE_TF_START_NODE_ID : LINE_TF_END_NODE_ID
  return {
    id,
    type: 'terminal',
    position,
    draggable: true,
    selectable: false,
    deletable: false,
    connectable: true,
    sourcePosition: Position.Right,
    targetPosition: Position.Left,
    data: {
      terminalType: type,
      label: type === 'start' ? '开始' : '结束',
      summary: type === 'start' ? '工序入口' : '工序出口',
    },
  }
}

function terminalPosition(
  layout: Record<string, { x: number; y: number }>,
  sessionPositions: Record<string, { x: number; y: number }>,
  terminalId: string,
  fallback: { x: number; y: number },
) {
  return sessionPositions[terminalId] ?? layout[terminalId] ?? fallback
}

export function buildFlowNodes(
  tfNodes: TfRecord[],
  tfEdges: TfEdgeRecord[],
  sessionPositions: Record<string, { x: number; y: number }> = {},
): Node[] {
  const layout = computeFlowLayout(tfNodes, tfEdges)
  const nodeIds = tfNodes.map((tf) => String(tf.id))
  const tfPositions = resolveLayoutCollisions(nodeIds, sessionPositions, layout)
  const tfFlowNodes = tfNodes.map((tf) => {
    const id = String(tf.id)
    return buildFlowNode(tf, tfPositions[id] ?? layout[id] ?? { x: 48, y: 56 })
  })
  const startPos = terminalPosition(
    layout,
    sessionPositions,
    LINE_TF_START_NODE_ID,
    { x: 48, y: LINE_TF_EMPTY_TERMINAL_Y },
  )
  const endPos = terminalPosition(
    layout,
    sessionPositions,
    LINE_TF_END_NODE_ID,
    {
      x: 48 + LINE_TF_EMPTY_TERMINAL_SPAN,
      y: LINE_TF_EMPTY_TERMINAL_Y,
    },
  )
  return [
    buildTerminalFlowNode('start', startPos),
    ...tfFlowNodes,
    buildTerminalFlowNode('end', endPos),
  ]
}

export function buildFlowEdges(tfEdges: TfEdgeRecord[], nodeById: Record<string, TfRecord>): Edge[] {
  const seen: Record<string, boolean> = {}
  const out: Edge[] = []
  tfEdges.forEach((e) => {
    const from = String(e.fromTfId)
    const to = String(e.toTfId)
    if (from === to) {
      return
    }
    const fromOk = isLineTfTerminalNodeId(from) || Boolean(nodeById[from])
    const toOk = isLineTfTerminalNodeId(to) || Boolean(nodeById[to])
    if (!fromOk || !toOk) {
      return
    }
    const k = edgeKey(from, to)
    if (seen[k]) {
      return
    }
    seen[k] = true
    out.push(buildFlowEdge(from, to))
  })
  return out
}

export function mergeFlowEdges(
  apiEdges: Edge[],
  draftEdges: Edge[],
  nodeById: Record<string, TfRecord>,
): Edge[] {
  const seen = new Set<string>()
  const out: Edge[] = []

  const append = (edge: Edge) => {
    const from = edge.source
    const to = edge.target
    if (!from || !to || from === to) {
      return
    }
    const fromOk = Boolean(nodeById[from]) || isLineTfTerminalNodeId(from)
    const toOk = Boolean(nodeById[to]) || isLineTfTerminalNodeId(to)
    if (!fromOk || !toOk) {
      return
    }
    const k = edgeKey(from, to)
    if (seen.has(k)) {
      return
    }
    seen.add(k)
    out.push(buildFlowEdge(from, to))
  }

  apiEdges.forEach(append)
  draftEdges.forEach(append)
  return out
}

export function captureNodePositions(
  positions: Record<string, { x: number; y: number }>,
  flowNodes: Node[],
) {
  flowNodes.forEach((node) => {
    positions[node.id] = { ...node.position }
  })
}

export function nextLocalNodePosition(positions: Record<string, { x: number; y: number }>) {
  const values = Object.values(positions)
  if (!values.length) {
    return { x: 120, y: 120 }
  }
  let maxX = values[0].x
  let maxY = values[0].y
  values.forEach((pos) => {
    maxX = Math.max(maxX, pos.x)
    maxY = Math.max(maxY, pos.y)
  })
  return { x: maxX + TF_NODE_W + 80, y: maxY }
}
