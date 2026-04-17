package com.ourexists.omes.ai.shared.service;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.ai.config.AiInspectionProperties;
import com.ourexists.omes.ai.model.AiKnowledgeIngestRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Service
public class PortalKnowledgeProxyService {

    private final RestTemplate restTemplate;
    private final AiInspectionProperties properties;

    public PortalKnowledgeProxyService(RestTemplate restTemplate, AiInspectionProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public boolean enabled() {
        return properties.isAgentPortalProxyEnabled();
    }

    public String ask(String question, Integer topK) {
        String url = trimEndSlash(properties.getPortalBaseUrl()) + "/inspection/ai/knowledge/ask";
        Map<String, Object> payload = new HashMap<>();
        payload.put("question", question);
        payload.put("topK", topK);
        Map<String, Object> data = callPortal(url, payload);
        Object answer = data.get("answer");
        return answer == null ? "" : String.valueOf(answer);
    }

    public Map<String, Object> ingest(AiKnowledgeIngestRequest request) {
        String url = trimEndSlash(properties.getPortalBaseUrl()) + "/inspection/ai/knowledge/ingest";
        Map<String, Object> payload = new HashMap<>();
        payload.put("knowledgeType", request.getKnowledgeType());
        payload.put("sourceName", request.getSourceName());
        payload.put("textContent", request.getTextContent());
        payload.put("structuredContent", request.getStructuredContent());
        return callPortal(url, payload);
    }

    private Map<String, Object> callPortal(String url, Map<String, Object> payload) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, buildForwardHeaders());
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        if (response == null) {
            throw new BusinessException("portal调用失败: empty response");
        }
        Object code = response.get("code");
        if (!(code instanceof Number) || ((Number) code).intValue() != 200) {
            throw new BusinessException(String.valueOf(response.getOrDefault("msg", "portal接口调用失败")));
        }
        Object data = response.get("data");
        if (!(data instanceof Map<?, ?> raw)) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
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
