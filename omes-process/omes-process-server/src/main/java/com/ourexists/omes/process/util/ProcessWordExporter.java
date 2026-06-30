package com.ourexists.omes.process.util;

import com.ourexists.omes.process.model.ProcessVO;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 按《橡胶、塑料制件工艺卡片》Word 模板导出 .docx（与导入模板一致，可再导出 PDF）。
 */
public final class ProcessWordExporter {

    private static final String TEMPLATE_PATH = "templates/process-card-template.docx";

    private static final int ROW_TITLE = 1;
    private static final int COL_TITLE = 1;
    private static final int ROW_PAGE = 2;
    private static final int COL_PROCESS_CODE = 2;
    private static final int COL_PAGE_NO = 3;

    private static final int ROW_BASIC = 4;
    private static final int ROW_PARAM_START = 5;
    private static final int COL_PARAM_LABEL = 1;
    private static final int COL_PARAM_VALUE = 2;

    private static final int ROW_STEP_HEADER_FIRST = 15;
    private static final int ROW_STEP_FIRST_FIRST = 16;
    private static final int ROW_STEP_LAST = 32;

    private static final int ROW_STEP_HEADER_CONT = 5;
    private static final int ROW_STEP_FIRST_CONT = 6;

    private static final int CONTINUATION_TEMPLATE_TABLE_INDEX = 1;

    /** 工艺卡片导出页数上限（含首页） */
    public static final int MAX_EXPORT_PAGES = 10;

    private static volatile byte[] continuationTableXmlBytes;

    /** 模板正文字号：Word w:sz=16（8pt） */
    private static final int TEMPLATE_FONT_SIZE_HALF_POINTS = 16;

    private static final int TEMPLATE_FONT_SIZE_POINTS = TEMPLATE_FONT_SIZE_HALF_POINTS / 2;

    private static final String TEMPLATE_FONT_FAMILY = "宋体";

    private static final StepColumnLayout FIRST_PAGE_COLUMNS =
            new StepColumnLayout(1, 2, 3, 4);
    private static final StepColumnLayout CONT_PAGE_COLUMNS =
            new StepColumnLayout(0, 1, 2, 3);

    private static final ThreadLocal<CTRPr> STEP_CONTENT_RUN_STYLE = new ThreadLocal<>();

    private ProcessWordExporter() {
    }

