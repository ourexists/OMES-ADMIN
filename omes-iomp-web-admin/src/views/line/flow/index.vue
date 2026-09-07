<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ArrowLeftOutlined,
  NodeIndexOutlined,
  PlusOutlined,
  ReloadOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import { useLineTfFlowGraph } from '@/composables/useLineTfFlowGraph'
import { formatTfStepLabel, isLineTfTerminalNodeId } from '@/composables/lineTfFlowShared'
import type { TfRecord } from '@/types/line'
import TfNodeFormModal from './components/TfNodeFormModal.vue'
import LineTfFlowCanvas from './components/LineTfFlowCanvas.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const lineId = computed(() => String(route.query.line_id || ''))
const pageTitle = computed(() => {
  const title = route.query.title
  const name = Array.isArray(title) ? title[0] : title
  return name ? `${t('lineFlowPage.title')} · ${name}` : t('lineFlowPage.title')
})

const canvasRef = ref<InstanceType<typeof LineTfFlowCanvas> | null>(null)
const tfFormOpen = ref(false)
const editingDraft = shallowRef<TfRecord | null>(null)

function openNodeEditor(nodeId: string) {
  if (isLineTfTerminalNodeId(nodeId)) {
    return
  }
  const draft = getNodeDraftById(nodeId)
  if (!draft) {
    return
  }
  editingDraft.value = draft
  tfFormOpen.value = true
}

const {
  loading,
  nodes,
  flowNodes,
  flowEdges,
  reloadAll,
  addLocalNode,
  addLocalNodeFromConnect,
  onNodeDragStop,
  onFlowNodesChange,
  saveEdges,
  onConnect,
  getNodeDraftById,
  saveNodeDraft,
} = useLineTfFlowGraph(() => lineId.value)

function goBack() {
  router.push('/view/line_tables')
}

const tfLocalMode = computed(
  () =>
    Boolean(editingDraft.value?.__localOnly) ||
    String(editingDraft.value?.id || '').startsWith('tmp_'),
)

async function onTfSaved(record: TfRecord) {
  await saveNodeDraft(record)
}

function deleteSelected() {
  canvasRef.value?.deleteSelected()
}

async function handleSaveEdges() {
  await saveEdges()
  canvasRef.value?.scheduleFitView(true)
}

async function handleReload() {
  await reloadAll()
  canvasRef.value?.scheduleFitView(true)
}

onMounted(async () => {
  if (!lineId.value) {
    return
  }
  await reloadAll()
  canvasRef.value?.scheduleFitView(true)
})
</script>

<template>
  <div class="admin-page process-flow-page process-flow-page--line">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle>
          <template #icon><NodeIndexOutlined /></template>
          {{ pageTitle }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-button type="link" class="back-link" @click="goBack">
          <ArrowLeftOutlined />
          {{ t('lineFlowPage.back') }}
        </a-button>
      </template>

      <div class="admin-panel-body">
        <div class="toolbar-strip">
          <div class="toolbar-strip__actions">
            <a-space wrap>
              <a-button type="primary" :disabled="!lineId" @click="addLocalNode">
                <template #icon><PlusOutlined /></template>
                {{ t('lineFlowPage.addNode') }}
              </a-button>
              <a-button danger :disabled="!lineId" @click="deleteSelected">
                {{ t('lineFlowPage.deleteSelected') }}
              </a-button>
              <a-button type="primary" ghost :disabled="!lineId" @click="handleSaveEdges">
                {{ t('lineFlowPage.saveEdges') }}
              </a-button>
              <a-button :loading="loading" @click="handleReload">
                <template #icon><ReloadOutlined /></template>
                {{ t('lineFlowPage.refresh') }}
              </a-button>
            </a-space>
          </div>
        </div>

        <div class="flow-canvas-wrap">
          <a-spin :spinning="loading">
            <div v-if="!lineId" class="flow-state flow-state--error">
              {{ t('lineFlowPage.missingLineId') }}
            </div>
            <LineTfFlowCanvas
              v-else
              ref="canvasRef"
              v-model:nodes="flowNodes"
              v-model:edges="flowEdges"
              class="flow-graph-panel"
              @connect="onConnect"
              @connect-add-node="(payload) => addLocalNodeFromConnect(payload.sourceNodeId, payload.position)"
              @node-drag-stop="onNodeDragStop"
              @nodes-change="onFlowNodesChange"
              @node-dbl-click="openNodeEditor"
            />
          </a-spin>
        </div>

        <div v-if="nodes.length" class="flow-node-panel">
          <div class="flow-node-panel__title">
            <UnorderedListOutlined />
            {{ t('lineFlowPage.nodeList') }}
          </div>
          <a-space wrap :size="[8, 8]">
            <a-tag
              v-for="node in nodes"
              :key="node.id"
              class="flow-node-chip"
              @dblclick="openNodeEditor(String(node.id))"
            >
              {{ formatTfStepLabel(node) }}
            </a-tag>
          </a-space>
        </div>
      </div>
    </a-card>

    <TfNodeFormModal
      v-model:open="tfFormOpen"
      :line-id="lineId"
      :draft="editingDraft"
      :local-mode="tfLocalMode"
      @saved="onTfSaved"
    />
  </div>
</template>
