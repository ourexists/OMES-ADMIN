<script setup lang="ts">
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ApartmentOutlined,
  EnvironmentOutlined,
  ReloadOutlined, WifiOutlined,
} from '@ant-design/icons-vue'
import type { EquipRecord, WorkshopNode } from '@/api/device'
import { fetchEquipGisList, fetchEquipTypes } from '@/api/device'
import WorkshopTree from '@/components/WorkshopTree.vue'
import BaiduEquipGisMap from '@/components/map/BaiduEquipGisMap.vue'
import { hasBaiduMapAk } from '@/config/baidu-map'
import { filterEquipWithCoords } from '@/utils/equip-gis'
import { mergeEquipRealtimeList } from '@/utils/equip-realtime'
import { mapOptions } from '@/utils/options'
import { message } from 'ant-design-vue'

const { t } = useI18n()
const router = useRouter()

const GIS_POLL_MS = 5000

const selectedWorkshop = ref<WorkshopNode | null>(null)
const listLoading = ref(false)
const initialLoading = ref(true)
const sourceList = shallowRef<EquipRecord[]>([])
const typeOptions = ref<{ value: string | number; label: string }[]>([])
const fitViewKey = ref(0)
const mapStageRef = ref<HTMLElement | null>(null)
const gisMapRef = ref<InstanceType<typeof BaiduEquipGisMap> | null>(null)
const mapMountReady = ref(false)
const mapHeight = ref(520)

const searchForm = reactive({
  name: '',
  selfCode: '',
  type: undefined as string | number | undefined,
})

let pollTimer: ReturnType<typeof setInterval> | null = null
let loadRequestId = 0
let silentLoading = false
let pageAlive = true

const mappedDevices = shallowRef<EquipRecord[]>([])

function refreshMappedDevices() {
  mappedDevices.value = filterEquipWithCoords(sourceList.value)
}

const workshopLabel = computed(() => selectedWorkshop.value?.name?.trim() || t('gisPage.allWorkshops'))

const mapAkReady = computed(() => hasBaiduMapAk())

function applyEquipList(nextList: EquipRecord[], silent: boolean) {
  if (!pageAlive) {
    return
  }
  const normalized = Array.isArray(nextList) ? nextList : []
  if (silent) {
    const merged = mergeEquipRealtimeList(sourceList.value, normalized)
    if (merged !== null) {
      sourceList.value = merged
      refreshMappedDevices()
    }
    return
  }
  sourceList.value = normalized
  refreshMappedDevices()
}

async function loadTypes() {
  const types = await fetchEquipTypes()
  typeOptions.value = mapOptions(types).map((item) => ({
    ...item,
    value: String(item.value),
  }))
}

async function loadData(options: { silent?: boolean; fitView?: boolean } = {}) {
  const silent = options.silent === true
  if (silent && silentLoading) {
    return
  }
  const requestId = ++loadRequestId
  if (!silent) {
    listLoading.value = true
  } else {
    silentLoading = true
  }
  try {
    const list = await fetchEquipGisList({
      workshopCode: selectedWorkshop.value?.selfCode ?? null,
      name: searchForm.name.trim() || undefined,
      selfCode: searchForm.selfCode.trim() || undefined,
      type: searchForm.type,
    })
    if (requestId !== loadRequestId || !pageAlive) {
      return
    }
    applyEquipList(list, silent)
    if (options.fitView) {
      fitViewKey.value += 1
    }
  } catch {
    if (pageAlive) {
      message.error(t('gisPage.loadFailed'))
    }
  } finally {
    if (requestId === loadRequestId && pageAlive) {
      if (!silent) {
        listLoading.value = false
      } else {
        silentLoading = false
      }
      initialLoading.value = false
      refreshMapSize()
    }
  }
}

function refreshMapSize() {
  void nextTick(() => {
    gisMapRef.value?.refreshMapSize()
  })
}

function onMapReady() {
  const refresh = () => gisMapRef.value?.refreshMapSize()
  refresh()
  window.setTimeout(refresh, 200)
  window.setTimeout(refresh, 600)
  window.setTimeout(refresh, 1200)
}

function onSearch() {
  void loadData({ fitView: true })
}

function onReset() {
  searchForm.name = ''
  searchForm.selfCode = ''
  searchForm.type = undefined
  onSearch()
}

function onWorkshopChange() {
  void loadData({ fitView: true })
}

function openDetail(item: EquipRecord) {
  const id = item.id != null ? String(item.id) : ''
  if (!id) {
    return
  }
  router.push({ path: '/view/equip_detail', query: { id } })
}

function updateMapHeight() {
  const stage = mapStageRef.value
  if (!stage) {
    return
  }
  const top = stage.getBoundingClientRect().top
  mapHeight.value = Math.max(400, Math.floor(window.innerHeight - top - 16))
}

function startPolling() {
  if (pollTimer != null) {
    return
  }
  pollTimer = setInterval(() => {
    if (document.hidden || listLoading.value) {
      return
    }
    void loadData({ silent: true })
  }, GIS_POLL_MS)
}