    public static byte[] export(ProcessVO process, byte[] processImageBytes) throws Exception {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
        if (!resource.exists()) {
            throw new IllegalStateException("工艺卡片 Word 模板不存在: " + TEMPLATE_PATH);
        }
        try (InputStream in = resource.getInputStream();
             XWPFDocument document = new XWPFDocument(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            List<ProcessCardStepExportPlanner.StepLine> allLines =
                    ProcessCardStepExportPlanner.buildStepLines(process);
            ProcessCardStepExportPlanner.validateExportLineCount(allLines.size());
            List<List<ProcessCardStepExportPlanner.StepLine>> pages =
                    ProcessCardStepExportPlanner.paginateStepLines(allLines);
            int totalPages = pages.size();
            if (totalPages > MAX_EXPORT_PAGES) {
                throw new IllegalStateException(
                        "工序内容超过 " + MAX_EXPORT_PAGES + " 页，请精简工序描述后导出");
            }

            trimExtraTables(document, totalPages);
            ensureTableCount(document, totalPages);

            try {
                loadStepContentRunStyle(document);
                fillFirstPage(tableAt(document, 0), process, pages.get(0), 1, totalPages, processImageBytes);
                for (int pageIndex = 1; pageIndex < totalPages; pageIndex++) {
                    fillContinuationPage(tableAt(document, pageIndex), process,
                            pages.get(pageIndex), pageIndex + 1, totalPages);
                }
            } finally {
                STEP_CONTENT_RUN_STYLE.remove();
            }

            document.write(out);
            return out.toByteArray();
        }
    }

    private static void ensureTableCount(XWPFDocument document, int totalPages) throws Exception {
        int guard = 0;
        while (physicalTableCount(document) < totalPages) {
            int before = physicalTableCount(document);
            appendContinuationTable(document);
            if (++guard > MAX_EXPORT_PAGES || physicalTableCount(document) <= before) {
                throw new IllegalStateException("续页表生成失败");
            }
        }
    }

    private static void trimExtraTables(XWPFDocument document, int totalPages) {
        while (physicalTableCount(document) > totalPages) {
            document.getDocument().getBody().removeTbl(physicalTableCount(document) - 1);
        }
    }

    private static void appendContinuationTable(XWPFDocument document) throws Exception {
        XWPFParagraph pageBreakParagraph = document.createParagraph();
        pageBreakParagraph.createRun().addBreak(BreakType.PAGE);
        CTTbl parsed = CTTbl.Factory.parse(new ByteArrayInputStream(loadContinuationTableXmlBytes()));
        document.getDocument().getBody().addNewTbl().set(parsed);
    }

    private static int physicalTableCount(XWPFDocument document) {
        return document.getDocument().getBody().sizeOfTblArray();
    }

    /** 始终从底层 CTTbl 构造，避免 insertNewTbl / set(parsed) 后 getTables() 行缓存过期。 */
    private static XWPFTable tableAt(XWPFDocument document, int index) {
        if (index < 0 || index >= physicalTableCount(document)) {
            throw new IllegalStateException("页表不存在: index=" + index);
        }
        return new XWPFTable(document.getDocument().getBody().getTblArray(index), document);
    }

    private static byte[] loadContinuationTableXmlBytes() throws Exception {
        if (continuationTableXmlBytes != null) {
            return continuationTableXmlBytes;
        }
        synchronized (ProcessWordExporter.class) {
            if (continuationTableXmlBytes != null) {
                return continuationTableXmlBytes;
            }
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
            try (InputStream in = resource.getInputStream();
                 XWPFDocument template = new XWPFDocument(in)) {
                if (template.getTables().size() <= CONTINUATION_TEMPLATE_TABLE_INDEX) {
                    throw new IllegalStateException("工艺卡片模板缺少续页表");
                }
                String xml = template.getTables().get(CONTINUATION_TEMPLATE_TABLE_INDEX).getCTTbl().xmlText();
                continuationTableXmlBytes = xml.getBytes(StandardCharsets.UTF_8);
            }
            return continuationTableXmlBytes;
        }
    }

    private static void fillFirstPage(XWPFTable table, ProcessVO process,
                                      List<ProcessCardStepExportPlanner.StepLine> lines,
                                      int pageNo, int totalPages, byte[] processImageBytes) {
        setCellText(table, ROW_TITLE, COL_TITLE, process.getProcessName());
        setCellText(table, ROW_PAGE, COL_PROCESS_CODE, process.getProcessCode());
        updatePageInfo(table, pageNo, totalPages, 4);

        setCellText(table, ROW_BASIC, 0, firstNonBlank(process.getProductCode(), process.getProductName()));
        setCellText(table, ROW_BASIC, 1, process.getComponentCode());
        setCellText(table, ROW_BASIC, 2, process.getComponentName());
        setCellText(table, ROW_BASIC, 3, ProcessCardFormatUtil.formatMaterial(process));
        setCellText(table, ROW_BASIC, 4, process.getTechCondition());

        fillParameters(table);
        fillParamValues(table, process);
        replaceProcessImage(table, processImageBytes);

        fillStepArea(table, FIRST_PAGE_COLUMNS, ROW_STEP_FIRST_FIRST, ROW_STEP_LAST, lines);
    }

    private static void fillContinuationPage(XWPFTable table, ProcessVO process,
                                           List<ProcessCardStepExportPlanner.StepLine> lines,
                                           int pageNo, int totalPages) {
        setCellText(table, ROW_TITLE, COL_TITLE, process.getProcessName());
        setCellText(table, ROW_PAGE, COL_PROCESS_CODE, process.getProcessCode());
        updatePageInfo(table, pageNo, totalPages, 4);

        setCellText(table, ROW_BASIC, 0, firstNonBlank(process.getProductCode(), process.getProductName()));
        setCellText(table, ROW_BASIC, 1, process.getComponentCode());
        setCellText(table, ROW_BASIC, 2, process.getComponentName());
        setCellText(table, ROW_BASIC, 3, ProcessCardFormatUtil.formatMaterial(process));

        fillStepArea(table, CONT_PAGE_COLUMNS, ROW_STEP_FIRST_CONT, ROW_STEP_LAST, lines);
    }

    private static void fillParameters(XWPFTable table) {
        setCellText(table, ROW_PARAM_START, COL_PARAM_LABEL, "材料预热");
        setCellText(table, ROW_PARAM_START + 1, COL_PARAM_LABEL, "压机压力");
        setCellText(table, ROW_PARAM_START + 2, COL_PARAM_LABEL, "压模图号");
        setCellText(table, ROW_PARAM_START + 3, COL_PARAM_LABEL, "压模槽数");
        setCellText(table, ROW_PARAM_START + 4, COL_PARAM_LABEL, "毛料重量");
        setCellText(table, ROW_PARAM_START + 5, COL_PARAM_LABEL, "压制温度");
        setCellText(table, ROW_PARAM_START + 6, COL_PARAM_LABEL, "保持时间");
    }

    private static void fillParamValues(XWPFTable table, ProcessVO process) {
        setCellText(table, ROW_PARAM_START, COL_PARAM_VALUE, process.getMaterialPreheat());
        setCellText(table, ROW_PARAM_START + 1, COL_PARAM_VALUE,
                ProcessCardFormatUtil.formatPressPressure(process.getPressPressure()));
        setCellText(table, ROW_PARAM_START + 2, COL_PARAM_VALUE,
                ProcessCardFormatUtil.formatMoldDrawingNos(process.getMolds()));
        setCellText(table, ROW_PARAM_START + 3, COL_PARAM_VALUE,
                ProcessCardFormatUtil.formatSlotCounts(process.getMolds()));
        setCellText(table, ROW_PARAM_START + 4, COL_PARAM_VALUE,
                ProcessCardFormatUtil.formatBlankWeight(
                        process.getBlankWeight(),
                        process.getBlankWeightUpperOffset(),
                        process.getBlankWeightLowerOffset()));
        setCellText(table, ROW_PARAM_START + 5, COL_PARAM_VALUE,
                ProcessCardFormatUtil.formatPressTemperature(
                        process.getPressTemperature(),
                        process.getPressTemperatureUpperOffset(),
                        process.getPressTemperatureLowerOffset()));
        setCellText(table, ROW_PARAM_START + 6, COL_PARAM_VALUE,
                ProcessCardFormatUtil.formatHoldTime(process.getHoldTimeSeconds()));
    }

    private static void fillStepArea(XWPFTable table, StepColumnLayout layout,
                                     int firstRow, int lastRow,
                                     List<ProcessCardStepExportPlanner.StepLine> lines) {
        int rowIndex = firstRow;
        for (ProcessCardStepExportPlanner.StepLine line : lines) {
            if (line.firstLineOfStep) {
                setCellText(table, rowIndex, layout.stepNoCol, String.valueOf(line.stepNo),
                        ParagraphAlignment.CENTER, XWPFTableCell.XWPFVertAlign.CENTER);
                setCellText(table, rowIndex, layout.equipCol, line.equipment,
                        ParagraphAlignment.CENTER, XWPFTableCell.XWPFVertAlign.CENTER);
                if (layout.toolingCol >= 0) {
                    setCellText(table, rowIndex, layout.toolingCol, line.tooling,
                            ParagraphAlignment.LEFT, XWPFTableCell.XWPFVertAlign.CENTER);
                }
            } else {
                clearCellText(table, rowIndex, layout.stepNoCol);
                clearCellText(table, rowIndex, layout.equipCol);
                if (layout.toolingCol >= 0) {
                    clearCellText(table, rowIndex, layout.toolingCol);
                }
            }
            setStepContentCell(table, rowIndex, layout.contentCol, line);
            rowIndex++;
        }
        for (int r = rowIndex; r <= lastRow; r++) {
            clearCellText(table, r, layout.stepNoCol);
            clearCellText(table, r, layout.contentCol);
            clearCellText(table, r, layout.equipCol);
            if (layout.toolingCol >= 0) {
                clearCellText(table, r, layout.toolingCol);
            }
        }
    }

    private static void replaceProcessImage(XWPFTable table, byte[] imageBytes) {
        XWPFTableCell cell = ProcessCardDiagramImageSupport.diagramCell(table);
        if (cell == null || imageBytes == null || imageBytes.length == 0) {
            return;
        }
        try {
            ProcessCardDiagramImageSupport.PreparedImage image =
                    ProcessCardDiagramImageSupport.prepareForWord(imageBytes);
            if (image == null) {
                return;
            }
            XWPFParagraph paragraph = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
            XWPFRun run = paragraph.createRun();
            applyTemplateRunStyle(paragraph, run);
            run.addPicture(new ByteArrayInputStream(image.bytes()), image.pictureType(), image.filename(),
                    image.widthEmu(), image.heightEmu());
        } catch (Exception ex) {
            throw new IllegalStateException("工艺简图写入失败: " + ex.getMessage(), ex);
        }
    }

    private static void updatePageInfo(XWPFTable table, int pageNo, int totalPages, int scanRows) {
        for (int r = 0; r < scanRows; r++) {
            XWPFTableRow row = table.getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = 0; c < row.getTableCells().size(); c++) {
                String text = cellText(row.getCell(c));
                if (ProcessCardTemplateLayout.isTotalPagesCell(text)) {
                    setCellText(row.getCell(c), ProcessCardTemplateLayout.formatTotalPages(totalPages));
                } else if (ProcessCardTemplateLayout.isCurrentPageCell(text)) {
                    setCellText(row.getCell(c), ProcessCardTemplateLayout.formatCurrentPage(pageNo));
                }
            }
        }
    }

