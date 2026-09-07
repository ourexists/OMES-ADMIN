<script setup lang="ts">
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ArrowDownOutlined,
  ArrowLeftOutlined,
  ArrowRightOutlined,
  ArrowUpOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  EyeOutlined,
  InboxOutlined,
  NodeIndexOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  VerticalAlignTopOutlined,
} from '@ant-design/icons-vue'
import {
  changeMpsPriority,
  fetchMpsBoard,
  joinMpsQueue,
  jumpMpsQueue,
  removeMpsQueue,
} from '@/api/mps'
import type { MpsRecord } from '@/types/mps'
import MoAdjustModal, { type MoAdjustType } from '@/views/mo/components/MoAdjustModal.vue'
import MpsBoardCard from './components/MpsBoardCard.vue'
import { message, Modal } from 'ant-design-vue'
import draggable from 'vuedraggable'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()

const loading = ref(false)
const saving = ref(false)
const pendingList = ref<MpsRecord[]>([])
const queueList = ref<MpsRecord[]>([])
const runningList = ref<MpsRecord[]>([])
const doneList = ref<MpsRecord[]>([])

const adjustOpen = ref(false)
const adjustType = ref<MoAdjustType | null>(null)
const adjustMps = ref<MpsRecord | null>(null)
const adjustMpsIds = ref<string[] | undefined>(undefined)

const hasDoneColumnItems = computed(
  () => runningList.value.length > 0 || doneList.value.length > 0,
)

function queueItemTone(index: number): 'queue-first' | 'queue' {
  return index === 0 ? 'queue-first' : 'queue'
}

function queueStatusLabel(index: number): string {
  return index === 0 ? t('mpsPage.queueNext') : t('mpsPage.queueWaiting')
}

const searchForm = reactive({
  moCode: '',
  productName: '',
  productCode: '',
})

function baseQuery() {
  return {
    page: 1,
    pageSize: 300,
    requirePage: false,
    moCode: searchForm.moCode.trim() || undefined,
    productName: searchForm.productName.trim() || undefined,
    productCode: searchForm.productCode.trim() || undefined,
  }
}

function applyRouteMoCode() {
  const raw = route.query.moCode
  const value = Array.isArray(raw) ? raw[0] : raw
  if (value) {
    searchForm.moCode = String(value)
  }
}

async function loadBoard() {
  loading.value = true
  try {
    const board = await fetchMpsBoard(baseQuery())
    pendingList.value = board?.waitQue || []
    queueList.value = board?.waitExec || []
    runningList.value = board?.execing || []
    doneList.value = board?.complete || []
  } finally {
    loading.value = false
  }
}

function onSearch() {
  loadBoard()
}

function onReset() {
  searchForm.moCode = ''
  searchForm.productName = ''
  searchForm.productCode = ''
  loadBoard()
}

function openDetail(record: MpsRecord) {
  router.push({ path: '/view/mps_form_edit', query: { id: record.id } })
}

async function handleJoinQueue(id: string) {
  await joinMpsQueue(id)
  message.success(t('mpsPage.joinSuccess'))
  await loadBoard()
}

function openAdjust(record: MpsRecord, type: MoAdjustType, mpsIds?: string[]) {
  if (!record.moCode) {
    message.warning(t('mpsPage.selectOne'))
    return
  }
  adjustMps.value = record
  adjustType.value = type
  adjustMpsIds.value = mpsIds
  adjustOpen.value = true
}

function openCancel(record: MpsRecord) {
  openAdjust(record, 'CANCEL_MPS', [record.id])
}

function onQueueAdjustMenu(record: MpsRecord, key: string | number) {
  const k = String(key)
  if (k === 'CANCEL_MPS') {
    openCancel(record)
    return
  }
  openAdjust(record, k as MoAdjustType)
}

async function persistOrderAtIndex(newIndex: number) {
  const next = queueList.value
  const currentId = next[newIndex]?.id
  if (!currentId) {
    return
  }
  await changeMpsPriority({
    pre: next[newIndex - 1]?.id ?? null,
    post: next[newIndex + 1]?.id ?? null,
    current: currentId,
  })
}

async function reorderQueue(currentId: string, newIndex: number) {
  const list = queueList.value
  const oldIndex = list.findIndex((item) => item.id === currentId)
  if (oldIndex < 0 || oldIndex === newIndex) {
    return
  }
  const next = [...list]
  const [item] = next.splice(oldIndex, 1)
  next.splice(newIndex, 0, item)
  queueList.value = next
  await persistOrderAtIndex(newIndex)
  await loadBoard()
}

