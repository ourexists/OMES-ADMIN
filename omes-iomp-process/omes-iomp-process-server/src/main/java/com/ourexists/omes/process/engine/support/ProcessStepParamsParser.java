package com.ourexists.omes.process.engine.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.engine.model.ProcessStepTimeSegment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析工序 {@code params} JSON，提取引擎动态参数（如 RAMP segments）。
 * <p>
 * 示例：{@code {"segments":[{"to":80,"duration":60},{"to":140,"duration":60,"holdDuration":3600}]}}
 */
@Component
@RequiredArgsConstructor
public class ProcessStepParamsParser {

    private final ObjectMapper objectMapper;

    public List<ProcessStepTimeSegment> parseRampSegments(String params) {
        if (!StringUtils.hasText(params)) {
            return List.of();
        }
        String trimmed = params.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            throw new BusinessException("工序 params 须为 JSON 格式");
        }
        try {
            JsonNode root = objectMapper.readTree(trimmed);
            JsonNode segmentsNode = root.isArray() ? root : root.get("segments");
            if (segmentsNode == null || !segmentsNode.isArray() || segmentsNode.isEmpty()) {
                return List.of();
            }
            List<ProcessStepTimeSegment> segments = new ArrayList<>();
            int index = 0;
            for (JsonNode item : segmentsNode) {
                index++;
                if (item == null || item.isNull()) {
                    continue;
                }
                JsonNode toNode = item.get("to");
                JsonNode durationNode = item.get("duration");
                if (toNode == null || toNode.isNull() || durationNode == null || durationNode.isNull()) {
                    throw new BusinessException("params.segments 第 " + index + " 段须包含 to 与 duration");
                }
                double to = toNode.asDouble();
                int durationSec = parseDurationSeconds(durationNode, index);
                Integer holdSec = null;
                JsonNode holdNode = item.get("holdDuration");
                if (holdNode != null && !holdNode.isNull()) {
                    holdSec = parseDurationSeconds(holdNode, index, "holdDuration");
                    if (holdSec <= 0) {
                        holdSec = null;
                    }
                }
                segments.add(new ProcessStepTimeSegment(to, durationSec, holdSec));
            }
            return segments;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("工序 params 解析失败: " + ex.getMessage());
        }
    }

    private static int parseDurationSeconds(JsonNode node, int segmentIndex) {
        return parseDurationSeconds(node, segmentIndex, "duration");
    }

    private static int parseDurationSeconds(JsonNode node, int segmentIndex, String field) {
        if (node.isNumber()) {
            int sec = node.asInt();
            if (sec <= 0) {
                throw new BusinessException("params.segments 第 " + segmentIndex + " 段 " + field + " 须大于 0");
            }
            return sec;
        }
        String text = node.asText("");
        if (!StringUtils.hasText(text)) {
            throw new BusinessException("params.segments 第 " + segmentIndex + " 段缺少 " + field);
        }
        int sec = DurationTextParser.parseToSeconds(text);
        if (sec <= 0) {
            throw new BusinessException("params.segments 第 " + segmentIndex + " 段 " + field + " 须大于 0");
        }
        return sec;
    }
}
