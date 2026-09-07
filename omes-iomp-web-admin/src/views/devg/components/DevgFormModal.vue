<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ClusterOutlined } from '@ant-design/icons-vue'
import { saveDevg } from '@/api/devg'
import type { DevgRecord } from '@/types/devg'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: DevgRecord | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()
const loading = ref(false)

const formState = reactive({
  id: '' as string | undefined,
  name: '',
  selfCode: '',
})

const isEdit = computed(() => Boolean(props.record?.id))
const title = computed(() => (isEdit.value ? t('devgPage.formEdit') : t('devgPage.formAdd')))

function resetForm() {
  if (props.record) {
    formState.id = props.record.id
    formState.name = props.record.name || ''
    formState.selfCode = props.record.selfCode || ''
  } else {
    formState.id = undefined
    formState.name = ''
    formState.selfCode = ''
  }
}

async function onSubmit() {
  if (!formState.name.trim()) {
    message.warning(t('devgPage.nameRequired'))
    return
  }
  if (!formState.selfCode.trim()) {
    message.warning(t('devgPage.codeRequired'))
    return
  }
  loading.value = true
  try {
    await saveDevg({
      id: formState.id,
      name: formState.name.trim(),
      selfCode: formState.selfCode.trim(),
    })
    message.success(t('devgPage.saveSuccess'))
    emit('success')
    emit('update:open', false)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      resetForm()
    }
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :confirm-loading="loading"
    width="480px"
    destroy-on-close
    class="process-form-modal process-form-modal--devg"
    @cancel="emit('update:open', false)"
    @ok="onSubmit"
  >
    <template #title>
      <span class="modal-title">
        <span class="modal-title__icon">
          <ClusterOutlined />
        </span>
        {{ title }}
      </span>
    </template>
    <a-form layout="vertical" class="system-form">
      <a-form-item :label="t('devgPage.name')" required>
        <a-input v-model:value="formState.name" allow-clear />
      </a-form-item>
      <a-form-item :label="t('devgPage.code')" required>
        <a-input v-model:value="formState.selfCode" :disabled="isEdit" allow-clear />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
