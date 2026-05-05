package com.ourexists.omes.stream.equip.sink.bridge;

/** 与门户持久化桥接消息 JSON 字段 {@code type} 约定一致（各队列仅含对应 type，字段仍保留便于排查）。 */
public final class EquipStreamPersistTypes {

    public static final String CHANGE = "change";
    public static final String STATE_SNAPSHOT = "state_snapshot";
    public static final String COLLECT_SNAPSHOT = "collect_snapshot";

    private EquipStreamPersistTypes() {
    }
}
