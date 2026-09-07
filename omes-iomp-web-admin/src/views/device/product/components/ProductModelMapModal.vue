<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ProductRecord } from '@/api/product'
import { fetchProductByCode } from '@/api/product'
import type { ProductModelRecord } from '@/api/product-model'
import { fetchProductModelById, saveProductModel } from '@/api/product-model'
import type { EquipAlarmRow, EquipAttrRow, EquipControlRow } from '@/types/equip-config'
import {
  hasProductAttrTemplate,
  mergeProductAttrConfig,
  stripProductAttrMeta,
} from '@/utils/product-attr-map'
import { message } from 'ant-design-vue'

const props = defineProps<{
  product: ProductRecord | null
  record: ProductModelRecord | null
}>()

const emit = defineEmits<{
  success: []
  cancel: []
}>()

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const productDetail = ref<ProductRecord | null>(null)
const modelDetail = ref<ProductModelRecord | null>(null)
const runMap = ref('')
const attrs = ref<EquipAttrRow[]>([])
const alarms = ref<EquipAlarmRow[]>([])
const controls = ref<EquipControlRow[]>([])

const hasProductTemplate = computed(() => hasProductAttrTemplate(productDetail.value?.attrConfig))

const hint = computed(() => {
  if (!hasProductTemplate.value) {
    return t('productPage.modelMapNoTemplateHint')
  }
  return t('productPage.modelMapHint', { name: productDetail.value?.name || props.product?.name })
})

const namedAttrs = computed(() => attrs.value.filter((row) => String(row.name || '').trim()))
const namedAlarms = computed(() =>
  alarms.value.filter((row) => String(row.name || '').trim() || String(row.text || '').trim()),
)
const namedControls = computed(() => controls.value.filter((row) => String(row.name || '').trim()))

function applyMerged(modelConfig?: ProductModelRecord['attrConfig']) {
  const merged = mergeProductAttrConfig(productDetail.value?.attrConfig, {
    runMap: modelConfig?.runMap || runMap.value,
    attrs: modelConfig?.attrs || attrs.value,
    alarms: modelConfig?.alarms || alarms.value,
    controls: modelConfig?.controls || controls.value,
  })
  runMap.value = merged.runMap || ''
  attrs.value = merged.attrs || []
  alarms.value = merged.alarms || []
  controls.value = merged.controls || []
}

async function loadConfig() {
  if (!props.record?.id) {
    return
  }
  loading.value = true
  try {
    const productCode = String(props.product?.code || props.record.productCode || '').trim()
    const [model, product] = await Promise.all([
      fetchProductModelById(props.record.id),
      productCode ? fetchProductByCode(productCode).catch(() => null) : Promise.resolve(null),
    ])
    modelDetail.value = model
    productDetail.value = product
    applyMerged(model?.attrConfig || {})
  } finally {
    loading.value = false
  }
}

function withMap<T extends { name?: string; map?: string }>(rows: T[]): T[] {
  return stripProductAttrMeta(rows).filter((row) => String(row.name || '').trim())
}

async function handleSave() {
  if (saving.value) {
    return
  }
  const current = modelDetail.value || props.record
  if (!current?.id) {
    return
  }
  if (!hasProductTemplate.value) {
    message.warning(t('productPage.modelMapNoTemplateHint'))
    return
  }
  saving.value = true
  try {
    await saveProductModel({
      id: current.id,
      productCode: current.productCode || props.product?.code,
      name: current.name,
      code: current.code,
      attrConfig: {
        runMap: runMap.value.trim(),
        attrs: withMap(attrs.value),
        alarms: withMap(alarms.value),
        controls: withMap(controls.value),
      },
    })
    message.success(t('productPage.saveSuccess'))
    emit('success')
  } finally {
    saving.value = false
  }
}

watch(
  () => props.record?.id,
  async (id) => {
    if (!id) {
      return
    }
    await loadConfig()
  },
  { immediate: true },
)
</script>

