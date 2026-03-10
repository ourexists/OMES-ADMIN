package com.ourexists.omes.device.core.equip.protocol;

public interface ProtocolManager {

    boolean start(ProtocolConnect connect);

    boolean stop(String connectId);

    void stopAll();

    String protocol();

    /**
     * 向设备写入控制值。
     *
     * @param connectId 网关连接 ID
     * @param address   PLC/设备 可写地址
     * @param value     写入值
     * @return 是否写入成功
     */
    default boolean write(String connectId, String address, Object value) {
        throw new UnsupportedOperationException(protocol() + " does not support write");
    }
}
