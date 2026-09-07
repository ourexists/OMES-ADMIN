<script setup lang="ts">
import './equip-screen-theme.css'
import { computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import {
  CheckOutlined,
  FullscreenExitOutlined,
  FullscreenOutlined,
} from '@ant-design/icons-vue'
import type { WorkshopNode, EquipRecord } from '@/api/device'
import { fetchEquipGisList } from '@/api/device'
import {
  fetchEquipStateSnapshotTrend,
  fetchScreenHealthSummary,
  type HealthSummary,
} from '@/api/equip-screen'
import {
  fetchAlarmMessagePage,
  fetchEquipRealtimeCount,
  markAlarmMessageRead,
  type AlarmMessageRecord,
  type EquipCountResult,
} from '@/api/overview'
import WorkshopSceneSidePanel from '@/components/WorkshopSceneSidePanel.vue'
import BaiduEquipScreenMap from '@/components/map/BaiduEquipScreenMap.vue'
import { hasBaiduMapAk } from '@/config/baidu-map'
import { mergeEquipRealtimeList } from '@/utils/equip-realtime'
import { TECH_SCREEN_MAP_COLORS } from '@/utils/equip-screen-map'
import { message } from 'ant-design-vue'
import { safeExitFullscreen } from '@/utils/fullscreen'

const SCREEN_REFRESH_MS = 30_000
const TECH_COLORS = TECH_SCREEN_MAP_COLORS

const STATUS_CHART = {
  chartText: '#94b4c4',
  center: '33%',
  rings: {
    alarm: { inner: '30%', outer: '32%' },
    run: { inner: '35%', outer: '43%' },
    online: { inner: '50%', outer: '67%' },
    onlineGlow: { inner: '67%', outer: '69%' },
  },
  trackColor: 'rgba(46, 62, 72, 0.52)',
  padAngle: {
    alarm: 2,
    run: 2.5,
    online: 2,
    onlineGlow: 1,
  },
  colors: {
    online: 'rgba(91, 243, 249, 0.38)',
    onlineGlow: TECH_COLORS.online,
    offline: '#324048',
    running: TECH_COLORS.running,
    stopped: '#b8923a',
    alarm: TECH_COLORS.alarm,
    normal: 'transparent',
  },
  glow: {
    onlineGlow: { blur: 24, color: 'rgba(91, 243, 249, 0.9)' },
    alarm: { blur: 12, color: 'rgba(255, 91, 96, 0.58)' },
    running: { blur: 8, color: 'rgba(107, 230, 167, 0.42)' },
  },
} as const

type StatusChartKey = keyof typeof STATUS_CHART.colors

function statusSegmentStyle(key: StatusChartKey) {
  const color = STATUS_CHART.colors[key]
  const glow = STATUS_CHART.glow[key as keyof typeof STATUS_CHART.glow]
  return {
    color,
    borderWidth: 0,
    borderRadius: 0,
    ...(glow ? { shadowBlur: glow.blur, shadowColor: glow.color } : {}),
  }
}

function statusSegmentEmphasis(key: StatusChartKey) {
  const color = STATUS_CHART.colors[key]
  const glow = STATUS_CHART.glow[key as keyof typeof STATUS_CHART.glow]
  return {
    color,
    borderWidth: 0,
    borderRadius: 0,
    ...(glow ? { shadowBlur: glow.blur + 6, shadowColor: glow.color } : {}),
  }
}

function statusRingTrack(radius: [string, string], center: [string, string], z: number): echarts.PieSeriesOption {
  return {
    name: 'track',
    type: 'pie',
    radius,
    center,
    silent: true,
    z,
    label: { show: false },
    labelLine: { show: false },
    data: [{
      value: 1,
      itemStyle: {
        color: STATUS_CHART.trackColor,
        borderWidth: 0,
        borderRadius: 0,
      },
    }],
  }
}

function statusRingSeries(
  name: string,
  radius: [string, string],
  center: [string, string],
  z: number,
  padAngle: number,
  items: Array<{ value: number; name: string; key: StatusChartKey }>,
): echarts.PieSeriesOption {
  return {
    name,
    type: 'pie',
    radius,
    center,
    z,
    padAngle,
    minAngle: 2,
    label: { show: false },
    labelLine: { show: false },
    emphasis: { scale: false, focus: 'self' },
    data: items.map(({ value, name: label, key }) => ({
      value,
      name: label,
      itemStyle: statusSegmentStyle(key),
      emphasis: { itemStyle: statusSegmentEmphasis(key) },
    })),
  }
}

const CHART_NO_ANIM: Pick<import('echarts').EChartsOption, 'animation' | 'animationDuration'> = {
  animation: false,
  animationDuration: 0,
}

const { t } = useI18n()

const rootRef = ref<HTMLElement | null>(null)
const mapStageRef = ref<HTMLElement | null>(null)
const statusChartRef = ref<HTMLElement | null>(null)
const trendChartRef = ref<HTMLElement | null>(null)
const currentTrendChartRef = ref<HTMLElement | null>(null)
const gisMapRef = ref<InstanceType<typeof BaiduEquipScreenMap> | null>(null)

let statusChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null
let currentTrendChart: echarts.ECharts | null = null
let pollTimer: ReturnType<typeof setInterval> | null = null
let clockTimer: ReturnType<typeof setInterval> | null = null
let resizeObserver: ResizeObserver | null = null
let layoutResizeTimer: ReturnType<typeof setTimeout> | null = null
let loadRequestId = 0
let lastCountsSnapshot = ''
let lastAlarmSnapshot = ''

const selectedWorkshop = ref<WorkshopNode | null>(null)
const currentTime = ref('')
const isPageFullscreen = ref(false)
const mapHeight = ref(400)
const fitViewKey = ref(0)

const counts = shallowRef<EquipCountResult>({})
const mapDevices = shallowRef<EquipRecord[]>([])
const alarmList = ref<AlarmMessageRecord[]>([])
const healthSummary = shallowRef<HealthSummary>({
  healthy: 0,
  attention: 0,
  warning: 0,
  fault: 0,
  avgScore: null,
})

const countLock = ref(false)
const chartLock = ref(false)
const currentTrendLock = ref(false)
const healthLock = ref(false)
const alarmLock = ref(false)
const mapLoading = ref(false)
const silentMapLoading = ref(false)

const mapAkReady = computed(() => hasBaiduMapAk())
const workshopLabel = computed(() => selectedWorkshop.value?.name?.trim() || '')
const hasEquipData = computed(() => (counts.value.total ?? 0) > 0)

const kpiItems = computed(() => [
  { key: 'total', field: 'total' as const, label: t('overviewPage.kpiTotal'), warn: false },
  { key: 'online', field: 'online' as const, label: t('overviewPage.kpiOnline'), warn: false },
  { key: 'offline', field: 'offline' as const, label: t('overviewPage.kpiOffline'), warn: (counts.value.offline ?? 0) > 0 },
  { key: 'alarm', field: 'alarm' as const, label: t('overviewPage.kpiAlarm'), warn: (counts.value.alarm ?? 0) > 0 },
  { key: 'run', field: 'run' as const, label: t('overviewPage.kpiRun'), warn: false },
  { key: 'stop', field: 'stopped' as const, label: t('overviewPage.kpiStop'), warn: false },
])

const stateProgressRows = computed(() => {
  const total = Number(counts.value.total || 0)
  const pct = (v: number) => (total <= 0 ? 0 : Math.round((v * 100) / total))
  return [
    { key: 'online', label: t('equipScreenPage.legendOnline'), value: counts.value.online ?? 0, color: TECH_COLORS.online, alarm: false, pct: pct(counts.value.online ?? 0) },
    { key: 'run', label: t('equipScreenPage.legendRunning'), value: counts.value.run ?? 0, color: TECH_COLORS.running, alarm: false, pct: pct(counts.value.run ?? 0) },
    { key: 'stop', label: t('equipScreenPage.legendStopped'), value: counts.value.stopped ?? 0, color: TECH_COLORS.stopped, alarm: false, pct: pct(counts.value.stopped ?? 0) },
    { key: 'alarm', label: t('equipScreenPage.legendAlarm'), value: counts.value.alarm ?? 0, color: TECH_COLORS.alarm, alarm: true, pct: pct(counts.value.alarm ?? 0) },
  ]
})

const statusRateChips = computed(() => {
  const total = Number(counts.value.total || 0)
  const pct = (v: number) => (total <= 0 ? 0 : Math.round((v * 100) / total))
  const alarm = counts.value.alarm ?? 0
  return [
    { key: 'online', label: t('equipScreenPage.rateOnline'), value: pct(counts.value.online ?? 0), tone: 'online' },
    { key: 'run', label: t('equipScreenPage.rateRun'), value: pct(counts.value.run ?? 0), tone: 'run' },
    { key: 'alarm', label: t('equipScreenPage.rateAlarm'), value: pct(alarm), tone: alarm > 0 ? 'alarm' : 'muted' },
  ]
})

const footerMarquee = computed(() => {
  const unread = Number(counts.value.alarm ?? 0)
  if (unread <= 0) {
    return { text: '', tone: '' }
  }
  if (unread >= 15) {
    return { text: t('equipScreenPage.marqueeDanger', { count: unread }), tone: 'danger' }
  }
  if (unread >= 5) {
    return { text: t('equipScreenPage.marqueeWarn', { count: unread }), tone: 'warn' }
  }
  return { text: t('equipScreenPage.marqueeNotice', { count: unread }), tone: 'notice' }
})

const mapLegendItems = computed(() => [
  { key: 'online', color: TECH_COLORS.online, label: t('equipScreenPage.legendOnline') },
  { key: 'offline', color: TECH_COLORS.offline, label: t('equipScreenPage.legendOffline') },
  { key: 'running', color: TECH_COLORS.running, label: t('equipScreenPage.legendRunning') },
  { key: 'stopped', color: TECH_COLORS.stopped, label: t('equipScreenPage.legendStopped') },
  { key: 'alarm', color: TECH_COLORS.alarm, label: t('equipScreenPage.legendAlarm') },
])

function resolveAlarmLevel(item: AlarmMessageRecord): 'notice' | 'warn' | 'danger' {
  const text = `${item.title ?? ''} ${item.context ?? ''}`.toLowerCase()
  if (/严重|紧急|critical|fatal|故障|宕机|停机|火警|中断/.test(text)) {
    return 'danger'
  }
  if (/告警|报警|警告|warn|异常|离线|超限|失败|error/.test(text)) {
    return 'warn'
  }
  return 'notice'
}

function alarmLevelLabel(level: 'notice' | 'warn' | 'danger') {
  if (level === 'danger') {
    return t('equipScreenPage.alarmLevelDanger')
  }
  if (level === 'warn') {
    return t('equipScreenPage.alarmLevelWarn')
  }
  return t('equipScreenPage.alarmLevelNotice')
}

function isAlarmUnread(item: AlarmMessageRecord): boolean {
  return item.readStatus == null || item.readStatus === 0
}

function formatAlarmTime(value?: string): string {
  if (!value) {
    return ''
  }
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('MM-DD HH:mm') : value
}

function alarmDisplayContent(item: AlarmMessageRecord): string {
  const title = (item.title ?? '').trim()
  const context = (item.context ?? '').trim()
  if (!context || context === title) {
    return ''
  }
  return context
}

function tickClock() {
  currentTime.value = new Date().toLocaleString()
}

function workshopCode() {
  return selectedWorkshop.value?.selfCode ?? null
}

function countsSnapshot(count: EquipCountResult): string {
  return [
    count.total ?? 0,
    count.online ?? 0,
    count.offline ?? 0,
    count.alarm ?? 0,
    count.run ?? 0,
    count.stopped ?? 0,
  ].join('|')
}

function alarmSnapshot(list: AlarmMessageRecord[]): string {
  return list.map((item) => `${item.id ?? ''}:${item.readStatus ?? 0}:${item.title ?? ''}`).join('|')
}

function buildStatusChartOption(count: EquipCountResult): echarts.EChartsOption {
  const online = count.online ?? 0
  const offline = count.offline ?? 0
  const run = count.run ?? 0
  const stopped = count.stopped ?? 0
  const alarm = count.alarm ?? 0
  const total = count.total ?? 0
  const notAlarm = Math.max(0, total - alarm)
  const onlineRate = total <= 0 ? 0 : Math.round((online * 100) / total)
  const pieCenter: [string, string] = ['50%', '50%']
  const { online: ringOnline, onlineGlow: ringOnlineGlow, run: ringRun, alarm: ringAlarm } = STATUS_CHART.rings
  const legendOnline = t('equipScreenPage.legendOnline')
  const legendOffline = t('equipScreenPage.legendOffline')
  const legendRunning = t('equipScreenPage.legendRunning')
  const legendStopped = t('equipScreenPage.legendStopped')
  const legendAlarm = t('equipScreenPage.legendAlarm')
  const legendNotAlarm = t('equipScreenPage.legendNotAlarm')

  return {
    ...CHART_NO_ANIM,
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(11, 23, 30, 0.96)',
      borderColor: 'rgba(91, 243, 249, 0.25)',
      borderWidth: 1,
      padding: [8, 12],
      textStyle: { color: '#dce8f2', fontSize: 12 },
      formatter: (params: unknown) => {
        const item = params as { name?: string; value?: number; percent?: number; seriesName?: string }
        if (item.seriesName === 'center' || item.seriesName === 'track' || item.seriesName === 'online-glow') {
          return ''
        }
        const value = Number(item.value ?? 0)
        const percent = Math.round(Number(item.percent ?? 0))
        return `${item.name ?? ''}<br/>${value} 台 · ${percent}%`
      },
    },
    legend: { show: false },
    series: [
      statusRingTrack([ringOnline.inner, ringOnlineGlow.outer], pieCenter, 0),
      statusRingTrack([ringRun.inner, ringRun.outer], pieCenter, 0),
      {
        name: 'center',
        type: 'pie',
        radius: ['0%', STATUS_CHART.center],
        center: pieCenter,
        silent: true,
        z: 1,
        label: {
          show: true,
          position: 'center',
          formatter: `{rate|${onlineRate}%}\n{label|${t('equipScreenPage.centerOnline')}}`,
          rich: {
            rate: {
              fontSize: 28,
              fontWeight: 700,
              color: '#5bf3f9',
              lineHeight: 34,
              textShadowColor: 'rgba(91, 243, 249, 0.35)',
              textShadowBlur: 8,
            },
            label: {
              fontSize: 11,
              color: STATUS_CHART.chartText,
              lineHeight: 18,
              letterSpacing: 1,
            },
          },
        },
        labelLine: { show: false },
        data: [{
          value: 1,
          name: 'center',
          itemStyle: {
            color: 'transparent',
            borderWidth: 0,
            shadowBlur: 0,
          },
        }],
      },
      statusRingSeries(
        t('equipScreenPage.chartAlarm'),
        [ringAlarm.inner, ringAlarm.outer],
        pieCenter,
        2,
        STATUS_CHART.padAngle.alarm,
        [
          { value: alarm, name: legendAlarm, key: 'alarm' },
          { value: notAlarm, name: legendNotAlarm, key: 'normal' },
        ],
      ),
      statusRingSeries(
        t('equipScreenPage.chartRun'),
        [ringRun.inner, ringRun.outer],
        pieCenter,
        3,
        STATUS_CHART.padAngle.run,
        [
          { value: run, name: legendRunning, key: 'running' },
          { value: stopped, name: legendStopped, key: 'stopped' },
        ],
      ),
      statusRingSeries(
        t('equipScreenPage.chartStatus'),
        [ringOnline.inner, ringOnline.outer],
        pieCenter,
        4,
        STATUS_CHART.padAngle.online,
        [
          { value: online, name: legendOnline, key: 'online' },
          { value: offline, name: legendOffline, key: 'offline' },
        ],
      ),
      {
        ...statusRingSeries(
          'online-glow',
          [ringOnlineGlow.inner, ringOnlineGlow.outer],
          pieCenter,
          5,
          STATUS_CHART.padAngle.onlineGlow,
          [
            { value: online, name: legendOnline, key: 'onlineGlow' },
            { value: offline, name: legendOffline, key: 'normal' },
          ],
        ),
        silent: true,
      },
    ],
  }
}

