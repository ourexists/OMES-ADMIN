import { type MaybeRefOrGetter, shallowRef, toValue, watch } from 'vue'
import type { Edge, Node } from '@vue-flow/core'
import { MarkerType } from '@vue-flow/core'
import type { TfEdgeRecord, TfRecord } from '@/types/line'
import type { MpsRuntimeTf, MpsTfRecord, MpsTfRuntimeTone } from '@/types/mps'
import { MPS_STATUS, MPS_TF_STATUS } from '@/types/mps'
import {
  LINE_TF_END_NODE_ID,
  LINE_TF_START_NODE_ID,
  buildFlowEdges,
  buildFlowNodes,
  buildNodeById,
  formatTfStepLabel,
  formatTfStepSummary,
  isLineTfTerminalNodeId,
} from '@/composables/lineTfFlowShared'

const GREEN_EDGE = { stroke: '#52c41a', strokeWidth: 2.5 }
const MUTED_EDGE = { stroke: '#91a4b7', strokeWidth: 2 }

export function mpsTfNodeId(tf: Pick<MpsTfRecord, 'selfCode' | 'id'>): string {
  return String(tf.selfCode || tf.id || '')
}

export function mpsTfTone(status?: number): MpsTfRuntimeTone {
  if (status === MPS_TF_STATUS.RUNNING) {
    return 'running'
  }
  if (status === MPS_TF_STATUS.DONE) {
    return 'done'
  }
  if (status === MPS_TF_STATUS.STOP) {
    return 'stop'
  }
  if (status === MPS_TF_STATUS.ERROR) {
    return 'error'
  }
  return 'pending'
}

export function isMpsTfReached(status?: number): boolean {
  return status === MPS_TF_STATUS.RUNNING || status === MPS_TF_STATUS.DONE
}

function displayTfStatus(status?: number, mpsStatus?: number): number {
  const value = status ?? MPS_TF_STATUS.PENDING
  if (value === MPS_TF_STATUS.ERROR || value === MPS_TF_STATUS.STOP) {
    return value
  }
  if (mpsStatus === MPS_STATUS.COMPLETE && value === MPS_TF_STATUS.PENDING) {
    return MPS_TF_STATUS.DONE
  }
  return value
}

function indexByCode<T extends { selfCode?: string }>(list: T[]): Map<string, T> {
  const map = new Map<string, T>()
  list.forEach((item) => {
    const code = item.selfCode?.trim()
    if (code && !map.has(code)) {
      map.set(code, item)
    }
  })
  return map
}