    private static void setCellText(XWPFTable table, int rowIndex, int colIndex, String value) {
        setCellText(cellAt(table, rowIndex, colIndex), value);
    }

    private static void setCellText(XWPFTable table, int rowIndex, int colIndex, String value,
                                    ParagraphAlignment alignment, XWPFTableCell.XWPFVertAlign verticalAlignment) {
        setCellText(cellAt(table, rowIndex, colIndex), value, alignment, verticalAlignment);
    }

    private static XWPFTableCell cellAt(XWPFTable table, int rowIndex, int colIndex) {
        if (table == null || rowIndex < 0 || rowIndex >= table.getNumberOfRows()) {
            return null;
        }
        XWPFTableRow row = table.getRow(rowIndex);
        if (row == null || colIndex < 0 || colIndex >= row.getTableCells().size()) {
            return null;
        }
        return row.getCell(colIndex);
    }

    private static void setStepContentCell(XWPFTable table, int rowIndex, int colIndex,
                                           ProcessCardStepExportPlanner.StepLine line) {
        XWPFTableCell cell = cellAt(table, rowIndex, colIndex);
        if (line.stepNameLine) {
            writeStepContentCellText(cell, line.content, ParagraphAlignment.CENTER, true);
        } else {
            writeStepContentCellText(cell, line.content, ParagraphAlignment.LEFT, false);
        }
    }

