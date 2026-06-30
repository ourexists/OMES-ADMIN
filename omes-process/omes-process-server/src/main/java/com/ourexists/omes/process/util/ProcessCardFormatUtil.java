package com.ourexists.omes.process.util;

import com.ourexists.omes.process.model.ProcessEquipmentRef;
import com.ourexists.omes.process.model.ProcessMoldItem;
import com.ourexists.omes.process.model.ProcessStepItem;
import com.ourexists.omes.process.model.ProcessToolingRef;
import com.ourexists.omes.process.model.ProcessVO;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工艺卡片导出字段格式化（Word / PDF 模板共用）。
 */
public final class ProcessCardFormatUtil {

    private ProcessCardFormatUtil() {
    }

    public static int resolveStepNo(ProcessStepItem step, int index) {
        if (step.getStepNo() != null && step.getStepNo() > 0) {
            return step.getStepNo();
        }
        return (index + 1) * 5;
    }

    public static String formatPressPressure(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return stripTrailingZeros(value) + "T";
    }

    public static String formatBlankWeight(BigDecimal weight, BigDecimal upper, BigDecimal lower) {
        if (weight == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(stripTrailingZeros(weight));
        if (upper != null && upper.compareTo(BigDecimal.ZERO) != 0) {
            sb.append('+').append(stripTrailingZeros(upper));
        }
        if (lower != null && lower.compareTo(BigDecimal.ZERO) != 0) {
            sb.append('-').append(stripTrailingZeros(lower));
        }
        sb.append('g');
        return sb.toString();
    }

    public static String formatPressTemperature(BigDecimal temp, BigDecimal upperOffset, BigDecimal lowerOffset) {
        if (temp == null) {
            return "";
        }
        BigDecimal tolerance = upperOffset != null ? upperOffset.abs()
                : (lowerOffset != null ? lowerOffset.abs() : null);
        if (tolerance == null) {
            return stripTrailingZeros(temp) + "℃";
        }
        return "(" + stripTrailingZeros(temp) + "±" + stripTrailingZeros(tolerance) + ")℃";
    }

    public static String formatHoldTime(Integer seconds) {
        if (seconds == null || seconds <= 0) {
            return "";
        }
        if (seconds % 60 == 0) {
            return (seconds / 60) + "min";
        }
        return seconds + "s";
    }

    public static String formatMoldDrawingNos(List<ProcessMoldItem> molds) {
        if (CollectionUtils.isEmpty(molds)) {
            return "";
        }
        return molds.stream()
                .map(ProcessMoldItem::getMoldDrawingNo)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining("\n"));
    }

    public static String formatSlotCounts(List<ProcessMoldItem> molds) {
        if (CollectionUtils.isEmpty(molds)) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        for (ProcessMoldItem mold : molds) {
            if (!StringUtils.hasText(mold.getMoldDrawingNo())) {
                continue;
            }
            int slot = mold.getSlotCount() != null ? mold.getSlotCount() : 0;
            lines.add(String.valueOf(slot));
        }
        if (lines.isEmpty()) {
            return "";
        }
        if (lines.size() == 1) {
            return lines.get(0);
        }
        return String.join("\n", lines);
    }

    public static String formatMaterial(ProcessVO vo) {
        if (StringUtils.hasText(vo.getMaterialCode()) && StringUtils.hasText(vo.getMaterialName())) {
            if (vo.getMaterialCode().equals(vo.getMaterialName())) {
                return vo.getMaterialCode().trim();
            }
            return vo.getMaterialCode().trim() + " " + vo.getMaterialName().trim();
        }
        if (StringUtils.hasText(vo.getMaterialCode())) {
            return vo.getMaterialCode().trim();
        }
        return StringUtils.hasText(vo.getMaterialName()) ? vo.getMaterialName().trim() : "";
    }

    public static String formatEquipments(ProcessStepItem step) {
        if (CollectionUtils.isEmpty(step.getEquipments())) {
            return "";
        }
        return step.getEquipments().stream()
                .map(ProcessCardFormatUtil::equipmentLabel)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("、"));
    }

    public static String formatToolings(ProcessStepItem step) {
        if (CollectionUtils.isEmpty(step.getToolings())) {
            return "";
        }
        return step.getToolings().stream()
                .map(ProcessCardFormatUtil::toolingLabel)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("、"));
    }

    public static List<String> splitStepContentLines(ProcessStepItem step) {
        List<String> lines = new ArrayList<>();
        String name = StringUtils.hasText(step.getStepName()) ? step.getStepName().trim() : "";
        String content = StringUtils.hasText(step.getStepContent()) ? step.getStepContent().trim() : "";
        if (StringUtils.hasText(name)) {
            lines.add(name);
        }
        if (StringUtils.hasText(content)) {
            for (String line : content.split("\\r?\\n")) {
                if (StringUtils.hasText(line)) {
                    lines.add(line.trim());
                }
            }
        }
        if (lines.isEmpty() && StringUtils.hasText(name)) {
            lines.add(name);
        }
        return lines;
    }

    /**
     * 导出用：工序名称单独一行；名称与正文、正文各段（仅按换行符）之间各空一行。
     * 段内超列宽按固定字数折到下一表格行，折行段之间不插空行。
     */
    public static List<String> splitStepContentLinesForExport(ProcessStepItem step) {
        List<String> lines = new ArrayList<>();
        String name = StringUtils.hasText(step.getStepName()) ? step.getStepName().trim() : "";
        String content = StringUtils.hasText(step.getStepContent()) ? step.getStepContent().trim() : "";
        if (StringUtils.hasText(name)) {
            lines.add(name);
        }
        if (StringUtils.hasText(content)) {
            if (StringUtils.hasText(name)) {
                lines.add("");
            }
            List<String> paragraphs = splitExportParagraphs(content);
            appendExportParagraphs(lines, paragraphs);
        }
        if (lines.isEmpty() && StringUtils.hasText(name)) {
            lines.add(name);
        }
        return lines;
    }

    private static void appendExportParagraphs(List<String> lines, List<String> paragraphs) {
        for (int p = 0; p < paragraphs.size(); p++) {
            lines.addAll(ProcessCardStepContentWrapper.wrapLine(paragraphs.get(p)));
            if (p < paragraphs.size() - 1) {
                lines.add("");
            }
        }
    }

    private static List<String> splitExportParagraphs(String content) {
        List<String> paragraphs = new ArrayList<>();
        for (String rawLine : content.split("\\r?\\n")) {
            if (StringUtils.hasText(rawLine)) {
                paragraphs.add(rawLine.trim());
            }
        }
        return paragraphs;
    }

    public static String safeFilename(String processCode, String processName, String ext) {
        String base = StringUtils.hasText(processCode) ? processCode.trim()
                : (StringUtils.hasText(processName) ? processName.trim() : "工艺卡片");
        base = base.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (base.length() > 80) {
            base = base.substring(0, 80);
        }
        return base + ext;
    }

    private static String equipmentLabel(ProcessEquipmentRef ref) {
        if (StringUtils.hasText(ref.getEquipmentName())) {
            return ref.getEquipmentName().trim();
        }
        return StringUtils.hasText(ref.getEquipmentCode()) ? ref.getEquipmentCode().trim() : "";
    }

    private static String toolingLabel(ProcessToolingRef ref) {
        if (StringUtils.hasText(ref.getToolingName())) {
            return ref.getToolingName().trim();
        }
        return StringUtils.hasText(ref.getToolingCode()) ? ref.getToolingCode().trim() : "";
    }

    private static String stripTrailingZeros(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
