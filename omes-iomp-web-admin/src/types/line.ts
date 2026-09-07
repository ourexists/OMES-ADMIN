export interface LineRecord {

  id: string

  selfCode?: string

  name?: string

  productCode?: string

  productName?: string

  materialCode?: string

  materialName?: string

  versionNo?: string

  type?: number

  typeDesc?: string

  mapDb?: number

  mapOffset?: string

  syncTime?: string

  plcTime?: string

  throughput?: number

  stepInterval?: number

}



export interface TfEquipmentRef {

  equipmentId?: string

  equipmentCode?: string

  equipmentName?: string

}



export interface TfToolingRef {

  toolingId?: string

  toolingCode?: string

  toolingName?: string

}



export interface TfRecord {

  id: string

  lineId?: string

  selfCode?: string

  name?: string

  stepNo?: number

  stepContent?: string

  stepScript?: string

  stepEngineConfig?: string

  equipments?: TfEquipmentRef[]

  toolings?: TfToolingRef[]

  /** 本地草稿节点（未入库） */

  __localOnly?: boolean

  __dirtyLocal?: boolean

}



export interface TfEdgeRecord {

  id?: string

  fromTfId: string

  toTfId: string

}

