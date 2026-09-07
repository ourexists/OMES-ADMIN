/**
 * 工序执行脚本 v3：流程图配置（驱动条件单条 / 执行动作 / 完成动作 / 异常条件）
 */

export const SCRIPT_VERSION = 3

export const DRIVE_NODE_LABEL = '驱动条件'

export const CONDITION_KIND_OPTIONS = [
    {value: 'NONE', label: '无'},
    {value: 'TIME', label: '时间'},
    {value: 'EVENT', label: '事件'}
]

/** 完成动作方式 */
export const COMPLETE_ACTION_OPTIONS = [
    {value: 'AUTO_NEXT', label: '自动下一工序'},
    {value: 'MANUAL_CONFIRM', label: '等待人工确认'}
]

export const ACTION_KIND_OPTIONS = [
    {value: 'RAMP_TIME', label: '斜坡控制（时间）'},
    {value: 'PID_CONTROL', label: '直接控制（PID）'}
]

export const EVENT_VARIABLE_OPTIONS = [
    {value: 'temp', label: '温度'},
    {value: 'pressure', label: '压力'},
    {value: 'setpoint', label: '设定值'}
]

/** 设备属性/过程量（属性体系完善前默认温度、压力） */
export const RAMP_VARIABLE_OPTIONS = [
    {value: 'temp', label: '温度'},
    {value: 'pressure', label: '压力'}
]

export function normalizeRampVariable(value) {
    const v = (value || 'temp').toLowerCase()
    return RAMP_VARIABLE_OPTIONS.some((o) => o.value === v) ? v : 'temp'
}

export function resolveProcessVariableLabel(variable) {
    const v = normalizeRampVariable(variable)
    const opt = RAMP_VARIABLE_OPTIONS.find((o) => o.value === v)
        || EVENT_VARIABLE_OPTIONS.find((o) => o.value === v)
    return opt?.label || v
}

export const EVENT_OPERATOR_OPTIONS = [
    {value: '>=', label: '≥'},
    {value: '<=', label: '≤'},
    {value: '>', label: '>'},
    {value: '<', label: '<'},
    {value: '==', label: '='}
]

/** 单条设备条件的逻辑：比较阈值 / 数值范围 */
export const EVENT_LOGIC_TYPE_OPTIONS = [
    {value: 'COMPARE', label: '比较'},
    {value: 'RANGE', label: '范围'}
]

export function defaultRampSegment() {
    return {to: undefined, duration: 60, holdDuration: undefined}
}

export function defaultAfterControl() {
    return {
        enabled: false,
        equipmentCode: '',
        variable: 'temp',
        target: undefined,
        useAdvancedPid: false,
        kp: undefined,
        ki: undefined,
        kd: undefined
    }
}

/** 斜坡第 idx 段起点：首段为绑定设备当前过程量（运行时读取），其后为上一段 to */
export function rampSegmentFromValue(action, index) {
    if (!action || index < 0) return undefined
    if (index === 0) return null
    const prev = action.rampSegments?.[index - 1]
    return prev?.to
}

export function rampSegmentFromLabel(action, index) {
    if (!action || index < 0) return '—'
    if (index === 0) {
        return `设备${resolveProcessVariableLabel(action.rampVariable)}`
    }
    const v = rampSegmentFromValue(action, index)
    return v != null && v !== '' ? String(v) : '—'
}

/** 斜坡最终目标值（末段 to） */
export function rampFinalValue(action) {
    const segs = action?.rampSegments || []
    if (!segs.length) return undefined
    return segs[segs.length - 1]?.to
}

export function buildStepEquipmentOptions(equipments) {
    return (equipments || [])
        .filter((eq) => eq?.equipmentCode || eq?.equipmentName)
        .map((eq) => ({
            value: eq.equipmentCode || eq.equipmentName,
            label: eq.equipmentName || eq.equipmentCode
        }))
}

export function resolveEquipmentLabel(equipments, code) {
    if (!code) return ''
    const eq = (equipments || []).find((item) => (item.equipmentCode || item.equipmentName) === code)
    return eq?.equipmentName || eq?.equipmentCode || code
}

export function defaultEventEquipmentCode(equipments) {
    return buildStepEquipmentOptions(equipments)[0]?.value || ''
}

export function defaultEventConditionItem(equipments) {
    return {
        equipmentCode: defaultEventEquipmentCode(equipments),
        logicType: 'COMPARE',
        variable: 'temp',
        operator: '>=',
        value: undefined,
        min: undefined,
        max: undefined
    }
}

function normalizeEventItem(raw) {
    const logicType = raw?.logicType === 'RANGE' ? 'RANGE' : 'COMPARE'
    return {
        equipmentCode: raw?.equipmentCode || '',
        logicType,
        variable: (raw?.variable || 'temp').toLowerCase(),
        operator: raw?.operator || '>=',
        value: raw?.value,
        min: raw?.min,
        max: raw?.max
    }
}

function isEventItemComplete(item) {
    if (!item?.equipmentCode) return false
    if (item.logicType === 'RANGE') {
        return item.min != null && item.max != null && item.min !== '' && item.max !== ''
    }
    return item.value != null && item.value !== ''
}

export function defaultCompleteAction(kind = 'AUTO_NEXT') {
    return {kind}
}

const COMPLETE_ACTION_KINDS = new Set(['AUTO_NEXT', 'MANUAL_CONFIRM'])

export function isCompleteActionKind(kind) {
    return COMPLETE_ACTION_KINDS.has((kind || '').toUpperCase())
}

export function normalizeCompleteAction(cond) {
    if (!cond?.kind) return defaultCompleteAction()
    const k = String(cond.kind).toUpperCase()
    if (isCompleteActionKind(k)) return {kind: k}
    return {kind: ''}
}

export function completeActionFromJson(json) {
    if (!json?.kind) return {kind: ''}
    return {kind: String(json.kind).toUpperCase()}
}

