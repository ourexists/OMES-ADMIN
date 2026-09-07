import { ref, shallowRef } from 'vue'
import type { Connection, Edge, Node, NodeChange } from '@vue-flow/core'
import { deleteTfs, fetchTfByLineId, fetchTfEdgesByLineId, saveTf, saveTfEdgesByLineId } from '@/api/line'
import type { TfEdgeRecord, TfRecord } from '@/types/line'
import { message } from 'ant-design-vue'
import { i18n } from '@/i18n'
import {
  buildFlowEdge,
  buildFlowEdges,
  buildFlowNodes,
  buildNodeById,
  captureNodePositions,
  dedupeTfRecords,
  edgeKey,
  isLineTfTerminalNodeId,
  mergeFlowEdges,
  nextLocalNodePosition,
  nextTfStepNo,
} from '@/composables/lineTfFlowShared'

function createLocalTfRecord(lineId: string, nodes: TfRecord[]): TfRecord {
  const now = Date.now()
  const stepNo = nextTfStepNo(nodes)
  return {
    id: `tmp_${now}`,
    lineId,
    name: `工序${Math.max(1, Math.round(stepNo / 10))}`,
    selfCode: `TF${String(now).slice(-6)}`,
    stepNo,
    stepContent: '',
    equipments: [],
    toolings: [],
    __localOnly: true,
  }
}

