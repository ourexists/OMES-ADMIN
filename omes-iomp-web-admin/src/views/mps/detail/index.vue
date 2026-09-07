<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeftOutlined, NodeIndexOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { fetchMpsById, startMpsTf } from '@/api/mps'
import { fetchTfByLineId, fetchTfEdgesByLineId } from '@/api/line'
import type { TfEdgeRecord, TfRecord } from '@/types/line'
import type { MpsRecord, MpsRuntimeTf, MpsTfRecord } from '@/types/mps'
import { MPS_STATUS } from '@/types/mps'
import { useMpsTfRuntimeFlow } from '@/composables/useMpsTfRuntimeFlow'
import { isStepEngineConfigured } from '@/utils/process/processStepScript'
import { message } from 'ant-design-vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import ProcessStepScriptModal from '@/components/process/ProcessStepScriptModal.vue'
import MoAdjustModal, { type MoAdjustType } from '@/views/mo/components/MoAdjustModal.vue'
import MpsTfRuntimeCanvas from './components/MpsTfRuntimeCanvas.vue'
import MpsTfRuntimeDrawer from './components/MpsTfRuntimeDrawer.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const record = ref<MpsRecord | null>(null)
const lineTfs = ref<TfRecord[]>([])
const lineEdges = ref<TfEdgeRecord[]>([])
const canvasRef = ref<InstanceType<typeof MpsTfRuntimeCanvas> | null>(null)
const drawerOpen = ref(false)
const selectedNodeId = ref('')
const engineOpen = ref(false)
const adjustOpen = ref(false)
const adjustType = ref<MoAdjustType | null>(null)

const mpsId = computed(() => String(route.query.id || ''))

const canAdjust = computed(() => {
  const status = record.value?.status
  return (
    status === MPS_STATUS.WAIT_QUE ||
    status === MPS_STATUS.WAIT_EXEC ||
    status === MPS_STATUS.EXECING
  )
})

function openAdjust(type: MoAdjustType) {
  if (!record.value?.moCode) return
  adjustType.value = type
  adjustOpen.value = true
}

function onAdjustMenu({ key }: { key: string | number }) {
  openAdjust(String(key) as MoAdjustType)
}

async function onAdjustSuccess() {
  await loadDetail()
}

const detailColumns = computed(() => [
  { title: t('moPage.matName'), dataIndex: 'matName', key: 'matName', ellipsis: true },
  { title: t('moPage.matCode'), dataIndex: 'matCode', key: 'matCode', width: 120 },
  { title: t('moPage.matQty'), dataIndex: 'matNum', key: 'matNum', width: 100 },
  { title: t('mpsPage.actualNum'), dataIndex: 'actualNum', key: 'actualNum', width: 100 },
  { title: t('moPage.equipCode'), dataIndex: 'devNo', key: 'devNo', width: 110 },
  { title: t('moPage.equipName'), dataIndex: 'devName', key: 'devName', width: 110, ellipsis: true },
  { title: t('moPage.devgCode'), dataIndex: 'dgCode', key: 'dgCode', width: 110 },
  { title: t('moPage.devgName'), dataIndex: 'dgName', key: 'dgName', width: 110, ellipsis: true },
  { title: t('moPage.feedPriority'), dataIndex: 'priority', key: 'priority', width: 80, align: 'center' as const },
])

function canStartTf(tf: MpsTfRecord): boolean {
  const status = record.value?.status
  return (status === MPS_STATUS.WAIT_EXEC || status === MPS_STATUS.EXECING) && tf.status === 0
}

const { flowNodes, flowEdges, runtimeById } = useMpsTfRuntimeFlow(
  () => record.value?.tfs,
  lineTfs,
  lineEdges,
  canStartTf,
  () => record.value?.status,
)

const selectedTf = computed<MpsRuntimeTf | null>(
  () => runtimeById.value[selectedNodeId.value] || null,
)

const selectedCanStart = computed(() => (selectedTf.value ? canStartTf(selectedTf.value) : false))

const selectedEngineConfigured = computed(() =>
  isStepEngineConfigured(
    selectedTf.value?.stepScript,
    selectedTf.value?.equipments,
    selectedTf.value?.stepEngineConfig,
  ),
)

async function loadLineFlow(lineId?: string, embedded?: TfRecord[]) {
  if (!lineId) {
    lineTfs.value = embedded || []
    lineEdges.value = []
    return
  }
  try {
    const [tfs, edges] = await Promise.all([
      embedded?.length ? Promise.resolve(embedded) : fetchTfByLineId(lineId),
      fetchTfEdgesByLineId(lineId),
    ])
    lineTfs.value = tfs || []
    lineEdges.value = edges || []
  } catch {
    lineTfs.value = embedded || []
    lineEdges.value = []
  }
}

async function loadDetail() {
  if (!mpsId.value) {
    return
  }
  loading.value = true
  try {
    const data = await fetchMpsById(mpsId.value)
    record.value = data
    await loadLineFlow(data?.lineVo?.id, data?.lineVo?.tfs)
    await nextTick()
    canvasRef.value?.scheduleFitView(true)
  } finally {
    loading.value = false
  }
}

function onNodeClick(nodeId: string) {
  selectedNodeId.value = nodeId
  drawerOpen.value = true
}

async function handleStartSelected() {
  const tf = selectedTf.value
  if (!tf?.id) {
    return
  }
  await startMpsTf(tf.id)
  message.success(t('mpsPage.tfStartSuccess'))
  await loadDetail()
}

function goBack() {
  router.push('/view/mps_tables')
}

watch(drawerOpen, (open) => {
  if (!open) {
    selectedNodeId.value = ''
    engineOpen.value = false
  }
})

onMounted(loadDetail)
</script>

