<script setup lang="ts">
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Dayjs } from 'dayjs'
import { LineChartOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import type { HealthIndicatorRecord } from '@/api/equip-health'
import { fetchHealthIndicatorsByStatTime } from '@/api/equip-health'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import { message } from 'ant-design-vue'

const { t } = useI18n()

const loading = ref(false)
const queried = ref(false)
const searchForm = reactive({
  statTime: null as Dayjs | null,
})
const dataSource = ref<HealthIndicatorRecord[]>([])

const columns = computed(() => [
  { title: t('healthIndicatorPage.colSn'), dataIndex: 'sn', key: 'sn', width: 140, ellipsis: true },
  {
    title: t('equipHealth.score'),
    dataIndex: 'score',
    key: 'score',
    width: 100,
    align: 'center' as const,
    sorter: (a: HealthIndicatorRecord, b: HealthIndicatorRecord) => (a.score ?? 0) - (b.score ?? 0),
  },
  {
    title: t('equipHealth.healthLevel'),
    key: 'healthLevel',
    width: 110,
    align: 'center' as const,
  },
  {
    title: t('equipHealth.alarmCount'),
    dataIndex: 'alarmCount',
    key: 'alarmCount',
    width: 100,
    align: 'center' as const,
  },
  {
    title: t('equipHealth.alarmDurationMin'),
    dataIndex: 'alarmDurationMinutes',
    key: 'alarmDurationMinutes',
    width: 130,
    align: 'center' as const,
  },
  {
    title: t('equipHealth.runDurationMin'),
    dataIndex: 'runDurationMinutes',
    key: 'runDurationMinutes',
    width: 130,
    align: 'center' as const,
  },
  {
    title: t('equipHealth.onlineDurationMin'),
    dataIndex: 'onlineDurationMinutes',
    key: 'onlineDurationMinutes',
    width: 140,
    align: 'center' as const,
  },
  {
    title: t('equipHealth.periodStart'),
    dataIndex: 'periodStart',
    key: 'periodStart',
    width: 170,
    ellipsis: true,
  },
  {
    title: t('equipHealth.periodEnd'),
    dataIndex: 'periodEnd',
    key: 'periodEnd',
    width: 170,
    ellipsis: true,
  },
])

const emptyDescription = computed(() =>
  queried.value ? t('healthIndicatorPage.emptyResult') : t('equipHealth.msg.selectStatTime'),
)

function healthLevelLabel(record: HealthIndicatorRecord): string {
  if (record.healthLevelDesc) {
    return record.healthLevelDesc
  }
  const level = record.healthLevel
  if (level === 0) return t('equipHealth.level.healthy')
  if (level === 1) return t('equipHealth.level.attention')
  if (level === 2) return t('equipHealth.level.warning')
  if (level === 3) return t('equipHealth.level.fault')
  return '-'
}

function healthLevelColor(level?: number): string {
  if (level === 0) return 'success'
  if (level === 1) return 'processing'
  if (level === 2) return 'warning'
  if (level === 3) return 'error'
  return 'default'
}

async function onSearch() {
  if (!searchForm.statTime) {
    message.warning(t('equipHealth.msg.selectStatTime'))
    return
  }
  loading.value = true
  queried.value = true
  try {
    const formatted = searchForm.statTime.format('YYYY-MM-DD HH:mm:ss')
    const list = await fetchHealthIndicatorsByStatTime(formatted)
    dataSource.value = Array.isArray(list) ? list : []
  } finally {
    loading.value = false
  }
}

function onClear() {
  searchForm.statTime = null
  dataSource.value = []
  queried.value = false
}
</script>

<template>
  <div class="admin-page health-indicator-page">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle>
          <template #icon><LineChartOutlined /></template>
          {{ t('healthIndicatorPage.title', t('menu.equipHealthQuery', '健康评估')) }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-tag v-if="queried && dataSource.length" color="processing">
          {{ t('healthIndicatorPage.total', { count: dataSource.length }) }}
        </a-tag>
      </template>

      <div class="admin-panel-body">
        <div class="search-toolbar search-toolbar--compact">
          <a-form layout="inline" class="search-form" @finish="onSearch">
            <a-form-item :label="t('equipHealth.statTime')" name="statTime">
              <a-date-picker size="small"
                v-model:value="searchForm.statTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                :placeholder="t('equipHealth.statTime')"
              />
            </a-form-item>
            <CompactSearchActions
              :query-title="t('healthIndicatorPage.query')"
              :reset-title="t('healthIndicatorPage.reset')"
              :loading="loading"
              @reset="onClear"
            >
              <a-tooltip :title="t('healthIndicatorPage.refresh')">
                <a-button size="small" :loading="loading" @click="onSearch">
                  <ReloadOutlined />
                </a-button>
              </a-tooltip>
            </CompactSearchActions>
          </a-form>
        </div>

        <TableScrollWrap :refresh-keys="[dataSource.length, queried]">
          <template #default="{ scrollY }">
            <a-table
              row-key="sn"
              size="middle"
              bordered
              class="scroll-table indicator-table"
              :columns="columns"
              :data-source="dataSource"
              :scroll="{ x: 1200, y: scrollY }"
              :pagination="{
                showSizeChanger: true,
                showTotal: (total: number) => t('healthIndicatorPage.paginationTotal', { total }),
              }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'healthLevel'">
                  <a-tag :color="healthLevelColor(record.healthLevel)">
                    {{ healthLevelLabel(record) }}
                  </a-tag>
                </template>
              </template>

              <template #emptyText>
                <a-empty :description="emptyDescription" />
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.panel-card {
  border-radius: var(--omes-radius-md);
  box-shadow: var(--omes-shadow-card-sm);
}

.panel-card :deep(.ant-card-head) {
  min-height: 48px;
  border-bottom: 1px solid var(--omes-color-border);
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.search-toolbar {
  margin-bottom: 12px;
  padding: 12px 16px;
  background: var(--omes-color-bg-elevated);
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-md);
}

.search-form {
  margin-bottom: 0;
}

.indicator-table :deep(.ant-table-thead > tr > th) {
  background: var(--omes-color-bg-elevated);
  font-weight: 600;
}
</style>
