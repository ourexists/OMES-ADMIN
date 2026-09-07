import { post, postPage } from '@/api/request'
import type { EquipRecord } from '@/api/device'
import type { EquipTrendPoint } from '@/api/overview'
import type { HealthIndicatorRecord } from '@/api/equip-health'
import { fetchHealthIndicatorsByStatTime } from '@/api/equip-health'

export interface EquipStateSnapshotQuery {
  startDate: string
  endDate: string
  workshopCode?: string | null
  onlineState?: number
  runState?: number
  alarmState?: number
  countType?: number
}

export function fetchEquipStateSnapshotTrend(query: EquipStateSnapshotQuery) {
  return post<EquipTrendPoint[]>('/equipStateSnapshot/countNumByTime', {
    needWorkshopCascade: true,
    ...query,
  })
}

export async function fetchScreenHealthSummary(workshopCode?: string | null) {
  const statTime = new Date()
  statTime.setHours(0, 0, 0, 0)
  const statTimeStr = formatStatTime(statTime)

  let list = await fetchHealthIndicatorsByStatTime(statTimeStr)

  if (workshopCode) {
    const page = await postPage<EquipRecord>('/equip/selectByPage', {
      workshopCode,
      needWorkshopCascade: true,
      requirePage: false,
    })
    const snSet = new Set(
      (page?.records ?? []).map((item) => item.selfCode).filter(Boolean) as string[],
    )
    if (snSet.size > 0) {
      list = list.filter((item) => item.sn && snSet.has(item.sn))
    }
  }

  return summarizeHealth(list)
}

function formatStatTime(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} 00:00:00`
}

export interface HealthSummary {
  healthy: number
  attention: number
  warning: number
  fault: number
  avgScore: number | null
}

function summarizeHealth(list: HealthIndicatorRecord[]): HealthSummary {
  const healthy = list.filter((d) => (d.healthLevel ?? 0) === 0).length
  const attention = list.filter((d) => (d.healthLevel ?? 0) === 1).length
  const warning = list.filter((d) => (d.healthLevel ?? 0) === 2).length
  const fault = list.filter((d) => (d.healthLevel ?? 0) === 3).length
  const total = list.length
  const avgScore =
    total > 0
      ? Math.round(list.reduce((sum, d) => sum + (d.score ?? 0), 0) / total)
      : null
  return { healthy, attention, warning, fault, avgScore }
}