function stopPolling() {
  if (pollTimer != null) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function onVisibilityChange() {
  if (!document.hidden) {
    void loadData({ silent: true })
  }
}

function onWindowResize() {
  updateMapHeight()
  refreshMapSize()
}

onMounted(async () => {
  pageAlive = true
  await loadTypes()
  document.addEventListener('visibilitychange', onVisibilityChange)
  window.addEventListener('resize', onWindowResize)
  await nextTick()
  updateMapHeight()
  mapMountReady.value = true
  await nextTick()
  updateMapHeight()
  refreshMapSize()
  void loadData({ fitView: true })
  startPolling()
})

onUnmounted(() => {
  pageAlive = false
  loadRequestId += 1
  stopPolling()
  document.removeEventListener('visibilitychange', onVisibilityChange)
  window.removeEventListener('resize', onWindowResize)
})
</script>

<template>
  <div class="gis-page">
    <a-row :gutter="16" class="gis-layout">
      <a-col :xs="24" :lg="5" class="sidebar-col">
        <a-card size="small" class="panel-card sidebar-card">
          <template #title>
            <AdminPanelTitle>
              <template #icon><ApartmentOutlined /></template>
              {{ t('gisPage.workshop') }}
            </AdminPanelTitle>
          </template>
          <div class="sidebar-tree-body">
            <WorkshopTree
              v-model="selectedWorkshop"
              fill
              :auto-select-first="false"
              @change="onWorkshopChange"
            />
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="19" class="map-col">
        <a-card size="small" class="panel-card map-card">
          <template #title>
            <AdminPanelTitle icon-class="card-title__icon--cyan">
              <template #icon><EnvironmentOutlined /></template>
              {{ t('gisPage.title') }}
            </AdminPanelTitle>
          </template>
          <template #extra>
            <a-space :size="8" wrap>
              <a-tag color="processing">
                {{ t('gisPage.onMap', { count: mappedDevices.length }) }}
              </a-tag>
              <span class="equip-live-badge">
                <WifiOutlined class="equip-live-badge__icon" />
                {{ t('gisPage.liveUpdating') }}
              </span>
            </a-space>
          </template>

          <div class="map-card-inner">
            <div class="toolbar-strip search-toolbar--compact">
              <a-form layout="inline" class="search-form search-form--compact" :model="searchForm" @finish="onSearch">
                <a-form-item name="name">
                  <a-input
                    v-model:value="searchForm.name"
                    allow-clear
                    size="small"
                    :placeholder="t('gisPage.searchName')"
                    class="search-input"
                  />
                </a-form-item>
                <a-form-item name="selfCode">
                  <a-input
                    v-model:value="searchForm.selfCode"
                    allow-clear
                    size="small"
                    :placeholder="t('gisPage.searchCode')"
                    class="search-input"
                  />
                </a-form-item>
                <a-form-item name="type">
                  <a-select
                    v-model:value="searchForm.type"
                    allow-clear
                    size="small"
                    :placeholder="t('gisPage.type')"
                    class="search-select"
                    :options="typeOptions"
                  />
                </a-form-item>
                <CompactSearchActions
                  :query-title="t('gisPage.query')"
                  :reset-title="t('gisPage.reset')"
                  :loading="listLoading"
                  @reset="onReset"
                >
                  <a-tooltip :title="t('gisPage.refresh')">
                    <a-button size="small" :loading="listLoading" @click="onSearch">
                      <ReloadOutlined />
                    </a-button>
                  </a-tooltip>
                </CompactSearchActions>
              </a-form>
            </div>

            <div ref="mapStageRef" class="map-stage">
              <div class="map-stage__legend">
                <div class="map-legend-item">
                  <span class="equip-status-dot equip-status-dot--online" />
                  {{ t('realtimePage.online') }}
                </div>
                <div class="map-legend-item">
                  <span class="equip-status-dot equip-status-dot--offline" />
                  {{ t('realtimePage.offline') }}
                </div>
                <div class="map-legend-item">
                  <span class="equip-status-dot equip-status-dot--run" />
                  {{ t('realtimePage.run') }}
                </div>
                <div class="map-legend-item">
                  <span class="equip-status-dot equip-status-dot--stop" />
                  {{ t('realtimePage.stop') }}
                </div>
                <div class="map-legend-item">
                  <span class="equip-status-dot equip-status-dot--alarm" />
                  {{ t('realtimePage.alarm') }}
                </div>
              </div>
              <div class="map-stage__workshop">{{ workshopLabel }}</div>

              <div class="map-stage__canvas">
                <a-alert
                  v-if="!mapAkReady"
                  type="warning"
                  show-icon
                  :message="t('equipFormPage.mapKeyMissing')"
                  class="map-stage__alert"
                />
                <BaiduEquipGisMap
                  v-else-if="mapMountReady"
                  ref="gisMapRef"
                  :height="mapHeight"
                  :devices="mappedDevices"
                  :fit-view-key="fitViewKey"
                  @ready="onMapReady"
                />
                <div v-if="mapAkReady && !initialLoading && mappedDevices.length === 0" class="map-stage__empty">
                  {{ t('gisPage.emptyCoords') }}
                </div>
              </div>
            </div>

            <div v-if="mappedDevices.length" class="gis-device-strip">
              <span class="gis-device-strip__label">{{ t('gisPage.deviceList') }}</span>
              <a-space :size="6" wrap>
                <a-tag
                  v-for="item in mappedDevices.slice(0, 24)"
                  :key="item.id"
                  class="gis-device-tag"
                  @click="openDetail(item)"
                >
                  {{ item.name || item.selfCode }}
                </a-tag>
                <span v-if="mappedDevices.length > 24" class="gis-device-strip__more">
                  {{ t('gisPage.moreDevices', { count: mappedDevices.length - 24 }) }}
                </span>
              </a-space>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped>
/* 高度随视口固定，地图区 flex 占满剩余空间；避免 JS 按页面 bottom 外边距叠来越叠越高 */
.gis-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px - 32px - 48px);
  max-height: calc(100vh - 64px - 32px - 48px);
  overflow: hidden;
}