const TREND_GRID_LINE = 'rgba(91, 243, 249, 0.08)'

function buildTrendBaseOption(): echarts.EChartsOption {
  return {
    ...CHART_NO_ANIM,
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', lineStyle: { type: 'dashed', color: 'rgba(91,243,249,0.12)' } },
    },
    grid: { left: 8, right: 8, top: 28, bottom: 22, containLabel: true },
    xAxis: {
      type: 'time',
      axisLine: { lineStyle: { color: '#20333f' } },
      axisLabel: { color: '#94b4c4', fontSize: 10 },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value',
      min: 0,
      minInterval: 1,
      splitNumber: 4,
      axisLine: { lineStyle: { color: '#20333f' } },
      axisLabel: { color: '#94b4c4', fontSize: 10 },
      splitLine: { lineStyle: { color: TREND_GRID_LINE } },
    },
    series: [
      {
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 2.4, color: TECH_COLORS.online },
        itemStyle: { color: TECH_COLORS.online },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(91, 243, 249, 0.32)' },
            { offset: 1, color: 'rgba(91, 243, 249, 0.02)' },
          ]),
          opacity: 0.35,
        },
        data: [] as [string | Date, number][],
      },
    ],
  }
}

function buildCurrentTrendOption(): echarts.EChartsOption {
  const axisStyle = { color: '#20333f' }
  const textStyle = { color: '#94b4c4', fontSize: 10 }
  return {
    ...CHART_NO_ANIM,
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', lineStyle: { type: 'dashed', color: 'rgba(91,243,249,0.12)' } },
    },
    legend: { top: 8, left: 'center', textStyle },
    grid: { left: 8, right: 8, top: 28, bottom: 22, containLabel: true },
    xAxis: {
      type: 'time',
      axisLine: { lineStyle: axisStyle },
      axisLabel: {
        ...textStyle,
        rotate: 0,
        hideOverlap: true,
        formatter: (value: number) => dayjs(value).format('HH:mm'),
      },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value',
      min: 0,
      minInterval: 1,
      splitNumber: 4,
      axisLine: { lineStyle: axisStyle },
      axisLabel: textStyle,
      splitLine: { lineStyle: { color: TREND_GRID_LINE } },
    },
    series: [
      {
        name: t('equipScreenPage.legendOnline'),
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 2.2, color: TECH_COLORS.online },
        itemStyle: { color: TECH_COLORS.online },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(91, 243, 249, 0.3)' },
            { offset: 1, color: 'rgba(91, 243, 249, 0.02)' },
          ]),
          opacity: 0.36,
        },
        data: [],
      },
      {
        name: t('equipScreenPage.legendRunning'),
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 2.2, color: TECH_COLORS.running },
        itemStyle: { color: TECH_COLORS.running },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(107, 230, 167, 0.28)' },
            { offset: 1, color: 'rgba(107, 230, 167, 0.02)' },
          ]),
          opacity: 0.34,
        },
        data: [],
      },
      {
        name: t('equipScreenPage.legendAlarm'),
        type: 'line',
        showSymbol: false,
        lineStyle: { width: 2.2, color: TECH_COLORS.alarm },
        itemStyle: { color: TECH_COLORS.alarm },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 91, 96, 0.26)' },
            { offset: 1, color: 'rgba(255, 91, 96, 0.02)' },
          ]),
          opacity: 0.28,
        },
        data: [],
      },
    ],
  }
}

