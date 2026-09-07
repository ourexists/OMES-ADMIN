import { get, post } from '@/api/request'

export interface AlarmLevelPenalty {
  count?: number
  perMin?: number
}

export interface HealthRuleTemplateConfig {
  periodHours?: number
  alarmCountPenalty?: number
  alarmDurationPenaltyPerMin?: number
  alarmMaxDeduction?: number
  alarmLevelPenalties?: Record<string, AlarmLevelPenalty>
  runRatioWeight?: number
  expectedRunRatio?: number
  onlineRatioWeight?: number
  lifecycleMaxDeduction?: number
  lifeYearThreshold?: number
  lifeYearPenaltyPerYear?: number
  lifeYearMaxDeduction?: number
  totalRunHoursThreshold?: number
  totalRunHoursPenaltyPer1000?: number
  totalRunHoursMaxDeduction?: number
  startStopCountThreshold?: number
  startStopCountPenaltyPer1000?: number
  startStopCountMaxDeduction?: number
  inspectionWeight?: number
  inspectionNoRecordPenalty?: number
  healthyThreshold?: number
  attentionThreshold?: number
  warningThreshold?: number
}

export interface HealthRuleTemplate {
  id?: string | null
  name?: string
  config?: HealthRuleTemplateConfig
  periodHours?: number
  healthyThreshold?: number
  attentionThreshold?: number
  warningThreshold?: number
}

export interface AlarmLevelItem {
  code: number
  desc?: string
}

export function fetchHealthRuleTemplates() {
  return get<HealthRuleTemplate[]>('/equipHealth/listTemplates')
}

export function fetchDefaultHealthTemplate() {
  return get<HealthRuleTemplate>('/equipHealth/getDefaultTemplate')
}

export function saveHealthRuleTemplate(data: HealthRuleTemplate) {
  return post<boolean>('/equipHealth/saveTemplate', data)
}

export function fetchHealthAlarmLevels() {
  return get<AlarmLevelItem[]>('/equipHealth/getAlarmLevels')
}

export function computeHealthByTemplate(templateId: string) {
  return get<{ count?: number; message?: string }>('/equipHealth/computeByTemplate', { templateId })
}

export interface HealthIndicatorRecord {
  id?: string
  equipId?: string
  sn?: string
  statTime?: string
  periodStart?: string
  periodEnd?: string
  score?: number
  healthLevel?: number
  healthLevelDesc?: string
  alarmCount?: number
  alarmDurationMinutes?: number
  runDurationMinutes?: number
  onlineDurationMinutes?: number
  templateId?: string
}

export function fetchHealthIndicatorsByStatTime(statTime: string) {
  return post<HealthIndicatorRecord[]>('/equipHealth/listByStatTime', { statTime })
}