    private static void loadStepContentRunStyle(XWPFDocument document) {
        STEP_CONTENT_RUN_STYLE.remove();
        try {
            XWPFTableCell cell = cellAt(tableAt(document, 0), ROW_STEP_FIRST_FIRST, FIRST_PAGE_COLUMNS.contentCol);
            if (cell == null || cell.getParagraphs().isEmpty()) {
                return;
            }
            XWPFParagraph paragraph = cell.getParagraphs().get(0);
            if (paragraph.getCTP().getPPr() != null && paragraph.getCTP().getPPr().isSetRPr()) {
                STEP_CONTENT_RUN_STYLE.set(
                        CTRPr.Factory.parse(paragraph.getCTP().getPPr().getRPr().xmlText()));
                return;
            }
            for (XWPFRun run : paragraph.getRuns()) {
                if (run.getCTR().isSetRPr()) {
                    STEP_CONTENT_RUN_STYLE.set((CTRPr) run.getCTR().getRPr().copy());
                    return;
                }
            }
        } catch (Exception ignored) {
            STEP_CONTENT_RUN_STYLE.remove();
        }
    }

    private static void writeStepContentCellText(XWPFTableCell cell, String text,
                                                 ParagraphAlignment alignment, boolean stepNameLine) {
        if (cell == null) {
            return;
        }
        String value = text != null ? text : "";
        while (cell.getParagraphs().size() > 1) {
            cell.removeParagraph(cell.getParagraphs().size() - 1);
        }
        XWPFParagraph paragraph = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        clearParagraphRuns(paragraph);
        writeStepContentParagraphText(paragraph, value);
        if (alignment != null) {
            paragraph.setAlignment(alignment);
        }
        normalizeStepContentCellLayout(cell, stepNameLine);
    }

