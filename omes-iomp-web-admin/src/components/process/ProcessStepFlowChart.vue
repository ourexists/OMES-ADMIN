<template>
  <div class="flow-chart-wrap">
    <div v-if="!readonly" class="flow-toolbar">
      <a-space wrap>
        <a-button size="small" @click="addNode('drive')">+ 驱动条件</a-button>
        <a-button size="small" @click="addNode('action')">+ 执行动作</a-button>
        <a-button size="small" @click="addNode('complete')">+ 完成动作</a-button>
        <a-button size="small" @click="addNode('exception')">+ 异常分支</a-button>
        <a-button size="small" danger :disabled="!canRemoveSelectedEdge" @click="removeSelectedEdge">删除选中连线</a-button>
        <a-button size="small" danger :disabled="!canRemoveSelected" @click="removeSelectedNode">删除选中节点</a-button>
      </a-space>
      <span class="flow-hint">点击连线选中后可删除；须包含开始与结束节点，驱动段可通过任意连线触达</span>
    </div>

    <div class="flow-shell">
      <div class="flow-board">
        <VueFlow
            class="process-vue-flow"
            :nodes="flowNodes"
            :edges="flowEdges"
            :min-zoom="0.35"
            :max-zoom="1.4"
            :default-viewport="{ x: 0, y: 120, zoom: 0.95 }"
            :nodes-draggable="!readonly"
            :nodes-connectable="!readonly"
            :elements-selectable="true"
            :edges-focusable="!readonly"
            :nodes-focusable="!readonly"
            :pan-on-drag="true"
            :zoom-on-scroll="true"
            :zoom-on-pinch="true"
            :prevent-scrolling="true"
            tabindex="0"
            @connect="onConnect"
            @node-click="onNodeClick"
            @node-drag-stop="onNodeDragStop"
            @edge-click="onEdgeClick"
            @keydown.delete.prevent="removeSelectedElement"
            @keydown.backspace.prevent="removeSelectedElement"
        >
          <template #node-processStep="{ id, data }">
            <div
                class="flow-node"
                :class="[
                  `tone-${data.tone}`,
                  { 'is-selected': selectedId === id, 'is-readonly': data.readonly }
                ]"
            >
              <template v-if="data.nodeType === 'start'">
                <Handle id="right" type="source" :position="Position.Right" />
              </template>
              <template v-else-if="data.nodeType === 'end'">
                <Handle id="left" type="target" :position="Position.Left" />
              </template>
              <template v-else>
                <Handle id="left" type="target" :position="Position.Left" />
                <Handle id="top" type="target" :position="Position.Top" />
                <Handle id="right" type="source" :position="Position.Right" />
                <Handle id="bottom" type="source" :position="Position.Bottom" />
              </template>
              <div class="flow-node-title">{{ data.title }}</div>
              <div class="flow-node-summary">{{ data.summary || '—' }}</div>
            </div>
          </template>
        </VueFlow>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Handle, MarkerType, Position, VueFlow } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import {
  defaultFlowNode,
  DRIVE_NODE_LABEL,
  normalizeAction,
  summarizeAction,
  getCompleteAction,
  summarizeCompleteAction,
  summarizeCondition
} from '@/utils/process/processStepScript'

const props = defineProps({
  flow: { type: Object, required: true },
  selectedId: { type: String, default: '' },
  readonly: { type: Boolean, default: false },
  equipments: { type: Array, default: () => [] }
})

const emit = defineEmits(['select-node'])

const selectedEdgeId = ref('')

const titleMap = {
  start: '开始',
  end: '结束',
  drive: DRIVE_NODE_LABEL,
  action: '执行动作',
  complete: '完成动作',
  exception: '异常分支'
}

const toneMap = {
  start: 'terminal',
  end: 'terminal',
  drive: 'drive',
  action: 'action',
  complete: 'complete',
  exception: 'exception'
}

