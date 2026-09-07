<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  EnvironmentOutlined,
  ExpandAltOutlined,
  FolderOutlined,
  ReloadOutlined,
  SearchOutlined,
  ShrinkOutlined,
} from '@ant-design/icons-vue'
import type { WorkshopNode } from '@/api/device'
import { fetchWorkshopTree } from '@/api/device'
import {
  buildWorkshopTreeIndex,
  collectExpandableKeys,
  collectSearchMatchKeys,
  filterWorkshopTree,
  resolveWorkshopBreadcrumb,
} from '@/utils/workshop-tree'

const SEARCH_DEBOUNCE_MS = 250

const { t } = useI18n()

const props = defineProps<{
  modelValue?: WorkshopNode | null
  autoSelectFirst?: boolean
  /** 嵌入下拉/弹层时使用固定高度，避免 flex 撑不满 */
  embedded?: boolean
  treeHeight?: number
  /** 外部传入树数据时不再重复请求 */
  sourceTreeData?: WorkshopNode[] | null
  /** 占满父容器剩余高度，树列表区域单独滚动 */
  fill?: boolean
  /** 大屏侧边场景树等深色主题 */
  variant?: 'default' | 'screen'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: WorkshopNode | null]
  change: [value: WorkshopNode | null]
  'tree-loaded': [nodes: WorkshopNode[]]
}>()

const loading = ref(false)
const treeData = ref<WorkshopNode[]>([])
const nodeByCode = shallowRef(new Map<string, WorkshopNode>())
const selectedKeys = ref<string[]>([])
const expandedKeys = ref<string[]>([])
const expandAll = ref(true)
const searchKeyword = ref('')
const debouncedSearch = ref('')

let searchDebounceTimer: ReturnType<typeof setTimeout> | null = null
let treeResizeObserver: ResizeObserver | null = null
let resizeRafId = 0

const treeBodyRef = ref<HTMLElement | null>(null)
const treeScrollHeight = ref(0)

const autoSelect = computed(() => props.autoSelectFirst !== false)

const isFill = computed(() => props.fill === true && !props.embedded)

const treeContainerStyle = computed(() => {
  if (props.embedded) {
    const height = props.treeHeight ?? 360
    return { height: `${height}px` }
  }
  if (isFill.value && treeScrollHeight.value > 0) {
    return { height: `${treeScrollHeight.value}px` }
  }
  return undefined
})

function updateTreeScrollHeight() {
  const body = treeBodyRef.value
  if (!body) {
    return
  }
  treeScrollHeight.value = Math.max(160, Math.floor(body.clientHeight))
}

function scheduleTreeScrollHeightUpdate() {
  if (resizeRafId) {
    cancelAnimationFrame(resizeRafId)
  }
  resizeRafId = requestAnimationFrame(() => {
    resizeRafId = 0
    updateTreeScrollHeight()
  })
}

async function scheduleTreeScrollUpdate() {
  await nextTick()
  scheduleTreeScrollHeightUpdate()
}

const nodeCount = computed(() => nodeByCode.value.size)

const searchActive = computed(() => Boolean(debouncedSearch.value.trim()))

const displayTreeData = computed(() => {
  const keyword = debouncedSearch.value.trim().toLowerCase()
  if (!keyword) {
    return treeData.value
  }
  return filterWorkshopTree(treeData.value, keyword)
})

const searchMatchKeys = computed(() => {
  const keyword = debouncedSearch.value.trim().toLowerCase()
  if (!keyword) {
    return null
  }
  return collectSearchMatchKeys(treeData.value, keyword)
})

const treeReady = computed(() => {
  if (!displayTreeData.value.length) {
    return false
  }
  if (!isFill.value) {
    return true
  }
  return treeScrollHeight.value > 0
})

function rebuildIndex(nodes: WorkshopNode[]) {
  const index = buildWorkshopTreeIndex(nodes)
  nodeByCode.value = index.nodeByCode
}

function syncExpandedKeys() {
  if (searchActive.value) {
    expandedKeys.value = collectExpandableKeys(displayTreeData.value)
    return
  }
  if (expandAll.value) {
    expandedKeys.value = collectExpandableKeys(treeData.value)
  }
}

