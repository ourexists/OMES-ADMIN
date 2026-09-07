import { get, post, postPage } from '@/api/request'
import { OAUTH_CLIENT } from '@/config'

export interface EquipCountResult {
  total?: number
  online?: number
  offline?: number
  alarm?: number
  run?: number
  stopped?: number
}

export interface EquipTrendPoint {
  time?: string | Date
  num?: number
}

export interface AlarmMessageRecord {
  id: string
  title?: string
  context?: string
  readStatus?: number
  createdTime?: string
}

/** 管理端设备统计/趋势查询范围（按场景筛选，不按移动端角色场景限制） */
function buildEquipScope(workshopCode?: string | null) {
  return {
    needWorkshopCascade: true,
    workshopCode: workshopCode ?? null,
  }
}

export function fetchEquipRealtimeCount(workshopCode?: string | null) {
  return post<EquipCountResult>('/equip/countRealtime', buildEquipScope(workshopCode))
}

export function fetchEquipOnlineTrend(workshopCode: string | null | undefined, startDate: string, endDate: string) {
  return post<EquipTrendPoint[]>('/equipStateSnapshot/countNumByTime', {
    startDate,
    endDate,
    onlineState: 1,
    countType: 1,
    ...buildEquipScope(workshopCode),
  })
}

export function fetchAlarmMessagePage(page: number, pageSize: number) {
  return postPage<AlarmMessageRecord>('/message/selectByPage', {
    page,
    pageSize,
    platform: OAUTH_CLIENT.platform,
    limitCurrentUser: true,
    type: 1,
  })
}

export function markAlarmMessageRead(messageId: string) {
  return get<boolean>('/message/read', { messageId })
}
