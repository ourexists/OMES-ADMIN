<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  ApartmentOutlined,
  DesktopOutlined,
  ExpandOutlined,
  FullscreenExitOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'
import type { WorkshopNode } from '@/api/device'
import { fetchWorkshopScadaUrl } from '@/api/workshop-config'
import WorkshopTree from '@/components/WorkshopTree.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message } from 'ant-design-vue'
import { safeExitFullscreen } from '@/utils/fullscreen'

const { t } = useI18n()

const selectedWorkshop = ref<WorkshopNode | null>(null)
const frameLoading = ref(false)
const frameLoadError = ref(false)
const frameUrl = ref('')
const refreshMinutes = ref(0)
const stageRef = ref<HTMLElement | null>(null)
const isFullscreen = ref(false)

const FRAME_LOAD_TIMEOUT_MS = 30_000

let refreshTimer: ReturnType<typeof setInterval> | null = null
let frameLoadTimer: ReturnType<typeof setTimeout> | null = null
let loadRequestId = 0

const workshopLabel = computed(() => selectedWorkshop.value?.name?.trim() || '')
const hasFrame = computed(() => Boolean(frameUrl.value))
const frameReady = computed(() => hasFrame.value && !frameLoading.value && !frameLoadError.value)
const showStageOverlay = computed(() => !frameReady.value)

const overlayMessage = computed(() => {
  if (frameLoading.value) {
    return t('workshopScadaViewerPage.loading')
  }
  if (frameLoadError.value) {
    return t('workshopScadaViewerPage.loadFailed')
  }
  if (!selectedWorkshop.value?.id) {
    return t('workshopScadaViewerPage.selectWorkshop')
  }
  if (!hasFrame.value) {
    return t('workshopScadaViewerPage.noConfig')
  }
  return ''
})

const statusText = computed(() => {
  if (frameLoading.value) {
    return t('workshopScadaViewerPage.loading')
  }
  if (frameLoadError.value) {
    return t('workshopScadaViewerPage.loadFailed')
  }
  if (!selectedWorkshop.value?.id) {
    return t('workshopScadaViewerPage.selectWorkshop')
  }
  if (!hasFrame.value) {
    return t('workshopScadaViewerPage.noConfig')
  }
  if (refreshMinutes.value > 0) {
    return t('workshopScadaViewerPage.runningWithInterval', { minutes: refreshMinutes.value })
  }
  return t('workshopScadaViewerPage.running')
})

function clearRefreshTimer() {
  if (refreshTimer != null) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

function clearFrameLoadWatch() {
  if (frameLoadTimer != null) {
    clearTimeout(frameLoadTimer)
    frameLoadTimer = null
  }
}

function startFrameLoadWatch() {
  clearFrameLoadWatch()
  frameLoadTimer = window.setTimeout(() => {
    frameLoadTimer = null
    if (frameLoading.value && hasFrame.value) {
      frameLoading.value = false
      frameLoadError.value = true
    }
  }, FRAME_LOAD_TIMEOUT_MS)
}

function markFrameReady() {
  clearFrameLoadWatch()
  frameLoading.value = false
  frameLoadError.value = false
}

function markFrameLoadFailed() {
  clearFrameLoadWatch()
  frameLoading.value = false
  frameLoadError.value = true
}

function scheduleRefresh(minutes: number, workshopId: string) {
  clearRefreshTimer()
  if (!minutes || minutes <= 0 || !workshopId) {
    return
  }
  refreshTimer = setInterval(() => {
    void reloadFrameUrl(workshopId, true)
  }, minutes * 60 * 1000)
}

async function reloadFrameUrl(workshopId: string, silent = false) {
  const requestId = ++loadRequestId
  if (!silent) {
    clearFrameLoadWatch()
    frameLoadError.value = false
    frameLoading.value = true
  }
  try {
    const result = await fetchWorkshopScadaUrl(workshopId)
    if (requestId !== loadRequestId) {
      return
    }
    const url = result?.url?.trim() || ''
    frameUrl.value = url
    refreshMinutes.value = result?.interval ?? 0
    if (url) {
      scheduleRefresh(refreshMinutes.value, workshopId)
      if (!silent) {
        startFrameLoadWatch()
      }
    } else {
      clearRefreshTimer()
      if (!silent) {
        markFrameReady()
      }
    }
  } catch {
    if (requestId === loadRequestId) {
      frameUrl.value = ''
      refreshMinutes.value = 0
      clearRefreshTimer()
      if (!silent) {
        markFrameLoadFailed()
        message.error(t('workshopScadaViewerPage.loadFailed'))
      }
    }
  }
}

async function loadForSelectedWorkshop() {
  clearRefreshTimer()
  clearFrameLoadWatch()
  frameUrl.value = ''
  refreshMinutes.value = 0
  frameLoadError.value = false

  const workshopId = selectedWorkshop.value?.id
  if (!workshopId) {
    frameLoading.value = false
    return
  }
  await reloadFrameUrl(workshopId)
}

function onFrameLoad() {
  markFrameReady()
}

function onFrameError() {
  markFrameLoadFailed()
}

function onReload() {
  void loadForSelectedWorkshop()
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
    message.warning(t('workshopScadaViewerPage.fullscreenFailed'))
  }
}

function onFullscreenChange() {
  isFullscreen.value = document.fullscreenElement === stageRef.value
}

watch(selectedWorkshop, () => {
  void loadForSelectedWorkshop()
})

document.addEventListener('fullscreenchange', onFullscreenChange)