function initCharts() {
  const initOpts = { renderer: 'canvas' as const }
  if (statusChartRef.value && !statusChart) {
    statusChart = echarts.init(statusChartRef.value, undefined, initOpts)
    statusChart.setOption(buildStatusChartOption({}))
  }
  if (trendChartRef.value && !trendChart) {
    trendChart = echarts.init(trendChartRef.value, undefined, initOpts)
    trendChart.setOption(buildTrendBaseOption())
  }
  if (currentTrendChartRef.value && !currentTrendChart) {
    currentTrendChart = echarts.init(currentTrendChartRef.value, undefined, initOpts)
    currentTrendChart.setOption(buildCurrentTrendOption())
  }
}

function scheduleChartResize() {
  statusChart?.resize()
  trendChart?.resize()
  currentTrendChart?.resize()
}

function scheduleLayoutResize() {
  if (layoutResizeTimer != null) {
    clearTimeout(layoutResizeTimer)
  }
  layoutResizeTimer = window.setTimeout(() => {
    layoutResizeTimer = null
    updateMapHeight()
    scheduleChartResize()
    gisMapRef.value?.refreshMapSize()
  }, 200)
}

function resizeCharts() {
  scheduleChartResize()
  scheduleLayoutResize()
}

function updateMapHeight() {
  const stage = mapStageRef.value
  if (!stage) {
    return
  }
  const next = Math.max(280, Math.floor(stage.getBoundingClientRect().height))
  if (Math.abs(next - mapHeight.value) >= 2) {
    mapHeight.value = next
  }
}

