package com.ourexists.omes.process.util;

import com.ourexists.omes.process.model.ProcessEquipmentRef;
import com.ourexists.omes.process.model.ProcessImportParseResult;
import com.ourexists.omes.process.model.ProcessMoldItem;
import com.ourexists.omes.process.model.ProcessStepItem;
import com.ourexists.omes.process.model.ProcessToolingRef;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工艺卡片（Word / PDF）导入共用的字段解析与写入。
 */
public final class ProcessCardImportSupport {

    static final Pattern STEP_NO = Pattern.compile("^\\d{1,3}$");
    static final Pattern PRESSURE = Pattern.compile("([\\d.]+)\\s*T", Pattern.CASE_INSENSITIVE);
    static final Pattern BLANK_WEIGHT = Pattern.compile("([\\d.]+)(?:\\+([\\d.]+))?(?:-([\\d.]+))?\\s*g?", Pattern.CASE_INSENSITIVE);
    static final Pattern TEMPERATURE = Pattern.compile("\\(?\\s*([\\d.]+)\\s*±\\s*([\\d.]+)\\s*\\)?");
    static final Pattern HOLD_TIME_MIN = Pattern.compile("([\\d.]+)\\s*min", Pattern.CASE_INSENSITIVE);
    static final Pattern HOLD_TIME_SEC = Pattern.compile("([\\d.]+)\\s*s(?:ec)?", Pattern.CASE_INSENSITIVE);
    static final Pattern PROCESS_CODE = Pattern.compile("\\d{3,5}-\\d{3,5}");
    static final Pattern MOLD_DRAWING_NO = Pattern.compile("\\d[Dd]\\d{3}-\\d{3,4}(?=\\d[Dd]\\d{3}-|$|[^0-9])");

    private ProcessCardImportSupport() {
    }

    public static boolean isParamLabel(String text) {
        return "材料预热".equals(text) || "压机压力".equals(text) || "压模图号".equals(text)
                || "压模槽数".equals(text) || "毛料重量".equals(text)
                || "压制温度".equals(text) || "保持时间".equals(text);
    }

    public static void applyParam(ProcessImportParseResult result, ParamAccumulator acc, String label, String value) {
        if (!StringUtils.hasText(label) || !StringUtils.hasText(value)) {
            return;
        }
        switch (label) {
            case "材料预热" -> result.setMaterialPreheat(value);
            case "压机压力" -> applyPressPressure(result, value);
            case "压模图号" -> acc.moldDrawingNos.addAll(splitMoldDrawingNos(value));
            case "压模槽数" -> acc.slotCountRaw = value;
            case "毛料重量" -> applyBlankWeight(result, value);
            case "压制温度" -> applyPressTemperature(result, value);
            case "保持时间" -> applyHoldTime(result, value);
            default -> {
            }
        }
    }

    public static void finalizeMolds(ProcessImportParseResult result, ParamAccumulator acc) {
        List<ProcessMoldItem> molds = buildMolds(acc.moldDrawingNos, acc.slotCountRaw);
        if (!molds.isEmpty()) {
            result.setMolds(molds);
        }
    }

    public static List<ProcessMoldItem> buildMolds(List<String> moldDrawingNos, String slotCountRaw) {
        List<String> drawingNos = new ArrayList<>();
        for (String drawingNo : moldDrawingNos) {
            if (StringUtils.hasText(drawingNo)) {
                drawingNos.add(drawingNo.trim());
            }
        }
        if (drawingNos.isEmpty()) {
            return List.of();
        }

        List<Integer> slotCounts = resolveSlotCountsForDrawings(drawingNos.size(), slotCountRaw);
        List<ProcessMoldItem> molds = new ArrayList<>();
        for (int i = 0; i < drawingNos.size(); i++) {
            ProcessMoldItem mold = new ProcessMoldItem();
            mold.setMoldDrawingNo(drawingNos.get(i));
            mold.setSlotCount(i < slotCounts.size() ? slotCounts.get(i) : 0);
            molds.add(mold);
        }
        return molds;
    }

    public static List<String> splitMoldDrawingNos(String value) {
        List<String> fromLines = splitLines(value);
        List<String> result = new ArrayList<>();
        for (String line : fromLines) {
            Matcher matcher = MOLD_DRAWING_NO.matcher(line);
            boolean found = false;
            while (matcher.find()) {
                result.add(matcher.group());
                found = true;
            }
            if (!found && StringUtils.hasText(line)) {
                result.add(line.trim());
            }
        }
        return result;
    }

    public static void applyPressPressure(ProcessImportParseResult result, String value) {
        Matcher matcher = PRESSURE.matcher(value);
        if (matcher.find()) {
            result.setPressPressure(toDecimal(matcher.group(1)));
            return;
        }
        BigDecimal numeric = parsePlainNumber(value);
        if (numeric != null) {
            result.setPressPressure(numeric);
        }
    }

