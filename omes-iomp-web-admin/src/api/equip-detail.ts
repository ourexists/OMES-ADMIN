import { get, post, postPage } from '@/api/request'
import type { PageQuery } from '@/types/api'
import type { EquipGwBinding } from '@/types/equip-config'
import type { EquipRecord } from '@/api/device'

export interface EquipRecordCountQuery {
  sn: string
  startDate: string
  endDate: string
  requirePage?: boolean
}

export interface EquipStateSegment {
  sn?: string
  startTime?: string
  endTime?: string
  state?: number
  level?: number
  reason?: string
  duration?: string | number
  powerStart?: string | number
  powerEnd?: string | number
  powerUse?: string | number
}

export interface EquipCollectRow {
  time?: string
  data?: Record<string, unknown>
}

export interface WriteControlPayload {
  equipId: string
  address: string
  value: number | string
}

export function fetchEquipDetailRealtime(id: string) {
  return get<EquipRecord>('/equip/selectRealtimeById', { id })
}

export function fetchEquipConfigBySn(equipSn: string) {
  return get<EquipGwBinding>('/equip/queryEquipConfigBySn', { equipSn })
}

export function writeEquipControl(payload: WriteControlPayload) {
  return post<boolean>('/equip/writeControl', payload)
}

export function fetchEquipOnlineSegments(query: EquipRecordCountQuery) {
  return post<EquipStateSegment[]>('/equipRecordOnline/countMerging', {
    ...query,
    requirePage: false,
  })
}

export function fetchEquipRunSegments(query: EquipRecordCountQuery) {
  return post<EquipStateSegment[]>('/equipRecordRun/countMerging', {
    ...query,
    requirePage: false,
  })
}

export function fetchEquipAlarmSegments(query: EquipRecordCountQuery) {
  return post<EquipStateSegment[]>('/equipRecordAlarm/countMerging', {
    ...query,
    requirePage: false,
  })
}

export function fetchEquipCollectPage(params: PageQuery & Record<string, unknown>) {
  return postPage<EquipCollectRow>('/equip/collect/selectByPage', params)
}

export function fetchEquipRecordRunPage(params: PageQuery & Record<string, unknown>) {
  return postPage<EquipStateSegment>('/equipRecordRun/selectByPage', params)
}

export function fetchEquipRecordOnlinePage(params: PageQuery & Record<string, unknown>) {
  return postPage<EquipStateSegment>('/equipRecordOnline/selectByPage', params)
}

export function fetchEquipRecordAlarmPage(params: PageQuery & Record<string, unknown>) {
  return postPage<EquipStateSegment>('/equipRecordAlarm/selectByPage', params)
}
