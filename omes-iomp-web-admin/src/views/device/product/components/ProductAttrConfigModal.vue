<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { fetchEquipAlarmLevels } from '@/api/device'
import type { ProductRecord } from '@/api/product'
import { fetchProductById, saveProduct } from '@/api/product'
import ConfigSpreadsheet, { type SpreadsheetColumn } from '@/components/config/ConfigSpreadsheet.vue'
import type { EquipAlarmRow, EquipAttrRow, EquipControlRow } from '@/types/equip-config'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: ProductRecord | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('attrs')
const product = ref<ProductRecord | null>(null)
const attrs = ref<EquipAttrRow[]>([])
const alarms = ref<EquipAlarmRow[]>([])
const controls = ref<EquipControlRow[]>([])
const alarmLevelOptions = ref<{ value: number; label: string }[]>([])
const jsonModalOpen = ref(false)
const jsonModalTitle = ref('')
const jsonText = ref('')
let jsonApplyTarget: 'attrs' | 'alarms' | 'controls' | 'all' = 'attrs'

const ALARM_TYPES = [
  { value: 0, label: '相等' },
  { value: 1, label: '大于' },
  { value: 2, label: '大于等于' },
  { value: 3, label: '小于' },
  { value: 4, label: '小于等于' },
  { value: 5, label: '范围' },
]

const CONTROL_TYPES = [
  { value: 0, label: '开关量' },
  { value: 1, label: '模拟量' },
]

const title = computed(() =>
  product.value?.name
    ? t('productPage.attrConfigTitle', { name: product.value.name })
    : t('productPage.attrConfigTitleDefault'),
)

const attrColumns = computed<SpreadsheetColumn[]>(() => [
  { key: 'name', title: t('equipAttrPage.attrName'), width: 150 },
  { key: 'unit', title: t('equipAttrPage.attrUnit'), width: 90 },
  { key: 'value', title: t('equipAttrPage.attrValue'), width: 110 },
  { key: 'needCollect', title: t('equipAttrPage.needCollect'), width: 80, type: 'checkbox' },
  { key: 'fluctuationEnabled', title: t('equipAttrPage.fluctuation'), width: 96, type: 'checkbox' },
])

const alarmColumns = computed<SpreadsheetColumn[]>(() => [
  { key: 'name', title: t('equipAttrPage.alarmName'), width: 140 },
  { key: 'type', title: t('equipAttrPage.alarmType'), width: 120, type: 'select', options: ALARM_TYPES },
  { key: 'val', title: t('equipAttrPage.alarmThreshold'), width: 90 },
  { key: 'min', title: t('equipAttrPage.alarmMin'), width: 80 },
  { key: 'max', title: t('equipAttrPage.alarmMax'), width: 80 },
  { key: 'text', title: t('equipAttrPage.alarmText'), width: 160 },
  {
    key: 'level',
    title: t('equipAttrPage.alarmLevel'),
    width: 100,
    type: 'select',
    options: alarmLevelOptions.value,
  },
])

const controlColumns = computed<SpreadsheetColumn[]>(() => [
  { key: 'name', title: t('equipAttrPage.controlName'), width: 140 },
  { key: 'type', title: t('equipAttrPage.controlType'), width: 110, type: 'select', options: CONTROL_TYPES },
  { key: 'unit', title: t('equipAttrPage.controlUnit'), width: 90 },
  { key: 'min', title: t('equipAttrPage.controlMin'), width: 90 },
  { key: 'max', title: t('equipAttrPage.controlMax'), width: 90 },
])

function emptyAttr(): EquipAttrRow {
  return { needCollect: true, fluctuationEnabled: true }
}

function emptyAlarm(): EquipAlarmRow {
  return { type: 2, level: 1 }
}

function emptyControl(): EquipControlRow {
  return { type: 0 }
}

function ensureRows() {
  if (!attrs.value.length) {
    attrs.value = [emptyAttr()]
  }
  if (!alarms.value.length) {
    alarms.value = [emptyAlarm()]
  }
  if (!controls.value.length) {
    controls.value = [emptyControl()]
  }
}

async function loadAlarmLevels() {
  const list = await fetchEquipAlarmLevels()
  alarmLevelOptions.value = (list || []).map((item) => ({
    value: Number(item.id),
    label: item.name,
  }))
}

