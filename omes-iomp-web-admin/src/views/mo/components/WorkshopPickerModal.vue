<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { WorkshopNode } from '@/api/device'
import WorkshopTree from '@/components/WorkshopTree.vue'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{
  'update:open': [value: boolean]
  select: [node: WorkshopNode]
}>()

const { t } = useI18n()
const selected = ref<WorkshopNode | null>(null)

function onConfirm() {
  if (!selected.value) {
    return
  }
  emit('select', selected.value)
  emit('update:open', false)
}

function onClose() {
  emit('update:open', false)
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      selected.value = null
    }
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :title="t('moPage.selectScene')"
    width="520px"
    class="production-picker-modal"
    :ok-text="t('moPage.pickerConfirm')"
    :cancel-text="t('moPage.cancel')"
    :ok-button-props="{ disabled: !selected }"
    destroy-on-close
    @ok="onConfirm"
    @cancel="onClose"
  >
    <WorkshopTree v-model="selected" embedded :tree-height="360" />
  </a-modal>
</template>
