<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { InspectItemRecord } from '@/api/inspect-item'
import { fetchInspectItemPool, inspectItemTypeLabel } from '@/api/inspect-item'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: [items: InspectItemRecord[]]
}>()

const { t } = useI18n()

const loading = ref(false)
const poolItems = ref<InspectItemRecord[]>([])
const selectedIds = ref<string[]>([])

const columns = computed(() => [
  { title: t('inspectTemplatePage.colItemName'), dataIndex: 'itemName', key: 'itemName', ellipsis: true },
  { title: t('inspectTemplatePage.colItemType'), key: 'itemType', width: 90 },
  { title: t('inspectTemplatePage.colUnit'), dataIndex: 'unit', key: 'unit', width: 120, ellipsis: true },
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedIds.value,
  onChange: (keys: string[]) => {
    selectedIds.value = keys
  },
}))

async function loadPool() {
  loading.value = true
  try {
    poolItems.value = (await fetchInspectItemPool()) || []
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      selectedIds.value = []
      loadPool()
    }
  },
)

function closeModal() {
  emit('update:open', false)
}

function onConfirm() {
  if (selectedIds.value.length === 0) {
    message.warning(t('inspectTemplatePage.loadItemsSelect'))
    return
  }
  const selected = poolItems.value.filter((item) => selectedIds.value.includes(item.id))
  emit('confirm', selected)
  closeModal()
}
</script>

<template>
  <a-modal
    :open="open"
    :title="t('inspectTemplatePage.loadItemsTitle')"
    width="640px"
    destroy-on-close
    :ok-text="t('inspectTemplatePage.confirm')"
    :cancel-text="t('inspectTemplatePage.cancel')"
    @update:open="emit('update:open', $event)"
    @ok="onConfirm"
  >
    <a-spin :spinning="loading">
      <a-table
        row-key="id"
        size="small"
        bordered
        :columns="columns"
        :data-source="poolItems"
        :row-selection="rowSelection"
        :pagination="{ pageSize: 8, showSizeChanger: false }"
        :scroll="{ y: 320 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'itemType'">
            {{ inspectItemTypeLabel(record.itemType) }}
          </template>
        </template>
        <template #emptyText>
          <a-empty :description="t('inspectTemplatePage.loadItemsEmpty')" />
        </template>
      </a-table>
    </a-spin>
  </a-modal>
</template>
