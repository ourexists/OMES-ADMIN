<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeftOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import InspectRecordDetail from '../components/InspectRecordDetail.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const recordId = computed(() => {
  const value = route.query.id
  return typeof value === 'string' && value ? value : null
})

function backList() {
  const taskId = route.query.taskId
  if (typeof taskId === 'string' && taskId) {
    router.push({ path: '/view/inspect_record_tables', query: { taskId } })
    return
  }
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/view/inspect_record_tables')
}
</script>

<template>
  <div class="admin-page inspect-record-detail-page">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle>
          <template #icon><FileTextOutlined /></template>
          {{ t('inspectRecordPage.detailTitle') }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-button type="link" @click="backList">
          <ArrowLeftOutlined />
          {{ t('inspectRecordPage.backList') }}
        </a-button>
      </template>

      <InspectRecordDetail :record-id="recordId" />
    </a-card>
  </div>
</template>

<style scoped>
.inspect-record-detail-page :deep(.ant-card-body) {
  padding-top: 16px;
}

.panel-card {
  border-radius: var(--omes-radius-md);
  box-shadow: var(--omes-shadow-card-sm);
}

.panel-card :deep(.ant-card-head) {
  min-height: 48px;
  border-bottom: 1px solid var(--omes-color-border);
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
</style>
