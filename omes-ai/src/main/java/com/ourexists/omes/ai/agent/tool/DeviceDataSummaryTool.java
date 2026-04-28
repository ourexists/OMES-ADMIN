package com.ourexists.omes.ai.agent.tool;

import com.ourexists.omes.ai.agent.model.AgentToolResult;
import com.ourexists.omes.ai.shared.service.PortalDeviceDataProxyService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DeviceDataSummaryTool implements AgentTool {

    private final PortalDeviceDataProxyService portalDeviceDataProxyService;

    public DeviceDataSummaryTool(PortalDeviceDataProxyService portalDeviceDataProxyService) {
        this.portalDeviceDataProxyService = portalDeviceDataProxyService;
    }

    @Override
    public String name() {
        return "device.data.summary";
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
        return query(
                toString(arguments.get("queryText")),
                toString(arguments.get("equipName")),
                toString(arguments.get("equipSn")),
                toInteger(arguments.get("days")),
                toInteger(arguments.get("pageSize"))
        );
    }

    @Tool("查询设备与点检数据汇总，返回设备数量、在线/告警数量及最近点检记录摘要")
    public AgentToolResult query(
            @P("用户原始问题，用于提取设备关键字") String queryText,
            @P("设备名称，可选") String equipName,
            @P("设备编码，可选") String equipSn,
            @P("统计天数，范围1-30，默认7") Integer days,
            @P("分页大小，范围1-50，默认10") Integer pageSize
    ) {
        Map<String, Object> filters = new HashMap<>();
        if (equipName != null && !equipName.isBlank()) {
            filters.put("equipName", equipName.trim());
        }
        if (equipSn != null && !equipSn.isBlank()) {
            filters.put("equipSn", equipSn.trim());
        }
        filters.put("days", normalize(days, 7, 1, 30));
        filters.put("pageSize", normalize(pageSize, 10, 1, 50));

        String summary = portalDeviceDataProxyService.summarizeDevice(queryText, filters);
        Map<String, Object> data = new HashMap<>();
        data.put("queryText", queryText);
        data.put("equipName", equipName);
        data.put("equipSn", equipSn);
        data.put("days", filters.get("days"));
        data.put("pageSize", filters.get("pageSize"));
        data.put("summary", summary);

        AgentToolResult result = new AgentToolResult();
        result.setToolName(name());
        result.setSuccess(true);
        result.setMessage("设备数据汇总完成");
        result.setData(data);
        return result;
    }

    private int normalize(Integer value, int defaultValue, int min, int max) {
        int actual = value == null ? defaultValue : value;
        return Math.max(min, Math.min(max, actual));
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
