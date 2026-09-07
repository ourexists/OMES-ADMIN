<script setup lang="ts">
import { markRaw, nextTick, ref, watch } from 'vue'
import type { Connection, Edge, Node, NodeChange } from '@vue-flow/core'
import { ConnectionMode, MarkerType, VueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import LineTfDagNode from './LineTfDagNode.vue'
import LineTfTerminalNode from './LineTfTerminalNode.vue'
import LineTfFlowCanvasHost from './LineTfFlowCanvasHost.vue'
import { isLineTfTerminalNodeId } from '@/composables/lineTfFlowShared'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'

const nodes = defineModel<Node[]>('nodes', { required: true })
const edges = defineModel<Edge[]>('edges', { required: true })

const emit = defineEmits<{
  connect: [connection: Connection]
  connectAddNode: [payload: { sourceNodeId: string; position: { x: number; y: number } }]
  nodeDragStop: [nodes: Node[]]
  deleteNodes: [nodeIds: string[]]
  nodesChange: [changes: NodeChange[]]
  nodeDblClick: [nodeId: string]
}>()

const hostRef = ref<InstanceType<typeof LineTfFlowCanvasHost> | null>(null)
const nodeTypes = {
  dag: markRaw(LineTfDagNode),
  terminal: markRaw(LineTfTerminalNode),
}

watch(
  () => nodes.value.length,
  (count, prevCount) => {
    if (count > 0 && prevCount === 0) {
      scheduleFitView()
    }
  },
)

function scheduleFitView(force = false) {
  nextTick(() => {
    hostRef.value?.scheduleFitView(force)
  })
}

function onNodeDblClick(event: { node: Node }) {
  const nodeId = event.node?.id ? String(event.node.id) : ''
  if (!nodeId || isLineTfTerminalNodeId(nodeId)) {
    return
  }
  emit('nodeDblClick', nodeId)
}

function onEdgeClick(event: { edge: Edge }) {
  hostRef.value?.setLastActiveEdgeId(event.edge?.id ? String(event.edge.id) : null)
}

function onPaneClick() {
  hostRef.value?.clearLastActiveEdge()
}

function deleteSelected() {
  hostRef.value?.deleteSelected()
}

defineExpose({ deleteSelected, scheduleFitView })
</script>

<template>
  <VueFlow
    v-model:nodes="nodes"
    v-model:edges="edges"
    :node-types="nodeTypes"
    :connection-mode="ConnectionMode.Strict"
    :default-edge-options="{
      type: 'smoothstep',
      animated: true,
      markerEnd: MarkerType.ArrowClosed,
      style: { stroke: '#91a4b7', strokeWidth: 2 },
    }"
    :delete-key-code="['Delete', 'Backspace']"
    :connection-line-style="{ stroke: '#111827', strokeWidth: 2.5, strokeDasharray: '5 5' }"
    :min-zoom="0.2"
    :max-zoom="2"
    :pan-on-drag="[1, 2]"
    pan-on-scroll
    zoom-on-scroll
    nodes-draggable
    nodes-connectable
    elements-selectable
    elevate-edges-on-select
    class="line-tf-flow-canvas"
    @pane-ready="scheduleFitView()"
    @nodes-initialized="scheduleFitView()"
    @node-double-click="onNodeDblClick"
    @nodes-change="(changes) => emit('nodesChange', changes)"
    @edge-click="onEdgeClick"
    @pane-click="onPaneClick"
  >
    <LineTfFlowCanvasHost
      ref="hostRef"
      @connect="(connection) => emit('connect', connection)"
      @connect-add-node="(payload) => emit('connectAddNode', payload)"
      @node-drag-stop="(value) => emit('nodeDragStop', value)"
      @nodes-change="(value) => emit('nodesChange', value)"
    />
    <Background :gap="20" :size="1" pattern-color="rgba(145, 164, 183, 0.22)" />
  </VueFlow>
</template>