export function useLineTfFlowGraph(lineId: () => string) {
  const loading = ref(false)
  const nodes = shallowRef<TfRecord[]>([])
  const edges = shallowRef<TfEdgeRecord[]>([])
  const flowNodes = shallowRef<Node[]>([])
  const flowEdges = shallowRef<Edge[]>([])
  const nodePositions = ref<Record<string, { x: number; y: number }>>({})

  function syncFlowGraph(options?: { preserveDraftEdges?: boolean }) {
    const nodeById = buildNodeById(nodes.value)
    const prevFlowEdges = options?.preserveDraftEdges === false ? [] : flowEdges.value
    flowNodes.value = buildFlowNodes(nodes.value, edges.value, nodePositions.value)
    const apiEdges = buildFlowEdges(edges.value, nodeById)
    flowEdges.value = mergeFlowEdges(apiEdges, prevFlowEdges, nodeById)
  }

  function getNodeDraftById(nodeId: string): TfRecord | null {
    if (isLineTfTerminalNodeId(nodeId)) {
      return null
    }
    const node = buildNodeById(nodes.value)[String(nodeId)]
    return node ? { ...node } : null
  }

  function applyNodeDraft(draft: TfRecord) {
    captureNodePositions(nodePositions.value, flowNodes.value)
    const list = [...nodes.value]
    const targetId = String(draft.id)
    const idx = list.findIndex((n) => String(n.id) === targetId)
    if (idx >= 0) {
      list[idx] = { ...list[idx], ...draft, __dirtyLocal: true }
    } else {
      list.push({ ...draft, __localOnly: true, __dirtyLocal: true })
    }
    nodes.value = list
    syncFlowGraph()
  }

  function addLocalNode() {
    captureNodePositions(nodePositions.value, flowNodes.value)
    const localNode = createLocalTfRecord(lineId(), nodes.value)
    nodePositions.value[String(localNode.id)] = nextLocalNodePosition(nodePositions.value)
    nodes.value = [...nodes.value, localNode]
    syncFlowGraph()
    message.info('已新增本地工序（保存工序信息后将写入数据库）')
  }

  function addLocalNodeFromConnect(sourceNodeId: string, position: { x: number; y: number }) {
    const sid = String(sourceNodeId)
    const nodeById = buildNodeById(nodes.value)
    if (!nodeById[sid] && !isLineTfTerminalNodeId(sid)) {
      return
    }
    captureNodePositions(nodePositions.value, flowNodes.value)
    const localNode = createLocalTfRecord(lineId(), nodes.value)
    const id = String(localNode.id)
    nodePositions.value[id] = position
    nodes.value = [...nodes.value, localNode]

    const k = edgeKey(sid, id)
    if (!flowEdges.value.some((e) => edgeKey(e.source, e.target) === k)) {
      flowEdges.value = [...flowEdges.value, buildFlowEdge(sid, id)]
    }
    syncFlowGraph({ preserveDraftEdges: true })
    message.info('已通过连线新增本地工序（保存工序信息后将写入数据库）')
  }

  function onNodeDragStop(draggedNodes: Node[]) {
    draggedNodes.forEach((node) => {
      nodePositions.value[node.id] = { ...node.position }
    })
  }

  function removeTfNodesLocal(nodeIds: string[]) {
    const removableIds = nodeIds.filter((id) => !isLineTfTerminalNodeId(id))
    if (!removableIds.length) {
      return
    }
    const idSet = new Set(removableIds.map(String))
    captureNodePositions(nodePositions.value, flowNodes.value)
    removableIds.forEach((id) => {
      delete nodePositions.value[String(id)]
    })
    nodes.value = nodes.value.filter((node) => !idSet.has(String(node.id)))
    edges.value = edges.value.filter(
      (edge) => !idSet.has(String(edge.fromTfId)) && !idSet.has(String(edge.toTfId)),
    )
    flowEdges.value = flowEdges.value.filter(
      (edge) => !idSet.has(edge.source) && !idSet.has(edge.target),
    )
  }

  function collectPersistedNodeIds(nodeIds: string[]): string[] {
    const nodeById = buildNodeById(nodes.value)
    return nodeIds
      .filter((id) => !isLineTfTerminalNodeId(id))
      .filter((id) => {
        const node = nodeById[String(id)]
        return Boolean(node && !node.__localOnly && !String(id).startsWith('tmp_'))
      })
  }

  async function removeTfNodes(nodeIds: string[]) {
    const removableIds = nodeIds.filter((id) => !isLineTfTerminalNodeId(id))
    if (!removableIds.length) {
      return
    }
    const persistedIds = collectPersistedNodeIds(removableIds)
    removeTfNodesLocal(removableIds)
    if (persistedIds.length) {
      await deleteTfs(persistedIds)
    }
  }

  /** 以画布当前可见工序为准，清理已从画布删除但未同步的数据 */
  function syncNodesFromFlowCanvas() {
    captureNodePositions(nodePositions.value, flowNodes.value)
    const visibleTfIds = new Set(
      flowNodes.value
        .map((node) => String(node.id))
        .filter((id) => !isLineTfTerminalNodeId(id)),
    )
    const staleIds = nodes.value
      .map((node) => String(node.id))
      .filter((id) => !visibleTfIds.has(id))
    if (staleIds.length) {
      removeTfNodesLocal(staleIds)
    }
  }

  function onFlowNodesChange(changes: NodeChange[]) {
    const removedIds = changes
      .filter((change) => change.type === 'remove')
      .map((change) => String(change.id))
      .filter((id) => !isLineTfTerminalNodeId(id))

    if (!removedIds.length) {
      return
    }

    const persistedIds = collectPersistedNodeIds(removedIds)
    removeTfNodesLocal(removedIds)
    if (persistedIds.length) {
      void deleteTfs(persistedIds)
    }
  }

  function onDeleteNodes(nodeIds: string[]) {
    void removeTfNodes(nodeIds)
  }

  function remapNodeId(oldId: string, newId: string) {
    const from = String(oldId)
    const to = String(newId)
    if (from === to) {
      return
    }
    if (nodePositions.value[from]) {
      nodePositions.value[to] = nodePositions.value[from]
      delete nodePositions.value[from]
    }
    flowEdges.value = flowEdges.value.map((edge) => {
      const source = edge.source === from ? to : edge.source
      const target = edge.target === from ? to : edge.target
      return {
        ...edge,
        id: `e-${source}-${target}`,
        source,
        target,
      }
    })
  }

  function hasUnpersistedNodes() {
    return nodes.value.some(
      (node) => node.__localOnly || String(node.id).startsWith('tmp_'),
    )
  }

  function buildSavePayload(draft: TfRecord): Partial<TfRecord> {
    const oldId = String(draft.id || '')
    const payload: Partial<TfRecord> = {
      lineId: draft.lineId || lineId(),
      name: draft.name?.trim(),
      selfCode: draft.selfCode?.trim(),
      stepNo: draft.stepNo,
      stepContent: draft.stepContent?.trim() || undefined,
      stepScript: draft.stepScript?.trim() || undefined,
      stepEngineConfig: draft.stepEngineConfig?.trim() || undefined,
      equipments: draft.equipments,
      toolings: draft.toolings,
    }
    if (oldId && !oldId.startsWith('tmp_')) {
      payload.id = oldId
    }
    return payload
  }

  async function saveNodeDraft(draft: TfRecord) {
    const oldId = String(draft.id || '')
    captureNodePositions(nodePositions.value, flowNodes.value)
    await saveTf(buildSavePayload(draft))
    const list = (await fetchTfByLineId(lineId())) || []
    const saved = list.find(
      (node) => node.selfCode === draft.selfCode?.trim() && node.stepNo === draft.stepNo,
    )
    if (saved && oldId.startsWith('tmp_')) {
      remapNodeId(oldId, String(saved.id))
    }
    nodes.value = dedupeTfRecords(list)
    syncFlowGraph({ preserveDraftEdges: true })
    message.success(i18n.global.t('lineFlowPage.saveSuccess'))
  }

  function isPersistedNodeId(id: string) {
    const node = buildNodeById(nodes.value)[String(id)]
    return Boolean(node && !node.__localOnly && !String(id).startsWith('tmp_'))
  }

  function collectEdgesFromFlow(currentEdges: Edge[]): TfEdgeRecord[] {
    const nodeById = buildNodeById(nodes.value)
    const pairSeen: Record<string, boolean> = {}
    const out: TfEdgeRecord[] = []

    const appendEdge = (fromTfId: string, toTfId: string) => {
      const k = edgeKey(fromTfId, toTfId)
      if (pairSeen[k]) {
        return
      }
      pairSeen[k] = true
      out.push({ fromTfId, toTfId })
    }

    const appendTfEdge = (fromTfId: string, toTfId: string) => {
      if (!isPersistedNodeId(fromTfId) || !isPersistedNodeId(toTfId)) {
        return
      }
      appendEdge(fromTfId, toTfId)
    }

    currentEdges.forEach((edge) => {
      const sid = edge.source
      const tid = edge.target
      if (!sid || !tid || sid === tid) {
        return
      }
      const sidTerminal = isLineTfTerminalNodeId(sid)
      const tidTerminal = isLineTfTerminalNodeId(tid)
      if (sidTerminal && tidTerminal) {
        return
      }
      if (sidTerminal) {
        if (!nodeById[tid] || !isPersistedNodeId(tid)) {
          return
        }
        appendEdge(sid, tid)
        return
      }
      if (tidTerminal) {
        if (!nodeById[sid] || !isPersistedNodeId(sid)) {
          return
        }
        appendEdge(sid, tid)
        return
      }
      if (!nodeById[sid] || !nodeById[tid]) {
        return
      }
      appendTfEdge(sid, tid)
    })

    return out
  }

  function onConnect(connection: Connection) {
    const sid = connection.source
    const tid = connection.target
    if (!sid || !tid || sid === tid) {
      return
    }
    const nodeById = buildNodeById(nodes.value)
    const sourceOk = Boolean(nodeById[sid]) || isLineTfTerminalNodeId(sid)
    const targetOk = Boolean(nodeById[tid]) || isLineTfTerminalNodeId(tid)
    if (!sourceOk || !targetOk) {
      return
    }
    const k = edgeKey(sid, tid)
    if (flowEdges.value.some((e) => edgeKey(e.source, e.target) === k)) {
      return
    }
    flowEdges.value = [...flowEdges.value, buildFlowEdge(sid, tid)]
  }

  async function saveEdges() {
    const id = lineId()
    if (!id) {
      return
    }
    syncNodesFromFlowCanvas()
    if (hasUnpersistedNodes()) {
      message.warning(i18n.global.t('lineFlowPage.saveNodesFirst'))
      return
    }
    const persistable = collectEdgesFromFlow(flowEdges.value)
    await saveTfEdgesByLineId(id, persistable)
    message.success(i18n.global.t('lineFlowPage.saveEdgesSuccess'))
    nodePositions.value = {}
    await reloadAll()
  }

  async function reloadAll() {
    const id = lineId()
    if (!id) {
      return
    }
    loading.value = true
    try {
      nodePositions.value = {}
      nodes.value = dedupeTfRecords((await fetchTfByLineId(id)) || [])
      await reloadEdges(false)
    } finally {
      loading.value = false
    }
  }

  async function reloadEdges(preserveDraftEdges = false) {
    const id = lineId()
    if (!id) {
      return
    }
    let list = (await fetchTfEdgesByLineId(id)) || []
    const nodeById = buildNodeById(nodes.value)
    const seen: Record<string, boolean> = {}
    list = list.filter((e) => {
      const from = String(e.fromTfId)
      const to = String(e.toTfId)
      if (from === to) {
        return false
      }
      const fromOk = isLineTfTerminalNodeId(from) || Boolean(nodeById[from])
      const toOk = isLineTfTerminalNodeId(to) || Boolean(nodeById[to])
      if (!fromOk || !toOk) {
        return false
      }
      const k = edgeKey(from, to)
      if (seen[k]) {
        return false
      }
      seen[k] = true
      return true
    })
    edges.value = list
    syncFlowGraph({ preserveDraftEdges })
  }

  syncFlowGraph()

  return {
    loading,
    nodes,
    flowNodes,
    flowEdges,
    reloadAll,
    reloadEdges,
    addLocalNode,
    addLocalNodeFromConnect,
    onNodeDragStop,
    removeTfNodes,
    onFlowNodesChange,
    onDeleteNodes,
    saveEdges,
    onConnect,
    getNodeDraftById,
    applyNodeDraft,
    saveNodeDraft,
    syncFlowGraph,
  }
}
