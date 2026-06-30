package com.ourexists.omes.process.util;

import com.ourexists.omes.process.model.ProcessImportParseResult;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.util.StringUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析《橡胶、塑料制件工艺卡片》电子版 PDF（Word 导出的可编辑 PDF，解析逻辑与 Word 一致）。
 */
public final class ProcessPdfParser {

    private ProcessPdfParser() {
    }

    public static ProcessImportParseResult parse(InputStream input) throws Exception {
        byte[] bytes = input.readAllBytes();
        if (PdfScanDetector.isScanned(bytes)) {
            throw new PdfParseException("该 PDF 为扫描件，无法自动解析，请上传 Word 或电子版 PDF");
        }
        try (PDDocument document = Loader.loadPDF(bytes)) {
            List<ProcessCardImportParser.ImportTable> tables = ProcessPdfTableExtractor.extractTables(document);
            ProcessImportParseResult result = ProcessCardImportParser.parse(
                    tables, r -> extractProcessImage(document, r));
            String headerCode = ProcessPdfTableExtractor.extractHeaderProcessCode(document);
            if (StringUtils.hasText(headerCode)) {
                result.setProcessCode(headerCode);
            }
            return result;
        }
    }

    private static void extractProcessImage(PDDocument document, ProcessImportParseResult result) throws Exception {
        PDImageXObject largest = findLargestDiagramImage(document);
        if (largest == null) {
            return;
        }
        BufferedImage buffered = largest.getImage();
        if (buffered == null) {
            return;
        }
        ProcessCardImageSupport.EncodedImage encoded = ProcessCardImageSupport.encodeBufferedImage(buffered);
        result.setProcessImageBytes(encoded.bytes());
        result.setProcessImageExtension(encoded.extension());
        result.setProcessImageContentType(encoded.contentType());
    }

    private static PDImageXObject findLargestDiagramImage(PDDocument document) throws IOException {
        PDImageXObject largest = null;
        int maxPixels = 0;
        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) {
                continue;
            }
            for (COSName name : resources.getXObjectNames()) {
                for (PDImageXObject image : collectDiagramImages(resources.getXObject(name))) {
                    int pixels = image.getWidth() * image.getHeight();
                    if (pixels > maxPixels) {
                        maxPixels = pixels;
                        largest = image;
                    }
                }
            }
        }
        return largest;
    }

    private static List<PDImageXObject> collectDiagramImages(PDXObject xObject) throws IOException {
        List<PDImageXObject> images = new ArrayList<>();
        collectDiagramImagesInto(xObject, images);
        return images;
    }

    private static void collectDiagramImagesInto(PDXObject xObject, List<PDImageXObject> sink) throws IOException {
        if (xObject instanceof PDImageXObject image) {
            if (ProcessCardImageSupport.isDiagramSize(image.getWidth(), image.getHeight())) {
                sink.add(image);
            }
            return;
        }
        if (!(xObject instanceof PDFormXObject form)) {
            return;
        }
        PDResources resources = form.getResources();
        if (resources == null) {
            return;
        }
        for (COSName name : resources.getXObjectNames()) {
            collectDiagramImagesInto(resources.getXObject(name), sink);
        }
    }

    public static final class PdfParseException extends Exception {
        public PdfParseException(String message) {
            super(message);
        }
    }
}