.gis-layout {
  flex: 1;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  align-items: stretch;
}

.gis-layout :deep(> .ant-col) {
  height: 100%;
  min-width: 0;
}

.sidebar-col,
.map-col {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.sidebar-col :deep(> .ant-card),
.map-col :deep(> .ant-card) {
  flex: 1;
  min-height: 0;
}

.panel-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  border-radius: 12px;
  border: 1px solid var(--omes-color-border);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.panel-card :deep(.ant-card-head) {
  flex-shrink: 0;
}

.panel-card :deep(.ant-card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  padding: 14px 16px 16px;
}

.sidebar-card :deep(.ant-card-body) {
  padding-bottom: 16px;
}

.sidebar-tree-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-tree-body :deep(.workshop-tree) {
  flex: 1;
  min-height: 0;
  height: 100%;
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: var(--omes-radius-md);
  background: rgba(22, 119, 255, 0.1);
  color: var(--omes-color-primary);
}

.title-icon--gis {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
}

.map-card-inner {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.toolbar-strip {
  flex-shrink: 0;
}

.search-form--compact :deep(.ant-form-item) {
  margin-bottom: 8px;
}

.search-input {
  width: 140px;
}

.search-select {
  width: 120px;
}

.map-stage {
  position: relative;
  flex: 1;
  min-height: 280px;
  display: flex;
  flex-direction: column;
}

.map-stage__canvas {
  position: relative;
  flex: 1;
  min-height: 280px;
  display: flex;
  flex-direction: column;
}

.map-stage__canvas :deep(.baidu-gis-map-wrap) {
  flex: 1 1 auto;
  min-height: 280px;
}

.map-stage__legend {
  position: absolute;
  z-index: 2;
  top: 10px;
  left: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 10px;
  border-radius: var(--omes-radius-md);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  font-size: 12px;
  pointer-events: none;
}

.map-legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.map-stage__workshop {
  position: absolute;
  z-index: 2;
  top: 10px;
  right: 10px;
  max-width: 40%;
  padding: 4px 10px;
  border-radius: var(--omes-radius-sm);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  font-size: 13px;
  color: var(--omes-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  pointer-events: none;
}

.map-stage__alert {
  flex-shrink: 0;
}

.map-stage__placeholder {
  flex: 1;
  min-height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--omes-radius-md);
  border: 1px solid var(--omes-color-border-hover);
  background: var(--omes-color-bg-layout);
}

.map-stage__empty {
  position: absolute;
  z-index: 1;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  padding: 8px 14px;
  border-radius: var(--omes-radius-md);
  background: rgba(255, 255, 255, 0.9);
  color: var(--omes-color-text-quaternary);
  font-size: 13px;
  pointer-events: none;
}

.equip-live-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #10b981;
}

.equip-live-badge__icon {
  animation: gis-pulse 1.6s ease-in-out infinite;
}

@keyframes gis-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.45;
  }
}

.gis-device-strip {
  flex-shrink: 0;
  padding-top: 4px;
  border-top: 1px solid var(--omes-color-border);
  max-height: 72px;
  overflow-y: auto;
}

.gis-device-strip__label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
}

.gis-device-tag {
  cursor: pointer;
}

.gis-device-strip__more {
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
}

@media (max-width: 991px) {
  .gis-page {
    height: auto;
    max-height: none;
    overflow: visible;
  }

  .gis-layout {
    flex: none;
    height: auto;
    overflow: visible;
  }

  .sidebar-col,
  .map-col {
    height: auto;
  }

  .panel-card {
    height: auto;
  }

  .sidebar-card :deep(.ant-card-body) {
    max-height: 480px;
  }

  .sidebar-col .panel-card {
    margin-bottom: 16px;
  }
}
</style>