async function onDragEnd(evt: { oldIndex?: number; newIndex?: number }) {
  const { oldIndex, newIndex } = evt
  if (oldIndex == null || newIndex == null || oldIndex === newIndex) {
    return
  }
  saving.value = true
  try {
    await persistOrderAtIndex(newIndex)
    message.success(t('mpsPage.sortSuccess'))
  } catch {
    await loadBoard()
  } finally {
    saving.value = false
  }
}

async function moveUp(record: MpsRecord, index: number) {
  if (index <= 0) {
    return
  }
  await reorderQueue(record.id, index - 1)
  message.success(t('mpsPage.sortSuccess'))
}

async function moveDown(record: MpsRecord, index: number) {
  if (index >= queueList.value.length - 1) {
    return
  }
  await reorderQueue(record.id, index + 1)
  message.success(t('mpsPage.sortSuccess'))
}

async function handleJump(record: MpsRecord) {
  await jumpMpsQueue(record.id)
  message.success(t('mpsPage.jumpSuccess'))
  await loadBoard()
}

function confirmJump(record: MpsRecord) {
  Modal.confirm({
    title: t('mpsPage.jumpConfirmTitle'),
    content: t('mpsPage.jumpConfirmContent'),
    onOk: () => handleJump(record),
  })
}

async function handleRemove(record: MpsRecord) {
  await removeMpsQueue(record.id)
  message.success(t('mpsPage.removeSuccess'))
  await loadBoard()
}

function confirmRemove(record: MpsRecord) {
  Modal.confirm({
    title: t('mpsPage.removeConfirmTitle'),
    content: t('mpsPage.removeConfirmContent'),
    onOk: () => handleRemove(record),
  })
}

onMounted(async () => {
  applyRouteMoCode()
  await loadBoard()
})

watch(
  () => route.query.moCode,
  () => {
    applyRouteMoCode()
    onSearch()
  },
)
</script>

