<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ClockCircleOutlined } from '@ant-design/icons-vue'
import { fetchTaskById, fetchTimerTaskTypes, saveTask } from '@/api/task'
import type { TaskRecord } from '@/types/task'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: TaskRecord | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const submitting = ref(false)
const typeOptions = ref<{ value: string; label: string }[]>([])

const isEdit = computed(() => Boolean(props.record?.id))
const title = computed(() => (isEdit.value ? t('taskPage.formEdit') : t('taskPage.formAdd')))

const formState = reactive({
  name: '',
  cron: '',
  type: undefined as string | undefined,
})

async function loadTypeOptions() {
  const list = await fetchTimerTaskTypes()
  typeOptions.value = (list || []).map((item) => ({
    value: item.id || item.selfCode || item.name,
    label: item.name || item.id,
  }))
}

watch(
  () => props.open,
  async (open) => {
    if (!open) {
      return
    }
    await loadTypeOptions()
    if (props.record?.id) {
      const detail = await fetchTaskById(props.record.id)
      formState.name = detail?.name || ''
      formState.cron = detail?.cron || ''
      formState.type = detail?.type
    } else {
      formState.name = ''
      formState.cron = ''
      formState.type = undefined
    }
  },
)

async function handleSubmit() {
  if (!formState.name?.trim()) {
    message.warning(t('taskPage.nameRequired'))
    return
  }
  if (!formState.cron?.trim()) {
    message.warning(t('taskPage.cronRequired'))
    return
  }
  if (!formState.type) {
    message.warning(t('taskPage.typeRequired'))
    return
  }

  submitting.value = true
  try {
    await saveTask({
      id: props.record?.id,
      name: formState.name.trim(),
      cron: formState.cron.trim(),
      type: formState.type,
    })
    message.success(t('taskPage.saveSuccess'))
    emit('update:open', false)
    emit('success')
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  emit('update:open', false)
}
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    width="520px"
    class="system-form-modal"
    :confirm-loading="submitting"
    destroy-on-close
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <template #title>
      <span class="modal-title">
        <span class="modal-title__icon modal-title__icon--task">
          <ClockCircleOutlined />
        </span>
        {{ title }}
      </span>
    </template>

    <a-form layout="vertical" class="system-form">
      <div class="system-form-section">
        <div class="system-form-section__title">
          <ClockCircleOutlined />
          {{ t('taskPage.formBasic') }}
        </div>
        <a-form-item :label="t('taskPage.colName')" required>
          <a-input
            v-model:value="formState.name"
            allow-clear
            :placeholder="t('taskPage.namePlaceholder')"
          />
        </a-form-item>
        <a-form-item :label="t('taskPage.colCron')" required>
          <a-input
            v-model:value="formState.cron"
            allow-clear
            :placeholder="t('taskPage.cronPlaceholder')"
          />
          <p class="system-form-hint">{{ t('taskPage.cronHint') }}</p>
        </a-form-item>
        <a-form-item :label="t('taskPage.colType')" required>
          <a-select
            v-model:value="formState.type"
            allow-clear
            :disabled="isEdit"
            :placeholder="t('taskPage.typePlaceholder')"
            :options="typeOptions"
          />
        </a-form-item>
      </div>
    </a-form>
  </a-modal>
</template>
