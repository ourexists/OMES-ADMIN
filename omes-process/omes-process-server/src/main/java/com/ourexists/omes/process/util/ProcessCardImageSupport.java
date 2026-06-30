package com.ourexists.omes.process.util;

import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** 工艺卡片导入简图编码（PDF 位图等）。 */
public final class ProcessCardImageSupport {

    private static final int MIN_DIAGRAM_EDGE = 80;

    private ProcessCardImageSupport() {
    }

    public record EncodedImage(byte[] bytes, String extension, String contentType) {
    }

    public static boolean isDiagramSize(int width, int height) {
        return width >= MIN_DIAGRAM_EDGE && height >= MIN_DIAGRAM_EDGE;
    }

    public static EncodedImage encodeBufferedImage(BufferedImage source) throws IOException {
        if (source == null) {
            throw new IOException("工艺简图为空");
        }
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        if (ImageIO.write(source, "png", pngOut) && pngOut.size() > 0) {
            return new EncodedImage(pngOut.toByteArray(), ".png", MediaType.IMAGE_PNG_VALUE);
        }
        BufferedImage rgb = toRgb(source);
        ByteArrayOutputStream jpgOut = new ByteArrayOutputStream();
        if (ImageIO.write(rgb, "jpg", jpgOut) && jpgOut.size() > 0) {
            return new EncodedImage(jpgOut.toByteArray(), ".jpg", MediaType.IMAGE_JPEG_VALUE);
        }
        throw new IOException("无法编码工艺简图");
    }

    private static BufferedImage toRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return rgb;
    }
}