function flushSearch() {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = null
  }
  debouncedSearch.value = searchKeyword.value
}

function onReset() {
  searchKeyword.value = ''
  flushSearch()
  syncExpandedKeys()
}

function expandToSelected(selfCode?: string | null) {
  if (!selfCode || !treeData.value.length) {
    return
  }
  const path = resolveWorkshopBreadcrumb(treeData.value, nodeByCode.value, selfCode)
  if (!path.length) {
    return
  }
  const ancestorKeys = path.slice(0, -1).map((node) => node.selfCode)
  expandedKeys.value = [...new Set([...expandedKeys.value, ...ancestorKeys])]
}

function applyTreeData(nodes: WorkshopNode[], options?: { autoSelect?: boolean }) {
  treeData.value = nodes
  rebuildIndex(treeData.value)
  syncExpandedKeys()
  emit('tree-loaded', nodes)

  if (props.modelValue?.selfCode) {
    const code = props.modelValue.selfCode
    selectedKeys.value = nodeByCode.value.has(code) ? [code] : []
    expandToSelected(code)
  } else if (options?.autoSelect !== false && autoSelect.value && treeData.value.length > 0) {
    const first = treeData.value[0]
    selectedKeys.value = [first.selfCode]
    emit('update:modelValue', first)
    emit('change', first)
  }
}

async function loadTree() {
  loading.value = true
  try {
    const data = await fetchWorkshopTree()
    applyTreeData(Array.isArray(data) ? data : [])
  } finally {
    loading.value = false
  }
}

function onSelect(keys: string[]) {
  selectedKeys.value = keys
  if (!keys.length) {
    emit('update:modelValue', null)
    emit('change', null)
    return
  }
  const node = nodeByCode.value.get(keys[0]) ?? null
  emit('update:modelValue', node)
  emit('change', node)
}

function expandAllNodes() {
  expandAll.value = true
  expandedKeys.value = collectExpandableKeys(treeData.value)
}

function collapseAllNodes() {
  expandAll.value = false
  expandedKeys.value = []
}

function onExpandedKeysChange(keys: string[]) {
  expandedKeys.value = keys
  const allKeys = collectExpandableKeys(treeData.value)
  expandAll.value = allKeys.length > 0 && keys.length >= allKeys.length
}

watch(
  () => props.modelValue?.selfCode,
  (selfCode) => {
    selectedKeys.value = selfCode && nodeByCode.value.has(selfCode) ? [selfCode] : []
    expandToSelected(selfCode)
  },
)

watch(
  () => props.sourceTreeData,
  (nodes) => {
    if (nodes?.length) {
      applyTreeData(nodes, { autoSelect: false })
    }
  },
  { immediate: true },
)

watch(searchKeyword, (value) => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
  }
  searchDebounceTimer = setTimeout(() => {
    searchDebounceTimer = null
    debouncedSearch.value = value
    syncExpandedKeys()
  }, SEARCH_DEBOUNCE_MS)
})

watch(debouncedSearch, () => {
  syncExpandedKeys()
})

watch(loading, (spinning) => {
  if (!spinning && isFill.value) {
    void scheduleTreeScrollUpdate()
  }
})

async function setupFillScroll() {
  if (!isFill.value) {
    return
  }
  await scheduleTreeScrollUpdate()
  if (treeBodyRef.value) {
    treeResizeObserver?.disconnect()
    treeResizeObserver = new ResizeObserver(() => scheduleTreeScrollHeightUpdate())
    treeResizeObserver.observe(treeBodyRef.value)
  }
}

onMounted(() => {
  if (!props.sourceTreeData?.length) {
    void loadTree()
  }
  if (isFill.value) {
    void setupFillScroll()
  }
})

watch(isFill, (enabled) => {
  if (enabled) {
    void setupFillScroll()
  } else {
    treeResizeObserver?.disconnect()
    treeResizeObserver = null
  }
})

onUnmounted(() => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
  }
  if (resizeRafId) {
    cancelAnimationFrame(resizeRafId)
  }
  treeResizeObserver?.disconnect()
  treeResizeObserver = null
})
</script>

