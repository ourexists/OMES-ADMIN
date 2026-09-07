<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { CloudDownloadOutlined } from '@ant-design/icons-vue'
import { downloadLineS7, fetchPlcServers } from '@/api/line'
import type { MapOption } from '@/api/line'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  lineId: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()
const loading = ref(false)
const serverOptions = ref<{ value: string; label: string }[]>([])
const serverName = ref<string>()

async function loadServers() {
  const list = await fetchPlcServers()
  serverOptions.value = (list || []).map((item: MapOption) => ({
    value: item.id,
    label: item.name,
  }))
  serverName.value = serverOptions.value[0]?.value
}

async function onSubmit() {
  if (!serverName.value) {
    message.warning(t('linePage.downloadServerRequired'))
    return
  }
  loading.value = true
  try {
    await downloadLineS7(props.lineId, serverName.value)
    message.success(t('linePage.downloadSuccess'))
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
      loadServers()
    }
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :confirm-loading="loading"
    width="420px"
    destroy-on-close
    class="process-form-modal process-form-modal--line"
    @cancel="emit('update:open', false)"
    @ok="onSubmit"
  >
    <template #title>
      <span class="modal-title">
        <span class="modal-title__icon">
          <CloudDownloadOutlined />
        </span>
        {{ t('linePage.downloadTitle') }}
      </span>
    </template>
    <a-form layout="vertical">
      <a-form-item :label="t('linePage.downloadServer')">
        <a-select
          v-model:value="serverName"
          :options="serverOptions"
          :placeholder="t('linePage.downloadServer')"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
