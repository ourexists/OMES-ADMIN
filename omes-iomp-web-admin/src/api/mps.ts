import { get, post, postPage } from '@/api/request'
import type { IdsPayload } from '@/types/api'
import type { MpsBoardData, MpsBoardQuery, MpsChangePriorityPayload, MpsPageQuery, MpsRecord } from '@/types/mps'

export interface MapOption {
  id: string
  name: string
}

export function fetchMpsPage(params: MpsPageQuery) {
  return postPage<MpsRecord>('/mps/selectByPage', {
    queryMO: true,
    queryLine: true,
    ...params,
  })
}

export function fetchMpsBoard(params: MpsBoardQuery) {
  return post<MpsBoardData>('/mps/selectBoard', {
    queryMO: true,
    queryLine: true,
    limitPerColumn: 300,
    ...params,
  })
}

export function fetchMpsById(id: string) {
  return get<MpsRecord>('/mps/selectById', { id })
}

export function deleteMpsList(ids: string[]) {
  return post<boolean>('/mps/delete', { ids } satisfies IdsPayload)
}

/** 批次取消（走统一调整入口） */
export function cancelMpsBatch(
  moCode: string,
  mpsIds: string[],
  opts?: { force?: boolean; operator?: string },
) {
  return post('/mo/adjust', {
    moCode,
    adjustType: 'CANCEL_MPS',
    source: 'UI',
    requestId: `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`,
    force: opts?.force,
    operator: opts?.operator,
    payload: { mpsIds },
  })
}

export function rescheduleMps(moCode: string, execTime: string, mpsIds: string[]) {
  return post('/mo/adjust', {
    moCode,
    adjustType: 'RESCHEDULE',
    source: 'UI',
    requestId: `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`,
    payload: { execTime, mpsIds, dequeueQueued: true, syncMo: true },
  })
}

/** 未开工批次改设备绑定（走统一调整入口） */
export function changeMpsDev(
  moCode: string,
  payload: { mpsId: string; matCode: string; devNo?: string; devName?: string },
) {
  return post('/mo/adjust', {
    moCode,
    adjustType: 'CHANGE_DEV',
    source: 'UI',
    requestId: `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`,
    payload,
  })
}

export async function fetchMpsStatusOptions() {
  const list = await get<MapOption[]>('/mps/status')
  return (list || []).map((item) => ({
    value: Number(item.id),
    label: item.name,
  }))
}

export function joinMpsQueue(id: string, type = 0) {
  return post<boolean>('/mps/joinQueue', { id, type })
}

export function joinMpsQueueBatch(ids: string[]) {
  return post<boolean>('/mps/joinQueueBatch', ids)
}

export function removeMpsQueue(id: string, type = 0) {
  return post<boolean>('/mps/removeQueue', { id, type })
}

export function jumpMpsQueue(id: string) {
  return post<boolean>('/mps/jumpQueue', { id })
}

export function changeMpsPriority(payload: MpsChangePriorityPayload) {
  return post<boolean>('/mps/changePriority', payload)
}

export function startMpsTf(tfId: string) {
  return get<boolean>('/mps/startTf', { tfId })
}
