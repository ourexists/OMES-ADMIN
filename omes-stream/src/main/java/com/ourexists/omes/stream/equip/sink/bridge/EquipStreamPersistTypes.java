package com.ourexists.omes.stream.equip.sink.bridge;

/** 与门户 {@code EquipStreamPersistMessageListener} 约定的 JSON 字段 {@code type}。 */
public final class EquipStreamPersistTypes {

    public static final String CHANGE = "change";
    public static final String STATE_SNAPSHOT = "state_snapshot";
    public static final String COLLECT_SNAPSHOT = "collect_snapshot";

    private EquipStreamPersistTypes() {
    }
}
