<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="not-found">
    <a-result
      status="404"
      :title="t('error.notFoundTitle')"
      :sub-title="t('error.notFoundDesc')"
    >
      <template #extra>
        <a-button type="primary" @click="goHome">{{ t('error.backHome') }}</a-button>
      </template>
    </a-result>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
