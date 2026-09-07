<script setup lang="ts">
import { reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { PlatformNode } from '@/api/ucenter'
import { savePlatform } from '@/api/ucenter'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: PlatformNode | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: [code: string]
}>()

const { t } = useI18n()

const formState = reactive({
  code: '',
  name: '',
})

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    formState.code = props.record?.code || ''
    formState.name = props.record?.name || ''
  },
)

async function handleSubmit() {
  if (!props.record?.id) {
    return
  }
  if (!formState.name.trim()) {
    message.warning(t('permissionPage.platformFormRequired'))
    return
  }

  await savePlatform({
    ...props.record,
    code: formState.code.trim(),
    name: formState.name.trim(),
  })

  message.success(t('permissionPage.saveSuccess'))
  emit('update:open', false)
  emit('success', formState.code.trim())
}
</script>

<template>
  <a-modal
    :open="open"
    :title="t('permissionPage.platformFormEdit')"
    width="480px"
    destroy-on-close
    @update:open="emit('update:open', $event)"
    @ok="handleSubmit"
  >
    <a-form layout="vertical">
      <a-form-item :label="t('permissionPage.formCode')" required>
        <a-input v-model:value="formState.code" disabled />
      </a-form-item>
      <a-form-item :label="t('permissionPage.formName')" required>
        <a-input v-model:value="formState.name" :placeholder="t('permissionPage.platformNamePlaceholder')" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
