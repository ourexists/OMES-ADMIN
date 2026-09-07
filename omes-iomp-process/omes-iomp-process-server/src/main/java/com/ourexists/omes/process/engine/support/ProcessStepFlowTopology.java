package com.ourexists.omes.process.engine.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流程图拓扑：补全连线、统计可执行驱动段（与前端 processStepScript.js 一致）。
 */
public final class ProcessStepFlowTopology {

    private ProcessStepFlowTopology() {
    }

    public static void ensureFlowEdges(JsonNode flow) {
        if (flow == null || !flow.isObject()) {
            return;
        }
        if (countExecutableFlowSegments(flow) > 0) {
            return;
        }
        JsonNode nodes = flow.get("nodes");
        if (nodes == null || !nodes.isArray() || nodes.isEmpty()) {
            return;
        }
        ArrayNode edges = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode();
        Map<String, JsonNode> nodesById = indexNodes(nodes);
        Map<String, List<String>> outgoing = indexOutgoing(flow.get("edges"));
        String startId = findFirstNodeId(nodes, "start");
        String endId = findFirstNodeId(nodes, "end");
        List<String> path = ProcessStepFlowPath.walkPrimaryPath(startId, endId, outgoing, nodesById);
        List<ProcessStepFlowPath.SegmentIndices> segments =
                ProcessStepFlowPath.segmentIndicesOnPath(path, nodesById);
        if (startId == null || segments.isEmpty()) {
            return;
        }
        List<JsonNode> exceptions = nodesByType(nodes, "exception");
        String prevTail = startId;
        for (int i = 0; i < segments.size(); i++) {
            ProcessStepFlowPath.SegmentIndices seg = segments.get(i);
            List<String> ordered = seg.orderedNodeIds();
            if (ordered.isEmpty()) {
                continue;
            }
            pushEdge(edges, prevTail, ordered.get(0));
            for (int j = 0; j < ordered.size() - 1; j++) {
                pushEdge(edges, ordered.get(j), ordered.get(j + 1));
            }
            String actionId = seg.actionId();
            if (i < exceptions.size()) {
                pushEdge(edges, actionId, textOrNull(exceptions.get(i).get("id")), "bottom", "top");
            }
            prevTail = ordered.get(ordered.size() - 1);
        }
        if (endId != null) {
            pushEdge(edges, prevTail, endId);
        }
        ((ObjectNode) flow).set("edges", edges);
    }

    public static int countExecutableFlowSegments(JsonNode flow) {
        if (flow == null) {
            return 0;
        }
        Map<String, JsonNode> nodesById = indexNodes(flow.get("nodes"));
        Map<String, List<String>> outgoing = indexOutgoing(flow.get("edges"));
        String startId = findFirstNodeId(flow.get("nodes"), "start");
        String endId = findFirstNodeId(flow.get("nodes"), "end");
        if (startId == null) {
            return 0;
        }
        try {
            return ProcessStepFlowPath.segmentIndicesOnPath(
                    ProcessStepFlowPath.walkPrimaryPath(startId, endId, outgoing, nodesById),
                    nodesById).size();
        } catch (com.ourexists.era.framework.core.exceptions.BusinessException ex) {
            return 0;
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

    private static List<JsonNode> nodesByType(JsonNode nodes, String type) {
        List<JsonNode> list = new ArrayList<>();
        if (nodes == null || !nodes.isArray()) {
            return list;
        }
        for (JsonNode node : nodes) {
            if (type.equals(textOrNull(node.get("type")))) {
                list.add(node);
            }
        }
        return list;
    }

    private static String findFirstNodeId(JsonNode nodes, String type) {
        if (nodes == null || !nodes.isArray()) {
            return null;
        }
        for (JsonNode node : nodes) {
            if (type.equals(textOrNull(node.get("type"))) && node.has("id")) {
                return node.get("id").asText();
            }
        }
        return null;
    }

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

    private static boolean hasEdge(ArrayNode edges, String source, String target) {
        if (source == null || target == null) {
            return false;
        }
        for (JsonNode edge : edges) {
            if (source.equals(textOrNull(edge.get("source")))
                    && target.equals(textOrNull(edge.get("target")))) {
                return true;
            }
        }
        return false;
    }

    private static void pushEdge(ArrayNode edges, String source, String target) {
        pushEdge(edges, source, target, "right", "left");
    }

    private static void pushEdge(ArrayNode edges,
                                 String source,
                                 String target,
                                 String sourceHandle,
                                 String targetHandle) {
        if (source == null || target == null || hasEdge(edges, source, target)) {
            return;
        }
        ObjectNode edge = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        edge.put("id", "e-auto-" + source + "-" + target);
        edge.put("source", source);
        edge.put("target", target);
        edge.put("sourceHandle", sourceHandle);
        edge.put("targetHandle", targetHandle);
        edge.put("type", "smoothstep");
        edge.put("animated", true);
        edges.add(edge);
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }
}