<template>
  <a-spin :spinning="loading" class="model-map-spin">
    <div class="model-map-panel">
      <a-alert
        class="model-map-hint"
        :type="hasProductTemplate ? 'info' : 'warning'"
        show-icon
        :message="hint"
      />

      <section class="map-section">
        <div class="map-section__title">{{ t('equipAttrPage.runMap') }}</div>
        <a-input
          v-model:value="runMap"
          allow-clear
          :disabled="!hasProductTemplate"
          :placeholder="t('productPage.modelMapPlaceholder')"
        />
      </section>

      <template v-if="hasProductTemplate">
        <section v-if="namedAttrs.length" class="map-section">
          <div class="map-section__title">
            {{ t('equipAttrPage.tabAttrs') }}
            <span class="map-section__count">{{ namedAttrs.length }}</span>
          </div>
          <div v-for="(row, idx) in namedAttrs" :key="`attr-${idx}`" class="map-row">
            <div class="map-row__meta">
              <span class="map-row__name" :title="row.name">{{ row.name }}</span>
              <span v-if="row.unit" class="map-row__unit">{{ row.unit }}</span>
            </div>
            <a-input
              v-model:value="row.map"
              allow-clear
              :placeholder="t('productPage.modelMapPlaceholder')"
            />
          </div>
        </section>

        <section v-if="namedAlarms.length" class="map-section">
          <div class="map-section__title">
            {{ t('equipAttrPage.tabAlarms') }}
            <span class="map-section__count">{{ namedAlarms.length }}</span>
          </div>
          <div v-for="(row, idx) in namedAlarms" :key="`alarm-${idx}`" class="map-row">
            <div class="map-row__meta">
              <span class="map-row__name" :title="row.name || row.text">{{ row.name || row.text }}</span>
            </div>
            <a-input
              v-model:value="row.map"
              allow-clear
              :placeholder="t('productPage.modelMapPlaceholder')"
            />
          </div>
        </section>

        <section v-if="namedControls.length" class="map-section">
          <div class="map-section__title">
            {{ t('equipAttrPage.tabControls') }}
            <span class="map-section__count">{{ namedControls.length }}</span>
          </div>
          <div v-for="(row, idx) in namedControls" :key="`ctrl-${idx}`" class="map-row">
            <div class="map-row__meta">
              <span class="map-row__name" :title="row.name">{{ row.name }}</span>
              <span v-if="row.unit" class="map-row__unit">{{ row.unit }}</span>
            </div>
            <a-input
              v-model:value="row.map"
              allow-clear
              :placeholder="t('productPage.modelMapPlaceholder')"
            />
          </div>
        </section>
      </template>

      <div class="model-map-footer">
        <a-button @click="emit('cancel')">{{ t('productPage.modelBack') }}</a-button>
        <a-button type="primary" :loading="saving" :disabled="!hasProductTemplate" @click="handleSave">
          {{ t('productPage.save') }}
        </a-button>
      </div>
    </div>
  </a-spin>
</template>

<style scoped>
.model-map-spin,
.model-map-spin :deep(.ant-spin-nested-loading),
.model-map-spin :deep(.ant-spin-container) {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.model-map-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.model-map-hint {
  flex-shrink: 0;
}

.map-section {
  padding: 14px 16px;
  background: var(--omes-color-bg-container);
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-md);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.map-section__title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--omes-color-border);
  font-weight: 600;
  color: var(--omes-color-text);
}

.map-section__count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 9px;
  font-size: 12px;
  font-weight: 500;
  color: var(--omes-color-primary);
  background: var(--omes-color-primary-bg);
}

.map-row {
  display: grid;
  grid-template-columns: minmax(120px, 200px) 1fr;
  gap: 12px;
  align-items: center;
  padding: 6px 8px;
  border-radius: var(--omes-radius-sm);
}

.map-row:hover {
  background: var(--omes-color-bg-elevated);
}

.map-row + .map-row {
  margin-top: 4px;
}

.map-row__meta {
  min-width: 0;
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.map-row__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.map-row__unit {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--omes-color-text-secondary);
}

.model-map-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  position: sticky;
  bottom: 0;
  z-index: 1;
  margin-top: 4px;
  padding: 12px 0 4px;
  border-top: 1px solid var(--omes-color-border);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.72) 0%, var(--omes-color-bg-container) 40%);
}
</style>
