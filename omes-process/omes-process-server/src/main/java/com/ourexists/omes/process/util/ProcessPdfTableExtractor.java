package com.ourexists.omes.process.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 将 Word 导出的可编辑 PDF 还原为与 {@link ProcessCardImportParser} / Word 表格相同的行结构。
 */
public final class ProcessPdfTableExtractor {

    private static final float Y_TOLERANCE = 8f;

    private static final float COL_STEP_NO_MAX = 130f;
    private static final float COL_CONTENT_MAX = 395f;
    private static final float COL_EQUIP_MAX = 455f;

    private static final float COL_PARAM_LABEL_MIN = 420f;
    private static final float COL_PARAM_VALUE_MIN = 490f;

    private static final float COL_BASIC_PRODUCT_MAX = 170f;
    private static final float COL_BASIC_COMPONENT_MAX = 300f;
    private static final float COL_BASIC_NAME_MAX = 430f;
    private static final float COL_BASIC_MATERIAL_MAX = 490f;

    private ProcessPdfTableExtractor() {
    }

    public static String extractHeaderProcessCode(PDDocument document) throws IOException {
        for (LineGroup group : lineGroups(document)) {
            if (group.pageIndex != 0) {
                break;
            }
            String text = joinGroupText(group);
            String normalized = ProcessCardImportSupport.normalizeLabel(text);
            if (normalized.contains("第") && normalized.contains("页")) {
                String code = ProcessCardImportSupport.extractStandaloneProcessCode(text);
                if (StringUtils.hasText(code)) {
                    return code;
                }
            }
        }
        return "";
    }

    public static List<ProcessCardImportParser.ImportTable> extractTables(PDDocument document) throws IOException {
        List<LineGroup> groups = lineGroups(document);
        List<ProcessCardImportParser.ImportTable> tables = new ArrayList<>();
        int pageCount = document.getNumberOfPages();
        for (int page = 0; page < pageCount; page++) {
            int finalPage = page;
            List<LineGroup> pageGroups = groups.stream().filter(g -> g.pageIndex == finalPage).toList();
            if (pageGroups.isEmpty()) {
                continue;
            }
            tables.add(new ProcessCardImportParser.ImportTable(buildRows(pageGroups)));
        }
        return tables;
    }

    private static List<ProcessCardImportParser.ImportRow> buildRows(List<LineGroup> pageGroups) {
        List<ProcessCardImportParser.ImportRow> rows = new ArrayList<>();
        int pendingParamIndex = -1;
        List<String> pendingMoldValues = new ArrayList<>();

        for (LineGroup group : pageGroups) {
            List<String> cells = toImportCells(group);
            if (cells.stream().noneMatch(StringUtils::hasText)) {
                continue;
            }
            if (shouldSkipPdfBannerRow(cells)) {
                continue;
            }
            if (ProcessCardImportSupport.isFooterText(joinCells(cells)) || looksLikeSignatureFooterRow(cells)) {
                continue;
            }

            if (isParamValueOnlyRow(group)) {
                String value = toParamValue(group);
                if (pendingParamIndex >= 0) {
                    mergeParamValue(rows, pendingParamIndex, value);
                    continue;
                }
                if (ProcessCardImportSupport.MOLD_DRAWING_NO.matcher(value).find()) {
                    pendingMoldValues.add(value);
                    continue;
                }
            }

            String paramLabel = findParamLabelInCells(cells);
            if (StringUtils.hasText(paramLabel)) {
                if ("压模图号".equals(paramLabel) && !pendingMoldValues.isEmpty()) {
                    cells = new ArrayList<>(cells);
                    while (cells.size() < 2) {
                        cells.add("");
                    }
                    String merged = String.join("\n", pendingMoldValues);
                    if (StringUtils.hasText(cells.get(1))) {
                        merged = appendLine(merged, cells.get(1));
                    }
                    cells.set(1, merged);
                    pendingMoldValues.clear();
                }
                pendingParamIndex = rows.size();
                rows.add(new ProcessCardImportParser.ImportRow(cells, contentRaw(group)));
                continue;
            }

            pendingParamIndex = -1;
            rows.add(new ProcessCardImportParser.ImportRow(cells, contentRaw(group)));
        }
        return rows;
    }

