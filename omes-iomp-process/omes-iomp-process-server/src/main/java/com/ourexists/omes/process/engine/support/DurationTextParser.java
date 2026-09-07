package com.ourexists.omes.process.engine.support;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationTextParser {

    private static final Pattern DURATION = Pattern.compile(
            "([\\d.]+)\\s*(h|hr|hrs|hour|hours|min|mins|minute|minutes|s|sec|secs|second|seconds)?",
            Pattern.CASE_INSENSITIVE);

    private DurationTextParser() {
    }

    public static long parseToMillis(String text) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("时长不能为空");
        }
        Matcher matcher = DURATION.matcher(text.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("无法解析时长: " + text);
        }
        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2);
        if (unit == null || unit.isBlank()) {
            return Math.round(value * 1000D);
        }
        String normalized = unit.toLowerCase();
        if (normalized.startsWith("h")) {
            return Math.round(value * 3_600_000D);
        }
        if (normalized.startsWith("min")) {
            return Math.round(value * 60_000D);
        }
        return Math.round(value * 1000D);
    }

    /** 解析为秒（整数），支持数字或 1h / 30min / 90s 等文本 */
    public static int parseToSeconds(Object raw) {
        if (raw == null) {
            throw new IllegalArgumentException("时长不能为空");
        }
        if (raw instanceof Number number) {
            int sec = number.intValue();
            if (sec <= 0) {
                throw new IllegalArgumentException("时长须大于 0 秒");
            }
            return sec;
        }
        long ms = parseToMillis(String.valueOf(raw).trim());
        int sec = (int) (ms / 1000L);
        if (sec <= 0) {
            throw new IllegalArgumentException("时长须大于 0 秒: " + raw);
        }
        return sec;
    }
}
