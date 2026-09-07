import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'

export interface InspectItemRecord {
  id: string
  itemName?: string
  itemType?: number
  itemTypeDesc?: string
  unit?: string
  minValue?: number | null
  maxValue?: number | null
  requiredFlag?: boolean
  templateId?: string | null
  templateName?: string
  createdTime?: string
  updatedTime?: string
}

export const INSPECT_ITEM_TYPES: Record<number, string> = {
  1: '选择',
  2: '数值',
  3: '是否',
}

export function inspectItemTypeLabel(type?: number): string {
  if (type == null) {
    return '-'
  }
  return INSPECT_ITEM_TYPES[type] || String(type)
}

/** 解析选择型巡检项 unit 字段中的选项列表 */
export function parseChoiceOptions(unit?: string | null): string[] {
  if (!unit?.trim()) {
    return []
  }
  return unit
    .split(/[,，、/／|｜]/)
    .map((s) => s.trim())
    .filter(Boolean)
}

/** 将选项列表序列化写入 unit 字段（与移动端、模板规则解析一致，使用 / 分隔） */
export function serializeChoiceOptions(options: string[]): string | undefined {
  const cleaned = options.map((o) => o.trim()).filter(Boolean)
  return cleaned.length > 0 ? cleaned.join('/') : undefined
}

export function fetchInspectItemPage(params: PageQuery & { itemName?: string; itemType?: number }) {
  return postPage<InspectItemRecord>('/inspection/item/selectByPage', params)
}

export function fetchInspectItemById(id: string) {
  return get<InspectItemRecord>('/inspection/item/selectById', { id })
}

export function fetchInspectItemPool() {
  return get<InspectItemRecord[]>('/inspection/item/listAllPool')
}

export function saveInspectItem(data: Partial<InspectItemRecord>) {
  return post<boolean>('/inspection/item/addOrUpdate', data)
}

export function deleteInspectItems(ids: string[]) {
  return post<boolean>('/inspection/item/delete', { ids } satisfies IdsPayload)
}
