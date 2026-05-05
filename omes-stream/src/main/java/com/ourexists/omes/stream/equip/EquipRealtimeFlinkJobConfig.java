package com.ourexists.omes.stream.equip;

/** Configuration for the equip realtime Flink job ({@code flink run} / cluster). */
public final class EquipRealtimeFlinkJobConfig {
    private final String equipRealtimeRabbitQueue;
    private final String equipNotifyCreateQueue;
    private final String equipStreamPersistChangeQueue;
    private final String equipStreamPersistStateQueue;
    private final String equipStreamPersistCollectQueue;
    private final long flinkRmqCheckpointIntervalMs;
    private final boolean flinkEnableCheckpointing;
    private final long flinkCheckpointTimeoutMs;
    private final int flinkRmqPrefetch;
    private final long offlineTimeoutMs;
    private final long snapshotIntervalMs;
    private final long attrFluctuationWindowMs;
    private final long attrFluctuationSlideMs;
    private final int parallelism;

    public EquipRealtimeFlinkJobConfig(
            String equipRealtimeRabbitQueue,
            String equipNotifyCreateQueue,
            String equipStreamPersistChangeQueue,
            String equipStreamPersistStateQueue,
            String equipStreamPersistCollectQueue,
            long flinkRmqCheckpointIntervalMs,
            boolean flinkEnableCheckpointing,
            long flinkCheckpointTimeoutMs,
            int flinkRmqPrefetch,
            long offlineTimeoutMs,
            long snapshotIntervalMs,
            long attrFluctuationWindowMs,
            long attrFluctuationSlideMs,
            int parallelism) {
        this.equipRealtimeRabbitQueue = equipRealtimeRabbitQueue;
        this.equipNotifyCreateQueue = equipNotifyCreateQueue;
        this.equipStreamPersistChangeQueue = equipStreamPersistChangeQueue;
        this.equipStreamPersistStateQueue = equipStreamPersistStateQueue;
        this.equipStreamPersistCollectQueue = equipStreamPersistCollectQueue;
        this.flinkRmqCheckpointIntervalMs = flinkRmqCheckpointIntervalMs;
        this.flinkEnableCheckpointing = flinkEnableCheckpointing;
        this.flinkCheckpointTimeoutMs = flinkCheckpointTimeoutMs;
        this.flinkRmqPrefetch = flinkRmqPrefetch;
        this.offlineTimeoutMs = offlineTimeoutMs;
        this.snapshotIntervalMs = snapshotIntervalMs;
        this.attrFluctuationWindowMs = attrFluctuationWindowMs;
        this.attrFluctuationSlideMs = attrFluctuationSlideMs;
        this.parallelism = parallelism;
    }

    public String equipRealtimeRabbitQueue() {
        return equipRealtimeRabbitQueue;
    }

    public String equipNotifyCreateQueue() {
        return equipNotifyCreateQueue;
    }

    public String equipStreamPersistChangeQueue() {
        return equipStreamPersistChangeQueue;
    }

    public String equipStreamPersistStateQueue() {
        return equipStreamPersistStateQueue;
    }

    public String equipStreamPersistCollectQueue() {
        return equipStreamPersistCollectQueue;
    }

    public long flinkRmqCheckpointIntervalMs() {
        return flinkRmqCheckpointIntervalMs;
    }

    public boolean flinkEnableCheckpointing() {
        return flinkEnableCheckpointing;
    }

    public long flinkCheckpointTimeoutMs() {
        return flinkCheckpointTimeoutMs;
    }

    public int flinkRmqPrefetch() {
        return flinkRmqPrefetch;
    }

    public long offlineTimeoutMs() {
        return offlineTimeoutMs;
    }

    public long snapshotIntervalMs() {
        return snapshotIntervalMs;
    }

    public long attrFluctuationWindowMs() {
        return attrFluctuationWindowMs;
    }

    public long attrFluctuationSlideMs() {
        return attrFluctuationSlideMs;
    }

    public int parallelism() {
        return parallelism;
    }
}
