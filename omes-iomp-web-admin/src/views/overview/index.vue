<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import {
  AlertOutlined,
  BellOutlined,
  CheckOutlined,
  ClusterOutlined,
  DashboardOutlined,
  DisconnectOutlined,
  PauseCircleOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  WifiOutlined,
} from '@ant-design/icons-vue'
import type { WorkshopNode } from '@/api/device'
import {
  fetchAlarmMessagePage,
  fetchEquipOnlineTrend,
  fetchEquipRealtimeCount,
  markAlarmMessageRead,
  type AlarmMessageRecord,
  type EquipCountResult,
} from '@/api/overview'
import { storeToRefs } from 'pinia'
import WorkshopTreeSelect from '@/components/WorkshopTreeSelect.vue'
import { useThemeStore } from '@/stores/theme'
import { EQUIP_STATUS_CHART_COLORS } from '@/utils/equip-status'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const POLL_MS = 10_000

const CHART_THEME = {
  online: '#0ea5e9',
  offline: '#94a3b8',
  running: '#10b981',
  stopped: '#f59e0b',
} as const

const { t } = useI18n()
const themeStore = useThemeStore()
const { omesTokens, variant } = storeToRefs(themeStore)

const pageRef = ref<HTMLElement | null>(null)
const statusChartRef = ref<HTMLElement | null>(null)
const trendChartRef = ref<HTMLElement | null>(null)

let statusChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null
let resizeObserver: ResizeObserver | null = null
let pollTimer: ReturnType<typeof setInterval> | null = null

const selectedWorkshop = ref<WorkshopNode | null>(null)
const countLoading = ref(false)
const trendLoading = ref(false)
const alarmLoading = ref(false)
const counts = shallowRef<EquipCountResult>({})
const countLock = ref(false)
const trendLock = ref(false)

const alarmData = ref<AlarmMessageRecord[]>([])
const alarmPagination = ref({ current: 1, pageSize: 3, total: 0 })

const workshopLabel = computed(() => selectedWorkshop.value?.name?.trim() || '')

const kpiItems = computed(() => [
  { key: 'total', value: counts.value.total ?? 0, label: t('overviewPage.kpiTotal'), tone: 'total', icon: ClusterOutlined },
  { key: 'online', value: counts.value.online ?? 0, label: t('overviewPage.kpiOnline'), tone: 'online', icon: WifiOutlined },
  { key: 'offline', value: counts.value.offline ?? 0, label: t('overviewPage.kpiOffline'), tone: 'offline', icon: DisconnectOutlined },
  { key: 'alarm', value: counts.value.alarm ?? 0, label: t('overviewPage.kpiAlarm'), tone: 'alarm', icon: BellOutlined },
  { key: 'run', value: counts.value.run ?? 0, label: t('overviewPage.kpiRun'), tone: 'run', icon: PlayCircleOutlined },
  { key: 'stop', value: counts.value.stopped ?? 0, label: t('overviewPage.kpiStop'), tone: 'stop', icon: PauseCircleOutlined },
])

const alarmColumns = computed(() => [
  { title: t('overviewPage.colTitle'), dataIndex: 'title', key: 'title', ellipsis: true },
  { title: t('overviewPage.colContent'), dataIndex: 'context', key: 'context', ellipsis: true },
  { title: t('overviewPage.colAction'), key: 'action', width: 88, align: 'center' as const },
])

function trendDateRange() {
  const now = dayjs()
  const start = now.subtract(6, 'day').startOf('day')
  const end = now.endOf('day')
  return {
    start,
    end,
    startDate: start.format('YYYY-MM-DD HH:mm:ss'),
    endDate: end.format('YYYY-MM-DD HH:mm:ss'),
  }
}

function buildStatusChartOption(count: EquipCountResult): echarts.EChartsOption {
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(11,18,32,0.9)',
      borderColor: omesTokens.value.colorPrimary,
      textStyle: { color: '#e6f1ff' },
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'middle',
      textStyle: { fontSize: 12 },
    },
    series: [
      {
        name: t('overviewPage.chartStatus'),
        type: 'pie',
        radius: ['35%', '50%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        label: { show: true, formatter: '{d}%' },
        data: [
          { value: count.online ?? 0, name: t('realtimePage.online'), itemStyle: { color: CHART_THEME.online } },
          { value: count.offline ?? 0, name: t('realtimePage.offline'), itemStyle: { color: CHART_THEME.offline } },
        ],
      },
      {
        name: t('overviewPage.chartRun'),
        type: 'pie',
        radius: ['15%', '25%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        label: { show: true, fontSize: 10, formatter: '{d}%' },
        data: [
          { value: count.run ?? 0, name: t('realtimePage.run'), itemStyle: { color: CHART_THEME.running } },
          { value: count.stopped ?? 0, name: t('realtimePage.stop'), itemStyle: { color: CHART_THEME.stopped } },
        ],
      },
    ],
  }
}

