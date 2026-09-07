import type { EquipAttrItem, EquipRecord } from '@/api/device'
import { resolveEquipSceneCoord } from '@/utils/equip-gis'

/** 大屏状态色（荧光青蓝工业风，适配深色底） */
export const TECH_SCREEN_MAP_COLORS = {
  online: '#5bf3f9',
  offline: '#4a6674',
  alarm: '#ff5b60',
  running: '#6be6a7',
  stopped: '#fcd066',
  unknown: '#4a6674',
} as const

export interface SceneStats {
  total: number
  online: number
  offline: number
  running: number
  stopped: number
  alarm: number
}

export interface SceneAggDevice {
  name: string
  selfCode?: string
  onlineState?: number
  runState?: number
  alarmState?: number
  attrs?: EquipAttrItem[]
  alarmTexts?: string[]
}

export interface SceneAggregatePoint {
  id: string
  sceneCode: string
  sceneName: string
  lng: number
  lat: number
  offsetLng?: number
  offsetLat?: number
  sceneStats: SceneStats
  devices: SceneAggDevice[]
  isSceneAggregate: true
}

interface SceneGroupBucket {
  sceneCode: string
  sceneName: string
  items: EquipRecord[]
  lng: number
  lat: number
  sceneStats: SceneStats
}

/** 按最后一级场景聚合；点位取该场景定位，点击展示该位置下全部设备状态 */
export function aggregateByScene(items: EquipRecord[]): Omit<SceneAggregatePoint, 'id'>[] {
  const groups = new Map<string, SceneGroupBucket>()

  for (const item of items) {
    const coord = resolveEquipSceneCoord(item)
    if (!coord) {
      continue
    }
    const wk = item.workshop
    const sceneCode =
      item.workshopCode ||
      wk?.selfCode ||
      `scene_${coord.lng.toFixed(4)}_${coord.lat.toFixed(4)}`
    const sceneName = wk?.name || '未命名场景'

    if (!groups.has(sceneCode)) {
      groups.set(sceneCode, {
        sceneCode,
        sceneName,
        items: [],
        lng: coord.lng,
        lat: coord.lat,
        sceneStats: { total: 0, online: 0, offline: 0, running: 0, stopped: 0, alarm: 0 },
      })
    }
    const g = groups.get(sceneCode)!
    g.items.push(item)
    g.sceneStats.total += 1
    if (item.onlineState === 1) {
      g.sceneStats.online += 1
    } else {
      g.sceneStats.offline += 1
    }
    if (item.runState === 1) {
      g.sceneStats.running += 1
    }
    if (item.runState === 0) {
      g.sceneStats.stopped += 1
    }
    if ((item.alarmState ?? 0) >= 1) {
      g.sceneStats.alarm += 1
    }
  }

  const result: Omit<SceneAggregatePoint, 'id'>[] = []
  for (const g of groups.values()) {
    const devices: SceneAggDevice[] = g.items.map((d) => ({
      name: d.name || d.selfCode || '-',
      selfCode: d.selfCode,
      onlineState: d.onlineState,
      runState: d.runState,
      alarmState: d.alarmState,
      attrs: d.attrs,
      alarmTexts: d.alarmTexts,
    }))
    result.push({
      sceneCode: g.sceneCode,
      sceneName: g.sceneName,
      lng: g.lng,
      lat: g.lat,
      sceneStats: g.sceneStats,
      devices,
      isSceneAggregate: true,
    })
  }
  return result
}

