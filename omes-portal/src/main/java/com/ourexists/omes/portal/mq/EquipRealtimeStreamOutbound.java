package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * 设备实时数据经 {@link StreamBridge} 发往 Spring Cloud Stream 出站 binding（Rabbit binder 等由配置决定）。
 */
@Slf4j
@Component
public class EquipRealtimeStreamOutbound {

    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    public static final String EQUIP_REALTIME_OUT_BINDING = "equipRealtime-out-0";

    public EquipRealtimeStreamOutbound(StreamBridge streamBridge,
                                       ObjectMapper objectMapper) {
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }

    public void send(EquipRealtime realtime) {
        if (realtime == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(realtime);
            streamBridge.send(EQUIP_REALTIME_OUT_BINDING, json);
        } catch (JsonProcessingException e) {
            log.error("Serialize equip realtime for stream outbound failed, equip={}", realtime.getSelfCode(), e);
            throw new IllegalStateException("equip realtime JSON serialization failed", e);
        }
    }
}
