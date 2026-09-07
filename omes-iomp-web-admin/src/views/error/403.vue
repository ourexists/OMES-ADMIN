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
  <div class="forbidden">
    <a-result
      status="403"
      :title="t('error.forbiddenTitle')"
      :sub-title="t('error.forbiddenDesc')"
    >
      <template #extra>
        <a-button type="primary" @click="goHome">{{ t('error.backHome') }}</a-button>
      </template>
    </a-result>
  </div>
</template>

<style scoped>
.forbidden {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