    private static boolean looksLikeSignatureFooterRow(List<String> cells) {
        String joined = joinCells(cells);
        if (!StringUtils.hasText(joined)) {
            return false;
        }
        if (joined.matches(".*\\d{4}-\\d{2}-\\d{2}.*") && joined.length() < 120) {
            return true;
        }
        return joined.contains("杨琪") && joined.contains("吴刚") && joined.length() < 40;
    }

    private static boolean shouldSkipPdfBannerRow(List<String> cells) {
        if (!WordImportContentSanitizer.isPageBannerRow(cells)) {
            return false;
        }
        if (isStepHeaderRow(cells) || isBasicHeaderRow(cells) || isBasicDataRow(cells)) {
            return false;
        }
        if (StringUtils.hasText(findParamLabelInCells(cells))) {
            return false;
        }
        String joined = ProcessCardImportSupport.normalizeLabel(joinCells(cells));
        if (joined.contains("工艺卡片") && !joined.contains("共") && !joined.contains("第")) {
            return false;
        }
        return true;
    }

    private static boolean isStepHeaderRow(List<String> cells) {
        String joined = cells.stream()
                .map(ProcessCardImportSupport::normalizeLabel)
                .reduce("", String::concat);
        return joined.contains("工序号") && joined.contains("工序内容");
    }

    private static boolean isBasicHeaderRow(List<String> cells) {
        String joined = cells.stream()
                .map(ProcessCardImportSupport::normalizeLabel)
                .reduce("", String::concat);
        return joined.contains("产品号") && joined.contains("零组件号");
    }

    private static boolean isBasicDataRow(List<String> cells) {
        String joined = joinCells(cells);
        return joined.contains("Q/1D") && (joined.contains("通用") || joined.contains("专用"));
    }

    private static void mergeParamValue(List<ProcessCardImportParser.ImportRow> rows, int index, String value) {
        if (index < 0 || index >= rows.size() || !StringUtils.hasText(value)) {
            return;
        }
        ProcessCardImportParser.ImportRow row = rows.get(index);
        List<String> cells = new ArrayList<>(row.cells());
        if (cells.size() < 2) {
            while (cells.size() < 2) {
                cells.add("");
            }
        }
        cells.set(1, appendLine(cells.get(1), value));
        rows.set(index, new ProcessCardImportParser.ImportRow(cells, row.contentRaw()));
    }

    private static List<String> toImportCells(LineGroup group) {
        String joined = joinGroupText(group);
        String normalized = ProcessCardImportSupport.normalizeLabel(joined);

        if (normalized.contains("产品号") && normalized.contains("零组件号")) {
            return toBasicCells(group);
        }
        if (isBasicDataRow(group)) {
            return toBasicCells(group);
        }
        if (hasParamLabel(group)) {
            return toParamCells(group);
        }
        if (normalized.contains("工序号") && normalized.contains("工序内容")) {
            return toStepCells(group);
        }
        return toStepCells(group);
    }

    private static boolean isBasicDataRow(LineGroup group) {
        String text = joinGroupText(group);
        return text.contains("Q/1D") && (text.contains("通用") || text.contains("专用"));
    }

    private static boolean hasParamLabel(LineGroup group) {
        return StringUtils.hasText(findParamLabelInGroup(group));
    }

    private static boolean isParamValueOnlyRow(LineGroup group) {
        boolean hasLabel = false;
        boolean hasValue = false;
        for (PositionedChunk chunk : group.chunks) {
            if (chunk.x >= COL_PARAM_LABEL_MIN && chunk.x < COL_PARAM_VALUE_MIN) {
                hasLabel = true;
            }
            if (chunk.x >= COL_PARAM_VALUE_MIN) {
                hasValue = true;
            }
        }
        return !hasLabel && hasValue;
    }

    private static String toParamValue(LineGroup group) {
        StringBuilder value = new StringBuilder();
        for (PositionedChunk chunk : group.chunks) {
            if (chunk.x >= COL_PARAM_VALUE_MIN) {
                if (value.length() > 0) {
                    value.append(' ');
                }
                value.append(normalizeCell(chunk.text));
            }
        }
        return value.toString().trim();
    }

    private static String contentRaw(LineGroup group) {
        String left = leftColumnText(group);
        String middle = middleColumnText(group);
        if (!ProcessCardImportSupport.isStepNoCell(normalizeCell(left))) {
            return mergeLeftAndContentColumn(left, middle);
        }
        return middle;
    }

