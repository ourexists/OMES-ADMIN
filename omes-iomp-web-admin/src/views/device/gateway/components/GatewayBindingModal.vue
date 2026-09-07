<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { GatewayRecord } from '@/api/gateway'
import GatewayBindingPanel from './GatewayBindingPanel.vue'

const props = defineProps<{
  open: boolean
  gateway: GatewayRecord | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const { t } = useI18n()

const gatewayId = computed(() => props.gateway?.id || '')

const title = computed(() => {
  const name = props.gateway?.serverName || props.gateway?.id
  if (name) {
    return t('gatewayPage.bindingTitle', { name })
  }
  return t('gatewayPage.binding')
})

function closeModal() {
  emit('update:open', false)
}
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    width="1080px"
    centered
    destroy-on-close
    class="binding-modal"
    wrap-class-name="binding-modal-wrap"
    :footer="null"
    @cancel="closeModal"
    @update:open="emit('update:open', $event)"
  >
    <GatewayBindingPanel
      v-if="gatewayId && open"
      :gateway-id="gatewayId"
      :gateway-name="gateway?.serverName"
      :gateway="gateway"
    />
  </a-modal>
</template>

<style scoped>
.binding-modal :deep(.ant-modal-content) {
  display: flex;
  flex-direction: column;
  min-height: 560px;
  max-height: calc(100vh - 96px);
}

.binding-modal :deep(.ant-modal-header) {
  flex-shrink: 0;
  border-bottom: 1px solid var(--omes-color-border);
}

.binding-modal :deep(.ant-modal-title) {
  font-weight: 600;
}

.binding-modal :deep(.ant-modal-body) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 16px 20px 20px;
  background: var(--omes-color-bg-layout);
}
</style>

<style>
/* wrap 挂载在 body，需全局样式限制不超屏 */
.binding-modal-wrap.ant-modal-wrap {
  overflow: hidden;
}

.binding-modal-wrap .ant-modal {
  top: 0;
  padding-bottom: 0;
  max-height: calc(100vh - 48px);
}
</style>
