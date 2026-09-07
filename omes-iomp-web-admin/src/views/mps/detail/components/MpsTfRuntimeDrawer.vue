<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { PlayCircleOutlined, SettingOutlined } from '@ant-design/icons-vue'
import type { MpsRuntimeTf } from '@/types/mps'
import { joinTfResourceNames } from '@/composables/lineTfFlowShared'

const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  tf: MpsRuntimeTf | null
  canStart: boolean
  engineConfigured: boolean
}>()

const emit = defineEmits<{
  start: []
  viewEngine: []
}>()

const { t } = useI18n()

const title = computed(() => {
  if (!props.tf) {
    return t('mpsPage.tfNodeDetail')
  }
  const name = props.tf.name || props.tf.selfCode || props.tf.nodeId
  if (props.tf.stepNo != null && props.tf.stepNo > 0) {
    return `${props.tf.stepNo} · ${name}`
  }
  return name
})

const equipmentText = computed(
  () => joinTfResourceNames(props.tf?.equipments, 'equipmentName', 'equipmentCode') || '—',
)
const toolingText = computed(
  () => joinTfResourceNames(props.tf?.toolings, 'toolingName', 'toolingCode') || '—',
)
</script>

<template>
  <a-drawer
    v-model:open="open"
    :title="title"
    width="420"
    destroy-on-close
  >
    <template v-if="tf">
      <a-descriptions size="small" :column="1" bordered>
        <a-descriptions-item :label="t('moPage.tfCode')">{{ tf.selfCode || '—' }}</a-descriptions-item>
        <a-descriptions-item :label="t('mpsPage.tfStatus')">{{ tf.statusDesc || '—' }}</a-descriptions-item>
        <a-descriptions-item :label="t('mpsPage.tfStartTime')">{{ tf.startTime || '—' }}</a-descriptions-item>
        <a-descriptions-item :label="t('mpsPage.tfEndTime')">{{ tf.endTime || '—' }}</a-descriptions-item>
        <a-descriptions-item :label="t('mpsPage.tfStartTemp')">{{ tf.startTemperature ?? '—' }}</a-descriptions-item>
        <a-descriptions-item :label="t('mpsPage.tfEndTemp')">{{ tf.endTemperature ?? '—' }}</a-descriptions-item>
        <a-descriptions-item :label="t('lineFlowPage.tfEquipments')">{{ equipmentText }}</a-descriptions-item>
        <a-descriptions-item :label="t('lineFlowPage.tfToolings')">{{ toolingText }}</a-descriptions-item>
      </a-descriptions>

      <div class="mps-tf-drawer__card">
        <div class="mps-tf-drawer__card-title">{{ t('mpsPage.tfCardSection') }}</div>
        <pre v-if="tf.stepContent" class="mps-tf-drawer__content">{{ tf.stepContent }}</pre>
        <a-empty v-else :description="t('mpsPage.tfCardEmpty')" />
      </div>

      <a-space class="mps-tf-drawer__actions" wrap>
        <a-button v-if="canStart" type="primary" @click="emit('start')">
          <PlayCircleOutlined />
          {{ t('mpsPage.tfStart') }}
        </a-button>
        <a-button v-if="engineConfigured" @click="emit('viewEngine')">
          <SettingOutlined />
          {{ t('mpsPage.tfEngineView') }}
        </a-button>
      </a-space>
    </template>
  </a-drawer>
</template>
