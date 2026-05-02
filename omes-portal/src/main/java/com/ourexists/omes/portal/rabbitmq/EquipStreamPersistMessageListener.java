package com.ourexists.omes.portal.rabbitmq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.feign.*;
import com.ourexists.omes.device.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消费 omes-stream Flink 桥接队列中的 JSON，按类型调用 device 服务入库（与原先门户侧定时/直连 PG 等价路径）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipStreamPersistMessageListener {

    private static final String TYPE_CHANGE = "change";
    private static final String TYPE_STATE_SNAPSHOT = "state_snapshot";
    private static final String TYPE_COLLECT_SNAPSHOT = "collect_snapshot";

    private final ObjectMapper objectMapper;
    private final EquipStateSnapshotFeign equipStateSnapshotFeign;
    private final EquipCollectFeign equipCollectFeign;
    private final EquipStreamPersistChangeAggregator equipStreamPersistChangeAggregator;

    @RabbitListener(queues = "${omes.device.rabbitmq.equip-stream-persist-queue:omes.equip.stream.persist}")
    public void onStreamPersist(String body) {
        try {
            UserContext.defaultTenant();
            JsonNode root = objectMapper.readTree(body);
            String type = root.path("type").asText("");
            switch (type) {
                case TYPE_CHANGE:
                    equipStreamPersistChangeAggregator.offer(root);
                    break;
                case TYPE_STATE_SNAPSHOT:
                    handleStateSnapshot(root);
                    break;
                case TYPE_COLLECT_SNAPSHOT:
                    handleCollectSnapshot(root);
                    break;
                default:
                    log.warn("Equip stream persist MQ: unknown type={}, payload={}", type, body);
            }
        } catch (Exception e) {
            log.error("Equip stream persist MQ: handle failed, payload={}", body, e);
        }
    }

    private void handleStateSnapshot(JsonNode root) throws Exception {
        EquipStateSnapshotDto dto = objectMapper.treeToValue(root.get("snapshot"), EquipStateSnapshotDto.class);
        RemoteHandleUtils.getDataFormResponse(equipStateSnapshotFeign.add(dto));
    }

    private void handleCollectSnapshot(JsonNode root) throws Exception {
        EquipCollectDto dto = objectMapper.treeToValue(root.get("collect"), EquipCollectDto.class);
        RemoteHandleUtils.getDataFormResponse(equipCollectFeign.save(dto));
    }
}
