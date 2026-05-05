package com.ourexists.omes.stream.equip.sink.bridge;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.model.EquipRecordAlarmDto;
import com.ourexists.omes.device.model.EquipRecordOnlineDto;
import com.ourexists.omes.device.model.EquipRecordRunDto;
import com.ourexists.omes.stream.equip.model.EquipRealtimeChangeEvent;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.stream.Collectors;

/**
 * 设备运行/报警/在线记录变更 → MQ，由门户调 {@code EquipRecord*Feign#add} 入库。
 */
public class EquipRecordChangeBridgeSink extends AbstractEquipStreamPersistRmqSink<EquipRealtimeChangeEvent> {

    public EquipRecordChangeBridgeSink(RMQConnectionConfig rmqConnectionConfig, String queueName) {
        super(rmqConnectionConfig, queueName);
    }

    @Override
    public void invoke(EquipRealtimeChangeEvent event, SinkFunction.Context context) throws Exception {
        if (event == null || objectMapper == null) {
            return;
        }
        EquipRealtime source = event.getSource();
        EquipRealtime target = event.getTarget();
        if (source == null || target == null) {
            return;
        }
        if (!event.isAlarmChanged() && !event.isRunChanged() && !event.isOnlineChanged()) {
            return;
        }
        Date start = new Date();
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", EquipStreamPersistTypes.CHANGE);
        if (event.isAlarmChanged()) {
            String reason = CollectionUtils.isEmpty(target.getAlarmTexts())
                    ? null
                    : target.getAlarmTexts().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(","));
            EquipRecordAlarmDto alarm = new EquipRecordAlarmDto()
                    .setSn(source.getSelfCode())
                    .setState(target.getAlarmState())
                    .setStartTime(start)
                    .setTenantId(source.getTenantId())
                    .setReason(reason)
                    .setLevel(target.getAlarmLevel())
                    .setEventId(event.getAlarmSegmentEventId())
                    .setPrevEventId(event.getAlarmPrevSegmentEventId());
            root.set("alarm", objectMapper.valueToTree(alarm));
        }
        if (event.isRunChanged()) {
            EquipRecordRunDto run = new EquipRecordRunDto()
                    .setSn(source.getSelfCode())
                    .setState(target.getRunState())
                    .setStartTime(start)
                    .setTenantId(source.getTenantId())
                    .setEventId(event.getRunSegmentEventId())
                    .setPrevEventId(event.getRunPrevSegmentEventId());
            root.set("run", objectMapper.valueToTree(run));
        }
        if (event.isOnlineChanged()) {
            EquipRecordOnlineDto online = new EquipRecordOnlineDto()
                    .setSn(source.getSelfCode())
                    .setState(target.getOnlineState())
                    .setStartTime(start)
                    .setTenantId(source.getTenantId())
                    .setEventId(event.getOnlineSegmentEventId())
                    .setPrevEventId(event.getOnlinePrevSegmentEventId());
            root.set("online", objectMapper.valueToTree(online));
        }
        // 供门户 equipStreamPersistChange 队列经 Aggregator 写 Redis（与门户 DEquipRealtimeManager 一致）
        root.set("realtime", objectMapper.valueToTree(target));
        publish(objectMapper.writeValueAsBytes(root));
    }
}
