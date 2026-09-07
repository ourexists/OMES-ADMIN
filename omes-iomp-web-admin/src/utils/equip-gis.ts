import type { EquipRecord } from '@/api/device'

/** 解析单轴坐标；空串、null、非数字字符串返回 null（避免 Number('') === 0 误判） */
export function parseEquipCoord(value?: string | number | null): number | null {
  if (value == null) {
    return null
  }
  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (trimmed === '') {
      return null
    }
    const n = Number(trimmed)
    return Number.isFinite(n) ? n : null
  }
  const n = Number(value)
  return Number.isFinite(n) ? n : null
}

type CoordSource = { lng?: string | number | null; lat?: string | number | null }

export function parseEquipLngLat(item: CoordSource): { lng: number; lat: number } | null {
  const lng = parseEquipCoord(item.lng)
  const lat = parseEquipCoord(item.lat)
  if (lng == null || lat == null) {
    return null
  }
  if (lng === 0 && lat === 0) {
    return null
  }
  if (Math.abs(lng) > 180 || Math.abs(lat) > 90) {
    return null
  }
  return { lng, lat }
}

/** GIS 点位只取设备所属最后一级场景定位 */
export function resolveEquipSceneCoord(item: EquipRecord): { lng: number; lat: number } | null {
  return parseEquipLngLat({
    lng: item.workshop?.lng,
    lat: item.workshop?.lat,
  })
}

export function filterEquipWithCoords(list: EquipRecord[]): EquipRecord[] {
  return list.filter((item) => resolveEquipSceneCoord(item) != null)
}
