package com.ourexists.omes.process.engine.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.ourexists.omes.process.engine.model.ProcessSegmentPhase;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 沿流程图主链从 start 走向 end，用于按画布顺序解析驱动段（非按节点类型 BFS）。
 */
public final class ProcessStepFlowPath {

    private ProcessStepFlowPath() {
    }

    /** 从 start 沿出边走到 end，得到主链节点 id 序列（含 start、不含 end 时到不了则尽量走远）。 */
    public static List<String> walkPrimaryPath(String startId,
                                               String endId,
                                               Map<String, List<String>> outgoing,
                                               Map<String, JsonNode> nodesById) {
        List<String> path = new ArrayList<>();
        if (startId == null) {
            return path;
        }
        String current = startId;
        Set<String> visited = new HashSet<>();
        while (current != null && !current.equals(endId)) {
            if (!visited.add(current)) {
                break;
            }
            path.add(current);
            List<String> nexts = outgoing.getOrDefault(current, List.of());
            if (nexts.isEmpty()) {
                break;
            }
            current = pickNextTowardEnd(current, nexts, endId, outgoing, nodesById);
        }
        return path;
    }

    /**
     * 在主链上切分驱动段：每段须含 drive、action、complete；{@link SegmentIndices#phaseOrder()} 为画布主链顺序。
     */
    public static List<SegmentIndices> segmentIndicesOnPath(List<String> path,
                                                            Map<String, JsonNode> nodesById) {
        List<SegmentIndices> segments = new ArrayList<>();
        int scanFrom = 0;
        while (scanFrom < path.size()) {
            Integer driveIdx = null;
            Integer actionIdx = null;
            Integer completeIdx = null;
            for (int j = scanFrom; j < path.size(); j++) {
                String type = nodeType(nodesById, path.get(j));
                if ("drive".equals(type) && driveIdx == null) {
                    driveIdx = j;
                } else if ("action".equals(type) && actionIdx == null) {
                    actionIdx = j;
                } else if ("complete".equals(type) && completeIdx == null) {
                    completeIdx = j;
                }
            }
            if (driveIdx == null || actionIdx == null || completeIdx == null) {
                break;
            }
            List<ProcessSegmentPhase> phaseOrder = buildPhaseOrder(driveIdx, actionIdx, completeIdx);
            segments.add(new SegmentIndices(
                    path.get(driveIdx),
                    path.get(actionIdx),
                    path.get(completeIdx),
                    phaseOrder));
            scanFrom = Math.max(Math.max(driveIdx, actionIdx), completeIdx) + 1;
        }
        return segments;
    }

    private static List<ProcessSegmentPhase> buildPhaseOrder(int driveIdx, int actionIdx, int completeIdx) {
        record Entry(int index, ProcessSegmentPhase phase) {
        }
        List<Entry> entries = List.of(
                new Entry(driveIdx, ProcessSegmentPhase.DRIVE),
                new Entry(actionIdx, ProcessSegmentPhase.ACTION),
                new Entry(completeIdx, ProcessSegmentPhase.COMPLETE));
        return entries.stream()
                .sorted(Comparator.comparingInt(Entry::index))
                .map(Entry::phase)
                .toList();
    }

    /** 按主链出现先后对同类型节点排序（用于自动补连线）。 */
    public static List<JsonNode> sortNodesOnPath(List<JsonNode> nodes,
                                                 String type,
                                                 List<String> path,
                                                 Map<String, JsonNode> nodesById) {
        List<JsonNode> filtered = new ArrayList<>();
        for (JsonNode node : nodes) {
            if (type.equals(nodeType(node, null))) {
                filtered.add(node);
            }
        }
        filtered.sort(Comparator
                .comparingInt((JsonNode n) -> pathIndex(path, textOrNull(n.get("id"))))
                .thenComparingInt(n -> positionX(nodesById, textOrNull(n.get("id")))));
        return filtered;
    }

    private static int positionX(Map<String, JsonNode> nodesById, String nodeId) {
        JsonNode node = nodesById != null ? nodesById.get(nodeId) : null;
        if (node == null || !node.has("position") || !node.get("position").has("x")) {
            return Integer.MAX_VALUE;
        }
        return node.get("position").get("x").asInt(Integer.MAX_VALUE);
    }

    public record SegmentIndices(String driveId,
                                 String actionId,
                                 String completeId,
                                 List<ProcessSegmentPhase> phaseOrder) {

        public List<String> orderedNodeIds() {
            return phaseOrder.stream()
                    .map(phase -> switch (phase) {
                        case DRIVE -> driveId;
                        case ACTION -> actionId;
                        case COMPLETE -> completeId;
                    })
                    .toList();
        }
    }

    private static int pathIndex(List<String> path, String nodeId) {
        if (nodeId == null) {
            return Integer.MAX_VALUE;
        }
        int idx = path.indexOf(nodeId);
        return idx < 0 ? Integer.MAX_VALUE : idx;
    }

    private static String pickNextTowardEnd(String current,
                                            List<String> candidates,
                                            String endId,
                                            Map<String, List<String>> outgoing,
                                            Map<String, JsonNode> nodesById) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        String best = null;
        int bestDepth = Integer.MAX_VALUE;
        for (String candidate : candidates) {
            if ("exception".equals(nodeType(nodesById, candidate))) {
                continue;
            }
            if (!canReach(endId, candidate, outgoing, new HashSet<>())) {
                continue;
            }
            int depth = depthToEnd(candidate, endId, outgoing, new HashSet<>());
            if (depth < bestDepth) {
                bestDepth = depth;
                best = candidate;
            }
        }
        if (best != null) {
            return best;
        }
        for (String candidate : candidates) {
            if (!"exception".equals(nodeType(nodesById, candidate))) {
                return candidate;
            }
        }
        return candidates.get(0);
    }

    private static boolean canReach(String targetId,
                                    String fromId,
                                    Map<String, List<String>> outgoing,
                                    Set<String> visiting) {
        if (targetId == null || fromId == null) {
            return false;
        }
        if (targetId.equals(fromId)) {
            return true;
        }
        if (!visiting.add(fromId)) {
            return false;
        }
        for (String next : outgoing.getOrDefault(fromId, List.of())) {
            if (canReach(targetId, next, outgoing, visiting)) {
                return true;
            }
        }
        return false;
    }

    private static int depthToEnd(String fromId,
                                String endId,
                                Map<String, List<String>> outgoing,
                                Set<String> visiting) {
        if (endId == null || fromId == null) {
            return Integer.MAX_VALUE;
        }
        if (endId.equals(fromId)) {
            return 0;
        }
        if (!visiting.add(fromId)) {
            return Integer.MAX_VALUE;
        }
        int min = Integer.MAX_VALUE;
        for (String next : outgoing.getOrDefault(fromId, List.of())) {
            int d = depthToEnd(next, endId, outgoing, visiting);
            if (d != Integer.MAX_VALUE) {
                min = Math.min(min, 1 + d);
            }
        }
        return min;
    }

    private static String nodeType(Map<String, JsonNode> nodesById, String nodeId) {
        if (nodesById == null || nodeId == null) {
            return null;
        }
        return nodeType(nodesById.get(nodeId), null);
    }

    private static String nodeType(JsonNode node, String ignored) {
        return node == null || node.isNull() ? null : textOrNull(node.get("type"));
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }
}
