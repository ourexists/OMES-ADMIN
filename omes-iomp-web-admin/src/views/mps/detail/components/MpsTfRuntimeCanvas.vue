<script setup lang="ts">
import { markRaw, nextTick, ref, watch } from 'vue'
import type { Edge, Node } from '@vue-flow/core'
import { ConnectionMode, MarkerType, VueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { isLineTfTerminalNodeId } from '@/composables/lineTfFlowShared'
import MpsTfRuntimeCanvasHost from './MpsTfRuntimeCanvasHost.vue'
import MpsTfRuntimeNode from './MpsTfRuntimeNode.vue'
import MpsTfRuntimeTerminal from './MpsTfRuntimeTerminal.vue'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'

const nodes = defineModel<Node[]>('nodes', { required: true })
const edges = defineModel<Edge[]>('edges', { required: true })

const emit = defineEmits<{
  nodeClick: [nodeId: string]
}>()

const hostRef = ref<InstanceType<typeof MpsTfRuntimeCanvasHost> | null>(null)
const nodeTypes = {
  runtime: markRaw(MpsTfRuntimeNode),
  runtimeTerminal: markRaw(MpsTfRuntimeTerminal),
}

function scheduleFitView(force = false) {
  nextTick(() => {
    hostRef.value?.scheduleFitView(force)
  })
}

watch(
  () => nodes.value.length,
  (count, prevCount) => {
    if (count > 0 && (prevCount === 0 || prevCount == null)) {
      scheduleFitView(true)
    }
  },
)

function onNodeClick(event: { node: Node }) {
  const nodeId = event.node?.id ? String(event.node.id) : ''
  if (!nodeId || isLineTfTerminalNodeId(nodeId)) {
    return
  }
  emit('nodeClick', nodeId)
}

defineExpose({ scheduleFitView })
</script>

<template>
  <VueFlow
    v-model:nodes="nodes"
    v-model:edges="edges"
    :node-types="nodeTypes"
    :connection-mode="ConnectionMode.Strict"
    :default-edge-options="{
      type: 'smoothstep',
      animated: false,
      markerEnd: MarkerType.ArrowClosed,
      style: { stroke: '#91a4b7', strokeWidth: 2 },
    }"
    :delete-key-code="null"
    :min-zoom="0.2"
    :max-zoom="2"
    :pan-on-drag="[1, 2]"
    pan-on-scroll
    zoom-on-scroll
    :nodes-draggable="false"
    :nodes-connectable="false"
    elements-selectable
    class="mps-tf-runtime-canvas"
    @pane-ready="scheduleFitView()"
    @nodes-initialized="scheduleFitView()"
    @node-click="onNodeClick"
  >
    <MpsTfRuntimeCanvasHost ref="hostRef" />
    <Background :gap="20" :size="1" pattern-color="rgba(145, 164, 183, 0.22)" />
  </VueFlow>
</template>
