package com.ourexists.omes.process.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;

import java.math.BigInteger;
import java.util.List;

/**
 * 为 docx→PDF 转换做表格预处理。xdocreport 对纵向合并单元格支持不完善，需展平简图区 vMerge。
 */
final class ProcessCardDocxPdfPreparer {

    private static final int PARAM_ROW_START = 5;
    private static final int PARAM_ROW_END = 14;
    private static final int[] PARAM_CELL_SPANS = {10, 4, 2};

    private ProcessCardDocxPdfPreparer() {
    }

    static void prepare(XWPFDocument document) {
        if (document.getDocument().getBody().sizeOfTblArray() == 0) {
            return;
        }
        XWPFTable table = new XWPFTable(document.getDocument().getBody().getTblArray(0), document);
        int[] colWidths = readGridColWidths(table.getCTTbl());
        flattenDiagramVerticalMerge(table);
        if (colWidths.length > 0) {
            applyParamCellWidths(table, colWidths);
        }
    }

    private static int[] readGridColWidths(CTTbl ctTbl) {
        CTTblGrid grid = ctTbl.getTblGrid();
        if (grid == null) {
            return new int[0];
        }
        List<CTTblGridCol> cols = grid.getGridColList();
        int[] widths = new int[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            Object w = cols.get(i).getW();
            widths[i] = w instanceof BigInteger bi ? bi.intValue() : 0;
        }
        return widths;
    }

    private static void applyParamCellWidths(XWPFTable table, int[] colWidths) {
        for (int row = PARAM_ROW_START; row <= PARAM_ROW_END && row < table.getNumberOfRows(); row++) {
            XWPFTableRow tableRow = table.getRow(row);
            int colOffset = 0;
            for (int cellIndex = 0; cellIndex < PARAM_CELL_SPANS.length
                    && cellIndex < tableRow.getTableCells().size(); cellIndex++) {
                int span = PARAM_CELL_SPANS[cellIndex];
                int width = sumWidths(colWidths, colOffset, span);
                setCellWidth(tableRow.getCell(cellIndex), width);
                colOffset += span;
            }
        }
    }

    private static int sumWidths(int[] colWidths, int start, int span) {
        int total = 0;
        for (int i = start; i < start + span && i < colWidths.length; i++) {
            total += colWidths[i];
        }
        return total;
    }

    private static void setCellWidth(XWPFTableCell cell, int widthDxa) {
        if (cell == null || widthDxa <= 0) {
            return;
        }
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        if (tcPr.isSetTcW()) {
            tcPr.getTcW().setW(BigInteger.valueOf(widthDxa));
            tcPr.getTcW().setType(org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA);
        } else {
            tcPr.addNewTcW().setW(BigInteger.valueOf(widthDxa));
            tcPr.getTcW().setType(org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA);
        }
    }

    private static void flattenDiagramVerticalMerge(XWPFTable table) {
        for (int row = PARAM_ROW_START; row <= PARAM_ROW_END && row < table.getNumberOfRows(); row++) {
            XWPFTableCell cell = table.getRow(row).getCell(0);
            if (cell == null) {
                continue;
            }
            CTTcPr tcPr = cell.getCTTc().getTcPr();
            if (tcPr != null && tcPr.isSetVMerge()) {
                tcPr.unsetVMerge();
            }
        }
    }
}