/** 同坐标多场景点时螺旋散开（与 equip_screen_tech.html applyMapPointOffset 一致） */
export function applyMapPointOffset<T extends { lng: number; lat: number }>(items: T[]): T[] {
  const groups = new Map<string, T[]>()
  for (const item of items) {
    const key = `${item.lng.toFixed(5)}_${item.lat.toFixed(5)}`
    const bucket = groups.get(key)
    if (bucket) {
      bucket.push(item)
    } else {
      groups.set(key, [item])
    }
  }

  const result: T[] = []
  for (const group of groups.values()) {
    if (group.length === 1) {
      result.push(group[0])
      continue
    }
    const baseLng = group[0].lng
    const baseLat = group[0].lat
    const latRad = (baseLat * Math.PI) / 180
    const lngMeter = Math.max(1, 111320 * Math.cos(latRad))
    const latMeter = 110540
    const stepMeter = 36
    group.forEach((item, idx) => {
      const ring = Math.floor(idx / 8) + 1
      const angle = (idx % 8) * (Math.PI / 4) + ring * 0.25
      const meter = stepMeter * ring
      const lngOffset = (Math.cos(angle) * meter) / lngMeter
      const latOffset = (Math.sin(angle) * meter) / latMeter
      result.push({
        ...item,
        offsetLng: baseLng + lngOffset,
        offsetLat: baseLat + latOffset,
      } as T)
    })
  }
  return result
}

export function prepareSceneMapPoints(items: EquipRecord[]): SceneAggregatePoint[] {
  const aggregated = aggregateByScene(items)
  const offset = applyMapPointOffset(aggregated)
  return offset.map((item) => {
    const lng = item.offsetLng ?? item.lng
    const lat = item.offsetLat ?? item.lat
    const id =
      item.sceneCode ||
      `sc_${lng.toFixed(5)}_${lat.toFixed(5)}`
    return { ...item, id } as SceneAggregatePoint
  })
}

export function sceneDisplayCoord(point: SceneAggregatePoint): { lng: number; lat: number } {
  return {
    lng: point.offsetLng ?? point.lng,
    lat: point.offsetLat ?? point.lat,
  }
}

/** 与 equip_screen_tech.html mapDotFillColor sceneStats 分支一致 */
export function mapDotFillColor(point: SceneAggregatePoint): string {
  const st = point.sceneStats
  const total = st.total
  const offline = st.offline
  if (st.alarm > 0) {
    return TECH_SCREEN_MAP_COLORS.alarm
  }
  if (total > 0 && offline >= total) {
    return TECH_SCREEN_MAP_COLORS.offline
  }
  return TECH_SCREEN_MAP_COLORS.online
}

/** 与 equip_screen_tech.html mapDotShouldRipple sceneStats 分支一致 */
export function mapDotShouldRipple(point: SceneAggregatePoint): boolean {
  const st = point.sceneStats
  const total = st.total
  const offline = st.offline
  if (total > 0 && offline >= total) {
    return false
  }
  return true
}

/** 与 equip_screen_tech.html mapDotRadius sceneStats 分支一致（MapLibre 像素半径） */
export function mapDotRadius(point: SceneAggregatePoint): number {
  const c = point.sceneStats.total
  return Math.min(9, Math.max(5, 4.2 + c * 0.38))
}

/** 与 techScreenMaplibre.js 默认 ripple 周期一致（供 CSS 动画使用） */
export const SCENE_DOT_RIPPLE_PERIOD_MS = 2200
export const SCENE_DOT_RIPPLE_PHASE_OFFSET = 0.52

