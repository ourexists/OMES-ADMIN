package com.ourexists.omes.process.util;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * 使用 JLaTeXMath 解析 / 渲染 LaTeX 公式。
 * <p>
 * 工艺卡片 OMML 先转为 LaTeX，经 JLaTeXMath 校验后再还原为可读文本供参数解析。
 */
public final class JLaTeXMathFormulaSupport {

    private static final int DEFAULT_FONT_SIZE = 20;
    private static final Pattern FRAC = Pattern.compile("\\\\frac\\{");
    private static final Pattern UNDERSET = Pattern.compile("\\\\underset\\{");
    private static final Pattern SUB_SUP_PAIR = Pattern.compile(
            "([^{}_^]+)_\\{([^{}]+)\\}\\^\\{([^{}]+)\\}");
    private static final Pattern SUP_ONLY = Pattern.compile("\\^\\{([^{}]+)\\}");
    private static final Pattern SUB_ONLY = Pattern.compile("_\\{([^{}]+)\\}");

    private JLaTeXMathFormulaSupport() {
    }

    /**
     * OMML → LaTeX → JLaTeXMath 校验 → 可读文本。
     */
    public static String resolvePlainFromLatex(String latex, java.util.function.Supplier<String> fallback) {
        if (!StringUtils.hasText(latex)) {
            return fallback.get();
        }
        if (parseLatex(latex).isPresent()) {
            return latexToPlainText(latex);
        }
        return fallback.get();
    }

