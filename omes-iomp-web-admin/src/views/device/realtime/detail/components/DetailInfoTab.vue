<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { CopyOutlined } from '@ant-design/icons-vue'
import type { EquipAttrItem, EquipControlItem, EquipRecord } from '@/api/device'
import { fetchEquipConfig } from '@/api/device'
import { writeEquipControl } from '@/api/equip-detail'
import type { EquipAttrRow, EquipControlRow } from '@/types/equip-config'
import { formatEquipStateText } from '@/utils/equip-status'

const props = defineProps<{
  equip: EquipRecord | null
  equipId: string
}>()

const emit = defineEmits<{
  refreshed: []
}>()

const { t } = useI18n()

const configLoading = ref(false)
const controlRows = ref<EquipControlRow[]>([])
const displayAttrs = ref<EquipAttrItem[]>([])
const analogInputs = ref<Record<number, number | null>>({})
const writingKey = ref('')

const hasAttrs = computed(() => displayAttrs.value.length > 0)
const hasControls = computed(() => controlRows.value.length > 0)

function attrValueText(value: unknown): string {
  if (value == null || value === '') {
    return '-'
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}

function mergeAttrValues(configAttrs: EquipAttrRow[], realtimeAttrs?: EquipAttrItem[]) {
  const valueMap = new Map<string, EquipAttrItem>()
  for (const item of realtimeAttrs || []) {
    if (item.name) {
      valueMap.set(item.name, item)
    }
  }
  if (configAttrs.length) {
    displayAttrs.value = configAttrs.map((row) => {
      const rt = row.name ? valueMap.get(row.name) : undefined
      return {
        name: row.name,
        unit: row.unit ?? rt?.unit,
        value: rt?.value ?? row.value,
      }
    })
    return
  }
  displayAttrs.value = realtimeAttrs || []
}

function mergeControlValues(configControls: EquipControlRow[], realtimeControls?: EquipControlItem[]) {
  const valueMap = new Map<string, unknown>()
  for (const item of realtimeControls || []) {
    if (item.map) {
      valueMap.set(item.map, item.value)
    }
  }
  controlRows.value = configControls.map((row) => ({
    ...row,
    value: row.map && valueMap.has(row.map) ? String(valueMap.get(row.map) ?? '') : row.value,
  }))
}

async function loadConfig() {
  if (!props.equipId) {
    return
  }
  configLoading.value = true
  try {
    const binding = await fetchEquipConfig(props.equipId)
    const configAttrs = binding?.config?.attrs || []
    const configControls = binding?.config?.controls || []
    mergeAttrValues(configAttrs, props.equip?.attrs)
    mergeControlValues(configControls, props.equip?.controls)
  } finally {
    configLoading.value = false
  }
}

async function copyText(text: string) {
  try {
    await navigator.clipboard.writeText(text)
    message.success(t('equipDetailPage.copied'))
  } catch {
    message.error(t('equipDetailPage.copyFailed'))
  }
}

function controlCurrent(ctrl: EquipControlRow) {
  return ctrl.value
}

function controlNumeric(ctrl: EquipControlRow): number | null {
  const raw = ctrl.value
  if (raw == null || raw === '') {
    return null
  }
  const num = Number(raw)
  return Number.isNaN(num) ? null : num
}

function isDigital(ctrl: EquipControlRow) {
  return ctrl.type === 0 || ctrl.type == null
}

async function writeDigital(ctrl: EquipControlRow, value: 0 | 1, idx: number) {
  if (!props.equipId || !ctrl.map) {
    return
  }
  const key = `${idx}-${value}`
  writingKey.value = key
  try {
    await writeEquipControl({ equipId: props.equipId, address: ctrl.map, value })
    message.success(t('equipDetailPage.writeSuccess'))
    emit('refreshed')
  } finally {
    writingKey.value = ''
  }
}

async function writeAnalog(ctrl: EquipControlRow, idx: number) {
  if (!props.equipId || !ctrl.map) {
    return
  }
  const raw = analogInputs.value[idx]
  if (raw == null) {
    message.warning(t('equipDetailPage.inputRequired'))
    return
  }
  writingKey.value = `analog-${idx}`
  try {
    await writeEquipControl({ equipId: props.equipId, address: ctrl.map, value: raw })
    message.success(t('equipDetailPage.writeSuccess'))
    emit('refreshed')
  } finally {
    writingKey.value = ''
  }
}

watch(
  () => [props.equipId, props.equip?.id, props.equip?.attrs, props.equip?.controls] as const,
  () => {
    void loadConfig()
  },
  { immediate: true },
)
</script>

<template>
  <a-spin :spinning="configLoading">
    <section class="info-section">
      <h3 class="section-title">{{ t('equipDetailPage.basicInfo') }}</h3>
      <div class="info-grid">
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.name') }}</span>
          <span class="info-value">{{ equip?.name || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.code') }}</span>
          <span class="info-value">
            {{ equip?.selfCode || '-' }}
            <a-button
              v-if="equip?.selfCode"
              type="link"
              size="small"
              @click="copyText(equip.selfCode!)"
            >
              <CopyOutlined /> {{ t('equipDetailPage.copy') }}
            </a-button>
          </span>
        </div>
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.type') }}</span>
          <span class="info-value">{{ equip?.typeDesc || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.onlineState') }}</span>
          <span class="info-value">{{ formatEquipStateText(t, 'online', equip?.onlineState) }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.runState') }}</span>
          <span class="info-value">{{ formatEquipStateText(t, 'run', equip?.runState) }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.alarmState') }}</span>
          <span class="info-value">{{ formatEquipStateText(t, 'alarm', equip?.alarmState) }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.address') }}</span>
          <span class="info-value">{{ equip?.workshop?.address || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.lngLat') }}</span>
          <span class="info-value">{{ equip?.workshop?.lng ?? '-' }} / {{ equip?.workshop?.lat ?? '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.createTime') }}</span>
          <span class="info-value">{{ equip?.createTime || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">{{ t('equipDetailPage.lastOnline') }}</span>
          <span class="info-value">{{ equip?.onlineChangeTime || '-' }}</span>
        </div>
      </div>
    </section>

    <section v-if="hasAttrs" class="info-section">
      <h3 class="section-title">{{ t('equipDetailPage.attrs') }}</h3>
      <div class="attrs-grid">
        <div v-for="(attr, idx) in displayAttrs" :key="`${attr.name}-${idx}`" class="attr-card">
          <div class="attr-label">{{ attr.name || '-' }}</div>
          <div class="attr-value">
            {{ attrValueText(attr.value) }}
            <small v-if="attr.unit">{{ attr.unit }}</small>
          </div>
        </div>
      </div>
    </section>

    <section v-if="hasControls" class="info-section">
      <h3 class="section-title">{{ t('equipDetailPage.controls') }}</h3>
      <div class="ctrl-grid">
        <div v-for="(ctrl, idx) in controlRows" :key="`${ctrl.map}-${idx}`" class="ctrl-card">
          <div class="ctrl-head">
            <span class="ctrl-name">{{ ctrl.name || '-' }}</span>
            <span class="ctrl-type" :class="isDigital(ctrl) ? 'is-digital' : 'is-analog'">
              {{ isDigital(ctrl) ? t('equipDetailPage.digital') : t('equipDetailPage.analog') }}
            </span>
          </div>
          <div class="ctrl-map">{{ t('equipDetailPage.addressMap') }}: {{ ctrl.map || '-' }}</div>
          <div class="ctrl-current">
            {{ t('equipDetailPage.currentValue') }}:
            <strong>{{ controlCurrent(ctrl) ?? '-' }}</strong>
            <span v-if="ctrl.unit && !isDigital(ctrl)">{{ ctrl.unit }}</span>
          </div>
          <div v-if="isDigital(ctrl)" class="ctrl-actions">
            <a-button
              size="small"
              :type="controlNumeric(ctrl) === 1 ? 'primary' : 'default'"
              :loading="writingKey === `${idx}-1`"
              @click="writeDigital(ctrl, 1, idx)"
            >
              ON
            </a-button>
            <a-button
              size="small"
              danger
              :type="controlNumeric(ctrl) === 0 ? 'primary' : 'default'"
              :loading="writingKey === `${idx}-0`"
              @click="writeDigital(ctrl, 0, idx)"
            >
              OFF
            </a-button>
          </div>
          <div v-else class="ctrl-analog">
            <a-input-number
              v-model:value="analogInputs[idx]"
              size="small"
              class="ctrl-input"
              :placeholder="t('equipDetailPage.inputValue')"
            />
            <span v-if="ctrl.unit" class="ctrl-unit">{{ ctrl.unit }}</span>
            <a-button
              size="small"
              type="primary"
              :loading="writingKey === `analog-${idx}`"
              @click="writeAnalog(ctrl, idx)"
            >
              {{ t('equipDetailPage.write') }}
            </a-button>
          </div>
        </div>
      </div>
    </section>
  </a-spin>
</template>

<style scoped>
.info-section {
  padding: 16px 20px;
  background: var(--omes-color-bg-container);
  border: 1px solid #e8eef5;
  border-radius: 14px;
}

.info-section + .info-section {
  margin-top: 16px;
}

.section-title {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  padding-bottom: 10px;
  border-bottom: 1px solid #eef2f7;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 24px;
}

.info-row {
  display: flex;
  gap: 12px;
  font-size: 13px;
  min-width: 0;
}

.info-label {
  width: 96px;
  flex-shrink: 0;
  color: #64748b;
}

.info-value {
  color: #0f172a;
  min-width: 0;
  word-break: break-all;
}

.attrs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.attr-card {
  padding: 16px 14px;
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: linear-gradient(180deg, #eff6ff 0%, #fff 100%);
  text-align: center;
}

.attr-card:nth-child(6n + 2) {
  border-color: #bbf7d0;
  background: linear-gradient(180deg, #ecfdf5 0%, #fff 100%);
}

.attr-card:nth-child(6n + 3) {
  border-color: #fde68a;
  background: linear-gradient(180deg, #fffbeb 0%, #fff 100%);
}

.attr-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}

.attr-value {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.attr-value small {
  margin-left: 4px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
}

.ctrl-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.ctrl-card {
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e8eef5;
  background: #fafcff;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ctrl-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.ctrl-name {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.ctrl-type {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid transparent;
}

.ctrl-type.is-digital {
  color: #0369a1;
  background: #f0f9ff;
  border-color: #bae6fd;
}

.ctrl-type.is-analog {
  color: #a16207;
  background: #fffbeb;
  border-color: #fde68a;
}

.ctrl-map,
.ctrl-current {
  font-size: 12px;
  color: #64748b;
}

.ctrl-actions,
.ctrl-analog {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ctrl-input {
  width: 120px;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
