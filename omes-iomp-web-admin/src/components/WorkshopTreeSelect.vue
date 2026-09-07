<script setup lang="ts">
import { computed, onMounted, ref, shallowRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  CloseCircleFilled,
  DownOutlined,
  EnvironmentOutlined,
} from '@ant-design/icons-vue'
import type { WorkshopNode } from '@/api/device'
import { fetchWorkshopTree } from '@/api/device'
import WorkshopTree from '@/components/WorkshopTree.vue'
import { buildWorkshopTreeIndex, resolveWorkshopBreadcrumb } from '@/utils/workshop-tree'

const props = withDefaults(
  defineProps<{
    modelValue?: WorkshopNode | null
    allowClear?: boolean
    variant?: 'default' | 'screen'
  }>(),
  {
    allowClear: true,
    variant: 'default',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: WorkshopNode | null]
  change: [value: WorkshopNode | null]
}>()

const { t } = useI18n()

const open = ref(false)
const treeMounted = ref(false)
const treeLoading = ref(false)
const treeData = ref<WorkshopNode[]>([])
const nodeByCode = shallowRef(new Map<string, WorkshopNode>())

const breadcrumbNodes = computed(() =>
  resolveWorkshopBreadcrumb(treeData.value, nodeByCode.value, props.modelValue?.selfCode),
)

const breadcrumbTitle = computed(() => breadcrumbNodes.value.map((node) => node.name).join(' / '))

const currentNodeName = computed(() => {
  if (!props.modelValue) {
    return ''
  }
  return breadcrumbNodes.value.at(-1)?.name || props.modelValue.name
})

const parentPathLabel = computed(() => {
  if (breadcrumbNodes.value.length <= 1) {
    return ''
  }
  return breadcrumbNodes.value
    .slice(0, -1)
    .map((node) => node.name)
    .join(' / ')
})

const displayTitle = computed(() => breadcrumbTitle.value || currentNodeName.value || t('realtimePage.workshopPlaceholder'))

watch(open, (visible) => {
  if (visible) {
    treeMounted.value = true
  }
})

function syncTreeIndex(nodes: WorkshopNode[]) {
  treeData.value = nodes
  nodeByCode.value = buildWorkshopTreeIndex(nodes).nodeByCode
}

async function loadTreeData() {
  treeLoading.value = true
  try {
    const data = await fetchWorkshopTree()
    syncTreeIndex(Array.isArray(data) ? data : [])
  } finally {
    treeLoading.value = false
  }
}

function onTreeLoaded(nodes: WorkshopNode[]) {
  syncTreeIndex(nodes)
}

function openPicker() {
  open.value = true
}

function closePicker() {
  open.value = false
}

function onTreeChange(node: WorkshopNode | null) {
  emit('update:modelValue', node)
  emit('change', node)
  open.value = false
}

function clearSelection(event?: Event) {
  event?.stopPropagation()
  emit('update:modelValue', null)
  emit('change', null)
}

function selectAllWorkshops() {
  clearSelection()
  open.value = false
}

onMounted(() => {
  void loadTreeData()
})
</script>

