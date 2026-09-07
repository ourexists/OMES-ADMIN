<script setup lang="ts">
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, shallowRef, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  AppstoreOutlined,
  LineChartOutlined,
  ReloadOutlined, UnorderedListOutlined,
  WifiOutlined,
} from '@ant-design/icons-vue'
import type { EquipRecord, WorkshopNode } from '@/api/device'
import { fetchEquipRealtimeList, fetchEquipTypes } from '@/api/device'
import WorkshopTreeSelect from '@/components/WorkshopTreeSelect.vue'
import RealtimeDeviceCard from './components/RealtimeDeviceCard.vue'
import RealtimeDeviceList from './components/RealtimeDeviceList.vue'
import { mapOptions } from '@/utils/options'
import { mergeEquipRealtimeList } from '@/utils/equip-realtime'

const { t } = useI18n()
const router = useRouter()

const REALTIME_POLL_MS = 5000
const VIEW_STORAGE_KEY = 'equip_realtime_view'
const FILTER_STORAGE_KEY = 'equip_realtime_filter'

type ViewMode = 'card' | 'list'
type StateFilter = '' | '1' | '0' | 'run' | 'stop' | 'alarm'

const selectedWorkshop = ref<WorkshopNode | null>(null)
/** 用户主动查询/刷新时的按钮 loading */
const listLoading = ref(false)
/** 首次进入或场景切换时的内容区 loading */
const initialLoading = ref(true)
const sourceList = shallowRef<EquipRecord[]>([])
const typeOptions = ref<{ value: string | number; label: string }[]>([])
const gridRef = ref<HTMLElement | null>(null)
const cardInnerRef = ref<HTMLElement | null>(null)
const scrollAreaHeight = ref(0)

const searchForm = reactive({
  name: '',
  selfCode: '',
  type: undefined as string | number | undefined,
})

const viewMode = ref<ViewMode>(
  (localStorage.getItem(VIEW_STORAGE_KEY) as ViewMode) === 'list' ? 'list' : 'card',
)
const stateFilter = ref<StateFilter>((localStorage.getItem(FILTER_STORAGE_KEY) as StateFilter) || '')

let pollTimer: ReturnType<typeof setInterval> | null = null
let loadRequestId = 0
let silentLoading = false

const showContentSpin = computed(() => initialLoading.value && sourceList.value.length === 0)

const gridScrollStyle = computed(() => {
  if (scrollAreaHeight.value <= 0) {
    return undefined
  }
  return {
    height: `${scrollAreaHeight.value}px`,
    maxHeight: `${scrollAreaHeight.value}px`,
  }
})

const totalCount = computed(() => sourceList.value.length)

type KpiItem = {
  key: StateFilter
  label: string
  count: number
  tone: string
}

const filteredList = computed(() => {
  const list = sourceList.value
  const f = stateFilter.value
  if (!f) return list
  if (f === 'run') return list.filter((item) => item.onlineState === 1 && item.runState === 1)
  if (f === 'stop') return list.filter((item) => item.onlineState === 1 && item.runState === 0)
  if (f === 'alarm') return list.filter((item) => item.onlineState === 1 && item.alarmState === 1)
  const state = Number(f)
  if (!Number.isNaN(state)) return list.filter((item) => item.onlineState === state)
  return list
})

/** 统计基于 filteredList，与列表数据一致；查询无变化时不重算 */
const statusCounts = computed(() => {
  const c = { online: 0, offline: 0, run: 0, stop: 0, alarm: 0 }
  for (const item of sourceList.value) {
    if (item.onlineState === 0) {
      c.offline += 1
      continue
    }
    c.online += 1
    if (item.runState === 1) c.run += 1
    else if (item.runState === 0) c.stop += 1
    if (item.alarmState === 1) c.alarm += 1
  }
  return c
})

const kpiItems = computed<KpiItem[]>(() => [
  { key: '1', label: t('realtimePage.online'), count: statusCounts.value.online, tone: 'online' },
  { key: '0', label: t('realtimePage.offline'), count: statusCounts.value.offline, tone: 'offline' },
  { key: 'run', label: t('realtimePage.run'), count: statusCounts.value.run, tone: 'run' },
  { key: 'stop', label: t('realtimePage.stop'), count: statusCounts.value.stop, tone: 'stop' },
  { key: 'alarm', label: t('realtimePage.alarm'), count: statusCounts.value.alarm, tone: 'alarm' },
])

