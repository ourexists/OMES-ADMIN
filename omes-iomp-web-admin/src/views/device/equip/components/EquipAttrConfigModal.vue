<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { AppstoreOutlined } from '@ant-design/icons-vue'
import type { EquipRecord } from '@/api/device'
import { fetchEquipConfig, saveEquipConfig } from '@/api/device'
import type { GatewayRecord } from '@/api/gateway'
import { fetchGatewayById } from '@/api/gateway'
import GatewayPickerCard from '@/components/device/GatewayPickerCard.vue'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  equip: EquipRecord | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const selectedGateway = ref<GatewayRecord | null>(null)

const hasProduct = computed(() => Boolean(String(props.equip?.type || '').trim()))
const hasModel = computed(() => Boolean(String(props.equip?.modelId || '').trim()))

const productHint = computed(() => {
  if (!hasProduct.value) {
    return t('productPage.noProductHint')
  }
  if (!hasModel.value) {
    return t('productPage.noModelHint')
  }
  return t('productPage.fromModelHint', {
    name: props.equip?.modelName || props.equip?.modelId,
    product: props.equip?.typeDesc || props.equip?.type,
  })
})

const title = computed(() =>
  props.equip?.name
    ? t('equipAttrPage.title', { name: props.equip.name })
    : t('equipAttrPage.titleDefault'),
)

async function loadConfig() {
  if (!props.equip?.id) {
    return
  }
  loading.value = true
  try {
    const binding = await fetchEquipConfig(props.equip.id)
    const gwId = binding?.gwId
    if (gwId) {
      try {
        selectedGateway.value = await fetchGatewayById(gwId)
      } catch {
        selectedGateway.value = { id: gwId }
      }
    } else {
      selectedGateway.value = null
    }
  } finally {
    loading.value = false
  }
}

function onGatewaySelect(gw: GatewayRecord) {
  selectedGateway.value = gw
}

function handleClose() {
  emit('update:open', false)
}

async function handleSave() {
  if (!props.equip?.id) {
    return
  }
  if (!selectedGateway.value?.id) {
    message.warning(t('equipAttrPage.gatewayRequired'))
    return
  }
  saving.value = true
  try {
    await saveEquipConfig({
      equipId: props.equip.id,
      gwId: selectedGateway.value.id,
    })
    message.success(t('equipAttrPage.saveSuccess'))
    emit('update:open', false)
    emit('success')
  } finally {
    saving.value = false
  }
}

watch(
  () => [props.open, props.equip?.id] as const,
  async ([open, equipId]) => {
    if (!open || !equipId) {
      return
    }
    await loadConfig()
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    width="520px"
    centered
    destroy-on-close
    class="equip-gateway-modal"
    :mask-closable="false"
    :confirm-loading="saving"
    @update:open="emit('update:open', $event)"
    @cancel="handleClose"
    @ok="handleSave"
  >
    <a-spin :spinning="loading">
      <a-alert
        class="gateway-hint"
        :type="hasModel ? 'info' : 'warning'"
        show-icon
        :message="productHint"
      />
      <div v-if="equip?.typeDesc || equip?.modelName" class="gateway-meta">
        <a-tag v-if="equip?.typeDesc" color="processing">
          <AppstoreOutlined />
          {{ equip.typeDesc }}
        </a-tag>
        <a-tag v-if="equip?.modelName" color="cyan">{{ equip.modelName }}</a-tag>
      </div>
      <GatewayPickerCard :gateway="selectedGateway" @select="onGatewaySelect" />
    </a-spin>
  </a-modal>
</template>

<style scoped>
.equip-gateway-modal :deep(.ant-modal-content) {
  border-radius: 12px;
}

.equip-gateway-modal :deep(.ant-modal-header) {
  padding: 16px 24px;
}

.equip-gateway-modal :deep(.ant-modal-title) {
  font-size: 17px;
}

.equip-gateway-modal :deep(.ant-modal-body) {
  padding: 16px 24px 8px;
}

.equip-gateway-modal :deep(.ant-modal-footer) {
  padding: 12px 24px 16px;
}

.equip-gateway-modal :deep(.ant-modal-footer .ant-btn) {
  min-width: 80px;
  height: 36px;
}

.gateway-hint {
  margin-bottom: 12px;
}

.gateway-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.gateway-meta :deep(.ant-tag) {
  margin: 0;
  border-radius: 999px;
}
</style>
