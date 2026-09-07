<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { SearchOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { applyBaiduMapStyle, bindBaiduMapAttributionHide, hasBaiduMapAk, loadBaiduMapGl, safeDestroyBaiduMap } from '@/config/baidu-map'

const props = withDefaults(
  defineProps<{
    lat?: string | number
    lng?: string | number
    address?: string
    height?: number
    /** 紧凑布局：工具栏单行、无提示文案 */
    compact?: boolean
  }>(),
  {
    height: 220,
    compact: false,
  },
)

const emit = defineEmits<{
  'update:lat': [value: string]
  'update:lng': [value: string]
  'update:address': [value: string]
}>()

const { t } = useI18n()

const mapElRef = ref<HTMLElement | null>(null)
const searchKeyword = ref('')
const mapReady = ref(false)
const mapContainerId = `baidu-map-${Math.random().toString(36).slice(2, 10)}`

let map: BMapGL.Map | null = null
let unbindAttributionHide: (() => void) | null = null
let marker: BMapGL.Marker | null = null
let localSearch: BMapGL.LocalSearch | null = null
let skipEmit = false
let initToken = 0

function parseCoord(value?: string | number): number | null {
  if (value === '' || value == null) {
    return null
  }
  const n = Number(value)
  return Number.isFinite(n) ? n : null
}

function emitPoint(lat: number, lng: number) {
  skipEmit = true
  emit('update:lat', lat.toFixed(6))
  emit('update:lng', lng.toFixed(6))
  skipEmit = false
}

function refreshMapSize() {
  map?.checkResize()
}

defineExpose({ refreshMapSize })

function setMarker(lat: number, lng: number, moveView = true) {
  if (!map || !window.BMapGL) {
    return
  }
  const { Point, Marker } = window.BMapGL
  const point = new Point(lng, lat)
  if (marker) {
    map.removeOverlay(marker)
    marker = null
  }
  marker = new Marker(point)
  map.addOverlay(marker)
  if (moveView) {
    map.centerAndZoom(point, 15)
  }
}

function reverseAddress(lat: number, lng: number) {
  if (!window.BMapGL) {
    return
  }
  const point = new window.BMapGL.Point(lng, lat)
  const geoc = new window.BMapGL.Geocoder()
  geoc.getLocation(point, (rs) => {
    if (rs?.address) {
      emit('update:address', rs.address)
    }
  })
}

function applyPoint(lat: number, lng: number, moveView = true) {
  emitPoint(lat, lng)
  setMarker(lat, lng, moveView)
  reverseAddress(lat, lng)
}

async function waitForContainerReady(el: HTMLElement, maxMs = 4000): Promise<boolean> {
  const start = Date.now()
  while (Date.now() - start < maxMs) {
    if (el.offsetWidth > 0 && el.offsetHeight > 0) {
      return true
    }
    await new Promise((r) => window.setTimeout(r, 50))
  }
  return el.offsetWidth > 0 && el.offsetHeight > 0
}

function initMap() {
  const BMap = window.BMapGL
  if (!BMap || map) {
    return
  }

  map = new BMap.Map(mapContainerId)
  map.centerAndZoom(new BMap.Point(118.796877, 32.060255), 6)
  map.enableScrollWheelZoom(true)
  applyBaiduMapStyle(map)
  unbindAttributionHide = bindBaiduMapAttributionHide(map, mapContainerId)

  map.addEventListener('click', (e) => {
    if (!e?.latlng) {
      return
    }
    applyPoint(e.latlng.lat, e.latlng.lng)
  })

  localSearch = new BMap.LocalSearch(map, {
    onSearchComplete(results) {
      if (!results || results.getCurrentNumPois() === 0) {
        message.warning(t('equipFormPage.searchNotFound'))
        return
      }
      const poi = results.getPoi(0)
      applyPoint(poi.point.lat, poi.point.lng)
      const addr = poi.address ?? poi.title
      if (addr) {
        emit('update:address', addr)
      }
    },
  })

  mapReady.value = true

  const lat = parseCoord(props.lat)
  const lng = parseCoord(props.lng)
  if (lat != null && lng != null) {
    setMarker(lat, lng, true)
  }

  scheduleResize()
}

function scheduleResize() {
  const run = () => refreshMapSize()
  run()
  requestAnimationFrame(() => {
    run()
    requestAnimationFrame(run)
  })
  ;[120, 320, 600].forEach((ms) => window.setTimeout(run, ms))
}

function destroyMap() {
  if (map) {
    unbindAttributionHide?.()
    unbindAttributionHide = null
    if (marker) {
      try {
        map.removeOverlay(marker)
      } catch {
        /* ignore */
      }
      marker = null
    }
    safeDestroyBaiduMap(map, mapContainerId)
    map = null
  }
  localSearch = null
  mapReady.value = false
}

async function bootstrapMap() {
  const token = ++initToken
  if (!hasBaiduMapAk()) {
    message.warning(t('equipFormPage.mapKeyMissing'))
    return
  }

  try {
    await loadBaiduMapGl()
    if (token !== initToken) {
      return
    }
    await nextTick()
    const el = mapElRef.value
    if (!el || !(await waitForContainerReady(el))) {
      message.error(t('equipFormPage.mapLoadFailed'))
      return
    }
    if (token !== initToken) {
      return
    }
    initMap()
  } catch {
    message.error(t('equipFormPage.mapLoadFailed'))
  }
}

function onSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    message.warning(t('equipFormPage.searchRequired'))
    return
  }
  if (!hasBaiduMapAk()) {
    message.warning(t('equipFormPage.mapKeyMissing'))
    return
  }
  if (!localSearch) {
    message.error(t('equipFormPage.searchFailed'))
    return
  }
  localSearch.search(keyword)
}