function applyEquipList(nextList: EquipRecord[], silent: boolean) {
  const normalized = Array.isArray(nextList) ? nextList : []
  if (silent) {
    const merged = mergeEquipRealtimeList(sourceList.value, normalized)
    if (merged !== null) {
      sourceList.value = merged
    }
    return
  }
  sourceList.value = normalized
}

function toggleStateFilter(key: StateFilter) {
  stateFilter.value = stateFilter.value === key ? '' : key
  localStorage.setItem(FILTER_STORAGE_KEY, stateFilter.value)
}

function clearStateFilter() {
  stateFilter.value = ''
  localStorage.setItem(FILTER_STORAGE_KEY, '')
}

function setViewMode(mode: ViewMode) {
  viewMode.value = mode
  localStorage.setItem(VIEW_STORAGE_KEY, mode)
}

async function loadTypes() {
  const types = await fetchEquipTypes()
  typeOptions.value = mapOptions(types).map((item) => ({
    ...item,
    value: String(item.value),
  }))
}

async function loadData(options: { silent?: boolean } = {}) {
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
    const list = await fetchEquipRealtimeList({
      workshopCode: selectedWorkshop.value?.selfCode ?? null,
      name: searchForm.name.trim() || undefined,
      selfCode: searchForm.selfCode.trim() || undefined,
      type: searchForm.type,
    })
    if (requestId !== loadRequestId) {
      return
    }
    applyEquipList(list, silent)
  } finally {
    if (requestId === loadRequestId) {
      if (!silent) {
        listLoading.value = false
      } else {
        silentLoading = false
      }
      initialLoading.value = false
      await nextTick()
      syncScrollLayout()
    }
  }
}

function onSearch() {
  void loadData()
}

function onReset() {
  searchForm.name = ''
  searchForm.selfCode = ''
  searchForm.type = undefined
  onSearch()
}

function openDetail(item: EquipRecord) {
  const id = item.id != null ? String(item.id) : ''
  if (!id) {
    return
  }
  router.push({ path: '/view/equip_detail', query: { id } })
}

function syncScrollLayout() {
  const inner = cardInnerRef.value
  if (!inner) {
    return
  }

  const toolbar = inner.querySelector('.toolbar-strip') as HTMLElement | null
  const anchorBottom = toolbar?.getBoundingClientRect().bottom ?? inner.getBoundingClientRect().top

  const pageEl = inner.closest('.realtime-page') as HTMLElement | null
  const cardBody = inner.closest('.ant-card-body') as HTMLElement | null
  const pageBottom = pageEl?.getBoundingClientRect().bottom ?? window.innerHeight
  const bodyBottom = cardBody?.getBoundingClientRect().bottom ?? pageBottom
  const bottomLimit = Math.min(pageBottom, bodyBottom)

  const nextHeight = Math.max(220, Math.floor(bottomLimit - anchorBottom - 16))

  if (nextHeight > 0 && nextHeight !== scrollAreaHeight.value) {
    scrollAreaHeight.value = nextHeight
  }
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
  }, REALTIME_POLL_MS)
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

watch(selectedWorkshop, () => {
  void loadData()
})

watch(viewMode, async () => {
  await nextTick()
  syncScrollLayout()
  if (gridRef.value && gridResizeObserver) {
    gridResizeObserver.disconnect()
    if (cardInnerRef.value) {
      gridResizeObserver.observe(cardInnerRef.value)
    }
  }
})

let gridResizeObserver: ResizeObserver | null = null

onMounted(async () => {
  await loadTypes()
  await loadData()
  startPolling()
  document.addEventListener('visibilitychange', onVisibilityChange)
  window.addEventListener('resize', syncScrollLayout)
  await nextTick()
  gridResizeObserver = new ResizeObserver(() => syncScrollLayout())
  if (cardInnerRef.value) {
    gridResizeObserver.observe(cardInnerRef.value)
  }
  syncScrollLayout()
})

onUnmounted(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', onVisibilityChange)
  window.removeEventListener('resize', syncScrollLayout)
  gridResizeObserver?.disconnect()
  gridResizeObserver = null
})
</script>