    public static void applyBlankWeight(ProcessImportParseResult result, String value) {
        String normalized = OfficeMathTextExtractor.normalizeMathPlainText(value);
        Matcher matcher = BLANK_WEIGHT.matcher(normalized);
        if (!matcher.find()) {
            BigDecimal numeric = parsePlainNumber(normalized);
            if (numeric != null) {
                result.setBlankWeight(numeric);
            }
            return;
        }
        result.setBlankWeight(toDecimal(matcher.group(1)));
        if (matcher.group(2) != null) {
            result.setBlankWeightUpperOffset(toDecimal(matcher.group(2)));
        }
        if (matcher.group(3) != null) {
            result.setBlankWeightLowerOffset(toDecimal(matcher.group(3)));
        }
    }

    public static void applyPressTemperature(ProcessImportParseResult result, String value) {
        String normalized = value.replace(" ", "").replace('℃', ' ').replace('°', ' ').trim();
        Matcher matcher = TEMPERATURE.matcher(normalized);
        if (matcher.find()) {
            result.setPressTemperature(toDecimal(matcher.group(1)));
            BigDecimal offset = toDecimal(matcher.group(2));
            result.setPressTemperatureUpperOffset(offset);
            result.setPressTemperatureLowerOffset(offset.negate());
            return;
        }
        BigDecimal numeric = parsePlainNumber(normalized);
        if (numeric != null) {
            result.setPressTemperature(numeric);
        }
    }

    public static void applyHoldTime(ProcessImportParseResult result, String value) {
        String text = value.trim();
        Matcher minMatcher = HOLD_TIME_MIN.matcher(text);
        if (minMatcher.find()) {
            BigDecimal minutes = toDecimal(minMatcher.group(1));
            result.setHoldTimeSeconds(minutes.multiply(BigDecimal.valueOf(60)).intValue());
            return;
        }
        Matcher secMatcher = HOLD_TIME_SEC.matcher(text);
        if (secMatcher.find()) {
            result.setHoldTimeSeconds(toDecimal(secMatcher.group(1)).intValue());
            return;
        }
        BigDecimal numeric = parsePlainNumber(text);
        if (numeric != null) {
            result.setHoldTimeSeconds(numeric.multiply(BigDecimal.valueOf(60)).intValue());
        }
    }

    public static void applyEquipments(ProcessStepItem step, String equipment) {
        for (String name : splitNames(equipment)) {
            ProcessEquipmentRef ref = new ProcessEquipmentRef();
            ref.setEquipmentCode(name);
            ref.setEquipmentName(name);
            step.getEquipments().add(ref);
        }
    }

    public static void applyStepEquipAndTooling(ProcessStepItem step, String equipment, String tooling) {
        List<String> equipmentNames = new ArrayList<>();
        List<String> toolingNames = new ArrayList<>();
        for (String name : splitEquipToolingNames(equipment)) {
            classifyEquipToolingName(name, equipmentNames, toolingNames);
        }
        for (String name : splitEquipToolingNames(tooling)) {
            classifyEquipToolingName(name, equipmentNames, toolingNames);
        }
        for (String name : equipmentNames) {
            ProcessEquipmentRef ref = new ProcessEquipmentRef();
            ref.setEquipmentCode(name);
            ref.setEquipmentName(name);
            step.getEquipments().add(ref);
        }
        for (String name : toolingNames) {
            ProcessToolingRef ref = new ProcessToolingRef();
            ref.setToolingCode(name);
            ref.setToolingName(name);
            step.getToolings().add(ref);
        }
    }

    static List<String> splitEquipToolingNames(String value) {
        List<String> names = new ArrayList<>();
        if (!StringUtils.hasText(value)) {
            return names;
        }
        for (String part : value.split("[,，、\\n或]")) {
            if (StringUtils.hasText(part)) {
                names.add(part.trim());
            }
        }
        return names;
    }

    static void classifyEquipToolingName(String name, List<String> equipmentNames, List<String> toolingNames) {
        if (!StringUtils.hasText(name) || isIgnorableImportRefName(name)) {
            return;
        }
        if (isProcessToolingName(name)) {
            toolingNames.add(name);
            return;
        }
        if (isProcessEquipmentName(name)) {
            equipmentNames.add(name);
        }
    }

    static boolean isIgnorableImportRefName(String name) {
        if (WordImportContentSanitizer.isPageMetadataText(name)) {
            return true;
        }
        String normalized = normalizeLabel(name);
        return normalized.matches("冶表\\d+.*");
    }

