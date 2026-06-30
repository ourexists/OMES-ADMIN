package com.ourexists.omes.process.util;

import com.ourexists.omes.process.model.ProcessImportParseResult;
import com.ourexists.omes.process.model.ProcessStepItem;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 《橡胶、塑料制件工艺卡片》导入共用解析（Word 为准；PDF 先还原为相同表格结构）。
 */
public final class ProcessCardImportParser {

    private ProcessCardImportParser() {
    }

    public record ImportRow(List<String> cells, String contentRaw) {
        public ImportRow {
            cells = cells == null ? List.of() : List.copyOf(cells);
            contentRaw = contentRaw == null ? "" : contentRaw;
        }
    }

    public record ImportTable(List<ImportRow> rows) {
        public ImportTable {
            rows = rows == null ? List.of() : List.copyOf(rows);
        }
    }

    @FunctionalInterface
    public interface ImportImageExtractor {
        void extract(ProcessImportParseResult result) throws Exception;
    }

    public static ProcessImportParseResult parse(List<ImportTable> tables, ImportImageExtractor imageExtractor)
            throws Exception {
        ProcessImportParseResult result = new ProcessImportParseResult();
        if (tables == null || tables.isEmpty()) {
            return result;
        }

        ProcessCardImportSupport.ParamAccumulator params = new ProcessCardImportSupport.ParamAccumulator();
        parseFirstPage(tables.get(0), result, params);

        List<ProcessStepItem> steps = new ArrayList<>();
        for (ImportTable table : tables) {
            parseStepsFromTable(table, steps);
        }
        result.setSteps(steps);
        ProcessCardStepTableParser.sanitizeImportedSteps(steps);

        if (imageExtractor != null) {
            imageExtractor.extract(result);
        }

        ProcessCardStepTableParser.enrichSecondStageParams(steps);
        ProcessCardImportSupport.finalizeMolds(result, params);
        return result;
    }

    private static void parseFirstPage(ImportTable table, ProcessImportParseResult result,
                                       ProcessCardImportSupport.ParamAccumulator params) {
        List<ImportRow> rows = table.rows();
        parseCardTitle(rows, result);

        int scanLimit = Math.min(rows.size(), 14);
        for (int r = 0; r < scanLimit; r++) {
            for (String cell : rows.get(r).cells()) {
                String code = extractProcessCode(cell);
                if (!StringUtils.hasText(result.getProcessCode()) && StringUtils.hasText(code)) {
                    result.setProcessCode(code);
                }
            }
        }

        for (int r = 0; r < scanLimit - 1; r++) {
            if (isBasicHeaderRow(rows.get(r).cells())) {
                applyBasicInfo(result, rows.get(r).cells(), rows.get(r + 1).cells());
                break;
            }
        }

        for (int r = 0; r < scanLimit; r++) {
            applyParamRow(rows.get(r), result, params);
        }
    }

    private static boolean isBasicHeaderRow(List<String> cells) {
        String joined = cells.stream()
                .map(ProcessCardImportSupport::normalizeLabel)
                .reduce("", String::concat);
        return joined.contains("产品号") && joined.contains("零组件号");
    }

    private static void parseCardTitle(List<ImportRow> rows, ProcessImportParseResult result) {
        if (rows.size() > ProcessCardTemplateLayout.TITLE_ROW) {
            List<String> titleRow = rows.get(ProcessCardTemplateLayout.TITLE_ROW).cells();
            if (titleRow.size() > ProcessCardTemplateLayout.WORD_TITLE_COL) {
                String title = ProcessCardTemplateLayout.normalizeTitleCell(
                        titleRow.get(ProcessCardTemplateLayout.WORD_TITLE_COL));
                if (StringUtils.hasText(title) && title.contains("工艺卡片")) {
                    result.setProcessName(title);
                    return;
                }
            }
        }
        for (ImportRow row : rows) {
            for (String cell : row.cells()) {
                String normalized = ProcessCardImportSupport.normalizeLabel(cell);
                if (normalized.contains("工艺卡片")) {
                    result.setProcessName(ProcessCardTemplateLayout.normalizeTitleCell(cell));
                    return;
                }
            }
        }
    }

