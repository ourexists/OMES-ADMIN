<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { EditOutlined, FileTextOutlined, ReloadOutlined, RollbackOutlined } from '@ant-design/icons-vue'
import type { InspectTemplateRecord } from '@/api/inspect-template'

const props = defineProps<{
  template: InspectTemplateRecord | null
  loading?: boolean
  productBlockCount?: number
  itemCount?: number
}>()

const emit = defineEmits<{
  refresh: []
  back: []
  edit: []
}>()

const { t } = useI18n()

const metaLine = computed(() => {
  const parts: string[] = []
  if (props.template?.createdTime) {
    parts.push(`${t('inspectTemplateDetailPage.createdTime')}: ${props.template.createdTime}`)
  }
  if (props.productBlockCount != null) {
    parts.push(t('inspectTemplateDetailPage.productCount', { count: props.productBlockCount }))
  }
  if (props.itemCount != null) {
    parts.push(t('inspectTemplateDetailPage.itemCount', { count: props.itemCount }))
  }
  return parts.join(' · ')
})
</script>

<template>
  <div class="detail-header">
    <div class="detail-overview">
      <div class="detail-icon">
        <FileTextOutlined />
      </div>
      <div class="detail-main">
        <div class="detail-name-row">
          <h1 class="detail-name">{{ template?.name || '-' }}</h1>
        </div>
        <p v-if="template?.remark" class="detail-remark">{{ template.remark }}</p>
        <p class="detail-meta">{{ metaLine || '-' }}</p>
      </div>
    </div>
    <a-space wrap>
      <a-button :loading="loading" @click="emit('refresh')">
        <ReloadOutlined />
        {{ t('inspectTemplateDetailPage.refresh') }}
      </a-button>
      <a-button @click="emit('edit')">
        <EditOutlined />
        {{ t('inspectTemplateDetailPage.edit') }}
      </a-button>
      <a-button type="primary" ghost @click="emit('back')">
        <RollbackOutlined />
        {{ t('inspectTemplateDetailPage.backList') }}
      </a-button>
    </a-space>
  </div>
</template>

<style scoped>
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background: var(--omes-color-bg-container);
  border: 1px solid #e8eef5;
  border-radius: 14px;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04);
}

.detail-overview {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.detail-icon {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
  color: var(--omes-color-primary);
  background: linear-gradient(135deg, var(--omes-color-primary-bg) 0%, #f0f7ff 100%);
  border: 1px solid #bae0ff;
}

.detail-main {
  min-width: 0;
}

.detail-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-name {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.detail-remark {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--omes-color-text-secondary);
  line-height: 1.5;
}

.detail-meta {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--omes-color-text-quaternary);
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
