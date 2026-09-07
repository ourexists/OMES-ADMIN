<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeftOutlined, FileTextOutlined, NodeIndexOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { fetchWorkshopTree } from '@/api/device'
import { fetchLineByCode, fetchMoById, findWorkshopByCode } from '@/api/mo'
import type { MoDetailRecord, MoRecord, MoTfRecord } from '@/types/mo'
import { MO_STATUS } from '@/types/mo'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const record = ref<MoRecord | null>(null)
const details = ref<MoDetailRecord[]>([])
const tfList = ref<MoTfRecord[]>([])
const sceneName = ref('')

const moId = computed(() => String(route.query.id || ''))

const detailColumns = computed(() => [
  { title: t('moPage.matName'), dataIndex: 'matName', key: 'matName', ellipsis: true },
  { title: t('moPage.matCode'), dataIndex: 'matCode', key: 'matCode', width: 130 },
  { title: t('moPage.matQty'), dataIndex: 'matNum', key: 'matNum', width: 110 },
  { title: t('moPage.equipCode'), dataIndex: 'devNo', key: 'devNo', width: 120 },
  { title: t('moPage.equipName'), dataIndex: 'devName', key: 'devName', width: 120, ellipsis: true },
  { title: t('moPage.devgCode'), dataIndex: 'dgCode', key: 'dgCode', width: 120 },
  { title: t('moPage.devgName'), dataIndex: 'dgName', key: 'dgName', width: 120, ellipsis: true },
  { title: t('moPage.feedPriority'), dataIndex: 'priority', key: 'priority', width: 90, align: 'center' as const },
])

const tfColumns = computed(() => [
  { title: t('moPage.tfCode'), dataIndex: 'selfCode', key: 'selfCode', width: 130 },
  { title: t('moPage.tfName'), dataIndex: 'name', key: 'name', ellipsis: true },
])

function statusColor(status?: number): string {
  if (status === MO_STATUS.PART) {
    return 'warning'
  }
  if (status === MO_STATUS.RUN) {
    return 'processing'
  }
  if (status === MO_STATUS.COMPLETE) {
    return 'success'
  }
  return 'default'
}

async function loadDetail() {
  if (!moId.value) {
    return
  }
  loading.value = true
  try {
    const data = await fetchMoById(moId.value)
    record.value = data
    details.value = data?.detailDtoList || []
    if (data?.lineCode) {
      const line = await fetchLineByCode(data.lineCode)
      record.value = { ...data, lineName: line?.name }
      tfList.value = line?.tfs || []
    }
    if (data?.tenantId) {
      const tree = await fetchWorkshopTree()
      const node = findWorkshopByCode(tree, data.tenantId)
      sceneName.value = node?.name || data.tenantId
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/view/mo_tables')
}

onMounted(loadDetail)
</script>

<template>
  <div class="admin-page production-detail-page production-detail-page--mo">
    <a-card size="small" class="admin-panel-card panel-card" :loading="loading">
      <template #title>
        <AdminPanelTitle>
          <template #icon><FileTextOutlined /></template>
          {{ t('moPage.detailTitle') }}
          <span v-if="record?.selfCode" class="card-title__badge">[{{ record.selfCode }}]</span>
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-space :size="8">
          <a-button size="small" :loading="loading" @click="loadDetail">
            <ReloadOutlined />
            {{ t('moPage.refresh') }}
          </a-button>
          <a-button size="small" @click="goBack">
            <ArrowLeftOutlined />
            {{ t('moPage.back') }}
          </a-button>
        </a-space>
      </template>

      <div v-if="record" class="meta-header">
        <div class="meta-badge">MO</div>
        <div class="meta-grid">
          <div class="meta-item">
            <span class="meta-label">{{ t('moPage.bomName') }}</span>
            <span class="meta-value">{{ record.productName || '—' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('moPage.bomCode') }}</span>
            <span class="meta-value meta-value--code">{{ record.productCode || '—' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('moPage.status') }}</span>
            <span class="meta-value">
              <a-tag :color="statusColor(record.status)">{{ record.statusDesc || '—' }}</a-tag>
            </span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('moPage.batchQty') }}</span>
            <span class="meta-value">{{ record.num ?? '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('moPage.remainingQty') }}</span>
            <span class="meta-value">{{ record.surplus ?? '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('moPage.scene') }}</span>
            <span class="meta-value">{{ sceneName || record.tenantId || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('moPage.batchWeight') }}</span>
            <span class="meta-value">{{ record.weight ?? '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('moPage.execTime') }}</span>
            <span class="meta-value meta-value--time">{{ record.execTime || '—' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('moPage.lineName') }}</span>
            <span class="meta-value">{{ record.lineName || record.lineCode || '-' }}</span>
          </div>
        </div>
      </div>

      <div class="production-section">
        <div class="production-section__title">
          <FileTextOutlined />
          {{ t('moPage.sectionBomDetail') }}
          <a-tag v-if="details.length" color="processing">{{ details.length }}</a-tag>
        </div>
        <div class="detail-table-wrap">
          <a-table
            row-key="matCode"
            size="small"
            bordered
            class="production-module-table detail-table"
            :columns="detailColumns"
            :data-source="details"
            :pagination="false"
            :scroll="{ x: 980 }"
          >
            <template #bodyCell="{ column, record: row }">
              <template v-if="column.key === 'matCode'">
                <span v-if="row.matCode" class="code-cell">{{ row.matCode }}</span>
                <span v-else class="empty-cell">—</span>
              </template>
              <template v-else-if="column.key === 'devNo'">
                <span v-if="row.devNo" class="code-cell code-cell--muted">{{ row.devNo }}</span>
                <span v-else class="empty-cell">—</span>
              </template>
            </template>
          </a-table>
        </div>
      </div>

      <div class="production-section">
        <div class="production-section__title">
          <NodeIndexOutlined />
          {{ t('moPage.tfSection') }}
          <span v-if="record?.lineName" class="production-section__subtitle">[{{ record.lineName }}]</span>
          <a-tag v-if="tfList.length" color="blue">{{ tfList.length }}</a-tag>
        </div>
        <div class="detail-table-wrap">
          <a-table
            row-key="selfCode"
            size="small"
            bordered
            class="production-module-table detail-table"
            :columns="tfColumns"
            :data-source="tfList"
            :pagination="false"
            :scroll="{ x: 640 }"
          >
            <template #bodyCell="{ column, record: row }">
              <template v-if="column.key === 'selfCode'">
                <span v-if="row.selfCode" class="code-cell">{{ row.selfCode }}</span>
                <span v-else class="empty-cell">—</span>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </a-card>
  </div>
</template>