    private static void applyBasicInfo(ProcessImportParseResult result, List<String> header, List<String> data) {
        for (int i = 0; i < header.size() && i < data.size(); i++) {
            String label = ProcessCardImportSupport.normalizeLabel(header.get(i));
            String value = data.get(i).trim();
            if (!StringUtils.hasText(value)) {
                continue;
            }
            switch (label) {
                case "产品号" -> result.setProductCode(value);
                case "零组件号" -> result.setComponentCode(value);
                case "零组件名称" -> result.setComponentName(value);
                case "材料" -> result.setMaterialCode(value);
                case "技术条件" -> result.setTechCondition(value);
                default -> {
                }
            }
        }
    }

    private static void applyParamRow(ImportRow row, ProcessImportParseResult result,
                                      ProcessCardImportSupport.ParamAccumulator params) {
        List<String> cells = row.cells();
        if (cells.isEmpty()) {
            return;
        }
        String label = "";
        String value = "";
        for (int i = 0; i < cells.size(); i++) {
            String normalized = ProcessCardImportSupport.normalizeLabel(cells.get(i));
            if (ProcessCardImportSupport.isParamLabel(normalized)) {
                label = normalized;
                if (i + 1 < cells.size()) {
                    value = cells.get(i + 1).trim();
                }
                break;
            }
        }
        if (!StringUtils.hasText(label)) {
            return;
        }
        if ("压模槽数".equals(label) || "毛料重量".equals(label)) {
            value = normalizeParamFormulaValue(label, value);
        }
        ProcessCardImportSupport.applyParam(result, params, label, value);
    }

    private static String normalizeParamFormulaValue(String label, String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (looksLikeImportedFormula(value)) {
            return OfficeMathTextExtractor.normalizeDisplayText(JLaTeXMathFormulaSupport.plainTextViaLatex(value));
        }
        return OfficeMathTextExtractor.normalizeDisplayText(value);
    }

    private static boolean looksLikeImportedFormula(String text) {
        return text.contains("℃") || text.contains("±") || text.contains("→")
                || text.contains("┴") || text.contains("φ") || text.contains("Φ")
                || text.contains("×") || text.contains("恒温");
    }

    private static void parseStepsFromTable(ImportTable table, List<ProcessStepItem> steps) {
        StepColumns columns = detectStepColumns(table.rows());
        if (columns.contentCol < 0) {
            columns.stepNoCol = 0;
            columns.contentCol = 1;
            columns.equipCol = 2;
            columns.toolingCol = 3;
        }

        List<ProcessCardTableRow> rows = new ArrayList<>();
        for (ImportRow row : table.rows()) {
            List<String> cells = row.cells();
            String stepNo = columnAt(cells, columns.stepNoCol);
            String contentCol = columnAt(cells, columns.contentCol);
            String content;
            if (StringUtils.hasText(row.contentRaw())) {
                content = row.contentRaw();
                if (StringUtils.hasText(stepNo) && !ProcessCardImportSupport.isStepNoCell(stepNo)) {
                    stepNo = "";
                }
            } else {
                content = ProcessCardImportSupport.mergeMisplacedStepColumn(stepNo, contentCol);
                if (StringUtils.hasText(stepNo) && !ProcessCardImportSupport.isStepNoCell(stepNo)) {
                    stepNo = "";
                }
            }
            rows.add(new ProcessCardTableRow(
                    stepNo,
                    content,
                    columnAt(cells, columns.equipCol),
                    columnAt(cells, columns.toolingCol)));
        }
        ProcessCardStepTableParser.parse(rows, steps, true);
    }

    private static String extractProcessCode(String text) {
        return ProcessCardImportSupport.extractStandaloneProcessCode(text);
    }

    static StepColumns detectStepColumns(List<ImportRow> rows) {
        StepColumns columns = new StepColumns();
        for (ImportRow row : rows) {
            List<String> cells = row.cells();
            for (int c = 0; c < cells.size(); c++) {
                String text = ProcessCardImportSupport.normalizeLabel(cells.get(c));
                if (text.contains("工序号")) {
                    columns.stepNoCol = c;
                } else if (text.contains("工序内容")) {
                    columns.contentCol = c;
                } else if ("设备".equals(text)) {
                    columns.equipCol = c;
                } else if (text.contains("工艺装备") || text.contains("工装")) {
                    columns.toolingCol = c;
                }
            }
            if (columns.contentCol >= 0) {
                return columns;
            }
        }
        return columns;
    }

