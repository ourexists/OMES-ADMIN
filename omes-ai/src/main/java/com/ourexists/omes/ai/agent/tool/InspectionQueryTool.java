package com.ourexists.omes.ai.agent.tool;

import com.ourexists.omes.ai.knowledge.model.InspectionReportRequest;
import com.ourexists.omes.ai.agent.model.AgentToolResult;
import com.ourexists.omes.ai.knowledge.model.InspectRecordDto;
import com.ourexists.omes.ai.shared.service.InspectionRecordQueryService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InspectionQueryTool implements AgentTool {

    private final InspectionRecordQueryService queryService;

    public InspectionQueryTool(InspectionRecordQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public String name() {
        return "inspection.query";
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
        return query(
                toInteger(arguments.get("days")),
                toInteger(arguments.get("limit")),
                toString(arguments.get("taskId")),
                toString(arguments.get("equipName")),
                toBoolean(arguments.get("includeOnlyAbnormal"))
        );
    }

    @Tool("查询巡检记录并统计异常数量")
    public AgentToolResult query(
            @P("查询天数，默认 7") Integer days,
            @P("返回记录上限") Integer limit,
            @P("任务 ID，可选") String taskId,
            @P("设备名称，可选") String equipName,
            @P("是否仅返回异常记录") Boolean includeOnlyAbnormal
    ) {
        InspectionReportRequest request = new InspectionReportRequest();
        request.setDays(days);
        request.setLimit(limit);
        request.setTaskId(taskId);
        request.setEquipName(equipName);
        request.setIncludeOnlyAbnormal(includeOnlyAbnormal);

        List<InspectRecordDto> records = queryService.listRecords(request);
        List<InspectRecordDto> filtered = queryService.filterAbnormal(records, request.getIncludeOnlyAbnormal());

        Map<String, Object> data = new HashMap<>();
        data.put("recordCount", filtered.size());
        data.put("abnormalCount", queryService.countAbnormal(filtered));
        data.put("taskId", request.getTaskId());
        data.put("equipName", request.getEquipName());

        AgentToolResult result = new AgentToolResult();
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
