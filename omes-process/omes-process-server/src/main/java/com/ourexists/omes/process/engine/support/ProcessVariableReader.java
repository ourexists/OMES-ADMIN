package com.ourexists.omes.process.engine.support;

import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.spi.ProcessSignalProvider;
import org.springframework.util.StringUtils;

/**
 * 从信号源读取过程变量（温度、压力、设定值等）。
 */
public final class ProcessVariableReader {

    private ProcessVariableReader() {
    }

    public static double readProcessVariable(ProcessExecutionContext context, String variable) {
        if (!StringUtils.hasText(variable)) {
            variable = "temp";
        }
        String normalized = variable.trim().toLowerCase();
        String equipmentCode = context.attr("activeEquipmentCode");
        ProcessSignalProvider provider = context.getSignalProvider();
        return switch (normalized) {
            case "temp", "temperature" -> readTemperature(provider, equipmentCode);
            case "setpoint", "sp" -> context.getCommandedSetpoint();
            case "pressure" -> readPressure(provider, equipmentCode);
            default -> throw new IllegalArgumentException("未知过程变量: " + variable);
        };
    }

    public static double readProcessVariable(ProcessExecutionContext context,
                                             String equipmentCode,
                                             String variable) {
        String previous = context.attr("activeEquipmentCode");
        if (StringUtils.hasText(equipmentCode)) {
            context.putAttr("activeEquipmentCode", equipmentCode.trim());
        }
        try {
            return readProcessVariable(context, variable);
        } finally {
            context.putAttr("activeEquipmentCode", previous);
        }
    }

    private static double readTemperature(ProcessSignalProvider provider, String equipmentCode) {
        if (StringUtils.hasText(equipmentCode)) {
            return provider.readTemperature(equipmentCode.trim());
        }
        return provider.readTemperature();
    }

    private static double readPressure(ProcessSignalProvider provider, String equipmentCode) {
        if (StringUtils.hasText(equipmentCode)) {
            return provider.readPressure(equipmentCode.trim());
        }
        return provider.readPressure();
    }
}
