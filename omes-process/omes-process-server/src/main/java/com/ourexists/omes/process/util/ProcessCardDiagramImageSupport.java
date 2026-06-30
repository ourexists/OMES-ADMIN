package com.ourexists.omes.process.util;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** 工艺卡片简图区图片加载与 Word 插入尺寸计算。 */
public final class ProcessCardDiagramImageSupport {

    private static final int DIAGRAM_ROW_START = 5;
    private static final int DIAGRAM_ROW_END = 14;
    private static final int DIAGRAM_COL = 0;

    /** 简图区宽约 7164 DXA ≈ 358pt */
    private static final double DIAGRAM_MAX_WIDTH_PT = 340;
    /** 简图区合并行高约 218pt */
    private static final double DIAGRAM_MAX_HEIGHT_PT = 210;

    private ProcessCardDiagramImageSupport() {
    }

    public static byte[] loadImageBytes(Path storageRoot, String storagePath) {
        if (!StringUtils.hasText(storagePath) || storageRoot == null) {
            return null;
        }
        String normalized = storagePath.replace('\\', '/').trim();
        int queryIdx = normalized.indexOf('?');
        if (queryIdx > 0) {
            normalized = normalized.substring(0, queryIdx);
        }
        try {
            Path filePath = storageRoot.resolve(normalized).normalize();
            if (!filePath.startsWith(storageRoot) || !Files.isRegularFile(filePath)) {
                return null;
            }
            return Files.readAllBytes(filePath);
        } catch (Exception ex) {
            return null;
        }
    }

    public static PreparedImage prepareForWord(byte[] imageBytes) throws IOException {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        byte[] normalized = normalizeImageBytes(imageBytes);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(normalized));
        if (image == null) {
            throw new IOException("无法识别工艺简图格式");
        }
        int pictureType = detectPictureType(normalized);
        String filename = pictureType == XWPFDocument.PICTURE_TYPE_JPEG
                ? "process-diagram.jpg" : "process-diagram.png";
        Dimension size = scaleToFit(image.getWidth(), image.getHeight(),
                DIAGRAM_MAX_WIDTH_PT, DIAGRAM_MAX_HEIGHT_PT);
        return new PreparedImage(normalized, pictureType, filename, size.widthEmu(), size.heightEmu());
    }

    public static XWPFTableCell diagramCell(XWPFTable table) {
        if (table == null || table.getNumberOfRows() <= DIAGRAM_ROW_START) {
            return null;
        }
        XWPFTableRow row = table.getRow(DIAGRAM_ROW_START);
        if (row == null || row.getTableCells().size() <= DIAGRAM_COL) {
            return null;
        }
        return row.getCell(DIAGRAM_COL);
    }

    private static byte[] normalizeImageBytes(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                return imageBytes;
            }
            ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
            if (ImageIO.write(image, "png", pngOut) && pngOut.size() > 0) {
                return pngOut.toByteArray();
            }
            ByteArrayOutputStream jpgOut = new ByteArrayOutputStream();
            BufferedImage rgb = toRgb(image);
            if (ImageIO.write(rgb, "jpg", jpgOut) && jpgOut.size() > 0) {
                return jpgOut.toByteArray();
            }
        }
        return imageBytes;
    }

    private static BufferedImage toRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        var graphics = rgb.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return rgb;
    }

    private static int detectPictureType(byte[] bytes) {
        if (bytes.length > 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
            return XWPFDocument.PICTURE_TYPE_JPEG;
        }
        return XWPFDocument.PICTURE_TYPE_PNG;
    }

    private static Dimension scaleToFit(int widthPx, int heightPx, double maxWidthPt, double maxHeightPt) {
        if (widthPx <= 0 || heightPx <= 0) {
            return new Dimension(Units.toEMU(maxWidthPt), Units.toEMU(maxHeightPt));
        }
        double widthPt = maxWidthPt;
        double heightPt = widthPt * heightPx / widthPx;
        if (heightPt > maxHeightPt) {
            heightPt = maxHeightPt;
            widthPt = heightPt * widthPx / heightPx;
        }
        return new Dimension(Units.toEMU(widthPt), Units.toEMU(heightPt));
    }

    public record PreparedImage(byte[] bytes, int pictureType, String filename, int widthEmu, int heightEmu) {
    }

    private record Dimension(int widthEmu, int heightEmu) {
    }
}