/** 聚合点展示快照，用于地图 overlay 跳过无变化更新 */
export function scenePointOverlaySnapshot(point: SceneAggregatePoint): string {
  const st = point.sceneStats
  const lng = point.offsetLng ?? point.lng
  const lat = point.offsetLat ?? point.lat
  return [
    point.id,
    st.total,
    st.online,
    st.offline,
    st.running,
    st.stopped,
    st.alarm,
    lng.toFixed(6),
    lat.toFixed(6),
    mapDotFillColor(point),
    mapDotRadius(point),
    mapDotShouldRipple(point) ? 1 : 0,
  ].join('|')
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function statusDotHtml(kind: 'online' | 'offline' | 'running' | 'stopped' | 'alarm' | 'unknown'): string {
  const classMap: Record<string, string> = {
    online: 'status-online',
    offline: 'status-offline',
    running: 'status-running',
    stopped: 'status-stopped',
    alarm: 'status-alarm',
    unknown: 'status-unknown',
  }
  return `<span class="status-dot-map ${classMap[kind]}"></span>`
}

function resolveRunDot(dev: SceneAggDevice): string {
  if (dev.onlineState !== 1) {
    return statusDotHtml('offline')
  }
  if (dev.runState === 1) {
    return statusDotHtml('running')
  }
  if (dev.runState === -1) {
    return statusDotHtml('unknown')
  }
  return statusDotHtml('stopped')
}

function resolveAlarmDot(dev: SceneAggDevice): string {
  if (dev.onlineState !== 1) {
    return statusDotHtml('offline')
  }
  if ((dev.alarmState ?? 0) >= 1) {
    return statusDotHtml('alarm')
  }
  if (dev.alarmState === -1) {
    return statusDotHtml('unknown')
  }
  return statusDotHtml('offline')
}

function formatAttrValue(value: unknown): string {
  if (value == null || value === '') {
    return '—'
  }
  if (typeof value === 'object') {
    return escapeHtml(JSON.stringify(value))
  }
  return escapeHtml(String(value))
}

type TranslateFn = (key: string, params?: Record<string, unknown>) => string

function mapPopupAttrsHtml(t: TranslateFn, attrs: SceneAggDevice['attrs']): string {
  if (!attrs?.length) {
    return ''
  }
  const lines = [
    '<div class="map-popup-section">',
    `<span class="map-popup-section-title">${escapeHtml(t('equipScreenPage.popupRealtimeAttrs'))}</span>`,
  ]
  for (const attr of attrs) {
    const unit = attr.unit ? ` ${escapeHtml(attr.unit)}` : ''
    lines.push(
      `<span class="map-popup-row map-popup-sub">${escapeHtml(attr.name ?? '—')}：${formatAttrValue(attr.value)}${unit}</span>`,
    )
  }
  lines.push('</div>')
  return lines.join('')
}

function mapPopupAlarmsHtml(t: TranslateFn, alarmTexts: string[] | undefined): string {
  if (!alarmTexts?.length) {
    return ''
  }
  const lines = [
    '<div class="map-popup-section">',
    `<span class="map-popup-section-title">${escapeHtml(t('equipScreenPage.popupAlarmInfo'))}</span>`,
  ]
  for (const txt of alarmTexts) {
    lines.push(
      `<span class="map-popup-row map-popup-sub map-popup-alarm-line">${escapeHtml(txt)}</span>`,
    )
  }
  lines.push('</div>')
  return lines.join('')
}

/** 场景聚合点 InfoWindow（对齐 equip_screen_tech createPopupHtml） */
export function buildSceneInfoWindowHtml(t: TranslateFn, point: SceneAggregatePoint): string {
  const st = point.sceneStats
  const lines: string[] = [
    `<span class="map-popup-row">${escapeHtml(t('equipScreenPage.mapSceneSummary', { total: st.total, online: st.online, offline: st.offline }))}</span>`,
    '<div class="map-device-list">',
  ]

  for (const dev of point.devices) {
    const onlineDot =
      dev.onlineState === 1 ? statusDotHtml('online') : statusDotHtml('offline')
    const runDot = resolveRunDot(dev)
    const alarmDot = resolveAlarmDot(dev)
    const codeHint = dev.selfCode
      ? `<span class="map-popup-sub map-device-code">${escapeHtml(dev.selfCode)}</span>`
      : ''
    const states = [
      `${escapeHtml(t('equipScreenPage.popupOnline'))}:${onlineDot}`,
      `${escapeHtml(t('equipScreenPage.popupRun'))}:${runDot}`,
      `${escapeHtml(t('equipScreenPage.popupAlarm'))}:${alarmDot}`,
    ].join(' ')
    lines.push(
      '<div class="map-device-item">',
      `<div class="map-device-head"><span class="map-device-name">${escapeHtml(dev.name || '—')}</span>${codeHint}<span>${states}</span></div>`,
      mapPopupAttrsHtml(t, dev.attrs),
      mapPopupAlarmsHtml(t, dev.alarmTexts),
      '</div>',
    )
  }

  lines.push('</div>')
  return lines.join('')
}

export function buildSceneInfoWindowTitle(point: SceneAggregatePoint): string {
  return point.sceneName || point.sceneCode || '场景'
}
