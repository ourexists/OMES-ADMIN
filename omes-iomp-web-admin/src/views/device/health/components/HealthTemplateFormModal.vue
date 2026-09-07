<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  AlertOutlined,
  ClockCircleOutlined,
  DashboardOutlined,
  FieldTimeOutlined,
  HeartOutlined,
  ReloadOutlined,
  SafetyCertificateOutlined,
  ToolOutlined,
} from '@ant-design/icons-vue'
import type { AlarmLevelItem, HealthRuleTemplate } from '@/api/equip-health'
import {
  fetchDefaultHealthTemplate,
  fetchHealthAlarmLevels,
  fetchHealthRuleTemplates,
  saveHealthRuleTemplate,
} from '@/api/equip-health'
import { defaultAlarmPenalty, mergeHealthConfig } from '@/utils/equip-health-defaults'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: HealthRuleTemplate | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const alarmLevels = ref<AlarmLevelItem[]>([])

const formState = reactive({
  id: '' as string | null,
  name: '',
  config: mergeHealthConfig(),
})

const isEdit = computed(() => Boolean(props.record?.id))
const title = computed(() =>
  isEdit.value ? t('equipHealth.templateEdit', '编辑模板') : t('equipHealth.templateAdd', '新增模板'),
)

function alarmPenalty(level: AlarmLevelItem, field: 'count' | 'perMin'): number {
  const key = String(level.code)
  const defaults = defaultAlarmPenalty(level)
  const stored = formState.config.alarmLevelPenalties?.[key]
  const value = stored?.[field]
  return value != null ? value : defaults[field]
}

function setAlarmPenalty(level: AlarmLevelItem, field: 'count' | 'perMin', value: number | null) {
  const key = String(level.code)
  if (!formState.config.alarmLevelPenalties) {
    formState.config.alarmLevelPenalties = {}
  }
  const current = formState.config.alarmLevelPenalties[key] || {}
  formState.config.alarmLevelPenalties[key] = {
    ...current,
    [field]: value ?? defaultAlarmPenalty(level)[field],
  }
}

function fillForm(template: HealthRuleTemplate | null) {
  formState.id = template?.id || null
  formState.name = template?.name || ''
  formState.config = mergeHealthConfig(template?.config)
}

async function loadAlarmLevels() {
  if (alarmLevels.value.length > 0) {
    return
  }
  const list = await fetchHealthAlarmLevels()
  alarmLevels.value = Array.isArray(list) ? list : []
}

async function loadFormData() {
  loading.value = true
  try {
    await loadAlarmLevels()
    if (props.record?.id) {
      const list = await fetchHealthRuleTemplates()
      const found = Array.isArray(list) ? list.find((item) => item.id === props.record?.id) : null
      fillForm(found || props.record)
      return
    }
    const defaults = await fetchDefaultHealthTemplate()
    fillForm(defaults)
    formState.id = null
    formState.name = ''
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      loadFormData()
    }
  },
)

function handleClose() {
  emit('update:open', false)
}

function handleReset() {
  loadFormData()
}

