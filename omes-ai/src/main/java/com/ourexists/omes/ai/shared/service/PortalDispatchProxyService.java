package com.ourexists.omes.ai.shared.service;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.ai.config.AgentProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortalDispatchProxyService {

    private final RestTemplate restTemplate;
    private final AgentProperties properties;

    public PortalDispatchProxyService(RestTemplate restTemplate, AgentProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String summarizeDispatch(String queryText, Map<String, Object> filters) {
        String keyword = firstNonBlank(
                asText(filters, "productName"),
                normalizeKeyword(queryText)
        );
        int pageSize = clamp(asInt(filters, "pageSize", 10), 1, 50);
        List<?> mpsList = callList("/mps/selectByPage", buildMpsQuery(keyword, pageSize));
        List<?> moList = callList("/mo/selectByPage", buildMoQuery(keyword, pageSize));
        return "调度视图汇总：MPS计划 " + mpsList.size() + " 条，MO工单 " + moList.size()
                + " 条。可继续执行插队(jumpQueue)、入队(joinQueue)、调优优先级(changePriority)。";
    }

    private Map<String, Object> buildMpsQuery(String keyword, int pageSize) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("page", 1);
        payload.put("pageSize", pageSize);
        payload.put("queryMO", true);
        if (!keyword.isBlank()) {
            payload.put("moCode", keyword);
            payload.put("productName", keyword);
        }
        return payload;
    }

    private Map<String, Object> buildMoQuery(String keyword, int pageSize) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("page", 1);
        payload.put("pageSize", pageSize);
        payload.put("queryDetail", false);
        if (!keyword.isBlank()) {
            payload.put("selfCode", keyword);
            payload.put("productName", keyword);
        }
        return payload;
    }

    private List<?> callList(String path, Map<String, Object> payload) {
        String url = trimEndSlash(properties.getPortalBaseUrl()) + path;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, buildForwardHeaders());
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        if (response == null) {
            return List.of();
        }
        Object code = response.get("code");
        if (!(code instanceof Number) || ((Number) code).intValue() != 200) {
            throw new BusinessException(String.valueOf(response.getOrDefault("msg", "portal接口调用失败")));
        }
        Object data = response.get("data");
        if (!(data instanceof List<?> list)) {
            return List.of();
        }
        return new ArrayList<>(list);
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
        copyHeaderIfPresent(request, headers, "token");
        copyHeaderIfPresent(request, headers, "x-token");
        copyHeaderIfPresent(request, headers, "x-access-token");
        copyHeaderIfPresent(request, headers, "access-token");
        copyHeaderIfPresent(request, headers, "era-token");
        copyHeaderIfPresent(request, headers, "x-era-token");
        copyXHeaders(request, headers);
        return headers;
    }

    private void copyHeaderIfPresent(HttpServletRequest request, HttpHeaders headers, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.trim().isEmpty()) {
            headers.add(name, value);
        }
    }

    private void copyXHeaders(HttpServletRequest request, HttpHeaders headers) {
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return;
        }
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name != null && name.toLowerCase().startsWith("x-") && !headers.containsKey(name)) {
                copyHeaderIfPresent(request, headers, name);
            }
        }
    }

    private String normalizeKeyword(String queryText) {
        if (queryText == null) {
            return "";
        }
        String compact = queryText.replaceAll("\\s+", " ").trim();
        if (compact.length() <= 32) {
            return compact;
        }
        return compact.substring(0, 32);
    }

    private String asText(Map<String, Object> filters, String key) {
        if (filters == null || key == null) {
            return "";
        }
        Object value = filters.get(key);
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        return text;
    }

    private int asInt(Map<String, Object> filters, String key, int defaultValue) {
        if (filters == null || key == null) {
            return defaultValue;
        }
        Object value = filters.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
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
