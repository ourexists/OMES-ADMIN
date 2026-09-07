<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import dayjs, { type Dayjs } from 'dayjs'
import {
  ArrowLeftOutlined,
  CheckCircleOutlined,
  NodeIndexOutlined,
  PlayCircleOutlined,
} from '@ant-design/icons-vue'
import type { LineRecord } from '@/api/mo'
import type { DevgRecord } from '@/types/devg'
import {
  calcMpsFlow,
  completeMpsFlow,
  fetchDevicesByDgId,
  fetchLineByCode,
  fetchMoById,
  matchDeviceForMaterial,
} from '@/api/mo'
import type { MoDetailRecord, MoRecord, MoTfRecord, MpsFlowPreviewRecord } from '@/types/mo'
import DevgPickerModal from '../components/DevgPickerModal.vue'
import LinePickerModal from '../components/LinePickerModal.vue'
import { message } from 'ant-design-vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const currentStep = ref(0)
const record = ref<MoRecord | null>(null)
const details = ref<MoDetailRecord[]>([])
const tfList = ref<MoTfRecord[]>([])
const previewList = ref<MpsFlowPreviewRecord[]>([])

const devgPickerOpen = ref(false)
const linePickerOpen = ref(false)

const formState = reactive({
  sequence: 0,
  execTime: null as Dayjs | null,
  execType: 0,
  execNum: 1,
  devgId: '',
  devgName: '',
  lineName: '',
  lineCode: '',
})

const moId = computed(() => String(route.query.id || ''))

const detailColumns = computed(() => [
  { title: t('moPage.matName'), dataIndex: 'matName', key: 'matName', ellipsis: true },
  { title: t('moPage.matCode'), dataIndex: 'matCode', key: 'matCode', width: 120 },
  { title: t('moPage.matQty'), dataIndex: 'matNum', key: 'matNum', width: 100 },
  { title: t('moPage.equipCode'), dataIndex: 'devNo', key: 'devNo', width: 110 },
  { title: t('moPage.equipName'), dataIndex: 'devName', key: 'devName', width: 110, ellipsis: true },
  { title: t('moPage.feedPriority'), dataIndex: 'priority', key: 'priority', width: 90 },
])

const tfColumns = computed(() => [
  { title: 'NO', key: 'index', width: 56, align: 'center' as const },
  { title: t('moPage.tfCode'), dataIndex: 'selfCode', key: 'selfCode', width: 120 },
  { title: t('moPage.tfName'), dataIndex: 'name', key: 'name', ellipsis: true },
])

const previewColumns = computed(() => [
  { title: t('moPage.moCode'), dataIndex: 'moCode', key: 'moCode', width: 130 },
  { title: t('moPage.bomCode'), key: 'productCode', width: 120 },
  { title: t('moPage.bomName'), key: 'productName', ellipsis: true },
  { title: t('moPage.planQty'), dataIndex: 'num', key: 'num', width: 90, align: 'center' as const },
  { title: t('moPage.planWeight'), dataIndex: 'weight', key: 'weight', width: 100, align: 'center' as const },
  { title: t('moPage.planExecTime'), dataIndex: 'execTime', key: 'execTime', width: 160 },
  { title: t('moPage.lineName'), key: 'line', width: 120 },
  { title: t('moPage.planBatch'), dataIndex: 'batch', key: 'batch', width: 90, align: 'center' as const },
])

async function loadMo() {
  if (!moId.value) {
    return
  }
  loading.value = true
  try {
    const data = await fetchMoById(moId.value)
    record.value = data
    details.value = (data?.detailDtoList || []).map((row) => ({ ...row }))
    formState.execNum = data?.surplus || 1
    formState.execTime = data?.execTime ? dayjs(data.execTime) : dayjs()
    if (data?.lineCode) {
      await applyLineCode(data.lineCode)
    }
  } finally {
    loading.value = false
  }
}

async function applyLineCode(code: string) {
  const line = await fetchLineByCode(code)
  if (!line) {
    formState.lineCode = ''
    formState.lineName = ''
    tfList.value = []
    return
  }
  formState.lineCode = line.selfCode || code
  formState.lineName = line.name || code
  tfList.value = (line.tfs || []).map((row) => ({ ...row }))
}