onUnmounted(() => {
  clearRefreshTimer()
  clearFrameLoadWatch()
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  safeExitFullscreen(stageRef.value)
})
</script>

<template>
  <div class="scada-viewer-page">
    <a-row :gutter="16" class="scada-layout">
      <a-col :xs="24" :lg="5" class="sidebar-col">
        <a-card size="small" class="panel-card sidebar-card">
          <template #title>
            <AdminPanelTitle>
              <template #icon><ApartmentOutlined /></template>
              {{ t('workshopScadaViewerPage.workshop') }}
            </AdminPanelTitle>
          </template>
          <div class="sidebar-tree-body">
            <WorkshopTree
              v-model="selectedWorkshop"
              fill
              :auto-select-first="false"
            />
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="19" class="viewer-col">
        <a-card size="small" class="panel-card viewer-card">
          <template #title>
            <AdminPanelTitle icon-class="card-title__icon--cyan">
              <template #icon><DesktopOutlined /></template>
              {{ t('workshopScadaViewerPage.title') }}
            </AdminPanelTitle>
          </template>
          <template #extra>
            <a-space :size="8" wrap>
              <a-tag v-if="workshopLabel" color="processing">{{ workshopLabel }}</a-tag>
              <a-tag :color="frameReady ? 'success' : frameLoadError ? 'error' : 'default'">{{ statusText }}</a-tag>
            </a-space>
          </template>

          <div class="viewer-card-inner">
            <div class="viewer-toolbar">
              <p class="page-desc">{{ t('workshopScadaViewerPage.subtitle') }}</p>
              <a-space :size="8">
                <a-button size="small" :disabled="!selectedWorkshop?.id" @click="onReload">
                  <template #icon><ReloadOutlined /></template>
                  {{ t('workshopScadaViewerPage.reload') }}
                </a-button>
                <a-button size="small" :disabled="!frameReady" @click="toggleFullscreen">
                  <template #icon>
                    <FullscreenExitOutlined v-if="isFullscreen" />
                    <ExpandOutlined v-else />
                  </template>
                  {{
                    isFullscreen
                      ? t('workshopScadaViewerPage.exitFullscreen')
                      : t('workshopScadaViewerPage.fullscreen')
                  }}
                </a-button>
              </a-space>
            </div>

            <div
              ref="stageRef"
              class="viewer-stage"
              :class="{ 'viewer-stage--ready': frameReady }"
            >
              <div
                v-if="showStageOverlay"
                class="viewer-overlay"
                :class="{ 'viewer-overlay--loading': frameLoading }"
              >
                <a-spin v-if="frameLoading" size="large" />
                <a-empty v-else :description="overlayMessage" />
                <p v-if="frameLoading">{{ t('workshopScadaViewerPage.loading') }}</p>
              </div>

              <iframe
                v-if="hasFrame"
                v-show="frameReady"
                :key="frameUrl"
                class="scada-frame"
                :src="frameUrl"
                title="SCADA"
                frameborder="0"
                @load="onFrameLoad"
                @error="onFrameError"
              />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped>
.scada-viewer-page {
  height: calc(100vh - 64px - 32px - 48px);
  max-height: calc(100vh - 64px - 32px - 48px);
  overflow: hidden;
}

.scada-layout {
  height: 100%;
  min-height: 0;
}

.scada-layout :deep(> .ant-col) {
  height: 100%;
  min-width: 0;
}

.sidebar-col,
.viewer-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-radius: 12px;
  border: 1px solid var(--omes-color-border);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.panel-card :deep(.ant-card-head) {
  flex-shrink: 0;
  min-height: 52px;
  border-bottom: 1px solid var(--omes-color-border);
  background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
}

.panel-card :deep(.ant-card-body) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 14px 16px 16px;
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 15px;
}

.title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--omes-radius-md);
  background: var(--omes-color-primary-bg);
  color: var(--omes-color-primary);
  font-size: 15px;
}

.title-icon--scada {
  background: #fff7e6;
  color: var(--omes-color-accent-orange-from);
}

.sidebar-tree-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.viewer-card-inner {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.viewer-toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  background: var(--omes-color-bg-elevated);
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-lg);
}

.page-desc {
  margin: 0;
  flex: 1;
  font-size: 13px;
  color: var(--omes-color-text-quaternary);
  line-height: 1.6;
}

.viewer-stage {
  position: relative;
  flex: 1;
  min-height: 0;
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-lg);
  overflow: hidden;
  background: var(--omes-color-bg-layout);
}

.viewer-stage--ready {
  background: #0b1020;
}

.viewer-stage:fullscreen {
  border-radius: 0;
  border: none;
}

.viewer-stage--ready:fullscreen {
  background: #000;
}

.viewer-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: #f7f8fa;
  z-index: 2;
}

.viewer-overlay p {
  margin: 0;
  color: var(--omes-color-text-quaternary);
}

.scada-frame {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
  background: #000;
}

@media (max-width: 992px) {
  .scada-viewer-page {
    height: auto;
    max-height: none;
    overflow: visible;
  }

  .scada-layout {
    flex: none;
  }

  .sidebar-col,
  .viewer-col {
    height: auto;
  }

  .panel-card {
    height: auto;
  }

  .sidebar-card :deep(.ant-card-body) {
    max-height: 420px;
  }

  .viewer-stage {
    min-height: 420px;
  }

  .sidebar-col .panel-card {
    margin-bottom: 16px;
  }

  .viewer-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
