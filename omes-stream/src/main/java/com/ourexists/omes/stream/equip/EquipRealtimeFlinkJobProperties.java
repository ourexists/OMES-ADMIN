package com.ourexists.omes.stream.equip;

import org.apache.flink.api.java.utils.ParameterTool;

final class EquipRealtimeFlinkJobProperties {

    private EquipRealtimeFlinkJobProperties() {
    }

    static EquipRealtimeFlinkJobConfig from(ParameterTool pt) {
        return new EquipRealtimeFlinkJobConfig(
                str(pt, "omes.device.rabbitmq.equip-realtime-queue", "OMES_EQUIP_REALTIME_QUEUE", "omes.equip.realtime"),
                str(pt, "omes.device.rabbitmq.equip-notify-create-queue", "OMES_EQUIP_NOTIFY_CREATE_QUEUE", "omes.notify.create"),
                str(
                        pt,
                        "omes.device.rabbitmq.equip-stream-persist-change-queue",
                        "OMES_EQUIP_STREAM_PERSIST_CHANGE_QUEUE",
                        "omes.equip.stream.persist.change"),
                str(
                        pt,
                        "omes.device.rabbitmq.equip-stream-persist-state-queue",
                        "OMES_EQUIP_STREAM_PERSIST_STATE_QUEUE",
                        "omes.equip.stream.persist.state"),
                str(
                        pt,
                        "omes.device.rabbitmq.equip-stream-persist-collect-queue",
                        "OMES_EQUIP_STREAM_PERSIST_COLLECT_QUEUE",
                        "omes.equip.stream.persist.collect"),
                pt.getLong(
                        "omes.device.flink.checkpoint-interval-ms",
                        pt.getLong("OMES_FLINK_RMQ_CHECKPOINT_MS", pt.getLong("OMES_FLINK_CHECKPOINT_INTERVAL_MS", 10_000L))),
                pt.getBoolean(
                        "omes.device.flink.enable-checkpointing",
                        pt.getBoolean("OMES_FLINK_ENABLE_CHECKPOINTING", false)),
                pt.getLong(
                        "omes.device.flink.checkpoint-timeout-ms",
                        pt.getLong("OMES_FLINK_CHECKPOINT_TIMEOUT_MS", 120_000L)),
                pt.getInt("omes.device.flink.rmq-prefetch", pt.getInt("OMES_FLINK_RMQ_PREFETCH", 100)),
                pt.getLong("omes.device.offline-timeout-ms", pt.getLong("OMES_EQUIP_OFFLINE_TIMEOUT_MS", 90_000L)),
                pt.getLong("omes.device.snapshot-interval-ms", pt.getLong("OMES_EQUIP_SNAPSHOT_INTERVAL_MS", 30_000L)),
                pt.getLong("omes.device.attr-fluctuation-window-ms", pt.getLong("OMES_EQUIP_ATTR_FLUCTUATION_WINDOW_MS", 90_000L)),
                pt.getLong("omes.device.attr-fluctuation-slide-ms", pt.getLong("OMES_EQUIP_ATTR_FLUCTUATION_SLIDE_MS", 5_000L)));
    }

    private static String str(ParameterTool pt, String dottedKey, String envKey, String defaultVal) {
        if (pt.has(dottedKey)) {
            return pt.get(dottedKey);
        }
        if (pt.has(envKey)) {
            return pt.get(envKey);
        }
        return defaultVal;
    }
}