<template>
  <div class="admin-page production-detail-page production-detail-page--mps">
    <a-card size="small" class="admin-panel-card panel-card" :loading="loading">
      <template #title>
        <AdminPanelTitle>
          <template #icon><NodeIndexOutlined /></template>
          {{ t('mpsPage.detailTitle') }}
          <span v-if="record?.id" class="card-title__badge">[{{ record.id }}]</span>
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-space>
          <a-dropdown v-if="canAdjust" :trigger="['click']">
            <a-button size="small" type="primary" ghost>{{ t('moPage.adjust') }}</a-button>
            <template #overlay>
              <a-menu @click="onAdjustMenu">
                <a-menu-item
                  v-if="record?.status === MPS_STATUS.WAIT_QUE || record?.status === MPS_STATUS.WAIT_EXEC"
                  key="RESCHEDULE"
                >
                  {{ t('mpsPage.reschedule') }}
                </a-menu-item>
                <a-menu-item
                  v-if="record?.status === MPS_STATUS.WAIT_QUE || record?.status === MPS_STATUS.WAIT_EXEC"
                  key="CHANGE_DEV"
                >
                  {{ t('mpsPage.changeDev') }}
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="CANCEL_MPS" danger>{{ t('mpsPage.delete') }}</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-button size="small" @click="loadDetail">
            <ReloadOutlined />
            {{ t('mpsPage.refresh') }}
          </a-button>
          <a-button size="small" @click="goBack">
            <ArrowLeftOutlined />
            {{ t('mpsPage.back') }}
          </a-button>
        </a-space>
      </template>

      <div v-if="record" class="meta-descriptions">
        <a-descriptions size="small" :column="{ xs: 1, sm: 2, md: 3, lg: 4 }" bordered>
          <a-descriptions-item :label="t('mpsPage.moCode')">{{ record.moCode || '—' }}</a-descriptions-item>
          <a-descriptions-item :label="t('mpsPage.batch')">{{ record.batch ?? '-' }}</a-descriptions-item>
          <a-descriptions-item :label="t('moPage.bomCode')">{{ record.moDto?.productCode || '—' }}</a-descriptions-item>
          <a-descriptions-item :label="t('moPage.bomName')">{{ record.moDto?.productName || '—' }}</a-descriptions-item>
          <a-descriptions-item :label="t('moPage.lineName')">
            {{ record.lineVo?.name || record.line || '-' }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('mpsPage.execTime')">{{ record.execTime || '—' }}</a-descriptions-item>
          <a-descriptions-item :label="t('mpsPage.planQty')">{{ record.num ?? '-' }}</a-descriptions-item>
          <a-descriptions-item :label="t('mpsPage.weight')">{{ record.weight ?? '-' }}</a-descriptions-item>
          <a-descriptions-item :label="t('mpsPage.status')" :span="2">
            <a-tag>{{ record.statusDesc || '—' }}</a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <div class="production-section">
        <div class="production-section__title">
          <NodeIndexOutlined />
          {{ t('moPage.sectionBomDetail') }}
        </div>
        <a-table
          row-key="matCode"
          size="middle"
          bordered
          class="production-module-table"
          :columns="detailColumns"
          :data-source="record?.details || []"
          :pagination="false"
          :scroll="{ x: 1100 }"
        />
      </div>

      <div class="production-section">
        <div class="production-section__title">
          <NodeIndexOutlined />
          {{ t('moPage.tfSection') }}
          <span class="production-section__subtitle">{{ t('mpsPage.tfFlowHint') }}</span>
        </div>
        <div class="mps-tf-flow-legend">
          <span class="mps-tf-flow-legend__item tone-pending">{{ t('mpsPage.tfFlowLegendPending') }}</span>
          <span class="mps-tf-flow-legend__item tone-running">{{ t('mpsPage.tfFlowLegendRunning') }}</span>
          <span class="mps-tf-flow-legend__item tone-done">{{ t('mpsPage.tfFlowLegendDone') }}</span>
          <span class="mps-tf-flow-legend__item tone-stop">{{ t('mpsPage.tfFlowLegendStop') }}</span>
          <span class="mps-tf-flow-legend__item tone-error">{{ t('mpsPage.tfFlowLegendError') }}</span>
        </div>
        <div class="mps-tf-flow-wrap">
          <a-empty
            v-if="!loading && !(record?.tfs || []).length"
            :description="t('mpsPage.tfFlowEmpty')"
          />
          <MpsTfRuntimeCanvas
            v-else-if="(record?.tfs || []).length"
            ref="canvasRef"
            v-model:nodes="flowNodes"
            v-model:edges="flowEdges"
            @node-click="onNodeClick"
          />
        </div>
      </div>
    </a-card>

    <MpsTfRuntimeDrawer
      v-model:open="drawerOpen"
      :tf="selectedTf"
      :can-start="selectedCanStart"
      :engine-configured="selectedEngineConfigured"
      @start="handleStartSelected"
      @view-engine="engineOpen = true"
    />
    <ProcessStepScriptModal
      v-model:open="engineOpen"
      :step-script="selectedTf?.stepScript || ''"
      :step-engine-config="selectedTf?.stepEngineConfig || ''"
      :step-id="selectedTf?.id || selectedTf?.selfCode || ''"
      :step-name="selectedTf?.name || ''"
      :step-no="selectedTf?.stepNo != null ? String(selectedTf.stepNo) : ''"
      :step-equipments="selectedTf?.equipments || []"
      readonly
    />
    <MoAdjustModal
      v-model:open="adjustOpen"
      :adjust-type="adjustType"
      :mps-record="record"
      :mps-ids="record?.id ? [record.id] : undefined"
      @success="onAdjustSuccess"
    />
  </div>
</template>
