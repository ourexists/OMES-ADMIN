package com.ourexists.omes.portal.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.util.StringUtils;

import java.util.Map;

public class JSONUtils {

    public static Object getByPath(Object root, String path) {
        if (root == null || !StringUtils.hasText(path)) return null;
        String trimmed = path.trim();
        if (!trimmed.contains(".") && !trimmed.contains("[")) return getDirect(root, trimmed);
        String[] parts = trimmed.split("\\.", -1);
        Object current = root;
        for (String part : parts) {
            if (current == null) return null;
            part = part.trim();
            if (part.isEmpty()) continue;
            int bracket = part.indexOf('[');
            if (bracket >= 0) {
                String key = bracket == 0 ? "" : part.substring(0, bracket);
                if (key.length() > 0) current = getDirect(current, key);
                int end = part.indexOf(']', bracket);
                while (bracket >= 0 && end >= 0) {
                    try {
                        int idx = Integer.parseInt(part.substring(bracket + 1, end).trim());
                        if (current instanceof JSONArray) current = ((JSONArray) current).get(idx);
                        else return null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    bracket = part.indexOf('[', end + 1);
                    end = bracket >= 0 ? part.indexOf(']', bracket) : -1;
                }
            } else {
                current = getDirect(current, part);
            }
        }
        return current;
    }

    public static Object getDirect(Object obj, String key) {
        if (obj == null || key == null) return null;
        if (obj instanceof JSONObject) return ((JSONObject) obj).get(key);
        if (obj instanceof Map) return ((Map<?, ?>) obj).get(key);
        return null;
    }

    public static String getStringByPath(Object root, String path) {
        Object v = getByPath(root, path);
        return v != null ? v.toString() : null;
    }
}
