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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortalDeviceDataProxyService {

    private final RestTemplate restTemplate;
    private final AgentProperties properties;

    public PortalDeviceDataProxyService(RestTemplate restTemplate, AgentProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String summarizeDevice(String queryText, Map<String, Object> filters) {
        String keyword = firstNonBlank(
                asText(filters, "equipName"),
                asText(filters, "equipSn"),
                normalizeKeyword(queryText)
        );
        int pageSize = clamp(asInt(filters, "pageSize", 10), 1, 50);
        int days = clamp(asInt(filters, "days", 7), 1, 30);
        List<?> equipList = callList("/equip/selectByPage", buildEquipQuery(keyword, pageSize));
        Map<String, Object> realtime = callMap("/equip/countRealtime", buildEquipQuery(keyword, pageSize));
        List<?> inspectRecords = callList("/inspection/record/selectByPage", buildInspectRecordQuery(keyword, pageSize, days));
        Object online = realtime.getOrDefault("onlineNum", 0);
        Object alarm = realtime.getOrDefault("alarmNum", 0);
        return "设备数据汇总：设备 " + equipList.size() + " 台，在线 " + online + " 台，告警 " + alarm
                + " 台；点检记录 " + inspectRecords.size() + " 条。建议优先排查告警设备并核对最近点检项。";
    }

    private Map<String, Object> buildEquipQuery(String keyword, int pageSize) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("page", 1);
        payload.put("pageSize", pageSize);
        payload.put("needRealtime", true);
        if (!keyword.isBlank()) {
            payload.put("name", keyword);
            payload.put("selfCode", keyword);
        }
        return payload;
    }

    private Map<String, Object> buildInspectRecordQuery(String keyword, int pageSize, int days) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("page", 1);
        payload.put("pageSize", pageSize);
        payload.put("days", days);
        if (!keyword.isBlank()) {
            payload.put("equipName", keyword);
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
        return list;
    }

    private Map<String, Object> callMap(String path, Map<String, Object> payload) {
        String url = trimEndSlash(properties.getPortalBaseUrl()) + path;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, buildForwardHeaders());
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        if (response == null) {
            return Map.of();
        }
        Object code = response.get("code");
        if (!(code instanceof Number) || ((Number) code).intValue() != 200) {
            throw new BusinessException(String.valueOf(response.getOrDefault("msg", "portal接口调用失败")));
        }
        Object data = response.get("data");
        if (!(data instanceof Map<?, ?> raw)) {
            return Map.of();
        }
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            map.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return map;
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
        return String.valueOf(value).trim();
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
