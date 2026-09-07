<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { TeamOutlined, UserAddOutlined } from '@ant-design/icons-vue'
import type { InspectPersonRecord } from '@/api/inspect-person'
import { fetchInspectPersonPage } from '@/api/inspect-person'
import { assignInspectTasks } from '@/api/inspect-task'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  taskIds: string[]
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const dataSource = ref<InspectPersonRecord[]>([])
const selectedPersonId = ref<string>()

const taskCount = computed(() => props.taskIds.length)

const columns = computed(() => [
  { title: t('inspectPersonPage.colName'), dataIndex: 'name', key: 'name', width: 120 },
  { title: t('inspectPersonPage.colJobNumber'), dataIndex: 'jobNumber', key: 'jobNumber', width: 110 },
  { title: t('inspectPersonPage.colMobile'), dataIndex: 'mobile', key: 'mobile', width: 128 },
  { title: t('inspectPersonPage.colAccount'), dataIndex: 'accountName', key: 'accountName', ellipsis: true },
])

async function loadPersons() {
  loading.value = true
  try {
    const result = await fetchInspectPersonPage({ page: 1, pageSize: 200 })
    dataSource.value = result.records || []
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (visible) => {
    if (visible) {
      selectedPersonId.value = undefined
      loadPersons()
    }
  },
)

function closeModal() {
  emit('update:open', false)
}

function onPersonSelect(keys: (string | number)[]) {
  selectedPersonId.value = keys.length ? String(keys[0]) : undefined
}

async function onConfirm() {
  if (!selectedPersonId.value) {
    message.warning(t('inspectTaskPage.assignPersonRequired'))
    return
  }
  saving.value = true
  try {
    await assignInspectTasks(props.taskIds, selectedPersonId.value)
    message.success(t('inspectTaskPage.assignSuccess'))
    emit('success')
    closeModal()
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <a-modal
    :open="open"
    width="720px"
    destroy-on-close
    class="inspect-assign-modal"
    :confirm-loading="saving"
    @cancel="closeModal"
    @ok="onConfirm"
  >
    <template #title>
      <span class="modal-title">
        <UserAddOutlined style="margin-right: 8px; color: var(--omes-color-accent-cyan-from)" />
        {{ t('inspectTaskPage.assignTitle') }}
      </span>
    </template>

    <div class="assign-summary">
      <span class="assign-summary__icon">
        <TeamOutlined />
      </span>
      <span class="assign-summary__text">
        {{ t('inspectTaskPage.assignHint', { count: taskCount }) }}
      </span>
    </div>

    <div class="assign-table-wrap">
      <a-table
        row-key="id"
        size="small"
        :pagination="false"
        :data-source="dataSource"
        :columns="columns"
        :row-selection="{
          type: 'radio',
          selectedRowKeys: selectedPersonId ? [selectedPersonId] : [],
          onChange: onPersonSelect,
        }"
        :scroll="{ y: 360 }"
      />
    </div>
  </a-modal>
</template>
