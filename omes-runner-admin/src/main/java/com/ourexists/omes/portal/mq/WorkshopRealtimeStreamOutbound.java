package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * 场景实时数据经 {@link StreamBridge} 发往 Spring Cloud Stream 出站 binding（与 {@link EquipRealtimeStreamOutbound} 同源模式）。
 */
@Slf4j
@Component
public class WorkshopRealtimeStreamOutbound {

    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    public static final String WORKSHOP_REALTIME_OUT_BINDING = "workshopRealtime-out-0";

    public WorkshopRealtimeStreamOutbound(StreamBridge streamBridge,
                                          ObjectMapper objectMapper) {
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }

    public void send(WorkshopRealtime realtime) {
        if (realtime == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(realtime);
            streamBridge.send(WORKSHOP_REALTIME_OUT_BINDING, json);
        } catch (JsonProcessingException e) {
            log.error("Serialize workshop realtime for stream outbound failed, workshopId={}", realtime.getId(), e);
            throw new IllegalStateException("workshop realtime JSON serialization failed", e);
        }
    }
}
