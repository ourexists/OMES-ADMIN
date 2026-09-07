import type { EquipRecord } from '@/api/device'

export type EquipVisualTone = 'offline' | 'alarm' | 'run' | 'stop' | 'online' | 'unknown'

/** 状态灯 / 药丸语义（对应 equip-status-dot--* / equip-status-pill--*） */
export type EquipStatusSignal =
  | 'online'
  | 'offline'
  | 'run'
  | 'stop'
  | 'alarm'
  | 'ok'
  | 'muted'
  | 'unknown'

export interface EquipStatusView {
  tone: EquipVisualTone
  labelKey:
    | 'realtimePage.offline'
    | 'realtimePage.alarm'
    | 'realtimePage.run'
    | 'realtimePage.stop'
    | 'realtimePage.online'
    | 'realtimePage.unknown'
}

/** ECharts 甘特图填色（读取 CSS 变量需在浏览器环境，此处导出与 CSS 一致的常量） */
export const EQUIP_STATUS_CHART_COLORS = {
  onlineOn: '#38bdf8',
  onlineOff: '#94a3b8',
  runOn: '#10b981',
  runOff: '#eab308',
  alarm: '#ef4444',
} as const

export function resolveGanttSegmentColor(type: number, state: number): string {
  const colors = EQUIP_STATUS_CHART_COLORS
  if (type === 0) {
    return state === 1 ? colors.onlineOn : colors.onlineOff
  }
  if (type === 1) {
    return state === 1 ? colors.runOn : colors.runOff
  }
  return colors.alarm
}

export function equipStatusDotClass(signal: EquipStatusSignal): string {
  return `equip-status-dot equip-status-dot--${signal}`
}

export function equipStatusPillClass(signal: EquipStatusSignal): string {
  return `equip-status-pill equip-status-pill--${signal}`
}

export function equipCardToneClass(tone: EquipVisualTone): string {
  return `equip-card-tone equip-card-tone--${tone}`
}

/** 实时卡片顶条色调 */
export function resolveEquipCardTone(
  item: Pick<EquipRecord, 'onlineState' | 'runState' | 'alarmState'>,
): EquipVisualTone {
  if (item.onlineState !== 1) {
    return 'offline'
  }
  if (item.alarmState === 1) {
    return 'alarm'
  }
  if (item.runState === 1) {
    return 'run'
  }
  if (item.runState === 0) {
    return 'stop'
  }
  return 'online'
}

export function resolveOnlineSignal(
  item: Pick<EquipRecord, 'onlineState'>,
): EquipStatusSignal {
  return item.onlineState === 1 ? 'online' : 'offline'
}

export function resolveRunSignal(
  item: Pick<EquipRecord, 'onlineState' | 'runState'>,
): EquipStatusSignal {
  if (item.onlineState !== 1) {
    return 'muted'
  }
  if (item.runState === 1) {
    return 'run'
  }
  if (item.runState === -1) {
    return 'unknown'
  }
  return 'stop'
}

export function resolveAlarmSignal(
  item: Pick<EquipRecord, 'onlineState' | 'alarmState'>,
): EquipStatusSignal {
  if (item.onlineState !== 1) {
    return 'muted'
  }
  if (item.alarmState === 1) {
    return 'alarm'
  }
  if (item.alarmState === -1) {
    return 'unknown'
  }
  return 'ok'
}

export function formatRunSignalTitle(
  t: (key: string) => string,
  item: Pick<EquipRecord, 'onlineState' | 'runState'>,
): string {
  if (item.onlineState !== 1) {
    return t('realtimePage.offline')
  }
  if (item.runState === 1) {
    return t('realtimePage.run')
  }
  if (item.runState === -1) {
    return t('realtimePage.unknown')
  }
  return t('realtimePage.stop')
}

export function formatAlarmSignalTitle(
  t: (key: string) => string,
  item: Pick<EquipRecord, 'onlineState' | 'alarmState'>,
): string {
  if (item.onlineState !== 1) {
    return t('realtimePage.offline')
  }
  if (item.alarmState === 1) {
    return t('realtimePage.alarm')
  }
  if (item.alarmState === -1) {
    return t('realtimePage.unknown')
  }
  return t('realtimePage.normal')
}

/** 与旧版 equip_detail 头部状态标签逻辑一致 */
export function resolveEquipStatusView(
  item: Pick<EquipRecord, 'onlineState' | 'runState' | 'alarmState'>,
): EquipStatusView {
  if (item.alarmState === 1) {
    return { tone: 'alarm', labelKey: 'realtimePage.alarm' }
  }
  if (item.onlineState !== 1) {
    return { tone: 'offline', labelKey: 'realtimePage.offline' }
  }
  if (
    item.runState === -1 ||
    item.runState == null ||
    item.alarmState === -1 ||
    item.alarmState == null
  ) {
    return { tone: 'unknown', labelKey: 'realtimePage.unknown' }
  }
  if (item.runState === 1) {
    return { tone: 'run', labelKey: 'realtimePage.run' }
  }
  return { tone: 'stop', labelKey: 'realtimePage.stop' }
}

export function formatEquipStateText(
  t: (key: string) => string,
  kind: 'online' | 'run' | 'alarm',
  value?: number | null,
): string {
  if (kind === 'online') {
    return value === 1 ? t('realtimePage.online') : t('realtimePage.offline')
  }
  if (value === -1 || value == null) {
    return t('realtimePage.unknown')
  }
  if (kind === 'run') {
    return value === 1 ? t('realtimePage.run') : t('realtimePage.stop')
  }
  return value === 1 ? t('realtimePage.alarm') : t('realtimePage.normal')
}