async function applyIndustrialPreset() {
  loading.value = true
  try {
    const preset = await fetchDefaultHealthTemplate()
    const currentId = formState.id
    const currentName = formState.name
    fillForm(preset)
    if (currentId) {
      formState.id = currentId
    }
    if (currentName) {
      formState.name = currentName
    }
    message.success(t('equipHealth.msg.industrialPresetApplied', '已填入工业常用预设'))
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formState.name.trim()) {
    message.warning(t('equipHealth.templateName', '模板名称'))
    return
  }
  saving.value = true
  try {
    await saveHealthRuleTemplate({
      id: formState.id,
      name: formState.name.trim(),
      config: formState.config,
    })
    message.success(t('healthPage.saveSuccess'))
    emit('success')
    emit('update:open', false)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    width="1120px"
    destroy-on-close
    :mask-closable="false"
    class="health-template-modal"
    @cancel="handleClose"
  >
    <a-spin :spinning="loading">
      <div class="health-form">
        <!-- 基础设置 -->
        <section class="section section--basic">
          <div class="section__head">
            <DashboardOutlined class="section__icon section__icon--basic" />
            <span>{{ t('equipHealth.sectionBasic', '基础设置') }}</span>
          </div>
          <div class="basic-bar">
            <div class="param param--grow">
              <label class="param__label required">{{ t('equipHealth.templateName', '模板名称') }}</label>
              <a-input v-model:value="formState.name" allow-clear placeholder="请输入模板名称" />
            </div>
            <div class="param param--fixed">
              <label class="param__label">{{ t('equipHealth.periodHours', '统计周期(小时)') }}</label>
              <div class="param__control">
                <a-input-number v-model:value="formState.config.periodHours" :min="1" class="input-num" />
                <span class="param__unit">小时</span>
              </div>
              <p class="param__desc">{{ t('equipHealth.desc.periodHours') }}</p>
            </div>
          </div>
        </section>

        <div class="form-split">
          <!-- 报警维度 -->
          <section class="section">
            <div class="section__head">
              <AlertOutlined class="section__icon section__icon--alarm" />
              <span>{{ t('equipHealth.sectionAlarm', '报警维度') }}</span>
            </div>
            <div class="param-grid param-grid--3">
              <div class="param">
                <label class="param__label">{{ t('equipHealth.alarmCountPenalty', '报警次数扣分') }}</label>
                <div class="param__control">
                  <a-input-number v-model:value="formState.config.alarmCountPenalty" :min="0" class="input-num" />
                  <span class="param__unit">分/次</span>
                </div>
                <p class="param__desc">{{ t('equipHealth.desc.alarmCountPenalty') }}</p>
              </div>
              <div class="param">
                <label class="param__label">{{ t('equipHealth.alarmDurationPenaltyPerMin', '时长扣分/分钟') }}</label>
                <div class="param__control">
                  <a-input-number
                    v-model:value="formState.config.alarmDurationPenaltyPerMin"
                    :min="0"
                    :step="0.1"
                    class="input-num"
                  />
                  <span class="param__unit">分/分钟</span>
                </div>
                <p class="param__desc">{{ t('equipHealth.desc.alarmDurationPenaltyPerMin') }}</p>
              </div>
              <div class="param">
                <label class="param__label">{{ t('equipHealth.alarmMaxDeduction', '报警最大扣分') }}</label>
                <div class="param__control">
                  <a-input-number v-model:value="formState.config.alarmMaxDeduction" :min="0" class="input-num" />
                  <span class="param__unit">分（上限）</span>
                </div>
                <p class="param__desc">{{ t('equipHealth.desc.alarmMaxDeduction') }}</p>
              </div>
            </div>
            <div class="alarm-block">
              <div class="alarm-block__title">{{ t('equipHealth.alarmLevelPenaltySection', '按报警等级扣分') }}</div>
              <table class="alarm-table">
                <thead>
                  <tr>
                    <th>等级</th>
                    <th>{{ t('equipHealth.unit.perCount', '分/次') }}</th>
                    <th>{{ t('equipHealth.unit.perMin', '分/分钟') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="level in alarmLevels" :key="level.code">
                    <td><a-tag>{{ level.desc || level.code }}</a-tag></td>
                    <td>
                      <a-input-number
                        :value="alarmPenalty(level, 'count')"
                        :min="0"
                        class="input-num-table"
                        @update:value="(v: number | null) => setAlarmPenalty(level, 'count', v)"
                      />
                    </td>
                    <td>
                      <a-input-number
                        :value="alarmPenalty(level, 'perMin')"
                        :min="0"
                        :step="0.1"
                        class="input-num-table"
                        @update:value="(v: number | null) => setAlarmPenalty(level, 'perMin', v)"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
              <p class="param__desc">{{ t('equipHealth.desc.alarmLevelPenaltySection') }}</p>
            </div>
          </section>

          <!-- 运行维度 / 在线 / 巡检 -->
          <section class="section section--stack">
            <div class="subsection">
              <div class="section__head section__head--sub">
                <ToolOutlined class="section__icon section__icon--run" />
                <span>{{ t('equipHealth.sectionRun', '运行维度') }}</span>
              </div>
              <div class="param-grid param-grid--2">
                <div class="param">
                  <label class="param__label">{{ t('equipHealth.runRatioWeight', '运行率权重') }}</label>
                  <div class="param__control">
                    <a-input-number
                      v-model:value="formState.config.runRatioWeight"
                      :min="0"
                      :max="100"
                      class="input-num"
                    />
                    <span class="param__unit">分</span>
                  </div>
                  <p class="param__desc">{{ t('equipHealth.desc.runRatioWeight') }}</p>
                </div>
                <div class="param">
                  <label class="param__label">{{ t('equipHealth.expectedRunRatio', '期望运行率') }}</label>
                  <div class="param__control">
                    <a-input-number
                      v-model:value="formState.config.expectedRunRatio"
                      :min="0"
                      :max="100"
                      class="input-num"
                    />
                    <span class="param__unit">%</span>
                  </div>
                  <p class="param__desc">{{ t('equipHealth.desc.expectedRunRatio') }}</p>
                </div>
              </div>
            </div>

            <div class="subsection">
              <div class="section__head section__head--sub">
                <ClockCircleOutlined class="section__icon section__icon--online" />
                <span>{{ t('equipHealth.sectionOnline', '在线维度') }}</span>
              </div>
              <div class="param">
                <label class="param__label">{{ t('equipHealth.onlineRatioWeight', '在线率权重') }}</label>
                <div class="param__control">
                  <a-input-number
                    v-model:value="formState.config.onlineRatioWeight"
                    :min="0"
                    :max="100"
                    class="input-num"
                  />
                  <span class="param__unit">分（权重）</span>
                </div>
                <p class="param__desc">{{ t('equipHealth.desc.onlineRatioWeight') }}</p>
              </div>
            </div>

            <div class="subsection">
              <div class="section__head section__head--sub">
                <SafetyCertificateOutlined class="section__icon section__icon--inspection" />
                <span>巡检维度</span>
              </div>
              <div class="param-grid param-grid--2">
                <div class="param">
                  <label class="param__label">巡检权重</label>
                  <div class="param__control">
                    <a-input-number
                      v-model:value="formState.config.inspectionWeight"
                      :min="0"
                      :max="100"
                      class="input-num"
                    />
                    <span class="param__unit">分</span>
                  </div>
                </div>
                <div class="param">
                  <label class="param__label">无巡检记录扣分</label>
                  <div class="param__control">
                    <a-input-number
                      v-model:value="formState.config.inspectionNoRecordPenalty"
                      :min="0"
                      class="input-num"
                    />
                    <span class="param__unit">分</span>
                  </div>
                </div>
              </div>
              <p class="param__desc">
                周期内无巡检记录时的扣分（上限不超过权重）；有记录时按最近巡检记录平均得分扣分。
              </p>
            </div>
          </section>
        </div>

        <!-- 生命周期 -->
        <section class="section">
          <div class="section__head">
            <FieldTimeOutlined class="section__icon section__icon--lifecycle" />
            <span>{{ t('equipHealth.sectionLifecycle', '生命周期') }}</span>
            <div class="section__head-extra">
              <span class="param__label">{{ t('equipHealth.lifecycleMaxDeduction', '扣分上限') }}</span>
              <a-input-number v-model:value="formState.config.lifecycleMaxDeduction" :min="0" class="input-num" />
              <span class="param__unit">分</span>
            </div>
          </div>
          <p class="section__intro">{{ t('equipHealth.desc.lifecycleMaxDeduction') }}</p>
          <div class="metric-row">
            <div class="metric-card">
              <div class="metric-card__title">{{ t('equipHealth.lifeYearThreshold', '使用年限阈值') }}</div>
              <div class="metric-card__fields">
                <div class="metric-card__field">
                  <span>阈值（年）</span>
                  <a-input-number v-model:value="formState.config.lifeYearThreshold" :min="0" class="input-num" />
                </div>
                <div class="metric-card__field">
                  <span>分/年</span>
                  <a-input-number
                    v-model:value="formState.config.lifeYearPenaltyPerYear"
                    :min="0"
                    :step="0.1"
                    class="input-num"
                  />
                </div>
                <div class="metric-card__field">
                  <span>扣分上限</span>
                  <a-input-number v-model:value="formState.config.lifeYearMaxDeduction" :min="0" class="input-num" />
                </div>
              </div>
              <p class="param__desc">{{ t('equipHealth.desc.lifeYear') }}</p>
            </div>
            <div class="metric-card">
              <div class="metric-card__title">{{ t('equipHealth.totalRunHoursThreshold', '累计运行小时阈值') }}</div>
              <div class="metric-card__fields">
                <div class="metric-card__field">
                  <span>阈值（小时）</span>
                  <a-input-number
                    v-model:value="formState.config.totalRunHoursThreshold"
                    :min="0"
                    class="input-num"
                  />
                </div>
                <div class="metric-card__field">
                  <span>分/千小时</span>
                  <a-input-number
                    v-model:value="formState.config.totalRunHoursPenaltyPer1000"
                    :min="0"
                    :step="0.1"
                    class="input-num"
                  />
                </div>
                <div class="metric-card__field">
                  <span>扣分上限</span>
                  <a-input-number
                    v-model:value="formState.config.totalRunHoursMaxDeduction"
                    :min="0"
                    class="input-num"
                  />
                </div>
              </div>
              <p class="param__desc">{{ t('equipHealth.desc.totalRunHours') }}</p>
            </div>
            <div class="metric-card">
              <div class="metric-card__title">{{ t('equipHealth.startStopCountThreshold', '启停总次数阈值') }}</div>
              <div class="metric-card__fields">
                <div class="metric-card__field">
                  <span>阈值（次）</span>
                  <a-input-number
                    v-model:value="formState.config.startStopCountThreshold"
                    :min="0"
                    class="input-num"
                  />
                </div>
                <div class="metric-card__field">
                  <span>分/千次</span>
                  <a-input-number
                    v-model:value="formState.config.startStopCountPenaltyPer1000"
                    :min="0"
                    :step="0.1"
                    class="input-num"
                  />
                </div>
                <div class="metric-card__field">
                  <span>扣分上限</span>
                  <a-input-number
                    v-model:value="formState.config.startStopCountMaxDeduction"
                    :min="0"
                    class="input-num"
                  />
                </div>
              </div>
              <p class="param__desc">{{ t('equipHealth.desc.startStopCount') }}</p>
            </div>
          </div>
        </section>

        <!-- 等级阈值 -->
        <section class="section section--threshold">
          <div class="section__head">
            <HeartOutlined class="section__icon section__icon--threshold" />
            <span>{{ t('equipHealth.sectionThreshold', '等级阈值') }}</span>
          </div>
          <div class="threshold-row">
            <div class="threshold-card threshold-card--healthy">
              <span class="threshold-card__label">{{ t('equipHealth.healthyThreshold', '健康线') }}</span>
              <div class="threshold-card__control">
                <span class="threshold-card__hint">得分 ≥</span>
                <a-input-number
                  v-model:value="formState.config.healthyThreshold"
                  :min="0"
                  :max="100"
                  class="input-num threshold-card__input"
                />
              </div>
              <p class="param__desc">{{ t('equipHealth.desc.healthyThreshold') }}</p>
            </div>
            <div class="threshold-card threshold-card--attention">
              <span class="threshold-card__label">{{ t('equipHealth.attentionThreshold', '关注线') }}</span>
              <div class="threshold-card__control">
                <span class="threshold-card__hint">得分 ≥</span>
                <a-input-number
                  v-model:value="formState.config.attentionThreshold"
                  :min="0"
                  :max="100"
                  class="input-num threshold-card__input"
                />
              </div>
              <p class="param__desc">{{ t('equipHealth.desc.attentionThreshold') }}</p>
            </div>
            <div class="threshold-card threshold-card--warning">
              <span class="threshold-card__label">{{ t('equipHealth.warningThreshold', '预警线') }}</span>
              <div class="threshold-card__control">
                <span class="threshold-card__hint">得分 ≥</span>
                <a-input-number
                  v-model:value="formState.config.warningThreshold"
                  :min="0"
                  :max="100"
                  class="input-num threshold-card__input"
                />
              </div>
              <p class="param__desc">{{ t('equipHealth.desc.warningThreshold') }}</p>
            </div>
            <div class="threshold-card threshold-card--fault">
              <span class="threshold-card__label">故障</span>
              <div class="threshold-card__control threshold-card__control--static">
                <span class="threshold-card__hint">得分 &lt; 预警线</span>
              </div>
              <p class="param__desc">低于预警线时判定为故障等级。</p>
            </div>
          </div>
        </section>
      </div>
    </a-spin>

    <template #footer>
      <div class="modal-footer">
        <a-button class="modal-footer__preset" @click="applyIndustrialPreset">
          {{ t('equipHealth.fillIndustrialPreset', '填入工业常用预设') }}
        </a-button>
        <a-space>
          <a-button @click="handleClose">{{ t('healthPage.cancel') }}</a-button>
          <a-button @click="handleReset">
            <ReloadOutlined />
            {{ t('healthPage.reset') }}
          </a-button>
          <a-button type="primary" :loading="saving" @click="handleSubmit">
            {{ t('healthPage.confirm') }}
          </a-button>
        </a-space>
      </div>
    </template>
  </a-modal>
</template>

<style scoped>
.health-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section {
  min-width: 0;
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-lg);
  background: var(--omes-color-bg-container);
  padding: 14px 16px 16px;
}

.section__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--omes-color-text);
}

.section__head--sub {
  margin-bottom: 10px;
  font-size: 13px;
}

.section__head-extra {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 400;
}

.section__intro {
  margin: -6px 0 12px;
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
  line-height: 1.5;
}

.section__icon {
  font-size: 16px;
}

.section__icon--basic { color: var(--omes-color-primary); }
.section__icon--alarm { color: #fa541c; }
.section__icon--run { color: var(--omes-color-accent-purple-from); }
.section__icon--online { color: var(--omes-color-accent-cyan-from); }
.section__icon--inspection { color: var(--omes-color-success); }
.section__icon--lifecycle { color: #eb2f96; }
.section__icon--threshold { color: #cf1322; }

.basic-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 240px;
  gap: 16px 24px;
  align-items: start;
}

.form-split {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  min-width: 0;
}

.section--stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
}

.subsection {
  padding-bottom: 14px;
  border-bottom: 1px dashed var(--omes-color-border);
}

.subsection:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.param-grid {
  display: grid;
  gap: 12px 16px;
}

.param-grid--2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.param-grid--3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.param-grid--3 .param {
  min-width: 0;
}

.param-grid--3 .input-num {
  width: 100%;
  max-width: 108px;
}

.param--grow {
  min-width: 0;
}

.param--fixed {
  min-width: 0;
}

.param__label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--omes-color-text-secondary);
}