    private static String middleColumnText(LineGroup group) {
        StringBuilder raw = new StringBuilder();
        for (PositionedChunk chunk : group.chunks.stream().sorted(Comparator.comparingDouble(c -> c.x)).toList()) {
            if (chunk.x >= COL_STEP_NO_MAX && chunk.x < COL_CONTENT_MAX) {
                appendChunk(raw, chunk);
            }
        }
        return raw.toString().trim();
    }

    private static String mergeLeftAndContentColumn(String left, String middle) {
        left = left == null ? "" : left.trim();
        middle = middle == null ? "" : middle.trim();
        if (!StringUtils.hasText(left)) {
            return middle;
        }
        if (!StringUtils.hasText(middle)) {
            return left;
        }
        if (middle.startsWith(left) || left.startsWith(middle)) {
            return middle.length() >= left.length() ? middle : left;
        }
        if (middle.contains(left)) {
            return middle;
        }
        return left + " " + middle;
    }

    private static String leftColumnText(LineGroup group) {
        StringBuilder left = new StringBuilder();
        for (PositionedChunk chunk : group.chunks.stream().sorted(Comparator.comparingDouble(c -> c.x)).toList()) {
            if (chunk.x < COL_STEP_NO_MAX) {
                appendChunk(left, chunk);
            }
        }
        return left.toString().trim();
    }

    private static void appendChunk(StringBuilder raw, PositionedChunk chunk) {
        if (raw.length() > 0) {
            raw.append(' ');
        }
        raw.append(normalizeCell(chunk.text));
    }

    private static List<String> toStepCells(LineGroup group) {
        String[] cols = new String[4];
        for (PositionedChunk chunk : group.chunks) {
            int index = stepColumnIndex(chunk.x);
            cols[index] = append(cols[index], chunk.text);
        }
        List<String> cells = new ArrayList<>(4);
        for (String col : cols) {
            cells.add(normalizeCell(col));
        }
        return cells;
    }

    private static List<String> toBasicCells(LineGroup group) {
        String[] cols = new String[5];
        for (PositionedChunk chunk : group.chunks) {
            int index = basicColumnIndex(chunk.x);
            cols[index] = append(cols[index], chunk.text);
        }
        List<String> cells = new ArrayList<>(5);
        for (String col : cols) {
            cells.add(normalizeCell(col));
        }
        return cells;
    }

    private static List<String> toParamCells(LineGroup group) {
        String label = "";
        String value = "";
        for (PositionedChunk chunk : group.chunks) {
            if (chunk.x >= COL_PARAM_VALUE_MIN) {
                value = append(value, chunk.text);
            } else if (chunk.x >= COL_PARAM_LABEL_MIN) {
                label = append(label, chunk.text);
            }
        }
        label = normalizeCell(label);
        value = normalizeCell(value);
        String paramLabel = findParamLabel(label);
        if (StringUtils.hasText(paramLabel) && label.length() > paramLabel.length()) {
            value = append(label.substring(paramLabel.length()).trim(), value);
            label = paramLabel;
        } else if (StringUtils.hasText(paramLabel)) {
            label = paramLabel;
        }
        return List.of(label, value.trim());
    }

    private static String findParamLabelInCells(List<String> cells) {
        for (String cell : cells) {
            String label = findParamLabel(cell);
            if (StringUtils.hasText(label)) {
                return label;
            }
        }
        return "";
    }

    private static String findParamLabelInGroup(LineGroup group) {
        for (PositionedChunk chunk : group.chunks) {
            if (chunk.x >= COL_PARAM_LABEL_MIN && chunk.x < COL_PARAM_VALUE_MIN) {
                String label = findParamLabel(normalizeCell(chunk.text));
                if (StringUtils.hasText(label)) {
                    return label;
                }
            }
        }
        return "";
    }

    private static List<LineGroup> lineGroups(PDDocument document) throws IOException {
        List<PositionedChunk> chunks = readChunks(document);
        List<LineGroup> groups = new ArrayList<>();
        for (PositionedChunk chunk : chunks) {
            LineGroup group = null;
            for (LineGroup existing : groups) {
                if (existing.pageIndex == chunk.pageIndex
                        && Math.abs(existing.y - chunk.y) <= Y_TOLERANCE) {
                    group = existing;
                    break;
                }
            }
            if (group == null) {
                group = new LineGroup(chunk.pageIndex, chunk.y);
                groups.add(group);
            }
            group.chunks.add(chunk);
        }
        groups.sort(Comparator
                .comparingInt((LineGroup g) -> g.pageIndex)
                .thenComparingDouble(g -> g.y));
        return groups;
    }

