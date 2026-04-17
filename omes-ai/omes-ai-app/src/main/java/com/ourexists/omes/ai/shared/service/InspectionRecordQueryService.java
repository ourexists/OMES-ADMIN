package com.ourexists.omes.ai.shared.service;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.ai.config.AiInspectionProperties;
import com.ourexists.omes.ai.model.AiInspectionReportRequest;
import com.ourexists.omes.ai.model.inspection.InspectRecordDto;
import com.ourexists.omes.ai.model.inspection.InspectRecordItemDto;
import com.ourexists.omes.ai.model.inspection.InspectRecordPageQuery;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InspectionRecordQueryService {

    private final AiInspectionProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public InspectionRecordQueryService(AiInspectionProperties properties,
                                        RestTemplate restTemplate,
                                        ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<InspectRecordDto> listRecords(AiInspectionReportRequest request) {
        int days = request.getDays() == null || request.getDays() <= 0 ? properties.getDefaultDays() : request.getDays();
        int limit = request.getLimit() == null || request.getLimit() <= 0 ? properties.getDefaultLimit() : request.getLimit();
        Date start = Date.from(Instant.now().minusSeconds(days * 24L * 3600L));
        InspectRecordPageQuery query = new InspectRecordPageQuery();
        query.setRecordTimeStart(start);
        query.setTaskId(request.getTaskId());
        query.setEquipName(request.getEquipName());
        query.setPage(1);
        query.setPageSize(limit);
        return selectByPageViaPortal(query);
    }

    public List<InspectRecordDto> filterAbnormal(List<InspectRecordDto> records, Boolean onlyAbnormal) {
        if (!Boolean.TRUE.equals(onlyAbnormal)) {
            return records;
        }
        return records.stream().filter(this::isAbnormal).collect(Collectors.toList());
    }

    public int countAbnormal(List<InspectRecordDto> records) {
        int count = 0;
        for (InspectRecordDto item : records) {
            if (isAbnormal(item)) {
                count++;
            }
        }
        return count;
    }

    public boolean isAbnormal(InspectRecordDto record) {
        if (record == null || record.getItems() == null) {
            return false;
        }
        for (InspectRecordItemDto item : record.getItems()) {
            if (item != null && Objects.equals(item.getResult(), 1)) {
                return true;
            }
        }
        return false;
    }

    private List<InspectRecordDto> selectByPageViaPortal(InspectRecordPageQuery query) {
        String baseUrl = trimEndSlash(properties.getPortalBaseUrl());
        String url = baseUrl + "/inspection/record/selectByPage";
        Map<String, Object> payload = objectMapper.convertValue(query, new TypeReference<Map<String, Object>>() {
        });
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, buildForwardHeaders());
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        if (response == null) {
            return Collections.emptyList();
        }
        Object code = response.get("code");
        if (!(code instanceof Number) || ((Number) code).intValue() != 200) {
            throw new BusinessException(String.valueOf(response.getOrDefault("msg", "portal接口调用失败")));
        }
        Object data = response.get("data");
        if (!(data instanceof List<?> list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(item -> objectMapper.convertValue(item, InspectRecordDto.class))
                .collect(Collectors.toList());
    }

    private HttpHeaders buildForwardHeaders() {
        HttpHeaders headers = new HttpHeaders();
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return headers;
        }
        HttpServletRequest request = attrs.getRequest();
        copyHeaderIfPresent(request, headers, "Authorization");
        copyHeaderIfPresent(request, headers, "x-era-platform");
        copyHeaderIfPresent(request, headers, "x-route-tenant");
        return headers;
    }

    private void copyHeaderIfPresent(HttpServletRequest request, HttpHeaders headers, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.trim().isEmpty()) {
            headers.add(name, value);
        }
    }

    private String trimEndSlash(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
