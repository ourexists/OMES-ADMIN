<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { LinkOutlined, UserOutlined } from '@ant-design/icons-vue'
import type { InspectPersonRecord } from '@/api/inspect-person'
import { fetchInspectPersonById, saveInspectPerson } from '@/api/inspect-person'
import type { AccountRecord } from '@/api/ucenter'
import { fetchAccountPage } from '@/api/ucenter'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: InspectPersonRecord | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const accountLoading = ref(false)
const accountOptions = ref<AccountRecord[]>([])

const formState = reactive({
  id: null as string | null,
  name: '',
  jobNumber: '',
  mobile: '',
  remark: '',
  linkType: 'existing' as 'existing' | 'sync',
  accountId: undefined as string | undefined,
  accName: '',
  password: '',
  nickName: '',
})

const isEdit = computed(() => Boolean(props.record?.id))
const title = computed(() =>
  isEdit.value ? t('inspectPersonPage.formEdit') : t('inspectPersonPage.formAdd'),
)

async function loadAccountOptions() {
  accountLoading.value = true
  try {
    const result = await fetchAccountPage({ page: 1, pageSize: 500, platform: 'mes-app' })
    accountOptions.value = result.records || []
  } finally {
    accountLoading.value = false
  }
}

function resetForm() {
  formState.id = null
  formState.name = ''
  formState.jobNumber = ''
  formState.mobile = ''
  formState.remark = ''
  formState.linkType = 'existing'
  formState.accountId = undefined
  formState.accName = ''
  formState.password = ''
  formState.nickName = ''
}

async function loadRecord(id: string) {
  loading.value = true
  try {
    const data = await fetchInspectPersonById(id)
    if (!data) {
      return
    }
    formState.id = data.id
    formState.name = data.name || ''
    formState.jobNumber = data.jobNumber || ''
    formState.mobile = data.mobile || ''
    formState.remark = data.remark || ''
    formState.linkType = 'existing'
    formState.accountId = data.accountId || undefined
    await loadAccountOptions()
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  async (open) => {
    if (!open) {
      return
    }
    resetForm()
    if (props.record?.id) {
      await loadRecord(props.record.id)
    } else {
      await loadAccountOptions()
    }
  },
)

watch(
  () => formState.linkType,
  (type) => {
    if (type === 'existing' && accountOptions.value.length === 0) {
      loadAccountOptions()
    }
  },
)

function closeModal() {
  emit('update:open', false)
}

async function onSubmit() {
  const name = formState.name.trim()
  if (!name) {
    message.warning(t('inspectPersonPage.nameRequired'))
    return
  }

  if (formState.linkType === 'sync') {
    if (!formState.accName.trim() || !formState.password.trim()) {
      message.warning(t('inspectPersonPage.syncAccountRequired'))
      return
    }
  }

  saving.value = true
  try {
    const payload = {
      person: {
        id: formState.id || undefined,
        name,
        jobNumber: formState.jobNumber.trim() || undefined,
        mobile: formState.mobile.trim() || undefined,
        remark: formState.remark.trim() || undefined,
      },
      syncAccount: formState.linkType === 'sync',
      accountId: formState.linkType === 'existing' ? formState.accountId || null : undefined,
      accountInfo:
        formState.linkType === 'sync'
          ? {
              accName: formState.accName.trim(),
              password: formState.password,
              nickName: formState.nickName.trim() || name,
            }
          : undefined,
    }
    await saveInspectPerson(payload)
    message.success(t('inspectPersonPage.saveSuccess'))
    emit('success')
    closeModal()
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    :width="640"
    :confirm-loading="saving"
    destroy-on-close
    @cancel="closeModal"
    @ok="onSubmit"
  >
    <a-spin :spinning="loading">
      <a-form layout="vertical" class="person-form">
        <div class="form-section">
          <div class="form-section__title">
            <UserOutlined />
            {{ t('inspectPersonPage.formBasic') }}
          </div>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('inspectPersonPage.colName')" required>
                <a-input
                  v-model:value="formState.name"
                  :placeholder="t('inspectPersonPage.namePlaceholder')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('inspectPersonPage.colJobNumber')">
                <a-input
                  v-model:value="formState.jobNumber"
                  :placeholder="t('inspectPersonPage.jobNumberPlaceholder')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('inspectPersonPage.colMobile')">
                <a-input
                  v-model:value="formState.mobile"
                  :placeholder="t('inspectPersonPage.mobilePlaceholder')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item :label="t('inspectPersonPage.colRemark')">
                <a-textarea
                  v-model:value="formState.remark"
                  :placeholder="t('inspectPersonPage.remarkPlaceholder')"
                  :rows="3"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </div>

        <div class="form-section">
          <div class="form-section__title">
            <LinkOutlined />
            {{ t('inspectPersonPage.formAccount') }}
          </div>
          <a-form-item :label="t('inspectPersonPage.linkType')">
            <a-radio-group v-model:value="formState.linkType">
              <a-radio value="existing">{{ t('inspectPersonPage.linkExisting') }}</a-radio>
              <a-radio value="sync">{{ t('inspectPersonPage.linkSync') }}</a-radio>
            </a-radio-group>
          </a-form-item>

          <template v-if="formState.linkType === 'existing'">
            <a-form-item :label="t('inspectPersonPage.colAccount')">
              <a-select
                v-model:value="formState.accountId"
                allow-clear
                show-search
                :loading="accountLoading"
                :placeholder="t('inspectPersonPage.accountPlaceholder')"
                option-filter-prop="label"
              >
                <a-select-option
                  v-for="acc in accountOptions"
                  :key="acc.id"
                  :value="acc.id"
                  :label="acc.nickName || acc.accName || acc.id"
                >
                  {{ acc.nickName || acc.accName || acc.id }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </template>

          <template v-else>
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item :label="t('inspectPersonPage.accName')" required>
                  <a-input
                    v-model:value="formState.accName"
                    :placeholder="t('inspectPersonPage.accNamePlaceholder')"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="t('inspectPersonPage.password')" required>
                  <a-input-password
                    v-model:value="formState.password"
                    :placeholder="t('inspectPersonPage.passwordPlaceholder')"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="t('inspectPersonPage.nickName')">
                  <a-input
                    v-model:value="formState.nickName"
                    :placeholder="t('inspectPersonPage.nickNamePlaceholder')"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </template>
        </div>
      </a-form>
    </a-spin>

    <template #footer>
      <a-button @click="closeModal">{{ t('inspectPersonPage.cancel') }}</a-button>
      <a-button type="primary" :loading="saving" @click="onSubmit">
        {{ t('inspectPersonPage.save') }}
      </a-button>
    </template>
  </a-modal>
</template>

<style scoped>
.person-form {
  margin-top: 8px;
}

.form-section {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--omes-color-bg-elevated);
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-md);
}

.form-section:last-child {
  margin-bottom: 0;
}

.form-section__title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-weight: 600;
  color: var(--omes-color-text-heading);
}
</style>
