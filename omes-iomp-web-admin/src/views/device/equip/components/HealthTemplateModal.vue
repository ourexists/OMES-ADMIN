<script setup lang="ts">
import { ref, watch } from 'vue'
import type { EquipRecord, HealthTemplate } from '@/api/device'
import { fetchEquipById, saveEquip } from '@/api/device'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  equip: EquipRecord | null
  templates: HealthTemplate[]
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const loading = ref(false)
const templateId = ref<string>()

watch(
  () => props.open,
  async (open) => {
    if (!open || !props.equip?.id) {
      return
    }
    loading.value = true
    try {
      const detail = await fetchEquipById(props.equip.id)
      templateId.value = detail.healthTemplateId || undefined
    } finally {
      loading.value = false
    }
  },
)

async function handleSubmit() {
  if (!props.equip?.id) {
    return
  }
  loading.value = true
  try {
    const detail = await fetchEquipById(props.equip.id)
    detail.healthTemplateId = templateId.value || null
    await saveEquip(detail)
    message.success('关联成功')
    emit('update:open', false)
    emit('success')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <a-modal
    :open="open"
    title="关联健康模板"
    width="480px"
    :confirm-loading="loading"
    destroy-on-close
    @update:open="emit('update:open', $event)"
    @ok="handleSubmit"
  >
    <a-form layout="vertical">
      <a-form-item label="健康模板">
        <a-select
          v-model:value="templateId"
          allow-clear
          placeholder="未关联"
          :options="templates.map((item) => ({ value: item.id, label: item.name || item.id }))"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
