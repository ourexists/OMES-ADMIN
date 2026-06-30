package com.ourexists.omes.process.util;

import com.ourexists.omes.process.model.ProcessVO;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 导出《橡胶、塑料制件工艺卡片》电子版 PDF（先按 Word 模板生成 .docx，再转换，与导入 PDF 格式一致）。
 */
public final class ProcessCardPdfExporter {

    private static final String FONT_PATH = "fonts/simhei.ttf";

    private static volatile BaseFont cachedBaseFont;

    private ProcessCardPdfExporter() {
    }

    public static byte[] export(ProcessVO process, byte[] processImageBytes) throws Exception {
        byte[] docx = ProcessWordExporter.export(process, processImageBytes);
        return convertDocxToPdf(docx);
    }

    static byte[] convertDocxToPdf(byte[] docxBytes) throws Exception {
        try (InputStream in = new ByteArrayInputStream(docxBytes);
             XWPFDocument document = new XWPFDocument(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ProcessCardDocxPdfPreparer.prepare(document);
            PdfConverter.getInstance().convert(document, out, buildPdfOptions());
            return out.toByteArray();
        }
    }

    private static PdfOptions buildPdfOptions() throws Exception {
        BaseFont baseFont = resolveBaseFont();
        PdfOptions options = PdfOptions.create();
        options.fontProvider((familyName, encoding, size, style, color) ->
                new Font(baseFont, size, style, color));
        return options;
    }

    private static BaseFont resolveBaseFont() throws Exception {
        if (cachedBaseFont != null) {
            return cachedBaseFont;
        }
        synchronized (ProcessCardPdfExporter.class) {
            if (cachedBaseFont != null) {
                return cachedBaseFont;
            }
            byte[] fontBytes = loadFontBytes();
            cachedBaseFont = BaseFont.createFont(
                    "SimHei.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    fontBytes,
                    null);
            return cachedBaseFont;
        }
    }

    private static byte[] loadFontBytes() throws Exception {
        ClassPathResource resource = new ClassPathResource(FONT_PATH);
        if (!resource.exists()) {
            throw new IllegalStateException("PDF 导出字体不存在: " + FONT_PATH);
        }
        try (InputStream in = resource.getInputStream()) {
            return in.readAllBytes();
        }
    }
}