<template>
  <div class="workshop-tree-select" :class="{ 'workshop-tree-select--screen': variant === 'screen' }">
    <button
      type="button"
      class="workshop-picker-trigger"
      :class="{
        'is-open': open,
        'has-value': !!modelValue,
        'has-breadcrumb': breadcrumbNodes.length > 1,
      }"
      @click="openPicker"
    >
    <span class="trigger-icon"><EnvironmentOutlined /></span>
    <span class="trigger-body">
      <span v-if="modelValue" class="trigger-current" :title="displayTitle">{{ currentNodeName }}</span>
      <span v-if="parentPathLabel" class="trigger-path" :title="breadcrumbTitle">{{ parentPathLabel }}</span>
      <span v-if="!modelValue" class="trigger-placeholder">{{ t('realtimePage.workshopPlaceholder') }}</span>
    </span>
    <span class="trigger-suffix">
      <CloseCircleFilled
        v-if="allowClear && modelValue"
        class="trigger-clear"
        @click="clearSelection"
      />
      <DownOutlined class="trigger-arrow" :class="{ 'is-open': open }" />
    </span>
  </button>

  <a-modal
    v-model:open="open"
    centered
    width="min(520px, 96vw)"
    class="workshop-picker-modal"
    :class="{ 'workshop-picker-modal--screen': variant === 'screen' }"
    :mask-closable="true"
    :destroy-on-close="false"
    @cancel="closePicker"
  >
    <template #title>
      <div class="modal-title-wrap">
        <span class="picker-title">{{ t('realtimePage.workshopPickerTitle') }}</span>
        <span class="picker-hint">{{ t('realtimePage.workshopPickerHint') }}</span>
      </div>
    </template>

    <div class="workshop-picker-body">
      <div v-if="breadcrumbNodes.length" class="picker-breadcrumb">
        <span class="picker-breadcrumb__label">{{ t('realtimePage.workshopPath') }}</span>
        <a-breadcrumb class="picker-breadcrumb__trail">
          <a-breadcrumb-item v-for="node in breadcrumbNodes" :key="node.selfCode">
            <span :title="node.name">{{ node.name }}</span>
          </a-breadcrumb-item>
        </a-breadcrumb>
      </div>

      <WorkshopTree
        v-if="treeMounted"
        :model-value="modelValue"
        :auto-select-first="false"
        :source-tree-data="treeData.length ? treeData : null"
        embedded
        :tree-height="420"
        @change="onTreeChange"
        @tree-loaded="onTreeLoaded"
      />
      <div v-else-if="treeLoading" class="picker-loading">
        <a-spin />
      </div>
    </div>

    <template #footer>
      <div class="modal-footer">
        <a-button v-if="allowClear" type="link" class="picker-all-btn" @click="selectAllWorkshops">
          {{ t('realtimePage.allWorkshops') }}
        </a-button>
        <a-button @click="closePicker">{{ t('request.cancel') }}</a-button>
      </div>
    </template>
  </a-modal>
  </div>
</template>

<style scoped>
.workshop-tree-select {
  display: inline-block;
  vertical-align: middle;
}

.workshop-picker-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-width: 260px;
  max-width: 360px;
  min-height: 28px;
  padding: 4px 10px;
  border: 1px solid #d9d9d9;
  border-radius: var(--omes-radius-sm);
  background: var(--omes-color-bg-container);
  color: var(--omes-color-text);
  font-size: 13px;
  line-height: 1.2;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.workshop-picker-trigger.has-breadcrumb {
  align-items: flex-start;
  padding-block: 5px;
}

.workshop-picker-trigger:hover,
.workshop-picker-trigger.is-open {
  border-color: var(--omes-color-primary-hover);
}

.workshop-picker-trigger.is-open {
  box-shadow: 0 0 0 2px rgba(5, 145, 255, 0.1);
}

.trigger-icon {
  flex-shrink: 0;
  color: #0284c7;
  font-size: 14px;
  margin-top: 1px;
}

.workshop-picker-trigger.has-breadcrumb .trigger-icon {
  margin-top: 3px;
}

.trigger-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
  text-align: left;
}

.trigger-current {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #0f172a;
  font-weight: 600;
}

.trigger-path {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
  color: #94a3b8;
}

.trigger-placeholder {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--omes-color-text-quaternary);
}

.trigger-suffix {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  align-self: center;
}

.workshop-picker-trigger.has-breadcrumb .trigger-suffix {
  align-self: flex-start;
  margin-top: 2px;
}

.trigger-clear {
  color: rgba(0, 0, 0, 0.25);
  font-size: 12px;
  transition: color 0.15s ease;
}

.trigger-clear:hover {
  color: var(--omes-color-text-quaternary);
}

.trigger-arrow {
  color: var(--omes-color-text-placeholder);
  font-size: 11px;
  transition: transform 0.2s ease;
}

.trigger-arrow.is-open {
  transform: rotate(180deg);
}

.modal-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.picker-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.3;
}

.picker-hint {
  font-size: 12px;
  font-weight: 400;
  color: #94a3b8;
}

.workshop-picker-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.picker-breadcrumb {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 10px;
  border-radius: var(--omes-radius-lg);
  background: var(--omes-color-bg-toolbar-from);
  border: 1px solid #e8eef5;
}