    /**
     * 已有可读文本时，先转 LaTeX 校验再还原，保证符号归一化一致。
     */
    public static String plainTextViaLatex(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return "";
        }
        String compact = plainText.replaceAll("\\s+", "");
        String latex = OmmlToLatexConverter.wrapText(compact);
        return resolvePlainFromLatex(latex, () -> compact);
    }

    /**
     * 将 OMML 提取出的工艺公式文本转为 LaTeX。
     */
    public static String toLatex(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return "";
        }
        return OmmlToLatexConverter.wrapText(plainText.replaceAll("\\s+", ""));
    }

    /**
     * 将本模块生成的 LaTeX 还原为工艺卡片可读文本。
     */
    public static String latexToPlainText(String latex) {
        if (!StringUtils.hasText(latex)) {
            return "";
        }
        String s = latex.trim();
        s = replaceFractions(s);
        s = replaceUnderset(s);
        s = replaceKnownCommands(s);
        s = unwrapTextBlocks(s);
        s = convertSubSupPairs(s);
        s = convertToleranceSuperscripts(s);
        s = convertSubscripts(s);
        s = s.replaceAll("\\s+", "");
        return s;
    }

    /**
     * 使用 JLaTeXMath 解析 LaTeX 公式；失败时返回空。
     */
    public static Optional<TeXFormula> parseLatex(String latex) {
        if (!StringUtils.hasText(latex)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new TeXFormula(latex));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * 解析工艺公式文本（OMML 提取结果）并渲染为 PNG。
     */
    public static Optional<byte[]> renderPlainFormulaToPng(String plainText) {
        String latex = toLatex(plainText);
        return parseLatex(latex).flatMap(formula -> renderToPng(formula, DEFAULT_FONT_SIZE));
    }

    public static Optional<byte[]> renderToPng(TeXFormula formula, int fontSize) {
        try {
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize);
            icon.setInsets(new java.awt.Insets(2, 2, 2, 2));
            BufferedImage image = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            var g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return Optional.of(out.toByteArray());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static String replaceFractions(String s) {
        StringBuilder out = new StringBuilder();
        int idx = 0;
        while (idx < s.length()) {
            int start = s.indexOf("\\frac{", idx);
            if (start < 0) {
                out.append(s.substring(idx));
                break;
            }
            out.append(s, idx, start);
            BraceContent num = readBraceContent(s, start + "\\frac".length());
            if (num == null) {
                out.append(s.substring(start));
                break;
            }
            if (num.end >= s.length() || s.charAt(num.end) != '{') {
                out.append(s, start, num.end);
                idx = num.end;
                continue;
            }
            BraceContent den = readBraceContent(s, num.end);
            if (den == null) {
                out.append(s.substring(start));
                break;
            }
            out.append(latexToPlainText(num.content))
                    .append('/')
                    .append(latexToPlainText(den.content));
            idx = den.end;
        }
        return out.toString();
    }

    private static String replaceUnderset(String s) {
        StringBuilder out = new StringBuilder();
        int idx = 0;
        while (idx < s.length()) {
            int start = s.indexOf("\\underset{", idx);
            if (start < 0) {
                out.append(s.substring(idx));
                break;
            }
            out.append(s, idx, start);
            BraceContent lim = readBraceContent(s, start + "\\underset".length());
            if (lim == null) {
                out.append(s.substring(start));
                break;
            }
            if (lim.end >= s.length() || s.charAt(lim.end) != '{') {
                out.append(s, start, lim.end);
                idx = lim.end;
                continue;
            }
            BraceContent body = readBraceContent(s, lim.end);
            if (body == null) {
                out.append(s.substring(start));
                break;
            }
            String limPlain = latexToPlainText(lim.content);
            if ("\\rightarrow".equals(body.content.trim())) {
                out.append("→┴").append(limPlain);
            } else {
                out.append(latexToPlainText(body.content)).append('┴').append(limPlain);
            }
            idx = body.end;
        }
        return out.toString();
    }

    private static String replaceKnownCommands(String s) {
        s = s.replaceAll("\\\\Phi\\\\text\\{([^{}]+)\\}", "Φ$1");
        s = s.replaceAll("\\\\varphi\\\\text\\{([^{}]+)\\}", "φ$1");
        return s.replace("\\rightarrow", "→")
                .replace("\\bot", "┴")
                .replace("\\times", "×")
                .replace("\\pm", "±")
                .replace("\\degree C", "℃")
                .replace("\\varphi", "φ")
                .replace("\\Phi", "Φ");
    }

    private static String unwrapTextBlocks(String s) {
        StringBuilder out = new StringBuilder();
        int idx = 0;
        while (idx < s.length()) {
            int start = s.indexOf("\\text{", idx);
            if (start < 0) {
                out.append(s.substring(idx));
                break;
            }
            out.append(s, idx, start);
            BraceContent block = readBraceContent(s, start + "\\text".length());
            if (block == null) {
                out.append(s.substring(start));
                break;
            }
            out.append(block.content);
            idx = block.end;
        }
        return out.toString();
    }

    private static String convertSubSupPairs(String s) {
        Matcher matcher = SUB_SUP_PAIR.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String base = matcher.group(1);
            String sub = normalizeMinus(matcher.group(2));
            String sup = matcher.group(3);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(base + "(" + sub + "/" + sup + ")"));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String convertToleranceSuperscripts(String s) {
        Matcher matcher = SUP_ONLY.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String sup = normalizeMinus(matcher.group(1));
            if (isEngineeringToleranceMark(sup)) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(sup));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement("^" + sup));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String convertSubscripts(String s) {
        Matcher matcher = SUB_ONLY.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement("_" + normalizeMinus(matcher.group(1))));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static boolean isEngineeringToleranceMark(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        return text.matches("^[+\\-−][\\d.]+");
    }

    private static String normalizeMinus(String text) {
        return text.replace('\u2212', '-').replace('\u2013', '-');
    }

    private static BraceContent readBraceContent(String s, int braceStart) {
        if (braceStart >= s.length() || s.charAt(braceStart) != '{') {
            return null;
        }
        int depth = 0;
        int contentStart = braceStart + 1;
        for (int i = braceStart; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return new BraceContent(s.substring(contentStart, i), i + 1);
                }
            }
        }
        return null;
    }

    private record BraceContent(String content, int end) {
    }
}
