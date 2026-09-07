<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { ExpandOutlined, FullscreenExitOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import {
  fetchWorkshopMeta2dConfig,
  fetchWorkshopRealtimeCollect,
} from '@/api/workshop-config'
import type {
  Meta2dInstance,
  WorkshopMeta2dBinding,
  WorkshopMeta2dConfig,
  WorkshopRealtimeCollectItem,
} from '@/types/workshop-config'
import { isMeta2dReady, loadMeta2dAssets } from '@/utils/meta2d-loader'
import { message } from 'ant-design-vue'
import { safeExitFullscreen } from '@/utils/fullscreen'

const { t } = useI18n()
const route = useRoute()

const canvasContainerId = 'workshop-meta2d-preview-canvas'
const loading = ref(false)
const meta2dError = ref('')
const statusText = ref('')
const stageRef = ref<HTMLElement | null>(null)
const isFullscreen = ref(false)

let meta2d: Meta2dInstance | null = null
let refreshTimer: ReturnType<typeof setInterval> | null = null
let meta2dConfig: WorkshopMeta2dConfig | null = null

const workshopId = computed(() => String(route.query.workshopId || '').trim())
const workshopCode = computed(() => String(route.query.workshopCode || '').trim())

function clearRefreshTimer() {
  if (refreshTimer != null) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

function buildCollectDict(rows: WorkshopRealtimeCollectItem[]): Record<string, unknown> {
  const dict: Record<string, unknown> = {}
  rows.forEach((row) => {
    const key = row.map || row.name
    if (key) {
      dict[key] = row.value ?? ''
    }
  })
  return dict
}

function applyBindings(dict: Record<string, unknown>) {
  const bindings = meta2dConfig?.bindings || []
  if (!meta2d || !bindings.length) {
    return
  }
  bindings.forEach((binding: WorkshopMeta2dBinding) => {
    if (!binding.penId || !binding.prop || binding.sourceType !== 'WORKSHOP_ATTR') {
      return
    }
    const value = dict[binding.sourceKey || ''] ?? ''
    try {
      const patch: Record<string, unknown> = { id: binding.penId }
      patch[binding.prop] = value
      meta2d?.setValue?.(patch)
    } catch {
      /* optional runtime patch */
    }
  })
}

async function refreshBindings() {
  const code = workshopCode.value
  if (!code) {
    return
  }
  const rows = await fetchWorkshopRealtimeCollect(code)
  applyBindings(buildCollectDict(rows || []))
  const intervalSec = meta2dConfig?.refreshIntervalSec ?? 2
  statusText.value = t('workshopMeta2dViewerPage.running', { seconds: intervalSec })
}

function startRefreshTimer() {
  clearRefreshTimer()
  const code = workshopCode.value
  if (!code || !meta2dConfig?.bindings?.length) {
    return
  }
  const intervalSec = Math.max(1, meta2dConfig.refreshIntervalSec ?? 2)
  refreshTimer = setInterval(() => {
    void refreshBindings()
  }, intervalSec * 1000)
}

function ensureMeta2dInstance(): boolean {
  if (!isMeta2dReady()) {
    return false
  }
  if (!meta2d) {
    meta2d = new window.Meta2d!(canvasContainerId)
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
    meta2dError.value = t('workshopMeta2dViewerPage.canvasError')
  }
}

async function loadPreview() {
  clearRefreshTimer()
  meta2dError.value = ''
  statusText.value = t('workshopMeta2dViewerPage.loading')

  if (!workshopId.value && !workshopCode.value) {
    meta2dError.value = t('workshopMeta2dViewerPage.missingParams')
    statusText.value = meta2dError.value
    return
  }

  if (!workshopCode.value) {
    meta2dError.value = t('workshopMeta2dViewerPage.needWorkshopCode')
    statusText.value = meta2dError.value
    return
  }

  loading.value = true
  try {
    await loadMeta2dAssets()
    if (!isMeta2dReady()) {
      meta2dError.value = t('workshopMeta2dPage.loadError')
      statusText.value = meta2dError.value
      return
    }

    if (!workshopId.value) {
      meta2dError.value = t('workshopMeta2dViewerPage.missingWorkshopId')
      statusText.value = meta2dError.value
      return
    }

    const dto = await fetchWorkshopMeta2dConfig(workshopId.value)
    meta2dConfig = dto?.meta2dConfig ?? null
    if (!meta2dConfig?.canvas) {
      meta2dError.value = t('workshopMeta2dViewerPage.noCanvas')
      statusText.value = meta2dError.value
      return
    }

    openCanvas(meta2dConfig)
    await refreshBindings()
    startRefreshTimer()
    if (!meta2dError.value) {
      statusText.value = t('workshopMeta2dViewerPage.ready')
    }
  } catch (e) {
    meta2dError.value = e instanceof Error ? e.message : t('workshopMeta2dViewerPage.loadFailed')
    statusText.value = meta2dError.value
  } finally {
    loading.value = false
  }
}

async function toggleFullscreen() {
  const el = stageRef.value
  if (!el) {
    return
  }
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen()
    } else {
      await el.requestFullscreen()
    }
  } catch {
    message.warning(t('workshopMeta2dViewerPage.fullscreenFailed'))
  }
}