export function defaultCondition(kind = 'NONE', equipments) {
    const base = {kind, duration: 60}
    if (kind === 'EVENT') {
        return {...base, ...defaultDriveEventCondition(equipments)}
    }
    return base
}

/** 驱动条件事件类型（多条设备条件） */
export function defaultDriveEventCondition(equipments) {
    return {
        kind: 'EVENT',
        eventConditions: [defaultEventConditionItem(equipments)]
    }
}

export function defaultAction(kind = 'RAMP_TIME', equipments) {
    return {
        kind,
        rampEquipmentCode: defaultEventEquipmentCode(equipments),
        rampVariable: 'temp',
        pidEquipmentCode: defaultEventEquipmentCode(equipments),
        pidVariable: 'temp',
        rampSegments: [defaultRampSegment()],
        afterControl: defaultAfterControl(),
        target: undefined,
        kp: undefined,
        ki: undefined,
        kd: undefined,
        useAdvancedPid: false
    }
}

export function defaultFlowNode(type = 'drive', index = 1, position, equipments) {
    const titleMap = {
        start: '开始',
        end: '结束',
        drive: DRIVE_NODE_LABEL,
        action: '执行动作',
        complete: '完成动作',
        exception: '异常分支'
    }
    const toneMap = {
        start: 'terminal',
        end: 'terminal',
        drive: 'drive',
        action: 'action',
        complete: 'complete',
        exception: 'exception'
    }
    const node = {
        id: type === 'start' || type === 'end' ? type : `${type}-${Date.now()}-${index}`,
        type,
        title: titleMap[type] || '节点',
        tone: toneMap[type] || 'drive',
        position: position || {x: type === 'start' ? 40 : type === 'end' ? 680 : 220, y: 80}
    }
    if (type === 'drive') {
        node.condition = defaultCondition('NONE', equipments)
    } else if (type === 'complete') {
        node.completeAction = defaultCompleteAction('AUTO_NEXT')
    } else if (type === 'exception') {
        node.condition = defaultCondition('NONE', equipments)
    } else if (type === 'action') {
        node.action = defaultAction('RAMP_TIME', equipments)
    }
    return node
}

export function defaultFlowGraph(equipments) {
    const start = defaultFlowNode('start', 1, {x: 40, y: 70}, equipments)
    const drive = defaultFlowNode('drive', 1, {x: 240, y: 70}, equipments)
    drive.id = 'drive-1'
    const action = defaultFlowNode('action', 1, {x: 440, y: 70}, equipments)
    action.id = 'action-1'
    const complete = defaultFlowNode('complete', 1, {x: 640, y: 70}, equipments)
    complete.id = 'complete-1'
    const end = defaultFlowNode('end', 1, {x: 840, y: 70}, equipments)
    const exception = defaultFlowNode('exception', 1, {x: 440, y: 210}, equipments)
    exception.id = 'exception-1'
    return {
        nodes: [start, drive, action, complete, end, exception],
        edges: [
            {
                id: 'e-start-drive-1',
                source: 'start',
                sourceHandle: 'right',
                target: 'drive-1',
                targetHandle: 'left',
                type: 'smoothstep',
                animated: true
            },
            {
                id: 'e-drive-1-action-1',
                source: 'drive-1',
                sourceHandle: 'right',
                target: 'action-1',
                targetHandle: 'left',
                type: 'smoothstep',
                animated: true
            },
            {
                id: 'e-action-1-complete-1',
                source: 'action-1',
                sourceHandle: 'right',
                target: 'complete-1',
                targetHandle: 'left',
                type: 'smoothstep',
                animated: true
            },
            {
                id: 'e-complete-1-end',
                source: 'complete-1',
                sourceHandle: 'right',
                target: 'end',
                targetHandle: 'left',
                type: 'smoothstep',
                animated: true
            },
            {
                id: 'e-action-1-exception-1',
                source: 'action-1',
                sourceHandle: 'bottom',
                target: 'exception-1',
                targetHandle: 'top',
                type: 'smoothstep',
                animated: true
            }
        ]
    }
}

export function defaultStepScriptForm(equipments) {
    return {
        configMode: 'none',
        /** @deprecated 使用 configMode */
        scriptEnabled: false,
        scriptText: '',
        scriptNodesText: '',
        scriptEdgesText: '',
        version: SCRIPT_VERSION,
        flow: defaultFlowGraph(equipments)
    }
}

export function isScriptConfigEnabled(form) {
    const mode = form?.configMode ?? (form?.scriptEnabled ? 'flow' : 'none')
    return mode === 'flow' || mode === 'script'
}

export function formatStepScriptJson(text) {
    const trimmed = String(text || '').trim()
    if (!trimmed) return ''
    try {
        return JSON.stringify(JSON.parse(trimmed), null, 2)
    } catch {
        return String(text || '')
    }
}

export function validateRawStepScript(stepScript, equipments) {
    const text = String(stepScript || '').trim()
    if (!text) return '请输入工序脚本 JSON'
    let obj
    try {
        obj = JSON.parse(text)
    } catch {
        return '脚本 JSON 格式无效'
    }
    if (obj.version !== SCRIPT_VERSION) {
        return `脚本 version 须为 ${SCRIPT_VERSION}`
    }
    if (!obj.flow?.nodes?.length) {
        return '脚本须包含 flow.nodes'
    }
    ensureFlowEdges(obj.flow)
    const segErr = validateFlowSegments(obj.flow)
    if (segErr) return segErr
    const parsed = parseStepScriptToForm(JSON.stringify(obj), equipments)
    if (parsed._parseError) return parsed._parseError
    return validateStepScriptForm({...parsed, configMode: 'flow'}, equipments)
}

export function isStepEngineConfigured(stepScript, equipments, stepEngineConfig) {
    if (stepEngineConfig?.trim()) return true
    if (!stepScript?.trim()) return false
    try {
        const obj = JSON.parse(String(stepScript).trim())
        return obj.version === SCRIPT_VERSION && Array.isArray(obj.flow?.nodes) && obj.flow.nodes.length > 0
    } catch {
        return false
    }
}

