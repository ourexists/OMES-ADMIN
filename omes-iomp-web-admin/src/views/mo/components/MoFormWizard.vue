<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs, { type Dayjs } from 'dayjs'
import {
  AppstoreOutlined,
  BarcodeOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  EnvironmentOutlined,
  FileTextOutlined,
  NodeIndexOutlined,
  SearchOutlined,
  ToolOutlined,
} from '@ant-design/icons-vue'
import type { BomRecord } from '@/api/bom'
import { fetchBomById } from '@/api/bom'
import type { WorkshopNode } from '@/api/device'
import type { LineRecord } from '@/api/mo'
import type { DevgRecord } from '@/types/devg'
import { fetchDevicesByDgId, fetchTfByLineId, matchDeviceForMaterial, saveMo } from '@/api/mo'
import type { MoDetailRecord, MoTfRecord } from '@/types/mo'
import BomPickerModal from './BomPickerModal.vue'
import DevgPickerModal from './DevgPickerModal.vue'
import LinePickerModal from './LinePickerModal.vue'
import WorkshopPickerModal from './WorkshopPickerModal.vue'
import { message } from 'ant-design-vue'

/** 与后端 BOMTypeEnum 一致：1=定比配比 */
const BOM_TYPE_PERCENT = 1

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const currentStep = ref(0)
const saving = ref(false)
const productType = ref<number>(0)
const details = ref<MoDetailRecord[]>([])
const tfList = ref<MoTfRecord[]>([])
const selectedTfKeys = ref<string[]>([])

const bomPickerOpen = ref(false)
const devgPickerOpen = ref(false)
const linePickerOpen = ref(false)
const scenePickerOpen = ref(false)

const formState = reactive({
  selfCode: '',
  tenantId: '0',
  sceneName: '',
  productName: '',
  productCode: '',
  productId: '',
  num: 1,
  weight: 0 as number | string,
  execTime: dayjs() as Dayjs | null,
  devgId: '',
  devgName: '',
  lineName: '',
  lineCode: '',
})

const isPercentType = computed(() => productType.value === BOM_TYPE_PERCENT)
const hasBomSelected = computed(() => Boolean(formState.productCode))

const detailColumns = computed(() => [
  { title: t('moPage.matName'), dataIndex: 'matName', key: 'matName', ellipsis: true },
  { title: t('moPage.matCode'), dataIndex: 'matCode', key: 'matCode', width: 130 },
  { title: t('moPage.matQty'), dataIndex: 'matNum', key: 'matNum', width: 120 },
  { title: t('moPage.feedPriority'), dataIndex: 'priority', key: 'priority', width: 100 },
])

const devgDetailColumns = computed(() => [
  ...detailColumns.value.slice(0, 3),
  { title: t('moPage.equipCode'), dataIndex: 'devNo', key: 'devNo', width: 120 },
  { title: t('moPage.equipName'), dataIndex: 'devName', key: 'devName', width: 120, ellipsis: true },
  { title: t('moPage.feedPriority'), dataIndex: 'priority', key: 'priority', width: 100 },
])

const tfColumns = computed(() => [
  { title: 'NO', key: 'index', width: 56, align: 'center' as const },
  { title: t('moPage.tfCode'), dataIndex: 'selfCode', key: 'selfCode', width: 120 },
  { title: t('moPage.tfName'), dataIndex: 'name', key: 'name', ellipsis: true },
])

const tfRowSelection = computed(() => ({
  selectedRowKeys: selectedTfKeys.value,
  onChange: (keys: string[]) => {
    selectedTfKeys.value = keys
  },
}))

function resetForm() {
  currentStep.value = 0
  productType.value = 0
  details.value = []
  tfList.value = []
  selectedTfKeys.value = []
  formState.selfCode = ''
  formState.tenantId = '0'
  formState.sceneName = ''
  formState.productName = ''
  formState.productCode = ''
  formState.productId = ''
  formState.num = 1
  formState.weight = 0
  formState.execTime = dayjs()
  formState.devgId = ''
  formState.devgName = ''
  formState.lineName = ''
  formState.lineCode = ''
}

