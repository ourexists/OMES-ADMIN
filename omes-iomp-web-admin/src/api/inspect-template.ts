import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'

export interface InspectTemplateItemRecord {
  id?: string
  templateId?: string
  productCode?: string
  referenceItemId?: string
  itemName?: string
  itemType?: number
  unit?: string
  sortOrder?: number
  weight?: number | null
  weightRate?: number | null
  ruleConfig?: string | null
}

export interface InspectTemplateRecord {
  id: string
  name?: string
  remark?: string
  createdTime?: string
  updatedTime?: string
  items?: InspectTemplateItemRecord[]
}

/** 规则配置 JSON 中单条规则 */
export interface InspectRuleRecord {
  ruleType: 1 | 2 | 3
  boolValue?: number
  minValue?: number | null
  maxValue?: number | null
  optionValue?: string
  weight: number
}

/** 模板编辑 UI：按产品分组 */
export interface TemplateProductBlock {
  key: string
  productCode: string
  rows: TemplateItemRow[]
}

export interface TemplateItemRow {
  key: string
  referenceItemId?: string
  itemName: string
  itemType: number
  unit?: string
  weight?: number | null
  weightRate?: number | null
  ruleConfig?: string
}

export function fetchInspectTemplatePage(params: PageQuery & { name?: string }) {
  return postPage<InspectTemplateRecord>('/inspection/template/selectByPage', params)
}

export function fetchInspectTemplateList() {
  return get<InspectTemplateRecord[]>('/inspection/template/selectList')
}

export function fetchInspectTemplateById(id: string) {
  return get<InspectTemplateRecord>('/inspection/template/selectById', { id })
}

export function fetchInspectTemplateWithItems(templateId: string) {
  return get<InspectTemplateRecord>('/inspection/template/selectWithItems', { templateId })
}

export function saveInspectTemplate(data: Partial<InspectTemplateRecord>) {
  return post<boolean>('/inspection/template/addOrUpdate', data)
}

export function deleteInspectTemplates(ids: string[]) {
  return post<boolean>('/inspection/template/delete', { ids } satisfies IdsPayload)
}

export function roundWeightRate(value: number): number {
  return Math.round(value * 10000) / 10000
}

export function parseRuleConfig(raw?: string | null): InspectRuleRecord[] {
  if (!raw?.trim()) {
    return []
  }
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

/** 详情页：格式化单条规则的条件描述（不含分值） */
export function formatRuleCondition(
  rule: InspectRuleRecord,
  itemType: number,
  labels: { yes: string; no: string; any: string },
): string {
  if (itemType === 3) {
    return rule.boolValue === 1 ? labels.yes : labels.no
  }
  if (itemType === 2) {
    const hasMin = rule.minValue != null && rule.minValue !== ('' as unknown as number)
    const hasMax = rule.maxValue != null && rule.maxValue !== ('' as unknown as number)
    if (hasMin && hasMax) {
      return `${rule.minValue} ~ ${rule.maxValue}`
    }
    if (hasMin) {
      return `≥ ${rule.minValue}`
    }
    if (hasMax) {
      return `≤ ${rule.maxValue}`
    }
    return labels.any
  }
  return (rule.optionValue || '').trim() || '-'
}

export function sumBlockWeight(rows: TemplateItemRow[]): number {
  return rows.reduce((sum, row) => sum + (Number(row.weight) || 0), 0)
}

export function templateItemsToProductBlocks(items: InspectTemplateItemRecord[] = []): TemplateProductBlock[] {
  const byProduct = new Map<string, TemplateItemRow[]>()
  for (const item of items) {
    const code = item.productCode || ''
    if (!byProduct.has(code)) {
      byProduct.set(code, [])
    }
    byProduct.get(code)!.push({
      key: item.id || `${code}-${byProduct.get(code)!.length}`,
      referenceItemId: item.referenceItemId || undefined,
      itemName: item.itemName || '',
      itemType: item.itemType ?? 2,
      unit: item.unit || '',
      weight: item.weight ?? null,
      weightRate: item.weightRate != null ? Number(item.weightRate) : null,
      ruleConfig: item.ruleConfig || undefined,
    })
  }
  if (byProduct.size === 0) {
    return [{ key: `block-${Date.now()}`, productCode: '', rows: [] }]
  }
  return Array.from(byProduct.entries()).map(([productCode, rows], index) => ({
    key: `block-${index}-${productCode}`,
    productCode,
    rows,
  }))
}

export function productBlocksToTemplateItems(
  blocks: TemplateProductBlock[],
  templateId?: string,
): InspectTemplateItemRecord[] {
  const items: InspectTemplateItemRecord[] = []
  let sortOrder = 0
  for (const block of blocks) {
    const productCode = block.productCode.trim() || null
    for (const row of block.rows) {
      const name = row.itemName.trim()
      if (!name) {
        continue
      }
      items.push({
        templateId: templateId || undefined,
        productCode: productCode || undefined,
        referenceItemId: row.referenceItemId || undefined,
        itemName: name,
        itemType: row.itemType,
        unit: row.unit?.trim() || undefined,
        sortOrder: sortOrder++,
        weight: row.weight ?? undefined,
        weightRate: row.weightRate ?? undefined,
        ruleConfig: row.ruleConfig?.trim() || undefined,
      })
    }
  }
  return items
}
