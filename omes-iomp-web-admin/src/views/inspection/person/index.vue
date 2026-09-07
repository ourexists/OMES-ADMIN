<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'
import type { InspectPersonRecord } from '@/api/inspect-person'
import { deleteInspectPersons, fetchInspectPersonPage } from '@/api/inspect-person'
import InspectPersonFormModal from './components/InspectPersonFormModal.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const { t } = useI18n()

const loading = ref(false)
const dataSource = ref<InspectPersonRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const searchForm = reactive({
  name: '',
  jobNumber: '',
})

const formOpen = ref(false)
const editingRecord = ref<InspectPersonRecord | null>(null)

const columns = computed(() => [
  { title: t('inspectPersonPage.colName'), dataIndex: 'name', key: 'name', width: 120, ellipsis: true },
  { title: t('inspectPersonPage.colJobNumber'), dataIndex: 'jobNumber', key: 'jobNumber', width: 120, ellipsis: true },
  { title: t('inspectPersonPage.colMobile'), dataIndex: 'mobile', key: 'mobile', width: 130 },
  { title: t('inspectPersonPage.colAccount'), dataIndex: 'accountName', key: 'accountName', width: 140, ellipsis: true },
  { title: t('inspectPersonPage.colRemark'), dataIndex: 'remark', key: 'remark', minWidth: 140, ellipsis: true },
  { title: t('inspectPersonPage.colCreatedTime'), dataIndex: 'createdTime', key: 'createdTime', width: 170 },
  { title: t('inspectPersonPage.colAction'), key: 'action', width: 150, fixed: 'right' as const },
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}))

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchInspectPersonPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name.trim() || undefined,
      jobNumber: searchForm.jobNumber.trim() || undefined,
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
  searchForm.name = ''
  searchForm.jobNumber = ''
  onSearch()
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  loadTable()
}

function openCreate() {
  editingRecord.value = null
  formOpen.value = true
}

function openEdit(record: InspectPersonRecord) {
  editingRecord.value = record
  formOpen.value = true
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('inspectPersonPage.deleteConfirm'),
    content: t('inspectPersonPage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteInspectPersons(ids)
      message.success(t('inspectPersonPage.deleteSuccess'))
      selectedRowKeys.value = []
      loadTable()
    },
  })
}

onMounted(loadTable)
</script>

<template>
  <div class="admin-page inspect-person-page">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle>
          <template #icon><TeamOutlined /></template>
          {{ t('inspectPersonPage.title') }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-space :size="8">
          <a-tag v-if="hasSelection" color="blue">
            {{ t('inspectPersonPage.selectedCount', { count: selectedRowKeys.length }) }}
          </a-tag>
          <a-tag v-if="pagination.total" color="processing">
            {{ t('inspectPersonPage.total', { count: pagination.total }) }}
          </a-tag>
        </a-space>
      </template>

      <div class="admin-panel-body">
        <div class="search-toolbar search-toolbar--compact">
          <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
            <a-form-item :label="t('inspectPersonPage.colName')" name="name">
              <a-input size="small"
                v-model:value="searchForm.name"
                allow-clear
                :placeholder="t('inspectPersonPage.searchName')"
                class="search-input"
              >
                <template #prefix>
                  <SearchOutlined class="input-prefix-icon" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item :label="t('inspectPersonPage.colJobNumber')" name="jobNumber">
              <a-input size="small"
                v-model:value="searchForm.jobNumber"
                allow-clear
                :placeholder="t('inspectPersonPage.searchJobNumber')"
                class="search-input"
              />
            </a-form-item>
            <CompactSearchActions
              :query-title="t('inspectPersonPage.query')"
              :reset-title="t('inspectPersonPage.reset')"
              @reset="onReset"
            />
          </a-form>
        </div>

        <div class="table-toolbar">
          <a-space wrap>
            <a-button type="primary" @click="openCreate">
              <template #icon><PlusOutlined /></template>
              {{ t('inspectPersonPage.add') }}
            </a-button>
            <a-button danger :disabled="!hasSelection" @click="confirmDelete(selectedRowKeys)">
              <template #icon><DeleteOutlined /></template>
              {{ t('inspectPersonPage.batchDelete') }}
            </a-button>
            <a-button :loading="loading" @click="loadTable">
              <template #icon><ReloadOutlined /></template>
              {{ t('inspectPersonPage.refresh') }}
            </a-button>
          </a-space>
        </div>

        <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
          <template #default="{ scrollY }">
            <a-table
              row-key="id"
              size="middle"
              bordered
              class="scroll-table inspect-person-table"
              :columns="columns"
              :data-source="dataSource"
              :row-selection="rowSelection"
              :scroll="{ x: 1000, y: scrollY }"
              :pagination="{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
                showTotal: (total: number) => t('inspectPersonPage.paginationTotal', { total }),
              }"
              @change="onTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                  <a-space wrap size="small" class="action-group">
                    <a-button type="link" size="small" @click="openEdit(record)">
                      <EditOutlined />
                      {{ t('inspectPersonPage.edit') }}
                    </a-button>
                    <a-button type="link" size="small" danger @click="confirmDelete([record.id])">
                      <DeleteOutlined />
                      {{ t('inspectPersonPage.delete') }}
                    </a-button>
                  </a-space>
                </template>
              </template>

              <template #emptyText>
                <a-empty :description="t('inspectPersonPage.empty')">
                  <a-button type="primary" @click="openCreate">
                    <PlusOutlined />
                    {{ t('inspectPersonPage.add') }}
                  </a-button>
                </a-empty>
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>

    <InspectPersonFormModal v-model:open="formOpen" :record="editingRecord" @success="loadTable" />
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

.search-input {
  width: 200px;
}

.input-prefix-icon {
  color: var(--omes-color-text-placeholder);
}

.table-toolbar {
  margin-bottom: 16px;
}

.inspect-person-table :deep(.ant-table-thead > tr > th) {
  background: var(--omes-color-bg-elevated);
  font-weight: 600;
}

.action-group :deep(.ant-btn-link) {
  padding-inline: 4px;
}

.action-group :deep(.ant-btn-link .anticon) {
  margin-inline-end: 4px;
  font-size: 13px;
}
</style>