function buildTrendChartOption(): echarts.EChartsOption {
  return {
    color: [EQUIP_STATUS_CHART_COLORS.onlineOn],
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 24, top: 24, bottom: 32 },
    xAxis: { type: 'time' },
    yAxis: {
      type: 'value',
      min: 0,
      minInterval: 1,
      splitNumber: 4,
    },
    series: [
      {
        name: t('overviewPage.trendSeries'),
        type: 'line',
        smooth: true,
        data: [] as [string | number, number][],
      },
    ],
  }
}

function canInitChart(el: HTMLElement | null): el is HTMLElement {
  return Boolean(el && el.clientWidth > 0 && el.clientHeight > 0)
}

function initCharts() {
  if (canInitChart(statusChartRef.value) && !statusChart) {
    statusChart = echarts.init(statusChartRef.value)
    statusChart.setOption(buildStatusChartOption(counts.value))
  }
  if (canInitChart(trendChartRef.value) && !trendChart) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption(buildTrendChartOption())
  }
}

watch(variant, () => {
  statusChart?.setOption(buildStatusChartOption(counts.value), false)
  trendChart?.setOption(buildTrendChartOption(), false)
})

function ensureChartsReady(): Promise<void> {
  return new Promise((resolve) => {
    let attempts = 0
    const tick = () => {
      initCharts()
      attempts += 1
      if ((statusChart && trendChart) || attempts >= 60) {
        resizeCharts()
        resolve()
        return
      }
      window.requestAnimationFrame(tick)
    }
    tick()
  })
}

function resizeCharts() {
  statusChart?.resize()
  trendChart?.resize()
}

async function loadCounts(silent = false) {
  if (countLock.value) {
    return
  }
  countLock.value = true
  if (!silent) {
    countLoading.value = true
  }
  try {
    const result = await fetchEquipRealtimeCount(selectedWorkshop.value?.selfCode)
    counts.value = result || {}
    statusChart?.setOption(buildStatusChartOption(counts.value), false)
  } catch {
    if (!silent) {
      message.error(t('overviewPage.loadCountFailed'))
    }
  } finally {
    countLock.value = false
    if (!silent) {
      countLoading.value = false
    }
  }
}

async function loadTrend(silent = false) {
  if (trendLock.value || !trendChart) {
    return
  }
  trendLock.value = true
  if (!silent) {
    trendLoading.value = true
  }
  try {
    const range = trendDateRange()
    const result = await fetchEquipOnlineTrend(
      selectedWorkshop.value?.selfCode,
      range.startDate,
      range.endDate,
    )
    const data = (result || []).map((item) => [item.time, item.num ?? 0] as [string | Date, number])
    trendChart.setOption({
      xAxis: { min: range.start.toDate(), max: range.end.toDate() },
      series: [{ data }],
    }, false)
  } catch {
    if (!silent) {
      message.error(t('overviewPage.loadTrendFailed'))
    }
  } finally {
    trendLock.value = false
    if (!silent) {
      trendLoading.value = false
    }
  }
}

async function loadAlarms() {
  alarmLoading.value = true
  try {
    const result = await fetchAlarmMessagePage(
      alarmPagination.value.current,
      alarmPagination.value.pageSize,
    )
    alarmData.value = result?.records ?? []
    alarmPagination.value.total = result?.total ?? 0
  } catch {
    message.error(t('overviewPage.loadAlarmFailed'))
  } finally {
    alarmLoading.value = false
  }
}

async function refreshAll(options: { silent?: boolean } = {}) {
  const silent = options.silent === true
  await Promise.all([loadCounts(silent), loadTrend(silent), loadAlarms()])
}

function onWorkshopChange() {
  alarmPagination.value.current = 1
  void refreshAll()
}

function onAlarmTableChange(pag: TablePaginationConfig) {
  alarmPagination.value.current = pag.current || 1
  alarmPagination.value.pageSize = pag.pageSize || 3
  void loadAlarms()
}

function alarmRowClass(record: AlarmMessageRecord) {
  return record.readStatus === 0 ? 'overview-alarm-row--unread' : ''
}

async function onMarkRead(record: AlarmMessageRecord) {
  if (!record.id || record.readStatus !== 0) {
    return
  }
  try {
    await markAlarmMessageRead(record.id)
    await loadAlarms()
  } catch {
    message.error(t('overviewPage.markReadFailed'))
  }
}

