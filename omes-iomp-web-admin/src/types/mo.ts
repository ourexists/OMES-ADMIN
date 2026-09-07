import type { PageQuery } from '@/types/api'

export const MO_STATUS = {
  INIT: 0,
  PART: 1,
  RUN: 2,
  COMPLETE: 3,
  CANCEL: 4,
} as const

export interface MoDetailRecord {
  id?: string
  matId?: string
  matName?: string
  matCode?: string
  matNum?: number | string
  matScale?: number | string
  priority?: number
  devNo?: string
  devName?: string
  dgCode?: string
  dgName?: string
}

export interface MoRecord {
  id: string
  tenantId?: string
  productId?: string
  productName?: string
  productCode?: string
  productType?: number
  num?: number
  surplus?: number
  weight?: number | string
  selfCode?: string
  lineCode?: string
  lineName?: string
  status?: number
  statusDesc?: string
  execTime?: string
  createdTime?: string
  source?: number
  sourceDesc?: string
  sourceId?: string
  devgId?: string
  detailDtoList?: MoDetailRecord[]
  tfDtoList?: MoTfRecord[]
}

export interface MoTfRecord {
  id?: string
  selfCode?: string
  name?: string
  status?: number
}

export interface MoPageQuery extends PageQuery {
  selfCode?: string
  productName?: string
  productCode?: string
  status?: number
}

export interface MoFormPayload {
  id?: string
  tenantId?: string
  productId?: string
  productName?: string
  productCode?: string
  productType?: number
  selfCode?: string
  num?: number | string
  weight?: number | string
  execTime?: string
  lineCode?: string
  devgId?: string
  detailDtoList?: MoDetailRecord[]
}

export interface MpsFlowPayload {
  moCode?: string
  sequence?: number
  execTime?: string
  execType?: number
  execNum?: number
  line?: string
  details?: MoDetailRecord[]
  tfs?: MoTfRecord[]
}

export interface MpsFlowPreviewRecord {
  moCode?: string
  num?: number
  weight?: number | string
  execTime?: string
  batch?: number
  moDto?: { productCode?: string; productName?: string }
  lineVo?: { name?: string }
}