    static ImportTable fromWordTable(XWPFTable table) {
        StepColumns columns = detectStepColumnsFromWord(table);
        List<ImportRow> rows = new ArrayList<>();
        List<XWPFTableRow> wordRows = table.getRows();
        for (int i = 0; i < wordRows.size(); i++) {
            XWPFTableRow wordRow = wordRows.get(i);
            List<String> cells = readWordRowTexts(wordRow);
            if (i >= 5 && i < 14) {
                cells = enrichWordParamCells(wordRow, cells);
            }
            String raw = columns.contentCol >= 0 && columns.contentCol < wordRow.getTableCells().size()
                    ? readWordCellTextRaw(wordRow.getCell(columns.contentCol))
                    : "";
            rows.add(new ImportRow(cells, raw));
        }
        return new ImportTable(rows);
    }

    private static List<String> enrichWordParamCells(XWPFTableRow wordRow, List<String> cells) {
        List<XWPFTableCell> tableCells = wordRow.getTableCells();
        List<String> enriched = new ArrayList<>(cells);
        for (int i = 0; i < enriched.size(); i++) {
            String normalized = ProcessCardImportSupport.normalizeLabel(enriched.get(i));
            if (ProcessCardImportSupport.isParamLabel(normalized) && i + 1 < tableCells.size()) {
                enriched.set(i + 1, readWordParamValueCell(tableCells.get(i + 1), normalized));
                break;
            }
        }
        return enriched;
    }

    private static StepColumns detectStepColumnsFromWord(XWPFTable table) {
        List<ImportRow> rows = new ArrayList<>();
        for (XWPFTableRow row : table.getRows()) {
            rows.add(new ImportRow(readWordRowTexts(row), ""));
        }
        return detectStepColumns(rows);
    }

    private static List<String> readWordRowTexts(XWPFTableRow row) {
        List<String> cells = new ArrayList<>();
        for (XWPFTableCell cell : row.getTableCells()) {
            cells.add(readWordCellText(cell));
        }
        return cells;
    }

    private static String readWordCellText(XWPFTableCell cell) {
        String display = OfficeMathTextExtractor.extractWordCellDisplayText(cell.getCTTc().xmlText());
        if (StringUtils.hasText(display)) {
            return display.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        }
        return fallbackWordCellText(cell, false);
    }

    private static String readWordCellTextRaw(XWPFTableCell cell) {
        if (cell == null) {
            return "";
        }
        String display = OfficeMathTextExtractor.extractWordCellDisplayText(cell.getCTTc().xmlText());
        if (StringUtils.hasText(display)) {
            return display;
        }
        return fallbackWordCellText(cell, true);
    }

    private static String readWordParamValueCell(XWPFTableCell cell, String label) {
        if ("压模槽数".equals(label) || "毛料重量".equals(label)) {
            List<String> parts = OfficeMathTextExtractor.extractMathRunParts(cell.getCTTc().xmlText());
            if (!parts.isEmpty()) {
                if ("压模槽数".equals(label)) {
                    return ProcessCardImportSupport.formatSlotCountFromMathParts(parts);
                }
                return JLaTeXMathFormulaSupport.plainTextViaLatex(String.join("", parts));
            }
        }
        return readWordCellTextRaw(cell);
    }

    private static String fallbackWordCellText(XWPFTableCell cell, boolean keepNewlines) {
        String plain = cell.getText();
        if (keepNewlines) {
            plain = plain.replaceAll("\\r\\n?", "\n").trim();
        } else if (StringUtils.hasText(plain)) {
            plain = plain.replace('\u0007', ' ').replaceAll("\\s+", " ").trim();
        }
        String math = OfficeMathTextExtractor.extractFromXmlFragment(cell.getCTTc().xmlText());
        if (!StringUtils.hasText(plain)) {
            return math;
        }
        if (!StringUtils.hasText(math) || plain.contains(math)) {
            return plain;
        }
        return plain + math;
    }

    private static String columnAt(List<String> cells, int index) {
        if (index < 0 || index >= cells.size()) {
            return "";
        }
        return cells.get(index) == null ? "" : cells.get(index).trim();
    }

    static final class StepColumns {
        private int stepNoCol = -1;
        private int contentCol = -1;
        private int equipCol = -1;
        private int toolingCol = -1;
    }
}
