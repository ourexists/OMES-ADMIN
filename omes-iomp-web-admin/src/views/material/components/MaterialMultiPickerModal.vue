<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  AppstoreOutlined,
  BarcodeOutlined,
  CheckOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import type { MaterialRecord } from '@/api/material'
import { fetchMaterialPage } from '@/api/material'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const props = defineProps<{
  open: boolean
  /** 已在配方原料明细中的 id，选择时显示但不可勾选 */
  excludeMatIds?: string[]
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: [rows: MaterialRecord[]]
}>()

const { t } = useI18n()

const loading = ref(false)
const dataSource = ref<MaterialRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const searchForm = reactive({
  name: '',
  selfCode: '',
})

const columns = computed(() => [
  { title: t('materialPage.name'), dataIndex: 'name', key: 'name', ellipsis: true },
  { title: t('materialPage.code'), dataIndex: 'selfCode', key: 'selfCode', width: 160, align: 'center' as const },
])

const excludeIdSet = computed(() => new Set(props.excludeMatIds || []))

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
  getCheckboxProps: (record: MaterialRecord) => ({
    disabled: excludeIdSet.value.has(record.id),
  }),
}))

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchMaterialPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name.trim() || undefined,
      selfCode: searchForm.selfCode.trim() || undefined,
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
  searchForm.selfCode = ''
  onSearch()
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  loadTable()
}

function closeModal() {
  emit('update:open', false)
}

function onConfirm() {
  const rows = dataSource.value.filter((row) => selectedRowKeys.value.includes(row.id))
  if (!rows.length) {
    message.warning(t('materialPage.pickerSelectOne'))
    return
  }
  const allowed = rows.filter((row) => !excludeIdSet.value.has(row.id))
  const skipped = rows.length - allowed.length
  if (!allowed.length) {
    message.warning(t('bomPage.detailSkipDuplicate', { count: skipped || rows.length }))
    return
  }
  if (skipped > 0) {
    message.info(t('bomPage.detailSkipDuplicate', { count: skipped }))
  }
  emit('confirm', allowed)
  emit('update:open', false)
}

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    selectedRowKeys.value = []
    searchForm.name = ''
    searchForm.selfCode = ''
    pagination.current = 1
    loadTable()
  },
)
</script>

<template>
  <a-modal
    :open="open"
    width="min(920px, 96vw)"
    destroy-on-close
    class="material-picker-modal"
    :title="t('bomPage.detailPickMaterial')"
    @cancel="closeModal"
    @update:open="emit('update:open', $event)"
  >
    <div class="picker-body">
      <div class="search-toolbar search-toolbar--compact">
        <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
          <a-form-item :label="t('materialPage.name')" name="name">
            <a-input size="small"
              v-model:value="searchForm.name"
              allow-clear
              :placeholder="t('materialPage.searchName')"
              class="search-input"
            >
              <template #prefix>
                <SearchOutlined class="input-prefix-icon" />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item :label="t('materialPage.code')" name="selfCode">
            <a-input size="small"
              v-model:value="searchForm.selfCode"
              allow-clear
              :placeholder="t('materialPage.searchCode')"
              class="search-input"
            >
              <template #prefix>
                <BarcodeOutlined class="input-prefix-icon" />
              </template>
            </a-input>
          </a-form-item>
          <CompactSearchActions
              :query-title="t('materialPage.query')"
              :reset-title="t('materialPage.reset')"
              @reset="onReset"
            />
        </a-form>
      </div>

      <div class="picker-toolbar">
        <a-tag v-if="hasSelection" color="blue">
          {{ t('materialPage.selectedCount', { count: selectedRowKeys.length }) }}
        </a-tag>
        <a-tag v-if="pagination.total" color="processing">
          {{ t('materialPage.total', { count: pagination.total }) }}
        </a-tag>
        <a-tag v-if="excludeMatIds?.length" color="default">
          {{ t('bomPage.detailAlreadyInList', { count: excludeMatIds.length }) }}
        </a-tag>
      </div>

      <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total, props.open ? '1' : '0']">
        <template #default="{ scrollY }">
          <a-table
            row-key="id"
            size="small"
            bordered
            class="scroll-table picker-table"
            :columns="columns"
            :data-source="dataSource"
            :loading="loading"
            :row-selection="rowSelection"
            :scroll="{ x: 560, y: scrollY }"
            :pagination="{
              current: pagination.current,
              pageSize: pagination.pageSize,
              total: pagination.total,
              showSizeChanger: true,
              showTotal: (total: number) => t('materialPage.paginationTotal', { total }),
            }"
            @change="onTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name'">
                <div class="mat-cell-name">
                  <span class="mat-cell-name__icon">
                    <AppstoreOutlined />
                  </span>
                  <span class="mat-cell-name__text">{{ record.name || '—' }}</span>
                </div>
              </template>
              <template v-else-if="column.key === 'selfCode'">
                <span v-if="record.selfCode" class="code-cell">{{ record.selfCode }}</span>
                <span v-else class="empty-cell">—</span>
              </template>
            </template>
            <template #emptyText>
              <a-empty :description="t('materialPage.empty')" />
            </template>
          </a-table>
        </template>
      </TableScrollWrap>
    </div>

    <template #footer>
      <a-button @click="closeModal">{{ t('materialPage.cancel') }}</a-button>
      <a-button type="primary" :disabled="!hasSelection" @click="onConfirm">
        <CheckOutlined />
        {{ t('materialPage.pickerConfirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<style scoped>
.material-picker-modal :deep(.ant-modal-body) {
  padding: 16px 24px 8px;
}

.picker-body {
  display: flex;
  flex-direction: column;
  min-height: 420px;
  max-height: min(68vh, 560px);
}

.search-toolbar {
  flex-shrink: 0;
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

.picker-toolbar {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.picker-body :deep(.table-scroll-wrap) {
  flex: 1;
  min-height: 280px;
}

.input-prefix-icon {
  color: var(--omes-color-text-placeholder);
}

.picker-table :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(180deg, var(--omes-color-bg-elevated) 0%, var(--omes-color-bg-layout) 100%);
  font-weight: 600;
}

.picker-table :deep(.ant-table-tbody > tr:nth-child(even) > td) {
  background: var(--omes-color-bg-muted);
}

.picker-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #f0f7ff !important;
}

.mat-cell-name {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.mat-cell-name__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  border-radius: var(--omes-radius-md);
  color: var(--omes-color-primary);
  background: var(--omes-color-primary-bg);
}

.mat-cell-name__text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.code-cell {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  color: var(--omes-color-primary);
  background: #f0f5ff;
  border: 1px solid var(--omes-color-primary-border);
  padding: 2px 10px;
  border-radius: var(--omes-radius-sm);
}

.empty-cell {
  color: rgba(0, 0, 0, 0.25);
}

@media (max-width: 768px) {
  .search-input {
    width: 100%;
  }
}
</style>