async function loadCounts(silent = false) {
  if (countLock.value) {
    return
  }
  countLock.value = true
  try {
    const result = await fetchEquipRealtimeCount(workshopCode())
    const next = result || {}
    const snap = countsSnapshot(next)
    if (snap !== lastCountsSnapshot) {
      lastCountsSnapshot = snap
      counts.value = next
      statusChart?.setOption(buildStatusChartOption(next), { notMerge: false, lazyUpdate: true })
    }
  } catch {
    if (!silent) {
      message.error(t('equipScreenPage.loadCountFailed'))
    }
  } finally {
    countLock.value = false
  }
}

async function loadTrend7d(silent = false) {
  if (chartLock.value || !trendChart) {
    return
  }
  chartLock.value = true
  try {
    const now = dayjs()
    const start = now.subtract(6, 'day').startOf('day')
    const end = now.endOf('day')
    const result = await fetchEquipStateSnapshotTrend({
      startDate: start.format('YYYY-MM-DD HH:mm:ss'),
      endDate: end.format('YYYY-MM-DD HH:mm:ss'),
      onlineState: 1,
      countType: 1,
      workshopCode: workshopCode(),
    })
    const data = (result || []).map((item) => [item.time, item.num ?? 0] as [string | Date, number])
    trendChart.setOption({
      xAxis: { min: start.toDate(), max: end.toDate() },
      series: [{ data }],
    }, { notMerge: false, lazyUpdate: true })
  } catch {
    if (!silent) {
      message.error(t('equipScreenPage.loadTrendFailed'))
    }
  } finally {
    chartLock.value = false
  }
}

