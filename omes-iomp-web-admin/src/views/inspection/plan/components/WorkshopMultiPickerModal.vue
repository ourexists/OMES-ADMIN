<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { EnvironmentOutlined } from '@ant-design/icons-vue'
import type { WorkshopNode } from '@/api/device'
import { fetchWorkshopTree } from '@/api/device'
import { buildWorkshopTreeIndex } from '@/utils/workshop-tree'

const props = defineProps<{
  open: boolean
  selectedCodes?: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: [codes: string, names: string[]]
}>()

const { t } = useI18n()

const loading = ref(false)
const treeData = ref<WorkshopNode[]>([])
const checkedKeys = ref<string[]>([])
const expandedKeys = ref<string[]>([])
const nodeByCode = ref(new Map<string, WorkshopNode>())

const selectedCodeSet = computed(() => {
  const raw = props.selectedCodes?.trim()
  if (!raw) {
    return new Set<string>()
  }
  return new Set(raw.split(',').map((s) => s.trim()).filter(Boolean))
})

const checkedCount = computed(() => checkedKeys.value.filter((code) => nodeByCode.value.has(code)).length)

async function loadTree() {
  loading.value = true
  try {
    const data = await fetchWorkshopTree()
    treeData.value = Array.isArray(data) ? data : []
    const index = buildWorkshopTreeIndex(treeData.value)
    nodeByCode.value = index.nodeByCode
    expandedKeys.value = index.expandableKeys
    checkedKeys.value = [...selectedCodeSet.value].filter((code) => nodeByCode.value.has(code))
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (visible) => {
    if (visible) {
      loadTree()
    }
  },
)

function closeModal() {
  emit('update:open', false)
}

function onConfirm() {
  const codes = checkedKeys.value.filter((code) => nodeByCode.value.has(code))
  const names = codes.map((code) => nodeByCode.value.get(code)?.name || code)
  emit('confirm', codes.join(','), names)
  closeModal()
}
</script>

<template>
  <a-modal
    :open="open"
    width="560px"
    destroy-on-close
    class="inspect-picker-modal"
    @cancel="closeModal"
    @ok="onConfirm"
  >
    <template #title>
      <span class="modal-title">
        <EnvironmentOutlined style="margin-right: 8px; color: var(--omes-color-primary)" />
        {{ t('inspectPlanPage.workshopPickerTitle') }}
      </span>
    </template>

    <a-spin :spinning="loading">
      <p class="inspect-picker-hint">
        {{ t('inspectPlanPage.workshopPickerHint') }}
        <template v-if="checkedCount">
          已 {{ t('inspectPlanPage.workshopPickerSelected', { count: checkedCount }) }}
        </template>
      </p>
      <div v-if="treeData.length" class="inspect-picker-tree">
        <a-tree
          v-model:checked-keys="checkedKeys"
          v-model:expanded-keys="expandedKeys"
          checkable
          :tree-data="treeData"
          :field-names="{ title: 'name', key: 'selfCode', children: 'children' }"
        />
      </div>
      <a-empty v-else :description="t('inspectPlanPage.workshopEmpty')" />
    </a-spin>
  </a-modal>
</template>