function formatEventItem(item) {
    if (!item?.equipmentCode) return ''
    const v = item.variable || 'temp'
    const prefix = `@${item.equipmentCode}:`
    if (item.logicType === 'RANGE') {
        if (item.min == null || item.max == null || item.min === '' || item.max === '') return ''
        return `${prefix}${v}>=${item.min} && ${prefix}${v}<=${item.max}`
    }
    if (item.value == null || item.value === '') return ''
    return `${prefix}${v}${item.operator || '>='}${item.value}`
}

function activeEventConditions(fields) {
    if (!Array.isArray(fields?.eventConditions)) {
        return []
    }
    return fields.eventConditions.filter(isEventItemComplete)
}

function eventItemToJson(c) {
    const item = normalizeEventItem(c)
    const base = {
        equipmentCode: item.equipmentCode,
        variable: item.variable,
        logicType: item.logicType
    }
    if (item.logicType === 'RANGE') {
        return {...base, min: Number(item.min), max: Number(item.max)}
    }
    return {
        ...base,
        operator: item.operator,
        value: Number(item.value)
    }
}

function summarizeEventItem(c, equipments) {
    const item = normalizeEventItem(c)
    const equipLabel = resolveEquipmentLabel(equipments, item.equipmentCode)
    const prefix = equipLabel ? `${equipLabel}·` : ''
    const v = VARIABLE_LABEL[item.variable] || item.variable
    if (item.logicType === 'RANGE') {
        return `${prefix}${v}${item.min}~${item.max}`
    }
    const op = OPERATOR_LABEL[item.operator] || item.operator
    return `${prefix}${v}${op}${item.value}`
}

function buildEventConditionFromFields(fields) {
    const items = activeEventConditions(fields)
    if (!items.length) return ''
    const parts = items.map(formatEventItem).filter(Boolean)
    return parts.length === 1 ? parts[0] : parts.join(' && ')
}

function eventLogicToJson(fields) {
    const items = activeEventConditions(fields)
    if (!items.length) return null
    return {conditions: items.map(eventItemToJson)}
}

function applyEventLogicToForm(form, logic) {
    if (!logic) return
    const list =
        Array.isArray(logic.conditions) && logic.conditions.length
            ? logic.conditions
            : logic.equipmentCode
              ? [logic]
              : []
    form.eventConditions = list.map((c) => normalizeEventItem(c))
}

export function getDriveCondition(node, equipments) {
    if (!node?.condition) return defaultCondition('NONE', equipments)
    return node.condition
}

export function completeActionToJson(cond) {
    const k = (cond?.kind || '').toUpperCase()
    if (!isCompleteActionKind(k)) return null
    return {kind: k}
}

export function conditionToJson(cond) {
    if (!cond) return {kind: 'NONE'}
    const k = (cond.kind || '').toUpperCase()
    if (isCompleteActionKind(k)) {
        return null
    }
    if (k === 'NONE') {
        return {kind: 'NONE'}
    }
    const payload = {kind: cond.kind}
    if (cond.kind === 'TIME') {
        payload.duration = Number(cond.duration)
        return payload
    }
    if (cond.kind === 'EVENT') {
        const logic = eventLogicToJson(cond)
        if (!logic) return null
        payload.condition = buildEventConditionFromFields(cond)
        payload.eventLogic = logic
        return payload
    }
    return null
}

export function conditionFromJson(json) {
    if (!json?.kind) return defaultCondition('NONE')
    const k = (json.kind || '').toUpperCase()
    if (isCompleteActionKind(k)) {
        return {...defaultCondition('NONE'), _parseError: '完成动作类型不能用于驱动/异常'}
    }
    const form = defaultCondition('NONE')
    form.kind = json.kind
    if (!['NONE', 'TIME', 'EVENT'].includes(k)) {
        form._parseError = `不支持的条件类型: ${k}`
        return form
    }
    if (json.kind === 'TIME') {
        form.duration = json.duration ?? 60
        return form
    }
    if (json.kind === 'EVENT') {
        if (!json.eventLogic) {
            form.kind = 'EVENT'
            form._parseError = '缺少 eventLogic'
            return form
        }
        applyEventLogicToForm(form, json.eventLogic)
        return form
    }
    return form
}

export function getCompleteAction(node) {
    if (!node) return defaultCompleteAction()
    if (!node.completeAction) {
        node.completeAction = defaultCompleteAction()
    }
    return normalizeCompleteAction(node.completeAction)
}

export function setCompleteAction(node, action) {
    if (!node) return
    node.completeAction = normalizeCompleteAction(action)
}

export function normalizeAction(value, equipments) {
    if (!value?.kind) return defaultAction('RAMP_TIME', equipments)
    return value
}

function actionKindFromTypeMode(type, mode) {
    const t = (type || '').toUpperCase()
    const m = (mode || '').toUpperCase()
    if (t === 'RAMP' && m === 'TIME') return 'RAMP_TIME'
    if (t === 'PID_CONTROL' && m === 'CONTROL') return 'PID_CONTROL'
    return null
}