async function loadCurrentTrend(silent = false) {
  if (currentTrendLock.value || !currentTrendChart) {
    return
  }
  currentTrendLock.value = true
  const start = dayjs().startOf('day')
  const end = dayjs()
  const snap = {
    startDate: start.format('YYYY-MM-DD HH:mm:ss'),
    endDate: end.format('YYYY-MM-DD HH:mm:ss'),
    workshopCode: workshopCode(),
  }
  try {
    const [onlineData, runData, alarmData] = await Promise.all([
      fetchEquipStateSnapshotTrend({ ...snap, onlineState: 1 }),
      fetchEquipStateSnapshotTrend({ ...snap, runState: 1 }),
      fetchEquipStateSnapshotTrend({ ...snap, alarmState: 1 }),
    ])
    currentTrendChart.setOption({
      xAxis: { min: start.toDate(), max: end.toDate() },
      series: [
        { data: (onlineData || []).map((item) => [item.time, item.num ?? 0]) },
        { data: (runData || []).map((item) => [item.time, item.num ?? 0]) },
        { data: (alarmData || []).map((item) => [item.time, item.num ?? 0]) },
      ],
    }, { notMerge: false, lazyUpdate: true })
  } catch {
    if (!silent) {
      message.error(t('equipScreenPage.loadTrendFailed'))
    }
  } finally {
    currentTrendLock.value = false
  }
}

async function loadHealth(silent = false) {
  if (healthLock.value) {
    return
  }
  healthLock.value = true
  try {
    healthSummary.value = await fetchScreenHealthSummary(workshopCode())
  } catch {
    if (!silent) {
      message.error(t('equipScreenPage.loadHealthFailed'))
    }
  } finally {
    healthLock.value = false
  }
}

async function loadAlarms(silent = false) {
  if (alarmLock.value) {
    return
  }
  alarmLock.value = true
  try {
    const result = await fetchAlarmMessagePage(1, 10)
    const records = result?.records ?? []
    const snap = alarmSnapshot(records)
    if (snap !== lastAlarmSnapshot) {
      lastAlarmSnapshot = snap
      alarmList.value = records
    }
  } catch {
    if (!silent) {
      message.error(t('equipScreenPage.loadAlarmFailed'))
    }
  } finally {
    alarmLock.value = false
  }
}

