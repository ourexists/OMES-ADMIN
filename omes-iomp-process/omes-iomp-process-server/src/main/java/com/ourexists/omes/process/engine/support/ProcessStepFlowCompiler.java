package com.ourexists.omes.process.engine.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.model.ProcessConditionSpec;
import com.ourexists.omes.process.engine.liteflow.ProcessLiteFlowChainRegistry;
import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepTimeSegment;
import com.ourexists.omes.process.engine.model.RampAfterControlSpec;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 将工序脚本 v3 流程图编译为顺序执行的驱动组合列表。
 */
public final class ProcessStepFlowCompiler {

    private static final int SCRIPT_VERSION = 3;

    private ProcessStepFlowCompiler() {
    }

    /**
     * 从工序脚本 v3 流程图主链首段执行动作提取 RAMP segments（供配方模板合并）。
     */
    public static List<ProcessStepTimeSegment> extractFirstRampSegments(JsonNode root, ObjectMapper objectMapper) {
        requireVersion3(root);
        JsonNode flow = root.get("flow");
        if (flow == null || !flow.has("nodes")) {
            return List.of();
        }
        Map<String, JsonNode> nodesById = indexNodes(flow.get("nodes"));
        Map<String, List<String>> outgoing = indexOutgoing(flow.get("edges"));
        String startId = findNodeId(nodesById, "start");
        String endId = findNodeId(nodesById, "end");
        if (startId == null || endId == null) {
            return List.of();
        }
        List<ProcessStepFlowPath.SegmentIndices> segmentIndices =
                ProcessStepFlowPath.segmentIndicesOnPath(
                        ProcessStepFlowPath.walkPrimaryPath(startId, endId, outgoing, nodesById),
                        nodesById);
        if (segmentIndices.isEmpty()) {
            return List.of();
        }
        ProcessStepFlowPath.SegmentIndices first = segmentIndices.get(0);
        ProcessStepDefinition action = readAction(nodesById.get(first.actionId()), objectMapper);
        if (action == null || !"RAMP".equalsIgnoreCase(action.getType())) {
            return List.of();
        }
        return action.getSegments() != null ? action.getSegments() : List.of();
    }

    public static List<ProcessStepCombination> compile(JsonNode root,
                                                       ObjectMapper objectMapper,
                                                       ProcessAviatorExpressionCompiler expressionCompiler,
                                                       ProcessLiteFlowChainRegistry chainRegistry,
                                                       String scriptKey) {
        requireVersion3(root);
        JsonNode flow = root.get("flow");
        if (flow == null || !flow.has("nodes")) {
            throw new BusinessException("工序脚本缺少 flow.nodes");
        }
        ProcessStepFlowTopology.ensureFlowEdges(flow);
        Map<String, JsonNode> nodesById = indexNodes(flow.get("nodes"));
        Map<String, List<String>> outgoing = indexOutgoing(flow.get("edges"));
        String startId = findNodeId(nodesById, "start");
        if (startId == null) {
            throw new BusinessException("流程图缺少开始节点");
        }
        String endId = findNodeId(nodesById, "end");
        if (endId == null) {
            throw new BusinessException("流程图缺少结束节点");
        }
        List<ProcessStepFlowPath.SegmentIndices> segmentIndices =
                ProcessStepFlowPath.segmentIndicesOnPath(
                        ProcessStepFlowPath.walkPrimaryPath(startId, endId, outgoing, nodesById),
                        nodesById);
        List<ProcessStepCombination> combinations = new ArrayList<>();
        int segment = 0;
        for (ProcessStepFlowPath.SegmentIndices indices : segmentIndices) {
            String driveId = indices.driveId();
            String actionId = indices.actionId();
            String completeId = indices.completeId();
            ProcessStepCombination combo = new ProcessStepCombination();
            combo.setName("段" + (++segment));
            JsonNode driveJson = nodesById.get(driveId).get("condition");
            combo.setDrive(readCondition(driveJson, objectMapper));
            combo.setAction(readAction(nodesById.get(actionId), objectMapper));
            combo.setComplete(readCompleteCondition(nodesById.get(completeId), objectMapper));
            combo.setPhaseOrder(indices.phaseOrder());
            JsonNode exceptionItemJson = firstItemJson(readExceptionNode(actionId, outgoing, nodesById));
            combo.setException(exceptionItemJson == null
                    ? noneCondition()
                    : readCondition(exceptionItemJson, objectMapper));
            expressionCompiler.enrichCombination(combo, driveJson, exceptionItemJson, objectMapper);
            expressionCompiler.compileComplete(combo.getComplete());
            combinations.add(combo);
        }
        if (combinations.isEmpty()) {
            throw new BusinessException(
                    "流程图未包含可执行的驱动段，主链须依次经过驱动条件、执行动作、完成动作（顺序可任意）并连至结束");
        }
        for (int i = 0; i < combinations.size(); i++) {
            ProcessStepCombination combo = combinations.get(i);
            if (combo.getAction() == null || !StringUtils.hasText(combo.getAction().getType())) {
                throw new BusinessException("第 " + (i + 1) + " 段须配置执行动作");
            }
        }
        chainRegistry.registerScriptChains(scriptKey, combinations);
        return combinations;
    }