export function actionToJson(action) {
    if (!action?.kind) return null
    const map = {
        RAMP_TIME: {type: 'RAMP', mode: 'TIME'},
        PID_CONTROL: {type: 'PID_CONTROL', mode: 'CONTROL'}
    }
    const base = map[action.kind]
    if (!base) return null

    if (action.kind === 'RAMP_TIME') {
        const segments = (action.rampSegments || [])
            .filter((s) => s.to != null && s.duration != null)
            .map((s) => {
                const seg = {to: Number(s.to), duration: Number(s.duration)}
                if (s.holdDuration != null && s.holdDuration !== '' && Number(s.holdDuration) > 0) {
                    seg.holdDuration = Number(s.holdDuration)
                }
                return seg
            })
        if (!segments.length) return null
        if (!action.rampEquipmentCode) return null
        const payload = {
            ...base,
            equipmentCode: action.rampEquipmentCode,
            variable: normalizeRampVariable(action.rampVariable),
            segments
        }
        if (action.afterControl?.enabled) {
            const ctrl = action.afterControl
            if (!ctrl.equipmentCode || ctrl.target == null) return null
            payload.afterControl = {
                enabled: true,
                equipmentCode: ctrl.equipmentCode,
                variable: normalizeRampVariable(ctrl.variable),
                target: Number(ctrl.target)
            }
            if (ctrl.useAdvancedPid) {
                if (ctrl.kp != null) payload.afterControl.kp = Number(ctrl.kp)
                if (ctrl.ki != null) payload.afterControl.ki = Number(ctrl.ki)
                if (ctrl.kd != null) payload.afterControl.kd = Number(ctrl.kd)
            }
        }
        return payload
    }
    if (action.kind === 'PID_CONTROL') {
        if (action.target == null) return null
        if (!action.pidEquipmentCode) return null
        const payload = {
            ...base,
            target: Number(action.target),
            equipmentCode: action.pidEquipmentCode,
            variable: normalizeRampVariable(action.pidVariable)
        }
        if (action.useAdvancedPid) {
            if (action.kp != null) payload.kp = Number(action.kp)
            if (action.ki != null) payload.ki = Number(action.ki)
            if (action.kd != null) payload.kd = Number(action.kd)
        }
        return payload
    }
    return null
}

export function actionFromJson(json) {
    const form = defaultAction('RAMP_TIME')
    if (!json?.type) {
        form.kind = null
        form._parseError = '缺少 type'
        return form
    }
    const typeUpper = String(json.type).toUpperCase()
    if (typeUpper === 'WAIT' || typeUpper === 'HOLD' || typeUpper === 'SET') {
        form.kind = null
        form._parseError = typeUpper
        return form
    }
    form.kind = actionKindFromTypeMode(json.type, json.mode)
    if (!form.kind) {
        form.kind = null
        form._parseError = json.type
        return form
    }

    if (form.kind === 'RAMP_TIME') {
        form.rampEquipmentCode = json.equipmentCode || form.rampEquipmentCode
        form.rampVariable = normalizeRampVariable(json.variable)
        form.rampSegments = Array.isArray(json.segments) && json.segments.length
            ? json.segments.map((s) => ({
                to: s.to,
                duration: s.duration ?? 60,
                holdDuration: s.holdDuration
            }))
            : [defaultRampSegment()]
        if (json.afterControl?.enabled) {
            const ac = json.afterControl
            form.afterControl = {
                enabled: true,
                equipmentCode: ac.equipmentCode || '',
                variable: normalizeRampVariable(ac.variable || 'temp'),
                target: ac.target,
                useAdvancedPid: ac.kp != null || ac.ki != null || ac.kd != null,
                kp: ac.kp,
                ki: ac.ki,
                kd: ac.kd
            }
        } else {
            form.afterControl = defaultAfterControl()
        }
        return form
    }
    if (form.kind === 'PID_CONTROL') {
        form.pidEquipmentCode = json.equipmentCode || form.pidEquipmentCode
        form.pidVariable = normalizeRampVariable(json.variable)
        form.target = json.target
        form.kp = json.kp
        form.ki = json.ki
        form.kd = json.kd
        form.useAdvancedPid = json.kp != null || json.ki != null || json.kd != null
    }
    return form
}

function cloneJson(value) {
    return JSON.parse(JSON.stringify(value))
}

function indexFlowNodes(nodes) {
    const map = {}
    for (const node of nodes || []) {
        if (node?.id) map[node.id] = node
    }
    return map
}

function indexFlowOutgoing(edges) {
    const map = {}
    for (const edge of edges || []) {
        if (!edge?.source || !edge?.target) continue
        if (!map[edge.source]) map[edge.source] = []
        map[edge.source].push(edge.target)
    }
    return map
}

function nodeTypeOf(nodesById, nodeId) {
    return nodesById[nodeId]?.type || null
}

function pickNextTowardEnd(fromId, candidates, endId, outgoing, nodesById) {
    if (!candidates?.length) return null
    if (candidates.length === 1) return candidates[0]
    let best = null
    let bestDepth = Number.MAX_SAFE_INTEGER
    for (const candidate of candidates) {
        if (nodeTypeOf(nodesById, candidate) === 'exception') continue
        if (!canReachFlowNode(endId, candidate, outgoing)) continue
        const depth = depthToEndFlow(candidate, endId, outgoing)
        if (depth < bestDepth) {
            bestDepth = depth
            best = candidate
        }
    }
    if (best) return best
    return candidates.find((id) => nodeTypeOf(nodesById, id) !== 'exception') || candidates[0]
}

function canReachFlowNode(targetId, fromId, outgoing, visiting = new Set()) {
    if (!targetId || !fromId) return false
    if (targetId === fromId) return true
    if (visiting.has(fromId)) return false
    visiting.add(fromId)
    for (const next of outgoing[fromId] || []) {
        if (canReachFlowNode(targetId, next, outgoing, visiting)) return true
    }
    return false
}

function depthToEndFlow(fromId, endId, outgoing, visiting = new Set()) {
    if (!endId || !fromId) return Number.MAX_SAFE_INTEGER
    if (endId === fromId) return 0
    if (visiting.has(fromId)) return Number.MAX_SAFE_INTEGER
    visiting.add(fromId)
    let min = Number.MAX_SAFE_INTEGER
    for (const next of outgoing[fromId] || []) {
        const d = depthToEndFlow(next, endId, outgoing, visiting)
        if (d !== Number.MAX_SAFE_INTEGER) min = Math.min(min, 1 + d)
    }
    return min
}