async function loadMap(options: { silent?: boolean; fitView?: boolean } = {}) {
  const silent = options.silent === true
  if (silent && silentMapLoading.value) {
    return
  }
  const requestId = ++loadRequestId
  if (!silent) {
    mapLoading.value = true
  } else {
    silentMapLoading.value = true
  }
  try {
    const list = await fetchEquipGisList({ workshopCode: workshopCode() })
    if (requestId !== loadRequestId) {
      return
    }
    if (silent) {
      const merged = mergeEquipRealtimeList(mapDevices.value, list)
      if (merged !== null) {
        mapDevices.value = merged
      }
    } else {
      mapDevices.value = list
    }
    if (options.fitView) {
      fitViewKey.value += 1
    }
  } catch {
    if (!silent) {
      message.error(t('equipScreenPage.loadMapFailed'))
    }
  } finally {
    if (requestId === loadRequestId) {
      if (!silent) {
        mapLoading.value = false
      } else {
        silentMapLoading.value = false
      }
      if (options.fitView) {
        void nextTick(() => gisMapRef.value?.refreshMapSize())
      }
    }
  }
}

async function refreshAll(options: { silent?: boolean; fitMap?: boolean } = {}) {
  const silent = options.silent === true
  const tasks: Promise<void>[] = [
    loadCounts(silent),
    loadCurrentTrend(silent),
    loadHealth(silent),
    loadAlarms(silent),
    loadMap({ silent, fitView: options.fitMap === true }),
  ]
  if (!silent) {
    tasks.push(loadTrend7d(silent))
  }
  await Promise.all(tasks)
}

function onWorkshopChange() {
  lastCountsSnapshot = ''
  lastAlarmSnapshot = ''
  void refreshAll({ fitMap: true })
}

async function onMarkAlarmRead(record: AlarmMessageRecord) {
  if (!record.id) {
    return
  }
  try {
    await markAlarmMessageRead(record.id)
    record.readStatus = 1
  } catch {
    message.error(t('equipScreenPage.markReadFailed'))
  }
}

async function togglePageFullscreen() {
  const el = rootRef.value
  if (!el) {
    return
  }
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen()
    } else {
      await el.requestFullscreen()
    }
  } catch {
    message.warning(t('equipScreenPage.fullscreenFailed'))
  }
}

function onFullscreenChange() {
  isPageFullscreen.value = document.fullscreenElement === rootRef.value
  window.setTimeout(resizeCharts, 100)
}

function startPolling() {
  if (pollTimer != null) {
    return
  }
  pollTimer = window.setInterval(() => {
    if (document.hidden) {
      return
    }
    void refreshAll({ silent: true })
  }, SCREEN_REFRESH_MS)
}

