import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'
import type {
  MoFormPayload,
  MoPageQuery,
  MoRecord,
  MoTfRecord,
  MpsFlowPayload,
  MpsFlowPreviewRecord,
} from '@/types/mo'
import type { WorkshopNode } from '@/api/device'

export interface MapOption {
  id: string
  name: string
}

export interface LineRecord {
  id: string
  selfCode?: string
  name?: string
  tfs?: MoTfRecord[]
}

export interface DeviceBindRecord {
  selfCode?: string
  name?: string
  matCode?: string
}

export function fetchMoPage(params: MoPageQuery) {
  return postPage<MoRecord>('/mo/selectByPage', params)
}

export function fetchMoById(id: string) {
  return get<MoRecord>('/mo/selectById', { id })
}

export function saveMo(data: MoFormPayload) {
  return post<boolean>('/mo/addOrUpdate', data)
}

/** 仅 INIT 草稿清理；业务取消请用 adjustMo CANCEL_MO */
export function deleteMos(ids: string[]) {
  return post<boolean>('/mo/delete', { ids } satisfies IdsPayload)
}

export interface MoAdjustCommand {
  moCode: string
  adjustType:
    | 'CANCEL_MO'
    | 'CANCEL_MPS'
    | 'RESCHEDULE'
    | 'PRIORITY'
    | 'CHANGE_LINE'
    | 'CHANGE_DEV'
    | 'QTY_UP'
    | 'QTY_DOWN'
    | string
  payload?: Record<string, unknown>
  source?: string
  requestId?: string
  operator?: string
  force?: boolean
}

export interface MoAdjustResult {
  logId?: string
  requestId?: string
  moCode?: string
  adjustType?: string
  voidedMpsIds?: string[]
  affectedMpsIds?: string[]
  createdMpsIds?: string[]
  hints?: string[]
  warnings?: string[]
  surplusDelta?: number
  idempotentReplay?: boolean
  preview?: boolean
}

export interface MoAdjustPreviewResult {
  moCode?: string
  adjustType?: string
  allowed?: boolean
  rejectReason?: string
  wouldVoidMpsIds?: string[]
  wouldAffectMpsIds?: string[]
  surplusDelta?: number
  newNum?: number
  newSurplus?: number
  hints?: string[]
  warnings?: string[]
  requiresForce?: boolean
}

export function adjustMo(command: MoAdjustCommand) {
  return post<MoAdjustResult>('/mo/adjust', {
    source: 'UI',
    requestId: command.requestId || `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`,
    ...command,
  })
}

export function previewMoAdjust(command: MoAdjustCommand) {
  return post<MoAdjustPreviewResult>('/mo/adjust/preview', {
    source: 'UI',
    requestId: command.requestId || `preview-${Date.now()}`,
    ...command,
  })
}

export function cancelMo(moCode: string, opts?: { force?: boolean; operator?: string }) {
  return adjustMo({
    moCode,
    adjustType: 'CANCEL_MO',
    payload: {},
    force: opts?.force,
    operator: opts?.operator,
  })
}

export function cancelMps(moCode: string, mpsIds: string[], opts?: { force?: boolean; operator?: string }) {
  return adjustMo({
    moCode,
    adjustType: 'CANCEL_MPS',
    payload: { mpsIds },
    force: opts?.force,
    operator: opts?.operator,
  })
}

export function rescheduleMo(moCode: string, execTime: string, mpsIds?: string[]) {
  return adjustMo({
    moCode,
    adjustType: 'RESCHEDULE',
    payload: { execTime, mpsIds, dequeueQueued: true, syncMo: true },
  })
}

export function changeMoLine(moCode: string, newLineCode: string, mpsIds?: string[]) {
  return adjustMo({
    moCode,
    adjustType: 'CHANGE_LINE',
    payload: { newLineCode, mpsIds },
  })
}

export function changeMoDev(moCode: string, payload: {
  mpsId: string
  matCode: string
  devNo?: string
  devName?: string
}) {
  return adjustMo({
    moCode,
    adjustType: 'CHANGE_DEV',
    payload,
  })
}

export function qtyUpMo(moCode: string, delta: number) {
  return adjustMo({ moCode, adjustType: 'QTY_UP', payload: { delta } })
}

export function qtyDownMo(moCode: string, newNum: number, mpsIds?: string[]) {
  return adjustMo({ moCode, adjustType: 'QTY_DOWN', payload: { newNum, mpsIds } })
}

export function reconcileMo(moCode: string) {
  return get('/mo/adjust/reconcile', { moCode })
}

export function adjustMoPriority(payload: {
  moCode: string
  mpsId: string
  mode?: 'JUMP' | 'REORDER'
  pre?: string | null
  post?: string | null
}) {
  return adjustMo({
    moCode: payload.moCode,
    adjustType: 'PRIORITY',
    payload: {
      mode: payload.mode || 'JUMP',
      mpsId: payload.mpsId,
      current: payload.mpsId,
      pre: payload.pre,
      post: payload.post,
    },
  })
}

export async function fetchMoStatusOptions() {
  const list = await get<MapOption[]>('/mo/status')
  return (list || []).map((item) => ({
    value: Number(item.id),
    label: item.name,
  }))
}

export function fetchLinePage(params: PageQuery & { name?: string; selfCode?: string }) {
  return postPage<LineRecord>('/line/selectByPage', params)
}

export function fetchLineByCode(code: string) {
  return get<LineRecord>('/line/selectByCode', { code })
}

export function fetchLineById(id: string) {
  return get<LineRecord>('/line/selectById', { id })
}

export function fetchTfByLineId(lineId: string) {
  return get<MoTfRecord[]>('/tf/selectByLineId', { lineId })
}

export function fetchDevicesByDgId(dgId: string) {
  return get<DeviceBindRecord[]>('/device/selectByDgIdAndStatus', { dgId })
}

export function matchDeviceForMaterial(devices: DeviceBindRecord[] | undefined, matCode?: string) {
  if (!matCode) {
    return undefined
  }
  return (devices || []).find((dev) => dev.matCode === matCode)
}

export function calcMpsFlow(data: MpsFlowPayload) {
  return post<MpsFlowPreviewRecord[]>('/flow/mps', data)
}

export function completeMpsFlow(data: MpsFlowPreviewRecord[]) {
  return post<boolean>('/flow/mpsFlowComplete', data)
}

export function findWorkshopByCode(nodes: WorkshopNode[] | undefined, selfCode: string): WorkshopNode | null {
  if (!nodes?.length || !selfCode.trim()) {
    return null
  }
  for (const node of nodes) {
    if (node.selfCode === selfCode) {
      return node
    }
    const child = findWorkshopByCode(node.children, selfCode)
    if (child) {
      return child
    }
  }
  return null
}
