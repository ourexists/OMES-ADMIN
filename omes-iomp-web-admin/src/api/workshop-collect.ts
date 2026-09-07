import { post, postPage } from '@/api/request'
import type { PageQuery } from '@/types/api'

export interface WorkshopCollectRow {
  workshopId?: string
  time?: string
  data?: Record<string, string | number | null>
}

export interface WorkshopCollectQuery extends PageQuery {
  workshopId: string
  startDate: string
  endDate: string
}

function normalizeCollectRows(data: unknown): WorkshopCollectRow[] {
  if (Array.isArray(data)) {
    return data as WorkshopCollectRow[]
  }
  if (data && typeof data === 'object') {
    const payload = data as { records?: WorkshopCollectRow[]; data?: WorkshopCollectRow[] }
    if (Array.isArray(payload.records)) {
      return payload.records
    }
    if (Array.isArray(payload.data)) {
      return payload.data
    }
  }
  return []
}

/** 场景采集报表：兼容分页包装与直接数组两种响应 */
export async function fetchWorkshopCollectList(params: WorkshopCollectQuery) {
  const body = {
    requirePage: false,
    page: 1,
    pageSize: 50000,
    ...params,
  }
  try {
    const page = await postPage<WorkshopCollectRow>('/workshop/collect/selectByPage', body)
    const fromPage = normalizeCollectRows(page)
    if (fromPage.length) {
      return fromPage
    }
  } catch {
    /* postPage 在无 pagination 时可能不匹配，回退 post */
  }
  const raw = await post<unknown>('/workshop/collect/selectByPage', body)
  return normalizeCollectRows(raw)
}
