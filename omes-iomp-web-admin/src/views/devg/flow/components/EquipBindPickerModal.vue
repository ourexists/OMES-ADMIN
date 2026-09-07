<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { EquipRecord } from '@/api/device'
import { fetchEquipPage } from '@/api/device'
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const props = defineProps<{
  open: boolean
  excludeIds?: string[]
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: [ids: string[]]
}>()

const { t } = useI18n()
const loading = ref(false)
const dataSource = ref<EquipRecord[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ name: '', selfCode: '' })
const checkedKeys = ref<string[]>([])

const excludeSet = computed(() => new Set(props.excludeIds || []))

const columns = computed(() => [
  { title: t('devgFlowPage.devName'), dataIndex: 'name', key: 'name', ellipsis: true },
  { title: t('devgFlowPage.devCode'), dataIndex: 'selfCode', key: 'selfCode', width: 150, align: 'center' as const },
  { title: t('devgFlowPage.devType'), dataIndex: 'typeDesc', key: 'typeDesc', width: 120, ellipsis: true },
  { title: t('devgFlowPage.workshop'), key: 'workshop', width: 120, ellipsis: true },
])

const rowSelection = computed(() => ({
  selectedRowKeys: checkedKeys.value,
  preserveSelectedRowKeys: true,
  getCheckboxProps: (record: EquipRecord) => ({
    disabled: excludeSet.value.has(record.id),
  }),
  onChange: (keys: (string | number)[]) => {
    checkedKeys.value = keys.map(String).filter((id) => !excludeSet.value.has(id))
  },
}))

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchEquipPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name.trim() || undefined,
      selfCode: searchForm.selfCode.trim() || undefined,
      queryWorkshop: true,
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

function onConfirm() {
  emit('confirm', checkedKeys.value)
  emit('update:open', false)
}

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    checkedKeys.value = []
    onReset()
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :title="t('devgFlowPage.pickerTitle')"
    width="860px"
    destroy-on-close
    :ok-button-props="{ disabled: !checkedKeys.length }"
    @cancel="emit('update:open', false)"
    @ok="onConfirm"
  >
    <div class="picker-search-toolbar search-toolbar--compact">
      <a-form layout="inline" class="picker-search-form" :model="searchForm" @finish="onSearch">
        <a-form-item>
          <a-input
            v-model:value="searchForm.name"
            allow-clear
            size="small"
            :placeholder="t('devgFlowPage.devName')"
          />
        </a-form-item>
        <a-form-item>
          <a-input
            v-model:value="searchForm.selfCode"
            allow-clear
            size="small"
            :placeholder="t('devgFlowPage.devCode')"
          />
        </a-form-item>
        <CompactSearchActions
          :query-title="t('devgFlowPage.query')"
          :reset-title="t('devgFlowPage.reset')"
          @reset="onReset"
        />
      </a-form>
    </div>
    <p v-if="checkedKeys.length" class="picker-hint">
      {{ t('devgFlowPage.selectedEquipCount', { count: checkedKeys.length }) }}
    </p>
    <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
      <template #default="{ scrollY }">
        <a-table
          row-key="id"
          size="middle"
          bordered
          :loading="loading"
          :columns="columns"
          :data-source="dataSource"
          :row-selection="rowSelection"
          :pagination="{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showTotal: (total: number) => t('devgFlowPage.paginationTotal', { total }),
          }"
          :scroll="{ x: 640, y: scrollY }"
          @change="onTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'workshop'">
              {{ (record as EquipRecord).workshop?.name || '-' }}
            </template>
          </template>
        </a-table>
      </template>
    </TableScrollWrap>
  </a-modal>
</template>

<style scoped>
.picker-hint {
  margin: 0 0 8px;
  color: var(--omes-color-text-secondary);
  font-size: 13px;
}
</style>