    static boolean isProcessToolingName(String name) {
        String normalized = normalizeLabel(name);
        if (!StringUtils.hasText(normalized)) {
            return false;
        }
        return normalized.contains("量规") || normalized.contains("卡尺") || normalized.contains("显微镜")
                || normalized.contains("测量仪") || normalized.contains("量具") || normalized.contains("针规")
                || normalized.contains("胶模") || normalized.contains("模具")
                || MOLD_DRAWING_NO.matcher(name).find();
    }

    static boolean isProcessEquipmentName(String name) {
        String normalized = normalizeLabel(name);
        if (!StringUtils.hasText(normalized) || isProcessToolingName(name)) {
            return false;
        }
        return normalized.contains("机") || normalized.contains("烘箱") || normalized.contains("烤箱")
                || normalized.contains("炉") || normalized.contains("台");
    }

    public static void applyToolings(ProcessStepItem step, String tooling) {
        for (String name : splitNames(tooling)) {
            ProcessToolingRef ref = new ProcessToolingRef();
            ref.setToolingCode(name);
            ref.setToolingName(name);
            step.getToolings().add(ref);
        }
    }

    public static void mergeStepEquipAndTooling(ProcessStepItem step, String equipment, String tooling) {
        List<String> equipmentNames = new ArrayList<>();
        List<String> toolingNames = new ArrayList<>();
        for (String name : splitEquipToolingNames(equipment)) {
            classifyEquipToolingName(name, equipmentNames, toolingNames);
        }
        for (String name : splitEquipToolingNames(tooling)) {
            classifyEquipToolingName(name, equipmentNames, toolingNames);
        }
        if (step.getEquipments().isEmpty()) {
            for (String name : equipmentNames) {
                ProcessEquipmentRef ref = new ProcessEquipmentRef();
                ref.setEquipmentCode(name);
                ref.setEquipmentName(name);
                step.getEquipments().add(ref);
            }
        }
        for (String name : toolingNames) {
            mergeToolings(step, name);
        }
    }

    public static void mergeToolings(ProcessStepItem step, String tooling) {
        for (String name : splitNames(tooling)) {
            boolean exists = step.getToolings().stream()
                    .anyMatch(item -> name.equals(item.getToolingName()) || name.equals(item.getToolingCode()));
            if (!exists) {
                ProcessToolingRef ref = new ProcessToolingRef();
                ref.setToolingCode(name);
                ref.setToolingName(name);
                step.getToolings().add(ref);
            }
        }
    }

