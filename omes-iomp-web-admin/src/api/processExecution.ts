import { post } from '@/api/request'

export function hotReloadStepScript(data: { stepId: string; stepScript: string }) {
  return post('/processes/execution/script/hot-reload', data)
}
