export interface SyncRecord {
  id: string
  syncTx?: string
  partStartTimestamp?: string
  partEndTimestamp?: string
  status?: string
  partMin?: string
  partMax?: string
  preMin?: string
  preMax?: string
  createdTime?: string
  resources?: SyncResourceRecord[]
}

export interface SyncResourceRecord {
  id: string
  syncId?: string
  point?: string
  status?: string
  reqData?: string
  respData?: string
  excep?: string
}

export interface MapOption {
  id: string
  name: string
  selfCode?: string
}