    public static String extractStepContent(String content, String stepName) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String trimmed = content.trim();
        String[] parts = trimmed.split("\\r?\\n", 2);
        String firstLine = parts[0].trim();
        String remainder = parts.length > 1 ? parts[1].trim() : "";
        if (isSameStepTitle(firstLine, stepName)) {
            return remainder;
        }
        return trimmed;
    }

    public static String normalizePdfDiameterGlyph(String text) {
        if (!StringUtils.hasText(text)) {
            return text == null ? "" : text;
        }
        String normalized = text.replaceAll("(?<=[为、①(（])[?？\uFFFD](?=\\d)", "φ");
        normalized = normalized.replaceAll("(?<=\\s)[?？\uFFFD](?=\\d)", "φ");
        if (normalized.length() > 0) {
            char first = normalized.charAt(0);
            if ((first == '?' || first == '？' || first == '\uFFFD') && normalized.length() > 1
                    && Character.isDigit(normalized.charAt(1))) {
                normalized = "φ" + normalized.substring(1);
            }
        }
        return normalized;
    }

    public static String mergeMisplacedStepColumn(String stepNoCol, String contentCol) {
        if (!StringUtils.hasText(stepNoCol) || isStepNoCell(stepNoCol)) {
            return contentCol == null ? "" : contentCol.trim();
        }
        String misplaced = stepNoCol.trim();
        if (!StringUtils.hasText(contentCol)) {
            return misplaced;
        }
        return misplaced + " " + contentCol.trim();
    }

    public static String extractStandaloneProcessCode(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        Matcher matcher = PROCESS_CODE.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            if (start > 0) {
                char prev = text.charAt(start - 1);
                if (Character.isLetter(prev) || Character.isDigit(prev)) {
                    continue;
                }
            }
            return matcher.group();
        }
        return "";
    }

    public static String extractStepName(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String firstLine = content.split("\\r?\\n", 2)[0].trim();
        if (firstLine.length() <= 20 && !firstLine.contains("。") && !firstLine.contains("，")) {
            return firstLine.replaceAll("\\s+", "");
        }
        return firstLine.length() > 32 ? firstLine.substring(0, 32) : firstLine;
    }

    public static void appendStepContent(ProcessStepItem step, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        if (!StringUtils.hasText(step.getStepContent())) {
            step.setStepContent(content.trim());
            return;
        }
        step.setStepContent(step.getStepContent().trim() + "\n" + content.trim());
    }

    public static boolean isStepNoCell(String text) {
        String trimmed = text.trim();
        if (STEP_NO.matcher(trimmed).matches()) {
            return true;
        }
        return trimmed.matches("\\d{1,3}\\.0+");
    }

    public static int parseStepNoCell(String text) {
        String trimmed = text.trim();
        if (trimmed.contains(".")) {
            return (int) Double.parseDouble(trimmed);
        }
        return Integer.parseInt(trimmed);
    }

    public static boolean isFooterText(String text) {
        String normalized = normalizeLabel(text);
        if (!StringUtils.hasText(normalized)) {
            return false;
        }
        return normalized.contains("编制") || normalized.contains("校对") || normalized.contains("审核")
                || normalized.contains("会签") || normalized.contains("审定") || normalized.contains("批准")
                || normalized.contains("更改单号") || "标记".equals(normalized);
    }

    public static String normalizeLabel(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }

    public static List<String> splitLines(String value) {
        List<String> lines = new ArrayList<>();
        for (String line : value.split("\\r?\\n")) {
            if (StringUtils.hasText(line)) {
                lines.add(line.trim());
            }
        }
        return lines;
    }

    public static List<String> splitNames(String value) {
        List<String> names = new ArrayList<>();
        if (!StringUtils.hasText(value)) {
            return names;
        }
        for (String part : value.split("[,，、\\n]")) {
            if (StringUtils.hasText(part)) {
                names.add(part.trim());
            }
        }
        return names;
    }

    public static BigDecimal parsePlainNumber(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("=")) {
            return null;
        }
        Matcher matcher = Pattern.compile("^[+-]?\\d+(?:\\.\\d+)?").matcher(trimmed);
        if (!matcher.find()) {
            return null;
        }
        try {
            return toDecimal(matcher.group());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String formatSlotCountFromMathParts(List<String> parts) {
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        List<String> numbers = new ArrayList<>();
        for (String part : parts) {
            Integer value = parseInteger(part);
            if (value != null) {
                numbers.add(String.valueOf(value));
            }
        }
        return String.join("\n", numbers);
    }

    private static boolean isSameStepTitle(String firstLine, String stepName) {
        if (!StringUtils.hasText(firstLine) || !StringUtils.hasText(stepName)) {
            return false;
        }
        return firstLine.replaceAll("\\s+", "").equals(stepName.replaceAll("\\s+", ""));
    }

    private static List<Integer> resolveSlotCountsForDrawings(int drawingCount, String rawValue) {
        if (drawingCount <= 0 || !StringUtils.hasText(rawValue)) {
            return List.of();
        }
        List<Integer> perLine = parseSlotCountLines(rawValue);
        if (drawingCount == 1) {
            Integer revised = parseSlotCount(rawValue);
            return List.of(revised != null ? revised : 0);
        }
        if (perLine.isEmpty()) {
            return List.of();
        }
        if (perLine.size() == 1) {
            int shared = perLine.get(0);
            List<Integer> sharedSlots = new ArrayList<>(drawingCount);
            for (int i = 0; i < drawingCount; i++) {
                sharedSlots.add(shared);
            }
            return sharedSlots;
        }
        List<Integer> paired = new ArrayList<>(drawingCount);
        for (int i = 0; i < drawingCount; i++) {
            paired.add(i < perLine.size() ? perLine.get(i) : 0);
        }
        return paired;
    }

    private static List<Integer> parseSlotCountLines(String value) {
        List<Integer> slotCounts = new ArrayList<>();
        for (String line : splitLines(value)) {
            Integer parsed = parseSlotCountOnLine(line);
            if (parsed != null) {
                slotCounts.add(parsed);
            }
        }
        return slotCounts;
    }

    private static Integer parseSlotCount(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        List<String> tokens = new ArrayList<>();
        for (String line : value.split("\\r?\\n")) {
            if (!StringUtils.hasText(line)) {
                continue;
            }
            for (String token : line.trim().split("\\s+")) {
                if (StringUtils.hasText(token)) {
                    tokens.add(token.trim());
                }
            }
        }
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Integer parsed = parseInteger(tokens.get(i));
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static Integer parseSlotCountOnLine(String line) {
        if (!StringUtils.hasText(line)) {
            return null;
        }
        List<String> tokens = new ArrayList<>();
        for (String token : line.trim().split("\\s+")) {
            if (StringUtils.hasText(token)) {
                tokens.add(token.trim());
            }
        }
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Integer parsed = parseInteger(tokens.get(i));
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.replaceAll("[^\\d-]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal toDecimal(String value) {
        return new BigDecimal(value).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    public static final class ParamAccumulator {
        public final List<String> moldDrawingNos = new ArrayList<>();
        public String slotCountRaw = "";
    }
}
