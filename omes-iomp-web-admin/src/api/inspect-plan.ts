import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'

export interface InspectPlanRecord {
  id: string
  name?: string
  templateId?: string
  templateName?: string
  cycleType?: number
  cycleTypeDesc?: string
  cycleConfig?: string
  workshopCode?: string
  equipIds?: string[]
  status?: number
  remark?: string
  createdTime?: string
  updatedTime?: string
}

export const INSPECT_PLAN_CYCLE_TYPES: Record<number, string> = {
  1: '每日',
  2: '每周',
  3: '每月',
}

export function inspectPlanCycleLabel(type?: number): string {
  if (type == null) {
    return '-'
  }
  return INSPECT_PLAN_CYCLE_TYPES[type] || String(type)
}

export function fetchInspectPlanPage(
  params: PageQuery & {
    name?: string
    templateId?: string
    cycleType?: number
    status?: number
  },
) {
  return postPage<InspectPlanRecord>('/inspection/plan/selectByPage', params)
}

export function fetchInspectPlanById(id: string) {
  return get<InspectPlanRecord>('/inspection/plan/selectById', { id })
}

export function saveInspectPlan(data: Partial<InspectPlanRecord>) {
  return post<boolean>('/inspection/plan/addOrUpdate', data)
}

export function deleteInspectPlans(ids: string[]) {
  return post<boolean>('/inspection/plan/delete', { ids } satisfies IdsPayload)
}

export function enableInspectPlan(planId: string) {
  return get<boolean>('/inspection/plan/enable', { planId })
}

export function disableInspectPlan(planId: string) {
  return get<boolean>('/inspection/plan/disable', { planId })
}

export function generateInspectPlanTasks(planId: string) {
  return get<boolean>('/inspection/plan/generateTasks', { planId })
}

export function fetchInspectPlanCycleTypes() {
  return get<Record<number, string>>('/inspection/plan/cycleTypes')
}
