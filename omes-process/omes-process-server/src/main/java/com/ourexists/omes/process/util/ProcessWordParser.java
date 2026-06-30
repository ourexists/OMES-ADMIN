package com.ourexists.omes.process.util;

import com.ourexists.omes.process.model.ProcessImportParseResult;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析《橡胶、塑料制件工艺卡片》Word（.docx）。
 */
public final class ProcessWordParser {

    private ProcessWordParser() {
    }

    public static ProcessImportParseResult parse(InputStream input) throws Exception {
        try (XWPFDocument document = new XWPFDocument(input)) {
            List<ProcessCardImportParser.ImportTable> tables = new ArrayList<>();
            for (XWPFTable table : document.getTables()) {
                tables.add(ProcessCardImportParser.fromWordTable(table));
            }
            return ProcessCardImportParser.parse(tables, result -> extractProcessImage(document, result));
        }
    }

    private static void extractProcessImage(XWPFDocument document, ProcessImportParseResult result) {
        Map<String, XWPFPictureData> candidates = new LinkedHashMap<>();
        collectPictureData(document.getAllPictures(), candidates);
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        for (XWPFRun run : paragraph.getRuns()) {
                            for (XWPFPicture picture : run.getEmbeddedPictures()) {
                                XWPFPictureData data = picture.getPictureData();
                                if (data != null) {
                                    candidates.putIfAbsent(String.valueOf(System.identityHashCode(data)), data);
                                }
                            }
                        }
                    }
                }
            }
        }
        XWPFPictureData selected = null;
        int maxSize = 0;
        for (XWPFPictureData picture : candidates.values()) {
            byte[] data = picture.getData();
            if (data == null || data.length <= maxSize) {
                continue;
            }
            selected = picture;
            maxSize = data.length;
        }
        if (selected == null || selected.getData() == null || selected.getData().length == 0) {
            return;
        }
        result.setProcessImageBytes(selected.getData());
        result.setProcessImageExtension("." + selected.suggestFileExtension());
        result.setProcessImageContentType(selected.getPackagePart() != null
                ? selected.getPackagePart().getContentType()
                : "image/png");
    }

    private static void collectPictureData(List<XWPFPictureData> pictures, Map<String, XWPFPictureData> sink) {
        if (pictures == null) {
            return;
        }
        for (XWPFPictureData picture : pictures) {
            if (picture == null) {
                continue;
            }
            String key = picture.getPackagePart() != null
                    ? picture.getPackagePart().getPartName().getName()
                    : String.valueOf(System.identityHashCode(picture));
            sink.putIfAbsent(key, picture);
        }
    }
}
