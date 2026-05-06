package com.ourexists.omes.stream.workshop;

public final class WorkshopRealtimeFlinkJobConfig {
    private final String workshopRealtimeRabbitQueue;
    private final String workshopStreamPersistCollectQueue;
    private final long flinkCheckpointIntervalMs;
    private final boolean flinkEnableCheckpointing;
    private final long flinkCheckpointTimeoutMs;
    private final boolean flinkUnalignedCheckpoint;
    private final long flinkAlignedCheckpointTimeoutMs;
    private final int flinkRmqPrefetch;
    private final long snapshotIntervalMs;
    private final long stateTtlMinutesCollectSnapshot;

    public WorkshopRealtimeFlinkJobConfig(
            String workshopRealtimeRabbitQueue,
            String workshopStreamPersistCollectQueue,
            long flinkCheckpointIntervalMs,
            boolean flinkEnableCheckpointing,
            long flinkCheckpointTimeoutMs,
            boolean flinkUnalignedCheckpoint,
            long flinkAlignedCheckpointTimeoutMs,
            int flinkRmqPrefetch,
            long snapshotIntervalMs,
            long stateTtlMinutesCollectSnapshot) {
        this.workshopRealtimeRabbitQueue = workshopRealtimeRabbitQueue;
        this.workshopStreamPersistCollectQueue = workshopStreamPersistCollectQueue;
        this.flinkCheckpointIntervalMs = flinkCheckpointIntervalMs;
        this.flinkEnableCheckpointing = flinkEnableCheckpointing;
        this.flinkCheckpointTimeoutMs = flinkCheckpointTimeoutMs;
        this.flinkUnalignedCheckpoint = flinkUnalignedCheckpoint;
        this.flinkAlignedCheckpointTimeoutMs = flinkAlignedCheckpointTimeoutMs;
        this.flinkRmqPrefetch = flinkRmqPrefetch;
        this.snapshotIntervalMs = snapshotIntervalMs;
        this.stateTtlMinutesCollectSnapshot = stateTtlMinutesCollectSnapshot;
    }

    public String workshopRealtimeRabbitQueue() {
        return workshopRealtimeRabbitQueue;
    }

    public String workshopStreamPersistCollectQueue() {
        return workshopStreamPersistCollectQueue;
    }

    public long flinkCheckpointIntervalMs() {
        return flinkCheckpointIntervalMs;
    }

    public boolean flinkEnableCheckpointing() {
        return flinkEnableCheckpointing;
    }

    public long flinkCheckpointTimeoutMs() {
        return flinkCheckpointTimeoutMs;
    }

    public boolean flinkUnalignedCheckpoint() {
        return flinkUnalignedCheckpoint;
    }

    public long flinkAlignedCheckpointTimeoutMs() {
        return flinkAlignedCheckpointTimeoutMs;
    }

    public int flinkRmqPrefetch() {
        return flinkRmqPrefetch;
    }

    public long snapshotIntervalMs() {
        return snapshotIntervalMs;
    }

    public long stateTtlMinutesCollectSnapshot() {
        return stateTtlMinutesCollectSnapshot;
    }
}
