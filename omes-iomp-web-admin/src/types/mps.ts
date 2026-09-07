import type { PageQuery } from '@/types/api'
import type { TfEquipmentRef, TfRecord, TfToolingRef } from '@/types/line'
import type { MoRecord } from '@/types/mo'

export const MPS_TF_STATUS = {
  PENDING: 0,
  RUNNING: 1,
  DONE: 2,
  STOP: 3,
  ERROR: 4,
} as const

export const MPS_STATUS = {
  WAIT_QUE: 0,
  WAIT_EXEC: 1,
  EXECING: 2,
  COMPLETE: 3,
  FILE: 4,
  CANCEL: 5,
} as const

export interface LineBrief {
  id?: string
  selfCode?: string
  name?: string
  tfs?: TfRecord[]
}

export interface MpsDetailRecord {
  id?: string
  matName?: string
  matCode?: string
  matNum?: number | string
  actualNum?: number | string
  devNo?: string
  devName?: string
  dgCode?: string
  dgName?: string
  priority?: number
}

export interface MpsTfRecord {
  id?: string
  selfCode?: string
  name?: string
  status?: number
  statusDesc?: string
  pre?: string
  nex?: string
  startTime?: string
  endTime?: string
  startTemperature?: number
  endTemperature?: number
}

export type MpsTfRuntimeTone = 'pending' | 'running' | 'done' | 'stop' | 'error'

export interface MpsRuntimeTf extends MpsTfRecord {
  nodeId: string
  stepNo?: number
  stepContent?: string
  stepScript?: string
  stepEngineConfig?: string
  equipments?: TfEquipmentRef[]
  toolings?: TfToolingRef[]
}

export interface MpsRecord {
  id: string
  moCode?: string
  sequence?: number
  execTime?: string
  line?: string
  batch?: number
  num?: number
  weight?: number | string
  status?: number
  statusDesc?: string
  moDto?: MoRecord
  lineVo?: LineBrief
  details?: MpsDetailRecord[]
  tfs?: MpsTfRecord[]
}

export interface MpsPageQuery extends PageQuery {
  moCode?: string
  productName?: string
  productCode?: string
  status?: number
  queryMO?: boolean
  queryLine?: boolean
  prioritySort?: boolean
}

export interface MpsChangePriorityPayload {
  pre?: string | null
  post?: string | null
  current: string
}

export interface MpsBoardQuery {
  moCode?: string
  productName?: string
  productCode?: string
  queryMO?: boolean
  queryLine?: boolean
  limitPerColumn?: number
}

export interface MpsBoardData {
  waitQue: MpsRecord[]
  waitExec: MpsRecord[]
  execing: MpsRecord[]
  complete: MpsRecord[]
}

