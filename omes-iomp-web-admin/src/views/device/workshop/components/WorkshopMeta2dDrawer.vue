<script setup lang="ts">
import { computed, nextTick, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { WorkshopNode } from '@/api/device'
import {
  fetchWorkshopMeta2dConfig,
  fetchWorkshopRealtimeCollect,
  saveWorkshopMeta2dConfig,
} from '@/api/workshop-config'
import type { Meta2dInstance, WorkshopMeta2dBinding, WorkshopMeta2dConfig } from '@/types/workshop-config'
import { isMeta2dReady, loadMeta2dAssets } from '@/utils/meta2d-loader'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  workshop: WorkshopNode | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const meta2dReady = ref(false)
const meta2dError = ref('')
const canvasContainerId = 'workshop-meta2d-canvas'

const penId = ref('')
const bindProp = ref('text')
const sourceKey = ref<string | undefined>(undefined)
const attrOptions = ref<{ value: string; label: string }[]>([])

const meta2dConfig = ref<WorkshopMeta2dConfig>({
  canvas: null,
  bindings: [],
  refreshIntervalSec: 2,
})

let meta2d: Meta2dInstance | null = null

const bindingsPreview = computed(() =>
  JSON.stringify(meta2dConfig.value.bindings || [], null, 2),
)

const bindPropOptions = [
  { value: 'text', label: 'text' },
  { value: 'color', label: 'color' },
  { value: 'visible', label: 'visible' },
]

function ensureMeta2dInstance(): boolean {
  if (!isMeta2dReady()) {
    return false
  }
  if (!meta2d) {
    meta2d = new window.Meta2d!(canvasContainerId)
    meta2d.on('active', (pens) => {
      if (pens?.length) {
        penId.value = pens[0].id || ''
      }
    })
  }
  return true
}

function openCanvas(data: WorkshopMeta2dConfig) {
  if (!ensureMeta2dInstance()) {
    return
  }
  try {
    if (data.canvas) {
      meta2d!.open(data.canvas)
    } else {
      meta2d!.open({ pens: [] })
    }
  } catch {
    /* canvas parse */
  }
}

async function loadAttrs() {
  if (!props.workshop?.selfCode) {
    attrOptions.value = []
    return
  }
  const rows = await fetchWorkshopRealtimeCollect(props.workshop.selfCode)
  attrOptions.value = (rows || [])
    .filter((r) => r.map)
    .map((r) => ({
      value: r.map!,
      label: `${r.name || ''}${r.unit ? ` [${r.unit}]` : ''} ${r.map}`.trim(),
    }))
}

async function loadConfig() {
  if (!props.workshop?.id) {
    return
  }
  loading.value = true
  meta2dError.value = ''
  try {
    await loadMeta2dAssets()
    meta2dReady.value = isMeta2dReady()
    if (!meta2dReady.value) {
      meta2dError.value = t('workshopMeta2dPage.loadError')
      return
    }
    await loadAttrs()
    const dto = await fetchWorkshopMeta2dConfig(props.workshop.id)
    meta2dConfig.value = dto?.meta2dConfig
      ? {
          canvas: dto.meta2dConfig.canvas ?? null,
          bindings: [...(dto.meta2dConfig.bindings || [])],
          refreshIntervalSec: dto.meta2dConfig.refreshIntervalSec ?? 2,
        }
      : { canvas: null, bindings: [], refreshIntervalSec: 2 }
    await nextTick()
    openCanvas(meta2dConfig.value)
  } catch (e) {
    meta2dError.value = e instanceof Error ? e.message : t('workshopMeta2dPage.loadError')
  } finally {
    loading.value = false
  }
}

function readCanvasFromEditor(): Record<string, unknown> | null {
  if (!meta2d) {
    return null
  }
  try {
    return meta2d.data?.() ?? meta2d.canvas?.data?.() ?? null
  } catch {
    return null
  }
}

async function handleSave() {
  if (!props.workshop?.id) {
    return
  }
  if (!meta2dReady.value) {
    message.warning(meta2dError.value || t('workshopMeta2dPage.loadError'))
    return
  }
  saving.value = true
  try {
    await saveWorkshopMeta2dConfig({
      workshopId: props.workshop.id,
      meta2dConfig: {
        canvas: readCanvasFromEditor(),
        bindings: meta2dConfig.value.bindings || [],
        refreshIntervalSec: meta2dConfig.value.refreshIntervalSec || 2,
      },
    })
    message.success(t('workshopMeta2dPage.saveSuccess'))
    emit('update:open', false)
    emit('success')
  } finally {
    saving.value = false
  }
}

function handleBind() {
  const id = penId.value.trim()
  const key = sourceKey.value
  if (!id) {
    message.warning(t('workshopMeta2dPage.selectPenHint'))
    return
  }
  if (!key) {
    message.warning(t('workshopMeta2dPage.selectAttrHint'))
    return
  }
  const list = [...(meta2dConfig.value.bindings || [])]
  const idx = list.findIndex((b) => b.penId === id && b.prop === bindProp.value)
  const item: WorkshopMeta2dBinding = {
    penId: id,
    prop: bindProp.value,
    sourceType: 'WORKSHOP_ATTR',
    sourceKey: key,
    options: {},
  }
  if (idx >= 0) {
    list[idx] = item
  } else {
    list.push(item)
  }
  meta2dConfig.value.bindings = list
  message.success(t('workshopMeta2dPage.bindUpdated'))
}

function handleUnbind() {
  const id = penId.value.trim()
  if (!id) {
    message.warning(t('workshopMeta2dPage.selectPenHint'))
    return
  }
  meta2dConfig.value.bindings = (meta2dConfig.value.bindings || []).filter((b) => b.penId !== id)
  message.success(t('workshopMeta2dPage.unbindDone'))
}

function openPreview() {
  if (!props.workshop?.id) {
    return
  }
  const qs = new URLSearchParams({
    workshopId: props.workshop.id,
    ...(props.workshop.selfCode ? { workshopCode: props.workshop.selfCode } : {}),
  })
  window.open(`/view/workshop_meta2d_view?${qs.toString()}`, '_blank')
}

function destroyMeta2d() {
  meta2d = null
}

watch(
  () => [props.open, props.workshop?.id] as const,
  async ([open, id]) => {
    if (open && id) {
      await loadConfig()
    } else if (!open) {
      destroyMeta2d()
      meta2dReady.value = false
    }
  },
)

onUnmounted(() => {
  destroyMeta2d()
})
</script>

<template>
  <a-modal
    :open="open"
    :title="t('workshopMeta2dPage.title', { name: workshop?.name || '' })"
    width="1200px"
    destroy-on-close
    :mask-closable="false"
    class="workshop-config-modal meta2d-modal"
    :styles="{ body: { padding: 0, maxHeight: '78vh', overflow: 'hidden' } }"
    @update:open="emit('update:open', $event)"
    @cancel="emit('update:open', false)"
  >
    <a-spin :spinning="loading" class="meta2d-spin">
      <div class="meta2d-layout">
        <div class="meta2d-toolbar">
          <a-space>
            <a-tag color="processing">{{ t('workshopMeta2dPage.badge') }}</a-tag>
            <span class="workshop-label">{{ workshop?.name || workshop?.selfCode }}</span>
          </a-space>
          <a-space>
            <a-button size="small" @click="loadConfig">{{ t('workshopMeta2dPage.reload') }}</a-button>
            <a-button size="small" @click="openPreview">{{ t('workshopMeta2dPage.preview') }}</a-button>
          </a-space>
        </div>

        <a-alert
          v-if="meta2dError"
          type="warning"
          show-icon
          class="meta2d-alert"
          :message="meta2dError"
        />

        <div v-else class="meta2d-body">
          <section class="side-panel canvas-panel">
            <header class="side-title">{{ t('workshopMeta2dPage.canvas') }}</header>
            <div :id="canvasContainerId" class="meta2d-canvas" />
          </section>

          <section class="side-panel bind-panel">
            <header class="side-title">{{ t('workshopMeta2dPage.binding') }}</header>
            <div class="bind-form">
              <a-form layout="vertical" size="small">
                <a-form-item :label="t('workshopMeta2dPage.penId')">
                  <a-input v-model:value="penId" readonly :placeholder="t('workshopMeta2dPage.penHint')" />
                </a-form-item>
                <a-form-item :label="t('workshopMeta2dPage.prop')">
                  <a-select v-model:value="bindProp" :options="bindPropOptions" />
                </a-form-item>
                <a-form-item :label="t('workshopMeta2dPage.dataSource')">
                  <a-select
                    v-model:value="sourceKey"
                    allow-clear
                    show-search
                    option-filter-prop="label"
                    :placeholder="t('workshopMeta2dPage.attrPlaceholder')"
                    :options="attrOptions"
                  />
                </a-form-item>
                <a-form-item :label="t('workshopMeta2dPage.refreshSec')">
                  <a-input-number
                    v-model:value="meta2dConfig.refreshIntervalSec"
                    :min="1"
                    :max="60"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-form>
              <a-space direction="vertical" style="width: 100%">
                <a-button type="primary" block @click="handleBind">
                  {{ t('workshopMeta2dPage.bind') }}
                </a-button>
                <a-button danger block @click="handleUnbind">
                  {{ t('workshopMeta2dPage.unbind') }}
                </a-button>
              </a-space>
              <div class="bindings-block">
                <div class="bindings-title">{{ t('workshopMeta2dPage.bindingList') }}</div>
                <pre class="bindings-pre">{{ bindingsPreview }}</pre>
              </div>
            </div>
          </section>
        </div>
      </div>
    </a-spin>

    <template #footer>
      <a-button @click="emit('update:open', false)">{{ t('workshopMeta2dPage.cancel') }}</a-button>
      <a-button type="primary" :loading="saving" @click="handleSave">
        {{ t('workshopMeta2dPage.save') }}
      </a-button>
    </template>
  </a-modal>
</template>

<style scoped>
.meta2d-spin {
  min-height: 0;
  height: 100%;
}

.meta2d-spin :deep(.ant-spin-container) {
  min-height: calc(78vh - 108px);
  display: flex;
  flex-direction: column;
}

.meta2d-layout {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.meta2d-toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--omes-color-border);
  background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
}

.workshop-label {
  font-weight: 600;
  color: var(--omes-color-text-label);
}

.meta2d-alert {
  margin: 16px;
}

.meta2d-body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 0;
}

.side-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  border-right: 1px solid var(--omes-color-border);
}

.bind-panel {
  border-right: none;
  background: var(--omes-color-bg-elevated);
}

.side-title {
  flex-shrink: 0;
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 600;
  border-bottom: 1px solid var(--omes-color-border);
  background: var(--omes-color-bg-container);
}

.canvas-panel .side-title {
  background: #fafbff;
}

.meta2d-canvas {
  flex: 1;
  min-height: 420px;
  background: #f7f8fa;
}

.bind-form {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 12px 14px 16px;
}

.bindings-block {
  margin-top: 16px;
}

.bindings-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--omes-color-text-quaternary);
  margin-bottom: 8px;
}

.bindings-pre {
  margin: 0;
  padding: 10px;
  max-height: 200px;
  overflow: auto;
  font-size: 11px;
  line-height: 1.5;
  background: #0b1020;
  color: #d7e2ff;
  border-radius: var(--omes-radius-md);
}

@media (max-width: 992px) {
  .meta2d-body {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr auto;
  }

  .bind-panel {
    max-height: 360px;
  }
}
</style>
