package com.ourexists.omes.process.util;

import com.ourexists.omes.process.model.ProcessStepItem;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 工艺卡片工序表解析（Word / PDF 共用，与 {@link ProcessWordParser} 列语义一致）。
 */
public final class ProcessCardStepTableParser {

    private ProcessCardStepTableParser() {
    }

    public static void parse(List<ProcessCardTableRow> rows, List<ProcessStepItem> steps, boolean breakOnFooter) {
        boolean seenStepHeader = false;

        for (ProcessCardTableRow row : rows) {
            List<String> cells = row.asCells();
            if (row.isBlank()) {
                continue;
            }
            if (isStepHeaderRow(cells)) {
                seenStepHeader = true;
                continue;
            }

            String stepNoText = columnAt(cells, 0);
            String content = columnAt(cells, 1);
            String equipment = WordImportContentSanitizer.sanitizeFieldValue(columnAt(cells, 2));
            String tooling = WordImportContentSanitizer.sanitizeFieldValue(columnAt(cells, 3));

            if (!seenStepHeader) {
                if (WordImportContentSanitizer.isPageBannerRow(cells)) {
                    continue;
                }
                if (!(StringUtils.hasText(stepNoText) && ProcessCardImportSupport.isStepNoCell(stepNoText))) {
                    if (!steps.isEmpty() && hasStepContinuationText(stepNoText, content, equipment, tooling)) {
                        ProcessStepItem current = steps.get(steps.size() - 1);
                        ProcessCardImportSupport.appendStepContent(current, content);
                        tryApplyRoomTempCureParams(current, content);
                        if (StringUtils.hasText(equipment) || StringUtils.hasText(tooling)) {
                            ProcessCardImportSupport.mergeStepEquipAndTooling(current, equipment, tooling);
                        }
                    }
                    continue;
                }
                seenStepHeader = true;
            }

            if (isFooterRow(cells)) {
                if (breakOnFooter) {
                    break;
                }
                continue;
            }

            if (!StringUtils.hasText(content) && !StringUtils.hasText(stepNoText)
                    && !StringUtils.hasText(equipment) && !StringUtils.hasText(tooling)) {
                continue;
            }

            if (StringUtils.hasText(stepNoText) && ProcessCardImportSupport.isStepNoCell(stepNoText)) {
                ProcessStepItem step = new ProcessStepItem();
                step.setStepNo(ProcessCardImportSupport.parseStepNoCell(stepNoText));
                String stepName = ProcessCardImportSupport.extractStepName(content);
                step.setStepName(stepName);
                step.setStepContent(ProcessCardImportSupport.extractStepContent(content, stepName));
                ProcessCardImportSupport.applyStepEquipAndTooling(step, equipment, tooling);
                steps.add(step);
                tryApplyRoomTempCureParams(step, content);
                continue;
            }

            if (steps.isEmpty()) {
                continue;
            }
            ProcessStepItem current = steps.get(steps.size() - 1);
            ProcessCardImportSupport.appendStepContent(current, content);
            tryApplyRoomTempCureParams(current, content);
            ProcessCardImportSupport.mergeStepEquipAndTooling(current, equipment, tooling);
        }
    }

    public static void sanitizeImportedSteps(List<ProcessStepItem> steps) {
        for (ProcessStepItem step : steps) {
            step.setStepContent(WordImportContentSanitizer.sanitizeStepContent(step.getStepContent()));
        }
    }

    public static void enrichSecondStageParams(List<ProcessStepItem> steps) {
        for (ProcessStepItem step : steps) {
            if (!RoomTempCureCurveTextParser.isRoomTempCureStep(step.getStepName())
                    || StringUtils.hasText(step.getParams())) {
                continue;
            }
            String blob = step.getStepContent();
            RoomTempCureCurveTextParser.tryParseToParamsJson(blob).ifPresent(json ->
                    RoomTempCureCurveTextParser.applyParsedParams(step, json, blob));
        }
    }

    private static void tryApplyRoomTempCureParams(ProcessStepItem step, String extraText) {
        if (!RoomTempCureCurveTextParser.isRoomTempCureStep(step.getStepName())) {
            return;
        }
        if (StringUtils.hasText(step.getParams())) {
            return;
        }
        String blob = RoomTempCureCurveTextParser.mergeParseBlob(step.getStepContent(), extraText);
        RoomTempCureCurveTextParser.tryParseToParamsJson(blob).ifPresent(json ->
                RoomTempCureCurveTextParser.applyParsedParams(step, json, blob));
    }

    private static boolean hasStepContinuationText(String stepNoText, String content,
                                                   String equipment, String tooling) {
        if (StringUtils.hasText(content)
                || (StringUtils.hasText(stepNoText) && !ProcessCardImportSupport.isStepNoCell(stepNoText))) {
            return true;
        }
        for (String name : ProcessCardImportSupport.splitEquipToolingNames(
                joinNonBlank(equipment, tooling))) {
            if (!ProcessCardImportSupport.isIgnorableImportRefName(name)) {
                return true;
            }
        }
        return false;
    }

    private static String joinNonBlank(String left, String right) {
        if (!StringUtils.hasText(left)) {
            return right == null ? "" : right;
        }
        if (!StringUtils.hasText(right)) {
            return left;
        }
        return left + " " + right;
    }

    private static boolean isStepHeaderRow(List<String> cells) {
        String joined = cells.stream()
                .map(ProcessCardImportSupport::normalizeLabel)
                .reduce("", String::concat);
        if (joined.contains("工序号") && joined.contains("工序内容")) {
            return true;
        }
        for (String cell : cells) {
            String text = ProcessCardImportSupport.normalizeLabel(cell);
            if (text.contains("工序号") && text.contains("工序内容")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFooterRow(List<String> cells) {
        for (String cell : cells) {
            if (ProcessCardImportSupport.isFooterText(cell)) {
                return true;
            }
        }
        return false;
    }

    private static String columnAt(List<String> cells, int index) {
        if (index < 0 || index >= cells.size()) {
            return "";
        }
        return cells.get(index) == null ? "" : cells.get(index).trim();
    }
}