watch(
  () => [props.lat, props.lng] as const,
  ([lat, lng]) => {
    if (skipEmit || !mapReady.value) {
      return
    }
    const la = parseCoord(lat)
    const ln = parseCoord(lng)
    if (la != null && ln != null) {
      setMarker(la, ln, true)
    }
  },
)

onMounted(() => {
  void bootstrapMap()
})

onUnmounted(() => {
  initToken++
  destroyMap()
})
</script>

<template>
  <div class="map-picker" :class="{ 'map-picker--compact': compact }">
    <template v-if="compact">
      <a-input
        :value="address"
        allow-clear
        class="map-picker__address"
        :placeholder="t('equipFormPage.addressPlaceholder')"
        @update:value="emit('update:address', $event)"
      />
      <div class="map-picker__toolbar">
        <a-input
          :value="lng"
          disabled
          class="map-picker__coord"
          :addon-before="t('equipFormPage.lng')"
        />
        <a-input
          :value="lat"
          disabled
          class="map-picker__coord"
          :addon-before="t('equipFormPage.lat')"
        />
        <a-input
          v-model:value="searchKeyword"
          allow-clear
          class="map-picker__search-input"
          :placeholder="t('equipFormPage.searchMapPlaceholder')"
          @press-enter="onSearch"
        >
          <template #prefix>
            <SearchOutlined />
          </template>
        </a-input>
        <a-button type="primary" class="map-picker__search-btn" @click="onSearch">
          {{ t('equipFormPage.searchMap') }}
        </a-button>
      </div>
    </template>
    <template v-else>
      <div class="map-picker__coords">
        <div class="coord-field">
          <span class="coord-label">{{ t('equipFormPage.lng') }}</span>
          <a-input :value="lng" disabled size="small" class="coord-input" />
        </div>
        <div class="coord-field">
          <span class="coord-label">{{ t('equipFormPage.lat') }}</span>
          <a-input :value="lat" disabled size="small" class="coord-input" />
        </div>
      </div>
      <a-input
        :value="address"
        size="small"
        allow-clear
        class="map-picker__address"
        :placeholder="t('equipFormPage.addressPlaceholder')"
        @update:value="emit('update:address', $event)"
      />
      <div class="map-picker__search">
        <a-input
          v-model:value="searchKeyword"
          allow-clear
          size="small"
          :placeholder="t('equipFormPage.searchMapPlaceholder')"
          @press-enter="onSearch"
        >
          <template #prefix>
            <SearchOutlined />
          </template>
        </a-input>
        <a-button type="primary" size="small" @click="onSearch">{{ t('equipFormPage.searchMap') }}</a-button>
      </div>
    </template>
    <div
      :id="mapContainerId"
      ref="mapElRef"
      class="map-picker__canvas"
      :style="{ height: `${height}px` }"
      :title="compact ? t('equipFormPage.mapHint') : undefined"
    />
  </div>
</template>

<style scoped>
.map-picker {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.map-picker--compact {
  gap: 8px;
}

.map-picker--compact .map-picker__coord :deep(.ant-input),
.map-picker--compact .map-picker__coord :deep(.ant-input-group-addon) {
  font-size: 12px;
}

.map-picker__coords {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.coord-field {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.coord-label {
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
}

.coord-input :deep(.ant-input) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
}

.map-picker__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.map-picker__coord {
  flex: 1 1 120px;
  min-width: 100px;
  max-width: 160px;
}

.map-picker__coord :deep(.ant-input) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 11px;
}

.map-picker__coord :deep(.ant-input-group-addon) {
  padding: 0 6px;
  font-size: 11px;
}

.map-picker__search-input {
  flex: 2 1 140px;
  min-width: 120px;
}

.map-picker__search-btn {
  flex: 0 0 auto;
}

.map-picker__search {
  display: flex;
  gap: 6px;
}

.map-picker__search .ant-input-affix-wrapper {
  flex: 1;
  min-width: 0;
}

.map-picker__canvas {
  width: 100%;
  position: relative;
  border-radius: var(--omes-radius-sm);
  overflow: hidden;
  border: 1px solid var(--omes-color-border-hover);
  background: var(--omes-color-bg-layout);
}
</style>
