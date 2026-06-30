package com.ourexists.omes.process.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * 判断 PDF 是否为扫描件（整页位图、几乎无可提取文本）。
 */
public final class PdfScanDetector {

    /** 全文档可识别字符过少则视为扫描件 */
    private static final int MIN_MEANINGFUL_CHARS = 48;
    /** 平均每页文本过少且含大图时视为扫描件 */
    private static final int MIN_CHARS_PER_PAGE_WITH_IMAGES = 28;
    private static final Pattern CJK = Pattern.compile("[\\p{IsHan}]");

    private PdfScanDetector() {
    }

    public static boolean isScanned(InputStream input) throws IOException {
        byte[] bytes = input.readAllBytes();
        return isScanned(bytes);
    }

    public static boolean isScanned(byte[] pdfBytes) throws IOException {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return true;
        }
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            int pageCount = document.getNumberOfPages();
            if (pageCount <= 0) {
                return true;
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            int meaningful = countMeaningfulChars(text);
            if (meaningful < MIN_MEANINGFUL_CHARS) {
                return true;
            }
            int imageCount = countPageImages(document);
            if (imageCount >= pageCount
                    && meaningful / pageCount < MIN_CHARS_PER_PAGE_WITH_IMAGES
                    && countCjk(text) < 12) {
                return true;
            }
            return !looksLikeProcessCard(text) && imageCount >= pageCount && meaningful < 120;
        }
    }

    private static boolean looksLikeProcessCard(String text) {
        if (text == null) {
            return false;
        }
        return text.contains("工艺卡片") || text.contains("工序") || text.contains("硫化");
    }

    private static int countMeaningfulChars(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!Character.isWhitespace(ch) && ch != '\u0000') {
                count++;
            }
        }
        return count;
    }

    private static int countCjk(String text) {
        if (text == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (CJK.matcher(String.valueOf(text.charAt(i))).matches()) {
                count++;
            }
        }
        return count;
    }

    private static int countPageImages(PDDocument document) throws IOException {
        int images = 0;
        for (PDPage page : document.getPages()) {
            if (pageHasLargeImage(page)) {
                images++;
            }
        }
        return images;
    }

    private static boolean pageHasLargeImage(PDPage page) throws IOException {
        PDResources resources = page.getResources();
        if (resources == null) {
            return false;
        }
        for (COSName name : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(name);
            if (xObject instanceof PDImageXObject image) {
                if (image.getWidth() >= 200 && image.getHeight() >= 200) {
                    return true;
                }
            }
        }
        return false;
    }
}
