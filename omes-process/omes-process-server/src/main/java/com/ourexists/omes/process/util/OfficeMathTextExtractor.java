package com.ourexists.omes.process.util;

import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 从 Word 单元格 Office Math（OMML）提取可读文本。
 */
public final class OfficeMathTextExtractor {

    private static final String MATH_NS = "http://schemas.openxmlformats.org/officeDocument/2006/math";
    private static final String DRAWING_NS = "http://schemas.openxmlformats.org/drawingml/2006/main";
    private static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

    private OfficeMathTextExtractor() {
    }

    /**
     * 按 Word 单元格内段落顺序合并普通文本与 Office 公式，转为计算机可读展示文本。
     */
    public static String extractWordCellDisplayText(String cellXml) {
        if (!StringUtils.hasText(cellXml)) {
            return "";
        }
        try {
            var doc = newNamespaceAwareDocument(new java.io.ByteArrayInputStream(
                    cellXml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            Element root = doc.getDocumentElement();
            StringBuilder sb = new StringBuilder();
            NodeList paragraphs = root.getElementsByTagNameNS(W_NS, "p");
            for (int i = 0; i < paragraphs.getLength(); i++) {
                if (paragraphs.item(i) instanceof Element paragraph) {
                    appendWordParagraphText(paragraph, sb);
                    if (i < paragraphs.getLength() - 1 && sb.length() > 0) {
                        sb.append('\n');
                    }
                }
            }
            return normalizeDisplayText(sb.toString());
        } catch (Exception ex) {
            return "";
        }
    }

    public static String normalizeDisplayText(String text) {
        if (!StringUtils.hasText(text)) {
            return text == null ? "" : text;
        }
        String normalized = text;
        normalized = normalized.replace('\u0007', ' ');
        normalized = normalized.replace('\u2212', '-');
        normalized = normalized.replace('\u2013', '-');
        normalized = normalized.replace('\u2014', '-');
        normalized = normalized.replace('\u00D7', '×');
        normalized = normalized.replace('\u210E', 'h');
        normalized = normalized.replace('\u2113', 'l');
        normalized = normalized.replace("ℎ", "h");
        normalized = normalized.replace("𝑔", "g");
        normalized = normalized.replace("𝜑", "φ");
        normalized = normalized.replace("𝛷", "Φ");
        normalized = normalized.replace("_^", "");
        normalized = normalized.replace("﹥", "→");
        normalized = normalized.replace("->", "→");
        normalized = normalized.replaceAll("[ \t\\x0B\\f]+", " ");
        normalized = normalized.replaceAll("(?m)[ \t]+\\n", "\n");
        normalized = normalized.replaceAll("尺寸为[、,\\s]+(?=[、,]|$)", "尺寸为");
        normalized = normalized.replaceAll("为[、,]+(?=[Φφ])", "为");
        return normalized.trim();
    }

    private static void appendWordParagraphText(Element paragraph, StringBuilder out) {
        NodeList children = paragraph.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (!(child instanceof Element el)) {
                continue;
            }
            appendWordInlineNode(el, out);
        }
    }

    private static void appendWordInlineNode(Element el, StringBuilder out) {
        String local = el.getLocalName();
        if (!StringUtils.hasText(local)) {
            String tag = el.getTagName();
            int idx = tag != null ? tag.indexOf(':') : -1;
            local = idx >= 0 ? tag.substring(idx + 1) : tag;
        }
        if (local == null) {
            return;
        }
        switch (local) {
            case "r" -> {
                NodeList texts = el.getElementsByTagNameNS(W_NS, "t");
                for (int i = 0; i < texts.getLength(); i++) {
                    Node node = texts.item(i);
                    if (node.getTextContent() != null) {
                        out.append(node.getTextContent());
                    }
                }
            }
            case "oMath" -> out.append(extractPlainViaLatex(el, false));
            case "oMathPara" -> {
                NodeList inner = el.getElementsByTagNameNS(MATH_NS, "oMath");
                for (int i = 0; i < inner.getLength(); i++) {
                    if (inner.item(i) instanceof Element mathEl) {
                        out.append(extractPlainViaLatex(mathEl, false));
                    }
                }
            }
            case "AlternateContent", "Choice" -> appendMathChildren(el, out);
            default -> {
                if (MATH_NS.equals(el.getNamespaceURI()) && "oMath".equals(local)) {
                    out.append(extractPlainViaLatex(el, false));
                } else {
                    appendWordChildren(el, out);
                }
            }
        }
    }

    private static void appendWordChildren(Element parent, StringBuilder out) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element el) {
                appendWordInlineNode(el, out);
            }
        }
    }

    static String extractFromXmlFragment(String xml) {
        if (!StringUtils.hasText(xml)) {
            return "";
        }
        try {
            var doc = newNamespaceAwareDocument(new java.io.ByteArrayInputStream(
                    xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            NodeList mathNodes = doc.getElementsByTagNameNS(MATH_NS, "oMath");
            if (mathNodes.getLength() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mathNodes.getLength(); i++) {
                    if (mathNodes.item(i) instanceof Element mathEl) {
                        sb.append(extractPlainViaLatex(mathEl, false));
                    }
                }
                if (sb.length() > 0) {
                    return normalizeMathPlainText(sb.toString());
                }
            }
            String runs = extractMathRunsText(xml);
            if (StringUtils.hasText(runs)) {
                return JLaTeXMathFormulaSupport.plainTextViaLatex(runs);
            }
            Set<String> texts = new LinkedHashSet<>();
            collectMathTexts(doc.getDocumentElement(), texts);
            if (texts.isEmpty()) {
                return "";
            }
            return texts.iterator().next();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * OMML → LaTeX → JLaTeXMath 校验 → 可读文本。
     *
     * @param flat true 时按 m:t 扁平拼接（参数区公差等）
     */
    public static String extractPlainViaLatex(Element mathEl, boolean flat) {
        String latex = flat ? OmmlToLatexConverter.fromFlat(mathEl) : OmmlToLatexConverter.fromStructured(mathEl);
        String plain = JLaTeXMathFormulaSupport.resolvePlainFromLatex(latex, () ->
                flat ? parseMathElementFlat(mathEl) : parseMathElement(mathEl));
        return normalizeMathPlainText(plain);
    }

    static String extractMathRunsText(String xml) {
        if (!StringUtils.hasText(xml)) {
            return "";
        }
        try {
            var doc = newNamespaceAwareDocument(new java.io.ByteArrayInputStream(
                    xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            NodeList nodes = doc.getElementsByTagNameNS(MATH_NS, "t");
            if (nodes.getLength() == 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getTextContent() != null) {
                    sb.append(node.getTextContent());
                }
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static List<String> extractMathRunParts(String xml) {
        List<String> parts = new ArrayList<>();
        if (!StringUtils.hasText(xml)) {
            return parts;
        }
        try {
            var doc = newNamespaceAwareDocument(new java.io.ByteArrayInputStream(
                    xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            NodeList nodes = doc.getElementsByTagNameNS(MATH_NS, "t");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getTextContent() != null && StringUtils.hasText(node.getTextContent())) {
                    parts.add(node.getTextContent().trim());
                }
            }
        } catch (Exception ignored) {
            // ignore malformed xml
        }
        return parts;
    }

    private static org.w3c.dom.Document newNamespaceAwareDocument(InputStream in) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(in);
    }

    private static void collectMathTexts(Element root, Set<String> sink) {
        NodeList mathNodes = root.getElementsByTagNameNS(MATH_NS, "oMath");
        for (int i = 0; i < mathNodes.getLength(); i++) {
            Node node = mathNodes.item(i);
            if (node instanceof Element mathEl) {
                String text = extractPlainViaLatex(mathEl, false);
                if (StringUtils.hasText(text)) {
                    sink.add(normalizeMathPlainText(text));
                }
            }
        }
        NodeList paraMathNodes = root.getElementsByTagNameNS(MATH_NS, "oMathPara");
        for (int i = 0; i < paraMathNodes.getLength(); i++) {
            Node node = paraMathNodes.item(i);
            if (node instanceof Element paraEl) {
                NodeList inner = paraEl.getElementsByTagNameNS(MATH_NS, "oMath");
                for (int j = 0; j < inner.getLength(); j++) {
                    Node innerNode = inner.item(j);
                    if (innerNode instanceof Element mathEl) {
                        String text = extractPlainViaLatex(mathEl, false);
                        if (StringUtils.hasText(text)) {
                            sink.add(normalizeMathPlainText(text));
                        }
                    }
                }
            }
        }
        NodeList textNodes = root.getElementsByTagNameNS(DRAWING_NS, "t");
        if (mathNodes.getLength() == 0 && paraMathNodes.getLength() == 0) {
            StringBuilder plain = new StringBuilder();
            for (int i = 0; i < textNodes.getLength(); i++) {
                Node node = textNodes.item(i);
                if (node.getTextContent() != null) {
                    plain.append(node.getTextContent());
                }
            }
            String text = normalizeMathPlainText(plain.toString());
            if (StringUtils.hasText(text) && looksLikeFormulaText(text)) {
                sink.add(JLaTeXMathFormulaSupport.plainTextViaLatex(text));
            }
        }
    }

    private static boolean looksLikeFormulaText(String text) {
        return text.contains("室温") || text.contains("℃") || text.contains("±")
                || text.contains("→") || text.contains("恒温") || text.contains("𝜑")
                || text.contains("𝛷") || text.contains("φ") || text.contains("Φ");
    }

    static String parseMathElement(Element mathEl) {
        StringBuilder out = new StringBuilder();
        appendMathChildren(mathEl, out);
        return out.toString();
    }

    /** 参数区公式：按 m:t 顺序拼接，保留 9.6+0.5g 等公差写法。 */
    static String parseMathElementFlat(Element mathEl) {
        NodeList nodes = mathEl.getElementsByTagNameNS(MATH_NS, "t");
        if (nodes.getLength() == 0) {
            return parseMathElement(mathEl);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getTextContent() != null) {
                sb.append(node.getTextContent());
            }
        }
        return sb.toString();
    }

    private static void appendMathChildren(Element parent, StringBuilder out) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (!(child instanceof Element el)) {
                continue;
            }
            appendMathNode(el, out);
        }
    }

    private static void appendMathNode(Element el, StringBuilder out) {
        String local = el.getLocalName();
        if (!StringUtils.hasText(local)) {
            String tag = el.getTagName();
            int idx = tag != null ? tag.indexOf(':') : -1;
            local = idx >= 0 ? tag.substring(idx + 1) : tag;
        }
        switch (local) {
            case "t" -> {
                if (el.getTextContent() != null) {
                    out.append(el.getTextContent());
                }
            }
            case "r" -> appendMathChildren(el, out);
            case "limUpp" -> {
                Element e = firstChild(el, "e");
                Element lim = firstChild(el, "lim");
                if (e != null) {
                    appendMathChildren(e, out);
                }
                out.append('┴');
                if (lim != null) {
                    appendMathChildren(lim, out);
                }
            }
            case "limLow" -> {
                Element e = firstChild(el, "e");
                Element lim = firstChild(el, "lim");
                if (e != null) {
                    appendMathChildren(e, out);
                }
                if (lim != null) {
                    out.append('_');
                    appendMathChildren(lim, out);
                }
            }
            case "sSubSup" -> appendSubSup(out, el, true, true);
            case "sSub" -> appendSubSup(out, el, true, false);
            case "sSup" -> appendSubSup(out, el, false, true);
            case "e" -> appendMathChildren(el, out);
            case "sub", "sup" -> appendMathChildren(el, out);
            case "num", "den" -> appendMathChildren(el, out);
            case "f" -> {
                Element num = firstChild(el, "num");
                Element den = firstChild(el, "den");
                if (num != null) {
                    appendMathChildren(num, out);
                }
                out.append('/');
                if (den != null) {
                    appendMathChildren(den, out);
                }
            }
            default -> appendMathChildren(el, out);
        }
    }

    private static Element firstChild(Element parent, String localName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element el && matchesLocalName(el, localName)) {
                return el;
            }
        }
        return null;
    }

    private static boolean matchesLocalName(Element el, String localName) {
        if (localName.equals(el.getLocalName())) {
            return true;
        }
        String tag = el.getTagName();
        return tag != null && tag.endsWith(":" + localName);
    }

    private static void appendSubSup(StringBuilder out, Element el, boolean withSub, boolean withSup) {
        Element e = firstChild(el, "e");
        Element sub = withSub ? firstChild(el, "sub") : null;
        Element sup = withSup ? firstChild(el, "sup") : null;
        if (e != null) {
            appendMathChildren(e, out);
        }
        String subText = collectMathText(sub);
        String supText = collectMathText(sup);
        if (StringUtils.hasText(subText) && StringUtils.hasText(supText)) {
            out.append('(').append(normalizeMinus(subText)).append('/').append(supText).append(')');
        } else if (StringUtils.hasText(subText)) {
            out.append('_').append(normalizeMinus(subText));
        } else if (StringUtils.hasText(supText)) {
            if (isEngineeringToleranceMark(supText)) {
                out.append(supText);
            } else {
                out.append('^').append(supText);
            }
        }
    }

    private static boolean isEngineeringToleranceMark(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        return text.matches("^[+\\-−][\\d.]+");
    }

    private static String collectMathText(Element el) {
        if (el == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        appendMathChildren(el, sb);
        return normalizeMinus(sb.toString().trim());
    }

    private static String normalizeMinus(String text) {
        return text.replace('\u2212', '-').replace('\u2013', '-');
    }

    static String normalizeMathPlainText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = normalizeDisplayText(text.replaceAll("\\s+", ""));
        normalized = normalized.replaceAll("\\^([+\\-])", "$1");
        return normalized;
    }
}