    private static void requireVersion3(JsonNode root) {
        if (root == null || !root.has("version") || root.get("version").asInt() != SCRIPT_VERSION) {
            throw new BusinessException("工序脚本 version 须为 3");
        }
    }

    private static Map<String, JsonNode> indexNodes(JsonNode nodes) {
        Map<String, JsonNode> map = new HashMap<>();
        if (nodes == null || !nodes.isArray()) {
            return map;
        }
        for (JsonNode node : nodes) {
            if (node.has("id")) {
                map.put(node.get("id").asText(), node);
            }
        }
        return map;
    }

    private static Map<String, List<String>> indexOutgoing(JsonNode edges) {
        Map<String, List<String>> map = new HashMap<>();
        if (edges == null || !edges.isArray()) {
            return map;
        }
        for (JsonNode edge : edges) {
            if (!edge.has("source") || !edge.has("target")) {
                continue;
            }
            map.computeIfAbsent(edge.get("source").asText(), k -> new ArrayList<>())
                    .add(edge.get("target").asText());
        }
        return map;
    }

    private static String findNodeId(Map<String, JsonNode> nodesById, String type) {
        for (Map.Entry<String, JsonNode> entry : nodesById.entrySet()) {
            if (type.equals(textOrNull(entry.getValue().get("type")))) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** BFS 查找可达的目标类型节点（不要求直接相邻） */
    private static String findReachableOfType(String fromId,
                                              Map<String, List<String>> outgoing,
                                              Map<String, JsonNode> nodesById,
                                              String targetType,
                                              Set<String> excludeResultIds) {
        if (fromId == null) {
            return null;
        }
        ArrayDeque<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        for (String nextId : outgoing.getOrDefault(fromId, List.of())) {
            if (visited.add(nextId)) {
                queue.add(nextId);
            }
        }
        while (!queue.isEmpty()) {
            String current = queue.poll();
            JsonNode node = nodesById.get(current);
            if (node != null && targetType.equals(textOrNull(node.get("type")))) {
                if (excludeResultIds == null || !excludeResultIds.contains(current)) {
                    return current;
                }
            }
            for (String nextId : outgoing.getOrDefault(current, List.of())) {
                if (visited.add(nextId)) {
                    queue.add(nextId);
                }
            }
        }
        return null;
    }

    private static JsonNode readExceptionNode(String actionId,
                                              Map<String, List<String>> outgoing,
                                              Map<String, JsonNode> nodesById) {
        String exceptionId = findReachableOfType(actionId, outgoing, nodesById, "exception", null);
        return exceptionId == null ? null : nodesById.get(exceptionId);
    }

    private static ProcessConditionSpec readCompleteCondition(JsonNode node, ObjectMapper objectMapper) {
        JsonNode first = firstItemJson(node);
        if (first == null && node != null && node.has("completeAction")) {
            first = node.get("completeAction");
        }
        if (first == null || first.isNull()) {
            throw new BusinessException("完成动作节点须配置方式（AUTO_NEXT 或 MANUAL_CONFIRM）");
        }
        String kindText = textOrNull(first.get("kind"));
        if (!StringUtils.hasText(kindText)) {
            throw new BusinessException("完成动作须指定 kind");
        }
        ProcessConditionKind kind = ProcessConditionKind.fromText(kindText);
        if (!kind.isCompleteActionKind()) {
            throw new BusinessException("完成动作仅支持 AUTO_NEXT、MANUAL_CONFIRM，不支持: " + kindText);
        }
        ProcessConditionSpec spec = new ProcessConditionSpec();
        spec.setKind(kind.name());
        return spec;
    }

    private static JsonNode firstItemJson(JsonNode node) {
        if (node == null || !node.has("items")) {
            return null;
        }
        JsonNode items = node.get("items");
        if (items.isArray()) {
            return items.isEmpty() ? null : items.get(0);
        }
        return items;
    }

    private static ProcessConditionSpec readFirstCondition(JsonNode node, ObjectMapper objectMapper) {
        if (node == null || !node.has("items")) {
            return noneCondition();
        }
        JsonNode items = node.get("items");
        if (items.isArray()) {
            if (items.isEmpty()) {
                return noneCondition();
            }
            return readCondition(items.get(0), objectMapper);
        }
        return readCondition(items, objectMapper);
    }

    private static ProcessConditionSpec readCondition(JsonNode json, ObjectMapper objectMapper) {
        if (json == null || json.isNull()) {
            return noneCondition();
        }
        try {
            ProcessConditionSpec spec = objectMapper.treeToValue(json, ProcessConditionSpec.class);
            if (spec == null || !StringUtils.hasText(spec.getKind())) {
                return noneCondition();
            }
            ProcessConditionKind kind = spec.resolvedKind();
            if (kind.isCompleteActionKind()) {
                throw new BusinessException("驱动/异常条件不能使用完成动作类型: " + spec.getKind());
            }
            if (!kind.isDriveOrExceptionKind()) {
                throw new BusinessException("不支持的条件类型: " + spec.getKind());
            }
            if (kind == ProcessConditionKind.EVENT && !hasEventLogic(json.get("eventLogic"))) {
                throw new BusinessException("事件条件须配置 eventLogic（设备、变量、阈值）");
            }
            return spec;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("条件解析失败: " + ex.getMessage());
        }
    }

    private static ProcessStepDefinition readAction(JsonNode node, ObjectMapper objectMapper) {
        if (node == null) {
            throw new BusinessException("执行动作节点不存在");
        }
        JsonNode actionJson = node.get("action");
        if (actionJson == null || actionJson.isNull()) {
            throw new BusinessException("执行动作节点缺少 action");
        }
        try {
            ProcessStepDefinition action = objectMapper.treeToValue(actionJson, ProcessStepDefinition.class);
            if (action == null || !StringUtils.hasText(action.getType())) {
                throw new BusinessException("执行动作配置无效");
            }
            String actionType = action.getType().trim().toUpperCase();
            if ("RAMP".equals(actionType)) {
                if (!"TIME".equalsIgnoreCase(action.getMode())) {
                    throw new BusinessException("斜坡动作 mode 须为 TIME");
                }
                if (!StringUtils.hasText(action.getEquipmentCode())) {
                    throw new BusinessException("斜坡动作须绑定关联设备");
                }
                if (!StringUtils.hasText(action.getVariable())) {
                    throw new BusinessException("斜坡动作须选择过程量（temp、pressure）");
                }
                validateAfterControl(action.getAfterControl());
            } else if ("PID_CONTROL".equals(actionType)) {
                if (!"CONTROL".equalsIgnoreCase(action.getMode())) {
                    throw new BusinessException("PID 控制动作 mode 须为 CONTROL");
                }
                if (!StringUtils.hasText(action.getEquipmentCode())) {
                    throw new BusinessException("PID 控制动作须绑定关联设备");
                }
                if (!StringUtils.hasText(action.getVariable())) {
                    throw new BusinessException("PID 控制动作须选择设备属性（temp、pressure）");
                }
                if (action.getTarget() == null) {
                    throw new BusinessException("PID 控制动作须配置目标值");
                }
            } else {
                throw new BusinessException("不支持的执行动作类型: " + action.getType());
            }
            return action;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("执行动作解析失败: " + ex.getMessage());
        }
    }

    private static void validateAfterControl(RampAfterControlSpec afterCtrl) {
        if (afterCtrl == null || !Boolean.TRUE.equals(afterCtrl.getEnabled())) {
            return;
        }
        if (afterCtrl.getTarget() == null) {
            throw new BusinessException("斜坡后设备控制已启用但未配置目标值");
        }
        if (!StringUtils.hasText(afterCtrl.getEquipmentCode())) {
            throw new BusinessException("斜坡后设备控制须绑定关联设备");
        }
        if (!StringUtils.hasText(afterCtrl.getVariable())) {
            throw new BusinessException("斜坡后设备控制须选择设备属性（如 temp、pressure）");
        }
    }

    private static ProcessConditionSpec noneCondition() {
        ProcessConditionSpec spec = new ProcessConditionSpec();
        spec.setKind("NONE");
        return spec;
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    private static boolean hasEventLogic(JsonNode logic) {
        if (logic == null || logic.isNull()) {
            return false;
        }
        if (logic.has("conditions") && logic.get("conditions").isArray()) {
            if (logic.get("conditions").isEmpty()) {
                return false;
            }
            for (JsonNode item : logic.get("conditions")) {
                if (!hasSingleEventItem(item)) {
                    return false;
                }
            }
            return true;
        }
        return hasSingleEventItem(logic);
    }

    private static boolean hasSingleEventItem(JsonNode logic) {
        if (logic == null || logic.isNull() || !logic.has("equipmentCode")) {
            return false;
        }
        if (!StringUtils.hasText(logic.get("equipmentCode").asText(""))) {
            return false;
        }
        if ("RANGE".equalsIgnoreCase(textOrNull(logic.get("logicType")))) {
            return logic.has("min") && logic.has("max");
        }
        return logic.has("value");
    }
}