    private static int stepColumnIndex(float x) {
        if (x < COL_STEP_NO_MAX) {
            return 0;
        }
        if (x < COL_CONTENT_MAX) {
            return 1;
        }
        if (x < COL_EQUIP_MAX) {
            return 2;
        }
        return 3;
    }

    private static int basicColumnIndex(float x) {
        if (x < COL_BASIC_PRODUCT_MAX) {
            return 0;
        }
        if (x < COL_BASIC_COMPONENT_MAX) {
            return 1;
        }
        if (x < COL_BASIC_NAME_MAX) {
            return 2;
        }
        if (x < COL_BASIC_MATERIAL_MAX) {
            return 3;
        }
        return 4;
    }

    private static List<PositionedChunk> readChunks(PDDocument document) throws IOException {
        PositionTextStripper stripper = new PositionTextStripper();
        stripper.setSortByPosition(true);
        stripper.getText(document);
        return stripper.chunks;
    }

    private static String findParamLabel(String line) {
        String normalized = ProcessCardImportSupport.normalizeLabel(line);
        for (String label : List.of("材料预热", "压机压力", "压模图号", "压模槽数", "毛料重量", "压制温度", "保持时间")) {
            if (normalized.startsWith(label) || normalized.contains(label)) {
                return label;
            }
        }
        return "";
    }

    private static String joinGroupText(LineGroup group) {
        return group.chunks.stream()
                .sorted(Comparator.comparingDouble(c -> c.x))
                .map(c -> c.text)
                .filter(StringUtils::hasText)
                .reduce("", (left, right) -> StringUtils.hasText(left) ? left + " " + right : right);
    }

    private static String joinCells(List<String> cells) {
        return String.join(" ", cells.stream().filter(StringUtils::hasText).toList());
    }

    private static String appendLine(String existing, String part) {
        if (!StringUtils.hasText(part)) {
            return existing == null ? "" : existing;
        }
        if (!StringUtils.hasText(existing)) {
            return part.trim();
        }
        return existing.trim() + "\n" + part.trim();
    }

    private static String append(String existing, String part) {
        if (!StringUtils.hasText(part)) {
            return existing == null ? "" : existing;
        }
        if (!StringUtils.hasText(existing)) {
            return part.trim();
        }
        return existing.trim() + " " + part.trim();
    }

    private static String normalizeCell(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String cleaned = WordImportContentSanitizer.stripLatexArtifacts(text.trim());
        cleaned = ProcessCardImportSupport.normalizePdfDiameterGlyph(cleaned);
        return OfficeMathTextExtractor.normalizeDisplayText(cleaned);
    }

    private static final class LineGroup {
        private final int pageIndex;
        private final float y;
        private final List<PositionedChunk> chunks = new ArrayList<>();

        private LineGroup(int pageIndex, float y) {
            this.pageIndex = pageIndex;
            this.y = y;
        }
    }

    private static final class PositionedChunk {
        private final int pageIndex;
        private final float x;
        private final float y;
        private final String text;

        private PositionedChunk(int pageIndex, float x, float y, String text) {
            this.pageIndex = pageIndex;
            this.x = x;
            this.y = y;
            this.text = text;
        }
    }

    private static final class PositionTextStripper extends PDFTextStripper {

        private final List<PositionedChunk> chunks = new ArrayList<>();
        private int pageIndex;

        private PositionTextStripper() throws IOException {
            super();
        }

        @Override
        protected void startPage(org.apache.pdfbox.pdmodel.PDPage page) throws IOException {
            pageIndex = getCurrentPageNo() - 1;
            super.startPage(page);
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            if (!StringUtils.hasText(text) || textPositions == null || textPositions.isEmpty()) {
                return;
            }
            float x = textPositions.get(0).getXDirAdj();
            float y = textPositions.get(0).getYDirAdj();
            String normalized = text.replace('\u00a0', ' ').trim();
            if (StringUtils.hasText(normalized)) {
                chunks.add(new PositionedChunk(pageIndex, x, y, normalized));
            }
        }
    }
}