/** 沿主链从 start 走向 end（与后端 ProcessStepFlowPath 一致） */
export function walkPrimaryPath(startId, endId, outgoing, nodesById) {
    const path = []
    if (!startId) return path
    let current = startId
    const visited = new Set()
    while (current && current !== endId) {
        if (visited.has(current)) break
        visited.add(current)
        path.push(current)
        const nexts = outgoing[current] || []
        if (!nexts.length) break
        current = pickNextTowardEnd(current, nexts, endId, outgoing, nodesById)
    }
    return path
}

function pathIndex(path, nodeId) {
    const idx = (path || []).indexOf(nodeId)
    return idx < 0 ? Number.MAX_SAFE_INTEGER : idx
}

function sortNodesOnPath(nodes, type, path, nodesById) {
    const filtered = (nodes || []).filter((n) => n?.type === type)
    filtered.sort((a, b) => {
        const ai = pathIndex(path, a.id)
        const bi = pathIndex(path, b.id)
        if (ai !== bi) return ai - bi
        const ax = Number(a.position?.x)
        const bx = Number(b.position?.x)
        return (Number.isFinite(ax) ? ax : Number.MAX_SAFE_INTEGER)
            - (Number.isFinite(bx) ? bx : Number.MAX_SAFE_INTEGER)
    })
    return filtered
}

const SEGMENT_PHASE = {DRIVE: 'DRIVE', ACTION: 'ACTION', COMPLETE: 'COMPLETE'}

function buildPhaseOrder(driveIdx, actionIdx, completeIdx) {
    return [
        {index: driveIdx, phase: SEGMENT_PHASE.DRIVE},
        {index: actionIdx, phase: SEGMENT_PHASE.ACTION},
        {index: completeIdx, phase: SEGMENT_PHASE.COMPLETE}
    ]
        .sort((a, b) => a.index - b.index)
        .map((e) => e.phase)
}

function orderedNodeIds(seg) {
    return (seg.phaseOrder || []).map((phase) => {
        if (phase === SEGMENT_PHASE.DRIVE) return seg.driveId
        if (phase === SEGMENT_PHASE.ACTION) return seg.actionId
        return seg.completeId
    })
}

/** 主链上切分驱动段；phaseOrder 为画布主链运行顺序 */
export function segmentIndicesOnPath(path, nodesById) {
    const segments = []
    let scanFrom = 0
    while (scanFrom < path.length) {
        let driveIdx = null
        let actionIdx = null
        let completeIdx = null
        for (let j = scanFrom; j < path.length; j++) {
            const t = nodeTypeOf(nodesById, path[j])
            if (t === 'drive' && driveIdx == null) driveIdx = j
            else if (t === 'action' && actionIdx == null) actionIdx = j
            else if (t === 'complete' && completeIdx == null) completeIdx = j
        }
        if (driveIdx == null || actionIdx == null || completeIdx == null) break
        const phaseOrder = buildPhaseOrder(driveIdx, actionIdx, completeIdx)
        segments.push({
            driveId: path[driveIdx],
            actionId: path[actionIdx],
            completeId: path[completeIdx],
            phaseOrder
        })
        scanFrom = Math.max(driveIdx, actionIdx, completeIdx) + 1
    }
    return segments
}

/** BFS 查找可达的目标类型节点（不要求直接相邻） */
function findReachableOfType(fromId, outgoing, nodesById, targetType, excludeResultIds = null) {
    if (!fromId) return null
    const queue = []
    const visited = new Set()
    for (const nextId of outgoing[fromId] || []) {
        if (!visited.has(nextId)) {
            visited.add(nextId)
            queue.push(nextId)
        }
    }
    while (queue.length) {
        const current = queue.shift()
        const node = nodesById[current]
        if (node?.type === targetType) {
            if (!excludeResultIds || !excludeResultIds.has(current)) {
                return current
            }
        }
        for (const nextId of outgoing[current] || []) {
            if (visited.has(nextId)) continue
            visited.add(nextId)
            queue.push(nextId)
        }
    }
    return null
}

/** 与后端 ProcessStepFlowCompiler 一致：统计可执行驱动段数量 */
export function countExecutableFlowSegments(flow) {
    const nodesById = indexFlowNodes(flow?.nodes)
    const outgoing = indexFlowOutgoing(flow?.edges)
    const startId = (flow?.nodes || []).find((n) => n.type === 'start')?.id
    const endId = (flow?.nodes || []).find((n) => n.type === 'end')?.id
    if (!startId) return 0
    try {
        return segmentIndicesOnPath(walkPrimaryPath(startId, endId, outgoing, nodesById), nodesById).length
    } catch {
        return 0
    }
}

export function validateFlowSegments(flow) {
    const prepared = cloneJson(flow || {nodes: [], edges: []})
    ensureFlowEdges(prepared)
    const nodesById = indexFlowNodes(prepared.nodes)
    const outgoing = indexFlowOutgoing(prepared.edges)
    const startId = (prepared.nodes || []).find((n) => n.type === 'start')?.id
    const endId = (prepared.nodes || []).find((n) => n.type === 'end')?.id
    if (!startId) {
        return '流程图未包含可执行的驱动段，请确保驱动条件、执行动作、完成动作可通过连线触达'
    }
    try {
        const segments = segmentIndicesOnPath(
            walkPrimaryPath(startId, endId, outgoing, nodesById),
            nodesById
        )
        if (segments.length <= 0) {
            return '流程图未包含可执行的驱动段，主链须依次经过驱动条件、执行动作、完成动作（顺序可任意）并连至结束'
        }
        return ''
    } catch (e) {
        return e?.message || '流程图主链无效'
    }
}

export function validateFlowTopology(flow) {
    if (!flow?.nodes?.length) {
        return '请配置流程图'
    }
    if (!(flow.nodes || []).some((n) => n.type === 'start')) {
        return '流程图缺少开始节点'
    }
    if (!(flow.nodes || []).some((n) => n.type === 'end')) {
        return '流程图缺少结束节点'
    }
    return ''
}

