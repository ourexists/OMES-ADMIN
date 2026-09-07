<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue'
import type { WorkshopNode } from '@/api/device'
import WorkshopTree from '@/components/WorkshopTree.vue'

const props = defineProps<{
  modelValue?: WorkshopNode | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: WorkshopNode | null]
  change: [value: WorkshopNode | null]
}>()

const { t } = useI18n()
const open = ref(false)
const treeMounted = ref(false)

watch(open, (visible) => {
  if (visible) {
    treeMounted.value = true
  }
})

function togglePanel() {
  open.value = !open.value
}

function onTreeChange(node: WorkshopNode | null) {
  emit('update:modelValue', node)
  emit('change', node)
}

function selectAllScenes() {
  emit('update:modelValue', null)
  emit('change', null)
}
</script>

<template>
  <div class="equip-screen-scene-side" :class="{ 'is-open': open }">
    <button
      type="button"
      class="equip-screen-scene-toggle"
      :title="open ? t('equipScreenPage.scenePanelClose') : t('equipScreenPage.scenePanelOpen')"
      :aria-label="open ? t('equipScreenPage.scenePanelClose') : t('equipScreenPage.scenePanelOpen')"
      @click="togglePanel"
    >
      <MenuUnfoldOutlined v-if="open" />
      <MenuFoldOutlined v-else />
    </button>

    <aside class="equip-screen-scene-panel" aria-labelledby="equip-screen-scene-title">
      <div class="equip-screen-scene-panel__header">
        <h2 id="equip-screen-scene-title" class="equip-screen-scene-panel__title">
          {{ t('equipScreenPage.scenePickerTitle') }}
        </h2>
        <button type="button" class="equip-screen-scene-panel__all-btn" @click="selectAllScenes">
          {{ t('realtimePage.allWorkshops') }}
        </button>
      </div>
      <div class="equip-screen-scene-panel__body">
        <WorkshopTree
          v-if="treeMounted"
          :model-value="modelValue"
          variant="screen"
          fill
          :auto-select-first="false"
          @update:model-value="onTreeChange"
          @change="onTreeChange"
        />
      </div>
    </aside>
  </div>
</template>
