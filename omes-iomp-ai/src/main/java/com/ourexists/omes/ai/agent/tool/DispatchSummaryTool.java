package com.ourexists.omes.ai.agent.tool;

import com.ourexists.omes.ai.shared.service.PortalDispatchProxyService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DispatchSummaryTool {

    private final PortalDispatchProxyService portalDispatchProxyService;

    public DispatchSummaryTool(PortalDispatchProxyService portalDispatchProxyService) {
        this.portalDispatchProxyService = portalDispatchProxyService;
    }

    @Tool("查询调度汇总，返回MPS/MO规模和调度建议")
    public String queryDispatchSummary(
            @P("用户问题原文") String queryText,
            @P("分页大小，默认10，范围1-50") Integer pageSize
    ) {
        int size = pageSize == null ? 10 : Math.max(1, Math.min(50, pageSize));
        Map<String, Object> filters = new HashMap<>();
        filters.put("pageSize", size);
        return portalDispatchProxyService.summarizeDispatch(queryText, filters);
    }
}