function hasFlowEdge(edges, source, target) {
    return (edges || []).some((e) => e.source === source && e.target === target)
}

function pushFlowEdge(edges, source, target, sourceHandle = 'right', targetHandle = 'left') {
    if (!source || !target || hasFlowEdge(edges, source, target)) return
    edges.push({
        id: `e-auto-${source}-${target}`,
        source,
        target,
        sourceHandle,
        targetHandle,
        type: 'smoothstep',
        animated: true
    })
}

/** 节点存在但连线缺失时，按类型顺序自动补全主链（不保留无效旧连线） */
export function ensureFlowEdges(flow) {
    if (!flow?.nodes?.length) return flow
    if (countExecutableFlowSegments(flow) > 0) return flow

    const nodes = flow.nodes
    const start = nodes.find((n) => n.type === 'start')
    const end = nodes.find((n) => n.type === 'end')
    const nodesById = indexFlowNodes(nodes)
    const outgoing = indexFlowOutgoing(flow.edges)
    const path = walkPrimaryPath(start.id, end?.id, outgoing, nodesById)
    const segments = segmentIndicesOnPath(path, nodesById)
    const exceptions = nodes.filter((n) => n.type === 'exception')
    if (!start || !segments.length) {
        return flow
    }

    const edges = []
    let prevTail = start.id
    for (let i = 0; i < segments.length; i++) {
        const seg = segments[i]
        const ordered = orderedNodeIds(seg)
        if (!ordered.length) continue
        pushFlowEdge(edges, prevTail, ordered[0])
        for (let j = 0; j < ordered.length - 1; j++) {
            pushFlowEdge(edges, ordered[j], ordered[j + 1])
        }
        if (exceptions[i]) {
            pushFlowEdge(edges, seg.actionId, exceptions[i].id, 'bottom', 'top')
        }
        prevTail = ordered[ordered.length - 1]
    }
    if (end) {
        pushFlowEdge(edges, prevTail, end.id)
    }
    flow.edges = edges
    return flow
}

/** 加载时规范化连线，保留用户已保存拓扑 */
export function normalizeFlowEdges(edges) {
    const seen = new Set()
    const result = []
    for (const edge of edges || []) {
        if (!edge?.source || !edge?.target) continue
        const key = `${edge.source}\0${edge.target}`
        if (seen.has(key)) continue
        seen.add(key)
        result.push({
            id: edge.id || `e-${edge.source}-${edge.target}`,
            source: edge.source,
            target: edge.target,
            sourceHandle: edge.sourceHandle || 'right',
            targetHandle: edge.targetHandle || 'left',
            type: edge.type || 'smoothstep',
            animated: edge.animated !== false
        })
    }
    return result
}

function flowNodeToJson(node) {
    const base = {
        id: node.id,
        type: node.type,
        title: node.title,
        position: node.position || {x: 0, y: 0}
    }
    if (node.type === 'drive') {
        base.condition = conditionToJson(node.condition)
    } else if (node.type === 'action') {
        base.action = actionToJson(node.action)
    } else if (node.type === 'complete') {
        const json = completeActionToJson(getCompleteAction(node))
        base.items = json ? [json] : []
        if (json) base.completeAction = json
    } else if (node.type === 'exception') {
        base.items = [conditionToJson(node.condition)].filter(Boolean)
    }
    return base
}

function flowNodeFromJson(json, index, equipments) {
    const node = defaultFlowNode(json.type || 'drive', index, json.position, equipments)
    node.id = json.id || node.id
    node.title = json.title || node.title
    if (json.position && typeof json.position === 'object') {
        node.position = {x: Number(json.position.x) || 0, y: Number(json.position.y) || 0}
    }
    if (node.type === 'drive') {
        node.condition = conditionFromJson(json.condition || {kind: 'NONE'})
    } else if (node.type === 'action') {
        if (!json.action) {
            node.action = {kind: null, _parseError: '缺少 action'}
        } else {
            node.action = actionFromJson(json.action)
        }
    } else if (node.type === 'complete') {
        const items = Array.isArray(json.items) ? json.items : []
        if (items.length) {
            node.completeAction = completeActionFromJson(items[0])
        } else if (json.completeAction) {
            node.completeAction = completeActionFromJson(json.completeAction)
        } else {
            node.completeAction = defaultCompleteAction()
        }
    } else if (node.type === 'exception') {
        const items = Array.isArray(json.items) ? json.items : []
        node.condition = items.length
            ? conditionFromJson(items[0])
            : defaultCondition('NONE', equipments)
    }
    return node
}

export function parseStepScriptToForm(stepScript, equipments) {
    const form = defaultStepScriptForm(equipments)
    if (!stepScript || !String(stepScript).trim()) {
        return form
    }
    let obj
    try {
        obj = JSON.parse(String(stepScript).trim())
    } catch {
        form.configMode = 'script'
        form.scriptText = String(stepScript).trim()
        form._parseError = '工序脚本 JSON 格式无效'
        return form
    }
    if (obj.version !== SCRIPT_VERSION) {
        form._parseError = `工序脚本 version 须为 ${SCRIPT_VERSION}`
        return form
    }
    if (!obj.flow?.nodes?.length) {
        form.configMode = 'script'
        form.scriptEnabled = true
        form.scriptText = formatStepScriptJson(String(stepScript).trim())
        form._parseError = '脚本须包含 flow.nodes'
        return form
    }
    const nodes = obj.flow.nodes.map((n, i) => flowNodeFromJson(n, i + 1, equipments))
    form.configMode = 'flow'
    form.scriptEnabled = true
    form.scriptText = formatStepScriptJson(String(stepScript).trim())
    const withStart = nodes.some((n) => n.id === 'start') ? nodes : [defaultFlowNode('start', 1, null, equipments), ...nodes]
    const withEnd = withStart.some((n) => n.id === 'end') ? withStart : [...withStart, defaultFlowNode('end', 1, null, equipments)]
    form.flow = {
        nodes: withEnd,
        edges: normalizeFlowEdges(Array.isArray(obj.flow.edges) ? obj.flow.edges : [])
    }
    return form
}

