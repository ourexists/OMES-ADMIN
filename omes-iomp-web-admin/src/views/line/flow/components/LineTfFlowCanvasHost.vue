<script setup lang="ts">
import type { Connection, Node } from '@vue-flow/core'
import { useVueFlow } from '@vue-flow/core'
import { Controls } from '@vue-flow/controls'
import { message } from 'ant-design-vue'
import { TF_NODE_H, TF_NODE_W, isLineTfTerminalNodeId } from '@/composables/lineTfFlowShared'
import '@vue-flow/controls/dist/style.css'

const emit = defineEmits<{
  connect: [connection: Connection]
  connectAddNode: [payload: { sourceNodeId: string; position: { x: number; y: number } }]
  nodeDragStop: [nodes: Node[]]
  deleteNodes: [nodeIds: string[]]
}>()

const {
  fitView,
  screenToFlowCoordinate,
  getSelectedNodes,
  getSelectedEdges,
  removeNodes,
  removeEdges,
  onConnect,
  onConnectStart,
  onConnectEnd,
  onNodeDragStop,
} = useVueFlow()

let lastActiveEdgeId: string | null = null
let connectSource: { nodeId: string; handleType?: string } | null = null
let connectHandled = false
let initialFitDone = false

/** 初始/自适应视野：限制最大缩放，避免节点过少时占满画布 */
const FIT_VIEW_PADDING = 0.52
const FIT_VIEW_MAX_ZOOM = 0.76

onConnect((connection) => {
  connectHandled = true
  emit('connect', connection)
})

onConnectStart(({ nodeId, handleType }) => {
  if (!nodeId) {
    return
  }
  connectSource = { nodeId: String(nodeId), handleType }
  connectHandled = false
})

onConnectEnd((event) => {
  const source = connectSource
  connectSource = null

  if (!source || connectHandled || source.handleType !== 'source') {
    connectHandled = false
    return
  }

  if (!event || !('clientX' in event)) {
    connectHandled = false
    return
  }

  const target = event.target as Element | null
  if (target?.closest('.vue-flow__node') || target?.closest('.vue-flow__handle')) {
    connectHandled = false
    return
  }

  // 仅在画布区域内松手才新增工序，避免点到工具栏按钮时误创建节点
  if (!target?.closest('.vue-flow__pane')) {
    connectHandled = false
    return
  }

  const flowPos = screenToFlowCoordinate({ x: event.clientX, y: event.clientY })
  emit('connectAddNode', {
    sourceNodeId: source.nodeId,
    position: {
      x: flowPos.x - TF_NODE_W / 2,
      y: flowPos.y - TF_NODE_H / 2,
    },
  })
  connectHandled = false
})

onNodeDragStop(({ nodes }) => {
  if (nodes.length) {
    emit('nodeDragStop', nodes)
  }
})

function setLastActiveEdgeId(id: string | null) {
  lastActiveEdgeId = id
}

function clearLastActiveEdge() {
  lastActiveEdgeId = null
}

function fitGraphView(force = false) {
  if (initialFitDone && !force) {
    return
  }
  try {
    fitView({
      padding: FIT_VIEW_PADDING,
      maxZoom: FIT_VIEW_MAX_ZOOM,
      minZoom: 0.2,
      duration: force ? 200 : 0,
    })
    initialFitDone = true
  } catch {
    /* ignore */
  }
}

function scheduleFitView(force = false) {
  requestAnimationFrame(() => {
    fitGraphView(force)
  })
}

function deleteSelected() {
  const selectedNodes = getSelectedNodes.value.filter((node) => !isLineTfTerminalNodeId(String(node.id)))
  const selectedEdges = getSelectedEdges.value
  if (selectedNodes.length || selectedEdges.length) {
    const nodeIds = selectedNodes.map((node) => String(node.id))
    if (selectedNodes.length) {
      removeNodes(nodeIds)
    }
    if (selectedEdges.length) {
      removeEdges(selectedEdges.map((edge) => edge.id))
    }
    lastActiveEdgeId = null
    return
  }
  if (lastActiveEdgeId) {
    removeEdges([lastActiveEdgeId])
    lastActiveEdgeId = null
    return
  }
  message.warning('请先选中要删除的工序或连线')
}

defineExpose({
  deleteSelected,
  setLastActiveEdgeId,
  clearLastActiveEdge,
  scheduleFitView,
  fitGraphView,
})
</script>

<template>
  <Controls position="bottom-right" :show-interactive="false" />
</template>
