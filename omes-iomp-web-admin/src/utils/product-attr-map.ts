import type {
  EquipAlarmRow,
  EquipAttrRow,
  EquipConfigDetail,
  EquipControlRow,
  ProductAttrConfig,
} from '@/types/equip-config'

function normalize(value?: string) {
  return (value || '').trim()
}

function attrKey(row?: EquipAttrRow) {
  return normalize(row?.name)
}

function alarmKey(row?: EquipAlarmRow) {
  return normalize(row?.name) || normalize(row?.text)
}

function controlKey(row?: EquipControlRow) {
  return normalize(row?.name)
}

function indexBy<T>(list: T[] | undefined, keyFn: (item: T) => string) {
  const map = new Map<string, T>()
  for (const item of list || []) {
    const key = keyFn(item)
    if (key && !map.has(key)) {
      map.set(key, item)
    }
  }
  return map
}

export function hasProductAttrTemplate(config?: ProductAttrConfig | null) {
  return Boolean(
    (config?.attrs && config.attrs.some((row) => attrKey(row))) ||
      (config?.alarms && config.alarms.some((row) => alarmKey(row))) ||
      (config?.controls && config.controls.some((row) => controlKey(row))),
  )
}

export function stripProductAttrMeta<T extends { fromProduct?: boolean }>(rows: T[]): Omit<T, 'fromProduct'>[] {
  return rows.map(({ fromProduct: _fromProduct, ...rest }) => rest)
}

export function mergeProductAttrConfig(
  template: ProductAttrConfig | undefined | null,
  device: EquipConfigDetail | undefined | null,
): EquipConfigDetail {
  const existing = device || {}
  if (!template) {
    return {
      ...existing,
      attrs: existing.attrs || [],
      alarms: existing.alarms || [],
      controls: existing.controls || [],
    }
  }
  return {
    ...existing,
    attrs: mergeAttrs(template.attrs, existing.attrs),
    alarms: mergeAlarms(template.alarms, existing.alarms),
    controls: mergeControls(template.controls, existing.controls),
  }
}

function mergeAttrs(templates?: EquipAttrRow[], existing?: EquipAttrRow[]) {
  const byName = indexBy(existing, attrKey)
  const result: EquipAttrRow[] = []
  for (const tpl of templates || []) {
    const key = attrKey(tpl)
    const hit = key ? byName.get(key) : undefined
    result.push({
      ...tpl,
      map: hit?.map ?? '',
      value: hit?.value ?? tpl.value,
      needCollect: hit?.needCollect ?? tpl.needCollect,
      fluctuationEnabled: hit?.fluctuationEnabled ?? tpl.fluctuationEnabled,
    })
  }
  return result
}

function mergeAlarms(templates?: EquipAlarmRow[], existing?: EquipAlarmRow[]) {
  const byKey = indexBy(existing, alarmKey)
  const result: EquipAlarmRow[] = []
  for (const tpl of templates || []) {
    const key = alarmKey(tpl)
    let hit = key ? byKey.get(key) : undefined
    if (!hit && normalize(tpl.text)) {
      hit = byKey.get(normalize(tpl.text))
    }
    result.push({
      ...tpl,
      map: hit?.map ?? '',
    })
  }
  return result
}

function mergeControls(templates?: EquipControlRow[], existing?: EquipControlRow[]) {
  const byName = indexBy(existing, controlKey)
  const result: EquipControlRow[] = []
  for (const tpl of templates || []) {
    const key = controlKey(tpl)
    const hit = key ? byName.get(key) : undefined
    result.push({
      ...tpl,
      map: hit?.map ?? '',
      value: hit?.value ?? tpl.value,
    })
  }
  return result
}