<template>
  <div class="admin-page admin-page--auto production-module-page production-module-page--mps mps-board-page">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle>
          <template #icon><NodeIndexOutlined /></template>
          {{ t('mpsPage.title') }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-button size="small" :loading="loading || saving" @click="loadBoard">
          <ReloadOutlined />
          {{ t('mpsPage.refresh') }}
        </a-button>
      </template>

      <div class="admin-panel-body">
        <div class="search-toolbar search-toolbar--compact">
          <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
            <a-form-item name="moCode">
              <a-input
                v-model:value="searchForm.moCode"
                allow-clear
                size="small"
                class="search-input"
                :placeholder="t('mpsPage.moCode')"
              />
            </a-form-item>
            <a-form-item name="productName">
              <a-input
                v-model:value="searchForm.productName"
                allow-clear
                size="small"
                class="search-input"
                :placeholder="t('moPage.bomName')"
              />
            </a-form-item>
            <a-form-item name="productCode">
              <a-input
                v-model:value="searchForm.productCode"
                allow-clear
                size="small"
                class="search-input"
                :placeholder="t('moPage.bomCode')"
              />
            </a-form-item>
            <CompactSearchActions
              :query-title="t('mpsPage.query')"
              :reset-title="t('mpsPage.reset')"
              @reset="onReset"
            />
          </a-form>
        </div>

        <a-spin :spinning="loading || saving" class="board-spin">
          <div class="mps-board">
            <!-- 第 1 步：待排产 -->
            <section class="board-col board-col--pending">
              <div class="board-col__accent" aria-hidden="true" />
              <header class="board-col__head">
                <div class="board-col__head-row">
                  <div class="board-col__title">
                    <span class="board-col__step">{{ t('mpsPage.colStep1') }}</span>
                    <span class="board-col__icon board-col__icon--pending">
                      <ClockCircleOutlined />
                    </span>
                    <span class="board-col__name">{{ t('mpsPage.colPending') }}</span>
                  </div>
                  <a-tag class="board-col__count">{{ pendingList.length }}</a-tag>
                </div>
                <p class="board-col__hint">{{ t('mpsPage.colPendingHint') }}</p>
              </header>
              <div class="board-col__body">
                <div v-if="!pendingList.length" class="col-empty">
                  <InboxOutlined />
                  <span>{{ t('mpsPage.pendingEmpty') }}</span>
                </div>
                <ul v-else class="col-list">
                  <li v-for="record in pendingList" :key="record.id">
                    <MpsBoardCard :record="record" tone="pending">
                      <template #actions>
                        <a-button
                          type="primary"
                          size="small"
                          class="board-action-btn board-action-btn--primary"
                          @click="handleJoinQueue(record.id)"
                        >
                          <ArrowRightOutlined />
                          {{ t('mpsPage.joinQueue') }}
                        </a-button>
                        <a-button size="small" class="board-action-btn" @click="openDetail(record)">
                          <EyeOutlined />
                          {{ t('mpsPage.detail') }}
                        </a-button>
                        <a-dropdown :trigger="['click']">
                          <a-button size="small" class="board-action-btn">
                            {{ t('moPage.adjust') }}
                          </a-button>
                          <template #overlay>
                            <a-menu @click="(info) => onQueueAdjustMenu(record, info.key)">
                              <a-menu-item key="RESCHEDULE">{{ t('mpsPage.reschedule') }}</a-menu-item>
                              <a-menu-item key="CHANGE_DEV">{{ t('mpsPage.changeDev') }}</a-menu-item>
                              <a-menu-divider />
                              <a-menu-item key="CANCEL_MPS" danger>{{ t('mpsPage.delete') }}</a-menu-item>
                            </a-menu>
                          </template>
                        </a-dropdown>
                      </template>
                    </MpsBoardCard>
                  </li>
                </ul>
              </div>
            </section>

            <div class="board-flow-arrow" aria-hidden="true">
              <ArrowRightOutlined />
            </div>

            <!-- 第 2 步：排队等待 -->
            <section class="board-col board-col--queue">
              <div class="board-col__accent" aria-hidden="true" />
              <header class="board-col__head">
                <div class="board-col__head-row">
                  <div class="board-col__title">
                    <span class="board-col__step">{{ t('mpsPage.colStep2') }}</span>
                    <span class="board-col__icon board-col__icon--queue">
                      <PlayCircleOutlined />
                    </span>
                    <span class="board-col__name">{{ t('mpsPage.colQueue') }}</span>
                  </div>
                  <a-tag class="board-col__count">{{ queueList.length }}</a-tag>
                </div>
                <p v-if="queueList.length" class="board-col__hint">
                  {{ t('mpsPage.queueOrderExplain') }}
                </p>
                <p v-else class="board-col__hint">{{ t('mpsPage.queueEmptyHint') }}</p>
              </header>
              <div class="board-col__body">
                <div v-if="!queueList.length" class="col-empty">
                  <InboxOutlined />
                  <span>{{ t('mpsPage.queueEmpty') }}</span>
                  <small>{{ t('mpsPage.queueEmptyHint') }}</small>
                </div>

                <draggable
                  v-else
                  v-model="queueList"
                  item-key="id"
                  tag="div"
                  class="queue-list"
                  handle=".board-card__drag"
                  :disabled="loading || saving"
                  ghost-class="queue-item--ghost"
                  drag-class="queue-item--drag"
                  :animation="180"
                  @end="onDragEnd"
                >
                  <template #item="{ element: record, index }">
                    <div class="queue-item" :class="{ 'queue-item--first': index === 0 }">
                      <MpsBoardCard
                        :record="record"
                        :rank="index + 1"
                        :tone="queueItemTone(index)"
                        show-drag-handle
                      >
                        <template #leading>
                          <a-tooltip :title="t('mpsPage.removeQueue')">
                            <a-button
                              danger
                              size="small"
                              class="icon-btn"
                              @click="confirmRemove(record)"
                            >
                              <ArrowLeftOutlined />
                            </a-button>
                          </a-tooltip>
                        </template>
                        <template #actions>
                          <a-tag
                            class="queue-status-tag"
                            :color="index === 0 ? 'success' : 'default'"
                          >
                            {{ queueStatusLabel(index) }}
                          </a-tag>
                          <span class="action-tools">
                            <a-button size="small" class="board-action-btn" @click="openDetail(record)">
                              {{ t('mpsPage.detail') }}
                            </a-button>
                            <a-dropdown :trigger="['click']">
                              <a-button size="small" class="board-action-btn">
                                {{ t('moPage.adjust') }}
                              </a-button>
                              <template #overlay>
                                <a-menu @click="(info) => onQueueAdjustMenu(record, info.key)">
                                  <a-menu-item key="RESCHEDULE">{{ t('mpsPage.reschedule') }}</a-menu-item>
                                  <a-menu-item key="CHANGE_DEV">{{ t('mpsPage.changeDev') }}</a-menu-item>
                                  <a-menu-divider />
                                  <a-menu-item key="CANCEL_MPS" danger>{{ t('mpsPage.delete') }}</a-menu-item>
                                </a-menu>
                              </template>
                            </a-dropdown>
                            <a-button
                              size="small"
                              class="board-action-btn"
                              :disabled="index === 0"
                              @click="moveUp(record, index)"
                            >
                              <ArrowUpOutlined />
                              {{ t('mpsPage.moveUp') }}
                            </a-button>
                            <a-button
                              size="small"
                              class="board-action-btn"
                              :disabled="index >= queueList.length - 1"
                              @click="moveDown(record, index)"
                            >
                              <ArrowDownOutlined />
                              {{ t('mpsPage.moveDown') }}
                            </a-button>
                            <a-button
                              v-if="index > 0"
                              size="small"
                              type="primary"
                              ghost
                              class="board-action-btn"
                              @click="confirmJump(record)"
                            >
                              <VerticalAlignTopOutlined />
                              {{ t('mpsPage.jumpQueue') }}
                            </a-button>
                          </span>
                        </template>
                      </MpsBoardCard>
                      <div v-if="index < queueList.length - 1" class="queue-connector" aria-hidden="true">
                        <ArrowDownOutlined />
                      </div>
                    </div>
                  </template>
                </draggable>
              </div>
            </section>

            <div class="board-flow-arrow" aria-hidden="true">
              <ArrowRightOutlined />
            </div>

            <!-- 第 3 步：生产进度 -->
            <section class="board-col board-col--done">
              <div class="board-col__accent" aria-hidden="true" />
              <header class="board-col__head">
                <div class="board-col__head-row">
                  <div class="board-col__title">
                    <span class="board-col__step">{{ t('mpsPage.colStep3') }}</span>
                    <span class="board-col__icon board-col__icon--done">
                      <CheckCircleOutlined />
                    </span>
                    <span class="board-col__name">{{ t('mpsPage.colOutcome') }}</span>
                  </div>
                  <a-tag class="board-col__count">{{ runningList.length + doneList.length }}</a-tag>
                </div>
                <p class="board-col__hint">{{ t('mpsPage.colOutcomeHint') }}</p>
              </header>
              <div class="board-col__body">
                <div v-if="!hasDoneColumnItems" class="col-empty">
                  <InboxOutlined />
                  <span>{{ t('mpsPage.colOutcomeEmpty') }}</span>
                </div>
                <ul v-else class="col-list col-list--done">
                  <li v-for="record in runningList" :key="`running-${record.id}`">
                    <MpsBoardCard :record="record" tone="running">
                      <template #actions>
                        <a-tag color="processing">{{ t('mpsPage.executingTag') }}</a-tag>
                        <a-button size="small" class="board-action-btn" @click="openDetail(record)">
                          <EyeOutlined />
                          {{ t('mpsPage.detail') }}
                        </a-button>
                        <a-button
                          size="small"
                          danger
                          class="board-action-btn"
                          @click="openCancel(record)"
                        >
                          {{ t('moPage.cancelForce') }}
                        </a-button>
                      </template>
                    </MpsBoardCard>
                  </li>
                  <li
                    v-if="runningList.length && doneList.length"
                    class="done-col-divider"
                    aria-hidden="true"
                  />
                  <li v-for="record in doneList" :key="record.id">
                    <MpsBoardCard :record="record" tone="done">
                      <template #actions>
                        <a-tag color="success">{{ t('mpsPage.colDone') }}</a-tag>
                        <a-button size="small" class="board-action-btn" @click="openDetail(record)">
                          <EyeOutlined />
                          {{ t('mpsPage.detail') }}
                        </a-button>
                      </template>
                    </MpsBoardCard>
                  </li>
                </ul>
              </div>
            </section>
          </div>
        </a-spin>
      </div>
    </a-card>

    <MoAdjustModal
      v-model:open="adjustOpen"
      :adjust-type="adjustType"
      :mps-record="adjustMps"
      :mps-ids="adjustMpsIds"
      @success="loadBoard"
    />
  </div>
</template>

