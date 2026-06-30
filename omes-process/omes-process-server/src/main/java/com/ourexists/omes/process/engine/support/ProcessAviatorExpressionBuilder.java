package com.ourexists.omes.process.engine.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.model.ProcessConditionSpec;
import org.springframework.util.StringUtils;

/**
 * 将驱动/异常条件编译为 Aviator 表达式（运行期配合 {@link ProcessAviatorEvaluator} 求值）。
 */
public final class ProcessAviatorExpressionBuilder {

    private ProcessAviatorExpressionBuilder() {
    }

    public static String buildEventExpression(ProcessConditionSpec spec, JsonNode sourceJson) {
        if (sourceJson != null && sourceJson.has("eventLogic") && !sourceJson.get("eventLogic").isNull()) {
            return compileEventLogic(sourceJson.get("eventLogic"));
        }
        if (StringUtils.hasText(spec.getCondition())) {
            return compileLegacyCondition(spec.getCondition().trim());
        }
        throw new BusinessException("事件条件缺少 eventLogic 或 condition 表达式");
    }

    public static String compileEventLogic(JsonNode logic) {
        if (logic == null || logic.isNull()) {
            throw new BusinessException("eventLogic 为空");
        }
        if (logic.has("conditions") && logic.get("conditions").isArray()) {
            StringBuilder joined = new StringBuilder();
            for (JsonNode item : logic.get("conditions")) {
                String part = compileEventItem(item);
                if (!StringUtils.hasText(part)) {
                    throw new BusinessException("eventLogic.conditions 存在无效项");
                }
                if (!joined.isEmpty()) {
                    joined.append(" && ");
                }
                joined.append(part);
            }
            if (joined.isEmpty()) {
                throw new BusinessException("eventLogic.conditions 为空");
            }
            return joined.toString();
        }
        return compileEventItem(logic);
    }

    private static String compileEventItem(JsonNode item) {
        if (item == null || item.isNull() || !item.has("equipmentCode")) {
            return "";
        }
        String equipment = escapeString(item.get("equipmentCode").asText(""));
        if (!StringUtils.hasText(equipment)) {
            return "";
        }
        String variable = item.has("variable")
                ? item.get("variable").asText("temp")
                : "temp";
        String varExpr = "pv('" + equipment + "','" + escapeString(variable) + "')";
        String logicType = item.has("logicType") ? item.get("logicType").asText("") : "";
        if ("RANGE".equalsIgnoreCase(logicType)) {
            if (!item.has("min") || !item.has("max")) {
                throw new BusinessException("RANGE 事件须配置 min、max");
            }
            double min = item.get("min").asDouble();
            double max = item.get("max").asDouble();
            return varExpr + " >= " + min + " && " + varExpr + " <= " + max;
        }
        if (!item.has("operator") || !item.has("value")) {
            throw new BusinessException("事件条件须配置 operator 与 value");
        }
        String operator = item.get("operator").asText(">=");
        double value = item.get("value").asDouble();
        return varExpr + " " + normalizeOperator(operator) + " " + value;
    }

    /**
     * 将前端 {@code @设备:变量>=值} 文本转为 Aviator 表达式。
     */
    public static String compileLegacyCondition(String condition) {
        String[] parts = condition.split("&&");
        StringBuilder joined = new StringBuilder();
        for (String raw : parts) {
            String part = compileLegacyPart(raw.trim());
            if (!StringUtils.hasText(part)) {
                throw new BusinessException("无法解析事件条件片段: " + raw);
            }
            if (!joined.isEmpty()) {
                joined.append(" && ");
            }
            joined.append(part);
        }
        return joined.toString();
    }

    private static String compileLegacyPart(String part) {
        if (!part.startsWith("@")) {
            throw new BusinessException("事件条件须以 @设备: 开头: " + part);
        }
        int colon = part.indexOf(':');
        if (colon <= 1) {
            throw new BusinessException("事件条件设备前缀无效: " + part);
        }
        String equipment = escapeString(part.substring(1, colon).trim());
        String rest = part.substring(colon + 1).trim();
        for (String operator : new String[]{">=", "<=", "==", ">", "<"}) {
            int idx = rest.indexOf(operator);
            if (idx > 0) {
                String variable = escapeString(rest.substring(0, idx).trim());
                double value = Double.parseDouble(rest.substring(idx + operator.length()).trim());
                return "pv('" + equipment + "','" + variable + "') "
                        + normalizeOperator(operator) + " " + value;
            }
        }
        throw new BusinessException("无法解析比较运算符: " + part);
    }

    private static String normalizeOperator(String operator) {
        return switch (operator) {
            case ">=" -> ">=";
            case "<=" -> "<=";
            case ">" -> ">";
            case "<" -> "<";
            case "==" -> "==";
            default -> throw new BusinessException("不支持的比较运算符: " + operator);
        };
    }

    private static String escapeString(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
