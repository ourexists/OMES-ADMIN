<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ApiOutlined, GatewayOutlined, SwapOutlined } from '@ant-design/icons-vue'
import { fetchGatewayList, type GatewayRecord } from '@/api/gateway'

defineProps<{
  gateway: GatewayRecord | null
  /** 与映射说明等同排展示时使用 */
  compact?: boolean
}>()

const emit = defineEmits<{
  select: [gateway: GatewayRecord]
}>()

const { t } = useI18n()
const pickerOpen = ref(false)
const loading = ref(false)
const gateways = ref<GatewayRecord[]>([])

async function openPicker() {
  pickerOpen.value = true
  loading.value = true
  try {
    gateways.value = (await fetchGatewayList()) || []
  } finally {
    loading.value = false
  }
}

function choose(gw: GatewayRecord) {
  emit('select', gw)
  pickerOpen.value = false
}
</script>

<template>
  <div class="gateway-picker">
    <div
      v-if="gateway"
      class="selected-card"
      :class="{ 'selected-card--compact': compact }"
      role="button"
      tabindex="0"
      @click="openPicker"
      @keyup.enter="openPicker"
    >
      <div class="selected-card__icon">
        <GatewayOutlined />
      </div>
      <div class="selected-card__main">
        <div class="selected-card__name">{{ gateway.serverName }}</div>
        <div class="selected-card__row">
          <ApiOutlined />
          <span :title="gateway.uri">{{ gateway.uri || '-' }}</span>
        </div>
        <a-tag class="selected-card__tag">{{ gateway.protocol || '-' }}</a-tag>
      </div>
      <a-button type="link" size="small" class="change-btn" @click.stop="openPicker">
        <SwapOutlined />
        {{ t('equipAttrPage.changeGateway') }}
      </a-button>
    </div>

    <a-button v-else type="dashed" :block="!compact" class="pick-trigger" :class="{ 'pick-trigger--compact': compact }" @click="openPicker">
      <GatewayOutlined />
      {{ t('equipAttrPage.selectGateway') }}
    </a-button>

    <a-modal
      v-model:open="pickerOpen"
      :title="t('equipAttrPage.selectGateway')"
      :footer="null"
      width="560px"
      class="picker-modal"
    >
      <a-spin :spinning="loading">
        <a-empty v-if="!gateways.length" />
        <div v-else class="pick-list">
          <button
            v-for="item in gateways"
            :key="item.id"
            type="button"
            class="pick-item"
            @click="choose(item)"
          >
            <span class="pick-item__name">{{ item.serverName }}</span>
            <span class="pick-item__meta">{{ item.protocol }} · {{ item.uri }}</span>
          </button>
        </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<style scoped>
.gateway-picker {
  margin-bottom: 12px;
}

.gateway-picker:has(.selected-card--compact),
.gateway-picker:has(.pick-trigger--compact) {
  margin-bottom: 0;
}

.selected-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid var(--omes-color-primary-border);
  border-radius: var(--omes-radius-lg);
  background: linear-gradient(135deg, #f0f5ff 0%, #fff 60%);
  cursor: pointer;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
}

.selected-card:hover {
  border-color: #91caff;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.12);
}

.selected-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  font-size: 22px;
  color: var(--omes-color-primary);
  background: var(--omes-color-bg-container);
  border-radius: var(--omes-radius-lg);
  flex-shrink: 0;
}

.selected-card__main {
  flex: 1;
  min-width: 0;
}

.selected-card__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--omes-color-text);
  margin-bottom: 4px;
}

.selected-card__row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--omes-color-text-tertiary);
  margin-bottom: 6px;
}

.selected-card__row span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-card__tag {
  margin: 0;
}

.change-btn {
  flex-shrink: 0;
}

.pick-trigger {
  height: 72px;
}

.pick-trigger--compact {
  height: 40px;
}

.selected-card--compact {
  padding: 8px 12px;
  gap: 10px;
  min-height: 40px;
}

.selected-card--compact .selected-card__icon {
  width: 32px;
  height: 32px;
  font-size: 16px;
  border-radius: var(--omes-radius-md);
}

.selected-card--compact .selected-card__main {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  column-gap: 10px;
  row-gap: 2px;
}

.selected-card--compact .selected-card__name {
  margin: 0;
  font-size: 14px;
  line-height: 1.3;
}

.selected-card--compact .selected-card__row {
  margin: 0;
  display: inline-flex;
  max-width: min(240px, 36vw);
}

.selected-card--compact .selected-card__tag {
  margin: 0;
  line-height: 18px;
}

.selected-card--compact .change-btn {
  padding-inline: 4px;
  font-size: 12px;
}

.pick-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.pick-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  width: 100%;
  padding: 12px 14px;
  text-align: left;
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-md);
  background: var(--omes-color-bg-container);
  cursor: pointer;
  transition:
    border-color 0.2s,
    background 0.2s;
}

.pick-item:hover {
  border-color: #91caff;
  background: var(--omes-color-primary-bg-hover);
}

.pick-item__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--omes-color-text);
}

.pick-item__meta {
  font-size: 12px;
  color: var(--omes-color-text-tertiary);
}
</style>
