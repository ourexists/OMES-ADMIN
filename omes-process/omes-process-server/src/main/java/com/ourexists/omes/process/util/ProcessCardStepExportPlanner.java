package com.ourexists.omes.process.util;

import com.ourexists.omes.process.model.ProcessStepItem;
import com.ourexists.omes.process.model.ProcessVO;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** 工艺卡片导出工序行规划（Word / PDF 共用）。 */
public final class ProcessCardStepExportPlanner {

    public static final int FIRST_PAGE_STEP_CAPACITY = 17;
    public static final int CONT_PAGE_STEP_CAPACITY = 27;

    private ProcessCardStepExportPlanner() {
    }

    public static void validateExportLineCount(int lineCount) {
        int maxLines = maxExportLines(ProcessWordExporter.MAX_EXPORT_PAGES);
        if (lineCount > maxLines) {
            throw new IllegalStateException(
                    "工序导出行数 " + lineCount + " 超过上限 " + maxLines + "，请精简工序描述");
        }
    }

    public static int maxExportLines(int maxPages) {
        if (maxPages <= 1) {
            return FIRST_PAGE_STEP_CAPACITY;
        }
        return FIRST_PAGE_STEP_CAPACITY + CONT_PAGE_STEP_CAPACITY * (maxPages - 1);
    }

    public static List<StepLine> buildStepLines(ProcessVO process) {
        List<StepRowPlan> plans = buildStepPlans(process.getSteps());
        List<StepLine> lines = new ArrayList<>();
        for (int planIdx = 0; planIdx < plans.size(); planIdx++) {
            StepRowPlan plan = plans.get(planIdx);
            boolean firstLine = true;
            int lineIdx = 0;
            for (String content : plan.contentLines) {
                StepLine line = new StepLine();
                line.firstLineOfStep = firstLine;
                line.stepNameLine = plan.hasStepName && lineIdx == 0;
                if (firstLine) {
                    line.stepNo = plan.stepNo;
                    line.equipment = plan.equipment;
                    line.tooling = plan.tooling;
                    firstLine = false;
                }
                line.content = content;
                lines.add(line);
                lineIdx++;
            }
            if (planIdx < plans.size() - 1) {
                lines.add(new StepLine());
            }
        }
        return lines;
    }

    public static List<List<StepLine>> paginateStepLines(List<StepLine> lines) {
        int totalPages = calcTotalPages(lines.size());
        List<List<StepLine>> pages = new ArrayList<>(totalPages);
        int offset = 0;
        for (int p = 0; p < totalPages; p++) {
            int capacity = p == 0 ? FIRST_PAGE_STEP_CAPACITY : CONT_PAGE_STEP_CAPACITY;
            int end = Math.min(offset + capacity, lines.size());
            List<StepLine> pageLines = new ArrayList<>(lines.subList(offset, end));
            if (p > 0) {
                trimLeadingBlankStepLines(pageLines);
            }
            pages.add(pageLines);
            offset = end;
        }
        if (pages.isEmpty()) {
            pages.add(List.of());
        }
        return pages;
    }

    private static void trimLeadingBlankStepLines(List<StepLine> pageLines) {
        while (!pageLines.isEmpty() && isBlankStepLine(pageLines.get(0))) {
            pageLines.remove(0);
        }
    }

    private static boolean isBlankStepLine(StepLine line) {
        return !line.firstLineOfStep && !StringUtils.hasText(line.content);
    }

    private static int calcTotalPages(int lineCount) {
        if (lineCount <= FIRST_PAGE_STEP_CAPACITY) {
            return 1;
        }
        int remaining = lineCount - FIRST_PAGE_STEP_CAPACITY;
        return 1 + (remaining + CONT_PAGE_STEP_CAPACITY - 1) / CONT_PAGE_STEP_CAPACITY;
    }

    private static List<StepRowPlan> buildStepPlans(List<ProcessStepItem> steps) {
        List<StepRowPlan> plans = new ArrayList<>();
        if (CollectionUtils.isEmpty(steps)) {
            return plans;
        }
        for (int i = 0; i < steps.size(); i++) {
            ProcessStepItem step = steps.get(i);
            StepRowPlan plan = new StepRowPlan();
            plan.stepNo = ProcessCardFormatUtil.resolveStepNo(step, i);
            plan.contentLines = ProcessCardFormatUtil.splitStepContentLinesForExport(step);
            if (plan.contentLines.isEmpty()) {
                plan.contentLines = List.of("");
            }
            plan.hasStepName = StringUtils.hasText(step.getStepName());
            plan.equipment = ProcessCardFormatUtil.formatEquipments(step);
            plan.tooling = ProcessCardFormatUtil.formatToolings(step);
            plans.add(plan);
        }
        return plans;
    }

    public static final class StepLine {
        public boolean firstLineOfStep;
        public boolean stepNameLine;
        public int stepNo;
        public String content = "";
        public String equipment = "";
        public String tooling = "";
    }

    private static final class StepRowPlan {
        private int stepNo;
        private boolean hasStepName;
        private List<String> contentLines = List.of();
        private String equipment = "";
        private String tooling = "";
    }
}