function parsePreCodes(pre?: string): string[] {
  return String(pre || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function mergeRuntimeTf(mps: MpsTfRecord, lineTf?: TfRecord): MpsRuntimeTf {
  const nodeId = mpsTfNodeId(mps)
  return {
    ...mps,
    nodeId,
    name: mps.name || lineTf?.name,
    selfCode: mps.selfCode || lineTf?.selfCode,
    stepNo: lineTf?.stepNo,
    stepContent: lineTf?.stepContent,
    stepScript: lineTf?.stepScript,
    stepEngineConfig: lineTf?.stepEngineConfig,
    equipments: lineTf?.equipments,
    toolings: lineTf?.toolings,
  }
}

function buildRuntimeEdges(
  runtimes: MpsRuntimeTf[],
  lineTfs: TfRecord[],
  lineEdges: TfEdgeRecord[],
): TfEdgeRecord[] {
  const runtimeByCode = indexByCode(runtimes)
  const lineById = new Map<string, TfRecord>()
  lineTfs.forEach((tf) => {
    if (tf.id) {
      lineById.set(String(tf.id), tf)
    }
  })

  const seen = new Set<string>()
  const out: TfEdgeRecord[] = []

  const push = (from: string, to: string) => {
    if (!from || !to || from === to) {
      return
    }
    const key = `${from}\0${to}`
    if (seen.has(key)) {
      return
    }
    seen.add(key)
    out.push({ fromTfId: from, toTfId: to })
  }

  lineEdges.forEach((edge) => {
    const fromRaw = String(edge.fromTfId || '')
    const toRaw = String(edge.toTfId || '')
    const from = isLineTfTerminalNodeId(fromRaw)
      ? fromRaw
      : mpsTfNodeId(lineById.get(fromRaw) || { selfCode: fromRaw })
    const to = isLineTfTerminalNodeId(toRaw)
      ? toRaw
      : mpsTfNodeId(lineById.get(toRaw) || { selfCode: toRaw })
    const fromOk = isLineTfTerminalNodeId(from) || runtimeByCode.has(from)
    const toOk = isLineTfTerminalNodeId(to) || runtimeByCode.has(to)
    if (fromOk && toOk) {
      push(from, to)
    }
  })

  if (!out.length) {
    runtimes.forEach((tf) => {
      parsePreCodes(tf.pre).forEach((pre) => {
        if (runtimeByCode.has(pre)) {
          push(pre, tf.nodeId)
        }
      })
    })
  }

  if (!out.length && runtimes.length > 1) {
    const ordered = [...runtimes].sort((a, b) => {
      const stepDiff = (a.stepNo ?? Number.MAX_SAFE_INTEGER) - (b.stepNo ?? Number.MAX_SAFE_INTEGER)
      if (stepDiff !== 0) {
        return stepDiff
      }
      return String(a.selfCode || a.id || '').localeCompare(String(b.selfCode || b.id || ''))
    })
    for (let i = 0; i < ordered.length - 1; i += 1) {
      push(ordered[i].nodeId, ordered[i + 1].nodeId)
    }
  }

  const incoming = new Map<string, number>()
  const outgoing = new Map<string, number>()
  runtimes.forEach((tf) => {
    incoming.set(tf.nodeId, 0)
    outgoing.set(tf.nodeId, 0)
  })
  out.forEach((edge) => {
    if (!isLineTfTerminalNodeId(edge.toTfId)) {
      incoming.set(edge.toTfId, (incoming.get(edge.toTfId) || 0) + 1)
    }
    if (!isLineTfTerminalNodeId(edge.fromTfId)) {
      outgoing.set(edge.fromTfId, (outgoing.get(edge.fromTfId) || 0) + 1)
    }
  })
  runtimes.forEach((tf) => {
    if ((incoming.get(tf.nodeId) || 0) === 0) {
      push(LINE_TF_START_NODE_ID, tf.nodeId)
    }
    if ((outgoing.get(tf.nodeId) || 0) === 0) {
      push(tf.nodeId, LINE_TF_END_NODE_ID)
    }
  })

  return out
}

function nodeReached(
  id: string,
  statusById: Record<string, number | undefined>,
  anyStarted: boolean,
  allDone: boolean,
): boolean {
  if (id === LINE_TF_START_NODE_ID) {
    return anyStarted || allDone
  }
  if (id === LINE_TF_END_NODE_ID) {
    return allDone
  }
  return isMpsTfReached(statusById[id])
}

function runtimeSummary(tf: MpsRuntimeTf): string {
  const content = formatTfStepSummary({
    id: tf.nodeId,
    name: tf.name,
    selfCode: tf.selfCode,
    stepNo: tf.stepNo,
    stepContent: tf.stepContent,
    equipments: tf.equipments,
    toolings: tf.toolings,
  })
  if (content && content !== '双击编辑工序') {
    return content
  }
  if (tf.startTime) {
    return tf.startTime
  }
  return tf.statusDesc || '待执行'
}

export function buildMpsTfRuntimeGraph(
  tfs: MpsTfRecord[],
  lineTfs: TfRecord[],
  lineEdges: TfEdgeRecord[],
  canStart: (tf: MpsTfRecord) => boolean,
  mpsStatus?: number,
): { nodes: Node[]; edges: Edge[]; runtimeById: Record<string, MpsRuntimeTf> } {
  const lineByCode = indexByCode(lineTfs)
  const runtimes = tfs
    .map((tf) => mergeRuntimeTf(tf, lineByCode.get(tf.selfCode?.trim() || '')))
    .filter((tf) => Boolean(tf.nodeId))
  const runtimeById: Record<string, MpsRuntimeTf> = {}
  runtimes.forEach((tf) => {
    runtimeById[tf.nodeId] = tf
  })

  const layoutNodes: TfRecord[] = runtimes.map((tf) => ({
    id: tf.nodeId,
    selfCode: tf.selfCode,
    name: tf.name,
    stepNo: tf.stepNo,
    stepContent: tf.stepContent,
    equipments: tf.equipments,
    toolings: tf.toolings,
  }))
  const layoutEdges = buildRuntimeEdges(runtimes, lineTfs, lineEdges)
  const statusById: Record<string, number | undefined> = {}
  runtimes.forEach((tf) => {
    statusById[tf.nodeId] = displayTfStatus(tf.status, mpsStatus)
  })
  const anyStarted = runtimes.some((tf) => (statusById[tf.nodeId] || 0) > MPS_TF_STATUS.PENDING)
  const allDone = runtimes.length > 0 && runtimes.every((tf) => statusById[tf.nodeId] === MPS_TF_STATUS.DONE)

  const nodes = buildFlowNodes(layoutNodes, layoutEdges).map((node) => {
    if (isLineTfTerminalNodeId(node.id)) {
      const isStart = node.id === LINE_TF_START_NODE_ID
      const reached = isStart ? anyStarted || allDone : allDone
      return {
        ...node,
        type: 'runtimeTerminal',
        draggable: false,
        connectable: false,
        selectable: false,
        data: {
          ...node.data,
          tone: reached ? 'done' : 'pending',
        },
      }
    }
    const tf = runtimeById[node.id]
    const tone = mpsTfTone(statusById[node.id])
    const layoutNode = layoutNodes.find((item) => item.id === node.id)
    return {
      ...node,
      type: 'runtime',
      draggable: false,
      connectable: false,
      data: {
        title: layoutNode ? formatTfStepLabel(layoutNode) : node.data.title,
        summary: tf ? runtimeSummary(tf) : node.data.summary,
        status: tf?.status,
        statusDesc: tf?.statusDesc || '待执行',
        canStart: tf ? canStart(tf) : false,
        tone,
      },
    }
  })

  const nodeById = buildNodeById(layoutNodes)
  const edges = buildFlowEdges(layoutEdges, nodeById).map((edge) => {
    const sourceReached = nodeReached(edge.source, statusById, anyStarted, allDone)
    const targetDone = edge.target === LINE_TF_END_NODE_ID
      ? allDone
      : statusById[edge.target] === MPS_TF_STATUS.DONE
    const green = sourceReached
    return {
      ...edge,
      animated: green && !targetDone,
      selectable: false,
      updatable: false,
      markerEnd: MarkerType.ArrowClosed,
      style: { ...(green ? GREEN_EDGE : MUTED_EDGE) },
    }
  })

  return { nodes, edges, runtimeById }
}

export function useMpsTfRuntimeFlow(
  tfs: MaybeRefOrGetter<MpsTfRecord[] | undefined>,
  lineTfs: MaybeRefOrGetter<TfRecord[] | undefined>,
  lineEdges: MaybeRefOrGetter<TfEdgeRecord[] | undefined>,
  canStart: (tf: MpsTfRecord) => boolean,
  mpsStatus: MaybeRefOrGetter<number | undefined> = undefined,
) {
  const flowNodes = shallowRef<Node[]>([])
  const flowEdges = shallowRef<Edge[]>([])
  const runtimeById = shallowRef<Record<string, MpsRuntimeTf>>({})

  watch(
    () => [toValue(tfs), toValue(lineTfs), toValue(lineEdges), toValue(mpsStatus)] as const,
    () => {
      const graph = buildMpsTfRuntimeGraph(
        toValue(tfs) || [],
        toValue(lineTfs) || [],
        toValue(lineEdges) || [],
        canStart,
        toValue(mpsStatus),
      )
      flowNodes.value = graph.nodes
      flowEdges.value = graph.edges
      runtimeById.value = graph.runtimeById
    },
    { immediate: true, deep: true },
  )

  return {
    flowNodes,
    flowEdges,
    runtimeById,
  }
}