<template>
  <div class="realtime-page">
    <a-card size="small" class="panel-card main-card">
      <template #title>
        <AdminPanelTitle>
          <template #icon><LineChartOutlined /></template>
          {{ t('menu.realtime', t('realtimePage.title')) }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <span class="equip-live-badge">
          <WifiOutlined class="equip-live-badge__icon" />
          {{ t('realtimePage.liveUpdating') }}
        </span>
      </template>

      <div ref="cardInnerRef" class="realtime-card-inner">
        <div class="kpi-strip">
            <button
              type="button"
              class="kpi-card kpi-card--all equip-kpi-accent--all"
              :class="{ 'kpi-card--active': stateFilter === '' }"
              @click="clearStateFilter"
            >
              <span class="kpi-card__value">{{ totalCount }}</span>
              <span class="kpi-card__label">{{ t('realtimePage.totalDevices') }}</span>
              <span class="kpi-card__hint">{{ t('realtimePage.showing', { count: filteredList.length }) }}</span>
            </button>
            <button
              v-for="item in kpiItems"
              :key="item.key"
              type="button"
              class="kpi-card"
              :class="[`equip-kpi-accent--${item.tone}`, { 'kpi-card--active': stateFilter === item.key }]"
              @click="toggleStateFilter(item.key)"
            >
              <span class="kpi-card__value">{{ item.count }}</span>
              <span class="kpi-card__label">{{ item.label }}</span>
            </button>
          </div>

          <div class="toolbar-strip search-toolbar--compact">
            <div class="toolbar-workshop">
              <span class="toolbar-workshop__label">{{ t('realtimePage.workshop') }}</span>
              <WorkshopTreeSelect v-model="selectedWorkshop" class="workshop-select" />
            </div>

            <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
              <a-form-item name="name">
                <a-input
                  v-model:value="searchForm.name"
                  allow-clear
                  size="small"
                  :placeholder="t('realtimePage.searchName')"
                  class="search-input"
                />
              </a-form-item>
              <a-form-item name="selfCode">
                <a-input
                  v-model:value="searchForm.selfCode"
                  allow-clear
                  size="small"
                  :placeholder="t('realtimePage.searchCode')"
                  class="search-input"
                />
              </a-form-item>
              <a-form-item name="type">
                <a-select
                  v-model:value="searchForm.type"
                  allow-clear
                  size="small"
                  :placeholder="t('realtimePage.type')"
                  class="search-select"
                  :options="typeOptions"
                />
              </a-form-item>
              <CompactSearchActions
                :query-title="t('realtimePage.query')"
                :reset-title="t('realtimePage.reset')"
                :loading="listLoading"
                @reset="onReset"
              >
                <a-tooltip :title="t('realtimePage.reload')">
                  <a-button size="small" :loading="listLoading" @click="loadData()">
                    <ReloadOutlined />
                  </a-button>
                </a-tooltip>
              </CompactSearchActions>
            </a-form>

            <div class="view-toggle">
              <a-button
                size="small"
                :type="viewMode === 'card' ? 'primary' : 'default'"
                @click="setViewMode('card')"
              >
                <AppstoreOutlined />
              </a-button>
              <a-button
                size="small"
                :type="viewMode === 'list' ? 'primary' : 'default'"
                @click="setViewMode('list')"
              >
                <UnorderedListOutlined />
              </a-button>
            </div>
          </div>

          <div class="content-panel">
            <a-spin :spinning="showContentSpin" class="content-spin">
              <div
                v-if="viewMode === 'card'"
                ref="gridRef"
                class="device-grid-scroll"
                :style="gridScrollStyle"
              >
                <div class="device-grid">
                  <RealtimeDeviceCard
                    v-for="item in filteredList"
                    :key="item.id"
                    v-memo="[item]"
                    :item="item"
                    @open="openDetail"
                  />

                  <a-empty v-if="!filteredList.length" class="grid-empty" :description="t('realtimePage.empty')" />
                </div>
              </div>

              <div v-else ref="gridRef" class="device-list-wrap" :style="gridScrollStyle">
                <RealtimeDeviceList :items="filteredList" @open="openDetail" />
              </div>
            </a-spin>
          </div>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.realtime-page {
  height: calc(100vh - 64px - 32px - 48px);
  max-height: calc(100vh - 64px - 32px - 48px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  background:
    radial-gradient(circle at 0% 0%, rgba(14, 165, 233, 0.06), transparent 42%),
    radial-gradient(circle at 100% 0%, rgba(16, 185, 129, 0.05), transparent 38%),
    #eef2f7;
  border-radius: 16px;
  padding: 4px;
}

.main-card {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.main-card:deep(> .ant-card-body) {
  flex: 1;
  min-height: 0;
  padding-bottom: 12px;
}

.panel-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(6px);
}

.panel-card :deep(.ant-card-head) {
  flex-shrink: 0;
  min-height: 52px;
  border-bottom: 1px solid #e8eef5;
  background: linear-gradient(180deg, var(--omes-color-bg-table-hover-alt) 0%, #fff 100%);
}

.panel-card :deep(.ant-card-body) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 14px 16px 16px;
}

.realtime-card-inner {
  flex: 1;
  min-height: 0;
  max-height: 100%;
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
  gap: 12px;
  overflow: hidden;
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  font-size: 15px;
  color: #0f172a;
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

.title-icon--realtime {
  background: linear-gradient(145deg, #dbeafe 0%, #ecfdf5 100%);
  color: #0284c7;
}

.equip-live-badge__icon {
  font-size: 13px;
  animation: live-pulse 2.4s ease-in-out infinite;
}

@keyframes live-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.45;
  }
}

.kpi-strip {
  display: grid;
  grid-template-columns: minmax(140px, 1.2fr) repeat(5, minmax(0, 1fr));
  gap: 10px;
  flex-shrink: 0;
}

.kpi-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: var(--omes-color-bg-container);
  cursor: pointer;
  text-align: left;
  transition: all 0.16s ease;
  position: relative;
  overflow: hidden;
}

.kpi-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--equip-kpi-accent, var(--equip-status-offline-accent));
}