    private static void writeStepContentParagraphText(XWPFParagraph paragraph, String text) {
        if (!text.contains("\n") && !text.contains("\r")) {
            XWPFRun run = paragraph.createRun();
            applyStepContentRunStyle(run);
            run.setText(text, 0);
            return;
        }
        String[] parts = text.split("\\r?\\n", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                XWPFRun breakRun = paragraph.createRun();
                applyStepContentRunStyle(breakRun);
                breakRun.addBreak();
            }
            XWPFRun run = paragraph.createRun();
            applyStepContentRunStyle(run);
            run.setText(parts[i], 0);
        }
    }

    private static void applyStepContentRunStyle(XWPFRun run) {
        CTRPr template = STEP_CONTENT_RUN_STYLE.get();
        if (template != null) {
            run.getCTR().setRPr((CTRPr) template.copy());
            return;
        }
        applyDefaultCardRunStyle(run);
    }

    private static void clearCellText(XWPFTable table, int rowIndex, int colIndex) {
        XWPFTableCell cell = cellAt(table, rowIndex, colIndex);
        if (cell == null || !StringUtils.hasText(cell.getText())) {
            return;
        }
        setCellText(cell, "");
    }

    private static void setCellText(XWPFTableCell cell, String value) {
        setCellText(cell, value, null, null);
    }

    private static void setCellText(XWPFTableCell cell, String value,
                                    ParagraphAlignment alignment, XWPFTableCell.XWPFVertAlign verticalAlignment) {
        if (cell == null) {
            return;
        }
        String text = value != null ? value : "";
        if (cell.getParagraphs().isEmpty()) {
            writeStyledCellText(cell, text);
        } else {
            XWPFParagraph first = cell.getParagraphs().get(0);
            if (!text.contains("\n") && !text.contains("\r")) {
                List<XWPFRun> runs = first.getRuns();
                if (runs.isEmpty()) {
                    XWPFRun run = first.createRun();
                    applyTemplateRunStyle(first, run);
                    run.setText(text, 0);
                } else {
                    runs.get(0).setText(text, 0);
                    ensureRunStyle(first, runs.get(0));
                    for (int i = 1; i < runs.size(); i++) {
                        runs.get(i).setText("", 0);
                    }
                }
                while (cell.getParagraphs().size() > 1) {
                    cell.removeParagraph(cell.getParagraphs().size() - 1);
                }
            } else {
                clearParagraphRuns(first);
                writeParagraphText(first, text);
                while (cell.getParagraphs().size() > 1) {
                    cell.removeParagraph(cell.getParagraphs().size() - 1);
                }
            }
        }
        if (alignment != null) {
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                paragraph.setAlignment(alignment);
            }
        }
        if (verticalAlignment != null) {
            applyCellVerticalAlignment(cell, verticalAlignment);
        }
    }

    private static void applyCellVerticalAlignment(XWPFTableCell cell,
                                                   XWPFTableCell.XWPFVertAlign verticalAlignment) {
        cell.setVerticalAlignment(verticalAlignment);
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        STVerticalJc.Enum jc = switch (verticalAlignment) {
            case TOP -> STVerticalJc.TOP;
            case CENTER, BOTH -> STVerticalJc.CENTER;
            case BOTTOM -> STVerticalJc.BOTTOM;
        };
        if (tcPr.isSetVAlign()) {
            tcPr.getVAlign().setVal(jc);
        } else {
            tcPr.addNewVAlign().setVal(jc);
        }
    }

    /** 折行续写格顶对齐，并去掉模板段前/段后距避免文字被挤到底部。 */
    private static void normalizeStepContentCellLayout(XWPFTableCell cell, boolean stepNameLine) {
        if (cell == null) {
            return;
        }
        applyCellVerticalAlignment(cell, stepNameLine
                ? XWPFTableCell.XWPFVertAlign.CENTER
                : XWPFTableCell.XWPFVertAlign.TOP);
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
            CTPPr pPr = paragraph.getCTP().isSetPPr()
                    ? paragraph.getCTP().getPPr()
                    : paragraph.getCTP().addNewPPr();
            if (pPr.isSetSpacing()) {
                if (pPr.getSpacing().isSetBefore()) {
                    pPr.getSpacing().unsetBefore();
                }
                if (pPr.getSpacing().isSetAfter()) {
                    pPr.getSpacing().unsetAfter();
                }
            }
            if (!stepNameLine && pPr.isSetTextAlignment()) {
                pPr.unsetTextAlignment();
            }
        }
    }

    private static void writeStyledCellText(XWPFTableCell cell, String text) {
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        applyTemplateRunStyle(paragraph, run);
        run.setText(text, 0);
    }

    private static void applyTemplateRunStyle(XWPFParagraph paragraph, XWPFRun run) {
        if (paragraph.getCTP().getPPr() != null && paragraph.getCTP().getPPr().isSetRPr()) {
            try {
                CTRPr runRPr = CTRPr.Factory.parse(paragraph.getCTP().getPPr().getRPr().xmlText());
                run.getCTR().setRPr(runRPr);
                return;
            } catch (Exception ignored) {
                // fall through to defaults
            }
        }
        for (XWPFRun existing : paragraph.getRuns()) {
            if (existing.getCTR().isSetRPr()) {
                run.getCTR().setRPr((CTRPr) existing.getCTR().getRPr().copy());
                return;
            }
        }
        applyDefaultCardRunStyle(run);
    }

    private static void ensureRunStyle(XWPFParagraph paragraph, XWPFRun run) {
        if (run.getCTR().isSetRPr()) {
            return;
        }
        applyTemplateRunStyle(paragraph, run);
    }

    private static void applyDefaultCardRunStyle(XWPFRun run) {
        run.setFontFamily(TEMPLATE_FONT_FAMILY, XWPFRun.FontCharRange.eastAsia);
        run.setFontFamily(TEMPLATE_FONT_FAMILY);
        run.setFontSize(TEMPLATE_FONT_SIZE_POINTS);
    }

    private static void clearParagraphRuns(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        for (int i = runs.size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }
    }

    private static void writeParagraphText(XWPFParagraph paragraph, String text) {
        if (!text.contains("\n") && !text.contains("\r")) {
            XWPFRun run = paragraph.createRun();
            applyTemplateRunStyle(paragraph, run);
            run.setText(text, 0);
            return;
        }
        String[] parts = text.split("\\r?\\n", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                XWPFRun breakRun = paragraph.createRun();
                applyTemplateRunStyle(paragraph, breakRun);
                breakRun.addBreak();
            }
            XWPFRun run = paragraph.createRun();
            applyTemplateRunStyle(paragraph, run);
            run.setText(parts[i], 0);
        }
    }

    private static String cellText(XWPFTableCell cell) {
        return cell == null ? "" : cell.getText();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private static final class StepColumnLayout {
        private final int stepNoCol;
        private final int contentCol;
        private final int equipCol;
        private final int toolingCol;

        private StepColumnLayout(int stepNoCol, int contentCol, int equipCol, int toolingCol) {
            this.stepNoCol = stepNoCol;
            this.contentCol = contentCol;
            this.equipCol = equipCol;
            this.toolingCol = toolingCol;
        }
    }
}
