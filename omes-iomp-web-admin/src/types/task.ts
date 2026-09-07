export const TASK_STATUS_STOPPING = 0
export const TASK_STATUS_RUNNING = 1

export interface TaskRecord {
  id: string
  name?: string
  type?: string
  cron?: string
  status?: number
  statusDesc?: string
  createdTime?: string
}

export interface TaskFormPayload {
  id?: string
  name: string
  type: string
  cron: string
}
