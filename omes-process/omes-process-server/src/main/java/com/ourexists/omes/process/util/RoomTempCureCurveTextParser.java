package com.ourexists.omes.process.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ourexists.omes.process.model.ProcessStepItem;
import com.ourexists.omes.process.engine.support.DurationTextParser;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析工艺卡片中的室温硫化曲线文案，如：
 * {@code 室温→┴1h80℃→┴1h140℃→┴1h200℃×6h恒温}，
 * 转为工序 {@code params} JSON（与前端 {@code roomTempCureParams.js} 一致）。
 */
public final class RoomTempCureCurveTextParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Pattern CURVE_HINT = Pattern.compile("室温.*℃.*(?:→|->|┴)");

    /**
     * 单段：{@code 1h80℃}、{@code ┴1h140℃}、{@code 1h200℃×6h恒温}
     */
    private static final Pattern SEGMENT = Pattern.compile(
            "(?:┴)?\\s*"
                    + "([\\d.]+)\\s*"
                    + "(h|hr|hrs|hour|hours|min|mins|minute|minutes|m(?!in)|s|sec|secs|second|seconds|小时|分|秒)?\\s*"
                    + "([\\d.]+)\\s*℃"
                    + "(?:\\s*×\\s*([\\d.]+)\\s*"
                    + "(h|hr|hrs|hour|hours|min|mins|minute|minutes|m(?!in)|s|sec|secs|second|seconds|小时|分|秒)?\\s*恒温)?",
            Pattern.CASE_INSENSITIVE);

    private RoomTempCureCurveTextParser() {
    }

    public static boolean isRoomTempCureStep(String stepName) {
        if (!StringUtils.hasText(stepName)) {
            return false;
        }
        String key = stepName.replaceAll("\\s+", "")
                .replaceAll("[^\\p{L}\\p{N}]", "")
                .trim();
        return "二段硫化".equals(key);
    }

    public static boolean looksLikeCurveText(String text) {
        return StringUtils.hasText(text) && CURVE_HINT.matcher(normalizeCurveText(text)).find();
    }

    /**
     * 写入已解析的 params，并在工序正文中保留/补全曲线文案。
     */
    public static void applyParsedParams(ProcessStepItem step, String paramsJson, String curveSourceText) {
        if (step == null || !StringUtils.hasText(paramsJson)) {
            return;
        }
        step.setParams(paramsJson);
        mergeCurveIntoStepContent(step, curveSourceText);
    }

    /**
     * 工序正文已含可解析曲线时不再改动；否则将曲线文案追加到正文。
     */
    public static void mergeCurveIntoStepContent(ProcessStepItem step, String curveSourceText) {
        if (step == null || !StringUtils.hasText(curveSourceText)) {
            return;
        }
        String content = step.getStepContent() == null ? "" : step.getStepContent().trim();
        if (contentContainsParseableCurve(content)) {
            return;
        }
        String curveLine = extractCurveLine(curveSourceText);
        if (!StringUtils.hasText(curveLine)) {
            return;
        }
        if (!StringUtils.hasText(content)) {
            step.setStepContent(curveLine);
            return;
        }
        int paramIdx = content.indexOf("参数为");
        if (paramIdx >= 0) {
            String afterParam = content.substring(paramIdx);
            if (!contentContainsParseableCurve(afterParam)) {
                step.setStepContent(content + curveLine);
                return;
            }
        }
        step.setStepContent(content + "\n" + curveLine);
    }

    public static String extractCurveLine(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = normalizeCurveText(text);
        int roomIdx = normalized.indexOf("室温");
        if (roomIdx < 0) {
            return "";
        }
        return normalized.substring(roomIdx).trim();
    }

    static boolean contentContainsParseableCurve(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        if (tryParseToParamsJson(content).isPresent()) {
            return true;
        }
        for (String line : content.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (looksLikeCurveText(trimmed) || tryParseToParamsJson(trimmed).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return {@code {"segments":[...]}} JSON，无法识别时为空
     */
    public static Optional<String> tryParseToParamsJson(String text) {
        List<Segment> segments = parseSegments(text);
        if (segments.isEmpty()) {
            return Optional.empty();
        }
        try {
            ObjectNode root = MAPPER.createObjectNode();
            ArrayNode arr = root.putArray("segments");
            for (Segment seg : segments) {
                ObjectNode item = arr.addObject();
                item.put("duration", seg.durationSec());
                item.put("to", seg.toTemp());
                if (seg.holdDurationSec() > 0) {
                    item.put("holdDuration", seg.holdDurationSec());
                }
            }
            return Optional.of(MAPPER.writeValueAsString(root));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * 从工序正文中移除已识别的曲线行，保留「参数为：」等说明文字。
     */
    public static String stripCurveFromContent(String content) {
        if (!StringUtils.hasText(content)) {
            return content == null ? "" : content;
        }
        StringBuilder kept = new StringBuilder();
        for (String line : content.split("\\r?\\n")) {
            if (!StringUtils.hasText(line)) {
                if (kept.length() > 0) {
                    kept.append('\n');
                }
                continue;
            }
            String trimmed = line.trim();
            if (looksLikeCurveText(trimmed) || tryParseToParamsJson(trimmed).isPresent()) {
                int paramIdx = trimmed.indexOf("参数为");
                if (paramIdx >= 0) {
                    String prefix = trimmed.substring(0, paramIdx + 3).trim();
                    if (StringUtils.hasText(prefix)) {
                        if (kept.length() > 0) {
                            kept.append('\n');
                        }
                        kept.append(prefix).append('：');
                    }
                }
                continue;
            }
            if (kept.length() > 0) {
                kept.append('\n');
            }
            kept.append(trimmed);
        }
        return kept.toString().trim();
    }

    static List<Segment> parseSegments(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String normalized = normalizeCurveText(text);
        int start = normalized.indexOf("室温");
        if (start < 0) {
            return List.of();
        }
        String afterRoom = normalized.substring(start + 2).trim();
        if (afterRoom.startsWith("→") || afterRoom.startsWith("->")) {
            afterRoom = afterRoom.substring(afterRoom.startsWith("->") ? 2 : 1).trim();
        }
        if (!StringUtils.hasText(afterRoom)) {
            return List.of();
        }

        String[] chunks = afterRoom.split("→|->");
        List<Segment> segments = new ArrayList<>();
        for (String chunk : chunks) {
            String part = chunk.trim();
            if (!StringUtils.hasText(part)) {
                continue;
            }
            parseSegment(part).ifPresent(segments::add);
        }
        return segments;
    }

    /**
     * 合并工序正文与当前行文本供曲线解析，避免 Word/PDF 导入时正文已含曲线又拼接整格重复解析。
     */
    public static String mergeParseBlob(String stepContent, String extraText) {
        String a = stepContent == null ? "" : stepContent.trim();
        String b = extraText == null ? "" : extraText.trim();
        if (!StringUtils.hasText(a)) {
            return b;
        }
        if (!StringUtils.hasText(b)) {
            return a;
        }
        if (a.equals(b) || b.contains(a)) {
            return b;
        }
        if (a.contains(b)) {
            return a;
        }
        return a + "\n" + b;
    }

    private static Optional<Segment> parseSegment(String part) {
        Matcher matcher = SEGMENT.matcher(part.trim());
        if (!matcher.lookingAt()) {
            return Optional.empty();
        }
        try {
            int durationSec = parseDurationToken(matcher.group(1), matcher.group(2));
            double to = Double.parseDouble(matcher.group(3));
            int holdSec = 0;
            if (matcher.group(4) != null) {
                holdSec = parseDurationToken(matcher.group(4), matcher.group(5));
            }
            if (durationSec <= 0 || !Double.isFinite(to)) {
                return Optional.empty();
            }
            return Optional.of(new Segment(durationSec, to, holdSec));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static int parseDurationToken(String amount, String unit) {
        String raw = amount.trim();
        if (StringUtils.hasText(unit)) {
            raw = raw + unit.trim();
        }
        return DurationTextParser.parseToSeconds(raw);
    }

    private static String normalizeCurveText(String text) {
        String normalized = text.replace('\r', ' ')
                .replace('\n', ' ')
                .replace('ℎ', 'h')
                .replace("﹥", "→")
                .replace("->", "→")
                .replaceAll("(\\d+)\\s*h\\s*(\\d+)\\s*℃", "$1h$2℃")
                .replaceAll("(\\d+)h\\s+(\\d+)\\s*℃", "$1h$2℃")
                .replaceAll("\\s+", " ")
                .trim();
        return OfficeMathTextExtractor.normalizeMathPlainText(normalized);
    }

    record Segment(int durationSec, double toTemp, int holdDurationSec) {
    }
}
