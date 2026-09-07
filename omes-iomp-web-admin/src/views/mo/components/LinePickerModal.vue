<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { CheckOutlined } from '@ant-design/icons-vue'
import type { LineRecord } from '@/api/mo'
import { fetchLinePage } from '@/api/mo'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{
  'update:open': [value: boolean]
  select: [record: LineRecord]
}>()

const { t } = useI18n()
const loading = ref(false)
const dataSource = ref<LineRecord[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ name: '', selfCode: '' })

const columns = computed(() => [
  { title: t('moPage.lineName'), dataIndex: 'name', key: 'name', ellipsis: true },
  { title: t('moPage.lineCode'), dataIndex: 'selfCode', key: 'selfCode', width: 160, align: 'center' as const },
  { title: t('moPage.pickerSelect'), key: 'action', width: 88, align: 'center' as const, fixed: 'right' as const },
])

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchLinePage({
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

function onSelect(record: LineRecord) {
  emit('select', record)
  emit('update:open', false)
}

function onClose() {
  emit('update:open', false)
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      onReset()
    }
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :title="t('moPage.selectLine')"
    width="760px"
    :footer="null"
    destroy-on-close
    class="production-picker-modal"
    @cancel="onClose"
  >
    <div class="picker-search-toolbar search-toolbar--compact">
      <a-form layout="inline" class="picker-search-form" :model="searchForm" @finish="onSearch">
        <a-form-item>
          <a-input size="small"
            v-model:value="searchForm.name"
            allow-clear
            class="picker-search-input"
            :placeholder="t('moPage.lineName')"
          />
        </a-form-item>
        <a-form-item>
          <a-input size="small"
            v-model:value="searchForm.selfCode"
            allow-clear
            class="picker-search-input"
            :placeholder="t('moPage.lineCode')"
          />
        </a-form-item>
        <CompactSearchActions
              :query-title="t('moPage.query')"
              :reset-title="t('moPage.reset')"
              @reset="onReset"
            />
      </a-form>
    </div>
    <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
      <template #default="{ scrollY }">
        <a-table
          row-key="id"
          size="middle"
          bordered
          class="production-module-table"
          :loading="loading"
          :columns="columns"
          :data-source="dataSource"
          :pagination="{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showTotal: (total: number) => t('moPage.paginationTotal', { total }),
          }"
          :scroll="{ x: 560, y: scrollY }"
          @change="onTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a-button type="link" size="small" @click="onSelect(record as LineRecord)">
                <CheckOutlined />
                {{ t('moPage.pickerSelect') }}
              </a-button>
            </template>
          </template>
        </a-table>
      </template>
    </TableScrollWrap>
  </a-modal>
</template>
