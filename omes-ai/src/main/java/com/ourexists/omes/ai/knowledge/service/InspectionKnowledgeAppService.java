package com.ourexists.omes.ai.knowledge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.ai.config.AgentProperties;
import com.ourexists.omes.ai.knowledge.model.InspectionReportRequest;
import com.ourexists.omes.ai.knowledge.model.KnowledgeIngestRequest;
import com.ourexists.omes.ai.knowledge.model.InspectRecordDto;
import com.ourexists.omes.ai.knowledge.model.InspectRecordItemDto;
import com.ourexists.omes.ai.shared.service.InspectionRecordQueryService;
import com.ourexists.omes.ai.shared.service.MultiProviderLlmService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class InspectionKnowledgeAppService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AgentProperties properties;
    private final InspectionRecordQueryService recordQueryService;
    private final InspectionKnowledgeService knowledgeStoreService;
    private final MultiProviderLlmService llmService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InspectionKnowledgeAppService(AgentProperties properties,
                                         InspectionRecordQueryService recordQueryService,
                                         InspectionKnowledgeService knowledgeStoreService,
                                         MultiProviderLlmService llmService) {
        this.properties = properties;
        this.recordQueryService = recordQueryService;
        this.knowledgeStoreService = knowledgeStoreService;
        this.llmService = llmService;
    }

    public int rebuildKnowledgeBase(Integer limit) {
        int actualLimit = limit == null || limit <= 0 ? 50 : limit;
        InspectionReportRequest request = new InspectionReportRequest();
        request.setLimit(actualLimit);
        request.setDays(7);
        request.setIncludeOnlyAbnormal(false);
        List<InspectRecordDto> records = recordQueryService.listRecords(request);
        List<String> docs = toKnowledgeDocuments(records);
        knowledgeStoreService.replaceKnowledge(docs);
        return docs.size();
    }

    public int ingestKnowledge(KnowledgeIngestRequest request) {
        List<String> docs = toKnowledgeDocuments(request);
        return knowledgeStoreService.addKnowledge(docs);
    }

    public String askKnowledge(String question, Integer topK) {
        int actualTopK = topK == null || topK <= 0 ? properties.getTopK() : topK;
        List<String> contexts = searchKnowledge(question, actualTopK);
        if (contexts.isEmpty()) {
            return "知识库暂无可匹配案例，请先执行“重建知识库”或扩大巡检记录采集范围。";
        }
        String contextText = String.join("\n---\n", contexts);
        String prompt = "你是制造业运维专家。结合以下巡检案例知识回答问题，要求步骤清晰、可执行。\n\n问题："
                + question + "\n\n知识上下文：\n" + contextText;
        return llmService.ask(prompt);
    }

    public List<String> searchKnowledge(String question, Integer topK) {
        int actualTopK = topK == null || topK <= 0 ? properties.getTopK() : topK;
        return knowledgeStoreService.searchKnowledge(question, actualTopK);
    }

    public String providerSummary() {
        return knowledgeStoreService.getProviderSummary();
    }

    private List<String> toKnowledgeDocuments(List<InspectRecordDto> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> docs = new ArrayList<>();
        for (InspectRecordDto record : records) {
            StringBuilder text = new StringBuilder();
            text.append("设备: ").append(record.getEquipName()).append(" (").append(record.getEquipSelfCode()).append(")\n")
                    .append("巡检时间: ").append(formatTime(record.getRecordTime())).append('\n')
                    .append("分值: ").append(record.getScore()).append('\n');
            if (record.getItems() != null) {
                for (InspectRecordItemDto item : record.getItems()) {
                    if (item == null) {
                        continue;
                    }
                    text.append("- 项目: ").append(item.getItemName())
                            .append(", 结果: ").append(item.getResultDesc())
                            .append(", 备注: ").append(item.getRemark())
                            .append('\n');
                }
            }
            docs.add(text.toString());
        }
        return docs;
    }

    private List<String> toKnowledgeDocuments(KnowledgeIngestRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }
        String knowledgeType = request.getKnowledgeType() == null ? "unstructured" : request.getKnowledgeType().trim().toLowerCase();
        if ("structured".equals(knowledgeType)) {
            return buildStructuredDocuments(request.getSourceName(), request.getStructuredContent());
        }
        return buildUnstructuredDocuments(request.getSourceName(), request.getTextContent());
    }

    private List<String> buildStructuredDocuments(String sourceName, String structuredContent) {
        if (structuredContent == null || structuredContent.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> rows;
            if (structuredContent.trim().startsWith("[")) {
                rows = objectMapper.readValue(structuredContent, new TypeReference<List<Map<String, Object>>>() {
                });
            } else {
                Map<String, Object> one = objectMapper.readValue(structuredContent, new TypeReference<Map<String, Object>>() {
                });
                rows = List.of(one);
            }
            List<String> docs = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                StringBuilder text = new StringBuilder();
                text.append("来源: ").append(sourceName == null ? "structured" : sourceName).append('\n');
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    text.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
                }
                docs.add(text.toString());
            }
            return docs;
        } catch (Exception ex) {
            throw new BusinessException("结构化知识解析失败，请确认是合法JSON对象或JSON数组");
        }
    }

    private List<String> buildUnstructuredDocuments(String sourceName, String textContent) {
        if (textContent == null || textContent.isBlank()) {
            return Collections.emptyList();
        }
        String[] chunks = textContent.split("\\n\\s*\\n");
        List<String> docs = new ArrayList<>();
        int idx = 1;
        for (String chunk : chunks) {
            String content = chunk == null ? "" : chunk.trim();
            if (content.isEmpty()) {
                continue;
            }
            String text = "来源: " + (sourceName == null ? "unstructured" : sourceName) + "\n段落序号: " + idx + "\n" + content;
            docs.add(text);
            idx++;
        }
        return docs;
    }

    private String formatTime(Date date) {
        if (date == null) {
            return "-";
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return TIME_FORMATTER.format(localDateTime);
    }
}
