<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchGatewayById, type GatewayRecord } from '@/api/gateway'
import GatewayBindingModal from './components/GatewayBindingModal.vue'

const route = useRoute()
const router = useRouter()

const gateway = ref<GatewayRecord | null>(null)
const loading = ref(false)
const modalOpen = ref(false)

const connectId = computed(() => String(route.query.connectId || ''))

async function loadGateway() {
  if (!connectId.value) {
    gateway.value = null
    modalOpen.value = false
    return
  }
  loading.value = true
  try {
    gateway.value = await fetchGatewayById(connectId.value)
  } catch {
    gateway.value = { id: connectId.value }
  } finally {
    loading.value = false
    modalOpen.value = true
  }
}

function onModalClose(open: boolean) {
  modalOpen.value = open
  if (!open) {
    router.replace('/view/gw_tables')
  }
}

onMounted(loadGateway)

watch(connectId, loadGateway)
</script>

<template>
  <a-spin :spinning="loading" class="binding-route-host">
    <GatewayBindingModal
      :open="modalOpen && Boolean(connectId)"
      :gateway="gateway"
      @update:open="onModalClose"
    />
  </a-spin>
</template>

<style scoped>
.binding-route-host {
  min-height: 120px;
}
</style>
