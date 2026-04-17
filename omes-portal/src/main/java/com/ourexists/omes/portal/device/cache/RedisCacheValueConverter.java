package com.ourexists.omes.portal.device.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheValueConverter {

    private final ObjectMapper objectMapper;

    public RedisCacheValueConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T convert(Object raw, Class<T> targetType) {
        if (raw == null) {
            return null;
        }
        if (targetType.isInstance(raw)) {
            return targetType.cast(raw);
        }
        return objectMapper.convertValue(raw, targetType);
    }
}