async function loadConfig() {
  if (!props.record?.id) {
    return
  }
  loading.value = true
  try {
    const detail = await fetchProductById(props.record.id)
    product.value = detail || props.record
    const cfg = detail?.attrConfig || {}
    attrs.value = Array.isArray(cfg.attrs) ? [...cfg.attrs] : []
    alarms.value = Array.isArray(cfg.alarms) ? [...cfg.alarms] : []
    controls.value = Array.isArray(cfg.controls) ? [...cfg.controls] : []
    ensureRows()
  } finally {
    loading.value = false
  }
}

function openJsonEditor(target: typeof jsonApplyTarget, titleKey: string) {
  jsonApplyTarget = target
  jsonModalTitle.value = t(titleKey)
  if (target === 'attrs') {
    jsonText.value = JSON.stringify(attrs.value, null, 2)
  } else if (target === 'alarms') {
    jsonText.value = JSON.stringify(alarms.value, null, 2)
  } else if (target === 'controls') {
    jsonText.value = JSON.stringify(controls.value, null, 2)
  } else {
    jsonText.value = JSON.stringify(
      { attrs: attrs.value, alarms: alarms.value, controls: controls.value },
      null,
      2,
    )
  }
  jsonModalOpen.value = true
}

function applyJson() {
  try {
    const parsed = JSON.parse(jsonText.value || '[]')
    if (jsonApplyTarget === 'attrs') {
      attrs.value = Array.isArray(parsed) ? parsed : []
    } else if (jsonApplyTarget === 'alarms') {
      alarms.value = Array.isArray(parsed) ? parsed : []
    } else if (jsonApplyTarget === 'controls') {
      controls.value = Array.isArray(parsed) ? parsed : []
    } else {
      const obj = parsed && typeof parsed === 'object' ? parsed : {}
      attrs.value = Array.isArray(obj.attrs) ? obj.attrs : []
      alarms.value = Array.isArray(obj.alarms) ? obj.alarms : []
      controls.value = Array.isArray(obj.controls) ? obj.controls : []
    }
    ensureRows()
    jsonModalOpen.value = false
    message.success(t('equipAttrPage.jsonApplied'))
  } catch {
    message.error(t('equipAttrPage.jsonInvalid'))
  }
}

function deleteAttrRow(idx: number) {
  attrs.value.splice(idx, 1)
  if (!attrs.value.length) {
    attrs.value = [emptyAttr()]
  }
}

function deleteAlarmRow(idx: number) {
  alarms.value.splice(idx, 1)
  if (!alarms.value.length) {
    alarms.value = [emptyAlarm()]
  }
}

function deleteControlRow(idx: number) {
  controls.value.splice(idx, 1)
  if (!controls.value.length) {
    controls.value = [emptyControl()]
  }
}

function handleClose() {
  emit('update:open', false)
}

function meaningfulRows<T extends Record<string, unknown>>(rows: T[], keys: (keyof T)[]) {
  return rows.filter((row) => keys.some((key) => {
    const value = row[key]
    return value !== undefined && value !== null && String(value).trim() !== ''
  }))
}

async function handleSave() {
  if (!product.value?.id) {
    return
  }
  saving.value = true
  try {
    await saveProduct({
      id: product.value.id,
      name: product.value.name,
      code: product.value.code,
      imageUrl: product.value.imageUrl,
      attrConfig: {
        attrs: meaningfulRows(attrs.value as Record<string, unknown>[], ['name']) as EquipAttrRow[],
        alarms: meaningfulRows(alarms.value as Record<string, unknown>[], ['name', 'text']) as EquipAlarmRow[],
        controls: meaningfulRows(controls.value as Record<string, unknown>[], ['name']) as EquipControlRow[],
      },
    })
    message.success(t('productPage.saveSuccess'))
    emit('update:open', false)
    emit('success')
  } finally {
    saving.value = false
  }
}