export function buildDefaultStepScriptJson(equipments) {
    const flow = defaultFlowGraph(equipments)
    ensureFlowEdges(flow)
    return formatStepScriptJson(JSON.stringify({
        version: SCRIPT_VERSION,
        flow: {
            nodes: flow.nodes.map(flowNodeToJson),
            edges: cloneJson(flow.edges || [])
        }
    }))
}

export function syncPersistedScriptText(form) {
    if (!form || !isScriptConfigEnabled(form)) {
        if (form) form.scriptText = ''
        return ''
    }
    try {
        const json = buildStepScriptFromForm(form)
        form.scriptText = json ? formatStepScriptJson(json) : ''
        return form.scriptText
    } catch {
        return form.scriptText || ''
    }
}

export function buildFlowNodesScriptJson(form) {
    const nodes = form?.flow?.nodes || []
    if (!nodes.length) return '[]'
    return formatStepScriptJson(JSON.stringify(nodes.map((n) => flowNodeToJson(n))))
}

export function buildFlowEdgesScriptJson(form) {
    return formatStepScriptJson(JSON.stringify(form?.flow?.edges || []))
}

export function populateScriptPartsFromFlow(flow, target = {}) {
    target.scriptNodesText = buildFlowNodesScriptJson({flow})
    target.scriptEdgesText = buildFlowEdgesScriptJson({flow})
    return target
}

export function mergeStepScriptParts(nodesText, edgesText) {
    const nodes = JSON.parse(String(nodesText || '[]').trim() || '[]')
    const edges = JSON.parse(String(edgesText || '[]').trim() || '[]')
    if (!Array.isArray(nodes)) throw new Error('nodes must be array')
    if (!Array.isArray(edges)) throw new Error('edges must be array')
    return JSON.stringify({version: SCRIPT_VERSION, flow: {nodes, edges}})
}

export function applyScriptEdgesToFlow(flow, edgesText) {
    const edges = JSON.parse(String(edgesText || '[]').trim() || '[]')
    if (!Array.isArray(edges)) throw new Error('edges must be array')
    flow.edges = cloneJson(edges)
    return flow
}

export function validateScriptParts(nodesText, edgesText, equipments) {
    let merged
    try {
        merged = mergeStepScriptParts(nodesText, edgesText)
    } catch {
        return '脚本 JSON 格式无效'
    }
    return validateRawStepScript(merged, equipments)
}

export function buildStepScriptFromForm(form) {
    const mode = form?.configMode ?? (form?.scriptEnabled ? 'flow' : 'none')
    if (mode === 'none') return ''
    if (mode === 'script') {
        const text = String(form.scriptText || '').trim()
        if (!text) return ''
        const obj = JSON.parse(text)
        if (obj.flow) {
            ensureFlowEdges(obj.flow)
            obj.flow.edges = normalizeFlowEdges(obj.flow.edges || [])
        }
        obj.version = SCRIPT_VERSION
        return JSON.stringify(obj)
    }
    if (!form.flow?.nodes?.length) {
        return ''
    }
    const flow = cloneJson(form.flow)
    ensureFlowEdges(flow)
    return JSON.stringify({
        version: SCRIPT_VERSION,
        flow: {
            nodes: flow.nodes.map(flowNodeToJson),
            edges: normalizeFlowEdges(flow.edges || [])
        }
    })
}

const VARIABLE_LABEL = {temp: '温度', pressure: '压力', setpoint: '设定值'}
const OPERATOR_LABEL = {'>=': '≥', '<=': '≤', '>': '>', '<': '<', '==': '='}
const CONDITION_KIND_LABEL = {NONE: '无', TIME: '时间', EVENT: '事件'}
const COMPLETE_ACTION_LABEL = {AUTO_NEXT: '自动下一工序', MANUAL_CONFIRM: '等待人工确认'}

export function summarizeCompleteAction(cond) {
    const k = (cond?.kind || '').toUpperCase()
    if (k === 'MANUAL_CONFIRM') return COMPLETE_ACTION_LABEL.MANUAL_CONFIRM
    if (k === 'AUTO_NEXT') return COMPLETE_ACTION_LABEL.AUTO_NEXT
    if (!k) return '未配置'
    return `无效(${cond.kind})`
}

export function summarizeCondition(cond, equipments) {
    const k = (cond?.kind || '').toUpperCase()
    if (isCompleteActionKind(k)) {
        return '无效：完成动作类型不能用于驱动/异常条件'
    }
    if (!cond || cond.kind === 'NONE') return '无'
    if (cond.kind === 'TIME') return `时间 ${cond.duration ?? '—'}s`
    if (cond.kind === 'EVENT') {
        const items = activeEventConditions(cond)
        if (!items.length) return '事件'
        const parts = items.map((c) => summarizeEventItem(c, equipments))
        return parts.length > 1 ? parts.join(' 且 ') : parts[0]
    }
    return CONDITION_KIND_LABEL[cond.kind] || cond.kind
}

