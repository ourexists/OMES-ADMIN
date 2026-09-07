<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { MaterialRecord } from '@/api/material'
import { fetchMaterialPage } from '@/api/material'
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const props = defineProps<{
  open: boolean
  selected?: Array<{ matCode?: string; matName?: string; selfCode?: string; name?: string }>
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: [records: MaterialRecord[]]
}>()

const { t } = useI18n()
const loading = ref(false)
const dataSource = ref<MaterialRecord[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ name: '', selfCode: '' })
const checkedKeys = ref<string[]>([])
const selectedMap = ref<Map<string, MaterialRecord>>(new Map())

const columns = computed(() => [
  { title: t('materialPage.name'), dataIndex: 'name', key: 'name', ellipsis: true },
  { title: t('materialPage.code'), dataIndex: 'selfCode', key: 'selfCode', width: 160, align: 'center' as const },
])

function syncSelectedFromProps() {
  const map = new Map<string, MaterialRecord>()
  ;(props.selected || []).forEach((item) => {
    const code = item.matCode || item.selfCode || ''
    if (!code) {
      return
    }
    map.set(code, {
      id: code,
      name: item.matName || item.name,
      selfCode: code,
    })
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
      const code = row.selfCode || row.id
      if (code && keySet.has(code)) {
        selectedMap.value.set(code, row)
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
    :title="t('devgFlowPage.selectMaterial')"
    width="760px"
    destroy-on-close
    class="production-picker-modal"
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
            class="picker-search-input"
            :placeholder="t('materialPage.name')"
          />
        </a-form-item>
        <a-form-item>
          <a-input
            v-model:value="searchForm.selfCode"
            allow-clear
            size="small"
            class="picker-search-input"
            :placeholder="t('materialPage.code')"
          />
        </a-form-item>
        <CompactSearchActions
          :query-title="t('devgFlowPage.query')"
          :reset-title="t('devgFlowPage.reset')"
          @reset="onReset"
        />
      </a-form>
    </div>
    <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
      <template #default="{ scrollY }">
        <a-table
          row-key="selfCode"
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
          :scroll="{ x: 560, y: scrollY }"
          @change="onTableChange"
        />
      </template>
    </TableScrollWrap>
  </a-modal>
</template>