function toNumber(value: unknown): number {
  const num = Number(value)
  return Number.isFinite(num) ? num : 0
}

function calcDetails() {
  const weight = toNumber(formState.weight)
  let totalWeight = 0
  details.value.forEach((item) => {
    if (item.priority == null) {
      item.priority = 0
    }
    if (productType.value === BOM_TYPE_PERCENT) {
      const scale = toNumber(item.matScale)
      item.matNum = scale ? (weight * scale) / 100 : 0
    } else {
      item.matNum = toNumber(item.matScale)
      item.matScale = 0
      totalWeight += toNumber(item.matNum)
    }
  })
  if (productType.value !== BOM_TYPE_PERCENT) {
    formState.weight = totalWeight
  }
}

function onBomSelected(record: BomRecord) {
  fetchBomById(record.id).then((result) => {
    if (!result) {
      return
    }
    productType.value = Number(result.type ?? 0)
    formState.productId = result.id
    formState.productCode = result.selfCode || ''
    formState.productName = result.name || ''
    details.value = (result.details || []).map((row) => ({
      matId: row.matId,
      matName: row.matName,
      matCode: row.matCode,
      matScale: row.matScale,
      matNum: row.matScale,
      priority: 0,
    }))
    calcDetails()
  })
}

function onSceneSelected(node: WorkshopNode) {
  formState.tenantId = node.selfCode
  formState.sceneName = node.name || node.selfCode
}

async function onDevgSelected(record: DevgRecord) {
  formState.devgId = record.id
  formState.devgName = record.name || record.selfCode || ''
  const devices = await fetchDevicesByDgId(record.id)
  details.value.forEach((detail) => {
    detail.devNo = undefined
    detail.devName = undefined
    detail.dgCode = undefined
    detail.dgName = undefined
    const matched = matchDeviceForMaterial(devices, detail.matCode)
    if (matched) {
      detail.devNo = matched.selfCode
      detail.devName = matched.name
      detail.dgCode = record.selfCode
      detail.dgName = record.name
    }
  })
}

async function onLineSelected(record: LineRecord) {
  formState.lineCode = record.selfCode || ''
  formState.lineName = record.name || record.selfCode || ''
  tfList.value = await fetchTfByLineId(record.id)
  selectedTfKeys.value = tfList.value.map((row) => row.id || row.selfCode || '').filter(Boolean)
}

function validateStep0(): boolean {
  if (!formState.productCode) {
    message.warning(t('moPage.bomRequired'))
    return false
  }
  if (!formState.num || formState.num < 1) {
    message.warning(t('moPage.batchQtyRequired'))
    return false
  }
  if (!formState.execTime) {
    message.warning(t('moPage.execTimeRequired'))
    return false
  }
  if (!details.value.length) {
    message.warning(t('moPage.detailRequired'))
    return false
  }
  return true
}

function nextStep() {
  if (currentStep.value === 0 && !validateStep0()) {
    return
  }
  if (currentStep.value < 3) {
    currentStep.value += 1
  }
}

function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value -= 1
  }
}

async function submitForm() {
  if (!formState.lineCode) {
    message.warning(t('moPage.lineRequired'))
    return
  }
  saving.value = true
  try {
    await saveMo({
      selfCode: formState.selfCode.trim() || undefined,
      tenantId: formState.tenantId || '0',
      productId: formState.productId,
      productName: formState.productName,
      productCode: formState.productCode,
      productType: productType.value,
      num: formState.num,
      weight: formState.weight,
      execTime: formState.execTime?.format('YYYY-MM-DD HH:mm:ss'),
      devgId: formState.devgId || undefined,
      lineCode: formState.lineCode,
      detailDtoList: details.value,
    })
    message.success(t('moPage.createSuccess'))
    currentStep.value = 3
    emit('success')
  } finally {
    saving.value = false
  }
}

function onClose() {
  emit('update:open', false)
}

function openScenePicker() {
  scenePickerOpen.value = true
}

function openDevgPicker() {
  devgPickerOpen.value = true
}

function openLinePicker() {
  linePickerOpen.value = true
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      resetForm()
    }
  },
)

