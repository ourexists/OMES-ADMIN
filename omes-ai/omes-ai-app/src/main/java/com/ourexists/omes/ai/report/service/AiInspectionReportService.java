package com.ourexists.omes.ai.report.service;

import com.ourexists.omes.ai.config.AiInspectionProperties;
import com.ourexists.omes.ai.knowledge.service.AiInspectionKnowledgeService;
import com.ourexists.omes.ai.model.AiInspectionReportRequest;
import com.ourexists.omes.ai.model.AiInspectionReportResponse;
import com.ourexists.omes.ai.model.inspection.InspectRecordDto;
import com.ourexists.omes.ai.shared.service.AiLlmService;
import com.ourexists.omes.ai.shared.service.InspectionRecordQueryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class AiInspectionReportService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AiInspectionProperties properties;
    private final InspectionRecordQueryService recordQueryService;
    private final AiInspectionKnowledgeService knowledgeService;
    private final AiLlmService llmService;

    public AiInspectionReportService(AiInspectionProperties properties,
                                     InspectionRecordQueryService recordQueryService,
                                     AiInspectionKnowledgeService knowledgeService,
                                     AiLlmService llmService) {
        this.properties = properties;
        this.recordQueryService = recordQueryService;
        this.knowledgeService = knowledgeService;
        this.llmService = llmService;
    }

    public AiInspectionReportResponse generateReport(AiInspectionReportRequest request) {
        List<InspectRecordDto> records = recordQueryService.listRecords(request);
        List<InspectRecordDto> filtered = recordQueryService.filterAbnormal(records, request.getIncludeOnlyAbnormal());
        int abnormalCount = recordQueryService.countAbnormal(filtered);
        List<String> retrievedKnowledge = knowledgeService.searchKnowledge("巡检异常原因与维修建议", properties.getTopK());
        String prompt = buildReportPrompt(filtered, abnormalCount, retrievedKnowledge);
        String report = llmService.ask(prompt);

        AiInspectionReportResponse response = new AiInspectionReportResponse();
        response.setRecordCount(filtered.size());
        response.setAbnormalCount(abnormalCount);
        response.setReport(report);
        response.setModelUsed(llmService.modelSummary());
        response.setRetrievedKnowledge(retrievedKnowledge);
        return response;
    }

    private String buildReportPrompt(List<InspectRecordDto> records, int abnormalCount, List<String> contexts) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是工业设备智能巡检助手，请根据输入生成巡检日报。\n")
                .append("输出结构必须包含：1.总体状态 2.故障解释 3.运维建议 4.优先级清单。\n")
                .append("如果数据不足，明确指出需要补充的数据。\n\n");
        sb.append("巡检记录总数: ").append(records.size()).append('\n');
        sb.append("异常记录数: ").append(abnormalCount).append('\n');
        sb.append("巡检记录摘要:\n").append(toRecordSummary(records)).append("\n\n");
        if (contexts != null && !contexts.isEmpty()) {
            sb.append("RAG知识库参考:\n");
            for (String context : contexts) {
                sb.append("- ").append(context).append('\n');
            }
        }
        return sb.toString();
    }

    private String toRecordSummary(List<InspectRecordDto> records) {
        if (records == null || records.isEmpty()) {
            return "暂无巡检记录。";
        }
        StringBuilder sb = new StringBuilder();
        int maxRows = Math.min(records.size(), 20);
        for (int i = 0; i < maxRows; i++) {
            InspectRecordDto row = records.get(i);
            String time = formatTime(row.getRecordTime());
            String equip = row.getEquipName() == null || row.getEquipName().isBlank() ? row.getEquipSelfCode() : row.getEquipName();
            long abnormalItems = row.getItems() == null ? 0 : row.getItems().stream().filter(it -> it != null && it.getResult() != null && it.getResult() == 1).count();
            sb.append(i + 1).append(") [").append(time).append("] ")
                    .append(equip == null ? "未知设备" : equip)
                    .append("，分值=").append(row.getScore() == null ? "-" : row.getScore())
                    .append("，异常项=").append(abnormalItems)
                    .append('\n');
        }
        if (records.size() > maxRows) {
            sb.append("... 其余 ").append(records.size() - maxRows).append(" 条省略\n");
        }
        return sb.toString();
    }

    private String formatTime(Date date) {
        if (date == null) {
            return "-";
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return TIME_FORMATTER.format(localDateTime);
    }
}