.param__label.required::before {
  content: '*';
  color: var(--omes-color-error);
  margin-right: 4px;
}

.param__control {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.param__unit {
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
  white-space: nowrap;
}

.param__desc {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
  line-height: 1.5;
}

.input-num {
  width: 108px;
}

.input-num-table {
  width: 100%;
  max-width: 120px;
}

.alarm-block {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--omes-color-border);
}

.alarm-block__title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--omes-color-text);
}

.alarm-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-md);
  overflow: hidden;
}

.alarm-table th,
.alarm-table td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--omes-color-border);
  text-align: left;
}

.alarm-table th {
  background: var(--omes-color-bg-elevated);
  color: var(--omes-color-text-quaternary);
  font-weight: 500;
}

.alarm-table tr:last-child td {
  border-bottom: none;
}

.metric-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  min-width: 0;
}

.metric-card {
  min-width: 0;
  overflow: hidden;
  padding: 12px 14px;
  background: var(--omes-color-bg-elevated);
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-md);
}

.metric-card__title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--omes-color-text);
  line-height: 1.4;
  word-break: break-word;
}

.metric-card__fields {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.metric-card__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
}

.metric-card__field .input-num {
  width: 100%;
  max-width: 100%;
}

.metric-card__field :deep(.ant-input-number) {
  width: 100%;
}

