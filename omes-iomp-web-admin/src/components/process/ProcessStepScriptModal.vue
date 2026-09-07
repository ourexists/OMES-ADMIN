<template>
  <a-modal
      v-model:open="openModel"
      :title="modalTitle"
      width="1040px"
      wrap-class-name="step-script-modal-wrap step-script-flow-modal"
      :mask-closable="false"
      destroy-on-close
      @cancel="handleCancel"
  >
    <div class="step-script-modal">
      <a-form layout="vertical" :disabled="readonly">
        <a-form-item label="配置方式">
          <a-radio-group v-model:value="form.configMode" :disabled="readonly">
            <a-radio value="none">无（不配置）</a-radio>
            <a-radio value="flow">流程图配置</a-radio>
            <a-radio value="script">脚本录入</a-radio>
          </a-radio-group>
        </a-form-item>

        <ProcessStepFlowChart
            v-if="form.configMode === 'flow'"
            :flow="form.flow"
            :selected-id="selectedNodeId"
            :readonly="readonly"
            :equipments="stepEquipments"
            @select-node="onSelectNode"
        />

        <div v-else-if="form.configMode === 'script'" class="script-input-toolbar">
          <span class="script-input-hint">编辑完整 stepScript JSON，保存后写入工序 stepScript 字段</span>
          <a-space v-if="!readonly">
            <a-button size="small" @click="insertScriptTemplate">插入模板</a-button>
            <a-button size="small" @click="formatPersistedScript(true)">格式化</a-button>
          </a-space>
        </div>

        <div v-else class="kind-hint none-hint">
          不生成执行脚本，该工序仅保留工序文本。
        </div>
      </a-form>
      <div v-if="stepEngineConfigPreview" class="engine-config-panel">
        <span class="script-preview-label">引擎配置（stepEngineConfig，已落库）</span>
        <pre class="script-part-code">{{ stepEngineConfigPreview }}</pre>
      </div>

      <div v-if="isScriptConfigEnabled(form) && hotReloadResult" class="hot-reload-result">
        <span class="script-preview-label">LiteFlow 链（{{ hotReloadResult.segmentCount }} 段）</span>
        <ul class="chain-list">
          <li v-for="seg in hotReloadResult.segments" :key="seg.chainId">
            <strong>{{ seg.name }}</strong>
            <code>{{ seg.chainId }}</code>
            <span class="chain-meta">{{ seg.driveKind }} → {{ seg.completeKind }}</span>
          </li>
        </ul>
      </div>
    </div>

    <ProcessStepFlowNodeModal
        v-if="form.configMode === 'flow'"
        :open="nodeModalOpen && !!selectedNode"
        :node="selectedNode"
        :readonly="readonly"
        :equipments="stepEquipments"
        @close="closeNodeModal"
    />

    <template #footer>
      <a-button v-if="!readonly && isScriptConfigEnabled(form)" type="link" danger class="footer-clear"
                @click="handleClear">
        清空
      </a-button>
      <a-space>
        <a-button @click="handleCancel">{{ readonly ? '关闭' : '取消' }}</a-button>
        <a-button
            v-if="!readonly && isScriptConfigEnabled(form)"
            :loading="hotReloadLoading"
            @click="handleHotReload"
        >
          应用引擎
        </a-button>
        <a-button v-if="!readonly" type="primary" @click="handleOk">确定</a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import {message} from 'ant-design-vue'
import ProcessStepFlowChart from '@/components/process/ProcessStepFlowChart.vue'
import ProcessStepFlowNodeModal from '@/components/process/ProcessStepFlowNodeModal.vue'
import {hotReloadStepScript} from '@/api/processExecution'
import {
  buildDefaultStepScriptJson,
  buildStepScriptFromForm,
  defaultFlowGraph,
  defaultStepScriptForm,
  formatStepScriptJson,
  isScriptConfigEnabled,
  parseStepScriptToForm,
  syncPersistedScriptText,
  validateStepScriptForm
} from '@/utils/process/processStepScript'

const props = defineProps({
  open: {type: Boolean, default: false},
  stepScript: {type: String, default: ''},
  stepEngineConfig: {type: String, default: ''},
  stepId: {type: String, default: ''},
  stepName: {type: String, default: ''},
  stepNo: {type: [Number, String], default: ''},
  stepEquipments: {type: Array, default: () => []},
  readonly: {type: Boolean, default: false}
})

const emit = defineEmits(['update:open', 'update:stepScript', 'ok'])

const openModel = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v)
})

const form = reactive(defaultStepScriptForm(props.stepEquipments))
const selectedNodeId = ref('')
const nodeModalOpen = ref(false)
const hotReloadLoading = ref(false)
const hotReloadResult = ref(null)
const selectedNode = computed(() => {
  if (!selectedNodeId.value) return null
  return form.flow?.nodes?.find((node) => node.id === selectedNodeId.value) || null
})

const modalTitle = computed(() => {
  const no = props.stepNo != null && props.stepNo !== '' ? `工序 ${props.stepNo}` : '工序'
  const name = props.stepName?.trim()
  return name ? `${no} · ${name} — 驱动引擎` : `${no} — 驱动引擎`
})

const persistedScriptPreview = computed(() => {
  if (!isScriptConfigEnabled(form) || form.configMode !== 'flow') return ''
  try {
    const json = buildStepScriptFromForm(form)
    return json ? formatStepScriptJson(json) : '（请完善流程图节点与连线）'
  } catch {
    return '（脚本生成失败，请检查流程图）'
  }
})

const stepEngineConfigPreview = computed(() => {
  const text = props.stepEngineConfig?.trim()
  if (!text) return ''
  return formatStepScriptJson(text)
})

function syncScriptEnabled() {
  form.scriptEnabled = isScriptConfigEnabled(form)
}

