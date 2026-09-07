<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { fetchEquipById, type EquipRecord } from '@/api/device'
import EquipAttrConfigModal from './components/EquipAttrConfigModal.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const equip = ref<EquipRecord | null>(null)
const modalOpen = ref(false)

const equipId = computed(() => String(route.query.id || route.params.id || ''))

onMounted(async () => {
  if (!equipId.value) {
    return
  }
  equip.value = await fetchEquipById(equipId.value)
  modalOpen.value = true
})

function onClose(open: boolean) {
  modalOpen.value = open
  if (!open) {
    router.back()
  }
}
</script>

<template>
  <div class="equip-attr-page">
    <a-empty v-if="!equipId" :description="t('equipAttrPage.missingEquipId')" />
    <EquipAttrConfigModal
      v-else
      :open="modalOpen"
      :equip="equip"
      @update:open="onClose"
    />
  </div>
</template>