const flowNodes = computed(() =>
  (props.flow.nodes || []).map((node) => ({
    id: node.id,
    type: 'processStep',
    position: node.position,
    selectable: false,
    draggable: !props.readonly,
    data: {
      nodeType: node.type,
      title: node.title || titleMap[node.type],
      tone: node.tone || toneMap[node.type],
      summary: nodeSummary(node),
      readonly: props.readonly || node.type === 'start' || node.type === 'end',
      expanded: props.selectedId === node.id
    }
  }))
)

const flowEdges = computed(() =>
  (props.flow.edges || []).map((edge) => ({
    ...edge,
    type: edge.type || 'smoothstep',
    animated: true,
    markerEnd: MarkerType.ArrowClosed,
    updatable: false,
    selectable: !props.readonly,
    selected: selectedEdgeId.value === edge.id,
    style: {
      stroke: selectedEdgeId.value === edge.id ? '#111827' : '#91a4b7',
      strokeWidth: selectedEdgeId.value === edge.id ? 3 : 2
    }
  }))
)

const canRemoveSelected = computed(() => {
  const node = getRawNode(props.selectedId)
  return !!node.id && node.type !== 'start' && node.type !== 'end'
})

const canRemoveSelectedEdge = computed(() => !!selectedEdgeId.value)

function nodeSummary(node) {
  if (node.type === 'start') return '工序入口'
  if (node.type === 'end') return '工序出口'
  if (node.type === 'action') {
    return summarizeAction(normalizeAction(node.action, props.equipments), props.equipments)
  }
  if (node.type === 'drive') return summarizeCondition(node.condition, props.equipments)
  if (node.type === 'complete') return summarizeCompleteAction(getCompleteAction(node))
  if (node.type === 'exception') return summarizeCondition(node.condition, props.equipments)
  return '—'
}

function addNode(type) {
  const count = (props.flow.nodes || []).filter((node) => node.type === type).length + 1
  const node = defaultFlowNode(type, count, { x: 220 + (count - 1) * 32, y: 80 + (count - 1) * 32 }, props.equipments)
  props.flow.nodes.push(node)
  emit('select-node', { id: node.id })
}

function onConnect(connection) {
  if (props.readonly || !connection.source || !connection.target) return
  props.flow.edges.push({
    ...connection,
    id: `e-${connection.source}-${connection.target}-${Date.now()}`,
    type: 'smoothstep',
    animated: true
  })
  selectedEdgeId.value = ''
}

function onNodeClick({ node }) {
  if (node.data?.nodeType === 'start' || node.data?.nodeType === 'end') return
  selectedEdgeId.value = ''
  emit('select-node', { id: node.id })
}

function onEdgeClick({ edge }) {
  if (props.readonly) return
  selectedEdgeId.value = edge.id
  emit('select-node', { id: '' })
}

function onNodeDragStop({ node }) {
  const raw = getRawNode(node.id)
  if (raw?.position) raw.position = { ...node.position }
}

function getRawNode(id) {
  return props.flow.nodes.find((node) => node.id === id) || {}
}

function removeSelectedEdge() {
  if (!selectedEdgeId.value) return
  const index = (props.flow.edges || []).findIndex((edge) => edge.id === selectedEdgeId.value)
  if (index >= 0) {
    props.flow.edges.splice(index, 1)
  }
  selectedEdgeId.value = ''
}

function removeSelectedNode() {
  if (!canRemoveSelected.value) return
  const id = props.selectedId
  const nodeIndex = props.flow.nodes.findIndex((node) => node.id === id)
  if (nodeIndex >= 0) {
    props.flow.nodes.splice(nodeIndex, 1)
  }
  for (let i = (props.flow.edges || []).length - 1; i >= 0; i--) {
    const edge = props.flow.edges[i]
    if (edge.source === id || edge.target === id) {
      props.flow.edges.splice(i, 1)
    }
  }
  emit('select-node', { id: '' })
}

