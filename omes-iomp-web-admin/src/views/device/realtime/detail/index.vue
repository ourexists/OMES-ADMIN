<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { fetchEquipDetailRealtime } from '@/api/equip-detail'
import type { EquipRecord } from '@/api/device'
import DetailHeader from './components/DetailHeader.vue'
import DetailInfoTab from './components/DetailInfoTab.vue'
import DetailRealtimeTab from './components/DetailRealtimeTab.vue'
import DetailLogTab from './components/DetailLogTab.vue'

const POLL_MS = 5000

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const equipId = computed(() => String(route.query.id || ''))
const equip = ref<EquipRecord | null>(null)
const loading = ref(false)
const activeTab = ref('info')

let pollTimer: ReturnType<typeof setInterval> | null = null

async function loadEquip(options: { silent?: boolean; notify?: boolean } = {}) {
  if (!equipId.value) {
    message.error(t('equipDetailPage.missingId'))
    return
  }
  if (!options.silent) {
    loading.value = true
  }
  try {
    equip.value = await fetchEquipDetailRealtime(equipId.value)
    if (options.notify) {
      message.success(t('equipDetailPage.syncOk'), 1)
    }
  } finally {
    if (!options.silent) {
      loading.value = false
    }
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/view/equip_realtime')
}

function startPolling() {
  stopPolling()
  pollTimer = setInterval(() => {
    if (document.hidden || loading.value) {
      return
    }
    void loadEquip({ silent: true })
  }, POLL_MS)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function onVisibilityChange() {
  if (!document.hidden) {
    void loadEquip({ silent: true })
  }
}

watch(equipId, () => {
  void loadEquip()
})

onMounted(() => {
  void loadEquip()
  startPolling()
  document.addEventListener('visibilitychange', onVisibilityChange)
})

onUnmounted(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', onVisibilityChange)
})
</script>

<template>
  <div class="admin-page equip-detail-page">
    <DetailHeader
      :equip="equip"
      @refresh="loadEquip({ silent: true, notify: true })"
      @back="goBack"
    />

    <a-card size="small" class="detail-tabs-card admin-panel-card">
      <a-tabs v-model:active-key="activeTab" class="detail-tabs">
        <a-tab-pane key="info" :tab="t('equipDetailPage.tabInfo')">
          <DetailInfoTab
            :equip="equip"
            :equip-id="equipId"
            @refreshed="loadEquip({ silent: true })"
          />
        </a-tab-pane>
        <a-tab-pane key="realtime" :tab="t('equipDetailPage.tabRealtime')">
          <DetailRealtimeTab :equip="equip" :active="activeTab === 'realtime'" />
        </a-tab-pane>
        <a-tab-pane key="invoke" :tab="t('equipDetailPage.tabInvoke')">
          <a-empty :description="t('equipDetailPage.invokePlaceholder')" />
        </a-tab-pane>
        <a-tab-pane key="log" :tab="t('equipDetailPage.tabLog')">
          <DetailLogTab :equip="equip" :active="activeTab === 'log'" />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<style scoped>
.equip-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.equip-detail-page > :first-child {
  flex-shrink: 0;
}

.detail-tabs-card {
  border-radius: 14px;
  border: 1px solid #e8eef5;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04);
}

.detail-tabs-card :deep(.ant-card-body) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding-top: 8px;
}

.detail-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.detail-tabs :deep(.ant-tabs-nav) {
  flex-shrink: 0;
}

.detail-tabs :deep(.ant-tabs-content-holder) {
  flex: 1;
  min-height: 0;
}

.detail-tabs :deep(.ant-tabs-content) {
  height: 100%;
}

.detail-tabs :deep(.ant-tabs-tabpane) {
  height: 100%;
}
</style>
