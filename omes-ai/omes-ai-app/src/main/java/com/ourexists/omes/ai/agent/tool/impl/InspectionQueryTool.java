package com.ourexists.omes.ai.agent.tool.impl;

import com.ourexists.omes.ai.agent.tool.AiAgentTool;
import com.ourexists.omes.ai.model.AiInspectionReportRequest;
import com.ourexists.omes.ai.model.agent.AiAgentToolResult;
import com.ourexists.omes.ai.model.inspection.InspectRecordDto;
import com.ourexists.omes.ai.shared.service.InspectionRecordQueryService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InspectionQueryTool implements AiAgentTool {

    private final InspectionRecordQueryService queryService;

    public InspectionQueryTool(InspectionRecordQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public String name() {
        return "inspection.query";
    }

    @Override
    public AiAgentToolResult execute(Map<String, Object> arguments) {
        AiInspectionReportRequest request = new AiInspectionReportRequest();
        request.setDays(toInteger(arguments.get("days")));
        request.setLimit(toInteger(arguments.get("limit")));
        request.setTaskId(toString(arguments.get("taskId")));
        request.setEquipName(toString(arguments.get("equipName")));
        request.setIncludeOnlyAbnormal(toBoolean(arguments.get("includeOnlyAbnormal")));

        List<InspectRecordDto> records = queryService.listRecords(request);
        List<InspectRecordDto> filtered = queryService.filterAbnormal(records, request.getIncludeOnlyAbnormal());

        Map<String, Object> data = new HashMap<>();
        data.put("recordCount", filtered.size());
        data.put("abnormalCount", queryService.countAbnormal(filtered));
        data.put("taskId", request.getTaskId());
        data.put("equipName", request.getEquipName());

        AiAgentToolResult result = new AiAgentToolResult();
        result.setToolName(name());
        result.setSuccess(true);
        result.setMessage("巡检记录查询完成");
        result.setData(data);
        return result;
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
