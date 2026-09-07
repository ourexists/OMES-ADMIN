export interface DevgRecord {
  id: string
  selfCode?: string
  name?: string
}

export interface EquipProcessMaterial {
  matCode?: string
  matName?: string
  maxCapacity?: number | string | null
}

export interface DevgEquipRecord {
  id: string
  name?: string
  selfCode?: string
  type?: string | number
  typeDesc?: string
  workshopCode?: string
  workshop?: { name?: string }
  processMaterials?: EquipProcessMaterial[]
}