.picker-breadcrumb__label {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  letter-spacing: 0.02em;
}

.picker-breadcrumb__trail {
  min-width: 0;
}

.picker-breadcrumb__trail :deep(.ant-breadcrumb-link),
.picker-breadcrumb__trail :deep(.ant-breadcrumb-separator) {
  font-size: 12px;
}

.picker-breadcrumb__trail :deep(.ant-breadcrumb-link:last-child) {
  color: var(--omes-color-primary);
  font-weight: 600;
}

.picker-breadcrumb__trail :deep(.ant-breadcrumb-link span) {
  display: inline-block;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}

.picker-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.picker-all-btn {
  margin-right: auto;
  padding-inline: 0;
}

.workshop-tree-select--screen .workshop-picker-trigger {
  min-width: 180px;
  max-width: min(28vw, 280px);
  background: linear-gradient(180deg, rgba(10, 24, 42, 0.96) 0%, rgba(6, 16, 30, 0.88) 100%);
  border-color: rgba(91, 243, 249, 0.32);
  color: #dce8f2;
  border-radius: 4px;
}

.workshop-tree-select--screen .workshop-picker-trigger:hover,
.workshop-tree-select--screen .workshop-picker-trigger.is-open {
  border-color: rgba(91, 243, 249, 0.55);
  box-shadow: 0 0 0 1px rgba(91, 243, 249, 0.12);
}

.workshop-tree-select--screen .trigger-current {
  color: #e8f0f8;
}

.workshop-tree-select--screen .trigger-path,
.workshop-tree-select--screen .trigger-placeholder {
  color: rgba(148, 163, 184, 0.82);
}

.workshop-tree-select--screen .trigger-icon {
  color: #5bf3f9;
}

.workshop-tree-select--screen .trigger-clear {
  color: rgba(148, 163, 184, 0.55);
}

.workshop-tree-select--screen .trigger-clear:hover {
  color: rgba(200, 212, 224, 0.85);
}

.workshop-tree-select--screen .trigger-arrow {
  color: rgba(148, 163, 184, 0.65);
}
</style>

<style>
.workshop-picker-modal .ant-modal-body {
  padding-top: 12px;
}

.workshop-picker-modal--screen.ant-modal .ant-modal-content {
  background: linear-gradient(180deg, rgba(10, 24, 42, 0.98) 0%, rgba(6, 16, 30, 0.98) 100%);
  border: 1px solid rgba(91, 243, 249, 0.28);
  box-shadow: 0 16px 48px var(--omes-color-text-tertiary);
}

.workshop-picker-modal--screen.ant-modal .ant-modal-header {
  background: transparent;
  border-bottom: 1px solid rgba(91, 243, 249, 0.18);
}

.workshop-picker-modal--screen.ant-modal .ant-modal-title,
.workshop-picker-modal--screen .picker-title {
  color: #e8f0f8;
}

.workshop-picker-modal--screen .picker-hint {
  color: rgba(148, 163, 184, 0.85);
}

.workshop-picker-modal--screen .picker-breadcrumb {
  background: rgba(8, 20, 36, 0.72);
  border-color: rgba(91, 243, 249, 0.16);
}

.workshop-picker-modal--screen .picker-breadcrumb__label {
  color: rgba(148, 163, 184, 0.9);
}

.workshop-picker-modal--screen .picker-breadcrumb__trail .ant-breadcrumb-link,
.workshop-picker-modal--screen .picker-breadcrumb__trail .ant-breadcrumb-separator {
  color: rgba(148, 163, 184, 0.75);
}

.workshop-picker-modal--screen .picker-breadcrumb__trail .ant-breadcrumb-link:last-child {
  color: #5bf3f9;
}

.workshop-picker-modal--screen.ant-modal .ant-modal-footer {
  border-top: 1px solid rgba(91, 243, 249, 0.14);
  background: transparent;
}

.workshop-picker-modal--screen.ant-modal .ant-modal-close {
  color: rgba(148, 163, 184, 0.75);
}

.workshop-picker-modal--screen.ant-modal .ant-btn-default {
  background: rgba(8, 20, 36, 0.85);
  border-color: rgba(91, 243, 249, 0.28);
  color: #c8d4e0;
}
</style>
