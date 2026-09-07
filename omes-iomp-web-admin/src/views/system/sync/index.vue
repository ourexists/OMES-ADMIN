<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import {
  CloudSyncOutlined,
  DatabaseOutlined,
  ReloadOutlined, ThunderboltOutlined,
} from '@ant-design/icons-vue'
import {
  breakpointSyncProcess,
  fetchSyncPage,
  fetchSyncStatusOptions,
  fetchSyncTxOptions,
} from '@/api/sync'
import type { SyncRecord } from '@/types/sync'
import SyncResourceModal from './components/SyncResourceModal.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const { t } = useI18n()

const loading = ref(false)
const dataSource = ref<SyncRecord[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const syncTxOptions = ref<{ value: string; label: string }[]>([])
const statusOptions = ref<{ value: string; label: string }[]>([])

const searchForm = reactive({
  syncTx: undefined as string | undefined,
  status: undefined as string | undefined,
})

const resourceOpen = ref(false)
const resourceSyncId = ref<string | null>(null)

const columns = computed(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 180, ellipsis: true },
  { title: t('syncPage.colSyncTx'), dataIndex: 'syncTx', key: 'syncTx', width: 160, ellipsis: true },
  { title: t('syncPage.colPartStart'), key: 'partStartTimestamp', width: 170 },
  { title: t('syncPage.colPartEnd'), key: 'partEndTimestamp', width: 170 },
  { title: t('syncPage.colStatus'), key: 'status', width: 100, align: 'center' as const },
  { title: t('syncPage.colPartMin'), dataIndex: 'partMin', key: 'partMin', width: 120, ellipsis: true },
  { title: t('syncPage.colPartMax'), dataIndex: 'partMax', key: 'partMax', width: 120, ellipsis: true },
  { title: t('syncPage.colCreatedTime'), key: 'createdTime', width: 170 },
  { title: t('syncPage.colAction'), key: 'action', width: 180, fixed: 'right' as const },
])

function formatDateTime(value?: string | null): string {
  if (!value) {
    return '-'
  }
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : value
}

function statusColor(status?: string): string {
  if (status === 'error') {
    return 'error'
  }
  if (status === 'end') {
    return 'success'
  }
  if (status === 'start') {
    return 'processing'
  }
  return 'default'
}

async function loadFilters() {
  const [txList, statusList] = await Promise.all([fetchSyncTxOptions(), fetchSyncStatusOptions()])
  syncTxOptions.value = (txList || []).map((item) => ({
    value: item.id || item.name,
    label: item.name || item.id,
  }))
  statusOptions.value = (statusList || []).map((item) => ({
    value: item.id || item.name,
    label: item.name || item.id,
  }))
}

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchSyncPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      syncTx: searchForm.syncTx,
      status: searchForm.status,
    })
    dataSource.value = result.records || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pagination.current = 1
  loadTable()
}

function onReset() {
  searchForm.syncTx = undefined
  searchForm.status = undefined
  onSearch()
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  loadTable()
}

function openResources(record: SyncRecord) {
  resourceSyncId.value = record.id
  resourceOpen.value = true
}

function confirmBreakpoint(record: SyncRecord) {
  const isError = record.status === 'error'
  Modal.confirm({
    title: t('syncPage.breakpointConfirm'),
    content: isError ? t('syncPage.breakpointMsg') : t('syncPage.breakpointAlert'),
    okType: isError ? 'primary' : 'danger',
    onOk: async () => {
      await breakpointSyncProcess(record.id)
      message.success(t('syncPage.breakpointSuccess'))
      loadTable()
    },
  })
}

onMounted(async () => {
  await loadFilters()
  await loadTable()
})
</script>

<template>
  <div class="admin-page system-module-page system-module-page--sync">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle :subtitle="t('syncPage.subtitle')">
          <template #icon><CloudSyncOutlined /></template>
          {{ t('syncPage.title') }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-space :size="8" class="extra-tags">
          <a-tag v-if="pagination.total" color="processing">
            {{ t('syncPage.total', { count: pagination.total }) }}
          </a-tag>
        </a-space>
      </template>

      <div class="admin-panel-body">
        <div class="search-toolbar search-toolbar--compact">
          <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
            <a-form-item :label="t('syncPage.colSyncTx')" name="syncTx">
              <a-select size="small"
                v-model:value="searchForm.syncTx"
                allow-clear
                class="search-select"
                :placeholder="t('syncPage.searchSyncTx')"
                :options="syncTxOptions"
              />
            </a-form-item>
            <a-form-item :label="t('syncPage.colStatus')" name="status">
              <a-select size="small"
                v-model:value="searchForm.status"
                allow-clear
                class="search-select-sm"
                :placeholder="t('syncPage.searchStatus')"
                :options="statusOptions"
              />
            </a-form-item>
            <CompactSearchActions
              :query-title="t('syncPage.query')"
              :reset-title="t('syncPage.reset')"
              @reset="onReset"
            />
          </a-form>
        </div>

        <div class="table-toolbar">
          <a-button :loading="loading" @click="loadTable">
            <template #icon><ReloadOutlined /></template>
            {{ t('syncPage.refresh') }}
          </a-button>
        </div>

        <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
          <template #default="{ scrollY }">
            <a-table
              row-key="id"
              size="middle"
              bordered
              class="scroll-table system-module-table"
              :columns="columns"
              :data-source="dataSource"
              :scroll="{ x: 1200, y: scrollY }"
              :pagination="{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
                showTotal: (total: number) => t('syncPage.paginationTotal', { total }),
              }"
              @change="onTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'partStartTimestamp'">
                  <span class="time-cell">{{ formatDateTime(record.partStartTimestamp) }}</span>
                </template>
                <template v-else-if="column.key === 'partEndTimestamp'">
                  <span class="time-cell">{{ formatDateTime(record.partEndTimestamp) }}</span>
                </template>
                <template v-else-if="column.key === 'createdTime'">
                  <span class="time-cell">{{ formatDateTime(record.createdTime) }}</span>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="statusColor(record.status)">{{ record.status || '—' }}</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space wrap size="small" class="action-group">
                    <a-button
                      v-if="record.status !== 'end'"
                      type="link"
                      size="small"
                      class="action-link--warn"
                      @click="confirmBreakpoint(record)"
                    >
                      <ThunderboltOutlined />
                      {{ t('syncPage.breakpoint') }}
                    </a-button>
                    <a-button type="link" size="small" @click="openResources(record)">
                      <DatabaseOutlined />
                      {{ t('syncPage.resource') }}
                    </a-button>
                  </a-space>
                </template>
              </template>

              <template #emptyText>
                <a-empty :description="t('syncPage.empty')" />
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>

    <SyncResourceModal v-model:open="resourceOpen" :sync-id="resourceSyncId" />
  </div>
</template>