<template>
  <div
    class="workshop-tree"
    :class="{
      'is-embedded': embedded,
      'is-fill': isFill,
      'is-screen': variant === 'screen',
    }"
  >
    <div class="tree-search-row">
      <a-input
        v-model:value="searchKeyword"
        allow-clear
        size="small"
        :placeholder="t('workshopTree.searchPlaceholder')"
        class="tree-search-input"
      >
        <template #prefix>
          <SearchOutlined class="search-icon" />
        </template>
      </a-input>
      <a-button size="small" class="tree-search-btn" @click="onReset">
        {{ t('workshopTree.reset') }}
      </a-button>
    </div>

    <div class="tree-actions-row">
      <a-space :size="4" class="tree-actions">
        <a-tooltip :title="t('workshopTree.expandAll')">
          <a-button size="small" @click="expandAllNodes">
            <ExpandAltOutlined />
          </a-button>
        </a-tooltip>
        <a-tooltip :title="t('workshopTree.collapseAll')">
          <a-button size="small" @click="collapseAllNodes">
            <ShrinkOutlined />
          </a-button>
        </a-tooltip>
        <a-tooltip :title="t('workshopTree.refresh')">
          <a-button size="small" :loading="loading" @click="loadTree">
            <ReloadOutlined />
          </a-button>
        </a-tooltip>
      </a-space>
      <a-tag v-if="nodeCount" color="processing" class="node-count">
        {{ t('workshopTree.total', { count: nodeCount }) }}
      </a-tag>
    </div>

    <div ref="treeBodyRef" class="tree-body">
      <div
        class="tree-container is-scroll tree-spin"
        :class="{ 'is-filtering': searchActive }"
        :style="treeContainerStyle"
      >
        <a-tree
          v-if="treeReady"
          :tree-data="displayTreeData"
          :selected-keys="selectedKeys"
          :expanded-keys="expandedKeys"
          :field-names="{ title: 'name', key: 'selfCode', children: 'children' }"
          block-node
          @select="onSelect"
          @update:expanded-keys="onExpandedKeysChange"
        >
          <template #title="node">
            <span
              class="tree-node"
              :class="{
                'is-leaf': !node.children?.length,
                'is-match': searchMatchKeys?.has(node.selfCode),
              }"
            >
              <FolderOutlined v-if="node.children?.length" class="node-icon folder" />
              <EnvironmentOutlined v-else class="node-icon leaf" />
              <span class="node-name" :title="node.name">{{ node.name }}</span>
            </span>
          </template>
        </a-tree>
        <a-empty
          v-else-if="!displayTreeData.length"
          :description="searchActive ? t('workshopTree.searchEmpty') : t('workshopTree.empty')"
          class="tree-empty"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.workshop-tree {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  gap: 8px;
}

.tree-search-row {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  background: var(--omes-color-bg-elevated);
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-lg);
}

.tree-search-input {
  flex: 1;
  min-width: 0;
}

.tree-search-btn {
  flex-shrink: 0;
}

.search-icon {
  color: var(--omes-color-text-placeholder);
}

.tree-actions-row {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 0 2px;
}

.tree-actions {
  flex-shrink: 0;
}

.node-count {
  flex-shrink: 0;
  margin: 0;
  max-width: 88px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  border-radius: 999px;
  font-size: 11px;
  line-height: 18px;
}

.tree-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.workshop-tree.is-fill .tree-body {
  flex: 1 1 0;
  height: 0;
}

.tree-spin {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tree-container {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 6px 4px 6px 6px;
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-lg);
  background: linear-gradient(180deg, #f8faff 0%, #fff 24%);
}

.tree-container.is-scroll {
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 2px;
  border: none;
  border-radius: 0;
  background: transparent;
}

.tree-container.is-scroll::-webkit-scrollbar {
  width: 6px;
}

.tree-container.is-scroll::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}

.tree-container.is-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.25);
}