function validateStep0(): boolean {
  const surplus = record.value?.surplus || 0
  if (formState.execNum > surplus) {
    message.warning(t('moPage.planQtyOverRemaining'))
    return false
  }
  if (formState.execNum < 1) {
    message.warning(t('moPage.planQtyMinOne'))
    return false
  }
  if (!formState.execTime) {
    message.warning(t('moPage.execTimeRequired'))
    return false
  }
  return true
}

function nextStep() {
  if (currentStep.value === 0 && !validateStep0()) {
    return
  }
  if (currentStep.value < 4) {
    currentStep.value += 1
  }
}

function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value -= 1
  }
}

async function onDevgSelected(devg: DevgRecord) {
  formState.devgId = devg.id
  formState.devgName = devg.name || devg.selfCode || ''
  const devices = await fetchDevicesByDgId(devg.id)
  details.value.forEach((detail) => {
    detail.devNo = undefined
    detail.devName = undefined
    detail.dgCode = undefined
    detail.dgName = undefined
    const matched = matchDeviceForMaterial(devices, detail.matCode)
    if (matched) {
      detail.devNo = matched.selfCode
      detail.devName = matched.name
      detail.dgCode = devg.selfCode
      detail.dgName = devg.name
    }
  })
}

async function onLineSelected(line: LineRecord) {
  formState.lineCode = line.selfCode || ''
  formState.lineName = line.name || line.selfCode || ''
  tfList.value = (line.tfs || (await fetchLineByCode(line.selfCode || ''))?.tfs || []).map((row) => ({ ...row }))
}

async function calcPreview() {
  if (!record.value?.selfCode) {
    return
  }
  saving.value = true
  try {
    previewList.value = await calcMpsFlow({
      moCode: record.value.selfCode,
      sequence: formState.sequence,
      execTime: formState.execTime?.format('YYYY-MM-DD HH:mm:ss'),
      execType: formState.execType,
      execNum: formState.execNum,
      line: formState.lineCode || undefined,
      details: details.value,
      tfs: tfList.value,
    })
    nextStep()
  } finally {
    saving.value = false
  }
}