function onFullscreenChange() {
  isFullscreen.value = document.fullscreenElement === stageRef.value
}

watch(
  () => [workshopId.value, workshopCode.value] as const,
  () => {
    void loadPreview()
  },
  { immediate: true },
)

document.addEventListener('fullscreenchange', onFullscreenChange)

onUnmounted(() => {
  clearRefreshTimer()
  meta2d = null
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  safeExitFullscreen(stageRef.value)
})
</script>

<template>
  <div class="meta2d-viewer-page">
    <div class="viewer-topbar">
      <a-space>
        <a-tag color="processing">{{ t('workshopMeta2dViewerPage.badge') }}</a-tag>
        <a-tag>{{ statusText }}</a-tag>
      </a-space>
      <a-space>
        <a-button size="small" @click="loadPreview">
          <template #icon><ReloadOutlined /></template>
          {{ t('workshopMeta2dViewerPage.reload') }}
        </a-button>
        <a-button size="small" :disabled="Boolean(meta2dError)" @click="toggleFullscreen">
          <template #icon>
            <FullscreenExitOutlined v-if="isFullscreen" />
            <ExpandOutlined v-else />
          </template>
          {{
            isFullscreen
              ? t('workshopMeta2dViewerPage.exitFullscreen')
              : t('workshopMeta2dViewerPage.fullscreen')
          }}
        </a-button>
      </a-space>
    </div>

    <a-spin :spinning="loading" class="viewer-spin">
      <div ref="stageRef" class="viewer-stage">
        <a-alert
          v-if="meta2dError"
          type="warning"
          show-icon
          class="viewer-alert"
          :message="meta2dError"
        />
        <div v-else :id="canvasContainerId" class="meta2d-canvas" />
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.meta2d-viewer-page {
  height: calc(100vh - 64px - 32px - 48px);
  max-height: calc(100vh - 64px - 32px - 48px);
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}

.viewer-topbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid var(--omes-color-border);
  border-radius: 12px;
  background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
}

.viewer-spin {
  flex: 1;
  min-height: 0;
}

.viewer-spin :deep(.ant-spin-container) {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.viewer-stage {
  position: relative;
  flex: 1;
  min-height: 0;
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: 12px;
  overflow: hidden;
  background: #f7f8fa;
}

.viewer-stage:fullscreen {
  border-radius: 0;
  border: none;
}

.viewer-alert {
  margin: 16px;
}

.meta2d-canvas {
  width: 100%;
  height: 100%;
  min-height: 480px;
}

@media (max-width: 992px) {
  .meta2d-viewer-page {
    height: auto;
    max-height: none;
  }

  .viewer-stage {
    min-height: 420px;
  }

  .viewer-topbar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
