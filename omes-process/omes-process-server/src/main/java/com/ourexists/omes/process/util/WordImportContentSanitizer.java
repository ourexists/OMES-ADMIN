package com.ourexists.omes.process.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word 工艺卡片续页页眉/页脚噪声过滤。
 */
public final class WordImportContentSanitizer {

    private static final Pattern PAGE_TOTAL = Pattern.compile("共\\s*\\d+\\s*页");
    private static final Pattern PAGE_NO = Pattern.compile("第\\s*\\d+\\s*页");
    private static final Pattern LATEX_ARTIFACT = Pattern.compile(
            "\\\\text\\{([^{}]*)}|_\\{\\}|\\^\\{([^{}]*)\\}|\\\\Phi|\\\\varphi");

    private WordImportContentSanitizer() {
    }

    public static boolean isPageBannerRow(List<String> cells) {
        if (cells == null || cells.isEmpty()) {
            return false;
        }
        String joined = joinNormalized(cells);
        if (!StringUtils.hasText(joined)) {
            return false;
        }
        if (joined.contains("橡胶") && joined.contains("塑料") && joined.contains("工艺卡片")) {
            return true;
        }
        if (PAGE_TOTAL.matcher(joined).find() || PAGE_NO.matcher(joined).find()) {
            return true;
        }
        if (joined.contains("工艺编号") && joined.contains("产品号")) {
            return true;
        }
        if (joined.contains("零组件号") && joined.contains("零组件名称")) {
            return true;
        }
        if (ProcessCardImportSupport.PROCESS_CODE.matcher(joined).find()
                && joined.contains("产品号")) {
            return true;
        }
        boolean hasComponentDrawing = cells.stream().anyMatch(c -> StringUtils.hasText(c) && c.contains("Q/1D"));
        boolean hasMaterialCode = cells.stream().anyMatch(c -> StringUtils.hasText(c)
                && (c.contains("FS6265") || c.contains("PS6265") || c.matches(".*[FP]S\\d{4,}.*")));
        if (hasComponentDrawing && hasMaterialCode) {
            return true;
        }
        return cells.stream().anyMatch(WordImportContentSanitizer::isPageMetadataText);
    }

    public static boolean isPageMetadataText(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String normalized = ProcessCardImportSupport.normalizeLabel(text);
        if ("编号".equals(normalized)) {
            return true;
        }
        if ("工序内容".equals(normalized) || normalized.contains("工序内容")) {
            return true;
        }
        if ("零组件号".equals(normalized)) {
            return true;
        }
        if (PAGE_TOTAL.matcher(normalized).find() || PAGE_NO.matcher(normalized).find()) {
            return true;
        }
        if ("工艺装备".equals(normalized) || "工装".equals(normalized) || "设备".equals(normalized)) {
            return true;
        }
        if (normalized.matches("YB\\d+-\\d+.*")) {
            return true;
        }
        if (normalized.matches("冶表\\d+.*")) {
            return true;
        }
        return false;
    }

    public static String sanitizeStepContent(String content) {
        if (!StringUtils.hasText(content)) {
            return content == null ? "" : content;
        }
        List<String> kept = new ArrayList<>();
        for (String line : content.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (!StringUtils.hasText(trimmed)) {
                continue;
            }
            if (isNoiseContentLine(trimmed)) {
                continue;
            }
            kept.add(stripInlineNoise(trimmed));
        }
        return String.join("\n", kept).trim();
    }

    public static String sanitizeFieldValue(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (String part : ProcessCardImportSupport.splitNames(value)) {
            if (!StringUtils.hasText(part) || isPageMetadataText(part)) {
                continue;
            }
            String normalized = ProcessCardImportSupport.normalizeLabel(part);
            if (PAGE_TOTAL.matcher(normalized).find() || PAGE_NO.matcher(normalized).find()) {
                continue;
            }
            parts.add(part.trim());
        }
        return String.join("、", parts);
    }

    public static String stripLatexArtifacts(String text) {
        if (!StringUtils.hasText(text)) {
            return text == null ? "" : text;
        }
        String cleaned = text;
        cleaned = cleaned.replaceAll("(Φ|φ)?(\\d+)\\^(\\d+)_\\{\\}\\\\text\\{(-?[\\d.]+)\\}", "$1$2($4/$3)");
        cleaned = cleaned.replace("_{}", "");
        cleaned = cleaned.replace("^_{}", "");
        var matcher = LATEX_ARTIFACT.matcher(cleaned);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement = matcher.group(1) != null ? matcher.group(1)
                    : matcher.group(2) != null ? matcher.group(2) : "Φ";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        cleaned = sb.toString().replace("\\Phi", "Φ").replace("\\varphi", "φ");
        return cleaned;
    }

    private static boolean isNoiseContentLine(String line) {
        String normalized = ProcessCardImportSupport.normalizeLabel(line);
        if (isPageMetadataText(line)) {
            return true;
        }
        if (normalized.contains("橡胶") && normalized.contains("塑料") && normalized.contains("工艺卡片")) {
            return true;
        }
        if (PAGE_TOTAL.matcher(normalized).find() || PAGE_NO.matcher(normalized).find()) {
            return true;
        }
        if ("工序号".equals(normalized)) {
            return true;
        }
        if (ProcessCardImportSupport.PROCESS_CODE.matcher(normalized).matches()) {
            return true;
        }
        return false;
    }

    private static String stripInlineNoise(String line) {
        String cleaned = line;
        cleaned = cleaned.replace("橡胶、塑料制件工艺卡片", "");
        cleaned = cleaned.replace("橡胶,塑料制件工艺卡片", "");
        cleaned = cleaned.replaceAll("零组件号\\s*", "");
        cleaned = cleaned.replaceAll("工\\s*序\\s*内\\s*容\\s*", "");
        cleaned = cleaned.replaceAll("Q/1D\\d+-\\d+×[\\d.]+-[A-Z]{2}", "");
        cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();
        return stripLatexArtifacts(cleaned);
    }

    private static String joinNormalized(List<String> cells) {
        StringBuilder sb = new StringBuilder();
        for (String cell : cells) {
            if (StringUtils.hasText(cell)) {
                sb.append(ProcessCardImportSupport.normalizeLabel(cell));
            }
        }
        return sb.toString();
    }
}