.workshop-tree.is-fill .tree-container.is-filtering {
  border-color: #91caff;
  background: linear-gradient(180deg, #f0f7ff 0%, #fff 28%);
}

.tree-container.is-filtering :deep(.ant-tree-node-content-wrapper) {
  opacity: 0.72;
}

.tree-container.is-filtering :deep(.tree-node.is-match .node-name) {
  opacity: 1;
  color: var(--omes-color-primary);
  font-weight: 600;
}

.tree-container :deep(.ant-tree) {
  background: transparent;
  font-size: 13px;
}

.tree-container :deep(.ant-tree-treenode) {
  padding: 2px 0;
  border-radius: var(--omes-radius-sm);
}

.tree-container :deep(.ant-tree-node-content-wrapper) {
  min-height: 32px;
  line-height: 32px;
  border-radius: var(--omes-radius-sm);
  transition: background 0.15s ease;
}

.tree-container :deep(.ant-tree-node-content-wrapper:hover) {
  background: var(--omes-color-primary-bg-hover);
}

.tree-container :deep(.ant-tree-node-selected) {
  background: var(--omes-color-primary-bg) !important;
}

.tree-container :deep(.ant-tree-switcher) {
  line-height: 32px;
}

.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  min-width: 0;
}

.node-icon {
  flex-shrink: 0;
  font-size: 14px;
}

.node-icon.folder {
  color: var(--omes-color-primary);
}

.node-icon.leaf {
  color: var(--omes-color-success);
}

.node-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--omes-color-text);
  font-weight: 500;
}

.tree-node.is-leaf .node-name {
  font-weight: 400;
}

.tree-container :deep(.ant-tree-node-selected) .node-name {
  color: var(--omes-color-primary);
}

.tree-empty {
  padding: 24px 0;
}

.workshop-tree.is-embedded {
  flex: none;
  min-height: 0;
  gap: 10px;
}

.workshop-tree.is-embedded .tree-spin {
  flex: none;
  overflow: hidden;
  height: auto;
}

.workshop-tree.is-embedded .tree-container {
  flex: none;
  min-height: 0;
}

.workshop-tree.is-screen {
  gap: 8px;
  min-height: 0;
}

.workshop-tree.is-screen .tree-search-row {
  background: rgba(8, 20, 36, 0.72);
  border-color: rgba(91, 243, 249, 0.18);
}

.workshop-tree.is-screen .search-icon {
  color: rgba(148, 163, 184, 0.75);
}

.workshop-tree.is-screen .tree-container {
  border-color: rgba(91, 243, 249, 0.16);
  background: rgba(6, 16, 30, 0.55);
}

.workshop-tree.is-screen .tree-container.is-filtering {
  border-color: rgba(91, 243, 249, 0.38);
  background: rgba(8, 22, 40, 0.72);
}

.workshop-tree.is-screen .tree-container.is-scroll::-webkit-scrollbar-thumb {
  background: rgba(91, 243, 249, 0.28);
}

.workshop-tree.is-screen .tree-container :deep(.ant-tree-node-content-wrapper) {
  color: #dce8f2;
}

.workshop-tree.is-screen .tree-container :deep(.ant-tree-node-content-wrapper:hover) {
  background: rgba(91, 243, 249, 0.1);
}

.workshop-tree.is-screen .tree-container :deep(.ant-tree-node-selected) {
  background: rgba(91, 243, 249, 0.18) !important;
}

.workshop-tree.is-screen .tree-container :deep(.ant-tree-node-selected) .node-name {
  color: #5bf3f9;
}

.workshop-tree.is-screen .tree-container :deep(.ant-tree-switcher) {
  color: rgba(148, 163, 184, 0.75);
}

.workshop-tree.is-screen .node-icon {
  color: #5bf3f9;
}

.workshop-tree.is-screen .node-name {
  color: #dce8f2;
}

.workshop-tree.is-screen :deep(.ant-input-affix-wrapper) {
  background: rgba(8, 20, 36, 0.85);
  border-color: rgba(91, 243, 249, 0.22);
}

.workshop-tree.is-screen :deep(.ant-input) {
  background: transparent;
  color: #dce8f2;
}

.workshop-tree.is-screen :deep(.ant-input::placeholder) {
  color: rgba(148, 163, 184, 0.65);
}

.workshop-tree.is-screen :deep(.ant-btn-default) {
  background: rgba(8, 20, 36, 0.85);
  border-color: rgba(91, 243, 249, 0.22);
  color: #c8d4e0;
}

.workshop-tree.is-screen :deep(.ant-tag-processing) {
  background: rgba(91, 243, 249, 0.12);
  border-color: rgba(91, 243, 249, 0.28);
  color: #5bf3f9;
}
</style>
