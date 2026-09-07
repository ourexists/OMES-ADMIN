<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import InspectRecordDetail from './InspectRecordDetail.vue'

const props = defineProps<{
  open: boolean
  recordId: string | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const { t } = useI18n()

const title = computed(() => t('inspectRecordPage.detailTitle'))

function closeModal() {
  emit('update:open', false)
}
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    :width="960"
    destroy-on-close
    :footer="null"
    class="inspect-record-detail-modal"
    @cancel="closeModal"
  >
    <InspectRecordDetail :record-id="recordId" compact />
  </a-modal>
</template>

<style scoped>
.inspect-record-detail-modal :deep(.ant-modal-body) {
  max-height: min(72vh, 760px);
  overflow-y: auto;
  padding-top: 12px;
}
</style>
