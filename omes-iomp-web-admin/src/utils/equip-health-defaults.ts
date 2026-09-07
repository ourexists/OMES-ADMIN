import type { AlarmLevelItem, HealthRuleTemplateConfig } from '@/api/equip-health'

export function createDefaultHealthConfig(): HealthRuleTemplateConfig {
  return {
    periodHours: 24,
    alarmCountPenalty: 5,
    alarmDurationPenaltyPerMin: 0.5,
    alarmMaxDeduction: 40,
    alarmLevelPenalties: {
      '0': { count: 2, perMin: 0.2 },
      '1': { count: 5, perMin: 0.5 },
      '2': { count: 10, perMin: 1.0 },
      '3': { count: 15, perMin: 1.5 },
    },
    runRatioWeight: 30,
    expectedRunRatio: 70,
    onlineRatioWeight: 20,
    lifecycleMaxDeduction: 15,
    lifeYearThreshold: 5,
    lifeYearPenaltyPerYear: 1,
    lifeYearMaxDeduction: 5,
    totalRunHoursThreshold: 10000,
    totalRunHoursPenaltyPer1000: 0.5,
    totalRunHoursMaxDeduction: 5,
    startStopCountThreshold: 5000,
    startStopCountPenaltyPer1000: 0.2,
    startStopCountMaxDeduction: 5,
    inspectionWeight: 10,
    inspectionNoRecordPenalty: 5,
    healthyThreshold: 85,
    attentionThreshold: 70,
    warningThreshold: 50,
  }
}

export function defaultAlarmPenalty(level: AlarmLevelItem): AlarmLevelItem & { count: number; perMin: number } {
  const code = level.code
  const count = code === 0 ? 2 : code === 1 ? 5 : code === 2 ? 10 : 15
  const perMin = code === 0 ? 0.2 : code === 1 ? 0.5 : code === 2 ? 1.0 : 1.5
  return { ...level, count, perMin }
}

export function mergeHealthConfig(source?: HealthRuleTemplateConfig | null): HealthRuleTemplateConfig {
  const defaults = createDefaultHealthConfig()
  if (!source) {
    return defaults
  }
  return {
    ...defaults,
    ...source,
    alarmLevelPenalties: {
      ...defaults.alarmLevelPenalties,
      ...(source.alarmLevelPenalties || {}),
    },
  }
}