watch(
    () => props.open,
    (visible) => {
      if (!visible) return
      const parsed = parseStepScriptToForm(props.stepScript, props.stepEquipments)
      Object.assign(form, defaultStepScriptForm(props.stepEquipments), parsed)
      syncScriptEnabled()
      hotReloadResult.value = null
      if (parsed._parseError) {
        message.warning(parsed._parseError)
      }
      if (form.configMode === 'none') {
        form.flow = defaultFlowGraph(props.stepEquipments)
        if (props.stepScript?.trim()) {
          form.configMode = 'script'
          syncScriptEnabled()
        }
      } else if (form.configMode === 'flow') {
        if (!form.flow?.nodes?.length) {
          form.flow = defaultFlowGraph(props.stepEquipments)
        }
        syncPersistedScriptText(form)
      }
      clearSelection()
    }
)

watch(
    () => form.configMode,
    (mode, oldMode) => {
      if (mode === 'script' && oldMode === 'flow') {
        syncPersistedScriptText(form)
        clearSelection()
      }
      if (mode === 'flow' && oldMode === 'script') {
        try {
          const parsed = parseStepScriptToForm(form.scriptText, props.stepEquipments)
          if (!parsed._parseError && parsed.flow?.nodes?.length) {
            form.flow = parsed.flow
            syncPersistedScriptText(form)
          }
        } catch {
          message.warning('脚本无法解析为流程图，请检查 JSON')
        }
      }
      if (mode === 'none') {
        clearSelection()
      }
      syncScriptEnabled()
    }
)

watch(
    () => form.flow,
    () => {
      if (form.configMode === 'flow') {
        syncPersistedScriptText(form)
      }
    },
    {deep: true}
)

function onSelectNode(node) {
  if (selectedNodeId.value === node.id && nodeModalOpen.value) {
    closeNodeModal()
    return
  }
  selectedNodeId.value = node.id
  nodeModalOpen.value = true
}

function clearSelection() {
  selectedNodeId.value = ''
  nodeModalOpen.value = false
}

function closeNodeModal() {
  nodeModalOpen.value = false
}

function insertScriptTemplate() {
  form.scriptText = buildDefaultStepScriptJson(props.stepEquipments)
}

function formatPersistedScript(showTip = true) {
  const before = form.scriptText
  form.scriptText = formatStepScriptJson(form.scriptText)
  if (showTip && before?.trim() && form.scriptText !== before) {
    message.success('已格式化入库脚本')
  }
}

function handleCancel() {
  openModel.value = false
  clearSelection()
}

function handleClear() {
  Object.assign(form, defaultStepScriptForm(props.stepEquipments))
  hotReloadResult.value = null
  emit('update:stepScript', '')
  emit('ok', '')
  openModel.value = false
  clearSelection()
}

async function handleHotReload() {
  const err = validateStepScriptForm(form, props.stepEquipments)
  if (err) {
    message.warning(err)
    return
  }
  let json
  try {
    json = buildStepScriptFromForm(form)
  } catch {
    message.warning('脚本 JSON 格式无效')
    return
  }
  if (!json) {
    message.warning('请完善引擎配置')
    return
  }
  hotReloadLoading.value = true
  try {
    const res = await hotReloadStepScript({
      stepId: props.stepId || undefined,
      stepScript: json
    })
    hotReloadResult.value = res.data
    message.success(`已注册 ${res.data?.segmentCount ?? 0} 段 LiteFlow 链`)
  } catch (e) {
    message.error(e?.message || '热刷新失败')
  } finally {
    hotReloadLoading.value = false
  }
}

function handleOk() {
  if (form.configMode === 'none') {
    emit('update:stepScript', '')
    emit('ok', '')
    openModel.value = false
    return
  }
  const err = validateStepScriptForm(form, props.stepEquipments)
  if (err) {
    message.warning(err)
    return
  }
  let json
  try {
    json = buildStepScriptFromForm(form)
  } catch {
    message.warning('脚本 JSON 格式无效')
    return
  }
  if (!json) {
    message.warning('请完善引擎配置')
    return
  }
  if (form.configMode === 'script') {
    form.scriptText = formatStepScriptJson(json)
  }
  emit('update:stepScript', json)
  emit('ok', json)
  openModel.value = false
  clearSelection()
}
</script>

<style scoped>
.step-script-modal {
  padding-top: 4px;
}

.kind-hint.none-hint {
  margin-top: 0;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.script-input-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.script-input-hint {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.script-input-textarea {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  background: #fafbfd;
}

.script-persist-panel {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
}

.script-part-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.script-part-code {
  margin: 0;
  padding: 10px 12px;
  background: #f6f8fa;
  border-radius: 6px;
  border: 1px solid #eef1f6;
  max-height: 360px;
  overflow: auto;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 11px;
  line-height: 1.5;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-all;
}

.script-preview-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.65);
}

.script-persist-hint {
  margin: 0;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.engine-config-panel {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
}

.hot-reload-result {
  margin-top: 12px;
  padding: 10px 12px;
  background: #f0fdf4;
  border-radius: 6px;
  border: 1px solid #bbf7d0;
}

.chain-list {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  color: #334155;
}

.chain-list li {
  margin-bottom: 6px;
}

.chain-list code {
  display: block;
  font-size: 11px;
  color: #166534;
  word-break: break-all;
}

.chain-meta {
  display: block;
  font-size: 11px;
  color: rgba(0, 0, 0, 0.45);
}

.footer-clear {
  padding-left: 0;
}
</style>

<style>
.step-script-modal-wrap .ant-modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.step-script-flow-modal .ant-modal-body {
  max-height: calc(100vh - 160px);
  overflow-y: auto;
}
</style>