.kpi-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
}

.kpi-card--active {
  border-color: #7dd3fc;
  background: linear-gradient(180deg, #f0f9ff 0%, #fff 100%);
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.12);
}

.kpi-card__value {
  font-size: 22px;
  font-weight: 800;
  line-height: 1.1;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.kpi-card__label {
  font-size: 12px;
  font-weight: 600;
  color: var(--equip-status-offline-fg);
}

.kpi-card__hint {
  margin-top: 2px;
  font-size: 11px;
  color: var(--equip-status-muted-fg);
}

.toolbar-strip {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 12px;
  background: var(--omes-color-bg-toolbar-from);
  border: 1px solid #e8eef5;
  border-radius: 12px;
}

.toolbar-workshop {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.toolbar-workshop__label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  white-space: nowrap;
}

.workshop-select {
  min-width: 260px;
}

.search-form {
  margin-bottom: 0;
  flex: 1;
  min-width: 280px;
}

.search-form :deep(.ant-form-item) {
  margin-bottom: 0;
  margin-inline-end: 8px;
}

.search-input {
  width: 148px;
}

.search-select {
  width: 128px;
}

.view-toggle {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.content-panel {
  position: relative;
  min-height: 0;
  max-height: 100%;
  overflow: hidden;
}

.content-spin {
  position: absolute;
  inset: 0;
}

.content-spin :deep(.ant-spin-nested-loading) {
  height: 100%;
}

.content-spin :deep(.ant-spin-container) {
  height: 100%;
  overflow: hidden;
}

.content-spin :deep(.ant-spin-blur) {
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.device-grid-scroll {
  box-sizing: border-box;
  width: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
}

.device-grid-scroll::-webkit-scrollbar {
  width: 8px;
}

.device-grid-scroll::-webkit-scrollbar-thumb {
  background: rgba(15, 23, 42, 0.22);
  border-radius: 4px;
}

.device-grid-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(15, 23, 42, 0.32);
}

.device-grid-scroll::-webkit-scrollbar-track {
  background: rgba(15, 23, 42, 0.04);
  border-radius: 4px;
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(272px, 1fr));
  gap: 14px;
  align-content: start;
  padding: 4px 6px 14px 4px;
}

.grid-empty {
  grid-column: 1 / -1;
  padding: 56px 0;
}

.device-list-wrap {
  box-sizing: border-box;
  width: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
  border: 1px solid #e8eef5;
  border-radius: 12px;
  background: var(--omes-color-bg-container);
}

.device-list-wrap::-webkit-scrollbar {
  width: 8px;
}

.device-list-wrap::-webkit-scrollbar-thumb {
  background: rgba(15, 23, 42, 0.22);
  border-radius: 4px;
}

@media (max-width: 1200px) {
  .kpi-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .realtime-page {
    height: auto;
    max-height: none;
    overflow: visible;
    padding: 0;
    background: transparent;
  }

  .panel-card {
    height: auto;
    margin-bottom: 16px;
  }

  .realtime-card-inner {
    height: auto;
    grid-template-rows: auto auto auto;
  }

  .content-panel {
    height: auto;
  }

  .device-grid-scroll {
    max-height: 480px;
  }

  .device-list-wrap {
    min-height: 320px;
  }

  .kpi-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar-strip {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-workshop {
    width: 100%;
  }

  .workshop-select {
    flex: 1;
    width: auto;
    min-width: 0;
  }

  .search-form {
    min-width: 0;
  }

  .search-input,
  .search-select {
    width: 100%;
  }

  .view-toggle {
    justify-content: flex-end;
  }
}
</style>
