package com.ourexists.omes.process.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 《橡胶、塑料制件工艺卡片》标准模板表头单元格位置（0-based，与导出模板一致）。
 */
public final class ProcessCardTemplateLayout {

    /** 主标题行（表头第二行） */
    public static final int TITLE_ROW = 1;
    /** Word 主标题列（表头行内第二格） */
    public static final int WORD_TITLE_COL = 1;

    private static final Pattern PAGE_NO_WITH_DIGIT = Pattern.compile("第\\s*\\d+\\s*页");

    private ProcessCardTemplateLayout() {
    }

    public static boolean isTotalPagesCell(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String compact = text.replaceAll("\\s+", "");
        return compact.contains("共") && compact.contains("页");
    }

    /** 模板占位「第 页」或已填「第2页」 */
    public static boolean isCurrentPageCell(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String compact = text.replaceAll("\\s+", "");
        if (PAGE_NO_WITH_DIGIT.matcher(compact).matches()) {
            return true;
        }
        return "第页".equals(compact);
    }

    public static String formatCurrentPage(int pageNo) {
        return "第 " + pageNo + " 页";
    }

    public static String formatTotalPages(int totalPages) {
        return "共 " + totalPages + " 页";
    }

    /** 仅做空白与公式展示字符规范化，不做标题文案匹配。 */
    public static String normalizeTitleCell(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        return OfficeMathTextExtractor.normalizeDisplayText(raw.trim());
    }
}
