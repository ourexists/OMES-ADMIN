import type { EquipRecord } from '@/api/device'

/** 用于判断实时数据是否变化，避免轮询时整表重渲染 */
export function equipRealtimeSnapshot(item: EquipRecord): string {
  const attrs =
    item.attrs?.map((a) => `${a.name ?? ''}\x00${String(a.value ?? '')}\x00${a.unit ?? ''}`).join('\x01') ?? ''
  const alarms = item.alarmTexts?.join('\x01') ?? ''
  return [
    item.id,
    item.name ?? '',
    item.selfCode ?? '',
    item.onlineState ?? '',
    item.runState ?? '',
    item.alarmState ?? '',
    item.workshop?.name ?? '',
    item.workshop?.lng ?? '',
    item.workshop?.lat ?? '',
    item.typeDesc ?? '',
    item.type ?? '',
    alarms,
    attrs,
  ].join('\x00')
}

/**
 * 合并轮询结果：未变化的设备保留原对象引用，供 v-memo / 子组件跳过更新。
 * @returns 新数组；若与当前展示等价则返回 null（无需触发响应式）
 */
export function mergeEquipRealtimeList(
  prev: EquipRecord[],
  next: EquipRecord[],
): EquipRecord[] | null {
  if (prev.length === 0) {
    return next
  }
  if (prev.length !== next.length) {
    return next
  }

  const prevById = new Map(prev.map((item) => [item.id, item]))
  const merged: EquipRecord[] = new Array(next.length)
  let changed = false

  for (let i = 0; i < next.length; i += 1) {
    const incoming = next[i]
    const existing = prevById.get(incoming.id)
    if (existing && equipRealtimeSnapshot(existing) === equipRealtimeSnapshot(incoming)) {
      merged[i] = existing
      if (prev[i] !== existing) {
        changed = true
      }
    } else {
      merged[i] = incoming
      changed = true
    }
  }

  if (!changed) {
    for (let i = 0; i < next.length; i += 1) {
      if (prev[i]?.id !== next[i].id) {
        changed = true
        break
      }
    }
  }

  return changed ? merged : null
}