export function summarizeAction(action, equipments) {
    if (!action?.kind) return '—'
    if (action.kind === 'RAMP_TIME') {
        const equipLabel = resolveEquipmentLabel(equipments, action.rampEquipmentCode)
        const prefix = equipLabel ? `${equipLabel} ` : ''
        const segs = action.rampSegments || []
        const parts = segs
            .map((s) => {
                if (s.to == null || s.duration == null) return null
                const hold =
                    s.holdDuration != null && s.holdDuration !== '' && Number(s.holdDuration) > 0
                        ? ` 保持${s.holdDuration}s`
                        : ''
                return `${s.duration}s→${s.to}${hold}`
            })
            .filter(Boolean)
        const varLabel = resolveProcessVariableLabel(action.rampVariable)
        const head = varLabel ? `从设备${varLabel}` : '从设备当前值'
        const finalVal = rampFinalValue(action)
        const tail = finalVal != null ? ` 终值${finalVal}` : ''
        const ctrl =
            action.afterControl?.enabled && action.afterControl.target != null
                ? (() => {
                    const equipLabel = resolveEquipmentLabel(equipments, action.afterControl.equipmentCode)
                    const varLabel = resolveProcessVariableLabel(action.afterControl.variable)
                    const prefix = equipLabel && varLabel ? `${equipLabel}${varLabel}` : ''
                    return prefix ? ` →${prefix}控制${action.afterControl.target}` : ` →控制${action.afterControl.target}`
                })()
                : ''
        return parts.length ? `${prefix}斜坡 ${head} ${parts.join('，')}${tail}${ctrl}` : `${prefix}斜坡${ctrl}`
    }
    if (action.kind === 'PID_CONTROL') {
        const equipLabel = resolveEquipmentLabel(equipments, action.pidEquipmentCode)
        const varLabel = resolveProcessVariableLabel(action.pidVariable)
        const prefix = equipLabel && varLabel ? `${equipLabel} ${varLabel} ` : ''
        return action.target != null ? `${prefix}PID→${action.target}` : `${prefix}PID`
    }
    return action.kind
}

export function summarizeStepScript(stepScript, equipments) {
    const form = parseStepScriptToForm(stepScript, equipments)
    if (!isScriptConfigEnabled(form)) return ''
    const nodes = form.flow.nodes.filter((n) => n.type !== 'start' && n.type !== 'end')
    return nodes.length ? `${nodes.length} 个流程节点，${form.flow.edges?.length || 0} 条连线` : '仅开始节点'
}

function validateEventFields(fields, label, equipments) {
    const options = buildStepEquipmentOptions(equipments)
    const items = activeEventConditions(fields)
    if (!items.length) {
        return `${label}：请配置至少一条设备条件`
    }
    if (options.length) {
        for (let i = 0; i < items.length; i++) {
            const item = normalizeEventItem(items[i])
            if (!item.equipmentCode) {
                return `${label}：条件 ${i + 1} 请选择关联设备`
            }
            if (item.logicType === 'RANGE') {
                if (Number(item.min) > Number(item.max)) {
                    return `${label}：条件 ${i + 1} 下限不能大于上限`
                }
            }
        }
    }
    return ''
}

export function validateCondition(cond, label, equipments) {
    if (cond?._parseError) {
        return `${label}：条件无效（${cond._parseError}）`
    }
    if (isCompleteActionKind(cond?.kind)) {
        return `${label}：驱动/异常条件不能使用完成动作类型（${cond.kind}）`
    }
    if (!cond || cond.kind === 'NONE') return ''
    if (cond.kind === 'TIME') {
        if (cond.duration == null || cond.duration <= 0) {
            return `${label}：请输入有效时长（秒）`
        }
    }
    if (cond.kind === 'EVENT') {
        return validateEventFields(cond, label, equipments)
    }
    return ''
}

export function validateAction(action, label, equipments) {
    const options = buildStepEquipmentOptions(equipments)
    if (!action?.kind) {
        const hint = action?._parseError ? `（${action._parseError}）` : ''
        return `${label}：执行动作无效${hint}，仅支持斜坡或 PID 控制`
    }
    if (action?.kind === 'RAMP_TIME') {
        if (options.length && !action.rampEquipmentCode) {
            return `${label}：请选择斜坡关联设备`
        }
        if (action.afterControl?.enabled) {
            if (options.length && !action.afterControl.equipmentCode) {
                return `${label}：请选择斜坡后控制关联设备`
            }
            if (!action.afterControl.variable) {
                return `${label}：请选择斜坡后控制过程量`
            }
            if (action.afterControl.target == null || action.afterControl.target === '') {
                return `${label}：请配置斜坡后控制目标值`
            }
        }
    }
    if (action?.kind === 'PID_CONTROL') {
        if (options.length && !action.pidEquipmentCode) {
            return `${label}：请选择 PID 关联设备`
        }
        if (action.target == null || action.target === '') {
            return `${label}：请配置 PID 控制目标值`
        }
    }
    if (!actionToJson(action)) return `${label}：请完善执行动作`
    return ''
}

export function validateCompleteAction(cond, label) {
    const k = (cond?.kind || '').toUpperCase()
    if (!isCompleteActionKind(k)) {
        return `${label}：须选择「自动下一工序」或「等待人工确认」`
    }
    return ''
}

export function validateStepScriptForm(form, equipments) {
    if (form?._parseError) return form._parseError
    const mode = form?.configMode ?? (form?.scriptEnabled ? 'flow' : 'none')
    if (mode === 'none') return ''
    if (mode === 'script') {
        return validateRawStepScript(form.scriptText, equipments)
    }
    if (!form.flow?.nodes?.length) {
        return '请配置流程图'
    }
    for (const node of form.flow.nodes) {
        if (node.type === 'start' || node.type === 'end') continue
        if (node.type === 'action') {
            const err = validateAction(
                normalizeAction(node.action, equipments),
                node.title || '执行动作',
                equipments
            )
            if (err) return err
        } else if (node.type === 'drive') {
            const err = validateCondition(node.condition, node.title || DRIVE_NODE_LABEL, equipments)
            if (err) return err
        } else if (node.type === 'complete') {
            const err = validateCompleteAction(
                getCompleteAction(node),
                node.title || '完成动作'
            )
            if (err) return err
        } else if (node.type === 'exception') {
            const err = validateCondition(
                node.condition,
                node.title || '异常分支',
                equipments
            )
            if (err) return err
        }
    }
    const topoErr = validateFlowTopology(form.flow)
    if (topoErr) return topoErr
    return validateFlowSegments(form.flow)
}