function startPolling() {
  stopPolling()
  pollTimer = setInterval(() => {
    if (document.visibilityState === 'hidden') {
      return
    }
    void refreshAll({ silent: true })
  }, POLL_MS)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function onVisibilityChange() {
  if (document.visibilityState === 'visible') {
    void refreshAll({ silent: true })
  }
}

watch(
  () => t('realtimePage.online'),
  () => {
    if (statusChart) {
      statusChart.setOption(buildStatusChartOption(counts.value), false)
    }
  },
)

onMounted(async () => {
  document.addEventListener('visibilitychange', onVisibilityChange)

  await nextTick()
  await ensureChartsReady()
  resizeObserver = new ResizeObserver(() => {
    initCharts()
    resizeCharts()
  })
  if (statusChartRef.value) {
    resizeObserver.observe(statusChartRef.value)
  }
  if (trendChartRef.value) {
    resizeObserver.observe(trendChartRef.value)
  }
  if (pageRef.value) {
    resizeObserver.observe(pageRef.value)
  }
  await refreshAll()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', onVisibilityChange)
  resizeObserver?.disconnect()
  statusChart?.dispose()
  trendChart?.dispose()
  statusChart = null
  trendChart = null
})
</script>

<template>
  <div ref="pageRef" class="overview-page admin-page">
    <header class="overview-hero overview-panel">
      <div class="overview-hero__body">
        <div class="overview-hero__brand">
          <div class="overview-hero__icon">
            <DashboardOutlined />
          </div>
          <div class="overview-hero__text">
            <h1 class="overview-hero__title">{{ t('overviewPage.pageTitle') }}</h1>
            <p class="overview-hero__sub">{{ t('overviewPage.subtitle') }}</p>
          </div>
        </div>
        <div class="overview-hero__tools">
          <WorkshopTreeSelect
            v-model="selectedWorkshop"
            class="overview-workshop-select"
            @change="onWorkshopChange"
          />
          <a-tag v-if="workshopLabel" class="overview-workshop-tag">{{ workshopLabel }}</a-tag>
          <a-tag color="processing" class="overview-live-tag">
            <WifiOutlined />
            {{ t('realtimePage.liveUpdating') }}
          </a-tag>
          <a-tooltip :title="t('overviewPage.refresh')">
            <a-button size="small" :loading="countLoading || trendLoading" @click="refreshAll()">
              <ReloadOutlined />
            </a-button>
          </a-tooltip>
        </div>
      </div>
    </header>

    <section class="overview-block">
      <h2 class="overview-block__title">{{ t('overviewPage.sectionKpi') }}</h2>
      <a-spin :spinning="countLoading">
        <div class="overview-kpi-grid">
          <div
            v-for="item in kpiItems"
            :key="item.key"
            class="overview-kpi-item overview-panel"
          >
            <div class="overview-kpi-item__icon" :class="`overview-kpi-item__icon--${item.tone}`">
              <component :is="item.icon" />
            </div>
            <div class="overview-kpi-item__main">
              <div class="overview-kpi-item__num">{{ item.value }}</div>
              <div class="overview-kpi-item__label">{{ item.label }}</div>
            </div>
          </div>
        </div>
      </a-spin>
    </section>

    <section class="overview-block">
      <h2 class="overview-block__title">{{ t('overviewPage.sectionCharts') }}</h2>
      <div class="overview-charts">
        <a-card size="small" class="overview-panel overview-chart-panel" :title="t('overviewPage.statusChartTitle')">
          <a-spin :spinning="countLoading">
            <div ref="statusChartRef" class="overview-chart" />
          </a-spin>
        </a-card>
        <a-card size="small" class="overview-panel overview-chart-panel" :title="t('overviewPage.trendChartTitle')">
          <a-spin :spinning="trendLoading">
            <div ref="trendChartRef" class="overview-chart" />
          </a-spin>
        </a-card>
      </div>
    </section>

    <section class="overview-block">
      <h2 class="overview-block__title">{{ t('overviewPage.sectionAlarm') }}</h2>
      <a-card size="small" class="overview-panel overview-alarm-panel" :title="t('overviewPage.alarmDetail')">
        <template #extra>
          <a-tag color="error">
            <AlertOutlined />
            {{ t('overviewPage.alarmHint') }}
          </a-tag>
        </template>
        <a-table
          class="overview-alarm-table"
          size="small"
          row-key="id"
          :columns="alarmColumns"
          :data-source="alarmData"
          :loading="alarmLoading"
          :row-class-name="alarmRowClass"
          :pagination="{
            current: alarmPagination.current,
            pageSize: alarmPagination.pageSize,
            total: alarmPagination.total,
            showSizeChanger: true,
            pageSizeOptions: ['3', '5', '10'],
          }"
          @change="onAlarmTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'title'">
              <span class="overview-alarm-title">{{ record.title || '—' }}</span>
            </template>
            <template v-else-if="column.key === 'context'">
              <span class="overview-alarm-context">{{ record.context || '—' }}</span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-tooltip v-if="record.readStatus === 0" :title="t('overviewPage.markRead')">
                <a-button type="link" size="small" @click="onMarkRead(record)">
                  <CheckOutlined />
                </a-button>
              </a-tooltip>
            </template>
          </template>
          <template #emptyText>
            <a-empty :description="t('overviewPage.alarmEmpty')" />
          </template>
        </a-table>
      </a-card>
    </section>
  </div>
</template>
