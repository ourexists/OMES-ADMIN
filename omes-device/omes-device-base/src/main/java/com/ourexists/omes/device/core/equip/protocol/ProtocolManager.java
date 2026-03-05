package com.ourexists.omes.device.core.equip.protocol;

public interface ProtocolManager {

    boolean start(ProtocolConnect connect);

    boolean stop(String connectId);

    void stopAll();

    String protocol();
}
