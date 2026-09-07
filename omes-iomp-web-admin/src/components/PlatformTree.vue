<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { PlatformNode } from '@/api/ucenter'
import { fetchPlatforms } from '@/api/ucenter'

const { t } = useI18n()

const props = defineProps<{
  modelValue?: PlatformNode | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: PlatformNode | null]
  change: [value: PlatformNode | null]
}>()

const loading = ref(false)
const treeData = ref<PlatformNode[]>([])
const selectedKeys = ref<string[]>([])

async function loadTree() {
  loading.value = true
  try {
    const data = await fetchPlatforms()
    treeData.value = Array.isArray(data) ? data : []
    if (props.modelValue?.code) {
      selectedKeys.value = [props.modelValue.code]
    } else if (treeData.value.length > 0) {
      const first = treeData.value[0]
      selectedKeys.value = [first.code]
      emit('update:modelValue', first)
      emit('change', first)
    }
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
  const code = keys[0]
  const node = findNode(treeData.value, code)
  emit('update:modelValue', node)
  emit('change', node)
}

function findNode(nodes: PlatformNode[], code: string): PlatformNode | null {
  for (const node of nodes) {
    if (node.code === code) {
      return node
    }
    if (node.children?.length) {
      const found = findNode(node.children, code)
      if (found) {
        return found
      }
    }
  }
  return null
}

watch(
  () => props.modelValue?.code,
  (code) => {
    selectedKeys.value = code ? [code] : []
  },
)

onMounted(loadTree)
</script>

<template>
  <div class="platform-tree">
    <a-spin :spinning="loading">
      <a-tree
        v-if="treeData.length"
        :tree-data="treeData"
        :selected-keys="selectedKeys"
        :field-names="{ title: 'name', key: 'code', children: 'children' }"
        default-expand-all
        block-node
        @select="onSelect"
      />
      <a-empty v-else :description="t('platformTree.empty')" />
    </a-spin>
  </div>
</template>

<style scoped>
.platform-tree {
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>