watch(
  () => [props.open, props.record?.id] as const,
  async ([open, productId]) => {
    if (!open || !productId) {
      return
    }
    activeTab.value = 'attrs'
    await loadAlarmLevels()
    await loadConfig()
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    width="92%"
    destroy-on-close
    wrap-class-name="attr-modal-wrap"
    class="attr-modal"
    :mask-closable="false"
    @update:open="emit('update:open', $event)"
    @cancel="handleClose"
  >
    <a-spin :spinning="loading" class="attr-modal-spin">
      <div class="attr-modal-inner">
        <a-alert type="info" show-icon class="product-attr-hint" :message="t('productPage.attrConfigHint')" />
        <div class="tabs-wrap">
          <a-tabs v-model:activeKey="activeTab" class="config-tabs">
            <a-tab-pane key="attrs" :tab="t('productPage.tabAttrs')">
              <div class="tab-pane-fill">
                <div class="tab-toolbar">
                  <a-space>
                    <a-button size="small" @click="attrs.push(emptyAttr())">{{ t('equipAttrPage.addRow') }}</a-button>
                    <a-button size="small" @click="openJsonEditor('attrs', 'equipAttrPage.jsonAttrs')">
                      {{ t('equipAttrPage.jsonMode') }}
                    </a-button>
                  </a-space>
                </div>
                <ConfigSpreadsheet
                  fill
                  :columns="attrColumns"
                  :rows="attrs as Record<string, unknown>[]"
                  @delete-row="deleteAttrRow"
                />
              </div>
            </a-tab-pane>

            <a-tab-pane key="alarms" :tab="t('productPage.tabAlarms')">
              <div class="tab-pane-fill">
                <div class="tab-toolbar">
                  <a-space>
                    <a-button size="small" @click="alarms.push(emptyAlarm())">{{ t('equipAttrPage.addRow') }}</a-button>
                    <a-button size="small" @click="openJsonEditor('alarms', 'equipAttrPage.jsonAlarms')">
                      {{ t('equipAttrPage.jsonMode') }}
                    </a-button>
                  </a-space>
                </div>
                <ConfigSpreadsheet
                  fill
                  :columns="alarmColumns"
                  :rows="alarms as Record<string, unknown>[]"
                  @delete-row="deleteAlarmRow"
                />
              </div>
            </a-tab-pane>

            <a-tab-pane key="controls" :tab="t('productPage.tabControls')">
              <div class="tab-pane-fill">
                <div class="tab-toolbar">
                  <a-space>
                    <a-button size="small" @click="controls.push(emptyControl())">{{ t('equipAttrPage.addRow') }}</a-button>
                    <a-button size="small" @click="openJsonEditor('controls', 'equipAttrPage.jsonControls')">
                      {{ t('equipAttrPage.jsonMode') }}
                    </a-button>
                  </a-space>
                </div>
                <ConfigSpreadsheet
                  fill
                  :columns="controlColumns"
                  :rows="controls as Record<string, unknown>[]"
                  @delete-row="deleteControlRow"
                />
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </div>
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="openJsonEditor('all', 'equipAttrPage.jsonAll')">{{ t('equipAttrPage.jsonAll') }}</a-button>
        <a-button @click="handleClose">{{ t('equipAttrPage.cancel') }}</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">{{ t('productPage.save') }}</a-button>
      </a-space>
    </template>
  </a-modal>

  <a-modal v-model:open="jsonModalOpen" :title="jsonModalTitle" width="720px" @ok="applyJson">
    <a-textarea v-model:value="jsonText" :rows="16" style="font-family: Consolas, monospace" />
  </a-modal>
</template>

<style>
.attr-modal-wrap .ant-modal {
  top: 24px;
  padding-bottom: 24px;
  max-width: calc(92vw + 16px);
}

.attr-modal-wrap .ant-modal-content {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 48px);
  max-height: calc(100vh - 48px);
}

.attr-modal-wrap .ant-modal-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 12px 20px;
  display: flex;
  flex-direction: column;
}

.attr-modal-wrap .ant-modal-footer {
  flex-shrink: 0;
}
</style>

<style scoped>
.attr-modal-spin,
.attr-modal-spin :deep(.ant-spin-nested-loading),
.attr-modal-spin :deep(.ant-spin-container) {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.attr-modal-inner {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.product-attr-hint {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.tabs-wrap {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.config-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.config-tabs :deep(.ant-tabs-nav) {
  flex-shrink: 0;
  margin-bottom: 8px;
}

.config-tabs :deep(.ant-tabs-content-holder),
.config-tabs :deep(.ant-tabs-content),
.config-tabs :deep(.ant-tabs-tabpane) {
  height: 100%;
  min-height: 0;
}

.config-tabs :deep(.ant-tabs-content) {
  display: flex;
}

.config-tabs :deep(.ant-tabs-tabpane-hidden) {
  display: none !important;
}

.config-tabs :deep(.ant-tabs-tabpane-active) {
  display: flex;
  flex-direction: column;
}

.tab-pane-fill {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tab-toolbar {
  flex-shrink: 0;
  margin-bottom: 10px;
}
</style>