async function submitComplete() {
  saving.value = true
  try {
    await completeMpsFlow(previewList.value)
    message.success(t('moPage.planCreateSuccess'))
    currentStep.value = 4
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/view/mo_tables')
}

function queryMpsPlans() {
  const moCode = record.value?.selfCode
  router.push({
    path: '/view/mps_tables',
    query: moCode ? { moCode } : undefined,
  })
}

onMounted(loadMo)
</script>

<template>
  <div class="admin-page production-detail-page production-detail-page--mo production-wizard-page">
    <a-card size="small" class="admin-panel-card panel-card" :loading="loading">
      <template #title>
        <AdminPanelTitle>
          <template #icon><PlayCircleOutlined /></template>
          {{ t('moPage.execTitle') }}
          <span v-if="record?.selfCode" class="card-title__badge">[{{ record.selfCode }}]</span>
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-button size="small" @click="goBack">
          <ArrowLeftOutlined />
          {{ t('moPage.back') }}
        </a-button>
      </template>

      <a-steps :current="Math.min(currentStep, 3)" size="small" class="wizard-steps">
        <a-step :title="t('moPage.execStepDesign')" />
        <a-step :title="t('moPage.stepDevg')" />
        <a-step :title="t('moPage.stepLine')" />
        <a-step :title="t('moPage.execStepSure')" />
      </a-steps>

      <div v-if="record" class="summary-bar">
        <span>{{ record.productName }} [{{ record.productCode }}]</span>
        <span>{{ t('moPage.batchQty') }}: {{ record.num }}</span>
        <span>{{ t('moPage.remainingQty') }}: {{ record.surplus }}</span>
        <span>{{ t('moPage.batchWeight') }}: {{ record.weight }}</span>
      </div>

      <div v-show="currentStep === 0" class="step-panel">
        <a-form layout="vertical">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item :label="t('moPage.planSequence')">
                <a-input-number v-model:value="formState.sequence" :min="0" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('moPage.planExecTime')" required>
                <a-date-picker
                  v-model:value="formState.execTime"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('moPage.planQty')" required>
                <a-input-number v-model:value="formState.execNum" :min="1" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item :label="t('moPage.planExecType')">
                <a-radio-group v-model:value="formState.execType">
                  <a-radio :value="0">{{ t('moPage.planExecTypeFull') }}</a-radio>
                  <a-radio :value="1">{{ t('moPage.planExecTypePartial') }}</a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
        <div class="step-actions">
          <a-button type="primary" @click="nextStep">{{ t('moPage.next') }}</a-button>
        </div>
      </div>

      <div v-show="currentStep === 1" class="step-panel">
        <a-form layout="inline" class="inline-picker-form">
          <a-form-item :label="t('moPage.devgName')">
            <a-input v-model:value="formState.devgName" disabled style="width: 220px" />
          </a-form-item>
          <a-form-item>
            <a-button @click="devgPickerOpen = true">{{ t('moPage.selectDevg') }}</a-button>
          </a-form-item>
        </a-form>
        <a-table
          row-key="matCode"
          size="middle"
          bordered
          class="production-module-table"
          :columns="detailColumns"
          :data-source="details"
          :pagination="false"
          :scroll="{ y: 320 }"
        >
          <template #bodyCell="{ column, record: row }">
            <template v-if="column.key === 'priority'">
              <a-input-number v-model:value="row.priority" :min="0" size="small" style="width: 100%" />
            </template>
          </template>
        </a-table>
        <div class="step-actions">
          <a-button @click="prevStep">{{ t('moPage.prev') }}</a-button>
          <a-button type="primary" @click="nextStep">{{ t('moPage.next') }}</a-button>
        </div>
      </div>

      <div v-show="currentStep === 2" class="step-panel">
        <a-form layout="inline" class="inline-picker-form">
          <a-form-item :label="t('moPage.lineName')">
            <a-input v-model:value="formState.lineName" disabled style="width: 220px" />
          </a-form-item>
          <a-form-item>
            <a-button @click="linePickerOpen = true">{{ t('moPage.selectLine') }}</a-button>
          </a-form-item>
        </a-form>
        <a-table
          row-key="id"
          size="middle"
          bordered
          class="production-module-table"
          :columns="tfColumns"
          :data-source="tfList"
          :pagination="false"
          :scroll="{ x: 980, y: 320 }"
        >
          <template #bodyCell="{ column, index }">
            <template v-if="column.key === 'index'">{{ index + 1 }}</template>
          </template>
        </a-table>
        <div class="step-actions">
          <a-button @click="prevStep">{{ t('moPage.prev') }}</a-button>
          <a-button type="primary" :loading="saving" @click="calcPreview">{{ t('moPage.next') }}</a-button>
        </div>
      </div>

      <div v-show="currentStep === 3" class="step-panel">
        <div class="production-section__title">
          <NodeIndexOutlined />
          {{ t('moPage.execStepSure') }}
        </div>
        <a-table
          row-key="batch"
          size="middle"
          bordered
          class="production-module-table"
          :columns="previewColumns"
          :data-source="previewList"
          :pagination="false"
          :scroll="{ x: 980 }"
        >
          <template #bodyCell="{ column, record: row }">
            <template v-if="column.key === 'productCode'">{{ row.moDto?.productCode || '—' }}</template>
            <template v-else-if="column.key === 'productName'">{{ row.moDto?.productName || '-' }}</template>
            <template v-else-if="column.key === 'line'">{{ row.lineVo?.name || '-' }}</template>
          </template>
        </a-table>
        <div class="step-actions">
          <a-button @click="prevStep">{{ t('moPage.prev') }}</a-button>
          <a-button type="primary" :loading="saving" @click="submitComplete">{{ t('moPage.complete') }}</a-button>
        </div>
      </div>

      <div v-show="currentStep === 4" class="success-panel">
        <CheckCircleOutlined class="success-icon" />
        <div class="success-title">{{ t('moPage.planCreateSuccess') }}</div>
        <a-space>
          <a-button type="primary" @click="queryMpsPlans">{{ t('moPage.viewPlans') }}</a-button>
          <a-button @click="goBack">{{ t('moPage.back') }}</a-button>
        </a-space>
      </div>
    </a-card>

    <DevgPickerModal v-model:open="devgPickerOpen" @select="onDevgSelected" />
    <LinePickerModal v-model:open="linePickerOpen" @select="onLineSelected" />
  </div>
</template>
