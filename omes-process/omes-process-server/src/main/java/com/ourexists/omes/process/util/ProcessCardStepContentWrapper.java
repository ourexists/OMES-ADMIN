package com.ourexists.omes.process.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 工艺卡片工序内容按模板列宽拆成多行，每行对应表格下一格。
 */
public final class ProcessCardStepContentWrapper {

    /** 与 Word 模板工序内容列宽一致（约 34 个汉字） */
    public static final int DEFAULT_MAX_CHARS = 34;

    private ProcessCardStepContentWrapper() {
    }

    public static List<String> wrapLine(String line) {
        return wrapLine(line, DEFAULT_MAX_CHARS);
    }

    /** 按固定列宽直接折行，不在标点处特殊处理。 */
    public static List<String> wrapLine(String line, int maxChars) {
        if (line == null) {
            return List.of("");
        }
        String text = line.trim();
        if (!StringUtils.hasText(text) || text.length() <= maxChars) {
            return List.of(text);
        }
        List<String> segments = new ArrayList<>();
        for (int offset = 0; offset < text.length(); offset += maxChars) {
            segments.add(text.substring(offset, Math.min(offset + maxChars, text.length())));
        }
        return segments;
    }
}