.metric-card__field :deep(.ant-input-number-input) {
  padding-inline: 8px;
}

.threshold-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  min-width: 0;
}

.threshold-card {
  min-width: 0;
  overflow: hidden;
  padding: 14px;
  border-radius: var(--omes-radius-md);
  border: 1px solid var(--omes-color-border);
}

.threshold-card--healthy {
  background: linear-gradient(180deg, #f6ffed 0%, #fff 100%);
  border-color: #b7eb8f;
}

.threshold-card--attention {
  background: linear-gradient(180deg, var(--omes-color-primary-bg) 0%, #fff 100%);
  border-color: #91caff;
}

.threshold-card--warning {
  background: linear-gradient(180deg, #fff7e6 0%, #fff 100%);
  border-color: #ffd591;
}

.threshold-card--fault {
  background: linear-gradient(180deg, #fff1f0 0%, #fff 100%);
  border-color: #ffa39e;
}

.threshold-card__label {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--omes-color-text);
}

.threshold-card__control {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 32px;
}

.threshold-card__control--static {
  padding: 4px 0;
}

.threshold-card__input {
  width: 88px;
}

.threshold-card__hint {
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
  white-space: nowrap;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.modal-footer__preset {
  margin-right: auto;
}

@media (max-width: 900px) {
  .form-split,
  .metric-row,
  .threshold-row,
  .param-grid--3,
  .basic-bar {
    grid-template-columns: 1fr;
  }

  .param-grid--2 {
    grid-template-columns: 1fr;
  }
}
</style>

<style>
.health-template-modal .ant-modal-body {
  max-height: calc(88vh - 120px);
  overflow-y: auto;
  padding-top: 12px;
  padding-bottom: 8px;
}

.health-template-modal .ant-modal-body::-webkit-scrollbar {
  width: 6px;
}

.health-template-modal .ant-modal-body::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}
</style>
