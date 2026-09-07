<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { MaterialRecord } from '@/api/material'
import { fetchMaterialPage } from '@/api/material'
import type { TfToolingRef } from '@/types/line'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const props = defineProps<{
  open: boolean
  selected: TfToolingRef[]
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: [value: TfToolingRef[]]
}>()

const { t } = useI18n()
const loading = ref(false)
const dataSource = ref<MaterialRecord[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ name: '', selfCode: '' })
const checkedKeys = ref<string[]>([])
const selectedMap = ref<Map<string, TfToolingRef>>(new Map())

const columns = computed(() => [
  { title: t('lineFlowPage.toolingName'), dataIndex: 'name', key: 'name', ellipsis: true },
  { title: t('lineFlowPage.toolingCode'), dataIndex: 'selfCode', key: 'selfCode', width: 160, align: 'center' as const },
])

function toRef(row: MaterialRecord): TfToolingRef {
  return {
    toolingId: row.id,
    toolingCode: row.selfCode || '',
    toolingName: row.name || row.selfCode || '',
  }
}

function syncSelectedFromProps() {
  const map = new Map<string, TfToolingRef>()
  ;(props.selected || []).forEach((item) => {
    if (item.toolingId) {
      map.set(String(item.toolingId), item)
    }
  })
  selectedMap.value = map
  checkedKeys.value = [...map.keys()]
}

const rowSelection = computed(() => ({
  selectedRowKeys: checkedKeys.value,
  preserveSelectedRowKeys: true,
  onChange: (keys: (string | number)[]) => {
    const keySet = new Set(keys.map(String))
    for (const id of [...selectedMap.value.keys()]) {
      if (!keySet.has(id)) {
        selectedMap.value.delete(id)
      }
    }
    dataSource.value.forEach((row) => {
      if (keySet.has(row.id)) {
        selectedMap.value.set(row.id, toRef(row))
      }
    })
    checkedKeys.value = keys.map(String)
  },
}))

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

function onConfirm() {
  emit('confirm', [...selectedMap.value.values()])
  emit('update:open', false)
}

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    syncSelectedFromProps()
    onReset()
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :title="t('lineFlowPage.toolingPickerTitle')"
    width="720px"
    destroy-on-close
    @cancel="emit('update:open', false)"
    @ok="onConfirm"
  >
    <p class="picker-hint">{{ t('lineFlowPage.toolingPickerHint') }}</p>
    <a-form layout="inline" class="picker-search">
      <a-form-item :label="t('lineFlowPage.toolingName')">
        <a-input v-model:value="searchForm.name" allow-clear @press-enter="onSearch" />
      </a-form-item>
      <a-form-item :label="t('lineFlowPage.toolingCode')">
        <a-input v-model:value="searchForm.selfCode" allow-clear @press-enter="onSearch" />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" @click="onSearch">{{ t('lineFlowPage.search') }}</a-button>
          <a-button @click="onReset">{{ t('lineFlowPage.reset') }}</a-button>
        </a-space>
      </a-form-item>
    </a-form>
    <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
      <template #default="{ scrollY }">
        <a-table
          row-key="id"
          size="small"
          :loading="loading"
          :columns="columns"
          :data-source="dataSource"
          :row-selection="rowSelection"
          :pagination="{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showTotal: (total: number) => t('lineFlowPage.total', { count: total }),
          }"
          :scroll="{ y: scrollY }"
          @change="onTableChange"
        />
      </template>
    </TableScrollWrap>
  </a-modal>
</template>

<style scoped>
.picker-search {
  margin-bottom: 12px;
}

.picker-hint {
  margin: 0 0 12px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
</style>
