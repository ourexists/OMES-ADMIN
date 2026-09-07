import type { EquipStateSegment } from '@/api/equip-detail'

export type GanttRow = [type: number, start: number, end: number, state: number]

function mergeGanttTrack(track: GanttRow[]): GanttRow[] {
  if (!track.length) {
    return []
  }
  const sorted = [...track].sort((a, b) => {
    if (a[3] !== b[3]) {
      return a[3] - b[3]
    }
    return a[1] - b[1]
  })
  const merged: GanttRow[] = []
  for (const cur of sorted) {
    const last = merged.at(-1)
    if (!last || last[3] !== cur[3] || last[2] < cur[1]) {
      merged.push([...cur])
      continue
    }
    last[2] = Math.max(last[2], cur[2])
  }
  return merged
}

export function toTimestamp(value?: string): number {
  if (!value) {
    return NaN
  }
  const normalized = value.length >= 19 ? value.slice(0, 19).replace(' ', 'T') : value
  return new Date(normalized).getTime()
}

export function buildGanttRows(
  online: EquipStateSegment[],
  run: EquipStateSegment[],
  alarm: EquipStateSegment[],
): GanttRow[] {
  const byType: GanttRow[][] = [[], [], []]
  for (const item of online) {
    const start = toTimestamp(item.startTime)
    const end = toTimestamp(item.endTime)
    if (!Number.isNaN(start) && !Number.isNaN(end)) {
      byType[0].push([0, start, end, item.state ?? 0])
    }
  }
  for (const item of run) {
    const start = toTimestamp(item.startTime)
    const end = toTimestamp(item.endTime)
    if (!Number.isNaN(start) && !Number.isNaN(end)) {
      byType[1].push([1, start, end, item.state ?? 0])
    }
  }
  for (const item of alarm) {
    if (item.state !== 1) {
      continue
    }
    const start = toTimestamp(item.startTime)
    const end = toTimestamp(item.endTime)
    if (!Number.isNaN(start) && !Number.isNaN(end)) {
      byType[2].push([2, start, end, item.state ?? 0])
    }
  }
  return [...mergeGanttTrack(byType[0]), ...mergeGanttTrack(byType[1]), ...mergeGanttTrack(byType[2])]
}

export function capEndDateToNow(date: Date): Date {
  const now = new Date()
  return date.getTime() > now.getTime() ? now : date
}
