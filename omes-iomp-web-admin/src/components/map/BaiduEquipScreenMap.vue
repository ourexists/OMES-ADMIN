<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { EquipRecord } from '@/api/device'
import {
  applyBaiduMapStyle,
  BAIDU_MAP_SCREEN_STYLE_ID,
  bindBaiduMapAttributionHide,
  hasBaiduMapAk,
  loadBaiduMapGl,
  safeDestroyBaiduMap,
} from '@/config/baidu-map'
import { createSceneDotOverlay, type SceneDotOverlayHandle } from '@/utils/baidu-scene-dot-overlay'
import { filterEquipWithCoords } from '@/utils/equip-gis'
import {
  buildSceneInfoWindowHtml,
  buildSceneInfoWindowTitle,
  mapDotFillColor,
  mapDotRadius,
  mapDotShouldRipple,
  prepareSceneMapPoints,
  sceneDisplayCoord,
  scenePointOverlaySnapshot,
  type SceneAggregatePoint,
} from '@/utils/equip-screen-map'

const props = withDefaults(
  defineProps<{
    devices: EquipRecord[]
    fitViewKey?: number
    height?: number
  }>(),
  {
    fitViewKey: 0,
    height: 480,
  },
)

const emit = defineEmits<{
  ready: []
  error: [message: string]
}>()

const { t } = useI18n()

const mapReady = ref(false)
const mapLoading = ref(false)
const mapError = ref('')
const mapContainerId = `baidu-screen-${Math.random().toString(36).slice(2, 10)}`

const mapHeightPx = ref(Math.max(props.height, 320))

let map: BMapGL.Map | null = null
let unbindAttributionHide: (() => void) | null = null

interface ScreenOverlayEntry {
  handle: SceneDotOverlayHandle
  point: SceneAggregatePoint
}

const overlayById = new Map<string, ScreenOverlayEntry>()
const infoById = new Map<string, BMapGL.InfoWindow>()
const overlaySnapById = new Map<string, string>()
let userInteracting = false
let initToken = 0
let openMarkerId: string | null = null
let markerJob = 0
let overlaySyncTimer: ReturnType<typeof setTimeout> | null = null

watch(
  () => props.height,
  (h) => {
    const next = Math.max(h, 320)
    if (next !== mapHeightPx.value) {
      mapHeightPx.value = next
      nextTick(() => refreshMapSize())
    }
  },
)

let resizeTimer: ReturnType<typeof setTimeout> | null = null

function refreshMapSize() {
  map?.checkResize()
}

function scheduleResize(options?: { eager?: boolean }) {
  if (options?.eager) {
    refreshMapSize()
    window.setTimeout(refreshMapSize, 200)
  }
  if (resizeTimer != null) {
    return
  }
  resizeTimer = window.setTimeout(() => {
    resizeTimer = null
    refreshMapSize()
  }, 120)
}

defineExpose({ refreshMapSize, mapReady, mapLoading, mapError })