function removeSelectedElement() {
  if (props.readonly) return
  if (selectedEdgeId.value) {
    removeSelectedEdge()
    return
  }
  removeSelectedNode()
}
</script>

<style scoped>
.flow-chart-wrap {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.flow-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.flow-hint {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.flow-toolbar :deep(.ant-btn) {
  cursor: pointer;
}

.flow-shell {
  width: 100%;
  height: 62vh;
  min-height: 420px;
  max-height: 720px;
  overflow: hidden;
  border: 1px solid #eef1f6;
  border-radius: 8px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}

.flow-board {
  width: 1600px;
  height: 1000px;
}

.process-vue-flow {
  width: 100%;
  height: 100%;
}

.process-vue-flow :deep(.vue-flow__pane) {
  cursor: grab;
}

.process-vue-flow :deep(.vue-flow__pane.dragging) {
  cursor: grabbing;
}

.process-vue-flow :deep(.vue-flow__connection-path) {
  stroke: #111827;
  stroke-width: 2.5;
}

.flow-node {
  width: 148px;
  min-height: 72px;
  padding: 10px 12px 8px;
  text-align: left;
  border-radius: 8px;
  border: 2px solid transparent;
  background: #fff;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s, transform 0.15s;
}

.flow-node:hover:not(.is-readonly) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
}

.flow-node.is-selected {
  border-color: #1677ff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.15);
}

.flow-node.is-readonly {
  cursor: default;
}

.flow-node :deep(.vue-flow__handle) {
  width: 10px;
  height: 10px;
  border: 1px solid #fff;
  background: var(--flow-node-accent, #111827);
  box-shadow: 0 0 0 2px var(--flow-node-accent-shadow, rgba(17, 24, 39, 0.18));
  cursor: crosshair;
  z-index: 5;
}

.flow-node.is-readonly :deep(.vue-flow__handle) {
  opacity: 0.7;
}

.flow-node :deep(.vue-flow__handle)::after {
  content: '';
  position: absolute;
  inset: -10px;
  border-radius: 50%;
}

.flow-node :deep(.vue-flow__handle:hover) {
  background: var(--flow-node-accent, #111827);
  box-shadow: 0 0 0 4px var(--flow-node-accent-shadow, rgba(17, 24, 39, 0.18));
}

.flow-node-title {
  font-size: 12px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
  margin-bottom: 4px;
}

.flow-node-summary {
  font-size: 11px;
  line-height: 1.4;
  color: rgba(0, 0, 0, 0.55);
  word-break: break-word;
}

.tone-terminal {
  --flow-node-accent: #8c8c8c;
  --flow-node-accent-shadow: rgba(140, 140, 140, 0.36);
  background: #f5f5f5;
  border-color: #d9d9d9;
}

.tone-drive {
  --flow-node-accent: #1677ff;
  --flow-node-accent-shadow: rgba(22, 119, 255, 0.28);
  border-color: #91caff;
  background: #e6f4ff;
}

.tone-action {
  --flow-node-accent: #52c41a;
  --flow-node-accent-shadow: rgba(82, 196, 26, 0.28);
  border-color: #b7eb8f;
  background: #f6ffed;
}

.tone-complete {
  --flow-node-accent: #722ed1;
  --flow-node-accent-shadow: rgba(114, 46, 209, 0.28);
  border-color: #d3adf7;
  background: #f9f0ff;
}

.tone-exception {
  --flow-node-accent: #ff4d4f;
  --flow-node-accent-shadow: rgba(255, 77, 79, 0.3);
  border-color: #ffccc7;
  background: #fff2f0;
}

:deep(.vue-flow__edge.animated path) {
  stroke-dasharray: 5;
  animation-duration: 0.8s;
}

:deep(.vue-flow__edge.selected path),
:deep(.vue-flow__edge.selected .vue-flow__edge-path) {
  stroke: #111827 !important;
  stroke-width: 3 !important;
}
</style>