function stopPolling() {
  if (pollTimer != null) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

watch(
  () => selectedWorkshop.value?.selfCode,
  () => {
    onWorkshopChange()
  },
)

onMounted(() => {
  tickClock()
  clockTimer = window.setInterval(tickClock, 1000)
  document.addEventListener('fullscreenchange', onFullscreenChange)

  void nextTick(() => {
    initCharts()
    updateMapHeight()
    void refreshAll({ fitMap: true })

    resizeObserver = new ResizeObserver(() => {
      scheduleLayoutResize()
    })
    if (rootRef.value) {
      resizeObserver.observe(rootRef.value)
    }
    if (mapStageRef.value) {
      resizeObserver.observe(mapStageRef.value)
    }
    startPolling()
  })
})

onUnmounted(() => {
  stopPolling()
  if (clockTimer != null) {
    clearInterval(clockTimer)
  }
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  safeExitFullscreen(rootRef.value)
  resizeObserver?.disconnect()
  if (layoutResizeTimer != null) {
    clearTimeout(layoutResizeTimer)
    layoutResizeTimer = null
  }
  statusChart?.dispose()
  trendChart?.dispose()
  currentTrendChart?.dispose()
  statusChart = null
  trendChart = null
  currentTrendChart = null
})
</script>

<template>
  <div ref="rootRef" class="equip-screen-root">
    <div class="equip-screen-root__bg" aria-hidden="true" />
    <div class="equip-screen-root__ambient" aria-hidden="true" />
    <div class="equip-screen-root__scan" aria-hidden="true" />

    <div class="equip-screen-container">
      <header class="equip-screen-header">
        <div class="equip-screen-header__bg" aria-hidden="true">
          <div class="equip-screen-header__corners">
            <span /><span /><span /><span />
          </div>
          <div class="equip-screen-header__glow" />
          <div class="equip-screen-header__rail equip-screen-header__rail--l" />
          <div class="equip-screen-header__rail equip-screen-header__rail--r" />
          <div class="equip-screen-header__scan equip-screen-header__scan--wide" />
          <div class="equip-screen-header__scan equip-screen-header__scan--narrow" />
        </div>
        <div class="equip-screen-header__left">
          <div class="equip-screen-header__brand" aria-hidden="true">
            <span class="equip-screen-header__brand-icon" />
            <span class="equip-screen-header__brand-text">OMES</span>
          </div>
          <div class="equip-screen-header__clock">
            <span class="equip-screen-header__live">{{ t('equipScreenPage.headerLive') }}</span>
            <span class="equip-screen-header__clock-dot" />
            <span class="equip-screen-header__time">{{ currentTime }}</span>
          </div>
        </div>
        <div class="equip-screen-title-wrap">
          <div class="equip-screen-title__chevrons equip-screen-title__chevrons--l" aria-hidden="true">
            <span /><span /><span />
          </div>
          <span class="equip-screen-title__wing equip-screen-title__wing--l" aria-hidden="true" />
          <div class="equip-screen-title__core">
            <span class="equip-screen-title__shine" aria-hidden="true" />
            <span class="equip-screen-title__subtitle">{{ t('equipScreenPage.headerSubtitle') }}</span>
            <h1 class="equip-screen-title">{{ t('equipScreenPage.title') }}</h1>
            <span class="equip-screen-title__bar" aria-hidden="true" />
          </div>
          <span class="equip-screen-title__wing equip-screen-title__wing--r" aria-hidden="true" />
          <div class="equip-screen-title__chevrons equip-screen-title__chevrons--r" aria-hidden="true">
            <span /><span /><span />
          </div>
        </div>
        <div class="equip-screen-header__right">
          <span class="equip-screen-header__sys-id">{{ t('equipScreenPage.headerSysStatus') }}</span>
          <span v-if="workshopLabel" class="equip-screen-workshop-name">{{ workshopLabel }}</span>
          <button type="button" class="equip-screen-btn-fs" @click="togglePageFullscreen">
            <FullscreenExitOutlined v-if="isPageFullscreen" />
            <FullscreenOutlined v-else />
            <span>{{
              isPageFullscreen ? t('equipScreenPage.exitFullscreen') : t('equipScreenPage.fullscreen')
            }}</span>
          </button>
        </div>
      </header>
      <div class="equip-screen-header-bridge" aria-hidden="true" />

      <main class="equip-screen-body">
        <aside class="equip-screen-side">
          <div class="equip-screen-panel">
            <div class="equip-screen-panel__title">{{ t('equipScreenPage.sectionOverview') }}</div>
            <div class="equip-screen-kpi">
              <div
                v-for="item in kpiItems"
                :key="item.key"
                class="equip-screen-kpi__item"
                :class="{ 'equip-screen-kpi__item--warn': item.warn }"
              >
                <div class="equip-screen-kpi__num">{{ counts[item.field] ?? 0 }}</div>
                <div class="equip-screen-kpi__label">{{ item.label }}</div>
              </div>
            </div>
          </div>

          <div class="equip-screen-panel equip-screen-panel--flex equip-screen-panel--status">
            <div class="equip-screen-panel__title">{{ t('equipScreenPage.statusChartTitle') }}</div>
            <div class="equip-screen-status-body">
              <div class="equip-screen-rate-row">
                <div
                  v-for="chip in statusRateChips"
                  :key="chip.key"
                  class="equip-screen-rate-chip"
                  :class="`equip-screen-rate-chip--${chip.tone}`"
                >
                  <span class="equip-screen-rate-chip__value">{{ chip.value }}%</span>
                  <span class="equip-screen-rate-chip__label">{{ chip.label }}</span>
                </div>
              </div>
              <div class="equip-screen-chart-wrap equip-screen-chart-wrap--status">
                <div ref="statusChartRef" class="equip-screen-chart" :class="{ 'is-hidden': !hasEquipData }" />
                <div v-if="!hasEquipData" class="equip-screen-chart-empty">
                  {{ t('equipScreenPage.statusEmpty') }}
                </div>
              </div>
              <div v-if="hasEquipData" class="equip-screen-progress-list equip-screen-progress-list--compact">
                <div class="equip-screen-progress-list__title">{{ t('equipScreenPage.statusRateTitle') }}</div>
                <div
                  v-for="row in stateProgressRows"
                  :key="row.key"
                  class="equip-screen-progress-item"
                  :class="{ 'is-alarm': row.alarm && row.value > 0 }"
                >
                  <span class="equip-screen-progress-label">
                    <span
                      class="equip-screen-progress-dot"
                      :style="{ background: row.color, color: row.color }"
                    />
                    {{ row.label }}
                  </span>
                  <div class="equip-screen-progress-track">
                    <div
                      class="equip-screen-progress-fill"
                      :style="{
                        width: `${row.pct}%`,
                        background: `linear-gradient(90deg, ${row.color}, rgba(255,255,255,0.15))`,
                      }"
                    />
                  </div>
                  <span class="equip-screen-progress-value">{{ row.value }} / {{ row.pct }}%</span>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <section class="equip-screen-center">
          <div class="equip-screen-map-wrap">
            <div class="equip-screen-panel equip-screen-panel--map">
              <div ref="mapStageRef" class="equip-screen-map-inner">
                <div class="equip-screen-map-legend">
                  <div
                    v-for="item in mapLegendItems"
                    :key="item.key"
                    class="equip-screen-map-legend__item"
                  >
                    <span
                      class="equip-screen-status-dot"
                      :style="{ color: item.color, background: item.color }"
                    />
                    {{ item.label }}
                  </div>
                </div>
                <div class="equip-screen-health-float">
                  <div class="equip-screen-health-float__label">{{ t('equipScreenPage.avgHealth') }}</div>
                  <div class="equip-screen-health-float__value">
                    {{ healthSummary.avgScore ?? '--' }}
                  </div>
                </div>
                <BaiduEquipScreenMap
                  v-if="mapAkReady"
                  ref="gisMapRef"
                  :devices="mapDevices"
                  :fit-view-key="fitViewKey"
                  :height="mapHeight"
                />
                <div v-else class="equip-screen-map-ak-hint">
                  {{ t('equipFormPage.mapKeyMissing') }}
                </div>
                <div class="equip-screen-health-row">
                  <div class="equip-screen-health-kpi">
                    <div class="equip-screen-health-kpi__item">
                      <div class="equip-screen-health-kpi__num equip-screen-health-kpi__num--ok">
                        {{ healthSummary.healthy }}
                      </div>
                      <div class="equip-screen-health-kpi__label">{{ t('equipScreenPage.healthOk') }}</div>
                    </div>
                    <div class="equip-screen-health-kpi__item">
                      <div class="equip-screen-health-kpi__num equip-screen-health-kpi__num--attention">
                        {{ healthSummary.attention }}
                      </div>
                      <div class="equip-screen-health-kpi__label">{{ t('equipScreenPage.healthAttention') }}</div>
                    </div>
                    <div class="equip-screen-health-kpi__item">
                      <div class="equip-screen-health-kpi__num equip-screen-health-kpi__num--warning">
                        {{ healthSummary.warning }}
                      </div>
                      <div class="equip-screen-health-kpi__label">{{ t('equipScreenPage.healthWarning') }}</div>
                    </div>
                    <div class="equip-screen-health-kpi__item">
                      <div class="equip-screen-health-kpi__num equip-screen-health-kpi__num--fault">
                        {{ healthSummary.fault }}
                      </div>
                      <div class="equip-screen-health-kpi__label">{{ t('equipScreenPage.healthFault') }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <aside class="equip-screen-side equip-screen-side-right">
          <div class="equip-screen-panel equip-screen-panel--trend">
            <div class="equip-screen-panel__title">{{ t('equipScreenPage.trend7dTitle') }}</div>
            <div class="equip-screen-chart-wrap">
              <div ref="trendChartRef" class="equip-screen-chart" />
            </div>
          </div>

          <div class="equip-screen-panel equip-screen-panel--trend">
            <div class="equip-screen-panel__title">{{ t('equipScreenPage.trendTodayTitle') }}</div>
            <div class="equip-screen-chart-wrap">
              <div ref="currentTrendChartRef" class="equip-screen-chart" />
            </div>
          </div>

          <div class="equip-screen-panel equip-screen-panel--alarm">
            <div class="equip-screen-panel__title">{{ t('equipScreenPage.recentAlarms') }}</div>
            <div class="equip-screen-alarm-list">
              <div
                v-for="item in alarmList"
                :key="item.id"
                class="equip-screen-alarm-item"
                :class="[
                  `equip-screen-alarm-item--${resolveAlarmLevel(item)}`,
                  isAlarmUnread(item) ? 'is-new' : 'is-read',
                ]"
              >
                <div
                  class="equip-screen-alarm-dot"
                  :class="{ 'equip-screen-alarm-dot--placeholder': !isAlarmUnread(item) }"
                />
                <div class="equip-screen-alarm-body">
                  <div class="equip-screen-alarm-top">
                    <span
                      class="equip-screen-alarm-tag"
                      :class="`equip-screen-alarm-tag--${resolveAlarmLevel(item)}`"
                    >
                      {{ alarmLevelLabel(resolveAlarmLevel(item)) }}
                    </span>
                    <div class="equip-screen-alarm-actions">
                      <span v-if="formatAlarmTime(item.createdTime)" class="equip-screen-alarm-time">
                        {{ formatAlarmTime(item.createdTime) }}
                      </span>
                      <button
                        v-if="isAlarmUnread(item)"
                        type="button"
                        class="equip-screen-alarm-read-btn"
                        :title="t('overviewPage.markRead')"
                        @click="onMarkAlarmRead(item)"
                      >
                        <CheckOutlined />
                      </button>
                    </div>
                  </div>
                  <div class="equip-screen-alarm-title">{{ item.title || '—' }}</div>
                  <div
                    v-if="alarmDisplayContent(item)"
                    class="equip-screen-alarm-content"
                    :title="alarmDisplayContent(item)"
                  >
                    {{ alarmDisplayContent(item) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </main>

      <footer
        v-if="footerMarquee.text"
        class="equip-screen-footer"
        :class="footerMarquee.tone ? `equip-screen-footer--${footerMarquee.tone}` : undefined"
      >
        <div class="equip-screen-footer__marquee">{{ footerMarquee.text }}</div>
      </footer>

      <WorkshopSceneSidePanel v-model="selectedWorkshop" />
    </div>
  </div>
</template>
