package com.ourexists.omes.process.util;

import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 将 Office Math（OMML）转为 LaTeX，供 JLaTeXMath 校验后再还原为工艺可读文本。
 */
final class OmmlToLatexConverter {

    private static final String MATH_NS = "http://schemas.openxmlformats.org/officeDocument/2006/math";

    private OmmlToLatexConverter() {
    }

    static String fromStructured(Element mathEl) {
        if (mathEl == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        appendMathChildren(mathEl, out);
        return out.toString().trim();
    }

    static String fromFlat(Element mathEl) {
        if (mathEl == null) {
            return "";
        }
        NodeList nodes = mathEl.getElementsByTagNameNS(MATH_NS, "t");
        if (nodes.getLength() == 0) {
            return fromStructured(mathEl);
        }
        StringBuilder raw = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getTextContent() != null) {
                raw.append(node.getTextContent());
            }
        }
        return wrapText(raw.toString());
    }

    private static void appendMathChildren(Element parent, StringBuilder out) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element el) {
                appendMathNode(el, out);
            }
        }
    }

    private static void appendMathNode(Element el, StringBuilder out) {
        String local = localName(el);
        switch (local) {
            case "t" -> {
                if (el.getTextContent() != null) {
                    out.append(wrapText(el.getTextContent()));
                }
            }
            case "r" -> appendMathChildren(el, out);
            case "limUpp" -> {
                Element e = firstChild(el, "e");
                Element lim = firstChild(el, "lim");
                String eLatex = collectLatex(e);
                String limLatex = collectLatex(lim);
                if (isArrowLatex(eLatex)) {
                    out.append("\\underset{").append(limLatex).append("}{\\rightarrow}");
                } else {
                    out.append("\\underset{").append(limLatex).append("}{").append(eLatex).append("}");
                }
            }
            case "limLow" -> {
                Element e = firstChild(el, "e");
                Element lim = firstChild(el, "lim");
                if (e != null) {
                    appendMathChildren(e, out);
                }
                if (lim != null) {
                    out.append("_{").append(collectLatex(lim)).append("}");
                }
            }
            case "sSubSup" -> appendSubSup(out, el, true, true);
            case "sSub" -> appendSubSup(out, el, true, false);
            case "sSup" -> appendSubSup(out, el, false, true);
            case "e", "sub", "sup", "num", "den" -> appendMathChildren(el, out);
            case "f" -> {
                Element num = firstChild(el, "num");
                Element den = firstChild(el, "den");
                out.append("\\frac{");
                out.append(collectLatex(num));
                out.append("}{");
                out.append(collectLatex(den));
                out.append('}');
            }
            default -> appendMathChildren(el, out);
        }
    }

    private static void appendSubSup(StringBuilder out, Element el, boolean withSub, boolean withSup) {
        Element e = firstChild(el, "e");
        Element sub = withSub ? firstChild(el, "sub") : null;
        Element sup = withSup ? firstChild(el, "sup") : null;
        if (e != null) {
            appendMathChildren(e, out);
        }
        String subLatex = collectLatex(sub);
        String supLatex = collectLatex(sup);
        if (StringUtils.hasText(subLatex) && StringUtils.hasText(supLatex)) {
            out.append("_{").append(subLatex).append("}^{").append(supLatex).append('}');
        } else if (StringUtils.hasText(subLatex)) {
            out.append("_{").append(subLatex).append('}');
        } else if (StringUtils.hasText(supLatex)) {
            out.append("^{").append(supLatex).append('}');
        }
    }

    private static String collectLatex(Element el) {
        if (el == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        appendMathChildren(el, sb);
        return sb.toString();
    }

    private static boolean isArrowLatex(String latex) {
        if (!StringUtils.hasText(latex)) {
            return false;
        }
        String compact = latex.replace("\\text{", "").replace("}", "").trim();
        return "→".equals(compact) || compact.contains("\\rightarrow");
    }

    static String wrapText(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        StringBuilder sb = new StringBuilder("\\text{");
        for (int offset = 0; offset < raw.length(); ) {
            int cp = raw.codePointAt(offset);
            offset += Character.charCount(cp);
            appendEscapedTextCodePoint(sb, cp);
        }
        sb.append('}');
        return sb.toString()
                .replace("\\text{}", "")
                .replace("\\text{}\\text{", "\\text{");
    }

    private static void appendEscapedTextCodePoint(StringBuilder sb, int cp) {
        switch (cp) {
            case '→' -> sb.append("}\\rightarrow\\text{");
            case '┴' -> sb.append("}\\bot\\text{");
            case '×' -> sb.append("}\\times\\text{");
            case '±' -> sb.append("}\\pm\\text{");
            case '℃' -> sb.append("}\\degree C\\text{");
            case 0x1D719, 0x03C6 -> sb.append("}\\varphi\\text{");
            case 0x1D6F7, 0x03A6 -> sb.append("}\\Phi\\text{");
            case '_', '^', '{', '}', '\\', '$', '%', '#', '&' -> sb.append("}\\text{")
                    .append((char) cp)
                    .append("}\\text{");
            default -> {
                if (Character.isBmpCodePoint(cp)) {
                    sb.append((char) cp);
                } else {
                    sb.append(new String(Character.toChars(cp)));
                }
            }
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

    private static String localName(Element el) {
        String local = el.getLocalName();
        if (StringUtils.hasText(local)) {
            return local;
        }
        String tag = el.getTagName();
        if (!StringUtils.hasText(tag)) {
            return "";
        }
        int idx = tag.indexOf(':');
        return idx >= 0 ? tag.substring(idx + 1) : tag;
    }
}