watch(
  () => formState.weight,
  () => {
    if (isPercentType.value) {
      calcDetails()
    }
  },
)
</script>

<template>
  <a-modal
    :open="open"
    width="min(1000px, 96vw)"
    :footer="null"
    destroy-on-close
    class="production-form-modal production-form-modal--mo"
    @cancel="onClose"
  >
    <template #title>
      <div class="modal-title-wrap">
        <span class="modal-title">
          <span class="modal-title__icon">
            <FileTextOutlined />
          </span>
          {{ t('moPage.formAdd') }}
        </span>
        <a-tag color="success">{{ t('moPage.formModeAdd') }}</a-tag>
      </div>
    </template>

    <a-steps :current="Math.min(currentStep, 2)" size="small" class="wizard-steps">
      <a-step :title="t('moPage.stepBasic')" />
      <a-step :title="t('moPage.stepDevg')" />
      <a-step :title="t('moPage.stepLine')" />
    </a-steps>

    <div v-show="currentStep === 0" class="step-panel">
      <div class="capability-layout capability-layout--step0">
        <section class="capability-block capability-block--identity">
          <header class="capability-block__head">
            <span class="capability-block__icon"><BarcodeOutlined /></span>
            <div class="capability-block__titles">
              <h4 class="capability-block__title">{{ t('moPage.capabilityIdentity') }}</h4>
              <p class="capability-block__hint">{{ t('moPage.capabilityIdentityHint') }}</p>
            </div>
          </header>
          <div class="capability-block__body">
            <a-form layout="vertical" class="basic-form">
              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('moPage.moCode')">
                    <a-input
                      v-model:value="formState.selfCode"
                      :placeholder="t('moPage.moCodeAuto')"
                      allow-clear
                    >
                      <template #prefix>
                        <BarcodeOutlined class="input-prefix-icon" />
                      </template>
                    </a-input>
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('moPage.scene')">
                    <div
                      class="picker-input-trigger"
                      role="button"
                      tabindex="0"
                      :aria-label="t('moPage.selectScene')"
                      @click="openScenePicker"
                      @keydown.enter.prevent="openScenePicker"
                      @keydown.space.prevent="openScenePicker"
                    >
                      <a-input
                        :value="formState.sceneName"
                        readonly
                        class="picker-input-trigger__input"
                        :placeholder="t('moPage.scenePlaceholder')"
                      >
                        <template #prefix>
                          <EnvironmentOutlined class="input-prefix-icon" />
                        </template>
                        <template #suffix>
                          <SearchOutlined class="picker-input-trigger__icon" />
                        </template>
                      </a-input>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </div>
        </section>

        <section class="capability-block capability-block--recipe">
          <header class="capability-block__head">
            <span class="capability-block__icon"><AppstoreOutlined /></span>
            <div class="capability-block__titles">
              <h4 class="capability-block__title">{{ t('moPage.capabilityRecipe') }}</h4>
              <p class="capability-block__hint">{{ t('moPage.capabilityRecipeHint') }}</p>
            </div>
            <a-tag v-if="details.length" color="processing">{{ details.length }}</a-tag>
          </header>
          <div class="capability-block__body">
            <div
              class="bom-picker-box"
              :class="{ 'bom-picker-box--empty': !formState.productCode }"
            >
              <div class="bom-picker-box__header">
                <div class="bom-picker-box__title">
                  <span>{{ t('moPage.sectionBom') }}</span>
                  <span class="bom-picker-box__required">*</span>
                </div>
                <a-button type="primary" size="small" @click="bomPickerOpen = true">
                  <SearchOutlined />
                  {{ t('moPage.selectBom') }}
                </a-button>
              </div>
              <div class="bom-picker-box__body">
                <div class="bom-picker-box__item">
                  <span class="bom-picker-box__label">{{ t('moPage.bomName') }}</span>
                  <span
                    class="bom-picker-box__value"
                    :class="{ 'bom-picker-box__value--placeholder': !formState.productName }"
                  >
                    {{ formState.productName || t('moPage.bomNotSelected') }}
                  </span>
                </div>
                <div class="bom-picker-box__item">
                  <span class="bom-picker-box__label">{{ t('moPage.bomCode') }}</span>
                  <span v-if="formState.productCode" class="code-cell">{{ formState.productCode }}</span>
                  <span v-else class="empty-cell">—</span>
                </div>
              </div>
            </div>

            <div class="capability-subsection" :class="{ 'capability-subsection--percent': isPercentType }">
              <div class="capability-subsection__head">
                <div class="capability-subsection__title">{{ t('moPage.sectionBomDetail') }}</div>
                <div v-if="isPercentType" class="bom-detail-percent-qty">
                  <label class="bom-detail-percent-qty__label">
                    {{ t('moPage.percentBomQty') }}
                    <span class="bom-picker-box__required">*</span>
                  </label>
                  <a-input-number
                    v-model:value="formState.weight"
                    :min="0"
                    :disabled="!hasBomSelected"
                    class="bom-detail-percent-qty__input"
                    @blur="calcDetails"
                  />
                </div>
              </div>
              <a-empty v-if="!details.length" class="detail-empty" :description="t('moPage.detailEmpty')" />
              <div v-else class="detail-table-wrap">
                <a-table
                  row-key="matCode"
                  size="small"
                  bordered
                  class="production-module-table detail-table"
                  :columns="detailColumns"
                  :data-source="details"
                  :pagination="false"
                  :scroll="{ y: 220 }"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'matCode'">
                      <span v-if="record.matCode" class="code-cell">{{ record.matCode }}</span>
                      <span v-else class="empty-cell">—</span>
                    </template>
                    <template v-else-if="column.key === 'matNum'">
                      <a-input-number
                        v-model:value="record.matNum"
                        :min="0"
                        size="small"
                        class="full-width-input"
                        @blur="calcDetails"
                      />
                    </template>
                    <template v-else-if="column.key === 'priority'">
                      <a-input-number
                        v-model:value="record.priority"
                        :min="0"
                        size="small"
                        class="full-width-input"
                      />
                    </template>
                  </template>
                </a-table>
              </div>
            </div>
          </div>
        </section>

        <section class="capability-block capability-block--schedule">
          <header class="capability-block__head">
            <span class="capability-block__icon"><CalendarOutlined /></span>
            <div class="capability-block__titles">
              <h4 class="capability-block__title">{{ t('moPage.capabilitySchedule') }}</h4>
              <p class="capability-block__hint">{{ t('moPage.capabilityScheduleHint') }}</p>
            </div>
          </header>
          <div class="capability-block__body">
            <a-form layout="vertical" class="basic-form">
              <a-row :gutter="16">
                <a-col :xs="24" :sm="12">
                  <a-form-item :label="t('moPage.batchQty')" required>
                    <a-input-number v-model:value="formState.num" :min="1" class="full-width-input" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="12">
                  <a-form-item :label="t('moPage.execTime')" required>
                    <a-date-picker
                      v-model:value="formState.execTime"
                      show-time
                      format="YYYY-MM-DD HH:mm:ss"
                      class="full-width-input"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </div>
        </section>
      </div>

      <div class="step-actions">
        <a-button type="primary" @click="nextStep">{{ t('moPage.next') }}</a-button>
      </div>
    </div>

    <div v-show="currentStep === 1" class="step-panel">
      <section class="capability-block capability-block--devg">
        <header class="capability-block__head">
          <span class="capability-block__icon"><ToolOutlined /></span>
          <div class="capability-block__titles">
            <h4 class="capability-block__title">{{ t('moPage.capabilityDevg') }}</h4>
            <p class="capability-block__hint">{{ t('moPage.capabilityDevgHint') }}</p>
          </div>
        </header>
        <div class="capability-block__body">
          <div
            class="picker-input-trigger picker-input-trigger--wide"
            role="button"
            tabindex="0"
            :aria-label="t('moPage.selectDevg')"
            @click="openDevgPicker"
            @keydown.enter.prevent="openDevgPicker"
            @keydown.space.prevent="openDevgPicker"
          >
            <a-input
              :value="formState.devgName"
              readonly
              class="picker-input-trigger__input"
              :placeholder="t('moPage.selectDevg')"
            >
              <template #suffix>
                <SearchOutlined class="picker-input-trigger__icon" />
              </template>
            </a-input>
          </div>
          <div class="detail-table-wrap capability-block__table">
            <a-table
              row-key="matCode"
              size="small"
              bordered
              class="production-module-table detail-table"
              :columns="devgDetailColumns"
              :data-source="details"
              :pagination="false"
              :scroll="{ y: 320 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'matCode'">
                  <span v-if="record.matCode" class="code-cell">{{ record.matCode }}</span>
                  <span v-else class="empty-cell">—</span>
                </template>
                <template v-else-if="column.key === 'devNo'">
                  <span v-if="record.devNo" class="code-cell code-cell--muted">{{ record.devNo }}</span>
                  <span v-else class="empty-cell">—</span>
                </template>
              </template>
            </a-table>
          </div>
        </div>
      </section>

      <div class="step-actions">
        <a-button @click="prevStep">{{ t('moPage.prev') }}</a-button>
        <a-button type="primary" @click="nextStep">{{ t('moPage.next') }}</a-button>
      </div>
    </div>

    <div v-show="currentStep === 2" class="step-panel">
      <section class="capability-block capability-block--line">
        <header class="capability-block__head">
          <span class="capability-block__icon"><NodeIndexOutlined /></span>
          <div class="capability-block__titles">
            <h4 class="capability-block__title">{{ t('moPage.capabilityLine') }}</h4>
            <p class="capability-block__hint">{{ t('moPage.capabilityLineHint') }}</p>
          </div>
          <a-tag v-if="tfList.length" color="blue">{{ tfList.length }}</a-tag>
        </header>
        <div class="capability-block__body">
          <div
            class="picker-input-trigger picker-input-trigger--wide"
            role="button"
            tabindex="0"
            :aria-label="t('moPage.selectLine')"
            @click="openLinePicker"
            @keydown.enter.prevent="openLinePicker"
            @keydown.space.prevent="openLinePicker"
          >
            <a-input
              :value="formState.lineName"
              readonly
              class="picker-input-trigger__input"
              :placeholder="t('moPage.selectLine')"
            >
              <template #suffix>
                <SearchOutlined class="picker-input-trigger__icon" />
              </template>
            </a-input>
          </div>
          <div class="detail-table-wrap capability-block__table">
            <a-table
              row-key="id"
              size="small"
              bordered
              class="production-module-table detail-table"
              :columns="tfColumns"
              :data-source="tfList"
              :row-selection="tfRowSelection"
              :pagination="false"
              :scroll="{ y: 320 }"
            >
              <template #bodyCell="{ column, record, index }">
                <template v-if="column.key === 'index'">{{ index + 1 }}</template>
                <template v-else-if="column.key === 'selfCode'">
                  <span v-if="record.selfCode" class="code-cell">{{ record.selfCode }}</span>
                  <span v-else class="empty-cell">—</span>
                </template>
              </template>
            </a-table>
          </div>
        </div>
      </section>

      <div class="step-actions">
        <a-button @click="prevStep">{{ t('moPage.prev') }}</a-button>
        <a-button type="primary" :loading="saving" @click="submitForm">{{ t('moPage.complete') }}</a-button>
      </div>
    </div>

    <div v-show="currentStep === 3" class="success-panel">
      <CheckCircleOutlined class="success-icon" />
      <div class="success-title">{{ t('moPage.createSuccess') }}</div>
      <a-button type="primary" @click="onClose">{{ t('moPage.close') }}</a-button>
    </div>

    <BomPickerModal v-model:open="bomPickerOpen" @select="onBomSelected" />
    <DevgPickerModal v-model:open="devgPickerOpen" @select="onDevgSelected" />
    <LinePickerModal v-model:open="linePickerOpen" @select="onLineSelected" />
    <WorkshopPickerModal v-model:open="scenePickerOpen" @select="onSceneSelected" />
  </a-modal>
</template>

<style scoped>
.full-width-input {
  width: 100%;
}
</style>