function toScenePoints(list: EquipRecord[]): SceneAggregatePoint[] {
  return prepareSceneMapPoints(filterEquipWithCoords(list))
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function wrapInfoWindowHtml(title: string, body: string): string {
  return `<div class="screen-map-popup-shell" style="background:rgba(18,36,46,0.98);"><div class="screen-map-popup-shell__title"><b>${escapeHtml(title)}</b></div>${body}</div>`
}

function applyInfoWindowDarkTheme() {
  const host = document.getElementById(mapContainerId)
  if (!host) {
    return
  }
  const bubbleSelectors = [
    '[class*="bubble_pop"]',
    '[class*="bubble_center"]',
    '[class*="bubble_content"]',
    '[class*="InfoWindow"]',
  ].join(',')
  host.querySelectorAll<HTMLElement>(bubbleSelectors).forEach((el) => {
    el.style.setProperty('background', 'rgba(18, 36, 46, 0.98)', 'important')
    el.style.setProperty('border-color', 'rgba(91, 243, 249, 0.38)', 'important')
    el.style.setProperty('color', '#e8f4ff', 'important')
    el.style.setProperty('box-shadow', 'none', 'important')
    el.style.setProperty('overflow-x', 'hidden', 'important')
    el.style.setProperty('overflow-y', 'hidden', 'important')
    el.style.setProperty('padding', '0', 'important')
  })
  host.querySelectorAll<HTMLElement>('[class*="bubble_pop"]').forEach((el) => {
    el.style.setProperty('border-radius', '4px', 'important')
    el.style.setProperty('box-shadow', '0 8px 28px var(--omes-color-text-quaternary)', 'important')
    el.style.setProperty('padding-top', '0', 'important')
    el.style.setProperty('position', 'relative', 'important')
  })
  host.querySelectorAll<HTMLElement>('[class*="bubble_top"]').forEach((el) => {
    el.style.setProperty('height', '0', 'important')
    el.style.setProperty('min-height', '0', 'important')
    el.style.setProperty('max-height', '0', 'important')
    el.style.setProperty('padding', '0', 'important')
    el.style.setProperty('margin', '0', 'important')
    el.style.setProperty('overflow', 'visible', 'important')
    el.style.setProperty('border', 'none', 'important')
  })
  host.querySelectorAll<HTMLElement>('[class*="bubble_title"]').forEach((el) => {
    el.style.setProperty('display', 'none', 'important')
    el.style.setProperty('height', '0', 'important')
    el.style.setProperty('min-height', '0', 'important')
    el.style.setProperty('max-height', '0', 'important')
    el.style.setProperty('padding', '0', 'important')
    el.style.setProperty('margin', '0', 'important')
    el.style.setProperty('overflow', 'hidden', 'important')
    el.style.setProperty('border', 'none', 'important')
  })
  host.querySelectorAll<HTMLElement>('[class*="bubble_close"]').forEach((el) => {
    el.style.setProperty('color', '#94a3b8', 'important')
    el.style.setProperty('font-size', '18px', 'important')
    el.style.setProperty('position', 'absolute', 'important')
    el.style.setProperty('top', '4px', 'important')
    el.style.setProperty('right', '4px', 'important')
    el.style.setProperty('padding', '4px 8px', 'important')
    el.style.setProperty('z-index', '2', 'important')
  })
  host.querySelectorAll<HTMLElement>('[class*="tail"], [class*="Tail"]').forEach((el) => {
    el.style.setProperty('border-top-color', 'rgba(18, 36, 46, 0.98)', 'important')
    el.style.setProperty('filter', 'none', 'important')
  })
}

function openInfoWindowForId(id: string) {
  if (!map || !window.BMapGL) {
    return
  }
  const entry = overlayById.get(id)
  const info = infoById.get(id)
  if (!entry || !info) {
    return
  }
  const coord = sceneDisplayCoord(entry.point)
  const mapPoint = new window.BMapGL.Point(coord.lng, coord.lat)
  openMarkerId = id
  map.openInfoWindow(info, mapPoint)
  window.requestAnimationFrame(() => {
    applyInfoWindowDarkTheme()
    window.requestAnimationFrame(() => applyInfoWindowDarkTheme())
  })
}

function upsertOverlay(point: SceneAggregatePoint) {
  if (!map || !window.BMapGL) {
    return
  }

  const snap = scenePointOverlaySnapshot(point)
  const existing = overlayById.get(point.id)
  if (existing && overlaySnapById.get(point.id) === snap) {
    return
  }
  overlaySnapById.set(point.id, snap)

  const { Point } = window.BMapGL
  const coord = sceneDisplayCoord(point)
  const mapPoint = new Point(coord.lng, coord.lat)
  const html = buildSceneInfoWindowHtml(t, point)
  const title = buildSceneInfoWindowTitle(point)
  const content = wrapInfoWindowHtml(title, html)

  const color = mapDotFillColor(point)
  const radius = mapDotRadius(point)
  const ripple = mapDotShouldRipple(point)

  let info = infoById.get(point.id)
  if (!info) {
    info = new window.BMapGL.InfoWindow(content, { width: 400, title: '' })
    infoById.set(point.id, info)
  } else {
    info.setContent(content)
    info.setTitle('')
  }

  if (existing) {
    existing.point = point
    existing.handle.setPoint(mapPoint)
    existing.handle.setStyle(color, radius, ripple)
    return
  }

  const handle = createSceneDotOverlay(mapPoint, {
    color,
    radius,
    ripple,
    onClick: () => openInfoWindowForId(point.id),
  })
  map.addOverlay(handle.overlay as BMapGL.Marker)
  overlayById.set(point.id, { handle, point })
}

function removeStaleOverlays(nextIds: Set<string>) {
  for (const id of [...overlayById.keys()]) {
    if (nextIds.has(id)) {
      continue
    }
    const entry = overlayById.get(id)
    if (entry && map) {
      map.removeOverlay(entry.handle.overlay as BMapGL.Marker)
    }
    overlayById.delete(id)
    infoById.delete(id)
    overlaySnapById.delete(id)
    if (openMarkerId === id) {
      map?.closeInfoWindow()
      openMarkerId = null
    }
  }
}

function syncOverlays(list: EquipRecord[]) {
  if (!map || !window.BMapGL?.Point) {
    return
  }
  try {
    const points = toScenePoints(list)
    const nextIds = new Set(points.map((item) => item.id))
    for (const point of points) {
      upsertOverlay(point)
    }
    removeStaleOverlays(nextIds)
  } catch {
    /* 地图销毁或 GL 内部异常时忽略本次同步，避免打断 Vue 更新 */
  }
}

function fitMapToPoints(list: EquipRecord[]) {
  if (!map || !window.BMapGL || userInteracting) {
    return
  }
  const points = toScenePoints(list)
  if (points.length === 0) {
    return
  }

  let minLng = Infinity
  let maxLng = -Infinity
  let minLat = Infinity
  let maxLat = -Infinity

  for (const item of points) {
    const c = sceneDisplayCoord(item)
    minLng = Math.min(minLng, c.lng)
    maxLng = Math.max(maxLng, c.lng)
    minLat = Math.min(minLat, c.lat)
    maxLat = Math.max(maxLat, c.lat)
  }

  if (!Number.isFinite(minLng)) {
    return
  }

  const { Point } = window.BMapGL
  refreshMapSize()

  if (minLng === maxLng && minLat === maxLat) {
    map.centerAndZoom(new Point(minLng, minLat), 14)
  } else {
    try {
      map.setViewport(
        [new Point(minLng, minLat), new Point(maxLng, maxLat)],
        { margins: [48, 48, 48, 48] },
      )
    } catch {
      map.centerAndZoom(new Point((minLng + maxLng) / 2, (minLat + maxLat) / 2), 10)
    }
  }

  scheduleResize({ eager: true })
}

function scheduleOverlaysAndFit(list: EquipRecord[], fit: boolean) {
  const job = ++markerJob
  if (!fit) {
    window.requestAnimationFrame(() => {
      if (job !== markerJob || !map) {
        return
      }
      syncOverlays(list)
    })
    return
  }
  window.setTimeout(() => {
    if (job !== markerJob || !map) {
      return
    }
    fitMapToPoints(list)
    window.requestAnimationFrame(() => {
      if (job !== markerJob || !map) {
        return
      }
      syncOverlays(list)
      scheduleResize()
    })
  }, 200)
}

function initMap() {
  const BMap = window.BMapGL
  if (!BMap || map) {
    return
  }

  map = new BMap.Map(mapContainerId)
  map.centerAndZoom(new BMap.Point(118.796877, 32.060255), 6)
  map.enableScrollWheelZoom(true)
  applyBaiduMapStyle(map, BAIDU_MAP_SCREEN_STYLE_ID)
  unbindAttributionHide = bindBaiduMapAttributionHide(map, mapContainerId)

  map.addEventListener('dragstart', () => {
    userInteracting = true
  })
  map.addEventListener('zoomstart', () => {
    userInteracting = true
  })
  map.addEventListener('dragend', () => {
    userInteracting = false
    scheduleResize()
  })
  map.addEventListener('zoomend', () => {
    userInteracting = false
    scheduleResize()
  })

  mapReady.value = true
  mapLoading.value = false
  mapError.value = ''

  scheduleResize()
  scheduleOverlaysAndFit(props.devices, true)
  emit('ready')
}

function destroyMap() {
  markerJob += 1
  if (overlaySyncTimer != null) {
    clearTimeout(overlaySyncTimer)
    overlaySyncTimer = null
  }
  if (resizeTimer != null) {
    clearTimeout(resizeTimer)
    resizeTimer = null
  }
  if (map) {
    unbindAttributionHide?.()
    unbindAttributionHide = null
    for (const entry of overlayById.values()) {
      try {
        map.removeOverlay(entry.handle.overlay as BMapGL.Marker)
      } catch {
        /* ignore */
      }
    }
    safeDestroyBaiduMap(map, mapContainerId)
    map = null
  }
  overlayById.clear()
  infoById.clear()
  overlaySnapById.clear()
  openMarkerId = null
  mapReady.value = false
}

async function waitForContainerReady(maxMs = 5000): Promise<boolean> {
  const el = document.getElementById(mapContainerId)
  if (!el) {
    return false
  }
  const start = Date.now()
  while (Date.now() - start < maxMs) {
    if (el.offsetWidth > 0 && el.offsetHeight > 0) {
      return true
    }
    await new Promise((r) => window.setTimeout(r, 50))
  }
  const el2 = document.getElementById(mapContainerId)
  return Boolean(el2 && el2.offsetWidth > 0 && el2.offsetHeight > 0)
}

function failLoad(message: string) {
  mapLoading.value = false
  mapError.value = message
  emit('error', message)
}

async function bootstrapMap() {
  if (mapReady.value && map) {
    scheduleResize()
    return
  }
  if (mapLoading.value) {
    return
  }

  const token = ++initToken
  mapLoading.value = true
  mapError.value = ''

  if (!hasBaiduMapAk()) {
    failLoad(t('equipFormPage.mapKeyMissing'))
    return
  }

  try {
    await loadBaiduMapGl()
    if (token !== initToken) {
      return
    }
    await nextTick()
    if (!(await waitForContainerReady())) {
      failLoad(t('equipFormPage.mapLoadFailed'))
      return
    }
    if (token !== initToken) {
      return
    }
    initMap()
  } catch {
    failLoad(t('equipFormPage.mapLoadFailed'))
  } finally {
    if (token === initToken && (mapReady.value || mapError.value)) {
      mapLoading.value = false
    }
  }
}

watch(
  () => props.devices,
  (list) => {
    if (!mapReady.value) {
      return
    }
    if (overlaySyncTimer != null) {
      clearTimeout(overlaySyncTimer)
    }
    overlaySyncTimer = window.setTimeout(() => {
      overlaySyncTimer = null
      scheduleOverlaysAndFit(list, false)
    }, 100)
  },
)

watch(
  () => props.fitViewKey,
  () => {
    if (!mapReady.value) {
      return
    }
    userInteracting = false
    fitMapToPoints(props.devices)
  },
)

onMounted(() => {
  void nextTick(() => {
    void bootstrapMap()
  })
})

onUnmounted(() => {
  initToken++
  destroyMap()
})
</script>

<template>
  <div class="baidu-screen-map-wrap">
    <div
      v-if="mapError"
      class="baidu-screen-map__overlay baidu-screen-map__overlay--error"
    >
      {{ mapError }}
    </div>
    <div
      v-else-if="mapLoading"
      class="baidu-screen-map__overlay"
    >
      {{ t('gisPage.mapLoading') }}
    </div>
    <div
      :id="mapContainerId"
      class="baidu-screen-map"
      :style="{ height: `${mapHeightPx}px` }"
    />
  </div>
</template>

<style scoped>
.baidu-screen-map-wrap {
  position: relative;
  width: 100%;
  height: 100%;
  flex: 1;
  min-height: 0;
}

.baidu-screen-map {
  width: 100%;
  height: 100%;
  background: var(--map-land, #0f222c);
}

.baidu-screen-map__overlay {
  position: absolute;
  inset: 0;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(2, 8, 20, 0.88);
  color: #94a3b8;
  font-size: 13px;
}

.baidu-screen-map__overlay--error {
  color: var(--status-alarm);
}
</style>

<style>
.screen-scene-dot {
  position: absolute;
  z-index: 2;
  width: calc((var(--dot-r) + 35px) * 2);
  height: calc((var(--dot-r) + 35px) * 2);
  transform: translate(-50%, -50%);
  pointer-events: auto;
  cursor: pointer;
}

.screen-scene-dot__core,
.screen-scene-dot__halo,
.screen-scene-dot__ripple {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  pointer-events: none;
}

.screen-scene-dot__halo {
  width: calc(var(--dot-r) * 2 + 12px);
  height: calc(var(--dot-r) * 2 + 12px);
  background: var(--dot-color);
  opacity: 0.38;
  filter: blur(3px);
}

.screen-scene-dot__core {
  width: calc(var(--dot-r) * 2);
  height: calc(var(--dot-r) * 2);
  background: var(--dot-color);
  border: 1.5px solid var(--omes-color-text-tertiary);
  box-sizing: border-box;
}

.screen-scene-dot__ripple {
  width: calc(var(--dot-r) * 2);
  height: calc(var(--dot-r) * 2);
  background: var(--dot-color);
  opacity: 0;
  animation: screen-scene-dot-ripple var(--dot-ripple-period, 2200ms) ease-out infinite;
}

.screen-scene-dot__ripple--b {
  animation-delay: var(--dot-ripple-delay-b, -1144ms);
}

@keyframes screen-scene-dot-ripple {
  0% {
    width: calc(var(--dot-r) * 2 + 6px);
    height: calc(var(--dot-r) * 2 + 6px);
    opacity: 0.42;
  }
  100% {
    width: calc((var(--dot-r) + 35px) * 2);
    height: calc((var(--dot-r) + 35px) * 2);
    opacity: 0;
  }
}

.screen-map-popup-shell {
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  padding: 10px 12px;
  background: rgba(18, 36, 46, 0.98);
  color: #e8f4ff;
  font-size: 12px;
  line-height: 1.5;
  overflow: hidden;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.screen-map-popup-shell__title {
  margin: 0 28px 4px 0;
  font-size: 14px;
  font-weight: 700;
  color: #e2e8f0;
  line-height: 1.35;
  word-break: break-word;
}

.screen-map-popup-shell__title b {
  font-weight: 700;
}

.map-popup-row {
  display: block;
  margin-top: 1px;
}

.map-device-list {
  margin-top: 4px;
  max-height: min(52vh, 360px);
  overflow-x: hidden;
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
  scrollbar-color: rgba(91, 243, 249, 0.35) rgba(11, 23, 30, 0.6);
}

.map-device-list::-webkit-scrollbar {
  width: 5px;
  height: 0;
}

.map-device-list::-webkit-scrollbar-track {
  background: rgba(5, 14, 28, 0.6);
  border-radius: 3px;
}

.map-device-list::-webkit-scrollbar-thumb {
  background: rgba(91, 243, 249, 0.35);
  border-radius: 3px;
}

.map-device-list::-webkit-scrollbar-thumb:hover {
  background: rgba(91, 243, 249, 0.5);
}

.map-device-item {
  padding: 6px 0 8px;
  border-bottom: 1px dashed rgba(91, 243, 249, 0.2);
  white-space: normal;
}

.map-device-item:last-child {
  border-bottom: none;
}

.map-device-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 8px;
  margin-bottom: 2px;
  font-size: 11px;
  color: #cbd5e1;
}

.map-device-name {
  display: inline-block;
  color: #e2e8f0;
  margin-right: 2px;
  padding: 1px 6px;
  border-radius: 999px;
  border: 1px solid rgba(91, 243, 249, 0.38);
  background: linear-gradient(180deg, rgba(30, 58, 138, 0.35) 0%, rgba(15, 23, 42, 0.45) 100%);
  box-shadow: inset 0 0 8px rgba(91, 243, 249, 0.18);
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.map-popup-sub {
  display: block;
  font-size: 11px;
  color: #94a3b8;
  line-height: 1.45;
  word-break: break-word;
}

.map-device-code {
  display: inline;
  margin-left: 4px;
  opacity: 0.85;
  word-break: break-all;
}

.map-popup-section {
  margin-top: 6px;
  padding: 4px 0 2px 8px;
  border-left: 2px solid rgba(91, 243, 249, 0.35);
}

.map-popup-section-title {
  display: block;
  font-size: 10px;
  color: #7dd3fc;
  letter-spacing: 1px;
  margin-bottom: 3px;
}

.map-popup-alarm-line {
  color: var(--status-alarm);
}

.status-dot-map {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  vertical-align: middle;
  box-shadow: 0 0 8px currentColor;
  margin: 0 1px;
}

.status-dot-map.status-online {
  color: var(--color-status-online, #5bf3f9);
  background: var(--color-status-online, #5bf3f9);
}

.status-dot-map.status-running {
  color: var(--color-status-running, #6be6a7);
  background: var(--color-status-running, #6be6a7);
}

.status-dot-map.status-stopped {
  color: var(--color-status-stopped, #fcd066);
  background: var(--color-status-stopped, #fcd066);
}

.status-dot-map.status-alarm {
  color: var(--color-status-alarm, #ff5b60);
  background: var(--color-status-alarm, #ff5b60);
}

.status-dot-map.status-offline,
.status-dot-map.status-unknown {
  color: var(--color-status-offline, #4a6674);
  background: var(--color-status-offline, #4a6674);
}
</style>
